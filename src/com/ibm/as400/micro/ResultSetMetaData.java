///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ResultSetMetaData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package java.sql;


/**
 *  An object that can be used to get information about the types and properties of the columns in a ResultSet
 *  object. The following code fragment creates the ResultSet object rs, creates the ResultSetMetaData object
 *  rsmd, and uses rsmd to find out how many columns rs has.
 *
 *  <pre>
 *      ResultSet rs = stmt.executeQuery("SELECT a, b, c FROM TABLE2");
 *      ResultSetMetaData rsmd = rs.getMetaData();
 *      int numberOfColumns = rsmd.getColumnCount();
 *      
 *  </pre>
 *
 *  <b>Note:</b>This class contains the smallest useful set of methods and data from java.sql.ResultSetMetaData
 */
public interface ResultSetMetaData
{
    /**
     *  Returns the number of columns in this ResultSet object.
     *
     *  @return the number of columns.
     *
     *  @exception SQLException  if a database access error occurs.
     **/
    int getColumnCount() throws SQLException;

    /**
     *  Retrieves the designated column's SQL type.
     *
     *  @param column the first column is 1, the second is 2, ...
     *
     *  @return SQL type from java.sql.Types.
     *
     *  @exception SQLException  if a database access error occurs.
     **/
    int getColumnType(int column) throws SQLException;
}
