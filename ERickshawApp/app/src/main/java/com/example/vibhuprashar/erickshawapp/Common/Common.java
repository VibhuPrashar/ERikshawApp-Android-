package com.example.vibhuprashar.erickshawapp.Common;

import android.location.Location;

import com.example.vibhuprashar.erickshawapp.Model.Users;
import com.example.vibhuprashar.erickshawapp.Remote.FCMClient;
import com.example.vibhuprashar.erickshawapp.Remote.IFCMService;
import com.example.vibhuprashar.erickshawapp.Remote.IGoogleAPI;
import com.example.vibhuprashar.erickshawapp.Remote.RetrofitClient;

public class Common {


    public static String currentToken = "";

    public static final String driver_tb1 = "Drivers";
    public static final String user_driver_tb1="DriversInformation";
    public static final String user_rider_tb1 = "RidersInformation";
    public static final String pickup_request_tb1 = "PickupRequest";
    public static final String token_tb1 = "Tokens";

    public static  Location mLastLocation= null;
    public static Users currentUser;



    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);


    }

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);


    }


}
