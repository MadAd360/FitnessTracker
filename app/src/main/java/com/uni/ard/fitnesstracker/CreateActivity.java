package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.UUID;


public class CreateActivity extends Activity {

    private EditText mBodyText;
    private Spinner spinner;
    private Spinner spinnerGoal;
    private Switch mTypeSwitch;
    private Long mRowId;
    private DBAdapter mDbHelper;
    private Cursor mNotesCursor;
    private GoalAttachAdapter spinnerGoalAdapter;

    private boolean goalSet = false;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    Log.d("BlueDevice", device.getName());
                    scanLeDevice(false);
                    mBluetoothGatt = device.connectGatt(getBaseContext(), false, mGattCallback);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mLeDeviceListAdapter.addDevice(device);
//                                mLeDeviceListAdapter.notifyDataSetChanged();
//                            }
//                        });
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle("Log Activity");
        getActionBar().setDisplayHomeAsUpEnabled(true);


        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        mDbHelper = new DBAdapter(this);
        mDbHelper.open();

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.measurement_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean unitDisable = sp.getBoolean(getResources().getString(R.string.pref_units_disable), false);

        if (unitDisable) {
            spinner.setEnabled(false);
        }


        spinnerGoal = (Spinner) findViewById(R.id.spinnerGoals);

        mNotesCursor = mDbHelper.fetchLogGoals(false);


        mBodyText = (EditText) findViewById(R.id.numberOf);
        mTypeSwitch = (Switch) findViewById(R.id.typeSwitch);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        startManagingCursor(mNotesCursor);
        spinnerGoalAdapter = new GoalAttachAdapter(this, mNotesCursor);
        spinnerGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(spinnerGoalAdapter);

        mRowId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            goalSet = true;
            Long goalRow = extras.getLong(DBAdapter.KEY_ACTIVITY_GOAL);
            Cursor cursor = mDbHelper.fetchGoal(goalRow);
            cursor.moveToFirst();
            boolean goalBoth = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_DUAL)) > 0;
            boolean goalType = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) > 0;
            if (!goalBoth) {
                if (goalType) {
                    mTypeSwitch.setChecked(true);
                    mTypeSwitch.setVisibility(View.GONE);
                } else {
                    mTypeSwitch.setChecked(false);
                    mTypeSwitch.setVisibility(View.GONE);
                }
                mNotesCursor = mDbHelper.fetchLogGoals(mTypeSwitch.isChecked());
                spinnerGoalAdapter = new GoalAttachAdapter(this, mNotesCursor);
                spinnerGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGoal.setAdapter(spinnerGoalAdapter);
            }
            String goalName = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_GOAL_TITLE));
            spinnerGoal.setSelection(spinnerGoalAdapter.getPosition(goalName));
            spinnerGoal.setVisibility(View.GONE);
        }


        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Boolean typeClimb = mTypeSwitch.isChecked();
                String distanceText = mBodyText.getText().toString();
                String unitText = spinner.getSelectedItem().toString();

                if (!distanceText.isEmpty() && !unitText.isEmpty()) {


                    Bundle bundle = new Bundle();

                    bundle.putInt(DBAdapter.KEY_ACTIVITY_NUMBER, new Integer(distanceText));
                    bundle.putString(DBAdapter.KEY_ACTIVITY_UNIT, unitText);
                    bundle.putBoolean(DBAdapter.KEY_TYPE, typeClimb);
                    Long goalNumber = spinnerGoalAdapter.getRowID(spinnerGoal.getSelectedItemPosition());
                    bundle.putLong(DBAdapter.KEY_ACTIVITY_GOAL, goalNumber);
                    if (mRowId != null) {
                        bundle.putLong(DBAdapter.KEY_ROWID, mRowId);
                    }

                    Intent mIntent = new Intent();
                    mIntent.putExtras(bundle);
                    setResult(RESULT_OK, mIntent);
                    close();
                    finish();
                } else {
                    Toast toast = Toast.makeText(CreateActivity.this, "Blank values exist.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }

        });
    }

    public void updateDropdown(View view) {
        if (!goalSet) {
            mNotesCursor = mDbHelper.fetchLogGoals(mTypeSwitch.isChecked());
            spinnerGoalAdapter = new GoalAttachAdapter(this, mNotesCursor);
            spinnerGoalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerGoal.setAdapter(spinnerGoalAdapter);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            Log.d("close", "not exist");
            return;
        }

        Log.d("close", mBluetoothGatt.getDevice().getName());

        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void cancelGoal(View view) {
        setResult(Activity.RESULT_CANCELED);
        close();

        finish();
    }

    public void trackCalories(View view){
        mBluetoothAdapter.cancelDiscovery();
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }

        if(mBluetoothGatt == null){
            scanLeDevice(true);
        }else {
            Log.d("BlueDevice", "START READ");
            //BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(UUID_HEART_RATE_MEASUREMENT, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
            //mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            Log.d("Bluetooth", characteristic.getService()+"");
            Log.d("Bluetooth", characteristic.getUuid()+"");
            Log.d("Bluetooth", mBluetoothGatt.readCharacteristic(characteristic) + "");
        }


    }

    private boolean mScanning;
    private Handler mHandler = new Handler();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private void scanLeDevice(final boolean enable) {



        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }


    private BluetoothGattCharacteristic characteristic;
    private BluetoothManager mBluetoothManager;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");



    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i("BLUE", "Connected to GATT server.");
                        Log.i("BLUE", "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i("BLUE", "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.d("BlueDevice", "SERVICE");
                    characteristic = mBluetoothGatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID_HEART_RATE_MEASUREMENT);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    } else {
                        Log.w("BLUE", "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.d("BlueDevice", "READ");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }
            };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        Log.d("BLUE", "update");
        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d("BLUE", "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d("BLUE", "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d("BLUE", String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }


//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (ACTION_GATT_CONNECTED.equals(action)) {
//                //mConnected = true;
//                //updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
//            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
//                //mConnected = false;
//                //updateConnectionState(R.string.disconnected);
//                Log.d("BLUERECEIVER", "Disconnected");
//                //invalidateOptionsMenu();
//                //clearUI();
//            } else if (
//                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                // Show all the supported services and characteristics on the
//                // user interface.
//                //displayGattServices(getSupportedGattServices());
//            } else if (ACTION_DATA_AVAILABLE.equals(action)) {
//                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//            }
//        }
//    };
}
