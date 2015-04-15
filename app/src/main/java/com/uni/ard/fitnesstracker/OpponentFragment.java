package com.uni.ard.fitnesstracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.uni.ard.fitnesstracker.dummy.DummyContent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class OpponentFragment extends Fragment implements AbsListView.OnItemClickListener, WifiP2pManager.PeerListListener {


    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    private ListAdapter mAdapter;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    WifiP2pManager.ActionListener mActionListener;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_LOG = 1;
    private static final int TREAT_CREATE = 3;

    DBAdapter mDbHelper;


    public static OpponentFragment newInstance() {
        OpponentFragment fragment = new OpponentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OpponentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }


        mDbHelper = new DBAdapter(getActivity());
        mDbHelper.open();

        //mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
        //      android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);


        mManager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getActivity().getBaseContext(), getActivity().getMainLooper(), null);
        mReceiver = new OpponentReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        mActionListener = new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //Log.d("WiFi", "discovering");
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank.  Code for peer discovery goes in the
                // onReceive method, detailed below.
            }

            @Override
            public void onFailure(int reasonCode) {
                //Log.d("WiFi", "discovering failed");
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
            }
        };


        mManager.stopPeerDiscovery(mChannel, mActionListener);
        mManager.discoverPeers(mChannel, mActionListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opponent, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("WiFi", "Item Clicked");

        //EditGoal sharedGoal = new EditGoal();
        Intent i = new Intent(getActivity(), EditGoal.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mIntentFilter);
        mManager.discoverPeers(mChannel, mActionListener);
    }

    /* unregister the broadcast receiver */
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
        mManager.stopPeerDiscovery(mChannel, mActionListener);
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        peers.clear();
        peers.addAll(peerList.getDeviceList());


        // If an AdapterView is backed by this data, notify it
        // of the change.  For instance, if you have a ListView of available
        // peers, trigger an update.
//                ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

        List<String> namesList = new ArrayList<String>();
        if (peers.size() == 0) {
            Log.d("WiFi", "No devices found");
        } else {
            Log.d("WiFi", "Devices found");
            for (WifiP2pDevice w : peers) {
                namesList.add(w.deviceName);
            }
        }

        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, namesList);
        mListView.setAdapter(mAdapter);

    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Pass the activity result to the fragment, which will
        // then pass the result to the login button.

        if (resultCode == Activity.RESULT_OK) {
            Bundle extras = intent.getExtras();
            switch (requestCode) {
                case ACTIVITY_CREATE:
                    String title = extras.getString(DBAdapter.KEY_GOAL_TITLE);
                    Integer walkTarget = extras.getInt(DBAdapter.KEY_GOAL_WALK);
                    String walkUnit = extras.getString(DBAdapter.KEY_GOAL_WALK_UNIT);
                    Integer climbTarget = extras.getInt(DBAdapter.KEY_GOAL_CLIMB);
                    String climbUnit = extras.getString(DBAdapter.KEY_GOAL_CLIMB_UNIT);
                    Long start = extras.getLong(DBAdapter.KEY_GOAL_START);
                    Long end = extras.getLong(DBAdapter.KEY_GOAL_END);
                    Boolean climb = extras.getBoolean(DBAdapter.KEY_TYPE);
                    Boolean both = extras.getBoolean(DBAdapter.KEY_GOAL_DUAL);
                    int status = GlobalVariables.GOAL_MODE_PENDING;
                    boolean active = true;
                    Date currentDate = new Date(GlobalVariables.getTime());
                    Date newDate = new Date(start);
                    Date newEndDate = new Date(end);
                    if (!newDate.after(currentDate)) {
                        status = GlobalVariables.GOAL_MODE_ACTIVE;
                    } else if (!newEndDate.after(currentDate)) {
                        status = GlobalVariables.GOAL_MODE_OVERDUE;
                    }

                    Long goalId = mDbHelper.createGoal(title, walkTarget, climbTarget, walkUnit, climbUnit, start, end, climb, both, false, active, status);
                    Long mapId = extras.getLong(DBAdapter.KEY_GOAL_MAP);
                    if (mapId != null) {
                        mDbHelper.updateGoalMap(goalId, mapId);
                    }

                    Long calorieId = extras.getLong(DBAdapter.KEY_GOAL_CALORIE);
                    if (calorieId != null) {
                        mDbHelper.connectTreat(goalId, calorieId);
                    }
                    Toast toast = Toast.makeText(this.getActivity(), "Goal created", Toast.LENGTH_LONG);
                    toast.show();

                    //Send crap
                    connect(peers.get(0));

                    break;
            }
        }
    }

    private void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(), "Connect success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getActivity(), "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
