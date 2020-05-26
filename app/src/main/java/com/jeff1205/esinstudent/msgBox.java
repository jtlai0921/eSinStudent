package com.jeff1205.esinstudent;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class msgBox {
    private static String resultValue;

    public static void getDialogOK(Context context,String Title,String Message,String OkText) {
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
            throw new RuntimeException();
            }
        };

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(Title);
        alert.setMessage(Message);
        alert.setPositiveButton(OkText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            resultValue = "Y";
            handler.sendMessage(handler.obtainMessage());
            }
        });

        alert.show();

        try{ Looper.loop(); }
        catch(RuntimeException e){}

    }

    public static String getDialogYesNo(Context context,String Title,String Message,String YesText,String NoText) {
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
            throw new RuntimeException();
            }
        };

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(Title);
        alert.setMessage(Message);
        alert.setPositiveButton(YesText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            resultValue = "Y";
            handler.sendMessage(handler.obtainMessage());
            }
        });

        alert.setNegativeButton(NoText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            resultValue = "N";
            handler.sendMessage(handler.obtainMessage());
            }
        });

        alert.show();

        try{ Looper.loop(); }
        catch(RuntimeException e){}

        return resultValue;
    }
}
