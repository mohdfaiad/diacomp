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
package org.bosik.diacomp.android.frontend.views;

import org.bosik.diacomp.android.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ExpandableView extends LinearLayout
{
	public static class OnSwitchedListener
	{
		protected void onExpanded()
		{
		};

		protected void onCollapsed()
		{
		};
	}

	private static class SavedState extends BaseSavedState
	{
		boolean expanded;

		public SavedState(Parcelable state)
		{
			super(state);
		}

		public SavedState(Parcel in)
		{
			super(in);
			expanded = (in.readByte() == 1);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			dest.writeByte((byte) (expanded ? 1 : 0));
		}
	}

	Button						groupSwitch;
	private View				contentPanel;
	private OnSwitchedListener	onSwitchedListener;
	private boolean				expanded;

	private static final String	KEY_EXPANDED	= "KEY_EXPANDED";

	@Override
	protected Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		SavedState state = new SavedState(superState);
		state.expanded = isExpanded();
		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		setExpanded(ss.expanded);
	}

	public ExpandableView(final Context context, AttributeSet attributes)
	{
		super(context, attributes);

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_expandable, this);

		if (isInEditMode())
		{
			return;
		}

		groupSwitch = (Button) findViewById(R.id.buttonGroupSwitch);
		groupSwitch.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setExpanded(!isExpanded());
			}
		});
	}

	public View getContentPanel()
	{
		return contentPanel;
	}

	public void setContentPanel(View contentPanel)
	{
		this.contentPanel = contentPanel;
	}

	public OnSwitchedListener getOnSwitchedListener()
	{
		return onSwitchedListener;
	}

	public void setOnSwitchedListener(OnSwitchedListener onSwitchedListener)
	{
		this.onSwitchedListener = onSwitchedListener;
	}

	public CharSequence getTitle()
	{
		return groupSwitch.getText();
	}

	public void setTitle(String title)
	{
		groupSwitch.setText(title);
	}

	public boolean isExpanded()
	{
		return expanded;
	}

	public void setExpanded(boolean expanded)
	{
		if (this.expanded != expanded)
		{
			if (expanded)
			{
				expand();
			}
			else
			{
				collapse();
			}
		}
	}

	public void expand()
	{
		expanded = true;
		Drawable icon = getResources().getDrawable(R.drawable.ic_group_expanded);
		groupSwitch.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

		if (getContentPanel() != null)
		{
			getContentPanel().setVisibility(View.VISIBLE);
		}

		if (getOnSwitchedListener() != null)
		{
			getOnSwitchedListener().onExpanded();
		}
	}

	public void collapse()
	{
		expanded = false;
		Drawable icon = getResources().getDrawable(R.drawable.ic_group_collapsed);
		groupSwitch.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

		if (getContentPanel() != null)
		{
			getContentPanel().setVisibility(View.GONE);
		}

		if (getOnSwitchedListener() != null)
		{
			getOnSwitchedListener().onCollapsed();
		}
	}
}
