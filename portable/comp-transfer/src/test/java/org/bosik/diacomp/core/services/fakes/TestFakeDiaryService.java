package org.bosik.diacomp.core.services.fakes;

import org.bosik.diacomp.core.services.diary.DiaryService;
import org.bosik.diacomp.core.services.diary.TestDiaryServiceCommon;
import org.bosik.diacomp.core.test.fakes.services.FakeDiaryService;

public class TestFakeDiaryService extends TestDiaryServiceCommon
{
	private final DiaryService	service	= new FakeDiaryService(false);

	@Override
	protected DiaryService getService()
	{
		return service;
	}
}