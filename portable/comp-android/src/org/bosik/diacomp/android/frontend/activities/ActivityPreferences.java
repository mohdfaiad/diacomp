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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import org.bosik.diacomp.android.R;
import org.bosik.diacomp.android.backend.features.preferences.account.PreferencesLocalService;
import org.bosik.diacomp.core.services.preferences.PreferenceEntry;
import org.bosik.diacomp.core.services.preferences.PreferenceID;
import org.bosik.diacomp.core.services.preferences.PreferencesTypedService;

import java.util.ArrayList;
import java.util.List;

public class ActivityPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private static final String								TAG			= ActivityPreferences.class.getSimpleName();

	// Services
	private SharedPreferences								devicePreferences;
	private PreferencesTypedService							syncablePreferences;

	private static List<OnSharedPreferenceChangeListener>	listeners	= new ArrayList<OnSharedPreferenceChangeListener>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Initializing services
		syncablePreferences = new PreferencesTypedService(new PreferencesLocalService(this));
		devicePreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Filling values
		Editor editor = devicePreferences.edit();
		for (PreferenceID id : PreferenceID.values())
		{
			editor.putString(id.getKey(), syncablePreferences.getStringValue(id));
		}
		editor.apply();

		// Inflating layout
		addPreferencesFromResource(R.xml.preferences);
		for (String key : devicePreferences.getAll().keySet())
		{
			updateDescription(devicePreferences, key);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
	{
		updateDescription(preferences, key);
		// Storage.applyPreference(sharedPreferences, key);

		for (PreferenceID id : PreferenceID.values())
		{
			if (id.getKey().equals(key))
			{
				PreferenceEntry<String> entry = syncablePreferences.getString(id);

				if (entry == null)
				{
					entry = new PreferenceEntry<String>();
					entry.setId(id);
					entry.setVersion(1);
				}
				else
				{
					entry.setVersion(entry.getVersion() + 1);
				}
				entry.setValue(preferences.getString(key, id.getDefaultValue()));
				syncablePreferences.setString(entry);

				break;
			}
		}

		for (OnSharedPreferenceChangeListener listener : listeners)
		{
			listener.onSharedPreferenceChanged(preferences, key);
		}
	}

	private void updateDescription(SharedPreferences sharedPreferences, String key)
	{
		android.preference.Preference p = findPreference(key);
		if (p instanceof EditTextPreference)
		{
			p.setSummary(sharedPreferences.getString(key, ""));
		}
	}

	public static void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)
	{
		if (!listeners.contains(listener))
		{
			listeners.add(listener);
		}
	}
}