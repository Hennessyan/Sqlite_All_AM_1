package com.llu17.youngq.sqlite_gps;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

/**
 * Created by pradeepsaiuppula on 2/9/17.
 */

public class Activity_Tracker extends Service implements  GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    public static final String ACTION = "com.llu17.youngq.sqlite_gps.Activity_Tracker";
    public static GoogleApiClient mApiClient;

    protected void onHandleIntent(Intent intent) {
        Log.d("intent","Handling intent");


    }

    @Override
    public void onCreate() {
        Log.d("---creating---","just created---------------------");

        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("---activity---","coneected®");
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();
        return super.onStartCommand(intent, flags, startId);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("---intent---","Handling intent");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("intent","Onrebind");
        super.onRebind(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("---activity---","coneected---------------------");
        Intent intent = new Intent( this, HandleActivity.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 500, pendingIntent );

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.e("errorrr----------------",connectionResult.getErrorCode()+connectionResult.getErrorMessage()+"");
    }

    @Override
    public void onDestroy() {
        mApiClient.disconnect();
        stopSelf();
        super.onDestroy();
        Log.d("---activity---","disconnect---------------------");
    }

}
