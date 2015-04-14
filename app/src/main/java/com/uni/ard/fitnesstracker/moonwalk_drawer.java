package com.uni.ard.fitnesstracker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.Date;

import io.fabric.sdk.android.Fabric;


public class moonwalk_drawer extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "Md5R7N1Su1s58AaVjDjuDL1zm";
    private static final String TWITTER_SECRET = "jQKuZlyYX715Kb1IT8mvPnZeP7tf1wiowkL5EufjInfHGICreW";


    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_LOG = 1;

    private DBAdapter mDbHelper;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_moonwalk_drawer);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        //mTitle = getString(R.string.title_section1);

        mDbHelper = new DBAdapter(this);
        mDbHelper.open();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean goalAll = sp.getBoolean(getResources().getString(R.string.pref_goal_all), false);
        boolean socialDisable = sp.getBoolean(getResources().getString(R.string.pref_social_disable), false);
        boolean match = false;
        if (!goalAll) {
            if (position == 0) {
                mTitle = getString(R.string.title_section1);
                fragmentManager.beginTransaction().replace(R.id.container, GoalsFragment.newInstance(true))
                        .commit();
                match = true;
            } else if (position == 1) {
                mTitle = getString(R.string.title_section4);
                fragmentManager.beginTransaction().replace(R.id.container, GoalsFragment.newInstance(false))
                        .commit();
                match = true;
            } else {
                position = position - 2;
            }
        } else {
            if (position == 0) {
                mTitle = "Goals";
                fragmentManager.beginTransaction().replace(R.id.container, GoalsFragment.newInstance(false))
                        .commit();
                match = true;
            } else {
                position = position - 1;
            }
        }

        if (!match) {
            if (position == 0) {
                mTitle = getString(R.string.title_section2);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ActivitiesFragment.newInstance(false, 0))
                        .commit();
                match = true;
            } else if (position == 1) {
                mTitle = getString(R.string.title_section3);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, StatsFragment.newInstance())
                        .commit();
                match = true;
            } else if (!socialDisable && position == 2) {
                mTitle = "Social Media";
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SocialMediaFragment.newInstance())
                        .commit();
                match = true;
            } else if ((socialDisable && position == 2) || position == 3) {
                mTitle = "Treats";
                fragmentManager.beginTransaction()
                        .replace(R.id.container, TreatsFragment.newInstance())
                        .commit();
                match = true;
            } else if ((socialDisable && position == 3) || position == 4) {
                mTitle = "Opponents";
                fragmentManager.beginTransaction()
                        .replace(R.id.container, OpponentFragment.newInstance())
                        .commit();
                match = true;
            }else {
                mTitle = getString(R.string.title_activity_settings);
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                match = true;
            }
        }

    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.log_action) {
            Intent i = new Intent(this, CreateActivity.class);
            startActivityForResult(i, ACTIVITY_LOG);
            return true;
        } else if (item.getItemId() == R.id.add_goal) {
            Intent i = new Intent(this, EditGoal.class);
            startActivityForResult(i, ACTIVITY_CREATE);
        } else if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.moonwalk_drawer, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        uiHelper.onActivityResult(requestCode, resultCode, intent);
        Log.d("twitter", "main");

        // Pass the activity result to the fragment, which will
        // then pass the result to the login button.
        Fragment fragment = getFragmentManager()
                .findFragmentById(R.id.container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, intent);
        }

        if (resultCode == Activity.RESULT_OK) {
            Bundle extras = intent.getExtras();
            switch (requestCode) {
                case ACTIVITY_LOG:
                    Boolean type = extras.getBoolean(DBAdapter.KEY_TYPE);
                    Integer number = extras.getInt(DBAdapter.KEY_ACTIVITY_NUMBER);
                    String unitAction = extras.getString(DBAdapter.KEY_ACTIVITY_UNIT);
                    Long goalNumber = extras.getLong(DBAdapter.KEY_ACTIVITY_GOAL);
                    mDbHelper.createActivity(number, unitAction, type, goalNumber, GlobalVariables.getTime());
                    Toast logToast = Toast.makeText(this, "Activity logged", Toast.LENGTH_LONG);
                    logToast.show();
                    break;
                case ACTIVITY_CREATE:
                    String title = extras.getString(DBAdapter.KEY_GOAL_TITLE);
                    Integer walkTarget = extras.getInt(DBAdapter.KEY_GOAL_WALK);
                    String walkUnit = extras.getString(DBAdapter.KEY_GOAL_WALK_UNIT);
                    Integer climbTarget = extras.getInt(DBAdapter.KEY_GOAL_CLIMB);
                    String climbUnit = extras.getString(DBAdapter.KEY_GOAL_CLIMB_UNIT);
                    Long start = extras.getLong(DBAdapter.KEY_GOAL_START);
                    Long end = extras.getLong(DBAdapter.KEY_GOAL_END);
                    Boolean climb = extras.getBoolean(DBAdapter.KEY_TYPE);
                    Boolean both = extras.getBoolean(DBAdapter.KEY_GOAL_DUAL);
                    int status = GlobalVariables.GOAL_MODE_PENDING;
                    boolean active = true;
                    Date currentDate = new Date(GlobalVariables.getTime());
                    Date newDate = new Date(start);
                    Date newEndDate = new Date(end);
                    if (!newDate.after(currentDate)) {
                        status = GlobalVariables.GOAL_MODE_ACTIVE;
                    } else if (!newEndDate.after(currentDate)) {
                        status = GlobalVariables.GOAL_MODE_OVERDUE;
                    }

                    Long goalId = mDbHelper.createGoal(title, walkTarget, climbTarget, walkUnit, climbUnit, start, end, climb, both, false, active, status);
                    Long mapId = extras.getLong(DBAdapter.KEY_GOAL_MAP);
                    if (mapId != null) {
                        mDbHelper.updateGoalMap(goalId, mapId);
                    }

                    Long calorieId = extras.getLong(DBAdapter.KEY_GOAL_CALORIE);
                    if (calorieId != null) {
                        mDbHelper.connectTreat(goalId, calorieId);
                    }
                    Toast toast = Toast.makeText(this, "Goal created", Toast.LENGTH_LONG);
                    toast.show();
                    break;
            }
        }
    }

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            if (state.isOpened()) {
                Log.d("MainActivity", "Facebook session opened.");
            } else if (state.isClosed()) {
                Log.d("MainActivity", "Facebook session closed.");
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }


}
