package com.intuit.quickfoods;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class QuickFoodsService extends Service{

    public NsdHelper mNsdHelper;
    public QuickFoodsConnection mConnection;
    public Handler mUpdateHandler;

    public static final String TAG = "QuickFoods";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // todo
            }
        };

        mConnection = new QuickFoodsConnection(mUpdateHandler);
        mNsdHelper = new NsdHelper(getApplicationContext());
        mNsdHelper.initializeNsd();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onDestroy();
    }
}
