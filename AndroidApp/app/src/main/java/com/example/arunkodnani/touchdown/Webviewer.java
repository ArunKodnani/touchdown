package com.example.arunkodnani.touchdown;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Webviewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webviewer);
        String url = getIntent().getStringExtra("id");
        WebView wv1=(WebView)findViewById(R.id.webview);
        //wv1.setWebViewClient(new MyBrowser());
        //wv1.getSettings().setLoadsImagesAutomatically(true);
        //wv1.getSettings().setJavaScriptEnabled(true);
        //wv1.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wv1.setWebViewClient(new WebViewClient());
        wv1.loadUrl(url);
    }

}

class MyBrowser extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}
