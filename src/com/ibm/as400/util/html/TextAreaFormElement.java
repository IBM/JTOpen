///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: TextAreaFormElement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;

/**
*  The TextAreaFormElement class represents a text area element, which can be used
*  in an HTML <i>form</i>.
*
*  <BLOCKQUOTE><PRE>
*  Here is an example of a TextAreaFormElement tag:
*  &lt;form&gt; 
*    &lt;textarea name=&quot;foo&quot; rows=&quot;3&quot; cols=&quot;40&quot;&gt; 
*    Default TEXTAREA value goes here 
*    &lt;/textarea&gt; 
*  &lt;/form&gt; 
*  </PRE></BLOCKQUOTE>
*
*  <p>TextAreaFormElement objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
**/
public class TextAreaFormElement implements HTMLTagElement, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String name_;            // The text area name.
    private int rows_;               // Number of visible text lines.
    private int cols_;               // Number of visible columns, in average char widths.
    private String text_;            // Initial text.


    transient private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
    transient private VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);

    /**
    *  Constructs a default TextAreaFormElement object.
    **/
    public TextAreaFormElement()
    {
        super();
        rows_ = 0;
        cols_ = 0;
    }      

    /**
    *  Constructs a TextAreaFormElement object with the specified control <i>name</i>.
    *  @param The control name of the text area.
    **/
    public TextAreaFormElement(String name)
    {
       this();
       try {
          setName(name);
       }
       catch (PropertyVetoException e)
       {
       }
    }

    /**
    *  Constructs a TextAreaFormElement object with the specified control <i>name</i>,
    *  number of <i>rows</i>, and <i>columns</i>.
    *  @param name The control name of the text area.
    *  @param rows The number of rows.
    *  @param cols The number of columns.
    **/
    public TextAreaFormElement(String name, int rows, int cols)
    {
        super();
        try {
           setName(name);
           setRows(rows);
           setColumns(cols);
        }
        catch (PropertyVetoException e)
        {
        }
    }

    /**
    Adds a PropertyChangeListener.  The specified PropertyChangeListener's
    <b>propertyChange</b> method will be called each time the value of any
    bound property is changed.
      @see #removePropertyChangeListener
      @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
       if (listener == null)
            throw new NullPointerException ("listener");
       changes_.addPropertyChangeListener(listener);
    }
 
 
    /**
    Adds the VetoableChangeListener.  The specified VetoableChangeListener's
    <b>vetoableChange</b> method will be called each time the value of any
    constrained property is changed.
      @see #removeVetoableChangeListener
      @param listener The VetoableChangeListener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
       if (listener == null)
            throw new NullPointerException ("listener");
       vetos_.addVetoableChangeListener(listener);
    }

    /** 
    *  Returns the number of visible columns in the text area.
    *  @return The number of columns.
    **/
    public int getColumns()
    {
        return cols_;
    }

    /**
    *  Returns the control name of the text area.
    *  @return The control name.
    **/
    public String getName()
    {
        return name_;
    }


    /**
    *  Returns the number of visible rows in the text area.
    *  @return The number of rows.
    **/
    public int getRows()
    {
        return rows_;
    }

    /**
    *  Returns the text area tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "Generating TextAreaFormElement tag...");

        if (name_ == null)
        {
           Trace.log(Trace.ERROR, "Attempting to get tag before setting name.");
           throw new ExtendedIllegalStateException(
               "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<textarea");

        s.append(" name=\"" + name_ + "\"");

        s.append(" rows=\"" + rows_ + "\"");
        s.append(" cols=\"" + cols_ + "\"");

        s.append(">\n");

        if (text_ != null)
            s.append(text_ + "\n");

        s.append("</textarea>");

        return s.toString();
    }

    /**
    *  Returns the initial text of the text area.
    *  @return The initial text.
    **/
    public String getText()
    {
        return text_;
    }

    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)          //$A1A
        throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        changes_ = new PropertyChangeSupport(this);
        vetos_ = new VetoableChangeSupport(this);
    }
    
    /**
    Removes the PropertyChangeListener from the internal list.
    If the PropertyChangeListener is not on the list, nothing is done.
      @see #addPropertyChangeListener
      @param listener The PropertyChangeListener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
       if (listener == null)
            throw new NullPointerException ("listener"); 
       changes_.removePropertyChangeListener(listener);  
    }
 
 
    /**
    Removes the VetoableChangeListener from the internal list.
    If the VetoableChangeListener is not on the list, nothing is done.
      @see #addVetoableChangeListener
      @param listener The VetoableChangeListener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
       if (listener == null)
            throw new NullPointerException ("listener");
       vetos_.removeVetoableChangeListener(listener);
    }

    /**
    *  Sets the number of visible columns in the text area.
    *  @param cols The number of columns.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setColumns(int cols)
      throws PropertyVetoException
    {
        if (cols < 0)
           throw new ExtendedIllegalArgumentException("cols", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = cols_;

        vetos_.fireVetoableChange("cols", new Integer(old), new Integer(cols) );

        cols_ = cols;

        changes_.firePropertyChange("cols", new Integer(old), new Integer(cols) );
    }
    
    /**
    *  Sets the control name of the text area.
    *  @param name The control name.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setName(String name)
      throws PropertyVetoException
    {
        if (name == null)
           throw new NullPointerException("name");

        String old = name_;

        vetos_.fireVetoableChange("name", old, name );

        name_ = name;

        changes_.firePropertyChange("name", old, name );
    }

    /**
    *  Sets the number of visible rows in the text area.
    *  @param rows The number of rows.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setRows(int rows)
      throws PropertyVetoException
    {
        if (rows < 0)
           throw new ExtendedIllegalArgumentException("rows", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = rows_;

        vetos_.fireVetoableChange("rows", new Integer(old), new Integer(rows) );

        rows_ = rows;

        changes_.firePropertyChange("rows", new Integer(old), new Integer(rows) );
    }

    /**
    *  Sets the initial text of the text area.
    *  @param text The initial text.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setText(String text)
      throws PropertyVetoException
    {
        if (text == null)
           throw new NullPointerException("text");

        String old = text_;

        vetos_.fireVetoableChange("text", old, text );

        text_ = text;

        changes_.firePropertyChange("text", old, text );
    }

    /**
    *  Returns a String representation of the text area tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
