package com.uni.ard.fitnesstracker;

import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


public class ChallengesFragment extends ListFragment {
    private static final String KEY_RESTRICT = "restrict";

    private DBAdapter mDbHelper;
    private static final int LOG_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private Cursor mNotesCursor;
    ChallengeCursorAdapter goals;
    Menu menu;

    private static final int ACTIVITY_LOG = 1;

    boolean restrict;

    public static ChallengesFragment newInstance() {
        ChallengesFragment fragment = new ChallengesFragment();
        Bundle args = new Bundle();
        //args.putBoolean(KEY_RESTRICT, restrict);
        fragment.setArguments(args);
        return fragment;
    }

    public ChallengesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();


        mDbHelper = new DBAdapter(getActivity());
        mDbHelper.open();
        //refreshListView();
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        boolean goalAll = sp.getBoolean(GlobalVariables.PREF_GOAL_ALL, false);
            mNotesCursor = mDbHelper.fetchChallengeGoals();
        getActivity().startManagingCursor(mNotesCursor);
        goals = new ChallengeCursorAdapter(getActivity(), mNotesCursor);
        setListAdapter(goals);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_goals, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        registerForContextMenu(getListView());

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor cursor = mDbHelper.fetchGoal(info.id);
        cursor.moveToFirst();
        boolean closed = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_COMPLETE)) > 0;
        menu.add(0, LOG_ID, 0, "Log Activity");
        if (!closed) {
            menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case DELETE_ID:
                mDbHelper.updateGoalComplete(info.id, true);

                mNotesCursor.requery();
                goals.notifyDataSetChanged();

                return true;
            case LOG_ID:
                Intent i = new Intent(getActivity(), CreateActivity.class);

                i.putExtra(DBAdapter.KEY_ACTIVITY_GOAL, info.id);
                startActivityForResult(i, ACTIVITY_LOG);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(getActivity(), GoalDetails.class);
        i.putExtra(DBAdapter.KEY_ROWID, id);
        startActivity(i);
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        mNotesCursor.requery();
        goals.notifyDataSetChanged();

    }

}
