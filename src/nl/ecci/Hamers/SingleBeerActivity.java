package nl.ecci.Hamers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

public class SingleBeerActivity extends ActionBarActivity {
    String name;
    String soort;
    String percentage;
    String brewer;
    String country;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.single_beer_item);

        TextView nameTV = (TextView) findViewById(R.id.beer_name);
        TextView soortTV = (TextView) findViewById(R.id.beer_soort);
        TextView percentageTV = (TextView) findViewById(R.id.beer_alc);
        TextView brewerTV = (TextView) findViewById(R.id.beer_brewer);
        TextView countryTV = (TextView) findViewById(R.id.beer_country);

        Bundle extras = getIntent().getExtras();

        name = extras.getString("name");
        soort = extras.getString("soort");
        percentage = extras.getString("percentage");
        brewer = extras.getString("brewer");
        country = extras.getString("country");


        nameTV.setText(name);
        soortTV.setText(soort);
        percentageTV.setText(percentage);
        brewerTV.setText(brewer);
        countryTV.setText(country);
    }

    /**
     * Called when the user clicks the button to create a new beerreview,
     * starts NewBeerActivity.
     * @param view
     */
    public void createReview(View view) {
        Intent intent = new Intent(this, NewBeerReviewActivity.class);
        startActivity(intent);
    }
}
