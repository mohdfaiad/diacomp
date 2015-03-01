package org.bosik.diacomp.web.backend.features.diary;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.bosik.diacomp.core.entities.business.diary.DiaryRecord;
import org.bosik.diacomp.core.entities.tech.Versioned;
import org.bosik.diacomp.core.persistence.parsers.Parser;
import org.bosik.diacomp.core.persistence.parsers.ParserDiaryRecord;
import org.bosik.diacomp.core.persistence.serializers.Serializer;
import org.bosik.diacomp.core.persistence.serializers.SerializerMap;
import org.bosik.diacomp.core.persistence.utils.ParserVersioned;
import org.bosik.diacomp.core.persistence.utils.SerializerAdapter;
import org.bosik.diacomp.core.rest.ResponseBuilder;
import org.bosik.diacomp.core.services.ObjectService;
import org.bosik.diacomp.core.services.diary.DiaryService;
import org.bosik.diacomp.core.services.exceptions.CommonServiceException;
import org.bosik.diacomp.core.services.exceptions.NotAuthorizedException;
import org.bosik.diacomp.core.services.exceptions.TooManyItemsException;
import org.bosik.diacomp.core.utils.Utils;
import org.bosik.diacomp.web.backend.common.UserSessionUtils;

@Path("diary/")
public class DiaryRest
{
	@Context
	HttpServletRequest									req;

	private final DiaryService							diaryService	= new DiaryLocalService()
																		{
																			@Override
																			protected int getCurrentUserId()
																			{
																				return UserSessionUtils.getId(req);
																			};
																		};

	private final Parser<DiaryRecord>					parser			= new ParserDiaryRecord();
	private final Parser<Versioned<DiaryRecord>>		parserVersioned	= new ParserVersioned<DiaryRecord>(parser);
	private final Serializer<Versioned<DiaryRecord>>	serializer		= new SerializerAdapter<Versioned<DiaryRecord>>(
																				parserVersioned);
	private final Serializer<Map<String, String>>		serializerMap	= new SerializerMap();

	@GET
	@Path("count/{prefix: .*}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response count(@PathParam("prefix") @DefaultValue("") String parPrefix) throws CommonServiceException
	{
		long time = System.currentTimeMillis();
		try
		{
			int count = diaryService.count(parPrefix);
			String response = String.valueOf(count);
			return Response.ok(response).build();
		}
		catch (NotAuthorizedException e)
		{
			return Response.status(Status.UNAUTHORIZED).entity(ResponseBuilder.buildNotAuthorized()).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ResponseBuilder.buildFails()).build();
		}
		finally
		{
			time = System.currentTimeMillis() - time;
			System.out.println(String.format("count(%s) performed in %d msec", parPrefix, time));
		}
	}

	@GET
	@Path("guid/{guid: .*}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response findById(@PathParam("guid") String parId) throws CommonServiceException
	{
		long time = System.currentTimeMillis();
		try
		{
			// Prefix form
			if (parId.length() <= ObjectService.ID_PREFIX_SIZE)
			{
				List<Versioned<DiaryRecord>> items = diaryService.findByIdPrefix(parId);

				String response = serializer.writeAll(items);
				return Response.ok(response).build();
			}

			// Full form
			else
			{
				Versioned<DiaryRecord> item = diaryService.findById(parId);

				if (item != null)
				{
					String response = serializer.write(item);
					return Response.ok(response).build();
				}
				else
				{
					String response = String.format("Item %s not found", parId);
					return Response.status(Status.NOT_FOUND).entity(response).build();
				}
			}
		}
		catch (NotAuthorizedException e)
		{
			return Response.status(Status.UNAUTHORIZED).entity(ResponseBuilder.buildNotAuthorized()).build();
		}
		catch (TooManyItemsException e)
		{
			return Response.status(Status.BAD_REQUEST).entity(ResponseBuilder.buildFails("Too many items found"))
					.build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ResponseBuilder.buildFails()).build();
		}
		finally
		{
			time = System.currentTimeMillis() - time;
			System.out.println(String.format("findById(%s) performed in %d msec", parId, time));
		}
	}

	@GET
	@Path("hash/{prefix: .*}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getHash(@PathParam("prefix") @DefaultValue("") String parPrefix) throws CommonServiceException
	{
		try
		{
			String s = diaryService.getHash(parPrefix);
			String response = s != null ? s : "";
			return Response.ok(response).build();
		}
		catch (NotAuthorizedException e)
		{
			return Response.status(Status.UNAUTHORIZED).entity(ResponseBuilder.buildNotAuthorized()).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ResponseBuilder.buildFails()).build();
		}
	}

	@GET
	@Path("hashes/{prefix: .*}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getHashChildren(@PathParam("prefix") @DefaultValue("") String parPrefix)
			throws CommonServiceException
	{
		try
		{
			Map<String, String> map = diaryService.getHashChildren(parPrefix);
			String response = serializerMap.write(map);
			return Response.ok(response).build();
		}
		catch (NotAuthorizedException e)
		{
			return Response.status(Status.UNAUTHORIZED).entity(ResponseBuilder.buildNotAuthorized()).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ResponseBuilder.buildFails()).build();
		}
	}

	@GET
	@Path("changes")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response findChanged(@QueryParam("since") String parTime) throws CommonServiceException
	{
		try
		{
			Date since = Utils.parseTimeUTC(parTime);
			List<Versioned<DiaryRecord>> items = diaryService.findChanged(since);
			String response = serializer.writeAll(items);
			return Response.ok(response).build();
		}
		catch (NotAuthorizedException e)
		{
			return Response.status(Status.UNAUTHORIZED).entity(ResponseBuilder.buildNotAuthorized()).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ResponseBuilder.buildFails()).build();
		}
	}

	@GET
	@Path("period")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response findPeriod(@QueryParam("start_time") String parStartTime,
			@QueryParam("end_time") String parEndTime, @QueryParam("show_rem") String parShowRem)
			throws CommonServiceException
	{
		try
		{
			Date startTime = Utils.parseTimeUTC(parStartTime);
			Date endTime = Utils.parseTimeUTC(parEndTime);
			boolean includeRemoved = Boolean.valueOf(parShowRem);

			List<Versioned<DiaryRecord>> items = diaryService.findPeriod(startTime, endTime, includeRemoved);
			String response = serializer.writeAll(items);
			return Response.ok(response).build();
		}
		catch (NotAuthorizedException e)
		{
			return Response.status(Status.UNAUTHORIZED).entity(ResponseBuilder.buildNotAuthorized()).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ResponseBuilder.buildFails()).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response save(@FormParam("items") String parItems) throws CommonServiceException
	{
		try
		{
			List<Versioned<DiaryRecord>> items = serializer.readAll(parItems);
			diaryService.save(items);

			String response = "Saved OK";
			return Response.ok(response).build();
		}
		catch (NotAuthorizedException e)
		{
			return Response.status(Status.UNAUTHORIZED).entity(ResponseBuilder.buildNotAuthorized()).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ResponseBuilder.buildFails()).build();
		}
	}
}