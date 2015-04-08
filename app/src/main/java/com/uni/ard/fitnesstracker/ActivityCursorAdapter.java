package com.uni.ard.fitnesstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Adam on 14/02/2015.
 */
public class ActivityCursorAdapter extends CursorAdapter {

    public ActivityCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_cell, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        TextView type = (TextView) view.findViewById(R.id.textActionType);
        TextView value = (TextView) view.findViewById(R.id.value);
        TextView unit = (TextView) view.findViewById(R.id.valueUnit);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);


        // Extract properties from cursor
        Integer valueNumber = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_NUMBER));
        String unitText = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ACTIVITY_UNIT));
        boolean typeClimb = cursor.getInt(
                cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int mColour = sp.getInt(context.getResources().getString(R.string.pref_user_colour), context.getResources().getColor(R.color.light_space_gray));

        Drawable planet = (Drawable) context.getResources().getDrawable(R.drawable.planet_circle);
        planet.setColorFilter(mColour, PorterDuff.Mode.MULTIPLY);
        icon.setImageDrawable(planet);


        float[] hsb = new float[3];
        Color.colorToHSV(mColour, hsb);

        if (hsb[2] > 0.5) {
            value.setTextColor(context.getResources().getColor(R.color.dark_space_gray));
        } else {
            value.setTextColor(context.getResources().getColor(R.color.light_space_gray));
        }
        value.setText(valueNumber + "");

        unit.setText(unitText);
        if (typeClimb) {
            type.setText("Climbed");
        } else {
            type.setText("Walked");
        }
    }

}
