package com.intuit.quickfoods;


import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.intuit.quickfoods.data.Base;
import com.intuit.quickfoods.helpers.DbHelper;
import com.intuit.quickfoods.helpers.NavigationDrawer;

import java.util.Arrays;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawer.NavigationDrawerCallbacks {

    /**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawer mNavigationDrawerFragment;

    // holds the current fragment
    private PlaceholderBase mActivePlaceHolder;
	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

    public Intent mQuickFoodsServiceIntent;
    public IQuickFoodsService mIQuickFoodsService;
    private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

         prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // create FTU shared preference if it doesn't exist already
        if(!prefs.contains(Base.FTU_SETUP_DISABLED)){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Base.FTU_SETUP_DISABLED,false);
            editor.commit();
        }
        // TODO download menu items from the server and store that instead of sample menu items
        if(!prefs.contains(Base.MENU_ITEMS)){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Base.MENU_ITEMS, Base.SAMPLE_MENU);
            editor.commit();
        }
        // if it is first time
        if (!prefs.getBoolean(Base.FTU_SETUP_DISABLED,false)){
            //Remove notification bar
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.welcome);


            Button setupWaiter = (Button)findViewById(R.id.setup_waiter);
            setupWaiter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Base.FTU_SETUP_DISABLED,true);
                    editor.putBoolean(Base.IS_KITCHEN,false);
                    editor.commit();
                    finish();
                    startActivity(getIntent());
                }
            });

            Button setupKitchen= (Button)findViewById(R.id.setup_kitchen);
            setupKitchen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Base.FTU_SETUP_DISABLED,true);
                    editor.putBoolean(Base.IS_KITCHEN,true);
                    editor.commit();
                    finish();
                    startActivity(getIntent());
                }
            });
        } else {

            setContentView(R.layout.activity_main);
            //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            //setSupportActionBar(toolbar);
            mNavigationDrawerFragment = (NavigationDrawer) getSupportFragmentManager()
                    .findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));

            // switch to kitchen window id it is kitchen
            if(prefs.getBoolean(Base.IS_KITCHEN, false)){
                onNavigationDrawerItemSelected(Arrays.asList(Base.nav_drawer_items).indexOf(Base.kitchen));
            }

            // Setup db
            DbHelper db = new DbHelper(this);
            db.getWritableDatabase();

//        Start service
            mQuickFoodsServiceIntent = new Intent(this, QuickFoodsService.class);
            mQuickFoodsServiceIntent.setData(Uri.parse("Some data"));
            this.startService(mQuickFoodsServiceIntent);


            ServiceConnection mConnection = new ServiceConnection() {

                public void onServiceDisconnected(ComponentName name) {
                    mIQuickFoodsService = null;
                }

                public void onServiceConnected(ComponentName name, IBinder service) {
                    mIQuickFoodsService = IQuickFoodsService.Stub.asInterface(service);
                }
            };

            bindService(new Intent(MainActivity.this, QuickFoodsService.class), mConnection, BIND_AUTO_CREATE);


        }
    }

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();

		mTitle = Base.nav_drawer_items[position];

		if (mTitle == Base.take_order) {
            mActivePlaceHolder = new PlaceholderTakeOrder();
		}
        else if (mTitle == Base.tables) {
            mActivePlaceHolder = new PlaceholderTables();
        }
		else if (mTitle == Base.items){
            mActivePlaceHolder = new PlaceholderItems();
		}
        else if (mTitle == Base.kitchen){
            mActivePlaceHolder = new PlaceholderKitchen();
        }
        else if (mTitle == Base.history){
            mActivePlaceHolder = new PlaceholderHistory();
        }
        else if (mTitle == Base.review){
            mActivePlaceHolder = new PlaceholderReviews();
        }

        if(mActivePlaceHolder != null){
            fragmentManager.beginTransaction().replace(R.id.container,
                    mActivePlaceHolder).commit();
        }
		else {
			/*
			 * Dont remove this! Initially when no item is loaded it will take
			 * baseplaceholder and prevents crash
			 */
			fragmentManager.beginTransaction()
					.replace(R.id.container, new PlaceholderBase())
					.commit();
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

        if (prefs.getBoolean(Base.FTU_SETUP_DISABLED,false)) {
            if (!mNavigationDrawerFragment.isDrawerOpen()) {
                // Only show items in the action bar relevant to this screen
                // if the drawer is not showing. Otherwise, let the drawer
                // decide what to show in the action bar.
                getMenuInflater().inflate(R.menu.main, menu);
                restoreActionBar();
                return true;
            }
        }
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onBackPressed() {
        if(mActivePlaceHolder != null){
            mActivePlaceHolder.onBackPressed();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, QuickFoodsService.class));
        super.onDestroy();
    }
}
