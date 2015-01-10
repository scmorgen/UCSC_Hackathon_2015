#include <pebble.h>

Window *window;	
TextLayer *title_layer, *location_layer, *temperature_layer, *time_layer;
char location_buffer[64], temperature_buffer[32], time_buffer[32], title_buffer[32];

// Key values for AppMessage Dictionary
enum {
	KEY_LOCATION = 0,
  KEY_TEMPERATURE = 1,
};

void up_click_handler(ClickRecognizerRef recognizer, void *context)
{
   text_layer_set_text(location_layer, "You pressed UP!");
}
 
void down_click_handler(ClickRecognizerRef recognizer, void *context)
{
   text_layer_set_text(location_layer, "You pressed DOWN!");
}
 
void select_click_handler(ClickRecognizerRef recognizer, void *context)
{
   text_layer_set_text(location_layer, "You pressed SELECT!");
}
//handles all button clicks
void click_config_provider(void *context)
{
    window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
    window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
    window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
}

static TextLayer* init_text_layer(GRect location)
{
  TextLayer *layer = text_layer_create(location);
  text_layer_set_text_color(layer, GColorBlack);
  text_layer_set_background_color(layer, GColorClear);
  text_layer_set_font(layer, fonts_get_system_font("RESOURCE_ID_GOTHIC_18"));
  text_layer_set_text_alignment(layer, GTextAlignmentCenter);
 
  return layer;
}
void window_load(Window *window) {
  //adding window's elements here
  title_layer = init_text_layer(GRect(5, 0, 144, 30));
  text_layer_set_text(title_layer, "Openweathermap.org");
  layer_add_child(window_get_root_layer(window), text_layer_get_layer(title_layer));
  
  location_layer = init_text_layer(GRect(5, 30, 144, 30));
  text_layer_set_text(location_layer, "Location: N/A");
  layer_add_child(window_get_root_layer(window), text_layer_get_layer(location_layer));
 
  temperature_layer = init_text_layer(GRect(5, 60, 144, 30));
  text_layer_set_text(temperature_layer, "Temperature: N/A");
  layer_add_child(window_get_root_layer(window), text_layer_get_layer(temperature_layer));
   
  time_layer = init_text_layer(GRect(5, 90, 144, 30));
  text_layer_set_text(time_layer, "Last updated: N/A");
  layer_add_child(window_get_root_layer(window), text_layer_get_layer(time_layer));
  
}

void window_unload(Window *window) {
  //destroy all elements here
  text_layer_destroy(title_layer);
  text_layer_destroy(location_layer);
  text_layer_destroy(temperature_layer);
  text_layer_destroy(time_layer);
}

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
    case KEY_LOCATION:
      //Location received
      snprintf(location_buffer, sizeof("Location: couldbereallylongname"), "Location: %s", string_value);
      text_layer_set_text(location_layer, (char*) &location_buffer);
      break;
    case KEY_TEMPERATURE:
      //Temperature received
      text_layer_set_text(title_layer, "Yes we got the temperature");
      snprintf(temperature_buffer, sizeof("Temperature: XX \u00B0C"), "Temperature: %d \u00B0C", value);
      text_layer_set_text(temperature_layer, (char*) &temperature_buffer);
      break;
  }
 
  //Set time this update came in
  time_t temp = time(NULL);
  struct tm *tm = localtime(&temp);
  strftime(time_buffer, sizeof("Last updated: XX:XX"), "Last updated: %H:%M", tm);
  text_layer_set_text(time_layer, (char*) &time_buffer);
}
// Called when a message is received from PebbleKitJS
static void in_received_handler(DictionaryIterator *received, void *context) {
	(void) context;
  int count= 0;
	Tuple * t = dict_read_first(received);
	while(t !=NULL) {
    snprintf(title_buffer, sizeof("Count is: XX \u00B0C"), "Count is: %d \u00B0C", count);
    text_layer_set_text(title_layer, title_buffer);
    
    process_tuple(t); 
    //Get next
    t = dict_read_next(received);
    count++;
	}
	
}

// Called when an incoming message from PebbleKitJS is dropped
static void in_dropped_handler(AppMessageResult reason, void *context) {	
}

// Called when PebbleKitJS does not acknowledge receipt of a message
static void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
}

void init(void) {
  //Initialize the app elements here!
	window = window_create();
  window_set_window_handlers(window, (WindowHandlers) {
    .load= window_load,
    .unload= window_unload,
  });
  window_set_click_config_provider(window, click_config_provider);
	window_stack_push(window, true);
	
	// Register AppMessage handlers
	app_message_register_inbox_received(in_received_handler); 
		
	app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());
	
}

void deinit(void) {
	app_message_deregister_callbacks();
  
	window_destroy(window);
}

int main( void ) {
	init();
	app_event_loop();
	deinit();
}