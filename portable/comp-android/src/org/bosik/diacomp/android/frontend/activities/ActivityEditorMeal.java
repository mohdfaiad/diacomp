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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bosik.diacomp.android.R;
import org.bosik.diacomp.android.backend.features.analyze.KoofServiceInternal;
import org.bosik.diacomp.android.frontend.UIUtils;
import org.bosik.diacomp.android.frontend.views.fdpicker.MealEditorView;
import org.bosik.diacomp.android.frontend.views.fdpicker.MealEditorView.OnChangeListener;
import org.bosik.diacomp.android.utils.ErrorHandler;
import org.bosik.diacomp.core.entities.business.FoodMassed;
import org.bosik.diacomp.core.entities.business.diary.records.MealRecord;
import org.bosik.diacomp.core.services.analyze.KoofService;
import org.bosik.diacomp.core.services.analyze.entities.Koof;
import org.bosik.diacomp.core.utils.Utils;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityEditorMeal extends ActivityEditorTime<MealRecord>
{

	public static final String	TAG					= ActivityEditorMeal.class.getSimpleName();

	public static final String	FIELD_BS_BASE		= "bosik.pack.bs.base";
	public static final String	FIELD_BS_LAST		= "bosik.pack.bs.last";
	public static final String	FIELD_BS_TARGET		= "bosik.pack.bs.target";
	public static final String	FIELD_INS_INJECTED	= "bosik.pack.insInjected";

	private KoofService			koofService;

	// FIXME: hardcoded BS value
	private static final double	BS_HYPOGLYCEMIA		= 3.8;

	// data
	boolean						modified;
	private Double				bsBase;
	private Double				bsLast;
	private Double				bsTarget;
	Double						insInjected;

	boolean						correctBs			= true;

	// components
	private Button				buttonTime;
	private Button				buttonDate;
	// private TextView textMealStatProts;
	// private TextView textMealStatFats;
	// private TextView textMealStatCarbs;
	// private TextView textMealStatValue;
	// private TextView textMealStatDosage;

	private LinearLayout		layoutShifted;
	private TextView			textMealCurrentDosage;
	private TextView			textMealShiftedCarbs;
	private TextView			textMealShiftedCarbsHypo;
	private TextView			textMealShiftedDosage;
	private TextView			textMealExpectedBs;
	Button						buttonCorrection;
	MealEditorView				mealEditor;

	// localization
	private String				captionProts;
	private String				captionFats;
	private String				captionCarbs;
	private String				captionValue;
	private String				captionDose;
	private String				captionGramm;
	private String				captionMmol;

	// ======================================================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// Service must be initialized before super() is called
		koofService = KoofServiceInternal.getInstance(getContentResolver());
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void setupInterface()
	{
		setContentView(R.layout.activity_editor_meal);

		// string constants
		captionProts = "P";
		captionFats = "F";
		captionCarbs = "C";
		captionValue = "V";
		captionDose = getString(R.string.common_unit_insulin);
		captionGramm = getString(R.string.common_unit_mass_gramm);
		captionMmol = getString(R.string.common_unit_bs_mmoll);

		// components

		buttonTime = (Button) findViewById(R.id.buttonMealTime);
		buttonTime.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showTimePickerDialog();
			}
		});
		buttonDate = (Button) findViewById(R.id.buttonMealDate);
		buttonDate.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showDatePickerDialog();
			}
		});

		// textMealStatProts = (TextView) findViewById(R.id.textMealStatProts);
		// textMealStatFats = (TextView) findViewById(R.id.textMealStatFats);
		// textMealStatCarbs = (TextView) findViewById(R.id.textMealStatCarbs);
		// textMealStatValue = (TextView) findViewById(R.id.textMealStatValue);
		// textMealStatDosage = (TextView) findViewById(R.id.textMealStatDosage);

		layoutShifted = (LinearLayout) findViewById(R.id.layoutMealShifted);
		textMealCurrentDosage = (TextView) findViewById(R.id.textMealCurrentDosage);
		textMealShiftedCarbs = (TextView) findViewById(R.id.textMealShiftedCarbs);
		textMealShiftedCarbsHypo = (TextView) findViewById(R.id.textMealShiftedCarbsHypo);
		textMealShiftedDosage = (TextView) findViewById(R.id.textMealShiftedDosage);
		textMealExpectedBs = (TextView) findViewById(R.id.textMealExpectedBs);
		buttonCorrection = (Button) findViewById(R.id.buttonMealCorrection);

		mealEditor = (MealEditorView) findViewById(R.id.mealEditorMeal);

		// list.setScroll
		// list.setScrollContainer(false);

		// list.setOnTouchListener(new OnTouchListener()
		// {
		// @Override
		// public boolean onTouch(View v, MotionEvent event)
		// {
		// if (event.getAction() == MotionEvent.ACTION_MOVE)
		// {
		// return true; // Indicates that this has been handled by you and will not be
		// // forwarded further.
		// }
		// return false;
		// }
		// });

		// list.addHeaderView(findViewById(R.id.layoutMealParent));

		// setting listeners
		mealEditor.setOnChangeListener(new OnChangeListener()
		{
			@Override
			public void onChange(List<FoodMassed> items)
			{
				modified = true;
				entity.getData().clear();
				for (FoodMassed item : mealEditor.getData())
				{
					entity.getData().add(item);
				}

				showMealInfo();
			}
		});

		buttonCorrection.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if ((insInjected != null) && (insInjected > Utils.EPS))
				{
					correctBs = !correctBs;
					ActivityEditorMeal.this.showMealInfo();

					if (correctBs)
					{
						buttonCorrection.setText("\\");
					}
					else
					{
						buttonCorrection.setText("—");
					}
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			getMenuInflater().inflate(R.menu.actions_meal, menu);
			MenuItem itemShort = menu.findItem(R.id.item_meal_short);
			itemShort.setChecked(entity.getData().getShortMeal());
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
		final MealRecord data = entity.getData();

		switch (item.getItemId())
		{
			case R.id.item_meal_short:
			{
				boolean newValue = !data.getShortMeal();
				data.setShortMeal(newValue);
				modified = true;
				item.setChecked(newValue);
				return true;
			}
			case R.id.item_meal_info:
			{
				String s = getString(R.string.editor_meal_tip_meal_info);
				String info = String.format(s, data.getProts(), data.getFats(), data.getCarbs(), data.getValue(),
						data.getMass());
				UIUtils.showTip(ActivityEditorMeal.this, info);
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	void showMealContent()
	{
		List<FoodMassed> items = new ArrayList<FoodMassed>();

		for (int i = 0; i < entity.getData().count(); i++)
		{
			items.add(entity.getData().get(i));
		}

		mealEditor.setData(items);
	}

	void showMealInfo()
	{
		// | Base | Last | Mod | BaseBS |
		// |------|------|-----|--------|
		// | ---- | ---- | Std | target | Dosage = carbs * k / q
		// | ---- | hypo | Hyp | lastBS | Delta_carbs = (BS_target - BS_last) / k <------------
		// | ---- | belw | Std | target | Dosage = carbs * k / q
		// | ---- | abov | Std | target | Dosage = carbs * k / q
		// | hypo | ---- | Std | baseBS |
		// | hypo | hypo | Hyp | lastBS | Delta_carbs = (BS_target - BS_last) / k <------------
		// | hypo | belw | Std | baseBS |
		// | hypo | abov | Std | baseBS |
		// | belw | ---- | Std | baseBS |
		// | belw | hypo | Hyp | lastBS | Delta_carbs = (BS_target - BS_last) / k <------------
		// | belw | belw | Std | baseBS |
		// | belw | abov | Std | baseBS |
		// | abov | ---- | Std | baseBS |
		// | abov | hypo | Hyp | lastBS | Delta_carbs = (BS_target - BS_last) / k <------------
		// | abov | belw | Std | baseBS |
		// | abov | abov | Std | baseBS |

		// Legend:
		// B - base BS
		// L - last BS
		// M - mode
		// - - no BS
		// h - hypoglycemic BS (0..BS_HYPOGLYCEMIA)
		// b - BS below target (BS_HYPOGLYCEMIA..BS_TARGET)
		// a - BS above target (BS_TARGET..)

		// commons
		int minutesTime = Utils.getDayMinutesUTC(entity.getData().getTime());
		Koof koof = koofService.getKoof(minutesTime);
		double carbs = entity.getData().getCarbs();
		double prots = entity.getData().getProts();

		double deltaBS = 0.0;

		if (bsLast != null && bsLast < BS_HYPOGLYCEMIA && (bsBase == null || Math.abs(bsBase - bsLast) > Utils.EPS))
		{
			// Hypoglygemic mode
			findViewById(R.id.mealRowInsulin).setVisibility(View.GONE);
			findViewById(R.id.mealRowCarbs).setVisibility(View.VISIBLE);

			deltaBS = bsTarget - bsLast;

			Double expectedBS = bsLast + (carbs * koof.getK()) + (prots * koof.getP());

			textMealExpectedBs.setText(String.format("%.1f %s", expectedBS, captionMmol));

			double shiftedCarbs = (prots * koof.getP() + deltaBS) / koof.getK() - carbs;
			if (shiftedCarbs < 0)
			{
				textMealShiftedCarbsHypo.setTextColor(getResources().getColor(R.color.meal_correction_negative));
			}
			else
			{
				textMealShiftedCarbsHypo.setTextColor(getResources().getColor(R.color.meal_correction_positive));
			}
			textMealShiftedCarbsHypo.setText(Utils.formatDoubleSigned(shiftedCarbs) + " " + captionGramm);
		}
		else if (bsBase != null)
		{
			// Standard mode
			findViewById(R.id.mealRowInsulin).setVisibility(View.VISIBLE);
			findViewById(R.id.mealRowCarbs).setVisibility(View.GONE);

			deltaBS = bsTarget - bsBase;

			Double expectedBS = bsBase == null ? null
					: bsBase + (carbs * koof.getK()) + (prots * koof.getP()) - (insInjected * koof.getQ());

			if (expectedBS != null)
			{
				if (expectedBS > BS_HYPOGLYCEMIA)
				{
					textMealExpectedBs.setText(String.format("%.1f %s", expectedBS, captionMmol));
				}
				else
				{
					textMealExpectedBs.setText(getString(R.string.editor_meal_label_expected_bs_hypoglycemia));
				}
			}
			else
			{
				textMealExpectedBs.setText("?");
			}

			// textMealStatProts.setText(String.format("%s %.1f", captionProts,
			// entity.getData().getProts()));
			// textMealStatFats.setText(String.format("%s %.1f", captionFats,
			// entity.getData().getFats()));
			// textMealStatCarbs.setText(String.format("%s %.1f", captionCarbs,
			// entity.getData().getCarbs()));
			// textMealStatValue.setText(String.format("%s %.1f", captionValue,
			// entity.getData().getValue()));
			// textMealStatDosage.setText(String.format("%.1f %s", insInjected, captionDose));

			// current dosage

			double currentDose = (-deltaBS + carbs * koof.getK() + (prots * koof.getP())) / koof.getQ();
			if (currentDose > 0)
			{
				textMealCurrentDosage.setText(String.format("%.1f %s", currentDose, captionDose));
			}
			else
			{
				textMealCurrentDosage.setText("--");
			}

			// shifted dosage

			if (insInjected > Utils.EPS)
			{
				layoutShifted.setVisibility(View.VISIBLE);

				double shiftedCarbs = (insInjected * koof.getQ() - prots * koof.getP() + deltaBS) / koof.getK() - carbs;
				if (shiftedCarbs < 0)
				{
					textMealShiftedCarbs.setTextColor(getResources().getColor(R.color.meal_correction_negative));
				}
				else
				{
					textMealShiftedCarbs.setTextColor(getResources().getColor(R.color.meal_correction_positive));
				}
				textMealShiftedCarbs.setText(Utils.formatDoubleSigned(shiftedCarbs) + " " + captionGramm);

				textMealShiftedDosage.setText(String.format("%.1f %s", insInjected, captionDose));

				textMealShiftedCarbs.setTypeface(Typeface.DEFAULT_BOLD);
				textMealCurrentDosage.setTypeface(Typeface.DEFAULT);
			}
			else
			{
				layoutShifted.setVisibility(View.GONE);
				textMealShiftedCarbs.setTypeface(Typeface.DEFAULT);
				textMealCurrentDosage.setTypeface(Typeface.DEFAULT_BOLD);
			}

			// before = null, target = null --> deltaBS = 0.0; --> dose
			// before = null, target != null --> deltaBS = 0.0; --> dose
			// before != null, target = null --> deltaBS = 0.0; --> dose, expectedBS
			// before != null, target != null --> deltaBS = target - before; --> dose, expectedBS

			// expectedBS = f(before, meal) // undefined, if before == null
			// dose = g(deltaBS, meal) // deltaBS = target - before (if both are not-null) : 0.0 (if
			// something is not defined)
		}
	}

	@Override
	protected void readEntity(Intent intent)
	{
		super.readEntity(intent);

		bsBase = intent.getExtras().containsKey(FIELD_BS_BASE) ? intent.getExtras().getDouble(FIELD_BS_BASE) : null;
		bsLast = intent.getExtras().containsKey(FIELD_BS_LAST) ? intent.getExtras().getDouble(FIELD_BS_LAST) : null;
		bsTarget = intent.getExtras().containsKey(FIELD_BS_TARGET) ? intent.getExtras().getDouble(FIELD_BS_TARGET)
				: null;
		insInjected = intent.getExtras().getDouble(FIELD_INS_INJECTED, 0.0);
	}

	@Override
	protected void showValuesInGUI(boolean createMode)
	{
		if (!createMode)
		{
			onDateTimeChanged(entity.getData().getTime());
		}
		else
		{
			onDateTimeChanged(new Date());
			mealEditor.requestFocus();
		}

		showMealContent();
		showMealInfo();
		modified = false;
	}

	@Override
	protected boolean getValuesFromGUI()
	{
		return true;
	}

	@Override
	public void onBackPressed()
	{
		if (modified)
		{
			submit();
			UIUtils.showTip(this, getString(R.string.editor_meal_message_saved));
		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	protected void onDateTimeChanged(Date time)
	{
		buttonTime.setText(formatTime(time));
		buttonDate.setText(formatDate(time));
		modified = true;
		showMealInfo();
	}
}