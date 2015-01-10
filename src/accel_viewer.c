#define DISKMAIN
  
/*

   The original source image is from:

      <http://openclipart.org/detail/26728/aiga-litter-disposal-by-anonymous>

   The source image was converted from an SVG into a RGB bitmap using
   Inkscape. It has no transparency and uses only black and white as
   colors.

*/

#ifdef OLD_VIEWER
  #include "pebble.h"

  #define HEIGHT 156
  #define WIDTH 144
  
static Window *window;

static Layer *layer;

static GBitmap *image;


// static void timer_callback(void *data) {
//   AccelData accel = (AccelData) { .x = 0, .y = 0, .z = 0 };

//   accel_service_peek(&accel);

//   for (int i = 0; i < NUM_DISCS; i++) {
//     Disc *disc = &discs[i];
//     disc_apply_accel(disc, accel);
//     disc_update(disc);
//   }

//   layer_mark_dirty(disc_layer);

//   timer = app_timer_register(ACCEL_STEP_MS, timer_callback, NULL);
// }


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
      (GPoint) {origin.x+i,origin.y},
      (GPoint) {origin.x+i,origin.y+ list[i]} 
    );
  }
}

static bool bargraph_on = 0;

static void layer_update_callback(Layer *me, GContext* ctx) {
  // We make sure the dimensions of the GRect to draw into
  // are equal to the size of the bitmap--otherwise the image
  // will automatically tile. Which might be what *you* want.

  //   GRect bounds = image->bounds; //unused
    graphics_draw_line	(	ctx,
      (GPoint) {0,0},
      (GPoint) {WIDTH,HEIGHT} 
    );
  if(bargraph_on){
    int list[11] = {0,1,2,3,4,5,6,7,8,9,10} ;
    draw_bar_graph(ctx, (GPoint){0, WIDTH/3}, list);
  }

  
//   graphics_draw_bitmap_in_rect(ctx, image, (GRect) { .origin = { 5, 5 }, .size = bounds.size });

//   graphics_draw_bitmap_in_rect(ctx, image, (GRect) { .origin = { 80, 60 }, .size = bounds.size });
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
//   int list[11] = {0,1,2,3,4,5,6,7,8,9,10} ;
//   draw_bar_graph(context, (GPoint){0, WIDTH/3}, list);
  bargraph_on = 1;
}

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

static void init(void) {
  window = window_create();
  window_stack_push(window, true /* Animated */);

  // Init the layer for display the image
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_frame(window_layer);
  layer = layer_create(bounds);
  layer_set_update_proc(layer, layer_update_callback);
  layer_add_child(window_layer, layer);

  window_set_click_config_provider(window, click_config_provider);
//   window_set_window_handlers(window, (WindowHandlers) {
//   	.load = window_load,
//     .unload = window_unload,
//   });
}

// int main(void) {
//   init();
//   app_event_loop();

//   window_destroy(window);
//   layer_destroy(layer);
// }

/////========================begin disks ============================================
// #include "pebble.h"

// #define MATH_PI 3.141592653589793238462
// #define NUM_DISCS 20
// #define DISC_DENSITY 0.25
// #define ACCEL_RATIO 0.05
// #define ACCEL_STEP_MS 50

// typedef struct Vec2d {
//   double x;
//   double y;
// } Vec2d;

// typedef struct Disc {
//   Vec2d pos;
//   Vec2d vel;
//   double mass;
//   double radius;
// } Disc;

// static Disc discs[NUM_DISCS];

// static double next_radius = 3;

// static Window *window;

// static GRect window_frame;

// static Layer *disc_layer;

// static AppTimer *timer;

// static double disc_calc_mass(Disc *disc) {
//   return MATH_PI * disc->radius * disc->radius * DISC_DENSITY;
// }

// static void disc_init(Disc *disc) {
//   GRect frame = window_frame;
//   disc->pos.x = frame.size.w/2;
//   disc->pos.y = frame.size.h/2;
//   disc->vel.x = 0;
//   disc->vel.y = 0;
//   disc->radius = next_radius;
//   disc->mass = disc_calc_mass(disc);
//   next_radius += 0.5;
// }

// static void disc_apply_force(Disc *disc, Vec2d force) {
//   disc->vel.x += force.x / disc->mass;
//   disc->vel.y += force.y / disc->mass;
// }

// static void disc_apply_accel(Disc *disc, AccelData accel) {
//   Vec2d force;
//   force.x = accel.x * ACCEL_RATIO;
//   force.y = -accel.y * ACCEL_RATIO;
//   disc_apply_force(disc, force);
// }

// static void disc_update(Disc *disc) {
//   const GRect frame = window_frame;
//   double e = 0.5;
//   if ((disc->pos.x - disc->radius < 0 && disc->vel.x < 0)
//     || (disc->pos.x + disc->radius > frame.size.w && disc->vel.x > 0)) {
//     disc->vel.x = -disc->vel.x * e;
//   }
//   if ((disc->pos.y - disc->radius < 0 && disc->vel.y < 0)
//     || (disc->pos.y + disc->radius > frame.size.h && disc->vel.y > 0)) {
//     disc->vel.y = -disc->vel.y * e;
//   }
//   disc->pos.x += disc->vel.x;
//   disc->pos.y += disc->vel.y;
// }

// static void disc_draw(GContext *ctx, Disc *disc) {
//   graphics_context_set_fill_color(ctx, GColorWhite);
//   graphics_fill_circle(ctx, GPoint(disc->pos.x, disc->pos.y), disc->radius);
// }

// static void disc_layer_update_callback(Layer *me, GContext *ctx) {
//   for (int i = 0; i < NUM_DISCS; i++) {
//     disc_draw(ctx, &discs[i]);
//   }
// }

// static void timer_callback(void *data) {
//   AccelData accel = (AccelData) { .x = 0, .y = 0, .z = 0 };

//   accel_service_peek(&accel);

//   for (int i = 0; i < NUM_DISCS; i++) {
//     Disc *disc = &discs[i];
//     disc_apply_accel(disc, accel);
//     disc_update(disc);
//   }

//   layer_mark_dirty(disc_layer);

//   timer = app_timer_register(ACCEL_STEP_MS, timer_callback, NULL);
// }

// static void window_load(Window *window) {
//   Layer *window_layer = window_get_root_layer(window);
//   GRect frame = window_frame = layer_get_frame(window_layer);

//   disc_layer = layer_create(frame);
//   layer_set_update_proc(disc_layer, disc_layer_update_callback);
//   layer_add_child(window_layer, disc_layer);

//   for (int i = 0; i < NUM_DISCS; i++) {
//     disc_init(&discs[i]);
//   }
// }

// static void window_unload(Window *window) {
//   layer_destroy(disc_layer);
// }

// static void init(void) {
//   window = window_create();
//   window_set_window_handlers(window, (WindowHandlers) {
//     .load = window_load,
//     .unload = window_unload
//   });
//   window_stack_push(window, true /* Animated */);
//   window_set_background_color(window, GColorBlack);

//   accel_data_service_subscribe(0, NULL);

//   timer = app_timer_register(ACCEL_STEP_MS, timer_callback, NULL);
// }

// static void deinit(void) {
//   accel_data_service_unsubscribe();

//   window_destroy(window);


// int main(void) {
//   init();
//   app_event_loop();
//   deinit();
// }


// ---------------------------------------begin button-------------------------------------//
// #include <pebble.h>

// static Window *window;
// static TextLayer *text_layer;

// static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
//   text_layer_set_text(text_layer, "Select");
// }

// static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
//   text_layer_set_text(text_layer, "Up");
// }

// static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
//   text_layer_set_text(text_layer, "Down");
// }

// static void click_config_provider(void *context) {
//   window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
//   window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
//   window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
// }

// static void window_load(Window *window) {
//   Layer *window_layer = window_get_root_layer(window);
//   GRect bounds = layer_get_bounds(window_layer);

//   text_layer = text_layer_create((GRect) { .origin = { 0, 72 }, .size = { bounds.size.w, 20 } });
//   text_layer_set_text(text_layer, "Press a button");
//   text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
//   layer_add_child(window_layer, text_layer_get_layer(text_layer));
// }

// static void window_unload(Window *window) {
//   text_layer_destroy(text_layer);
// }

// static void init(void) {
//   window = window_create();
//   window_set_click_config_provider(window, click_config_provider);
//   window_set_window_handlers(window, (WindowHandlers) {
// 	.load = window_load,
//     .unload = window_unload,
//   });
//   const bool animated = true;
//   window_stack_push(window, animated);
// }

// static void deinit(void) {
//   window_destroy(window);
// }


// int main(void) {
//   init();
//   app_event_loop();
//   deinit();
// }

#endif