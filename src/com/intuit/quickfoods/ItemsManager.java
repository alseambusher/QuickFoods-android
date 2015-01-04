package com.intuit.quickfoods;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ItemsManager {

    public static final String TABLE_ITEMS = "items";
    public static final String ITEM_ID = "_id";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_ITEM = "item";

    // Database creation sql statement
    public static final String ITEMS_TABLE_CREATE = "create table "
            + TABLE_ITEMS + "("
            + ITEM_ID + " integer primary key autoincrement, "
            + COLUMN_CATEGORY + " text not null, "
            + COLUMN_DESCRIPTION + " text not null, "
            + COLUMN_PRICE + " text not null, "
            + COLUMN_ITEM + " text not null);";

    public static String [] COLUMNS = {
            ITEM_ID,
            COLUMN_CATEGORY,
            COLUMN_DESCRIPTION,
            COLUMN_PRICE,
            COLUMN_ITEM
    };

    public static ContentValues Item(String category, String description, String price, String item){
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_ITEM, item);

        return values;
    }

    public static ContentValues toItem(Cursor c){
        ContentValues item = new ContentValues();
        for (String column : COLUMNS){
            item.put(column, c.getString(c.getColumnIndex(column)));
        }
        return item;
    }
// todo not tested
    public static List<ContentValues> getAllItems(Context context){
        List<ContentValues> values = new ArrayList<ContentValues>();
        SQLiteDatabase db = new DbHelper(context).getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            values.add(toItem(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return values;

    }

    public static String[] getAllItems(Context context, String column){
        List<String> values = new ArrayList<String>();
        SQLiteDatabase db = new DbHelper(context).getReadableDatabase();

        Cursor cursor = db.query(TABLE_ITEMS, new String[] {column}, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            values.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return values.toArray(new String[values.size()]);
    }

    public static int getId(Context context, String item){
        SQLiteDatabase db = new DbHelper(context).getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, new String[]{ITEM_ID}, COLUMN_ITEM + " = '" + item + "'", null, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            int _id = Integer.parseInt(cursor.getString(0));
            cursor.close();
            db.close();
            return _id;
        }
        else {
            cursor.close();
            db.close();
            return -1;
        }
    }

    public static String getCategory(Context context, String item){
        SQLiteDatabase db = new DbHelper(context).getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, new String[]{COLUMN_CATEGORY}, COLUMN_ITEM + " = '" + item + "'", null, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            String category = cursor.getString(0);
            cursor.close();
            db.close();
            return category;
        }
        else {
            cursor.close();
            db.close();
            return null;
        }
    }
}
