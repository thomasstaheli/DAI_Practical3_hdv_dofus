package ch.heigvd.dai.api.auth;

import ch.heigvd.dai.database.Sqlite;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Auth {
    private final JsonWebToken jsonWebToken;
    private final Sqlite database;
    private record AuthBody(String username, String password) {}

    public Auth(Sqlite database) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.database = database;
        this.jsonWebToken = new JsonWebToken();
    }

    public void register(Context ctx) throws SQLException {
        try {
            AuthBody body = ctx.bodyValidator(AuthBody.class).check((o) -> o.username != null && o.password != null && !o.password.isEmpty(), "Invalid body").get();
            // TODO: hash password
            try (
                PreparedStatement pstmt = database.prepare("SELECT user_id FROM user WHERE username = ?", new Object[]{body.username()});
                ResultSet result = pstmt.executeQuery()
            ) {
                if (result.next()) throw new ConflictResponse();
            }

            try (PreparedStatement pstmt = database.prepare("INSERT INTO user(username, password) VALUES (?, ?)", new Object[]{body.username(), body.password()})) {
                pstmt.execute();
                ctx.status(201).json("ok");
            }
        } catch (Exception e) {
            this.disconnect(ctx);
            throw e;
        }
    }

    public void login(Context ctx) throws SQLException {
        try {
            AuthBody body = ctx.bodyValidator(AuthBody.class).check((o) -> o.username != null && o.password != null && !o.password.isEmpty(), "Invalid body").get();
            // TODO: hash password
            try (
                PreparedStatement pstmt = database.prepare("SELECT user_id FROM user WHERE username = ? AND password = ?", new Object[]{body.username(), body.password()});
                ResultSet result = pstmt.executeQuery()
            ) {
                if (!result.next()) throw new UnauthorizedResponse();
                int id = result.getInt("user_id");
                ctx.cookie("token", jsonWebToken.generate(id));
                ctx.status(200).json("ok");
            }
        } catch (Exception e) {
            this.disconnect(ctx);
            throw e;
        }
    }

    public void disconnect(Context ctx) {
        ctx.removeCookie("token");

        ctx.status(200).json("ok");
    }

    public void protect(Context ctx) {
        String path = ctx.path();
        if (path.equals("/login") || path.equals("/register")) {
            return;
        }

        String token = ctx.cookie("token");
        if (token == null || !this.jsonWebToken.isValid(token)) {
            this.disconnect(ctx);
            throw new UnauthorizedResponse();
        }
    }

    public int getMe(Context ctx) throws JWTVerificationException {
        return jsonWebToken.getSubject(ctx.cookie("token"));
    }
}
