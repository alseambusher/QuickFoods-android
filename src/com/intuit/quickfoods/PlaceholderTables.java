package com.intuit.quickfoods;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderTables extends PlaceholderBase {

    public PlaceholderTables() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_tables,
                container, false);

        ListView lview= (ListView) view.findViewById(R.id.tables);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.container,
                        new PlaceholderTakeOrder()).commit();
                ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(Base.take_order);

            }
        });
        //fab.attachToListView(lview);

        List<Base.Table> tables = OrderManager.getTables(getActivity());

        final List<String> list = new ArrayList<>();
        Map<String,Integer> status = new HashMap<>();
        for (int i = 0; i < tables.size(); i+=2) {
            status.put(tables.get(i).table_no,tables.get(i).status);
            try {
                status.put(tables.get(i+1).table_no,tables.get(i+1).status);
                list.add("" + tables.get(i).table_no + "," + tables.get(i + 1).table_no);
            } catch (Exception e){
                list.add("" + tables.get(i).table_no);
            }
        }
        ArrayAdapter<String> adapter = new TablesAdapter(getActivity(), view,
                R.layout.table_tile, list, status);
        lview.setAdapter(adapter);
        return view;
    }
    private class TablesAdapter extends ArrayAdapter<String> {
        Context context;
        View view;
        List<String> objects;
        Map<String,Integer> status;
        public TablesAdapter (Context context, View view, int layoutResourceId,
                                  List objects, Map status) {
            super(context, layoutResourceId, objects);
            this.objects = objects;
            this.context = context;
            this.view = view;
            this.status = status;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            if (convertView == null)
                convertView = inflater.inflate(R.layout.table_tile, parent, false);
            String[] items = getItem(position).split(",");

            TextView tile1 = (TextView) convertView.findViewById(R.id.table_tile_element1);
            TextView tile2 = (TextView) convertView.findViewById(R.id.table_tile_element2);
            tile1.setText(items[0]);
            tile1.setBackground(context.getResources().getDrawable(Base.ITEM_BORDER[status.get(items[0])]));
            tile1.setOnClickListener(new TableClickListener(items[0]));
            if(items.length>1) {
                tile2.setText(items[1]);
                tile2.setBackground(context.getResources().getDrawable(Base.ITEM_BORDER[status.get(items[1])]));
                tile2.setOnClickListener(new TableClickListener(items[1]));
            }
            return convertView;
        }

        private class TableClickListener implements View.OnClickListener{
            String tableNo;
            TableClickListener(String tableNo){
                this.tableNo = tableNo;
            }

            @Override
            public void onClick(View v) {
                // TODO
            }
        }
    }
}
