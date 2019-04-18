package com.google.sample.traffic_jam_horizontal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class HomeActivity extends AppCompatActivity
{
    private static int Splash_times_out=1500;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent Mainintent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(Mainintent);
                finish();
            }
        },Splash_times_out);
    }
}
