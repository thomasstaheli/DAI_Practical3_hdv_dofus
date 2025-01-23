package ch.heigvd.dai.api.users;

import ch.heigvd.dai.api.Status;
import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.caching.Cacher;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.http.Context;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class User {
    private final Sqlite database;
    private final Auth auth;
    private final Cacher cacher;

    private record UserEntry(Integer id, String username, Integer kamas) {
        public static UserEntry get(ResultSet resultSet) throws SQLException {
            return new UserEntry(
                    resultSet.getInt("user_id"),
                    resultSet.getString("username"),
                    resultSet.getInt("kamas")
            );
        }
    }

    public User(Sqlite database, Auth auth, Cacher cacher) {
        this.database = database;
        this.auth = auth;
        this.cacher = cacher;
    }
    public void getMe(Context ctx) throws SQLException {
      cacher.checkCache(String.valueOf(auth.getMe(ctx)), ctx);
      try (
            PreparedStatement pstmt = database.prepare("SELECT * FROM user WHERE user_id = ?", new Object[]{auth.getMe(ctx)});
            ResultSet result = pstmt.executeQuery()
        ) {
            result.next();
            UserEntry userEntry = UserEntry.get(result);
            cacher.setCacheHeader(String.valueOf(auth.getMe(ctx)), ctx);
            ctx.status(200).json(userEntry);
        }
    }

    public void removeMe(Context ctx) throws SQLException {
        try (
                PreparedStatement pstmt = database.prepare("DELETE FROM user WHERE user_id = ?", new Object[]{auth.getMe(ctx)})
        ) {
            pstmt.execute();
            cacher.removeCache(String.valueOf(auth.getMe(ctx)));
            cacher.setLastModified("ALL");
            ctx.status(200).json(Status.ok());
        }
    }

    public void partialUpdateMe(Context ctx) throws SQLException, NoSuchAlgorithmException {
        Auth.AuthBody body = Auth.AuthBody.partial(ctx);

        StringBuilder query = new StringBuilder("UPDATE user SET ");
        List<Object> params = new ArrayList<>();

        if (body.username() != null) {
            query.append("username = ?, ");
            params.add(body.username());
        }
        if (body.password() != null) {
            query.append("password = ?, ");
            params.add(Auth.hash(body.password()));
        }

        if (!params.isEmpty()) {
            query.setLength(query.length() - 2);
            query.append(" WHERE user_id = ?");
            params.add(auth.getMe(ctx));

            try (
                    PreparedStatement pstmt = database.prepare(query.toString(), params.toArray())
            ) {
                pstmt.execute();
                cacher.setLastModified(String.valueOf(auth.getMe(ctx)));
                cacher.setLastModified("ALL");
            }
        }

        ctx.status(200).json(Status.ok());
    }

    public void updateMe(Context ctx) throws SQLException, NoSuchAlgorithmException {
        Auth.AuthBody body = Auth.AuthBody.full(ctx);

        try (
                PreparedStatement pstmt = database.prepare("UPDATE user SET username = ?, password = ? WHERE user_id = ?", new Object[]{body.username(), Auth.hash(body.password()), auth.getMe(ctx)})
        ) {
          pstmt.execute();
          cacher.setLastModified(String.valueOf(auth.getMe(ctx)));
          cacher.setLastModified("ALL");
          ctx.status(200).json(Status.ok());
        }
    }

    public void getAll(Context ctx) throws SQLException {
      cacher.checkCache("ALL", ctx);
      try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM user", new Object[]{});
                ResultSet result = pstmt.executeQuery()
        ) {
            List<UserEntry> users = new ArrayList<>();
            while (result.next()) {
                users.add(UserEntry.get(result));
            }
            cacher.setCacheHeader("ALL", ctx);
            ctx.status(200).json(users);
        }
    }

    public void getOne(Context ctx) throws SQLException {
        int id = Integer.parseInt(ctx.pathParam("id"));
        cacher.checkCache(String.valueOf(id), ctx);
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM user WHERE user_id = ?", new Object[]{id});
                ResultSet result = pstmt.executeQuery()
        ) {
            result.next();
            UserEntry userEntry = UserEntry.get(result);
            cacher.setCacheHeader(String.valueOf(id), ctx);
            ctx.status(200).json(userEntry);
        }
    }
}
