package ch.heigvd.dai.api;

import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.api.hdv.Hdv;
import ch.heigvd.dai.api.users.User;
import ch.heigvd.dai.caching.Cacher;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.Javalin;
import io.javalin.http.InternalServerErrorResponse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Api {
    private final Javalin app;
    private final Auth auth;
    private final User user;
    private final Hdv hdv;

    public Api(Sqlite database) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.app = Javalin.create(config -> {
            config.validation.register(LocalDateTime.class, LocalDateTime::parse);
        });
        Cacher cacher = new Cacher();
        this.auth = new Auth(database, cacher);
        this.user = new User(database, auth, cacher);
        this.hdv = new Hdv(database, auth);
    }

    public void start(int port) {
        app.exception(SQLException.class, (e, ctx) -> {
            System.out.println(e.getMessage());
            throw new InternalServerErrorResponse();
        });

        app.before(auth::protect);

        app.get("/ping", ctx -> ctx.json(Status.ok()));

        // Authentication
        app.post("/register", auth::register);
        app.post("/login", auth::login);
        app.get("/disconnect", auth::disconnect);

        // Users
        app.get("/users", user::getAll);
        app.get("/users/me", user::getMe);
        app.put("/users/me", user::partialUpdateMe);
        app.patch("/users/me", user::updateMe);
        app.delete("/users/me", user::removeMe);
        app.get("/users/{id}", user::getOne);

        // Hdv
        app.get("/hdv", hdv::getAll);
        app.get("/hdv/me", hdv::getMe);
        app.get("/hdv/{id}", hdv::buy);
        app.delete("/hdv/{id}", hdv::remove);
        app.post("/hdv", hdv::create);
        app.patch("/hdv/{id}", hdv::update);
        app.put("/hdv/{id}", hdv::partialUpdate);

        app.start(port);
    }
}
