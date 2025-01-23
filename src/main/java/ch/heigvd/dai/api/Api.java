package ch.heigvd.dai.api;

import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.api.users.Inventory;
import ch.heigvd.dai.api.users.User;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.Javalin;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Api {
    private final Javalin app;
    private final Sqlite database;
    private final Auth auth;
    private final User user;
    private Inventory inventoryController;

    public Api(Sqlite database) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.app = Javalin.create();
        this.database = database;
        this.auth = new Auth(database);
        this.user = new User(database, auth);
        this.inventoryController = new Inventory(database);
    }

    public void start(int port) {
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

        app.get("/myinvetory/{user_id}", inventoryController::getInventory);

        app.post("/register", ctx -> ctx.result("Création du compte"));
        app.post("/login", ctx -> ctx.result("Connextion"));

        // Partie hdv
        // Pour récuérer les offres d'un user avec /hdv?id=41
        app.get("/hdv", ctx -> ctx.result("renvoi des objets"));
        app.get("/hdv/{item_id}", ctx -> ctx.result("renvoi toutes les offres d'un objet"));

        // Le sell_id est généré au moment du post (vente d'un objet)
        app.delete("/hdv/sell/{sell_id}", ctx -> ctx.result("renvoi toutes les offres d'un objet"));
        app.post("/hdv/sell", ctx -> ctx.result("Pour envoyer une offre"));
        app.patch("/hdv/sell", ctx -> ctx.result("Pour modifier une offre (le prix de l'offre)"));

        app.start(port);
    }
}
