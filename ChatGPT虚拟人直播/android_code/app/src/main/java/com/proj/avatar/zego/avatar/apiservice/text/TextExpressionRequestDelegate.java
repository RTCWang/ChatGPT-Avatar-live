package  com.proj.avatar.zego.avatar.apiservice.text;

/**
 * 文本驱动的http请求代理，用户可自定义实现
 */
public abstract class TextExpressionRequestDelegate {

    /**
     * 请求
     * @param textBlock
     * @return TextExpressionAudioData 结果
     */
    public abstract TextExpressionAudioData request(String textBlock);
}
