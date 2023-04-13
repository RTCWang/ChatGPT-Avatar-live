package com.proj.avatar.view;

import android.content.Context;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;

public class ShowUtils {
    public interface OnClickOkCancelListener {
        void onOk();

        void onCancel();
    }

    public interface OnClickOkListener {
        void onOk();
    }

    public static void alert(Context ctx, String title, String msg, OnClickOkListener onOkListener) {
        AlertView alert = new AlertView(title, msg, "确定",
                null, null, ctx, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (onOkListener != null) onOkListener.onOk();
            }
        });
        alert.show();
    }

    public static void comfirm(Context ctx, String title, String msg, String ok, OnClickOkCancelListener clickListener) {
        AlertView alert = new AlertView(title, msg, "取消",
                new String[]{ok}, null, ctx, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (clickListener == null) return;
                if (position == 0) {
                    clickListener.onOk();
                } else {//position = -1
                    clickListener.onCancel();
                }
            }
        });
        alert.show();
    }
}
