package com.example.vibhuprashar.androidriderapp.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.vibhuprashar.androidriderapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

public class CustomerInfoWindow implements GoogleMap.InfoWindowAdapter {


    View myView;

    public CustomerInfoWindow(Context context)
    {
        myView = LayoutInflater.from(context).inflate(R.layout.custom_rider_info_window,null);



    }




    @Override
    public View getInfoWindow(Marker marker) {


        TextView txtPickupTitle = ((TextView)myView.findViewById(R.id.txtPickupInfo));
        txtPickupTitle.setText(marker.getTitle());
        TextView txtPikupSnippet = ((TextView)myView.findViewById(R.id.txtPickupSnippet));
        txtPikupSnippet.setText(marker.getSnippet());
        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
