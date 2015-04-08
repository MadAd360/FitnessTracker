package com.uni.ard.fitnesstracker;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DecimalFormat;


public class StatsFragment extends Fragment {


    private Cursor mActivitiesCursor;
    private DBAdapter mDbHelper;

    ImageButton myPlanet;
    TextView distanceText;
    TextView moonDistance;
    TextView otherDistance;

    private int stepValue;
    private int currentUnitPosition;
    private int mColour;

    private Double moonDistancePercentage;
    private Double otherDistancePercentage;

    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new DBAdapter(getActivity());
        mDbHelper.open();

        currentUnitPosition = 0;

        evaluate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //set view values
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        draw(view);

        Button spaceLink = (Button) view.findViewById(R.id.spaceLink);

        myPlanet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeUnits();
            }
        });

        spaceLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://en.wikipedia.org/wiki/120347_Salacia");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        return view;
    }

    public void evaluate() {
        mActivitiesCursor = mDbHelper.fetchAllActivities();
        getActivity().startManagingCursor(mActivitiesCursor);

        stepValue = 0;
        if (mActivitiesCursor.moveToFirst()) {
            do {
                int initialValue = mActivitiesCursor.getInt(mActivitiesCursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_NUMBER));
                String initialUnit = mActivitiesCursor.getString(mActivitiesCursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_UNIT));
                boolean type = mActivitiesCursor.getInt(mActivitiesCursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
                double newValue = GlobalVariables.convertUnits(initialValue, type, "Steps", initialUnit);
                stepValue = stepValue + (int) newValue;
            } while (mActivitiesCursor.moveToNext());
        }

        double kmValue = GlobalVariables.convertUnits(stepValue, false, "Kilometres", "Steps");

        moonDistancePercentage = (kmValue / 10917.0) * 100;
        otherDistancePercentage = (kmValue / 2670.353756) * 100;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mColour = sp.getInt(getActivity().getResources().getString(R.string.pref_user_colour), getActivity().getResources().getColor(R.color.light_space_gray));
    }

    public void draw(View view) {
        myPlanet = (ImageButton) view.findViewById(R.id.myPlanet);
        distanceText = (TextView) view.findViewById(R.id.myPlanetDistance);
        moonDistance = (TextView) view.findViewById(R.id.moonDistance);
        otherDistance = (TextView) view.findViewById(R.id.otherDistance);

        String[] mUnitArray = getResources().getStringArray(R.array.measurement_array);
        String unit = mUnitArray[currentUnitPosition];

        double unitValue = GlobalVariables.convertUnits(stepValue, false, unit, "Steps");
        DecimalFormat dfPlanet = new DecimalFormat();
        dfPlanet.setMaximumFractionDigits(2);
        String unitValueFormat = dfPlanet.format(unitValue);
        distanceText.setText("Approx " + unitValueFormat + " " + unit);


        DecimalFormat df = new DecimalFormat("#.######");
        moonDistance.setText(df.format(moonDistancePercentage) + "%");
        otherDistance.setText(df.format(otherDistancePercentage) + "%");


        Drawable planet = (Drawable) getResources().getDrawable(R.drawable.planet_circle);
        planet.setColorFilter(mColour, PorterDuff.Mode.MULTIPLY);
        myPlanet.setBackground(planet);
        float[] hsb = new float[3];
        Color.colorToHSV(mColour, hsb);

        if (hsb[2] > 0.5) {
            distanceText.setTextColor(getResources().getColor(R.color.dark_space_gray));
        } else {
            distanceText.setTextColor(getResources().getColor(R.color.light_space_gray));
        }
    }

    public void changeUnits() {

        String[] mUnitArray = getResources().getStringArray(R.array.measurement_array);

        currentUnitPosition = (currentUnitPosition + 1) % mUnitArray.length;

        String unit = mUnitArray[currentUnitPosition];

        double unitValue = GlobalVariables.convertUnits(stepValue, false, unit, "Steps");

        DecimalFormat dfPlanet = new DecimalFormat();
        dfPlanet.setMaximumFractionDigits(4);
        String unitValueFormat = dfPlanet.format(unitValue);
        distanceText.setText("Approx " + unitValueFormat + " " + unit);
    }

    @Override
    public void onResume() {
        super.onResume();
        evaluate();
        draw(getView());
    }
}
