package com.uni.ard.fitnesstracker;

import android.content.Context;
import android.database.Cursor;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Adam on 16/02/2015.
 */
public class GoalAttachAdapter extends ArrayAdapter {

    ArrayList<Long> elements;


    public GoalAttachAdapter(Context context, Cursor cursor) {
        super(context, android.R.layout.simple_spinner_item);
        elements = new ArrayList<Long>();
        add("No Goal");
        elements.add(new Long(0));
        if (cursor.moveToFirst()) {
            do {
                Long rowID = cursor.getLong(cursor.getColumnIndex(DBAdapter.KEY_ROWID));
                String title = cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_GOAL_TITLE));
                add(title);
                elements.add(rowID);
            } while (cursor.moveToNext());
        }
    }

    public Long getRowID(int position) {
        return elements.get(position);
    }
}
