package bosik.compensation.data.test;

import org.bosik.compensation.persistence.entity.FoodData;

import junit.framework.TestCase;

public class FoodDataTest extends TestCase
{
	private FoodData food = new FoodData();
	
	public void testName()
	{
		// ���������� ����
		food.setName("name"); assertEquals("name", food.getName());
		
		// ����-����		
		try { food.setName(null);	fail(); } catch (Exception e) {}
		try { food.setName("  "); 	fail(); } catch (Exception e) {}
		try { food.setName(""); 	fail(); } catch (Exception e) {}
	}

	public void testRelProts()
	{
		// ���������� ����
		food.setRelProts(0.0); assertEquals(0.0, food.getRelProts());
		food.setRelProts(12.8); assertEquals(12.8, food.getRelProts());
		food.setRelProts(100.0); assertEquals(100.0, food.getRelProts());
		
		// ����-����		
		try { food.setRelProts(-0.01);	fail(); } catch (Exception e) {}
		try { food.setRelProts(100.01);	fail(); } catch (Exception e) {}
	}

	public void testRelFats()
	{
		// ���������� ����
		food.setRelFats(0.0); assertEquals(0.0, food.getRelFats());
		food.setRelFats(12.8); assertEquals(12.8, food.getRelFats());
		food.setRelFats(100.0); assertEquals(100.0, food.getRelFats());
		
		// ����-����		
		try { food.setRelFats(-0.01);	fail(); } catch (Exception e) {}
		try { food.setRelFats(100.01); 	fail(); } catch (Exception e) {}
	}

	public void testRelCarbs()
	{
		// ���������� ����
		food.setRelCarbs(0.0); assertEquals(0.0, food.getRelCarbs());
		food.setRelCarbs(12.8); assertEquals(12.8, food.getRelCarbs());
		food.setRelCarbs(100.0); assertEquals(100.0, food.getRelCarbs());
		
		// ����-����		
		try { food.setRelCarbs(-0.01);	fail(); } catch (Exception e) {}
		try { food.setRelCarbs(100.01);	fail(); } catch (Exception e) {}
	}

	public void testRelValue()
	{
		// ���������� ����
		food.setRelValue(0.0); assertEquals(0.0, food.getRelValue());
		food.setRelValue(12.8); assertEquals(12.8, food.getRelValue());
		food.setRelValue(102.8); assertEquals(102.8, food.getRelValue());
		
		// ����-����		
		try { food.setRelValue(-0.01);	fail(); } catch (Exception e) {}
	}
}
