package com.uni.ard.fitnesstracker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.UiLifecycleHelper;
import com.viewpagerindicator.TabPageIndicator;

import java.util.Date;


public class GoalDetails extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    Long rowId;
    int statusNumber;
    long startNumber = 0;
    long endNumber = 0;
    private DBAdapter mDbHelper;
    boolean goalBoth;
    boolean goalType;

    private static final int ACTIVITY_EDIT = 0;
    private static final int ACTIVITY_LOG = 1;
    private static final int TWEETER_REQ_CODE = 2;

    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_details);
        mDbHelper = new DBAdapter(this);
        mDbHelper.open();

        final ActionBar actionBar = getActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            rowId = extras.getLong(DBAdapter.KEY_ROWID);


            Cursor cursor = mDbHelper.fetchGoal(rowId);
            cursor.moveToFirst();
            boolean active = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_ACTIVE)) > 0;
            boolean closed = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_COMPLETE)) > 0;
            startNumber = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_START));
            endNumber = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_END));
            double walkProgress = mDbHelper.getGoalWalkProgress(rowId);
            double climbProgress = mDbHelper.getGoalClimbProgress(rowId);
            int walkTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK));
            int climbTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB));
            boolean completed = false;
            if (walkProgress >= walkTotal && climbProgress >= climbTotal) {
                completed = true;
            }
            statusNumber = GlobalVariables.getStatus(active, closed, startNumber, endNumber, completed);

            goalBoth = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_DUAL)) > 0;
            goalType = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
//        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
//            }
//        });
//
//        // For each of the sections in the app, add a tab to the action bar.
//        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//            // Create a tab with text corresponding to the page title defined by
//            // the adapter. Also specify this Activity object, which implements
//            // the TabListener interface, as the callback (listener) for when
//            // this tab is selected.
//            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
//                            .setTabListener(this));
//        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.goal_view, menu);
        if (statusNumber != GlobalVariables.GOAL_MODE_PENDING) {
            menu.removeItem(R.id.editGoal);
        }

        if (statusNumber != GlobalVariables.GOAL_MODE_ACTIVE && statusNumber != GlobalVariables.GOAL_MODE_OVERDUE) {
            menu.removeItem(R.id.suspendGoal);
            menu.removeItem(R.id.log_action);
        }

        if (statusNumber != GlobalVariables.GOAL_MODE_SUSPENDED) {
            menu.removeItem(R.id.resumeGoal);
        }

        if (statusNumber == GlobalVariables.GOAL_MODE_ABANDONED || statusNumber == GlobalVariables.GOAL_MODE_COMPLETED) {
            menu.removeItem(R.id.abandonGoal);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean valueReturn = false;

        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        if (item.getItemId() == R.id.log_action) {
            Intent i = new Intent(this, CreateActivity.class);

            i.putExtra(DBAdapter.KEY_ACTIVITY_GOAL, rowId);
            startActivityForResult(i, ACTIVITY_LOG);
            return true;
        }

        if (item.getItemId() == R.id.editGoal) {
            editGoal();
            valueReturn = true;
        } else if (item.getItemId() == R.id.suspendGoal) {
            statusNumber = GlobalVariables.GOAL_MODE_SUSPENDED;
            mDbHelper.updateGoalActive(rowId, false);
            valueReturn = true;
        } else if (item.getItemId() == R.id.resumeGoal) {
            Date currentDate = new Date(GlobalVariables.getTime());
            Date newStartDate = new Date(startNumber);
            Date newEndDate = new Date(endNumber);
            if (newEndDate.before(currentDate)) {
                statusNumber = GlobalVariables.GOAL_MODE_OVERDUE;
            } else if (!newStartDate.after(currentDate)) {
                statusNumber = GlobalVariables.GOAL_MODE_ACTIVE;
            }
            mDbHelper.updateGoalActive(rowId, true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Edit goal before resume?");

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(getApplicationContext(), EditGoal.class);
                            i.putExtra(DBAdapter.KEY_ROWID, rowId);
                            i.putExtra(GlobalVariables.KEY_GOAL_STATUS, statusNumber);
                            startActivityForResult(i, ACTIVITY_EDIT);
                        }
                    });
            builder.show();
            valueReturn = true;
        } else if (item.getItemId() == R.id.abandonGoal) {
            statusNumber = GlobalVariables.GOAL_MODE_ABANDONED;
            mDbHelper.updateGoalComplete(rowId, true);
            valueReturn = true;
        } else if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        if (valueReturn) {
            mDbHelper.updateGoalPreviousState(rowId, statusNumber);
            mSectionsPagerAdapter.notifyDataSetChanged();
            mViewPager.setAdapter(mSectionsPagerAdapter);
            invalidateOptionsMenu();
            return valueReturn;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editGoal() {
        Intent i = new Intent(this, EditGoal.class);
        i.putExtra(DBAdapter.KEY_ROWID, rowId);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        uiHelper.onResume();
        mSectionsPagerAdapter.notifyDataSetChanged();
        mDbHelper.updateGoalPreviousState(rowId, statusNumber);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

//        uiHelper.onActivityResult(requestCode, resultCode, intent, new FacebookDialog.Callback() {
//            @Override
//            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
//                Log.e("Activity", String.format("Error: %s", error.toString()));
//                Toast toast = Toast.makeText(GoalDetails.this, "Facebook has failed", Toast.LENGTH_LONG);
//                toast.show();
//            }
//
//            @Override
//            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
//                String postId = FacebookDialog.getNativeDialogPostId(data);
//                if(postId != null){
////                    String postId = postIdFull.substring(postIdFull.indexOf('_') + 1);
//                    mDbHelper.updateGoalFacebookId(rowId, postId);
//                    Log.i("Activity", "Shared!!!!");
//                }
//                Log.i("Activity", "Success!");
//            }
//        });

        if (resultCode == Activity.RESULT_OK) {
            Bundle extras = intent.getExtras();
            switch (requestCode) {
                case ACTIVITY_LOG:
                    Boolean logType = extras.getBoolean(DBAdapter.KEY_TYPE);
                    Integer number = extras.getInt(DBAdapter.KEY_ACTIVITY_NUMBER);
                    String unitAction = extras.getString(DBAdapter.KEY_ACTIVITY_UNIT);
                    Long goalNumber = extras.getLong(DBAdapter.KEY_ACTIVITY_GOAL);
                    mDbHelper.createActivity(number, unitAction, logType, goalNumber, GlobalVariables.getTime());

                    Toast logToast = Toast.makeText(this, "Activity logged", Toast.LENGTH_LONG);
                    logToast.show();

                    break;
                case ACTIVITY_EDIT:
                    Long rowId = extras.getLong(DBAdapter.KEY_ROWID);
                    if (rowId != null) {
                        String title = extras.getString(DBAdapter.KEY_GOAL_TITLE);
                        int walkTotal = extras.getInt(DBAdapter.KEY_GOAL_WALK);
                        int climbTotal = extras.getInt(DBAdapter.KEY_GOAL_CLIMB);
                        String walkUnit = extras.getString(DBAdapter.KEY_GOAL_WALK_UNIT);
                        String climbUnit = extras.getString(DBAdapter.KEY_GOAL_CLIMB_UNIT);
                        startNumber = extras.getLong(DBAdapter.KEY_GOAL_START);
                        endNumber = extras.getLong(DBAdapter.KEY_GOAL_END);
                        boolean type = extras.getBoolean(DBAdapter.KEY_TYPE);
                        boolean both = extras.getBoolean(DBAdapter.KEY_GOAL_DUAL);
                        mDbHelper.updateGoal(rowId, title, walkTotal, climbTotal, walkUnit, climbUnit, startNumber, endNumber, type, both);
                        Date currentDate = new Date(GlobalVariables.getTime());
                        Date newStartDate = new Date(startNumber);
                        Date newEndDate = new Date(endNumber);
                        if (newEndDate.before(currentDate)) {
                            statusNumber = GlobalVariables.GOAL_MODE_OVERDUE;
                            invalidateOptionsMenu();
                        } else if (!newStartDate.after(currentDate)) {
                            statusNumber = GlobalVariables.GOAL_MODE_ACTIVE;
                            invalidateOptionsMenu();
                        }


                        Long mapId = extras.getLong(DBAdapter.KEY_GOAL_MAP);
                        if (mapId != null) {
                            mDbHelper.updateGoalMap(rowId, mapId);
                        }
                        Toast toast = Toast.makeText(this, "Goal updated", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    break;
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return GoalView.newInstance(rowId);
            }

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean statsDisable = sp.getBoolean(getResources().getString(R.string.pref_statistics_disable), false);
            boolean socialDisable = sp.getBoolean(getResources().getString(R.string.pref_social_disable), false);

            if (statusNumber == GlobalVariables.GOAL_MODE_COMPLETED && !socialDisable) {
                if (position == 1) {
                    return ShareFragment.newInstance(rowId);
                } else {
                    position = position - 1;
                }
            }

            if (!statsDisable) {
                if (goalBoth) {
                    if (position == 1) {
                        return GoalStatistics.newInstance(rowId, false);
                    } else if (position == 2) {
                        return GoalStatistics.newInstance(rowId, true);
                    } else {
                        position = position - 2;
                    }
                } else {
                    if (position == 1) {
                        if (goalType) {
                            return GoalStatistics.newInstance(rowId, true);
                        } else {
                            return GoalStatistics.newInstance(rowId, false);
                        }
                    } else {
                        position = position - 1;
                    }
                }
            }


            switch (position) {
                case 1:
                    return ActivitiesFragment.newInstance(true, rowId);
                default:
                    return GoalHistoryFragment.newInstance(rowId);
            }
        }

        @Override
        public int getCount() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean statsDisable = sp.getBoolean(getResources().getString(R.string.pref_statistics_disable), false);
            boolean socialDisable = sp.getBoolean(getResources().getString(R.string.pref_social_disable), false);

            int count = 3;
            if (!statsDisable) {
                if (goalBoth) {
                    count = count + 2;
                } else {
                    count = count + 1;
                }
            }

            if (statusNumber == GlobalVariables.GOAL_MODE_COMPLETED && !socialDisable) {
                count = count + 1;
            }
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Details";

            }


            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean statsDisable = sp.getBoolean(getResources().getString(R.string.pref_statistics_disable), false);
            boolean socialDisable = sp.getBoolean(getResources().getString(R.string.pref_social_disable), false);

            if (statusNumber == GlobalVariables.GOAL_MODE_COMPLETED && !socialDisable) {
                if (position == 1) {
                    return "Social Media";
                } else {
                    position = position - 1;
                }
            }

            if (!statsDisable) {
                if (goalBoth) {
                    if (position == 1) {
                        return "Walked";
                    } else if (position == 2) {
                        return "Climbed";
                    } else {
                        position = position - 2;
                    }
                } else {
                    if (position == 1) {
                        return "Statistics";
                    } else {
                        position = position - 1;
                    }
                }
            }


            switch (position) {
                case 1:
                    return "Exercise";
                default:
                    return "History";
            }

        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

        }
    }


}
