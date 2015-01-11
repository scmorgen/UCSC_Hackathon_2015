package com.example.arcainne.BamBam;

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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class HostActivity extends ActionBarActivity {

    public final static String WIFI_APP = "WIFI_APP";
    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;
    BroadcastReceiver receiver;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private List<WifiP2pDevice> friends = new ArrayList<WifiP2pDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        Log.v(WIFI_APP, "onCreate() called.\n");

        // - - - Set up intent filter so system alerts about wifi get parsed by app - - -
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // Get the wifi manager and a channel object from its initialization
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_host, menu);
        return true;
    }
    */

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

    @Override
    public void onResume(){
        super.onResume();
        Log.v(WIFI_APP,"onResume() called.\n");
        receiver = new WifiBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(receiver, intentFilter);



        // - - - - - Try to discover peers - - - - -
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.v(WIFI_APP, "HUZZAH! You have discovered peers!");

                mManager.requestPeers(mChannel,mPeerListListener);
            }

            @Override
            public void onFailure(int reason) {
                Log.e(WIFI_APP, "Error! Could not discover peers!");
            }
        });


    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
        Log.v(WIFI_APP,"onPause() called.\n");
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        Activity mActivity;
        WifiP2pManager mManager;
        WifiP2pManager.Channel mChannel;

        public WifiBroadcastReceiver(WifiP2pManager mManager,
                                     WifiP2pManager.Channel mChannel,
                                     Activity mActivity){
            this.mManager = mManager;
            this.mActivity = mActivity;
            this.mChannel = mChannel;
        }

        @Override
        public void onReceive(Context context, Intent intent){
            Log.v(WIFI_APP,"BroadcastReceiver.onReceive() called.\n");

            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                Log.v(WIFI_APP,"State: WIFI_P2P_STATE_CHANGED_ACTION.\n");
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                /*int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    activity.setIsWifiP2pEnabled(true);
                } else {
                    activity.setIsWifiP2pEnabled(false);
                }*/
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.v(WIFI_APP,"State: WIFI_P2P_PEERS_CHANGED_ACTION.\n");

                if (mManager != null) {
                    mManager.requestPeers(mChannel, mPeerListListener);
                }

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.v(WIFI_APP,"State: WIFI_P2P_CONNECTION_CHANGED_ACTION.\n");
                // Connection state changed!  We should probably do something about
                // that.

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Log.v(WIFI_APP,"State: WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.\n");
                /*DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                */
            }
        }
    }


    private WifiP2pManager.PeerListListener mPeerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Log.v(WIFI_APP,"PeerListListener.onPeersAvailable() called.\n");

            // Out with the old, in with the new. Look at the new peers and try to print a list.
            peers.clear();
            peers.addAll(peerList.getDeviceList());
            Log.v(WIFI_APP,"Listing peers found: \n");
            for (WifiP2pDevice device : peers){
                Log.v(WIFI_APP,device.toString() + "\n");
            }

            // Now, just to test connections, try to connect to first one.
            if (peers.size()>0){
                Log.v(WIFI_APP,"Trying to connect to first device.\n");

                WifiP2pDevice device = peers.get(0);

                connectToFriend(device);

            }

            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of available
            // peers, trigger an update.
            //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            //if (peers.size() == 0) {
            //    Log.d(WiFiDirectActivity.TAG, "No devices found");
            //    return;
            //}
        }
    };

    public void connectToFriend(WifiP2pDevice device){
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel,config,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(WIFI_APP,"Connection attempt onSuccess()!\n");
                //friends.add();
            }

            @Override
            public void onFailure(int reason) {
                Log.v(WIFI_APP,"Connection attempt onFailure()!\n");
                Log.v(WIFI_APP,"Reason: " + reason);
            }
        });
    }
}
