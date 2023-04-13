package  com.proj.avatar.zego.avatar.apiservice.text;

/**
 * 文本驱动相关定义等
 */
public class TextConstants {

    /**
     * ZegoText ERROR Code
     */
    public static final class ErrorCode {

        public static final int Succeed = 0; // 成功

        // 参数错误
        public static final int ErrInvalidParameters  = 4;  // 错误的参数
        public static final int ErrAppIdNotAvailable  = 5;  // appid 不可用
        public static final int ErrInvalidSign        = 6;  // 非法签名
        public static final int ErrFailedCheckAppIdAndSign = 8;  //appid校验失败，请检查签名。
        public static final int ErrTimeoutSign        = 9;  //签名过期，请重新生成
        public static final int ErrInvalidTimestamp   = 10; // 不是毫秒级时间戳

        public static final int ErrInvalidTextLen     = 10001; // 文本过长
        public static final int ErrInvalidTextUTF8    = 10002; //传入文本非utf8编码
        public static final int ErrInvalidVoiceType   = 10003; // voice_type非法，请参考音色列表
        public static final int ErrInvalidVolume      = 10005; // 参数volume音量错误
        public static final int ErrInvalidSpeed       = 10006; // 参数speed语速错误
        public static final int ErrIllegalText        = 20001; //请求文本含有非法字符，或请求文本没有有效字符

        //ZegoTextAPI终端逻辑错误码
        public static final int ErrNoCharacter = 90004; //没有character
        public static final int ErrEmptyText = 90005; //空文本
        public static final int ErrLongText = 90006;  //文本太长
        //android终端专用
        public static final int ErrNoRequestDelegate = 90002;//没有requestDelegate进行网络请求
        public static final int ErrNoAudioPlayerDelegate = 90003;//没有AudioPlayerDelegate进行声音播放
        public static final int ErrState = 90007; // 状态错误
        public static final int ErrNoRequestData = 90008; //网络请求返回为null
        public static final int ErrNoAudioData = 90009; //网络请求返回数据没有声音内容
    }

    //表情个数
    public static final int EXP_COUNT = 55;

    //api字数输入上限制
    public static final int MAX_INPUT_TEXT_BLOCK_SIZE = 1000;

    //采样率
    public static final int SAMPLE_RATE = 16000;

    //采样位数
    public static final int SAMPLE_BYTES = 2;

    //fps
    public static final int SAMPLE_FPS = 30;

    //timer 间隔ms
    public static final int TIMER_INTERVAL = 10;

    //文本cache个数
    public static final int DATA_CACHE_COUNT = 10;

    //文本请求尝试次数
    public static final int DATA_REQUEST_COUNT = 3;

    //分割文本阈值， > SPLIT_CHAR_COUNT
    public static int SPLIT_CHAR_COUNT = 50;
}
