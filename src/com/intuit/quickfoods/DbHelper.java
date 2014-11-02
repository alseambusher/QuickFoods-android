package com.intuit.quickfoods;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quickfoods.db";
    private static final int DATABASE_VERSION = 1;

    public static SQLiteDatabase mDb;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        mDb = database;
        database.execSQL(ItemsManager.ITEMS_TABLE_CREATE);
        database.execSQL(OrderManager.ORDER_TABLE_CREATE);
        addDummyItems();
    }

    // todo this isn't working. check this
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DbHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + ItemsManager.TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + OrderManager.TABLE_ORDER);
        onCreate(db);
    }

    public static void ordersUpdater(){
        // TODO
    }

    // this gets all the items from the server
    public static void itemsUpdater(){
        // TODO
    }

    public void addDummyItems(){
        List<ContentValues> values = new ArrayList<ContentValues>();
        values.add(ItemsManager.Item("veg", "desc", "59", "Veg Manchow Soup"));
        values.add(ItemsManager.Item("veg", "desc", "99", "Gobi Manchurian"));
        values.add(ItemsManager.Item("non veg", "desc", "189", "Chilly Chicken"));
        values.add(ItemsManager.Item("veg", "desc", "149", "Paneer Butter Masala"));
        values.add(ItemsManager.Item("non veg", "desc", "130", "Chicken Butter Masala"));
        values.add(ItemsManager.Item("veg", "desc", "150", "Paneer Koftha"));
        values.add(ItemsManager.Item("drinks", "desc", "30", "Pepsi"));

        for (ContentValues value: values){
            mDb.insert(ItemsManager.TABLE_ITEMS, null, value);
        }

    }
}