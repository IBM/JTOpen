///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBDateCellRenderer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;



/**
The DBDateCellRenderer class renders a date using the
current default locale.
Null values are represented using a dash.
**/
class DBDateCellRenderer
extends DBCellRenderer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




// Static data.
private static DateFormat dateFormat_ = DateFormat.getDateInstance ();
private static DateFormat timeFormat_ = DateFormat.getTimeInstance ();
private static DateFormat timestampFormat_ = DateFormat.getDateTimeInstance ();

/**
Data in column is a time.
**/
public static final int FORMAT_TIME = 1;
/**
Data in column is a timestamp.
**/
public static final int FORMAT_TIMESTAMP = 2;
/**
Data in column is a date.
**/
public static final int FORMAT_DATE = 3;

// Instance of this renderer is a date, time, or timestamp.
private int type_;


static
{
    dateFormat_.setTimeZone(TimeZone.getDefault());
    timeFormat_.setTimeZone(TimeZone.getDefault());
    timestampFormat_.setTimeZone(TimeZone.getDefault());
}

/**
Constructs a DBDateCellRenderer object.
**/
public DBDateCellRenderer (int type)
{
    super ();
    type_ = type;
}


/**
Returns the copyright.
**/
private static String getCopyright()
{
    return Copyright_v.copyright;
}



// @C1A
/**
Returns the display size for one of the formatters.
This is useful to size GUIs regardless of locale.
**/
public static int getDisplaySize(int type)
{
    Date sample = new Date();
    switch(type) {
    case FORMAT_DATE:
        return dateFormat_.format(sample).length();
    case FORMAT_TIME:
        return timeFormat_.format(sample).length();
    case FORMAT_TIMESTAMP:
    default:
        return timestampFormat_.format(sample).length();
    }
}


/**
Returns the text representation for this object.

@param value The object for which to get the text representation for.
@return The text representation of the object.
**/
public String getText(Object value)
{
    if (value instanceof Date)
    {
        switch (type_)
        {
            case FORMAT_DATE:
                return dateFormat_.format ((Date) value);
            case FORMAT_TIME:
                return timeFormat_.format ((Date) value);
            default:  // timestamp
                return timestampFormat_.format ((Date) value);
        }
    }
    else if (value != null)
        return value.toString ();
    else
        return "-";
}

}
