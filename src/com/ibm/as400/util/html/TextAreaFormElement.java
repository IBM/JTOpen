///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: TextAreaFormElement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
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
public class TextAreaFormElement extends HTMLTagAttributes implements java.io.Serializable   // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private String name_;            // The text area name.
    private int rows_;               // Number of visible text lines.
    private int cols_;               // Number of visible columns, in average char widths.
    private String text_;            // Initial text.

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A


    transient private VetoableChangeSupport vetos_; //@CRS

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
        try
        {
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
        try
        {
            setName(name);
            setRows(rows);
            setColumns(cols);
        }
        catch (PropertyVetoException e)
        {
        }
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
        if (vetos_ == null) vetos_ = new VetoableChangeSupport(this); //@CRS
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
        {
            StringBuffer buffer = new StringBuffer(" dir=\"");
            buffer.append(dir_);
            buffer.append("\"");

            return buffer.toString();
        }
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
        {
            StringBuffer buffer = new StringBuffer(" lang=\"");
            buffer.append(lang_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
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
        //@C1D

        if (name_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting name.");
            throw new ExtendedIllegalStateException(
                                                   "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<textarea");

        s.append(" name=\"");
        s.append(name_);
        s.append("\"");

        s.append(" rows=\"");
        s.append(rows_);
        s.append("\"");

        s.append(" cols=\"");
        s.append(cols_);
        s.append("\"");

        s.append(getLanguageAttributeTag());                                          //$B1A
        s.append(getDirectionAttributeTag());                                         //$B1A
        s.append(getAttributeString());                                               // @Z1A

        s.append(">\n");

        if (text_ != null)
            s.append(text_);
            // @C2D    

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

        //@CRS changes_ = new PropertyChangeSupport(this);
        //@CRS vetos_ = new VetoableChangeSupport(this);
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
        if (vetos_ != null) vetos_.removeVetoableChangeListener(listener); //@CRS
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

        if (vetos_ != null) vetos_.fireVetoableChange("cols", new Integer(old), new Integer(cols) ); //@CRS

        cols_ = cols;

        if (changes_ != null) changes_.firePropertyChange("cols", new Integer(old), new Integer(cols) ); //@CRS
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
        if (vetos_ != null) vetos_.fireVetoableChange("dir", old, dir ); //@CRS

        dir_ = dir;

        if (changes_ != null) changes_.firePropertyChange("dir", old, dir ); //@CRS
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
        if (vetos_ != null) vetos_.fireVetoableChange("lang", old, lang ); //@CRS

        lang_ = lang;

        if (changes_ != null) changes_.firePropertyChange("lang", old, lang ); //@CRS
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

        if (vetos_ != null) vetos_.fireVetoableChange("name", old, name ); //@CRS

        name_ = name;

        if (changes_ != null) changes_.firePropertyChange("name", old, name ); //@CRS
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

        if (vetos_ != null) vetos_.fireVetoableChange("rows", new Integer(old), new Integer(rows) ); //@CRS

        rows_ = rows;

        if (changes_ != null) changes_.firePropertyChange("rows", new Integer(old), new Integer(rows) ); //@CRS
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

        if (vetos_ != null) vetos_.fireVetoableChange("text", old, text ); //@CRS

        text_ = text;

        if (changes_ != null) changes_.firePropertyChange("text", old, text ); //@CRS
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
