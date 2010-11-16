///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: TimeFieldDescription.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.util.Vector;
//@B0A
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *Represents the description of the data in a time field.
 *The TimeFieldDescription class allows:
 *<ul>
 *<li>The user to describe a time field to the RecordFormat object.
 *<li>The RecordFormat object to describe a time field to the user.
 *</ul>
 *click <a href="doc-files/recordxmp.html">here</a> to see an example.
**/
public class TimeFieldDescription extends FieldDescription implements Serializable
{
    static final long serialVersionUID = 4L;
  // The time format for this field
  private String timeFormat_ = null;
  // The time separator for this field
  private String timeSeparator_ = null;

  /**
   *Constructs a TimeFieldDescription object.
  **/
  public TimeFieldDescription()
  {
  }

  /**
   *Constructs a TimeFieldDescription object. It uses the data type
   *and name of the field specified.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   **/
  public TimeFieldDescription(AS400Text dataType, String name)
  {
    super(dataType, name);
  }

  /**
   *Constructs a TimeFieldDescription object.  It uses the data type,
   *name, and DDS name of the field specified.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   **/
  public TimeFieldDescription(AS400Text dataType, String name, String ddsName)
  {
    super(dataType, name, ddsName);
  }

  /**
   *Constructs a TimeFieldDescription object. It uses the data type
   *and name of the field specified.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   **/
  public TimeFieldDescription(AS400Time dataType, String name)
  {
    super(dataType, name);
  }

  /**
   *Constructs a TimeFieldDescription object.  It uses the data type,
   *name, and DDS name of the field specified.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   **/
  public TimeFieldDescription(AS400Time dataType, String name, String ddsName)
  {
    super(dataType, name, ddsName);
  }

  
  /**
   *Returns the DDS description for the field.  This is a string containing
   *the description of the field as it would be specified in a DDS source file.
   *This method is used by AS400File.createDDSSourceFile (called by the AS400File.create methods)
   *to specify the field
   *in the DDS source file which is used to create the file for the user who
   *has passed in a RecordFormat object.
   *@return The DDS description of this field properly formatted for entry
   *into a DDS source file.
  **/
  String[] getDDSDescription()
  {
    Vector v = new Vector();
    // Name columns (10)
    StringBuffer desc = new StringBuffer(ddsName_);
    // Blank pad the ddsName to 10 characters.
    while(desc.length() < 10)
    {
      desc.append(" ");
    }
    // Reference column (1)
    if (!refFld_.equals(""))
    {
      desc.append("R");
    }
    else
    {
      desc.append(" ");
    }
    // Get length as 5 digit string, right justified
    // Length columns (5)
    desc.append("     ");  // No length can be specified for a time field
    // Type column (1)
    desc.append("T");
    // Decimal positions columns (2)
    desc.append("  ");
    // Not used columns (7)
    desc.append("       ");
    // Add fixed portion of DDS description to Vector
    v.addElement(desc.toString());
    // Add the field level keywords
    String[] keywords = super.getFieldFunctions();
    if (keywords != null)
    {
      for (int i = 0; i < keywords.length; ++i)
      {
        v.addElement(keywords[i]);
      }
    }
    if (timeFormat_ != null)
    {
      v.addElement("TIMFMT(" + timeFormat_ + ") ");
    }
    if (timeSeparator_ != null)
    {
      if (!formatHasFixedSeparator(timeFormat_)) {
        v.addElement("TIMSEP('" + timeSeparator_ + "') ");
      }
      else {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DDS time format " + timeFormat_ + " has a fixed separator.");
      }
    }
    if (defaultValue_ != null)
    {
      v.addElement("DFT('" + defaultValue_.toString() + "') ");
    }
    //@B0A
    else if (isDFTNull_)
    {
      v.addElement("DFT(*NULL) ");
    }
    String[] s = new String[v.size()];
    v.copyInto(s);
    return s;
  }

  private static boolean formatHasFixedSeparator(String format)
  {
    // Both DATSEP and TIMSEP have the same lists of "fixed-separator" formats.
    return DateFieldDescription.formatHasFixedSeparator(format);
  }

  /**
   *Returns the value specified for the TIMFMT keyword for this field.
   *@return The value specified for TIMFMT for
   *        this field.  If TIMFMT was not specified for this field,
   *        an empty string  is returned.
  **/
  public String getTIMFMT()
  {
    return (timeFormat_ == null ? "" : timeFormat_);
  }

  /**
   *Returns the value specified for the TIMSEP keyword for this field.
   *@return The value specified for TIMSEP for
   *        this field.  If TIMSEP was not specified for this field,
   *        an empty string is returned.
  **/
  public String getTIMSEP()
  {
    return (timeSeparator_ == null ? "" : timeSeparator_);
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400Text dataType)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    dataType_ = dataType;
    // Set the length of the field based on the data type
    length_ = dataType.getByteLength();
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400Time dataType)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    dataType_ = dataType;
    // Set the length of the field based on the data type
    length_ = dataType.getByteLength();
  }

  //@B0C - javadoc
  /**
   *Sets the value for the DFT keyword for this field.
   *@param defaultValue The default value for this
   *                   field.  The <i>defaultValue</i> cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(String defaultValue)
  {
    if (defaultValue == null)
    {
      throw new NullPointerException("defaultValue");
    }
    defaultValue_ = defaultValue;
    isDFTNull_ = false; //@B0A
    isDFTCurrent_ = false; //@B0A
    DFTCurrentValue_ = null; //@B0A
  }

  //@B0A
  /**
   *Sets the default value for this field to be the current date.
   *Calling this method will replace the DFT keyword that was previously
   *set on a call to setDFT(String) or setDFTNull().
  **/
  public void setDFTCurrent()
  {
    isDFTCurrent_ = true;
    isDFTNull_ = false;
    defaultValue_ = null;
    DFTCurrentValue_ = (new SimpleDateFormat("HH.mm.ss")).format(new Date());
  }

  //@B0A
  /**
   *Sets the value for the DFT keyword to be *NULL for this field.
   *Calling this method will replace the DFT keyword that was previously
   *set on a call to setDFT(String) or setDFTCurrent(). Note: This field
   *must also have its ALWNULL keyword set to true to prevent DDS errors.
  **/
  public void setDFTNull()
  {
    isDFTNull_ = true;
    defaultValue_ = null;
    isDFTCurrent_ = false;
    DFTCurrentValue_ = null;
  }

  /**
   *Sets the value to specify for the TIMFMT keyword for this field.
   *@param timeFormat The value to specify for TIMFMT for
   *        this field.  The <i>timeFormat</i> cannot be null.
  **/
  public void setTIMFMT(String timeFormat)
  {
    if (timeFormat == null) throw new NullPointerException("timeFormat");

    if (timeFormat.startsWith("*")) {
      timeFormat_ = timeFormat.toUpperCase();
    }
    else {
      timeFormat_ = "*" + timeFormat.toUpperCase();
    }

    // Inform the AS400Time object of the format.
    if (dataType_ instanceof AS400Time) {
      ((AS400Time)dataType_).setFormat(timeFormat);
    }
  }

  /**
   *Sets the value to specify for the TIMSEP keyword for this field.
   *@param separator The value to specify for TIMSEP for this field.
   *A null value indicates "no separator".
  **/
  public void setTIMSEP(String separator)
  {
    // Inform the AS400Time object of the separator.
    if (dataType_ instanceof AS400Time)
    {
      if (separator != null && separator.length() > 1) {
        throw new ExtendedIllegalArgumentException("separator (" + separator + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      }
      Character sep = ( separator == null ? null : new Character(separator.charAt(0)));
      ((AS400Time)dataType_).setSeparator(sep);
    }
    timeSeparator_ = separator;
  }
}
