package com.sheshu.timelapsecam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sheshu.timelapsecam.model.TesseractHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TextRecognitionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mTextView =(TextView) findViewById(R.id.converted_text);

        mTessHelper = new TesseractHelper(this);
        mTessHelper.initTesseract();


    }
    TextView mTextView;
    TesseractHelper mTessHelper;

    @BindView(R.id.convert)
    Button covert;


    @OnClick(R.id.convert)
    void onPressConvert(View v){

        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);

        String output = mTessHelper.processImageView(image);

        mTextView.setText(output);
    }





}
