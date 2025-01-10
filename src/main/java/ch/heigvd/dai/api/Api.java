package ch.heigvd.dai.api;

import ch.heigvd.dai.api.users.Inventory;
import ch.heigvd.dai.api.users.User;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.Javalin;

public class Api {
    private final Javalin app;
    private final Sqlite database;

    public Api(Sqlite database) {
        this.app = Javalin.create();
        this.database = database;
    }

    public void start(int port) {
        app.get("/ping", ctx -> ctx.result("pong"));
        // Partie user
        app.get("/user", ctx -> ctx.result("list of user"));

        app.get("/user/me", User::getMe);

        // A enlever peut être (get un user (nom, prenom, ...))
        app.get("/user/{user_id}", ctx -> ctx.result("list of user items"));
        app.patch("/user/me", ctx -> ctx.result("Update one or more param of my user"));
        app.delete("/user/me", ctx -> ctx.result("To delete my account"));

        app.get("/myinvetory/{user_id}", Inventory::getInventory);

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
