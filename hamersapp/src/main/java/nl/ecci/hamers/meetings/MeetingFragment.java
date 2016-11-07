package nl.ecci.hamers.meetings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import nl.ecci.hamers.MainActivity;
import nl.ecci.hamers.R;
import nl.ecci.hamers.loader.GetCallback;
import nl.ecci.hamers.loader.Loader;

import static nl.ecci.hamers.MainActivity.prefs;

public class MeetingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final ArrayList<Meeting> dataSet = new ArrayList<>();
    private MeetingAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.meeting_fragment, container, false);
        RecyclerView meeting_list = (RecyclerView) view.findViewById(R.id.meeting_recyclerview);

        setHasOptionsMenu(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        meeting_list.setLayoutManager(layoutManager);

        initSwiper(view, meeting_list, layoutManager);

        adapter = new MeetingAdapter(dataSet, getActivity());
        meeting_list.setAdapter(adapter);

        onRefresh();

        return view;
    }

    private void initSwiper(View view, final RecyclerView meeting_list, final LinearLayoutManager lm) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.meeting_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light);

        swipeRefreshLayout.setOnRefreshListener(this);

        meeting_list.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                boolean enable = false;
                if (meeting_list != null && meeting_list.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = lm.findFirstCompletelyVisibleItemPosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = meeting_list.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });
    }

    @Override
    public void onRefresh() {
        setRefreshing(true);
        Loader.getData(Loader.MEETINGURL, getContext(), MainActivity.prefs, new GetCallback() {
            @Override
            public void onSuccess(String response) {
                new populateList().execute(response);
            }

            @Override
            public void onError(VolleyError error) {
                // Nothing
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
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

    private class populateList extends AsyncTask<String, Void, ArrayList<Meeting>> {
        @Override
        protected final ArrayList<Meeting> doInBackground(String... params) {
            ArrayList<Meeting> result;
            Type type = new TypeToken<ArrayList<Meeting>>() {
            }.getType();
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat(MainActivity.dbDF.toPattern());
            Gson gson = gsonBuilder.create();

            if (params.length > 0) {
                result = gson.fromJson(params[0], type);
            } else {
                result = gson.fromJson(prefs.getString(Loader.MEETINGURL, null), type);
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Meeting> result) {
            if (!result.isEmpty()) {
                dataSet.clear();
                dataSet.addAll(result);
                if (MeetingFragment.this.adapter != null) {
                    MeetingFragment.this.adapter.notifyDataSetChanged();
                }
            }
            setRefreshing(false);
        }
    }
}