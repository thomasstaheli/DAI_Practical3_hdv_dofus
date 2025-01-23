package ch.heigvd.dai;

import ch.heigvd.dai.api.Api;
import ch.heigvd.dai.database.Sqlite;

public class Main {
  public static final int PORT = 8080;

  public static void main(String[] args) {
    try {
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
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}