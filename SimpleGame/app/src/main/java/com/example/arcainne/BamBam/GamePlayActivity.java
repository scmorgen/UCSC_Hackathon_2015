package com.example.arcainne.BamBam;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.Random;
import java.util.UUID;


public class GamePlayActivity extends ActionBarActivity {

    private TextView tvInstructions;

    /************ INITALIZATION of GLOBAL VARIABLES FOR WATCH INTERACTIONS *******/
    //This needs to be used for communication
    //This is a list of commands from watch to client
    private static final int
            KEY_GESTURE = 0,
            GESTURE_1 = 1,
            GESTURE_2 = 2,
            GESTURE_3 = 3;

    //This is a list of commands from client to watch
    private static final int
            KEY_SEND_ROLE= 4,       //Key, use a string as data
            KEY_SEND_PHASE=5,       //Key, use an enum below as data
            KEY_SCORE_UPDATE= 6,    //Key, use an int as data
            WAITING_ROOM_SCREEN= 7,
            GAME_PLAY_SCREEN=8,
            FINAL_SCREEN=9;

    //This is the connection to our particular phone app (determined by UUID given in cloudpebble)
    private UUID Pebble_UUID = UUID.fromString("7c02f3fb-ff81-4893-aa1c-f741b2e7c3ff");
    private PebbleKit.PebbleDataReceiver pebbleDataReceiverReceiver; //this is our data receiver
    private int score=0;
    private int buttonCount=0;
    private PebbleDictionary[] lastMessage= new PebbleDictionary[3];
    private static int transactionID=0;

    /**********END OF VARIABLES FOR PEBBLE INTERACTION ******/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        // Get TextView instructions object from activity_game_play.xml
        tvInstructions = (TextView) findViewById(R.id.instruction_id);
        tvInstructions.setText("No instruction yet!");

        //Place in OnCreate Code to start up Pebble
        stopWatchApp();
        startWatchApp();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_play, menu);
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
    protected void onResume() {
        super.onResume();
        startWatchApp();
        //Place in onResume or where-ever one expects to receive messages from pebble
        pebbleDataReceiverReceiver = new PebbleKit.PebbleDataReceiver(Pebble_UUID) {

            //Function for receiving data from Pebble
            @Override
            public void receiveData(Context context, int transactionId, PebbleDictionary data) {
                //ACK the message
                PebbleKit.sendAckToPebble(context, transactionId);

                //Check the key exists
                if(data.getUnsignedIntegerAsLong( KEY_GESTURE) != null) {
                    int button = data.getUnsignedIntegerAsLong(KEY_GESTURE).intValue();
                    buttonCount++;
                    switch(button) {
                        case GESTURE_1:
                            //Insert Instructions here upon receiving a gesture 1 (please empty)
                            tvInstructions.setText("Gesture 1 Done!, sending a round start: "+buttonCount );
                            String role= chooseRandomRole();
                            sendRoundStartToWatch(role);
                            break;
                        case GESTURE_2:
                            //Insert Instructions here upon receiving a gesture 2 (please empty)
                            tvInstructions.setText("Gesture 2 Done! sending a phase: "+ buttonCount);
                            if (score>=100) {
                                sendPhaseToWatch(FINAL_SCREEN, 100);
                            } else {
                                sendPhaseToWatch(WAITING_ROOM_SCREEN, score);
                            }
                            break;
                        case GESTURE_3:
                            //Insert Instructions for gesture 3 (please empty)
                            tvInstructions.setText("Gesture 3 Done!, increasing score"+ buttonCount);
                            score+=10;
                            sendScoreToWatch(score);
                            break;
                    }
                }
            }
        };

        PebbleKit.registerReceivedDataHandler(this, pebbleDataReceiverReceiver);
        PebbleKit.registerReceivedAckHandler(getApplicationContext(), new PebbleKit.PebbleAckReceiver(Pebble_UUID) {

            @Override
            public void receiveAck(Context context, int transactionId) {
                Log.i(getLocalClassName(), "Received ack for transaction " + transactionId);
            }

        });

        PebbleKit.registerReceivedNackHandler(getApplicationContext(), new PebbleKit.PebbleNackReceiver(Pebble_UUID) {
            //resends message if it isn't received.
            @Override
            public void receiveNack(Context context, int wrongedId) {
                Log.i(getLocalClassName(), "Received nack for transaction " + wrongedId);
                PebbleDictionary message= lastMessage[wrongedId];
                PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), Pebble_UUID, message, transactionID);
            }

        });

        //End of pebble code for Game Play

    }

    @Override
    protected void onPause() {
        super.onPause();

        //Don't receive data from Pebble
        unregisterReceiver(pebbleDataReceiverReceiver);
        stopWatchApp();
    }

    //Function to start up Watch (ideally when app started up)
    public void startWatchApp() {
        PebbleKit.startAppOnPebble(getApplicationContext(), Pebble_UUID);
    }

    //Function to stop Watch (ideally when app is stopped)
    // Send a broadcast to close the specified application on the connected Pebble
    public void stopWatchApp() {
        PebbleKit.closeAppOnPebble(getApplicationContext(), Pebble_UUID);
    }

    //Three functions to send info to watch:
    //Round start: send game play screen and role
    //Change Phase: sends phase change and score
    //sendScoreToWatch: just sends score
    public void sendRoundStartToWatch(String role) {
        PebbleDictionary data = new PebbleDictionary();
        data.addInt32(KEY_SEND_PHASE, GAME_PLAY_SCREEN);
        data.addString(KEY_SEND_ROLE, role);
        sendDataToWatch(data);

    }

    public void sendPhaseToWatch(int phase, int score) {
        PebbleDictionary data = new PebbleDictionary();
        data.addInt32(KEY_SEND_PHASE, phase);
        data.addInt32(KEY_SCORE_UPDATE, score);
        PebbleKit.sendDataToPebble(getApplicationContext(), Pebble_UUID, data);
    }

    public void sendScoreToWatch(int score) {
        PebbleDictionary data = new PebbleDictionary();
        data.addInt32(KEY_SCORE_UPDATE, score);
        PebbleKit.sendDataToPebble(getApplicationContext(), Pebble_UUID, data);

    }
    private void sendDataToWatch(PebbleDictionary data) {
        lastMessage[transactionID]=data;
        PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), Pebble_UUID, data, transactionID);
        transactionID++;
        if (transactionID>2) transactionID=0;
    }

    private String chooseRandomRole() {
        String roles[]= {
                "GnomesInRome",
                "BamBamNotBatman",
                "SeanAndSamson",
                "RobABaby",
                "Sojalicious",
                "OrcsALot",
                "Coxpiece",
                "Peterpanopolis",
                "Maximillion"
        };
        Random r= new Random();
        int index= r.nextInt(roles.length);
        return roles[index];
    }
}
