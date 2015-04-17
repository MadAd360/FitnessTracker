package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class EditGoal extends Activity {


    private static final String DATE_TYPE = "dateType";

    private EditText mTitleText;
    private EditText mWalkText;
    private View mClimbLayout;
    private View mWalkLayout;
    private EditText mClimbText;
    private Button mStartDateText;
    private Button mEndDateText;
    private Button mStartTimeText;
    private Button mEndTimeText;
    private Spinner walkSpinner;
    private Spinner climbSpinner;
    private Long mRowId;
    private Switch mTypeSwitch;
    private Switch mTypeBothSwitch;
    private View mCustomLayout;
    //private Button customButton;
    private Button mapButton;
    private View mChallengeLayout;
    private Button challengeButton;

    private EditText mOpponentText;
    private EditText mPenaltyText;

    ArrayAdapter<CharSequence> walkAdapter;
    ArrayAdapter<CharSequence> climbAdapter;

    private long endNumber = 0;
    private long startNumber = 0;

    private DBAdapter mDbHelper;


    private static final int MAP_ID = 1;
    private static final int CALORIE_ID = 2;


    DateFormat dateFormat;
    DateFormat timeFormat;

    boolean mapType;
    Long mapId;
    double mapStartLat;
    double mapStartLong;
    double mapEndLat;
    double mapEndLong;

    boolean calorieType;
    Long calorieId;
    int caloriesCount;

    boolean challenge = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_goal);
        setTitle(R.string.title_activity_edit_goal);
        mDbHelper = new DBAdapter(this);
        mDbHelper.open();
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean unitDisable = sp.getBoolean(getResources().getString(R.string.pref_units_disable), false);

        dateFormat = android.text.format.DateFormat.getDateFormat(this);
        timeFormat = android.text.format.DateFormat.getTimeFormat(this);

        walkSpinner = (Spinner) findViewById(R.id.walkSpinner);
        walkAdapter = ArrayAdapter.createFromResource(this,
                R.array.measurement_array, R.layout.spinner_item);
        walkAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        walkSpinner.setAdapter(walkAdapter);

        climbSpinner = (Spinner) findViewById(R.id.climbSpinner);
        climbAdapter = ArrayAdapter.createFromResource(this,
                R.array.measurement_array, R.layout.spinner_item);
        climbAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        climbSpinner.setAdapter(climbAdapter);

        if (unitDisable) {
            walkSpinner.setEnabled(false);
            climbSpinner.setEnabled(false);
        }

        mTitleText = (EditText) findViewById(R.id.goalName);
        mWalkText = (EditText) findViewById(R.id.numberWalk);
        mClimbText = (EditText) findViewById(R.id.numberClimb);
        mWalkLayout = findViewById(R.id.walkLayout);
        mClimbLayout = findViewById(R.id.climbLayout);
        mStartDateText = (Button) findViewById(R.id.startDateButton);
        mEndDateText = (Button) findViewById(R.id.endDateButton);
        mStartTimeText = (Button) findViewById(R.id.startTimeButton);
        mEndTimeText = (Button) findViewById(R.id.endTimeButton);
        mTypeSwitch = (Switch) findViewById(R.id.typeSwitch);
        mTypeBothSwitch = (Switch) findViewById(R.id.bothSwitch);
        mCustomLayout = findViewById(R.id.customLayout);
        //customButton = (Button) findViewById(R.id.selectCustom);
        mapButton = (Button) findViewById(R.id.selectMap);

        challengeButton = (Button) findViewById(R.id.setChallenge);
        mChallengeLayout = findViewById(R.id.challengeLayout);
        mOpponentText = (EditText) findViewById(R.id.opponentName);
        mPenaltyText = (EditText) findViewById(R.id.caloriePenalty);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        changedSingleType();

        mRowId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            challengeButton.setVisibility(View.GONE);

            mRowId = extras.getLong(DBAdapter.KEY_ROWID);

            int statusNumber = extras.getInt(GlobalVariables.KEY_GOAL_STATUS);

            if (statusNumber == GlobalVariables.GOAL_MODE_OVERDUE) {


                mStartDateText.setEnabled(false);
                mEndDateText.setEnabled(false);
                mStartTimeText.setEnabled(false);
                mEndTimeText.setEnabled(false);
            }

            Cursor cursor = mDbHelper.fetchGoal(mRowId);

            String title;
            Long newStartNumber;
            Long newEndNumber;
            Boolean type;
            Boolean both;

            title = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TITLE));
            newStartNumber = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_START));
            newEndNumber = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_END));
            String walkUnit = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK_UNIT));
            int walkGoal = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK));
            String climbUnit = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB_UNIT));
            int climbGoal = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB));
            type = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
            both = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_DUAL)) > 0;
            mapId = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_MAP));
            calorieId = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CALORIE));

            if (mapId != null) {
                mapType = true;
            } else {
                if(calorieId != null){
                    calorieType = true;
                    Cursor treatCursor = mDbHelper.fetchTreat(calorieId);

                    if(treatCursor.moveToFirst()){
                        caloriesCount = treatCursor.getInt(treatCursor.getColumnIndexOrThrow(DBAdapter.KEY_TREAT_CALORIES));
                    }
                }else{
                    calorieType = false;
                }
                mapType = false;
            }



            if (title != null) {
                mTitleText.setText(title);
            }
            if (unitDisable) {
                int originalWalk = new Integer(walkGoal);
                int originalClimb = new Integer(climbGoal);
                int formatWalk = (int) GlobalVariables.convertUnits(originalWalk, false, "Steps", walkUnit);
                int formatClimb = (int) GlobalVariables.convertUnits(originalClimb, false, "Steps", climbUnit);
                mWalkText.setText(Integer.toString(formatWalk));
                mClimbText.setText(Integer.toString(formatClimb));
            } else {
                mWalkText.setText(Integer.toString(walkGoal));
                mClimbText.setText(Integer.toString(climbGoal));
                if (walkUnit != null) {
                    walkSpinner.setSelection(walkAdapter.getPosition(walkUnit));
                }

                if (climbUnit != null) {
                    climbSpinner.setSelection(climbAdapter.getPosition(climbUnit));
                }
            }


            if (startNumber == 0) {
                startNumber = newStartNumber;
                Date start = new Date(startNumber);
                String startDate = dateFormat.format(start);
                String startTime = timeFormat.format(start);
                mStartDateText.setText(startDate);
                mStartTimeText.setText(startTime);
            }
            if (endNumber == 0) {
                endNumber = newEndNumber;
                Date end = new Date(endNumber);
                String endDate = dateFormat.format(end);
                String endTime = timeFormat.format(end);
                mEndDateText.setText(endDate);
                mEndTimeText.setText(endTime);
            }

            if (type != null) {
                mTypeSwitch.setChecked(type);
            }

            if (both != null) {
                mTypeBothSwitch.setChecked(both);
                if (both) {
                    mTypeSwitch.setVisibility(View.GONE);
                    mWalkLayout.setVisibility(View.VISIBLE);
                    mClimbLayout.setVisibility(View.VISIBLE);
                }
            }
        } else {
            startNumber = GlobalVariables.getTime();
            Date currentDate = new Date(startNumber);
            Calendar currentCalendar = GregorianCalendar.getInstance();
            currentCalendar.setTime(currentDate);
            String startDate = dateFormat.format(currentCalendar.getTime());
            String startTime = timeFormat.format(currentCalendar.getTime());
            mStartDateText.setText(startDate);
            mStartTimeText.setText(startTime);

            currentCalendar.add(Calendar.DATE, 1);
            endNumber = currentCalendar.getTimeInMillis();
            String endDate = dateFormat.format(currentCalendar.getTime());
            String endTime = timeFormat.format(currentCalendar.getTime());
            mEndDateText.setText(endDate);
            mEndTimeText.setText(endTime);
        }


        boolean mapDisable = sp.getBoolean(getResources().getString(R.string.pref_map_disable), false);

        if (mapDisable) {
            //customButton.setVisibility(View.GONE);
            mapButton.setVisibility(View.GONE);
        }

            mCustomLayout.setVisibility(View.GONE);
        mChallengeLayout.setVisibility(View.GONE);

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                String titleText = mTitleText.getText().toString();
                String walkText = mWalkText.getText().toString();
                String climbText = mClimbText.getText().toString();
                String walkUnitText = walkSpinner.getSelectedItem().toString();
                String climbUnitText = climbSpinner.getSelectedItem().toString();
                Boolean typeClimb = mTypeSwitch.isChecked();
                Boolean typeBoth = mTypeBothSwitch.isChecked();

                String challengeOpponentText = mOpponentText.getText().toString();
                String challengePenaltyText = mPenaltyText.getText().toString();

                if (mapType) {
                    if (mapId == null) {
                        mapId = mDbHelper.createMap(mapStartLat, mapStartLong, mapEndLat, mapEndLong);
                    }
                } else {
                    if (mRowId != null) {
                        mDbHelper.deleteMap(mRowId);
                    }
                    mapId = null;
                }

                if(calorieType){
                    typeBoth = false;
                    typeClimb = false;
                    walkText = caloriesCount + "";
                    walkUnitText = "Calories";
                }

                if(challenge){
                    if(challengeOpponentText.isEmpty()) {
                        Toast toast = Toast.makeText(EditGoal.this, "Opponent must not be blank", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }else if(challengePenaltyText.isEmpty()){
                        Toast toast = Toast.makeText(EditGoal.this, "Penalty must not be blank", Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }
                }


                if (titleText.isEmpty()) {

                    Toast toast = Toast.makeText(EditGoal.this, "Title must not be blank", Toast.LENGTH_LONG);
                    toast.show();
                } else if ((typeBoth || !typeClimb) && (walkText.isEmpty() || walkText == "0")) {
                    Toast toast = Toast.makeText(EditGoal.this, "Walk target must not be blank or 0", Toast.LENGTH_LONG);
                    toast.show();
                } else if ((typeBoth || typeClimb) && (climbText.isEmpty() || climbText == "0")) {
                    Toast toast = Toast.makeText(EditGoal.this, "Climb target must not be blank or 0", Toast.LENGTH_LONG);
                    toast.show();
                } else {

                    Date startDate = new Date(startNumber);
                    Date endDate = new Date(endNumber);
                    if (startDate.before(endDate)) {
                        Bundle bundle = new Bundle();

                        bundle.putString(DBAdapter.KEY_GOAL_TITLE, mTitleText.getText().toString());
                        if (typeBoth) {
                            bundle.putInt(DBAdapter.KEY_GOAL_WALK, new Integer(mWalkText.getText().toString()));
                            bundle.putString(DBAdapter.KEY_GOAL_WALK_UNIT, walkUnitText);
                            bundle.putInt(DBAdapter.KEY_GOAL_CLIMB, new Integer(mClimbText.getText().toString()));
                            bundle.putString(DBAdapter.KEY_GOAL_CLIMB_UNIT, climbUnitText);
                        } else if (typeClimb) {
                            bundle.putInt(DBAdapter.KEY_GOAL_WALK, 0);
                            bundle.putString(DBAdapter.KEY_GOAL_WALK_UNIT, "Steps");
                            bundle.putInt(DBAdapter.KEY_GOAL_CLIMB, new Integer(mClimbText.getText().toString()));
                            bundle.putString(DBAdapter.KEY_GOAL_CLIMB_UNIT, climbUnitText);
                        } else {
                            bundle.putInt(DBAdapter.KEY_GOAL_WALK, new Integer(walkText));
                            bundle.putString(DBAdapter.KEY_GOAL_WALK_UNIT, walkUnitText);
                            bundle.putInt(DBAdapter.KEY_GOAL_CLIMB, 0);
                            bundle.putString(DBAdapter.KEY_GOAL_CLIMB_UNIT, "Steps");
                        }
                        bundle.putLong(DBAdapter.KEY_GOAL_START, startNumber);
                        bundle.putLong(DBAdapter.KEY_GOAL_END, endNumber);
                        bundle.putBoolean(DBAdapter.KEY_TYPE, typeClimb);
                        bundle.putBoolean(DBAdapter.KEY_GOAL_DUAL, typeBoth);
                        if (mapType) {
                            bundle.putLong(DBAdapter.KEY_GOAL_MAP, mapId);
                        }
                        if(calorieType){
                            bundle.putLong(DBAdapter.KEY_GOAL_CALORIE, calorieId);
                        }
                        if (mRowId != null) {
                            bundle.putLong(DBAdapter.KEY_ROWID, mRowId);
                        }
                        if(challenge){
                            bundle.putString(DBAdapter.KEY_CHALLENGE_OPPONENT, challengeOpponentText);
                            bundle.putInt(DBAdapter.KEY_CHALLENGE_PENALTY, new Integer(challengePenaltyText));
                        }

                        Intent mIntent = new Intent();
                        mIntent.putExtras(bundle);
                        setResult(RESULT_OK, mIntent);
                        finish();
                    } else {
                        Toast toast = Toast.makeText(EditGoal.this, "End date must be after start date", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }

        });
    }

    public void cancelGoal(View view) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void showStartDatePickerDialog(View v) {
        selectDate("Start Date", false);
    }

    public void showEndDatePickerDialog(View v) {
        selectDate("End Date", true);
    }

    public void showStartTimePickerDialog(View v) {
        selectTime("Start Time", false);
    }

    public void showEndTimePickerDialog(View v) {
        selectTime("End Time", true);
    }

    public void changedType(View v) {
        if (mTypeBothSwitch.isChecked()) {
            mTypeSwitch.setVisibility(View.GONE);
            mWalkLayout.setVisibility(View.VISIBLE);
            mClimbLayout.setVisibility(View.VISIBLE);
        } else {
            mTypeSwitch.setVisibility(View.VISIBLE);
            changedSingleType();
        }

    }

    public void changedSingleType() {
        if (mTypeSwitch.isChecked()) {
            mWalkLayout.setVisibility(View.GONE);
            mClimbLayout.setVisibility(View.VISIBLE);
        } else {
            mWalkLayout.setVisibility(View.VISIBLE);
            mClimbLayout.setVisibility(View.GONE);
        }

    }

    public void changedSingleType(View v) {
        if (mTypeSwitch.isChecked()) {
            mWalkLayout.setVisibility(View.GONE);
            mClimbLayout.setVisibility(View.VISIBLE);
        } else {
            mWalkLayout.setVisibility(View.VISIBLE);
            mClimbLayout.setVisibility(View.GONE);
        }

    }

    public void selectTime(String title, final boolean endType) {
        LayoutInflater inflater = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialog_layout = inflater.inflate(R.layout.time_dialog, null);
        final Calendar date = new GregorianCalendar();
        if (endType) {
            date.setTimeInMillis(endNumber);
        } else {
            date.setTimeInMillis(startNumber);
        }
        final TimePicker tp = (TimePicker) dialog_layout.findViewById(R.id.timePicker);
        tp.setCurrentHour(date.get(GregorianCalendar.HOUR_OF_DAY));
        tp.setCurrentMinute(date.get(GregorianCalendar.MINUTE));
        builder.setView(dialog_layout)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int hour = tp.getCurrentHour();
                        int minute = tp.getCurrentMinute();
                        int day = date.get(Calendar.DAY_OF_MONTH);
                        int month = date.get(Calendar.MONTH);
                        int year = date.get(Calendar.YEAR);
                        Calendar setDate = new GregorianCalendar(year, month, day, hour, minute);
                        if (endType) {
                            endNumber = setDate.getTimeInMillis();
                            Date end = new Date(endNumber);
                            String endTime = timeFormat.format(end);
                            mEndTimeText.setText(endTime);
                        } else {
                            startNumber = setDate.getTimeInMillis();
                            Date start = new Date(startNumber);
                            String startTime = timeFormat.format(start);
                            mStartTimeText.setText(startTime);
                        }
                    }
                });
        builder.setTitle(title);
        builder.show();
    }

    public void selectDate(String title, final boolean endType) {
        LayoutInflater inflater = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialog_layout = inflater.inflate(R.layout.date_dialog, null);
        final Calendar date = new GregorianCalendar();
        if (endType) {
            date.setTimeInMillis(endNumber);
        } else {
            date.setTimeInMillis(startNumber);
        }
        final DatePicker dp = (DatePicker) dialog_layout.findViewById(R.id.datePicker);
        dp.updateDate(date.get(GregorianCalendar.YEAR),
                date.get(GregorianCalendar.MONTH), date.get(GregorianCalendar.DAY_OF_MONTH));
        builder.setView(dialog_layout)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int hour = date.get(Calendar.HOUR_OF_DAY);
                        int minute = date.get(Calendar.MINUTE);
                        int day = dp.getDayOfMonth();
                        int month = dp.getMonth();
                        int year = dp.getYear();
                        Calendar setDate = new GregorianCalendar(year, month, day, hour, minute);
                        if (endType) {
                            endNumber = setDate.getTimeInMillis();
                            Date end = new Date(endNumber);
                            String endDate = dateFormat.format(end);
                            String endTime = timeFormat.format(end);
                            mEndDateText.setText(endDate);
                        } else {
                            startNumber = setDate.getTimeInMillis();
                            Date start = new Date(startNumber);
                            String startDate = dateFormat.format(start);
                            String startTime = timeFormat.format(start);
                            mStartDateText.setText(startDate);
                        }
                    }
                });
        builder.setTitle(title);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectMap(View v) {
        mCustomLayout.setVisibility(View.GONE);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (!ni.isConnected()) {
            Toast toast = Toast.makeText(EditGoal.this, "Currently no internet connection", Toast.LENGTH_LONG);
            toast.show();
        } else {
            Intent i = new Intent(this, MapDistance.class);
            startActivityForResult(i, MAP_ID);
        }
    }

    public void selectCustom(View v) {
        mapType = false;
        calorieType = false;
        calorieId = null;
        mCustomLayout.setVisibility(View.VISIBLE);
    }


    public void setChallenge(View v) {
        if(mChallengeLayout.getVisibility() == View.GONE){
            challenge = true;
            mChallengeLayout.setVisibility(View.VISIBLE);
            challengeButton.setText("Remove Challenge");
        }else{
            challenge = false;
            mChallengeLayout.setVisibility(View.GONE);
            challengeButton.setText("Set Challenge");
        }
    }

    public void selectCalorie(View v) {
        mCustomLayout.setVisibility(View.GONE);
        Intent i = new Intent(this, TreatsActivity.class);
        startActivityForResult(i, CALORIE_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MAP_ID) {
                Bundle extras = intent.getExtras();
                int newTotal = extras.getInt(DBAdapter.KEY_GOAL_WALK);
                String newUnit = extras.getString(DBAdapter.KEY_GOAL_WALK_UNIT);
                mapStartLat = extras.getDouble(DBAdapter.KEY_MAP_START_LAT);
                mapStartLong = extras.getDouble(DBAdapter.KEY_MAP_START_LONG);
                mapEndLat = extras.getDouble(DBAdapter.KEY_MAP_END_LAT);
                mapEndLong = extras.getDouble(DBAdapter.KEY_MAP_END_LONG);
                mapType = true;
                calorieType = false;
                mapId = null;
                mTypeSwitch.setChecked(false);
                mTypeBothSwitch.setChecked(false);

                mWalkText.setText(Integer.toString(newTotal));

                if (newUnit != null) {
                    walkSpinner.setSelection(walkAdapter.getPosition(newUnit));
                }
            }else if(requestCode == CALORIE_ID){
                mapType = false;
                calorieType = true;
                Bundle extras = intent.getExtras();
                calorieId = extras.getLong(DBAdapter.KEY_GOAL_CALORIE);
                Cursor cursor = mDbHelper.fetchTreat(calorieId);

                if(cursor.moveToFirst()){
                    caloriesCount = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TREAT_CALORIES));
                }
                //sort out calorie stuff
            }
        }
    }


}
