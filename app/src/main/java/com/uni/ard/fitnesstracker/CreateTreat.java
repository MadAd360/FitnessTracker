package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class CreateTreat extends Activity {

    private static final int TREAT_ID = 1;

    EditText treatNameView;
    EditText treatCaloriesView;
    ImageButton treatImageView;
    Bitmap iconImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_treat);

        treatNameView = (EditText) findViewById(R.id.treatName);
        treatCaloriesView = (EditText) findViewById(R.id.treatCalories);
        treatImageView = (ImageButton) findViewById(R.id.treatImage);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_treat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addImage(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), TREAT_ID);
    }

    public void cancelTreat(View view) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void confirmTreat(View view) {
        Bundle bundle = new Bundle();
        bundle.putString(DBAdapter.KEY_TREAT_NAME, treatNameView.getText().toString());
        bundle.putInt(DBAdapter.KEY_TREAT_CALORIES, new Integer(treatCaloriesView.getText().toString()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int shrink = iconImage.getHeight()/300;
        if(shrink < 1){
            shrink = 1;
        }
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(iconImage, iconImage.getWidth()/shrink, iconImage.getHeight()/shrink, true);
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        byte[] data = outputStream.toByteArray();
        bundle.putByteArray(DBAdapter.KEY_TREAT_IMAGE, data);
        Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Uri selectedImageUri = intent.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                int shrink = bitmap.getHeight()/1000;
                if(shrink < 1){
                    shrink = 1;
                }
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/shrink, bitmap.getHeight()/shrink, true);
                treatImageView.setImageBitmap(scaledBitmap);
                iconImage = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            //save image
        }
    }
}
