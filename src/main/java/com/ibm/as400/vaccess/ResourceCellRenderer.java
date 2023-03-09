///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceCellRenderer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.resource.Presentation;
import com.ibm.as400.resource.Resource;
import com.ibm.as400.resource.ResourceMetaData;
import java.awt.Component;
import java.awt.Image;
import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;



/**
The ResourceCellRenderer class renders the name of a resource
using a small icon and text description.
**/
class ResourceCellRenderer
extends JLabel
implements ListCellRenderer, TableCellRenderer, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static  Border              emptyBorder_            = new EmptyBorder(1, 2, 1, 2);

    private         ResourceMetaData    metaData_               = null;



/**
Constructs a ResourceCellRenderer object.

@param metaData     The resource meta data which describes the column, 
                    or null if none.
**/
    public ResourceCellRenderer(ResourceMetaData metaData)
    {
        metaData_ = metaData;

        setOpaque(true);

        if (metaData != null) {
            if (Number.class.isAssignableFrom(metaData.getType()))                
                setHorizontalAlignment(RIGHT);
            else
                setHorizontalAlignment(LEFT);
        }
        else
            setHorizontalAlignment(LEFT);
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
        setTextAndIcon(value);

        // Handle selection.
        if (selected) {
            setForeground(list.getSelectionForeground());
            setBackground(list.getSelectionBackground());
        }
        else {
            setForeground(list.getForeground());
            setBackground(list.getBackground());
        }

        // Handle focus.
	    if (hasFocus)
            setBorder(new LineBorder(list.getForeground()));
        else
            setBorder(emptyBorder_);

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
    public Component getTableCellRendererComponent(JTable table,
                                                    Object value,
                                                    boolean selected,
                                                    boolean hasFocus,
                                                    int rowIndex,
                                                    int columnIndex)
    {
        setTextAndIcon(value);

        // Handle selection.
        if (selected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        }
        else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }

        // Handle focus.
	    if (hasFocus)
            setBorder(new LineBorder(table.getForeground()));
        else
            setBorder(emptyBorder_);

        return this;
    }



/**
Sets the text and icon.

@param  value       The value.
**/
    private void setTextAndIcon(Object value)
    {
        if (value != null) {

            // If the value is a Resource object and it has a presentation, 
            // then use its presentation name and 16x16 icon.
            if (value instanceof Resource) {
                Resource resource  = (Resource)value;
                Presentation presentation = resource.getPresentation();
                if (presentation != null) {
                    setTextAndIcon(presentation);
                    return;
                }
            }

            // If there is a resource meta data object associated with
            // this renderer and a presentation is available for the value, 
            // then use it to choose the appropriate MRI string.
            if (metaData_ != null) {
                Presentation presentation = metaData_.getPossibleValuePresentation(value);
                if (presentation != null) {
                    setTextAndIcon(presentation);
                    return;
                }
            }
    
            // Otherwise, just toString() it and don't use an icon.
            setText(value.toString());
            setIcon(null);
        }
    }
            


/**
Sets the text and icon.

@param  presentation    The presentation.
**/
    private void setTextAndIcon(Presentation presentation)
    {
        setText(presentation.getName());
        Image icon = (Image)presentation.getValue(Presentation.ICON_COLOR_16x16);
        if (icon != null)
            setIcon(new ImageIcon(icon));
        else
            setIcon(null);
    }
            

}
