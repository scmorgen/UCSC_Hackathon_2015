#define DISKMAIN
  
#ifdef DISKMAIN
#include "pebble.h"

#define ACCEL_STEP_MS 50
#define NUM_ACCEL_SAMPLES 1

#define HEIGHT 156
#define WIDTH 144

  
static Window *window;

static GRect window_frame;

static Layer *bargraph_layer;

static AppTimer *timer;

static int accel_x[WIDTH];
static int accel_y[WIDTH];
static int accel_z[WIDTH];

///--------------------FUNCTIONS--------------------------------

void draw_bar_graph(GContext* ctx, GPoint origin, int* list) {
  int i;
  //draw background rect (clear out old stuff)
  GSize graphSize = {WIDTH, HEIGHT/3};
  GRect backgndRect = { .origin = origin, .size = graphSize };
  graphics_context_set_fill_color(ctx,	GColorWhite);	
  graphics_fill_rect(	ctx, backgndRect, 0, GCornerNone );
  //draw bar graph
  for(i=0 ; i < (int)WIDTH ; i++) {
    graphics_draw_line(	ctx,
      (GPoint) {origin.x+i, origin.y + HEIGHT/3/2 },
      (GPoint) {origin.x+i, origin.y + list[i]} 
    );
  }
}

static void bargraph_layer_update_callback(Layer *me, GContext *context) {
  draw_bar_graph(context, (GPoint){0, 0*HEIGHT/3}, accel_x);
  draw_bar_graph(context, (GPoint){0, 1*HEIGHT/3}, accel_y);
  draw_bar_graph(context, (GPoint){0, 2*HEIGHT/3}, accel_z);
}

static void accel_handler(AccelData *data, uint32_t num_samples){
  AccelData accel = (AccelData) { .x = 0, .y = 0, .z = 0 };
  accel_service_peek(&accel);
  int i;
  //first shift the lists to the right (Ew gross!)
  for(i=(WIDTH-1); i>0; i--){
    accel_x[i]=accel_x[i-1];
    accel_y[i]=accel_y[i-1];
    accel_z[i]=accel_z[i-1];
  }
  //and then drop in the new value
  accel_x[0]=data[0].x
  accel_y[0]=data[0].y;
  accel_z[0]=data[0].z;
}

static void timer_callback(void *data) {
  
  layer_mark_dirty(bargraph_layer);

  timer = app_timer_register(ACCEL_STEP_MS, timer_callback, NULL);
}

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect frame = window_frame = layer_get_frame(window_layer);

  bargraph_layer = layer_create(frame);
  layer_set_update_proc(bargraph_layer, bargraph_layer_update_callback);
  layer_add_child(window_layer, bargraph_layer);
}

static void window_unload(Window *window) {
  layer_destroy(bargraph_layer);
}

static void init(void) {
  window = window_create();
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload
  });
  window_stack_push(window, true /* Animated */);
  window_set_background_color(window, GColorBlack);

  accel_data_service_subscribe(0, NULL);

  timer = app_timer_register(ACCEL_STEP_MS, timer_callback, NULL);
  
  //subscribe to accel data service:
  accel_data_service_subscribe(NUM_ACCEL_SAMPLES, accel_handler);
  accel_service_set_sampling_rate(ACCEL_SAMPLING_10HZ);
}

static void deinit(void) {
  accel_data_service_unsubscribe();

  window_destroy(window);
}


int main(void) {
  init();
  app_event_loop();
  deinit();
}
#endif