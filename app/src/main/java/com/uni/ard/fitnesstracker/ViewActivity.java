package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;


public class ViewActivity extends Activity {
    private Spinner spinnerGoal;
    private Long mRowId;
    private DBAdapter mDbHelper;
    private Cursor mNotesCursor;
    private GoalAttachAdapter spinnerGoalAdapter;
    private Long goalId;

    Integer valueNumber;
    String unitText;
    boolean typeClimb;
    Long activityDate;

    DateFormat dateFormat;
    DateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        setTitle("Activity");
        getActionBar().setDisplayHomeAsUpEnabled(true);

        dateFormat = android.text.format.DateFormat.getDateFormat(this);
        timeFormat = android.text.format.DateFormat.getTimeFormat(this);


        mDbHelper = new DBAdapter(this);
        mDbHelper.open();


        spinnerGoal = (Spinner) findViewById(R.id.spinnerGoals);

        mRowId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            mRowId = extras.getLong(DBAdapter.KEY_ROWID);

            Cursor cursor = mDbHelper.fetchActivity(mRowId);

            TextView type = (TextView) findViewById(R.id.textActionType);
            TextView value = (TextView) findViewById(R.id.value);
            TextView unit = (TextView) findViewById(R.id.valueUnit);
            ImageView icon = (ImageView) findViewById(R.id.icon);
            TextView dateText = (TextView) findViewById(R.id.activityDate);
            TextView timeText = (TextView) findViewById(R.id.activityTime);


            // Extract properties from cursor
            valueNumber = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_NUMBER));
            unitText = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_UNIT));
            activityDate = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_DATE));
            typeClimb = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;


            Date start = new Date(activityDate);
            String startDate = dateFormat.format(start);
            String startTime = timeFormat.format(start);
            dateText.setText(startDate);
            timeText.setText(startTime);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            int mColour = sp.getInt(getResources().getString(R.string.pref_user_colour), getResources().getColor(R.color.light_space_gray));

            Drawable planet = (Drawable) getResources().getDrawable(R.drawable.planet_circle);
            planet.setColorFilter(mColour, PorterDuff.Mode.MULTIPLY);
            icon.setImageDrawable(planet);


            float[] hsb = new float[3];
            Color.colorToHSV(mColour, hsb);

            if (hsb[2] > 0.5) {
                value.setTextColor(getResources().getColor(R.color.dark_space_gray));
            } else {
                value.setTextColor(getResources().getColor(R.color.light_space_gray));
            }
            value.setText(valueNumber + "");

            unit.setText(unitText);
            if (typeClimb) {
                type.setText("Climbed");
            } else {
                type.setText("Walked");
            }

            goalId = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_GOAL));
            if (goalId != 0) {
                Button goalButton = (Button) findViewById(R.id.goal);
                Cursor goalCursor = mDbHelper.fetchGoal(goalId);
                String goalName = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TITLE));
                goalButton.setText(goalName);
                View attachLayout = findViewById(R.id.attachGoalLayout);
                View attachButton = findViewById(R.id.attachButton);
                attachLayout.setVisibility(View.GONE);
                attachButton.setVisibility(View.GONE);
            } else {

                mNotesCursor = mDbHelper.fetchLogGoals(typeClimb);


                startManagingCursor(mNotesCursor);
                spinnerGoalAdapter = new GoalAttachAdapter(this, mNotesCursor);
                spinnerGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGoal.setAdapter(spinnerGoalAdapter);

                View attachLayout = findViewById(R.id.attachGoalLayout);
                attachLayout.setVisibility(View.GONE);
                View attachedLayout = findViewById(R.id.attachedLayout);
                attachedLayout.setVisibility(View.GONE);
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAttach(View view) {
        View attachLayout = findViewById(R.id.attachGoalLayout);
        View attachButton = findViewById(R.id.attachButton);
        attachLayout.setVisibility(View.VISIBLE);
        attachButton.setVisibility(View.GONE);
    }

    public void confirm(View view) {

        Long goalNumber = spinnerGoalAdapter.getRowID(spinnerGoal.getSelectedItemPosition());
        goalId = goalNumber;
        mDbHelper.updateActivity(mRowId, valueNumber, unitText, typeClimb, goalId, activityDate);

        View attachLayout = findViewById(R.id.attachGoalLayout);
        View attachButton = findViewById(R.id.attachButton);
        attachLayout.setVisibility(View.GONE);
        attachButton.setVisibility(View.GONE);
        View attachedLayout = findViewById(R.id.attachedLayout);
        Button goalButton = (Button) findViewById(R.id.goal);
        Cursor goalCursor = mDbHelper.fetchGoal(goalId);
        String goalName = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TITLE));
        goalButton.setText(goalName);
        attachedLayout.setVisibility(View.VISIBLE);

        Toast toast = Toast.makeText(this, "Activity updated", Toast.LENGTH_LONG);
        toast.show();
    }

    public void cancelActivity(View view) {
        View attachLayout = findViewById(R.id.attachGoalLayout);
        View attachButton = findViewById(R.id.attachButton);
        attachLayout.setVisibility(View.GONE);
        attachButton.setVisibility(View.VISIBLE);
    }

    public void goalView(View view) {
        Intent i = new Intent(this, GoalDetails.class);
        i.putExtra(DBAdapter.KEY_ROWID, goalId);
        startActivity(i);
    }
}
