///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDataFactory.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;



/**
<p>A factory that generates appropriate SQLData objects given
various conditions.
**/
class SQLDataFactory
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



// @C2A
/**
Converts a String representation of a number in scientific 
notation to a String representation without scientific
notation.

@param scientificNotation   The String representation of a number
                            in scientific notation.
@return                     The String representation without
                            scientific notation.
**/
    static String convertScientificNotation (String scientificNotation)
    {
        // Check to see if it is indeed scientific notation.
        int e = scientificNotation.indexOf ('E');
        if (e < 0)
            return scientificNotation;
 
        // Parse the exponent and ensure that we don't have a number starting with '+'
        // as the parseInt function will throw a number format exception if we pass that.
        // @F3D int exponent = Integer.parseInt (scientificNotation.substring (e + 1));
        String tempValue = scientificNotation.substring (e + 1);                       // @F3a
        if (tempValue.startsWith("+"))                                                 // @F3a
            tempValue = tempValue.substring(1);                                        // @F3a
        int exponent = Integer.parseInt (tempValue);                                   // @F3a
        
        // Parse the sign.
        boolean sign = (scientificNotation.charAt (0) != '-');            

        // Parse the mantissa and pad with either trailing
        // or leading 0's based on the sign and magnitude
        // of the exponent.
        StringBuffer buffer = new StringBuffer ();
        if (exponent < 0) {
            int digits = -exponent;
            for (int i = 1; i <= digits; ++i)
                buffer.append ('0');                
            buffer.append (scientificNotation.substring (sign ? 0 : 1, e));                
        }
        else if (exponent >= 0) {                       //@G3C Added the equals.
            buffer.append (scientificNotation.substring (sign ? 0 : 1, e));                
            for (int i = 1; i <= exponent; ++i)
                buffer.append ('0');                
        }
        String mantissa = buffer.toString ();

        // All that is left is to move the decimal point.
        // So we copy the digits, insert the decimal point
        // at the correct place.
        int decimalPoint = mantissa.indexOf ('.') + exponent;
        if (exponent > 0)                                           // @C5A
            ++decimalPoint;                                         // @C5A
        buffer = new StringBuffer ();
        int mantissaLength = mantissa.length();
        for (int i = 0; i < mantissaLength; ++i) {
            if (i == decimalPoint)
                buffer.append ('.');
            char ch = mantissa.charAt (i);                        
            if (ch != '.') 
                buffer.append (ch); 
        }

        // Strip leading and trailing 0's, if any.
        int start = 0;
        for (; start < buffer.length() && buffer.charAt (start) == '0'; ++start);   // @G3C  
        int end = buffer.length () - 1;
        for (; end >= 0 && buffer.charAt (end) == '0'; --end);                      // @G3C  
	String result;                                                              // @G3C  
	if (end >= start) {                                                         // @G3A  
           result = buffer.toString ().substring (start, end + 1);                  // @G3C  
	} else {                                                                    // @G3A  
	    result = "0";                                                           // @G3A  
	}                                                                           // @G3A  

        // Add the sign and return.
        return (sign ? "" : "-") + result;
    }



/**
Compute the scale of an object.  This is the number
of digits to the right of the decimal point.

@param  object      A Java object.
@return             the scale.
**/
    static int getScale (Object value)
    {
        int scale = 0;

        String toString = value.toString();
        int point = toString.indexOf ('.');
        if (point != -1)
            scale = toString.length () - point - 1;

        return scale;
    }



/**
Compute the precision of an object.  This is the
total number of digits.

@param  object      A Java object.
@return             the precision.
**/
    static int getPrecision (Object value)
    {
        int precision = 0;

        if (value instanceof Boolean)
            precision = 1;

        else 
        {
            String toString = value.toString();
            
            if (toString.charAt(0) == '-')                                  // @F1a
               toString = toString.substring(1);                            // @F1a
            
            int length = toString.length();

            // We need to truncate any padding zeroes.  Without this,          @E2A
            // the precision of 0.1000 was getting computed as 5 rather        @E2A
            // than 1.                                                      // @E2A
            int startIndex = -1;                                            // @E2A
            int endIndex = length;                                          // @E2A
            int pointIndex = toString.indexOf('.');                         // @E2A
            if (pointIndex >= 0) {                                          // @E2A
                while(toString.charAt(++startIndex) == '0');                // @E2A
                while(toString.charAt(--endIndex) == '0');                  // @E2A
            }                                                               // @E2A
            else {                                                          // @E2A
                startIndex = 0;                                             // @E2A
                endIndex = length - 1;                                      // @E2A
            }                                                               // @E2A

            // Count the characters that are actually digits.                  @E2A
            for (int i = startIndex; i <= endIndex; ++i)                    // @E2C
                if (Character.isDigit (toString.charAt (i)))
                    ++precision;
        }

        return precision;
    }



/**
Map a SQL type code defined in java.sql.Types
to a SQL type code that is supported by DB2 for
OS/400.

@param  sqlType     SQL type code defined in java.sql.Types.
@return             SQL type code that is supported
                    by DB2 for OS/400.
**/
/* @D0D - moved into first newData() method.
    private static int mapSQLType (int sqlType)
    {
        switch (sqlType) {

        case Types.BIGINT:
            return Types.INTEGER;

        case Types.BIT:
            return Types.SMALLINT;

        case Types.LONGVARBINARY:
            return Types.VARBINARY;

        case Types.LONGVARCHAR:
            return Types.VARCHAR;

        case Types.TINYINT:
            return Types.SMALLINT;

        default:
            return sqlType;
        }
    }
*/    



/**
Return a SQLData object corresponding to a
a SQL type code defined in java.sql.Types.
In the case where a SQL type code specifies a
type that is not supported in DB2 for OS/400, then
it will map to the next closest type.

@param  sqlType     SQL type code defined in java.sql.Types.
@param  maxLength   Max length of data.
@param  precision   Precision of data.
@param  scale       Scale of data.
@param  settings    The conversion settings.
@param  vrm         The OS/400 Version, Release, and Modification.
@return             A SQLData object.

@exception  SQLException    If no valid type can be
                            mapped.
**/

    static SQLData newData (int sqlType,
                            int maxLength,
                            int precision,
                            int scale,
                            SQLConversionSettings settings,
                            int vrm)                            // @D0C
        throws SQLException
    {
        switch (sqlType) {                                      // @D0C

        case Types.BIGINT:                                      // @D0A
            if (vrm >= AS400JDBCConnection.BIGINT_SUPPORTED_)   // @D0A
                return new SQLBigint();                         // @D0A
            else
                return new SQLInteger();

        case Types.BINARY:
            return new SQLBinary (maxLength, settings);

        case Types.BLOB:
            return new SQLBlob (maxLength - 4, settings);       // @D1C

        case Types.CHAR:
            return new SQLChar (maxLength, false, settings);

        case Types.CLOB:
            return new SQLClob (maxLength - 4, settings);    // @D1C @E1C
            //return new SQLClob (maxLength - 4, false, settings);    // @D1C @E1C

        case Types.DATE:
            return new SQLDate (settings);

        case Types.DECIMAL:
            if (settings != null)                                           // @E0A
                if (! settings.useBigDecimal())                             // @E0A
                    return new SQLDecimal2 (precision, scale, settings);    // @E0A
            return new SQLDecimal(precision, scale, settings);              // @E0A   

        case Types.DOUBLE:
            return new SQLDouble (settings);

        case Types.FLOAT:
            return new SQLFloat (settings);

        case Types.INTEGER:
            return new SQLInteger ();

        case Types.NUMERIC:
            if (settings != null)                                           // @E0A
                if (! settings.useBigDecimal())                             // @E0A
                    return new SQLNumeric2 (precision, scale, settings);    // @E0A
            return new SQLNumeric(precision, scale, settings);              // @E0A   

        case Types.REAL:
            return new SQLReal (settings);

        case Types.SMALLINT:
        case Types.TINYINT:                                     // @D0A
        case Types.BIT:                                         // @D0A
            return new SQLSmallint ();

        case Types.TIME:
            return new SQLTime (settings);

        case Types.TIMESTAMP:
            return new SQLTimestamp (settings);

        case Types.VARBINARY:
        case Types.LONGVARBINARY:                               // @D0A
            return new SQLVarbinary (maxLength, false, settings);

        case Types.VARCHAR:
        case Types.LONGVARCHAR:                                 // @D0A
            return new SQLVarchar (maxLength, false, false, settings);  // @E1C

        default:
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_INVALID);
     		return null;

        }
    }



/**
Return a SQLData object corresponding to the
type of a Java object.  The mapping is described in
section 8, table 3 ("Standard mapping from Java types
to SQL types") in the JDBC 1.10 specification.
In the case where this table specifies a type that is
not supported in DB2 for OS/400, then it will map to
the next closest type.

@param  object      A Java object.
@param  settings    The conversion settings.
@param  vrm         The OS/400 Version, Release, and Modification.
@return             A SQLData object.

@exception  SQLException    If no valid type can be
                            mapped.
**/
/* @D0D - This method is no longer used.
    static SQLData newData (Object object, 
                            SQLConversionSettings settings, 
                            int vrm)                                // @D0C
        throws SQLException
    {
        if (object instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) object;
            return new SQLNumeric (getPrecision (bigDecimal), bigDecimal.scale(), settings);
        }

        else if (object instanceof Blob)
            return new SQLBlob ((int) ((Blob) object).length (), settings);

        else if (object instanceof Boolean)
            return new SQLSmallint ();

        else if (object instanceof Byte)
            return new SQLSmallint ();

        else if (object instanceof byte[])
            return new SQLVarbinary (((byte[]) object).length, false, settings);

        else if (object instanceof Clob)
            return new SQLClob ((int) ((Clob) object).length (), settings);

        else if (object instanceof Date)
            return new SQLDate (settings);

        else if (object instanceof Double)
            return new SQLDouble (settings);

        else if (object instanceof Float)
            return new SQLReal (settings);

        else if (object instanceof Integer)
            return new SQLInteger ();

        else if (object instanceof Long) {
            if (vrm >= AS400JDBCConnection.BIGINT_SUPPORTED_)   // @D0A
                return new SQLBigint();                         // @D0A
            else                                                // @D0A
                return new SQLInteger ();
        }                                                       // @D0A

        else if (object instanceof Short)
            return new SQLSmallint ();

        else if (object instanceof String)
            return new SQLVarchar (((String) object).length(), settings);

        else if (object instanceof Time)
            return new SQLTime (settings);

        else if (object instanceof Timestamp)
            return new SQLTimestamp (settings);

        else {
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_INVALID);
      		return null;
      	}
    }
*/    



/**
Return a SQLData object corresponding to the
specific OS/400 native type identifier.

@param  connection      The connection.
@param  id              The id.                                   
@param  nativeType      An OS/400 native type identifier.
@param  length          Length of data (in bytes).
@param  precision       Precision of data.
@param  scale           Scale of data.
@param  ccsid           CCSID of data field.
@param  translateBinary Indicates if binary fields should
                        be translated.
@param  settings        The conversion settings.
@param  lobMaxSize      The lob max size.                                      @C3A
@return                 A SQLData object.

@exception  SQLException    If no valid type can be
                            mapped.
**/
    static SQLData newData (AS400JDBCConnection connection,
                            int id,
                            int nativeType,
                            int length,
                            int precision,
                            int scale,
                            int ccsid,
                            boolean translateBinary,
                            SQLConversionSettings settings,
                            int lobMaxSize,                                 // @C3A
                            int columnIndex)     //@F2A
        throws SQLException
    {
        switch (nativeType) {

        case 384:                           // Date.
            return new SQLDate (settings);

        case 388:                           // Time.
            return new SQLTime (settings);

        case 392:                           // Timestamp.
            return new SQLTimestamp (settings);

        case 396:                           // Datalink.
            return new SQLDatalink (length - 2, settings);

        case 404:                           // Blob.
            return new SQLBlob (length - 4, settings);          // @D1C

        case 408:                           // Clob.
            if ((ccsid == 65535) && (translateBinary == false))	  //@E4C
                return new SQLBlob (length - 4, settings);      // @D1C
            else
                return new SQLClob (length - 4, settings); // @D1C @E1C
                //return new SQLClob (length - 4, false, settings); // @D1C @E1C
        
        case 412:                           // Dbclob.
            return new SQLDBClob(length - 4, settings);    // @D1C
            //return new SQLClob (length - 4, true, settings);    // @D1C

        case 448:                           // Varchar.
            if ((ccsid == 65535) && (translateBinary == false))   //@E4C
                return new SQLVarbinary (length - 2, false, settings);
            else
                return new SQLVarchar (length - 2, false, false, settings);

        case 456:                           // Varchar long.
            if ((ccsid == 65535) && (translateBinary == false))    //@E4C
                return new SQLVarbinary (length - 2, true, settings);
            else
                return new SQLVarchar (length - 2, false, true, settings);

        case 452:                           // Char.
            if ((ccsid == 65535) && (translateBinary == false))    //@E4C
                return new SQLBinary (length, settings);
            else
                return new SQLChar (length, false, settings);

        case 464:                           // Graphic (pure DBCS).
            return new SQLVarchar (length - 2, true, false, settings); // @C1C @C4C

        case 472:                           // Graphic long (pure DBCS).
            return new SQLVarchar (length - 2, true, true, settings); // @C1C @C4C

        case 468:                           // Graphic fix (pure DBCS).
            return new SQLChar (length, true, settings); // @C1C @C4C

        case 480:                           // Float.
            if (length == 4)
                return new SQLReal (settings);
            else
                return new SQLDouble (settings);

        case 484:                           // Packed decimal.
            if (settings != null)                                           // @E0A
                if (! settings.useBigDecimal())                             // @E0A
                    return new SQLDecimal2 (precision, scale, settings);    // @E0A
            return new SQLDecimal(precision, scale, settings);              // @E0A   

        case 488:                           // Zoned decimal.
            if (settings != null)                                           // @E0A
                if (! settings.useBigDecimal())                             // @E0A
                    return new SQLNumeric2 (precision, scale, settings);    // @E0A
            return new SQLNumeric(precision, scale, settings);              // @E0A   

        case 492:                           // Bigint.   // @D0A
            return new SQLBigint();                      // @D0A

        case 496:                           // Integer.
            return new SQLInteger (scale);               // @A0C

        case 500:                           // Smallint.
            return new SQLSmallint (scale);              // @A0C

        case 904:                          // @J1A
            return new SQLRowID(settings); // @J1A - Added support for the ROWID data type

        case 960:                           // Blob locator.
            return new SQLBlobLocator (connection, id, lobMaxSize, settings, connection.getConverter(ccsid), columnIndex);  //@F2C //@J0M added converter

        case 964:                           // Clob locator.
            if ((ccsid == 65535) && (translateBinary == false))               //@E4C
                return new SQLBlobLocator (connection, id, lobMaxSize, settings, connection.getConverter(ccsid), columnIndex); //@F2C //@J0M added converter
            else
                return new SQLClobLocator (connection, id, lobMaxSize, settings, connection.getConverter(ccsid), columnIndex); // @E1C //@F2C
                //return new SQLClobLocator (connection, id, lobMaxSize, false, settings, connection.getConverter(ccsid), columnIndex); // @E1C //@F2C
        
        case 968:                           // Dbclob locator.
            return new SQLDBClobLocator(connection, id, lobMaxSize, settings, connection.getConverter(ccsid), columnIndex); // @E1C //@F2C
            //return new SQLClobLocator (connection, id, lobMaxSize, true, settings, connection.getConverter(ccsid), columnIndex); // @E1C //@F2C

        default:
            JDError.throwSQLException (JDError.EXC_INTERNAL, new IllegalArgumentException(Integer.toString(nativeType))); // @E3C
            return null;
        }
    }



/**
Return a SQLData object corresponding to the
specific OS/400 native type string.

@param  nativeType  An OS/400 native type.
@param  length      Length of data (in bytes).
@param  precision   Precision of data.
@param  scale       Scale of data.
@param  settings    The conversion settings.
@return             A SQLData object.

@exception  SQLException    If no valid type can be
                            mapped.
**/
//
// In some cases, there are several different strings
// that match a particular data type.  This is because,
// different functions refer to the types with
// different strings.
//
    static SQLData newData (String nativeType,
                            int length,
                            int precision,
                            int scale,
                            SQLConversionSettings settings)
        throws SQLException
    {
        if (nativeType.equals ("BINARY"))
            return new SQLBinary (length, settings);

        else if (nativeType.equals("BIGINT"))                   // @D0A
            return new SQLBigint();                             // @D0A

        else if (nativeType.equals ("BLOB"))
            return new SQLBlob (length, settings);          // @D1C @G1C Remove length-4

        else if (nativeType.equals ("CHAR"))
            return new SQLChar (length, false, settings);

        else if (nativeType.equals ("CHARACTE"))
            return new SQLChar (length, false, settings);

        else if (nativeType.equals ("CHARACTER"))
            return new SQLChar (length, false, settings);

        else if (nativeType.equals ("CHARACTER VARYING"))
            return new SQLVarchar (length, false, false, settings);     // @E1C

        else if (nativeType.equals ("CLOB"))
            return new SQLClob (length, settings);           // @D1C @E1C @G1C Remove length-4
            //return new SQLClob (length, false, settings);           // @D1C @E1C @G1C Remove length-4

        else if (nativeType.equals ("DATALINK"))
            return new SQLDatalink (length, settings);

        else if (nativeType.equals ("DBCLOB"))           // @G2A
            return new SQLDBClob (length, settings); // @G2A
            //return new SQLClob (length, true, settings); // @G2A

        else if (nativeType.equals ("DATE"))
            return new SQLDate (settings);

        else if (nativeType.equals ("DECIMAL")) {
            if (settings != null)                                           // @E0A
                if (! settings.useBigDecimal())                             // @E0A
                    return new SQLDecimal2 (precision, scale, settings);    // @E0A
            return new SQLDecimal(precision, scale, settings);              // @E0A   
        }

        else if (nativeType.equals ("DOUBLE"))
            return new SQLDouble (settings);

        else if (nativeType.equals ("DOUBLE P"))
            return new SQLDouble (settings);

        else if (nativeType.equals ("DOUBLE PRECISION"))
            return new SQLDouble (settings);

        else if (nativeType.equals ("FLOAT")) {
            if (length == 4)
                return new SQLReal (settings);
            else
                return new SQLDouble (settings);
        }

        else if (nativeType.equals ("GRAPHIC"))
            return new SQLChar (length, true, settings); // @C1C @C4C

        else if (nativeType.equals ("GRAPHIC VARYING"))
            return new SQLVarchar (length, true, false, settings); // @C1C @C4C @E1C

        else if (nativeType.equals ("INTEGER"))
            return new SQLInteger ();

        else if (nativeType.equals ("NUMERIC")) {
            if (settings != null)                                           // @E0A
                if (! settings.useBigDecimal())                             // @E0A
                    return new SQLNumeric2 (precision, scale, settings);    // @E0A
            return new SQLNumeric(precision, scale, settings);              // @E0A   
        }

        else if (nativeType.equals ("REAL"))
            return new SQLReal (settings);

        else if (nativeType.equals("ROWID")) // @J1A
            return new SQLRowID(settings);   // @J1A

        else if (nativeType.equals ("SMALLINT"))
            return new SQLSmallint ();

        else if (nativeType.equals ("TIME"))
            return new SQLTime (settings);

        else if (nativeType.equals ("TIMESTAM"))
            return new SQLTimestamp (settings);

        else if (nativeType.equals ("TIMESTAMP"))
            return new SQLTimestamp (settings);

        else if (nativeType.equals ("TIMESTMP"))
            return new SQLTimestamp (settings);

        else if (nativeType.equals ("VARBINARY"))
            return new SQLVarbinary (length, false, settings);

        else if (nativeType.equals ("VARCHAR"))
            return new SQLVarchar (length, false, false, settings);     // @E1C

        else if (nativeType.equals ("VARG"))
            return new SQLVarchar (length, true, false, settings);      // @E1C

        else if (nativeType.equals ("VARGRAPH"))
            return new SQLVarchar (length, true, false, settings);      // @E1C

        else {
            JDError.throwSQLException (JDError.EXC_INTERNAL, new IllegalArgumentException(nativeType)); // @E3C
     		return null;
     	}
    }



/**
Truncates the precision of a BigDecimal by removing digits from
the left side of the decimal point.
**/
    public static BigDecimal truncatePrecision (BigDecimal bd, int precision)
    {
        boolean positive = bd.longValue () > 0;
        StringBuffer buffer = new StringBuffer (positive ? "" : "-");
        buffer.append (bd.toString ().substring (positive ? precision : precision + 1));
        return new BigDecimal (buffer.toString ());
    }



/**
Truncates the precision of a String representation of a number
by removing digits from the right side of the decimal point.
**/
    public static String truncateScale (String value, int scale)
    {
        int point = value.indexOf ('.');
        if (point >= 0) {
            StringBuffer buffer = new StringBuffer (value);
            for (int i = 1; i <= scale; ++i)
                buffer.append ('0');
            return buffer.toString ().substring (0, point + scale + 1);
        }
        else
            return value;
    }

}
