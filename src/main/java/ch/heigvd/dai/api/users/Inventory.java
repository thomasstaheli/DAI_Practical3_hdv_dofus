package ch.heigvd.dai.api.users;

import io.javalin.http.Context;

public class Inventory {

  public static void getInventory(Context ctx) {

    int id = 0;



    String result = "My inventory";



    // Renvoie le réssultat à la page
    ctx.result(result);
  }

}
