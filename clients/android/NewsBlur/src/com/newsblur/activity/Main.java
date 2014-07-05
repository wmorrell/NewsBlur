package com.newsblur.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.newsblur.R;
import com.newsblur.fragment.FolderListFragment;
import com.newsblur.fragment.LogoutDialogFragment;
import com.newsblur.service.BootReceiver;
import com.newsblur.service.NBSyncService;
import com.newsblur.util.FeedUtils;
import com.newsblur.view.StateToggleButton.StateChangedListener;

public class Main extends NbActivity implements StateChangedListener {

	private ActionBar actionBar;
	private FolderListFragment folderFeedList;
	private FragmentManager fragmentManager;
	private Menu menu;

	@Override
	public void onCreate(Bundle savedInstanceState) {

        PreferenceManager.setDefaultValues(this, R.layout.activity_settings, false);

		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		setupActionBar();

		fragmentManager = getFragmentManager();
		folderFeedList = (FolderListFragment) fragmentManager.findFragmentByTag("folderFeedListFragment");
		folderFeedList.setRetainInstance(true);

        // make sure the interval sync is scheduled, since we are the root Activity
        BootReceiver.scheduleSyncService(this);
	}

    @Override
    protected void onResume() {
        super.onResume();
        // this view doesn't show stories, it is safe to perform cleanup
        NBSyncService.enableCleanup(true);
        triggerSync();
    }

	private void setupActionBar() {
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_profile) {
			Intent profileIntent = new Intent(this, Profile.class);
			startActivity(profileIntent);
			return true;
		} else if (item.getItemId() == R.id.menu_refresh) {
            NBSyncService.forceFeedsFolders();
			triggerSync();
			return true;
		} else if (item.getItemId() == R.id.menu_add_feed) {
			Intent intent = new Intent(this, SearchForFeeds.class);
			startActivityForResult(intent, 0);
			return true;
		} else if (item.getItemId() == R.id.menu_logout) {
			DialogFragment newFragment = new LogoutDialogFragment();
			newFragment.show(getFragmentManager(), "dialog");
		} else if (item.getItemId() == R.id.menu_settings) {
            Intent settingsIntent = new Intent(this, Settings.class);
            startActivity(settingsIntent);
            return true;
        }
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void changedState(int state) {
		folderFeedList.changeState(state);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			folderFeedList.hasUpdated();
		}
	}

    @Override
	public void handleUpdate() {
		folderFeedList.hasUpdated();
        if (NBSyncService.isFeedFolderSyncRunning()) {
		    setProgressBarIndeterminateVisibility(true);
            setRefreshEnabled(false);
        } else {
		    setProgressBarIndeterminateVisibility(false);
            setRefreshEnabled(true);
        }
	}

    private void setRefreshEnabled(boolean enabled) {
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.menu_refresh);
            if (item != null) {
                item.setEnabled(enabled);
            }
        }
    }
            

}
