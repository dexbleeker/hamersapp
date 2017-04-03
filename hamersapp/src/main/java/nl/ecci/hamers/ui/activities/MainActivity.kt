package nl.ecci.hamers.ui.activities

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatDelegate
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.element_header.view.*
import kotlinx.android.synthetic.main.element_toolbar.*
import kotlinx.android.synthetic.main.main.*
import nl.ecci.hamers.R
import nl.ecci.hamers.data.GetCallback
import nl.ecci.hamers.data.Loader
import nl.ecci.hamers.ui.fragments.*
import nl.ecci.hamers.utils.DataUtils
import nl.ecci.hamers.utils.DataUtils.getGravatarURL
import nl.ecci.hamers.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : HamersActivity() {
    private var backPressedOnce: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        initDrawer()
        initToolbar()

        if (savedInstanceState == null) {
            selectItem(this, navigation_view.menu.getItem(0))
            val night_mode = prefs?.getString("night_mode", "off")
            AppCompatDelegate.setDefaultNightMode(getNightModeInt(night_mode.toString()))
            recreate()
        }

        checkPlayServices()

        DataUtils.hasApiKey(this, prefs)

        fillHeader()
    }

    private fun initDrawer() {
        navigation_view!!.setNavigationItemSelectedListener { menuItem ->
            selectItem(this@MainActivity, menuItem)
            menuItem.isChecked = true
            drawer_layout!!.closeDrawers()
            Utils.hideKeyboard(parent)
            true
        }
    }

    override fun initToolbar() {
        super.initToolbar()
        val mDrawerToggle = ActionBarDrawerToggle(
                this,
                drawer_layout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        )
        drawer_layout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private fun checkPlayServices(): Boolean {
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show()
            } else {
                GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
            }
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    override fun onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed()
            return
        }

        this.backPressedOnce = true
        Utils.showToast(this, getString(R.string.press_back_again), Toast.LENGTH_SHORT)

        Handler().postDelayed({ backPressedOnce = false }, 2000)
    }

    private fun fillHeader() {
        val user = DataUtils.getOwnUser(this)
        if (user.id != Utils.notFound) {

            val headerView = LayoutInflater.from(this).inflate(R.layout.element_header, navigation_view, false)
            navigation_view.addHeaderView(headerView)

            headerView.header_user_name.text = user.name
            headerView.header_user_email.text = user.email

            // Image
            Glide.with(applicationContext).load(getGravatarURL(user.email)).into(headerView.header_user_image)
        } else {
            Loader.getData(this, Loader.WHOAMIURL, object : GetCallback {
                override fun onSuccess(response: String) {
                    fillHeader()
                }
            }, null)
        }
    }

    companion object {
        val locale = Locale("nl")
        val dbDF = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", locale)
        val appDF = SimpleDateFormat("EEE dd MMM yyyy HH:mm", locale)
        val appDTF = SimpleDateFormat("EEEE dd MMMM yyyy", locale)
        private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

        @AppCompatDelegate.NightMode
        fun getNightModeInt(nightMode: String): Int {
            when (nightMode) {
                "auto" -> return AppCompatDelegate.MODE_NIGHT_AUTO
                "on" -> return AppCompatDelegate.MODE_NIGHT_YES
                else -> return AppCompatDelegate.MODE_NIGHT_NO
            }
        }

        /**
         * Swaps fragments in the main content view
         * This method is static to allow for usage in ChangeFragment
         */
        fun selectItem(activity: HamersActivity, menuItem: MenuItem) {
            var fragment: Fragment? = null
            val fragmentClass: Class<*>

            when (menuItem.itemId) {
                R.id.navigation_item_events -> fragmentClass = EventFragment::class.java
                R.id.navigation_item_beers -> fragmentClass = BeerFragment::class.java
                R.id.navigation_item_news -> fragmentClass = NewsFragment::class.java
                R.id.navigation_item_users -> fragmentClass = UserFragment::class.java
                R.id.navigation_item_meetings -> fragmentClass = MeetingFragment::class.java
                R.id.navigation_item_stickers -> fragmentClass = StickerFragment::class.java
                R.id.navigation_item_settings -> fragmentClass = SettingsFragment::class.java
                R.id.navigation_item_changes -> fragmentClass = ChangeFragment::class.java
                R.id.navigation_item_about -> fragmentClass = AboutFragment::class.java
                else -> fragmentClass = QuoteFragment::class.java
            }

            try {
                fragment = fragmentClass.newInstance() as Fragment
            } catch (ignored: Exception) {
            }

            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction.replace(R.id.content_frame, fragment).commit()
        }
    }
}