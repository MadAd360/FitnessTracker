package com.uni.ard.fitnesstracker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Adam on 14/02/2015.
 */
public class HistoryCursorAdapter extends CursorAdapter {

    public HistoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.history_row, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        TextView dateView = (TextView) view.findViewById(R.id.date);
        TextView messageView = (TextView) view.findViewById(R.id.message);

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);


        // Extract properties from cursor
        Long dateNumber = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_HISTORY_DATE));
        String message = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_HISTORY_MESSAGE));

        String dateText = dateFormat.format(new Date(dateNumber));
        String timeText = timeFormat.format(new Date(dateNumber));

        messageView.setText(message);
        dateView.setText(dateText + " " + timeText);
    }

}
