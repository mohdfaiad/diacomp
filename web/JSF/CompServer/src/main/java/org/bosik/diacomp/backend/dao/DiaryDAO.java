package org.bosik.diacomp.backend.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.bosik.diacomp.persistence.common.Versioned;
import org.bosik.diacomp.utils.Utils;

public class DiaryDAO implements IDiaryDAO
{
	private final MySQLAccess	db	= new MySQLAccess();

	private static List<Versioned<String>> parseDiaryRecords(ResultSet resultSet) throws SQLException
	{
		// Don't parse to DiaryPage: we have to convert it into String in REST anyway

		// Parser<DiaryRecord> parser = new ParserDiaryRecord();
		// Serializer<DiaryRecord> serializer = new SerializerAdapter<DiaryRecord>(parser);

		List<Versioned<String>> result = new LinkedList<Versioned<String>>();

		while (resultSet.next())
		{
			String id = resultSet.getString(MySQLAccess.COLUMN_DIARY_GUID);
			Date timeStamp = resultSet.getDate(MySQLAccess.COLUMN_DIARY_TIMESTAMP);
			int version = resultSet.getInt(MySQLAccess.COLUMN_DIARY_VERSION);
			boolean deleted = (resultSet.getInt(MySQLAccess.COLUMN_DIARY_DELETED) == 1);
			String content = resultSet.getString(MySQLAccess.COLUMN_DIARY_CONTENT);

			Versioned<String> item = new Versioned<String>();
			item.setId(id);
			item.setTimeStamp(timeStamp);
			item.setVersion(version);
			item.setDeleted(deleted);
			// item.setData(serializer.read(content));
			item.setData(content);

			result.add(item);
		}

		return result;
	}

	@Override
	public List<Versioned<String>> findMod(int userId, Date time)
	{
		try
		{
			String clause = String.format("(%s = %d) AND (%s >= '%s')", MySQLAccess.COLUMN_DIARY_USER, userId,
					MySQLAccess.COLUMN_DIARY_TIMESTAMP, Utils.formatTimeUTC(time));
			//System.out.println("Requesting SQL clause: " + clause);
			ResultSet set = db.select(MySQLAccess.TABLE_DIARY, clause);
			List<Versioned<String>> result = parseDiaryRecords(set);
			set.close();
			return result;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
