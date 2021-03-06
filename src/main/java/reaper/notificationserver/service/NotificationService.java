package reaper.notificationserver.service;

import org.apache.log4j.Logger;
import reaper.notificationserver.api.exceptions.HttpExceptions;
import reaper.notificationserver.db.DataSource;
import reaper.notificationserver.service.gcm.GcmApi;
import reaper.notificationserver.service.gcm.GcmHelper;
import reaper.notificationserver.service.gcm.GcmResponse;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class NotificationService
{
    private static Logger log = Logger.getLogger(NotificationService.class);

    public void register(String userId, String token) throws HttpExceptions.ServerError
    {
        String SQL_DELETE = "DELETE FROM users where user_id = ?";
        String SQL_INSERT = "INSERT INTO users VALUES (?, ?, ?)";

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
            preparedStatement.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()));
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

    public void send(List<String> userIds, Notification.Data data) throws HttpExceptions.ServerError
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
                Notification notification = NotificationFactory.multicastNotification(tokens, data);
                GcmApi gcmApi = GcmHelper.getApi();
                GcmResponse response = gcmApi.send(notification);

                if (response != null)
                {
                    log.info(response.getMusticastId() + " : Failure Count = " + response.getFailureCount() + "; Reg. ID update count = " + response.getCanonicalIdCount());

                    if (response.getFailureCount() != 0 || response.getCanonicalIdCount() != 0)
                    {
                        List<GcmResponse.Result> results = response.getResults();
                        int size = results.size();
                        for (int i = 0; i < size; i++)
                        {
                            GcmResponse.Result result = results.get(i);
                            String userId = userIds.get(i);

                            log.info("Multicast Error : " + userId + " -> " + GsonProvider.get().toJson(result));

                            if (result.getRegistrationId() != null)
                            {
                                register(userId, result.getRegistrationId());
                            }

                            if (result.getError() != null)
                            {
                                saveFailedNotification(result.getMessageId(), userId, GsonProvider.get().toJson(data));
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

    public void send(String channelId, Notification.Data data) throws HttpExceptions.ServerError
    {
        channelId = "/topics/" + channelId;

        Notification notification = NotificationFactory.broadcastNotification(channelId, data);
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

    public List<String> pullFailedNotifications(String userId)throws HttpExceptions.ServerError
    {
        List<String> result = new ArrayList<>();

        String SQL_READ = "SELECT notification_data from notification_failures WHERE user_id = ? ORDER BY time_created DESC";

        Connection connection = null;
        try
        {
            connection = DataSource.getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(SQL_READ);
            preparedStatement.setString(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
            {
                result.add(resultSet.getString(1));
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();

            return result;
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
            log.error("Unable to pull pending notifications for user (" + userId + ")", e);
            throw new HttpExceptions.ServerError();
        }
    }

    private void saveFailedNotification(String notificationId, String userId, String notificationJsonData)
    {
        String SQL_INSERT = "INSERT INTO notification_failures VALUES (?, ?, ?)";

        Connection connection = null;
        try
        {
            connection = DataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, notificationJsonData);
            preparedStatement.setTimestamp(3, Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()));
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
