package com.example.admin.hackuapp;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by admin on 2016/08/02.
 */
public class WavRec extends Activity{

    private int SAMPLING_RATE;
    AudioRecord audioRec = null;
    boolean bIsRecording = false;
    int bufSize;
    private String fileName=null;
    
    public WavRec(int sampling,String fileName){
        this.SAMPLING_RATE=sampling;
        this.fileName="/"+fileName;
        init();
    }
    public WavRec(int sampling){
        this(sampling,"rec.wav");
    }
    public WavRec(String fileName){
        this(16000,fileName);
    }
    public WavRec(){
        this(16000);
    }
    private void init(){
        bufSize = AudioRecord.getMinBufferSize(
                SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRec = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize * 2);//２倍にしないと処理落ちするらしい。
        
    }
    public void rec_start(){
        bIsRecording = true;
//          プロセスの優先度を上げる
        android.os.Process.setThreadPriority(
                android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//          バッファサイズを求める サンプルレート8kHz  モノラル 16ビット/サンプル
        int audioBuf = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT) * 2;
//          レコーダの取得  サンプルレート8kHz  モノラル 16ビット/サンプル
        audioRec =  new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBuf);
//          バッファ
        byte[] buffer = new byte[audioBuf];
// 録音開始
        audioRec.startRecording();
        try{
// フラグが落ちるまでループ  例外処理略
            new Thread(new Runnable(){
                int audioBuf = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT) * 2;
                byte[] buffer = new byte[audioBuf];
                FileOutputStream out;
                String file_name="/rec.raw";
                public void run(){
                    try{
                        File recFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + file_name);
                        if(recFile.isFile()){
                            recFile.delete();
                        }
                        recFile.createNewFile();
                        FileOutputStream out =  new FileOutputStream(recFile);
                        while(bIsRecording) {
                            audioRec.read(buffer, 0, audioBuf);
                            out.write(buffer);
                        }
                        Log.d("rec","rec stop");
                        audioRec.stop();
                        out.close();
                    }catch(Exception e){
                        Log.d("error",e.toString());
                        System.out.println("error");
                    }

                    out = null;
                }
            }).start();
        }catch(Exception e){
           Log.d("error",e.toString());
           Log.d("error",e.getMessage());
        }
    }
    public void rec_stop(){
        if (bIsRecording) {
            bIsRecording = false;
        }
    }
    
    
    public void convert(){
        try{
            // 録音したファイル
            File recFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rec.raw");
            // WAVファイル
            File wavFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + this.fileName);
            // ストリーム
            FileInputStream in = new FileInputStream(recFile);
            FileOutputStream out = new FileOutputStream(wavFile);
            int fileLeng= (int) in.getChannel().size();
            byte[] header = createHeader(SAMPLING_RATE, fileLeng);
            // ヘッダの書き出し
            out.write(header);

            // 録音したファイルのバイトデータ読み込み
            int n = 0, offset = 0;
//        byte[] buffer = new byte[(int) recFile.length()];
            byte[] buffer = new byte[fileLeng];
            while (offset < buffer.length
                    && (n = in.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += n;
            }
            // バイトデータ書き込み
            out.write(buffer);

            // 終了
            in.close();
            out.close();
        }catch(Exception e){
            System.out.println("convert miss");
        }
        
    }
    public static byte[] createHeader(int sampleRate, int datasize) {
        byte[] byteRIFF = {'R', 'I', 'F', 'F'};
        byte[] byteFilesizeSub8 = intToBytes((datasize + 36));  // ファイルサイズ-8バイト数
        byte[] byteWAVE = {'W', 'A', 'V', 'E'};
        byte[] byteFMT_ = {'f', 'm', 't', ' '};
        byte[] byte16bit = intToBytes(16);                  // fmtチャンクのバイト数
        byte[] byteSamplerate = intToBytes(sampleRate);     // サンプルレート
        byte[] byteBytesPerSec = intToBytes(sampleRate * 2);    // バイト/秒 = サンプルレート x 1チャンネル x 2バイト
        byte[] bytePcmMono = {0x01, 0x00, 0x01, 0x00};      // フォーマットID 1 =リニアPCM  ,  チャンネル 1 = モノラル
        byte[] byteBlockBit = {0x02, 0x00, 0x10, 0x00};     // ブロックサイズ2バイト サンプルあたりのビット数16ビット
        byte[] byteDATA = {'d', 'a', 't', 'a'};
        byte[] byteDatasize = intToBytes(datasize);         // データサイズ

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(byteRIFF);
            out.write(byteFilesizeSub8);
            out.write(byteWAVE);
            out.write(byteFMT_);
            out.write(byte16bit);
            out.write(bytePcmMono);
            out.write(byteSamplerate);
            out.write(byteBytesPerSec);
            out.write(byteBlockBit);
            out.write(byteDATA);
            out.write(byteDatasize);

        } catch (Exception e) {
            return out.toByteArray();
        }

        return out.toByteArray();
    }

    // int型32ビットデータをリトルエンディアンのバイト配列にする
    public static byte[] intToBytes(int value) {
        byte[] bt = new byte[4];
        bt[0] = (byte) (value & 0x000000ff);
        bt[1] = (byte) ((value & 0x0000ff00) >> 8);
        bt[2] = (byte) ((value & 0x00ff0000) >> 16);
        bt[3] = (byte) ((value & 0xff000000) >> 24);
        return bt;
    }
    
    
}
