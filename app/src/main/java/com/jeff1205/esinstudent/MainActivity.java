package com.jeff1205.esinstudent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    /***
     * 以下為 定位用變數
     */
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 6000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private FusedLocationProviderClient mFusedLocClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocRequest;
    private LocationSettingsRequest mLocSettingsRequest;
    private LocationCallback mLocCallback;
    private Location mCurrentLoc;
    private Boolean mReqLocUpdates;

    private Button mStopUpdBtn;
    private Button mInsteadBtn;
    private TextView tvDistance;
    private TextView tvTimeAM;
    private TextView tvTimePM;
    /***
     * 結束 定位用變數　========================
     */

    private long mExitTime;

    private String mClassId;
//    private String mClassName;
    private String mStudentId;
    private String mStudentName;
    private String mImei;
    private boolean mAllowSign;

    private String mDate;

    // 從 table 抓下列資料
    private Double mClassLongitude;
    private Double mClassLatitude;
    private Location mlocClass;
    private Location mlocStudent;
    private String mAllowDistance;
    private String mRegPassword;

    // 實際簽到時間距離 & 設定簽到起迄時時間
    private String mStudentlevel;
    private String mSin01time;
    private String mSin02time;
    private String mSin01distance;
    private String mSin02distance;
    private String mSin02stime;
    private String mSignAMPM = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //設定隱藏標題, 隱藏後自己做一行
        getSupportActionBar().hide();
        //設定隱藏狀態
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Android嚴苛模式設定
        setStrictMode();

        if (!DBConnector.checkURLStatus(gVariable.gWebURL + "SignList.php").equals("OK")) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(MainActivity.this, serviceError.class);
            startActivity(intent);
        } else {

            // 檢查手機是否註冊, 未註冊則跳轉至註冊頁面
            mStudentId = getSharedPreferences("Student", MODE_PRIVATE)
                    .getString("StudentId", "");
            if (mStudentId.equals("")) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(MainActivity.this, PhoneRegister.class);
                startActivity(intent);
            } else {
                // 從手機取回資料
                mStudentName = getSharedPreferences("Student", MODE_PRIVATE)
                        .getString("StudentName", "");
                mClassId = getSharedPreferences("Student", MODE_PRIVATE)
                        .getString("ClassId", "");
//                mClassName = getSharedPreferences("Student", MODE_PRIVATE)
//                        .getString("ClassName", "");
                mImei = getSharedPreferences("Student", MODE_PRIVATE)
                        .getString("Imei", "");

                // 從手機取得的資料寫回 全域變數
                gVariable.setgStudentId(mStudentId);
                gVariable.setgStudentName(mStudentName);
                gVariable.setgClassId(mClassId);
//                gVariable.setgClassName(mClassName);
                gVariable.setgImei(mImei);

                // 用 classid+imei 驗證和讀取班級簽到時間, 允許簽到距離, 教室座標.....
                getClassSinData();

                // 如果導師開放註冊(全員或個人), 則直接進入手機註冊頁面
                if (!mRegPassword.equals("00000")) {
                    if (mRegPassword.equals("000" + mStudentId) || mRegPassword.compareTo("10000") > 0) {
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setClass(MainActivity.this, PhoneRegister.class);
                        startActivity(intent);
                    }
                }
                gVariable.setgIsLeader(mStudentlevel.equals("1"));      // 設定幹部全域變數

                // 如果是為幹部才顯示代簽按鍵
                ShowBtnInst(gVariable.getgIsLeader());

                gVariable.setgSysDate();            // 設定系統日放全域變數
                mDate = gVariable.getgSysDate();
                mStopUpdBtn = findViewById(R.id.btnSignIn);
                mInsteadBtn = findViewById(R.id.btnInstead);
                tvDistance = findViewById(R.id.txtDistance2);
                tvTimeAM = findViewById(R.id.txtTimeAM);
                tvTimePM = findViewById(R.id.txtTimePM);
                tvTimeAM.setText(mSin01time);
                tvTimePM.setText(mSin02time);

                // 開始定位 & 計算距離 & 決定是enable簽到鍵
                if (gVariable.getcurTime().compareTo(mSin02stime) >= 0) {
                    // 當下時間>=下午開始簽到時間, 下午簽到
                    if (mSin02time.equals("")) mSignAMPM = "PM";
                } else {
                    // 否則為上午簽到
                    if (mSin01time.equals("")) mSignAMPM = "AM";
                }
                //
                if (!mSignAMPM.equals("")) {
                    // 不為空白才需簽到, 此時才需開啟定位簽到功能
                    mAllowSign = true;
                    startGetLoc();

                    // 未簽到不可代簽
                    mInsteadBtn.setEnabled(false);
                } else {
                    // 否則顯示 已簽到資料, 並 disable 簽到鍵
                    if (gVariable.getcurTime().compareTo(mSin02stime) >= 0)
                        tvDistance.setText(mSin02distance + " 公尺");
                    else tvDistance.setText(mSin01distance + " 公尺");

                    // disable 簽到鍵
                    mStopUpdBtn.setEnabled(false);
                }

                // 顯示學員簽到資料
                showSignlist(mClassId, mDate);
            }
        }
    }

    // Android嚴苛模式設定
    private void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    // 顯示或隱藏 代簽到鍵
    private void ShowBtnInst(Boolean OnOff) {
        Button btn = findViewById(R.id.btnInstead);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (OnOff==true) {
            params.weight = 2f;
            btn.setText("幹部代簽");
        } else {
            params.weight = 0f;
            btn.setText("");
        }
        btn.setLayoutParams(params);
    }

    // 顯示 簽到表
    private void showSignlist(String mClassId,String mDate){
        TableLayout tblSign = findViewById(R.id.tblSignList);

        SignList sl = new SignList();
        sl.ShowSignList(tblSign,mClassId,mDate);
    }

    private void startGetLoc() {
        mlocClass = new Location("A");
        mlocStudent = new Location("B");

        mFusedLocClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        // 未抓到定位前先 disable button
        mReqLocUpdates = false;
        setButtonsEnabledState();
        startLocUpdates();

    }

    // 按鍵功能
    public void clickButton(View view) {
        if (view.getId()==R.id.btnRefresh) {
            showSignlist(mClassId,mDate);
        } else if (view.getId()==R.id.btnInstead) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, InstSignIn.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else {
            // 執行簽到動作
            String POSTresult;
            String[][] parm = {
                    {"WebApiURL",gVariable.gWebURL+"insSignIn.php"},
                    {"ClassId",   mClassId},
                    {"StudentId", mStudentId},
                    {"Longitude", String.valueOf((double) mCurrentLoc.getLongitude())},
                    {"Latitude",  String.valueOf((double)mCurrentLoc.getLatitude())},
                    {"Distance",  tvDistance.getText().toString()},
                    {"AMPM",      mSignAMPM}
            };

            POSTresult = DBConnector.sendPOST(parm);
            try {
                JSONObject jsonData = new JSONObject(POSTresult);
                if (jsonData.getString("ReturnCode").substring(0,2).equals("-1")) {
                    msgBox.getDialogOK(this, "簽到結果", "新增簽到資料異常", "確認");
                } else {
                    mAllowSign = false;

                    // 己簽到才可代簽
                    mInsteadBtn.setEnabled(true);
                    // 刷新學員簽到資料
                    tvTimeAM.setText(jsonData.getString("sin01time"));
                    tvTimePM.setText(jsonData.getString("sin02time"));
                    showSignlist(mClassId, mDate);
                    msgBox.getDialogOK(this, "簽到結果", "簽到作業完成", "確認");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    private void getClassSinData() {
        String POSTresult;
        String[][] parm = {
                {"WebApiURL",gVariable.gWebURL+"getClassSinData.php"},
                {"ClassId",    mClassId},
                {"StudentImei",mImei}
        };
        POSTresult = DBConnector.sendPOST(parm);
        try {
            JSONObject jsonData = new JSONObject(POSTresult);
            mStudentlevel   = jsonData.getString("studentlevel");
            mSin01time      = jsonData.getString("sin01time");
            mSin02time      = jsonData.getString("sin02time");
            mSin01distance  = jsonData.getString("sin01distance");
            mSin02distance  = jsonData.getString("sin02distance");
            mSin02stime     = jsonData.getString("sin02stime");
            mAllowDistance  = jsonData.getString("distance");
            mClassLongitude = Double.parseDouble(jsonData.getString("longitude"));
            mClassLatitude  = Double.parseDouble(jsonData.getString("latitude"));
            mRegPassword    = jsonData.getString("regpassword");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    /*** ===================================================
//     *
//     * 以下為 Android Studio 定位範例修改而來
//     *
//     **** =================================================== */
    private void updLocUI() {
        if (mAllowSign == false) {
            mStopUpdBtn.setEnabled(false);
            mStopUpdBtn.setTextColor(Color.parseColor("#bbbbbb"));
            mReqLocUpdates = false;
            stopLocUpdates();
            return;
        }

        if (mCurrentLoc != null) {
            double distance = 0;
            String cdistance;

            mlocClass.setLatitude(mClassLatitude);
            mlocClass.setLongitude(mClassLongitude);
            mlocStudent.setLatitude(mCurrentLoc.getLatitude());
            mlocStudent.setLongitude(mCurrentLoc.getLongitude());
            distance = mlocClass.distanceTo(mlocStudent);

            if(distance < 1000 ) {
                cdistance = String.valueOf((int)distance);
                tvDistance.setText(cdistance + " 公尺");
            } else {
                cdistance = new DecimalFormat("#.00").format(distance / 1000);
                tvDistance.setText(cdistance + " 公里");
            }


            if (distance > Double.parseDouble(mAllowDistance)) {
                mStopUpdBtn.setEnabled(false);
                mStopUpdBtn.setTextColor(Color.parseColor("#bbbbbb"));
            } else {
                mStopUpdBtn.setEnabled(true);
                mStopUpdBtn.setTextColor(Color.parseColor("#000000"));
            }
        }
    }

//    private void updateValuesFromBundle(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            // Update the value of mReqLocUpdates from the Bundle, and make sure that
//            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
//            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
//                mReqLocUpdates = savedInstanceState.getBoolean(
//                        KEY_REQUESTING_LOCATION_UPDATES);
//            }
//
//            // Update the value of mCurrentLoc from the Bundle and update the UI to show the
//            // correct latitude and longitude.
//            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
//                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLoc
//                // is not null.
//                mCurrentLoc = savedInstanceState.getParcelable(KEY_LOCATION);
//            }
//
//            updateUI();
//        }
//    }

    private void createLocationRequest() {
        mLocRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLoc = locationResult.getLastLocation();
                updLocUI();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocRequest);
        mLocSettingsRequest = builder.build();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            // Check for the integer request code originally supplied to startResolutionForResult().
//            case REQUEST_CHECK_SETTINGS:
//                switch (resultCode) {
//                    case Activity.RESULT_OK:
//                        Log.i(TAG, "User agreed to make required location settings changes.");
//                        // Nothing to do. startLocUpdates() gets called in onResume again.
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        Log.i(TAG, "User chose not to make required location settings changes.");
//                        mReqLocUpdates = false;
//                        updateUI();
//                        break;
//                }
//                break;
//        }
//    }
//
    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     */
    public void btnStopUpdLoc(View view) {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocUpdates();
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocClient.requestLocationUpdates(mLocRequest,
                                mLocCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mReqLocUpdates = false;
                        }

                        updateUI();
                    }
                });
    }

    /**
     * Updates all UI fields.
     */
    private void updateUI() {
        setButtonsEnabledState();
        updLocUI();
    }

//    /**
//     * Disables both buttons when functionality is disabled due to insuffucient location settings.
//     * Otherwise ensures that only one button is enabled at any time. The Start Updates button is
//     * enabled if the user is not requesting location updates. The Stop Updates button is enabled
//     * if the user is requesting location updates.
//     */
    private void setButtonsEnabledState() {
        if (mReqLocUpdates) {
            mStopUpdBtn.setEnabled(true);
            mStopUpdBtn.setTextColor(Color.parseColor("#000000"));
        } else {
            mStopUpdBtn.setEnabled(false);
            mStopUpdBtn.setTextColor(Color.parseColor("#bbbbbb"));
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocUpdates() {
        if (!mReqLocUpdates) {
            Log.d(TAG, "stopLocUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocClient.removeLocationUpdates(mLocCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mReqLocUpdates = false;
                        setButtonsEnabledState();
                    }
                });
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
//        // location updates if the user has requested them.
//        if (mReqLocUpdates && checkPermissions()) {
//            startLocUpdates();
//        } else if (!checkPermissions()) {
//            requestPermissions();
//        }
//
//        updateUI();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // Remove location updates to save battery.
//        stopLocUpdates();
//    }
//
//    /**
//     * Stores activity data in the Bundle.
//     */
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mReqLocUpdates);
//        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLoc);
//        super.onSaveInstanceState(savedInstanceState);
//    }
//
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mReqLocUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }
}

