///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ServerTrace.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
  The ServerTrace class enables server job tracing for the
  server jobs that handle Toolbox requests.
  The following jobs can be traced:
  <br>
  <ol>
  <li>JDBC<br>
      The ServerTrace class can be used to trace JDBC server jobs.
      Various types of traces can be started.  These types include:
      <UL>
         <li>Database monitor -- same as CL command STRDBMON <br>
         <li>Debug the server job -- same as CL command STRDBG <br>
         <li>Save the server job's joblog -- same as CL command CHGJOB to change logging options<br>
         <li>Start trace on the server job -- same as CL command TRCJOB<br>
         <li>Save SQL information -- PRTSQLINF <br>
      </UL>
      Tracing must be started before a connection is made to
      the server since the client enables server tracing only at connect time.
      Trace data is collected in spooled files on the server.  The
      various types of tracing can be turn on in any combination by adding the
      constants that represent each type of trace.
      For example, to start the
      database monitor and save the job log:
      <pre>
         ServerTrace.setJDBCServerTraceCategories(ServerTrace.JDBC_SAVE_SERVER_JOBLOG + ServerTrace.JDBC_START_DATABASE_MONITOR);
         // ...

         Connection c = DriverManager.getConnection(url);
      </pre>
      Server trace can also be started via the com.ibm.as400.access.ServerTrace.JDBC system property.
      For example,
      <pre>
         java -Dcom.ibm.as400.access.ServerTrace.JDBC=10 myClass
      </pre>
   </oL>
**/


class ServerTrace
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private static int     JDBCServerTraceCategories_ = 0;


    /**
      Start tracing the JDBC client.  This is the same as setting
      property "trace=true";  Note the constant is not public.
      It is defined only to be compatible with ODBC
      The numeric value of this constant is 1.
     **/
    static final int JDBC_TRACE_CLIENT = 1;

    /**
      Start the database monitor on the JDBC server job.
      The numeric value of this constant is 2.
     **/
    static final int JDBC_START_DATABASE_MONITOR = 2;

    /**
      Start debug on the JDBC server job.
      The numeric value of this constant is 4.
     **/
    static final int JDBC_DEBUG_SERVER_JOB = 4;

    /**
      Save the joblog when the JDBC server job ends.
      The numeric value of this constant is 8.
     **/
    static final int JDBC_SAVE_SERVER_JOBLOG = 8;

    /**
      Start job trace on the JDBC server job.
      The numeric value of this constant is 16.
     **/
    static final int JDBC_TRACE_SERVER_JOB = 16;

    /**
      Save SQL information.
      The numeric value of this constant is 32.  This
      option is valid when connecting to OS/400 V5R1 and
      newer versions of OS/400 and i5/OS.
     **/
    static final int JDBC_SAVE_SQL_INFORMATION = 32;


    /**
      Start the database host server trace.
      The numeric value of this constant is 64.
      This option is valid when connecting to V5R3 and
      newer versions of i5/OS.
    **/
    static final int JDBC_TRACE_DATABASE_HOST_SERVER = 64;      //@540

    static
    {
       loadTraceProperties ();
    }



    // This is only here to prevent the user from constructing a ServerTrace object.
    private ServerTrace()
    {
    }


    /**
     *  Gets the value of JDBC server tracing.  Zero means tracing is
     *  disabled.
     *  @return JDBCServerTrace value.
     **/
    static final int getJDBCServerTraceCategories()
    {
       return JDBCServerTraceCategories_;
    }



    static void loadTraceProperties ()
    {
        // The following section is new
        // Load and apply the JDBC Server Trace value.
        String value = SystemProperties.getProperty (SystemProperties.TRACE_JDBC_SERVER);
        if (value != null)
        {
            try
            {
               JDBCServerTraceCategories_ = (new Integer(value)).intValue();
            }
            catch (Exception e)
            {
                if (JDTrace.isTraceOn())   
                   JDTrace.logInformation( new String("ServerTrace"), "Value " + value + " for JDBCServerTrace is not valid.");
            }
        }

    }


    /**
     *  Sets JDBC server tracing.
     *  Tracing must be started before a connection is made.
     *  @param categories Valid values are:
     *   <UL>
     *   <LI> JDBC_START_DATABASE_MONITOR - start the database monitor
     *   <LI> JDBC_DEBUG_SERVER_JOB - start debug on the server job
     *   <LI> JDBC_SAVE_SERVER_JOBLOG - save the joblog when the server job ends
     *   <LI> JDBC_TRACE_SERVER_JOB - start trace on the server job
     *   <LI> JDBC_SAVE_SQL_INFORMATION - save SQL information
     *   <LI> JDBC_TRACE_DATABASE_HOST_SERVER - start database host server trace
     *   </UL>
     *   The constants can be added together to start more than one type
     *   of tracing.
     **/

    static void setJDBCServerTraceCategories(int categories)
    {
       JDBCServerTraceCategories_ = categories;
    }

}

