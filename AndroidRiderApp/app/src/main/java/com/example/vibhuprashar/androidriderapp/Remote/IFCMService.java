package com.example.vibhuprashar.androidriderapp.Remote;

import com.example.vibhuprashar.androidriderapp.Model.FCMResponse;
import com.example.vibhuprashar.androidriderapp.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {



    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAAYU1IKpM:APA91bFP3pLIzamwqB8ggqwFl2GGdZ-MDxnnBf2XNBP9D1vankiP0tjQzcOruThcmsuXUMV056mrdTp--wGwj1E6rgPwITQwcmr5fRtlc19khzjR39lP6qeYY9umHf_HO-q8CB6F7csD"

    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);






}
