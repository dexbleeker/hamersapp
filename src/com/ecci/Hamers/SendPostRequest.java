package com.ecci.Hamers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendPostRequest extends AsyncTask<String, String, String> {

    private static final String baseurl =  "http://zondersikkel.nl/api/v1/";
    public static final String QUOTE = "/quote";
    private SharedPreferences prefs;
    private String type;
    private String urlParams;
    private Fragment f;
    private Context mContext;

    public SendPostRequest(Context context, Fragment f, String type, SharedPreferences s, String urlParams){
        prefs = s;
        this.f = f;
        this.type = type;
        this.urlParams = urlParams;
        mContext = context;
    }

    protected String doInBackground(String... params) {
        int response = -1;
        try {
            URL url = new URL(baseurl + prefs.getString("apikey", "a") + type);
            System.out.println(url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", "" + Integer.toString(urlParams.getBytes().length));
            con.setUseCaches (false);

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParams);
            wr.flush();
            wr.close();

            response = con.getResponseCode();
            con.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(response);
    }

    protected void onPostExecute(String result) {
        if(result.equals("201")) {
            Toast.makeText(mContext, "Quote posted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Quote not posted, try again later...", Toast.LENGTH_SHORT).show();
        }
    }
}
