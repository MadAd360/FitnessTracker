package com.uni.ard.fitnesstracker;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;


public class GoalView extends Fragment {
    private DBAdapter mDbHelper;

    private TextView mTitleText;
    private TextView mTargetWalkText;
    private TextView mTargetClimbText;
    private TextView mUnitWalkText;
    private TextView mUnitClimbText;
    private ProgressBar mTargetWalkProgress;
    private ProgressBar mTargetClimbProgress;
    private View walkLayout;
    private View climbLayout;
    private View progressLayout;
    private TextView mapProgressText;
    private View mapLayout;
    private MapView mapView;
    private TextView mstartDateText;
    private TextView mendDateText;
    private TextView mStatusText;
    private ImageView icon;

    DateFormat dateFormat;
    DateFormat timeFormat;

    String title;
    int walkTotal;
    double walkProgress;
    String walkUnit;
    int climbTotal;
    double climbProgress;
    String climbUnit;
    Long startNumber;
    Long endNumber;
    Boolean type;
    Boolean both;
    Long rowId;
    int statusNumber;
    Long mapId;

    private GoogleMap mMap;
    private Polyline targetPath;
    private Polyline progressPath;
    RequestQueue queue;

    public static GoalView newInstance(Long rowId) {
        GoalView fragment = new GoalView();
        Bundle args = new Bundle();
        args.putLong(DBAdapter.KEY_ROWID, rowId);
        fragment.setArguments(args);
        return fragment;
    }

    public GoalView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DBAdapter(getActivity());
        mDbHelper.open();

        queue = Volley.newRequestQueue(getActivity());

        dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
        timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

        Bundle extras = getArguments();

        rowId = extras.getLong(DBAdapter.KEY_ROWID);

        updateValues();


        Cursor cursor = mDbHelper.fetchGoal(rowId);
        mapId = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_MAP));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean mapDisable = sp.getBoolean(getResources().getString(R.string.pref_map_disable), false);

        if (mapDisable) {
            mapId = new Long(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_goal_view, container, false);


        mStatusText = (TextView) view.findViewById(R.id.status);
        mTitleText = (TextView) view.findViewById(R.id.title);
        mTargetWalkText = (TextView) view.findViewById(R.id.totalWalk);
        mTargetClimbText = (TextView) view.findViewById(R.id.totalClimb);
        mUnitWalkText = (TextView) view.findViewById(R.id.unitWalk);
        mUnitClimbText = (TextView) view.findViewById(R.id.unitClimb);
        mTargetWalkProgress = (ProgressBar) view.findViewById(R.id.progressBarWalk);
        mTargetClimbProgress = (ProgressBar) view.findViewById(R.id.progressBarClimb);
        mstartDateText = (TextView) view.findViewById(R.id.goalStartDate);
        mendDateText = (TextView) view.findViewById(R.id.goalEndDate);
        icon = (ImageView) view.findViewById(R.id.action_icon);
        walkLayout = view.findViewById(R.id.layoutWalk);
        climbLayout = view.findViewById(R.id.layoutClimb);
        progressLayout = view.findViewById(R.id.progressLayout);
        mapLayout = view.findViewById(R.id.mapLayout);
        mapProgressText = (TextView) view.findViewById(R.id.mapProgress);


        if (mapId == 0) {
            mapLayout.setVisibility(View.GONE);
        } else {
            mapView = (MapView) view.findViewById(R.id.map);
            progressLayout.setVisibility(View.GONE);
            mapView.onCreate(savedInstanceState);
            mMap = mapView.getMap();

            MapsInitializer.initialize(getActivity());

        }


        updateView();

        return view;
    }


    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        updateValues();
        updateView();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    public void updateValues() {
        Cursor cursor = mDbHelper.fetchGoal(rowId);

        if (cursor.moveToFirst()) {

            title = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TITLE));
            startNumber = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_START));
            endNumber = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_END));
            walkUnit = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK_UNIT));
            climbUnit = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB_UNIT));
            type = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
            both = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_DUAL)) > 0;
            walkProgress = mDbHelper.getGoalWalkProgress(rowId);
            climbProgress = mDbHelper.getGoalClimbProgress(rowId);
            walkTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK));
            climbTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB));
            boolean active = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_ACTIVE)) > 0;
            boolean complete = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_COMPLETE)) > 0;
            boolean completed = false;
            if (walkProgress >= walkTotal && climbProgress >= climbTotal) {
                completed = true;
            }
            statusNumber = GlobalVariables.getStatus(active, complete, startNumber, endNumber, completed);

            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if (!ni.isConnected()) {
                mapId = new Long(0);
            }
        }
    }


    public void updateView() {

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int mColour = sp.getInt(getActivity().getResources().getString(R.string.pref_user_colour), getActivity().getResources().getColor(R.color.blue_space));

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        mTitleText.setText(title);
        if (mapId == 0) {
            if (type || both) {
                String climbProgressFormat = df.format(climbProgress);
                mTargetClimbText.setText(climbProgressFormat + " of " + climbTotal);
                mUnitClimbText.setText(climbUnit);

                LayerDrawable progressDrawable = (LayerDrawable) getActivity().getResources().getDrawable(R.drawable.custom_progress);
                Drawable progressCircle = (Drawable) progressDrawable.findDrawableByLayerId(android.R.id.progress);
                progressCircle.setColorFilter(mColour, PorterDuff.Mode.MULTIPLY);

                mTargetClimbProgress.setProgressDrawable(progressDrawable);
                mTargetClimbProgress.setProgress((int) climbProgress);
                mTargetClimbProgress.setMax(climbTotal);
                climbLayout.setVisibility(View.VISIBLE);
            } else {
                climbLayout.setVisibility(View.GONE);

            }

            if (!type || both) {
                String walkProgressFormat = df.format(walkProgress);
                mTargetWalkText.setText(walkProgressFormat + " of " + walkTotal);
                mUnitWalkText.setText(walkUnit);

                LayerDrawable progressDrawable = (LayerDrawable) getActivity().getResources().getDrawable(R.drawable.custom_progress);
                Drawable progressCircle = (Drawable) progressDrawable.findDrawableByLayerId(android.R.id.progress);
                progressCircle.setColorFilter(mColour, PorterDuff.Mode.MULTIPLY);

                mTargetWalkProgress.setProgressDrawable(progressDrawable);
                mTargetWalkProgress.setProgress((int) walkProgress);
                mTargetWalkProgress.setMax(walkTotal);
                walkLayout.setVisibility(View.VISIBLE);
            } else {
                walkLayout.setVisibility(View.GONE);
            }
        } else {
            Cursor mapCursor = mDbHelper.fetchMap(mapId);

            mapCursor.moveToFirst();

            final Double startLat = mapCursor.getDouble(mapCursor.getColumnIndexOrThrow(DBAdapter.KEY_MAP_START_LAT));
            final Double startLong = mapCursor.getDouble(mapCursor.getColumnIndexOrThrow(DBAdapter.KEY_MAP_START_LONG));
            final Double endLat = mapCursor.getDouble(mapCursor.getColumnIndexOrThrow(DBAdapter.KEY_MAP_END_LAT));
            final Double endLong = mapCursor.getDouble(mapCursor.getColumnIndexOrThrow(DBAdapter.KEY_MAP_END_LONG));


            final LatLng startPoint = new LatLng(startLat, startLong);
            final LatLng endPoint = new LatLng(endLat, endLong);

            if (targetPath != null) {
                targetPath.remove();
            }
            if (progressPath != null) {
                progressPath.remove();
            }


            final String stringUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" +
                    startPoint.latitude + "," + startPoint.longitude +
                    "&destination=" + endPoint.latitude + "," + endPoint.longitude +
                    "&mode=walking";

            StringRequest stringRequest = new StringRequest(Request.Method.GET, stringUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            int distanceProgress = 0;
                            boolean progressComplete = false;


                            try {
                                JSONObject jsonParse = new JSONObject(response);

                                JSONObject leg = jsonParse.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);

                                JSONObject endRoutePoint = leg.getJSONObject("end_location");

                                JSONArray steps = leg.getJSONArray("steps");

                                PolylineOptions tempPath = new PolylineOptions().geodesic(true)
                                        .color(getActivity().getResources().getColor(R.color.dark_space_gray))
                                        .width(20);


                                int mProgressColour = sp.getInt(getActivity().getResources().getString(R.string.pref_user_colour), getResources().getColor(R.color.light_space_gray));
                                PolylineOptions tempProgressPath = new PolylineOptions().geodesic(true)
                                        .color(mProgressColour)
                                        .width(10);
//
                                for (int i = 0; i < steps.length(); i++) {
                                    JSONObject step = steps.getJSONObject(i);

                                    int newDistance = step.getJSONObject("distance").getInt("value");


                                    JSONObject startStepPoint = step.getJSONObject("start_location");
                                    JSONObject endStepPoint = step.getJSONObject("end_location");

                                    if (!progressComplete) {
                                        tempProgressPath.add(new LatLng(startStepPoint.getDouble("lat"), startStepPoint.getDouble("lng")));
                                    }
                                    tempPath.add(new LatLng(startStepPoint.getDouble("lat"), startStepPoint.getDouble("lng")));

                                    if ((distanceProgress + newDistance >= walkProgress)&& !progressComplete) {
                                        double walkPercentage = ((walkProgress - distanceProgress) / newDistance);
                                        Double progressLat = startStepPoint.getDouble("lat") + ((endStepPoint.getDouble("lat") - startStepPoint.getDouble("lat")) * (walkPercentage));
                                        Double progressLong = startStepPoint.getDouble("lng") + ((endStepPoint.getDouble("lng") - startStepPoint.getDouble("lng")) * (walkPercentage));
                                        tempProgressPath.add(new LatLng(progressLat, progressLong));
                                        progressComplete = true;
                                    }
                                    distanceProgress = distanceProgress + newDistance;
                                }

                                if (!progressComplete) {
                                    tempProgressPath.add(new LatLng(endRoutePoint.getDouble("lat"), endRoutePoint.getDouble("lng")));
                                }

                                tempPath.add(new LatLng(endRoutePoint.getDouble("lat"), endRoutePoint.getDouble("lng")));

                                targetPath = mMap.addPolyline(tempPath);
                                progressPath = mMap.addPolyline(tempProgressPath);

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(progressPath.getPoints().get(progressPath.getPoints().size() - 1), 15);
                                mMap.animateCamera(cameraUpdate);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {


                    double walkPercentage = (walkProgress / walkTotal);

                    if (walkPercentage > 1) {
                        walkPercentage = 1;
                    }

                    targetPath = mMap.addPolyline(new PolylineOptions().geodesic(true)
                                    .color(getActivity().getResources().getColor(R.color.dark_space_gray))
                                    .width(20)
                                    .add(startPoint)
                                    .add(endPoint)
                    );

                    Double progressLat = startLat + ((endLat - startLat) * (walkPercentage));
                    Double progressLong = startLong + ((endLong - startLong) * (walkPercentage));


                    LatLng progressPoint = new LatLng(progressLat, progressLong);

                    int mProgressColour = sp.getInt(getActivity().getResources().getString(R.string.pref_user_colour), getResources().getColor(R.color.light_space_gray));

                    progressPath = mMap.addPolyline(new PolylineOptions().geodesic(true)
                                    .color(mProgressColour)
                                    .width(10)
                                    .add(startPoint)
                                    .add(progressPoint)
                    );
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(progressPath.getPoints().get(1), 15);
                    mMap.animateCamera(cameraUpdate);
                }
            });
            queue.add(stringRequest);

            mapProgressText.setText("Walked " + walkProgress + " of " + walkTotal + " " + walkUnit);
        }


        int mPlanetColour = sp.getInt(getActivity().getResources().getString(R.string.pref_user_colour), getResources().getColor(R.color.light_space_gray));
        if (!both) {
            if (type) {
                LayerDrawable iconDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.climb_icon);
                Drawable planet = (Drawable) iconDrawable.findDrawableByLayerId(R.id.planetIcon);
                planet.setColorFilter(mPlanetColour, PorterDuff.Mode.MULTIPLY);
                icon.setImageDrawable(iconDrawable);
            } else {
                LayerDrawable iconDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.walk_icon);
                Drawable planet = (Drawable) iconDrawable.findDrawableByLayerId(R.id.planetIcon);
                Drawable arrow = (Drawable) iconDrawable.findDrawableByLayerId(R.id.walkArrow);

                float[] hsb = new float[3];
                Color.colorToHSV(mPlanetColour, hsb);

                if (hsb[2] > 0.5) {
                    arrow.setColorFilter(getResources().getColor(R.color.dark_space_gray), PorterDuff.Mode.MULTIPLY);
                } else {
                    arrow.setColorFilter(getResources().getColor(R.color.light_space_gray), PorterDuff.Mode.MULTIPLY);
                }
                planet.setColorFilter(mPlanetColour, PorterDuff.Mode.MULTIPLY);
                icon.setImageDrawable(iconDrawable);
            }
        } else {
            LayerDrawable iconDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.both_icon);
            Drawable planet = (Drawable) iconDrawable.findDrawableByLayerId(R.id.planetIcon);
            Drawable arrow = (Drawable) iconDrawable.findDrawableByLayerId(R.id.walkArrow);

            float[] hsb = new float[3];
            Color.colorToHSV(mPlanetColour, hsb);

            if (hsb[2] > 0.5) {
                arrow.setColorFilter(getResources().getColor(R.color.dark_space_gray), PorterDuff.Mode.MULTIPLY);
            } else {
                arrow.setColorFilter(getResources().getColor(R.color.light_space_gray), PorterDuff.Mode.MULTIPLY);
            }
            planet.setColorFilter(mPlanetColour, PorterDuff.Mode.MULTIPLY);
            icon.setImageDrawable(iconDrawable);
        }

        Date end = new Date(endNumber);
        String endDate = dateFormat.format(end);
        String endTime = timeFormat.format(end);
        mendDateText.setText(endDate + " " + endTime);

        Date start = new Date(startNumber);
        String startDate = dateFormat.format(start);
        String startTime = timeFormat.format(start);
        mstartDateText.setText(startDate + " " + startTime);

        String statusText = "";

        switch (statusNumber) {
            case GlobalVariables.GOAL_MODE_ACTIVE:
                statusText = "Active";
                mStatusText.setTextColor(getResources().getColor(R.color.dark_space_gray));
                break;
            case GlobalVariables.GOAL_MODE_PENDING:
                statusText = "Pending";
                mStatusText.setTextColor(Color.parseColor("#FFB77600"));
                break;
            case GlobalVariables.GOAL_MODE_ABANDONED:
                statusText = "Abandoned";
                mStatusText.setTextColor(Color.parseColor("#FF000000"));
                break;
            case GlobalVariables.GOAL_MODE_OVERDUE:
                statusText = "Overdue";
                mStatusText.setTextColor(Color.parseColor("#FF880000"));
                break;
            case GlobalVariables.GOAL_MODE_COMPLETED:
                statusText = "Completed";
                mStatusText.setTextColor(Color.parseColor("#FF008800"));
                break;
            case GlobalVariables.GOAL_MODE_SUSPENDED:
                statusText = "Suspended";
                mStatusText.setTextColor(Color.parseColor("#FF777700"));
                break;
        }

        mStatusText.setText(statusText);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

}
