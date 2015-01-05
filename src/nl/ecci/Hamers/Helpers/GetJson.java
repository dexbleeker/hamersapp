package nl.ecci.Hamers.Helpers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.widget.Toast;
import nl.ecci.Hamers.Beers.BeerFragment;
import nl.ecci.Hamers.Events.EventFragment;
import nl.ecci.Hamers.MainActivity;
import nl.ecci.Hamers.Quotes.QuoteListFragment;
import nl.ecci.Hamers.R;
import nl.ecci.Hamers.Users.UserFragment;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class GetJson extends AsyncTask<String, String, String> {
    public static final String baseURL = "http://zondersikkel.nl/api/v1/";
    public static final String QUOTEURL = "/quote.json";
    public static final String USERURL = "/user.json";
    public static final String EVENTURL = "/event.json";
    public static final String BEERURL = "/beer.json";
    private static final boolean DEBUG = false;
    private Fragment f;
    private String typeURL;
    private SharedPreferences prefs;
    private Activity a;
    private boolean firstload;

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
            URL url = new URL(baseURL + prefs.getString(DataManager.APIKEYKEY, "a") + typeURL);
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
        if (typeURL == USERURL) {
            try {
                downloadProfilepictures(new JSONArray(buffer.toString()));
            } catch (JSONException e) {
                if (DEBUG) {
                    System.out.println("--------------------------JSONException!: ");
                    e.printStackTrace();
                }
                //todo mogelijk deze exceptie handlen
            }
            ;
        }
        if (typeURL == BEERURL) {
            try {
                downloadBeerpictures(new JSONArray(buffer.toString()));
            } catch (JSONException e) {
                if (DEBUG) {
                    System.out.println("--------------------------JSONException!: ");
                    e.printStackTrace();
                }
                //todo mogelijk deze exceptie handlen
            }
        }
        return buffer.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        if (firstload && a instanceof MainActivity) {
            ((MainActivity) a).loadData2(prefs);
        }
        if (result == null || result.equals("{}")) {
            Toast.makeText(a, a.getString(R.string.toast_downloaderror), Toast.LENGTH_SHORT).show();
        } else {
            if (typeURL == USERURL) {
                prefs.edit().putString(DataManager.USERKEY, result).apply();
            }
            // Quotelist fragment
            if (f instanceof QuoteListFragment) {
                prefs.edit().putString(DataManager.QUOTEKEY, result).apply();
                ((QuoteListFragment) f).populateList(prefs);
            }
            // User fragment
            else if (f instanceof UserFragment) {
                ((UserFragment) f).populateList(prefs);
            }
            // Event fragment
            else if (f instanceof EventFragment) {
                prefs.edit().putString(DataManager.EVENTKEY, result).apply();
                ((EventFragment) f).populateList(prefs);
            }
            // Beer fragment
            else if (f instanceof BeerFragment) {
                prefs.edit().putString(DataManager.BEERKEY, result).apply();
                ((BeerFragment) f).populateList(prefs);
            }
        }

    }


    private void downloadProfilepictures(JSONArray users) {
        for (int i = 0; i < users.length(); i++) {
            try {
                if (DEBUG) {
                    System.out.println("downloading user picture " + i);
                }
                URL url = new URL("http://gravatar.com/avatar/" + MD5Util.md5Hex(users.getJSONObject(i).getString("email")));
                InputStream in = new BufferedInputStream(url.openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                prefs.edit().putString(DataManager.USERIMAGEKEY + users.getJSONObject(i).getString("id"), Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)).apply();
                //For restoring byte[] array = Base64.decode(stringFromSharedPrefs, Base64.DEFAULT);
            } catch (IOException e) {
                if (DEBUG) {
                    System.out.println("--------------------------IOException!: ");
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                if (DEBUG) {
                    System.out.println("--------------------------JSONException!: ");
                    e.printStackTrace();
                }
            }
        }
    }

    private void downloadBeerpictures(JSONArray beers) {
        for (int i = 0; i < beers.length(); i++) {
            try {
                if (DEBUG) {
                    System.out.println("downloading beer picture " + i);
                }
                URL url = new URL(beers.getJSONObject(i).getString("picture"));
                InputStream in = new BufferedInputStream(url.openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                prefs.edit().putString(DataManager.BEERIMAGEKEY + beers.getJSONObject(i).getString("name"), Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)).apply();
            } catch (MalformedURLException e) {
                if (DEBUG) {
                    System.out.println("--------------------------Malformed URL!: ");
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                if (DEBUG) {
                    System.out.println("--------------------------JSONException!: ");
                    e.printStackTrace();
                }
            } catch (IOException e) {
                if (DEBUG) {
                    System.out.println("--------------------------IOException!: ");
                    e.printStackTrace();
                }
            }
            //For restoring byte[] array = Base64.decode(stringFromSharedPrefs, Base64.DEFAULT);

        }
    }

}
