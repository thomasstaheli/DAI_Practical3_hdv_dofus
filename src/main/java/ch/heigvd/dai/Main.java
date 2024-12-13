package ch.heigvd.dai;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class Main {
  public static final int PORT = 8080;

  public static void main(String[] args) {
    Javalin app = Javalin.create();

    app.get("/ping", ctx -> ctx.result("pong"));
    // Partie user
    app.get("/user", ctx -> ctx.result("list of user"));

    app.get("/user/me", Main::getMe);

    // A enlever peut être (get un user (nom, prenom, ...))
    app.get("/user/{user_id}", ctx -> ctx.result("list of user items"));
    app.patch("/user/me", ctx -> ctx.result("Update one or more param of my user"));
    app.delete("/user/me", ctx -> ctx.result("To delete my account"));

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

    app.start(PORT);
  }

  public static void getMe(Context ctx) {
    ctx.result("My user");
  }
}