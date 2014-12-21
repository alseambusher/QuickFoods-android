package com.intuit.quickfoods;


import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements
		NavigationDrawer.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawer mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

    private Intent mQuickFoodsServiceIntent;
    IQuickFoodsService mIQuickFoodsService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawer) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

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

    // send data to connected devices through the service
    public boolean sendData(String message){
        try {
            return mIQuickFoodsService.send(message);
        } catch (RemoteException e) {
            return false;
        }
    }

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();

		mTitle = Constants.nav_drawer_items[position];

		if (mTitle == Constants.take_order) {
			fragmentManager.beginTransaction().replace(R.id.container,
							new PlaceholderTakeOrder()).commit();
		}
		else if (mTitle == Constants.items){
			fragmentManager.beginTransaction().replace(R.id.container,
                    new PlaceholderItems()).commit();
		}
        else if (mTitle == Constants.kitchen){
            fragmentManager.beginTransaction().replace(R.id.container,
                    new PlaceholderKitchen()).commit();
        }
        else if (mTitle == Constants.history){
            fragmentManager.beginTransaction().replace(R.id.container,
                    new PlaceholderHistory()).commit();
        }
        else if (mTitle == Constants.kitchen){
            fragmentManager.beginTransaction().replace(R.id.container,
                    new PlaceholderReviews()).commit();
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
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
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

}
