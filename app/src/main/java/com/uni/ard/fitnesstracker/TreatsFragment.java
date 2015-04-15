package com.uni.ard.fitnesstracker;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.io.IOException;

public class TreatsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private DBAdapter mDbHelper;
    private Cursor mNotesCursor;
    GridView grid;
    TextView freeCalorieText;
    TreatCursorAdapter treatAdapter;
    private static final int TREAT_CREATE = 3;


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

        freeCalorieText = (TextView) view.findViewById(R.id.caloriesFree);

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        int freeCalories = sp.getInt("pref_calorie_free", 0);

        freeCalorieText.setText("Burned " + freeCalories + " calories");

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

    public void addTreat(){
        Intent i = new Intent(getActivity(), CreateTreat.class);
        startActivityForResult(i, TREAT_CREATE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == getActivity().RESULT_OK) {
            if(requestCode == TREAT_CREATE) {
                Bundle extras = intent.getExtras();
                String treatName = extras.getString(mDbHelper.KEY_TREAT_NAME);
                int calories = extras.getInt(mDbHelper.KEY_TREAT_CALORIES);
                byte[] treatImage = extras.getByteArray(mDbHelper.KEY_TREAT_IMAGE);
                mDbHelper.insertTreat(treatName, treatImage, calories);
                mNotesCursor.requery();
                treatAdapter.notifyDataSetChanged();
            }
        }
    }
}
