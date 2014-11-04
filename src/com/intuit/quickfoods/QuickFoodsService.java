package com.intuit.quickfoods;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class QuickFoodsService extends Service{

    public static final String TAG = "SocketConnection";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isKitchen = prefs.getBoolean("is_kitchen", false);
        String kitchenPort = prefs.getString("kitchen_port", "1337");
        String waiterPort = prefs.getString("waiter_port", "1338");

        Thread tServer;
        if(isKitchen)
            tServer = new server(Integer.parseInt(kitchenPort));
        else
            tServer = new server(Integer.parseInt(waiterPort));
        tServer.start();

        return START_STICKY;
    }

    public class server extends Thread{
        private ServerSocket serverSocket;

        server(int port){
            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setSoTimeout(10000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run()
        {
            while(true)
            {
                try
                {
                    Log.d(TAG, "Waiting for client on port " +
                            serverSocket.getLocalPort());
                    Socket server = serverSocket.accept();
                    Log.d(TAG, "Just connected to "
                            + server.getRemoteSocketAddress());
                    DataInputStream in =
                            new DataInputStream(server.getInputStream());

                    // TODO do whatever u want with this data
                    String data = in.readUTF();

                    //DataOutputStream out =
                            //new DataOutputStream(server.getOutputStream());
                    //out.writeUTF("Thank you for connecting to "
                            //+ server.getLocalSocketAddress() + "\nGoodbye!");
                    server.close();
                }catch(SocketTimeoutException s)
                {
                    Log.e(TAG, "Socket timed out!");
                    break;
                }catch(IOException e)
                {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}