package com.jeff1205.esinstudent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class gVariable {

    /*
    private static String  gClassId = "20101";
    private static String  gClassName = "Android應用技術班";
    private static String  gStudentId = "13";
    private static String  gStudentName = "楊建豐";
     */

    public final static String gWebURL = "http://123.194.136.229/webapi/esign/";

    private static String gClassId;
//    private static String gClassName;
    private static String gStudentId;
    private static String gStudentName;

    private static Boolean gIsLeader;
    private static String  gSysDate;
    private static String  gImei;

    public static void setgClassId(String gClassId)         {gVariable.gClassId = gClassId;}
//    public static void setgClassName(String gClassName)     {gVariable.gClassName = gClassName;}
    public static void setgStudentId(String gStudentId)     {gVariable.gStudentId = gStudentId;}
    public static void setgStudentName(String gStudentName) {gVariable.gStudentName = gStudentName;}
    public static void setgIsLeader(Boolean gIsLeader)      {gVariable.gIsLeader = gIsLeader;}
    public static void setgImei(String gImei)               {gVariable.gImei = gImei;}
    public static void setgSysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
        gSysDate = sdf.format(curDate);
    }

    public static String getgClassId()      {return gClassId;}
//    public static String getgClassName()    {return gClassName;}
    public static String getgStudentId()    {return gStudentId;}
    public static String getgStudentName()  {return gStudentName;}
    public static Boolean getgIsLeader()    {return gIsLeader;}
    public static String getgImei()         {return gImei;}
    public static String getgSysDate()      {return gSysDate;}
    public static String getcurTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date dt = new Date(System.currentTimeMillis());     // 獲取當前時間
        return sdf.format(dt);
    }
}
