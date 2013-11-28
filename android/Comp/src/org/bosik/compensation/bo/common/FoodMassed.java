package org.bosik.compensation.bo.common;

import org.bosik.compensation.persistence.common.Unique;

/**
 * Stores food's name, relative parameters (PFCV on 100g) and mass. Has methods for serialization /
 * deserialization.
 * 
 * @author Bosik
 * 
 */
public class FoodMassed extends Food
{
	private double	mass;

	public FoodMassed()
	{

	}

	public FoodMassed(String name, double relProts, double relFats, double relCarbs, double relValue, double mass)
	{
		super(name, relProts, relFats, relCarbs, relValue);
		setMass(mass);
	}

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

	public double getProts()
	{
		return (getRelProts() / 100) * mass;
	}

	public double getFats()
	{
		return (getRelFats() / 100) * mass;
	}

	public double getCarbs()
	{
		return (getRelCarbs() / 100) * mass;
	}

	public double getValue()
	{
		return (getRelValue() / 100) * mass;
	}

	// ================================ CLONE ================================

	@Override
	public Unique clone() throws CloneNotSupportedException
	{
		FoodMassed result = (FoodMassed) super.clone();
		result.setMass(getMass());
		return result;
	}
}
