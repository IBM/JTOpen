///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLQueryClause.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Dimension;
import javax.swing.JTextPane;


/**
Class used to create a JTextPane that displays a set amount of lines
in the viewport.
**/
class SQLQueryClause
extends JTextPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


private int numRows_;             // Number of lines to display
private int pheight_ = 0;         // Height in pixels of component

/**
Constructs a SQLQueryClause object.
**/
public SQLQueryClause(int rows)
{
    numRows_ = rows;
}



public void appendText(String text)
{
    String oldText = getText().trim();
    if (oldText.length() == 0)
        setText(text);
    else {
        StringBuffer buffer = new StringBuffer(oldText);
        buffer.append(' ');
        buffer.append(text);
        setText(buffer.toString());
    }
}



public void appendTextWithComma(String text)
{
    String oldText = getText().trim();
    if (oldText.length() == 0)
        setText(text);
    else {
        StringBuffer buffer = new StringBuffer(oldText);
        buffer.append(", ");
        buffer.append(text);
        setText(buffer.toString());
    }
}



/**
Returns the preferred size of the viewport for a view component.
@return the preferred size of the viewport for a view component.
**/
public Dimension getPreferredScrollableViewportSize()
{
    if (pheight_ == 0)
    {
        if (getFont() == null)
            return super.getPreferredScrollableViewportSize();
        // Determine the preferred hieght - height of the font
        // times the number of rows+1.
        pheight_ = getFontMetrics(getFont()).getHeight() * (numRows_ + 1);
    }
    return new Dimension(getPreferredSize().width, pheight_);
}


public String getText()
{
    return super.getText().trim();
}


//@B0A: Override setText() to avoid bug # 4183255.
// The bug involves the underlying Document model of a JEditorPane being
// removed when setText() is called. When the Document is removed, our
// listeners don't get notified of any changes in the text. The workaround
// is to call insertString() on the Document directly.
// This is "supposed" to be fixed in JDK 1.2.2.
public void setText(String s)
{
  try
  {
    this.getDocument().remove(0, this.getDocument().getLength());
    this.getDocument().insertString(0, s, null);
  }
  catch(javax.swing.text.BadLocationException e) {}
}
}
