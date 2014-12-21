package com.intuit.quickfoods;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class DataSender {
    public String TAG ="DataSender";
    public String serverName;
    public int port;
    public Context mContext;

    public DataSender(Context context){
        mContext = context;
    }
/*
    public class sendData extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            String data = strings[0];
            try {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    */
    // todo check if this has to be run asynchronously
    public void sendData(String message){
        if(!message.isEmpty()) {
            try {
                ((MainActivity) mContext).mIQuickFoodsService.send(message);
            } catch (RemoteException e) {
            }
        }
    }

    public void submit_order(String table_no){
        List<ContentValues>items = OrderManager.getAllItemsFromTable(mContext, OrderManager.COLUMN_TABLE_NO +" = "+ table_no
                +" and "+ OrderManager.COLUMN_STATUS +" = "+ Constants.ITEM_CREATED_STATUS );

        if (items.size() == 0)
            return;

        String data = Constants._TO_K_ORDER_SUBMIT+Constants.DELIMITER_COMMAND;
        // format order_id,table_no,item_count,category,direction,order_item
        for(ContentValues item:items){
            for (String column: OrderManager.COLUMNS){
                data += item.getAsString(column) + Constants.DELIMITER_ITEM;
            }
            data += Constants.DELIMITER_ITEM_SET;
        }

        sendData(data);
    }

    public void send_directions(int order_id, String directions){
        // format order_id,directions
        String data = Constants._TO_K_ORDER_DIRECTIONS+Constants.DELIMITER_COMMAND;
        data += order_id + Constants.DELIMITER_ITEM;
        data += directions;

        sendData(data);
    }

    public void send_delete_order(int order_id){
        String data = Constants._TO_K_DELETE_ORDER+Constants.DELIMITER_COMMAND;
        data += order_id;
        sendData(data);
    }

    // sent to waiter
    public void item_complete(int order_id){
       String data = Constants._TO_W_ORDER_COMPLETE+Constants.DELIMITER_COMMAND;
        data +=order_id;
        sendData(data);
    }
}
