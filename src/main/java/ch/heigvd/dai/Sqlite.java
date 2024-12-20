package ch.heigvd.dai;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Sqlite {

  private Connection conn;
  private Statement stmt;

  public void init() throws SQLException {
    // create a database connection
    conn = DriverManager.getConnection("jdbc:sqlite:dofus_hdv.db");
    stmt = conn.createStatement();

    // Load script
    try {
      executeSql("./hdv_schema.sql");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void executeSql(String sqlFilePath) throws IOException, SQLException {
    // path to our SQL Script file
    BufferedReader br = new BufferedReader(new FileReader(sqlFilePath));

    // String Builder to build the query line by line.
    StringBuilder query = new StringBuilder();
    String line;

    while((line = br.readLine()) != null) {

      if(line.trim().startsWith("-- ")) {
        continue;
      }

      // Append the line into the query string and add a space after that
      query.append(line).append(" ");

      if(line.trim().endsWith(";")) {
        // Execute the Query
        stmt.execute(query.toString().trim());
        // Empty the Query string to add new query from the file
        query = new StringBuilder();
      }
    }
  }

  public ResultSet sql_prompt(String command) throws SQLException {
    stmt.executeUpdate(command);
    return stmt.getResultSet();
  }



  // Example from https://github.com/xerial/sqlite-jdbc
  public static void main(String[] args)
  {
    // NOTE: Connection and Statement are AutoCloseable.
    //       Don't forget to close them both in order to avoid leaks.
    try
            (
                    // create a database connection
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");
                    Statement statement = connection.createStatement();
            )
    {
      statement.setQueryTimeout(30);  // set timeout to 30 sec.

      statement.executeUpdate("drop table if exists person");
      statement.executeUpdate("create table person (id integer, name string)");
      statement.executeUpdate("insert into person values(1, 'leo')");
      statement.executeUpdate("insert into person values(2, 'yui')");
      ResultSet rs = statement.executeQuery("select * from person");
      while(rs.next())
      {
        // read the result set
        System.out.println("name = " + rs.getString("name"));
        System.out.println("id = " + rs.getInt("id"));
      }
    }
    catch(SQLException e)
    {
      // if the error message is "out of memory",
      // it probably means no database file is found
      e.printStackTrace(System.err);
    }
  }
}
