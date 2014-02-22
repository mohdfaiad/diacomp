package org.bosik.diacomp.persistence.services.web;

import org.bosik.diacomp.android.persistence.services.web.WebDiaryService;
import org.bosik.diacomp.core.services.DiaryService;
import org.bosik.diacomp.persistence.services.TestDiaryService;
import org.bosik.diacomp.persistence.services.web.utils.client.TestWebClient;

public class TestWebDiaryService extends TestDiaryService
{
	@Override
	protected DiaryService getService()
	{
		// DO NOT MAKE IT STATIC - IT CAUSES android.os.NetworkOnMainThreadException
		return new WebDiaryService(TestWebClient.getWebClient());
	}
}
