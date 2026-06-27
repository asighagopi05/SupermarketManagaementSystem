package smms;
import java.sql.*;

public class DBConnection {

    public static Connection getConnection() {
        Connection con = null;

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            con = DriverManager.getConnection(
            	"jdbc:oracle:thin:@//localhost:1521/orcl1",
                "system",
                "pass#123"
            );

        } catch (Exception e) {
            System.out.println(e);
        }

        return con;
    }
}