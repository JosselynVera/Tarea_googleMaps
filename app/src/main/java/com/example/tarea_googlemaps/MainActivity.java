package com.example.tarea_googlemaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener{
    private RequestQueue requestQueue;
    private GoogleMap mMap;
    private LatLng defaultLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        mMap.clear();

        agregarMarcador(latLng, "Ubicación seleccionada");

        defaultLocation = latLng;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        defaultLocation = new LatLng(-1.0123939432800628, -79.4695139058818);
        CameraUpdate camUpd1 = CameraUpdateFactory.newLatLngZoom(defaultLocation, 17);
        mMap.moveCamera(camUpd1);

        agregarMarcador(defaultLocation, "Universidad Tecnica Estatal de Quevedo");
        mMap.setOnMapClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
    }

    private void agregarMarcador(LatLng position, String placeId) {
        mMap.addMarker(new MarkerOptions().position(position).title(placeId));
    }

    private void obtenerLugares(LatLng latLng, double radius, String lugar) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?fields=name,place_id" +
                "&location=" + latLng.latitude + "," + latLng.longitude +
                "&radius=" + radius +
                "&type=" + lugar +
                "&key=AIzaSyCaLDRuW2PSlE4s9_ff3kEStPETEbuqWCE";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");

                            for (int i = 0; i < results.length(); i++) {
                                String nombreLugar = results.getJSONObject(i).getString("name");
                                String placeId = results.getJSONObject(i).getString("place_id");
                                JSONObject ubicacion = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location");
                                double latitud = ubicacion.getDouble("lat");
                                double longitud = ubicacion.getDouble("lng");

                                LatLng lugarLatLng = new LatLng(latitud, longitud);

                                agregarMarcador(lugarLatLng, placeId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error al obtener lugares", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
    public void clickRestaurant(View view) {
        mMap.clear();
        agregarMarcador(defaultLocation, "Universidad Tecnica Estatal de Quevedo");
        Toast.makeText(this, "Cargando restaurantes", Toast.LENGTH_SHORT).show();
        obtenerLugares(defaultLocation, 1500, "restaurant");
    }
    public void clickHoteles(View view){
        mMap.clear();
        agregarMarcador(defaultLocation, "Universidad Tecnica Estatal de Quevedo");
        Toast.makeText(this, "Cargando Hoteles", Toast.LENGTH_SHORT).show();
        obtenerLugares(defaultLocation, 1500,"lodging");
    }

    public void clickShopping(View view){
        mMap.clear();
        agregarMarcador(defaultLocation, "Universidad Tecnica Estatal de Quevedo");
        Toast.makeText(this, "Cargando Tiendas", Toast.LENGTH_SHORT).show();
        obtenerLugares(defaultLocation, 1000,"shopping");
    }

    private void obtenerDetallesLugar(String placeId, TextView tvTitle, TextView tvLocation, ImageView ivLogo, TextView tvHours) {
        String url = "https://maps.googleapis.com/maps/api/place/details/json" +
                "?fields=name,rating,formatted_phone_number,geometry,icon,opening_hours" +
                "&place_id=" + placeId +
                "&key=AIzaSyCaLDRuW2PSlE4s9_ff3kEStPETEbuqWCE";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            String nombre = result.getString("name");
                            JSONObject ubicacion = result.getJSONObject("geometry").getJSONObject("location");
                            double latitud = ubicacion.getDouble("lat");
                            double longitud = ubicacion.getDouble("lng");
                            String logoUrl = result.optString("icon", "");
                            String horarios = result.optJSONObject("opening_hours").getString("weekday_text");

                            tvTitle.setText(nombre);
                            tvLocation.setText("Ubicación: " + latitud + ", " + longitud);
                            if (!logoUrl.isEmpty()) {
                                Picasso.get().load(logoUrl).into(ivLogo);
                            }
                            tvHours.setText("Horarios: " + horarios);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    class CustomInfoWindowAdapter implements InfoWindowAdapter {
        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView tvTitle = mWindow.findViewById(R.id.title);
            TextView tvLocation = mWindow.findViewById(R.id.location);
            ImageView ivLogo = mWindow.findViewById(R.id.logo);
            TextView tvHours = mWindow.findViewById(R.id.hours);

            String placeId = marker.getTitle();

            obtenerDetallesLugar(placeId, tvTitle, tvLocation, ivLogo, tvHours);

            return mWindow;
        }
    }
}