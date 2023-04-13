package com.proj.avatar.zego.avatar.apiservice.text;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.proj.avatar.zego.KeyCenter;
import com.zego.avatar.ZegoCharacter;
import com.zego.avatar.bean.ZegoExpression;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import com.proj.avatar.zego.avatar.apiservice.text.internal.ZegoTextAudioPlayerDelegate;
import com.proj.avatar.zego.avatar.apiservice.text.internal.ZegoTextExpressionRequestDelegate;

/**
 * 文本驱动 API
 */
public class ZegoTextAPI {

    private static String TAG = "ZegoTextAPI";

    private ZegoTextExpressionRequestDelegate mRequestDelegate = null;
    private ZegoTextAudioPlayerDelegate mAudioPlayerDelegate = null;

    private ArrayBlockingQueue<String> mTextBlockQueue = new ArrayBlockingQueue<String>(10000);
    private ArrayBlockingQueue<TextExpressionAudioData> mDataQueue = new ArrayBlockingQueue<TextExpressionAudioData>(10000);

    private ITextExpressionCallback mCallBack = null;

    private boolean mStart = false;
    private Timer timer = null;
    private TimerTask task = null;
    private Thread mRequestThread = null;
    private HandlerThread mAudioPlayThread = null;
    private Handler mAudioPlayHandler;

    private ZegoCharacter mCharacter = null;

    private int mFrameBytes = 0;//SAMPLE_RATE * SAMPLE_BYTES / SAMPLE_FPS;
    private float mFrameMs = 0f;

    private String[] mCharSplits = new String[]{".", ",", "?", "!", ";",
            "。", "，", "？", "！", "；", " "};

    public ZegoTextAPI(ZegoCharacter character){

        mFrameBytes = TextConstants.SAMPLE_RATE * TextConstants.SAMPLE_BYTES / TextConstants.SAMPLE_FPS;
        mFrameMs = 1000F/ TextConstants.SAMPLE_FPS;

        mRequestDelegate = new ZegoTextExpressionRequestDelegate();
        mRequestDelegate.setAppId(KeyCenter.APP_ID);
        mRequestDelegate.setAppSign(KeyCenter.APP_SIGN);
        mAudioPlayerDelegate = new ZegoTextAudioPlayerDelegate();

        mCharacter = character;
    }

    /**
     * 设置文本驱动回调
     * @param cb
     */
    public void setTextExpressionCallback(ITextExpressionCallback cb){
        mCallBack = cb;
    }

    /**
     * 开始文本驱动
     * @param txt
     */
    public synchronized void playTextExpression(String txt){


        if(mRequestDelegate == null){
            _doCallbackError(TextConstants.ErrorCode.ErrNoRequestDelegate, "not have request delegate");
            return ;
        }

        if(mAudioPlayerDelegate == null){
            _doCallbackError(TextConstants.ErrorCode.ErrNoAudioPlayerDelegate, "not have audio delegate");
            return ;
        }

        if(mCharacter == null){
            _doCallbackError(TextConstants.ErrorCode.ErrNoCharacter, "not have Character");
            return ;
        }

        //do 2， check txt
        if(txt == null || txt.length() <= 0){
            _doCallbackError(TextConstants.ErrorCode.ErrEmptyText, "error: empty text");
            return ;
        }

        if( txt.length() > TextConstants.MAX_INPUT_TEXT_BLOCK_SIZE){
            _doCallbackError(TextConstants.ErrorCode.ErrLongText, "text is too long , need < 1000");
            return ;
        }

        //do 4, 分包
        splitText(txt);

        if(!mStart) {
            mStart = true;
            if(mCallBack != null){
                mCallBack.onStart();
            }

            startRequestThread();
            startAudioHandlerThread();

            if (mAudioPlayerDelegate != null)
                mAudioPlayerDelegate.start();

            if (timer == null) {
                timer = new Timer();
            }
            if (task == null) {
                task = new TimerTask() {

                    TextExpressionAudioData mCurData = null;

                    @Override
                    public void run() {

//                        Log.d(TAG, "timer task tick: " + System.currentTimeMillis());
                        if(!mStart)
                            return;

                        //do 1, 如果当前数据播放完毕， 就从队列获取新数据，进行语音播报
                        if(mCurData == null || mCurData.processComplete){
                            TextExpressionAudioData data = mDataQueue.poll();
                            if(data != null && data.code == 0) {
                                mCurData = data;
                                if ( mCurData.audioPcmData != null && mCurData.audioPcmData.length > 0) {
                                    Message msg = Message.obtain();
                                    msg.what = 2;
                                    msg.obj = data;
                                    mAudioPlayHandler.sendMessage(msg);
                                }
                            }
                        }

                        //do 2有语音播放开始， 并没完毕， 寻找并播放表情
                        if (mCurData != null && !mCurData.processComplete && mCurData.startTimeStamp > 0) {

                            if(!processAudioExpression(mCurData, mCurData.startTimeStamp)){
                                Log.e(TAG, "processAudioExpression error, skip, mCurData.processComplete = true");
                                mCurData.processComplete = true;
                            }else{
//                                Log.d(TAG, "processAudioExpression ok");
                            }
                            if(mCurData.processComplete && mDataQueue.size() <= 0) {
                                mCharacter.setExpression(new ZegoExpression());
                            }
                        }
                    }
                };
                timer.schedule(task, 0, TextConstants.TIMER_INTERVAL);
            }
        }
    }

    /**
     * 停止文本驱动
     */
    public synchronized void stopTextExpression(){

        //do 1,  check playState
        if(!mStart){
            _doCallbackError(TextConstants.ErrorCode.ErrState, "stopped, no need stop");
            Log.e(TAG, "stopTextExpression error : is not start");
            return ;
        }

//        Log.d(TAG, "stopTextExpression AudioPlayerDelegate.stop begin ");
        if(mAudioPlayerDelegate != null) {
            mAudioPlayerDelegate.stop();
        }
//        Log.d(TAG, "stopTextExpression AudioPlayerDelegate.stop over ");

        mStart = false;
        stopAudioHandlerThread();
        mRequestThread.interrupt();
        mDataQueue.clear();

        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        mTextBlockQueue.clear();
        mDataQueue.clear();
        mCharacter.setExpression(new ZegoExpression());

        if(mCallBack != null){
            mCallBack.onEnd();
        }

        Log.e(TAG, "stopTextExpression over ");
    }

    /**
     * 设置声音
     * @param volume
     */
    public void setVolume(double volume) {
        if(mRequestDelegate != null){
            mRequestDelegate.setVolume(volume);
        }
    }

    /**
     * 设置速度
     * @param speed
     */
    public void setSpeed(double speed) {
        if(mRequestDelegate != null){
            mRequestDelegate.setSpeed(speed);
        }
    }

    /**
     * 设置音色
     * @param voice_type
     */
    public void setVoiceType(int voice_type) {
        if(mRequestDelegate != null){
            mRequestDelegate.setVoiceType(voice_type);
        }
    }

    /**
     * 设置语言
     * @param primary_language
     */
    public void setPrimaryLanguage(int primary_language) {
        if(mRequestDelegate != null){
            mRequestDelegate.setPrimaryLanguage(primary_language);
        }
    }

    private void startAudioHandlerThread(){
        mAudioPlayThread = new HandlerThread("audio play");
        mAudioPlayThread.start();
        mAudioPlayHandler = new Handler(mAudioPlayThread.getLooper()){

            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == 2){
                    if(mAudioPlayerDelegate != null) {
                        TextExpressionAudioData data = (TextExpressionAudioData)msg.obj;
                        data.startTimeStamp = System.currentTimeMillis();
                        mAudioPlayerDelegate.sendData(data.audioPcmData, 0, data.audioPcmData.length);
//                        Log.d(TAG, "mAudioPlayerDelegate play thread over");
                    }
                }
                super.handleMessage(msg);
            }
        };
    }

    public void stopAudioHandlerThread(){
        if(mAudioPlayThread != null) {
            Log.d(TAG,"quit mAudioPlayThread");
            mAudioPlayHandler.removeCallbacksAndMessages(null);
            mAudioPlayThread.getLooper().quit();
            try {
                mAudioPlayThread.join();
            }catch (Exception e){
                e.printStackTrace();
            }
            mAudioPlayHandler = null;
            mAudioPlayThread = null;
        }
    }

    private void startRequestThread(){
        mRequestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "mRequestThread start");

                while (mStart){
                    try {
                        if(mDataQueue.size() < TextConstants.DATA_CACHE_COUNT){
//                            Log.d(TAG, "mRequestThread request start");
                            //非阻塞， 获取文本块，请求网络。 结果push队列中
                            String txt = mTextBlockQueue.poll();
                            if(txt != null) {
                                for (int i = 0; i < TextConstants.DATA_REQUEST_COUNT; i++) {
                                    TextExpressionAudioData data = mRequestDelegate.request(txt);
                                    if (data == null) {
                                        Log.e(TAG, "网路请求错误");
                                        _doCallbackError(TextConstants.ErrorCode.ErrNoRequestData, "net request data is null");
                                        continue;
                                    }

                                    if (data.code == 0) {
                                        if (data.audioData.length() > 0) {
                                            //转pcm
                                            data.audioPcmData = toPcm(data.audioData);
                                            if (!mDataQueue.offer(data)) {
                                                Log.e(TAG, "语音表情队列 push出错");
                                            }
                                        } else {
                                            Log.e(TAG, "回包没有声音数据");
                                            _doCallbackError(TextConstants.ErrorCode.ErrNoAudioData, "data not have audio part");
                                        }
                                        break;
                                    } else {
                                        Log.e(TAG, "回包数据无效 code = " + data.code + ", msg = " + data.message);
                                        _doCallbackError(data.code, data.message);
                                    }
                                }
                            }else{
                                Thread.sleep(200);
                            }
                        }else{
//                            Log.d(TAG, "mRequestThread request sleep");
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "mRequestThread exit");
            }
        });
        mRequestThread.start();

    }

    //audio base64 转为 pcm
    private byte[] toPcm(String audioBase64){
        return Base64.decode(audioBase64, Base64.NO_WRAP);
    }

    private int getStartSplitIndex(String word, int start){

        int retIndex = -1;
        boolean first = true;

        for(String c : mCharSplits){
            int index = word.indexOf(c, start);
            if(index > 0 ) {
                if(first){
                    retIndex = index;
                    first = false;
                }else{
                    retIndex = Math.min(retIndex, index);
                }

            }
        }
        if(retIndex > 0) {
            Log.d(TAG, "split word " + word.substring(start, retIndex + 1));
        }
        return retIndex;
    }

    private int getEnoughWord(String word, int start){

       int startIndex = start;
       while(true){

           int retIndex = getStartSplitIndex(word, startIndex);
           if(retIndex < 0){ //-1
               return retIndex;
           }else{
                if(retIndex - start < TextConstants.SPLIT_CHAR_COUNT && retIndex < word.length() - 1){
                    startIndex = retIndex + 1;
                    continue;
                }else{
                    return retIndex;
                }
           }
       }
    }

    //文本分包
    private void splitText(String txt){

        int startIndex = 0;
        while (true){
            int retIndex = getEnoughWord(txt, startIndex);
            if(retIndex > 0 && retIndex > startIndex){
                try {
                    String result = txt.substring(startIndex , retIndex + 1);
                    mTextBlockQueue.offer(result);
                    Log.d(TAG,"get valid text at center: " + result.length() + " : " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(retIndex >= txt.length() - 1){
                    return;
                }
                startIndex = retIndex + 1;
                continue;

            }else if(retIndex == -1){
                if(startIndex < txt.length()) {
                    try {
                        String result = txt.substring(startIndex);
                        mTextBlockQueue.put(result);
                        Log.d(TAG,"get valid text at end : " + result.length() + " : " + result);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return;

            }else{
                if(retIndex >= txt.length() - 1){
                    return;
                }
                startIndex = retIndex + 1;
                continue;
            }

        }
    }

    //表情转换
    private void toExpression(TextExpressionAudioData data, int index, ZegoExpression expression){
        Integer[] exp = data.expressionList.get(index);
        expression.angleX = (float)exp[0] / 100f;
        expression.angleY = (float)exp[1] / 100f;
        expression.angleZ = (float)exp[2] / 100f;
        for(int i = 3; i < TextConstants.EXP_COUNT ; i++){
            expression.expressions[i - 3] = (float)exp[i] / 100f;
        }
        expression.timeStamp = System.currentTimeMillis();
    }

    private boolean processAudioExpression(TextExpressionAudioData data, long start){
        if(data == null){
            return false;
        }

        if(data.processComplete){
            Log.e(TAG, "processAudioExpression data is processComplete" );
            return false;
        }

        if(data.expressionList.size() <= 0){
            Log.e(TAG, "processAudioExpression data.expressionList.size() <= 0" );
            return false;
        }

        if(data.audioPcmData.length <= 0){
            Log.e(TAG, "processAudioExpression data.audioPcmData.length <= 0" );
            return false;
        }

        long durationMs = data.audioPcmData.length * 1000L / (TextConstants.SAMPLE_RATE * TextConstants.SAMPLE_BYTES);
        long curPlayTimeMs = System.currentTimeMillis() - start;

        if(curPlayTimeMs >= durationMs){
            //超过播放时长
            ZegoExpression expression = new ZegoExpression();
            toExpression(data, data.expressionList.size() - 1, expression);
            mCharacter.setExpression(expression);
            data.processComplete = true;
            Log.d(TAG, "processAudioExpression curPlayTimeMs >= durationMs， set last expression");

        }else{

            int frame_index = (int) (curPlayTimeMs / mFrameMs);
            if(frame_index < data.expressionList.size() - 1){
                //中间表情
                ZegoExpression expression = new ZegoExpression();
                toExpression(data, frame_index, expression);
                mCharacter.setExpression(expression);
//                Log.d(TAG, "processAudioExpression set expression index: " + frame_index);
            }else{
                //结尾表情
                ZegoExpression expression = new ZegoExpression();
                toExpression(data, data.expressionList.size() - 1, expression);
                data.processComplete = true;
                mCharacter.setExpression(expression);
                Log.d(TAG, "processAudioExpression set last expression index: " + (data.expressionList.size() - 1));
            }
        }

        return true;
    }

    private void _doCallbackError(int errorCode, String msg){
        if(mCallBack != null){
            mCallBack.onError(errorCode, msg);
        }
    }
}
