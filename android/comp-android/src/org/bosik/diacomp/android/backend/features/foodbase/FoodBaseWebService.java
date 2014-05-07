package org.bosik.diacomp.android.backend.features.foodbase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.bosik.diacomp.android.backend.common.webclient.WebClient;
import org.bosik.diacomp.core.entities.business.foodbase.FoodItem;
import org.bosik.diacomp.core.entities.tech.Versioned;
import org.bosik.diacomp.core.persistence.serializers.Serializer;
import org.bosik.diacomp.core.persistence.serializers.SerializerFoodItem;
import org.bosik.diacomp.core.rest.StdResponse;
import org.bosik.diacomp.core.services.exceptions.AlreadyDeletedException;
import org.bosik.diacomp.core.services.exceptions.CommonServiceException;
import org.bosik.diacomp.core.services.exceptions.NotFoundException;
import org.bosik.diacomp.core.services.exceptions.PersistenceException;
import org.bosik.diacomp.core.services.foodbase.FoodBaseService;
import org.bosik.diacomp.core.utils.Utils;

@SuppressWarnings("unchecked")
public class FoodBaseWebService implements FoodBaseService
{
	// private static final String TAG = FoodBaseWebService.class.getSimpleName();

	private final WebClient							webClient;
	private final Serializer<Versioned<FoodItem>>	serializer	= new SerializerFoodItem();

	public FoodBaseWebService(WebClient webClient)
	{
		this.webClient = webClient;
	}

	@Override
	public void add(Versioned<FoodItem> item) throws PersistenceException
	{
		// TODO: current implementation doesn't fail for duplicates
		save(Arrays.<Versioned<FoodItem>> asList(item));
	}

	@Override
	public void delete(String id) throws NotFoundException, AlreadyDeletedException
	{
		Versioned<FoodItem> item = findById(id);

		if (item == null)
		{
			throw new NotFoundException(id);
		}

		if (item.isDeleted())
		{
			throw new AlreadyDeletedException(id);
		}

		item.setDeleted(true);
		save(Arrays.<Versioned<FoodItem>> asList(item));
	}

	@Override
	public List<Versioned<FoodItem>> findAll(boolean includeRemoved)
	{
		try
		{
			String url = String.format("api/food/all/?show_rem=%s", Utils.formatBooleanInt(includeRemoved));
			StdResponse resp = webClient.doGetSmart(url);
			return serializer.readAll(resp.getResponse());
		}
		catch (Exception e)
		{
			throw new CommonServiceException(e);
		}
	}

	@Override
	public List<Versioned<FoodItem>> findAny(String filter)
	{
		try
		{
			String url = String.format("api/food/search/?q=%s", filter);
			StdResponse resp = webClient.doGetSmart(url);
			return serializer.readAll(resp.getResponse());
		}
		catch (Exception e)
		{
			throw new CommonServiceException(e);
		}
	}

	@Override
	public List<Versioned<FoodItem>> findChanged(Date since)
	{
		try
		{
			String url = String.format("api/food/changes/?since=%s", Utils.formatTimeUTC(since));
			StdResponse resp = webClient.doGetSmart(url);
			return serializer.readAll(resp.getResponse());
		}
		catch (Exception e)
		{
			throw new CommonServiceException(e);
		}
	}

	@Override
	public Versioned<FoodItem> findOne(String exactName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Versioned<FoodItem> findById(String guid)
	{
		try
		{
			String url = String.format("api/food/guid/%s", guid);
			StdResponse resp = webClient.doGetSmart(url);
			return serializer.read(resp.getResponse());
		}
		catch (NotFoundException e)
		{
			return null;
		}
		catch (Exception e)
		{
			throw new CommonServiceException(e);
		}
	}

	@Override
	public void save(List<Versioned<FoodItem>> items) throws NotFoundException, PersistenceException
	{
		String url = "api/food/";
		try
		{
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("items", serializer.writeAll(items)));
			webClient.doPutSmart(url, params, WebClient.CODEPAGE_UTF8);
		}
		catch (Exception e)
		{
			throw new CommonServiceException("URL: " + url, e);
		}
	}
}
