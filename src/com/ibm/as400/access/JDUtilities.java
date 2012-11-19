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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;                                            // @J1c
import java.util.Hashtable;



/**
The JDUtilities class provides utilities for use in the implementation
of the JDBC driver.
**/
class JDUtilities
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    private static final byte escape        = (byte)0x1B;           // @D0A

    // @j4 the following two constants are now final
    static final int vrm710 = AS400.generateVRM(7, 1, 0);           // @710 //@128sch
    static final int vrm610 = AS400.generateVRM(6, 1, 0);           // @610
    static final int vrm540 = AS400.generateVRM(5, 4, 0);           // @540
    static final int vrm530 = AS400.generateVRM(5, 3, 0);           // @G0A
    static final int vrm520 = AS400.generateVRM(5, 2, 0);           // @J2a @J3a
    static final int vrm510 = AS400.generateVRM(5, 1, 0);           //      @J31a
    static final int vrm450 = AS400.generateVRM(4, 5, 0);           // @G0A
    static final int vrm440 = AS400.generateVRM(4, 4, 0);
    static final int vrm430 = AS400.generateVRM(4, 3, 0);

    static int JDBCLevel_ = 10;                                     // @J4a
    static int JVMLevel_ = 120; //1.2.0                             //@big
    private final static Object bigDecimalLock_ = new Object();           //@big

    // @J4a
    static
    {
       try
       {
          Class.forName("java.sql.Blob");
          JDBCLevel_ = 20;

          Class.forName("java.sql.Savepoint");
          JDBCLevel_ = 30;

          Class.forName("java.util.concurrent.Semaphore");       //@big
          JVMLevel_  = 150;  //jre 5.0                           //@big

          Class.forName("java.sql.SQLXML");                      //@big
          JDBCLevel_ = 40;                                       //@big
          JVMLevel_  = 160;  //jre 6.0                           //@big
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

    static final String padZeros (long value, int digits)
    {
        String temp = "000000000000" + Long.toString (value); // @A1C
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
          settings = SQLConversionSettings.getConversionSettings (connection);
        }
        int id = connection.getID();
        DBReplyRequestedDS reply = null;

        try
        {
            // Create a request
            //@P0C
            DBReturnObjectInformationRequestDS request = null;
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
                else
                {
                    request.setLibraryName("*LIBL", connection.converter_); //@libl
                    request.setLibraryNameSearchPatternIndicator(0xF0);     //@libl
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
                if (request != null) { request.returnToPool(); request = null; }
                // Cannot close this reply.  Pass to AS400JDBCResultSet to close
                // if (reply != null) { reply.returnToPool(); reply = null; }
            }

        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (caller, JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache,
                                       connection.getCatalog(), "Schemas", connection, reply); //@in2

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
                //buffer.append (new String (rawChars, 0, length2));  //@pdd jdbc40
                buffer.append (rawChars, 0, length2);                 //@pda jdbc40 performance
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


    //@pda jdbc40 new method for unknown length
    /**
     Reads a reader and returns its data as a String.
     Reads until reader returns -1 for eof.

     @param  input       The reader.
     @return             The string.

     **/
    static final String readerToString (Reader input)
    throws SQLException
    {
        StringBuffer buffer = new StringBuffer ();
        try {

            char[] rawChars = new char[32000];
            int actualLength = 0;
            while (input.ready ()) {
                int length2 = input.read (rawChars);
                if (length2 < 0)
                    break;
                //buffer.append (new String (rawChars, 0, length2));  //@pdd jdbc40
                buffer.append (rawChars, 0, length2);                 //@pda jdbc40 performance
                actualLength += length2;
            }

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
       Statement statement = null; //@scan1
       try
       {
           statement = connection.createStatement();

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
       }finally //@scan1
       {
           if(statement != null)
               statement.close();
       }

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
    	    //
    	    // Restructured logic to not use available ..
    	    // @A5C
            int length2 = input.read (rawBytes);                 /*@A5A*/

            while (length2 >= 0 && actualLength < length ) {     /*@A5C*/
        		if (length2 > 0) {                               /*@A5A*/
        		    if (actualLength + length2 <= length) {
        			   System.arraycopy (rawBytes, 0, buffer, actualLength, length2);
        		    } else {
        		       // copy part (if needed).
        			   System.arraycopy (rawBytes, 0, buffer, actualLength, length - actualLength);
        		    }
        		    actualLength += length2;
        		}                                                /*@A5A*/
        		length2 = input.read (rawBytes);                 /*@A5A*/
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



    //@PDA jdbc40
    /**
    Reads an input stream and returns its data as a byte array.

    @param  input       The input stream.
    @return             The string.

    @exception SQLException If the length is not valid or the
                            conversion is not possible.
    **/
        static final byte[] streamToBytes (InputStream input)
            throws SQLException
        {
            //@pda copy code from native since ByteBuffer is not available on ibm java
        	ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();


            int blocksize = 4096;
            byte[] buffer = new byte[blocksize];
            try {
        	int length2 = input.read (buffer);

        	while (length2 >=  0) {
        	    outBuffer.write(buffer, 0, length2);
        	    length2 = input.read (buffer);
        	}
            } catch (IOException e) {
            	JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
            }

            return outBuffer.toByteArray();
        }




        //@pda method from native
        /**
        Reads an input stream and returns its data as a String.

        @param  input       The input stream.
        @param  encoding    The encoding.
        @return             The string.

        @exception SQLException If the length is not valid or the
                                conversion is not possible.
         **/
        static String streamToString (InputStream input,
        		String encoding)
        throws SQLException
        {
        	byte[] rawBytes = streamToBytes(input);

        	try {
        		return new String (rawBytes, 0, rawBytes.length, encoding);
        	}  catch (IOException e) {
        		JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        		return null;
        	}
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


    //@big For 1.4 backlevel support of Decimal and Numeric types
    /**
     * bigDecimalToPlainString takes a big decimal and converts it into a plain string, without an
     * exponent.  This was the default behavior of toString before JDK 1.5.
     * bigDecimalToPlainString was taken from Native driver for java 1.5 support and changed a bit for toolbox
     * Coded so it will compile on java 1.4 also
     */
    static java.lang.reflect.Method toPlainStringMethod = null;
    static Object[] emptyArgs;

    public static String bigDecimalToPlainString(BigDecimal bigDecimal) {
        if (JVMLevel_ >= 150) {
            // We compile using JDK 1.4, so we have to get to the new method via
            // reflection

            if (toPlainStringMethod == null) {
                synchronized(bigDecimalLock_) {
                    if (toPlainStringMethod == null) {
                        try {
                            Class bigDecimalClass = Class.forName("java.math.BigDecimal");
                            Class[] parameterTypes = new Class[0];
                            toPlainStringMethod = bigDecimalClass.getMethod("toPlainString",  parameterTypes);
                            emptyArgs = new Object[0];
                        } catch (Exception e) {
                            if (JDTrace.isTraceOn ())
                            {
                               JDTrace.logException(null, "Exception while calling BigDecimal.toPlainString.", e);
                            }
                            toPlainStringMethod = null;

                            return bigDecimal.toString();
                        }
                    } /* if */
                } /* synchronized */
            } /* toPlainStringMethod == null */
            String returnString;
            try {
                returnString =
                    (String) toPlainStringMethod.invoke((Object) bigDecimal, emptyArgs);
            } catch (Exception e) {
                if (JDTrace.isTraceOn ())
                {
                   JDTrace.logException(null, "Exception while calling BigDecimal.toPlainString.", e);
                }

                returnString = bigDecimal.toString();
            }

            return returnString;

        } else { /* not JDK15 */

            return bigDecimal.toString();
        }
    }


    //@xml3
    //removes declaration (header)
    //returns input string if there is no xml declaration
    //@xmlNat changed to be same as native driver
    static final String stripXMLDeclaration(String s) throws SQLException
    {
        int i = 0;
        int len = s.length();
        while (i < len && ( s.charAt(i) == '\ufeff' || Character.isWhitespace(s.charAt(i)))) {
           i++;
        }
        if ((i+1)<len && s.charAt(i) == '<' && s.charAt(i + 1) == '?') {
           i += 2;
           while ((i+1) < len && (s.charAt(i) != '?' || s.charAt(i + 1) != '>')) {
               i++;
           }
           if ((i+1) < len && s.charAt(i) == '?' && s.charAt(i + 1) == '>') {
               return s.substring(i + 2);
           }

        }
        return s;

    }

    //@xmlutf8
    static final boolean hasXMLDeclaration(String xml)
    {
        if(xml.length() < 6) //@BE1
            return false;      //@BE1
        if(xml.substring(0, 7).indexOf("<?xml") != -1) //@BE1 (utf-16be has byteordermark in first char)
            return true;  //if invalid decl, then let hostserver return error
        else
            return false;
    }

    //@xmlutf16 remove encoding inside of XML declaration
    static final String handleXMLDeclarationEncoding(String xml)
    {
        if(xml.length() < 6) //@BE1
            return xml;      //@BE1
        if(xml.substring(0, 7).indexOf("<?xml") != -1) //@BE1 (utf-16be has byteordermark in first char)
        {
            int end = xml.indexOf("?>") ;
            int encStart = xml.indexOf("encoding=");
            if(end != -1 && encStart != -1)
            {
                int encEnd = xml.indexOf(" ",encStart);
                if(encEnd == -1 || encEnd > end)
                    encEnd = end; //end of declaration with no space after encoding

                return xml.substring(0, encStart) + xml.substring(encEnd);
            }
        }
        return xml;
    }

    /**
     * returns the type names based on the type from java.sql.Types
     */

    public static String getTypeName(int typeNumber) {
        switch (typeNumber) {
      case Types.SMALLINT:
          return "SMALLINT";
      case Types.INTEGER:
          return "INTEGER";
      case Types.BIGINT:
          return "BIGINT";
      case Types.FLOAT:
          return "FLOAT";
      case Types.REAL:
          return "REAL";
      case Types.DOUBLE:
          return "DOUBLE";
      case Types.NUMERIC:
          return "NUMERIC";
      case Types.DECIMAL:
          return "DECIMAL";
      case -360:
          return "DECFLOAT";
      case Types.CHAR:
          return "CHAR";
      case Types.VARCHAR:
          return "VARCHAR";
      case Types.DATALINK:
          return "DATALINK";
      case Types.BINARY:
          return "BINARY";
      case Types.VARBINARY:
          return "VARBINARY";
      case Types.TIME:
          return "TIME";
      case Types.DATE:
          return "DATE";
      case Types.TIMESTAMP:
          return "TIMESTAMP";
      case Types.BLOB:
          return "BLOB";
      case Types.CLOB:
          return "CLOB";

          //
                // New for JDK 1.6
                //

      case -8:   /* Types.ROWID */
          return "ROWID";
      case -15:  /* Types.NCHAR */
          return "NCHAR";
      case -9:  /* Types.NVARCHAR */
          return "NVARCHAR";
      case 2011:  /* Types.NCLOB */
          return "NCLOB";
      case -16:   /*  Types.LONGNVARCHAR */
          return "NVARCHAR";
      case 2009:  /* Types.SQLXML */
          return "SQLXML";

      default:
          return "UNKNOWN";

        }
    }

    static String[][] typeNameToTypeCode = {

        {"ARRAY","2003"},
        {"BIGINT","-5"},
        {"BINARY","-2"},
        {"BIT","-7"},
        {"BLOB","2004"},
        {"BOOLEAN","16"},
        {"CHAR","1"},
        {"CLOB","2005"},
        {"DATALINK","70"},
        {"DATE","91"},
        {"DBCLOB", "2005"},
        {"DECIMAL","3"},
        {"DISTINCT","2001"},
        {"DOUBLE","8"},
        {"FLOAT","6"},
        {"INTEGER","4"},
        {"JAVA_OBJECT","2000"},
        {"LONGNVARCHAR","-16"},
        {"LONGVARBINARY","-4"},
        {"LONGVARCHAR","-1"},
        {"NULL","0"},
        {"NUMERIC","2"},
        {"DECFLOAT","-360"},
        {"OTHER","1111"},
        {"REAL","7"},
        {"REF","2006"},
        {"ROWID","-8"},
        {"SMALLINT","5"},
        {"STRUCT","2002"},
        {"TIME","92"},
        {"TIMESTAMP","93"},
        {"TINYINT","-6"},
        {"VARBINARY","-3"},
        {"VARCHAR","12"},
 /*ifndef JDBC40*/
        {"NCHAR","1"},
        {"GRAPHIC", "1"},
        {"NCLOB","2005"},
        {"NVARCHAR","12"},
        {"SQLXML","2005"},
        {"VARGRAPHIC", "12"},
 /*endif */
 /*ifdef JDBC40
        {"NCHAR","-15"},
        {"GRAPHIC", "-15"},
        {"NCLOB","2011"},
        {"NVARCHAR","-9"},
        {"SQLXML","2009"},
        {"VARGRAPHIC", "-9"},
 endif */

    };





    static Hashtable typeNameHashtable = null ;

    public static int getTypeCode(String typeName) throws SQLException {
    if (typeNameHashtable == null) {
      typeNameHashtable = new Hashtable();
      for (int i = 0; i < typeNameToTypeCode.length; i++) {
        typeNameHashtable.put(typeNameToTypeCode[i][0], new Integer(
            typeNameToTypeCode[i][1]));
      }
    }

    Integer integer = (Integer) typeNameHashtable.get(typeName);
    if (integer == null) {
      if (JDTrace.isTraceOn())
        JDTrace.logInformation(null, "Unable to get type from " + typeName);
      JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      return 0;
    } else {
      int typecode = integer.intValue();
      if (typecode != 0) {
        return typecode;
      } else {
        if (JDTrace.isTraceOn())
          JDTrace.logInformation(null, "Unable to get type from " + typeName);
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_INVALID);
        return 0;
      }
    }
  }

    public static Hashtable instanceHashtable;

    public static boolean classIsInstanceOf(Class thisClass, String interfaceName) {
        if (instanceHashtable == null) {
        instanceHashtable = new Hashtable();
        }

        Hashtable interfaceHash = (Hashtable) instanceHashtable.get(thisClass);
        if (interfaceHash == null) {
          interfaceHash = new Hashtable();
          instanceHashtable.put(thisClass, interfaceHash);
        }

        Boolean answer = (Boolean) interfaceHash.get(interfaceName);
     if (answer == null) {
       boolean booleanAnswer = false;

       Class lookClass = thisClass;
       while (lookClass != null && !booleanAnswer) {
         if (JDTrace.isTraceOn()) {
           JDTrace.logInformation(null, "JDUtilities.classIsInstance checking "
               + lookClass.getName() + " of "
               + thisClass.getName() + " for " + interfaceName);
         }
         if (interfaceName.equals(lookClass.getName())) {
           booleanAnswer = true;
         } else {
           Class[] interfaces = lookClass.getInterfaces();
           for (int i = 0; !booleanAnswer && i < interfaces.length; i++) {
             if (JDTrace.isTraceOn()) {
               JDTrace.logInformation(null,
                   "DB2Utilities.classIsInstance checking "
                       + interfaces[i].getName() + " of "
                       + thisClass.getName() + " for "
                       + interfaceName);
             }
             if (interfaces[i].getName().equals(interfaceName)) {
               booleanAnswer = true;
             }
           }
           if (!booleanAnswer) {
             lookClass = lookClass.getSuperclass();
           }
         }
       }
       answer = new Boolean(booleanAnswer);
       interfaceHash.put(interfaceName, answer);
        }

        return answer.booleanValue();


    }


}
