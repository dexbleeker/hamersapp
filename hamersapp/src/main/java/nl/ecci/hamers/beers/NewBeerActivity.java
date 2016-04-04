package nl.ecci.hamers.beers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import nl.ecci.hamers.R;
import nl.ecci.hamers.helpers.DataManager;

public class NewBeerActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_beer_activity);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void postBeer(View view) {
        try {
            EditText beer_title = (EditText) findViewById(R.id.beer_title_et);
            EditText beer_picture = (EditText) findViewById(R.id.beer_picture_et);
            EditText beer_soort = (EditText) findViewById(R.id.beer_soort_et);
            EditText beer_percentage = (EditText) findViewById(R.id.beer_percentage_et);
            EditText beer_brewer = (EditText) findViewById(R.id.beer_brewer_et);
            EditText beer_country = (EditText) findViewById(R.id.beer_country_et);

            String title = URLEncoder.encode(beer_title.getText().toString(), "UTF-8");
            String picture = URLEncoder.encode(beer_picture.getText().toString(), "UTF-8");
            String soort = URLEncoder.encode(beer_soort.getText().toString(), "UTF-8");
            String percentage = URLEncoder.encode(beer_percentage.getText().toString(), "UTF-8");
            String brewer = URLEncoder.encode(beer_brewer.getText().toString(), "UTF-8");
            String country = URLEncoder.encode(beer_country.getText().toString(), "UTF-8");

            if (!percentage.contains("%")) {
                percentage = percentage + "%";
            }

            Map<String, String> params = new HashMap<>();
            params.put("beer[name]", title);
            params.put("beer[picture]", picture);
            params.put("beer[percentage]", percentage);
            params.put("beer[country]", country);
            params.put("beer[brewer]", brewer);
            params.put("beer[soort]", soort);

            DataManager.postData(this, prefs, DataManager.BEERURL, DataManager.BEERKEY, params);
        } catch (UnsupportedEncodingException ignored) {
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}