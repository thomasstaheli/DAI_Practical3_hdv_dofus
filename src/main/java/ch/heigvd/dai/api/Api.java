package ch.heigvd.dai.api;

import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.api.users.Inventory;
import ch.heigvd.dai.api.hdv.Hdv;
import ch.heigvd.dai.api.users.User;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.Javalin;
import io.javalin.http.InternalServerErrorResponse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class Api {
    private final Javalin app;
    private final Auth auth;
    private final User user;
    private Inventory inventoryController;
    private final Hdv hdv;

    public Api(Sqlite database) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.app = Javalin.create();
        this.auth = new Auth(database);
        this.user = new User(database, auth);
        this.inventoryController = new Inventory(database, auth);
        this.hdv = new Hdv(database, auth);
    }

    public void start(int port) {
        app.exception(SQLException.class, (e, ctx) -> {
            System.out.println(e.getMessage());
            throw new InternalServerErrorResponse();
        });

        app.before(auth::protect);

        app.get("/ping", ctx -> ctx.result("pong"));

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

        // Partie inventaire
        app.get("/myinventory", inventoryController::getInventory);
        app.post("/myinventory", inventoryController::insertItem);
        app.delete("/myinventory/{item_id}", inventoryController::deleteItem);
        app.patch("/myinventory/{item_id}", inventoryController::updateItem);
        app.put("/myinventory/{item_id}", inventoryController::updateItem);

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
