package com.example.android.sunshine.app;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by Audi on 26/10/16.
 */

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    public static final String TOKEN = "token";
    public static final String REGISTERED_GCM_TOKEN = "reg_token";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, refreshedToken);
        editor.putBoolean(REGISTERED_GCM_TOKEN, true);
        editor.apply();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

    }

}
