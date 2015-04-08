package com.uni.ard.fitnesstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

/**
 * Created by Adam on 26/03/2015.
 */
public class ColorPreference extends DialogPreference {

    ColorPicker picker;
    SVBar svBar;

    private int mColour;

    String colourKey;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setDialogLayoutResource(R.layout.color_picker_layout);
    }

    //    private MyCustomView myView;
//
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // the view was created by my custom onCreateDialogView()

        picker = (ColorPicker) view.findViewById(R.id.picker);
        svBar = (SVBar) view.findViewById(R.id.svbar);

        picker.addSVBar(svBar);

        colourKey = getContext().getResources().getString(R.string.pref_user_colour);

        SharedPreferences sharedPreferences = getSharedPreferences();
        mColour = sharedPreferences.getInt(colourKey, getContext().getResources().getColor(R.color.light_space_gray));

        picker.setColor(mColour);
        svBar.setColor(mColour);
        picker.setOldCenterColor(mColour);
        picker.setNewCenterColor(mColour);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            SharedPreferences.Editor editor = getEditor();
            editor.putInt(colourKey, picker.getColor());
            editor.commit();
            Toast logToast = Toast.makeText(getContext(), "Colour changed", Toast.LENGTH_LONG);
            logToast.show();
        }
    }
}
