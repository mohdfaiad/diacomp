package org.bosik.diacomp.android.persistence.services.web;

import java.util.Date;
import java.util.List;
import org.bosik.diacomp.android.persistence.services.web.utils.client.WebClient;
import org.bosik.diacomp.core.entities.business.diary.DiaryRecord;
import org.bosik.diacomp.core.entities.tech.Versioned;
import org.bosik.diacomp.core.persistence.serializers.Parser;
import org.bosik.diacomp.core.persistence.serializers.ParserDiaryRecord;
import org.bosik.diacomp.core.persistence.serializers.Serializer;
import org.bosik.diacomp.core.persistence.serializers.utils.ParserVersioned;
import org.bosik.diacomp.core.persistence.serializers.utils.SerializerAdapter;
import org.bosik.diacomp.core.services.DiaryService;
import org.bosik.diacomp.core.services.exceptions.CommonServiceException;

public class WebDiaryService implements DiaryService
{
	// private static String TAG = WebDiaryService.class.getSimpleName();
	private WebClient							webClient;
	private Parser<DiaryRecord>					parser		= new ParserDiaryRecord();
	private Parser<Versioned<DiaryRecord>>		parserV		= new ParserVersioned<DiaryRecord>(parser);
	private Serializer<Versioned<DiaryRecord>>	serializerV	= new SerializerAdapter<Versioned<DiaryRecord>>(parserV);

	/* ============================ CONSTRUCTOR ============================ */

	public WebDiaryService(WebClient webClient)
	{
		if (webClient == null)
		{
			throw new NullPointerException("WebClient can't be null");
		}

		this.webClient = webClient;
	}

	/* ============================ API ============================ */

	@Override
	public List<Versioned<DiaryRecord>> getRecords(List<String> guids) throws CommonServiceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Versioned<DiaryRecord>> getRecords(Date time, boolean includeRemoved) throws CommonServiceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Versioned<DiaryRecord>> getRecords(Date fromDate, Date toDate, boolean includeRemoved)
			throws CommonServiceException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postRecords(List<Versioned<DiaryRecord>> records) throws CommonServiceException
	{
		// TODO Auto-generated method stub
	}

	/* ======================= ROUTINES ========================= */

}
