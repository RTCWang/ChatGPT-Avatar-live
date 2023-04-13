package com.proj.avatar.zego.avatar.apiservice.text.internal;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.proj.avatar.zego.avatar.apiservice.text.TextConstants;


/**
 * 内置的TextAudioPlayerDelegate
 */
public class ZegoTextAudioPlayerDelegate {

    private AudioTrack mAudioTrack = null;

    private final static String TAG = "ZegoTextAudioPlayerDelegate";

    private final static int AUDIO_SAMPLE_RATE = TextConstants.SAMPLE_RATE;

    //设置采样精度，将采样的数据以PCM进行编码，每次采集的数据位宽为16bit。
    private final static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private int bufferSizeOutBytes;

    public void start() {

        bufferSizeOutBytes = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AUDIO_FORMAT);
        if(mAudioTrack == null) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AUDIO_FORMAT, bufferSizeOutBytes, AudioTrack.MODE_STREAM);
        }
        mAudioTrack.play();
    }

    public void sendData(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if(mAudioTrack != null){
            mAudioTrack.write(audioData, offsetInBytes, sizeInBytes);
        }
    }

    public void stop() {

        if (mAudioTrack != null) {

            mAudioTrack.stop();
            mAudioTrack.flush();
            mAudioTrack.release();
            mAudioTrack = null;
        }

    }
}

