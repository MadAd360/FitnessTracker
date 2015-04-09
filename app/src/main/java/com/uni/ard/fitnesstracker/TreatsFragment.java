package com.uni.ard.fitnesstracker;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class TreatsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private DBAdapter mDbHelper;
    private Cursor mNotesCursor;
    GridView grid;
    TreatCursorAdapter treatAdapter;


    public static TreatsFragment newInstance() {
        TreatsFragment fragment = new TreatsFragment();
        Bundle args = new Bundle();
//        if (filter) {
//            args.putLong(DBAdapter.KEY_ROWID, rowId);
//        }
        fragment.setArguments(args);
        return fragment;
    }

    public TreatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mDbHelper = new DBAdapter(getActivity());
        mDbHelper.open();

        Bundle args = getArguments();

        mNotesCursor = mDbHelper.fetchTreats();

        getActivity().startManagingCursor(mNotesCursor);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_treat_grid, container, false);

        grid = (GridView) view.findViewById(android.R.id.list);
        grid.setEmptyView(view.findViewById(android.R.id.empty));

        // Now create an array adapter and set it to display using our row
        treatAdapter = new TreatCursorAdapter(getActivity(), mNotesCursor);
        grid.setAdapter(treatAdapter);
        grid.setOnItemClickListener(this);
        return view;

    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        treatAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        //eat the treat?
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
