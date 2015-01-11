package com.spacehackteam.wifitests;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends ActionBarActivity {

    public final static String WIFI_APP = "WIFI_APP";
    public final static String GAME_NAME = "_gestureGame";
    public final static int SERVER_PORT = 2132;
    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;
    BroadcastReceiver receiver;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private Set<WifiP2pDevice> friends = new HashSet<WifiP2pDevice>();
    Button hostButton;
    Button joinButton;
    TextView hostNameTextView;
    boolean isHostFlag = false;
    boolean isTransmittingFlag = false;
    private TextView friendListTextView;
    private WifiP2pGroup mGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(MainActivity.WIFI_APP,"onCreate() called.\n");

        // - - - Set up intent filter so system alerts about wifi get parsed by app - - -
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        // Get the wifi manager and a channel object from its initialization
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);

        hostNameTextView = (TextView) findViewById(R.id.hostNameTextView);
        hostButton = (Button) findViewById(R.id.hostButton);
        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTransmittingFlag) return;
                hostNameTextView.setText("I AM HOST! ALL HAIL HOST!");
                isTransmittingFlag = true;
                isHostFlag = true;
                hostButton.setText("Attempting to host.");
                joinButton.setText("");

                lookForBroadcasts();

                // - - - - - Try to discover peers - - - - -
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.v(MainActivity.WIFI_APP, "HUZZAH! You have discovered peers!");
                        mManager.requestPeers(mChannel,mPeerListListener);
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(MainActivity.WIFI_APP, "Error! Could not discover peers!");
                    }
                });
            }
        });

        joinButton = (Button) findViewById(R.id.joinGameButton);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTransmittingFlag) return;
                isTransmittingFlag = true;
                joinButton.setText("Looking For Host");
                hostButton.setText("");

                lookForBroadcasts();

                // - - - - - Try to discover peers - - - - -
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.v(MainActivity.WIFI_APP, "HUZZAH! You have discovered peers!");
                        mManager.requestPeers(mChannel,mPeerListListener);
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e(MainActivity.WIFI_APP, "Error! Could not discover peers!");
                    }
                });
            }
        });

        friendListTextView = (TextView) findViewById(R.id.friendListTextView);
    }

    private void lookForBroadcasts(){
        receiver = new WifiBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(MainActivity.WIFI_APP,"onResume() called.\n");
        if (isTransmittingFlag) lookForBroadcasts();
    }

    @Override
    public void onPause(){
        if (null != receiver) unregisterReceiver(receiver);

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





    /*private void startRegistration() {
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

    private void discoverService() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                Log.v(MainActivity.WIFI_APP, "DnsSdTxtRecord available from" + fullDomain);
                Log.v(MainActivity.WIFI_APP,"From Device: " + device.toString());
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {
                Log.v(MainActivity.WIFI_APP,"DNsSdServiceResponse available from" + instanceName);

            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);


        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.v(MainActivity.WIFI_APP,"Bonjour service request successfully placed.");
                        // Success!
                    }

                    @Override
                    public void onFailure(int code) {
                        Log.v(MainActivity.WIFI_APP,"Bonjour service request unsuccessful!");
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.v(MainActivity.WIFI_APP, "discoverServices.onSuccess() success!");
            }

            @Override
            public void onFailure(int code) {
                Log.v(MainActivity.WIFI_APP, "discoverServices.onSuccess() failure! :-(");
            }
        });
    }*/






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

                if (mGroup != null) {
                    Log.v(MainActivity.WIFI_APP,"Members of our group: ");
                    for(WifiP2pDevice device : mGroup.getClientList()){
                        Log.v(MainActivity.WIFI_APP,
                                device.deviceAddress + " - " + device.deviceName);
                    }
                    Log.v(MainActivity.WIFI_APP,"Group Host is : " + mGroup.getOwner().deviceName);
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
            Log.v(MainActivity.WIFI_APP,"PeerListListener.onPeersAvailable() called.\n");

            // Out with the old, in with the new. Look at the new peers and try to print a list.
            peers.clear();
            peers.addAll(peerList.getDeviceList());
            Log.v(MainActivity.WIFI_APP,"Listing peers found: \n");
            for (WifiP2pDevice device : peers){
                Log.v(MainActivity.WIFI_APP,device.toString() + "\n");
                Log.v(MainActivity.WIFI_APP,"- - - - - - - - - - - - -");
            }

            // Now, just to test connections, try to connect to first one.
            if (isHostFlag){
                for (int i = 0; i < peerList.getDeviceList().size(); i++) {
                    WifiP2pDevice device = peers.get(i);
                    connectToFriend(device);
                }

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
        Log.v(MainActivity.WIFI_APP," - - - Entering connectToFriend() - - -");


        if (friends.contains(device)){
            Log.v(MainActivity.WIFI_APP,"This device is already your friend!");
            return;
        }

        Log.v(MainActivity.WIFI_APP, "Trying to connect to device: "
                + device.deviceAddress + " - " + device.deviceName);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;


        mManager.connect(mChannel,config,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(MainActivity.WIFI_APP, "Connection attempt onSuccess()! Adding to friends.\n");

                mManager.requestGroupInfo(mChannel,mGroupInfoListener);


                mManager.requestConnectionInfo(mChannel,new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {

                        if (!info.groupFormed){
                            Log.v(MainActivity.WIFI_APP,"Group malformed! Trying again.");
                            mManager.requestPeers(mChannel,mPeerListListener);
                            return;

                        }
                        Log.v(MainActivity.WIFI_APP,"Is Group Owner: " + info.isGroupOwner);

                                hostNameTextView.setText(info.groupOwnerAddress.toString());


                        // InetAddress from WifiP2pInfo struct.
                        InetAddress groupOwnerAddress = info.groupOwnerAddress;

                        // After the group negotiation, we can determine the group owner.
                        if (info.groupFormed && info.isGroupOwner) {
                            // Do whatever tasks are specific to the group owner.
                            // One common case is creating a server thread and accepting
                            // incoming connections.
                        } else if (info.groupFormed) {
                            // The other device acts as the client. In this case,
                            // you'll want to create a client thread that connects to the group
                            // owner.
                        }
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.v(MainActivity.WIFI_APP,"Connection attempt onFailure()!\n");
                Log.v(MainActivity.WIFI_APP,"Reason: " + reason);
            }
        });
    }



    WifiP2pManager.GroupInfoListener mGroupInfoListener = new WifiP2pManager.GroupInfoListener(){
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group){
            Log.v(MainActivity.WIFI_APP,"GroupInfoListener called. Saving our group to member.");
            mGroup = group;
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
