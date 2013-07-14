package bosik.compensation.data.test;

import junit.framework.TestCase;
import org.bosik.compensation.persistence.entity.foodbase.Food;
import org.bosik.compensation.persistence.entity.foodbase.FoodBase;
import org.bosik.compensation.persistence.repository.foodbase.FoodBaseFormatter;

public class FoodBaseFormatterTest extends TestCase
{
	private final String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<foods version=\"167\">\n"
			+ "	<food name=\"�������\" prots=\"0.9\" fats=\"0.1\" carbs=\"9\" val=\"41\" table=\"True\"/>\n"
			+ "	<food name=\"����\" prots=\"12.7\" fats=\"11.5\" carbs=\"0.7\" val=\"157\" table=\"False\"/>\n" + "</foods>";

	public void testGetVersion()
	{
		int version = FoodBaseFormatter.getVersion(xml);
		assertEquals(167, version);
	}

	public void testRead()
	{
		FoodBase base = FoodBaseFormatter.read(xml);

		assertEquals(167, base.getVersion());
		assertEquals(2, base.count());

		assertEquals("�������", base.get(0).getName());
		assertEquals(0.9, base.get(0).getRelProts());
		assertEquals(0.1, base.get(0).getRelFats());
		assertEquals(9.0, base.get(0).getRelCarbs());
		assertEquals(41.0, base.get(0).getRelValue());
		assertEquals(true, base.get(0).getFromTable());

		assertEquals("����", base.get(1).getName());
		assertEquals(12.7, base.get(1).getRelProts());
		assertEquals(11.5, base.get(1).getRelFats());
		assertEquals(0.7, base.get(1).getRelCarbs());
		assertEquals(157.0, base.get(1).getRelValue());
		assertEquals(false, base.get(1).getFromTable());
	}

	public void testWriteRead()
	{
		FoodBase base = new FoodBase();
		Food food;

		food = new Food();
		food.setName("�������");
		food.setRelProts(0.9);
		food.setRelFats(0.1);
		food.setRelCarbs(9.0);
		food.setRelValue(41);
		food.setFromTable(true);
		base.add(food);

		food = new Food();
		food.setName("����");
		food.setRelProts(12.7);
		food.setRelFats(11.5);
		food.setRelCarbs(0.7);
		food.setRelValue(157);
		food.setFromTable(false);
		base.add(food);

		String xml = FoodBaseFormatter.write(base);

		// =======================================================

		FoodBase anotherBase = FoodBaseFormatter.read(xml);

		assertEquals(base.getVersion(), anotherBase.getVersion());
		assertEquals(base.getVersion(), 2);
		assertEquals(base.count(), anotherBase.count());
		assertEquals(base.count(), 2);

		for (int i = 0; i < base.count(); i++)
		{
			Food food1 = base.get(i);
			Food food2 = anotherBase.get(i);
			
			assertEquals(food1.getName(), food2.getName());
			assertEquals(food1.getRelProts(), food2.getRelProts());
			assertEquals(food1.getRelFats(), food2.getRelFats());
			assertEquals(food1.getRelCarbs(), food2.getRelCarbs());
			assertEquals(food1.getRelValue(), food2.getRelValue());
			assertEquals(food1.getFromTable(), food2.getFromTable());
		}
	}
}
