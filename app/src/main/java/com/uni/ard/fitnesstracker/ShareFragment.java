package com.uni.ard.fitnesstracker;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ShareFragment extends Fragment {

    private DBAdapter mDbHelper;
    Long rowId;
    String facebookId;
    Long twitterId;

    private ListView lv;

    private Button likeButton;
    private Button commentButton;
    private Button favouriteButton;
    private Button retweetButton;
    private Button shareFacebookButton;
    private Button shareTwitterButton;
    private Button loginButton;

    View facebookLayout;
    View twitterLayout;
    View listLayout;

    boolean likesSelected;

    private UiLifecycleHelper uiHelper;


    public static ShareFragment newInstance(Long rowId) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        args.putLong(DBAdapter.KEY_ROWID, rowId);
        fragment.setArguments(args);
        return fragment;
    }

    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DBAdapter(getActivity());
        mDbHelper.open();


        uiHelper = new UiLifecycleHelper(getActivity(), null);
        uiHelper.onCreate(savedInstanceState);

        Bundle extras = getArguments();

        rowId = extras.getLong(DBAdapter.KEY_ROWID);
        likesSelected = true;


        Cursor cursor = mDbHelper.fetchGoal(rowId);
        cursor.moveToFirst();
        facebookId = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_FACEBOOK_ID));
        twitterId = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TWITTER_ID));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_facebook, container, false);

        likeButton = (Button) view.findViewById(R.id.facebookLikes);
        commentButton = (Button) view.findViewById(R.id.facebookComments);
        favouriteButton = (Button) view.findViewById(R.id.twitterFavourites);
        retweetButton = (Button) view.findViewById(R.id.twitterRetweets);
        shareFacebookButton = (Button) view.findViewById(R.id.shareFacebook);
        shareTwitterButton = (Button) view.findViewById(R.id.shareTwitter);
        loginButton = (Button) view.findViewById(R.id.loginSocial);
        facebookLayout = view.findViewById(R.id.facebookLayout);
        twitterLayout = view.findViewById(R.id.twitterLayout);
        listLayout = view.findViewById(R.id.listLayout);

        lv = (ListView) view.findViewById(R.id.listView);
        lv.setEmptyView(view.findViewById(android.R.id.empty));
        updateView();

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLikeList();
            }
        });
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommentList();
            }
        });

        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavouriteList();
            }
        });
        retweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRetweetList();
            }
        });

        shareFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFacebook();
            }
        });
        shareTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTwitter();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSocial();
            }
        });

        return view;
    }

    public void setupFacebook() {
        shareFacebookButton.setVisibility(View.GONE);
        facebookLayout.setVisibility(View.VISIBLE);
        listLayout.setVisibility(View.VISIBLE);
        setLikeCount();
        setCommentCount();
    }

    public void setupTwitter() {
        shareTwitterButton.setVisibility(View.GONE);
        twitterLayout.setVisibility(View.VISIBLE);
        listLayout.setVisibility(View.VISIBLE);
        setFavouriteCount();
        setRetweetCount();
    }

    public void setLikeCount() {
        Session session = Session.getActiveSession();
        Bundle params = new Bundle();
        params.putBoolean("summary", true);
        Request request = new Request(session, "/" + facebookId + "/likes", params, HttpMethod.GET, new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                try {
                    JSONObject res = response.getGraphObject().getInnerJSONObject().getJSONObject("summary");
                    int numberOf = res.getInt("total_count");
                    likeButton.setText("Likes: " + numberOf);
                } catch (Exception e) {
                }
            }
        });
        RequestAsyncTask task = new RequestAsyncTask(request);
        task.execute();
    }

    //
    public void setCommentCount() {
        Session session = Session.getActiveSession();
        Bundle params = new Bundle();
        params.putBoolean("summary", true);
        Request request = new Request(session, "/" + facebookId + "/comments", params, HttpMethod.GET, new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                try {
                    JSONObject res = response.getGraphObject().getInnerJSONObject().getJSONObject("summary");
                    int numberOf = res.getInt("total_count");
                    commentButton.setText("Comments: " + numberOf);
                } catch (Exception e) {
                }
            }
        });
        RequestAsyncTask task = new RequestAsyncTask(request);
        task.execute();
    }

    public void setFavouriteCount() {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
// Can also use Twitter directly: Twitter.getApiClient()
        StatusesService statusesService = twitterApiClient.getStatusesService();
        statusesService.show(twitterId, null, null, null, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                favouriteButton.setText("Favourites: " + result.data.favoriteCount);
            }

            public void failure(TwitterException exception) {
                //Do something on failure
            }
        });
    }

    public void setRetweetCount() {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
// Can also use Twitter directly: Twitter.getApiClient()
        StatusesService statusesService = twitterApiClient.getStatusesService();
        statusesService.show(twitterId, null, null, null, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                retweetButton.setText("Retweets: " + result.data.retweetCount);
            }

            public void failure(TwitterException exception) {
                //Do something on failure
            }
        });
    }

    public void shareFacebook() {
        Session session = Session.getActiveSession();

        Bundle params = new Bundle();
        params.putString("message", generateMessage());
        params.putString("link", "https://www.facebook.com/MoonwalkFitness");
        new Request(
                session,
                "/me/feed",
                params,
                HttpMethod.POST,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        try {
                            String res = response.getGraphObject().getInnerJSONObject().getString("id");
                            facebookId = res;
                            mDbHelper.updateGoalFacebookId(rowId, facebookId);
                            Toast logToast = Toast.makeText(getActivity(), "Facebook status shared", Toast.LENGTH_LONG);
                            logToast.show();
                            setupFacebook();
                        } catch (Exception e) {
                        }
                    }
                }
        ).executeAsync();
    }

    public void shareTwitter() {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
//        TwitterSession session =
//                Twitter.getSessionManager().getActiveSession();
//        Long userid = session.getUserId();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        statusesService.update(generateMessage() + " https://www.facebook.com/MoonwalkFitness #Moonwalk", null, null, null, null, null, null, null, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                twitterId = result.data.id;
                mDbHelper.updateGoalTwitterId(rowId, twitterId);
                Toast logToast = Toast.makeText(getActivity(), "Twitter status shared", Toast.LENGTH_LONG);
                logToast.show();
                setupTwitter();
            }

            public void failure(TwitterException exception) {
                //Do something on failure
            }
        });
    }

    public void setLikeList() {

        Session session = Session.getActiveSession();
        final Request request = new Request(session, "/" + facebookId + "/likes", null, HttpMethod.GET, new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                try {
                    JSONArray resultArray = response.getGraphObject().getInnerJSONObject().getJSONArray("data");

                    List<String> likeArray = new ArrayList<String>();

                    for (int i = 0; i < resultArray.length(); i++) {
                        String name = resultArray.getJSONObject(i).getString("name");
                        likeArray.add(name + " likes your post");
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            getActivity(),
                            R.layout.comment_row,
                            likeArray);

                    lv.setAdapter(arrayAdapter);
                } catch (Exception e) {
                }
            }
        });
        RequestAsyncTask task = new RequestAsyncTask(request);
        task.execute();
    }

    public void setCommentList() {

        Session session = Session.getActiveSession();
        final Request request = new Request(session, "/" + facebookId + "/comments", null, HttpMethod.GET, new Request.Callback() {

            @Override
            public void onCompleted(Response response) {
                try {
                    JSONArray resultArray = response.getGraphObject().getInnerJSONObject().getJSONArray("data");

                    List<String> commentArray = new ArrayList<String>();

                    for (int i = 0; i < resultArray.length(); i++) {
                        String name = resultArray.getJSONObject(i).getJSONObject("from").getString("name");
                        String message = resultArray.getJSONObject(i).getString("message");
                        commentArray.add(name + " said: " + message);
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            getActivity(),
                            R.layout.comment_row,
                            commentArray);

                    lv.setAdapter(arrayAdapter);
                } catch (Exception e) {
                }
            }
        });
        RequestAsyncTask task = new RequestAsyncTask(request);
        task.execute();
    }

    public void setFavouriteList() {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
// Can also use Twitter directly: Twitter.getApiClient()
        StatusesService statusesService = twitterApiClient.getStatusesService();
        statusesService.show(twitterId, null, null, null, new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                List<String> commentArray = new ArrayList<String>();
                int number = result.data.favoriteCount;
                if (number == 1) {
                    commentArray.add("Tweet favourited by " + result.data.favoriteCount + " person");
                } else {
                    commentArray.add("Tweet favourited by " + result.data.favoriteCount + " people");
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.comment_row,
                        commentArray);

                lv.setAdapter(arrayAdapter);
            }

            public void failure(TwitterException exception) {
                //Do something on failure
            }
        });
    }

    public void setRetweetList() {
        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();
        CustomTwitterAPI twitterApiClient = new CustomTwitterAPI(session);
        RetweetService statusesService = twitterApiClient.getRetweetService();
        statusesService.retweets(twitterId, null, null, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                List<String> commentArray = new ArrayList<String>();
                for (Tweet tweet : result.data) {
                    String name = tweet.user.name;
                    commentArray.add("Retweeted by " + name);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.comment_row,
                        commentArray);

                lv.setAdapter(arrayAdapter);
            }

            public void failure(TwitterException exception) {
                //Do something on failure
            }
        });
    }

    public String generateMessage() {
        Cursor cursor = mDbHelper.fetchGoal(rowId);
        cursor.moveToFirst();
        boolean bothType = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_DUAL)) > 0;
        boolean climbType = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
        if (bothType) {
            int walkTarget = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK));
            int climbTarget = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB));
            String walkUnit = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK_UNIT));
            String climbUnit = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB_UNIT));
            String message = "I have walked " + walkTarget + " " + walkUnit + " and climbed " + climbTarget + " " + climbUnit + "!!!!";
            return message;
        } else {
            if (climbType) {
                int climbTarget = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB));
                String climbUnit = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB_UNIT));
                String message = "I have climbed " + climbTarget + " " + climbUnit + "!!!!";
                return message;
            } else {
                int walkTarget = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK));
                String walkUnit = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK_UNIT));
                String message = "I have walked " + walkTarget + " " + walkUnit + "!!!!";
                return message;
            }
        }
    }

    public void updateView() {
        listLayout.setVisibility(View.GONE);
        loginButton.setVisibility(View.GONE);

        final Session facebookSession = Session.getActiveSession();
        if (facebookSession != null && facebookSession.isOpened()) {
            if (facebookId == null) {
                shareFacebookButton.setVisibility(View.VISIBLE);
                facebookLayout.setVisibility(View.GONE);
            } else {
                setupFacebook();
            }
        } else {
            facebookLayout.setVisibility(View.GONE);
            shareFacebookButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }

        TwitterSession twitterSession =
                Twitter.getSessionManager().getActiveSession();
        if (twitterSession != null) {
            if (twitterId == 0) {
                shareTwitterButton.setVisibility(View.VISIBLE);
                twitterLayout.setVisibility(View.GONE);
            } else {
                setupTwitter();
            }
        } else {
            shareTwitterButton.setVisibility(View.GONE);
            twitterLayout.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    public void loginSocial() {
        Intent i = new Intent(getActivity(), SocialMediaActivity.class);
        startActivity(i);
    }
}
