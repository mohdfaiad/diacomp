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
package org.bosik.diacomp.android.backend.common.db.tables;

import java.util.ArrayList;
import java.util.List;

import org.bosik.diacomp.android.backend.common.db.Column;
import org.bosik.diacomp.android.backend.common.db.Table;

import android.net.Uri;

public class TableDishbase extends Table
{
	public static final String	COLUMN_ID			= "GUID";
	public static final String	COLUMN_TIMESTAMP	= "TimeStamp";
	public static final String	COLUMN_HASH			= "Hash";
	public static final String	COLUMN_VERSION		= "Version";
	public static final String	COLUMN_DELETED		= "Deleted";
	public static final String	COLUMN_DATA			= "Data";
	public static final String	COLUMN_NAMECACHE	= "NameCache";

	public static final Uri		CONTENT_URI			= new TableDishbase().getUri();
	public static final int		CODE				= 3;

	@Override
	public String getName()
	{
		return "dishbase";
	}

	@Override
	public int getCode()
	{
		return CODE;
	}

	@Override
	public String getContentType()
	{
		return "org.bosik.diacomp.dish";
	}

	@Override
	public List<Column> getColumns()
	{
		List<Column> columns = new ArrayList<>();

		columns.add(new Column(COLUMN_ID, Column.TYPE_TEXT, true, false));
		columns.add(new Column(COLUMN_TIMESTAMP, Column.TYPE_TEXT, false, false));
		columns.add(new Column(COLUMN_HASH, Column.TYPE_TEXT, false, false));
		columns.add(new Column(COLUMN_VERSION, Column.TYPE_INTEGER, false, false));
		columns.add(new Column(COLUMN_DELETED, Column.TYPE_INTEGER, false, false));
		columns.add(new Column(COLUMN_NAMECACHE, Column.TYPE_TEXT, false, false));
		columns.add(new Column(COLUMN_DATA, Column.TYPE_TEXT, false, false));

		return columns;
	}
}
