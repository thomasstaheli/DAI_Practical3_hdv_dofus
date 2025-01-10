package ch.heigvd.dai;

import io.javalin.http.Context;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
  public static final int PORT = 8080;

  public static void main(String[] args) {
    try (Sqlite database = new Sqlite()) {
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
    } catch (SQLException | IOException e) {
      System.err.println(e.getMessage());
    }
  }

  public static void getMe(Context ctx) {
    ctx.result("My user");
  }
}