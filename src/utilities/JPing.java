///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JPing.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
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
 *  The JPing class is used to determine if OS/400 services are running.
 *  <p>
 *  
 *  JPing can be run as a command line program, as follows:
 *  <BLOCKQUOTE></PRE>
 *  <strong>java utilities.JPing</strong>   System   [ options ]
 *  </PRE></BLOCKQUOTE> 
 *  <b>Options:</b>
 *  <p> 
 *  <dl>
 *
 *  <dt><b><code>-help </b></code>
 *  <dd>Displays the help text.
 *  The -help option may be abbreviated to -h or -?.
 *
 *  <dt><b><code>-service </b></code><var>0S/400 Service</var>
 *  <dd>Specifies the specific OS/400 service to ping.
 *  The -service option may be abbreviated to -s.  The valid
 *  services include:  <i>as-file, as-netprt, as-rmtcmd, as-dtaq,
 *  as-database, as-ddm, as-central,</i> and <i>as-signon</i>.  If this
 *  option is not specified, by default, all of the services
 *  will be pinged.
 *  
 *  <dt><b><code>-ssl </b></code>
 *  <dd>Specifies whether or not to ping the ssl port(s).
 *  The default setting will not ping the ssl port(s).
 *
 *  <dt><b><code>-timeout </b></code>
 *  <dd>Specifies the timeout period in milliseconds.
 *  The -timeout option may be abbreviated to -t.  The default 
 *  setting is 20000 (20 sec).  
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
 *  To determine, in a program, if the OS/400 services are running, use
 *  com.ibm.as400.access.AS400JPing.
 *
 *  @see com.ibm.as400.access.AS400JPing
 *
 **/
public class JPing
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   
   // These are the temp variables for the option values passed in
   // via the command line.  To eliminate the need of using static variables
   // throughout the class, I needed to create some temporary static
   // variables for use in main, where they will then set the private
   // non-static variables for use throughout the entire class.
   private static String  sys_;
   private static int     srv_ = AS400JPing.ALL_SERVICES;
   private static boolean ssl_ = false;
   private static long    time_ = 20000;                    //$A1A

   // Where MRI comes from.
   private static ResourceBundle resource_ = ResourceBundle.getBundle("utilities.UTMRI");


   
   /**
    *   Run JPing.
    **/
   public static void main(String args[])
   {
      PrintWriter writer = new PrintWriter(System.out, true);    //The PrintWriter used when running via the command line.
      
      if (args.length == 0) 
      {
         writer.println();
         usage();
      }
       
      try 
      {
         // Determine which command line argument were used.
         parseParms(args);

         AS400JPing obj = new AS400JPing(sys_, srv_, ssl_);
         obj.setTimeout(time_);                              //$A1A
         obj.setPrintWriter(System.out);
         
         writer.println();
         writer.print(resource_.getString("JPING_VERIFYING"));
         writer.print(sys_);
         writer.println("...");
         writer.println();
         
         boolean rtn = obj.ping();

         if (rtn)
            writer.println(resource_.getString("JPING_VERIFIED"));
         else
            writer.println(resource_.getString("JPING_NOTVERIFIED"));
      }
      catch(Exception e)
      {
         e.printStackTrace(writer);
         if (Trace.isTraceOn())
            Trace.log(Trace.ERROR, e);
         System.exit(0);
      }
   }


   /**
    *  Parse out the command line arguments for the JPing command.
    **/
   private static void parseParms(String args[]) throws Exception
   {
      String s,t;

      Vector v = new Vector();
      v.addElement("-service");
      v.addElement("-ssl");
      v.addElement("-timeout");                  //$A1A

      Hashtable shortcuts = new Hashtable();
      shortcuts.put("-help", "-h");
      shortcuts.put("-?", "-h");
      shortcuts.put("-s", "-service");
      shortcuts.put("-t", "-timeout");           //$A1A
      
      CommandLineArguments arguments = new CommandLineArguments(args, v, shortcuts);

      // If this flag is specified by the user, display the help (usage) text.
      if(arguments.getOptionValue("-h") != null)
         usage();

      // Get the AS400 system that the user wants to ping.
      sys_ = arguments.getOptionValue("");

      // Get the specific AS400 service the user wants to ping.
      s = arguments.getOptionValue("-service");
      if (s != null)
      {
         if (s.equals("as-file"))
               srv_ = AS400.FILE;
         else if (s.equals("as-netprt"))
               srv_ = AS400.PRINT;
         else if (s.equals("as-rmtcmd"))
               srv_ = AS400.COMMAND;
         else if (s.equals("as-dtaq"))
               srv_ = AS400.DATAQUEUE;
         else if (s.equals("as-database"))
               srv_ = AS400.DATABASE;
         else if (s.equals("as-ddm"))
               srv_ = AS400.RECORDACCESS;
         else if (s.equals("as-central"))
               srv_ = AS400.CENTRAL;
         else if (s.equals("as-signon"))
               srv_ = AS400.SIGNON;
         else
            throw new ExtendedIllegalArgumentException("service", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
         
      // The user wants to use SSL if they specify this flag.
      if (arguments.getOptionValue("-ssl") != null)
         ssl_ = true;

      // Get the JPing timeout value.
      t = arguments.getOptionValue("-timeout");              //$A1A
      if (t != null)                                         //$A1A
         time_ = (new Integer(t)).intValue();                //$A1A
   }


   /**
    *  Print out the usage for the JPing command.
    **/
   static void usage() 
   {
      System.out.println ();
      System.out.println (resource_.getString("JPING_USAGE"));
      System.out.println (resource_.getString("JPING_HELP"));
      System.out.println (resource_.getString("JPING_SERVICE") +
                          resource_.getString("JPING_SERVICE2") +
                          resource_.getString("JPING_SERVICE3"));
      System.out.println (resource_.getString("JPING_SSL"));
      System.out.println (resource_.getString("JPING_TIMEOUT"));        //$A1A
      System.exit(0);
   }
}
