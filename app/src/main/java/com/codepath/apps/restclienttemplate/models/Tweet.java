package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final String TAG = "Tweet.java";

    public String body;
    public String createdAt;
    public String imgMedia;
    public String statusId;
    public User user;
    public Integer retweetCount;
    public Integer likeCount;
    public Long numId;
    public boolean favorited;
    public boolean retweeted;


    public Tweet() {
    }

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = tweet.getRelativeTimeAgo(jsonObject.getString("created_at"));
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.statusId = jsonObject.getString("id_str");
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.likeCount = (Integer) jsonObject.get("favorite_count");
        tweet.numId = jsonObject.getLong("id");
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweeted = jsonObject.getBoolean("retweeted");

       if(jsonObject.getJSONObject("entities").has("media")) {
            JSONArray media = jsonObject.getJSONObject("entities").getJSONArray("media");
            //Log.i("Tweet", media.toString());
            tweet.imgMedia = media.getJSONObject(0).getString("media_url_https");
            //Log.i("Tweet", tweet.imgMedia);
        } else {
            tweet.imgMedia = "";
        }

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for(int i = 0; i <jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (ParseException e) {
            Log.i(TAG, "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }

    public static String[] getTimeAndDatePosted(String rawJsonDate) {
        String[] timeAndDate = new String[2];
        timeAndDate[0] = rawJsonDate.substring(4, 11) + rawJsonDate.substring(rawJsonDate.length()-4);
        //String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

        String time = rawJsonDate.substring(11,16);
        if(Integer.valueOf(time.substring(0,2)) >= 12 && Integer.valueOf(time.substring(0,2)) <= 23) {
            if(Integer.valueOf(time.substring(0,2)) >12) {
                time = String.valueOf(Integer.valueOf(time.substring(0,2)) - 12) + time.substring(2);
            }
            time += " PM";
        } else if(Integer.valueOf(time.substring(0,2)) >= 1 && Integer.valueOf(time.substring(0,2)) <= 11) {
            time += " AM";
        } else {
            time = "12" + time.substring(2) + "AM";
        }

        timeAndDate[1] = time;
        return timeAndDate;
    }

}

