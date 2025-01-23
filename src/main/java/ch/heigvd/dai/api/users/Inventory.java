package ch.heigvd.dai.api.users;

import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.http.Context;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Inventory {

  Sqlite database;
  Auth auth;

  private record InventoryUser(Integer id, String nom, Integer quantity) {
    public static InventoryUser get(ResultSet resultSet) throws SQLException {
      return new InventoryUser (
              resultSet.getInt("item_id"),
              resultSet.getString("nom"),
              resultSet.getInt("quantity")
      );
    }
  }

  public Inventory(Sqlite database) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    this.database = database;
    this.auth = new Auth(database);
  }

  public Context getInventory(Context ctx) {

    try (PreparedStatement preparedStatement = database.prepare(
      "SELECT inventory_user.item_id, item.nom, inventory_user.quantity " +
            "FROM inventory_user " +
            "INNER JOIN item ON inventory_user.item_id = item.item_id " +
            "WHERE inventory_user.user_id = ?",
            new Object[]{auth.getMe(ctx)});
         ResultSet result = preparedStatement.executeQuery()) {

      List<InventoryUser> inventoryFromUser = new ArrayList<>();
      while (result.next()) {
        inventoryFromUser.add(InventoryUser.get(result));
      }
      ctx.status(200).json(inventoryFromUser);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return ctx;
  }

  public void insertItem(Context ctx) {

  }
}
