package  com.proj.avatar.zego.avatar.apiservice.text;

/**
 *  启动文本驱动回调
 */
public interface ITextExpressionCallback {
    /**
     * 文本驱动播放启动时，回调
     */
    void onStart();

    /**
     * 文本驱动播放出错时，回调
     * @param errorCode
     */
    void onError(int errorCode, String msg);

    /**
     * 文本驱动播放结束时，回调
     */
    void onEnd();
}
