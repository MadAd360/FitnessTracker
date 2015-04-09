package com.uni.ard.fitnesstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayInputStream;

/**
 * Created by Adam on 14/02/2015.
 */
public class TreatCursorAdapter extends CursorAdapter {

    public TreatCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_treat_cell, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        TextView nameView = (TextView) view.findViewById(R.id.textTreatName);
        TextView calorieView = (TextView) view.findViewById(R.id.calories);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);


        // Extract properties from cursor
        Integer caloriesNumber = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TREAT_CALORIES));
        String treatName = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TREAT_NAME));

        byte[] imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TREAT_IMAGE));

        ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
        Bitmap iconImage= BitmapFactory.decodeStream(imageStream);
        icon.setImageBitmap(iconImage);

        nameView.setText(treatName);
        calorieView.setText(caloriesNumber + "");
    }

}
