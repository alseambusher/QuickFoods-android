package com.intuit.quickfoods;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    public static final String TABLE_ORDER = "orders";
    public static final String ORDER_ID = "_id";
    public static final String COLUMN_TABLE_NO = "table_no";
    public static final String COLUMN_ITEM_COUNT = "count";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_ORDER_ITEM = "item_id";

    public static final String[] COLUMNS = new String[] {
            ORDER_ID,
            COLUMN_TABLE_NO,
            COLUMN_ITEM_COUNT,
            COLUMN_STATUS,
            COLUMN_ORDER_ITEM
    };

    public static final String ORDER_TABLE_CREATE = "create table "
            + TABLE_ORDER + "("
            + ORDER_ID + " integer primary key autoincrement, "
            + COLUMN_TABLE_NO + " integer, "
            + COLUMN_ITEM_COUNT + " integer, "
            + COLUMN_STATUS + " integer, "
            + COLUMN_ORDER_ITEM + " text not null);";

    public static ContentValues Order(String table_no, int count, int status, String item){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TABLE_NO, table_no);
        values.put(COLUMN_ITEM_COUNT, count);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_ORDER_ITEM, item);

        return values;
    }
    public static ContentValues newOrderItemValue(String table_no, int count, String item){
        return Order(table_no, count, Constants.ITEM_CREATED_STATUS, item);
    }

    public static ContentValues toItem(Cursor c){
        ContentValues item = new ContentValues();
        for (String column : COLUMNS){
            item.put(column, c.getString(c.getColumnIndex(column)));
        }
        return item;
    }
    // returns order id
    public static long newOrderItem(Context context, String table_no, int count, String item){
        int item_id = ItemsManager.getId(context, item);
        if (item_id > -1){
            ContentValues orderItem = newOrderItemValue(table_no, count, item);
            SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
            return db.insert(TABLE_ORDER, null, orderItem);
        }
        return -1;
    }
    public static List<ContentValues> getAllItemsFromTable(Context context, String table_no){
        List<ContentValues> items = new ArrayList<ContentValues>();
        SQLiteDatabase db = new DbHelper(context).getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDER, COLUMNS,COLUMN_TABLE_NO + " = " + table_no, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            items.add(toItem(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return items;

    }
    public static void deleteOrderItem(Context context, int order_id){
        SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
        db.delete(TABLE_ORDER, ORDER_ID + " = " + order_id, null);
    }

    public static void submit_order(Context context, String table_no){
        SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(COLUMN_STATUS, Constants.ITEM_IN_KITCHEN);
        db.update(TABLE_ORDER, value, COLUMN_TABLE_NO + " = " + table_no, null);
    }
}
