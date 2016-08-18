package com.example.admin.hackuapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.io.File;


/**
 * Created by admin on 2016/08/02.
 */
public class HttpPostTask extends AsyncTask<Void, Void, Void> {

    // 設定事項
    private String request_encoding = "UTF-8";
    private String response_encoding = "UTF-8";

    // 初期化事項
    private Activity parent_activity = null;
    private String post_url = null;
    private Handler ui_handler = null;
    private List<NameValuePair> post_params = null, post_header = null;
    private String fileName=null;
    // 処理中に使うメンバ
    private ResponseHandler<Void> response_handler = null;
    private String http_err_msg = null;
    private ProgressDialog dialog;
    public String http_ret_msg = null;
//    private ProgressDialog dialog = null;
    public String result=null;
    private int status=0;
    private String post="";

    // 生成時
    public HttpPostTask( Activity parent_activity, String post_url, Handler ui_handler )
    {
        // 初期化
        this.parent_activity = parent_activity;
        this.post_url = post_url;
        this.ui_handler = ui_handler;

        // 送信パラメータは初期化せず，new後にsetさせる
        post_params = new ArrayList<NameValuePair>();
        post_header = new ArrayList<NameValuePair>();
    }


  /* --------------------- POSTパラメータ --------------------- */


    // 追加
    public void addPostParam( String post_name, String post_value )
    {
        post_params.add(new BasicNameValuePair( post_name, post_value ));
    }
    public void addPostHeader( String post_name, String post_value )
    {
        post_header.add(new BasicNameValuePair( post_name, post_value ));
    }
    public void setfileName(String fileName){
        this.fileName="/"+fileName;
    }
    public void setPost(String post){this.post=post;}
  /* --------------------- 処理本体 --------------------- */
  private JSONObject getJSON(String text){
      try {
          JSONObject jsonObj = new JSONObject(text);
          return jsonObj;
      }catch(Exception e){
          return null;
      }
  }

    // タスク開始時
    protected void onPreExecute(){
        // ダイアログを表示
        dialog = new ProgressDialog( parent_activity );
        dialog.setMessage("通信中・・・");
        dialog.show();
        Log.d("posttest","pre execute");
        // レスポンスハンドラを生成
        response_handler = new ResponseHandler<Void>() {

            // HTTPレスポンスから，受信文字列をエンコードして文字列として返す
            @Override
            public Void handleResponse(HttpResponse response) throws IOException
            {
                Log.d(
                        "posttest",
                        "レスポンスコード：" + response.getStatusLine().getStatusCode()
                );
                // 正常に受信できた場合は200
                switch (response.getStatusLine().getStatusCode()) {
                    case HttpStatus.SC_OK:
                        Log.d("posttest", "レスポンス取得に成功");

                        // レスポンスデータをエンコード済みの文字列として取得する。
                        // ※IOExceptionの可能性あり
                        HttpPostTask.this.http_ret_msg = EntityUtils.toString(
                                response.getEntity(),
                                HttpPostTask.this.response_encoding
                        );
                        Log.d("test",HttpPostTask.this.http_ret_msg);

                        if( HttpPostTask.this.http_ret_msg==null){
                            System.out.println("miss?");
                        }
                    break;
                    case HttpStatus.SC_ACCEPTED:
                        Log.d("posttest","202");
                        HttpPostTask.this.http_ret_msg = EntityUtils.toString(
                                response.getEntity(),
                                HttpPostTask.this.response_encoding
                        );
                        HttpPostTask.this.http_ret_msg =(response.getHeaders("Operation-Location")[0].getValue());
                        if( HttpPostTask.this.http_ret_msg==null){
                            System.out.println("miss?2");
                        }
                        try{
                            result=(response.getHeaders("Operation-Location")[0].getValue());
                        }catch(Exception e){
                        }
                        break;
                    case HttpStatus.SC_NOT_FOUND:
                        // 404
                        Log.d("posttest", "存在しない");
                        HttpPostTask.this.http_err_msg = "404 Not Found";
                        break;

                    default:
                        Log.d("posttest", "通信エラー");
                        HttpPostTask.this.http_err_msg = "通信エラーが発生";
                }

                return null;
            }

        };
    }
    
    
    private byte[] getFile(String fileName){
        try{
            File file=new File(fileName);
            byte[] data = new byte[(int)file.length()];
            FileInputStream in = new FileInputStream(file);
            in.read(data);
            in.close();
            return data;
        }catch(Exception e){
            Log.d("get File",e.getMessage());
            return null;
        }
    }
    
    private void postRequest(){
        HttpClient httpclient=new DefaultHttpClient();
        try{
            URI uri=new URI(post_url);
            HttpPost request = new HttpPost(uri);
            for(NameValuePair p:this.post_header)
                request.setHeader(p.getName(),p.getValue());
            request.setEntity(new ByteArrayEntity(getFile(Environment.getExternalStorageDirectory().getAbsolutePath() + fileName)));
            httpclient.execute(request,response_handler);
            System.out.println();
        }catch(Exception e){
            Log.d("error",e.toString());
            Log.d("error",e.getMessage());
        }
    }
    private void getRequest(){
        try{
            HttpClient httpClient = new DefaultHttpClient();
            // 大阪の天気予報XMLデータ
            HttpGet httpGet = new HttpGet(post_url);
            for(NameValuePair p:this.post_header)
                httpGet.setHeader(p.getName(),p.getValue());
            httpClient.execute(httpGet,response_handler);
        }catch(Exception e){
            
        }
        
    }
    
    private void identification(){
        try{
            postRequest();
            result=http_ret_msg;
            post_url=result;
            Thread.sleep(2000);
            Log.d("newurl",post_url);
            getRequest();
        }catch(Exception e){
            
        }
        Log.d("identification","end");
        
    }
    private void enroll(){
        postRequest();
        
        
    }

    private void newacount(){
        post_url="https://api.projectoxford.ai/spid/v1.0/identificationProfiles/";
        HttpClient httpclient=new DefaultHttpClient();
        try{
            URI uri=new URI(post_url);
            HttpPost request = new HttpPost(uri);
            for(NameValuePair p:this.post_header)
                request.setHeader(p.getName(),p.getValue());
            request.setEntity(new ByteArrayEntity("{'locale':'en-US'}".getBytes()));
            httpclient.execute(request,response_handler);
            System.out.println();
        }catch(Exception e){
            Log.d("error",e.toString());
            Log.d("error",e.getMessage());
        }
        try{
            Thread.sleep(2000);
        }catch(Exception e){

        }
        post_url="https://api.projectoxford.ai/spid/v1.0/identificationProfiles/"+http_ret_msg+"/enroll";
        postRequest();



    }

    // メイン処理
    protected Void doInBackground(Void... unused) {
        if(post.equals("identification")){
            Log.d("posttest", "postします");
            identification();
        }else if(post.equals("enroll")){
            Log.d("posttest", "enrollします");
            enroll();
        }else if(post.equals("new")){
            Log.d("new acount","new");
            newacount();
        }
        return null;
    }
    // タスク終了時
    protected void onPostExecute(Void unused) {
        // ダイアログを消す
        Log.d("request","request end");
        dialog.dismiss();
        // 受信結果をUIに渡すためにまとめる
        Message message = new Message();
        Bundle bundle = new Bundle();
        
        if (http_err_msg != null) {
            // エラー発生時
            bundle.putBoolean("http_post_success", false);
            bundle.putString("http_response", http_err_msg);
        } else {
            // 通信成功時
            bundle.putBoolean("http_post_success", true);
            bundle.putString("http_response", http_ret_msg);
            bundle.putString("http_type",post);
        }
        message.setData(bundle);
        // 受信結果に基づいてUI操作させる
        ui_handler.sendMessage(message);

    }

}