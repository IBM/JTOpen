///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBCellEditor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;



/**
 * Used to be able to edit cell text in updatable SQLResultSetTablePanes.
**/
class DBCellEditor extends DefaultCellEditor
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  static final JTextArea textArea_ = new JTextArea();
  static final JScrollPane scrollPane_ = new JScrollPane(textArea_);

  // Also see the DBCellSelector class.

  static
  {
    textArea_.setEditable(true);
    textArea_.setBackground(Color.white);
    textArea_.setLineWrap(false);

    // The text in the cell scrolls automatically, so we don't need a horizontal bar.
    // Plus, the cell isn't big enough to hold both the scrollbar and the text.    
    scrollPane_.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    // The scroll pane is used for all cells when we are editable.
    // We put the scroll bar there so the user knows that we are scrollable.
    scrollPane_.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  }

  
  /**
   * Calls the DefaultCellEditor constructor with a JTextField.
  **/
  public DBCellEditor()
  {
    super(new JTextField()); // Just so we can get constructed.
  }

  
  /**
   * Returns the value inside the cell. This is just the text inside the JTextArea.
   * This method is needed for the JTable to properly call setValueAt() on the model
   * after a cell is finished being edited.
  **/
  public Object getCellEditorValue()
  {
    return textArea_.getText();
  }


  /**
   * Always use a JTextArea, since someone could paste multi-line text into the cell.
  **/  
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
  {
    // If the text is only one line, then use the regular JTextField;
    // otherwise, use a JTextArea wrapped in a vertically scrollable JScrollPane.
    
    // Swing doesn't like it if the value is null.
    String s = (value == null ? "" : value.toString());
    textArea_.setText(s);
    return scrollPane_;
  }
}


