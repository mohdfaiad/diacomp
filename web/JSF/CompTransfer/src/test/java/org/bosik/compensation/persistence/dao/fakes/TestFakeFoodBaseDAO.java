package org.bosik.compensation.persistence.dao.fakes;

import org.bosik.compensation.fakes.dao.FakeFoodBaseDAO;
import org.bosik.compensation.persistence.dao.FoodBaseDAO;
import org.bosik.compensation.persistence.dao.TestFoodBaseDAO;
import org.junit.Ignore;

@Ignore
public class TestFakeFoodBaseDAO extends TestFoodBaseDAO
{
	@Override
	protected FoodBaseDAO getDAO()
	{
		return new FakeFoodBaseDAO();
	}
}