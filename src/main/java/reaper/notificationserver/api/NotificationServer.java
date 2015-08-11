package reaper.notificationserver.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import reaper.notificationserver.api.exceptions.HttpExceptions;
import reaper.notificationserver.api.request.Request;
import reaper.notificationserver.api.request.RequestFactory;
import reaper.notificationserver.service.GsonProvider;
import reaper.notificationserver.service.NotificationService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path("/")
public class NotificationServer
{
    private static Logger log = Logger.getLogger(NotificationServer.class);

    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(String postDataJson)
    {
        try
        {
            Request request = RequestFactory.create(postDataJson);

            String userId = request.get("user_id");
            String token = request.get("token");

            if (userId == null || token == null)
            {
                throw new HttpExceptions.BadRequest();
            }

            NotificationService service = new NotificationService();
            service.register(userId, token);

            return Response.ok().build();
        }
        catch (HttpExceptions.BadRequest e)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        catch (HttpExceptions.ServerError e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("send")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(String postDatJson)
    {
        try
        {
            Request request = RequestFactory.create(postDatJson);

            List<String> userIds;
            try
            {
                String recipientJson = request.get("to");
                Type type = new TypeToken<List<String>>()
                {
                }.getType();
                userIds = new Gson().fromJson(recipientJson, type);
            }
            catch (Exception e)
            {
                log.error("Unable to process recipient user id list");
                throw new HttpExceptions.BadRequest();
            }

            String data = request.get("data");

            if(userIds == null || userIds.isEmpty() || data == null || data.isEmpty())
            {
                log.error("user_ids/data cannot be null/empty");
                throw new HttpExceptions.BadRequest();
            }

            NotificationService service = new NotificationService();
            service.send(userIds, data);

            return Response.ok().build();
        }
        catch (HttpExceptions.BadRequest e)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        catch (HttpExceptions.ServerError e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("broadcast")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response broadcast(String postDatJson)
    {
        try
        {
            Request request = RequestFactory.create(postDatJson);

            String channelId = request.get("to");
            String data = request.get("data");

            if(channelId == null || channelId.isEmpty() || data == null || data.isEmpty())
            {
                log.error("channel_id/data cannot be null/empty");
                throw new HttpExceptions.BadRequest();
            }

            NotificationService service = new NotificationService();
            service.send(channelId, data);

            return Response.ok().build();
        }
        catch (HttpExceptions.BadRequest e)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        catch (HttpExceptions.ServerError e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("pull")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pull(String postDatJson)
    {
        try
        {
            Request request = RequestFactory.create(postDatJson);

            String userId = request.get("user_id");

            if(userId == null || userId.isEmpty())
            {
                log.error("user_id cannot be null/empty");
                throw new HttpExceptions.BadRequest();
            }

            NotificationService service = new NotificationService();
            Map<String, List<String>> result = new HashMap<>();
            result.put("notifications", service.pullFailedNotifications(userId));

            return Response.ok(GsonProvider.get().toJson(result), MediaType.APPLICATION_JSON_TYPE).build();
        }
        catch (HttpExceptions.BadRequest e)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        catch (HttpExceptions.ServerError e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
