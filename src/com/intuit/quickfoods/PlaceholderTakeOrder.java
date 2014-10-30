package com.intuit.quickfoods;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceholderTakeOrder extends PlaceholderBase {
	
	public PlaceholderTakeOrder() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_take_order,
				container, false);
		
		Button table_no_go = (Button) view.findViewById(R.id.button1);
		table_no_go.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView table_no = (TextView) view.findViewById(R.id.take_order_table_no) ;
				String table_no_value = table_no.getText().toString();
				Toast.makeText(getActivity(), "Loading Table "+table_no_value, Toast.LENGTH_SHORT).show();
			}
		});
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Data.food_items);
        AutoCompleteTextView take_order_add_item= (AutoCompleteTextView)
                view.findViewById(R.id.take_order_add_item);
        take_order_add_item.setAdapter(adapter);
		
		return view;
	}
	
	public void refreshItemList(){
		// TODO
	}
}