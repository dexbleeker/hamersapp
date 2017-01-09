package nl.ecci.hamers.stickers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import nl.ecci.hamers.R;
import nl.ecci.hamers.loader.GetCallback;
import nl.ecci.hamers.loader.Loader;

import static nl.ecci.hamers.MainActivity.prefs;

public class StickerFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap map;
    private final ArrayList<Sticker> dataSet = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sticker_fragment, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) view.findViewById(R.id.stickers_map);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
        addMarkers();
    }

    private void onRefresh() {
        Loader.getData(getContext(), Loader.STICKERURL, new GetCallback() {
            @Override
            // TODO
            public void onSuccess(String response) {
                new populateMap().execute(dataSet);
            }

            @Override
            public void onError(VolleyError error) {
                // Nothing
            }
        }, null);
    }

    private void addMarkers() {
        if (map != null && !dataSet.isEmpty()) {
            for (Sticker sticker : dataSet) {
                map.addMarker(new MarkerOptions().position(new LatLng(sticker.getLat(), sticker.getLon())));
            }
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private class populateMap extends AsyncTask<ArrayList<Sticker>, Void, ArrayList<Sticker>> {
        @SafeVarargs
        @Override
        protected final ArrayList<Sticker> doInBackground(ArrayList<Sticker>... param) {
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<ArrayList<Sticker>>() {
            }.getType();

            return gson.fromJson(prefs.getString(Loader.STICKERURL, null), type);
        }

        @Override
        protected void onPostExecute(ArrayList<Sticker> result) {
            if (!result.isEmpty()) {
                dataSet.clear();
                dataSet.addAll(result);
                addMarkers();
            }
        }
    }
}
