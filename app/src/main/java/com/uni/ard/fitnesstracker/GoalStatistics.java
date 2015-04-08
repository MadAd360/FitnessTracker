package com.uni.ard.fitnesstracker;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class GoalStatistics extends Fragment {

    Long rowId;
    private DBAdapter mDbHelper;

    GraphView graph;
    DateFormat dateFormat;
    Long currentFocus;
    boolean statTypeClimb;


    public static GoalStatistics newInstance(Long rowId, Boolean typeClimb) {
        GoalStatistics fragment = new GoalStatistics();
        Bundle args = new Bundle();
        args.putLong(DBAdapter.KEY_ROWID, rowId);
        args.putBoolean(DBAdapter.KEY_TYPE, typeClimb);
        fragment.setArguments(args);
        return fragment;
    }

    public GoalStatistics() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DBAdapter(getActivity());
        mDbHelper.open();

        dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());

        if (getArguments() != null) {
            rowId = getArguments().getLong(DBAdapter.KEY_ROWID);
            statTypeClimb = getArguments().getBoolean(DBAdapter.KEY_TYPE);
        }

        if (currentFocus == null) {
            currentFocus = GlobalVariables.getTime();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_goal_statistics, container, false);

        Cursor goalCursor = mDbHelper.fetchGoal(rowId);

        int target;
        if (statTypeClimb) {
            target = goalCursor.getInt(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB));
        } else {
            target = goalCursor.getInt(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK));
        }
        Long start = goalCursor.getLong(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_START));
        Long end = goalCursor.getLong(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_END));

        Cursor cursor = mDbHelper.fetchGoalActivities(rowId);

        ArrayList<DataPoint> progressPoints = new ArrayList<DataPoint>();

        double previousScore = 0;
        Long current = currentFocus;
        Long latestTime = currentFocus;

        while (cursor.moveToNext()) {
            Boolean activityType = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
            String goalUnit;
            if (activityType) {
                goalUnit = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB_UNIT));
            } else {
                goalUnit = goalCursor.getString(goalCursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK_UNIT));
            }
            if (activityType == statTypeClimb) {
                Long time = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_DATE));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_UNIT));
                Integer distance = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_NUMBER));
                double distanceFormat = GlobalVariables.convertUnits(distance, statTypeClimb, goalUnit, unit);
                distanceFormat = distanceFormat + previousScore;
                progressPoints.add(new DataPoint(time, previousScore));
                progressPoints.add(new DataPoint(time, distanceFormat));
                previousScore = distanceFormat;
                latestTime = time;
            }
        }

        progressPoints.add(new DataPoint(current, previousScore));

        graph = (GraphView) view.findViewById(R.id.graph);
        DataPoint[] progressArray = new DataPoint[progressPoints.size()];
        progressPoints.toArray(progressArray);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(progressArray);
        graph.addSeries(series);
        series.setTitle("Progress");
        series.setColor(Color.parseColor("#FFB77600"));

        Date startDate = new Date(start);
        Date endDate = new Date(end);


        if (previousScore >= target) {
            current = latestTime;
        }
        Date currentDate = new Date(current);

        if (startDate.before(currentDate) && previousScore > 0) {
            double difference = current - start;
            double ratio = previousScore / (((((difference / 1000) / 60) / 60)));

            long length = (long) ((((target - previousScore) / ratio) * 60 * 60 * 1000) + current);

            DataPoint[] estimationArray = new DataPoint[2];
            estimationArray[0] = new DataPoint(current, previousScore);
            estimationArray[1] = new DataPoint(length, target);

            LineGraphSeries<DataPoint> seriesEstimate = new LineGraphSeries<DataPoint>(estimationArray);
            graph.addSeries(seriesEstimate);
            seriesEstimate.setTitle("Estimate");
            TextView estimate = (TextView) view.findViewById(R.id.estimate);
            String estimateText = dateFormat.format(length);
            estimate.setText(estimateText);
            Date estimateDate = new Date(length);
            if (estimateDate.after(endDate)) {
                estimate.setTextColor(Color.parseColor("#FF880000"));
                seriesEstimate.setColor(Color.parseColor("#FF880000"));
            } else {
                estimate.setTextColor(Color.parseColor("#FF008800"));
                seriesEstimate.setColor(Color.parseColor("#FF008800"));
            }

            double endAfterStart = end - start;
            double targetPercentValue = endAfterStart * 0.7;
            long estimateAfterStart = length - start;

            TextView hint = (TextView) view.findViewById(R.id.hint);
            if (estimateAfterStart < targetPercentValue) {
                hint.setText("Hint: You may want to set a greater target in the future");
            } else if (estimateAfterStart > targetPercentValue * 2) {
                hint.setText("Hint: You may want to set a smaller target in the future");
            } else if (estimateAfterStart > endAfterStart) {
                hint.setText("Hint: You require a little more effort to meet the deadline");
            } else {
                hint.setText("You are on target");
            }
            if (previousScore >= target) {
                TextView estimateMessage = (TextView) view.findViewById(R.id.estimateMessage);
                estimateMessage.setText("Completion date =");
            }
        } else {
            TextView estimate = (TextView) view.findViewById(R.id.estimate);
            estimate.setText("N/A");
            TextView hint = (TextView) view.findViewById(R.id.hint);
            hint.setText("No progress recorded towards goal");
        }

        if (endDate.after(currentDate)) {
            DataPoint[] requiredArray = new DataPoint[2];
            requiredArray[0] = new DataPoint(current, previousScore);
            requiredArray[1] = new DataPoint(end, target);

            LineGraphSeries<DataPoint> seriesRequired = new LineGraphSeries<DataPoint>(requiredArray);
            graph.addSeries(seriesRequired);
            seriesRequired.setTitle("Required");
            seriesRequired.setColor(Color.parseColor("#FF777700"));
            TextView required = (TextView) view.findViewById(R.id.required);
            double difference = end - current;
            int day = (int) (((((difference / 1000) / 60) / 60) / 24));
            day++;
            double requiredValue = (target - previousScore) / (day);
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            required.setText("" + df.format(requiredValue));
            if (previousScore >= target) {
                required.setText("0");
            }
        } else {
            TextView required = (TextView) view.findViewById(R.id.required);
            required.setText("N/A");
        }

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(target);

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);
        setViewPort();

        Button previous = (Button) view.findViewById(R.id.previous);
        Button next = (Button) view.findViewById(R.id.next);

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousDay();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextDay();
            }
        });


        return view;
    }

    public void setViewPort() {
        graph.getViewport().setXAxisBoundsManual(true);
        Date focusDate = new Date(currentFocus);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(focusDate);
        calendar.add(Calendar.DATE, -2);
        graph.getViewport().setMinX(calendar.getTime().getTime());
        calendar.add(Calendar.DATE, 4);
        graph.getViewport().setMaxX(calendar.getTime().getTime());
        graph.getGridLabelRenderer().invalidate(true, false);
    }

    public void previousDay() {
        Date focusDate = new Date(currentFocus);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(focusDate);
        calendar.add(Calendar.DATE, -1);
        currentFocus = calendar.getTime().getTime();
        graph.invalidate();
        setViewPort();
    }

    public void nextDay() {
        Date focusDate = new Date(currentFocus);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(focusDate);
        calendar.add(Calendar.DATE, 1);
        currentFocus = calendar.getTime().getTime();
        graph.invalidate();
        setViewPort();
    }

}
