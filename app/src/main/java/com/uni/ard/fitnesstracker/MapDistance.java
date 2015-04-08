package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapDistance extends FragmentActivity implements GoogleMap.OnMapClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    boolean setStartPoint = false;
    boolean setEndPoint = false;
    Marker startPoint;
    Marker endPoint;
    Polyline path;
    RequestQueue queue;
    int distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_distance);
        setUpMapIfNeeded();

        queue = Volley.newRequestQueue(this);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.startPoint) {
            setStart();
            return true;
        }

        if (item.getItemId() == R.id.endPoint) {
            setEnd();
            return true;
        }
        return false;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setOnMapClickListener(this);
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (setStartPoint) {
            startPoint = mMap.addMarker(new MarkerOptions().position(latLng).title("Start Point"));
            setStartPoint = false;
        } else if (setEndPoint) {
            endPoint = mMap.addMarker(new MarkerOptions().position(latLng).title("End Point"));
            setEndPoint = false;
        }
        if (endPoint != null && startPoint != null) {
            if (path != null) {
                path.remove();
            }

            final String stringUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" +
                    startPoint.getPosition().latitude + "," + startPoint.getPosition().longitude +
                    "&destination=" + endPoint.getPosition().latitude + "," + endPoint.getPosition().longitude +
                    "&mode=walking";

            distance = 0;

            StringRequest stringRequest = new StringRequest(Request.Method.GET, stringUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", stringUrl);
//                            Log.d("Response", response);
                            if (path != null) {
                                path.remove();
                            }
                            try {
                                JSONObject jsonParse = new JSONObject(response);

                                JSONObject leg = jsonParse.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);

//                                Log.d("ResponseStep", steps.toString());
                                JSONObject endRoutePoint = leg.getJSONObject("end_location");

                                JSONArray steps = leg.getJSONArray("steps");

                                PolylineOptions tempPath = new PolylineOptions().geodesic(true);
//
                                for (int i = 0; i < steps.length(); i++) {
                                    JSONObject step = steps.getJSONObject(i);


                                    JSONObject startRoutePoint = step.getJSONObject("start_location");

                                    distance = distance + step.getJSONObject("distance").getInt("value");

                                    tempPath.add(new LatLng(startRoutePoint.getDouble("lat"), startRoutePoint.getDouble("lng")));
                                }

                                tempPath.add(new LatLng(endRoutePoint.getDouble("lat"), endRoutePoint.getDouble("lng")));

                                path = mMap.addPolyline(tempPath);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (path != null) {
                        path.remove();
                    }
                    path = mMap.addPolyline(new PolylineOptions().geodesic(true)
                                    .add(startPoint.getPosition())
                                    .add(endPoint.getPosition())
                    );

                    float[] results = new float[1];
                    Location.distanceBetween(startPoint.getPosition().latitude, startPoint.getPosition().longitude,
                            endPoint.getPosition().latitude, endPoint.getPosition().longitude, results);
                    distance = (int) results[0];
                }
            });
            queue.add(stringRequest);

            path = mMap.addPolyline(new PolylineOptions().geodesic(true)
                            .add(startPoint.getPosition())
                            .add(endPoint.getPosition())
            );
        }
    }

    public void setStart() {
        if (path != null) {
            path.remove();
        }
        if (startPoint != null) {
            startPoint.remove();
        }
        setEndPoint = false;
        setStartPoint = true;
    }

    public void setEnd() {
        if (path != null) {
            path.remove();
        }
        if (endPoint != null) {
            endPoint.remove();
        }
        setEndPoint = true;
        setStartPoint = false;
    }

    public void cancelMap(View view) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void confirmPoints(View v) {
        if (startPoint == null) {
            Toast toast = Toast.makeText(MapDistance.this, "Start point is not set", Toast.LENGTH_LONG);
            toast.show();
        } else if (endPoint == null) {
            Toast toast = Toast.makeText(MapDistance.this, "End point is not set", Toast.LENGTH_LONG);
            toast.show();
        } else {
            Bundle bundle = new Bundle();
            LatLng startPosition = startPoint.getPosition();
            LatLng endPosition = endPoint.getPosition();

            bundle.putInt(DBAdapter.KEY_GOAL_WALK, distance);
            bundle.putString(DBAdapter.KEY_GOAL_WALK_UNIT, "Metres");
            bundle.putDouble(DBAdapter.KEY_MAP_START_LAT, startPosition.latitude);
            bundle.putDouble(DBAdapter.KEY_MAP_START_LONG, startPosition.longitude);
            bundle.putDouble(DBAdapter.KEY_MAP_END_LAT, endPosition.latitude);
            bundle.putDouble(DBAdapter.KEY_MAP_END_LONG, endPosition.longitude);

            Intent mIntent = new Intent();
            mIntent.putExtras(bundle);
            setResult(RESULT_OK, mIntent);
            finish();
        }
    }


}
