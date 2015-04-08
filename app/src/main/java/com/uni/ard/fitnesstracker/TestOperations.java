package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;


public class TestOperations extends Activity {


    private static final String DATE_TYPE = "dateType";

    private DBAdapter mDbHelper;

    private Spinner spinner;
    private TextView systemDate;
    private TextView systemMode;
    private TextView logDate;
    private TextView logAmount;
    private SeekBar stepsPerSecond;
    private Spinner spinnerGoal;

    private Button startStop;

    private Cursor mNotesCursor;
    private GoalAttachAdapter spinnerGoalAdapter;

    DateFormat dateFormat;
    DateFormat timeFormat;

    Long systemDateNumber;
    Long logDateNumber;

    int count = 0;
    boolean counterOn = false;
    Timer T = new Timer();
    TimerTask task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_operations);
        setTitle("Test Functions");
        mDbHelper = new DBAdapter(this);
        mDbHelper.open();
        getActionBar().setDisplayHomeAsUpEnabled(true);

        dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.measurement_array, R.layout.spinner_item_dark);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);

        if (systemDateNumber == null) {
            systemDateNumber = GlobalVariables.getTime();
        }
        systemDate = (TextView) findViewById(R.id.date);
        systemMode = (TextView) findViewById(R.id.testMode);
        updateSystemTime();

        if (logDateNumber == null) {
            logDateNumber = GlobalVariables.getTime();
        }
        logDate = (TextView) findViewById(R.id.logDate);
        updateLogTime();

        logAmount = (TextView) findViewById(R.id.logAmount);
        logAmount.setText("" + count);

        stepsPerSecond = (SeekBar) findViewById(R.id.seekBar);
        stepsPerSecond.setOnSeekBarChangeListener(new SeekBarChanged());


        spinnerGoal = (Spinner) findViewById(R.id.spinnerGoals);
        mNotesCursor = mDbHelper.fetchActiveGoals();
        startManagingCursor(mNotesCursor);
        spinnerGoalAdapter = new GoalAttachAdapter(this, mNotesCursor);
        spinnerGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(spinnerGoalAdapter);

        startStop = (Button) findViewById(R.id.startStopLog);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_operations, menu);
        return true;
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showSystemDatePickerDialog(View v) {
        selectDateTime("System Date/Time", true);
    }

    public void resetSystemTime(View v) {
        GlobalVariables.setTestMode(false);
        systemDateNumber = GlobalVariables.getTime();
        updateSystemTime();
    }

    public void selectDateTime(String title, final boolean systemType) {
        LayoutInflater inflater = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialog_layout = inflater.inflate(R.layout.date_time_dialog, null);
        Calendar date = new GregorianCalendar();
        if (systemType) {
            date.setTimeInMillis(systemDateNumber);
        } else {
            date.setTimeInMillis(logDateNumber);
        }
        final TimePicker tp = (TimePicker) dialog_layout.findViewById(R.id.timePicker);
        tp.setCurrentHour(date.get(GregorianCalendar.HOUR_OF_DAY));
        tp.setCurrentMinute(date.get(GregorianCalendar.MINUTE));
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
                        int hour = tp.getCurrentHour();
                        int minute = tp.getCurrentMinute();
                        int day = dp.getDayOfMonth();
                        int month = dp.getMonth();
                        int year = dp.getYear();
                        Calendar setDate = new GregorianCalendar(year, month, day, hour, minute);
                        if (systemType) {
                            systemDateNumber = setDate.getTimeInMillis();
                            GlobalVariables.setTestMode(true);
                            GlobalVariables.setTestTime(systemDateNumber);
                            updateSystemTime();
                        } else {
                            logDateNumber = setDate.getTimeInMillis();
                            updateLogTime();
                        }
                    }
                });
        builder.setTitle(title);
        builder.show();
    }

    public void showLogDatePickerDialog(View v) {
        selectDateTime("Log Date/Time", false);
    }

    public void updateSystemTime() {

        String systemDateText = dateFormat.format(systemDateNumber);
        String systemTimeText = timeFormat.format(systemDateNumber);
        systemDate.setText(systemDateText + " " + systemTimeText);
        boolean testMode = GlobalVariables.getTestMode();
        if (testMode) {
            systemMode.setText("Test Mode On");
            Toast toast = Toast.makeText(this, "Test mode on - system time updated", Toast.LENGTH_LONG);
            toast.show();
        } else {
            systemMode.setText("Test Mode Off");
            Toast toast = Toast.makeText(this, "Test mode off - system time reset", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void updateLogTime() {

        String logDateText = dateFormat.format(logDateNumber);
        String logTimeText = timeFormat.format(logDateNumber);
        logDate.setText(logDateText + " " + logTimeText);
    }

    public void toggleLogging(View v) {
        if (!counterOn) {
            T = new Timer();
            counterOn = true;
            int logOften = 1000 / (stepsPerSecond.getProgress() + 1);
            seekBarChanged(logOften);
            startStop.setText("Pause");
        } else {
            counterOn = false;
            T.cancel();
            startStop.setText("Start");
        }
    }

    public void reset(View v) {

        counterOn = false;
        startStop.setText("Start");
        T.cancel();

        count = 0;
        logAmount.setText("" + count);
    }

    public void record(View v) {
        String unit = spinner.getSelectedItem().toString();
        Long goalNumber = spinnerGoalAdapter.getRowID(spinnerGoal.getSelectedItemPosition());
        Cursor attachedGoal = mDbHelper.fetchGoal(goalNumber);
        boolean type = false;
        if (attachedGoal.moveToFirst()) {
            type = attachedGoal.getInt(attachedGoal.getColumnIndex(DBAdapter.KEY_TYPE)) > 0;

        }
        mDbHelper.createActivity(count, unit, type, goalNumber, logDateNumber);
        reset(v);
    }

    public void seekBarChanged(int logOften) {
        if (task != null) {
            task.cancel();

        }
        task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logAmount.setText("" + count);
                        count++;
                    }
                });
            }
        };
        T.scheduleAtFixedRate(task, 0, logOften);
    }

    private class SeekBarChanged implements SeekBar.OnSeekBarChangeListener {

        int currentProgress = 1;

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

            currentProgress = progress + 1;
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (counterOn) {
                seekBarChanged(1000 / (currentProgress));
            }
        }

    }


}
