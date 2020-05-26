package com.jeff1205.esinstudent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class InstSignIn extends AppCompatActivity {
    String mSignMSG;
    String mClassId;
    String mStudentId;
    String mInstId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instsignin);

        //設定隱藏標題, 隱藏後自己做一行
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        mClassId = gVariable.getgClassId();
        mInstId = gVariable.getgStudentId();

    }

    public void btnOnClick(View view) {
        TextView tv;
        EditText st = findViewById(R.id.edtStudentId);
        mStudentId= st.getText().toString();

        // 未輸入被代簽, 不處理
        if (mStudentId.equals("")) return;

        // 被代簽學號 = 代簽學號 不處理
        if (mStudentId.equals(mInstId)) {
            tv = findViewById(R.id.txtStudentName2);
            tv.setText(gVariable.getgStudentName());
            msgBox.getDialogOK(view.getContext(), "錯誤訊息", "不可為自已代簽到!","重新輸入");
        } else {
            mSignMSG = InstSign("S");  // 更新前先詢問是否更新
            if (mSignMSG.substring(0, 2).equals("-1")) {
                msgBox.getDialogOK(view.getContext(), "錯誤訊息", mSignMSG.substring(3),"重來");
            } else {
                tv = findViewById(R.id.txtStudentName2);
                tv.setText(mSignMSG.substring(3));

                if (msgBox.getDialogYesNo(view.getContext(),
                        "代簽訊息","是否確定幫學員 [" + mSignMSG + "] 代簽?","確認","取消")=="Y") {
                    // 確定執行代簽
                    mSignMSG = InstSign("U");
                    if (mSignMSG.substring(0, 2).equals("-1")) {
                        // 代簽 insert 失敗
                        msgBox.getDialogOK(view.getContext(), "錯誤訊息", mSignMSG.substring(3),"重來");
                    } else {
                        // 代簽 insert 成功
                        msgBox.getDialogOK(view.getContext(), "代簽訊息", "代簽完成!"+mSignMSG,"確認");
                    }
                }
            }
        }
    }

    private String InstSign(String ShowOrUpdate) {
        String POSTresult;
        String RtnString;

        String[][] parm = {     // 第一個陣列一定要用 webapi php 程式網址, 後面才是真正要 POST 的資料
            {"WebApiURL", gVariable.gWebURL+"InstSignIn.php"},
            {"ClassId",   mClassId},      // 被代簽學號
            {"Student1",  mStudentId},    // 被代簽學號
            {"Student2",  mInstId},       // 代簽學號
            {"ShowOrUpdate", ShowOrUpdate}      // 詢問 或 執行代簽
        };
        POSTresult = DBConnector.sendPOST(parm);
        try {
            JSONObject jsonData = new JSONObject(POSTresult);
            RtnString = jsonData.getString("ReturnCode");
        } catch (JSONException e) {
            RtnString = "-1.JSON錯誤";
            e.printStackTrace();
        }
        return RtnString;
    }

}
