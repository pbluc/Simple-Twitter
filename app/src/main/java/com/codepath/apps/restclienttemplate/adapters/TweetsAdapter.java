package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {
    private static final String TAG = "TweetsAdapter";

    Context context;
    List<Tweet> tweets;

    final private ListItemClickListener mOnClickListener;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets, ListItemClickListener onClickListener) {
        this.context = context;
        this.tweets = tweets;
        this.mOnClickListener = onClickListener;
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // For each row, inflate the layout
    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // Get the data at the position
        Tweet tweet = tweets.get(position);
        // Bind the tweet with view holder
        holder.bind(tweet);
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfileImage;
        ImageView ivImgMedia;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvCreatedAt;
        ImageView ivReplyTo;
        TextView tvReplyCount;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ivImgMedia = itemView.findViewById(R.id.ivImgMedia);
            ivReplyTo = itemView.findViewById(R.id.ivReplyTo);
            tvReplyCount = itemView.findViewById(R.id.tvReplyCount);

            itemView.setOnClickListener(this);

            ivReplyTo.setOnClickListener(this);
        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText("@" + tweet.user.screenName);
            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);
            if(!tweet.imgMedia.equals("")) {
                ivImgMedia.setVisibility(View.VISIBLE);
                Glide.with(context).load(tweet.imgMedia).centerCrop().into(ivImgMedia);
            } else {
                ivImgMedia.setVisibility(View.GONE);
            }

            if(tweet.replyCount != 0) {
                tvReplyCount.setText(String.valueOf(tweet.replyCount));
            } else {
                tvReplyCount.setText("");
            }

            tvCreatedAt.setText(tweet.createdAt);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            switch(view.getId()) {
                case R.id.ivReplyTo:
                    mOnClickListener.onReplyTo(position);
                    break;
                default:
                    mOnClickListener.onListItemClick(position);
                    break;

            }
        }
    }

    public interface ListItemClickListener {
        void onListItemClick(int position);
        void onReplyTo(int position);
    }

}
