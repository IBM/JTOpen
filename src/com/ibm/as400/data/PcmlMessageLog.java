///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlMessageLog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.io.*;

import java.util.Date;

/**
 * Provides control over logging and tracing activity within this 
 * package. <code>PcmlMessageLog</code> can be used to redirect error logging to a 
 * specific log file or <code>OutputStream</code>.  It is also used
 * to suppress the low level information/error messages normally written 
 * to the console.  
 */
public class PcmlMessageLog
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static String       m_logFileName;
    private static OutputStream m_outputStream;
    private static PrintWriter  m_logTarget;

    private static boolean      m_traceEnabled = false;
    private static String       m_hexDigits; 
    private static StringBuffer m_cp37Table;   

    static
    {
        try 
        { 
            setLogFileName(null); 
        }
        catch (IOException e)     
        {}

        m_hexDigits = "0123456789ABCDEF";
        m_cp37Table = new StringBuffer(256);
        for (int i = 0; i < 256; i++)
        {
            m_cp37Table.append('.');
        }
        m_cp37Table.setCharAt(0x40, ' ');        m_cp37Table.setCharAt(0x4b, '.');        m_cp37Table.setCharAt(0x4c, '<');
        m_cp37Table.setCharAt(0x4d, '(');        m_cp37Table.setCharAt(0x4e, '+');        m_cp37Table.setCharAt(0x50, '&');
        m_cp37Table.setCharAt(0x5a, '!');        m_cp37Table.setCharAt(0x5b, '$');        m_cp37Table.setCharAt(0x5c, '*');
        m_cp37Table.setCharAt(0x5d, ')');        m_cp37Table.setCharAt(0x5e, ';');        m_cp37Table.setCharAt(0x60, '-');
        m_cp37Table.setCharAt(0x61, '/');        m_cp37Table.setCharAt(0x6a, '|');        m_cp37Table.setCharAt(0x6b, ',');
        m_cp37Table.setCharAt(0x6c, '%');        m_cp37Table.setCharAt(0x6d, '_');        m_cp37Table.setCharAt(0x6e, '>');
        m_cp37Table.setCharAt(0x6f, '?');        m_cp37Table.setCharAt(0x79, '`');        m_cp37Table.setCharAt(0x7a, ':');
        m_cp37Table.setCharAt(0x7b, '#');        m_cp37Table.setCharAt(0x7c, '@');        m_cp37Table.setCharAt(0x7d, '\'');
        m_cp37Table.setCharAt(0x7e, '=');        m_cp37Table.setCharAt(0x7f, '"');        
        m_cp37Table.setCharAt(0x81, 'a');        m_cp37Table.setCharAt(0x82, 'b');        m_cp37Table.setCharAt(0x83, 'c');
        m_cp37Table.setCharAt(0x84, 'd');        m_cp37Table.setCharAt(0x85, 'e');        m_cp37Table.setCharAt(0x86, 'f');
        m_cp37Table.setCharAt(0x87, 'g');        m_cp37Table.setCharAt(0x88, 'h');        m_cp37Table.setCharAt(0x89, 'i');
        m_cp37Table.setCharAt(0x91, 'j');        m_cp37Table.setCharAt(0x92, 'k');        m_cp37Table.setCharAt(0x93, 'l');
        m_cp37Table.setCharAt(0x94, 'm');        m_cp37Table.setCharAt(0x95, 'n');        m_cp37Table.setCharAt(0x96, 'o');
        m_cp37Table.setCharAt(0x97, 'p');        m_cp37Table.setCharAt(0x98, 'q');        m_cp37Table.setCharAt(0x99, 'r');
        m_cp37Table.setCharAt(0xa2, 's');        m_cp37Table.setCharAt(0xa3, 't');        m_cp37Table.setCharAt(0xa4, 'u');
        m_cp37Table.setCharAt(0xa5, 'v');        m_cp37Table.setCharAt(0xa6, 'w');        m_cp37Table.setCharAt(0xa7, 'x');
        m_cp37Table.setCharAt(0xa8, 'y');        m_cp37Table.setCharAt(0xa9, 'z');        m_cp37Table.setCharAt(0xc1, 'A');
        m_cp37Table.setCharAt(0xc2, 'B');        m_cp37Table.setCharAt(0xc3, 'C');        m_cp37Table.setCharAt(0xc4, 'D');
        m_cp37Table.setCharAt(0xc5, 'E');        m_cp37Table.setCharAt(0xc6, 'F');        m_cp37Table.setCharAt(0xc7, 'G');
        m_cp37Table.setCharAt(0xc8, 'H');        m_cp37Table.setCharAt(0xc9, 'I');        m_cp37Table.setCharAt(0xd1, 'J');
        m_cp37Table.setCharAt(0xd2, 'K');        m_cp37Table.setCharAt(0xd3, 'L');        m_cp37Table.setCharAt(0xd4, 'M');
        m_cp37Table.setCharAt(0xd5, 'N');        m_cp37Table.setCharAt(0xd6, 'O');        m_cp37Table.setCharAt(0xd7, 'P');
        m_cp37Table.setCharAt(0xd8, 'Q');        m_cp37Table.setCharAt(0xd9, 'R');        m_cp37Table.setCharAt(0xe2, 'S');
        m_cp37Table.setCharAt(0xe3, 'T');        m_cp37Table.setCharAt(0xe4, 'U');        m_cp37Table.setCharAt(0xe5, 'V');
        m_cp37Table.setCharAt(0xe6, 'W');        m_cp37Table.setCharAt(0xe7, 'X');        m_cp37Table.setCharAt(0xe8, 'Y');
        m_cp37Table.setCharAt(0xe9, 'Z');        
        m_cp37Table.setCharAt(0xf0, '0');        m_cp37Table.setCharAt(0xf1, '1');        m_cp37Table.setCharAt(0xf2, '2');
        m_cp37Table.setCharAt(0xf3, '3');        m_cp37Table.setCharAt(0xf4, '4');        m_cp37Table.setCharAt(0xf5, '5');
        m_cp37Table.setCharAt(0xf6, '6');        m_cp37Table.setCharAt(0xf7, '7');        m_cp37Table.setCharAt(0xf8, '8');
        m_cp37Table.setCharAt(0xf9, '9');
         
    }

    static void main(String[] args)
        throws IOException
    {
        logError("This is a test error to the console");
        traceOut("Message to stdout");
        traceErr("Message to stderr");
        
        traceOut("Test dump of byte array: " + byteArrayToHexString(new byte[] {0, 1, 2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32} ) );
        byte[] bytes = new byte[Byte.MAX_VALUE - Byte.MIN_VALUE + 1];
        for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) 
        {
            bytes[b - Byte.MIN_VALUE] = b;
        }
        bytes[Byte.MAX_VALUE - Byte.MIN_VALUE] = Byte.MAX_VALUE;
        dumpBytes(m_logTarget, bytes);

        setLogFileName("pcml.log");
        logError("This is a test error to pcml.log");

        setLogStream(new FileOutputStream("user.log"));
        logError("This is a test error to user.log with throwable", new IllegalArgumentException("test exception"));

        setLogFileName(null);
        logError("This is a test error back to the console");

        setTraceEnabled(false);
        traceOut("Message to stdout should not be sent");
        traceErr("Message to stderr should not be sent");
      

        System.out.println("Test complete!");
    }

    // Prevent the user from constructing a PcmlMessageLog object
    private PcmlMessageLog() {}

    /**
     * Sets the log file name.
     *
     * If the file exists, error data is appended to it.
     * If the file does not exist, it is created.
     *
     * @param fileName the log file name.  If null, output goes to <code>System.err</code>.
     * @exception IOException if the file cannot be accessed
     * @see #getLogFileName
    */    
    public static void setLogFileName(String fileName)
        throws IOException
    {
        if (m_logTarget != null)
            m_logTarget.flush();

        if (m_logFileName != null)
            m_logTarget.close();

        m_logFileName = fileName;

        if (m_logFileName != null)
        {
            File file = new File(m_logFileName);
            m_outputStream = new FileOutputStream(m_logFileName, file.exists());
            m_logTarget = new PrintWriter(m_outputStream, true);
        }
        else
        {
            m_outputStream = System.err;
            m_logTarget = new PrintWriter(System.err, true);
        }
    }

    /**
     * Returns the log file name.
     *
     * Returns null if errors are being logged to <code>System.err</code>, or if
     * an <code>OutputStream</code> was specified on a call to <code>setLogStream</code>.
     *
     * @return the log file name
     * @see #setLogFileName
     * @see #setLogStream
    */    
    public static String getLogFileName()
    {
        return m_logFileName;
    }

    /**
     * Sets the log stream.
     *
     * This method allows applications to redirect errors generated
     * by this package to the same <code>OutputStream</code> used by the
     * application itself for logging errors.
     *
     * @param stream the <code>OutputStream</code> to which error data should be sent.
     * If null, output goes to <code>System.err</code>.
     * @see #getLogStream
    */
    public static void setLogStream(OutputStream stream)
    {
        m_logTarget.flush();

        if (m_logFileName != null)
            m_logTarget.close();

        m_logFileName = null;

        if (stream != null)
        {
            m_outputStream = stream;
            m_logTarget = new PrintWriter(m_outputStream, true);
        }
        else
        {
            m_outputStream = System.err;
            m_logTarget = new PrintWriter(System.err, true);
        }
    }

    /**
     * Returns the log stream.
     *
     * This method is guaranteed to return a valid non-null <code>OutputStream</code>.
     *
     * @return the <code>OutputStream</code> to which error data is being sent
     * @see #setLogStream
    */
    public static OutputStream getLogStream()
    {
        return m_outputStream;
    }

    /**
     * Logs an error string to the current logging destination.
     *
     * The string will be prepended with a header containing a 
     * date and timestamp if logging has been redirected to
     * a destination other than <code>System.err</code>.
     *
     * @param errorData the data to be logged
    */
    public static void logError(Object errorData)
    {
        synchronized(m_logTarget)
        {
            logTimeStamp();
            m_logTarget.println(errorData);
        }
    }

    /**
     * Logs an error string and a stack trace to the current logging destination.
     *
     * The string will be prepended with a header containing a 
     * date and timestamp if logging has been redirected to
     * a destination other than <code>System.err</code>.
     *
     * @param errorData the data to be logged
     * @param throwable the <code>Throwable</code> which will be used to obtain the stack trace
    */
    public static void logError(Object errorData, Throwable t)
    {
        synchronized(m_logTarget)
        {
            logTimeStamp();
            m_logTarget.println(errorData);

            if (t != null)
            {
                logTimeStamp();
                m_logTarget.println("Stack trace:");
                t.printStackTrace(m_logTarget);
            }
        }
    }

    /**
     * Logs a stack trace to the current logging destination.
     *
     * @param throwable the <code>Throwable</code> which will be used to obtain the stack trace
    */
    public static void printStackTrace(Throwable t)
    {
        synchronized(m_logTarget)
        {
            logTimeStamp();
            m_logTarget.println("Stack trace:");
            t.printStackTrace(m_logTarget);
        }
    }

    /**
     * Controls whether low level trace messages will be written to the console.
     *
     * The default value is <code>true</code>.
     *
     * @param enabled If true, allows the messages; otherwise, suppresses the messages.
     * @see #isTraceEnabled
    */
    public static void setTraceEnabled(boolean enabled)
    {
        m_traceEnabled = enabled;
    }

    /**
     * Determines whether low level trace messages will be written to the console.
     *
     * The default value is <code>true</code>.
     *
     * @return true if the messages are allowed; false otherwise.
     * @see #setTraceEnabled
    */
    public static boolean isTraceEnabled()
    {
        return m_traceEnabled;
    }

    /**
     * Writes data to <code>System.out</code> if low level tracing is enabled.
     *
     * @param data the data to be logged
     * @see #traceErr
    */
    public static void traceOut(Object data)
    {
        if (m_traceEnabled)
            System.out.println(data);
    }

    /**
     * Writes data to <code>System.err</code> if low level tracing is enabled.
     *
     * @param data the data to be logged
     * @see #traceOut
    */
    public static void traceErr(Object data)
    {
        if (m_traceEnabled)
            System.err.println(data);
    }

    static void traceParameter(String program, String parmName, byte[] bytes) // @A1C
    {
        if (m_traceEnabled)
        {
            synchronized(m_logTarget)
            {
                logTimeStamp();
                m_logTarget.println();
                m_logTarget.println(program  + "\t  " + parmName); // @A1C
                dumpBytes(m_logTarget, bytes);
            }
        }
    }

    private static void logTimeStamp()
    {
        if (!m_outputStream.equals(System.err))
        {
            m_logTarget.print((new Date()).toString());
            m_logTarget.print("  ");
        }
    }

    private static void dumpBytes(PrintWriter writer, byte[] ba) 
    {
        int bytes, offset;
        String offStr;
        String cp37Str;
        
        if (!m_traceEnabled)
            return;
        
        if (ba == null)
            return;
            
        bytes = ba.length;
        offset = 0;
        cp37Str = "";
        while (offset < bytes) 
        {
            if ((offset % 32) == 0)
            {
                if (offset == 0) 
                {
                    writer.println("Offset : 0....... 4....... 8....... C....... 0....... 4....... 8....... C.......   0...4...8...C...0...4...8...C...");
                }
                else
                {
                    writer.println(" *" + cp37Str + "*");
                    cp37Str = "";
                }
                    
                offStr = "      " + Integer.toHexString(offset);
                offStr = offStr.substring(offStr.length() - 6);
                writer.print(offStr + " : ");
            }
            writer.print( byteArrayToHexString(ba, offset, 4) + " ");
            cp37Str = cp37Str + byteArrayToCP37String(ba, offset, 4);
            offset = offset + 4;

        }
        if (offset > 0)
        {
            // Add more blanks for the case where the number of bytes
            // was not a multiple of four. In this case, offset 
            // 'overshot' the number of bytes.
            for (int b = bytes; b < offset; b++)
            {
                writer.print("  ");
                cp37Str = cp37Str + " ";
            }
            // Now pad the line to a multiple of 32 bytes so the
            // character dump on the right side is lined up.
            while ((offset % 32) != 0)
            {
                writer.print("         ");
                cp37Str = cp37Str + "    ";
                offset = offset + 4;
            }
            writer.println(" *" + cp37Str + "*");
            cp37Str = "";
        }
        
    }

    private static String byteArrayToCP37String(byte[] ba, int index, int length) 
    {
        int bytes;
        int endIndex;
        
        if (ba == null)
            return "";
            
        bytes = ba.length;
        if (bytes == 0 || index < 0 || index >= bytes )
            return "";
            
        if (index + length <= bytes)
            endIndex = index + length - 1;
        else
            endIndex = bytes - 1;    
                    
        StringBuffer cp37String = new StringBuffer((endIndex-index+1));    
        for (int b = index; b <= endIndex; b++)
        {
            if (ba[b] < 0)
            {
                cp37String.append( m_cp37Table.charAt(256 + ba[b]) ); 
            }
            else
            {
                cp37String.append( m_cp37Table.charAt(ba[b]) ); 
            }
        }
        return cp37String.toString();
    }

    static String byteArrayToHexString(byte[] ba) 
    {
        if (ba == null)
            return "";
        else
            return byteArrayToHexString(ba, 0, ba.length);
    }

    static String byteArrayToHexString(byte[] ba, int index, int length) 
    {
        int bytes;
        int endIndex;
        
        if (ba == null)
            return "";
            
        bytes = ba.length;
        if (bytes == 0 || index < 0 || index >= bytes )
            return "";
            
        if (index + length <= bytes)
            endIndex = index + length - 1;
        else
            endIndex = bytes - 1;    
                    
        StringBuffer hexString = new StringBuffer((endIndex-index+1)*2);    
        for (int b = index; b <= endIndex; b++)
        {
            hexString.append( byteToHexString(ba[b]) ); 
        }
        return hexString.toString();
    }
    
    private static String byteToHexString(byte aByte) 
    {
        int highNibble = ((aByte << 24) >>> 28);
        int lowNibble =  ((aByte << 28) >>> 28);
        return m_hexDigits.substring(highNibble, highNibble+1) + m_hexDigits.substring(lowNibble, lowNibble+1); 
    }
}
