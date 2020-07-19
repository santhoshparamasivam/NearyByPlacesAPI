package com.example.placesapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.HashMap;
import java.util.List;

/**
 * Created by admin on 02-08-2017.
 */

public class LocationShareAdapter extends RecyclerView.Adapter<LocationShareAdapter.LocationShareViewHolder> {

    public Context context;
    public List<HashMap<String, String>> placeList;

    public LocationShareAdapter(Context context, List<HashMap<String, String>> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @Override
    public LocationShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.location_share_adapter, parent, false);
        return new LocationShareViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocationShareViewHolder holder, int position) {
        HashMap<String, String> placeHash = placeList.get(position);
        double lat = Double.parseDouble(placeHash.get("lat"));
        // Getting longitude of the place
        double lng = Double.parseDouble(placeHash.get("lng"));
        // Getting name
        String name = placeHash.get("place_name");
        // Getting vicinity
        String vicinity = placeHash.get("vicinity");

        holder.placeName.setText(name);
        holder.fullPlaceName.setText(vicinity);
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class LocationShareViewHolder extends RecyclerView.ViewHolder {
       TextView placeName;
        TextView fullPlaceName;

        public LocationShareViewHolder(View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.place_name);
            fullPlaceName = itemView.findViewById(R.id.full_place);
        }
    }
}
