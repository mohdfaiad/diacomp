package org.bosik.compensation.services;

import java.util.Date;
import java.util.List;
import org.bosik.compensation.bo.FoodMassed;
import org.bosik.compensation.bo.RelativeTagged;
import org.bosik.compensation.bo.diary.DiaryPage;
import org.bosik.compensation.bo.diary.records.DiaryRecord;
import org.bosik.compensation.bo.diary.records.MealRecord;
import org.bosik.compensation.persistence.dao.BaseDAO;
import org.bosik.compensation.persistence.dao.DiaryDAO;
import org.bosik.compensation.persistence.dao.DishBaseDAO;
import org.bosik.compensation.persistence.dao.FoodBaseDAO;
import org.bosik.compensation.utils.Utils;
import android.util.Log;

public class RelevantIndexator
{
	private static final String	TAG	= RelevantIndexator.class.getSimpleName();

	public static void indexate(DiaryDAO diary, FoodBaseDAO foodBase, DishBaseDAO dishBase)
	{
		/**/long time = System.currentTimeMillis();

		// constructing dates list
		final int PERIOD = 30; // days
		final Date max = Utils.now();
		final Date min = new Date(max.getTime() - (PERIOD * 86400000));

		final List<Date> dates = Utils.getPeriodDates(max, PERIOD);

		// clear tags
		clearTags(foodBase);
		clearTags(dishBase);

		// process
		List<DiaryPage> pages = diary.getPages(dates);
		for (DiaryPage page : pages)
		{
			for (int i = 0; i < page.count(); i++)
			{
				DiaryRecord rec = page.get(i);
				if (rec.getClass().equals(MealRecord.class))
				{
					MealRecord meal = (MealRecord) rec;
					for (int j = 0; j < meal.count(); j++)
					{
						FoodMassed food = meal.get(j);
						int delta = f(page.getDate(), min, max);
						process(food.getName(), delta, foodBase, dishBase);
					}
				}
			}
		}

		/**/Log.v(TAG, String.format("Indexated in %d msec", System.currentTimeMillis() - time));
	}

	private static int f(Date curDate, Date minDate, Date maxDate)
	{
		int delta = (int) ((100 * (curDate.getTime() - minDate.getTime())) / (maxDate.getTime() - minDate.getTime()));
		return delta * delta;
	}

	private static <T extends RelativeTagged> void clearTags(BaseDAO<T> base)
	{
		List<T> list = base.findAll();
		for (T item : list)
		{
			item.setTag(0);
			base.update(item);
		}
	}

	private static void process(String name, int delta, FoodBaseDAO foodBase, DishBaseDAO dishBase)
	{
		if (process(name, delta, foodBase))
			return;
		if (process(name, delta, dishBase))
			return;
	}

	@SuppressWarnings("unchecked")
	private static <T extends RelativeTagged> boolean process(String name, int delta, BaseDAO<T> base)
	{
		RelativeTagged item = base.findOne(name);
		if (null != item)
		{
			item.setTag(item.getTag() + delta);
			base.update((T) item);
			return true;
		}
		else
		{
			return false;
		}
	}
}
