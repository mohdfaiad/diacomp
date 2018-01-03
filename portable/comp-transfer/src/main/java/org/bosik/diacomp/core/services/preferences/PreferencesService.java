/*
 * Diacomp - Diabetes analysis & management system
 * Copyright (C) 2013 Nikita Bosik
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bosik.diacomp.core.services.preferences;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class PreferencesService
{
	/**
	 * Returns hash
	 *
	 * @return
	 */
	public String getHash()
	{
		final int prime = 31;
		int hash = 1;

		List<PreferenceEntry<String>> items = getAll();
		Collections.sort(items, new Comparator<PreferenceEntry<String>>()
		{
			@Override
			public int compare(PreferenceEntry<String> o1, PreferenceEntry<String> o2)
			{
				return o1.getId().getKey().compareTo(o2.getId().getKey());
			}
		});

		for (PreferenceEntry<String> entity : items)
		{
			if (entity.getId().isSyncable())
			{
				// It's a public API and can't be changed
				hash = prime * hash + entity.getVersion();
			}
		}

		return String.valueOf(hash);
	}

	/**
	 * Returns all preferences
	 *
	 * @return
	 */
	public abstract List<PreferenceEntry<String>> getAll();

	/**
	 * Returns string preference
	 *
	 * @param id
	 * @return Entry if preference found, null otherwise
	 */
	public abstract PreferenceEntry<String> getString(PreferenceID id);

	/**
	 * Updates single string entry
	 *
	 * @param entry
	 */
	public abstract void setString(PreferenceEntry<String> entry);

	/**
	 * Updates multiple string entries
	 *
	 * @param entries
	 */
	public abstract void update(List<PreferenceEntry<String>> entries);
}
