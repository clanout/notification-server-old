package reaper.notificationserver.api.request;

import com.google.gson.annotations.SerializedName;

public class NotificationPullRequest
{
    @SerializedName("user_id")
    private String userId;

    public String getUserId()
    {
        return userId;
    }
}
