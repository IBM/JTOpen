///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ScrollingTextPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;


/**
The ScrollingTextPane class represents a scrollable text
pane which expands to fill its area.
**/
class ScrollingTextPane
extends JComponent
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    private JTextPane       textPane_;



    public ScrollingTextPane(boolean editable)
    {
        textPane_ = new JTextPane();
        textPane_.setEditable(editable);
        setLayout(new BorderLayout());
        add("Center", new JScrollPane(textPane_));
    }



    public ScrollingTextPane(JTextPane textPane)
    {
        textPane_ = textPane;
        setLayout(new BorderLayout());
        add("Center", new JScrollPane(textPane_));
    }



    public void appendText(String text)
    {
        StringBuffer buffer = new StringBuffer(textPane_.getText());
        buffer.append(text);
        textPane_.setText(buffer.toString());
    }



    public String getText()
    {
        return textPane_.getText();
    }




    public void setText(String text)
    {
        if (text == null)
            textPane_.setText("");
        else
            textPane_.setText(text);
    }



}
