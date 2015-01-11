#include <pebble.h>
#include "HelperFcns.h"

//Private functions
void send_int(uint8_t key, uint8_t cmd)
{
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
      
    Tuplet value = TupletInteger(key, cmd);
    dict_write_tuplet(iter, &value);
      
    app_message_outbox_send();
}

//private variables
static int lastGesture=GESTURE_1;



//handles sending a gesture
void sendGesture(int gestureKey) {
  if (gestureKey==-1) //for case where gesture is not sent
    send_int(KEY_GESTURE, lastGesture);
  else {
    lastGesture= gestureKey;
    send_int(KEY_GESTURE, gestureKey);
  }
  
}

