package com.example.arunkodnani.touchdown;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Articles extends AppCompatActivity {
    static List<String> al = new ArrayList<String>();
    static String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);
//
//        CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
//                this.getApplicationContext(), "identity-pool-id", Regions.US_EAST_1);

        al.clear();
        al.add("Articles");
        lv = (ListView) findViewById(R.id.teamslist2);
        String url = "https://bvlxit8h9a.execute-api.us-east-1.amazonaws.com/BetaStage/getarticles";
        String userID ="2";
        String query="";
        try {
            query = String.format("userID=%s", URLEncoder.encode(userID,charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        ArticlesCall callAPI = new ArticlesCall();
        Object response = null;
        response =callAPI.execute(new Object[]{url,query,this});










    }

    public void goToHome(View view) {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void updateDisplayList(Object result) throws JSONException{
        final HashMap<String,String> summaries = new HashMap<>();
        final HashMap<String,String> articleid = new HashMap<>();
        JSONObject reader=null;
        reader = new JSONObject(result.toString());
        JSONObject messages = reader.getJSONObject("messages");
        JSONArray ItemsArray = messages.getJSONArray("Items");
        JSONObject almostdata = ItemsArray.getJSONObject(0);
        JSONObject data = almostdata.getJSONObject("data");
        JSONArray names = data.names();

        al.add(AuthenticatorActivity.credentialsProvider.getIdentityId());
        for(int i=0;i<names.length();i++){
            JSONArray articleArray = data.getJSONArray(names.getString(i));
            JSONObject articleObject = articleArray.getJSONObject(0);
            al.add(articleObject.getString("Title"));
            summaries.put(articleObject.getString("Title"),articleObject.getString("Summary"));
            //summaries.put(articleObject.getString("Title"),names.getString(i));
        }
        System.out.println("Debug:  "+names.toString());
        al.add(names.toString());

        //al.add(result.toString());
        System.out.println("Debug: Display List Updated");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                Articles.this,
                android.R.layout.simple_list_item_1,
                al);

        lv.setAdapter(arrayAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                Intent intent = new Intent(Articles.this, Webviewer.class);
                intent.putExtra("id","http://www.tutorialspoint.com");
                startActivity(intent);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long arg) {
                Toast.makeText(getApplicationContext(),summaries.get(al.get(position)),Toast.LENGTH_LONG).show();
                return true;
            }
        });





        return;


    }
}
