package com.example.admin.hackuapp;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.json.JSONObject;

public class Recording extends AppCompatActivity {

    WavRec wr=new WavRec();
    Api api=new Api(this);
    String profileID = "";

    private DBAccesser dba;
    private JSONObject getJSON(String text){
        try {
            JSONObject jsonObj = new JSONObject(text);
            return jsonObj;
        }catch(Exception e){
            return null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,}, 1);
        }

        dba = new DBAccesser(this);

        CompoundButton toggle = (CompoundButton)findViewById(R.id.rec_switch);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 状態が変更された isChecked
//                Toast.makeText(Recording.this, "isChecked : " + isChecked, Toast.LENGTH_SHORT).show();
                if(isChecked){
                    wr=new WavRec();
                    wr.rec_start();
                }else{
                    wr.rec_stop();
                    wr.convert();
                }
            }
        });

        findViewById(R.id.new_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
//                wr.convert();
                api.setSubkey("705b6408172b465ebe0579742f062214");
                api.setFileName("rec.wav");
                api.handler=new HttpPostHandler(){
                    public void onIdentCompleted(String response) {
                        // 受信結果をUIに表示
                        JSONObject r=getJSON(response);
//                        Log.d("test","tets");
                        try{
//                            System.out.println(r.get("processingResult"));
                            Object pr=r.get("processingResult");
                            if(pr instanceof JSONObject){
                                api.result.add(((JSONObject)pr).getString("identifiedProfileId"));
                            }
//                            System.out.println(api.result.toString());
                        }catch(Exception e){
                            System.out.println("error "+e.toString());

                        }
                    }
                    @Override
                    public void onNewCompleted(String response) {
                        // 受信結果をUIに表示
                        JSONObject r=getJSON(response);
                        String pr="";
                        try{
                            pr=r.getString("identificationProfileId");
                            Intent newCard = new Intent();
                            newCard.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.EditCard");
                            newCard.putExtra("profileId", pr);
                            startActivity(newCard);
//                            System.out.println(pr);
                        }catch(Exception e){
                            System.out.println("error "+e.toString());
                        }
                        api.enroll("https://api.projectoxford.ai/spid/v1.0/identificationProfiles/"+pr+"/enroll");
                    }
                    @Override
                    public void onPostCompleted(String response) {
                        // 受信結果をUIに表示


                    }

                    @Override
                    public void onPostFailed(String response) {
                    }
                };
                
                api.newaccount();

                //profileID = api.result.get(0);

                //newCard.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.EditCard");
                //newCard.putExtra("profileId", api.result.get(0));
                //startActivity(newCard);

//                api.enroll("https://api.projectoxford.ai/spid/v1.0/identificationProfiles/"+api.result.get(0)+"/enroll");
            }
        });
        /*
        findViewById(R.id.newCard_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                Intent newCard = new Intent();
                newCard.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.EditCard");
                newCard.putExtra("profileId", api.result.get(0));
                startActivity(newCard);

//                api.enroll("https://api.projectoxford.ai/spid/v1.0/identificationProfiles/"+api.result.get(0)+"/enroll");
            }
        });
        */
        findViewById(R.id.check_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                api.setUrl("https://api.projectoxford.ai/spid/v1.0/identify");

                DBLine[] dbLines = dba.getAll();
                String[] IDs;

                if(dbLines != null) {
                    IDs =  new String[dbLines.length];
                    int i = 0;
                    for (DBLine row : dbLines) {
                        IDs[i++] = row.MS_PROFILE_ID;
                    }
                    api.setId(IDs);
                    //                api.setId(new String[]{ "9f5eb3d8-f0d9-40cf-83b6-e4f4870ca1a9","c9d4d5e5-708c-4f3c-9ddd-6c67b4b6a6cd","c9aad336-f824-49a3-be4e-c0aa5efa709d"});
                    api.setSubkey("");
                    api.setFileName("rec.wav");
                    api.handler=new HttpPostHandler(){
                        public void onIdentCompleted(String response) {
                            // 受信結果をUIに表示
                            JSONObject r=getJSON(response);
//                            Log.d("test","tets");
                            try{
//                                System.out.println(r.get("processingResult"));
                                Object pr=r.get("processingResult");
                                String result="";
                                if(pr instanceof JSONObject){
                                    result=((JSONObject)pr).getString("identifiedProfileId");
                                }
                                if(result==null||result.equals("00000000-0000-0000-0000-000000000000")){
                                    Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_SHORT).show();
                                }else{
                                    Intent cardDetail = new Intent();
                                    cardDetail.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.BusinessCardsDetailActivity");
                                    cardDetail.putExtra(BusinessCardsDetailFragment.ARG_ITEM_ID, dba.getByMs_profile_id(result).BOOK_ID);
                                    startActivity(cardDetail);
                                }
                                
//                                System.out.println(api.result.toString());
                            }catch(Exception e){
//                                System.out.println("error "+e.toString());
                                Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_SHORT).show();

                            }
                        }
                        @Override
                        public void onNewCompleted(String response) {
                            // 受信結果をUIに表示
                            JSONObject r=getJSON(response);
                            String pr="";
                            try{
                                pr=r.getString("identificationProfileId");
                                Intent cardDetail = new Intent();
                                cardDetail.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.BusinessCardsDetailActivity");
                                cardDetail.putExtra(BusinessCardsDetailFragment.ARG_ITEM_ID, dba.getByMs_profile_id(pr).BOOK_ID);
                                startActivity(cardDetail);
                            }catch(Exception e){
                                System.out.println("error "+e.toString());
                            }
                        }
                        @Override
                        public void onPostCompleted(String response) {
                            // 受信結果をUIに表示


                        }

                        @Override
                        public void onPostFailed(String response) {
                        }
                    };
                    api.identification();
//                    String resultId = api.result.get(0);
//
//                    Intent cardDetail = new Intent();
//                    cardDetail.putExtra(BusinessCardsDetailFragment.ARG_ITEM_ID, dba.getByMs_profile_id(resultId).BOOK_ID);
//                    startActivity(cardDetail);
                }else{
                    Toast.makeText(Recording.this, "DataBase is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*
        findViewById(R.id.list_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                String resultId = api.result.get(0);
                Intent cardDetail = new Intent();
                cardDetail.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.BusinessCardsDetailActivity");
                cardDetail.putExtra(BusinessCardsDetailFragment.ARG_ITEM_ID, dba.getByMs_profile_id(resultId).BOOK_ID);
                startActivity(cardDetail);

            }
        });
        */
    }

}
