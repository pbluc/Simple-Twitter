package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class DetailsActivity extends AppCompatActivity {

    public static final String TAG = "DetailsActivity";

    TwitterClient client;

    ImageView ivProfileImage;
    ImageView ivImgMedia;
    ImageView ivReplyTo;
    ImageView ivRetweet;

    TextView tvName;
    TextView tvScreenName;
    TextView tvBody;
    TextView tvTimePosted;
    TextView tvDatePosted;
    TextView tvRetweetCount;
    TextView tvLikeCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        client = TwitterApp.getRestClient(this);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivImgMedia = findViewById(R.id.ivImgMedia);

        tvName = findViewById(R.id.tvName);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvBody = findViewById(R.id.tvBody);
        tvTimePosted = findViewById(R.id.tvTimePosted);
        tvDatePosted = findViewById(R.id.tvDatePosted);
        tvRetweetCount = findViewById(R.id.tvRetweetCount);
        tvLikeCount = findViewById(R.id.tvLikeCount);

        tvName.setTypeface(tvName.getTypeface(), Typeface.BOLD);

        final Tweet detailTweet = Parcels.unwrap(getIntent().getParcelableExtra("selected tweet"));
        final User detailUser = detailTweet.user;

        Glide.with(this).load(detailUser.profileImageUrl).into(ivProfileImage);
        if(!detailTweet.imgMedia.equals("")) {
            ivImgMedia.setVisibility(View.VISIBLE);
            Glide.with(this).load(detailTweet.imgMedia).centerCrop().into(ivImgMedia);
        } else {
            ivImgMedia.setVisibility(View.GONE);
        }

        tvName.setText(detailUser.name);
        tvScreenName.setText("@" + detailUser.screenName);

        if(detailTweet.retweetCount != 0) {
            tvRetweetCount.setVisibility(View.VISIBLE);
            tvRetweetCount.setText(String.valueOf(detailTweet.retweetCount) + tvRetweetCount.getText());
        } else {
            tvRetweetCount.setVisibility(View.GONE);
        }

        if(detailTweet.likeCount != 0) {
            tvLikeCount.setVisibility(View.VISIBLE);
            tvLikeCount.setText(String.valueOf(detailTweet.likeCount) + tvLikeCount.getText());
        } else {
            tvLikeCount.setVisibility(View.GONE);
        }

        final String statusId = detailTweet.statusId;

        client.getFullTextTweet(statusId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                //pb.setVisibility(View.VISIBLE);
                Log.i(TAG, "onSuccess to retrieve full text");
                try {
                    String fullBodyText = json.jsonObject.getString("full_text");
                    tvBody.setText(fullBodyText);

                    String[] timeAndDatePosted = Tweet.getTimeAndDatePosted(json.jsonObject.getString("created_at"));
                    tvTimePosted.setText(timeAndDatePosted[1]);
                    tvDatePosted.setText(timeAndDatePosted[0]);
                    //pb.setVisibility(View.INVISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retrieve full text tweet", throwable);
            }
        });

    }
}