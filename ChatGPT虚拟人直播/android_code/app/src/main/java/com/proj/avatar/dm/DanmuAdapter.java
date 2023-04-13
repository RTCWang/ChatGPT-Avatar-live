package com.proj.avatar.dm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

//import com.orzangleli.xdanmuku.XAdapter;
import com.proj.avatar.R;

import java.util.Random;

/**
 * Created by Administrator on 2017/4/17.
 */

public class DanmuAdapter extends XAdapter<DanmuEntity> {

    Random random;


    private Context context;

    public DanmuAdapter(Context c) {
        super();
        context = c;
        random = new Random();
    }

    @Override
    public View getView(DanmuEntity danmuEntity, View convertView) {

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_danmu, null);
            holder = new ViewHolder();
            holder.content = (TextView) convertView.findViewById(R.id.dmTxt);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.content.setText(danmuEntity.content);
        holder.content.setTextColor(danmuEntity.getTextColor());
        return convertView;
    }

    @Override
    public int[] getViewTypeArray() {
        int type[] = {0, 1};
        return type;
    }

    @Override
    public int getSingleLineHeight() {
//        return 50;
        //将所有类型弹幕的布局拿出来，找到高度最大值，作为弹道高度
        View view = LayoutInflater.from(context).inflate(R.layout.item_danmu, null);
        //指定行高
        view.measure(0, 0);

//        View view2 = LayoutInflater.from(context).inflate(R.layout.item_super_danmu, null);
//        //指定行高
//        view2.measure(0, 0);

//        return Math.max(view.getMeasuredHeight(),view2.getMeasuredHeight());
        return view.getMeasuredHeight();
    }


    class ViewHolder {
        public TextView content;
    }


}
