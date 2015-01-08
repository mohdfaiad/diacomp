package org.bosik.diacomp.web.backend.features.base.food.function;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.bosik.diacomp.core.entities.business.foodbase.FoodItem;
import org.bosik.diacomp.core.entities.tech.Versioned;
import org.bosik.diacomp.core.persistence.parsers.Parser;
import org.bosik.diacomp.core.persistence.parsers.ParserFoodItem;
import org.bosik.diacomp.core.persistence.serializers.Serializer;
import org.bosik.diacomp.core.persistence.utils.SerializerAdapter;
import org.bosik.diacomp.core.services.ObjectService;
import org.bosik.diacomp.core.utils.Utils;
import org.bosik.diacomp.web.backend.common.mysql.MySQLAccess;

public class MySQLFoodbaseDAO implements FoodbaseDAO
{
	// Foodbase table
	private static final String					TABLE_FOODBASE				= "foodbase2";
	private static final String					COLUMN_FOODBASE_GUID		= "_GUID";
	private static final String					COLUMN_FOODBASE_USER		= "_UserID";
	private static final String					COLUMN_FOODBASE_TIMESTAMP	= "_TimeStamp";
	private static final String					COLUMN_FOODBASE_VERSION		= "_Version";
	private static final String					COLUMN_FOODBASE_DELETED		= "_Deleted";
	private static final String					COLUMN_FOODBASE_CONTENT		= "_Content";
	private static final String					COLUMN_FOODBASE_NAMECACHE	= "_NameCache";

	// Foodbase hash table
	private static final String					TABLE_FOODBASE_HASH			= "dishbase_hash";
	private static final String					COLUMN_FOODBASE_HASH_USER	= "_UserID";
	private static final String					COLUMN_FOODBASE_HASH_GUID	= "_GUID";
	private static final String					COLUMN_FOODBASE_HASH_HASH	= "_Hash";

	private static final MySQLAccess			db							= new MySQLAccess();
	private static final Parser<FoodItem>		parser						= new ParserFoodItem();
	private static final Serializer<FoodItem>	serializer					= new SerializerAdapter<FoodItem>(parser);

	private static List<Versioned<FoodItem>> parseFoodItems(ResultSet resultSet) throws SQLException
	{
		List<Versioned<FoodItem>> result = new LinkedList<Versioned<FoodItem>>();

		while (resultSet.next())
		{
			String id = resultSet.getString(COLUMN_FOODBASE_GUID);
			Date timeStamp = Utils.parseTimeUTC(resultSet.getString(COLUMN_FOODBASE_TIMESTAMP));
			int version = resultSet.getInt(COLUMN_FOODBASE_VERSION);
			boolean deleted = (resultSet.getInt(COLUMN_FOODBASE_DELETED) == 1);
			String content = resultSet.getString(COLUMN_FOODBASE_CONTENT);

			Versioned<FoodItem> item = new Versioned<FoodItem>();
			item.setId(id);
			item.setTimeStamp(timeStamp);
			item.setVersion(version);
			item.setDeleted(deleted);
			item.setData(serializer.read(content));
			//item.setData(content);

			result.add(item);
		}

		return result;
	}

	@Override
	public List<Versioned<FoodItem>> findAll(int userId, boolean includeRemoved)
	{
		try
		{
			String clause = String.format("(%s = %d)", COLUMN_FOODBASE_USER, userId);
			if (!includeRemoved)
			{
				clause += String.format(" AND (%s = '%s')", COLUMN_FOODBASE_DELETED, Utils.formatBooleanInt(false));
			}

			String order = COLUMN_FOODBASE_NAMECACHE;

			ResultSet set = db.select(TABLE_FOODBASE, clause, order);
			List<Versioned<FoodItem>> result = parseFoodItems(set);
			set.close();
			return result;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Versioned<FoodItem>> findChanged(int userId, Date since)
	{
		try
		{
			String clause = String.format("(%s = %d) AND (%s >= '%s')", COLUMN_FOODBASE_USER, userId,
					COLUMN_FOODBASE_TIMESTAMP, Utils.formatTimeUTC(since));
			String order = COLUMN_FOODBASE_NAMECACHE;

			ResultSet set = db.select(TABLE_FOODBASE, clause, order);
			List<Versioned<FoodItem>> result = parseFoodItems(set);
			set.close();
			return result;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Versioned<FoodItem> findById(int userId, String guid)
	{
		try
		{
			String clause = String.format("(%s = %d) AND (%s = '%s')", COLUMN_FOODBASE_USER, userId,
					COLUMN_FOODBASE_GUID, guid);

			ResultSet set = db.select(TABLE_FOODBASE, clause, null);
			List<Versioned<FoodItem>> result = parseFoodItems(set);
			set.close();
			return result.isEmpty() ? null : result.get(0);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Versioned<FoodItem>> findByIdPrefix(int userId, String prefix)
	{
		if (prefix.length() != ObjectService.ID_PREFIX_SIZE)
		{
			throw new IllegalArgumentException(String.format("Invalid prefix length, expected %d chars, but %d found",
					ObjectService.ID_PREFIX_SIZE, prefix.length()));
		}

		try
		{
			String clause = String.format("(%s = %d) AND (%s LIKE '%s%%')", COLUMN_FOODBASE_USER, userId,
					COLUMN_FOODBASE_GUID, prefix);

			String order = COLUMN_FOODBASE_NAMECACHE;

			ResultSet set = db.select(TABLE_FOODBASE, clause, order);
			List<Versioned<FoodItem>> result = parseFoodItems(set);
			set.close();
			return result;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Versioned<FoodItem>> findAny(int userId, String filter)
	{
		try
		{
			String clause = String.format("(%s = %d) AND (%s = '%s') AND (%s LIKE '%%%s%%')", COLUMN_FOODBASE_USER,
					userId, COLUMN_FOODBASE_DELETED, Utils.formatBooleanInt(false), COLUMN_FOODBASE_NAMECACHE, filter);
			String order = COLUMN_FOODBASE_NAMECACHE;

			ResultSet set = db.select(TABLE_FOODBASE, clause, order);
			List<Versioned<FoodItem>> result = parseFoodItems(set);
			set.close();
			return result;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void save(int userId, List<Versioned<FoodItem>> items)
	{
		try
		{
			for (Versioned<FoodItem> item : items)
			{
				final String content = serializer.write(item.getData());
				final String nameCache = item.getData().getName();
				final String timeStamp = Utils.formatTimeUTC(item.getTimeStamp());
				final String version = String.valueOf(item.getVersion());
				final String deleted = Utils.formatBooleanInt(item.isDeleted());

				if (findById(userId, item.getId()) != null)
				{
					// presented, update

					SortedMap<String, String> set = new TreeMap<String, String>();
					set.put(COLUMN_FOODBASE_TIMESTAMP, timeStamp);
					set.put(COLUMN_FOODBASE_VERSION, version);
					set.put(COLUMN_FOODBASE_DELETED, deleted);
					set.put(COLUMN_FOODBASE_CONTENT, content);
					set.put(COLUMN_FOODBASE_NAMECACHE, nameCache);

					SortedMap<String, String> where = new TreeMap<String, String>();
					where.put(COLUMN_FOODBASE_GUID, item.getId());
					where.put(COLUMN_FOODBASE_USER, String.valueOf(userId));

					db.update(TABLE_FOODBASE, set, where);
				}
				else
				{
					// not presented, insert

					LinkedHashMap<String, String> set = new LinkedHashMap<String, String>();
					set.put(COLUMN_FOODBASE_GUID, item.getId());
					set.put(COLUMN_FOODBASE_USER, String.valueOf(userId));
					set.put(COLUMN_FOODBASE_TIMESTAMP, timeStamp);
					set.put(COLUMN_FOODBASE_VERSION, version);
					set.put(COLUMN_FOODBASE_DELETED, deleted);
					set.put(COLUMN_FOODBASE_CONTENT, content);
					set.put(COLUMN_FOODBASE_NAMECACHE, nameCache);

					db.insert(TABLE_FOODBASE, set);
				}
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void delete(int userId, String id)
	{
		try
		{
			SortedMap<String, String> set = new TreeMap<String, String>();
			set.put(COLUMN_FOODBASE_DELETED, Utils.formatBooleanInt(true));

			SortedMap<String, String> where = new TreeMap<String, String>();
			where.put(COLUMN_FOODBASE_GUID, id);
			where.put(COLUMN_FOODBASE_USER, String.valueOf(userId));

			db.update(TABLE_FOODBASE, set, where);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Versioned<FoodItem> findOne(int userId, String exactName)
	{
		try
		{
			String clause = String.format("(%s = %d) AND (%s = '%s')", COLUMN_FOODBASE_USER, userId,
					COLUMN_FOODBASE_NAMECACHE, exactName);

			ResultSet set = db.select(TABLE_FOODBASE, clause, null);
			List<Versioned<FoodItem>> result = parseFoodItems(set);
			set.close();
			return result.isEmpty() ? null : result.get(0);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getHash(int userId, String prefix)
	{
		try
		{
			String clause = String.format("(%s = %d) AND (%s = '%s')", COLUMN_FOODBASE_HASH_USER, userId,
					COLUMN_FOODBASE_HASH_GUID, prefix);

			ResultSet set = db.select(TABLE_FOODBASE_HASH, clause, null);

			String hash = "";

			if (set.next())
			{
				hash = set.getString(COLUMN_FOODBASE_HASH_HASH);
			}

			set.close();
			return hash;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
