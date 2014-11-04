package com.intuit.quickfoods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class DataSender {
    public String TAG ="DataSender";
    public String serverName;
    public int port;

    public DataSender(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean isKitchen = prefs.getBoolean("is_kitchen", false);

        if(isKitchen)
            port = Integer.parseInt(prefs.getString("waiter_port", "1338"));
        else
            port = Integer.parseInt(prefs.getString("kitchen_port", "1337"));

        serverName = prefs.getString("ip_address","0.0.0.0");

    }
    public void sendData(String data){
        try
        {
            Log.d(TAG, "Connecting to " + serverName
                    + " on port " + port);
            Socket client = new Socket(serverName, port);
            Log.d(TAG, "Just connected to "
                    + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out =
                    new DataOutputStream(outToServer);

            out.writeUTF(data);

            //InputStream inFromServer = client.getInputStream();
            //DataInputStream in =
            //new DataInputStream(inFromServer);
            //System.out.println("Server says " + in.readUTF());
            client.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
