package nl.ecci.hamers.users;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import nl.ecci.hamers.MainActivity;
import nl.ecci.hamers.R;
import nl.ecci.hamers.helpers.DataManager;
import uk.co.senab.photoview.PhotoViewAttacher;

import static nl.ecci.hamers.helpers.Utils.convertNicknames;
import static nl.ecci.hamers.helpers.Utils.getGravatarURL;

public class SingleUserActivity extends AppCompatActivity {
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail);

        initToolbar();

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        final User user = DataManager.getUser(MainActivity.prefs, getIntent().getIntExtra(User.USER_ID, -1));

        collapsingToolbar.setTitle(user.getName());

        loadBackdrop(user);

        fillRow(findViewById(R.id.row_user_name), getString(R.string.user_name), user.getName());
        fillRow(findViewById(R.id.row_user_quotecount), getString(R.string.user_quotecount), String.valueOf(user.getQuoteCount()));
        fillRow(findViewById(R.id.row_user_reviewcount), getString(R.string.user_reviewcount), String.valueOf(user.getReviewCount()));
        fillRow(findViewById(R.id.row_user_batch), getString(R.string.user_batch), String.valueOf(user.getBatch()));

        View nicknameRow = findViewById(R.id.row_user_nickname);
        View nicknameDivider = findViewById(R.id.user_nickname_divider);
        if (user.getNicknames().size() > 0) {
            fillRow(nicknameRow, getString(R.string.user_nickname), convertNicknames(user.getNicknames()));
        } else if (nicknameRow != null && nicknameDivider != null) {
            nicknameRow.setVisibility(View.GONE);
            nicknameDivider.setVisibility(View.GONE);
        }

        if (user.getMember() == User.Member.LID) {
            fillRow(findViewById(R.id.row_user_status), getString(R.string.user_status), getString(R.string.user_member));
        } else {
            fillRow(findViewById(R.id.row_user_status), getString(R.string.user_status), getString(R.string.user_member_ex));
        }

        View emailRow = findViewById(R.id.row_user_email);
        if (emailRow != null) {
            fillRow(emailRow, getString(R.string.user_email), user.getEmail());
            emailRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{user.getEmail()});
                    startActivity(intent);
                }
            });
        }
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void loadBackdrop(User user) {
        final ImageView imageView = (ImageView) findViewById(R.id.user_backdrop);

        mAttacher = new PhotoViewAttacher(imageView);

        ImageLoader.getInstance().displayImage(getGravatarURL(user.getEmail()), imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (mAttacher != null) {
                    mAttacher.update();
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
    }

    private void fillRow(View view, @NonNull final String title, @NonNull final String description) {
        TextView titleView = (TextView) view.findViewById(R.id.row_title);
        titleView.setText(title);

        TextView descriptionView = (TextView) view.findViewById(R.id.row_description);
        descriptionView.setText(description);
    }
}
