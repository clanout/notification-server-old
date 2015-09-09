package reaper.notificationserver.api;

import org.apache.log4j.Logger;
import reaper.notificationserver.api.exceptions.HttpExceptions;
import reaper.notificationserver.api.request.BroadcastNotificationRequest;
import reaper.notificationserver.api.request.MulticastNotificationRequest;
import reaper.notificationserver.api.request.NotificationPullRequest;
import reaper.notificationserver.api.request.NotificationRegistrationRequest;
import reaper.notificationserver.service.GsonProvider;
import reaper.notificationserver.service.Notification;
import reaper.notificationserver.service.NotificationService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
            NotificationRegistrationRequest request = GsonProvider.get().fromJson(postDataJson, NotificationRegistrationRequest.class);

            String userId = request.getUserId();
            String token = request.getToken();

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
            MulticastNotificationRequest request = GsonProvider.get().fromJson(postDatJson, MulticastNotificationRequest.class);

            List<String> userIds = request.getUserIds();
            Notification.Data data = request.getNotification();

            if (userIds == null || userIds.isEmpty() || data == null)
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
            BroadcastNotificationRequest request = GsonProvider.get().fromJson(postDatJson, BroadcastNotificationRequest.class);

            String channelId = request.getChannelId();
            Notification.Data data = request.getNotification();

            if (channelId == null || channelId.isEmpty() || data == null)
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
            NotificationPullRequest request = GsonProvider.get().fromJson(postDatJson, NotificationPullRequest.class);

            String userId = request.getUserId();

            if (userId == null || userId.isEmpty())
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
