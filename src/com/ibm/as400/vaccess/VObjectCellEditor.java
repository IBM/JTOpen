///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VObjectCellEditor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.awt.Component;



/**
The VObjectCellEditor class allows editing of the
name of an object.
**/
class VObjectCellEditor
extends DefaultCellEditor
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a VObjectCellEditor object.
**/
    public VObjectCellEditor ()
    {
        super (new JTextField ());

        // We do not want double clicks to start an edit,
        // since that means invoke the default action.
        setClickCountToStart (100);                         // @C0C
    }



/**
Builds the component as needed.

@param  value               The value.
@param  open                true if the icon should be open,
                            false otherwise.
@param  defaultComponent    The component built by default.
**/
    private Component buildComponent (Object value,
                                      boolean open,
                                      Component defaultComponent)
    {
        JLabel label = new JLabel ();
        if (value instanceof VObject)
            label.setIcon (((VObject) value).getIcon (16, open));
        else
            label.setIcon (null);

        JPanel panel = new JPanel ();
        panel.setLayout (new BorderLayout ());
        panel.add ("West", label);
        panel.add ("Center", defaultComponent);

        return panel;
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Edits the value for a table.

@param  table               The table.
@param  value               The value.
@param  selected            true if the item is selected, false otherwise.
@param  rowIndex            The row index.
@param  columnIndex         The column index.
@return                     The rendered component.
**/
    public Component getTableCellEditorComponent (JTable table,
                                                  Object value,
                                                  boolean selected,
                                                  int rowIndex,
                                                  int columnIndex)
    {
        Component defaultComponent = super.getTableCellEditorComponent (table,
            value, selected, rowIndex, columnIndex);
        return buildComponent (value, false, defaultComponent);
    }



/**
Edits the value for a tree.


@param  tree        The tree.
@param  value       The value.
@param  selected    true if the item is selected, false otherwise.
@param  expanded    true if the item is expanded, false otherwise.
@param  leaf        true if the item is a leaf, false otherwise.
@param  row         The index within the tree.
@return             The rendered component.
**/
    public Component getTreeCellEditorComponent (JTree tree,
                                                 Object value,
                                                 boolean selected,
                                                 boolean expanded,
                                                 boolean leaf,
                                                 int index)
    {
        Component defaultComponent = super.getTreeCellEditorComponent (tree,
            value, selected, expanded, leaf, index);
        return buildComponent (value, selected, defaultComponent);
    }




}
