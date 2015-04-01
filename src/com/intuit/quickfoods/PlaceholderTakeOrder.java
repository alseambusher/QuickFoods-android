package com.intuit.quickfoods;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.intuit.quickfoods.data.Base;
import com.intuit.quickfoods.data.ItemsManager;
import com.intuit.quickfoods.data.FoodMenuItem;
import com.intuit.quickfoods.data.OrderManager;
import com.intuit.quickfoods.helpers.BillPrinterManager;
import com.intuit.quickfoods.helpers.DataSender;
import com.intuit.quickfoods.helpers.SwipeDismissListViewTouchListener;
import com.intuit.quickfoods.helpers.SwipeDismissTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlaceholderTakeOrder extends PlaceholderBase {
    private View m_view;
    private ViewGroup m_itemsContainer; // placeholder for items container
    private List<ContentValues> m_food_items; // items added to order list
    private GridView menu_grid; // placeholder for m_menu
    private List<String> m_menu_list; // elements in the grid
    private FoodMenuItem m_menu; // menu taken from json
    private String m_table_no_selected;
    private int mDefaultGridColumnNos = -1;
    private View mOrderStub;
    private List<Integer> mMenuGridColors = new ArrayList<>();

    // to manage back button
    private List<OnItemClickListener> mItemClickListenerHistory = new ArrayList<>();
    private List<List> mMenuListHistory = new ArrayList<>();

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
        m_menu = gson.fromJson(menu_json, FoodMenuItem.class);

        menu_grid = (GridView) m_view.findViewById(R.id.menu_items);
        m_menu_list = new ArrayList<>();
        setFirstPageGridElements();

        ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity(),
                R.layout.menu_item_tile, m_menu_list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (mMenuGridColors.get(position) != null) {
                    TextView tile = (TextView) view.findViewById(R.id.menu_item_element);
                    tile.setBackgroundColor(mMenuGridColors.get(position));
                }
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

    private void setGridColors(Boolean isRandom) {
        mMenuGridColors.clear();
        Random rnd = new Random();
        for (int i = 0; i < m_menu_list.size(); i++) {
            int color;
            if (isRandom) {
                color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            } else {
                FoodMenuItem foodMenuItem = m_menu.search(m_menu_list.get(i));
                if (foodMenuItem == null) {
                    // TODO set veg and non veg color
                    color = Color.BLUE;
                } else {
                    color = foodMenuItem.color;
                }
            }
            mMenuGridColors.add(color);
        }
    }

    private void setFirstPageGridElements() {
        m_menu_list.clear();
        for (String table : OrderManager.getTables(getActivity())) {
            m_menu_list.add(table);
        }
        m_menu_list.add("+");
        setGridColors(true);
    }

    private void setNavigationTitle(String title) {
        TextView tv = (TextView) m_view.findViewById(R.id.menu_breadcrumbs);
        tv.setText(title);
    }

    // when m_menu item is clicked
    private class TablesOnItemClickListener implements OnItemClickListener {
        private boolean isBackPressed = false;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mMenuListHistory.size() == 0)
                addHistory(this);

            final String table_no = m_menu_list.get(position);
            if (table_no.equals("+")) {
                List<String> open_table_list = new ArrayList<>();
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences
                                (getActivity());
                int max_tables = Integer.parseInt(prefs.getString(Base.MAX_TABLES,
                        getActivity().getResources()
                                .getString(R.string.default_max_tables)));
                for (int i = 1; i <= max_tables; i++) {
                    if (!m_menu_list.contains(String.valueOf(i))) {
                        open_table_list.add(String.valueOf(i));
                    }
                }
                m_menu_list.clear();
                m_menu_list.addAll(open_table_list);
            } else {
                m_table_no_selected = table_no;
                // if stub is not inflated then inflate it, else just set visibility to visible
                if (mOrderStub == null) {
                    mOrderStub = ((ViewStub) m_view.findViewById(R.id
                            .stub_import_order_items_load))
                            .inflate();
                } else {
                    mOrderStub.setVisibility(View.VISIBLE);
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
                for (FoodMenuItem item : m_menu.subMenuItems) {
                    m_menu_list.add(item.name);
                }

                menu_grid.setOnItemClickListener(new MenuItemOnItemClickListener());
            }
            setProperties();
            menu_grid.invalidateViews();
        }

        public void setProperties() {
            if (isBackPressed) {
                // clear table id
                m_table_no_selected = null;
                setNavigationTitle("Tables");
                setFirstPageGridElements();
                // hide order
                mOrderStub.setVisibility(View.GONE);
                isBackPressed = false;
            } else if (m_table_no_selected == null){
                setNavigationTitle("Free Tables");
                setGridColors(true);
            }
            else {
                setNavigationTitle("Table " + m_table_no_selected + " - Categories");
                setGridColors(false);
            }
            if (mDefaultGridColumnNos == -1)
                mDefaultGridColumnNos = menu_grid.getNumColumns();
            menu_grid.setNumColumns(mDefaultGridColumnNos);
        }

        public void setBackPressed() {
            isBackPressed = true;
            setProperties();
        }
    }

    // when m_menu item is clicked
    private class MenuItemOnItemClickListener implements OnItemClickListener {
        private String mNavigationTitle;
        private int mColumns;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            addHistory(this);

            mNavigationTitle = "Table " + m_table_no_selected + " - " + m_menu_list.get(position);
            FoodMenuItem clicked = m_menu.search(m_menu_list.get(position));
            if (clicked != null) {
                m_menu_list.clear();
                if (clicked.subMenuItems != null) {
                    mColumns = mDefaultGridColumnNos;
                    for (FoodMenuItem subFoodMenuItem : clicked.subMenuItems) {
                        m_menu_list.add(subFoodMenuItem.name);
                    }
                } else {
                    mColumns = 1;
                    for (ContentValues food_menu_item : ItemsManager.getAllItemsForCategory
                            (getActivity(),
                                    clicked.name)) {
                        m_menu_list.add(food_menu_item.getAsString(ItemsManager.COLUMN_ITEM));

                    }
                    menu_grid.setOnItemClickListener(new FoodItemOnClickListener());
                }
            }

            setProperties();
            menu_grid.invalidateViews();
        }

        public void setProperties() {
            setGridColors(false);
            menu_grid.setNumColumns(mColumns);
            setNavigationTitle(mNavigationTitle);
            // reset mColumns to handle back button
            mColumns = mDefaultGridColumnNos;
        }
    }

    // What happens when FoodItems are clicked
    private class FoodItemOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            String item = m_menu_list.get(position);
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

    private void addHistory(OnItemClickListener listener) {
        // add to history
        mItemClickListenerHistory.add(listener);
        List<String> current_menu_list = new ArrayList();
        for (String item : m_menu_list)
            current_menu_list.add(item);
        mMenuListHistory.add(current_menu_list);
    }

    @Override
    public void onBackPressed() {
        if (mMenuListHistory.size() > 0 && mItemClickListenerHistory.size() > 0) {
            m_menu_list.clear();
            m_menu_list.addAll(mMenuListHistory.remove(mMenuListHistory.size() - 1));

            OnItemClickListener listener = mItemClickListenerHistory.remove
                    (mItemClickListenerHistory.size() - 1);
            if (listener instanceof TablesOnItemClickListener) {
                ((TablesOnItemClickListener) listener).setBackPressed();
            } else if (listener instanceof MenuItemOnItemClickListener) {
                ((MenuItemOnItemClickListener) listener).setProperties();
            }

            menu_grid.setOnItemClickListener(listener);
            menu_grid.invalidateViews();
        }
        // TODO if back is pressed from tables page, ask if you want to exit
        super.onBackPressed();
    }
}