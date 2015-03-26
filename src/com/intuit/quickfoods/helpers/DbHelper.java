package com.intuit.quickfoods.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.intuit.quickfoods.data.ItemsManager;
import com.intuit.quickfoods.data.OrderManager;

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
        // Starters, Salads and Soups, Sandwiches, Pizzeria, Pasta, International, Asian, Regional, Desserts, Beverages
        List<ContentValues> values = new ArrayList<ContentValues>();
        values.add(ItemsManager.Item("Starters:non veg", "desc", "59", "Crisp fried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Starters:veg", "desc", "59", "Mezee Platter with Hummus, Bangkadesh, Olives and Pakistan "));
        values.add(ItemsManager.Item("Starters:non veg", "desc", "59", "Unfried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Starters:veg", "desc", "59", "Exotic Vegetable and Pine Nut Basket"));
        values.add(ItemsManager.Item("Starters:veg", "desc", "59", "Vegetable Nuts and bolts"));
        values.add(ItemsManager.Item("Starters:non veg", "desc", "59", "Fried Dragons with Paneer"));

        values.add(ItemsManager.Item("Soups and Salads:non veg", "desc", "59", "Crisp fried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Soups and Salads:veg", "desc", "59", "Mezee Platter with Hummus, Bangkadesh, Olives and Pakistan "));
        values.add(ItemsManager.Item("Soups and Salads:non veg", "desc", "59", "Unfried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Soups and Salads:veg", "desc", "59", "Exotic Vegetable and Pine Nut Basket"));
        values.add(ItemsManager.Item("Soups and Salads:veg", "desc", "59", "Vegetable Nuts and bolts"));
        values.add(ItemsManager.Item("Soups and Salads:non veg", "desc", "59", "Fried Dragons with Paneer"));

        values.add(ItemsManager.Item("International:non veg", "desc", "59", "Crisp fried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("International:veg", "desc", "59", "Mezee Platter with Hummus, Bangkadesh, Olives and Pakistan "));
        values.add(ItemsManager.Item("International:non veg", "desc", "59", "Unfried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("International:veg", "desc", "59", "Exotic Vegetable and Pine Nut Basket"));
        values.add(ItemsManager.Item("International:veg", "desc", "59", "Vegetable Nuts and bolts"));
        values.add(ItemsManager.Item("International:non veg", "desc", "59", "Fried Dragons with Paneer"));

        values.add(ItemsManager.Item("Asian:non veg", "desc", "59", "Crisp fried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Asian:veg", "desc", "59", "Mezee Platter with Hummus, Bangkadesh, Olives and Pakistan "));
        values.add(ItemsManager.Item("Asian:non veg", "desc", "59", "Unfried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Asian:veg", "desc", "59", "Exotic Vegetable and Pine Nut Basket"));
        values.add(ItemsManager.Item("Asian:veg", "desc", "59", "Vegetable Nuts and bolts"));
        values.add(ItemsManager.Item("Asian:non veg", "desc", "59", "Fried Dragons with Paneer"));

        values.add(ItemsManager.Item("Chinese:non veg", "desc", "59", "Crisp fried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Chinese:veg", "desc", "59", "Mezee Platter with Hummus, Bangkadesh, Olives and Pakistan "));
        values.add(ItemsManager.Item("Chinese:non veg", "desc", "59", "Unfried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Chinese:veg", "desc", "59", "Exotic Vegetable and Pine Nut Basket"));
        values.add(ItemsManager.Item("Chinese:veg", "desc", "59", "Vegetable Nuts and bolts"));
        values.add(ItemsManager.Item("Chinese:non veg", "desc", "59", "Fried Dragons with Paneer"));

        values.add(ItemsManager.Item("Regional:non veg", "desc", "59", "Crisp fried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Regional:veg", "desc", "59", "Mezee Platter with Hummus, Bangkadesh, Olives and Pakistan "));
        values.add(ItemsManager.Item("Regional:non veg", "desc", "59", "Unfried Prawns with Sambal Oelek"));
        values.add(ItemsManager.Item("Regional:veg", "desc", "59", "Exotic Vegetable and Pine Nut Basket"));
        values.add(ItemsManager.Item("Regional:veg", "desc", "59", "Vegetable Nuts and bolts"));
        values.add(ItemsManager.Item("Regional:non veg", "desc", "59", "Fried Dragons with Paneer"));

        values.add(ItemsManager.Item("Beverages:Milk Shakes", "desc", "150", "Vanilla"));
        values.add(ItemsManager.Item("Beverages:Milk Shakes", "desc", "150", "Chocolate"));
        values.add(ItemsManager.Item("Beverages:Milk Shakes", "desc", "150", "Strawberry"));
        values.add(ItemsManager.Item("Beverages:Milk Shakes", "desc", "150", "Oreo"));

        values.add(ItemsManager.Item("Beverages:Coffee", "desc", "150", "Vanilla"));
        values.add(ItemsManager.Item("Beverages:Coffee", "desc", "150", "Chocolate"));
        values.add(ItemsManager.Item("Beverages:Coffee", "desc", "150", "Strawberry"));
        values.add(ItemsManager.Item("Beverages:Coffee", "desc", "150", "Oreo"));

        values.add(ItemsManager.Item("Beverages:Tea", "desc", "150", "Vanilla"));
        values.add(ItemsManager.Item("Beverages:Tea", "desc", "150", "Chocolate"));
        values.add(ItemsManager.Item("Beverages:Tea", "desc", "150", "Strawberry"));
        values.add(ItemsManager.Item("Beverages:Tea", "desc", "150", "Oreo"));

        values.add(ItemsManager.Item("Beverages:Others", "desc", "150", "Vanilla"));
        values.add(ItemsManager.Item("Beverages:Others", "desc", "150", "Chocolate"));
        values.add(ItemsManager.Item("Beverages:Others", "desc", "150", "Strawberry"));
        values.add(ItemsManager.Item("Beverages:Others", "desc", "150", "Oreo"));

        for (ContentValues value: values){
            mDb.insert(ItemsManager.TABLE_ITEMS, null, value);
        }

    }
}