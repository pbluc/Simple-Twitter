package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements TweetsAdapter.ListItemClickListener {

    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;

    TwitterClient client;
    RecyclerView rvTweets;
    Button btnLogout;
    private SwipeRefreshLayout swipeContainer;
    ProgressBar pb;

    List<Tweet> tweets;
    TweetsAdapter adapter;

    private EndlessRecyclerViewScrollListener scrollListener;

    Long lowestTweetIdSeen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        btnLogout = findViewById(R.id.btnLogout);
        pb = findViewById(R.id.pbLoading);
        // Lookup the swipe container
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Find the recycler view
        rvTweets = findViewById(R.id.rvTweets);

        // Initialize list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        // Recycler view setup: layout manager and the adapter
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(adapter);

        // Retain an instance so that we can call 'resetState()' for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        populateHomeTimeline();

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Code to refresh the list here
                // Make sure to call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully
                fetchTimelineAsync(0);
            }
        });
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter
    private void loadNextDataFromApi(int page) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
    }

    @Override
    public void onListItemClick(int position) {
        Intent intent = new Intent(this, DetailsActivity.class);
        Log.i(TAG, tweets.get(position).body);
        intent.putExtra("selected tweet", Parcels.wrap(tweets.get(position)));
        startActivity(intent);
    }

    @Override
    public void onReplyTo(int position) {
        Intent intent = new Intent(this, ComposeActivity.class);
        intent.putExtra("tweet user", tweets.get(position).user.screenName);
        intent.putExtra("tweet status id", tweets.get(position).statusId);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onFavorite(final int position) {
        client.favoriteTweet(tweets.get(position).numId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Log.i(TAG, "On Success! Favorited: " + json.toString());
                    Tweet tweet = Tweet.fromJson(json.jsonObject);
                    // Update the RV with the tweet
                    // Modify data source of tweets
                    tweets.set(position, tweet);
                    // Update the adapter
                    adapter.notifyItemChanged(position);
                    scrollListener.resetState();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Favorite tweet post error: " + response);
            }
        });
    }

    @Override
    public void onRetweet(int position) {
        client.retweetPost(tweets.get(position).statusId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Tweet tweet = Tweet.fromJson(json.jsonObject);
                    // Update the RV with the tweet
                    // Modify data source of tweets
                    tweets.add(0, tweet);
                    // Update the adapter
                    adapter.notifyItemInserted(0);
                    scrollListener.resetState();
                    rvTweets.smoothScrollToPosition(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Retweet post error: " + response);
            }
        });
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // 'client' here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    // Clear out old items before appending in the new ones
                    adapter.clear();
                    // the data has come back, add new items to the adapter
                    adapter.addAll(Tweet.fromJsonArray(json.jsonArray));
                    adapter.notifyDataSetChanged();
                    scrollListener.resetState();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Fetch timeline error: " + response);
            }
        });
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                pb.setVisibility(View.VISIBLE);
                Log.i(TAG, "onSuccess! " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    scrollListener.resetState();
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                    e.printStackTrace();
                }
                pb.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure!" + response, throwable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.compose) {
            // Compose icon has been selected
            //Toast.makeText(this, "Compose!", Toast.LENGTH_LONG).show();
            // Navigate to the compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data from the intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Update the RV with the tweet
            // Modify data source of tweets
            tweets.add(0, tweet);
            // Update the adapter
            adapter.notifyItemInserted(0);
            scrollListener.resetState();
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void logoutToRest(View view) {
        pb.setVisibility(View.VISIBLE);
        client.clearAccessToken(); // forget who's logged in
        pb.setVisibility(View.INVISIBLE);
        finish(); // navigate backwards to Login screen
    }
}