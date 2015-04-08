package com.uni.ard.fitnesstracker;

import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GoalHistoryFragment extends ListFragment {

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private DBAdapter mDbHelper;
    private Cursor mNotesCursor;
    HistoryCursorAdapter history;

    private Long rowId;

    // TODO: Rename and change types of parameters
    public static GoalHistoryFragment newInstance(Long rowId) {
        GoalHistoryFragment fragment = new GoalHistoryFragment();
        Bundle args = new Bundle();
        args.putLong(DBAdapter.KEY_ROWID, rowId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GoalHistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            rowId = getArguments().getLong(DBAdapter.KEY_ROWID);
        }

        mDbHelper = new DBAdapter(getActivity());
        mDbHelper.open();
        //refreshListView();
        mNotesCursor = mDbHelper.fetchGoalHistory(rowId);
        getActivity().startManagingCursor(mNotesCursor);
        history = new HistoryCursorAdapter(getActivity(), mNotesCursor);
        setListAdapter(history);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.goal_history_layout, container, false);
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        mNotesCursor.requery();
        history.notifyDataSetChanged();

    }
}
