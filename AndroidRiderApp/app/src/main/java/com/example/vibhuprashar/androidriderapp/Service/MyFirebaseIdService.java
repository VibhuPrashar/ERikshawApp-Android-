package com.example.vibhuprashar.androidriderapp.Service;

import com.example.vibhuprashar.androidriderapp.Common.Common;
import com.example.vibhuprashar.androidriderapp.Model.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {


        String refershedToken = FirebaseInstanceId.getInstance().getToken();
        updateTokenToServer(refershedToken);




    }

    private void updateTokenToServer(String refershedToken) {


        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tb1);
        Token token = new Token(refershedToken);
        if(FirebaseAuth.getInstance().getCurrentUser()!= null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);



































    }


}
