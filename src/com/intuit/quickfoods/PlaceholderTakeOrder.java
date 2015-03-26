package com.intuit.quickfoods;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.intuit.quickfoods.data.Base;
import com.intuit.quickfoods.data.MenuItem;
import com.intuit.quickfoods.data.OrderManager;
import com.intuit.quickfoods.helpers.BillPrinterManager;
import com.intuit.quickfoods.helpers.DataSender;
import com.intuit.quickfoods.helpers.SwipeDismissListViewTouchListener;
import com.intuit.quickfoods.helpers.SwipeDismissTouchListener;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderTakeOrder extends PlaceholderBase {
    public View view;
	public ViewGroup itemsContainer;
    public List<ContentValues> food_items;

	public PlaceholderTakeOrder() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_take_order,
				container, false);

        Gson gson = new Gson();
        String json = "{'name':'main', 'subMenuItems':[{'name':'Starters'},{'name':'Soups and Salads'},{'name':'International'},{'name':'Asian'},{'name':'Chinese'},{'name':'Regional'},{'name':'Beverages', " +
                "subMenuItems:[{'name':'Milk Shakes'},{'name':'Coffee'}]}]}";
        final MenuItem menu = gson.fromJson(json, MenuItem.class);
        //Log.d("gson",);

        final GridView grid = (GridView) view.findViewById(R.id.menu_items);
        final List list=new ArrayList<String>();
        for(MenuItem item: menu.subMenuItems){
            list.add(item.name);
        }
        ArrayAdapter<String> adp=new ArrayAdapter<String> (getActivity(),
                R.layout.menu_item_tile,list);
        //grid.setNumColumns(3);
        //grid.setBackgroundColor(Color.BLACK);
        //grid.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        grid.setAdapter(adp);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                String clicked = (String)list.get(arg2);
                list.clear();
                for(MenuItem item: menu.subMenuItems){
                    if(item.name == clicked){
                        if(item.subMenuItems != null){
                            for(MenuItem subMenuItem: item.subMenuItems){
                                list.add(subMenuItem.name);
                            }

                        }
                        else{
                            // TODO equal to null
                        }
                    }
                }
                grid.invalidateViews();
            }
        });
        /*
        new ShowcaseView.Builder(getActivity())
                .setTarget(new ActionViewTarget(getActivity(), ActionViewTarget.Type.HOME))
                .setContentTitle("ShowcaseView")
                .setContentText("This is highlighting the Home button")
                .hideOnTouchOutside()
                .build();
                */

        // GO BUTTON
		ImageButton table_no_go = (ImageButton) view.findViewById(R.id.b_select_table);
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

                food_items = OrderManager.getAllItemsFromTable(getActivity(), OrderManager.COLUMN_TABLE_NO + " = " + table_no_value);
                refreshFoodItemList();

                final TextView newItemCount = (TextView) view.findViewById(R.id.take_order_count);

                // ADD ITEM BUTTON CLICK
                /*
                ImageButton add_item = (ImageButton) view.findViewById(R.id.take_order_add_item_button);
                add_item.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newItemValue = take_order_add_item.getText().toString();
                        int newItemCountValue = Integer.parseInt(newItemCount.getText().toString());

                        // todo make check if item is valid
                        if (!newItemValue.isEmpty()) {
                            long order_id = OrderManager.newOrderItem(getActivity(),table_no_value, newItemCountValue, newItemValue);
                            ContentValues order = OrderManager.newOrderItemValue(getActivity(), table_no_value, newItemCountValue, newItemValue);
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
                */

                // SUBMIT BUTTON CLICK
                Button submit_button = (Button) view.findViewById(R.id.submit_bill);
                submit_button.setOnClickListener( new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DataSender(getActivity()).submit_order(table_no_value);
                        OrderManager.submit_order(getActivity(), table_no_value);
                        food_items = OrderManager.getAllItemsFromTable(getActivity(), OrderManager.COLUMN_TABLE_NO +" = "+table_no_value);
                        refreshFoodItemList();
                    }
                });

                // MAKE BILL BUTTON
                Button bill_button = (Button) view.findViewById(R.id.make_bill);
                bill_button.setOnClickListener( new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO check this
                        BillPrinterManager printer = new BillPrinterManager(getActivity());
                        printer.doWebViewPrint();
                    }
                });
            }
        });
        return view;
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

                        // inform kitchen
                        if(Integer.parseInt(OrderManager.getColumn(getActivity(),
                                food_list_item.getId(),
                                OrderManager.COLUMN_STATUS)) == Base.ITEM_IN_KITCHEN){
                                new DataSender(getActivity()).send_delete_order(food_list_item.getId());
                        }

                        OrderManager.deleteOrderItem(getActivity(), food_list_item.getId());

                        String table_no_value = table_no.getText().toString();
                        food_items = OrderManager.getAllItemsFromTable(getActivity(), OrderManager.COLUMN_TABLE_NO +" = "+ table_no_value);
                        refreshFoodItemList();

                    }
                });
	}

    // new food list item
    public TextView FoodListItem(final String itemValue, int count, int itemStatus , String directions, final int order_id){

        final TextView food_list_item = new TextView(getActivity());
        food_list_item.setTextAppearance(getActivity(), R.style.Theme_Quickfoods_ItemListTextView);
        food_list_item.setBackgroundResource(Base.ITEM_BORDER[itemStatus]);
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

        // if item is complete it shouldn't be able to dismiss it and shouldn't be able to add directions
        if (itemStatus != Base.ITEM_COMPLETE) {
            food_list_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeOrderDetails(order_id, itemValue);
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
        itemsContainer  = (ViewGroup) view.findViewById(R.id.take_order_dismissable_container);
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

    public void takeOrderDetails(final int order_id, String item){
        AlertDialog.Builder detailsDialog = new AlertDialog.Builder(getActivity());
        detailsDialog.setTitle(item);
        detailsDialog.setMessage("Directions");

        final EditText directionsBox = new EditText(getActivity());
        // load the previously entered
        directionsBox.setText(OrderManager.getColumn(getActivity(), order_id, OrderManager.COLUMN_DIRECTIONS));
        detailsDialog.setView(directionsBox);

        detailsDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String  oldDirections = OrderManager.getColumn(getActivity(), order_id, OrderManager.COLUMN_DIRECTIONS);
                String directionsValue = directionsBox.getText().toString();
                if (!directionsValue.equals(oldDirections)) {
                    OrderManager.updateOrder(getActivity(), order_id, OrderManager.COLUMN_DIRECTIONS, directionsValue);
                    String table_no_value = ((TextView) view.findViewById(R.id.take_order_table_no)).getText().toString();
                    food_items = OrderManager.getAllItemsFromTable(getActivity(), OrderManager.COLUMN_TABLE_NO + " = " + table_no_value);
                    refreshFoodItemList();
                    // send kitchen
                    new DataSender(getActivity()).send_directions(order_id,directionsValue);
                }
            }
        });

        detailsDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
            }
        });
        detailsDialog.show();
    }
}