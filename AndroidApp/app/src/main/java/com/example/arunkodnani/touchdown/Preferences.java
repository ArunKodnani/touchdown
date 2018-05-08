package com.example.arunkodnani.touchdown;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Preferences extends AppCompatActivity {

    static String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    static List<String> al = new ArrayList<String>();
    XmlPullParserFactory xmlFactoryObject;
    XmlPullParser myparser;
    CheckBox team1,team2,team3,team4,team;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        team = (CheckBox) findViewById(R.id.team);
        team1 = (CheckBox) findViewById(R.id.team1);
        team2 = (CheckBox) findViewById(R.id.team2);
        team3 = (CheckBox) findViewById(R.id.team3);
        team4 = (CheckBox) findViewById(R.id.team4);

        String user= AuthenticatorActivity.credentialsProvider.getIdentityId();
        //TO DO  Check if the user exists in the database

        String url = "https://bvlxit8h9a.execute-api.us-east-1.amazonaws.com/BetaStage/getclicks";
        String query="";
        try {
            query = String.format("userID=%s", URLEncoder.encode(user,charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        PreferencesCall callAPI = new PreferencesCall();
        Object response = null;
        response =callAPI.execute(new Object[]{url,query,this});
    }
    public void updateDisplayList(Object result){

        final HashMap<String,String> gameStore = new HashMap<>();
        try {
            xmlFactoryObject = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        try {
            myparser = xmlFactoryObject.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes(StandardCharsets.UTF_8));
        try {
            myparser.setInput(stream, null);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        int event = 0;
        String gameID="";
        String hometeam="",awayteam="";
        try {
            event = myparser.getEventType();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        while (event != XmlPullParser.END_DOCUMENT)  {
            String name=myparser.getName();
            switch (event){
                case XmlPullParser.START_TAG:
                    if(name.equals("game")){
                        gameID = myparser.getAttributeValue(null,"id");
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if(name.equals("home")){
                        hometeam = myparser.getAttributeValue(null,"name");
                    }
                    else if(name.equals("away")){
                        awayteam =  myparser.getAttributeValue(null,"name");
                        String gameName = hometeam+ " Vs "+awayteam+" "+gameID;
                        al.add(gameName);
                        gameStore.put(gameName,gameID);
                    }
                    break;
            }
            try {
                event = myparser.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //al.add(result.toString());
        System.out.println("Debug: Display List Updated");

        return;
    }
}
