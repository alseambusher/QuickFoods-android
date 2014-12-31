package com.intuit.quickfoods;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderKitchen extends PlaceholderBase {
    public View view;
    public ViewGroup itemsContainer;
    public List<ContentValues> food_items;
    public AutoCompleteTextView search_items;
    public EditText category;
    public EditText tableNo;
    public KitchenRefresh kThread;
    public boolean stopRefresh = false;

    public PlaceholderKitchen() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_kitchen,
                container, false);

        search_items = (AutoCompleteTextView) view.findViewById(R.id.filter_kitchen);
        category = (EditText) view.findViewById(R.id.category_filter_kitchen);
        tableNo = (EditText) view.findViewById(R.id.kitchen_table_no);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, ItemsManager.getAllItems(getActivity(),ItemsManager.COLUMN_ITEM));
        search_items.setAdapter(adapter);

        food_items = getFoodItems();
        refreshFoodItemList();

        // APPLY FILTER BUTTON
        final Button filter_button = (Button) view.findViewById(R.id.filter_kitchen_apply);
        filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                food_items = getFoodItems();
                refreshFoodItemList();
            }
        });

        kThread = new KitchenRefresh();
        kThread.start();

        return view;
    }

    public List<ContentValues> getFoodItems(){
        List<String> filterQuery = new ArrayList<String>();

        String categoryValue = category.getText().toString();
        String tableNoValue = tableNo.getText().toString();
        String searchItemValue = search_items.getText().toString();

        if (!tableNoValue.isEmpty()){
            filterQuery.add(OrderManager.COLUMN_TABLE_NO +" = "+ tableNoValue);
        }
        if (!searchItemValue.isEmpty()){
            filterQuery.add(OrderManager.COLUMN_ORDER_ITEM +" = '"+ searchItemValue +"'");
        }
        if (!categoryValue.isEmpty()){
            filterQuery.add(OrderManager.COLUMN_CATEGORY+" = '"+ categoryValue +"'");
        }
        filterQuery.add(OrderManager.COLUMN_STATUS + " = " + Constants.ITEM_IN_KITCHEN);
        return OrderManager.getAllItemsFromTable(getActivity(), filterQuery);
    }

    public SwipeDismissTouchListener touchListener(final TextView food_list_item){
        final TextView table_no = (TextView) view.findViewById(R.id.take_order_table_no) ;
        return new SwipeDismissTouchListener(
                food_list_item,null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        itemsContainer.removeView(food_list_item);
                        OrderManager.completeOrderItem(getActivity(), food_list_item.getId());

                        new DataSender(getActivity()).item_complete(food_list_item.getId());

                        food_items = getFoodItems();
                        refreshFoodItemList();
                    }
                });
    }

    // new food list item
    public TextView FoodListItem(String itemValue, int count, int itemStatus , String directions, int order_id){

        final TextView food_list_item = new TextView(getActivity());
        food_list_item.setTextAppearance(getActivity(), R.style.Theme_Quickfoods_ItemListTextView);

        // todo remove hardcoded color categories
        String category = OrderManager.getColumn(getActivity(), order_id, OrderManager.COLUMN_CATEGORY);
        if (category.equals("non veg"))
            food_list_item.setBackgroundResource(Constants.ITEM_BORDER[0]);
        else if (category.equals("drinks"))
            food_list_item.setBackgroundResource(Constants.ITEM_BORDER[1]);
        else
            food_list_item.setBackgroundResource(Constants.ITEM_BORDER[2]);

        food_list_item.setTextColor(getResources().getColor(R.color.white));
        food_list_item.setId(order_id);
        food_list_item.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (directions.isEmpty()) {
            food_list_item.setText(itemValue + " - " + count);
            food_list_item.setPadding(10, 20, 10, 20);
        }
        else {
            food_list_item.setTextSize(20.8f);
            food_list_item.setText(itemValue + " - " + count + "\n" + directions);
            food_list_item.setPadding(10, 15, 10, 15);
        }

        // if item is complete it shouldn't be able to dismiss it
        if (itemStatus != Constants.ITEM_COMPLETE) {
            food_list_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // todo ?
                }
            });
            food_list_item.setOnTouchListener(touchListener(food_list_item));
        }
        return food_list_item;
    }
    public void refreshFoodItemList(){
        try {
            itemsContainer.removeAllViews();
        } catch (Exception e){}

        ListView listView = new ListView(getActivity());

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());

        // Set up normal ViewGroup example
        itemsContainer  = (ViewGroup) view.findViewById(R.id.kitchen_dismissable_container);

        for (ContentValues item : food_items) {
            TextView food_list_item = FoodListItem(
                    item.getAsString(OrderManager.COLUMN_ORDER_ITEM),
                    item.getAsInteger(OrderManager.COLUMN_ITEM_COUNT),
                    item.getAsInteger(OrderManager.COLUMN_STATUS),
                    item.getAsString(OrderManager.COLUMN_DIRECTIONS),
                    item.getAsInteger(OrderManager.ORDER_ID)
            );
            itemsContainer.addView(food_list_item);
        }
    }

    // this only refreshes the ui
    public class KitchenRefresh extends Thread{
        public void run(){
            while (!stopRefresh) {
                // TODO:performance perform refresh only if items have changed
                food_items = getFoodItems();
                refreshFoodItemList();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRefresh = true;
    }
}
