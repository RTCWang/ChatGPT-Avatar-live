package com.proj.avatar.dm;

import android.view.View;

/**
 * Created by Administrator on 2017/3/30.
 */

public abstract class DanmuConverter<M>{
    public abstract int getSingleLineHeight();
    public abstract View convert(M model);
}
