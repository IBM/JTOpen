///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBCellRenderer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dimension;



/**
The DBCellRenderer class renders a value by using a string
representation of the value.
Null values are represented using a dash.
**/
class DBCellRenderer
extends DefaultTableCellRenderer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";





/**
Constructs a DBCellRenderer object.
**/
public DBCellRenderer ()
{
    this (SwingConstants.LEFT);
}



/**
Constructs a DBCellRenderer object.

@param horizontalAlignment One of the following SwingConstants: LEFT, RIGHT, or CENTER.
**/
public DBCellRenderer (int horizontalAlignment)
{
    super ();
    setHorizontalAlignment (horizontalAlignment);
    setBorder (new EmptyBorder (0,5,0,5));
}


/**
Returns the copyright.
**/
private static String getCopyright()
{
    return Copyright_v.copyright;
}



/**
Converts a byte array to a string of hex digits.
@param data The data to convert to hex characters.
@return A string containing the hexidecimal representation
of <i>data</i>.
**/
String getHexDigits(byte[] data)
{
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < data.length; i++)
    {
        int leftDigitValue = (data[i] >>> 4) & 0xf;
        int rightDigitValue = data[i] & 0xf;
        char rightDigit = rightDigitValue < 10 ?
          (char) (48 + rightDigitValue) :
          (char) (rightDigitValue - 10 + 65);
        char leftDigit = leftDigitValue < 10 ?
          (char) (48 + leftDigitValue) :
          (char) (leftDigitValue - 10 + 65);
        result.append(leftDigit);
        result.append(rightDigit);
    }
    return result.toString();
}




/**
Renders the value for a table.

@param  table               The table.
@param  value               The value.
@param  selected            true if the item is selected; false otherwise.
@param  hasFocus            true if the item has focus; false otherwise.
@param  rowIndex            The row index.
@param  columnIndex         The column index.
@return                     The rendered component.
**/
public Component getTableCellRendererComponent (JTable table,
                                                Object value,
                                                boolean selected,
                                                boolean hasFocus,
                                                int rowIndex,
                                                int columnIndex)
{
    // Note that subclasses count on this method calling getText().
    String v = getText(value);
    setText (v);
    return super.getTableCellRendererComponent(
        table, v, selected, hasFocus, rowIndex, columnIndex);
}



/**
Returns the text representation for this object.

@param value The object for which to get the text representation for.
@return The text representation of the object.
**/
public String getText(Object value)
{
    if (value == null)
    {
        return "-";
    }
    else if (value instanceof byte[])
    {
        return getHexDigits((byte[])value);
    }
    return value.toString();
}

}
