package org.bosik.compensation.services.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;
import org.bosik.compensation.persistence.dao.DiaryDAO.PageVersion;

public class TestSyncDiary extends TestCase
{
	private static boolean ordered(List<PageVersion> modList)
	{
		for (int i = 0; i < (modList.size() - 1); i++)
		{
			if (modList.get(i).date.compareTo(modList.get(i + 1).date) > 0)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Empty array
	 */
	public void testSort_0()
	{
		List<PageVersion> modList = new ArrayList<PageVersion>();
		SyncDiaryDAO.sort(modList);
		assertTrue(ordered(modList));
	}

	/**
	 * Single element array
	 */
	public void testSort_1()
	{
		List<PageVersion> modList = new ArrayList<PageVersion>();
		modList.add(new PageVersion(new Date(2013, 01, 03), 23));
		SyncDiaryDAO.sort(modList);
		assertTrue(ordered(modList));
	}

	/**
	 * Three unsorted elements array
	 */
	public void testSort_3_unordered()
	{
		List<PageVersion> modList = new ArrayList<PageVersion>();
		modList.add(new PageVersion(new Date(2013, 01, 03), 23));
		modList.add(new PageVersion(new Date(2013, 01, 02), 11));
		modList.add(new PageVersion(new Date(2013, 01, 01), 48));
		SyncDiaryDAO.sort(modList);
		assertTrue(ordered(modList));
	}

	/**
	 * Three sorted elements array
	 */
	public void testSort_3_ordered()
	{
		List<PageVersion> modList = new ArrayList<PageVersion>();
		modList.add(new PageVersion(new Date(2013, 01, 01), 23));
		modList.add(new PageVersion(new Date(2013, 01, 02), 11));
		modList.add(new PageVersion(new Date(2013, 01, 06), 48));
		SyncDiaryDAO.sort(modList);
		assertTrue(ordered(modList));
	}

	public void testGetOverLists1()
	{
		List<PageVersion> modList1 = new ArrayList<PageVersion>();
		modList1.add(new PageVersion(new Date(2013, 01, 01), 23));
		modList1.add(new PageVersion(new Date(2013, 01, 06), 48));
		modList1.add(new PageVersion(new Date(2013, 01, 02), 11));

		List<PageVersion> modList2 = new ArrayList<PageVersion>();
		modList2.add(new PageVersion(new Date(2013, 01, 01), 23));
		modList2.add(new PageVersion(new Date(2013, 01, 02), 11));
		modList2.add(new PageVersion(new Date(2013, 01, 06), 48));

		List<Date> over1 = new ArrayList<Date>();
		List<Date> over2 = new ArrayList<Date>();

		SyncDiaryDAO.getOverLists(modList1, modList2, over1, over2);

		assertTrue(over1.isEmpty());
		assertTrue(over2.isEmpty());
	}

	// TODO: ����������� ��������� �������� getOverLists
}