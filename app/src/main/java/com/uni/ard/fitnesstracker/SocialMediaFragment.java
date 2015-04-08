package com.uni.ard.fitnesstracker;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;


public class SocialMediaFragment extends Fragment {


    private LoginButton loginBtn;
    private TwitterLoginButton loginButton;
    private Button logoutButtonTwitter;
    private TextView facebookUser;
    private TextView twitterUser;


    public static SocialMediaFragment newInstance() {
        SocialMediaFragment fragment = new SocialMediaFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SocialMediaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        facebookUser = (TextView) view.findViewById(R.id.userFacebook);
        twitterUser = (TextView) view.findViewById(R.id.userTwitter);

        final Session facebookSession = Session.getActiveSession();
        if (facebookSession != null && facebookSession.isOpened()) {
            Request request = Request.newMeRequest(facebookSession, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (facebookSession == Session.getActiveSession()) {
                        if (user != null) {
                            facebookUser.setText("Logged in as: " + user.getName());
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
        } else {
            facebookUser.setVisibility(View.GONE);
        }

        loginBtn = (LoginButton) view.findViewById(R.id.authButtonFacebook);
        loginBtn.setPublishPermissions(Arrays.asList("publish_actions"));
        loginBtn.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                if (user != null) {
                    facebookUser.setText("Logged in as: " + user.getName());
                    facebookUser.setVisibility(View.VISIBLE);
                } else {
                    facebookUser.setVisibility(View.GONE);
                }
            }
        });


        loginButton = (TwitterLoginButton) view.findViewById(R.id.authButtonTwitter);
        logoutButtonTwitter = (Button) view.findViewById(R.id.logoutButtonTwitter);
        TwitterSession twitterSession =
                Twitter.getSessionManager().getActiveSession();
        if (twitterSession != null) {
            twitterUser.setText("Logged in as: " + twitterSession.getUserName());
            loginButton.setVisibility(View.GONE);
        } else {
            logoutButtonTwitter.setVisibility(View.GONE);
            twitterUser.setVisibility(View.GONE);
        }
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                loginButton.setVisibility(View.GONE);
                logoutButtonTwitter.setVisibility(View.VISIBLE);
                twitterUser.setText("Logged in as: " + result.data.getUserName());
                twitterUser.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });


        logoutButtonTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutTwitter();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    public void logoutTwitter() {
        Twitter.getInstance();
        Twitter.logOut();
        loginButton.setVisibility(View.VISIBLE);
        logoutButtonTwitter.setVisibility(View.GONE);
        twitterUser.setVisibility(View.GONE);
    }


}
