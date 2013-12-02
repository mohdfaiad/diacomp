package org.bosik.compensation.bo.foodbase;

import java.util.Locale;
import org.bosik.compensation.bo.common.Food;
import org.bosik.compensation.persistence.common.UniqueNamed;

/**
 * Food item for food base
 * 
 * @author Bosik
 * 
 */
public class FoodItem extends Food implements Cloneable
{
	private static final long	serialVersionUID	= -1062568910858912955L;

	private boolean				fromTable;
	private int					tag;

	// ================================ GET / SET ================================

	public boolean getFromTable()
	{
		return fromTable;
	}

	public void setFromTable(boolean fromTable)
	{
		this.fromTable = fromTable;
	}

	public int getTag()
	{
		return tag;
	}

	public void setTag(int tag)
	{
		this.tag = tag;
	}

	// ================================ CLONE ================================

	@Override
	public UniqueNamed clone() throws CloneNotSupportedException
	{
		FoodItem result = (FoodItem) super.clone();

		result.setFromTable(getFromTable());
		result.setTag(getTag());

		return result;
	}

	@Override
	public String toString()
	{
		return String.format(Locale.US, "%s: %s[%.1f|%.1f|%.1f|%.1f]:%s", getId(), getName(), getRelProts(),
				getRelFats(), getRelCarbs(), getRelValue(), getFromTable());
	}
}
