package by.bsu.d0mpi.UP_PostGallery.dao.impl;

import by.bsu.d0mpi.UP_PostGallery.dao.LikeDao;
import by.bsu.d0mpi.UP_PostGallery.exception.DAOException;
import by.bsu.d0mpi.UP_PostGallery.model.Like;
import by.bsu.d0mpi.UP_PostGallery.model.Post;
import by.bsu.d0mpi.UP_PostGallery.pool.BasicConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MySqlLikeDao extends MySqlAbstractDao<Integer, Like> implements LikeDao {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SQL_SELECT_POSTS_ID_LIKED_BY_USER =
            "SELECT likes_post_id FROM likes WHERE likes.likes_user_id = ?";
    private static final String SQL_UPDATE_LIKE =
            "UPDATE likes SET likes_user_id = ?, likes_post_id = ? WHERE like_id = ?";
    private static final String SQL_INSERT_LIKE =
            "INSERT INTO likes (likes_user_id, likes_post_id) VALUES (?, ?)";

    private MySqlLikeDao(String tableName, String idColumn) {
        super(tableName, idColumn);
    }

    private static MySqlLikeDao instance;

    public static MySqlLikeDao getInstance() {
        MySqlLikeDao localInstance = instance;
        if (localInstance == null) {
            synchronized (MySqlLikeDao.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new MySqlLikeDao("likes", "like_id");
                }
            }
        }
        return localInstance;
    }

    public List<Integer> getListOfLikedPostsId(int userId) {
        List<Integer> likesId = new ArrayList<>();
        try (final Connection connection = BasicConnectionPool.getInstance().getConnection();
             final PreparedStatement statement = connection.prepareStatement(SQL_SELECT_POSTS_ID_LIKED_BY_USER)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int postId = resultSet.getInt(1);
                likesId.add(postId);
            }
            return likesId;
        } catch (SQLException | DAOException e) {
            LOGGER.error("Dao connection exception");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    protected List<Like> mapResultSet(Connection connection, ResultSet resultSet) throws SQLException {
        List<Like> likes = new ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt(1);
            int postId = resultSet.getInt(2);
            int userId = resultSet.getInt(3);
            likes.add(new Like(id, postId, userId));
        }
        return likes;
    }

    @Override
    protected void setDefaultStatementArgs(PreparedStatement statement, Like entity) throws SQLException {
        statement.setInt(1, entity.getPostId());
        statement.setInt(2, entity.getAuthorId());
    }

    @Override
    public boolean create(Like entity) {
        try (PreparedStatement statement = BasicConnectionPool.getInstance().getConnection().prepareStatement(SQL_INSERT_LIKE, Statement.RETURN_GENERATED_KEYS)) {
            setDefaultStatementArgs(statement, entity);
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                entity.setId(id);
                return true;
            } else {
                LOGGER.error("No autoincremented index after trying to add record into table user");
                return false;
            }
        } catch (SQLException | DAOException e) {
            LOGGER.error("DB connection error", e);
            return false;
        }
    }

    @Override
    public Like update(Like entity) {
        try (PreparedStatement statement = BasicConnectionPool.getInstance().getConnection().prepareStatement(SQL_UPDATE_LIKE)) {
            setDefaultStatementArgs(statement, entity);
            statement.setInt(3, entity.getId());
            statement.executeUpdate();
            return entity;
        } catch (SQLException | DAOException e) {
            LOGGER.error("DB connection error", e);
            return entity;
        }    }
}
