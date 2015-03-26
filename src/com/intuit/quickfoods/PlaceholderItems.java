package com.intuit.quickfoods;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.intuit.quickfoods.data.ItemsManager;

public class PlaceholderItems extends PlaceholderBase {
	
	public PlaceholderItems() {
		super();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view= inflater.inflate(R.layout.fragment_items,
				container, false);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, ItemsManager.getAllItems(getActivity(), ItemsManager.COLUMN_ITEM));
        AutoCompleteTextView search_items= (AutoCompleteTextView)
                view.findViewById(R.id.search_items);
        search_items.setAdapter(adapter);
		
		return view;
	}
}
