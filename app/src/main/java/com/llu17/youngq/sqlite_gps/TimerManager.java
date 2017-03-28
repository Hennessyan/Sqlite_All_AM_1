package com.llu17.youngq.sqlite_gps;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by youngq on 17/3/13.
 */

public class TimerManager {
    private static List<Timer> timerList = new ArrayList<Timer>();
//    private static int count = 0;
    public static Timer createTimer(){
        Timer timer = new Timer();
        timerList.add(timer);
//        count++;
//        Log.e("-----time count-----"," " +count);
        return timer;
    }
    public static void stopAllTimer(){
        for(Timer timer : timerList){
            timer.cancel();
        }
    }
}
