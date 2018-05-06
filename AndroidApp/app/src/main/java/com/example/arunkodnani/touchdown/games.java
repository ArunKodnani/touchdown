package com.example.arunkodnani.touchdown;

import android.os.AsyncTask;



import java.io.*;
import java.net.*;
import java.util.Scanner;
import static com.example.arunkodnani.touchdown.MainActivity.charset;


public class games extends AsyncTask {
    MainActivity ma;

    @Override
    protected Object doInBackground(Object[] objects) {
        System.out.println("Debig: input to async call"+objects[0]+" "+objects[1]);
        ma = (MainActivity) objects[2];
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
        }
    }

    protected void onPostExecute(Object result) {
        ma.updateDisplayList(result);
    }
}
