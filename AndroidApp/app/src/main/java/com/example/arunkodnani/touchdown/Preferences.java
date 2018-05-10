package com.example.arunkodnani.touchdown;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

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
    static List<String> users = new ArrayList<String>();

    CheckBox team1,team2,team3,team4,team5,team6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        team5 = (CheckBox) findViewById(R.id.team5);
        team1 = (CheckBox) findViewById(R.id.team1);
        team2 = (CheckBox) findViewById(R.id.team2);
        team3 = (CheckBox) findViewById(R.id.team3);
        team4 = (CheckBox) findViewById(R.id.team4);
        team6 = (CheckBox) findViewById(R.id.team6);

        String user = AuthenticatorActivity.credentialsProvider.getIdentityId();
        //TO DO  Check if the user exists in the database

        String url = "https://bvlxit8h9a.execute-api.us-east-1.amazonaws.com/BetaStage/getclicks";
        String query = "";
        try {
            query = String.format("userID=%s", URLEncoder.encode(user, charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        PreferencesCall callAPI = new PreferencesCall();
        Object response = null;
        response = callAPI.execute(new Object[]{url, query, this});
    }

    public void sendTeams(View view) {
        if(team1.isChecked())
        {
            String text=team1.getText().toString();
            users.add(text);
        }
        if(team2.isChecked())
        {
            String text=team2.getText().toString();
            users.add(text);
        }
        if(team3.isChecked())
        {
            String text=team3.getText().toString();
            users.add(text);
        }
        if(team4.isChecked())
        {
            String text=team4.getText().toString();
            users.add(text);
        }
        if(team5.isChecked())
        {
            String text=team5.getText().toString();
            users.add(text);
        }
        if(team6.isChecked())
        {
            String text=team6.getText().toString();
            users.add(text);
        }

        Toast.makeText(Preferences.this,users.toString(),Toast.LENGTH_SHORT).show();
        for(String tosend: users){
            String url = "https://bvlxit8h9a.execute-api.us-east-1.amazonaws.com/BetaStage/insertpreference";
            String userID =AuthenticatorActivity.credentialsProvider.getIdentityId();
            String query="";
            try {
                query = String.format("userID=%s", URLEncoder.encode(userID,charset));
                query = query+"&"+String.format("preference=%s", URLEncoder.encode(tosend,charset));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Toast.makeText(Preferences.this,query,Toast.LENGTH_SHORT).show();
            PreferencesCall callAPI = new PreferencesCall();
            Object response = null;
            response =callAPI.execute(new Object[]{url,query,this});
        }

        Intent intent;
        intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);

    }
}
