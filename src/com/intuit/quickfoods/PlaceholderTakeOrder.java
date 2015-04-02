package com.intuit.quickfoods;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
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
import com.intuit.quickfoods.data.FoodMenuItem;
import com.intuit.quickfoods.data.ItemsManager;
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
    private ViewGroup mItemsContainer; // placeholder for items container
    private ViewGroup mItemsCountContainer; // placeholder for items count
    private List<ContentValues> mFoodItems; // items added to order list
    private GridView mMenuGrid; // placeholder for m_menu
    private List<String> m_menu_list; // elements in the grid
    private FoodMenuItem m_menu; // menu taken from json
    private String m_table_no_selected;
    private int mDefaultGridColumnNos = -1;
    private View mOrderStub;
    private List<Integer> mMenuGridColors = new ArrayList<>();
    private List<Integer> mMenuGridBackground = new ArrayList<>(); // this is used when items are
    private int mGridNumColumns;
    // stored

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

        mMenuGrid = (GridView) m_view.findViewById(R.id.menu_items);
        m_menu_list = new ArrayList<>();
        setFirstPageGridElements();

        ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity(),
                R.layout.menu_item_tile, m_menu_list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (mMenuGridColors.size() > 0 && mMenuGridColors.get(position) != null) {
                    TextView tile = (TextView) view.findViewById(R.id.menu_item_element);
                    tile.setBackgroundColor(mMenuGridColors.get(position));
                } else if (mMenuGridBackground.size() > 0 && mMenuGridBackground.get(position) !=
                        null) {
                    TextView tile = (TextView) view.findViewById(R.id.menu_item_element);
                    tile.setBackgroundResource(mMenuGridBackground.get(position));
                }
                view.forceLayout();
                return view;
            }
        };
        mMenuGrid.setAdapter(adp);
        mMenuGrid.setOnItemClickListener(new TablesOnItemClickListener());
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
        mMenuGridBackground.clear();
        Random rnd = new Random();
        for (int i = 0; i < m_menu_list.size(); i++) {
            int color;
            if (isRandom) {
                color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                mMenuGridColors.add(color);
            } else {
                FoodMenuItem foodMenuItem = m_menu.search(m_menu_list.get(i));
                if (foodMenuItem == null) {
                    String description = ItemsManager.getDescription(getActivity(),
                            m_menu_list.get(i));
                    // default color
                    if (description != null) {
                        if (description.equals(Base.VEG))
                            color = Base.COLOR_VEG;
                        else if (description.equals(Base.NON_VEG))
                            color = Base.COLOR_NON_VEG;
                        else
                            color = R.drawable.black_blue_border;
                        mMenuGridBackground.add(color);
                    }
                } else {
                    color = foodMenuItem.color;
                    mMenuGridColors.add(color);
                }
            }
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

                mFoodItems = OrderManager.getAllItemsFromTable(getActivity(), OrderManager.COLUMN_TABLE_NO + " = " + table_no);
                refreshFoodItemList();

                // SUBMIT BUTTON CLICK
                Button submit_button = (Button) m_view.findViewById(R.id.submit_bill);
                submit_button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DataSender(getActivity()).submit_order(table_no);
                        OrderManager.submit_order(getActivity(), table_no);
                        mFoodItems = OrderManager.getAllItemsFromTable(getActivity(), OrderManager.COLUMN_TABLE_NO + " = " + table_no);
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

                mMenuGrid.setOnItemClickListener(new MenuItemOnItemClickListener());
            }
            setProperties();
            mMenuGrid.invalidateViews();
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
            } else if (m_table_no_selected == null) {
                setNavigationTitle("Free Tables");
                setGridColors(true);
            } else {
                setNavigationTitle("Table " + m_table_no_selected + " - Categories");
                setGridColors(false);
            }
            if (mDefaultGridColumnNos == -1) {
                mDefaultGridColumnNos = mMenuGrid.getNumColumns();
                mGridNumColumns = mDefaultGridColumnNos;
            }
            mMenuGrid.setNumColumns(mGridNumColumns);
        }

        public void setBackPressed() {
            isBackPressed = true;
            setProperties();
        }
    }

    // when m_menu item is clicked
    private class MenuItemOnItemClickListener implements OnItemClickListener {
        private String mNavigationTitle;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            addHistory(this);

            mNavigationTitle = "Table " + m_table_no_selected + " - " + m_menu_list.get(position);
            FoodMenuItem clicked = m_menu.search(m_menu_list.get(position));
            if (clicked != null) {
                m_menu_list.clear();
                if (clicked.subMenuItems != null) {
                    mGridNumColumns = mDefaultGridColumnNos;
                    for (FoodMenuItem subFoodMenuItem : clicked.subMenuItems) {
                        m_menu_list.add(subFoodMenuItem.name);
                    }
                } else {
                    mGridNumColumns = 1;
                    for (ContentValues food_menu_item : ItemsManager.getAllItemsForCategory
                            (getActivity(),
                                    clicked.name)) {
                        m_menu_list.add(food_menu_item.getAsString(ItemsManager.COLUMN_ITEM));

                    }
                    mMenuGrid.setOnItemClickListener(new FoodItemOnClickListener());
                }
            }

            setProperties();
            mMenuGrid.invalidateViews();
        }

        public void setProperties() {
            mMenuGrid.setNumColumns(mGridNumColumns);
            setGridColors(false);
            setNavigationTitle(mNavigationTitle);
        }
        public void setBackPressed(){
            mGridNumColumns = mDefaultGridColumnNos;
            setProperties();
        }
    }

    // What happens when FoodItems are clicked
    private class FoodItemOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            String item = m_menu_list.get(position);
            // todo check if the item already exists, if yes get the count and increment it by 1
            // todo else set default value 1
            int newItemCountValue = 1;
            ContentValues searchedFoodItem = null;
            for (ContentValues foodItem : mFoodItems) {
                if (foodItem.getAsInteger(OrderManager.COLUMN_STATUS) == Base.ITEM_CREATED_STATUS) {
                    if (foodItem.getAsString(OrderManager.COLUMN_ORDER_ITEM) == item) {
                        newItemCountValue = foodItem.getAsInteger(OrderManager.COLUMN_ITEM_COUNT)
                                + 1;
                        searchedFoodItem = foodItem;
                    }
                }
            }

            long order_id;
            if (newItemCountValue > 1 && searchedFoodItem != null) {
                order_id = searchedFoodItem.getAsInteger(OrderManager.ORDER_ID);
                OrderManager.updateOrder(getActivity(), (int) order_id,
                        OrderManager.COLUMN_ITEM_COUNT,
                        String.valueOf(newItemCountValue));
                mFoodItems.remove(searchedFoodItem);
            } else {
                order_id = OrderManager.newOrderItem(getActivity(), m_table_no_selected,
                        newItemCountValue, item);
            }
            ContentValues order = OrderManager.newOrderItemValue(getActivity(),
                    m_table_no_selected, newItemCountValue, item);
            order.put(OrderManager.ORDER_ID, order_id);
            mFoodItems.add(order);
            refreshFoodItemList();
        }
    }

    public SwipeDismissTouchListener TouchListener(final TextView food_list_item,
                                                   final TextView food_list_item_count) {
        return new SwipeDismissTouchListener(
                food_list_item, null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        mItemsContainer.removeView(food_list_item);
                        mItemsContainer.removeView(food_list_item_count);

                        // inform kitchen
                        if (Integer.parseInt(OrderManager.getColumn(getActivity(),
                                food_list_item.getId(),
                                OrderManager.COLUMN_STATUS)) == Base.ITEM_IN_KITCHEN) {
                            new DataSender(getActivity()).send_delete_order(food_list_item.getId());
                        }

                        OrderManager.deleteOrderItem(getActivity(), food_list_item.getId());

                        mFoodItems = OrderManager.getAllItemsFromTable(getActivity(),
                                OrderManager.COLUMN_TABLE_NO + " = " + m_table_no_selected);
                        refreshFoodItemList();

                    }
                });
    }


    public void refreshFoodItemList() {
        try {
            mItemsContainer.removeAllViews();
            mItemsCountContainer.removeAllViews();
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

        // Set up normal ViewGroup
        mItemsContainer = (ViewGroup) m_view.findViewById(R.id.take_order_dismissable_container);
        mItemsCountContainer = (ViewGroup) m_view.findViewById(R.id
                .take_order_count);
        // show in reverse order
        for (int index = mFoodItems.size() - 1; index >= 0; index--) {
            FoodListItemView fv = new FoodListItemView(
                    mFoodItems.get(index).getAsString(OrderManager.COLUMN_ORDER_ITEM),
                    mFoodItems.get(index).getAsInteger(OrderManager.COLUMN_ITEM_COUNT),
                    mFoodItems.get(index).getAsInteger(OrderManager.COLUMN_STATUS),
                    mFoodItems.get(index).getAsString(OrderManager.COLUMN_DIRECTIONS),
                    mFoodItems.get(index).getAsInteger(OrderManager.ORDER_ID));
            mItemsContainer.addView(fv.mFoodItemView);
            mItemsCountContainer.addView(fv.mItemCountView);
        }
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
                ((MenuItemOnItemClickListener) listener).setBackPressed();
            }

            mMenuGrid.setOnItemClickListener(listener);
            mMenuGrid.invalidateViews();
        }
        // TODO if back is pressed from tables page, ask if you want to exit
        super.onBackPressed();
    }

    private class FoodListItemView {
        public TextView mFoodItemView, mItemCountView;

        FoodListItemView(final String itemValue, final int count, int itemStatus,
                         String directions, final int order_id) {

            mFoodItemView = new TextView(getActivity());
            mItemCountView = new TextView(getActivity());
            setStyle(mFoodItemView, itemStatus);
            setStyle(mItemCountView, itemStatus);

            mFoodItemView.setId(order_id);
            mFoodItemView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mItemCountView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            mItemCountView.setText("" + count);
            if (itemStatus == Base.ITEM_CREATED_STATUS) {
                mItemCountView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setItemCountDialog(order_id, count);
                    }
                });
            }

            // At this point the views are not created yet. Hence creating an observer to set the
            // height of mItemCountView from mFoodItemView
            try {
                final ViewTreeObserver observer = mItemCountView.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mItemCountView.setHeight(mFoodItemView.getHeight());
                        if (observer.isAlive())
                            observer.removeOnGlobalLayoutListener(this);
                    }
                });
            } catch (Exception e) {
            }

            if (directions.isEmpty()) {
                mFoodItemView.setText(itemValue);
            } else {
                mFoodItemView.setTextSize(20.8f);
                mFoodItemView.setText(itemValue + "\n" + directions);
                mFoodItemView.setPadding(10, 15, 10, 15);
            }

            // if item is complete it shouldn't be able to dismiss it and shouldn't be able to add directions
            if (itemStatus != Base.ITEM_COMPLETE) {
                mFoodItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        takeOrderDetails(order_id, itemValue);
                    }
                });
                mFoodItemView.setOnTouchListener(TouchListener(mFoodItemView, mItemCountView));
            }
        }

        private void setStyle(TextView tv, int itemStatus) {
            tv.setTextAppearance(getActivity(), R.style.Theme_Quickfoods_ItemListTextView);
            tv.setBackgroundResource(Base.ITEM_BORDER[itemStatus]);
            tv.setTextColor(getResources().getColor(R.color.white));
            tv.setPadding(10, 20, 10, 20);

        }

        private void takeOrderDetails(final int order_id, String item) {
            AlertDialog.Builder detailsDialog = new AlertDialog.Builder(getActivity());
            detailsDialog.setTitle(item);
            detailsDialog.setMessage("Directions");

            final EditText directionsBox = new EditText(getActivity());
            // load the previously entered
            directionsBox.setText(OrderManager.getColumn(getActivity(), order_id, OrderManager.COLUMN_DIRECTIONS));
            directionsBox.selectAll();
            detailsDialog.setView(directionsBox);

            detailsDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String oldDirections = OrderManager.getColumn(getActivity(), order_id, OrderManager.COLUMN_DIRECTIONS);
                    String directionsValue = directionsBox.getText().toString();
                    if (!directionsValue.equals(oldDirections)) {
                        OrderManager.updateOrder(getActivity(), order_id, OrderManager.COLUMN_DIRECTIONS, directionsValue);
                        mFoodItems = OrderManager.getAllItemsFromTable(getActivity(),
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

        private void setItemCountDialog(final int order_id, int count) {
            AlertDialog.Builder countDialog = new AlertDialog.Builder(getActivity());
            countDialog.setTitle("Count");
            countDialog.setMessage("Directions");

            final EditText countBox = new EditText(getActivity());
            countBox.setInputType(InputType.TYPE_CLASS_NUMBER);
            countBox.setText("" + count);
            countBox.selectAll();
            countDialog.setView(countBox);

            countDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String oldDirections = OrderManager.getColumn(getActivity(), order_id,
                            OrderManager.COLUMN_ITEM_COUNT);
                    String directionsValue = countBox.getText().toString();
                    if (!directionsValue.equals(oldDirections)) {
                        OrderManager.updateOrder(getActivity(), order_id, OrderManager.COLUMN_ITEM_COUNT,
                                directionsValue);
                        mFoodItems = OrderManager.getAllItemsFromTable(getActivity(),
                                OrderManager.COLUMN_TABLE_NO + " = " + m_table_no_selected);
                        refreshFoodItemList();
                    }
                }
            });

            countDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // do nothing
                }
            });
            countDialog.show();
        }
    }


}