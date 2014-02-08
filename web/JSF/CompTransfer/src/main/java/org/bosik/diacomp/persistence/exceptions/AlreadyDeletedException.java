package org.bosik.diacomp.persistence.exceptions;

public class AlreadyDeletedException extends CommonServiceException
{
	private static final long	serialVersionUID	= 1L;

	public AlreadyDeletedException(String id)
	{
		super(String.format("Item '%s' is already deleted", id));
	}
}
