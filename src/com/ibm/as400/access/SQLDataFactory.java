///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SQLDataFactory.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

/**
<p>A factory that generates appropriate SQLData objects given
various conditions.
**/
class SQLDataFactory
{
    // @C2A
    /**
    Converts a String representation of a number in scientific
    notation to a String representation without scientific
    notation.

    Also handles the use of a different character as the decimal separator.
    This is indicated by the conversion settings.

    @param scientificNotation   The String representation of a number
                                in scientific notation.
    @param settings             The SQLConversionSettings to be used.  The only
                                setting currently used is the decimalSeparator.
    @return                     The String representation without
                                scientific notation.
    **/
    static String convertScientificNotation(String scientificNotation, SQLConversionSettings settings) /*@F5C*/
    {
        if ((settings !=null) && settings.getDecimalSeparator().equals(",")) {  /* @F5A */
          scientificNotation = scientificNotation.replace(',', '.');
        }
        // Check to see if it is indeed scientific notation.
        int e = scientificNotation.indexOf('E');
        if(e < 0)
            return scientificNotation;

        // Parse the exponent and ensure that we don't have a number starting with '+'
        // as the parseInt function will throw a number format exception if we pass that.
        // @F3D int exponent = Integer.parseInt(scientificNotation.substring(e + 1));
        String tempValue = scientificNotation.substring(e + 1);                       // @F3a
        if(tempValue.startsWith("+"))                                                 // @F3a
            tempValue = tempValue.substring(1);                                        // @F3a
        int exponent = Integer.parseInt(tempValue);                                   // @F3a

        // Parse the sign.
        boolean sign = (scientificNotation.charAt(0) != '-');

        String mantissa = scientificNotation.substring(sign ? 0 : 1, e);            //@K1A

        //Check to see if the number contains a decimal point.  If not, add a decimal point and a 0 to
        //the end of the number.
	int index = mantissa.indexOf('.');                                          //@K1A
	if(index == -1)                                                             //@K1A
	{                                                                           //@K1A
		mantissa = mantissa + ".0";                                         //@K1A
		index = mantissa.indexOf('.');                                      //@K1A
	}                                                                           //@K1A

	StringBuffer buffer = new StringBuffer();                                   //@K1A

        //Check if the exponent is positive.  If it is, move the decimal to the right
	if( exponent >= 0 )                                                         //@K1A
	{                                                                           //@k1A
            //Determine the new position the decimal should be in
            int newLocation = index + exponent;                                     //@K1A
            //Copy the numbers before the current decimal
            buffer.append(mantissa.substring(0, index));                            //@K1A
            //Copy the numbers after the decimal
  	    buffer.append(mantissa.substring(index + 1));                           //@K1A
	    mantissa = buffer.toString();                                           //@K1A
            //Determine the length of the string, if the length is less than the new position for the decimal
            //must add 0's to the end of the number
	    int length = mantissa.length();                                         //@K1A
	    for (; length<newLocation; length++)                                    //@K1A
                buffer.append("0");                                                 //@K1A
            //If length is greater than the new decimal location
            if(length > newLocation)                                                //@K1A
            {
                //Copy the digits before the decimal
                String temp = buffer.toString().substring(0, newLocation);          //@K1A
                //put in the decimal
                temp = temp + ".";                                                  //@K1A
                //Copy the rest of the digits to the number
                temp = temp + buffer.toString().substring(newLocation);             //@K1A
                buffer = new StringBuffer(temp);
            }
            else                                                                    //@K1A
                buffer.append(".0");                                                //@K1A
	}                                                                           //@K1A
        else   //negative exponent                                                  //@K1A
        {                                                                           //@K1A
            //IF the decimal point will be at the beginning
            if( (-exponent - index) > 0)                                            //@K1A
            {                                                                       //@K1A
                buffer.append("0.");                                                //@K1A
                //Pad the number with 0's in front if we have to move the decimal to left more times
                //than we currently have digits
                for(int i = 0; i< (-exponent - index) ; i++)                        //@K1A
                    buffer.append("0");                                             //@K1A
                //copy the digits before the current decimal
                buffer.append(mantissa.substring(0, index));                        //@K1A
                //copy the digits after the current decimal
                buffer.append(mantissa.substring(index+1));                         //@K1A
            }                                                                       //@K1A
            else        //A number will be at the beginning                         //@K1A
            {                                                                       //@K1A
                //Copy the number without the current decimal point
                String temp = mantissa.substring(0, index);                         //@K1A
                temp = temp + mantissa.substring(index+1);                          //@K1A

                //Copy the number from the beginning to the location of the new decimal point
                buffer.append(temp.substring(0, index + exponent));                 //@K1A
                //Add the decimal point
                buffer.append('.');                                                 //@K1A
                //Copy the rest of the number
                buffer.append(temp.substring(index + exponent));                    //@K1A
            }                                                                       //@K1A
        }                                                                           //@K1A

        //Add the sign to the number and return
	String number = (sign ? "" : "-") + buffer.toString();                      //@K1A
	return number;                                                              //@K1A

        /**                                                                             //@K1D
        // Parse the mantissa and pad with either trailing
        // or leading 0's based on the sign and magnitude
        // of the exponent.
        StringBuffer buffer = new StringBuffer();
        if(exponent < 0)
        {
            int digits = -exponent;
            for(int i = 1; i <= digits; ++i)
                buffer.append('0');
            buffer.append(scientificNotation.substring(sign ? 0 : 1, e));
        }
        else if(exponent >= 0)
        {                       //@G3C Added the equals.
            buffer.append(scientificNotation.substring(sign ? 0 : 1, e));
            for(int i = 1; i <= exponent; ++i)
                buffer.append('0');
        }
        String mantissa = buffer.toString();

        // All that is left is to move the decimal point.
        // So we copy the digits, insert the decimal point
        // at the correct place.
        int decimalPoint = mantissa.indexOf('.') + exponent;
        if(exponent > 0)                                           // @C5A
            ++decimalPoint;                                         // @C5A
        buffer = new StringBuffer();
        int mantissaLength = mantissa.length();
        for(int i = 0; i < mantissaLength; ++i)
        {
            if(i == decimalPoint)
                buffer.append('.');
            char ch = mantissa.charAt(i);
            if(ch != '.')
                buffer.append(ch);
        }

        // Strip leading and trailing 0's, if any.
        int start = 0;
        for(; start < buffer.length() && buffer.charAt(start) == '0'; ++start);   // @G3C
        int end = buffer.length() - 1;
        for(; end >= 0 && buffer.charAt(end) == '0'; --end);                      // @G3C
        String result;                                                              // @G3C
        if(end >= start)
        {                                                         // @G3A
            result = buffer.toString().substring(start, end + 1);                  // @G3C
        }
        else
        {                                                                    // @G3A
            result = "0";                                                           // @G3A
        }                                                                           // @G3A

        // check to make sure we have more than just "."
        if(result.equals("."))
            result = "0.0";

        // Add the sign and return.
        return(sign ? "" : "-") + result;
        **/
    }

    /**
    Compute the scale of an object.  This is the number
    of digits to the right of the decimal point.

    @param  object      A Java object.
    @return             the scale.
    **/
    static int getScale(Object value)
    {
        int scale = 0;

        String toString = value.toString();
        int point = toString.indexOf('.');
        if(point != -1)
            scale = toString.length() - point - 1;

        return scale;
    }

    /**
    Compute the precision of an object.  This is the
    total number of digits.

    @param  object      A Java object.
    @return             the precision.
    **/
    static int getPrecision(Object value)
    {
        int precision = 0;

        if(value instanceof Boolean)
            precision = 1;

        else
        {
            String toString = value.toString();

            if(toString.charAt(0) == '-')                                  // @F1a
                toString = toString.substring(1);                            // @F1a

            int length = toString.length();

            // We need to truncate any padding zeroes.  Without this,          @E2A
            // the precision of 0.1000 was getting computed as 5 rather        @E2A
            // than 1.                                                      // @E2A
            int startIndex = -1;                                            // @E2A
            int endIndex = length;                                          // @E2A
            int pointIndex = toString.indexOf('.');                         // @E2A
            if(pointIndex >= 0)
            {                                          // @E2A
                while(toString.charAt(++startIndex) == '0');                // @E2A
                while(toString.charAt(--endIndex) == '0');                  // @E2A
            }                                                               // @E2A
            else
            {                                                          // @E2A
                startIndex = 0;                                             // @E2A
                endIndex = length - 1;                                      // @E2A
            }                                                               // @E2A

            // Count the characters that are actually digits.                  @E2A
            for(int i = startIndex; i <= endIndex; ++i)                    // @E2C
                if(Character.isDigit(toString.charAt(i)))
                    ++precision;
        }

        return precision;
    }


    //@DFA
    /**
    Compute the smallest possible precision of a BigDecimal.  This is the
    total number of digits, disregarding the trailing 0's.  Needed so that we can use as
    a comparison for truncation.
    (ie.  9E17 in BigDecimal is represented as BigInt(900000000000000000), but we want
    to calculate precision taking into account that trailing 0's can be represented in an exponent that
    is not possible in the BigDecimal object as a negative scale.)

    @param  value       BigDecimal object.
    @param  maxSize     max size of precision (16 or 34 for decfloats)
    @return             the precision.
    **/
    static int[] getPrecisionForTruncation(BigDecimal value, int maxSize)  //@rnd1
    {
        int precision = 0;

        String toString = value.unscaledValue().toString(); //value.toString(); 1.6 returns "123E+4", 1.4 returns "1230000"

        //int pointIndex = value.scale();//@rnd1 toString.indexOf('.');

        if(toString.charAt(0) == '-')
            toString = toString.substring(1);

        int length = toString.length();

        // We need to truncate any ending zeroes.  Without this,
        // the precision of 1e5 was getting computed as 5 rather
        // than 1.

        int endIndex = length;

        //@rnd1 if(pointIndex != 0)
        //@rnd1     maxSize++;  //allow for extra '.' char
        while((toString.charAt(--endIndex) == '0')  &&  (endIndex > maxSize) );

        int numberZeros = length - endIndex - 1; //@rnd1

        if(endIndex == maxSize)
        {
            if(toString.charAt(endIndex) == '0')
            {//@rnd1
                precision = endIndex;
                numberZeros++; //@rnd1
            }//@rnd1
            else
                precision = endIndex + 1;
        }
        else
            precision = endIndex + 1;

        //@rnd1 if(pointIndex != -1)
        //@rnd1     precision--;


        int[] retVal = new int[2];  //@rnd1
        retVal[0] = precision;      //@rnd1
        retVal[1] = numberZeros;    //@rnd1
        return retVal;              //@rnd1
    }


    /**
    Return a SQLData object corresponding to a
    a SQL type code defined in java.sql.Types.
    In the case where a SQL type code specifies a
    type that is not supported in DB2 for IBM i, then
    it will map to the next closest type.

    @param  sqlType     SQL type code defined in java.sql.Types.
    @param  maxLength   Max length of data.
    @param  precision   Precision of data.
    @param  scale       Scale of data.
    @param  settings    The conversion settings.
    @param  vrm         The OS/400 or IBM i Version, Release, and Modification.
    @return             A SQLData object.

    @exception  SQLException    If no valid type can be
                                mapped.
    **/

    //Note:  this method is not used anywhere.  I am tempted to remove it, but it has a nice history of mapping of sqlTypes to SQLData objects
    static SQLData newData(int sqlType,
                           int maxLength,
                           int precision,
                           int scale,
                           SQLConversionSettings settings,
                           int vrm,
                           JDProperties properties) // @M0A - added the JDProperties parm
    throws SQLException
    {
        switch(sqlType)
        {                                      // @D0C

            case Types.BIGINT:                                      // @D0A
                if(vrm >= JDUtilities.vrm450)   // @D0A
                    return new SQLBigint(vrm, settings);    //trunc3                         // @D0A
                else
                    return new SQLInteger(vrm, settings);    //trunc3

            case Types.BINARY:
            {                                                            // @M0C - changed the code to return a
                if(vrm >= JDUtilities.vrm530)                            // @M0C - SQLBinary for v5r3 and newer
                    return new SQLBinary(maxLength, settings);           // @M0C - only because the old SQLBinary
                else                                                     // @M0C - function has been moved to
                    return new SQLCharForBitData(maxLength, settings);   // @M0C - SQLCharForBitData
            }                                                            // @M0C

            case Types.BLOB:
                return new SQLBlob(maxLength - 4, settings);       // @D1C

            case Types.CHAR:
                return new SQLChar(maxLength, settings);

            case Types.CLOB:
                return new SQLClob(maxLength - 4, settings);    // @D1C @E1C
                //return new SQLClob(maxLength - 4, false, settings);    // @D1C @E1C

            case Types.DATE:
                return new SQLDate(settings, -1);	// @550C

            case Types.DECIMAL:
                if(settings != null)                                           // @E0A
                    if(! settings.useBigDecimal())                             // @E0A
                        return new SQLDecimal2(precision, scale, settings, vrm, properties);  // @M0C - pass the JDProperties so we can get the scale
                return new SQLDecimal(precision, scale, settings, vrm, properties);           // @M0C  // @E0A

            case Types.DOUBLE:
                return new SQLDouble(settings);

            case Types.FLOAT:
                return new SQLFloat(settings);

            case Types.INTEGER:
                return new SQLInteger(vrm, settings);    //trunc3

            case Types.NUMERIC:
                if(settings != null)                                           // @E0A
                    if(! settings.useBigDecimal())                             // @E0A
                        return new SQLNumeric2(precision, scale, settings, vrm, properties);  // @M0C - pass the JDProperties so we can get the scale
                return new SQLNumeric(precision, scale, settings, vrm, properties);           // @M0C  // @E0A

            case Types.REAL:
                return new SQLReal(settings);

            case Types.SMALLINT:
            case Types.TINYINT:                                     // @D0A
            case Types.BIT:                                         // @D0A
                return new SQLSmallint(vrm, settings);    //trunc3

            case Types.TIME:
                return new SQLTime(settings, -1);	// @550C

            case Types.TIMESTAMP:
                return new SQLTimestamp(settings);

            case Types.VARBINARY:
            {
                if(vrm >= JDUtilities.vrm530)
                    return new SQLVarbinary(maxLength, settings);
                else
                    return new SQLVarcharForBitData(maxLength, settings);
            }

            case Types.LONGVARBINARY:                               // @D0A
            {                                                                    // @M0C - changed the code to return a
                if(vrm >= JDUtilities.vrm530)                                    // @M0C - SQLVarbinary for v5r3 and newer
                    return new SQLVarbinary(maxLength, settings);                // @M0C - only because the old SQLVarbinary
                else                                                             // @M0C - function has been moved to
                    return new SQLLongVarcharForBitData(maxLength, settings);    // @M0C - SQLVarcharForBitData
            }                                                                    // @M0C

            case Types.VARCHAR:
                return new SQLVarchar(maxLength, settings);

            case Types.LONGVARCHAR:                                 // @D0A
                return new SQLLongVarchar(maxLength, settings);     // @E1C

            default:
                JDError.throwSQLException(JDError.EXC_DATA_TYPE_INVALID);
                return null;

        }
    }

    /**
    Return a SQLData object corresponding to the
    specific IBM i native type identifier.

    @param  connection      The connection.
    @param  id              The id.
    @param  nativeType      An IBM i native type identifier.
    @param  length          Length of data (in bytes).  For an array, this is the size of the array elements.
    @param  precision       Precision of data.
    @param  scale           Scale of data.
    @param  ccsid           CCSID of data field.
    @param  translateBinary Indicates if binary fields should
                            be translated.
    @param  settings        The conversion settings.
    @param  lobMaxSize      The lob max size.                                      @C3A
    @param  columnIndex     The columnIndex
    @param  dateFormat      The dateFormat
    @param  timeFormat      The timeFormat
    @param  compositeContentType The compositeContentType (type of data in composite type (array, structs or associative-array) NativeType specifies if array or struct etc.
    @return                 A SQLData object.

    @exception  SQLException    If no valid type can be
                                mapped.
    **/
    //@array comment: we are assuming here that all of the metadata parms (except sqlType) is for the array content type
    static SQLData newData(AS400JDBCConnection connection,
                           int id,
                           int nativeType,
                           int length,
                           int precision,
                           int scale,
                           int ccsid,
                           boolean translateBinary,
                           SQLConversionSettings settings,
                           int lobMaxSize,                                 // @C3A
                           int columnIndex,     //@F2A
                           int dateFormat,		// @550A
                           int timeFormat, 		// @550A
                           int compositeContentType,    //@array this corresponds to the nativeType numbering system //@datarray 0 = array element, -1 = nonarray, other = that type of an array.
                           int xmlCharType) //@xml3 SB or DB XML
    throws SQLException
    {
        switch(nativeType)
        {

            case 384:                           // Date.
            { //@datarray
                if(compositeContentType == 0) //@datarray
                    dateFormat = 5;  //@datarray always iso for input and output for arrays of dates due to zda constraint
                return new SQLDate(settings, dateFormat);	// @550
            } //@datarray
            case 388:                           // Time.
                return new SQLTime(settings, timeFormat);

            case 392:                           // Timestamp.
                return new SQLTimestamp(settings);

            case 396:                           // Datalink.
                return new SQLDatalink(length - 2, settings);

            case 404:                           // Blob.
                return new SQLBlob(length - 4, settings);          // @D1C

            case 408:                           // Clob.
                if((ccsid == 65535) && (translateBinary == false))   //@E4C
                    return new SQLBlob(length - 4, settings);      // @D1C
                else
                    return new SQLClob(length - 4, settings); // @D1C @E1C
                //return new SQLClob(length - 4, false, settings); // @D1C @E1C

            case 412:                           // Dbclob.
                return new SQLDBClob(length - 4, settings);    // @D1C
                //return new SQLClob(length - 4, true, settings);    // @D1C

            case 448:                           // Varchar.
                if((ccsid == 65535) && (translateBinary == false))   //@E4C
                    return new SQLVarcharForBitData(length - 2, settings);  // @M0C - changed from SQLVarbinary
                else
                    return new SQLVarchar(length - 2, settings);

            case 456:                           // Varchar long.
                if((ccsid == 65535) && (translateBinary == false))    //@E4C
                    return new SQLLongVarcharForBitData(length - 2, settings);  // @M0C - changed from SQLVarbinary
                else
                    return new SQLLongVarchar(length - 2, settings);

            case 452:                           // Char.
                if((ccsid == 65535) && (translateBinary == false))    //@E4C
                    return new SQLCharForBitData(length, settings);  // @M0C - changed from SQLBinary
                else
                    return new SQLChar(length, settings);

            case 464:                           // Graphic (pure DBCS).
                if(ccsid == 65535)     //@bingra
                    return new SQLVargraphic((length-2)/2, settings, ccsid);  //@bingra
                return new SQLVargraphic(length - 2, settings, ccsid); // @C1C @C4C @cca1

            case 472:                           // Graphic long (pure DBCS).
                return new SQLLongVargraphic(length - 2, settings); // @C1C @C4C

            case 468:                           // Graphic fix (pure DBCS).
                if(ccsid == 65535)     //@bingra
                    return new SQLGraphic(length/2, settings, ccsid);  //@bingra
                return new SQLGraphic(length, settings, ccsid); // @C1C @C4C @cca1

            case 480:                           // Float.
                if(length == 4)
                    return new SQLReal(settings);
                else
                    return new SQLDouble(settings);

            case 484:                           // Packed decimal.
                if(settings != null)                                           // @E0A
                    if(! settings.useBigDecimal())                             // @E0A
                        return new SQLDecimal2(precision, scale, settings, connection.getVRM(), connection.getProperties()); // @M0C - pass the JDProperties object so we can get the precision
                return new SQLDecimal(precision, scale, settings, connection.getVRM(), connection.getProperties());          // @M0C  // @E0A

            case 488:                           // Zoned decimal.
                if(settings != null)                                           // @E0A
                    if(! settings.useBigDecimal())                             // @E0A
                        return new SQLNumeric2(precision, scale, settings, connection.getVRM(), connection.getProperties()); // @M0C - pass the JDProperties object so we can get the precision
                return new SQLNumeric(precision, scale, settings, connection.getVRM(), connection.getProperties());          // @M0C  // @E0A

            case 492:                           // Bigint.   // @D0A
                return new SQLBigint(connection.getVRM(), settings);                      // @D0A //trunc3

            case 496:                           // Integer.
                return new SQLInteger(scale, connection.getVRM(), settings);               // @A0C //trunc3

            case 500:                           // Smallint.
                return new SQLSmallint(scale, connection.getVRM(), settings);              // @A0C //trunc3

            case 904:                          // Rowid.     // @M0A - Added support for the ROWID data type
                return new SQLRowID(settings);               // @M0A

            case 908:                          // Varbinary. // @M0A - added support for VARBINARY type
                return new SQLVarbinary(length-2, settings); // @M0A

            case 912:                          // Binary.    // @M0A - added support for BINARY type
                return new SQLBinary(length, settings);      // @M0A

            case 960:                           // Blob locator.
                return new SQLBlobLocator(connection, id, lobMaxSize, settings, connection.getConverter(ccsid), columnIndex);  //@F2C //@J0M added converter

            case 964:                           // Clob locator.
                if((ccsid == 65535) && (translateBinary == false))               //@E4C
                    return new SQLBlobLocator(connection, id, lobMaxSize, settings, connection.getConverter(ccsid), columnIndex); //@F2C //@J0M added converter
                else
                    return new SQLClobLocator(connection, id, lobMaxSize, settings, connection.getConverter(ccsid), columnIndex); // @E1C //@F2C
                //return new SQLClobLocator(connection, id, lobMaxSize, false, settings, connection.getConverter(ccsid), columnIndex); // @E1C //@F2C

            case 968:                           // Dbclob locator.
                return new SQLDBClobLocator(connection, id, lobMaxSize, settings, connection.getConverter(ccsid), columnIndex); // @E1C //@F2C
                //return new SQLClobLocator(connection, id, lobMaxSize, true, settings, connection.getConverter(ccsid), columnIndex); // @E1C //@F2C

            case 996:                           // Decimal float.  //@DFA
                if(precision == 16) //@DFA
                    return new SQLDecFloat16(settings, connection.getVRM(), connection.getProperties() );     //@DFA
                else
                    return new SQLDecFloat34(settings, connection.getVRM(), connection.getProperties() );     //@DFA
            case SQLData.NATIVE_ARRAY:                                //@array
                return new SQLArray( length, newData( connection,
                                                        id,
                                                        compositeContentType,
                                                        length,
                                                        precision,
                                                        scale,
                                                        ccsid,
                                                        translateBinary,
                                                        settings,
                                                        lobMaxSize,
                                                        columnIndex,
                                                        dateFormat,
                                                        timeFormat,
                                                        0, 0) , connection.getVRM());   //@array   create SQLData array wrapper of actual datatype

            case 2452: //@xml3 xml returned in bloblocator
            case 988:  // the xml type will be seen when a parameter is retrieved from a cached package.  We'll change
            	       // this to an SQLXML Locator 01/27/2010
                if(ccsid == 65535)
                    xmlCharType = 2; //sb=0 or db=1 binary=2
                return new SQLXMLLocator(connection, id, lobMaxSize, settings, connection.getConverter(ccsid), columnIndex, xmlCharType); //@xml3

            default:
                JDError.throwSQLException(JDError.EXC_INTERNAL, new IllegalArgumentException(Integer.toString(nativeType))); // @E3C
                return null;
        }
    }

    /**
    Return a SQLData object corresponding to the
    specific IBM i native type string.

    @param  nativeType  An IBM i native type.
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
    static SQLData newData(String nativeType,
                           int length,
                           int precision,
                           int scale,
                           int ccsid,               //@KKB
                           SQLConversionSettings settings,
                           int vrm,                  // @M0C - added vrm parm
                           JDProperties properties)  // @M0C - added JDProperties parm
    throws SQLException
    {
        if(properties == null)                 //@array7
            properties = new JDProperties();   //@array7
        if(nativeType.equals("BINARY"))
        {                                                           // @M0C - changed to return SQLBinary
            if(vrm >= JDUtilities.vrm530)                           // @M0C - only for v5r3 and newer
                return new SQLBinary(length, settings);             // @M0C - because the original function
            else                                                    // @M0C - from SQLBinary has been moved
                return new SQLCharForBitData(length, settings);     // @M0C - to SQLCharForBitData in order
        }                                                           // @M0C - to add support for real BINARY

        else if(nativeType.equals("BIGINT"))                   // @D0A
            return new SQLBigint(vrm, settings);                             // @D0A //trunc3

        else if(nativeType.equals("BLOB"))
            return new SQLBlob(length, settings);          // @D1C @G1C Remove length-4

        else if(nativeType.equals("BINARY LARGE OBJECT"))  //@KKB
            return new SQLBlob(length, settings);           //@KKB

        else if(nativeType.equals("CHAR"))
        {
            if(ccsid == 65535 && !properties.getBoolean(JDProperties.TRANSLATE_BINARY))                                      //@KKB
                return new SQLCharForBitData(length, settings);     //@KKB
            else                                                    //@KKB
                return new SQLChar(length, settings);
        }

        else if(nativeType.equals("CHARACTE"))
        {
            if(ccsid == 65535 && !properties.getBoolean(JDProperties.TRANSLATE_BINARY))                                      //@KKB
                return new SQLCharForBitData(length, settings);     //@KKB
            else                                                    //@KKB
                return new SQLChar(length, settings);
        }

        else if(nativeType.equals("CHARACTER"))
        {
            if(ccsid == 65535 && !properties.getBoolean(JDProperties.TRANSLATE_BINARY))                                      //@KKB
                return new SQLCharForBitData(length, settings);     //@KKB
            else                                                    //@KKB
                return new SQLChar(length, settings);
        }

        else if(nativeType.equals("CHARACTER VARYING"))
        {
            if(ccsid == 65535 && !properties.getBoolean(JDProperties.TRANSLATE_BINARY))                                      //@KKB
                return new SQLVarcharForBitData(length, settings);      //@KKB
            else                                                        //@KKB
                return new SQLVarchar(length, settings);     // @E1C
        }

        else if(nativeType.equals("CLOB"))
            return new SQLClob(length, settings);           // @D1C @E1C @G1C Remove length-4
        //return new SQLClob(length, false, settings);           // @D1C @E1C @G1C Remove length-4

        else if(nativeType.equals("CHARACTER LARGE OBJECT"))    //@KKB
            return new SQLClob(length, settings);               //@KKB

        else if(nativeType.equals("DATALINK"))
            return new SQLDatalink(length, settings);

        else if(nativeType.equals("DBCLOB"))           // @G2A
            return new SQLDBClob(length, settings); // @G2A
        //return new SQLClob(length, true, settings); // @G2A

        else if(nativeType.equals("DOUBLE-BYTE CHARACTER LARGE OBJECT"))    //@KKB
            return new SQLDBClob(length, settings);                         //@KKB

        else if(nativeType.equals("DATE"))
            return new SQLDate(settings, -1);	// @550

        else if(nativeType.equals("DECIMAL"))
        {
            if(settings != null)                                           // @E0A
                if(! settings.useBigDecimal())                             // @E0A
                    return new SQLDecimal2(precision, scale, settings, vrm, properties); // @M0C - pass the JDProperties so we can get the scale
            return new SQLDecimal(precision, scale, settings, vrm, properties);          // @M0C  // @E0A
        }
        else if(nativeType.equals("DECFLOAT"))  //@DFA
        {                                       //@DFA
            if(precision == 16)                 //@DFA
                return new SQLDecFloat16(settings, vrm, properties);     //@DFA
            else                                                         //@DFA
                return new SQLDecFloat34(settings, vrm, properties);     //@DFA
        }                                                                //@DFA

        else if(nativeType.equals("DOUBLE"))
            return new SQLDouble(settings);

        else if(nativeType.equals("DOUBLE P"))
            return new SQLDouble(settings);

        else if(nativeType.equals("DOUBLE PRECISION"))
            return new SQLDouble(settings);

        else if(nativeType.equals("FLOAT"))
        {
            if(length == 4)
                return new SQLReal(settings);
            else
                return new SQLDouble(settings);
        }

        else if(nativeType.equals("GRAPHIC") || nativeType.equals("NCHAR")) //@j40type
            return new SQLGraphic(length, settings, ccsid); // @C1C @C4C @cca1

        else if(nativeType.equals("GRAPHIC VARYING"))
            return new SQLVargraphic(length, settings, ccsid); // @C1C @C4C @E1C @cca1

        else if(nativeType.equals("INTEGER"))
            return new SQLInteger(vrm, settings);    //trunc3

        else if(nativeType.equals("NUMERIC"))
        {
            if(settings != null)                                           // @E0A
                if(! settings.useBigDecimal())                             // @E0A
                    return new SQLNumeric2(precision, scale, settings, vrm, properties); // @M0C - pass the JDProperties so we can get the scale
            return new SQLNumeric(precision, scale, settings, vrm, properties);          // @M0C  // @E0A
        }

        else if(nativeType.equals("REAL"))
            return new SQLReal(settings);

        else if(nativeType.equals("ROWID"))  // @M0A - added support for the ROWID type
            return new SQLRowID(settings);   // @M0A

        else if(nativeType.equals("SMALLINT"))
            return new SQLSmallint(vrm, settings);    //trunc3

        else if(nativeType.equals("TIME"))
            return new SQLTime(settings, -1);	// @550C

        else if(nativeType.equals("TIMESTAM"))
            return new SQLTimestamp(settings);

        else if(nativeType.equals("TIMESTAMP"))
            return new SQLTimestamp(settings);

        else if(nativeType.equals("TIMESTMP"))
            return new SQLTimestamp(settings);

        else if(nativeType.equals("VARBINARY"))
        {                                                                  // @M0C - changed to return SQLVarbinary
            if(vrm >= JDUtilities.vrm530)                                  // @M0C - only for v5r3 and newer
                return new SQLVarbinary(length, settings);                 // @M0C - because the original function
            else                                                           // @M0C - from SQLVarbinary has been moved
                return new SQLVarcharForBitData(length, settings);         // @M0C - to SQLVarcharForBitData in order
        }                                                                  // @M0C - to add support for real VARBINARY

        else if(nativeType.equals("VARBIN"))                               //@K1A
        {
            if(vrm >= JDUtilities.vrm530)
                return new SQLVarbinary(length, settings);
            else
                return new SQLVarcharForBitData(length, settings);
        }

        else if(nativeType.equals("BINARY VARYING"))                       //@K1A
        {
            if(vrm >= JDUtilities.vrm530)
                return new SQLVarbinary(length, settings);
            else
                return new SQLVarcharForBitData(length, settings);
        }

        else if(nativeType.equals("VARCHAR"))
        {
            if(ccsid == 65535 && !properties.getBoolean(JDProperties.TRANSLATE_BINARY))                                      //@KKB
                return new SQLVarcharForBitData(length, settings);      //@KKB
            else                                                        //@KKB
                return new SQLVarchar(length, settings);     // @E1C
        }

        else if(nativeType.equals("VARG"))
            return new SQLVargraphic(length, settings, ccsid);      // @E1C @cca1

        else if(nativeType.equals("VARGRAPH"))
            return new SQLVargraphic(length, settings, ccsid);      // @E1C @cca1

        else if(nativeType.equals("VARGRAPHIC") || nativeType.equals("NVARCHAR")) //@j40type
            return new SQLVargraphic(length, settings, ccsid); //@KKB @cca1
        else if(nativeType.equals("ARRAY"))
            return new SQLArray( length, newData( nativeType,
                                        length,
                                        precision,
                                        scale,
                                        ccsid,
                                        settings,
                                        vrm,
                                        properties) , vrm);   //@array   create SQLData array wrapper of actual datatype //length is element length

        else
        {
            JDError.throwSQLException(JDError.EXC_INTERNAL, new IllegalArgumentException(nativeType)); // @E3C
            return null;
        }
    }

    /**
    Truncates the precision of a BigDecimal by removing digits from
    the left side of the decimal point.
    **/
    public static BigDecimal truncatePrecision(BigDecimal bd, int precision)
    {
        boolean positive = bd.longValue() > 0;
        StringBuffer buffer = new StringBuffer(positive ? "" : "-");
        buffer.append(bd.toString().substring(positive ? precision : precision + 1));
        return new BigDecimal(buffer.toString());
    }

    /**
    Truncates the precision of a String representation of a number
    by removing digits from the right side of the decimal point.
    **/
    public static String truncateScale(String value, int scale)
    {
        int point = value.indexOf('.');
        if(point >= 0)
        {
            StringBuffer buffer = new StringBuffer(value);
            for(int i = 1; i <= scale; ++i)
                buffer.append('0');
            return buffer.toString().substring(0, point + scale + 1);
        }
        else
            return value;
    }
}
