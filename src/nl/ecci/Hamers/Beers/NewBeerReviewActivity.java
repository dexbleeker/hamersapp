package nl.ecci.Hamers.Beers;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import nl.ecci.Hamers.Helpers.Fragments.DatePickerFragment;
import nl.ecci.Hamers.R;

public class NewBeerReviewActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener {
    private SeekBar sb;
    private TextView progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_beer_review_activity);

        // Seekbar
        sb = (SeekBar) findViewById(R.id.ratingseekbar);
        sb.setOnSeekBarChangeListener(this);
        progress = (TextView) findViewById(R.id.rating);

        getSupportActionBar().setHomeButtonEnabled(true);
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

    public void showDatePickerDialog(View v) {
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), "proefdatum");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int rating, boolean fromUser) {
        // change progress text label with current seekbar value
        progress.setText("Cijfer: " + (rating + 1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
/**
    private void postReview(int user_id, int beer_id, String description, int rating, int proefdag, int proefmaand, int proefjaar) {
        String arguments = "beer[user_id]=" +user_id + "&beer[beer_id]=" + beer_id + "&beer[description]=" + description + "&beer[rating]=" + rating
                + "&beer[proefdatum(1i)]=" + proefjaar + "&beer[proefdatum(2i)]=" + proefmaand + "&beer[proefdatum(3i)]=" + proefdag + "&beer[proefdatum(4i)]=" + 20 + "&beer[proefdatum(5i)]=" + 00;
        SendPostRequest req = new SendPostRequest(this, SendPostRequest.REVIEWURL, PreferenceManager.getDefaultSharedPreferences(this), arguments);
        req.execute();
    }
*/
}