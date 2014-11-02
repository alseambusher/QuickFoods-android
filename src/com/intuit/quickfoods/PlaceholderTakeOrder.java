package com.intuit.quickfoods;


import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlaceholderTakeOrder extends PlaceholderBase {
    public View view;
	public ViewGroup itemsContainer;
    // todo change this to a object with other stuff too
    public List<ContentValues> food_items;

	public PlaceholderTakeOrder() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_take_order,
				container, false);

		Button table_no_go = (Button) view.findViewById(R.id.button1);
		table_no_go.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                final TextView table_no = (TextView) view.findViewById(R.id.take_order_table_no) ;
                final String table_no_value = table_no.getText().toString();

                if (table_no_value.isEmpty()){
                    Toast.makeText(getActivity(),
                            "Table no. invalid",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                try{
                    ((ViewStub) view.findViewById(R.id.stub_import_order_items_load)).inflate();
                } catch (Exception e){}

                food_items = OrderManager.getAllItemsFromTable(getActivity(),table_no_value);
                refreshFoodItemList();

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, ItemsManager.getAllItems(getActivity(), ItemsManager.COLUMN_ITEM));
                final AutoCompleteTextView take_order_add_item= (AutoCompleteTextView)
                        view.findViewById(R.id.take_order_add_item);
                take_order_add_item.setAdapter(adapter);
                take_order_add_item.requestFocus();

                Button add_item = (Button) view.findViewById(R.id.take_order_add_item_button);
                add_item.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newItemValue = take_order_add_item.getText().toString();
                        // todo make check if item is valid
                        if (!newItemValue.isEmpty()) {
                            // todo send count value properly
                            long order_id = OrderManager.newOrderItem(getActivity(),table_no_value, 1, newItemValue);
                            ContentValues order = OrderManager.newOrderItemValue(table_no_value, 1, newItemValue);
                            order.put(OrderManager.ORDER_ID, order_id);
                            food_items.add(order);
                            refreshFoodItemList();
                            take_order_add_item.setText("");
                        }
                        else {
                            Toast.makeText(getActivity(),
                                    "Item invalid",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return view;
	}
	
	public SwipeDismissTouchListener touchListener(final TextView food_list_item){
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
                        OrderManager.deleteOrderItem(getActivity(), food_list_item.getId());
                    }
                });
	}

    // new food list item
    public TextView FoodListItem(String itemValue, int itemStatus , int order_id){

        final TextView food_list_item = new TextView(getActivity());
        food_list_item.setTextAppearance(getActivity(), R.style.Theme_Quickfoods_ItemListTextView);
        food_list_item.setBackgroundResource(Constants.ITEM_BORDER[itemStatus]);
        food_list_item.setPadding(10, 20, 10, 20);
        food_list_item.setTextColor(getResources().getColor(R.color.white));
        food_list_item.setId(order_id);
        food_list_item.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        food_list_item.setText(itemValue);

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
    // Todo pass data as an argument
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
        itemsContainer  = (ViewGroup) view.findViewById(R.id.dismissable_container);
        // todo change this to iterator
        for (int i = 0; i < food_items.size(); i++) {
            // Todo add actual statuses here
            TextView food_list_item = FoodListItem(
                    food_items.get(i).getAsString(OrderManager.COLUMN_ORDER_ITEM),
                    food_items.get(i).getAsInteger(OrderManager.COLUMN_STATUS),
                    food_items.get(i).getAsInteger(OrderManager.ORDER_ID)
            );
            itemsContainer.addView(food_list_item);
        }
    }
}