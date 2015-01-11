#pragma once
#include "pebble.h"
enum {
    KEY_GESTURE = 0,
    GESTURE_1 = 1,
    GESTURE_2 = 2,
    GESTURE_3 = 3,
    KEY_SEND_ROLE= 4,
    KEY_SEND_PHASE=5,
    KEY_SCORE_UPDATE= 6,
    WAITING_ROOM_SCREEN= 7,
    GAME_PLAY_SCREEN=8,
    FINAL_SCREEN=9  
};



//send a gesture out to the phone
void sendGesture(int gestureKey);

