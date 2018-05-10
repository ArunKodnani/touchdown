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

    CheckBox teams[] = new CheckBox[32];
            //1,team2,team3,team4,team5,team6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        teams[4] = (CheckBox) findViewById(R.id.team5);
        teams[0] = (CheckBox) findViewById(R.id.team1);
        teams[1] = (CheckBox) findViewById(R.id.team2);
        teams[2] = (CheckBox) findViewById(R.id.team3);
        teams[3] = (CheckBox) findViewById(R.id.team4);
        teams[5] = (CheckBox) findViewById(R.id.team6);
        teams[6] = (CheckBox) findViewById(R.id.team7);
        teams[7] = (CheckBox) findViewById(R.id.team8);
        teams[8] = (CheckBox) findViewById(R.id.team9);
        teams[9] = (CheckBox) findViewById(R.id.team10);
        teams[10] = (CheckBox) findViewById(R.id.team11);
        teams[11] = (CheckBox) findViewById(R.id.team12);
        teams[12] = (CheckBox) findViewById(R.id.team13);
        teams[13] = (CheckBox) findViewById(R.id.team14);
        teams[14] = (CheckBox) findViewById(R.id.team15);
        teams[15] = (CheckBox) findViewById(R.id.team16);
        teams[16] = (CheckBox) findViewById(R.id.team17);
        teams[17] = (CheckBox) findViewById(R.id.team18);
        teams[18] = (CheckBox) findViewById(R.id.team19);
        teams[19] = (CheckBox) findViewById(R.id.team20);
        teams[20] = (CheckBox) findViewById(R.id.team21);
        teams[21] = (CheckBox) findViewById(R.id.team22);
        teams[22] = (CheckBox) findViewById(R.id.team23);
        teams[23] = (CheckBox) findViewById(R.id.team24);
        teams[24] = (CheckBox) findViewById(R.id.team25);
        teams[25] = (CheckBox) findViewById(R.id.team26);
        teams[26] = (CheckBox) findViewById(R.id.team27);
        teams[27] = (CheckBox) findViewById(R.id.team28);
        teams[28] = (CheckBox) findViewById(R.id.team29);
        teams[29] = (CheckBox) findViewById(R.id.team30);
        teams[30] = (CheckBox) findViewById(R.id.team31);
        teams[31] = (CheckBox) findViewById(R.id.team32);

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
        for(int i=0;i<32;i++)
        {
            if(teams[i].isChecked())
            {
                String text=teams[i].getText().toString();
                users.add(text);
            }
        }

        //Toast.makeText(Preferences.this,users.toString(),Toast.LENGTH_SHORT).show();
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
