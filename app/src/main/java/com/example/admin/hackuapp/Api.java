package com.example.admin.hackuapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 非同期通信でPOSTリクエストをする
 *
 */
public class Api extends AppCompatActivity {
    private Activity ac;
    private String url;
    private List<String> id= new ArrayList<String>();
    private String subkey=null;
    private String fileName=null;
    private boolean post=false;
    public List<String> result=new ArrayList<String>();
    private Intent intent;
    
    Api(Activity ac){
        this.ac=ac;
    }
    public void setUrl(String url){
        this.url=url;
    }
    public void setId(String[]idList){
        this.id.clear();
        for(String s:idList){
            this.id.add(s);
        }
        Log.d("test",String.valueOf(this.id.size()));
    }


    public void setSubkey(String subkey){
        this.subkey=subkey;
    }
    public void setFileName(String name){
        this.fileName=name;
    }
    public void setPost(boolean post){
        this.post=post;
    }
    private String makeUri(String url,int count){
        String uri=url;
        uri+="?";
        for(int i=count*10;i<this.id.size();i++){
            if(i==0){
                uri+="identificationProfileIds=";
            }
            uri+=id.get(i);
            if(i!=this.id.size()-1){
                uri+=",";
            }else{
                uri+="&";
            }
        }
        uri+="shortAudio=true";
        Log.d("uri result",uri);
        return uri;
    }
    private JSONObject getJSON(String text){
        try {
            JSONObject jsonObj = new JSONObject(text);
            return jsonObj;
        }catch(Exception e){
            return null;
        }
    }
    
    private HttpPostTask request(){
        return request(this.url,0);
    }
    private HttpPostTask request(int count){
        return request(this.url,count);
    }
    private HttpPostTask request(String url,int count){
        return new HttpPostTask(
                ac,makeUri(url,count),

                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler(){
                    public void onIdentCompleted(String response) {
                        // 受信結果をUIに表示
                        JSONObject r=getJSON(response);
                        Log.d("test","tets");
                        try{
                            System.out.println(r.get("processingResult"));
                            Object pr=r.get("processingResult");
                            if(pr instanceof JSONObject){
                                result.add(((JSONObject)pr).getString("identifiedProfileId"));
                            }
                            System.out.println(result.toString());
                        }catch(Exception e){
                            System.out.println("error "+e.toString());
                            
                        }

                        String str="";
                        for(int i=0;i<result.size();i++){
                            str+=result.get(i)+"\n";
                        }
                        System.out.println(str);
                    }
                    @Override
                    public void onNewCompleted(String response) {
                        // 受信結果をUIに表示
                        result.clear();
                        JSONObject r=getJSON(response);
                        String pr="";
                        try{
                            pr=r.getString("identificationProfileId");
                            result.add(pr);

                            //intent.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.EditCard");
                            //intent.putExtra("profileId", result.get(0));
                            //startActivity(intent);

                            System.out.println(pr);
                        }catch(Exception e){
                            System.out.println("error "+e.toString());
                        }
                        enroll("https://api.projectoxford.ai/spid/v1.0/identificationProfiles/"+pr+"/enroll");
                    }
                    @Override
                    public void onPostCompleted(String response) {
                        // 受信結果をUIに表示

                        
                    }

                    @Override
                    public void onPostFailed(String response) {

                        Toast.makeText(
                                ac.getApplicationContext(),
                                "エラーが発生しました。",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
    
    // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
    public void enroll(String url) {
        // 非同期タスクを定義
        this.url=url;
        Log.d("enroll","miss");
        Log.d("connection",url);
        HttpPostTask task = request();
        task.addPostHeader("Ocp-Apim-Subscription-Key",this.subkey);
        task.addPostHeader("Content-Type","application/octet-stream");
        task.setPost("enroll");
        task.setfileName(fileName);
        // タスクを開始
        task.execute();
        System.out.println("task start");
        
    }
    /*
    public void enroll(){
        exec_post();

    }
    */
    public void newaccount(){
        HttpPostTask task = request();
        task.addPostHeader("Ocp-Apim-Subscription-Key",this.subkey);
        task.addPostHeader("Content-Type","application/json");
        task.setPost("new");
        // タスクを開始
        task.execute();
        System.out.println("task start");
        try{
            task.get(1500, TimeUnit.MILLISECONDS);
        }catch(Exception e){

        }

    }

    public void identification()throws Exception{
        
        List confidence=new ArrayList();
        String result;
        for(int i=0;i<this.id.size()/10+1;i++){
            HttpPostTask task=request(i);
            task.addPostHeader("Ocp-Apim-Subscription-Key",this.subkey);
            task.addPostHeader("Content-Type","application/octet-stream");
            task.setPost("identification");
            task.setfileName(fileName);
            task.execute();
        }
        
        
    }
    
}