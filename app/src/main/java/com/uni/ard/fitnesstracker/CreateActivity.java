package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;


public class CreateActivity extends Activity {

    private EditText mBodyText;
    private Spinner spinner;
    private Spinner spinnerGoal;
    private Switch mTypeSwitch;
    private Long mRowId;
    private DBAdapter mDbHelper;
    private Cursor mNotesCursor;
    private GoalAttachAdapter spinnerGoalAdapter;

    private boolean goalSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle("Log Activity");
        getActionBar().setDisplayHomeAsUpEnabled(true);


        mDbHelper = new DBAdapter(this);
        mDbHelper.open();

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
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
                String distanceText = mBodyText.getText().toString();
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
                    Toast toast = Toast.makeText(CreateActivity.this, "Blank values exist.", Toast.LENGTH_LONG);
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
}
