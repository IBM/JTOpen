///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceHeaderRenderer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.resource.ResourceMetaData;
import java.awt.Component;
import java.io.Serializable;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;



/**
The ResourceHeaderRenderer class renders the header for
Resource visual components.
**/
class ResourceHeaderRenderer
extends JLabel
implements TableCellRenderer, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static final Border border_ = new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new EmptyBorder (0,5,0,5));

    private         ResourceMetaData    metaData_               = null;



/**
Constructs a ResourceHeaderRenderer object.

@param metaData     The resource meta data which describes the column, 
                    or null if none.
**/
    public ResourceHeaderRenderer(ResourceMetaData metaData)
    {
        metaData_ = metaData;

        setOpaque(true);
        setBorder(border_);

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
        setText(value.toString());
        setFont(table.getFont());
        JTableHeader header = table.getTableHeader();
        setForeground(header.getForeground());
        setBackground(header.getBackground());
        return this;
    }
            

}
