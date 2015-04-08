package com.uni.ard.fitnesstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Adam on 14/02/2015.
 */
public class GoalCursorAdapter extends CursorAdapter {

    public GoalCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.goal_row, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated templateString[] from = new String[] { DBAdapter.KEY_GOAL_TITLE, DBAdapter.KEY_GOAL_TOTAL};
        int[] to = new int[]{R.id.title, R.id.total};
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView progressValue = (TextView) view.findViewById(R.id.total);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        ImageView icon = (ImageView) view.findViewById(R.id.action_icon);
        TextView date = (TextView) view.findViewById(R.id.date);

        DBAdapter mDbHelper = new DBAdapter(context);
        mDbHelper.open();


        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);


        // Extract properties from cursor


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int mColour = sp.getInt(context.getResources().getString(R.string.pref_user_colour), context.getResources().getColor(R.color.light_space_gray));
        boolean both = cursor.getInt(
                cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_DUAL)) > 0;
        LayerDrawable iconDrawable;
        if (!both) {
            boolean type = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
            if (type) {
                iconDrawable = (LayerDrawable) context.getResources().getDrawable(R.drawable.climb_icon);
                Drawable planet = (Drawable) iconDrawable.findDrawableByLayerId(R.id.planetIcon);

                planet.setColorFilter(mColour, PorterDuff.Mode.MULTIPLY);
            } else {
                iconDrawable = (LayerDrawable) context.getResources().getDrawable(R.drawable.walk_icon);
                Drawable planet = (Drawable) iconDrawable.findDrawableByLayerId(R.id.planetIcon);
                Drawable arrow = (Drawable) iconDrawable.findDrawableByLayerId(R.id.walkArrow);

                float[] hsb = new float[3];
                Color.colorToHSV(mColour, hsb);


                if (hsb[2] > 0.5) {
                    arrow.setColorFilter(context.getResources().getColor(R.color.dark_space_gray), PorterDuff.Mode.MULTIPLY);
                } else {
                    arrow.setColorFilter(context.getResources().getColor(R.color.light_space_gray), PorterDuff.Mode.MULTIPLY);
                }
                planet.setColorFilter(mColour, PorterDuff.Mode.MULTIPLY);
            }
        } else {
            iconDrawable = (LayerDrawable) context.getResources().getDrawable(R.drawable.both_icon);
            Drawable planet = (Drawable) iconDrawable.findDrawableByLayerId(R.id.planetIcon);
            Drawable arrow = (Drawable) iconDrawable.findDrawableByLayerId(R.id.walkArrow);

            float[] hsb = new float[3];
            Color.colorToHSV(mColour, hsb);


            if (hsb[2] > 0.5) {
                arrow.setColorFilter(context.getResources().getColor(R.color.dark_space_gray), PorterDuff.Mode.MULTIPLY);
            } else {
                arrow.setColorFilter(context.getResources().getColor(R.color.light_space_gray), PorterDuff.Mode.MULTIPLY);
            }
            planet.setColorFilter(mColour, PorterDuff.Mode.MULTIPLY);
        }
        String titleText = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TITLE));
        Long startNumber = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_START));
        Long endNumber = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_END));
        // Populate fields with extracted properties
        Long rowId = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROWID));
        double walkProgress = mDbHelper.getGoalWalkProgress(rowId);
        double climbProgress = mDbHelper.getGoalClimbProgress(rowId);
        int walkTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_WALK));
        int climbTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_CLIMB));
        if (walkProgress > walkTotal) {
            walkProgress = walkTotal;
        }
        if (climbProgress > climbTotal) {
            climbProgress = climbTotal;
        }
        int progress = (int) (walkProgress + climbProgress);
        int goal = walkTotal + climbTotal;
        title.setText(titleText);
        progressValue.setText(progress + "/" + goal);
        progressBar.setMax(goal);
        progressBar.setProgress(progress);

        boolean active = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_ACTIVE)) > 0;
        boolean closed = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_COMPLETE)) > 0;

        boolean completed = false;
        if (walkProgress >= walkTotal && climbProgress >= climbTotal) {
            completed = true;
        }

        int statusNumber = GlobalVariables.getStatus(active, closed, startNumber, endNumber, completed);

        if (statusNumber == GlobalVariables.GOAL_MODE_PENDING) {
            String startText = dateFormat.format(new Date(startNumber));
            date.setText("Start: " + startText);

        } else {
            String endText = dateFormat.format(new Date(endNumber));
            date.setText("End: " + endText);
        }

        mDbHelper.updateGoalPreviousState(rowId, statusNumber);


        LayerDrawable listBackground = (LayerDrawable) view.getBackground();
        Drawable listOutline = (Drawable) listBackground.findDrawableByLayerId(R.id.outline);


        listOutline.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
        listBackground.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);

        switch (statusNumber) {
            case GlobalVariables.GOAL_MODE_ACTIVE:
                listOutline.setColorFilter(0xFF999999, PorterDuff.Mode.SRC_ATOP);
                break;
            case GlobalVariables.GOAL_MODE_PENDING:
                listOutline.setColorFilter(0xFFB77600, PorterDuff.Mode.SRC_ATOP);
                break;
            case GlobalVariables.GOAL_MODE_ABANDONED:
                listBackground.setColorFilter(0xCC888888, PorterDuff.Mode.MULTIPLY);
                iconDrawable.setColorFilter(0xCC888888, PorterDuff.Mode.MULTIPLY);
                break;
            case GlobalVariables.GOAL_MODE_OVERDUE:
                listOutline.setColorFilter(0xFF880000, PorterDuff.Mode.SRC_ATOP);
                break;
            case GlobalVariables.GOAL_MODE_COMPLETED:
                listOutline.setColorFilter(0xFF008800, PorterDuff.Mode.SRC_ATOP);
                break;
            case GlobalVariables.GOAL_MODE_SUSPENDED:
                listOutline.setColorFilter(0xFFBBBB00, PorterDuff.Mode.SRC_ATOP);
                break;
        }

        icon.setImageDrawable(iconDrawable);
    }

}
