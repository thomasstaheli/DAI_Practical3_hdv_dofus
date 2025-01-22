package ch.heigvd.dai.api.users;

import ch.heigvd.dai.api.Status;
import ch.heigvd.dai.api.auth.Auth;
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
    private record UserEntry(Integer id, String username, Integer kamas) {
        public static UserEntry get(ResultSet resultSet) throws SQLException {
            return new UserEntry(
                    resultSet.getInt("user_id"),
                    resultSet.getString("username"),
                    resultSet.getInt("kamas")
            );
        }
    }

    public User(Sqlite database, Auth auth) {
        this.database = database;
        this.auth = auth;
    }
    public Context getMe(Context ctx) throws SQLException {
        try (
            PreparedStatement pstmt = database.prepare("SELECT * FROM user WHERE user_id = ?", new Object[]{auth.getMe(ctx)});
            ResultSet result = pstmt.executeQuery()
        ) {
            result.next();
            UserEntry userEntry = UserEntry.get(result);
            ctx.status(200).json(userEntry);
        }

        return ctx;
    }

    public Context removeMe(Context ctx) throws SQLException {
        try (
                PreparedStatement pstmt = database.prepare("DELETE FROM user WHERE user_id = ?", new Object[]{auth.getMe(ctx)});
        ) {
            pstmt.execute();
        }

        return ctx.status(200).json(Status.ok());
    }

    public Context partialUpdateMe(Context ctx) throws SQLException, NoSuchAlgorithmException {
        Auth.AuthBody body = Auth.AuthBody.partial(ctx);

        StringBuilder query = new StringBuilder("UPDATE user SET ");
        List<Object> params = new ArrayList<>();
        boolean hasUpdates = false;

        if (body.username() != null) {
            query.append("username = ?, ");
            params.add(body.username());
            hasUpdates = true;
        }
        if (body.password() != null) {
            query.append("password = ?, ");
            params.add(Auth.hash(body.password()));
            hasUpdates = true;
        }

        if (hasUpdates) {
            query.setLength(query.length() - 2);
            query.append(" WHERE user_id = ?");
            params.add(auth.getMe(ctx));

            try (
                    PreparedStatement pstmt = database.prepare(query.toString(), params.toArray());
            ) {
                pstmt.execute();
            }
        }

        return ctx.status(200).json(Status.ok());
    }

    public Context updateMe(Context ctx) throws SQLException, NoSuchAlgorithmException {
        Auth.AuthBody body = Auth.AuthBody.full(ctx);

        try (
                PreparedStatement pstmt = database.prepare("UPDATE user SET username = ?, password = ? WHERE user_id = ?", new Object[]{body.username(), Auth.hash(body.password()), auth.getMe(ctx)});
        ) {
            pstmt.execute();
        }

        return ctx.status(200).json(Status.ok());
    }

    public Context getAll(Context ctx) throws SQLException {
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM user", new Object[]{});
                ResultSet result = pstmt.executeQuery()
        ) {
            List<UserEntry> users = new ArrayList<>();
            while (result.next()) {
                users.add(UserEntry.get(result));
            }
            ctx.status(200).json(users);
        }

        return ctx;
    }

    public Context getOne(Context ctx) throws SQLException {
        int id = Integer.parseInt(ctx.pathParam("id"));
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM user WHERE user_id = ?", new Object[]{id});
                ResultSet result = pstmt.executeQuery()
        ) {
            result.next();
            UserEntry userEntry = UserEntry.get(result);
            ctx.status(200).json(userEntry);
        }

        return ctx;
    }
}
