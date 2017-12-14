package com.example.ankitpatidar.democheck.view;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;


import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ankitpatidar.democheck.model.ListItemHolder;
import com.example.ankitpatidar.democheck.R;
import com.squareup.picasso.Picasso;

/**
 * Created by ankitpatidar on 29/10/17.
 *
 * Detail Activity of each project listed in list.
 * Here i used collapsing bar layout for making image animation for effective.
 * and also animation is used when opening detail activity from listview called as Shared element animation.
 */

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);

        //Animation effect on enter of activity
        TransitionInflater inflater = TransitionInflater.from(this);
        Transition transition = inflater.inflateTransition(R.transition.transition);
        getWindow().setSharedElementEnterTransition(transition);

        //Image view for displaying image on app Bar
        ImageView imageView = (ImageView) findViewById(R.id.thumbnailImage);

        //Getting data from intent
        ListItemHolder listItemHolder = (ListItemHolder) getIntent().getSerializableExtra("Data");


        //Picasso for displaying image in detail view
        Picasso.with(this).load(listItemHolder.getImg_url()).error(R.drawable.placeholder_thumbnail).placeholder(R.drawable.placeholder_thumbnail).into(imageView);

        TextView textView = (TextView) findViewById(R.id.titleTextView);

        textView.setText(listItemHolder.getTitle());

        //Toolbar and collapsing layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingLayout);


        //Webview for showing detail informaiton related to project
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        if (isNetworkAvailable(this)) {
            webView.loadUrl("http://kickstarter.com" + listItemHolder.getUrl());
        }
        webView.setWebViewClient(new WebViewClient() {

        });

        setSupportActionBar(toolbar);
        collapsingToolbarLayout.setTitle("Description");
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        //Navigation icon on toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    //Checking network connectivity
    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


}
