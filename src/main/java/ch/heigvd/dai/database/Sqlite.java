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
    try (BufferedReader br = new BufferedReader(new FileReader(sqlFilePath))) {
      StringBuilder query = new StringBuilder();
      String line;
      boolean insideBlock = false; // Pour suivre les blocs BEGIN ... END;

      while ((line = br.readLine()) != null) {
        // Supprime les commentaires et les espaces inutiles
        line = line.replaceAll("/\\*.*?\\*/", "").trim(); // Supprime les commentaires en bloc
        if (line.startsWith("--") || line.isEmpty()) continue; // Ignore les commentaires en ligne

        // Vérifie le début d'un bloc (comme un trigger)
        if (line.toUpperCase().startsWith("BEGIN")) {
          insideBlock = true;
        }

        // Ajoute la ligne au buffer
        query.append(line).append(" ");

        // Vérifie la fin du bloc (END;)
        if (insideBlock && line.trim().endsWith("END;")) {
          insideBlock = false; // Fin du bloc
        }

        // Si ce n'est pas un bloc, exécute la requête à chaque point-virgule
        if (!insideBlock && line.trim().endsWith(";")) {
          executeQuery(query.toString().trim());
          query = new StringBuilder(); // Réinitialise le buffer
        }
      }
    }
  }

  private void executeQuery(String sql) {
    try {
      stmt.execute(sql); // Exécute la requête
    } catch (SQLException e) {
      System.err.println("Erreur SQL : " + e.getMessage());
      System.err.println("Requête fautive : " + sql);
    }
  }

  public PreparedStatement prepare(String query, Object[] args) throws SQLException {
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

}
