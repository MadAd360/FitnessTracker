package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;


public class StepsTrackerActivity extends Activity implements SensorEventListener {

    private EditText mBodyText;
    private Spinner spinner;
    private Spinner spinnerGoal;
    private Switch mTypeSwitch;
    private Long mRowId;
    private DBAdapter mDbHelper;
    private Cursor mNotesCursor;
    private GoalAttachAdapter spinnerGoalAdapter;

    private boolean goalSet = false;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private boolean stepDetectOn;
    private TextView mStepText;
    private Button mStepButton;

    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle("Log Activity");
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mDbHelper = new DBAdapter(this);
        mDbHelper.open();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        spinner = (Spinner) findViewById(R.id.spinner);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.measurement_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean unitDisable = sp.getBoolean(getResources().getString(R.string.pref_units_disable), false);

        if (unitDisable) {
            spinner.setEnabled(false);
        }


        spinnerGoal = (Spinner) findViewById(R.id.spinnerGoals);

        mNotesCursor = mDbHelper.fetchLogGoals(false);


        mBodyText = (EditText) findViewById(R.id.numberOf);
        mTypeSwitch = (Switch) findViewById(R.id.typeSwitch);
        mStepButton = (Button) findViewById(R.id.stepButton);
        mStepText = (TextView) findViewById(R.id.numberSteps);

        mStepText.setVisibility(View.GONE);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        startManagingCursor(mNotesCursor);
        spinnerGoalAdapter = new GoalAttachAdapter(this, mNotesCursor);
        spinnerGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(spinnerGoalAdapter);

        mRowId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            goalSet = true;
            Long goalRow = extras.getLong(DBAdapter.KEY_ACTIVITY_GOAL);
            Cursor cursor = mDbHelper.fetchGoal(goalRow);
            cursor.moveToFirst();
            boolean goalBoth = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_DUAL)) > 0;
            boolean goalType = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
            if (!goalBoth) {
                if (goalType) {
                    mTypeSwitch.setChecked(true);
                    mTypeSwitch.setVisibility(View.GONE);
                } else {
                    mTypeSwitch.setChecked(false);
                    mTypeSwitch.setVisibility(View.GONE);
                }
                mNotesCursor = mDbHelper.fetchLogGoals(mTypeSwitch.isChecked());
                spinnerGoalAdapter = new GoalAttachAdapter(this, mNotesCursor);
                spinnerGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGoal.setAdapter(spinnerGoalAdapter);
            }
            String goalName = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TITLE));
            spinnerGoal.setSelection(spinnerGoalAdapter.getPosition(goalName));
            spinnerGoal.setVisibility(View.GONE);
        }


        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Boolean typeClimb = mTypeSwitch.isChecked();
                String distanceText;
                if(stepDetectOn){
                    distanceText = "" + numSteps;
                }else{
                    distanceText = mBodyText.getText().toString();
                }
                String unitText = spinner.getSelectedItem().toString();

                if (!distanceText.isEmpty() && !unitText.isEmpty()) {


                    Bundle bundle = new Bundle();

                    bundle.putInt(DBAdapter.KEY_ACTIVITY_NUMBER, new Integer(distanceText));
                    bundle.putString(DBAdapter.KEY_ACTIVITY_UNIT, unitText);
                    bundle.putBoolean(DBAdapter.KEY_TYPE, typeClimb);
                    Long goalNumber = spinnerGoalAdapter.getRowID(spinnerGoal.getSelectedItemPosition());
                    bundle.putLong(DBAdapter.KEY_ACTIVITY_GOAL, goalNumber);
                    if (mRowId != null) {
                        bundle.putLong(DBAdapter.KEY_ROWID, mRowId);
                    }

                    Intent mIntent = new Intent();
                    mIntent.putExtras(bundle);
                    setResult(RESULT_OK, mIntent);
                    finish();
                } else {
                    Toast toast = Toast.makeText(StepsTrackerActivity.this, "Blank values exist.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }

        });
    }

    public void updateDropdown(View view) {
        if (!goalSet) {
            mNotesCursor = mDbHelper.fetchLogGoals(mTypeSwitch.isChecked());
            spinnerGoalAdapter = new GoalAttachAdapter(this, mNotesCursor);
            spinnerGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerGoal.setAdapter(spinnerGoalAdapter);
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

    public void cancelGoal(View view) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    protected void onResume() {
        super.onResume();
        if(stepDetectOn) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void onPause() {
        super.onPause();
        if(stepDetectOn) {
            mSensorManager.unregisterListener(this);
        }
    }

    private float oldX = 1f;
    private float oldY = 1f;
    private float oldZ = 1f;
    private final int HISTORY_LEN = 10;
    private Double[] last10 = new Double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    private boolean isSleeping = false;
    private boolean canStep = true;
       private int numSteps = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
//Right in here is where you put code to read the current sensor values and
        //update any views you might have that are displaying the sensor information
        //You'd get accelerometer values like this:

        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float mSensorX = Math.abs(event.values[0])/9.8f;
        float mSensorY = Math.abs(event.values[1])/9.8f;
        float mSensorZ = Math.abs(event.values[2])/9.8f;

        //double dot = Math.abs(Math.sqrt(oldX * mSensorX + oldY * mSensorY + oldZ * mSensorZ));
        double a = (Math.sqrt(oldX * oldX + oldY * oldY + oldZ * oldZ));
        double b = (Math.sqrt(mSensorX * mSensorX + mSensorY * mSensorY + mSensorZ * mSensorZ));

        double di = (mSensorX * oldX + mSensorY* oldY + mSensorZ*oldZ)/(a*b);

        double wmaDi = 10*di;

        for(int i = 0; i<HISTORY_LEN-1; i++)
        {
            wmaDi += (HISTORY_LEN-1-i) * last10[i];
        }

        wmaDi /= 55.0;

        //average = [NSString stringWithFormat:@"%@%f,",average,wmaDi];

        //dot /= (a * b);
        //self.dotLbl.text = [NSString stringWithFormat:@"%lf",wmaDi];

        Log.d("wmaDi", "wmaDi: " + Math.abs(wmaDi) + " CanStep: " + canStep);

        //if(Math.abs(wmaDi) < 0.986/* > 0.992)
            if(Math.abs(wmaDi) < 0.986/* && ABS(wmaDi) >0.7*/)
        {
            if (canStep) {
                //isSleeping = true;
                canStep = false;
                //[self performSelector:@selector(wakeUp) withObject:nil afterDelay:0.2];
            }
        }
        else if (Math.abs(wmaDi) > 0.992) {
            if (!canStep) {
                numSteps++;
                //self.numStepsLbl.text = [NSString stringWithFormat:@"%d", numSteps];
                canStep = true;
            }
        }

        for(int i = 0; i<HISTORY_LEN-1; i++)
        {
            last10[i+1] = last10[i];
        }


        last10[0] = di;

        oldX = mSensorX;
        oldY = mSensorY;
        oldZ = mSensorZ;

        Log.d("Sensor", "X: " + mSensorX + " Y: " + mSensorY + " Z: " + mSensorZ);
        Log.d("Steps", "Steps: " + numSteps);
        mStepText.setText("" + numSteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void stepDetection(View view){
        if(stepDetectOn){
            numSteps = 0;
            stepDetectOn = false;
            mStepText.setVisibility(View.GONE);
            mBodyText.setVisibility(View.VISIBLE);
            mBodyText.setText(mStepText.getText());
            mStepText.setText("0");
            mStepButton.setText("Start Step Detection");
            spinner.setSelection(adapter.getPosition("Steps"));
            mSensorManager.unregisterListener(this);
        }else{
            numSteps = 0;
            stepDetectOn = true;
            mStepText.setVisibility(View.VISIBLE);
            mBodyText.setVisibility(View.GONE);
            mStepButton.setText("Stop Step Detection");
            mStepText.setText("0");
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}
