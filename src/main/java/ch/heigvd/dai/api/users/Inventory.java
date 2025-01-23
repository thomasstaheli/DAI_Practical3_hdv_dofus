package ch.heigvd.dai.api.users;

import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.http.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Inventory {

  Sqlite database;
  Auth auth;

  public Inventory(Sqlite database) {
    this.database = database;
    //this.auth = new Auth(database);
  }

  public void getInventory(Context ctx) {

    int id = 0;

    try (PreparedStatement preparedStatement = database.prepare(
      "SELECT item.nom, inventory_user.quantity " +
            "FROM inventory_user " +
            "INNER JOIN item ON item.item_id = inventory_user.item_id " +
            "WHERE inventory_user.user_id = ?",
            new Object[]{id});
        ResultSet res = preparedStatement.executeQuery()) {
      res.next();
      System.out.println(res.getInt("user_id"));
      ctx.result(res.getInt("user_id") + "");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
