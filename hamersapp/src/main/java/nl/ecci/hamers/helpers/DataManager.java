package nl.ecci.hamers.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nl.ecci.hamers.MainActivity;

public final class DataManager {
    public static final String QUOTEURL = "/quote.json";
    public static final String USERURL = "/user.json";
    public static final String EVENTURL = "/event.json";
    public static final String NEWSURL = "/news.json";
    public static final String BEERURL = "/beer.json";
    public static final String REVIEWURL = "/review.json";
    public static final String WHOAMIURL = "/whoami.json";
    public static final String MOTIEURL = "/motions";
    public static final String SIGNUPURL = "/signup";
    public static final String GCMURL = "/register";
    public static final String QUOTEKEY = "quoteData";
    public static final String USERKEY = "userData";
    public static final String EVENTKEY = "eventData";
    public static final String NEWSKEY = "newsData";
    public static final String BEERKEY = "beerData";
    public static final String REVIEWKEY = "reviewdata";
    public static final String APIKEYKEY = "apikey";
    public static final String WHOAMIKEY = "whoamikey";
    public static final String AUTHENTICATED = "authenticated";


    public static void getData(Context context, final SharedPreferences prefs, final String dataURL, final String dataKEY) {
        String url = MainActivity.baseURL + prefs.getString(DataManager.APIKEYKEY, "a") + dataURL;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        prefs.edit().putString(dataKEY, response).apply();
                        populateList(dataURL, prefs);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("--------------------\nError:\n" + error.toString());
                        if (error instanceof AuthFailureError) {
                            // Wrong API key
                        }
                    }
                });

        Singleton.getInstance(context).addToRequestQueue(request);
    }

    public static void populateList(String data, SharedPreferences prefs) {
        switch (data) {
            case QUOTEURL:
                MainActivity.QUOTE_FRAGMENT.populateList(prefs);
                break;
            case BEERURL:
                MainActivity.BEER_FRAGMENT.populateList(prefs);
                break;
            case REVIEWURL:
                MainActivity.BEER_FRAGMENT.populateList(prefs);
                break;
            case EVENTURL:
                MainActivity.EVENT_FRAGMENT.populateList(prefs);
                break;
            case NEWSURL:
                MainActivity.NEWS_FRAGMENT.populateList(prefs);
                break;
            case USERURL:
                MainActivity.USER_FRAGMENT.populateList(prefs);
                break;
        }
    }

    public static JSONObject getUser(SharedPreferences prefs, int id) {
        JSONArray users;
        try {
            if ((users = getJsonArray(prefs, USERKEY)) != null) {
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    if (user.getInt("id") == id) {
                        return user;
                    }
                }
            }
        } catch (JSONException e) {
            return null;
        }
        return null;
    }

    public static JSONObject getEvent(SharedPreferences prefs, String title, Date date) {
        JSONArray events;
        try {
            if ((events = getJsonArray(prefs, EVENTKEY)) != null) {
                for (int i = 0; i < events.length(); i++) {
                    JSONObject temp = events.getJSONObject(i);
                    DateFormat dbDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", new Locale("nl"));
                    Date dbDatum = dbDF.parse(temp.getString("date"));
                    if (dbDatum.equals(date)) {
                        if (temp.getString("title").equals(title)) {
                            return temp;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isAuthenticated(SharedPreferences prefs) {
        return prefs.getBoolean("Authenticated", false);
    }

    public static JSONObject getBeer(SharedPreferences prefs, String name) {
        JSONArray beers;
        try {
            if ((beers = getJsonArray(prefs, BEERKEY)) != null) {
                for (int i = 0; i < beers.length(); i++) {
                    JSONObject temp = beers.getJSONObject(i);
                    if (temp.getString("name").equals(name)) {
                        return temp;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static JSONObject getBeer(SharedPreferences prefs, int id) {
        JSONArray beers;
        try {
            if ((beers = getJsonArray(prefs, BEERKEY)) != null) {
                for (int i = 0; i < beers.length(); i++) {
                    JSONObject temp = beers.getJSONObject(i);
                    if (temp.getInt("id") == id) {
                        return temp;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static JSONArray getJsonArray(SharedPreferences prefs, String key) {
        try {
            return new JSONArray(prefs.getString(key, null));
        } catch (JSONException | NullPointerException e) {
            return null;
        }
    }

    public static int usernameToID(SharedPreferences prefs, String name) {
        JSONArray userJSON;
        int returnv = -1;
        try {
            if ((userJSON = getJsonArray(prefs, USERKEY)) != null) {
                for (int i = 0; i < userJSON.length(); i++) {
                    if (userJSON.getJSONObject(i).getString("name").equals(name)) {
                        returnv = userJSON.getJSONObject(i).getInt("id");
                    }
                }
            }
        } catch (JSONException e) {
            return returnv;
        }
        return returnv;
    }

    public static String IDToEmail(SharedPreferences prefs, int id) {
        String result = null;
        JSONArray users;
        try {
            if ((users = getJsonArray(prefs, USERKEY)) != null) {
                for (int i = 0; i < users.length(); i++) {
                    if (users.getJSONObject(i).getInt("id") == id) {
                        result = users.getJSONObject(i).getString("email");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String UserIDtoUserName(SharedPreferences prefs, int id) {
        String result = null;
        JSONArray users;
        try {
            if ((users = getJsonArray(prefs, USERKEY)) != null) {
                for (int i = 0; i < users.length(); i++) {
                    if (users.getJSONObject(i).getInt("id") == id) {
                        result = users.getJSONObject(i).getString("name");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String BeerIDtoBeerName(SharedPreferences prefs, int id) {
        String result = null;
        JSONArray users;
        try {
            if ((users = getJsonArray(prefs, BEERKEY)) != null) {
                for (int i = 0; i < users.length(); i++) {
                    if (users.getJSONObject(i).getInt("id") == id) {
                        result = users.getJSONObject(i).getString("name");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getUserID(SharedPreferences prefs) {
        int result = 0;
        JSONArray whoami = DataManager.getJsonArray(prefs, DataManager.WHOAMIKEY);
        try {
            if (whoami != null) {
                result = whoami.getJSONObject(0).getInt("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getUserName(SharedPreferences prefs) {
        return UserIDtoUserName(prefs, getUserID(prefs));
    }
}
