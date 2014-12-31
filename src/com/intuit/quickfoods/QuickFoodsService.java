package com.intuit.quickfoods;
/*
import android.app.Service;
import android.content.Context;
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class QuickFoodsService extends Service{

    public static final String TAG = "SocketConnection";
    public Service service = this;

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
                //serverSocket.setSoTimeout(10000);
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
                    Log.d(TAG,data);
                    String command = data.substring(0,data.indexOf(Constants.DELIMITER_COMMAND));
                   // if (Integer.parseInt(command) == Constants._TO_K_ORDER_SUBMIT){
                    if (command.compareTo("1") == 0){
                        String notCommand = data.substring(data.indexOf(Constants.DELIMITER_COMMAND)+1,
                               data.length());
                        StringTokenizer tokenizer = new StringTokenizer(notCommand, Constants.DELIMITER_ITEM_SET);
                        while (tokenizer.hasMoreElements()){
                            String itemSet = (String) tokenizer.nextElement();
                                StringTokenizer tokenizerItem = new StringTokenizer(itemSet, Constants.DELIMITER_ITEM);
                                List<String> item = new ArrayList<String>();
                                while ((tokenizerItem.hasMoreElements())){
                                    item.add((String)tokenizerItem.nextElement());
                                }
                                OrderManager.newOrderItem(getApplicationContext(),
                                        item.get(0), // orderid
                                        item.get(1), //table no
                                        item.get(2), // count
                                        item.get(5), // item
                                        item.get(4) // directions
                                );
                            }
                    }

                    else if (command.compareTo("2") == 0){
                        String notCommand = data.substring(data.indexOf(Constants.DELIMITER_COMMAND)+1,
                                data.length());
                        StringTokenizer tokenizer = new StringTokenizer(notCommand, Constants.DELIMITER_ITEM_SET);
                        while (tokenizer.hasMoreElements()){
                            String itemSet = (String) tokenizer.nextElement();
                            StringTokenizer tokenizerItem = new StringTokenizer(itemSet, Constants.DELIMITER_ITEM);
                            List<String> item = new ArrayList<String>();
                            while ((tokenizerItem.hasMoreElements())){
                                item.add((String)tokenizerItem.nextElement());
                            }
                            new Notification(service,"Order complete for table "
                                     + OrderManager.getColumn(
                                    getApplicationContext(),
                                    Integer.parseInt(item.get(0)),
                                    OrderManager.COLUMN_TABLE_NO)
                                    , "Item "
                                    + OrderManager.getColumn(
                                    getApplicationContext(),
                                    Integer.parseInt(item.get(0)),
                                    OrderManager.COLUMN_ORDER_ITEM) );

                            OrderManager.completeOrderItem(getApplicationContext(), Integer.parseInt(item.get(0)));
                        }
                    }

                    else if (command.compareTo("4") == 0){
                        String notCommand = data.substring(data.indexOf(Constants.DELIMITER_COMMAND)+1,
                                data.length());
                        StringTokenizer tokenizer = new StringTokenizer(notCommand, Constants.DELIMITER_ITEM_SET);
                        while (tokenizer.hasMoreElements()){
                            String itemSet = (String) tokenizer.nextElement();
                            StringTokenizer tokenizerItem = new StringTokenizer(itemSet, Constants.DELIMITER_ITEM);
                            List<String> item = new ArrayList<String>();
                            while ((tokenizerItem.hasMoreElements())){
                                item.add((String)tokenizerItem.nextElement());
                            }
                            new Notification(service,"Order cancelled from table number "
                                    + OrderManager.getColumn(
                                    getApplicationContext(),
                                    Integer.parseInt(item.get(0)),
                                    OrderManager.COLUMN_TABLE_NO)
                                    , "Item "
                                    + OrderManager.getColumn(
                                    getApplicationContext(),
                                    Integer.parseInt(item.get(0)),
                                    OrderManager.COLUMN_ORDER_ITEM) );

                            OrderManager.deleteOrderItem(getApplicationContext(), Integer.parseInt(item.get(0)));
                        }
                    }


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
}*/

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class QuickFoodsService extends Service{

    public NsdHelper mNsdHelper;
    public QuickFoodsConnection mConnection;
    public Handler mUpdateHandler;
    public Service service = this;

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
                // todo when you  a message
                // todo check this
                String data = msg.getData().getString("msg").substring(6);
                    Log.d(TAG,data);
                    String command = data.substring(0,data.indexOf(Constants.DELIMITER_COMMAND));
                    if (Integer.parseInt(command) == Constants._TO_K_ORDER_SUBMIT){
                        Toast.makeText(service, command, Toast.LENGTH_LONG).show();
                        String notCommand = data.substring(data.indexOf(Constants.DELIMITER_COMMAND)+1,
                               data.length());
                        StringTokenizer tokenizer = new StringTokenizer(notCommand, Constants.DELIMITER_ITEM_SET);

                        while (tokenizer.hasMoreElements()){
                            String itemSet = (String) tokenizer.nextElement();
                                StringTokenizer tokenizerItem = new StringTokenizer(itemSet, Constants.DELIMITER_ITEM);
                                List<String> item = new ArrayList<String>();
                                while ((tokenizerItem.hasMoreElements())){
                                    item.add((String)tokenizerItem.nextElement());
                                }
                                OrderManager.newOrderItem(getApplicationContext(),
                                        item.get(0), // orderid
                                        item.get(1), //table no
                                        item.get(2), // count
                                        item.get(5), // item
                                        item.get(4) // directions
                                );
                            }
                    }

                    else if (Integer.parseInt(command) == Constants._TO_W_ORDER_COMPLETE){
                        String notCommand = data.substring(data.indexOf(Constants.DELIMITER_COMMAND)+1,
                                data.length());
                        StringTokenizer tokenizer = new StringTokenizer(notCommand, Constants.DELIMITER_ITEM_SET);
                        while (tokenizer.hasMoreElements()){
                            String itemSet = (String) tokenizer.nextElement();
                            StringTokenizer tokenizerItem = new StringTokenizer(itemSet, Constants.DELIMITER_ITEM);
                            List<String> item = new ArrayList<String>();
                            while ((tokenizerItem.hasMoreElements())){
                                item.add((String)tokenizerItem.nextElement());
                            }
                            new Notification(service,"Order complete for table "
                                     + OrderManager.getColumn(
                                    getApplicationContext(),
                                    Integer.parseInt(item.get(0)),
                                    OrderManager.COLUMN_TABLE_NO)
                                    , "Item "
                                    + OrderManager.getColumn(
                                    getApplicationContext(),
                                    Integer.parseInt(item.get(0)),
                                    OrderManager.COLUMN_ORDER_ITEM) );

                            OrderManager.completeOrderItem(getApplicationContext(), Integer.parseInt(item.get(0)));
                        }
                    }

                    else if (Integer.parseInt(command) == Constants._TO_K_DELETE_ORDER){
                        String notCommand = data.substring(data.indexOf(Constants.DELIMITER_COMMAND)+1,
                                data.length());
                        StringTokenizer tokenizer = new StringTokenizer(notCommand, Constants.DELIMITER_ITEM_SET);
                        while (tokenizer.hasMoreElements()){
                            String itemSet = (String) tokenizer.nextElement();
                            StringTokenizer tokenizerItem = new StringTokenizer(itemSet, Constants.DELIMITER_ITEM);
                            List<String> item = new ArrayList<String>();
                            while ((tokenizerItem.hasMoreElements())){
                                item.add((String)tokenizerItem.nextElement());
                            }
                            new Notification(service,"Order cancelled from table number "
                                    + OrderManager.getColumn(
                                    getApplicationContext(),
                                    Integer.parseInt(item.get(0)),
                                    OrderManager.COLUMN_TABLE_NO)
                                    , "Item "
                                    + OrderManager.getColumn(
                                    getApplicationContext(),
                                    Integer.parseInt(item.get(0)),
                                    OrderManager.COLUMN_ORDER_ITEM) );

                            OrderManager.deleteOrderItem(getApplicationContext(), Integer.parseInt(item.get(0)));
                        }
                    }
            }
        };

        mConnection = new QuickFoodsConnection(mUpdateHandler);
        mNsdHelper = new NsdHelper(getApplicationContext());
        mNsdHelper.initializeNsd();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isKitchen = prefs.getBoolean("is_kitchen", false);

        if (isKitchen) {
            advertise(); // register only if it is kitchen
        } else {
            discover();
            while (connect() == false){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }

    IQuickFoodsService.Stub mStub = new IQuickFoodsService.Stub(){

        public void send(String messageString) {
            if (!messageString.isEmpty()) {
                try {
                    Log.d(TAG,"Sending:" + messageString);
                    mConnection.sendMessage(messageString);
                }
                catch (Exception e){
                    // TODO try again for sometime
                    Log.e(TAG,"Unable to send message");
                }
            }
        }
    };

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

    public boolean connect() {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
            return true;
        } else {
            Log.d(TAG, "No service to connect to!");
            return false;
        }
    }


    @Override
    public void onDestroy() {
        mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onDestroy();
    }
}