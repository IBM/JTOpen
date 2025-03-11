# JDBC Driver Usage

## Downloading the driver

The jt400.jar driver is included in the [JTOpen package on Maven Central](https://mvnrepository.com/artifact/net.sf.jt400/jt400). 

The driver must be placed in the directory that is specified by the application.

## JDBC URL

JDBC URL:  jdbc:as400://hostname/default-schema

replace hostname and default-schema with the database details from the IBMi server.

e.g. `jdbc:as400://pub400.com/qgpl`

## Usage 

Javadoc can be found at [https://javadoc.io/doc/net.sf.jt400/jt400/latest/index.html](https://javadoc.io/doc/net.sf.jt400/jt400/latest/index.html)

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class AS400JDBCExample {
    public static void main(String[] args) {
        String url = "jdbc:as400://pub400.com";
        
        Properties props = new Properties();
        props.put("user", "user");
        props.put("password", "password");
        props.put("prompt", "true"); // login form if needed
        // add more properties here

        try (Connection conn = DriverManager.getConnection(url, props)) {
            System.out.println("Connected to IBM i database successfully!");

            String sql = "SELECT SERVICE_NAME, EXAMPLE FROM QSYS2.SERVICES_INFO where SERVICE_CATEGORY = 'PRODUCT'";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    System.out.println("Service: " + rs.getString("SERVICE_NAME"));
                    System.out.println("Example SQL: ");
                    System.out.println(rs.getString("EXAMPLE"));
                    System.out.println();
                    System.out.println();
                }
            }

            System.out.println("Connection closed.");
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }
    }
}

```

## IBM Toolbox for Java JDBC properties

Many properties can be specified when connecting to DB2 for IBM i using JDBC. All properties are optional and can be specified either as part of the URL or in a java.util.Properties object. If a property is set in both the URL and a Properties object, the value in the URL will be used.

Driver properties can be found at [https://javadoc.io/doc/net.sf.jt400/jt400/latest/com/ibm/as400/access/AS400JDBCDriver.html](https://javadoc.io/doc/net.sf.jt400/jt400/latest/com/ibm/as400/access/AS400JDBCDriver.html)
