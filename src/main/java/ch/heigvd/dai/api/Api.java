package ch.heigvd.dai.api;

import ch.heigvd.dai.api.auth.Auth;
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

    public Api(Sqlite database) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.app = Javalin.create();
        this.database = database;
        this.auth = new Auth(database);
        this.user = new User(database, auth);
    }

    public void start(int port) {
        app.before(auth::protect);

        app.get("/ping", ctx -> ctx.result("pong"));
        // Partie user
        app.get("/user", ctx -> ctx.result("list of user"));

        app.get("/users/me", user::getMe);

        // A enlever peut être (get un user (nom, prenom, ...))
        app.get("/users/{user_id}", ctx -> ctx.result("list of user items"));
        app.patch("/users/me", ctx -> ctx.result("Update one or more param of my user"));
        app.delete("/users/me", ctx -> ctx.result("To delete my account"));

        app.post("/register", auth::register);
        app.post("/login", auth::login);
        app.get("/disconnect", auth::disconnect);

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
