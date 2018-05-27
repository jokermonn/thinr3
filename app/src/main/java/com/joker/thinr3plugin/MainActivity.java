package com.joker.thinr3plugin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getString(R.string.app_name);

    CustomView customView = findViewById(R.id.cv);
    customView.update();
  }
}
