package com.intuit.quickfoods;

import android.graphics.Color;

public final class Constants {

    public static String take_order = "Take Order";
    public static String items = "Items";
    public static String kitchen = "Kitchen";
    public static String history = "History";
    public static String review = "Reviews";

    // if you change this order the todal order will change
    public static String[] nav_drawer_items = new String[]{take_order, items,
            kitchen, history, review};

    // colors
    public static int nav_drawer_text_color = Color.BLACK;

    public static int ITEM_CREATED_STATUS = 0;
    public static int ITEM_IN_KITCHEN = 1;
    public static int ITEM_COMPLETE = 2;
    public static int[] ITEM_BORDER = new int[]{
            R.drawable.black_orange_border,
            R.drawable.black_blue_border,
            R.drawable.black_green_border
    };

    // TODO load this from db
    public static String[] food_items = new String[]{
            "Veg Manchow Soup",
            "Gobi Manchurian",
            "Chilly Chicken",
            "Paneer Butter Masala",
            "Chicken Butter Masala",
            "Paneer Koftha",
            "Pepsi"
    };
}
