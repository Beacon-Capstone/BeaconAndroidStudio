package com.capstone.while1.beaconandroidstudio.beacondata;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
import java.util.function.Consumer;

class BeaconData {
    /*
        Constants
     */
    private static final int DEFAULT_MILES_FOR_EVENTS = 10;
    private static final String CREDENTIALS_FILE_NAME = "credentials.txt";

    private static String restAPIDomain = "http://f5183551.ngrok.io";
    private static String loginToken = null;
    private static ArrayList<Event> eventData = null;
    private static String lastUpdatedTime = null;
    private static String username = "joemelt101";
    private static String password = "password";
    private static RequestQueue queue = null;
    private static Float latitude = null;
    private static Float longitude = null;
    private static Consumer<ArrayList<Event>> updatedEventHandler = null;
    private static Runnable onInitialized = null;
    private static ArrayList<Integer> eventsVotedFor = null;

    // Make the constructor private to force static use of the class.
    private BeaconData() {
    }

    // Done - Init
    private static Boolean userHasLocallySavedLoginInformation(Context context) {
        File loginFile = new File(context.getFilesDir(), CREDENTIALS_FILE_NAME);
        return loginFile.exists(); // If it exists, then there is information stored locally for this user...
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
    public static Boolean retrieveLoginData(Context context) {
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

    // Done - Init
    public static void initiate(Context context, float longitude, float latitude) {
        BeaconData.latitude = latitude;
        BeaconData.longitude = longitude;

        // Initiate network queue
        queue = Volley.newRequestQueue(context);

        boolean wasAbleToRetrieveLoginInformation = retrieveLoginData(context);

        // If there is already a stored username and password, then use them to login
        if (wasAbleToRetrieveLoginInformation) {
            // login to get a token
            login(token -> {
                // On Successful login
                System.out.println("Token = " + token);

                // Now get the events voted for
                getEventsVotedFor(
                        () -> {
                            System.out.println("Successfully grabbed the events voted for");
                            if (isInitialized()) {
                                onInitialized.run();
                            }
                        },
                        s -> System.out.println(s)
                );

                // Now get the nearby event data
                downloadAllEventsInArea(() -> {
                    // Successfully download the updates, see if we're ready to call onInitialized()
                    if (isInitialized()) {
                        onInitialized.run();
                    }
                });
            }, errorMsg -> System.out.println(errorMsg));
        } else {
            System.err.println("Need login information before initialization is possible! Call registerLogin() first if there is no current login information.");
        }
    }

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
    private static void login(Consumer<String> onSuccess, Consumer<String> onFailure) {
        login(username, password, onSuccess, onFailure);
    }

    // Done - Init
    private static void login(String username, String password, Consumer<String> onSuccess, Consumer<String> onFailure) {
        BeaconData.username = username;
        BeaconData.password = password;
        String queryString = generateQueryString("username", username, "password", password);
        String uri = restAPIDomain + "/api/Authenticator/Token" + queryString;

        JsonObjectRequest tokenRequest = new JsonObjectRequest(Request.Method.GET, uri, null,
                jobj -> {
                    try {
                        Boolean loginSuccessful = jobj.getBoolean("loginSuccessful");

                        if (loginSuccessful) {
                            String token = jobj.getString("token");
                            onSuccess.accept(token);
                        } else {
                            onFailure.accept("Invalid Credentials");
                        }
                    } catch (Exception ex) {
                        // TODO: Problem loading the data!
                        onFailure.accept("Failed to parse server response.");
                    }
                },
                error -> {
                    onFailure.accept(error.getMessage());
                }
        );

        System.out.println("***************************" + tokenRequest.toString());
        queue.add(tokenRequest);
    }

    // Done - Init
    private static void updateLastUpdatedTime() {
        // Calculate the UTC time and apply it to an appendable string in lastUpdatedTime
        final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        lastUpdatedTime = f.format((new Date()));
    }

    // Done - Initial
    private static void downloadAllEventsInArea(Runnable onSuccess) {
        String queryString = generateQueryString("token", loginToken, "lat", latitude.toString(), "lng", longitude.toString());
        String uri = restAPIDomain + "/api/Events/da" + queryString;

        // Request all events
        // Place in event objects
        // Notify for each of them that the events now exist

        // Ask for the information from the server...
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, uri, null,
                jobj -> {
                    try {
                        //TODO: Fix this... Double check the structure of the data...
                        for (int i = 0; i < jobj.length(); ++i) {
                            JSONObject jsonObject = jobj.getJSONObject(i);
                            Event event = new Event();
                            event.id = jsonObject.getInt("id");
                            event.deleted = jsonObject.getBoolean("deleted");
                            event.description = jsonObject.getString("description");
                            event.latitude = jsonObject.getDouble("latitude");
                            event.longitude = jsonObject.getDouble("longitude");
                            event.voteCount = jsonObject.getInt("voteCount");

                            System.out.println(event.timeCreated);
                            System.out.println(event.timeLastUpdated);
                            eventData.add(event);
                        }

                        // Done adding stuff!
                        onSuccess.run();
                    } catch (Exception ex) {
                        // TODO: Problem loading the data!
                        System.err.println(ex);
                    }

                    updatedEventHandler.accept(eventData);
                },
                error -> {
                    System.out.println("Failed to get the data... Retrying...");
                }
        );

        queue.add(request);
    }

    // Done - Initial
    private static void downloadUpdates() {
        String queryString = generateQueryString("token", loginToken, "lat", latitude.toString(), "lng", longitude.toString(), "lastUpdatedTime", lastUpdatedTime);
        String uri = restAPIDomain + "/api/Events/du" + queryString;

        // Ask for the information from the server...
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,
                jobj -> {
                    ArrayList<Event> updatedEvents = new ArrayList<>();

                    try {
                        if (jobj.getBoolean("wasSuccessful")) {
                            JSONArray eventsJsonArray = jobj.getJSONArray("events");
                            for (int i = 0; i < eventsJsonArray.length(); ++i) {
                                JSONObject jsonObject = eventsJsonArray.getJSONObject(i);
                                Event event = new Event();
                                event.id = jsonObject.getInt("id");
                                event.deleted = jsonObject.getBoolean("deleted");
                                event.description = jsonObject.getString("description");
                                event.latitude = jsonObject.getDouble("latitude");
                                event.longitude = jsonObject.getDouble("longitude");
                                event.voteCount = jsonObject.getInt("voteCount");
                                System.out.println(event.timeCreated);
                                System.out.println(event.timeLastUpdated);

                                // Test to see if the event was deleted
                                if (event.deleted) {
                                    // Remove from local database
                                    int indexToDelete = getEventIndex(event.id);
                                    eventData.remove(indexToDelete);
                                } else {
                                    // The event was not deleted...
                                    if (eventExists(jsonObject.getInt("id")) == false) {
                                        // Create the new event
                                        eventData.add(event);
                                    } else {
                                        // Update an existing event
                                        int index = getEventIndex(event.id);
                                        eventData.set(index, event);
                                    }
                                }

                                // Make sure to add it to the notification list...
                                updatedEvents.add(event);
                            }
                        } else {
                            System.err.println("Request failed for getting updates...");
                        }
                    } catch (Exception ex) {
                        // TODO: Problem loading the data!
                        System.err.println(ex);
                    }

                    updatedEventHandler.accept(updatedEvents);
                },
                error -> {
                    System.out.println("Failed to get the data... Retrying...");
                }
        );

        queue.add(request);
    }

    // Done - Initial
    private static Boolean eventExists(int id) {
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
    public static ArrayList<Event> getEventsWithinDistance(float distance) {
        ArrayList<Event> eventsWithinDistance = new ArrayList<Event>();

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
    public static void deleteEvent(Integer id) {
        // Remove from the server first
        // If successful, remove locally
        // Send a notification for the successful deletion
        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/" + id + queryString;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE,
                uri, null,
                obj -> {
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
                            System.err.println(obj.getString("Message"));
                        }
                    } catch (JSONException ex) {
                        System.err.println(ex);
                    }
                },
                error -> System.err.println(error));

        queue.add(request);
    }

    // Done - Init
    public static void updateEvent(Event event) {
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
                obj -> {
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
                },
                error -> {
                    // Connection error, report it in the log
                    System.err.println(error);
                });
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
    public static void createEvent(String name, String description) {
        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/Create" + queryString;

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("description", description);
        } catch (JSONException ex) {
            System.err.println(ex);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, obj,
                jobj -> {
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
                },
                error -> {
                    System.err.println(error);
                });

        queue.add(request);
    }

    // Done - Init
    public static void voteUpOnEvent(int id) {
        if (haveVotedForEvent(id) == false) {
            System.err.println("Already voted for this event...");
            return;
        }

        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/uv/" + id + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, null,
                jobj -> {
                    try {
                        if (jobj.getBoolean("wasSuccessful")) {
                            eventsVotedFor.add(id);
                            Event event = getEvent(id);
                            event.voteCount++;
                        } else {
                            System.err.println(jobj.getString("Message"));
                        }
                    } catch (JSONException ex) {
                        System.err.println(ex);
                    }
                },
                error ->
                {
                    System.err.println(error);
                });
    }

    // Done - Init
    public static void voteDownOnEvent(Integer id) {
        if (haveVotedForEvent(id) == false) {
            System.err.println("Already voted for this event...");
            return;
        }

        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/dv/" + id + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, null,
                jobj -> {
                    try {
                        if (jobj.getBoolean("wasSuccessful")) {
                            eventsVotedFor.add(id);
                            Event event = getEvent(id);
                            event.voteCount--;
                        } else {
                            System.err.println(jobj.getString("Message"));
                        }
                    } catch (JSONException ex) {
                        System.err.println(ex);
                    }
                },
                error ->
                {
                    System.err.println(error);
                });
    }

    // Done - Init
    public static void unvoteOnEvent(int id) {
        // Make sure that the event was voted for...
        if (haveVotedForEvent(id) == false) {
            System.err.println("Can't remove a vote for an event that you never voted for...");
            return;
        }

        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/unvote/" + id + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, null,
                jobj -> {
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
                                if (eventsVotedFor.get(i) == id) {
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
                },
                error -> {
                    System.err.println(error);
                });
    }

    // Done - Init
    public static void getEventsVotedFor(Runnable onSuccess, Consumer<String> onFailure) {
        String queryString = generateQueryString("token", loginToken);
        String uri = restAPIDomain + "/api/Events/votedFor" + queryString;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, null,
                jobj -> {
                    try {
                        if (jobj.getBoolean("wasSuccessful")) {
                            JSONArray arr = jobj.getJSONArray("votes");

                            for (int i = 0; i < arr.length(); ++i) {
                                Integer val = arr.getInt(i);
                                eventsVotedFor.add(val);
                            }

                            if (onSuccess != null) {
                                onSuccess.run();
                            }
                        } else {
                            if (onFailure != null) {
                                onFailure.accept("Invalid login detected when trying to retrieve user votes.");
                            }
                        }
                    } catch (JSONException ex) {
                        System.err.println(ex);
                    }
                },
                error ->
                {
                    System.err.println(error);
                });

    }

    // Done - Init
    public static boolean haveVotedForEvent(int id) {
        for (int i = 0; i < eventsVotedFor.size(); ++i) {
            return eventsVotedFor.get(i) == id;
        }

        return true;
    }

    public static void setAttendedEvent(int id, Callable<Integer> callMe) {
        try {
            callMe.call();
        } catch (Exception e) {
        }
    }

    // Done - Final
    public void setEventUpdateHandler(Consumer<ArrayList<Event>> updatedEventHandler) {
        BeaconData.updatedEventHandler = updatedEventHandler;
    }
}
