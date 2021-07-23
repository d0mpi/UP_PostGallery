package by.bsu.d0mpi.UP_PostGallery.dao.impl;

import by.bsu.d0mpi.UP_PostGallery.dao.UserDao;
import by.bsu.d0mpi.UP_PostGallery.exception.DAOException;
import by.bsu.d0mpi.UP_PostGallery.model.Like;
import by.bsu.d0mpi.UP_PostGallery.model.Role;
import by.bsu.d0mpi.UP_PostGallery.model.User;
import by.bsu.d0mpi.UP_PostGallery.pool.BasicConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class MySqlUserDao extends MySqlAbstractDao<Integer, User> implements UserDao {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SQL_UPDATE_USER =
            "UPDATE users SET user_login = ?, user_password = ?, user_role_id = ? WHERE user_id = ?";
    private static final String SQL_INSERT_USER =
            "INSERT INTO users (user_login, user_password, user_role_id, user_registration_time) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_LOGIN =
            "SELECT * FROM users WHERE user_login = ?";
    private static final String SQL_COUNT_OF_POSTS_BY_AUTHOR =
            "SELECT COUNT(post_id) FROM posts WHERE post_author = ?";
    private static final String SQL_COUNT_OF_LIKES_BY_AUTHOR =
            "SELECT COUNT(post_id) FROM posts JOIN likes ON post_id = likes_post_id where post_author = ? AND post_author!=likes_user_login";


    private static MySqlUserDao instance;

    private MySqlUserDao(String tableName, String idColumn) {
        super(tableName, idColumn);
    }

    public static MySqlUserDao getInstance() {
        MySqlUserDao localInstance = instance;
        if (localInstance == null) {
            synchronized (MySqlUserDao.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new MySqlUserDao("users", "user_id");
                }
            }
        }
        return localInstance;
    }

    @Override
    protected List<User> mapResultSet(Connection connection, ResultSet resultSet) throws SQLException {
        List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt(1);
            String login = resultSet.getString(2);
            String password = resultSet.getString(3);
            Role role = Role.getRoleByOrdinalNumber(resultSet.getInt(4));
            Date date = resultSet.getDate(5);
            users.add(new User(id, login, password, role, date));
        }
        return users;
    }

    @Override
    protected void setDefaultStatementArgs(PreparedStatement statement, User entity) throws SQLException {
        statement.setString(1, entity.getLogin());
        statement.setString(2, entity.getPassword());
        statement.setInt(3, entity.getRole().ordinal());
    }

    @Override
    public User create(User entity) {
        try (final Connection connection = BasicConnectionPool.getInstance().getConnection();
             final PreparedStatement statement = connection.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            setDefaultStatementArgs(statement, entity);
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long timeMillis = System.currentTimeMillis();
            statement.setDate(4, new java.sql.Date(timeMillis));
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                entity.setId(id);
                entity.setRegistrationDate(new Date(timeMillis));
                return entity;
            } else {
                LOGGER.error("No autoincremented index after trying to add record into table user");
                return null;
            }
        } catch (SQLException | DAOException e) {
            LOGGER.error("DB connection error", e);
            return null;
        }
    }

    @Override
    public User update(User entity) {
        try (PreparedStatement statement = BasicConnectionPool.getInstance().getConnection().prepareStatement(SQL_UPDATE_USER)) {
            setDefaultStatementArgs(statement, entity);
            statement.setInt(4, entity.getId());
            statement.executeUpdate();
            return entity;
        } catch (SQLException | DAOException e) {
            LOGGER.error("DB connection error", e);
            return entity;
        }
    }

    @Override
    public boolean isLoginPresented(String login) {
        return findUserByLogin(login).isPresent();
    }

    @Override
    public Optional<User> findUserByLogin(String login) {
        return findPreparedEntities(SQL_SELECT_BY_LOGIN, statement -> {
            try {
                statement.setString(1, login);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).stream().findFirst();
    }

    @Override
    public int getNumberOfPostsByAuthor(String login) {
        try (final Connection connection = BasicConnectionPool.getInstance().getConnection();
             final PreparedStatement statement = connection.prepareStatement(SQL_COUNT_OF_POSTS_BY_AUTHOR)
        ) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException | DAOException e) {
            LOGGER.error("DB connection error", e);
            return 0;
        }
    }


    @Override
    public int getNumberOfLikesByAuthor(String login) {
        try (final Connection connection = BasicConnectionPool.getInstance().getConnection();
             final PreparedStatement statement = connection.prepareStatement(SQL_COUNT_OF_LIKES_BY_AUTHOR)
        ) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException | DAOException e) {
            LOGGER.error("DB connection error", e);
            return 0;
        }
    }
}
