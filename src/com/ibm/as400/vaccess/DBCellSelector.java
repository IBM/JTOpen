///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBCellSelector.java
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
 * Used to be able to select cell text in non-updatable SQLResultSetTablePanes.
 * It is not intended to be a typical cell editor. It is only used for selecting cell data, not editing it.
**/
class DBCellSelector extends DefaultCellEditor
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  static final JTextField textField_ = new JTextField();

  static final JTextArea textArea_ = new JTextArea();
  static final JScrollPane scrollPane_ = new JScrollPane(textArea_);

  static
  {
    textField_.setEditable(true); // So we get a cursor. We add a KeyAdapter that consumes alphabetic characters.
    textArea_.setEditable(true);
    textField_.setBackground(Color.white); // For some reason, the default in JDK 1.3 is gray? or transparent?
    textArea_.setBackground(Color.white);
    textArea_.setLineWrap(false);
    
    KeyAdapter adapter = new KeyAdapter()
    {
      public void keyTyped(KeyEvent e)
      {
        if (e.getModifiers() == 0) // A typical copy/paste key should have modifiers; normal keys won't.
        {
          e.consume(); // Don't let the user type data into the cell.
        }
      }
    };
    
    textField_.addKeyListener(adapter);
    textArea_.addKeyListener(adapter);

    // The text in the cell scrolls automatically, so we don't need a horizontal bar.
    // Plus, the cell isn't big enough to hold both the scrollbar and the text.    
    scrollPane_.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    // The scroll pane is only used for text areas which are only used for cells with multi-line values.
    // So we always want a vertical scrollbar.
    scrollPane_.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  
    // Don't want headers or borders, since we are in a cell and don't have extra room for them.
    scrollPane_.setColumnHeader(null);
    scrollPane_.setRowHeader(null);
    scrollPane_.setViewportBorder(null);
  }

  
  /**
   * Calls the DefaultCellEditor constructor with a JTextField.
  **/
  public DBCellSelector()
  {
    super(textField_);
  }

  
  /**
   * Uses the superclass's default cell editor component unless the data value in the cell
   * contains a newline, in which case we use a JTextArea inside a JScrollPane.
  **/  
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
  {
    // If the text is only one line, then use the regular JTextField;
    // otherwise, use a JTextArea wrapped in a vertically scrollable JScrollPane.
    if (value == null) value = ""; // Swing doesn't like it if the value is null.
    Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
    String s = value.toString();
    if (s.indexOf("\n") > -1)
    {
      textArea_.setText(s);
      c = scrollPane_;
    }
    return c;
  }
}


