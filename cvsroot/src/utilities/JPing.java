///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  JPing.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package utilities;

import com.ibm.as400.access.AS400JPing;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.CommandLineArguments;

import java.io.PrintWriter;
import java.util.Vector;
import java.util.Hashtable;
import java.util.ResourceBundle;



/**
 *  Determines if services are running on the IBM i system.
 *  <p>
 *  
 *  JPing can be run as a command line program, as follows:
 *  <BLOCKQUOTE><PRE>
 *  <strong>java utilities.JPing</strong>  <i>systemName</i>   [ options ]
 *  </PRE></BLOCKQUOTE> 
 *  <b>Options:</b>
 *  <p> 
 *  <dl>
 *
 *  <dt><b><code>-help </b></code>
 *  <dd>Displays the help text.
 *  The -help option may be abbreviated to -h.
 *
 *  <dt><b><code>-service </b></code><i>serviceName</i>
 *  <dd>Specifies the specific service to ping.
 *  The -service option may be abbreviated to -s.  The valid
 *  services include:  <i>as-file, as-netprt, as-rmtcmd, as-dtaq,
 *  as-database, as-ddm, as-central,</i> and <i>as-signon</i>.  If this
 *  option is not specified, by default, all of the services
 *  will be pinged.
 *  
 *  <dt><b><code>-ssl </b></code>
 *  <dd>Specifies whether or not to ping the SSL port(s).
 *  The default setting will not ping the SSL port(s).
 *
 *  <dt><b><code>-timeout </b></code>
 *  <dd>Specifies the timeout period in milliseconds.
 *  The -timeout option may be abbreviated to -t.  The default 
 *  setting is 20000 (20 sec).  
 *
 *  <dt><b><code>-verbose </b></code>
 *  <dd>Specifies verbose output.
 *  The -verbose option may be abbreviated to -v.  The default 
 *  setting is non-verbose.  
 *  
 *  </dl>
 *  </PRE></BLOCKQUOTE>
 *  <p>
 *  <br>
 *  Here is an example of calling JPing from the command line:
 *  <br>
 *  <BLOCKQUOTE><PRE>
 *  java utilities.JPing myServer -service as-signon -ssl -timeout 10000
 *  </PRE></BLOCKQUOTE>
 *  The JPing output will look something like the following:
 *  <BLOCKQUOTE><PRE>
 *  Verifying connections to system myServer...
 *  
 *  Successfully connected to server application: as-signon-s
 *  Connection Verified
 *  </PRE></BLOCKQUOTE>
 *
 *  To determine from within a program, if the services are running, use
 *  {@link com.ibm.as400.access.AS400JPing AS400JPing}.
 *
 **/
public class JPing
{
   // These are the temp variables for the option values passed in
   // via the command line.  To eliminate the need of using static variables
   // throughout the class, I needed to create some temporary static
   // variables for use in main, where they will then set the private
   // non-static variables for use throughout the entire class.
   private static String  sys_;
   private static int     srv_ = AS400JPing.ALL_SERVICES;
   private static boolean ssl_ = false;
   private static boolean verbose_ = false;
   private static long    time_ = 20000;

   // Where MRI comes from.
   private static ResourceBundle resource_ = ResourceBundle.getBundle("utilities.UTMRI");


   
   /**
    *   Run JPing.
    **/
   public static void main(String args[])
   {
      PrintWriter writer = new PrintWriter(System.out, true);    //The PrintWriter used when running via the command line.
       
      try 
      {
         // Determine which command line argument were used.
         if (!parseParms(args, writer)) {
           usage(writer);
           return;
         }

         if (verbose_) {
           Trace.setTraceDiagnosticOn(true);
           Trace.setTraceOn(true);
         }
         AS400JPing obj = new AS400JPing(sys_, srv_, ssl_);
         obj.setTimeout(time_);
         obj.setPrintWriter(System.out);
         
         writer.println();
         writer.print(resource_.getString("JPING_VERIFYING"));
         writer.print(sys_);
         writer.println("...");
         writer.println();
         
         boolean rtn;
         if (srv_ == AS400JPing.ALL_SERVICES) rtn = obj.pingAllServices();
         else                                 rtn = obj.ping();

         if (rtn)
            writer.println(resource_.getString("JPING_VERIFIED"));
         else
            writer.println(resource_.getString("JPING_NOTVERIFIED"));
      }
      catch(Throwable e)
      {
         e.printStackTrace(writer);
         if (Trace.isTraceOn())
            Trace.log(Trace.ERROR, e);
      }
      finally
      {
         System.exit(0); // this is necessary in case a signon dialog popped up
      }
   }


   /**
    *  Parse out the command line arguments for the JPing command.
    **/
   private static boolean parseParms(String args[], PrintWriter writer) throws Exception
   {
      if (args.length == 0) return false;

      Vector options = new Vector();
      options.addElement("-service");
      options.addElement("-ssl");
      options.addElement("-timeout");
      options.addElement("-verbose");

      Hashtable shortcuts = new Hashtable();
      shortcuts.put("-h", "-help");
      shortcuts.put("-?", "-help");
      shortcuts.put("-s", "-service");
      shortcuts.put("-t", "-timeout");
      shortcuts.put("-v", "-verbose");
      
      CommandLineArguments arguments = new CommandLineArguments(args, options, shortcuts);

      // If this flag is specified by the user, just display the help text.
      if (arguments.isOptionSpecified("-help")) {
        return false;
      }

      // Get the system that the user wants to ping.
      sys_ = arguments.getOptionValue("");
      if (sys_ == null || sys_.length() == 0 || sys_.indexOf(' ') != -1) {
        return false;
      }

      // Get the specific IBM i service the user wants to ping.
      String s = arguments.getOptionValue("-service");
      if (s != null && s.length() != 0)
      {
        srv_ = toServiceNumber(s);
        if (srv_ == UNRECOGNIZED) {
          writer.println ("Service value not recognized: " + s);
          return false;
        }
      }
         
      // The user wants to use SSL if they specify this flag.
      ssl_ = (arguments.isOptionSpecified("-ssl"));

      // Get the JPing timeout value.
      String t = arguments.getOptionValue("-timeout");
      if (t != null)
         time_ = (new Integer(t)).intValue();
         
      // The user wants verbose output if they specify this flag.
      verbose_ = (arguments.isOptionSpecified("-verbose"));

      return true;
   }


   private static final int UNRECOGNIZED = -1;
   private static int toServiceNumber(String serviceName)
   {
     int serviceNumber;
     String s = serviceName.toLowerCase();

     if      (s.indexOf("file") != -1 ||     // as-file
              s.indexOf("ifs") != -1)
       serviceNumber = AS400.FILE;
     else if (s.indexOf("prt") != -1 ||      // as-netprt
              s.indexOf("print") != -1)
       serviceNumber = AS400.PRINT;
     else if (s.indexOf("cmd") != -1 ||      // as-rmtcmd
              s.indexOf("command") != -1)
       serviceNumber = AS400.COMMAND;
     else if (s.indexOf("dtaq") != -1 ||     // as-dtaq
              s.indexOf("dq") != -1)
       serviceNumber = AS400.DATAQUEUE;
     else if (s.indexOf("database") != -1 || // as-database
              s.indexOf("db") != -1)
       serviceNumber = AS400.DATABASE;
     else if (s.indexOf("ddm") != -1 ||      // as-ddm
              s.indexOf("rla") != -1)
       serviceNumber = AS400.RECORDACCESS;
     else if (s.indexOf("central") != -1)    // as-central
       serviceNumber = AS400.CENTRAL;
     else if (s.indexOf("sign") != -1)       // as-signon
       serviceNumber = AS400.SIGNON;
     else
       serviceNumber = UNRECOGNIZED;

     return serviceNumber;
   }


   /**
    *  Print out the usage for the JPing command.
    **/
   static void usage(PrintWriter writer) 
   {
      writer.println ();
      writer.println (resource_.getString("JPING_USAGE"));
      writer.println (resource_.getString("JPING_HELP"));
      writer.println (resource_.getString("JPING_SERVICE") +
                          resource_.getString("JPING_SERVICE2") +
                          resource_.getString("JPING_SERVICE3"));
      writer.println (resource_.getString("JPING_SSL"));
      writer.println (resource_.getString("JPING_TIMEOUT"));
      writer.println (resource_.getString("JPING_VERBOSE"));
   }
}
