package reaper.notificationserver.api;

import reaper.notificationserver.api.exceptions.HttpExceptions;
import reaper.notificationserver.api.request.Request;
import reaper.notificationserver.api.request.RequestFactory;
import reaper.notificationserver.service.NotificationService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/")
public class NotificationServer
{
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

            if(userId == null || token == null)
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
}
