package reaper.notificationserver.api.request;

import com.google.gson.annotations.SerializedName;

public class NotificationRegistrationRequest
{
    @SerializedName("user_id")
    private String userId;

    @SerializedName("token")
    private String token;

    public String getUserId()
    {
        return userId;
    }

    public String getToken()
    {
        return token;
    }
}
