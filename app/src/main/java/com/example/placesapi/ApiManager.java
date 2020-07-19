package com.example.placesapi;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiManager {

//    @GET("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
    @GET("api/place/nearbysearch/json")
    Call<ResponseBody>nearPlaceSearch(@Query("location") String location, @Query("radius") String radius, @Query("types") String types, @Query("sensor") String sensor, @Query("key") String key);

}
