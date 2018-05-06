package com.example.arunkodnani.touchdown;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

import com.amazonaws.mobile.client.AWSMobileClient;


import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    static List<String> al = new ArrayList<String>();
    XmlPullParserFactory xmlFactoryObject;
    XmlPullParser myparser;
    ListView lv2,lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("MainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        al.clear();
        lv = (ListView) findViewById(R.id.teamslist1);

        String url = "https://api.sportradar.us/nfl-ot2/games/2017/PST/03/schedule.xml";
        String api_key ="rr7maet8u7uataccpcqt5e5j";
        String query="";
        try {
            query = String.format("api_key=%s", URLEncoder.encode(api_key,charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        games callAPI = new games();
        Object response = null;

        response = callAPI.execute(new Object[]{url,query,this});
        while (response==null){

        }
        //Object response = callAPI.doInBackground(new String[]{url,query});
        System.out.println("Debug: response in main thread"+response.toString());

        // Instanciating an array list (you don't need to do this,
        // you already have yours).



        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                al);

        lv.setAdapter(arrayAdapter);

        //lv.setTextColor(Color.WHITE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                Intent intent = new Intent(MainActivity.this, TeamDetails.class);
                startActivity(intent);
            }
        });


        /*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/
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
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                al);

        lv.setAdapter(arrayAdapter);

        //lv2.setAdapter(arrayAdapter);
        //lv.setTextColor(Color.BLACK);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                String valueToSend = gameStore.get(al.get(position));
//                Toast.makeText(getApplicationContext(),valueToSend,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, TeamDetails.class);
                intent.putExtra("id",valueToSend);
                startActivity(intent);
            }
        });
        return;
    }

    @Override
    public void onBackPressed() {
        /*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Teams) {
            // Handle the camera action
        } else if (id == R.id.nav_Articles) {

        } else if (id == R.id.nav_Leagues) {

        } else if (id == R.id.nav_Schedule) {

        } else if (id == R.id.nav_share) {

        }

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void nextActivity(View view) {
        Intent intent;
        if(view == findViewById(R.id.btnteams)) {
            intent = new Intent(this, TeamDetails.class);
            startActivity(intent);
        }
        else if (view == findViewById(R.id.articles)) {
            intent = new Intent(this, Articles.class);
            startActivity(intent);

        }

    }
    public void goToHome(View view) {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public static String requestApi(String url,String query) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url + "?" + query).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);
        InputStream response = connection.getInputStream();
        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            //System.out.println(responseBody);
            return responseBody;
        }
    }
}
