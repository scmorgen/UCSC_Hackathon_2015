

// Function to send a message to the Pebble using AppMessage API

function HTTPGET(url) {
    var req = new XMLHttpRequest();
    req.open("GET", url, false);
    req.send(null);
    return req.responseText;
}

var getWeather = function() {
    //Get weather info
    var response = HTTPGET("http://api.openweathermap.org/data/2.5/weather?q=London,uk");
 
    //Convert to JSON
    var json = JSON.parse(response);
 
    //Extract the data
    var temperature = Math.round(json.main.temp - 273.15);
    var location = json.name;
 
    //Console output to check all is working.
    console.log("It is " + temperature + " degrees in " + location + " today!");
 
    //Construct a key-value dictionary
    var dict = { "KEY_TEMPERATURE": temperature, "KEY_LOCATION" : location};
 
    //Send data to watch for display
    Pebble.sendAppMessage(dict);
};

// Called when JS is ready
Pebble.addEventListener("ready",
							function(e) {
                getWeather();
							});
												
// Called when incoming message from the Pebble is received
//Pebble.addEventListener("appmessage",
							//function(e) {
							//	console.log("Received Status: " + e.payload.status);
							//	sendMessage();
							//});