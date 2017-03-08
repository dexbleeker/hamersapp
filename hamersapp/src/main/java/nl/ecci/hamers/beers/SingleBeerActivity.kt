package nl.ecci.hamers.beers

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.*
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.beer_detail.*
import kotlinx.android.synthetic.main.review_row.view.*
import nl.ecci.hamers.MainActivity
import nl.ecci.hamers.R
import nl.ecci.hamers.helpers.HamersActivity
import nl.ecci.hamers.helpers.SingleImageActivity
import nl.ecci.hamers.helpers.Utils
import nl.ecci.hamers.loader.Loader
import java.util.*

class SingleBeerActivity : HamersActivity() {

    private var beer: Beer? = null
    private var gson: Gson? = null
    private var ownReview: Review? = null

    // Activity for result
    internal var reviewRequestCode = 1
    internal var beerRequestCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.beer_detail)

        initToolbar()

        val gsonBuilder = GsonBuilder()
        gsonBuilder.setDateFormat(MainActivity.dbDF.toPattern())
        gson = gsonBuilder.create()

        review_create_button.setOnClickListener { updateReview(ownReview) }

        beer = Utils.getBeer(prefs!!, intent.getIntExtra(Beer.BEER, -1))

        setValues()

        ImageLoader.getInstance().displayImage(beer!!.imageURL, beer_image)

        beer_image.setOnClickListener {
            val intent = Intent(this@SingleBeerActivity, SingleImageActivity::class.java)
            val transitionName = getString(R.string.transition_single_image)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@SingleBeerActivity, beer_image, transitionName)
            intent.putExtra(Beer.BEER, gson!!.toJson(beer, Beer::class.java))
            ActivityCompat.startActivity(this@SingleBeerActivity, intent, options.toBundle())
        }

        getReviews()
    }

    private fun setValues() {
        fillDetailRow(row_kind, getString(R.string.beer_soort), beer!!.kind)
        fillDetailRow(row_alc, getString(R.string.beer_alc), beer!!.percentage)
        fillDetailRow(row_brewer, getString(R.string.beer_brewer), beer!!.brewer)
        fillDetailRow(row_country, getString(R.string.beer_country), beer!!.country)

        beer_name.text = beer!!.name

        if (beer!!.rating == null) {
            fillDetailRow(row_rating, getString(R.string.beer_rating), "Nog niet bekend")
        } else {
            fillDetailRow(row_rating, getString(R.string.beer_rating), beer!!.rating)
        }
    }

    private fun getReviews() {
        val reviewList: ArrayList<Review>
        val type = object : TypeToken<ArrayList<Review>>() {
        }.type

        var hasReviews = false
        reviewList = GsonBuilder().create().fromJson<ArrayList<Review>>(prefs!!.getString(Loader.REVIEWURL, null), type)

        for (review in reviewList) {
            if (review.beerID == beer!!.id) {
                hasReviews = true
                if (review.userID == Utils.getOwnUser(prefs!!).id) {
                    review_create_button.setText(R.string.edit_review)
                    ownReview = review
                }
                insertReview(review)
            }
        }

        if (!hasReviews) {
            review_insert_point.removeAllViews()
        }
    }

    /**
     * Called when the user clicks the button to create a new beer review,
     * starts NewBeerActivity.
     */
    private fun updateReview(review: Review?) {
        val intent = Intent(this, NewReviewActivity::class.java)
        intent.putExtra(Beer.BEER, beer!!.id)

        if (review != null) {
            intent.putExtra(Review.REVIEW, gson!!.toJson(review, Review::class.java))
        }

        startActivityForResult(intent, reviewRequestCode)
    }

    private fun insertReview(review: Review) {
        val view = layoutInflater.inflate(R.layout.review_row, review_insert_point, false)
        val divider = layoutInflater.inflate(R.layout.divider, review_insert_point, false)

        view.review_title.text = String.format("%s: ", Utils.getUser(prefs, review.userID).name)
        view.review_body.text = review.description
        view.review_date.text = MainActivity.appDF2.format(review.proefdatum)
        view.review_rating.text = String.format("Cijfer: %s", review.rating)

        // Insert into view
        review_insert_point.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        review_insert_point.addView(divider)
        if (Utils.getOwnUser(prefs!!).id == review.userID) {
            registerForContextMenu(view)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.review_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.review_update -> {
                updateReview(ownReview)
                return true
            }
            R.id.review_delete -> {
                deleteReview()
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_item -> {
                val intent = Intent(this, NewBeerActivity::class.java)
                if (beer != null) {
                    intent.putExtra(Beer.BEER, beer!!.id)
                }
                startActivityForResult(intent, beerRequestCode)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteReview() {
        AlertDialog.Builder(this@SingleBeerActivity)
                .setTitle(getString(R.string.review_delete))
                .setMessage(getString(R.string.review_delete_message))
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    // continue with delete
                }
                .setNegativeButton(android.R.string.no) { _, _ ->
                    // do nothing
                }
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == reviewRequestCode) {
                val newBody = data?.getStringExtra(reviewBody)
                val newRating = data?.getIntExtra(reviewRating, -1)

                if (ownReview != null) {
                    for (i in 0..review_insert_point.childCount - 1) {
                        val view = review_insert_point.getChildAt(i)
                        val bodyTextView = view.findViewById(R.id.review_body) as TextView
                        val ratingTextView = view.findViewById(R.id.review_rating) as TextView
                        if (bodyTextView.text === ownReview!!.description) {
                            bodyTextView.text = newBody
                            ratingTextView.text = String.format("Cijfer: %s", newRating)
                        }
                    }
                }
            } else if (requestCode == beerRequestCode) {
                beer!!.name = data?.getStringExtra(beerName)
                beer!!.kind = data?.getStringExtra(beerKind)
                beer!!.percentage = data?.getStringExtra(beerPercentage)
                beer!!.percentage = data?.getStringExtra(beerPercentage)
                beer!!.brewer = data?.getStringExtra(beerBrewer)
                beer!!.country = data?.getStringExtra(beerCountry)
                beer!!.rating = beer!!.rating!! + " (Nog niet bijgewerkt)"

                setValues()
            }
        }
    }

    companion object {
        val reviewRating = "reviewRating"
        val reviewBody = "reviewBody"
        val beerName = "beerName"
        val beerKind = "beerKind"
        val beerPercentage = "beerPercentage"
        val beerBrewer = "beerBrewer"
        val beerCountry = "beerCountry"
    }
}