///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLConversionSettings.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;



/**
This class keeps track of the current data formatting options.
Storing these in a single object not only makes it more convenient
to pass around, but also enables the settings to be changed
on-the-fly and be reflected in existing data.
**/
class SQLConversionSettings
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Formatting constants.  Using constants makes
    // quick comparisons and switch-case statements easier.
    // All constant values correspond directly to the index
    // of the string in the JDProperties class.
    public static final int DATE_FORMAT_JULIAN          = 0;
    public static final int DATE_FORMAT_MDY             = 1;
    public static final int DATE_FORMAT_DMY             = 2;
    public static final int DATE_FORMAT_YMD             = 3;
    public static final int DATE_FORMAT_USA             = 4;
    public static final int DATE_FORMAT_ISO             = 5;
    public static final int DATE_FORMAT_EUR             = 6;
    public static final int DATE_FORMAT_JIS             = 7;

    public static final int TIME_FORMAT_HMS             = 0;
    public static final int TIME_FORMAT_USA             = 1;
    public static final int TIME_FORMAT_ISO             = 2;
    public static final int TIME_FORMAT_EUR             = 3;
    public static final int TIME_FORMAT_JIS             = 4;




    // Private data.
    private int                 dateFormat_;
    private String              dateSeparator_;
    private String              decimalSeparator_;
    private int                 maxFieldSize_;
    private int                 timeFormat_;
    private String              timeSeparator_;
    private boolean             useBigDecimal_;                     // @E0A
    private int                 bidiStringType_;                    // @E1A
    private boolean             bidiImplicitReordering_;            // @KBA
    private boolean             bidiNumericOrdering_;               // @KBA
    private boolean             translateBoolean_;                  // @PDA



/**
Constructs a SQLConversionSettings object.

@param  connection  Connection to the server.
**/
    SQLConversionSettings (AS400JDBCConnection connection)
        throws SQLException
    {
        JDProperties properties = connection.getProperties ();

        dateFormat_         = properties.getIndex (JDProperties.DATE_FORMAT);
        dateSeparator_      = properties.getString (JDProperties.DATE_SEPARATOR);
        decimalSeparator_   = properties.getString (JDProperties.DECIMAL_SEPARATOR);
        timeFormat_         = properties.getIndex (JDProperties.TIME_FORMAT);
        timeSeparator_      = properties.getString (JDProperties.TIME_SEPARATOR);
	bidiStringType_     = getInt(properties.getString (JDProperties.BIDI_STRING_TYPE)); // @E1A
        bidiImplicitReordering_ = properties.getBoolean(JDProperties.BIDI_IMPLICIT_REORDERING); //@KBA
        bidiNumericOrdering_ = properties.getBoolean(JDProperties.BIDI_NUMERIC_ORDERING);   //@KBA

        if (dateSeparator_.equalsIgnoreCase (JDProperties.DATE_SEPARATOR_SPACE))
            dateSeparator_ = " ";
        if (timeSeparator_.equalsIgnoreCase (JDProperties.TIME_SEPARATOR_SPACE))
            timeSeparator_ = " ";

        maxFieldSize_       = 0;
        useBigDecimal_      = properties.getBoolean(JDProperties.BIG_DECIMAL);          // @E0A
        translateBoolean_   = properties.getBoolean(JDProperties.TRANSLATE_BOOLEAN);    // @PDA
    }


    //@E1A
    /**
    Get int value of bidiString property which is a string.  Return -1 if empty string 
    (property not set) since 0 is BidiStringType.DEFAULT.
    
    @return The int value of a string bidiString property.
    **/
    int getInt (String value)
    {
	try {                                                               
	    return Integer.parseInt (value);
	}                                                                   
	catch (NumberFormatException e) {                                   
	    return -1;                                                       
	}                                                                   
    }


    //@E1A
    /** 
    Returns the bidi string type.
    
    @return The bidi string type.
    **/
    int getBidiStringType ()
    {
	return bidiStringType_;
    }

    //@KBA
    boolean getBidiImplicitReordering()
    {
        return bidiImplicitReordering_;
    }

    //@KBA
    boolean getBidiNumericOrdering()
    {
        return bidiNumericOrdering_;
    }
    
    //@PDA
    boolean getTranslateBoolean()
    {
        return translateBoolean_;
    }


/**
Returns the current date format.

@return     The date format.
**/
    int getDateFormat ()
    {
        return dateFormat_;
    }



/**
Returns the current date separator.

@return     The date separator.
**/
    String getDateSeparator ()
    {
        return dateSeparator_;
    }



/**
Returns the current decimal separator.

@return     The decimal separator.
**/
    String getDecimalSeparator ()
    {
        return decimalSeparator_;
    }



/**
Returns the current max field size.

@return     The max field size (in bytes).
**/
    int getMaxFieldSize ()
    {
        return maxFieldSize_;
    }



/**
Returns the current time format.

@return     The time format.
**/
    int getTimeFormat ()
    {
        return timeFormat_;
    }



/**
Returns the current time separator.

@return     The time separator.
**/
    String getTimeSeparator ()
    {
        return timeSeparator_;
    }



/**
Sets the current max field size.

@param  maxFieldSize     The maximum field size
                         (in bytes).
**/
    void setMaxFieldSize (int maxFieldSize)
    {
        maxFieldSize_ = maxFieldSize;
    }


// @E0A
/**
Indicates if packed/zoned decimal conversions should
use a BigDecimal.

@return true of packed/zoned decimal conversions should
        use a BigDecimal, false otherwise.
**/
    boolean useBigDecimal()
    {
        return useBigDecimal_;
    }


}

