package com.example.arunkodnani.touchdown;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static com.example.arunkodnani.touchdown.MainActivity.charset;
//AuthenticatorActivity.credentialsProvider.getIdentity

public class FirstLogonCall extends AsyncTask {
    Welcome w;
    @Override
    protected Object doInBackground(Object[] objects) {
        System.out.println("Debig: input to async call"+objects[0]+" "+objects[1]);
        w = (Welcome) objects[2];
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(objects[0].toString() + "?" + objects[1].toString()).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setRequestProperty("Accept-Charset", charset);
        InputStream response = null;
        try {
            response = connection.getInputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            //System.out.println(responseBody);
            System.out.println("Debug: Responsebody in function = "+responseBody);
            return responseBody;
        }catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    protected void onPostExecute(Object result) {
        System.out.println("Debug: Yes or No "+result.toString());
     if(result.toString().equals("\"no\""))
        {
            Intent intent;
            intent = new Intent(w, MainActivity.class);
            w.startActivity(intent);
        }
        else
        {

            Intent intent;
            intent = new Intent(w, Preferences.class);
            w.startActivity(intent);
        }
        return;
    }
}
