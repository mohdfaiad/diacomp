package org.bosik.diacomp.core.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StdResponse
{
	public static final String	TAG_CODE		= "code";
	public static final String	TAG_RESPONSE	= "resp";

	private int					code;
	private String				response;

	public StdResponse(int code, String response)
	{
		this.code = code;
		this.response = response;
	}

	public StdResponse(String s)
	{
		StdResponse decoded = StdResponse.decode(s);
		setCode(decoded.getCode());
		setResponse(decoded.getResponse());
	}

	public int getCode()
	{
		return code;
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	public String getResponse()
	{
		return response;
	}

	public void setResponse(String response)
	{
		this.response = response;
	}

	private static boolean isValidJsonObject(String s)
	{
		try
		{
			new JSONObject(s);
			return true;
		}
		catch (JSONException e)
		{
			return false;
		}
	}

	private static boolean isValidJsonArray(String s)
	{
		try
		{
			new JSONArray(s);
			return true;
		}
		catch (JSONException e)
		{
			return false;
		}
	}

	public static StdResponse decode(String s)
	{
		try
		{
			JSONObject json = new JSONObject(s);
			int code = json.getInt(TAG_CODE);
			String msg;

			if (json.has(TAG_RESPONSE))
			{
				try
				{
					msg = json.getJSONObject(TAG_RESPONSE).toString();
				}
				catch (JSONException e)
				{
					try
					{
						msg = json.getJSONArray(TAG_RESPONSE).toString();
					}
					catch (JSONException e2)
					{
						msg = json.getString(TAG_RESPONSE);
					}
				}
			}
			else
			{
				msg = "";
			}

			return new StdResponse(code, msg);
		}
		catch (JSONException e)
		{
			throw new RuntimeException(String.format("Failed to parse JSON string '%s'", s), e);
		}
	}

	public static String encode(StdResponse resp)
	{
		JSONObject json = new JSONObject();
		json.put(TAG_CODE, resp.getCode());

		if (isValidJsonObject(resp.getResponse()))
		{
			json.put(TAG_RESPONSE, new JSONObject(resp.getResponse()));
		}
		else if (isValidJsonArray(resp.getResponse()))
		{
			json.put(TAG_RESPONSE, new JSONArray(resp.getResponse()));
		}
		else
		{
			json.put(TAG_RESPONSE, resp.getResponse());
		}
		return json.toString();
	}
}
