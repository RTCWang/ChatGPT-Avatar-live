package  com.proj.avatar.zego.avatar.apiservice.text;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TextExpressionAudioData {
    public int code = -1;
    public String message = "";
    public String uniqueID = "";
    public String audioData = "";
    public List<Integer[]> expressionList = new ArrayList<>();

    public byte[] audioPcmData = null;
    public int fps = 30;
    public int sampleRate = 16000;
    public String codec = "pcm";

    //是否处理完毕
    public boolean processComplete = false;
    //开始时间戳
    public long startTimeStamp = 0;
    //预留seq序列id
    public int seq_id = -1;
}
