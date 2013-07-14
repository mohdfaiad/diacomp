package org.bosik.compensation.persistence.entity;

import java.text.DecimalFormat;
import java.text.ParseException;
import org.bosik.compensation.persistence.entity.common.CustomItem;
import org.bosik.compensation.utils.Utils;

public class FoodMassed extends FoodData
{
	private static final DecimalFormat df = new DecimalFormat("###.#");
	private static final char FOOD_SEP = '|';
	
	private double mass;

	// ================================ GET / SET ================================

	public double getMass()
	{
		return mass;
	}

	public void setMass(double mass)
	{
		checkNonNegativeThrowable(mass);
		this.mass = mass;
	}

	// TODO: подумать об индексном доступе к полям (в т.ч. в MealRecord)
	public double getProts()
	{
		return getRelProts() / 100 * mass;
	}

	public double getFats()
	{
		return getRelFats() / 100 * mass;
	}

	public double getCarbs()
	{
		return getRelCarbs() / 100 * mass;
	}

	public double getValue()
	{
		return getRelValue() / 100 * mass;
	}

	// ================================ CLONE ================================

	@Override
	public CustomItem clone() throws CloneNotSupportedException
	{
		FoodMassed result = (FoodMassed) super.clone();

		result.setMass(getMass());

		return result;
	}

	// ================================ I / O ================================

	/**
	 * Читает из текстового представления FoodMassed
	 * 
	 * @param s
	 *            Строка
	 * @throws ParseException
	 */
	public void read(String s) throws ParseException
	{
		String[] t = s.split("[\\[" + FOOD_SEP + "\\]:]+"); // БОЯН :D

		if (t.length != 6)
			throw new IllegalArgumentException("Incorrect FoodMassed format: " + s);

		// внутри сеттеров - дополнительные проверки
		setName(t[0]);
		setRelProts(Utils.parseDouble(t[1]));
		setRelFats(Utils.parseDouble(t[2]));
		setRelCarbs(Utils.parseDouble(t[3]));
		setRelValue(Utils.parseDouble(t[4]));
		setMass(Utils.parseDouble(t[5]));
	}

	/**
	 * Создаёт текстовое представление
	 * 
	 * @return Строка
	 */
	public String write()
	{
		return getName() + '[' + df.format(getRelProts()) + FOOD_SEP + df.format(getRelFats()) + FOOD_SEP + df.format(getRelCarbs()) + FOOD_SEP
				+ df.format(getRelValue()) + "]:" + df.format(mass);
	}

	/**
	 * Создаёт демо-экземпляр для тестирования
	 * 
	 * @return
	 */
	public static FoodMassed demo()
	{
		FoodMassed food = new FoodMassed();
		food.setName("Колбаса");
		food.setMass(78);
		food.setRelProts(12.2);
		food.setRelFats(18.9);
		food.setRelCarbs(0);
		food.setRelValue(272);
		return food;
	}
}
