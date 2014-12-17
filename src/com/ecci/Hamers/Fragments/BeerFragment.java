package com.ecci.Hamers.Fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.ecci.Hamers.GetJson;
import com.ecci.Hamers.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BeerFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public BeerFragment() {
        // Empty constructor required for fragment subclasses
    }

    ArrayList<String> listItems =new ArrayList<String>();
    ArrayAdapter<String> adapter;
    SwipeRefreshLayout swipeView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beer_fragment, container, false);
        ListView beer_list = (ListView) view.findViewById(R.id.beer_listView);

        initSwiper(view, beer_list);

        adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, listItems);
        beer_list.setAdapter(adapter); //Set adapter and that's it.

        return view;
    }

    public void initSwiper(View view, ListView beer_list) {
        swipeView = (SwipeRefreshLayout) view.findViewById(R.id.beer_swipe_container);
        swipeView.setOnRefreshListener(this);
        swipeView.setColorSchemeResources(android.R.color.holo_red_light);

        swipeView.setOnRefreshListener(this);

        beer_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    swipeView.setEnabled(true);
                else
                    swipeView.setEnabled(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        GetJson g = new GetJson(this, GetJson.BEER, PreferenceManager.getDefaultSharedPreferences(this.getActivity()));
        g.execute();
    }

    public void populateList(JSONArray json){
        System.out.println(json);

        listItems.clear();
        for(int i = 0; i< json.length(); i++){
            JSONObject temp;
            try {
                temp = json.getJSONObject(i);
                listItems.add(temp.getString("name"));
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        swipeView.setRefreshing(false);
    }
}