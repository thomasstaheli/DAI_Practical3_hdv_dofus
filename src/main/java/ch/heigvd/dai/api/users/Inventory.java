package ch.heigvd.dai.api.users;

import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

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

  public record InventoryBody(Integer item_id, Integer quantity) {
    public static InventoryBody full(Context ctx) {
      return ctx.bodyValidator(InventoryBody.class).check((o) -> o.item_id != null &&
              o.quantity != null && o.quantity > 0 && o.item_id > 0, "Invalid body").get();
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

      boolean hasResult = false;

      List<InventoryUser> inventoryFromUser = new ArrayList<>();
      while (result.next()) {
        inventoryFromUser.add(InventoryUser.get(result));
        if (!hasResult) hasResult = true;
      }

      if (!hasResult) {
        throw new NotFoundResponse("No item found in inventory");
      }

      ctx.status(200).json(inventoryFromUser);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return ctx;
  }

  public void insertItem(Context ctx) throws SQLException {
    InventoryBody body = InventoryBody.full(ctx);

    try (PreparedStatement preparedStatement = database.prepare(
      "INSERT INTO " +
              "inventory_user (user_id, item_id, quantity) " +
              "VALUES (?, ?, ?)",
            new Object[]{auth.getMe(ctx), body.item_id, body.quantity});
        PreparedStatement checkIfExist = database.prepare(
            "SELECT 1 " +
            "FROM inventory_user " +
            "WHERE user_id = ? AND item_id = ?",
            new Object[]{auth.getMe(ctx), body.item_id})) {

      // Test if the item already exist in the inventory
      ResultSet result = checkIfExist.executeQuery();
      // Return false if the item already exist in the inventory
      if(result.next()) {
        throw new ConflictResponse("Item already exist in inventory");
      }

      preparedStatement.executeUpdate();

      ctx.status(201).result("SUCCES : Item added to inventory");
    }

  }

  public void deleteItem(Context ctx) throws SQLException {
    int item_id = Integer.parseInt(ctx.pathParam("item_id"));

    try (PreparedStatement preparedStatement = database.prepare(
      "DELETE " +
            "FROM inventory_user " +
            "WHERE user_id = ? AND item_id = ?",
            new Object[]{auth.getMe(ctx), item_id})) {

      int rowsAffected = preparedStatement.executeUpdate();
      if (rowsAffected == 0) {
        throw new NotFoundResponse("Item does not exist in inventory");
      }

      ctx.status(201).result("SUCCESS: Item added to inventory");
    }
  }

  public void updateItem(Context ctx) throws SQLException {
    InventoryBody body = InventoryBody.full(ctx);

    try (PreparedStatement preparedStatement = database.prepare(
      "UPDATE inventory_user " +
              "SET quantity = ? " +
              "WHERE user_id = ? AND item_id = ?",
            new Object[]{body.quantity, auth.getMe(ctx), body.item_id})) {

      int rowsAffected = preparedStatement.executeUpdate();
      if (rowsAffected == 0) {
        throw new NotFoundResponse("Item does not exist in inventory");
      }
      ctx.status(201).result("SUCCESS: Item added to inventory");
    }
  }

}
