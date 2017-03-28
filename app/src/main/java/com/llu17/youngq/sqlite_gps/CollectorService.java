package com.llu17.youngq.sqlite_gps;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.llu17.youngq.sqlite_gps.data.GpsContract;
import com.llu17.youngq.sqlite_gps.data.GpsDbHelper;

import java.lang.reflect.Method;
import java.util.Timer;

import static com.llu17.youngq.sqlite_gps.MainActivity.mDb;

/**
 * Created by youngq on 17/2/15.
 */

public class CollectorService extends Service implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTION = "com.llu17.youngq.sqlite_gps.CollectorService";
    private final static String tag = "UploadService";
    public static String id = ""; //phone id
    /*===GPS===*/
    public static LocationManager locationManager;
    private static double latitude, longitude;

    public static LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.e("===location===","changed!");
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            MainActivity.tv_longitude.setText("real longitude: " + longitude);
            MainActivity.tv_latitude.setText("real latitude: " + latitude);

            long temp_time = System.currentTimeMillis();
            ContentValues cv = new ContentValues();
            cv.put(GpsContract.GpsEntry.COLUMN_ID,id);
            cv.put(GpsContract.GpsEntry.COLUMN_TIMESTAMP,temp_time);
            cv.put(GpsContract.GpsEntry.COLUMN_LATITUDE, latitude);
            cv.put(GpsContract.GpsEntry.COLUMN_LONGITUDE, longitude);

            try
            {
                mDb.beginTransaction();
                mDb.insert(GpsContract.GpsEntry.TABLE_NAME, null, cv);
                mDb.setTransactionSuccessful();
                Log.e("===insert===","success!");
            }
            catch (SQLException e) {
                //too bad :(
            }
            finally
            {
                mDb.endTransaction();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(intent);
        }
    };
    /*===Sensor:accelerometer & gyroscope===*/
    private static int RATE = 1000;  //100 -> 10 samples/s 50 -> 20 samples/s 20 -> 50 samples/s
    public static SensorManager sensorManager;
    private Sensor sensor;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private long timestamp;
    private int label = 0;
    private double[] stepcount = new double[2]; //stepcount 1.last one 2.real step
    private double[] acce = new double[3];    //accelerator
    private double[] angle = new double[3];
    private double[] gyro = new double[3];  //gyroscope
//    public static Timer timer;
    private int timerid = 0;

    /*===Motion State===*/
    public static GoogleApiClient mApiClient;

    protected void onHandleIntent(Intent intent) {
        Log.d("intent","Handling intent");


    }

    @Override
    public void onCreate() {
        Log.e("---creating---","just created---------------------");
        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("====intent====","====Handling intent====");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("intent","Onrebind");
        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        id = getSerialNumber();
        label = 0;
        Log.e("1111111","11111111");
        /*===GPS===*/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.e("2222222","222222222");
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            Log.e("33333333","33333333");
        }
        catch(SecurityException e){
            e.getStackTrace();
        }
        Log.e("=@@GPS@@=","=@@begin@@=");
//        Toast.makeText(this, "Starting the GPS!!!!!", Toast.LENGTH_SHORT).show();
        /*===Sensor===*/
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        Log.e("=$$Sensor$$=","=$$begin$$=");
        /*---step count---*/
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
//        Toast.makeText(this, "Sensor Service Started", Toast.LENGTH_SHORT).show();
//        timer = new Timer();
//        timer.schedule(new Upload(acce, gyro), 0, RATE);
        TimerManager.createTimer().schedule(new Upload(acce, gyro, stepcount), 0, RATE);
        Log.e("=&&timer" + timerid + "&&=","=&&begin&&=");
        timerid++;
//        id ++;
        /*===Motion State===*/
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();
        Log.e("=**Motion**=","=**begin**=");
        return START_STICKY;
    }

    ////////////////////////////////
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("---activity---","coneected---------------------");
        Intent intent = new Intent( this, HandleActivity.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 500, pendingIntent );

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("activity","Suspended---------------------");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("error----------------",connectionResult.getErrorCode()+connectionResult.getErrorMessage()+"");
    }
    ////////////////////////////////

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            locationManager.removeUpdates(locationListener);
        }
        catch(SecurityException e){
            e.getStackTrace();
        }
        Log.e("=@@GPS@@=","=@@stop@@=");
//        timer.cancel();
        TimerManager.stopAllTimer();
        Log.e("=&&timer&&=","=&&stop&&=");
        sensorManager.unregisterListener(this);
        Log.e("=$$Sensor$$=","=$$stop$=");
//        Toast.makeText(this, "Stopping the Sensor", Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

        mApiClient.disconnect();
        stopSelf();
        Log.e("=**Motion**=","=**stop**=");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*---step count---*/
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            if(label == 0) {
                stepcount[0] = event.values[0];
                label++;
            }
            stepcount[1] = event.values[0] - stepcount[0];
            Log.e("stepcount[0]:",""+stepcount[0]);
            Log.e("stepcount[1]:",""+stepcount[1]);
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acce[0] = event.values[0];
            acce[1] = event.values[1];
            acce[2] = event.values[2];
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (timestamp != 0) {
                // 得到两次检测到手机旋转的时间差（纳秒），并将其转化为秒
                final float dT = (event.timestamp - timestamp) * NS2S;
                // 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度
                angle[0] += event.values[0] * dT;
                angle[1] += event.values[1] * dT;
                angle[2] += event.values[2] * dT;
                // 将弧度转化为角度
                gyro[0] = (float) Math.toDegrees(angle[0]);
                gyro[1] = (float) Math.toDegrees(angle[1]);
                gyro[2] = (float) Math.toDegrees(angle[2]);
            }
            timestamp = event.timestamp;
        }
//        Log.d("hi","debug");
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.e("onAccuracy®Changed",""+accuracy);
    }
    private static String getSerialNumber(){

        String serial = null;

        try {

            Class<?> c =Class.forName("android.os.SystemProperties");

            Method get =c.getMethod("get", String.class);

            serial = (String)get.invoke(c, "ro.serialno");

        } catch (Exception e) {

            e.printStackTrace();

        }

        return serial;

    }
}
