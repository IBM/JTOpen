import java.util.*;
import java.sql.*;

/**
 * This class connects to a target iSeries database
 * and primes the target system with the database
 * schema, tables, stored procedures required by
 * the JdbcMidpBid demo application.  This is  a
 * one time setup for each you plan on running to.
 * <p>
 * This class loads the AS/400 Native JDBC driver
 * or the IBM Toolbox for Java driver to perform the prime.
 * <p>
 * Set the CLASSPATH to point to the Naitve or Toolbox
 * driver then run the java program like the following:
 * <p>
 * java PrimeRealEstateDB jdbc:as400://myAS400  myUid  myPwd
 *
 **/
public class PrimeRealEstateDB
{
    java.sql.Connection        conn;
    public PrimeRealEstateDB(Connection conn)
    {
        this.conn = conn;
    }
    public void run() throws SQLException {
        Statement      stmt = conn.createStatement();
        try
        {
            System.out.println("Try: Create Collection QJdbcMe");
            stmt.execute("Create collection QJdbcMe");
        }
        catch (Exception e)
        {
            try
            {
                System.out.println("Try: Create Schema QJdbcMe");
                stmt.execute("Create Schema QJdbcMe");
            }
            catch (Exception ex)
            {
            }
        }
        try
        {
            System.out.println("Try: Drop Table QJdbcMe.RECalendar");
            stmt.execute("Drop Table QJdbcMe.RECalendar");
        }
        catch (Exception e)
        {
        }
        // Not in a try block. This must work
        System.out.println("Create Table QJdbcMe.RECalendar");
        stmt.execute("Create Table QJdbcMe.RECalendar "
                     +"("
                     +"Mls          CHAR(6) NOT NULL WITH DEFAULT, "
                     +"ApptDate     CHAR(10) NOT NULL WITH DEFAULT, "
                     +"ApptTime     CHAR(5) NOT NULL WITH DEFAULT,"
                     +"Unique ("
                     +"   Mls, ApptDate, ApptTime"
                     +"   )"
                     +")");
        try
        {
            System.out.println("Try: Drop Procedure QJdbcMe.RESchedule");
            stmt.execute("Drop Procedure QJdbcMe.RESchedule");
        }
        catch (Exception e)
        {
        }
        try
        {
            System.out.println("Create Procedure QJdbcMe.RESchedule");
            stmt.execute("Create Procedure"
                         +" QJdbcMe.RESchedule"
                         +" ( IN  MlsP        CHAR (6), in ApptDateP CHAR(10)"
                         +"  ,IN  ApptTimeP   CHAR (5) )"
                         +" RESULT SETS 1"
                         +" LANGUAGE SQL NOT DETERMINISTIC"
                         +" BEGIN"
                         +"   Declare c1 cursor for select ApptDate || ' ' || ApptTime"
                         +"           from QJdbcMe.RECalendar "
                         +"           where MLS = MlsP and ApptDate = ApptDateP and"
                         +"           ApptTime = ApptTimeP;"
                         +"   Insert into QJdbcMe.RECalendar values(MlsP, ApptDateP, ApptTimeP);"
                         +"   Open c1;"
                         +"   Set Result Sets Cursor c1;"
                         +" end");
        }
        catch (Exception e)
        {
            System.err.println("================================================\n"+
                               "WARNING WARNING SQL Stored procedure didn't work\n");
            System.err.println("JdbcRealEstate 'schedule showing' won't work\n"+
                               "================================================");
        }
        try
        {
            System.out.println("Try: Drop Table QJdbcMe.RealEstate");
            stmt.execute("Drop Table QJdbcMe.RealEstate");
        }
        catch (Exception e)
        {
        }
        // Not in a try block. This must work
        System.out.println("Create Table QJdbcMe.RealEstate");
        stmt.execute("Create Table QJdbcMe.RealEstate "
                     +"("
                     +"Mls          CHAR(6) NOT NULL WITH DEFAULT, "
                     +"Address      VARCHAR(25), "
                     +"City         VARCHAR(25), "
                     +"State        CHAR(2),"
                     +"Zip          CHAR(5),"

                     +"YearBuilt    CHAR(4),"
                     +"Style        VARCHAR(15),"
                     +"SqFt         INT, "
                     +"Bedrooms     INT, "
                     +"Baths        INT, "

                     +"Acres        FLOAT, "
                     +"GarageStalls INT, "
                     +"Price        DECIMAL ( 11, 2) , "
                     +"CurrentBid   DECIMAL ( 11, 2) , "

                     +"Primary Key (MLS)"
                     +")");

        PreparedStatement pStmt = conn.prepareStatement
                                  ("Insert Into QJdbcMe.RealEstate values("
                                   +"?, ?, ?, ?, ?, "
                                   +"?, ?, ?, ?, ?, "
                                   +"?, ?, ?, ?"
                                   +")");

        // Not in a try block. This must work
        PrimeRow       primeRows = new PrimeRow(14);
        int            countRows = primeRows.numRowCapable();
        System.out.println("Insert rows into QJdbcMe.RealEstate...");
        for (int i=0; i<countRows; ++i)
        {
            primeRows.setRow(pStmt, i);
            pStmt.execute();
        }

    }
    public static void main(String args[])
    {
        if (args.length != 1 && args.length != 3)
        {
            System.out.println("Usage: java PrimeRealEstateDB <url> [<user> <password>]");
            System.exit(1);
        }
        try
        {
            Class.forName("com.ibm.db2.jdbc.app.DB2Driver");
        }
        catch (Exception e)
        {
            System.out.println("Didn't find class com.ibm.db2.jdbc.app.DB2Driver");
        }
        try
        {
            Class.forName("COM.ibm.db2.jdbc.app.DB2Driver");
        }
        catch (Exception e)
        {
            System.out.println("Didn't find class COM.ibm.db2.jdbc.app.DB2Driver");
        }
        try
        {
            Class.forName("com.ibm.as400.access.AS400JDBCDriver");
        }
        catch (Exception e)
        {
            System.out.println("Didn't find class com.ibm.as400.access.AS400JDBCDriver");
        }

        try
        {
            Connection conn;
            if (args.length == 1)
            {
                conn = DriverManager.getConnection(args[0]);
            }
            else
            {
                conn = DriverManager.getConnection(args[0], args[1], args[2]);
            }
            conn.setAutoCommit(false);

            try
            {
                PrimeRealEstateDB     p = new PrimeRealEstateDB(conn);
                p.run();
                conn.commit();
            }
            catch (Exception e)
            {
                System.out.println("Error, will rollback... Error:" + e);
                e.printStackTrace();
                try
                {
                    conn.rollback();
                }
                catch (Exception ex)
                {
                    System.out.println("Rollback Failed: " + ex);
                }
            }
            finally
            {
                try
                {
                    conn.close();
                }
                catch (Exception e)
                {
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error, will rollback... Error:" + e);
            e.printStackTrace();
        }
        System.exit(0);
    }
}

/**
 * A little abstraction that contains all of the
 * rows of data that our RealEstate demo will use.
 */
class PrimeRow
{
    public final static int ExpectedColumns = 14;
    public static final String rows[] = {
        // 1       2                     3              4      5         6         7              8           9        10       11       12       13               14
        // mls     address               city           state  zip       year      style          sqft        bedrooms baths    acre     garage   Price            Current Bid
        "JDBCA0", "9321 22th St SW",     "Rochester",   "MN",  "55902",  "1970",   "Hide-out",    "3000",     "5",     "3",     "0.25",  "3",     "109000.99",     "0",
        "JDBCA1", "3605 Hwy 52 N",       "Rochester",   "MN",  "55906",  "1948",   "Commercial",  "100000",   "6000",  "250",   "40.5",  "0",     "999999999.99",  "0",
        "JDBCA2", "11 Buy IBM Stock",    "Rochester",   "MN",  "55901",  "2000",   "Mansion",     "3500",     "6",     "4",     "3",     "4",     "199000.00",     "222000.00",
        "JDBCA3", "12 Use IBM Software", "Rochester",   "MN",  "55901",  "2000",   "Mansion",     "3500",     "6",     "4",     "3",     "4",     "189000.00",     "201000.00",
        "JDBCA4", "450 OS/400 Way",      "Rochester",   "MN",  "55901",  "2000",   "2 Story"  ,   "3000",     "7",     "5",     "3",     "9",     "201000.00",     "210000.00",
        "JDBCA5", "1 Microsoft Way",     "Redmond"  ,   "WA",  "20911",  "1980",   "Commercial",  "40000",    "2500",  "100",   "5.0",   "0",     "10099.79",      "0",
        "JDBCA6", "2 Microsoft Way",     "Redmond"  ,   "WA",  "20911",  "2001",   "Commercial",  "30000",    "2400",  "90",    "3.0",   "0",     "10099.69",      "0",
        "JDBCA7", "101 IBM Chip Rd",     "Rochester",   "MN",  "55906",  "1980",   "Split"    ,   "1700",     "2",     "2",     "0.25",  "1",     "114000.00",     "0",
        "JDBCA8", "115 IBM Disk Drive",  "Rochester",   "MN",  "55906",  "1981",   "Rambler"  ,   "2200",     "3",     "2",     "0.25",  "2",     "129000.00",     "0",
        "JDBCA9", "987 Tech. Ct NE",     "Rochester",   "MN",  "55906",  "1989",   "Multi-lvl",   "2400",     "4",     "2",     "0.25",  "2",     "169000.00",     "0",
        "JDBCB1", "10 Elm Street",       "Somewhere",   "MN",  "55911",  "1965",   "2 story"  ,   "2100",     "3",     "2",     "0.25",  "2",     "103000.00",     "0",
        "JDBCB2", "11 Elm Street",       "Somewhere",   "MN",  "55911",  "1966",   "2 story"  ,   "1900",     "3",     "2",     "0.25",  "2",     "101000.00",     "0",
        "JDBCB3", "12 Elm Street",       "Somewhere",   "MN",  "55911",  "1967",   "2 story"  ,   "2200",     "3",     "2",     "0.25",  "2",     "89000.00",      "92000.00",
        "JDBCB4", "13 Elm Street",       "Somewhere",   "MN",  "55911",  "1968",   "2 story"  ,   "1500",     "2",     "1",     "0.25",  "2",     "29000.00",      "10000.00",
        "JDBCB5", "14 Elm Street",       "Somewhere",   "MN",  "55911",  "1967",   "2 story"  ,   "2400",     "3",     "2",     "0.25",  "2",     "79000.00",      "0",
        "JDBCB6", "15 Elm Street",       "Somewhere",   "MN",  "55911",  "1966",   "2 story"  ,   "2500",     "3",     "2",     "0.25",  "2",     "101000.00",     "0",
        "JDBCB7", "16 Elm Street",       "Somewhere",   "MN",  "55911",  "1965",   "2 story"  ,   "2600",     "3",     "2",     "0.25",  "2",     "110000.00",     "0",
    };
    java.sql.Connection        conn;

    public PrimeRow(int numColumns) throws IllegalArgumentException {
        if (numColumns != ExpectedColumns)
        {
            throw new IllegalArgumentException("Table Change? Expected " + ExpectedColumns +
                                               " columns");

        }
    }
    public int numRowCapable()
    {
        return(rows.length / ExpectedColumns);
    }
    public void setRow(PreparedStatement stmt, int row)
    throws SQLException, IllegalArgumentException  {
        if (row > numRowCapable())
        {
            throw new IllegalArgumentException("Invalid Row: row(" + row +
                                               ") >= maxRow(" + numRowCapable() + ")");
        }
        for (int i=0; i<ExpectedColumns; ++i)
        {
            // Set all these as a string for convenience. The driver
            // will convert to the correct underlying SQL type.
            stmt.setString(i+1, rows[(row*ExpectedColumns)+i]);
        }
        // Show some status.
        System.out.println(row + ": " + rows[(row*ExpectedColumns)+1]);
    }
}



