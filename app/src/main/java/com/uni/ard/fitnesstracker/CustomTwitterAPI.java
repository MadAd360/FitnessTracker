package com.uni.ard.fitnesstracker;

/**
 * Created by Adam on 26/03/2015.
 */

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

import retrofit.http.GET;
import retrofit.http.Query;

class CustomTwitterAPI extends TwitterApiClient {
    public CustomTwitterAPI(TwitterSession session) {
        super(session);
    }

    /**
     * Provide CustomService with defined endpoints
     */
    public RetweetService getRetweetService() {
        return getService(RetweetService.class);
    }
}

// example users/show service endpoint
interface RetweetService {
    @GET("/1.1/statuses/retweets.json")
    void retweets(@Query("id") Long id, @Query("count") Long count, @Query("trim_user") Boolean trim, com.twitter.sdk.android.core.Callback<java.util.List<com.twitter.sdk.android.core.models.Tweet>> callback);
}
