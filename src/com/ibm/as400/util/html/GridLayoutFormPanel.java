///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: GridLayoutFormPanel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;

/**
*  The GridLayoutFormPanel class represents a grid layout of HTML form elements.
*
*  <p>GridLayoutFormPanel objects generate the following events:
*  <UL>
*  <LI><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*    <li>elementAdded
*    <li>elementRemoved
*    </ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </UL>
*
*  <P>
*  This examples creates a GridLayoutFormPanel object with two columns.
*  <BLOCKQUOTE><PRE>
*  <P>         // Create a text form input element for the system.
*  LabelFormElement sysPrompt = new LabelFormElement("System:");
*  TextFormInput system = new TextFormInput("System");
*  <P>         // Create a text form input element for the userId.
*  LabelFormElement userPrompt = new LabelFormElement("User:");
*  TextFormInput user = new TextFormInput("User");
*  <P>         // Create a password form input element for the password.
*  LabelFormElement passwordPrompt = new LabelFormElement("Password:");
*  PasswordFormInput password = new PasswordFormInput("Password");
*  <P>         // Create the GridLayoutFormPanel object with two columns and add the form elements.
*  GridLayoutFormPanel panel = new GridLayoutFormPanel(2);
*  panel.addElement(sysPrompt);
*  panel.addElement(system);
*  panel.addElement(userPrompt);
*  panel.addElement(user);
*  panel.addElement(passwordPrompt);
*  panel.addElement(password);
*  <P>         // Create the submit button to the form.
*  SubmitFormInput logonButton = new SubmitFormInput("logon", "Logon");
*  <P>         // Create HTMLForm object and add the panel to it.
*  HTMLForm form = new HTMLForm(servletURI);
*  form.addElement(panel);
*  form.addElement(logonButton);
*  </PRE></BLOCKQUOTE>
**/
public class GridLayoutFormPanel extends LayoutFormPanel
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private int columns_;            // The number of columns in the layout.

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A
    private String[] columnDir_; // The direction of the text interpretation used on each cell for a given table column.
    private String[] columnAlign_; // The alignment used on each cell for a given table column.
    private String align_; // The alignment used on the overall table.


    /**
    *  Constructs a default GridLayoutFormPanel with one column.
    **/
    public GridLayoutFormPanel()
    {
        super();
        columns_ = 1;
        columnDir_ = new String[columns_];
        columnAlign_ = new String[columns_];
    }

    /**
    *  Constructs a GridLayoutFormPanel with the specified number of <i>columns</i>.
    *  @param columns The number of columns.
    **/
    public GridLayoutFormPanel(int columns)
    {
        super();
        try
        {
            setColumns(columns);
        }
        catch (PropertyVetoException e)
        {
        }
    }

    /**
    * Adds a PropertyChangeListener.  The specified PropertyChangeListener's
    * <b>propertyChange</b> method will be called each time the value of any
    * bound property is changed.
    *
    * @see #removePropertyChangeListener
    *
    * @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        changes_.addPropertyChangeListener(listener);
    }


    /**
     * Adds the VetoableChangeListener.  The specified VetoableChangeListener's
     * <b>vetoableChange</b> method will be called each time the value of any
     * constrained property is changed.
     *
     * @see #removeVetoableChangeListener
     *
     * @param listener The VetoableChangeListener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        vetos_.addVetoableChangeListener(listener);
    }

    /**
    *  Returns the <i>alignment</i> of the text interpretation.
    *  @return The alignment of the text.
    **/
    public String getAlignment()
    {
        return align_;
    }

    /**
     * Returns the alignment attribute tag.
    **/
    String getAlignmentAttributeTag()
    {
      if (align_ != null && align_.length() > 0)
      {
        return " align=\"" + align_ + "\"";
      }
      return "";
    }

    /**
     * Returns the cell alignment attribute tag for the given column.
    **/
    String getCellAlignmentAttributeTag(int column)
    {
      if (columnAlign_[column] != null && columnAlign_[column].length() > 0)
      {
        return " align=\"" + columnAlign_[column] + "\"";
      }
      return "";
    }

    /**
     * Returns the cell direction attribute tag for the given column.
    **/
    String getCellDirectionAttributeTag(int column)
    {
      if (columnDir_[column] != null && columnDir_[column].length() > 0)
      {
        return " dir=\"" + columnDir_[column] + "\"";
      }
      return "";
    }

    /**
     * Returns the <i>alignment</i> of the text that is used for
     * elements in a specific column in the grid. Use {@link #getAlignment getAlignment}
     * to determine the alignment used for the overall grid.
     * @param column The column for which to retrieve the alignment. The column is 0-based.
     * @return The alignment of the text, or null if one was not set.
     * @see #setColumnAlignment
    **/
    public String getColumnAlignment(int column)
    {
      if (column < 0 || column >= columns_)
      {
        throw new ExtendedIllegalArgumentException("column", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
      }
      return columnAlign_[column];
    }

    /**
     * Returns the <i>direction</i> of the text interpretation that is
     * used for elements in a specific column in the grid. Use {@link #getDirection getDirection}
     * to determine the direction used for the overall grid.
     * @param column The column for which to retrieve the direction. The column is 0-based.
     * @return The direction of the text, or null if one was not set.
     * @see #setColumnDirection
    **/
    public String getColumnDirection(int column)
    {
      if (column < 0 || column >= columns_)
      {
        throw new ExtendedIllegalArgumentException("column", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
      }
      return columnDir_[column];
    }

    /**
    *  Returns the number of columns in the layout.
    *  @return The number of columns.
    **/
    public int getColumns()
    {
        return columns_;
    }


    /**
    *  Returns the <i>direction</i> of the text interpretation.
    *  @return The direction of the text.
    **/
    public String getDirection()                               //$B1A
    {
        return dir_;
    }

    /**
    *  Returns the direction attribute tag.
    *  @return The direction tag.
    **/
    String getDirectionAttributeTag()                                                 //$B1A
    {
        //@C1D

        if ((dir_ != null) && (dir_.length() > 0))
            return " dir=\"" + dir_ + "\"";
        else
            return "";
    }


    /**
    *  Returns the <i>language</i> of the input element.
    *  @return The language of the input element.
    **/
    public String getLanguage()                                //$B1A
    {
        return lang_;
    }


    /**
    *  Returns the language attribute tag.
    *  @return The language tag.
    **/
    String getLanguageAttributeTag()                                                  //$B1A
    {
        //@C1D

        if ((lang_ != null) && (lang_.length() > 0))
            return " lang=\"" + lang_ + "\"";
        else
            return "";
    }


    /**
    *  Returns the grid layout panel tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C1D

        StringBuffer s = new StringBuffer("<table border=\"0\"");                     //$B1C

        s.append(getLanguageAttributeTag());                                          //$B1A
        s.append(getDirectionAttributeTag());                                         //$B1A

        s.append(">\n");                                                              //$B1A

        int index = 0;
        for (int i=0; i< getSize(); i++)
        {
            if (index == 0)
                s.append("<tr>\n");

            HTMLTagElement e = getElement(i);
            s.append("<td");
            s.append(getCellDirectionAttributeTag(index));
            s.append(getCellAlignmentAttributeTag(index));
            s.append(">");
            s.append(e.getTag());
            s.append("</td>\n");

            index++;
            if (index >= columns_)
            {
                s.append("</tr>\n");
                index = 0;
            }
        }

        if (index != 0)
            s.append("</tr>\n");

        s.append("</table>\n");

        return s.toString();
    }

    /**
     * Removes the PropertyChangeListener from the internal list.
     * If the PropertyChangeListener is not on the list, nothing is done.
     *
     * @see #addPropertyChangeListener
     *
     * @param listener The PropertyChangeListener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        changes_.removePropertyChangeListener(listener);
    }


    /**
     * Removes the VetoableChangeListener from the internal list.
     * If the VetoableChangeListener is not on the list, nothing is done.
     *
     * @see #addVetoableChangeListener
     * @param listener The VetoableChangeListener.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        vetos_.removeVetoableChangeListener(listener);
    }

    /**
     * Sets the <i>alignment</i> of the text for the grid.
     * @param align The alignment. Use one of the following constants
     * defined in HTMLConstants:  LEFT, RIGHT, or CENTER.
     * @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setAlignment(String align)
    {
      if (align == null) throw new NullPointerException("align");
      if (!align.equals(HTMLConstants.LEFT) &&
          !align.equals(HTMLConstants.RIGHT) &&
          !align.equals(HTMLConstants.CENTER))
      {
        throw new ExtendedIllegalArgumentException("align", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
      align_ = align;
    }

    /**
     * Sets the <i>alignment</i> of the text interpretation that is used
     * for elements in a specific column in the grid. Use {@link #setAlignment setAlignment}
     * to set the alignment for the overall grid.
     * @param column The column. This value is 0-based.
     * @param align The alignment. One of the following constants
     * defined in HTMLConstants:  LEFT, RIGHT, or CENTER.
     * @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setColumnAlignment(int column, String align)
    {
      if (align == null) throw new NullPointerException("align");
      if (column < 0 || column >= columns_)
      {
        throw new ExtendedIllegalArgumentException("column", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
      }
      // If direction is not one of the valid HTMLConstants, throw an exception.
      if (!align.equals(HTMLConstants.LEFT) &&
          !align.equals(HTMLConstants.RIGHT) &&
          !align.equals(HTMLConstants.CENTER))
      {
        throw new ExtendedIllegalArgumentException("align", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
      columnAlign_[column] = align;
    }

    /**
     * Sets the <i>direction</i> of the text interpretation that is used
     * for elements in a specific column in the grid. Use {@link #setDirection setDirection}
     * to set the direction for the overall grid.
     * @param column The column. This value is 0-based.
     * @param dir The direction. One of the following constants
     * defined in HTMLConstants:  LTR or RTL.
     * @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setColumnDirection(int column, String dir)
    {
      if (dir == null) throw new NullPointerException("dir");
      if (column < 0 || column >= columns_)
      {
        throw new ExtendedIllegalArgumentException("column", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
      }
      // If direction is not one of the valid HTMLConstants, throw an exception.
      if (!dir.equals(HTMLConstants.LTR) && !dir.equals(HTMLConstants.RTL))
      {
        throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
      columnDir_[column] = dir;
    }

    /**
    *  Sets the number of columns in the layout.
    * Note that this will not preserve any column alignment or column direction
    * information that was previously set.
    *  @param columns The number of columns.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setColumns(int columns)
    throws PropertyVetoException
    {
        if (columns < 0)
            throw new ExtendedIllegalArgumentException("columns", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = columns_;
        vetos_.fireVetoableChange("columns", new Integer(old), new Integer(columns) );

        columns_ = columns;
        columnDir_ = new String[columns_]; // Don't preserve the old direction info.
        columnAlign_ = new String[columns_]; // Don't preserve the old alignment info.

        changes_.firePropertyChange("columns", new Integer(old), new Integer(columns) );
    }

    /**
    *  Sets the <i>direction</i> of the text interpretation.
    *  @param dir The direction.  One of the following constants
    *  defined in HTMLConstants:  LTR or RTL.
    *
    *  @see com.ibm.as400.util.html.HTMLConstants
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setDirection(String dir)                                     //$B1A
    throws PropertyVetoException
    {
        if (dir == null)
            throw new NullPointerException("dir");

        // If direction is not one of the valid HTMLConstants, throw an exception.
        if ( !(dir.equals(HTMLConstants.LTR))  && !(dir.equals(HTMLConstants.RTL)) )
        {
            throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        String old = dir_;
        vetos_.fireVetoableChange("dir", old, dir );

        dir_ = dir;

        changes_.firePropertyChange("dir", old, dir );
    }


    /**
    *  Sets the <i>language</i> of the input tag.
    *  @param lang The language.  Example language tags include:
    *  en and en-US.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setLanguage(String lang)                                      //$B1A
    throws PropertyVetoException
    {
        if (lang == null)
            throw new NullPointerException("lang");

        String old = lang_;
        vetos_.fireVetoableChange("lang", old, lang );

        lang_ = lang;

        changes_.firePropertyChange("lang", old, lang );
    }
}
