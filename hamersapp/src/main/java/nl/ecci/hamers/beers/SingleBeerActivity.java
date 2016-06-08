package nl.ecci.hamers.beers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nl.ecci.hamers.MainActivity;
import nl.ecci.hamers.R;
import nl.ecci.hamers.helpers.DataManager;
import nl.ecci.hamers.helpers.SingleImageActivity;

import static nl.ecci.hamers.helpers.DataManager.getJsonArray;
import static nl.ecci.hamers.helpers.DataManager.getUser;
import static nl.ecci.hamers.helpers.DataManager.getOwnUser;

public class SingleBeerActivity extends AppCompatActivity {

    private int id;
    private String name;
    private Button reviewButton;
    private ViewGroup reviewViewGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.single_beer);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        TextView nameTV = (TextView) findViewById(R.id.beer_name);

        View kindRow = findViewById(R.id.row_kind);
        View alcRow = findViewById(R.id.row_alc);
        View brewerRow = findViewById(R.id.row_brewer);
        View countryRow = findViewById(R.id.row_country);
        View ratingRow = findViewById(R.id.row_rating);
        reviewViewGroup = (ViewGroup) findViewById(R.id.reviews);

        final ImageView beerImage = (ImageView) findViewById(R.id.beer_image);
        reviewButton = (Button) findViewById(R.id.sendreview_button);

        Bundle extras = getIntent().getExtras();

        id = extras.getInt(Beer.BEER_ID);
        name = extras.getString(Beer.BEER_NAME);
        final String kind = extras.getString(Beer.BEER_KIND);
        final String url = extras.getString(Beer.BEER_URL);
        final String percentage = extras.getString(Beer.BEER_PERCENTAGE);
        final String brewer = extras.getString(Beer.BEER_BREWER);
        final String country = extras.getString(Beer.BEER_COUNTRY);
        final String rating = extras.getString(Beer.BEER_RATING);

        fillRow(kindRow, getString(R.string.beer_soort), kind);
        fillRow(alcRow, getString(R.string.beer_alc), percentage);
        fillRow(brewerRow, getString(R.string.beer_brewer), brewer);
        fillRow(countryRow, getString(R.string.beer_country), country);

        nameTV.setText(name);

        if (rating.equals("null")) {
            fillRow(ratingRow, getString(R.string.beer_rating), "Nog niet bekend");
        } else {
            fillRow(ratingRow, getString(R.string.beer_rating), rating);
        }

        // Universal Image Loader
        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(url, beerImage);

        beerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleBeerActivity.this, SingleImageActivity.class);
                String transitionName = getString(R.string.transition_single_image);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(SingleBeerActivity.this, beerImage, transitionName);
                intent.putExtra(Beer.BEER_NAME, name);
                intent.putExtra(Beer.BEER_URL, url);
                ActivityCompat.startActivity(SingleBeerActivity.this, intent, options.toBundle());
            }
        });

        getReviews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getReviews() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        JSONArray reviews;
        boolean hasReviews = false;
        try {
            if ((reviews = getJsonArray(MainActivity.prefs, DataManager.REVIEWKEY)) != null) {
                for (int i = 0; i < reviews.length(); i++) {
                    JSONObject jsonObject = reviews.getJSONObject(i);
                    Review review = gson.fromJson(jsonObject.toString(), Review.class);
                    if (review.getBeerID() == id) {
                        hasReviews = true;
                        if (review.getUserID() == getOwnUser(MainActivity.prefs).getUserID()) {
                            reviewButton.setVisibility(View.GONE);
                        }
                        insertReview(review);
                    }
                }
                if (!hasReviews) {
                    reviewViewGroup.removeAllViews();
                }
            }
        } catch (JSONException e) {
            Toast.makeText(this, getString(R.string.snackbar_reviewloaderror), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the user clicks the button to create a new beerreview,
     * starts NewBeerActivity.
     */
    public void createReview(View view) {
        Intent intent = new Intent(this, NewBeerReviewActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("name", name);

        int requestCode = 1;
        startActivityForResult(intent, requestCode);
    }

    private void insertReview(Review review) {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.review_row, null);
        View divider = inflater.inflate(R.layout.divider, null);

        TextView title = (TextView) view.findViewById(R.id.review_title);
        TextView body = (TextView) view.findViewById(R.id.review_body);
        TextView date = (TextView) view.findViewById(R.id.review_date);
        TextView ratingTV = (TextView) view.findViewById(R.id.review_rating);

        String name = null;
        try {
            name = getUser(MainActivity.prefs, review.getUserID()).getName();
        } catch (NullPointerException ignored) {
        }

        Date datum = review.getProefdatum();

        title.setText(String.format("%s: ", name));
        body.setText(review.getDescription());
        date.setText(MainActivity.appDF.format(datum));
        ratingTV.setText(String.format("Cijfer: %s", review.getRating()));

        // Insert into view
        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.review_insert_point);
        if (insertPoint != null) {
            insertPoint.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            insertPoint.addView(divider);
        }
    }

    private void refreshActivity() {
        finish();
        startActivity(getIntent());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        refreshActivity();
    }

    private String parseDate(String dateTemp) throws ParseException {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", MainActivity.locale);
        DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", MainActivity.locale);
        return outputFormat.format(inputFormat.parse(dateTemp));
    }

    private void fillRow(View view, final String title, final String description) {
        TextView titleView = (TextView) view.findViewById(R.id.row_title);
        titleView.setText(title);

        TextView descriptionView = (TextView) view.findViewById(R.id.row_description);
        descriptionView.setText(description);
    }
}
