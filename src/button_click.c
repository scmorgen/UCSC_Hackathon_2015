#include <pebble.h>
#include <HelperFcns.h>

//text display variables
static Window *window;
TextLayer *phase_layer, *score_layer, *role_layer, *gesture_layer;
char phase_buffer[64], score_buffer[32], role_buffer[32], gesture_buffer[32];


//accelerometer variables
#define WIDTH 144
#define ACCEL_STEP_MS 50
#define NUM_ACCEL_SAMPLES 1
int height_graph= 90;
int offset= 30;
static int accel_x[WIDTH];
static int accel_y[WIDTH];
static int accel_z[WIDTH];
static Layer *bargraph_layer;
static AppTimer *timer;
static int count;
static int index=0;
static int curXAccel, curYAccel, curZAccel;

/********Gesture holders****/
static bool looking_for_jazzhands=0;
static int jazz_hands[2];
static int raise_the_roofs[2];
static int threshold_z_jh= 600;
static int threshold_x_RR= 400;
static int threshold_absz_5= 600;

/*******************Button Clicks**************/
//Private Functions to handle Button Clicks
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(gesture_layer, "Last Gesture: SELECT");
  sendGesture(GESTURE_2);
}
 
static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(gesture_layer, "Last Gesture: UP");
  sendGesture(GESTURE_1);
}
 
static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(gesture_layer, "Last Gesture: DOWN");
  sendGesture(GESTURE_3);
  
}

//
static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

/*************Bargraph Functions *****************/
void draw_bar_graph(GContext* ctx, GPoint origin, GSize size, int* list) {
  int i;
  //draw background rect (clear out old stuff)
  GRect backgndRect = { .origin = origin, .size = size };
  graphics_context_set_fill_color(ctx,	GColorWhite);	
  graphics_fill_rect(	ctx, backgndRect, 0, GCornerNone );
  //draw bar graph
  for(i=0 ; i < (int)WIDTH ; i++) {
    int xorg=origin.x+i;
    int xdest=xorg; //vertial lines
    int yorg=origin.y + size.h/2;
    int ydest=yorg + list[i]/100;
    //make sure it fits:
    if(ydest < origin.y) ydest = origin.y;
    if(ydest > origin.y + size.h) ydest = origin.y + size.h;
    
    graphics_draw_line(	ctx,
      (GPoint) {xorg, yorg},
      (GPoint) {xdest, ydest} 
    );
  }
}

static void bargraph_layer_update_callback(Layer *me, GContext *context) {
  
  draw_bar_graph(context, (GPoint){0, 0*height_graph/4+offset},(GSize){WIDTH,height_graph/3-1}, accel_x);
  draw_bar_graph(context, (GPoint){0, 1*height_graph/4+offset},(GSize){WIDTH,height_graph/3-1}, accel_y);
  draw_bar_graph(context, (GPoint){0, 2*height_graph/4+offset},(GSize){WIDTH,height_graph/3-1}, accel_z);
}

static void timer_callback(void *data) {
  layer_mark_dirty(bargraph_layer);
  timer = app_timer_register(ACCEL_STEP_MS, timer_callback, NULL);
}

/*******************Accelerometer *******************/
//resets variables needed for jazz hands
void resetJazzHands() {
  jazz_hands[0]= jazz_hands[1]=0;
  looking_for_jazzhands=0;
}

//handles variables checking for jazzhands and sends message if it recognizes them
void checkJazzHands(int zChange) {
    if (!looking_for_jazzhands) looking_for_jazzhands=1;
    
    if (zChange>0) jazz_hands[0]++; //increment positive change count
    else jazz_hands[1]++; //increment negative change count
    
    if (jazz_hands[0]>=3 && jazz_hands[1]>=3) {
      text_layer_set_text(gesture_layer, "Last Gesture: JazzHands!");
      sendGesture(GESTURE_1);
      resetJazzHands();
    }
}
//ACCELEROMETRY:
static void accel_handler(AccelData *data, uint32_t num_samples){
  AccelData accel = (AccelData) { .x = 0, .y = 0, .z = 0 };
  accel_service_peek(&accel);
  
  
  
  //and then drop in the new value
  curXAccel=data[0].x;
  curYAccel=data[0].y;
  curZAccel=data[0].z;
  
  
  //first shift the lists to the right (Ew gross!)
  
    accel_x[index]= curXAccel-accel_x[index];
    accel_y[index]=curYAccel-accel_y[index];
    accel_z[index]=curZAccel-accel_z[index];
  
  index++;
  if (index==WIDTH) index= 0;
  
  accel_x[index]= curXAccel;
  accel_y[index]=curYAccel;
  accel_z[index]=curZAccel;
  
  if (index==WIDTH) index= 0;

  //Gestures
  //Jazz hands consists of multiple high peaks of alternating negative and positive
  //on Z Axis
//   static int jazz_hands[2];
// static int raise_the_roofs[2];
// static threshold_z_jh= 600;
// static threshold_x_RR= 400;
// static threshold_absz_5= 600;
  //looking for jazzhands
  int zChange= accel_z[index-1];
  int xChange=accel_x[index-1];
  
  if (abs(zChange)>=threshold_z_jh) {
    count=20;
    checkJazzHands(zChange);
    }

  
  //High Five is one high on Z axis
  //Raise the roof is positiveand negative on x axis
  
  if (count<=0){
    text_layer_set_text(gesture_layer, "Last Gesture: None");
    resetJazzHands();
  }
  count--;
  
  
}

/******************Message Incoming/Outgoing ***************/
void process_tuple(Tuple *t)
{
  //Get key
  int key = t->key;
 
  //Get integer value, if present
  int value = t->value->int32;
 
  //Get string value, if present
  char string_value[32];
  strcpy(string_value, t->value->cstring);
 
  //Decide what to do
  switch(key) {
    case KEY_SEND_PHASE:
      //Location received
      if (value==WAITING_ROOM_SCREEN) {
        strcpy(string_value, "Waiting Room");
      } else if (value == GAME_PLAY_SCREEN) {
        strcpy(string_value,"Play Screen");
      } else if (value== FINAL_SCREEN) {
        strcpy(string_value,"Its the end!");
      }
      snprintf(phase_buffer, sizeof("a long phasename"), "%s", string_value);
      text_layer_set_text(phase_layer, (char*) &phase_buffer);
      break;
    case KEY_SEND_ROLE:
      //Role received
      snprintf(role_buffer, sizeof("a really long role name"), "%s", string_value);
      text_layer_set_text(role_layer, (char*) &role_buffer);
      break;
    case KEY_SCORE_UPDATE:
      snprintf(score_buffer, sizeof("Score: XXX"), "Score: %d", value);
      text_layer_set_text(score_layer, (char*) &score_buffer);
      break;
    break;
      
  }
}


static void inbox_dropped_callback(AppMessageResult reason, void *context) {
  APP_LOG(APP_LOG_LEVEL_ERROR, "Message dropped!");
}

static void outbox_failed_callback(DictionaryIterator *iterator, AppMessageResult reason, void *context) {
  APP_LOG(APP_LOG_LEVEL_ERROR, "Outbox send failed!");
  sendGesture(-1);
}

static void outbox_sent_callback(DictionaryIterator *iterator, void *context) {
  APP_LOG(APP_LOG_LEVEL_INFO, "Outbox send success!");
}
// Called when a message is received from PebbleKitJS
static void in_received_handler(DictionaryIterator *received, void *context) {
	(void) context;
	Tuple * t = dict_read_first(received);
	while(t !=NULL) {
    
    process_tuple(t); 
    t = dict_read_next(received);
	}
	
}


/*****************Text and Windows ********************/
//hangles setting up the text layer
static TextLayer* init_text_layer(GRect location, GTextAlignment alignment, char * aFont)
{
  TextLayer *layer = text_layer_create(location);
  text_layer_set_text_color(layer, GColorBlack);
  text_layer_set_background_color(layer, GColorClear);
  text_layer_set_font(layer, fonts_get_system_font(aFont));
  text_layer_set_text_alignment(layer, alignment);
  return layer;
}

//loads the screen
void window_load(Window *window) {
  //adding window's elements here
  
  //odd notation for bar graph
  Layer *window_layer = window_get_root_layer(window);
  GRect frame = layer_get_frame(window_layer);
  bargraph_layer = layer_create(frame);
  layer_set_update_proc(bargraph_layer, bargraph_layer_update_callback);
  layer_add_child(window_layer, bargraph_layer);
  
  phase_layer = init_text_layer(GRect(5, 0, 144, 30), GTextAlignmentLeft, FONT_KEY_GOTHIC_14);
  text_layer_set_text(phase_layer, "INIT");
  layer_add_child(window_get_root_layer(window), text_layer_get_layer(phase_layer));
  
  score_layer = init_text_layer(GRect(0, 0, 138, 30), GTextAlignmentRight, FONT_KEY_GOTHIC_18_BOLD);
  text_layer_set_text(score_layer, "");
  layer_add_child(window_get_root_layer(window), text_layer_get_layer(score_layer));
 
  role_layer = init_text_layer(GRect(5, 60, 144, 30), GTextAlignmentCenter, FONT_KEY_GOTHIC_24_BOLD);
  text_layer_set_text(role_layer, "");
  layer_add_child(window_get_root_layer(window), text_layer_get_layer(role_layer));
   
  gesture_layer = init_text_layer(GRect(5, 110, 144, 30), GTextAlignmentCenter, FONT_KEY_GOTHIC_18);
  text_layer_set_text(gesture_layer, "Last Gesture: N/A");
  layer_add_child(window_get_root_layer(window), text_layer_get_layer(gesture_layer));
  
}

//destroys the screen at the end
void window_unload(Window *window) {
  //destroy all elements here
  text_layer_destroy(phase_layer);
  text_layer_destroy(score_layer);
  text_layer_destroy(role_layer);
  text_layer_destroy(gesture_layer);
}

//*******************Main Body*********/
 
static void init(void) {
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);
  
  //Register AppMessage events
  
  app_message_register_inbox_received(in_received_handler);
  app_message_register_inbox_dropped(inbox_dropped_callback);
  app_message_register_outbox_failed(outbox_failed_callback);
  app_message_register_outbox_sent(outbox_sent_callback);
  app_message_open(512, 512);    //Large input and output buffer sizes
  
  //Timer
  timer = app_timer_register(ACCEL_STEP_MS, timer_callback, NULL);
  
  //subscribe to accel data service:
  //accel_data_service_subscribe(0, NULL);
  accel_data_service_subscribe(NUM_ACCEL_SAMPLES, accel_handler);
  accel_service_set_sampling_rate(ACCEL_SAMPLING_10HZ);
}
 
static void deinit(void) {
  accel_data_service_unsubscribe(); 
  app_message_deregister_callbacks(); 
  window_destroy(window);
}
 
int main(void) {
  init();
 
  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", window);
 
  app_event_loop();
  deinit();
}

