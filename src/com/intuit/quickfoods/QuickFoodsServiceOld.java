package com.intuit.quickfoods;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

public class QuickFoodsServiceOld extends Service{

    public NsdHelper mNsdHelper;
    public QuickFoodsConnection mConnection;
    public Handler mUpdateHandler;

    public static final String TAG = "SocketConnection";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG,msg.getData().getString("msg"));
                // todo when you  a message
            }
        };

        mConnection = new QuickFoodsConnection(mUpdateHandler);
        mNsdHelper = new NsdHelper(getApplicationContext());
        mNsdHelper.initializeNsd();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isKitchen = prefs.getBoolean("is_kitchen", false);
        if (isKitchen) advertise(); // register only if it is kitchen
        discover();
        connect();
        send();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void advertise() {
        // Register service
        if(mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }

    public void discover() {
        mNsdHelper.discoverServices();
    }

    public void connect() {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

    public void send() {
        //EditText messageView = (EditText) this.findViewById(R.id.chatInput);
        //if (messageView != null) {
        //String messageString = messageView.getText().toString();
        // TODO
        String messageString = "DUMMY";
        if (!messageString.isEmpty()) {
            mConnection.sendMessage(messageString);
        }
        //messageView.setText("");
        //}
    }

    @Override
    public void onDestroy() {
        mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onDestroy();
    }
}