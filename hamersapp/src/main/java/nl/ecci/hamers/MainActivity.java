package nl.ecci.hamers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nl.ecci.hamers.beers.BeerFragment;
import nl.ecci.hamers.changes.ChangeFragment;
import nl.ecci.hamers.events.EventFragment;
import nl.ecci.hamers.helpers.DataUtils;
import nl.ecci.hamers.helpers.HamersActivity;
import nl.ecci.hamers.helpers.Utils;
import nl.ecci.hamers.loader.GetCallback;
import nl.ecci.hamers.loader.Loader;
import nl.ecci.hamers.meetings.MeetingFragment;
import nl.ecci.hamers.news.NewsFragment;
import nl.ecci.hamers.quotes.QuoteFragment;
import nl.ecci.hamers.stickers.StickerFragment;
import nl.ecci.hamers.users.User;
import nl.ecci.hamers.users.UserFragment;

public class MainActivity extends HamersActivity {
    public static final Locale locale = new Locale("nl");
    public static final SimpleDateFormat dbDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", locale);
    public static final SimpleDateFormat appDF = new SimpleDateFormat("EEE dd MMM yyyy HH:mm", locale);
    public static final SimpleDateFormat appDTF = new SimpleDateFormat("EEEE dd MMMM yyyy", locale);
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static SharedPreferences prefs;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean backPressedOnce;

    /**
     * Setup of default ImageLoader configuration (Universal Image Loader)
     * https://github.com/nostra13/Android-Universal-Image-Loader
     */
    private static void configureDefaultImageLoader(Context context) {
        File cacheDir = StorageUtils.getCacheDirectory(context);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoaderConfiguration defaultConfiguration
                = new ImageLoaderConfiguration.Builder(context)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .defaultDisplayImageOptions(options)
                .build();

        // Initialize ImageLoader with configuration
        ImageLoader.getInstance().init(defaultConfiguration);
    }

    /**
     * Parse date
     */
    public static Date parseDate(String dateString) {
        Date date = null;
        try {
            // Event date
            if (!dateString.equals("null")) {
                DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", MainActivity.locale);
                date = inputFormat.parse(dateString);
            }
        } catch (ParseException ignored) {
        }
        return date;
    }

    @AppCompatDelegate.NightMode
    public static int getNightModeInt(String nightMode) {
        switch (nightMode) {
            case "auto":
                return AppCompatDelegate.MODE_NIGHT_AUTO;
            case "on":
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_NO;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initDrawer();
        initToolbar();

        configureDefaultImageLoader(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState == null) {
            selectItem(this, navigationView.getMenu().getItem(0));
            String night_mode = prefs.getString("night_mode", "off");
            AppCompatDelegate.setDefaultNightMode(getNightModeInt(night_mode));
            recreate();
        }

        checkPlayServices();

        DataUtils.INSTANCE.hasApiKey(this, prefs);

        fillHeader();

        Loader.INSTANCE.getAllData(this);
    }

    private void initDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selectItem(MainActivity.this, menuItem);
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                Utils.INSTANCE.hideKeyboard(getParent());
                return true;
            }
        });
    }

    @Override
    public void initToolbar() {
        super.initToolbar();
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                getToolbar(),
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    /**
     * Swaps fragments in the main content view
     */
    public static void selectItem(HamersActivity activity, MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass;

        switch (menuItem.getItemId()) {
            case R.id.navigation_item_events:
                fragmentClass = EventFragment.class;
                break;
            case R.id.navigation_item_beers:
                fragmentClass = BeerFragment.class;
                break;
            case R.id.navigation_item_news:
                fragmentClass = NewsFragment.class;
                break;
            case R.id.navigation_item_users:
                fragmentClass = UserFragment.class;
                break;
            case R.id.navigation_item_meetings:
                fragmentClass = MeetingFragment.class;
                break;
            case R.id.navigation_item_stickers:
                fragmentClass = StickerFragment.class;
                break;
            case R.id.navigation_item_settings:
                fragmentClass = SettingsFragment.class;
                break;
            case R.id.navigation_item_changes:
                fragmentClass = ChangeFragment.class;
                break;
            case R.id.navigation_item_about:
                fragmentClass = AboutFragment.class;
                break;
            default:
                fragmentClass = QuoteFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception ignored) {
        }

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.backPressedOnce = true;
        Utils.INSTANCE.showToast(this, getString(R.string.press_back_again), Toast.LENGTH_SHORT);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressedOnce = false;
            }
        }, 2000);
    }

    private void fillHeader() {
        User user = DataUtils.INSTANCE.getOwnUser(this);
        if (user.getId() != Utils.INSTANCE.getNotFound()) {
            View headerLayout = navigationView.getHeaderView(0);
            TextView userName = (TextView) headerLayout.findViewById(R.id.header_user_name);
            TextView userEmail = (TextView) headerLayout.findViewById(R.id.header_user_email);
            ImageView userImage = (ImageView) headerLayout.findViewById(R.id.header_user_image);

            if (userName != null && userEmail != null && userImage != null) {
                userName.setText(user.getName());
                userEmail.setText(user.getEmail());

                // Image
                String url = DataUtils.INSTANCE.getGravatarURL(user.getEmail());
                ImageLoader.getInstance().displayImage(url, userImage);
            }
        } else {
            Loader.INSTANCE.getData(this, Loader.INSTANCE.getWHOAMIURL(), new GetCallback() {
                @Override
                public void onSuccess(@NonNull String response) {
                    fillHeader();
                }
            }, null);
        }
    }
}
