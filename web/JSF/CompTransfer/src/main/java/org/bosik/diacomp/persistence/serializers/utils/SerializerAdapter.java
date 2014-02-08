package org.bosik.diacomp.persistence.serializers.utils;

import java.util.List;
import org.bosik.diacomp.persistence.serializers.Parser;
import org.bosik.diacomp.persistence.serializers.Serializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SerializerAdapter<T> implements Serializer<T>
{
	private Parser<T>	parser;

	public SerializerAdapter(Parser<T> parser)
	{
		this.parser = parser;
	}

	public T read(String s)
	{
		try
		{
			JSONObject json = new JSONObject(s);
			return parser.read(json);
		}
		catch (JSONException e)
		{
			throw new IllegalArgumentException("Failed to parse JSON: " + s, e);
		}
	}

	public List<T> readAll(String s)
	{
		try
		{
			JSONArray json = new JSONArray(s);
			return parser.readAll(json);
		}
		catch (JSONException e)
		{
			throw new IllegalArgumentException("Failed to parse JSON: " + s, e);
		}
	}

	public String write(T object)
	{
		try
		{
			return parser.write(object).toString();
		}
		catch (JSONException e)
		{
			throw new RuntimeException("Failed to encode JSON", e);
		}
	}

	public String writeAll(List<T> objects)
	{
		try
		{
			return parser.writeAll(objects).toString();
		}
		catch (JSONException e)
		{
			throw new RuntimeException("Failed to encode JSON", e);
		}
	}
}