package ch.heigvd.dai;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Sqlite implements AutoCloseable {

  private Connection conn;
  private Statement stmt;

  public Sqlite() throws SQLException  {
    try {
      conn = DriverManager.getConnection("jdbc:sqlite:dofus_hdv.db");
      stmt = conn.createStatement();
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
  }

  @Override
  public void close() throws SQLException {
    if (conn != null) conn.close();
    if (stmt != null) stmt.close();
  }

  public void init() throws SQLException, IOException {
    executeSql("./scripts/init.sql");
  }

  public void seed() throws SQLException, IOException {
      executeSql("./scripts/seed.sql");
  }


  public void executeSql(String sqlFilePath) throws IOException, SQLException {
    BufferedReader br = new BufferedReader(new FileReader(sqlFilePath));

    StringBuilder query = new StringBuilder();
    String line;

    while((line = br.readLine()) != null) {
      if (line.trim().startsWith("--")) continue;

      query.append(line).append(" ");

      if(line.trim().endsWith(";")) {
        stmt.execute(query.toString().trim());
        query = new StringBuilder();
      }
    }
  }
}
