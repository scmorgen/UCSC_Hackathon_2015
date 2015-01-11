package com.spacehackteam.gameserver2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends ActionBarActivity {

    public final static String WIFI_APP = "WIFI_APP";
    public final static String GAME_NAME = "_gestureGame";
    public final static int SERVER_PORT = 8888;
    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;
    BroadcastReceiver receiver;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private Set<WifiP2pDevice> friends = new HashSet<WifiP2pDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(MainActivity.WIFI_APP, "onCreate() called.\n");
        // - - - Set up intent filter so system alerts about wifi get parsed by app - - -
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        // Get the wifi manager and a channel object from its initialization
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);

        startRegistration();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(MainActivity.WIFI_APP,"onResume() called.\n");
        receiver = new WifiBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(receiver, intentFilter);



        // - - - - - Try to discover peers - - - - -
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.v(MainActivity.WIFI_APP, "Peer discovery successful. Requesting Peers now.");

                mManager.requestPeers(mChannel,mPeerListListener);
            }

            @Override
            public void onFailure(int reason) {
                Log.e(MainActivity.WIFI_APP, "Error! Could not discover peers!");
            }
        });


    }

    @Override
    public void onPause(){
        unregisterReceiver(receiver);
        Log.v(MainActivity.WIFI_APP,"onPause() called.\n");
        super.onPause();
    }

    @Override
    public void onDestroy(){
        Log.v(MainActivity.WIFI_APP,"onDestroy() called.");

        // Release the WifiP2P group
        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(MainActivity.WIFI_APP, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(MainActivity.WIFI_APP, "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }

        super.onDestroy();
    }


    // - - - - - - - Registration as a service provider - - - - - - - -

    private void startRegistration() {
        //  Create a string map containing information about your service.
        Map record = new HashMap();
        record.put("listenport", String.valueOf(MainActivity.SERVER_PORT));


        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance(MainActivity.GAME_NAME, "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(MainActivity.WIFI_APP,"Successfully posted advertisement as Hosting service!\n");
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
            }

            @Override
            public void onFailure(int arg0) {
                Log.v(MainActivity.WIFI_APP,"Failed to post advertisement as Hosting service.\n");
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            }
        });
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
            Log.v(MainActivity.WIFI_APP,"BroadcastReceiver.onReceive() called.\n");

            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                Log.v(MainActivity.WIFI_APP,"State: WIFI_P2P_STATE_CHANGED_ACTION.\n");

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.v(MainActivity.WIFI_APP,"State: WIFI_P2P_PEERS_CHANGED_ACTION.\n");

                if (mManager != null) {
                    mManager.requestPeers(mChannel, mPeerListListener);
                }

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.v(MainActivity.WIFI_APP,"State: WIFI_P2P_CONNECTION_CHANGED_ACTION.\n");
                // Connection state changed!  We should probably do something about
                // that.

                if (mManager == null) {
                    return;
                }

                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    Log.v(MainActivity.WIFI_APP,"NetworkInfo object says we are connected!\n");
                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            Log.v(MainActivity.WIFI_APP,
                                    "requestConnectionInfo.onConnectionInfoAvailable() called.\n");
                        }
                    });
                }

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Log.v(MainActivity.WIFI_APP,"State: WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.\n");
            }
        }
    }




    private WifiP2pManager.PeerListListener mPeerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Log.v(MainActivity.WIFI_APP,"PeerListListener.onPeersAvailable() called.\n");

            // Out with the old, in with the new. Look at the new peers and try to print a list.
            peers.clear();
            peers.addAll(peerList.getDeviceList());
            Log.v(MainActivity.WIFI_APP,"Listing peers found: \n");
            for (WifiP2pDevice device : peers){
                Log.v(MainActivity.WIFI_APP,device.toString() + "\n");
            }

            // Now, just to test connections, try to connect to first one.
            //if (peers.size()>0){
            //   Log.v(MainActivity.WIFI_APP,"Trying to connect to first device.\n");
            // WifiP2pDevice device = peers.get(0);
            //connectToFriend(device);
            //}

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


























    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
