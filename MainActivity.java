package com.verity.app;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
    LinearLayout root, chatList; TextView status; EditText input; TextToSpeech tts;
    final int VOICE=20;
    // Put your deployed Verity v3 server URL here, e.g. https://your-service.onrender.com
    static final String SERVER_URL = "https://YOUR-VERITY-SERVER.onrender.com";

    int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    TextView tv(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(36,36,36));t.setPadding(dp(12),dp(8),dp(12),dp(8));return t;}
    Button bt(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b;}

    @Override public void onCreate(Bundle b){super.onCreate(b);tts=new TextToSpeech(this,x->{});showHome(); if(getIntent().getBooleanExtra("open_voice", false)){ new Handler().postDelayed(this::startVoice, 400); }}
    void base(String title){root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(10),dp(8),dp(10),dp(8));root.setBackgroundColor(Color.rgb(255,248,232));setContentView(root);TextView h=tv(title,25);h.setGravity(Gravity.CENTER);h.setTypeface(null,1);root.addView(h,new LinearLayout.LayoutParams(-1,dp(60)));}

    void showHome(){
        base("VERITY");
        ImageView face=new ImageView(this);face.setImageResource(R.drawable.verity_face);face.setScaleType(ImageView.ScaleType.CENTER_INSIDE);root.addView(face,new LinearLayout.LayoutParams(-1,dp(180)));
        status=tv("안녕,난 베리티야. 난 너의 개인 도우미지! 아무거나 물어봐 난 모든지알거든!",17);status.setGravity(Gravity.CENTER);root.addView(status);
        Button talk=bt("🎙️ 베리티와 대화");talk.setOnClickListener(v->showChat());root.addView(talk,new LinearLayout.LayoutParams(-1,dp(58)));
        Button hist=bt("🗂️ 대화 기록");hist.setOnClickListener(v->showHistory());root.addView(hist,new LinearLayout.LayoutParams(-1,dp(58)));
        Button rec=bt("🔴 음성녹음");rec.setOnClickListener(v->{status.setText("녹음할 말을 해주세요!");speak("녹음할 말을 해주세요!");});root.addView(rec,new LinearLayout.LayoutParams(-1,dp(58)));
    }

    void showChat(){
        base("🧠 베리티 대화");
        ScrollView sv=new ScrollView(this);chatList=new LinearLayout(this);chatList.setOrientation(LinearLayout.VERTICAL);sv.addView(chatList);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);
        input=new EditText(this);input.setHint("베리티에게 말해보세요");bar.addView(input,new LinearLayout.LayoutParams(0,dp(58),1));
        Button send=bt("보내기");send.setOnClickListener(v->sendText());bar.addView(send,new LinearLayout.LayoutParams(dp(90),dp(58)));
        Button voice=bt("🎙️");voice.setOnClickListener(v->startVoice());bar.addView(voice,new LinearLayout.LayoutParams(dp(65),dp(58)));
        root.addView(bar);Button home=bt("← 홈");home.setOnClickListener(v->showHome());root.addView(home);
    }

    void addMsg(String who,String msg){TextView t=tv(who+"\n"+msg,16);t.setBackgroundColor(Color.WHITE);chatList.addView(t,new LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT));}

    void sendText(){String s=input.getText().toString().trim();if(s.isEmpty())return;addMsg("나",s);input.setText("");callAI(s);}

    void callAI(final String text){
        addMsg("베리티","생각 중...");
        new Thread(()->{
            try{
                URL u=new URL(SERVER_URL+"/api/chat");HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
                JSONObject body=new JSONObject();body.put("message",text);OutputStream os=c.getOutputStream();os.write(body.toString().getBytes("UTF-8"));os.close();
                BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream(),"UTF-8"));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);
                JSONObject out=new JSONObject(sb.toString());String answer=out.optString("text","응답을 받지 못했어.");
                runOnUiThread(()->{if(chatList.getChildCount()>0)chatList.removeViewAt(chatList.getChildCount()-1);addMsg("베리티",answer);speak(answer);});
            }catch(Exception e){runOnUiThread(()->{if(chatList.getChildCount()>0)chatList.removeViewAt(chatList.getChildCount()-1);addMsg("베리티","서버 연결을 확인해줘.");speak("서버 연결을 확인해줘.");});}
        }).start();
    }

    void startVoice(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},10);return;}
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"베리티에게 말해보세요");startActivityForResult(i,VOICE);
    }
    @Override protected void onActivityResult(int r,int code,Intent data){super.onActivityResult(r,code,data);if(r==VOICE&&code==RESULT_OK&&data!=null){ArrayList<String>a=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty()){input.setText(a.get(0));sendText();}}}
    void showHistory(){base("🗂️ 대화 기록");root.addView(tv("현재 버전에서는 대화 화면을 열어 바로 이어서 사용할 수 있어.",17));Button b=bt("← 홈");b.setOnClickListener(v->showHome());root.addView(b);}
    void speak(String s){if(tts!=null)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"verity");}
    @Override protected void onDestroy(){if(tts!=null)tts.shutdown();super.onDestroy();}
}
