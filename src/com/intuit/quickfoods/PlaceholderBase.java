package com.intuit.quickfoods;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaceholderBase extends Fragment {
	
	//private static final String ARG_SECTION_NUMBER = "section_number";

	public PlaceholderBase() {
		Bundle args = new Bundle();
		// you can put stuff into fragments
		//args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		this.setArguments(args);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// if you wanna send back to mainactivity do this:
		//((MainActivity) activity).onSectionAttached(getArguments().getInt(
				//ARG_SECTION_NUMBER));
	}
}