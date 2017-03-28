package com.llu17.youngq.sqlite_gps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.util.Log;


/**
 * Created by youngq on 17/3/9.
 */

public class CollectorUtils {
    public static void startRecordService(Context context, int seconds, Class<?> cls, String action) {
        //获取AlarmManager系统服务
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        //包装需要执行Service的Intent
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //触发服务的起始时间
        long triggerAtTime = System.currentTimeMillis();

        //使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
        manager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime,
                seconds * 1000, pendingIntent);
        Log.e("===manager===","===begin===");
    }


    public static void stopRecordService(Context context, Class<?> cls,String action) {
        if(action.equals(CollectorService.ACTION)) {
            CollectorService c = new CollectorService();
            c.onDestroy();
        }
        else if(action.equals(Activity_Tracker.ACTION)){
            Activity_Tracker a = new Activity_Tracker();
            a.onDestroy();

        }
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //取消正在执行的服务
        manager.cancel(pendingIntent);
        Log.e("===manager===","===stop===");
    }
}
