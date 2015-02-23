package org.bosik.diacomp.android.backend.common.webclient.exceptions;

/**
 * Authentication exception
 * 
 * @author Bosik
 */
// TODO: cleanup Android exceptions
public class AuthException extends WebClientException
{
	private static final long	serialVersionUID	= 7885618396446513997L;

	public AuthException(String detailMessage)
	{
		super(detailMessage);
	}
}
