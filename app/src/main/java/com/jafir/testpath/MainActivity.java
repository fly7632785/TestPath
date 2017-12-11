package com.jafir.testpath;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

    }
    public void move(View view) {
        ((MyView)findViewById(R.id.pathview)).startMove(400);
    }

    public void reset(View view) {
        ((MyView)findViewById(R.id.pathview)).reset();
    }
}
