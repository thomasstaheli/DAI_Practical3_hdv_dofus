package ch.heigvd.dai;

import ch.heigvd.dai.api.Api;
import ch.heigvd.dai.database.Sqlite;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public class Main {
  public static final int PORT = 8080;

  public static void main(String[] args) {
    try {
      // TODO: close connection
      Sqlite database = new Sqlite();
      for (String arg : args) {
        switch (arg) {
          case "--init":
            database.init();
            break;
          case "--seed":
            database.seed();
            break;
        }
      }

      Api api = new Api(database);
      api.start(PORT);
    } catch (SQLException | IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
      System.err.println(e.getMessage());
    }
  }
}