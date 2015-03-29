package com.intuit.quickfoods.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.intuit.quickfoods.helpers.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderManager {
    public static final String TABLE_ORDER = "orders";
    public static final String ORDER_ID = "_id";
    public static final String COLUMN_TABLE_NO = "table_no";
    public static final String COLUMN_ITEM_COUNT = "count";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DIRECTIONS= "directions";
    public static final String COLUMN_ORDER_ITEM = "item_id";

    public static final String[] COLUMNS = new String[] {
            ORDER_ID,
            COLUMN_TABLE_NO,
            COLUMN_ITEM_COUNT,
            COLUMN_STATUS,
            COLUMN_CATEGORY,
            COLUMN_DIRECTIONS,
            COLUMN_ORDER_ITEM,
    };

    public static final String ORDER_TABLE_CREATE = "create table "
            + TABLE_ORDER + "("
            + ORDER_ID + " integer primary key autoincrement, "
            + COLUMN_TABLE_NO + " integer, "
            + COLUMN_ITEM_COUNT + " integer, "
            + COLUMN_STATUS + " integer, "
            + COLUMN_CATEGORY + " text not null, "
            + COLUMN_DIRECTIONS + " text, "
            + COLUMN_ORDER_ITEM + " text not null);";

    public static ContentValues Order(String table_no, int count, int status, String item, String category, String directions){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TABLE_NO, table_no);
        values.put(COLUMN_ITEM_COUNT, count);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DIRECTIONS, directions);
        values.put(COLUMN_ORDER_ITEM, item);

        return values;
    }
    public static ContentValues Order(String table_no, int count, int status, String item, String category) {
        return Order(table_no, count, status, item, "");
    }
    public static ContentValues newOrderItemValue(Context context, String table_no, int count, String item, String directions){
        return Order(table_no, count, Base.ITEM_CREATED_STATUS, item, ItemsManager.getCategory(context, item), directions);
    }
    public static ContentValues newOrderItemValue(Context context, String table_no, int count, String item){
       return newOrderItemValue(context, table_no, count, item, "");
    }

    public static ContentValues toItem(Cursor c){
        ContentValues item = new ContentValues();
        for (String column : COLUMNS){
            item.put(column, c.getString(c.getColumnIndex(column)));
        }
        return item;
    }
    // returns order id
    public static long newOrderItem(Context context, String table_no, int count, String item, String directions){
        int item_id = ItemsManager.getId(context, item);
        if (item_id > -1){
            ContentValues orderItem = newOrderItemValue(context, table_no, count, item);
            SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
            long returnValue = db.insert(TABLE_ORDER, null, orderItem);
            db.close();
            return  returnValue;
        }
        return -1;
    }
    // this is used only by kitchen
    public static long newOrderItem(Context context, String order_id, String table_no, String count, String item, String directions){
        int item_id = ItemsManager.getId(context, item);
        if (item_id > -1){
            ContentValues orderItem = newOrderItemValue(context, table_no, Integer.parseInt(count), item, directions);
            orderItem.put(ORDER_ID, order_id);
            orderItem.put(COLUMN_STATUS, Base.ITEM_IN_KITCHEN);
            SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
            long returnValue = db.insert(TABLE_ORDER, null, orderItem);
            db.close();
            return  returnValue;
        }
        return -1;
    }
    public static long newOrderItem(Context context, String table_no, int count, String item){
        return newOrderItem(context, table_no, count, item, "");
    }

    public static  List<ContentValues> getAllItemsFromTable(Context context, String whereClause){
        List<ContentValues> items = new ArrayList<ContentValues>();
        SQLiteDatabase db = new DbHelper(context).getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDER, COLUMNS, whereClause, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            items.add(toItem(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return items;
    }
    public static  List<ContentValues> getAllItemsFromTable(Context context, List<String> whereClause){
        String clause = ("" + Arrays.asList(whereClause)).replaceAll("(^\\[|\\]$)", "").replaceAll("\\[|\\]","").replace(", ", " and " );
        return getAllItemsFromTable(context,clause);
    }
    public static void deleteOrderItem(Context context, int order_id){
        SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
        db.delete(TABLE_ORDER, ORDER_ID + " = " + order_id, null);
        db.close();
    }

    public static void completeOrderItem(Context context, int order_id){
        SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
        ContentValues newValues = new ContentValues();
        newValues.put(COLUMN_STATUS, Base.ITEM_COMPLETE);
        db.update(TABLE_ORDER, newValues, ORDER_ID + " = " + order_id, null);
        db.close();
    }

    public static void submit_order(Context context, String table_no){
        SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(COLUMN_STATUS, Base.ITEM_IN_KITCHEN);
        db.update(TABLE_ORDER, value, COLUMN_TABLE_NO + " = " + table_no +" and "+ COLUMN_STATUS +" = "+ Base.ITEM_CREATED_STATUS, null);
        db.close();
    }

    public static String getColumn(Context context, int order_id, String column){
        SQLiteDatabase db = new DbHelper(context).getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDER, new String[] {column}, ORDER_ID +" = "+order_id, null, null, null, null, null);
        cursor.moveToFirst();
        String item = cursor.getString(0);
        cursor.close();
        db.close();
        return item;
    }
    public static void updateOrder(Context context, int order_id, String column, String value){
        SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
        ContentValues container =  new ContentValues();
        container.put(column, value);
        db.update(TABLE_ORDER, container, ORDER_ID +" = "+ order_id, null);
        db.close();
    }
    public static List<String> getTables(Context context){
        SQLiteDatabase db = new DbHelper(context).getWritableDatabase();
        List<String> tables = new ArrayList<>();
        Cursor cursor = db.query(TABLE_ORDER, new String[] {COLUMN_TABLE_NO}, null, null, COLUMN_TABLE_NO,
                null,
                COLUMN_TABLE_NO);

        cursor.moveToFirst();
         while (!cursor.isAfterLast()){
            tables.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return tables;
    }
}
