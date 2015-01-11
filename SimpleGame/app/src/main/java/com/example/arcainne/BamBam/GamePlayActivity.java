package com.example.arcainne.BamBam;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

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
            KEY_SEND_ROLE= 4,
            KEY_SEND_PHASE=5,
            KEY_SCORE_UPDATE= 6,
            WAITING_ROOM_SCREEN= 7,
            GAME_PLAY_SCREEN=8,
            FINAL_SCREEN=9;

    //This is the connection to our particular phone app (determined by UUID given in cloudpebble)
    private UUID Pebble_UUID = UUID.fromString("7c02f3fb-ff81-4893-aa1c-f741b2e7c3ff");
    private PebbleKit.PebbleDataReceiver mReceiver; //this is our data receiver

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        tvInstructions = (TextView) findViewById(R.id.instruction_id);
        tvInstructions.setText("No instruction yet!");

        //Place in OnCreate Code to start up Pebble
        PebbleKit.startAppOnPebble(getApplicationContext(), Pebble_UUID);
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
        mReceiver = new PebbleKit.PebbleDataReceiver(Pebble_UUID) {

            //Function for receiving data from Pebble
            @Override
            public void receiveData(Context context, int transactionId, PebbleDictionary data) {
                //ACK the message
                PebbleKit.sendAckToPebble(context, transactionId);

                //Check the key exists
                if(data.getUnsignedIntegerAsLong( KEY_GESTURE) != null) {
                    int button = data.getUnsignedIntegerAsLong(KEY_GESTURE).intValue();
                    switch(button) {
                        case GESTURE_1:
                            //Insert Instructions here upon receiving a gesture 1 (please empty)

                            tvInstructions.setText("Gesture 1 Done!, sending transition");
                            sendToWatchApp(KEY_SEND_PHASE, WAITING_ROOM_SCREEN);
                            sendToWatchApp(KEY_SEND_ROLE, "Elf");
                            break;
                        case GESTURE_2:
                            //Insert Instructions here upon receiving a gesture 2 (please empty)
                            tvInstructions.setText("Gesture 2 Done!, sending transition");
                            sendToWatchApp(KEY_SEND_PHASE, GAME_PLAY_SCREEN);
                            sendToWatchApp(KEY_SEND_ROLE, "Batman");
                            break;
                        case GESTURE_3:
                            //Insert Instructions for gesture 3 (please empty)
                            tvInstructions.setText("Gesture 3 Done!, sending transmission");
                            sendToWatchApp(KEY_SEND_PHASE, FINAL_SCREEN);
                            sendToWatchApp(KEY_SEND_ROLE, "Orc");
                            break;
                    }
                }
            }
        };

        PebbleKit.registerReceivedDataHandler(this, mReceiver);
        //End of pebble code for Game Play
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Don't receive data from Pebble
        unregisterReceiver( mReceiver);
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

    // Send Data to Watch
    // Ideally Just when phases changed (start, waitRoom, exit, etc)
    public void sendToWatchApp(int key, Object message) {
        //String time = String.format("%02d:%02d", rand.nextInt(60), rand.nextInt(60));
        //String distance = String.format("%02.02f", 32 * rand.nextDouble());
        //String addl_data = String.format("%02d:%02d", rand.nextInt(10), rand.nextInt(60));

        PebbleDictionary data = new PebbleDictionary();
        if (message instanceof String) {
            data.addString(key, (String) message);
        }
        else if (message instanceof Integer) {
            data.addInt32(key, (int) message);
        }
        else {
            tvInstructions.setText("Message is not an appropriate data-type; Message: "+message);
        }

        PebbleKit.sendDataToPebble(getApplicationContext(), Pebble_UUID, data);
    }
}
