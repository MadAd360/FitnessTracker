package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class TreatsActivity extends Activity implements AdapterView.OnItemClickListener {

    private DBAdapter mDbHelper;
    private Cursor mNotesCursor;
    GridView grid;
    TreatCursorAdapter treatAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activitylist_grid);
        setTitle("Select Treat Goal");


        mDbHelper = new DBAdapter(this);
        mDbHelper.open();

        mNotesCursor = mDbHelper.fetchTreats();

        startManagingCursor(mNotesCursor);

        grid = (GridView) findViewById(android.R.id.list);
        grid.setEmptyView(findViewById(android.R.id.empty));

        // Now create an array adapter and set it to display using our row
        treatAdapter = new TreatCursorAdapter(this, mNotesCursor);
        grid.setAdapter(treatAdapter);
        grid.setOnItemClickListener(this);

        //setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        treatAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Bundle bundle = new Bundle();

        bundle.putLong(DBAdapter.KEY_GOAL_CALORIE, id);

        Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        finish();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//        if (resultCode == Activity.RESULT_OK) {
//            Bundle extras = intent.getExtras();
//            switch (requestCode) {
//                case ACTIVITY_LOG:
//                    Boolean logType = extras.getBoolean(DBAdapter.KEY_TYPE);
//                    Integer number = extras.getInt(DBAdapter.KEY_ACTIVITY_NUMBER);
//                    String unitAction = extras.getString(DBAdapter.KEY_ACTIVITY_UNIT);
//                    Long date = extras.getLong(DBAdapter.KEY_ACTIVITY_DATE);
//                    Long goalNumber = extras.getLong(DBAdapter.KEY_ACTIVITY_GOAL);
//                    Long rowId = extras.getLong(DBAdapter.KEY_ROWID);
//                    mDbHelper.updateActivity(rowId, number, unitAction, logType, goalNumber, date);
//                    break;
//            }
//        }
//    }
}
