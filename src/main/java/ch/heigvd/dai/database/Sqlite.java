package ch.heigvd.dai.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class Sqlite implements AutoCloseable {

  private Connection conn;
  private Statement stmt;

  public Sqlite() throws SQLException  {
    try {
      // TODO only if --init
      File file = new File("./dofus_hdv.db");
      if (file.exists()) file.delete();

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


  private void executeSql(String sqlFilePath) throws IOException, SQLException {
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

  private PreparedStatement prepareStatement(String query, Object[] args) throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement(query);
    for (int index = 0; index < args.length; index++) {
      if (args[index].getClass() == Integer.class) {
        pstmt.setInt(index + 1, (int) args[index]);
      } else if (args[index].getClass() == String.class) {
        pstmt.setString(index + 1, (String) args[index]);
      }
    }

    return pstmt;
  }

  public ResultSet select(String query, Object[] args) throws SQLException {
    try (PreparedStatement pstmt = prepareStatement(query, args)) {
      return pstmt.executeQuery();
    }
  }

  public boolean insert(String query, Object[] args) throws SQLException {
    try (PreparedStatement pstmt = prepareStatement(query, args)) {
      return pstmt.execute();
    }
  }
}
