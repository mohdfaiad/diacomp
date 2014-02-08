package org.bosik.diacomp.persistence.exceptions;

public class StoreException extends CommonServiceException
{
	private static final long	serialVersionUID	= 1L;

	public StoreException(String msg)
	{
		super(msg);
	}

	public StoreException(Exception e)
	{
		super(e);
	}
}