///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VObjectCellRenderer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.Component;
import java.awt.Dimension;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;



/**
The VObjectCellRenderer class renders the name of an object
using a small icon and text description.
**/
class VObjectCellRenderer
extends JLabel
implements ListCellRenderer, TableCellRenderer, TreeCellRenderer, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static DateFormat                   dateTimeFormat_         = DateFormat.getDateTimeInstance (); //@A1C
    private static DateFormat                   dateFormat_             = DateFormat.getDateInstance (); //@A1A
    private static DateFormat                   timeFormat_             = DateFormat.getTimeInstance (); //@A1A
    private static EmptyBorder                  emptyBorder_            = new EmptyBorder (1, 2, 1, 2);
    private static EmptyBorder                  emptyBorder2_           = new EmptyBorder (0, 5, 0, 5);



/**
Static initializer.
**/
    static
    {
        dateTimeFormat_.setTimeZone (TimeZone.getDefault ()); //@A1C
        dateFormat_.setTimeZone (TimeZone.getDefault ()); //@A1A
        timeFormat_.setTimeZone (TimeZone.getDefault ()); //@A1A
    }



/**
Constructs a VObjectCellRenderer object.
**/
    public VObjectCellRenderer ()
    {
        initialize ();
        setHorizontalAlignment (SwingConstants.LEFT);
    }



/**
Constructs a VObjectCellRenderer object.

@param horizontalAlignment One of the following SwingConstants: LEFT, RIGHT, or CENTER.
**/
    public VObjectCellRenderer (int horizontalAlignment)
    {
        initialize ();
        setHorizontalAlignment (horizontalAlignment);
    }



/**
Renders the value for a list.

@param  list        The list.
@param  value       The value.
@param  rowIndex    The row index.
@param  selected    true if the item is selected, false otherwise.
@param  hasFocus    true if the item has focus, false otherwise.
@return             The rendered component.
**/
    public Component getListCellRendererComponent (JList list,
                                                   Object value,
                                                   int rowIndex,
                                                   boolean selected,
                                                   boolean hasFocus)
    {
        normalize (value, false);

        // Handle selection.
        if (selected) {
            setForeground (list.getSelectionForeground ());
            setBackground (list.getSelectionBackground ());
        }
        else {
            setForeground (list.getForeground ());
            setBackground (list.getBackground ());
        }

        // Handle focus.
	    if (hasFocus)
            setBorder (new LineBorder (list.getForeground ()));
        else
            setBorder (emptyBorder_);

        return this;
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
        normalize (value, false);

        // Handle selection.
        if (selected) {
            setForeground (table.getSelectionForeground ());
            setBackground (table.getSelectionBackground ());
        }
        else {
            setForeground (table.getForeground ());
            setBackground (table.getBackground ());
        }

        // Handle focus.
	    if (hasFocus)
            setBorder (new LineBorder (table.getForeground ()));
        else
            setBorder (emptyBorder_);

        return this;
    }



/**
Renders the value for a tree.


@param  tree        The tree.
@param  value       The value.
@param  selected    true if the item is selected, false otherwise.
@param  expanded    true if the item is expanded, false otherwise.
@param  leaf        true if the item is a leaf, false otherwise.
@param  row         The index within the tree.
@param  hasFocus    true if the item has focus, false otherwise.
@return             The rendered component.
**/
    public Component getTreeCellRendererComponent (JTree tree,
                                                   Object value,
                                                   boolean selected,
                                                   boolean expanded,
                                                   boolean leaf,
                                                   int index,
                                                   boolean hasFocus)
    {
        normalize (value, false);

        // Handle selection.
        if (selected) {
            setForeground (tree.getBackground ());
            setBackground (tree.getForeground ());

            // Handle focus.
    	    if (hasFocus)
                setBorder (new LineBorder (tree.getForeground ()));
            else
                setBorder (emptyBorder_);
        }
        else {
            setBorder (emptyBorder_);
            setForeground (tree.getForeground ());
            setBackground (tree.getBackground ());
        }

        return this;
    }



/**
Initializes the component.
**/
    private void initialize ()
    {
        setBorder (emptyBorder2_);
        setOpaque (true);
    }



/**
Normalizes the component.

@param  value       The value.
@param  open        true if the icon should be open, false otherwise.
**/
    private void normalize(Object value, boolean open)
    {
        // If its a VObject, then set the text and icon.
        if (value instanceof VObject)
        {
            VObject object = (VObject)value;
            setText (object.getText ());
            setIcon (object.getIcon (16, open));
        }

        //@A1A
        else if (value instanceof java.sql.Date) {
            setText (dateFormat_.format ((java.util.Date) value));
            setIcon (null);
        }

        //@A1A
        else if (value instanceof java.sql.Time) {
            setText (timeFormat_.format ((java.util.Date) value));
            setIcon (null);
        }

        // If its a date, then format it based on the locale.
        else if (value instanceof java.util.Date) { //@A1C
            setText (dateTimeFormat_.format ((java.util.Date) value)); //@A1C
            setIcon (null);
        }

        //@A2A
        // If it's a name cell in a Permission table, handle it.
        else if (value instanceof PermissionNameCellObject)
	{
            setText(((PermissionNameCellObject)value).getText());
            setIcon(((PermissionNameCellObject)value).getIcon(16, open));
	}

        // Otherwise, set the text using toString().
        else if (value != null) {
            setText (value.toString ());
            setIcon (null);
        }

        // Handle a null value.
        else {
            setText (null);
            setIcon (null);
        }
    }
}
