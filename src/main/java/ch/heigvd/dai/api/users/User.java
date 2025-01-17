package ch.heigvd.dai.api.users;

import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.http.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private final Sqlite database;
    private final Auth auth;

    public User(Sqlite database, Auth auth) {
        this.database = database;
        this.auth = auth;
    }
    public Context getMe(Context ctx) throws SQLException {
        try (
            PreparedStatement pstmt = database.prepare("SELECT username FROM user WHERE user_id = ?", new Object[]{auth.getMe(ctx)});
            ResultSet result = pstmt.executeQuery()
        ) {
            result.next();
            String username = result.getString("username");
            ctx.status(200).json(username);
        }

        return ctx;
    }
}
