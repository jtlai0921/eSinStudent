package com.jeff1205.esinstudent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
//import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class PhoneRegister extends AppCompatActivity {
    Button btn;
    private int READ_PHONE_STATE = 1;

    private EditText etStudentId,etStudentName,etClassId,etPassWord;

    private String mStudentId="";
    private String mStudentName="";
    private String mClassId="";
//    private String mClassName="";
    private String mPassWord="";
    private String mImei="";
    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoneregister);

        //設定隱藏標題, 隱藏後自己做一行
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // 要求授權, 未取得前註冊鍵先關掉
        btn = findViewById(R.id.btnRegMenu);
        btn.setEnabled(false);

        // 如正常取得權限, 則 mImei 已正常會抓到值了
        mImei = getSharedPreferences("Student", MODE_PRIVATE)
                .getString("Imei", "");
        if (mImei.equals("")) {
            getPermission();
            while (mImei.equals("")) {
                mImei = mygetIMEI();
            }
        }
        btn.setEnabled(true);

        // 如已授權但沒正常註冊就離開, 下次進入不需再授權, 那如何何取得 imei ???????
        //   所以如已授權, 則直接先將 imei 寫入手機
        SharedPreferences pref = getSharedPreferences("Student", MODE_PRIVATE);
        pref.edit()
            .putString("Imei", mImei)
            .apply();

        // 在導師有開放註冊時,如手機已有註冊則抓出來直接 Show 在畫面
        //  如尚未冊會抓到空白一樣直接顯示, 讓user輸入
        mStudentId = getSharedPreferences("Student", MODE_PRIVATE)
                .getString("StudentId", "");
        mStudentName = getSharedPreferences("Student", MODE_PRIVATE)
                .getString("StudentName", "");
        mClassId = getSharedPreferences("Student", MODE_PRIVATE)
                .getString("ClassId", "");
//        mClassName = getSharedPreferences("Student", MODE_PRIVATE)
//                .getString("ClassName", "");
//        mImei = getSharedPreferences("Student", MODE_PRIVATE)
//                .getString("Imei", "");

        etStudentId = findViewById(R.id.edtStudentId);
        etStudentId.setText(mStudentId);
        etStudentName = findViewById(R.id.edtStudentName);
        etStudentName.setText(mStudentName);
        etClassId = findViewById(R.id.edtClassId);
        etClassId.setText(mClassId);
        etPassWord = findViewById(R.id.edtPassWord);
        etPassWord.setText("");
    }

    public void btnReg(View view) {
        String dbResultS;
        String dbResultI;

        //判斷導師帳號 導師姓名 密碼 都有輸入值 才執行
        mStudentId   = etStudentId.getText().toString();
        mStudentName = etStudentName.getText().toString();
        mClassId     = etClassId.getText().toString();
        mPassWord    = etPassWord.getText().toString();

        if (mStudentId.equals("") ||
            mStudentName.equals("") ||
            mClassId.equals("") ||
            mPassWord.equals("")) {
            msgBox.getDialogOK(this,"錯誤訊息","註冊欄位輸不完整","請檢查");
            return;
        }

        // 先查詢後再處理
        dbResultS = dbProcess("S");
        if (dbResultS.substring(0, 2).equals("-1")) {
            // -1,資料處理異常!
            msgBox.getDialogOK(this, "錯誤訊息", dbResultS.substring(3), "確定");
            return;
        }

        // 查詢後續處理 查詢返回值->1.新增(I), 2.更新(U), 3.不同名覆蓋(U)
        if (msgBox.getDialogYesNo(this,"請確認",dbResultS.substring(3),"確定","取消")=="N") {
            return;
        }
        if (dbResultS.substring(0, 2).equals(" 1")) {
            // 確定執行 Insert
            dbResultI = dbProcess("I");
        } else {
            // 確定執行 Update
            dbResultI = dbProcess("U");
        }

        // Insert 或 Update 後返回值
        if (dbResultI.substring(0, 2).equals("-1")) {
            msgBox.getDialogOK(this, "錯誤訊息", dbResultI.substring(3),"確定");
            return;
        }

        // 處理手機SharedPreferences資料 --------------------------------------------------
        SharedPreferences pref = getSharedPreferences("Student", MODE_PRIVATE);
        pref.edit()
            .putString("StudentId",   mStudentId)
            .putString("StudentName", mStudentName)
            .putString("ClassId",     mClassId)
//            .putString("ClassName",   mClassName)
            .putString("Imei",        mImei)
            .apply();

        msgBox.getDialogOK(this, "註冊訊息", "手機註冊完成!","確認");
        // 直接結束
        System.exit(0);
    }

    private String dbProcess(String ShowInsUpd) {
        String POSTresult;
        String askMsg;
        String[][] parm = {     // 第一個陣列一定要用 webapi php 程式網址, 後面才是真正要 POST 的資料
                {"WebApiURL",   gVariable.gWebURL+"RegStudentData.php"},
                {"StudentId",   mStudentId},
                {"StudentName", mStudentName},
                {"ClassId",     mClassId},
                {"Imei",        mImei},
                {"ShowInsUpd",  ShowInsUpd}
        };
        POSTresult = DBConnector.sendPOST(parm);
        try {
            JSONObject jsonData = new JSONObject(POSTresult);
            if (ShowInsUpd.equals("S")) {
                if (jsonData.getString("reccls").equals("0")) {
                    return "-1,班級代號錯誤!";
                }
                if (jsonData.getString("regpassword").equals("00000")) {
                    return "-1,此班級目前未開放註冊!";
                }
                if (!jsonData.getString("regpassword").equals(mPassWord)) {
                    return "-1,註冊碼密錯誤!";
                }
                if (mStudentId.compareTo(jsonData.getString("maxstudent"))>0) {
                    return "-1,學號>班級人數!";
                }

                // 全班開放註冊第一碼 用->月+日+秒 末碼當第一碼(一定不為0), 後4碼隨機取得


                if (jsonData.getString("recstu").equals("0")) {
                    // 無資料, 詢問是否確定新增
                    askMsg = " 1.是否確定註冊 ?";
                } else {
                    if (jsonData.getString("studentname").equals(mStudentName)) {
                        // 有資料同姓名, 詢問是否確定修改
                        askMsg = " 2.是否確定修改資料 ?";
                    } else {
                        // 有資料姓名不同, 詢問是否確定覆蓋
                        askMsg = " 3.此學號已被[" + jsonData.getString("studentname") + "]同學使用,是否確定覆蓋 ?";
                    }
                }
                return askMsg;
            } else {
                return jsonData.getString("studentname") ;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return "-1,資料處理異常!";
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判斷用戶是否點擊了“返回鍵”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //與上次點擊返回鍵時刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大於2000ms則認為是誤操作，使用Toast進行提示
                Toast.makeText(this, "再按一次退出程式", Toast.LENGTH_SHORT).show();
                //並記錄下本次點擊“返回鍵”的時刻，以便下次進行判斷
                mExitTime = System.currentTimeMillis();
            } else {
                //小於2000ms則認為是使用者確實希望退出程式-調用System.exit()方法進行退出
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getPermission() {
        // 要求授權
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION +
                        Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE},
                    READ_PHONE_STATE);
        } else {
//            TelephonyManager tp = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            if (Build.VERSION.SDK_INT >= 23) {
//                mImei = tp.getDeviceId(0);
//            }else{
//                mImei = tp.getDeviceId();
//            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        // 授權 callback
//        if (grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//
//            // 正常取得授權, 開啟註冊按鍵
//            btn.setEnabled(true);
//            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION +
//                            Manifest.permission.READ_PHONE_STATE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                // 已經都回答同意授權了, 為何還是不等??
//                TelephonyManager tp = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                if (Build.VERSION.SDK_INT >= 23) {
//                    mImei = tp.getDeviceId(0);
//                }else{
//                    mImei = tp.getDeviceId();
//                }
//            } else {
//                TelephonyManager tp = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                if (Build.VERSION.SDK_INT >= 23) {
//                    mImei = tp.getDeviceId(0);
//                }else{
//                    mImei = tp.getDeviceId();
//                }
//            }
//
//        } else {
//            // 未取得全部授權
//            if (msgBox.getDialogYesNo(this,"未取得全部授權","請提供授權程式才能正常執行","重新授權","拒絕授權")=="Y") {
//                // 重新取得檔案寫入的權限
//                getPermission();
//            } else {
//                // 拒絕授權
//                msgBox.getDialogOK(this,"錯誤訊息","無法取得權限","結束離開");
//                // 強迫結束APP
//                System.exit(0);
//            }
//        }
//    }

//    @SuppressWarnings("deprecation")
    private String mygetIMEI() {
        String Imei;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        TelephonyManager tp = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Imei = tp.getImei();
            } catch (Exception e) {
                // 郁棻SONY手機抓有問題, 先避開
                Double dImeiA = Math.random() * (9999999-1000001 + 1 ) + 1000001;
                Double dImeiB = Math.random() * (999999 -100001  + 1 ) + 100001;
                Imei = "86" + String.valueOf(dImeiA).substring(0,7) + String.valueOf(dImeiB).substring(0,6);
            }
        }else{
            Imei = tp.getDeviceId();
        }

//        if (Build.VERSION.SDK_INT >= 26) {
//            if (tp.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
//                Imei = tp.getMeid();
//            } else if (tp.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
//                Imei = tp.getImei();
//            } else {
//                Imei = ""; // default!!!
//            }
//        } else {
//            Imei = tp.getDeviceId();
//        }
        return Imei;
    }
//    /**
//     * Returns the IMEI (International Mobile Equipment Identity). Return null if IMEI is not
//     * available.
//     */
//    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
//    public String getImei() {
//        return getImei(getSlotIndex());
//    }
//
//    /**
//     * Returns the IMEI (International Mobile Equipment Identity). Return null if IMEI is not
//     * available.
//     *
//     * @param slotIndex of which IMEI is returned
//     */
//    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
//    public String getImei(int slotIndex) {
//        ITelephony telephony = getITelephony();
//        if (telephony == null) return null;
//
//        try {
//            return telephony.getImeiForSlot(slotIndex, getOpPackageName());
//        } catch (RemoteException ex) {
//            return null;
//        } catch (NullPointerException ex) {
//            return null;
//        }
//    }
}
