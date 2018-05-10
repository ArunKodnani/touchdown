package com.example.arunkodnani.touchdown;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Welcome extends AppCompatActivity {
    static String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        String url = "https://bvlxit8h9a.execute-api.us-east-1.amazonaws.com/BetaStage/firstlogon";
        String userID = AuthenticatorActivity.credentialsProvider.getIdentityId();
        String query="";
        try {
            query = String.format("userID=%s", URLEncoder.encode(userID,charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        FirstLogonCall callAPI = new FirstLogonCall();
        Object response = null;
        response =callAPI.execute(new Object[]{url,query,this});
    }
}
