package reaper.notificationserver.service;

import org.apache.log4j.Logger;
import reaper.notificationserver.api.exceptions.HttpExceptions;
import reaper.notificationserver.db.DataSource;
import reaper.notificationserver.service.gcm.GcmApi;
import reaper.notificationserver.service.gcm.GcmHelper;
import reaper.notificationserver.service.gcm.GcmResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationService
{
    private static Logger log = Logger.getLogger(NotificationService.class);

    public void register(String userId, String token) throws HttpExceptions.ServerError
    {
        String SQL_DELETE = "DELETE FROM users where user_id = ?";
        String SQL_INSERT = "INSERT INTO users VALUES (?, ?, CURRENT_TIMESTAMP)";

        Connection connection = null;
        try
        {
            connection = DataSource.getConnection();
            connection.setAutoCommit(false);

            PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE);
            preparedStatement.setString(1, userId);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(SQL_INSERT);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, token);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            connection.commit();
            connection.close();

            log.info("Registered : " + userId + " -> " + token);
        }
        catch (SQLException e)
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e1)
                {
                    log.error("Unable to close db connection [" + e1.getMessage() + "]");
                }
            }
            log.error("Unable to register user (" + userId + ")", e);
            throw new HttpExceptions.ServerError();
        }
    }

    public void send(List<String> userIds, String jsonData) throws HttpExceptions.ServerError
    {
        List<String> tokens = new ArrayList<>();

        String SQL_SELECT = "SELECT token from users WHERE user_id = ANY(?)";

        Connection connection = null;
        try
        {
            connection = DataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT);
            preparedStatement.setArray(1, connection.createArrayOf("varchar", userIds.toArray()));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                tokens.add(resultSet.getString(1));
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();

            if (!tokens.isEmpty())
            {
                Notification notification = NotificationFactory.multicastNotification(tokens, jsonData);
                GcmApi gcmApi = GcmHelper.getApi();
                GcmResponse response = gcmApi.send(notification);

                if (response != null)
                {
                    if (response.getFailureCount() != 0 || response.getCanonicalIdCount() != 0)
                    {
                        log.info(response.getMusticastId() + " : Failure Count = " + response.getFailureCount() + "; Reg. ID update count = " + response.getCanonicalIdCount());
                        List<GcmResponse.Result> results = response.getResults();
                        int size = results.size();
                        for (int i = 0; i < size; i++)
                        {
                            GcmResponse.Result result = results.get(i);
                            String userId = userIds.get(i);

                            if (result.getRegistrationId() != null)
                            {
                                register(userId, result.getRegistrationId());
                            }

                            if (result.getError() != null)
                            {
                                saveFailedNotification(result.getMessageId(), userId, jsonData);
                            }
                        }
                    }
                }
                else
                {
                    log.error("Failed to send multicast notification to " + userIds.toString());
                    throw new HttpExceptions.ServerError();
                }
            }
        }
        catch (SQLException e)
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e1)
                {
                    log.error("Unable to close db connection [" + e1.getMessage() + "]");
                }
            }
            log.error("Unable to send notification", e);
            throw new HttpExceptions.ServerError();
        }
    }

    public void send(String channelId, String jsonData) throws HttpExceptions.ServerError
    {
        channelId = "/topics/" + channelId;

        Notification notification = NotificationFactory.broadcastNotification(channelId, jsonData);
        GcmApi gcmApi = GcmHelper.getApi();
        GcmResponse response = gcmApi.send(notification);

        if (response.getError() == null)
        {
            log.info("Successfully broadcasted message : " + response.getMessageId());
        }
        else
        {
            log.error("Failed to broadcast message [" + response.getError() + "]");
            throw new HttpExceptions.ServerError();
        }
    }

    private void saveFailedNotification(String notificationId, String userId, String notificationJsonData)
    {
        String SQL_INSERT = "INSERT INTO notification_failures VALUES (?, ?, CURRENT_TIMESTAMP)";

        Connection connection = null;
        try
        {
            connection = DataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, notificationJsonData);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        }
        catch (SQLException e)
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e1)
                {
                    log.error("Unable to close db connection [" + e1.getMessage() + "]");
                }

                log.error("Unable to save failed notification for user (" + userId + ")", e);
            }
        }
    }

}
