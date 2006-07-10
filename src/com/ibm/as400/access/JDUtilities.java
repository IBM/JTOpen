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
    static final int vrm540 = AS400.generateVRM(5, 4, 0);           // @540
    static final int vrm530 = AS400.generateVRM(5, 3, 0);           // @G0A
    static final int vrm520 = AS400.generateVRM(5, 2, 0);           // @J2a @J3a   
    static final int vrm510 = AS400.generateVRM(5, 1, 0);           //      @J31a
    static final int vrm450 = AS400.generateVRM(4, 5, 0);           // @G0A
    static final int vrm440 = AS400.generateVRM(4, 4, 0);
    static final int vrm430 = AS400.generateVRM(4, 3, 0);
                 
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
    static final void decompress (byte[] source,
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
    static final String padZeros (int value, int digits)
    {
        String temp = "000000000" + Integer.toString (value); // @A1C
        return temp.substring (temp.length () - digits);
    }


    //@DELIMa
    /**
    Returns the names of the libraries on the system.
    This will return a ResultSet with a list of all the libraries.

    @param caller The calling object.
    @param connection The connection to the database.
    @param setting The conversion settings (optional).
    @param libraryListOnly  If true, returns only the libraries on the library list.
                        Otherwise returns all libraries on the system.
    @return  A ResultSet containing the list of the libraries.

    @exception  SQLException    If the connection is not open
                                or an error occurs.
    **/
    static final ResultSet getLibraries (Object caller, AS400JDBCConnection connection, SQLConversionSettings settings, boolean libraryListOnly)
    throws SQLException
    {
        // Schema = library
        connection.checkOpen ();

        JDRowCache rowCache = null;  // Creates a set of rows that
                                     // are readable one at a time

        if (settings == null) {
          settings = new SQLConversionSettings (connection);
        }
        int id = connection.getID();

        try
        {
            // Create a request
            //@P0C
            DBReturnObjectInformationRequestDS request = null;
            DBReplyRequestedDS reply = null;
            try
            {
                request = DBDSPool.getDBReturnObjectInformationRequestDS (
                       DBReturnObjectInformationRequestDS.FUNCTIONID_RETRIEVE_LIBRARY_INFO,
                       id, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);


                if (!libraryListOnly) // Return list of all libraries on the system
                {
                  
                  request.setLibraryName("%", connection.converter_);
                  request.setLibraryNameSearchPatternIndicator(0xF1);
                }


                // Set the Library Information to Return Bitmap
                // Return only the library name
                request.setLibraryReturnInfoBitmap(0x80000000);

                // Send the request and cache all results from the system
                reply = connection.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0)
                {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (caller, connection, id, errorClass, returnCode);
                }

                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();

                // Put the result data into a row cache
                JDServerRow row =  new JDServerRow (connection, id, dataFormat, settings);

                // Put the data format into a row format object
                JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache(row, connection, id, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));
                boolean isJDBC3 = JDUtilities.JDBCLevel_ >= 30; //@F2A @j4a

                JDFieldMap[] maps = null;    //@F2C
                String[] fieldNames = null;  //@F2C
                SQLData[] sqlData = null;    //@F2C
                int[] fieldNullables = null; //@F2C
                // Set up the result set in the format required by JDBC
                if (!isJDBC3)
                {
                    fieldNames = new String[] {"TABLE_SCHEM"};

                    sqlData = new SQLData[] {new SQLVarchar (128, settings)};   //schema name

                    fieldNullables = new int[] {AS400JDBCDatabaseMetaData.columnNoNulls};
                    maps = new JDFieldMap[1];   
                }
                else
                {
                    fieldNames = new String[] {"TABLE_SCHEM",
                        "TABLE_CATALOG"};  //@G4A

                    sqlData = new SQLData[] {new SQLVarchar (128, settings),   //schema name
                        new SQLVarchar (128, settings)};  //table catalog  //@G4A

                    fieldNullables = new int[] {AS400JDBCDatabaseMetaData.columnNoNulls, 
                        AS400JDBCDatabaseMetaData.columnNullable}; //@G4A
                    maps = new JDFieldMap[2];   //@G4C
                }

                // Create the mapped row format that is returned in the
                // result set.
                // This does not actual move the data, it just sets up
                // the mapping
                maps[0] = new JDSimpleFieldMap (1); // table schema  // @A3C @E4C
                if (isJDBC3)  //@F2A
                {
                    maps[1] = new JDHardcodedFieldMap (connection.getCatalog ()); // table catalog //@G4A
                }

                // Create the mapped row cache that is returned in the
                // result set
                JDMappedRow mappedRow = new JDMappedRow (fieldNames, sqlData,
                                                         fieldNullables, maps);
                rowCache = new JDMappedRowCache (mappedRow,
                                                 serverRowCache);
            }
            finally
            {
                if (request != null) request.inUse_ = false;
                if (reply != null) reply.inUse_ = false;
            }

        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (caller, JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache,
                                       connection.getCatalog(), "Schemas");

    }


    /**
     Strips outer double-quotes from name (if present).
     **/
    static final String stripOuterDoubleQuotes(String name/*, boolean uppercase*/)
    {
      if(name.startsWith("\"") && name.endsWith("\"")) {
        name = name.substring(1, name.length()-1);
      }
      return name;
    }


    /**
     Prepares the name to be enclosed in double-quotes, for example for use as column-name values in an INSERT INTO statement.
     1. Strip outer double-quotes (if present).
     2. Double-up any embedded double-quotes.
     **/
    static final String prepareForDoubleQuotes(String name)
    {
      // Strip outer double-quotes.
      name = stripOuterDoubleQuotes(name);

      // Double-up any embedded double-quotes.
      if(name.indexOf('\"') == -1)
      {
        return name;  // name has no embedded double-quotes, so nothing more to do
      }
      else
      {
        StringBuffer buf = new StringBuffer(name);
        for (int i=name.length()-1; i >= 0; i--)  // examine char-by-char, from end
        {
          if(buf.charAt(i) == '\"')
          {
            buf.insert(i, '\"');  // double the embedded double-quote
          }
        }
        return buf.toString();
      }
    }


    /**
     Prepares the name to be enclosed in single-quotes, for example for use in the WHERE clause of a SELECT statement.
     1. Unless name is delimited by outer double-quotes, uppercase the name.
     2. Strip outer double-quotes (if present).
     3. Collapse any doubled embedded double-quotes, to single double-quotes.
     4. Double-up any embedded single-quotes.
     **/
    static final String prepareForSingleQuotes(String name, boolean uppercase)
    {
      // 1. Unless name is delimited by outer double-quotes, uppercase the name.
      if(name.startsWith("\"") && name.endsWith("\""))
      {
        // 2. Strip outer double-quotes.
        name = name.substring(1, name.length()-1);
      }
      else
      {
        // Don't uppercase if any embedded quotes.
        if (uppercase && name.indexOf('\'') == -1) {
          name = name.toUpperCase();
        }
      }

      // 3. Collapse any doubled embedded double-quotes, to single double-quotes.
      // 4. Double-up any embedded single-quotes.
      if(name.indexOf('\"') == -1 && name.indexOf('\'') == -1)
      {
        return name;  // name has no embedded double-quotes, so nothing more to do
      }
      else
      {
        StringBuffer buf = new StringBuffer(name);
        for (int i=name.length()-1; i >= 0; i--)  // examine char-by-char, from end
        {
          char thisChar = buf.charAt(i);
          if(thisChar == '\"')
          {
            if(i>0 && buf.charAt(i-1) == '\"')
            {
              buf.deleteCharAt(i);
              i--;  // don't re-examine the prior double-quote
            }
          }
          else if(thisChar == '\'')
          {
            buf.insert(i, '\'');  // double the single-quote
          }
        }
        return buf.toString();
      }
    }


    /**
     Strips out beginning/ending matching double-quotes, and internal double
     embedded double-quotes get collapsed to one double-quote.
     1. Strip outer double-quotes (if present).
     2. Collapse any doubled embedded double-quotes, to single double-quotes.
     **/
    static final String stripOutDoubleEmbededQuotes(String name)
    {
      if(name.startsWith("\"") && name.endsWith("\""))
      {
        // 1. Strip outer double-quotes.
        name = name.substring(1, name.length()-1);
      }
  
      // 2. Collapse any doubled embedded double-quotes, to single double-quotes.
      if(name.indexOf('\"') == -1 )
      {
        return name;  // name has no embedded double-quotes, so nothing more to do
      }
      else
      {
        StringBuffer buf = new StringBuffer(name);
        for (int i=name.length()-1; i >= 0; i--)  // examine char-by-char, from end
        {
          char thisChar = buf.charAt(i);
          if(thisChar == '\"')
          {
            if(i>0 && buf.charAt(i-1) == '\"')
            {
              buf.deleteCharAt(i);
              i--;  // don't re-examine the prior double-quote
            }
          }
        }
        return buf.toString();
      }
    }



/**
Reads a reader and returns its data as a String.

@param  input       The reader.
@param  length      The length.
@return             The string.

@exception SQLException If the length is not valid or the
                        conversion is not possible.
**/
    static final String readerToString (Reader input,
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
Runs a CL command via the database host server.  It uses the QCMDEXC
stored procedure to run the command

@param  connection  Connection to the system
@param  command     The CL command to run

@exception SQLException If the command failed.
**/
   static final void runCommand(Connection connection, String command, boolean SQLNaming)
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
    static final byte[] streamToBytes (InputStream input,
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
            //@KKC if (actualLength != length)
            //@KKC    JDError.throwSQLException (JDError.EXC_BUFFER_LENGTH_INVALID);

            //@KKC throw an exception if length is greater than the actual length
            if(actualLength < length)                                               //@KKC
                JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);          //@KKC
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
    static final String streamToString (InputStream input,
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


    //@DELIMa
    /**
     Uppercases the name if it's not enclosed in double-quotes.
     **/
    static final String upperCaseIfNotQuoted(String name)
    {
      // 1. Unless name is delimited by outer double-quotes, uppercase the name.
      if(name.startsWith("\""))
        return name;
      else
        return name.toUpperCase();
    }


}
