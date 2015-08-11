package reaper.notificationserver.service.gcm;

import reaper.notificationserver.service.Notification;
import retrofit.http.Body;
import retrofit.http.POST;

public interface GcmApi
{
    @POST("/send")
    GcmResponse send(@Body Notification notification);
}
