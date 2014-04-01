package org.bosik.diacomp.android.backend.features.diary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.bosik.diacomp.core.entities.business.diary.DiaryRecord;
import org.bosik.diacomp.core.entities.tech.Versioned;
import org.bosik.diacomp.core.services.diary.DiaryService;

public class DiarySyncService
{
	// private static final String TAG = "DiarySyncService";

	/* ============================ HELPER CLASSES ============================ */

	public static interface Callback
	{
		public void update_max(int max);

		public void update_progress(int progress);
	}

	/* ============================ METHODS ============================ */

	// TODO: made public for test purposes only

	/**
	 * Calculates GUID lists for synchronization
	 *
	 * @param items1
	 *            First list
	 * @param items2
	 *            Second list
	 * @param newer1
	 *            GUIDs of items which has greater version in the first list
	 * @param newer2
	 *            GUIDs of items which has greater version in the second list
	 * @param only1
	 *            GUIDs of items which are presented only in the first list
	 * @param only2
	 *            GUIDs of items which are presented only in the second list
	 */
	public static void getOverLists(List<Versioned<DiaryRecord>> items1, List<Versioned<DiaryRecord>> items2,
			List<Versioned<DiaryRecord>> newer1, List<Versioned<DiaryRecord>> newer2,
			List<Versioned<DiaryRecord>> only1, List<Versioned<DiaryRecord>> only2)
	{
		// null checks
		if (null == items1)
		{
			throw new NullPointerException("items1 can't be null");
		}
		if (null == items2)
		{
			throw new NullPointerException("items2 can't be null");
		}
		if (null == newer1)
		{
			throw new NullPointerException("newer1 can't be null");
		}
		if (null == newer2)
		{
			throw new NullPointerException("newer2 can't be null");
		}
		if (null == only1)
		{
			throw new NullPointerException("only1 can't be null");
		}
		if (null == only2)
		{
			throw new NullPointerException("only2 can't be null");
		}

		// preparation

		Collections.sort(items1, Versioned.COMPARATOR_GUID);
		Collections.sort(items2, Versioned.COMPARATOR_GUID);
		newer1.clear();
		newer2.clear();
		only1.clear();
		only2.clear();
		int i = 0;
		int j = 0;

		// parallel processing
		while ((i < items1.size()) && (j < items2.size()))
		{
			Versioned<DiaryRecord> p1 = items1.get(i);
			Versioned<DiaryRecord> p2 = items2.get(j);
			int c = Versioned.COMPARATOR_GUID.compare(p1, p2);
			if (c < 0)
			{
				only1.add(p1);
				i++;
			}
			else if (c > 0)
			{
				only2.add(p2);
				j++;
			}
			else
			{
				if (p1.getVersion() > p2.getVersion())
				{
					newer1.add(p1);
				}
				else if (p1.getVersion() < p2.getVersion())
				{
					newer2.add(p2);
				}
				i++;
				j++;
			}
		}

		// finish first list
		while (i < items1.size())
		{
			only1.add(items1.get(i));
			i++;
		}

		// finish second list
		while (j < items2.size())
		{
			only2.add(items2.get(j));
			j++;
		}
	}

	/**
	 * Synchronizes two diary services
	 *
	 * @param service1
	 *            First service
	 * @param service2
	 *            Second service
	 * @param since
	 *            Modification time limiter: records modified after this time is taking in account
	 *            only
	 * @return Total number of transferred records
	 */
	public static int synchronize(DiaryService service1, DiaryService service2, Date since)
	{
		// FIXME: see algorithm update for desktop app

		// null checks
		if (null == service1)
		{
			throw new NullPointerException("service1 can't be null");
		}
		if (null == service2)
		{
			throw new NullPointerException("service2 can't be null");
		}
		if (null == since)
		{
			throw new NullPointerException("since date can't be null");
		}

		// requesting items
		List<Versioned<DiaryRecord>> items1 = service1.getRecords(since);
		List<Versioned<DiaryRecord>> items2 = service2.getRecords(since);

		// null checks again
		if (null == items1)
		{
			throw new NullPointerException("modList1 is null");
		}
		if (null == items2)
		{
			throw new NullPointerException("modList2 is null");
		}

		// calculating transferring lists
		List<Versioned<DiaryRecord>> newer1 = new ArrayList<Versioned<DiaryRecord>>();
		List<Versioned<DiaryRecord>> newer2 = new ArrayList<Versioned<DiaryRecord>>();
		List<Versioned<DiaryRecord>> only1 = new ArrayList<Versioned<DiaryRecord>>();
		List<Versioned<DiaryRecord>> only2 = new ArrayList<Versioned<DiaryRecord>>();
		getOverLists(items1, items2, newer1, newer2, only1, only2);

		// debug

		/*
		 * if (BuildConfig.DEBUG) { Log.v(TAG, "1 --> 2 : total count: " +
		 * String.valueOf(over1.size())); for (int i = 0; i < over1.size(); i++) { Log.v(TAG,
		 * "1 --> 2 : " + String.valueOf(over1.get(i))); } Log.v(TAG, "2 --> 1 : total count: " +
		 * String.valueOf(over2.size())); for (int i = 0; i < over2.size(); i++) { Log.v(TAG,
		 * "2 --> 1 : " + String.valueOf(over2.get(i))); } }
		 */

		// checking items with are only partially presented
		for (Versioned<DiaryRecord> item1 : only1)
		{
			Versioned<DiaryRecord> item2 = service2.getRecord(item1.getId());
			if ((item2 == null) || (item2.getVersion() < item1.getVersion()))
			{
				newer1.add(item1);
			}
			else if (item2.getVersion() > item1.getVersion())
			{
				newer2.add(item2);
			}
		}

		for (Versioned<DiaryRecord> item2 : only2)
		{
			Versioned<DiaryRecord> item1 = service1.getRecord(item2.getId());
			if ((item1 == null) || (item1.getVersion() < item2.getVersion()))
			{
				newer2.add(item2);
			}
			else if (item1.getVersion() > item2.getVersion())
			{
				newer1.add(item1);
			}
		}

		// transfer

		// THINK: divide into small groups?
		service1.postRecords(newer2);
		service2.postRecords(newer1);

		// Result is number of transferred records
		return newer1.size() + newer2.size();
	}
}
