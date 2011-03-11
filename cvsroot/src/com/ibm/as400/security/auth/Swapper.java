////////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileTokenCredential.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2009-2009 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.security.auth;

import com.ibm.as400.access.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 Provides utility methods to perform credential swaps for existing remote connections.
 The methods in this class allow you to do work under
 a different user, providing you've obtained a {@link ProfileTokenCredential ProfileTokenCredential}
 probably from {@link com.ibm.as400.access.AS400#getProfileToken(String,String) AS400.getProfileToken()}.
 <p>
 <i>Comparison with the swap() methods of ProfileTokenCredential:</i>

 <p>
 The <code>swap()</code> methods of this class have as one of their arguments either an AS400 object or a Connection object.  The contract of these methods is to swap the profile in use <em>for a specified connection</em>.  Here is the usage pattern:
 <pre>
 AS400 conn1 = new AS400(sysName, userID, password);
 ProfileTokenCredential myCred = new ProfileTokenCredential(....);
 Swapper.swap(conn1, myCred);
 // conn1 is now running under the new (swapped-to) profile
 </pre>

 <p>
 In constrast, the contract of the <code>swap()</code> methods of class {@link ProfileTokenCredential ProfileTokenCredential} is to swap the profile in use for the <em>current thread of execution</em>.  They don't swap the profile in use for a specific AS400 object, but rather for any <em>subsequently-created</em> AS400 objects.  Here is the usage pattern:
 <pre>
 AS400 conn1 = new AS400();
 ProfileTokenCredential myCred = new ProfileTokenCredential(....);
 myCred.swap();
 AS400 conn2 = new AS400();  // conn2 is running under the swapped-to profile.
 // conn1 is still running under the original profile.
 </pre>

 <p>
 The <code>Swapper.swap()</code> methods are useful for swapping credentials for existing remote connections.  The <code>ProfileTokenCredential.swap()</code> methods are useful for swapping the current thread of execution when running natively in an IBM i JVM.
 <p>
 This class is mostly based on a prototype contributed by Steve Johnson-Evers.
 **/

public class Swapper
{
  // Prevent instantiation of this class.
  private Swapper() {}


  /**
   Swaps the profile on the specified system.
   This method calls system API QSYSETP ("Set To Profile Token").
   <p>
   Note: This method is intended for use with remote connections only,
   and only swaps the profile used by
   {@link com.ibm.as400.access.CommandCall CommandCall},
   {@link com.ibm.as400.access.ProgramCall ProgramCall}, and
   {@link com.ibm.as400.access.ServiceProgramCall ServiceProgramCall}.
   If your Java application is running "natively", that is, on-thread on the
   IBM i JVM, and you wish to swap the current thread to a different profile,
   use one of the <code>swap()</code> methods of
   {@link ProfileTokenCredential ProfileTokenCredential} instead of this method.

   @param system The remote IBM i system.
   @param newCredential The credential to use for the swap.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  IOException  If an error occurs while communicating with the system.
   @see ProfileTokenCredential#swap()
   @see ProfileTokenCredential#swap(boolean)
   **/
  public static void swap(AS400 system, ProfileTokenCredential newCredential)
    throws AS400SecurityException, IOException
  {
    if (system == null)        throw new NullPointerException("system");
    if (newCredential == null) throw new NullPointerException("newCredential");

    // If running natively, suggest the use of ProfileTokenCredential.swap() instead.
    if (system.canUseNativeOptimizations())
    {
      Trace.log(Trace.WARNING, "When running natively, swaps should be performed via ProfileTokenCredential.swap() instead of Swapper.swap().");
    }

    swapToToken(system, newCredential.getToken());
    newCredential.fireSwapped();
  }


  /**
   Swaps the profile on the specified JDBC connection.
   This method calls system API QSYSETP ("Set To Profile Token").

   @param connection A JDBC connection to the IBM i system.
   Must be an instance of
   {@link com.ibm.as400.access.AS400JDBCConnection} or {@link com.ibm.as400.access.AS400JDBCConnectionHandle}.
   @param newCredential The credential to use for the swap.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  SQLException  If the connection is not open, or an error occurs.
   **/
  public static void swap(Connection connection, ProfileTokenCredential newCredential)
    throws AS400SecurityException, IOException, SQLException
  {
    if (connection == null)    throw new NullPointerException("connection");
    if (newCredential == null) throw new NullPointerException("newCredential");

    swapToToken(connection, newCredential.getToken());
    newCredential.fireSwapped();
  }


  /**
   Swaps the profile, using the specified profile token.
   This method calls system API QSYSETP ("Set To Profile Token").
   <p>
   Note: This method is intended for use with remote connections only,
   and only swaps the profile used by
   {@link com.ibm.as400.access.CommandCall CommandCall},
   {@link com.ibm.as400.access.ProgramCall ProgramCall}, and
   {@link com.ibm.as400.access.ServiceProgramCall ServiceProgramCall}.
   If your Java application is running "natively", that is, on-thread on the
   IBM i JVM, and you wish to swap the current thread to a different profile,
   use one of the <code>swap()</code> methods of
   {@link ProfileTokenCredential ProfileTokenCredential} instead of this method.

   @param system The IBM i system.
   @param token The bytes from {@link ProfileTokenCredential#getToken()}
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  IOException  If an error occurs while communicating with the system.
   @see #swap(AS400, ProfileTokenCredential)
   **/
  public static void swapToToken(AS400 system, byte[] token)
    throws AS400SecurityException, IOException
  {
    if (system == null) throw new NullPointerException("system");
    if (token == null)  throw new NullPointerException("token");

    // API takes 2 parameters: A char(32) profile token and error code
    ProgramParameter[] parmList = new ProgramParameter[2];

    // Input: Profile token (32A)
    parmList[0] = new ProgramParameter(token);

    // Input/Output: Error code
    parmList[1] = new ErrorCodeParameter();

    // Call the program
    ProgramCall pgm = new ProgramCall(system, "/QSYS.LIB/QSYSETPT.PGM", parmList);
    pgm.suggestThreadsafe(); // Run on-thread if possible; allows app to use disabled profile.

    try
    {
      if (!pgm.run()) {
        throw new SwapFailedException(pgm.getMessageList());
      }

      ///TBD experiment
      AS400Message[] msgs = pgm.getMessageList();
      if (msgs != null && msgs.length != 0)
      {
        System.out.println("Messages returned from QSYSETPT:"); ///
        for (int i=0; i<msgs.length; i++)
        {
          System.out.println(msgs[i].toString());
        }
      }
    }
    catch (AS400SecurityException e) { throw e; }
    catch (RuntimeException e) { throw e; }
    catch (Exception e) { throw new RuntimeException(e.getMessage(), e); }
  }


  /**
   Swaps the profile, using the specified profile token.
   This method uses SQL's <code>call</code> statement to pass the token to QSYSETPT.

   @param connection A JDBC connection to the IBM i system.
   @param token The bytes from {@link ProfileTokenCredential#getToken()}
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  SQLException  If the connection is not open, or an error occurs.
   @see #swap(Connection, ProfileTokenCredential)
   **/
  public static void swapToToken(Connection connection, byte[] token)
    throws AS400SecurityException, IOException, SQLException
  {
    if (connection == null) throw new NullPointerException("connection");
    if (token == null)      throw new NullPointerException("token");

    // Note: Since we _always_ submit our JDBC requests via the Database Server, we don't really have a "Toolbox native" mode when using a JDBC connection.  So the swap should behave the same, whether the Java app is running remotely or natively on IBM i.
    StringBuffer sql = new StringBuffer(80);
    sql.append("CALL QSYS");
    sql.append(connection.getMetaData().getCatalogSeparator());
    sql.append("QSYSETPT (X'");

    for (int i=0; i<token.length; i++) {
      int unsignedByte = token[ i ];
      if (unsignedByte < 0) {
        unsignedByte = 256 + unsignedByte;
      } else if (unsignedByte < 16) {
        sql.append('0');
      }
      sql.append(Integer.toHexString(unsignedByte).toUpperCase());
    }

    sql.append("', X'0000')");

    Statement stmt = null;
    try {
      stmt = connection.createStatement();
      stmt.execute(sql.toString());
      ///TBD: Check stmt for returned messages, warnings, etc
      SQLWarning warning = stmt.getWarnings();
      if (warning != null) System.out.println("SQLWarning: " + warning.getErrorCode() + ": " + warning.getSQLState()); ///
    }
    finally {
      if (stmt != null) {
        try { stmt.close(); }
        catch (Exception e) {
          if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Error while closing statement", e);
        }
      }
    }
  }

}
