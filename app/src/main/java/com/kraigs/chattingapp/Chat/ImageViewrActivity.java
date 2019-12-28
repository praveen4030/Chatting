package com.kraigs.chattingapp.Chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.kraigs.chattingapp.R;
import com.squareup.picasso.Picasso;

public class ImageViewrActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewr);

        ImageView imageView;
        String imageUrl;
        TextView textTv;
        imageView = findViewById(R.id.image_viewer);

        textTv = findViewById(R.id.textTv);
        imageUrl = getIntent().getStringExtra("url");
        String text = getIntent().getStringExtra("description");
        if (text!=null){
            textTv.setText(text);
        }

        Picasso.get().load(imageUrl).into(imageView);

    }
}
