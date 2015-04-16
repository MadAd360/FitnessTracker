package com.uni.ard.fitnesstracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class OpponentReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.PeerListListener peerListListener;

    public OpponentReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                            OpponentFragment activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.peerListListener = activity;
        Log.d("WiFi", "Created receiver");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("WiFi", action);

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d("WiFi", "enabled");
                // Wifi P2P is enabled
            } else {
                // Wi-Fi P2P is not enabled
                Log.d("WiFi", "disabled");
            }
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            Log.d("WiFi", "peers");
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

//    public void discoverDevices(){
//
//
//        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                Log.d("WiFi", "discovering");
//                // Code for when the discovery initiation is successful goes here.
//                // No services have actually been discovered yet, so this method
//                // can often be left blank.  Code for peer discovery goes in the
//                // onReceive method, detailed below.
//            }
//
//            @Override
//            public void onFailure(int reasonCode) {
//                // Code for when the discovery initiation fails goes here.
//                // Alert the user that something went wrong.
//            }
//        });
//    }

}
