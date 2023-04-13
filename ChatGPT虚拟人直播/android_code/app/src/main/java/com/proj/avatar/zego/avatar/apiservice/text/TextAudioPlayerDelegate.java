package  com.proj.avatar.zego.avatar.apiservice.text;

/**
 * 文本驱动的声音播放代理，用户自定义实现
 */
public abstract class TextAudioPlayerDelegate {

    /**
     * 开始播放
     */
     public abstract void start();

    /**
     * 播放数据
     * @param audioData
     * @param offsetInBytes
     * @param sizeInBytes
     */
     public abstract void sendData(byte[] audioData, int offsetInBytes, int sizeInBytes) ;


    /**
     * 停止播放
     */
     public abstract void stop();
}
