package nl.ecci.hamers.users;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.ecci.hamers.MainActivity;
import nl.ecci.hamers.R;
import nl.ecci.hamers.helpers.DataManager;

public class UserListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private final ArrayList<User> dataSet = new ArrayList<>();
    private ArrayAdapter<User> adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean exUser;

    public UserListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_list_fragment, container, false);
        ListView user_list = (ListView) view.findViewById(R.id.users_listView);

        setHasOptionsMenu(true);

        adapter = new UserListAdapter(this.getActivity(), dataSet);
        user_list.setAdapter(adapter);

        initSwiper(view, user_list);

        exUser = getArguments().getBoolean(UserFragmentPagerAdapter.exUser, false);

        sort();

        onRefresh();

        return view;
    }

    private void initSwiper(View view, final ListView user_list) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.users_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light);

        swipeRefreshLayout.setOnRefreshListener(this);

        user_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (user_list != null && user_list.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = user_list.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = user_list.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.user_menu, menu);
    }

    @Override
    public void onRefresh() {
        setRefreshing(true);
        DataManager.getData(getContext(), MainActivity.prefs, DataManager.USERURL, DataManager.USERKEY);
    }

    public void populateList() {
        JSONArray json;
        try {
            if ((json = DataManager.getJsonArray(MainActivity.prefs, DataManager.USERKEY)) != null) {
                dataSet.clear();
                for (int i = 0; i < json.length(); i++) {
                    JSONObject temp;
                    temp = json.getJSONObject(i);
                    User user = new User(temp.getString("name"), temp.getInt("id"), temp.getString("email"), temp.getInt("quotes"), temp.getInt("reviews"), temp.getBoolean("lid"), temp.getString("nickname="));

                    if (exUser && !user.isMember()) {
                        dataSet.add(user);
                    } else if (!exUser && user.isMember()) {
                        dataSet.add(user);
                    }

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        } catch (JSONException e) {
            Toast.makeText(getActivity(), getString(R.string.snackbar_loaderror), Toast.LENGTH_SHORT).show();
        }
        setRefreshing(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_username:
                sortByUsername();
                return true;
            case R.id.sort_quotes:
                sortbyQuoteCount();
                return true;
            case R.id.sort_reviews:
                sortbyReviewCount();
                return true;
            default:
                return false;
        }
    }

    private void sort() {
        String sortPref = MainActivity.prefs.getString("userSort", "");
        switch (sortPref) {
            case "name":
                sortByUsername();
                break;
            case "quotecount":
                sortbyQuoteCount();
                break;
            case "reviewcount":
                sortbyReviewCount();
                break;
        }
    }

    private void setRefreshing(final Boolean bool) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(bool);
                }
            });
        }
    }

    private void sortByUsername() {
        final Comparator<User> nameComperator = new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getName().compareToIgnoreCase(user2.getName());
            }
        };
        Collections.sort(dataSet, nameComperator);
        adapter.notifyDataSetChanged();
    }

    private void sortbyQuoteCount() {
        final Comparator<User> quoteComperator = new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user2.getQuotecount() - user1.getQuotecount();
            }
        };
        Collections.sort(dataSet, quoteComperator);
        adapter.notifyDataSetChanged();
    }

    private void sortbyReviewCount() {
        final Comparator<User> reviewComperator = new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user2.getReviewcount() - user1.getReviewcount();
            }
        };
        Collections.sort(dataSet, reviewComperator);
        adapter.notifyDataSetChanged();
    }
}