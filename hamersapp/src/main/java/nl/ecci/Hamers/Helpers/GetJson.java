package nl.ecci.Hamers.Helpers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import nl.ecci.Hamers.Beers.BeerFragment;
import nl.ecci.Hamers.Events.EventFragment;
import nl.ecci.Hamers.MainActivity;
import nl.ecci.Hamers.News.NewsFragment;
import nl.ecci.Hamers.Quotes.QuoteListFragment;
import nl.ecci.Hamers.R;
import nl.ecci.Hamers.Users.UserFragment;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class GetJson extends AsyncTask<String, String, String> {
    public static final String QUOTEURL = "/quote.json";
    public static final String USERURL = "/user.json";
    public static final String EVENTURL = "/event.json";
    public static final String NEWSURL = "/news.json";
    public static final String BEERURL = "/beer.json";
    public static final String REVIEWURL = "/review.json";
    private static final boolean DEBUG = false;
    private final Fragment f;
    private final String typeURL;
    private final SharedPreferences prefs;
    private final Activity a;
    private final boolean firstload;

    public GetJson(Activity a, Fragment f, String typeURL, SharedPreferences s, Boolean firstload) {
        this.f = f;
        this.typeURL = typeURL;
        this.prefs = s;
        this.a = a;
        this.firstload = firstload;
    }

    protected String doInBackground(String... params) {
        BufferedReader reader;
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(MainActivity.baseURL + prefs.getString(DataManager.APIKEYKEY, "a") + typeURL);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            reader.close();
        } catch (MalformedURLException e) {
            if (DEBUG) {
                System.out.println("--------------------------Malformed URL!: ");
                e.printStackTrace();
            }
            return null;
        } catch (IOException e) {
            if (DEBUG) {
                System.out.println("--------------------------IOException!: ");
                e.printStackTrace();
            }
            return null;
        }
        return buffer.toString();
    }

    @Override
    protected void onPostExecute(String result) {

        if (result == null) {
            if (firstload) {
                ((MainActivity) a).loadData2(prefs, false);
            }
        } else {
            try {
                JSONArray arr = new JSONArray(result);
                if (arr.getJSONObject(0).has("error")) {
                    ((MainActivity) a).loadData2(prefs, false);
                }
                if (firstload && a instanceof MainActivity) {
                    ((MainActivity) a).loadData2(prefs, true);
                }
                if (result.equals("{}")) {
                    Toast.makeText(a, a.getString(R.string.toast_downloaderror), Toast.LENGTH_SHORT).show();
                } else {
                    if (f instanceof QuoteListFragment) {
                        prefs.edit().putString(DataManager.QUOTEKEY, result).apply();
                        MainActivity.quoteListFragment.populateList(prefs);
                    }
                    // User fragment
                    else if (f instanceof UserFragment) {
                        prefs.edit().putString(DataManager.USERKEY, result).apply();
                        GetJson g = new GetJson(a, f, USERURL, prefs, false);
                        g.execute();
                        MainActivity.userFragment.populateList(prefs);
                    }
                    // Event fragment
                    else if (f instanceof EventFragment) {
                        prefs.edit().putString(DataManager.EVENTKEY, result).apply();
                        MainActivity.eventFragment.populateList(prefs);
                    } else if (f instanceof NewsFragment) {
                        prefs.edit().putString(DataManager.NEWSKEY, result).apply();
                        MainActivity.newsFragment.populateList(prefs);
                    }
                    // Beer fragment
                    else if (f instanceof BeerFragment) {
                        prefs.edit().putString(DataManager.BEERKEY, result).apply();
                        GetJson g = new GetJson(a, f, BEERURL, prefs, false);
                        g.execute();
                        GetJson g2 = new GetJson(a, null, REVIEWURL, prefs, false);
                        g2.execute();
                        MainActivity.beerFragment.populateList(prefs);
                    }
                    // Quote
                    else if (typeURL.equals(QUOTEURL)) {
                        prefs.edit().putString(DataManager.QUOTEKEY, result).apply();
                        MainActivity.quoteListFragment.populateList(prefs);
                    }
                    // Beer
                    else if (typeURL.equals(BEERURL)) {
                        prefs.edit().putString(DataManager.BEERKEY, result).apply();
                        MainActivity.beerFragment.populateList(prefs);
                    }
                    // Review
                    else if (typeURL.equals(REVIEWURL)) {
                        prefs.edit().putString(DataManager.REVIEWKEY, result).apply();
                        GetJson g = new GetJson(a, f, BEERURL, prefs, false);
                        g.execute();
                    }
                }
            } catch (JSONException e) {
                if (firstload) {
                    ((MainActivity) a).loadData2(prefs, false);
                }
            }
        }
    }
}
