/*
 *  Diacomp - Diabetes analysis & management system
 *  Copyright (C) 2013 Nikita Bosik
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.bosik.diacomp.android.frontend.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import org.bosik.diacomp.android.R;
import org.bosik.diacomp.android.backend.features.analyze.KoofServiceInternal;
import org.bosik.diacomp.android.backend.features.preferences.account.PreferencesLocalService;
import org.bosik.diacomp.android.frontend.UIUtils;
import org.bosik.diacomp.android.frontend.fragments.FragmentMassUnitDialog;
import org.bosik.diacomp.android.frontend.fragments.chart.Chart;
import org.bosik.diacomp.android.frontend.fragments.chart.ProgressBundle;
import org.bosik.diacomp.android.utils.ErrorHandler;
import org.bosik.diacomp.core.entities.business.Rate;
import org.bosik.diacomp.core.services.analyze.KoofService;
import org.bosik.diacomp.core.services.analyze.entities.Koof;
import org.bosik.diacomp.core.services.analyze.entities.KoofList;
import org.bosik.diacomp.core.services.preferences.PreferenceID;
import org.bosik.diacomp.core.services.preferences.PreferencesTypedService;
import org.bosik.diacomp.core.utils.Utils;
import org.bosik.merklesync.HashUtils;
import org.bosik.merklesync.Versioned;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ActivityRates extends FragmentActivity implements DialogInterface.OnClickListener
{
	// Constants
	private static final String TAG                = ActivityRates.class.getSimpleName();
	private static final int    DIALOG_RATE_CREATE = 11;
	private static final int    DIALOG_RATE_MODIFY = 12;

	// components
	private Chart       chart;
	private ListView    list;
	private BaseAdapter adapter;

	// data
	private List<Versioned<Rate>>       rates; // TODO: save/restore on activity re-creation
	private List<List<Versioned<Rate>>> history;
	private int                         historyIndex;
	private boolean BU = true; // TODO: persist

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rates);

		PreferencesTypedService preferences = new PreferencesTypedService(new PreferencesLocalService(this));
		String data = preferences.getStringValue(PreferenceID.RATES_DATA);
		rates = readRatesSafely(data);

		history = new ArrayList<>();
		history.add(new ArrayList<>(rates));
		historyIndex = 0;

		list = (ListView) findViewById(R.id.listRates);
		list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long itemIndex)
			{
				showRateEditor(rates.get(position), false);
			}
		});
		list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener()
		{
			@Override
			public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b)
			{
				int selectedCount = list.getCheckedItemCount();
				setSubtitle(actionMode, selectedCount);
			}

			@Override
			public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
			{
				MenuInflater inflater = actionMode.getMenuInflater();
				inflater.inflate(R.menu.actions_rates_context, menu);
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode actionMode, Menu menu)
			{
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem)
			{
				SparseBooleanArray checkList = list.getCheckedItemPositions();
				Set<String> ids = new HashSet<>();
				for (int i = 0; i < checkList.size(); i++)
				{
					if (checkList.valueAt(i))
					{
						int index = checkList.keyAt(i);
						if (index >= 0 && index < rates.size())
						{
							ids.add(rates.get(index).getId());
						}
					}
				}

				if (!ids.isEmpty())
				{
					int count = 0;
					for (Iterator<Versioned<Rate>> i = rates.iterator(); i.hasNext(); )
					{
						if (ids.contains(i.next().getId()))
						{
							i.remove();
							count++;
						}
					}

					save(rates);
					saveStateToHistory();
					adapter.notifyDataSetChanged();

					String text = String.format(getString(R.string.base_tip_items_removed), count); // FIXME
					Toast.makeText(list.getContext(), text, Toast.LENGTH_LONG).show();
				}

				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode actionMode)
			{
			}

			private void setSubtitle(ActionMode mode, int selectedCount)
			{
				mode.setSubtitle(selectedCount == 0 ? null : String.valueOf(selectedCount));
			}
		});

		adapter = new BaseAdapter()
		{
			@Override
			public int getCount()
			{
				return rates.size();
			}

			@Override
			public Object getItem(int position)
			{
				if (position >= 0 && position < rates.size())
				{
					return rates.get(position);
				}
				else
				{
					return null;
				}
			}

			@Override
			public boolean hasStableIds()
			{
				return true;
			}

			@Override
			public long getItemId(int position)
			{
				Object item = getItem(position);
				if (item != null)
				{
					return ((Versioned<Rate>) item).getData().getTime();
				}
				else
				{
					return position;
				}
			}

			@Override
			public View getView(int position, View view, ViewGroup parent)
			{
				Versioned<Rate> rate = (Versioned<Rate>) getItem(position);

				if (rate != null)
				{
					if (view == null)
					{
						view = getLayoutInflater().inflate(R.layout.view_rate, null);
					}

					((TextView) view.findViewById(R.id.ratesItemTime)).setText(Utils.formatTimeMin(rate.getData().getTime()));

					((TextView) view.findViewById(R.id.ratesItemK)).setText(formatK(rate.getData(), BU));
					((TextView) view.findViewById(R.id.ratesItemKUnit)).setText(formatKUnit(BU));

					((TextView) view.findViewById(R.id.ratesItemQ)).setText(formatQ(rate.getData()));
					((TextView) view.findViewById(R.id.ratesItemQUnit)).setText(formatQUnit());

					((TextView) view.findViewById(R.id.ratesItemX)).setText(formatX(rate.getData(), BU));
					((TextView) view.findViewById(R.id.ratesItemXUnit)).setText(formatXUnit(BU));
				}

				view.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_base_item));
				return view;
			}

			@Override
			public void notifyDataSetChanged()
			{
				super.notifyDataSetChanged();
				invalidateOptionsMenu();
				if (chart != null)
				{
					chart.refresh();
				}
			}
		};

		list.setAdapter(adapter);

		// Check that the activity is using the layout version with
		// the fragment_container FrameLayout
		if (findViewById(R.id.ratesChart) != null)
		{
			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null)
			{
				return;
			}

			// Create a new Fragment to be placed in the activity layout
			chart = new Chart();
			chart.setChartType(Chart.ChartType.DAILY);
			updateChartTitle();
			chart.setDescription(getString(R.string.charts_insulin_consumption_daily_description));
			chart.setDataLoader(new ProgressBundle.DataLoader()
			{
				@Override
				public Collection<Series<?>> load(ContentResolver contentResolver)
				{
					// TODO: Probably it's better to reload rates here

					KoofList coefficients = buildCoefficients(rates);

					List<DataPoint> dataAvg = new ArrayList<>();
					if (coefficients != null)
					{
						for (int time = 0; time < Utils.MinPerDay; time += 5)
						{
							Koof c = coefficients.getKoof(time);
							double value = BU ? c.getK() / c.getQ() * Utils.CARB_PER_BU : c.getK() / c.getQ();
							dataAvg.add(new DataPoint((double) time / Utils.MinPerHour, value));
						}
					}

					LineGraphSeries<DataPoint> seriesAvg = new LineGraphSeries<>(dataAvg.toArray(new DataPoint[dataAvg.size()]));
					seriesAvg.setColor(getResources().getColor(R.color.charts_x));

					return Collections.<Series<?>>singletonList(seriesAvg);
				}
			});

			// In case this activity was started with special instructions from an
			// Intent, pass the Intent's extras to the fragment as arguments
			//chart.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction().add(R.id.ratesChart, chart).commit();
		}
	}

	private void updateChartTitle()
	{
		chart.setTitle(String.format("%s, %s/%s", getString(R.string.common_koof_x), getString(R.string.common_unit_insulin),
				BU ? getString(R.string.common_unit_mass_bu) : getString(R.string.common_unit_mass_gramm)));
	}

	private static KoofList buildCoefficients(List<Versioned<Rate>> rates)
	{
		if (rates == null || rates.isEmpty())
		{
			return null;
		}

		KoofList list = new KoofList();

		for (int i = -1; i < rates.size(); i++)
		{
			int iPreStart = i - 1;
			int iStart = i;
			int iEnd = i + 1;
			int iAfterEnd = i + 2;

			int nPreStart = (iPreStart + rates.size()) % rates.size();
			int nStart = (iStart + rates.size()) % rates.size();
			int nEnd = iEnd % rates.size();
			int nAfterEnd = iAfterEnd % rates.size();

			final Rate ratePreStart = rates.get(nPreStart).getData();
			final Rate rateStart = rates.get(nStart).getData();
			final Rate rateEnd = rates.get(nEnd).getData();
			final Rate rateAfterEnd = rates.get(nAfterEnd).getData();

			double tPreStart = (iPreStart >= 0) ? ratePreStart.getTime() : ratePreStart.getTime() - Utils.MinPerDay;
			double tStart = (iStart >= 0) ? rateStart.getTime() : rateStart.getTime() - Utils.MinPerDay;
			double tEnd = (iEnd < rates.size()) ? rateEnd.getTime() : rateEnd.getTime() + Utils.MinPerDay;
			double tAfterEnd = (iAfterEnd < rates.size()) ? rateAfterEnd.getTime() : rateAfterEnd.getTime() + Utils.MinPerDay;

			// (tPreStart, rates.get(nPreStart).getData().get_())
			// (tStart, rates.get(nStart).getData().get_())
			// (tEnd, rates.get(nEnd).getData().get_())
			// (tAfterEnd, rates.get(nAfterEnd).getData().get_())

			int tFrom = ((int) tStart >= 0) ? (int) tStart : 0;
			int tTo = ((int) tEnd < Utils.MinPerDay) ? (int) tEnd : Utils.MinPerDay - 1;

			Function<Double, Double> fK = cubeInterpolation(tPreStart, ratePreStart.getK(), tStart, rateStart.getK(), tEnd, rateEnd.getK(),
					tAfterEnd, rateAfterEnd.getK());

			for (int t = tFrom; t < tTo; t++)
			{
				list.getKoof(t).setK(fK.apply((double) t));
			}

			Function<Double, Double> fQ = cubeInterpolation(tPreStart, ratePreStart.getQ(), tStart, rateStart.getQ(), tEnd, rateEnd.getQ(),
					tAfterEnd, rateAfterEnd.getQ());

			for (int t = tFrom; t < tTo; t++)
			{
				list.getKoof(t).setQ(fQ.apply((double) t));
			}

			for (int t = tFrom; t < tTo; t++)
			{
				list.getKoof(t).setP(0.0);
			}
		}

		return list;
	}

	private interface Function<Input, Output>
	{
		Output apply(Input value);
	}

	public static Function<Double, Double> cubeInterpolation(double x1, double y1, double x2, double y2, double dy1, double dy2)
	{
		final double a = (dy1 + dy2) / (x2 - x1) / (x2 - x1) - 2 * (y2 - y1) / (x2 - x1) / (x2 - x1) / (x2 - x1);
		final double b = (y2 - y1) / (x2 - x1) / (x2 - x1) - (dy1 + a * (x2 * x2 + x1 * x2 - 2 * x1 * x1)) / (x2 - x1);
		final double c = dy1 - 3 * x1 * x1 * a - 2 * x1 * b;
		final double d = y1 - x1 * x1 * x1 * a - x1 * x1 * b - x1 * c;

		return new Function<Double, Double>()
		{
			@Override
			public Double apply(Double x)
			{
				return a * x * x * x + b * x * x + c * x + d;
			}
		};
	}

	public static Function<Double, Double> cubeInterpolation(double x0, double y0, double x1, double y1, double x2, double y2, double x3,
			double y3)
	{
		double dy1 = (y2 - y0) / (x2 - x0);
		double dy2 = (y3 - y1) / (x3 - x1);
		return cubeInterpolation(x1, y1, x2, y2, dy1, dy2);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.findItem(R.id.itemRatesUndo).setEnabled(historyIndex > 0);
		menu.findItem(R.id.itemRatesRedo).setEnabled(historyIndex < history.size() - 1);
		menu.findItem(R.id.itemRatesClear).setEnabled(!rates.isEmpty());
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.actions_rates, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemRatesAdd:
			{
				showRateEditor(null, true);
				return true;
			}

			case R.id.itemRatesUndo:
			{
				historyUndo();

				save(rates);
				adapter.notifyDataSetChanged();
				return true;
			}

			case R.id.itemRatesRedo:
			{
				historyRedo();

				save(rates);
				adapter.notifyDataSetChanged();
				return true;
			}

			case R.id.itemRatesReplaceWithAuto:
			{
				KoofService service = KoofServiceInternal.getInstance(this);

				rates.clear();
				for (int time = 0; time < Utils.MinPerDay; time += 2 * Utils.MinPerHour)
				{
					Koof c = service.getKoof(time);
					Versioned<Rate> versioned = new Versioned<>(new Rate(time, c));
					versioned.setId(HashUtils.generateGuid());
					rates.add(versioned);
				}

				save(rates);
				saveStateToHistory();
				adapter.notifyDataSetChanged();
				return true;
			}

			case R.id.itemRatesUnitMass:
			{
				FragmentMassUnitDialog newFragment = new FragmentMassUnitDialog();
				Bundle args = new Bundle();
				args.putBoolean(FragmentMassUnitDialog.KEY_BU, BU);
				newFragment.setArguments(args);
				newFragment.show(getFragmentManager(), "unitMassPicker");
				return true;
			}

			case R.id.itemRatesClear:
			{
				if (!rates.isEmpty())
				{
					rates.clear();
					save(rates);
					saveStateToHistory();
					adapter.notifyDataSetChanged();
				}
				return true;
			}

			default:
			{
				return false;// super.onOptionsItemSelected(item);
			}
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		System.out.println("Selected: " + which);
		BU = getString(R.string.common_unit_mass_bu).equals(getResources().getStringArray(R.array.unit_mass_options)[which]);

		adapter.notifyDataSetChanged();
		updateChartTitle();
	}

	private void saveStateToHistory()
	{
		history.add(new ArrayList<>(rates));
		historyIndex = history.size() - 1;
	}

	private void historyUndo()
	{
		if (historyIndex > 0)
		{
			historyIndex--;
			rates = new ArrayList<>(history.get(historyIndex));
		}
	}

	private void historyRedo()
	{
		if (historyIndex < history.size() - 1)
		{
			historyIndex++;
			rates = new ArrayList<>(history.get(historyIndex));
		}
	}

	// handled
	private void showRateEditor(Versioned<Rate> entity, boolean createMode)
	{
		try
		{
			if (createMode)
			{
				Rate rec = new Rate();
				rec.setTime(0); // FIXME
				entity = new Versioned<>(rec);
			}

			Intent intent = new Intent(this, ActivityEditorRate.class);
			intent.putExtra(ActivityEditor.FIELD_ENTITY, entity);
			intent.putExtra(ActivityEditor.FIELD_CREATE_MODE, createMode);
			intent.putExtra(ActivityEditorRate.KEY_INTENT_USE_BU, BU);

			startActivityForResult(intent, createMode ? DIALOG_RATE_CREATE : DIALOG_RATE_MODIFY);
		}
		catch (Exception e)
		{
			ErrorHandler.handle(e, this);
		}
	}

	private String formatK(Rate rate, boolean BU)
	{
		return "K: " + Utils.formatK(rate.getK(), BU);
	}

	private String formatKUnit(boolean BU)
	{
		String unitBS = getString(R.string.common_unit_bs_mmoll);
		String unitMass = BU ? getString(R.string.common_unit_mass_bu) : getString(R.string.common_unit_mass_gramm);
		return unitBS + "/" + unitMass;
	}

	private String formatQ(Rate rate)
	{
		return "Q: " + Utils.formatQ(rate.getQ());
	}

	private String formatQUnit()
	{
		String unitBS = getString(R.string.common_unit_bs_mmoll);
		String unitDosage = getString(R.string.common_unit_insulin);
		return unitBS + "/" + unitDosage;
	}

	private String formatX(Rate rate, boolean BU)
	{
		return "X: " + Utils.formatX(rate.getK() / rate.getQ(), BU);
	}

	private String formatXUnit(boolean BU)
	{
		String unitDosage = getString(R.string.common_unit_insulin);
		String unitMass = BU ? getString(R.string.common_unit_mass_bu) : getString(R.string.common_unit_mass_gramm);
		return unitDosage + "/" + unitMass;
	}

	private List<Versioned<Rate>> readRatesSafely(String data)
	{
		try
		{
			List<Versioned<Rate>> versioned = new ArrayList<>();
			for (Rate rate : Rate.readList(data))
			{
				Versioned<Rate> item = new Versioned<>(rate);
				item.setId(HashUtils.generateGuid());
				versioned.add(item);
			}
			return versioned;
		}
		catch (JSONException e)
		{
			Log.e(TAG, "Failed to read rates JSON: " + data, e);
			return new ArrayList<>();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		try
		{
			switch (requestCode)
			{
				case DIALOG_RATE_CREATE:
				{
					if (resultCode == Activity.RESULT_OK)
					{
						rates.add((Versioned<Rate>) intent.getExtras().getSerializable(ActivityEditor.FIELD_ENTITY));

						sortByTime(rates);
						save(rates);
						saveStateToHistory();
						adapter.notifyDataSetChanged();
					}
					break;
				}

				case DIALOG_RATE_MODIFY:
				{
					if (resultCode == Activity.RESULT_OK)
					{
						Versioned<Rate> rec = (Versioned<Rate>) intent.getExtras().getSerializable(ActivityEditor.FIELD_ENTITY);
						rates.remove(rec);
						rates.add(rec);

						sortByTime(rates);
						save(rates);
						saveStateToHistory();
						adapter.notifyDataSetChanged();
					}
					break;
				}
			}
		}
		catch (Exception e)
		{
			ErrorHandler.handle(e, this);
		}
	}

	private void save(List<Versioned<Rate>> versioned)
	{
		List<Rate> rates = new ArrayList<>();
		for (Versioned<Rate> item : versioned)
		{
			rates.add(item.getData());
		}

		try
		{
			PreferencesTypedService preferences = new PreferencesTypedService(new PreferencesLocalService(this));
			String json = Rate.writeList(rates);
			preferences.setStringValue(PreferenceID.RATES_DATA, json);
		}
		catch (JSONException e)
		{
			UIUtils.showTip(this, "Failed to save rates"); // TODO: i18n
			e.printStackTrace();
		}
	}

	private void sortByTime(List<Versioned<Rate>> rates)
	{
		Collections.sort(rates, new Comparator<Versioned<Rate>>()
		{
			@Override
			public int compare(Versioned<Rate> lhs, Versioned<Rate> rhs)
			{
				return lhs.getData().getTime() - rhs.getData().getTime();
			}
		});
	}
}
