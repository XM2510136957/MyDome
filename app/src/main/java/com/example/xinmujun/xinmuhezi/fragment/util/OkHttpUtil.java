package com.example.xinmujun.xinmuhezi.fragment.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtil {

    private static OkHttpClient client;
    private static String fhurl="http://fh.izidc.cn/dwz.php?longurl=";
    private String ae_url;



    public  void getFhlj(String url, final Handler handler) {

        String url1 = fhurl + url;

        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();

        //调用ok的get请求
        Request request = new Request.Builder().get().url(url1).build();
        final Call call = client.newCall(request);

        //同步execute //同步请求 //同步是耗时的 //同步execute需要开启子线程
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Response response = call.execute();
                    Message message=new Message();
                    if (response.isSuccessful()) {
                        String string = response.body().string(); //调用者只需要实现provide方法就能拿到这个String了
                        Log.i("防洪链接,服务器端获取:", "链接: "+string);

                        String fh = fhjx(string);

                            message.obj = fh;
                            message.what = 0;
                            handler.sendMessage(message);
                    }else {
                        message.what = 1;
                        handler.sendMessage(message);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private String fhjx(String s)  {


        JSONObject jsonObject= null;
        try {
            jsonObject = new JSONObject(s);
            ae_url = jsonObject.getString("ae_url");

            Log.i("防洪链接:", "链接: "+ ae_url);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ae_url;
    }
}
