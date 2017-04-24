package com.capstone.while1.beaconandroidstudio.beacondata;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Callable;

public class BeaconData {
    /*
        Constants
     */
    private static final int DEFAULT_MILES_FOR_EVENTS = 10;
    private static final String CREDENTIALS_FILE_NAME = "credentials.txt";
    private static String restAPIDomain = "http://e04d10cf.ngrok.io";
    private static String loginToken = null;
    private static ArrayList<Event> eventData = null;
    private static String lastUpdatedTime = null;
    private static String username = null;
    private static String password = null;
    private static Integer currentUserId = null;
    private static RequestQueue queue = null;
    private static BeaconConsumer<ArrayList<Event>> updatedEventHandler = null;
    private static Runnable onInitialized = null;
    private static ArrayList<Vote> eventsVotedFor = null;

    // Make the constructor private to force static use of the class.
    private BeaconData() {
    }

    // Done - Init
    public static Boolean userHasLocallySavedLoginInformation(Context context) {
        File loginFile = new File(context.getFilesDir(), CREDENTIALS_FILE_NAME);
        return loginFile.exists(); // If it exists, then there is information stored locally for this user...
    }

    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    // Done - Tested
    public static Boolean registerLogin(Context context, String username, String password) {
        // Create a convenient string format for future access
        JSONObject userJSON = new JSONObject();
        try {
            userJSON.put("username", username);
            userJSON.put("password", password);
        } catch (JSONException ex) {
            System.err.println(ex);
            return false;
        }

        // Now write it to a new file, overwriting it if it already exists...
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(CREDENTIALS_FILE_NAME, Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(fileOutputStream);
            writer.write(userJSON.toString());
            writer.close();
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }

        // Success, so return true
        return true;
    }

    public static void isValidLogin(String username, String password, final Runnable success, final Runnable failure) {
        String queryStr = generateQueryString("username", username, "password", password);
        String uri = restAPIDomain + "/api/Authenticator/IsValidLogin" + queryStr;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        try {
                            if (jobj.getBoolean("wasSuccessful") == true) {
                                success.run();
                            } else {
                                failure.run();
                            }
                        } catch (JSONException ex) {
                            System.err.println(ex);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError x) {
                        System.err.println(x);
                    }
                });

        queue.add(request);
    }

    // Done - Init
    public static Boolean deleteLoginInformation(Context context) {
        if (userHasLocallySavedLoginInformation(context)) {
            File loginFile = new File(context.getFilesDir(), CREDENTIALS_FILE_NAME);
            loginFile.delete();
            return true;
        } else {
            // There is no user credentials to delete
            return false;
        }
    }

    // Done - Final
    public static Boolean isInitialized() {
        return eventData != null && eventsVotedFor != null;
    }

    // Done - Tested
    private static String getInternallyStoredFileContents(Context context, String internalFilename) {
        StringBuilder fileContents = new StringBuilder();

        try {
            File cFile = new File(context.getFilesDir(), internalFilename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput(CREDENTIALS_FILE_NAME)));
            String line;

            while ((line = reader.readLine()) != null) {
                fileContents.append(line);
            }

            reader.close();
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }

        return fileContents.toString();
    }

    // Done - Tested
    public static Boolean tryToLoadUserInfo(Context context) {
        String credentialFileContents = getInternallyStoredFileContents(context, CREDENTIALS_FILE_NAME);

        if (credentialFileContents == null) {
            // Unsuccessful in return attempt
            return false;
        }

        // Else, found the file!
        try {
            JSONObject credentials = new JSONObject(credentialFileContents);
            username = credentials.getString("username");
            password = credentials.getString("password");
        } catch (JSONException ex) {
            System.err.println(ex);
            return false;
        }

        // Successfully parsed the username and password from the file
        return true;
    }

    // Done - Final
    public static void setOnInitialized(Runnable onInitialized) {
        BeaconData.onInitialized = onInitialized;
    }

    public static void initiateQueue(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
    }

    public static boolean isQueueInitialized() {
        return queue != null;
    }

    public static boolean isLoggedIn() {
        return loginToken != null;
    }

    public static void isValidUsername(String username, final BeaconConsumer<Boolean> handler) {
        String query = generateQueryString("username", username);
        String uri = restAPIDomain + "/api/Users/UsernameIsTaken" + query;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Success
                    try {
                        boolean usernameIsTaken = response.getBoolean("isTaken");
                        handler.accept(usernameIsTaken);
                    } catch (JSONException ex) {
                        System.err.println(ex);
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Failed
                    System.err.println(error);
                }
            });

        queue.add(request);
    }

    public static void retrieveLoginToken(final BeaconConsumer<Integer> success, final Runnable failed, Context context) {
         login(new BeaconConsumer2<String, Integer>() {
                   @Override
                   public void accept(String token, Integer userId) {
                       BeaconData.loginToken = token;
                       success.accept(userId);
                   }
               },
                 new BeaconConsumer<String>() {
                     @Override
                     public void accept(String err) {
                         System.err.println(err);
                         failed.run();
                     }
                 }, context);
    }

    public static void syncVotesForEvents(final Runnable success, final Runnable failed) {
        getEventsVotedFor(
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Successfully grabbed the events voted for");
                    }
                },
                new BeaconConsumer<String>()
                {
                    @Override
                    public void accept(String s) {
                        System.out.println(s);
                    }
                }
        );
    }

    public static boolean areVotesLoaded() {
        return eventsVotedFor != null;
    }

    public static boolean areEventsDownloadedInitially() {
        return eventData != null;
    }

    // Done - Init
//    public static void initiate(Context context, final Float latitude, final Float longitude) {
//        // Initiate network queue
//        queue = Volley.newRequestQueue(context);
//
//        boolean wasAbleToRetrieveLoginInformation = tryToLoadUserInfo(context);
//
//        // If there is already a stored username and password, then use them to login
//        if (wasAbleToRetrieveLoginInformation) {
//            // login to get a token
//            login(new BeaconConsumer<String>() {
//                @Override
//                public void accept(String token) {
//                    // On Successful login
//                    BeaconData.loginToken = token;
//
//                    // Now get the events voted for
//                    getEventsVotedFor(
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                    System.out.println("Successfully grabbed the events voted for");
//                                    if (isInitialized()) {
//                                        onInitialized.run();
//                                    }
//                                }
//                            },
//                            new BeaconConsumer<String>()
//                            {
//                                @Override
//                                public void accept(String s) {
//                                    System.out.println(s);
//                                }
//                            }
//                    );
//
//                    // Now get the nearby event data
//                    downloadAllEventsInArea(new Runnable() {
//                        @Override
//                        public void run() {
//                            // Successfully download the updates, see if we're ready to call onInitialized()
//                            if (isInitialized()) {
//                                onInitialized.run();
//                            }
//                        }
//                    }, latitude, longitude);
//                }
//            }, new BeaconConsumer<String>() {
//                @Override
//                public void accept(String errorMsg) {
//                    System.out.println(errorMsg);
//                }
//            });
//        } else {
//            System.err.println("Need login information before initialization is possible! Call registerLogin() first if there is no current login information.");
//        }
//    }

    // Done - Init
    private static String generateQueryString(String... strings) {
        // Handle the special case here...
        StringBuilder sb = new StringBuilder("?" + strings[0] + "=" + strings[1]);

        for (int i = 2; i < strings.length; ++i) {
            if (i % 2 == 0) {
                // Parameter name detected
                sb.append("&" + strings[i] + "=");
            } else {
                // Value detected
                sb.append(strings[i]);
            }
        }

        return sb.toString();
    }

    // Done - Init
    private static void login(BeaconConsumer2<String, Integer> onSuccess, BeaconConsumer<String> onFailure, Context context) {
        if (tryToLoadUserInfo(context))
            login(username, password, onSuccess, onFailure);
        else
            Log.e("tryToLoadUserInfo", "did it work");
    }

    // Done - Init
    private static void login(String username, String password, final BeaconConsumer2<String, Integer> onSuccess, final BeaconConsumer<String> onFailure) {
        BeaconData.username = username;
        BeaconData.password = password;
        String queryString = generateQueryString("username", username, "password", password);
        Log.d("username", username);
        Log.d("password", password);
        String uri = restAPIDomain + "/api/Authenticator/Token" + queryString;

        JsonObjectRequest tokenRequest = new JsonObjectRequest(Request.Method.GET, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        Log.i("BeaconData.login", "Login response received from the server.");
                        try {
                            Boolean loginSuccessful = jobj.getBoolean("loginSuccessful");

                            if (loginSuccessful) {
                                String token = jobj.getString("token");
                                int id = jobj.getInt("userId");
                                Log.i("BeaconData.login", "Login was successful! (token, id) = (" + token + ", " + id + ")");

                                // Store userId for future use
                                currentUserId = id;

                                // Notify of successful login attempt.
                                onSuccess.accept(token, id);
                            } else {
                                Log.e("BeaconData.login", "Invalid Credentials");
                                onFailure.accept("Invalid Credentials");
                            }
                        } catch (Exception ex) {
                            // TODO: Problem loading the data!
                            Log.e("BeaconData.login", "Failed to parse server response.");
                            onFailure.accept("Failed to parse server response.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onFailure.accept(error.getMessage());
                    }
                }
        );

        System.out.println("***************************" + tokenRequest.toString());
        queue.add(tokenRequest);
    }

    private static Event createEventFromEventJsonObject(JSONObject jsonObject) {
        try {
            Event event = new Event();
            event.id = jsonObject.getInt("id");
            event.name = jsonObject.getString("name");
            event.creatorId = jsonObject.getInt("creatorId");
            event.name = jsonObject.getString("name");
            event.deleted = jsonObject.getBoolean("deleted");
            event.description = jsonObject.getString("description");
            event.latitude = jsonObject.getDouble("latitude");
            event.longitude = jsonObject.getDouble("longitude");
            event.voteCount = jsonObject.getInt("voteCount");
            event.timeCreated = jsonObject.getString("timeCreated");
            event.timeLastUpdated = jsonObject.getString("timeLastUpdated");

            return event;
        } catch (JSONException e) {
            Log.e("createEventFromEvent...", "Failed to parse event from event JsonObject");
            return null;
        }
    }

    // Done - Initial
    public static void downloadAllEventsInArea(final Runnable onSuccess, Float latitude, Float longitude) {
        String queryString = generateQueryString("token", loginToken, "lat", latitude.toString(), "lng", longitude.toString());
        String uri = restAPIDomain + "/api/Events/da" + queryString;

        // Request all events
        // Place in event objects
        // Notify for each of them that the events now exist

        // Ask for the information from the server...
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        try {
                            Log.i("downloadAllEventsInArea", "Successfully received a response");
                            if (eventData == null) {
                                eventData = new ArrayList<>();
                            }

                            if (jobj.getBoolean("wasSuccessful")) {
                                Log.i("downloadAllEventsInArea", "Response deemed successful");
                                JSONArray events = jobj.getJSONArray("events");

                                for (int i = 0; i < events.length(); ++i) {
                                    JSONObject jsonObject = events.getJSONObject(i);
                                    eventData.add(createEventFromEventJsonObject(jsonObject));
                                }
                            }

                            // Update the last updated time...
                            lastUpdatedTime = jobj.getString("currentServerTime");

                            // Done adding stuff!
                            onSuccess.run();

                            // Check to see if the class is done loading...
                            if (isInitialized()) {
                                onInitialized.run();
                            }
                        } catch (JSONException ex) {
                            Log.e("downloadAllEventsInArea", ex.toString());
                        }

                        updatedEventHandler.accept(eventData);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("downloadAllEventsInArea", "Failed to get the data.");
                    }
                }
        );

        queue.add(request);
    }

    // Done - Initial
    public static void downloadUpdates(Float latitude, Float longitude) {
        String queryString = generateQueryString("token", loginToken, "lat", latitude.toString(), "lng", longitude.toString(), "lastUpdatedTime", lastUpdatedTime);
        String uri = restAPIDomain + "/api/Events/du" + queryString;

        // Ask for the information from the server...
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        ArrayList<Event> updatedEvents = new ArrayList<>();

                        try {
                            Log.i("downloadUpdates", "Response received from the web server.");

                            if (jobj.getBoolean("wasSuccessful")) {
                                Log.i("downloadUpdates", "Response received was deemed successful.");
                                JSONArray eventsJsonArray = jobj.getJSONArray("events");
                                for (int i = 0; i < eventsJsonArray.length(); ++i) {
                                    Event event = createEventFromEventJsonObject(eventsJsonArray.getJSONObject(i));

                                    // Test to see if the event was deleted
                                    if (event.deleted) {
                                        // Remove from local database
                                        int indexToDelete = getEventIndex(event.id);
                                        if (indexToDelete != -1) {
                                            eventData.remove(indexToDelete);
                                        }
                                    } else {
                                        // The event was not deleted...
                                        if (!eventExists(event.id)) {
                                            // Create the new event
                                            eventData.add(event);
                                        } else {
                                            // Update an existing event
                                            int index = getEventIndex(event.id);
                                            eventData.set(index, event);
                                        }
                                    }

                                    lastUpdatedTime = jobj.getString("currentServerTime");

                                    // Make sure to add it to the notification list...
                                    updatedEvents.add(event);
                                }
                            } else {
                                Log.e("downloadUpdates", "Failed to download updates");
                            }
                        } catch (JSONException ex) {
                            Log.e("downloadUpdates", ex.toString());
                        }

                        updatedEventHandler.accept(updatedEvents);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("downloadUpdates", "Failed to get the data.");
                    }
                }
        );

        queue.add(request);
    }

    // Done - Initial
    public static Boolean eventExists(int id) {
        for (int i = 0; i < eventData.size(); ++i) {
            if (eventData.get(i).id == id) {
                return true;
            }
        }

        return false;
    }

    // Done - Final
    public static ArrayList<Event> getEvents() {
        return eventData;
    }

    // Done - Init
    public static ArrayList<Event> getEventsWithinDistance(float distance, float latitude, float longitude) {
        ArrayList<Event> eventsWithinDistance = new ArrayList<>();

        for (Event e : eventData) {
            double dlon = e.longitude - longitude;
            double dlat = e.latitude - latitude;
            double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(e.latitude) * Math.cos(latitude) * Math.pow(Math.sin(dlon / 2), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double cDistance = 3961 * c; //Number of miles in radius of Earth * c

            if (cDistance < distance) {
                // Event is close enough to consider
                eventsWithinDistance.add(e);
            }
        }

        // Return all of the events within the specified distance
        return eventsWithinDistance;
    }

    // Done - Init
    public static void deleteEvent(final Integer id) {
        // Remove from the server first
        // If successful, remove locally
        // Send a notification for the successful deletion
        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/" + id + queryString;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE,
                uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject obj) {
                        try {
                            if (obj.getBoolean("wasSuccessful")) {
                                // Deletion was successful from the server!

                                // Notify the listener that the event was deleted
                                int eIndex = getEventIndex(id);
                                Event e = eventData.get(eIndex);
                                e.deleted = true;
                                ArrayList<Event> updatedEvents = new ArrayList<>();
                                updatedEvents.add(e);
                                updatedEventHandler.accept(updatedEvents);

                                // Remove from local cache
                                eventData.remove(eIndex);
                            } else {
                                Log.e("deleteEvent", obj.getString("Message"));
                            }
                        } catch (JSONException ex) {
                            Log.e("deleteEvent", ex.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError x) {
                        Log.e("deleteEvent", x.toString());
                    }
                });

        queue.add(request);
    }

    // Done - Init
    public static void updateEvent(final Event event) {
        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events" + queryString;

        JSONObject eventObj = new JSONObject();
        try {
            //event.deleted, event.timeCreated, event.voteCount, event.latitude, event.longitude, event.description, event.id, event.name
            eventObj.put("id", event.id);
            eventObj.put("deleted", event.deleted);
            eventObj.put("timeCreated", event.timeCreated);
            eventObj.put("voteCount", event.voteCount);
            eventObj.put("latitude", event.latitude);
            eventObj.put("longitude", event.longitude);
            eventObj.put("description", event.description);
            eventObj.put("name", event.name);
        } catch (JSONException ex) {
            System.err.println(ex);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri,
                eventObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject obj) {
                        try {
                            if (obj.getBoolean("wasSuccessful")) {
                                // Successful!
                                // Event was updated successfully, so updated our local copy
                                int updatedEventIndex = getEventIndex(event.id);
                                eventData.set(updatedEventIndex, event);

                                // Notify the listener of changes
                                ArrayList<Event> updatedChanges = new ArrayList<>();
                                updatedChanges.add(event);
                                updatedEventHandler.accept(updatedChanges);
                            } else {
                                // Unsuccessful, print error message from server
                                System.err.println(obj.getString("message"));
                            }
                        } catch (JSONException ex) {
                            // Error parsing stuff...
                            System.err.println(ex);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Connection error, report it in the log
                        System.err.println(error);
                    }
                });

        queue.add(request);
    }

    // Done - Init
    public static Event getEvent(int id) {
        for (int i = 0; i < eventData.size(); ++i) {
            if (eventData.get(i).id == id) {
                return eventData.get(i);
            }
        }

        return null;
    }

    // Done - Init
    public static int getEventIndex(int id) {
        for (int i = 0; i < eventData.size(); ++i) {
            if (eventData.get(i).id == id) {
                return i;
            }
        }

        return -1;
    }

    // Done - Init
    public static void createEvent(String name, String description, double latitude, double longitude) {
        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/Create" + queryString;

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("description", description);
            obj.put("latitude", latitude);
            obj.put("longitude", longitude);
        } catch (JSONException ex) {
            System.err.println(ex);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        try {
                            if (jobj.getBoolean("wasSuccessful")) {
                                // Server says we successfully created an event!
                                // Now we need to parse the details and store locally...
                                Event event = new Event();
                                event.id = jobj.getInt("id");
                                event.deleted = jobj.getBoolean("deleted");
                                event.description = jobj.getString("description");
                                event.latitude = jobj.getDouble("latitude");
                                event.longitude = jobj.getDouble("longitude");
                                event.voteCount = jobj.getInt("voteCount");
                                System.out.println(jobj.getString("timeCreated"));
                                System.out.println(jobj.getString("timeLastUpdated"));

                                eventData.add(event);

                                // Notify that the event was created
                                ArrayList<Event> createdEvents = new ArrayList<>();
                                createdEvents.add(event);
                                updatedEventHandler.accept(createdEvents);
                            } else {
                                System.err.println(jobj.getString("Message"));
                            }
                        } catch (JSONException ex) {
                            System.err.println(ex);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError x) {
                        System.err.println(x);
                    }
                });

        queue.add(request);
        //code for pulling all new events from database
       // updateAllEvents
    }

    public static void createUser(String username, String email, String password, final Runnable onSuccess, final BeaconConsumer<String> onFailure) {
        String queryString = generateQueryString("username", username, "email", email, "password", password);
        String uri = restAPIDomain + "/api/Users/PostUser" + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, null,
        new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Success
                try {
                    if (response.getBoolean("wasSuccessful")) {
                        // Done!
                        onSuccess.run();
                    } else {
                        System.err.println(response.getString("message"));
                        onFailure.accept(response.getString("message"));
                    }
                } catch (JSONException ex) {
                    System.err.println(ex);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Failed
                System.err.println(error);
            }
        });

        queue.add(request);
    }

    // Done - Init
    public static void voteUpOnEvent(final int id) {
        if (!haveVotedForEvent(id)) {
            System.err.println("Already voted for this event...");
            return;
        }

        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/uv/" + id + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        try {
                            if (jobj.getBoolean("wasSuccessful")) {
                                Vote newVote = new Vote();
                                JSONObject vote = jobj.getJSONObject("vote");
                                newVote.eventId = jobj.getInt("eventId");
                                newVote.numVotes = jobj.getInt("numVotes");

                                eventsVotedFor.add(newVote);
                                Event event = getEvent(id);
                                assert event != null;
                                event.voteCount++;
                            } else {
                                System.err.println(jobj.getString("Message"));
                            }
                        } catch (JSONException ex) {
                            System.err.println(ex);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.err.println(error);
                    }
                });

        queue.add(request);
    }

    // Done - Init
    public static void voteDownOnEvent(final Integer id) {
        if (haveVotedForEvent(id) == false) {
            System.err.println("Already voted for this event...");
            return;
        }

        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/dv/" + id + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        try {
                            if (jobj.getBoolean("wasSuccessful")) {
                                Vote cVote = new Vote();
                                cVote.eventId = jobj.getJSONObject("vote").getInt("eventId");
                                cVote.numVotes = jobj.getJSONObject("vote").getInt("numVotes");
                                eventsVotedFor.add(cVote);
                                Event event = getEvent(id);
                                assert event != null;
                                event.voteCount--;
                            } else {
                                System.err.println(jobj.getString("Message"));
                            }
                        } catch (JSONException ex) {
                            Log.e("voteDownOnEvent", ex.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("voteDownOnEvent", error.toString());
                    }
                });

        queue.add(request);
    }

    // Done - Init
    public static void unvoteOnEvent(final int id) {
        // Make sure that the event was voted for...
        if (!haveVotedForEvent(id)) {
            System.err.println("Can't remove a vote for an event that you never voted for...");
            return;
        }

        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/unvote/" + id + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        try {
                            if (jobj.getBoolean("wasSuccessful")) {
                                JSONArray votes = jobj.getJSONArray("votes");
                                int numVotes = 0;
                                // Remove vote locally (value from Event)
                                for (int i = 0; i < votes.length(); ++i) {
                                    if (votes.getJSONObject(i).getInt("EventId") == id) {
                                        numVotes = votes.getJSONObject(i).getInt("NumVotes");
                                    }
                                }

                                // Remove vote itself
                                for (int i = 0; i < eventsVotedFor.size(); ++i) {
                                    if (eventsVotedFor.get(i).eventId == id) {
                                        eventsVotedFor.remove(i);
                                        Event event = getEvent(id);
                                        event.voteCount -= numVotes;
                                    }
                                }
                            } else {
                                System.err.println(jobj.getString("Message"));
                            }
                        } catch (JSONException ex) {
                            System.err.println(ex);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.err.println(error);
                    }
                });
    }

    // Done - Init
    public static void getEventsVotedFor(final Runnable onSuccess, final BeaconConsumer<String> onFailure) {
        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/VotedFor" + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        Log.i("getEventsVotedFor", "Server response received");
                        try {
                            if (eventsVotedFor == null) {
                                eventsVotedFor = new ArrayList<>();
                            }

                            if (jobj.getBoolean("wasSuccessful")) {
                                Log.i("getEventsVotedFor", "Response deemed successful.");
                                JSONArray arr = jobj.getJSONArray("votes");

                                for (int i = 0; i < arr.length(); ++i) {
                                    Vote cVote = new Vote();
                                    cVote.eventId = arr.getJSONObject(i).getInt("eventId");
                                    cVote.numVotes = arr.getJSONObject(i).getInt("numVotes");
                                    eventsVotedFor.add(cVote);
                                }

                                if (onSuccess != null) {
                                    onSuccess.run();

                                    // Check to see if the class is done loading...
                                    if (isInitialized()) {
                                        Log.i("getEventsVotedFor", "onInitialized() called");
                                        onInitialized.run();
                                    }
                                }
                            } else {
                                if (onFailure != null) {
                                    Log.e("getEventsVotedFor", "Response received, but was deemed unsuccessful");
                                    onFailure.accept("Invalid login detected when trying to retrieve user votes.");
                                }
                            }
                        } catch (JSONException ex) {
                            System.err.println(ex);
                            Log.e("getEventsVotedFor", ex.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.err.println(error);
                        Log.e("getEventsVotedFor", error.toString());
                    }
                });

        queue.add(request);
    }

    // Done - Init
    public static boolean haveVotedForEvent(int id) {
        for (int i = 0; i < eventsVotedFor.size(); ++i) {
            if (eventsVotedFor.get(i).eventId == id) {
                return true;
            }
        }

        return false;
    }

    public static Voted getVoteDecisionForEvent(int id) {
        for (int i = 0; i < eventsVotedFor.size(); ++i) {
            if (eventsVotedFor.get(i).eventId == id) {
                if (eventsVotedFor.get(i).numVotes > 0) {
                    return Voted.FOR;
                }
                else {
                    return Voted.AGAINST;
                }
            }
        }

        return null;
    }

    public static void setAttendedEvent(int id, Callable<Integer> callMe) {
        try {
            callMe.call();
        } catch (Exception e) {
        }
    }

    // Done - Final
    public static void setEventUpdateHandler(BeaconConsumer<ArrayList<Event>> updatedEventHandler) {
        BeaconData.updatedEventHandler = updatedEventHandler;
    }

    public static void updateUserPassword(String currPass, String newPass) {
        String queryString = generateQueryString("id", currentUserId.toString(), "oldPassword", currPass, "newPassword", newPass);
        String uri = restAPIDomain + "/api/Users/changePassword" + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jobj) {
                        try {
                            if (jobj.getBoolean("wasSuccessful")) {
                                System.out.println("success");
                            } else {
                                System.err.println(jobj.getString("Message"));
                            }
                        } catch (JSONException ex) {
                            System.err.println(ex);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.err.println(error);
                    }
                });

        queue.add(request);
    }
}
