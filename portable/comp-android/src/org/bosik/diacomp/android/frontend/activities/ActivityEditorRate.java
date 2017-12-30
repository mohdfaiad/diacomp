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

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import org.bosik.diacomp.android.R;
import org.bosik.diacomp.android.frontend.UIUtils;
import org.bosik.diacomp.core.entities.business.Rate;
import org.bosik.diacomp.core.utils.Utils;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class ActivityEditorRate extends ActivityEditor<Rate>
{
	// TODO: i18n
	private static final String MSG_INCORRECT_VALUE = "Введите корректное значение";
	public static final  String KEY_INTENT_USE_BU   = "org.bosik.diacomp.useBU";

	private static final int INDEX_K = 1;
	private static final int INDEX_Q = 2;
	private static final int INDEX_X = 3;

	// components
	private Button   buttonTime;
	private EditText editK;
	private EditText editQ;
	private EditText editX;
	private Button   buttonOK;

	private boolean BU;
	private boolean ignoreUpdates = false;

	/* =========================== OVERRIDDEN METHODS ================================ */

	@Override
	protected void setupInterface()
	{
		setContentView(R.layout.activity_editor_rate);

		BU = getIntent().getExtras().getBoolean(KEY_INTENT_USE_BU, false);

		buttonTime = (Button) findViewById(R.id.buttonRateTime);
		buttonTime.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// showTimePickerDialog();
			}
		});

		editK = (EditText) findViewById(R.id.editRateK);
		editQ = (EditText) findViewById(R.id.editRateQ);
		editX = (EditText) findViewById(R.id.editRateX);

		LinkedHashSet<Integer> indexes = new LinkedHashSet<>(Arrays.asList(INDEX_X, INDEX_Q, INDEX_K));
		editK.addTextChangedListener(new MyTextWatcher(indexes, INDEX_K));
		editQ.addTextChangedListener(new MyTextWatcher(indexes, INDEX_Q));
		editX.addTextChangedListener(new MyTextWatcher(indexes, INDEX_X));

		buttonOK = (Button) findViewById(R.id.buttonRateOK);
		buttonOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ActivityEditorRate.this.submit();
			}
		});
	}

	@Override
	protected void showValuesInGUI(boolean createMode)
	{
		buttonTime.setText(Utils.formatTimeMin(entity.getData().getTime()));

		if (!createMode)
		{
			// sic: reversed
			editX.setText(Utils.formatX(entity.getData().getK() / entity.getData().getQ(), BU));
			editQ.setText(Utils.formatQ(entity.getData().getQ()));
			editK.setText(Utils.formatK(entity.getData().getK(), BU));
		}
		else
		{
			editK.setText("");
			editQ.setText("");
			editX.setText("");
		}
	}

	private boolean readDouble(EditText editor)
	{
		try
		{
			Utils.parseExpression(editor.getText().toString());
			return true;
		}
		catch (IllegalArgumentException e)
		{
			UIUtils.showTip(this, MSG_INCORRECT_VALUE);
			editor.requestFocus();
			return false;
		}
	}

	@Override
	protected boolean getValuesFromGUI()
	{
		return readDouble(editK) && readDouble(editQ) && readDouble(editX);
	}

	private class MyTextWatcher implements TextWatcher
	{
		private final LinkedHashSet<Integer> indexes;
		private final int                    index;

		public MyTextWatcher(LinkedHashSet<Integer> indexes, int index)
		{
			this.indexes = indexes;
			this.index = index;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
		}

		@Override
		public void afterTextChanged(Editable s)
		{
			if (!ignoreUpdates)
			{
				ignoreUpdates = true;
				try
				{
					double value = Double.parseDouble(s.toString());

					if (value > 0)
					{
						double k = entity.getData().getK();
						double q = entity.getData().getQ();
						double x = entity.getData().getK() / entity.getData().getQ();

						switch (index)
						{
							case INDEX_K:
							{
								k = (BU ? value / Utils.CARB_PER_BU : value);
								editK.setTextColor(getResources().getColor(R.color.font_black));
								break;
							}
							case INDEX_Q:
							{
								q = value;
								editQ.setTextColor(getResources().getColor(R.color.font_black));
								break;
							}
							case INDEX_X:
							{
								x = (BU ? value / Utils.CARB_PER_BU : value);
								editX.setTextColor(getResources().getColor(R.color.font_black));
								break;
							}
						}

						moveToEnd(indexes, index);
						switch (indexes.iterator().next())
						{
							case INDEX_K:
							{
								k = x * q;
								editK.setText(Utils.formatK(k, BU));
								editK.setTextColor(getResources().getColor(R.color.font_gray));
								break;
							}
							case INDEX_Q:
							{
								q = k / x;
								editQ.setText(Utils.formatQ(q));
								editQ.setTextColor(getResources().getColor(R.color.font_gray));
								break;
							}
							case INDEX_X:
							{
								x = k / q;
								editX.setText(Utils.formatX(x, BU));
								editX.setTextColor(getResources().getColor(R.color.font_gray));
								break;
							}
						}

						entity.getData().setK(k);
						entity.getData().setQ(q);
					}
				}
				catch (NumberFormatException e)
				{
					// ignore
				}
				finally
				{
					ignoreUpdates = false;
				}
			}
		}

		private void moveToEnd(LinkedHashSet<Integer> indexes, int value)
		{
			indexes.remove(value);
			indexes.add(value);
		}
	}
}