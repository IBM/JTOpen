///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VObjectHeaderRenderer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;



/**
The VObjectHeaderRenderer class renders the name of an object
using a small icon and text description.
**/
class VObjectHeaderRenderer
extends JButton
implements TableCellRenderer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a VObjectHeaderRenderer object.
**/
    public VObjectHeaderRenderer ()
    {
        this ("", SwingConstants.LEFT);
    }


/**
Constructs a VObjectHeaderRenderer object.

@param text The text for the header
**/
    public VObjectHeaderRenderer (String text)
    {
        this (text, SwingConstants.LEFT);
    }


/**
Constructs a VObjectHeaderRenderer object.

@param horizontalAlignment One of the following SwingConstants: LEFT, RIGHT, or CENTER.
**/
    public VObjectHeaderRenderer (int horizontalAlignment)
    {
        this ("", horizontalAlignment);
    }


/**
Constructs a VObjectHeaderRenderer object.

@param text The text for the header
@param horizontalAlignment One of the following SwingConstants: LEFT, RIGHT, or CENTER.
**/
    public VObjectHeaderRenderer (String text, int horizontalAlignment)
    {
        super (text);
        setHorizontalAlignment (horizontalAlignment);
        setBorder (new CompoundBorder (new BevelBorder(BevelBorder.RAISED), new EmptyBorder (0,5,0,5)));
    }



/**
Renders the value for a table.

@param  table               The table.
@param  value               The value.
@param  selected            true if the item is selected, false otherwise.
@param  hasFocus            true if the item has focus, false otherwise.
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
        setText (value.toString ());
        return this;
    }



}
