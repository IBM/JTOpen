package com.ibm.jtopenlite.samples;
import java.sql.*;
import java.io.*;


/**
 * Sample class to run sql statements
 */
public class RunSql {
   public final static String PROMPT="ENTER SQL STATEMENT or exit > ";
   public static void main(String[] args) {
	   try {
		   Class.forName("com.ibm.jtopenlite.database.jdbc.JDBCDriver");
		   String url = args[0];
		   String userid = args[1];
		   String password = args[2];
		   Connection connection = DriverManager.getConnection(url, userid, password);
		   Statement statement = connection.createStatement();
		   BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		   System.out.print(PROMPT);
		   String line = reader.readLine();
		   if (line != null) line = line.trim();
		   while ((line != null) && ! line.equalsIgnoreCase("exit") ) {
			   try {
				   boolean results = statement.execute(line);
				   if (results) {
					   ResultSet rs = statement.getResultSet();
					   ResultSetMetaData rsmd = rs.getMetaData();
					   int columnCount = rsmd.getColumnCount();
					   StringBuffer sb = new StringBuffer();
					   while (rs.next()) {
						  sb.setLength(0);
						  sb.append(rs.getString(1));
						  for (int column = 2; column <= columnCount; column++) {
							  sb.append(",");
							  sb.append(rs.getString(column));
						  }
						  System.out.println(sb.toString());
					   }
				   }
			   } catch (SQLException sqlex) {
				   System.out.println("SQLException caught");
				   System.out.println(sqlex.toString());
			   }
			   System.out.print(PROMPT);
			   line = reader.readLine(); 
			   if (line != null) line = line.trim();
		   }

	   } catch (Exception e) {
		   System.out.println("Fatal error occurred");
	     e.printStackTrace(System.out);
	     System.out.println("Usage: java com.ibm.jtopenlite.samples.RunSql JDBCURL USERID PASSWORD");

	   }
   }

}
