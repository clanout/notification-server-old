package reaper.notificationserver.service;

import org.apache.log4j.Logger;
import reaper.notificationserver.api.exceptions.HttpExceptions;
import reaper.notificationserver.db.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            if(connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e1)
                {
                }
            }
            log.error("Unable to register user", e);
            throw new HttpExceptions.ServerError();
        }
    }
}
