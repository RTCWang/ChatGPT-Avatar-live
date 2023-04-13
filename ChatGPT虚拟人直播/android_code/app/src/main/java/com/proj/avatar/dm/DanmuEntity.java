package com.proj.avatar.dm;


/**
 * Created by Administrator on 2017/3/30.
 */

public class DanmuEntity extends Model {
    public String content;
    public int textColor;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
