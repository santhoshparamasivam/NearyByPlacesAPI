package com.example.placesapi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.placesapi.ApiManager;
import com.example.placesapi.LocationShareAdapter;
import com.example.placesapi.PlacesModel;
import com.example.placesapi.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {


    RecyclerView recyclerView;
    LocationManager manager1;
        GoogleMap  mMap;
    List<HashMap<String, String>> jsonPlaces;
    String strAccuracy;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        recyclerView = findViewById(R.id.recyclerview);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);



        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LocationActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        manager1 = (LocationManager) LocationActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager1.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else {
            fetchLocation();
        }

    }



    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                       finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }


    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(LocationActivity.this);
                    onLocationSearch(String.valueOf(currentLocation.getLatitude()),String.valueOf(currentLocation.getLongitude()));
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
        googleMap.addMarker(markerOptions);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                }else {
                    String details = "To share your location, you need to enable GPS permissions for this app. The following screen will ask for permission, please approve.?";
                    AlertBoxMethod(details);
                }
                break;
        }
    }

    private void AlertBoxMethod(String details) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(details)
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fetchLocation();
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }





    public void onLocationSearch(String lat,String lng){
        Log.e("lat and lon",lat+" "+lng);
        if(!TextUtils.isEmpty(lat) || !TextUtils.isEmpty(lng)){
            String location = lat+","+lng;
            String radius = "4000";
            String types = "point_of_interest";
            String key = "AIzaSyA0gwJELfYavI_pC_PGzOUs57MCKsjzwxw";

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiManager api = retrofit.create(ApiManager.class);
            Call<ResponseBody> call = api.nearPlaceSearch(location,radius,types,"true",key);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        Log.e("jsonPlaces",response.code()+"  ");
                        String successResponse = response.body().string();
                        PlacesModel placesJson = new PlacesModel();
                        JSONObject jObject = new JSONObject(successResponse);
                        Log.e("jObject",jObject.toString());
                        List<HashMap<String, String>> places = placesJson.parse(jObject);
                        jsonPlaces = places;
                        Log.e("jsonPlaces",jsonPlaces.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < jsonPlaces.size(); i++) {
                                    // Creating a marker
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    // Getting a place from the places list
                                    HashMap<String, String> hmPlace = jsonPlaces.get(i);
                                    // Getting latitude of the place
                                    double lat = Double.parseDouble(hmPlace.get("lat"));
                                    // Getting longitude of the place
                                    double lng = Double.parseDouble(hmPlace.get("lng"));
                                    // Getting name
                                    String name = hmPlace.get("place_name");
                                    Log.d("Map", "place: " + name);
                                    // Getting vicinity
                                    String vicinity = hmPlace.get("vicinity");
                                    LatLng latLng = new LatLng(lat, lng);
                                    // Setting the position for the marker
                                    markerOptions.position(latLng);
                                    markerOptions.title(name + " : " + vicinity);
                                    markerOptions.icon(bitmapDescriptorFromVector(LocationActivity.this, R.drawable.ic_map_pin_location));
                                    // Placing a marker on the touched position
                                    mMap.addMarker(markerOptions);
                                }
                                LocationShareAdapter adapter = new LocationShareAdapter(LocationActivity.this, jsonPlaces);
                                recyclerView.setAdapter(adapter);

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.clone().enqueue(this);
                    t.printStackTrace();
                }
            });
        }
    }
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
