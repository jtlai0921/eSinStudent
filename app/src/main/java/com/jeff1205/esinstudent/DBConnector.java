package com.jeff1205.esinstudent;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class DBConnector {

    public static String sendPOST(String[][] parm) {

        String result = "";
        HttpURLConnection urlCon=null;
        InputStream inString =null;
        try {
            URL url=new URL(parm[0][1]);
            Map<String,Object> params = new LinkedHashMap<>();
            int i;
            for (i=1;i<parm.length;i++) {
                params.put(parm[i][0],parm[i][1]);
            }

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            urlCon=(HttpURLConnection) url.openConnection();     //對資料庫打開連結
            urlCon.setRequestMethod("POST");
            urlCon.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            urlCon.setDoInput(true);    // 資料 in,out 都打開
            urlCon.setDoOutput(true);
            urlCon.getOutputStream().write(postDataBytes);  // POST 資料到 webapi

            urlCon.connect();    //接通資料庫
            inString = urlCon.getInputStream();  //從 database 開啟 stream

            BufferedReader bufReader = new BufferedReader(new InputStreamReader(inString, "utf-8"), 8);
            StringBuilder builder = new StringBuilder();
            String line = null;
            while((line = bufReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            inString.close();
            result = builder.toString();
        } catch(Exception e) {
            Log.e("log_tag", e.toString());
        }

        return result;
    }

    public static String checkURLStatus(String pURL) {
        String rtnString;
        URL url = null;
        try {
            url = new URL(pURL);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(1000);
            InputStream is = connection.getInputStream();
            is.close();
            return "OK";
        } catch (Exception e) {
            //ActiveMQUtilLogger.LOGGER.failedToCheckURL(e, url == null ? " " : url.toString());
            return "Err";
        }

    }

}
