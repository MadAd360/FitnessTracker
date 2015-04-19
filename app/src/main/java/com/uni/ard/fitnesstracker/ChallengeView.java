package com.uni.ard.fitnesstracker;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;


public class ChallengeView extends Fragment {
    private DBAdapter mDbHelper;
    private Long rowId;

    private TextView mOpponentText;
    private TextView mPenaltyText;
    private TextView mOutcomeText;
    private TextView mCompletionText;
    private Button mOutcomeButton;

    private boolean complete;
    String opponentName;
    int penalty;
    boolean lost;
    boolean won;
    Long challengeId;
    Long end;


    public static ChallengeView newInstance(Long rowId) {
        ChallengeView fragment = new ChallengeView();
        Bundle args = new Bundle();
        args.putLong(DBAdapter.KEY_ROWID, rowId);
        fragment.setArguments(args);
        return fragment;
    }

    public ChallengeView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DBAdapter(getActivity());
        mDbHelper.open();

        Bundle extras = getArguments();

        rowId = extras.getLong(DBAdapter.KEY_ROWID);

        updateValues();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_challenge_view, container, false);


        mOpponentText = (TextView) view.findViewById(R.id.opponent);
        mPenaltyText = (TextView) view.findViewById(R.id.penalty);
        mOutcomeText = (TextView) view.findViewById(R.id.outcome);
        mCompletionText = (TextView) view.findViewById(R.id.completion);
        mOutcomeButton = (Button) view.findViewById(R.id.outcomeSelect);
        updateView();

        mOutcomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOutcome();
            }
        });


        return view;
    }


    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        updateValues();
        updateView();
    }

    public void updateValues(){
        Cursor cursor = mDbHelper.fetchGoal(rowId);
        challengeId = cursor.getLong(
                cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CHALLENGE));

        complete = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_COMPLETE)) > 0;

        Cursor challengeCursor = mDbHelper.fetchChallenge(challengeId);

        opponentName = challengeCursor.getString(
                challengeCursor.getColumnIndexOrThrow(DBAdapter.KEY_CHALLENGE_OPPONENT));
        penalty = challengeCursor.getInt(
                challengeCursor.getColumnIndexOrThrow(DBAdapter.KEY_CHALLENGE_PENALTY));
        lost = challengeCursor.getInt(
                challengeCursor.getColumnIndexOrThrow(DBAdapter.KEY_CHALLENGE_LOST))>0;
        won = challengeCursor.getInt(
                challengeCursor.getColumnIndexOrThrow(DBAdapter.KEY_CHALLENGE_WON))>0;
        end = challengeCursor.getLong(
                challengeCursor.getColumnIndexOrThrow(DBAdapter.KEY_CHALLENGE_FINISH));
    }

    public void updateView(){
        mOpponentText.setText(opponentName + "");
        mPenaltyText.setText(penalty + "");
        if(lost){
            mOutcomeText.setText("Lost");
            mOutcomeButton.setVisibility(View.GONE);
            mOutcomeText.setTextColor(0xFF880000);
        }else if(won){
            mOutcomeText.setText("Won");
            mOutcomeButton.setVisibility(View.GONE);
            mOutcomeText.setTextColor(0xFF008800);
        }else{
            mOutcomeText.setText("Ongoing");
            mOutcomeText.setTextColor(0xFFB77600);
        }



        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

        if(end == 0){
            mCompletionText.setText("N/A");
        }else{
            String dateText = dateFormat.format(new Date(end));
            String timeText = timeFormat.format(new Date(end));
            mCompletionText.setText(dateText + " " + timeText);
        }
    }

    public void setOutcome(){
        if(complete){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
            //builder1.setMessage("Select the outcome of the challenge");
            builder1.setTitle("Set Outcome");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Lost",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mDbHelper.updateChallenge(challengeId,true, false);
                            updateValues();
                            updateView();
                            mDbHelper.updateGoalComplete(rowId, true);
                            if(end == 0){
                                mDbHelper.updateChallengeEnd(challengeId, GlobalVariables.getTime());
                            }
                            SharedPreferences sp = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity());
                            int freeCalories = sp.getInt("pref_calorie_free", 0);
                                int newCalories = freeCalories - penalty;
                                sp.edit().putInt("pref_calorie_free", newCalories).apply();
                            mOutcomeButton.setVisibility(View.GONE);
                            dialog.cancel();
                        }
                    });
            builder1.setNegativeButton("Won",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mDbHelper.updateChallenge(challengeId,false, true);
                            updateValues();
                            updateView();
                            mOutcomeButton.setVisibility(View.GONE);
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }else{
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
            builder1.setMessage("Did you win or lose?");
            //builder1.setTitle("Set Outcome");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Lost",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mDbHelper.updateChallenge(challengeId,true, false);
                            updateValues();
                            updateView();
                            mDbHelper.updateGoalComplete(rowId, true);
                            if(end == 0){
                                mDbHelper.updateChallengeEnd(challengeId, GlobalVariables.getTime());
                            }
                            SharedPreferences sp = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity());
                            int freeCalories = sp.getInt("pref_calorie_free", 0);
                            int newCalories = freeCalories - penalty;
                            sp.edit().putInt("pref_calorie_free", newCalories).apply();
                            mOutcomeButton.setVisibility(View.GONE);
                            dialog.cancel();
                        }
                    });
            builder1.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }
}
