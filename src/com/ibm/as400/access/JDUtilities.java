///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDUtilities.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;                                            // @J1c



/**
The JDUtilities class provides utilities for use in the implementation
of the JDBC driver.
**/
class JDUtilities
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    private static final byte escape        = (byte)0x1B;           // @D0A

    // @j4 the following two constants are now final
    static final int vrm530 = AS400.generateVRM(5, 3, 0);           // @G0A
    static final int vrm520 = AS400.generateVRM(5, 2, 0);           // @J2a @J3a   
    static final int vrm510 = AS400.generateVRM(5, 1, 0);           //      @J31a
    static final int vrm450 = AS400.generateVRM(4, 5, 0);           // @G0A
                 
    static int JDBCLevel_ = 10;                                     // @J4a         
     
    // @J4a
    static
    {
       try 
       { 
          Class.forName("java.sql.Blob"); 
          JDBCLevel_ = 20;

          Class.forName("java.sql.Savepoint"); 
          JDBCLevel_ = 30;
       }                                         
       catch (Throwable e) { }   
    }                          
                             


// @D0A
/**
Decompresses data from one byte array to another.

@param source               The source (compressed) bytes.
@param sourceOffset         The offset in the source bytes to start decompressing.
@param sourceLength         The length (compressed) of the bytes to decompress.
@param destination          The destination (uncompressed) bytes.  It is assumed
                            that this byte array is already created.
@param destinationOffset    The offset in the destination bytes.
**/
    static void decompress (byte[] source,
                            int sourceOffset,
                            int sourceLength,
                            byte[] destination,
                            int destinationOffset)
    {
        int i = sourceOffset;               // Index into source.
        int j = destinationOffset;          // Index into destination.

        int sourceEnd = sourceOffset + sourceLength;
        while(i < sourceEnd) {
            if (source[i] == escape) {
                if (source[i+1] == escape) {
                    destination[j++] = escape;
                    i += 2;
                }
                else {
                    int repetitions = BinaryConverter.byteArrayToInt(source, i+2);
                    for(int k = 1; k <= repetitions; ++k)
                        destination[j++] = source[i+1];
                    i += 6;
                }
            }
            else
                destination[j++] = source[i++];
        }
    }


/**
Pads a numeric value on the left with zeros.  This is a utility
for use in implementing various pieces of the JDBC driver.

@param value    The numeric value.
@param digits   The number of digits.
@return         The padded string.
**/
    static String padZeros (int value, int digits)
    {
        String temp = "000000000" + Integer.toString (value); // @A1C
        return temp.substring (temp.length () - digits);
    }



/**
Reads a reader and returns its data as a String.

@param  input       The reader.
@param  length      The length.
@return             The string.

@exception SQLException If the length is not valid or the
                        conversion is not possible.
**/
    static String readerToString (Reader input,
                                  int length)
        throws SQLException
    {
        StringBuffer buffer = new StringBuffer ();
        try {
            char[] rawChars = new char[(length == 0) ? 1 : length];
            int actualLength = 0;
            while (input.ready ()) {
                int length2 = input.read (rawChars);
                if (length2 < 0)
                    break;
                buffer.append (new String (rawChars, 0, length2));
                actualLength += length2;
            }

            // The spec says to throw an exception when the
            // actual length does not match the specified length.
            // I think this is strange since this means the length
            // parameter is essentially not needed.  I.e., we always
            // read the exact number of bytes in the stream.
            if (actualLength != length)
                JDError.throwSQLException (JDError.EXC_BUFFER_LENGTH_INVALID);
        }
        catch (IOException e) {
            JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        }

        return buffer.toString ();
    }





//@j1 new method
/**
Runs a CL command via the database server.  It uses the QCMDEXC
stored procedure to run the command

@param  connection  Connection to the server
@param  command     The CL command to run

@exception SQLException If the command failed.
**/
   static void runCommand(Connection connection, String command, boolean SQLNaming)
                          throws SQLException
   {
      Statement statement = connection.createStatement();

      // We run commands via the QCMDEXC stored procedure.  That procedure
      // requires the length of the command be included with the command 
      // specified in precision 15, scale 5 format.  That is,
      // "CALL QSYS.QCMDEXC('command-to-run', 000000nnnn.00000)"
      String commandLength = "0000000000" + command.length();
             commandLength = commandLength.substring(commandLength.length() - 10) +
                               ".00000";
                                                                                  
      String commandPreface;

      if (SQLNaming)
         commandPreface = "CALL QSYS.QCMDEXC('";
      else
         commandPreface = "CALL QSYS/QCMDEXC('";
                                                                                  
      String SQLCommand = commandPreface + command  + "', " + commandLength + ")";

      statement.executeUpdate(SQLCommand);
      statement.close();
   }






/**
Reads an input stream and returns its data as a byte array.

@param  input       The input stream.
@param  length      The length.
@return             The string.

@exception SQLException If the length is not valid or the
                        conversion is not possible.
**/
    static byte[] streamToBytes (InputStream input,
                                 int length)
        throws SQLException
    {
        byte[] buffer = new byte[length];
        try {
            byte[] rawBytes = new byte[(length == 0) ? 1 : length];
            int actualLength = 0;
            while (input.available () > 0) {
                int length2 = input.read (rawBytes);
                if (actualLength + length2 <= length)
                    System.arraycopy (rawBytes, 0, buffer, actualLength, length2);
                actualLength += length2;
            }

            // The spec says to throw an exception when the
            // actual length does not match the specified length.
            // I think this is strange since this means the length
            // parameter is essentially not needed.  I.e., we always
            // read the exact number of bytes in the stream.
            if (actualLength != length)
                JDError.throwSQLException (JDError.EXC_BUFFER_LENGTH_INVALID);
        }
        catch (IOException e) {
            JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        }

        return buffer;
    }


/**
Reads an input stream and returns its data as a String.

@param  input       The input stream.
@param  length      The length.
@param  encoding    The encoding.
@return             The string.

@exception SQLException If the length is not valid or the
                        conversion is not possible.
**/
    static String streamToString (InputStream input,
                                  int length,
                                  String encoding)
        throws SQLException
    {
        StringBuffer buffer = new StringBuffer ();
        try {
            byte[] rawBytes = new byte[(length == 0) ? 1 : length];
            int actualLength = 0;
            while (input.available () > 0) {
                int length2 = input.read (rawBytes);
                buffer.append (new String (rawBytes, 0, length2, encoding));
                actualLength += length2;
            }

            // The spec says to throw an exception when the
            // actual length does not match the specified length.
            // I think this is strange since this means the length
            // parameter is essentially not needed.  I.e., we always
            // read the exact number of bytes in the stream.
            if (actualLength != length)
                JDError.throwSQLException (JDError.EXC_BUFFER_LENGTH_INVALID);
        }
        catch (IOException e) {
            JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        }

        return buffer.toString ();
    }


}
