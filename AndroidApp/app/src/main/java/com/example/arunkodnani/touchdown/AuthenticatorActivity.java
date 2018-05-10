package com.example.arunkodnani.touchdown;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.regions.Regions;

public class AuthenticatorActivity extends AppCompatActivity {

    public static CognitoCachingCredentialsProvider credentialsProvider= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(final AWSStartupResult awsStartupResult) {
                AuthUIConfiguration config =
                        new AuthUIConfiguration.Builder()
                                .userPools(true)  // true? show the Email and Password UI
                                .backgroundColor(Color.BLUE) // Change the backgroundColor
                                .isBackgroundColorFullScreen(true) // Full screen backgroundColor the backgroundColor full screenff
                                .fontFamily("sans-serif-light") // Apply sans-serif-light as the global font
                                .canCancel(true)
                                .build();
                SignInUI signinUI = (SignInUI) AWSMobileClient.getInstance().getClient(AuthenticatorActivity.this, SignInUI.class);
                credentialsProvider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(), // Context
                        "us-east-1:afea43de-7631-4f76-99f4-8db0021db6f3", // Identity Pool ID
                       Regions.US_EAST_1 // Region
                );
                //System.out.println("Check credentials: "+credentialsProvider.getIdentityId()+" " +credentialsProvider.getIdentityPoolId());
                signinUI.login(AuthenticatorActivity.this, Welcome.class).authUIConfiguration(config).execute();
            }
        }).execute();
    }

}
