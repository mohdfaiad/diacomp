package org.bosik.diacomp.android.frontend.activities;

import org.bosik.diacomp.android.BuildConfig;
import org.bosik.diacomp.android.R;
import org.bosik.diacomp.android.backend.common.Storage;
import org.bosik.diacomp.android.frontend.UIUtils;
import org.bosik.diacomp.android.frontend.fragments.FragmentBase;
import org.bosik.diacomp.android.utils.ErrorHandler;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActivityMain extends FragmentActivity
{
	/* =========================== CONSTANTS ================================ */

	static final String			TAG	= ActivityMain.class.getSimpleName();
	// private static final int RESULT_SPEECH_TO_TEXT = 620;

	/* =========================== FIELDS ================================ */

	DemoCollectionPagerAdapter	mDemoCollectionPagerAdapter;
	ViewPager					mViewPager;

	/* =========================== CLASSES ================================ */

	// TODO: вынести в отдельный модуль, отвязать
	// TODO: refresh diary view on productive (non-trivial) sync

	class AsyncTaskAnalyzeDiary extends AsyncTask<Void, Integer, Void>
	{
		// <Params, Progress, Result>
		private ProgressDialog	dialog;

		@Override
		protected void onPreExecute()
		{
			dialog = new ProgressDialog(ActivityMain.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setCancelable(false);
			// TODO: i18n
			dialog.setMessage("Analyzing...");
		}

		@Override
		protected Void doInBackground(Void... par)
		{
			publishProgress(50);
			Storage.analyzeKoofs();
			publishProgress(100);

			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			if (dialog.isShowing())
			{
				dialog.dismiss();
			}

			// TODO: i18n
			UIUtils.showTip(ActivityMain.this, "Model updated");
		}
	}

	/* =========================== METHODS ================================ */

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
		{
			if (BuildConfig.DEBUG)
			{
				UIUtils.showTip(this, "Debug mode is on");
			}

			// Core initialization
			ActivityPreferences.init(this);
			Storage.init(this, getContentResolver(), ActivityPreferences.preferences);

			// НАСТРОЙКА ИНТЕРФЕЙСА

			// устанавливаем макет
			setContentView(R.layout.activity_main);

			mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mDemoCollectionPagerAdapter);
			// mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
			// {
			// @Override
			// public void onPageSelected(int position)
			// {
			// getActionBar().setSelectedNavigationItem(position);
			// }
			// });

			// final ActionBar actionBar = getActionBar();

			// Specify that tabs should be displayed in the action bar.
			// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			// Create a tab listener that is called when the user changes tabs.
			// ActionBar.TabListener tabListener = new ActionBar.TabListener()
			// {
			// @Override
			// public void onTabSelected(Tab tab, FragmentTransaction ft)
			// {
			// mViewPager.setCurrentItem(tab.getPosition());
			// }
			//
			// @Override
			// public void onTabUnselected(Tab tab, FragmentTransaction ft)
			// {
			// // TODO Auto-generated method stub
			// }
			//
			// @Override
			// public void onTabReselected(Tab tab, FragmentTransaction ft)
			// {
			// // TODO Auto-generated method stub
			// }
			// };
			//
			// // Add 3 tabs, specifying the tab's text and TabListener
			// for (int i = 0; i < mDemoCollectionPagerAdapter.getCount(); i++)
			// {
			// CharSequence title = mDemoCollectionPagerAdapter.getPageTitle(i);
			// actionBar.addTab(actionBar.newTab().setText(title).setTabListener(tabListener));
			// }
		}
		catch (Exception e)
		{
			ErrorHandler.handle(e, this);
		}
	}

	// handled
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.diary_menu, menu);
		}
		catch (Exception e)
		{
			ErrorHandler.handle(e, this);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.item_diary_preferences:
			{
				Intent settingsActivity = new Intent(getBaseContext(), ActivityPreferences.class);
				startActivity(settingsActivity);
				return true;
			}
			// case R.id.item_base_addFood:
			// {
			// FoodItem food = new FoodItem();
			// food.setName(editSearch.getText().toString());
			// showFoodEditor(new Versioned<FoodItem>(food), true);
			// return true;
			// }
			// case R.id.item_base_addDish:
			// {
			// DishItem dish = new DishItem();
			// dish.setName(editSearch.getText().toString());
			// showDishEditor(new Versioned<DishItem>(dish), true);
			// return true;
			// }
			default:
			{
				return false;// super.onOptionsItemSelected(item);
			}
		}
	}
}

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter
{
	public DemoCollectionPagerAdapter(FragmentManager fm)
	{
		super(fm);
	}

	@Override
	public Fragment getItem(int i)
	{
		return new FragmentBase();
	}

	@Override
	public int getCount()
	{
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		switch (position)
		{
			case 0:
			{
				return "Diary";
			}
			case 1:
			{
				return "Bases";
			}
			default:
			{
				throw new IllegalArgumentException(String.format("Index %d is out of bounds", position));
			}
		}
	}
}