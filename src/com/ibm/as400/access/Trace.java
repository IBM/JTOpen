///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: Trace.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.StringTokenizer;                                   // $D0A

/**
  The Trace class logs trace points and diagnostic messages.  Each trace point and diagnostic message is logged by category.  The valid categories are:<br>
  <ul>
  <li>DATASTREAM<br>
  This category is used by JT/400 classes to log data flow between the local host and the remote system.  It is not intended for use by classes that use JT/400 classes.
  <li>DIAGNOSTIC<br>
  This category is used to log object state information.
  <li>ERROR<br>
  This category is used to log errors that cause an exception.
  <li>INFORMATION<br>
  This category is used to track the flow of control through the code.
  <li>WARNING<br>
  This category is used to log errors that are recoverable.
  <li>CONVERSION<br>
  This category is used by JT/400 classes to log character set conversions between Unicode and native code pages.  It is not intended for use by classes that use JT/400 classes.
  <li>PROXY<br>
  This category is used by JT/400 classes to log data flow between the client and the proxy server.  It is not intended for use by classes that use JT/400 classes.
  <li>THREAD<br>
  This category is used to enable or disable tracing of thread information. This is useful when debugging multi-threaded applications. Trace information can not be directly logged to this category.
  <li>ALL<br>
  This category is used to enable or disable tracing for all of the above categories at once. Trace information can not be directly logged to this category.
  </ul>

  The caller can enable or disable all tracing or specific trace categories.  Enabling or disabling one category does not affect other categories.

  <pre>
      // tracing is off by default
      ..
      Trace.setTraceErrorOn(true);          // error messages enabled
      Trace.setTraceWarningOn(true);        // warning messages enabled
      Trace.setTraceOn(true);               // error and warning tracing enabled

      ..
      Trace.setTraceOn(false);              // all tracing disabled
      ..
      Trace.setTraceOn(true);               // error and warning tracing enabled
      ..
      Trace.setTraceInformationOn(true);    // trace info. messages enabled
      ..
      Trace.setTraceWarningOn(false);       // warning messages disabled
  </pre>

  The traces are logged to standard out by default.  A file name can be provided to log to a file.  File logging is only possible in an application as most browsers do not allow access to the local file system.

  Two techniques for logging traces are as follows:

  <pre>
      ..
      // Let the log method determine if logging should occur
      Trace.log(Trace.INFORMATION, "I got here...");
      ..
      // Pre-determine if we should log.  This may be more efficient
      // if you are collecting data to log.
      if (Trace.isTraceOn() && Trace.isTraceInformationOn())
      {
            Trace.log(Trace.INFORMATION, "I got here...");
      }
  </pre>

  It is suggested that programs provide some mechanism to enable tracing at run-time, so 
  that the modification and recompilation of code is not necessary.  Two possibilities 
  for that mechanism are a command line argument (for applications) or a menu option 
  (for applications and applets). 
  
  <p>In addition, tracing can be set using the "com.ibm.as400.access.Trace.category" 
  and "com.ibm.as400.access.Trace.file" <a href="SystemProperties.html">system properties</a>.
 **/
public class Trace
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   

    private static boolean traceOn_;
    private static boolean traceInfo_;
    private static boolean traceWarning_;
    private static boolean traceError_;
    private static boolean traceDiagnostic_;
    private static boolean traceDatastream_;
    private static boolean traceConversion_;
    private static boolean traceProxy_;                           // @D0A
    private static boolean traceThread_;                          // @D3A

    private static String fileName_ = null;
    private static PrintWriter destination_ = new PrintWriter(System.out, true);

    /**
      Data stream trace category.  This category is used by JT/400 classes to log data flow between the local host and the remote system.  It is not intended for use by classes that use JT/400 classes.
     **/
    public static final int DATASTREAM = 0;
    /**
      Diagnostic message trace category.  This category is used to log object state information.
     **/
    public static final int DIAGNOSTIC = 1;
    /**
      Error message trace category.  This category is used to log errors that cause an exception.
     **/
    public static final int ERROR = 2;
    /**
      Information message trace category.  This category is used to track the flow of control through the code.
     **/
    public static final int INFORMATION = 3;
    /**
      Warning message trace category.  This category is used to log errors that are recoverable.
     **/
    public static final int WARNING = 4;
    /**
      Character set conversion trace category.  This category is used by JT/400 classes to log conversions between Unicode and native code pages.  It is not intended for use by classes that use JT/400 classes.
     **/
    public static final int CONVERSION = 5;
    /**
      Proxy trace category.  This category is used by JT/400 classes to log data flow between the client and the proxy server.  It is not intended for use by classes that use JT/400 classes.
     **/
    public static final int PROXY = 6;                           // @D0A

    // This is used so we don't have to change our bounds checking every time we add a new trace category.
    private static final int LAST_ONE = 6; // @D3A

    // The following are trace categories which cannot be log()-ed to directly.
    /**
      Thread trace category.  This category is used to enable or disable tracing of thread information. This is useful when debugging multi-threaded applications. Trace information can not be directly logged to this category.
     **/
    public static final int THREAD = 99; // @D3A

    /**
      All trace category. This category is used to enable or disable tracing for all of the other categories at once. Trace information can not be directly logged to this category.
    **/
    public static final int ALL = 100; //@D2A


    // @D0A
    static
    {
        loadTraceProperties ();
    }



    // This is only here to prevent the user from constructing a Trace object.
    private Trace()
    {
    }

    /**
      Returns the trace file name.
      @return  The file name if logging to file.  If logging to System.out, null is returned.
     **/
    public static String getFileName()
    {
	return fileName_;
    }

    /**
      Returns the PrintWriter object.
      @return  The PrintWriter object for the trace data output.
     **/
    public static PrintWriter getPrintWriter()
    {
	return destination_;
    }

    //@D2A
    /**
      Indicates if all of the tracing categories are enabled.
      @return  true if all categories are traced; false otherwise.
     **/
    public static final boolean isTraceAllOn()
    {
	   return traceConversion_ && traceDatastream_ && traceDiagnostic_ &&
             traceError_ && traceInfo_ && traceProxy_ && traceWarning_ && traceThread_; //@D3C
    }

    
    /**
      Indicates if character set conversion tracing is enabled.
      @return  true if conversions are traced; false otherwise.
     **/
    public static final boolean isTraceConversionOn()
    {
	return traceConversion_;
    }

    /**
      Indicates if data stream tracing is enabled.
      @return  true if data streams are traced; false otherwise.
     **/
    public static final boolean isTraceDatastreamOn()
    {
	return traceDatastream_;
    }

    /**
      Indicates if diagnostic tracing is enabled.
      @return  true if diagnostic messages are traced; false otherwise.
     **/
    public static final boolean isTraceDiagnosticOn()
    {
	return traceDiagnostic_;
    }

    /**
      Indicates if error tracing is enabled.
      @return  true if error messages are traced; false otherwise.
     **/
    public static final boolean isTraceErrorOn()
    {
	return traceError_;
    }

    /**
      Indicates if information tracing is enabled.
      @return  true if information messages are traced; false otherwise.
     **/
    public static final boolean isTraceInformationOn()
    {
	return traceInfo_;
    }

    /**
      Indicates if overall tracing is enabled.  If this is false, no tracing occurs.
      @return  true if tracing is enabled; false otherwise.
     **/
    public static final boolean isTraceOn()
    {
	return traceOn_;
    }

    // @D0A
    /**
      Indicates if proxy tracing is enabled.
      @return  true if proxy tracing is enabled; false otherwise.
     **/
    public static final boolean isTraceProxyOn()
    {
	return traceProxy_;
    }

    // @D3A
    /**
      Indicates if thread tracing is enabled.
      @return  true if thread tracing is enabled; false otherwise.
     **/
    public static final boolean isTraceThreadOn()
    {
	return traceThread_;
    }

    /**
      Indicates if warning tracing is enabled.
      @return  true if warning messages are traced; false otherwise.
     **/
    public static final boolean isTraceWarningOn()
    {
	return traceWarning_;
    }



    // @D0A
    static void loadTraceProperties ()
    {
            // Load and apply the trace categories system property.
        String categories = SystemProperties.getProperty (SystemProperties.TRACE_CATEGORY);        
        if (categories != null) {
            setTraceOn (true);
            StringTokenizer tokenizer = new StringTokenizer (categories, ", ;");
            while (tokenizer.hasMoreTokens ()) {
                String category = tokenizer.nextToken ();
                if (category.equalsIgnoreCase ("datastream"))
                    setTraceDatastreamOn (true);
                else if (category.equalsIgnoreCase ("diagnostic"))
                    setTraceDiagnosticOn (true);
                else if (category.equalsIgnoreCase ("error"))
                    setTraceErrorOn (true);
                else if (category.equalsIgnoreCase ("information"))
                    setTraceInformationOn (true);
                else if (category.equalsIgnoreCase ("warning"))
                    setTraceWarningOn (true);
                else if (category.equalsIgnoreCase ("conversion"))
                    setTraceConversionOn (true);
                else if (category.equalsIgnoreCase ("proxy"))
                    setTraceProxyOn (true);
                else if (category.equalsIgnoreCase ("thread")) //@D3A
                    setTraceThreadOn (true); //@D3A
                else if (category.equalsIgnoreCase ("all")) //@D2A
                    setTraceAllOn (true); //@D2A
                else {
                    if (isTraceOn ())
                        Trace.log (Trace.WARNING, "Trace category not valid: " + category + ".");
                }
            }            
        }

        // Load and apply the trace file system property.
        String file = SystemProperties.getProperty (SystemProperties.TRACE_FILE);
        if (file != null) {
            try {
                setFileName (file);
            }
            catch (IOException e) {
                if (isTraceOn ())
                    Trace.log (Trace.WARNING, "Trace file not valid: " + file + ".", e);
            }
        }
    }



    // Log time stamp information in the trace.
    private static void logTimeStamp()
    {
      if (traceThread_)                                        //@D3A
      {                                                        //@D3A
        destination_.print(Thread.currentThread().toString()); //@D3A
        destination_.print("  ");                              //@D3A
      }                                                        //@D3A
      destination_.print((new Date()).toString());
      destination_.print("  ");
    }

    /**
      Logs a message in the specified category.  If the category is disabled, nothing is logged.
      @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR, INFORMATION, WARNING, CONVERSION, PROXY].
      @param  message  The message to log.
     **/
    public static final void log(int category, String message)
    {
        // Validate parameters.
        //@D2 - note: It doesn't make sense to log something to Trace.ALL,
        // so we count it as an illegal argument.
        if (category < DATASTREAM || category > LAST_ONE) // @D0C @D3C
        {
	    throw new ExtendedIllegalArgumentException("category (" + Integer.toString(category) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	}
	if (message == null)
	{
	    throw new NullPointerException("message");
	}

	if (traceOn_ && traceCategory(category))
	{
	    synchronized(destination_)
	    {
		logTimeStamp();
		destination_.println(message);
		if (category == ERROR)
		{
		    new Throwable().printStackTrace(destination_);
		}
	    }
	}
    }

    /**
      Logs a message in the specified category.  If the category is disabled, nothing is logged.
      @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR, INFORMATION, WARNING, CONVERSION, PROXY].
      @param  message  The message to log.
      @param  e  The Throwable object that contains the stack trace to log.
     **/
    public static final void log(int category, String message, Throwable e)
    {
	// Validate parameters.
	//@D2 - note: It doesn't make sense to log something to Trace.ALL,
   // so we count it as an illegal argument.
	if (category < DATASTREAM || category > LAST_ONE)                   // @D0C @D3C
	{
	    throw new ExtendedIllegalArgumentException("category (" + Integer.toString(category) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	}
	if (message == null)
	{
	    throw new NullPointerException("message");
	}
	if (e == null)
	{
	    throw new NullPointerException("e");
	}

	if (traceOn_ && traceCategory(category))
	{
	    synchronized(destination_)
	    {
		logTimeStamp();
		destination_.println(message);
		e.printStackTrace(destination_);
	    }
	}
    }

    /**
      Logs a message in the specified category.  If the category is disabled, nothing is logged.
      @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR, INFORMATION, WARNING, CONVERSION, PROXY].
      @param  e  The Throwable object that contains the stack trace to log.
     **/
    public static final void log(int category, Throwable e)
    {
        log (category, e.getMessage (), e);
    }

    /**
      Logs a message and an integer value in the specified category.  If the category is disabled, nothing is logged.  The integer value is appended to the end of the message, preceded by two blanks.
      @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR, INFORMATION, WARNING, CONVERSION, PROXY].
      @param  message  The message to log.
      @param  value  The integer value to log.
     **/
    public static final void log(int category, String message, int value)
    {
        // Validate parameters.
        //@D2 - note: It doesn't make sense to log something to Trace.ALL,
        // so we count it as an illegal argument.
	if (category < DATASTREAM || category > LAST_ONE)              // @D0C @D3C
	{
	    throw new ExtendedIllegalArgumentException("category (" + Integer.toString(category) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	}
	if (message == null)
	{
	    throw new NullPointerException("message");
	}

	if (traceOn_ && traceCategory(category))
	{
	    synchronized(destination_)
	    {
		logTimeStamp();
		destination_.print(message);
		destination_.print("  ");
		destination_.println(Integer.toString(value));
		if (category == ERROR)
		{
		    new Throwable().printStackTrace(destination_);
		}
	    }
	}
    }

    /**
      Logs a message and a boolean value in the specified category.  If the category is disabled, nothing is logged.  The boolean value is appended to the end of the message, preceded by two blanks.  true is logged for true, and false is logged for false.
      @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR, INFORMATION, WARNING, CONVERSION, PROXY].
      @param  message  The message to log.
      @param  value  The boolean data to log.
     **/
    public static final void log(int category, String message, boolean value)
    {
        // Validate parameters.
	//@D2 - note: It doesn't make sense to log something to Trace.ALL,
   // so we count it as an illegal argument.
	if (category < DATASTREAM || category > LAST_ONE)                  // @D0C @D3C
	{
	    throw new ExtendedIllegalArgumentException("category (" + Integer.toString(category) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	}
	if (message == null)
	{
	    throw new NullPointerException("message");
	}

	if (traceOn_ && traceCategory(category))
	{
	    synchronized(destination_)
	    {
		logTimeStamp();
		destination_.print(message + "  ");
		destination_.println(value);
		if (category == ERROR)
		{
		    new Throwable().printStackTrace(destination_);
		}
	    }
	}
    }

    /**
      Logs a message and byte data in the specified category.  If the category is disabled, nothing is logged.  The byte data is appended to the end of the message, sixteen bytes per line.
      @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR, INFORMATION, WARNING, CONVERSION, PROXY].
      @param  message  The message to log.
      @param  data  The bytes to log.
     **/
    public static final void log(int category, String message, byte[] data)
    {
	log(category, message, data, 0, data.length);
    }

    /**
      Logs a message and byte data in the specified category.  If the category is disabled, nothing is logged.  The byte data is appended to the end of the message, sixteen bytes per line.
      @param category The message category [DATASTREAM, DIAGNOSTIC, ERROR, INFORMATION, WARNING, CONVERSION, PROXY].
      @param  message  The message to log.
      @param  data  The bytes to log.
      @param  offset  The start offset in the data.
      @param  length  The number of bytes of data to log.
     **/
    public static final void log(int category, String message, byte[] data, int offset, int length)
    {
        // Validate parameters.
	//@D2 - note: It doesn't make sense to log something to Trace.ALL,
   // so we count it as an illegal argument.
	if (category < DATASTREAM || category > LAST_ONE)                  // @D0C @D3C
	{
	    throw new ExtendedIllegalArgumentException("category (" + Integer.toString(category) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	}
	if (message == null)
	{
	    throw new NullPointerException("message");
	}
	if (data == null)
	{
	    throw new NullPointerException("data");
	}

	if (traceOn_ && traceCategory(category))
	{
	    synchronized(destination_)
	    {
		logTimeStamp();
		destination_.println(message);
		printByteArray(data, offset, length);
		if (category == ERROR)
		{
		    new Throwable().printStackTrace(destination_);
		}
	    }
	}
    }

    // Logs data from a byte array starting at offset for the length specified.  Output sixteen bytes per line, two hexidecimal digits per byte, one space between bytes.
    private static void printByteArray(byte[] data, int offset, int length)
    {
	for (int i = 0; i < length; i++, offset++)
	{
	    int leftDigitValue = (data[offset] >>> 4) & 0x0F;
	    int rightDigitValue = data[offset] & 0x0F;
	    // 0x30 = '0', 0x41 = 'A'
	    char leftDigit = leftDigitValue < 0x0A ? (char)(0x30 + leftDigitValue) : (char)(leftDigitValue - 0x0A + 0x41);
	    char rightDigit = rightDigitValue < 0x0A ? (char)(0x30 + rightDigitValue) : (char)(rightDigitValue - 0x0A + 0x41);
	    destination_.print(leftDigit);
	    destination_.print(rightDigit);
	    destination_.print(" ");

	    if ((i & 0x0F ) == 0x0F)
	    {
		destination_.println();
	    }
	}
	if (((length - 1) & 0x0F) != 0x0F)
	{
	    // Finish the line of data.
	    destination_.println();
	}
    }

    //@D2A
    /**
      Sets tracing for all categories on or off.  The actual tracing does not happen unless tracing is on.
      @param  traceAll  If true, tracing for each category is on; otherwise, tracing for each category is off.
      @see  setTraceOn
     **/
    public static void setTraceAllOn(boolean traceAll)
    {
      traceConversion_ = traceAll;
      traceDatastream_ = traceAll;
      traceDiagnostic_ = traceAll;
      traceError_      = traceAll;
      traceInfo_       = traceAll;
      traceProxy_      = traceAll;
      traceThread_     = traceAll; //@D3A
      traceWarning_    = traceAll;
    }

    /**
      Sets character set conversion tracing on or off.  The actual tracing does not happen unless tracing is on.
      @param  traceConversion  If true, conversion tracing is on; otherwise, conversion tracing is off.
      @see  setTraceOn
     **/
    public static void setTraceConversionOn(boolean traceConversion)
    {
	traceConversion_ = traceConversion;
    }

    /**
      Sets data stream tracing on or off.  The actual tracing does not happen unless tracing is on.
      @param  traceDatastream  If true, data stream tracing is on; otherwise, data stream tracing is off.
      @see  setTraceOn
     **/
    public static void setTraceDatastreamOn(boolean traceDatastream)
    {
	traceDatastream_ = traceDatastream;
    }

    /**
      Sets diagnostic tracing on or off.  The actual tracing does not happen unless tracing is on.
      @param  traceDiagnostic  If true, diagnostic tracing is on; otherwise, diagnostic tracing is off.
      @see  setTraceOn
     **/
    public static void setTraceDiagnosticOn(boolean traceDiagnostic)
    {
	traceDiagnostic_ = traceDiagnostic;
    }

    /**
      Sets error tracing on or off.  The actual tracing does not happen  unless tracing is on.
      @param  traceError  If true, error tracing is on; otherwise, error tracing is off.
      @see  setTraceOn
     **/
    public static void setTraceErrorOn(boolean traceError)
    {
	traceError_ = traceError;
    }

    /**
      Sets the trace file name.  If the file exists, output is appended to it.  If the file does not exist, it is created.
      @param  fileName  The log file name.  If this is null, output goes to System.out.
      @exception  IOException  If an error occurs while accessing the file.
     **/
    public static synchronized void setFileName(String fileName) throws IOException
    {
        // Flush the current destination stream.
	destination_.flush();

	if (fileName != null)
	{
            // Create a FileOutputStream and PrintWriter to handle the trace data.  If the file exists we want to append to it.
	    File file = new File(fileName);
	    FileOutputStream os = new FileOutputStream(fileName, file.exists());
	    destination_ = new PrintWriter(os, true);
	    fileName_ = fileName;
	}
	else
	{
	    if (fileName_ != null)
	    {
		destination_.close();
	    }
	    fileName_ = null;
	    destination_ = new PrintWriter(System.out, true);
	}
    }

    /**
      Sets the PrintWriter object.  All further trace output is sent to it.
      @param  obj  The PrintWriter object.  If this is null, output goes to System.out.
      @exception  IOException  If an error occurs while accessing the file.
     **/
    public static synchronized void setPrintWriter(PrintWriter obj) throws IOException
    {
	// Flush the current destination stream.
	destination_.flush();

	if (fileName_ != null)
	{
	    destination_.close();
	    fileName_ = null;
	}

	if (obj != null)
	{
	    destination_ = obj;
	}
	else
	{
	    destination_ = new PrintWriter(System.out, true);
	}
    }

    /**
      Sets information tracing on or off.  The actual tracing does not happen unless tracing is on.
      @param  traceInformation  If true, information tracing is on; otherwise, information tracing is off.
      @see  setTraceOn
     **/
    public static void setTraceInformationOn(boolean traceInformation)
    {
	traceInfo_ = traceInformation;
    }

    /**
      Sets tracing on or off.  When this is off nothing is logged in any category, even those that are on.  When this is on, tracing occurs for all categories that are also on.
      @param  traceOn  If true, tracing is on; otherwise, all tracing is disabled.
     **/
    public static void setTraceOn(boolean traceOn)
    {  traceOn_ = traceOn;
       if (traceOn_)                                    //$D1A
          System.out.println(Copyright.version);        //$D1C
    }

    // @D0A
    /**
      Sets proxy stream tracing on or off.  The actual tracing does not happen unless tracing is on.
      @param  traceProxy  If true, proxy tracing is on; otherwise, proxy tracing is off.
      @see  setTraceOn
     **/
    public static void setTraceProxyOn(boolean traceProxy)
    {
	traceProxy_ = traceProxy;
    }

    // @D3A
    /**
      Sets thread tracing on or off.  The actual tracing does not happen unless tracing is on.
      @param  traceError  If true, thread tracing is on; otherwise, thread tracing is off.
      @see  setTraceOn
     **/
    public static void setTraceThreadOn(boolean traceThread)
    {
	traceThread_ = traceThread;
    }

    /**
      Sets warning tracing on or off.  The actual tracing does not happen unless tracing is enabled.
      @param  traceWarning  If true, warning tracing is enabled; otherwise, warning tracing is disabled.
      @see  setTraceOn
     **/
    public static void setTraceWarningOn(boolean traceWarning)
    {
	traceWarning_ = traceWarning;
    }


    
    // Indicates if this category is being traced or not.
    private static boolean traceCategory(int category)
    {
	boolean trace = false;
	switch (category)
	{
	    case INFORMATION:
		trace = traceInfo_;
		break;
	    case WARNING:
		trace = traceWarning_;
		break;
	    case ERROR:
		trace = traceError_;
		break;
	    case DIAGNOSTIC:
		trace = traceDiagnostic_;
		break;
	    case DATASTREAM:
		trace = traceDatastream_;
		break;
	    case CONVERSION:
		trace = traceConversion_;
		break;
	    case PROXY:
		trace = traceProxy_;
		break;
	}

	return trace;
    }
}
