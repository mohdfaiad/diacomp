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
package org.bosik.diacomp.android.backend.features.analyze;

import java.util.Date;
import java.util.List;
import org.bosik.diacomp.core.entities.business.diary.DiaryRecord;
import org.bosik.diacomp.core.services.analyze.AnalyzeCore;
import org.bosik.diacomp.core.services.analyze.KoofService;
import org.bosik.diacomp.core.services.analyze.entities.Koof;
import org.bosik.diacomp.core.services.analyze.entities.KoofList;
import org.bosik.diacomp.core.services.diary.DiaryService;
import org.bosik.diacomp.core.utils.Utils;
import org.bosik.merklesync.Versioned;
import android.content.Context;

public class KoofServiceImpl implements KoofService
{
	private static final String	TAG			= KoofServiceImpl.class.getSimpleName();

	private final DiaryService	diaryService;
	private final AnalyzeCore	analyzeCore;
	private final KoofDao		koofDao;
	private final int			analyzePeriod;
	private final double		adaptation;

	private static final Koof	STD_KOOF	= new Koof(0.25, 2.5, 0.0);

	/**
	 * 
	 * @param diaryService
	 * @param analyzeCore
	 * @param analyzePeriod
	 *            In days
	 * @param adaptation
	 *            [0 .. 0.1]
	 */
	public KoofServiceImpl(Context context, DiaryService diaryService, AnalyzeCore analyzeCore, int analyzePeriod,
			double adaptation)
	{
		this.diaryService = diaryService;
		this.analyzeCore = analyzeCore;
		this.koofDao = new KoofDao(context);
		this.analyzePeriod = analyzePeriod;
		this.adaptation = adaptation;
	}

	@Override
	public void update()
	{
		Date timeTo = new Date();
		Date timeFrom = new Date(timeTo.getTime() - (analyzePeriod * Utils.MsecPerDay));
		List<Versioned<DiaryRecord>> recs = diaryService.findPeriod(timeFrom, timeTo, false);
		KoofList koofs = analyzeCore.analyze(recs);

		if (koofs != null) // null in case i.g. there are no diary records
		{
			koofDao.save(koofs);
		}
	}

	@Override
	public Koof getKoof(int time)
	{
		Koof koof = koofDao.find(time % Utils.MinPerDay);
		return koof != null ? koof : STD_KOOF;
	}
}