package com.intuit.quickfoods;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.intuit.quickfoods.data.Base;
import com.intuit.quickfoods.data.ItemsManager;
import com.intuit.quickfoods.data.MenuItem;
import com.intuit.quickfoods.data.OrderManager;
import com.intuit.quickfoods.helpers.BillPrinterManager;
import com.intuit.quickfoods.helpers.DataSender;
import com.intuit.quickfoods.helpers.SwipeDismissListViewTouchListener;
import com.intuit.quickfoods.helpers.SwipeDismissTouchListener;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderTakeOrder extends PlaceholderBase {
    private View m_view;
    private ViewGroup m_itemsContainer; // placeholder for items container
    private List<ContentValues> m_food_items; // items added to order list
    private GridView menu_grid; // placeholder for m_menu
    private List m_menu_list; // elements in the grid
    private MenuItem m_menu; // menu taken from json
    private String m_table_no_selected;

    public PlaceholderTakeOrder() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m_view = inflater.inflate(R.layout.fragment_take_order,
                container, false);

        String menu_json = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Base
                .MENU_ITEMS, Base.SAMPLE_MENU);
        Gson gson = new Gson();
        m_menu = gson.fromJson(menu_json, MenuItem.class);

        menu_grid = (GridView) m_view.findViewById(R.id.menu_items);
        m_menu_list = new ArrayList<String>();
        for(String table: OrderManager.getTables(getActivity())){
            m_menu_list.add(table);
        }
        m_menu_list.add("+");
        ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity(),
                R.layout.menu_item_tile, m_menu_list){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                // TODO set colors here
                return view;
            }
        };
        menu_grid.setAdapter(adp);
        menu_grid.setOnItemClickListener(new TablesOnItemClickListener());
        setNavigationTitle("Tables");
        /*
        new ShowcaseView.Builder(getActivity())
                .setTarget(new ActionViewTarget(getActivity(), ActionViewTarget.Type.HOME))
                .setContentTitle("ShowcaseView")
                .setContentText("This is highlighting the Home button")
                .hideOnTouchOutside()
                .build();
                */
        return m_view;
    }

    private void setNavigationTitle(String title){
        TextView tv = (TextView) m_view.findViewById(R.id.menu_breadcrumbs);
        tv.setText(title);
    }

    // when m_menu item is clicked
    private class TablesOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO take care of plus
            final String table_no = (String) m_menu_list.get(position);
            if(table_no.equals("+")){
                setNavigationTitle("Free Tables");
                List<String> open_table_list = new ArrayList<>();
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences
                                (getActivity());
                int max_tables = Integer.parseInt(prefs.getString(Base.MAX_TABLES,
                        getActivity().getResources()
                                .getString(R.string.default_max_tables)));
                for (int i = 1; i <= max_tables; i++){
                    if(!m_menu_list.contains(String.valueOf(i))){
                        open_table_list.add(String.valueOf(i));
                    }
                }
                m_menu_list.clear();
                m_menu_list.addAll(open_table_list);
            }
            else {
                m_table_no_selected = table_no;
                setNavigationTitle("Table "+m_table_no_selected+" - Categories");
                try {
                    ((ViewStub) m_view.findViewById(R.id.stub_import_order_items_load)).inflate();
                } catch (Exception e) {
                }

                m_food_items = OrderManager.getAllItemsFromTable(getActivity(), OrderManager.COLUMN_TABLE_NO + " = " + table_no);
                refreshFoodItemList();

                // SUBMIT BUTTON CLICK
                Button submit_button = (Button) m_view.findViewById(R.id.submit_bill);
                submit_button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DataSender(getActivity()).submit_order(table_no);
                        OrderManager.submit_order(getActivity(), table_no);
                        m_food_items = OrderManager.getAllItemsFromTable(getActivity(), OrderManager.COLUMN_TABLE_NO + " = " + table_no);
                        refreshFoodItemList();
                    }
                });

                // MAKE BILL BUTTON
                Button bill_button = (Button) m_view.findViewById(R.id.make_bill);
                bill_button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO check this
                        BillPrinterManager printer = new BillPrinterManager(getActivity());
                        printer.doWebViewPrint();
                    }
                });

                m_menu_list.clear();
                for (MenuItem item : m_menu.subMenuItems) {
                    m_menu_list.add(item.name);
                }

                menu_grid.setOnItemClickListener(new MenuItemOnItemClickListener());
            }
            menu_grid.invalidateViews();
        }
    }

    // when m_menu item is clicked
    private class MenuItemOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setNavigationTitle("Table "+m_table_no_selected+" - "+ m_menu_list.get(position));
            MenuItem clicked = m_menu.search((String) m_menu_list.get(position));
            if (clicked != null) {
                m_menu_list.clear();
                if (clicked.subMenuItems != null) {
                    for (MenuItem subMenuItem : clicked.subMenuItems) {
                        m_menu_list.add(subMenuItem.name);
                    }
                } else {
                    for (ContentValues food_menu_item : ItemsManager.getAllItemsForCategory
                            (getActivity(),
                                    clicked.name)) {
                        m_menu_list.add(food_menu_item.getAsString(ItemsManager.COLUMN_ITEM));

                    }
                    menu_grid.setNumColumns(1);
                    menu_grid.setOnItemClickListener(new FoodItemOnClickListener());
                }
            }
            menu_grid.invalidateViews();
        }
    }

    //TODO back button
    // What happens when FoodItems are clicked
    private class FoodItemOnClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            String item = (String) m_menu_list.get(position);
            TextView newItemCount = (TextView) m_view.findViewById(R.id.take_order_count);
            if (newItemCount != null) {
                int newItemCountValue = Integer.parseInt(newItemCount.getText().toString());

                long order_id = OrderManager.newOrderItem(getActivity(), m_table_no_selected,
                        newItemCountValue, item);
                ContentValues order = OrderManager.newOrderItemValue(getActivity(),
                        m_table_no_selected, newItemCountValue, item);
                order.put(OrderManager.ORDER_ID, order_id);
                m_food_items.add(order);
                refreshFoodItemList();
            }
        }
    }

    public SwipeDismissTouchListener TouchListener(final TextView food_list_item) {
        return new SwipeDismissTouchListener(
                food_list_item, null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        m_itemsContainer.removeView(food_list_item);

                        // inform kitchen
                        if (Integer.parseInt(OrderManager.getColumn(getActivity(),
                                food_list_item.getId(),
                                OrderManager.COLUMN_STATUS)) == Base.ITEM_IN_KITCHEN) {
                            new DataSender(getActivity()).send_delete_order(food_list_item.getId());
                        }

                        OrderManager.deleteOrderItem(getActivity(), food_list_item.getId());

                        m_food_items = OrderManager.getAllItemsFromTable(getActivity(),
                                OrderManager.COLUMN_TABLE_NO + " = " + m_table_no_selected);
                        refreshFoodItemList();

                    }
                });
    }

    // new food list item
    public TextView FoodListItem(final String itemValue, int count, int itemStatus, String directions, final int order_id) {

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
        } else {
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
            food_list_item.setOnTouchListener(TouchListener(food_list_item));
        }
        return food_list_item;
    }

    // Todo pass data as an argument
    public void refreshFoodItemList() {
        try {
            m_itemsContainer.removeAllViews();
        } catch (Exception e) {
        }

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
        m_itemsContainer = (ViewGroup) m_view.findViewById(R.id.take_order_dismissable_container);
        for (ContentValues item : m_food_items) {
            TextView food_list_item = FoodListItem(
                    item.getAsString(OrderManager.COLUMN_ORDER_ITEM),
                    item.getAsInteger(OrderManager.COLUMN_ITEM_COUNT),
                    item.getAsInteger(OrderManager.COLUMN_STATUS),
                    item.getAsString(OrderManager.COLUMN_DIRECTIONS),
                    item.getAsInteger(OrderManager.ORDER_ID)
            );
            m_itemsContainer.addView(food_list_item);
        }
    }

    public void takeOrderDetails(final int order_id, String item) {
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
                String oldDirections = OrderManager.getColumn(getActivity(), order_id, OrderManager.COLUMN_DIRECTIONS);
                String directionsValue = directionsBox.getText().toString();
                if (!directionsValue.equals(oldDirections)) {
                    OrderManager.updateOrder(getActivity(), order_id, OrderManager.COLUMN_DIRECTIONS, directionsValue);
                    m_food_items = OrderManager.getAllItemsFromTable(getActivity(),
                            OrderManager.COLUMN_TABLE_NO + " = " + m_table_no_selected);
                    refreshFoodItemList();
                    // send kitchen
                    new DataSender(getActivity()).send_directions(order_id, directionsValue);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}