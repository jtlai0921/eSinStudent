package com.jeff1205.esinstudent;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignList extends AppCompatActivity {
    private String POSTresult;

    public void ShowSignList (TableLayout tblSign,String mClassId,String mDate) {
        tblSign.removeAllViews();   // 每次按就先清空 table
        getPostData(mClassId,mDate);

        // 表頭
        SignListHeader(tblSign);

        try {   // JSON 要求要 try, catch
            JSONArray jsonArray = new JSONArray(POSTresult);
            // 表身明細
            SignListDetail(tblSign, jsonArray);

        } catch (Exception e) {
            Log.e("log_tag", e.toString());
        }
        //}
    }

    private void getPostData(String mClassId,String mDate) {
        String[][] parm = {
                {"WebApiURL",gVariable.gWebURL+"SignList.php"},
                {"ClassId",mClassId},
                {"SignDate",mDate}
        };

        POSTresult = DBConnector.sendPOST(parm);
    }

    private void SignListHeader(TableLayout tblSign){
        tblSign.setStretchAllColumns(true);     // 各欄自动拉伸寬度
        TextView student = new TextView(tblSign.getContext());
        student.setText("學員");
        setText(student,0xFFFFFFFF,"C");

        TextView sin01time = new TextView(tblSign.getContext());
        sin01time.setText("上午");
        setText(sin01time,0xFFFFFFFF,"C");

        TextView studentname01 = new TextView(tblSign.getContext());
        studentname01.setText("上午代簽");
        setText(studentname01,0xFFFFFFFF,"C");

        TextView sin02time = new TextView(tblSign.getContext());
        sin02time.setText("下午");
        setText(sin02time,0xFFFFFFFF,"C");

        TextView studentname02 = new TextView(tblSign.getContext());
        studentname02.setText("下午代簽");
        setText(studentname02,0xFFFFFFFF,"C");

        TableRow tr = new TableRow(tblSign.getContext());
        tr.setBackgroundColor(0xFF5C6BC0);
        tr.addView(student);
        tr.addView(sin01time);
        tr.addView(studentname01);
        tr.addView(sin02time);
        tr.addView(studentname02);
        tblSign.addView(tr);
    }

    private void SignListDetail(TableLayout tblSign,JSONArray jsonArray) throws JSONException {
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonData = jsonArray.getJSONObject(i);

            // 已退訓且無簽到記錄,則不顯示
            if (jsonData.getString("studentstatus").equals("1")
                    && jsonData.getString("sin01time").equals("")
                    && jsonData.getString("sin02time").equals("")) continue;

            TableRow tr = new TableRow(tblSign.getContext());
            tr.setBackgroundColor((i % 2 == 0) ? 0xFFDDDDDD : 0xFFFFFFFF);

            TextView student = new TextView(tblSign.getContext());
            student.setText(jsonData.getString("student")); //資料欄位名稱
            setText(student,0,"");

            TextView sin01time = new TextView(tblSign.getContext());
            sin01time.setText((jsonData.getString("sin01time").equals(""))?
                    "未簽到":jsonData.getString("sin01time"));
            setText(sin01time,0,"C");
            if (jsonData.getString("sin01sts").equals("1")){
                sin01time.setBackgroundColor(0xFFFFAB91);
            } else if (jsonData.getString("sin01sts").equals("2")){
                sin01time.setBackgroundColor(0xFFFFF59D);
            }

            TextView studentname01 = new TextView(tblSign.getContext());
            studentname01.setText(jsonData.getString("studentname01"));
            setText(studentname01,0,"");

            TextView sin02time = new TextView(tblSign.getContext());
            sin02time.setText((jsonData.getString("sin02time").equals(""))?
                    "未簽到":jsonData.getString("sin02time"));
            setText(sin02time,0,"C");
            sin02time.setBackgroundResource(R.drawable.textview_border);
            if (jsonData.getString("sin02sts").equals("1")){
                sin02time.setBackgroundColor(0xFFFFAB91);
            } else if (jsonData.getString("sin02sts").equals("2")){
                sin02time.setBackgroundColor(0xFFFFF59D);
            }

            TextView studentname02 = new TextView(tblSign.getContext());
            studentname02.setText(jsonData.getString("studentname02"));
            setText(studentname02,0,"");

            tr.addView(student);
            tr.addView(sin01time);
            tr.addView(studentname01);
            tr.addView(sin02time);
            tr.addView(studentname02);

            tblSign.addView(tr);

        }
    }

    private static void setText(TextView tv,int textColor,String alignment) {
        TableRow.LayoutParams view_layout = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setTextSize(14);    // 放大一點可以捲動
        tv.setBackgroundResource(R.drawable.textview_border);
        tv.setLayoutParams(view_layout);
        if (alignment.equals("C")) {
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        if (textColor!=0){
            tv.setTextColor(textColor);
        }
    }

}
