///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DateFieldDescription.java
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
 *The DateFieldDescription class 
 *represents the description of the data in a date field.
 *It allows:
 *<ul>
 *<li>The user to describe a date field to the RecordFormat object.
 *<li>The RecordFormat object to describe a date field to the user.
 *</ul>
 *Click <a href="doc-files/recordxmp.html">here</a>to see an example.
**/
public class DateFieldDescription extends FieldDescription implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

  // The date format for this field
  private String dateFormat_ = "";
  // The date separator for this field
  private String dateSeparator_ = "";

  /**
   *Constructs a DateFieldDescription object.
  **/
  public DateFieldDescription()
  {
  }

  /**
   *Constructs a DateFieldDescription object. It uses the specified data type and name
   *of the field.
   *The length of the field will be the length specified on the AS400Text object.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public DateFieldDescription(AS400Text dataType, String name)
  {
    super(dataType, name);
  }

  /**
   *Constructs a DateFieldDescription object. It uses the specified data type, name, and
   *DDS name of the field.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
  **/
  public DateFieldDescription(AS400Text dataType, String name, String ddsName)
  {
    super(dataType, name, ddsName);
  }

  
  /**
   *Returns the value specified for the DATFMT keyword for this field.
   *@return The value specified for DATFMT for
   *        this field.  If DATFMT was not specified for this field,
   *        an empty string is returned.
  **/
  public String getDATFMT()
  {
    return dateFormat_;
  }

  /**
   *Returns the value specified for the DATSEP keyword for this field.
   *@return The value specified for DATSEP for
   *        this field.  If DATSEP was not specified for this field,
   *        an empty string is returned.
  **/
  public String getDATSEP()
  {
    return dateSeparator_;
  }

  /**
   *Returns the DDS description for the field.  This is a string containing
   *the description of the field as it would be specified in a DDS source file.
   *This method is used by AS400File.createDDSSourceFile to specify the field
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
    desc.append("     ");  // No length can be specified for a date field
    // Type column (1)
    desc.append("L");
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
    if (!dateFormat_.equals(""))
    {
      v.addElement("DATFMT(" + dateFormat_ + ") ");
    }
    if (!dateSeparator_.equals(""))
    {
      v.addElement("DATSEP('" + dateSeparator_ + "') ");
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
   *Sets the value to specify for the DATFMT keyword for this field.
   *@param dateFormat The value to specify for DATFMT for
   *        this field.  The <i>dateFormat</i> cannot be null.
  **/
  public void setDATFMT(String dateFormat)
  {
    if (dateFormat == null)
    {
      throw new NullPointerException("dateFormat");
    }
    dateFormat_ = dateFormat;
  }

  /**
   *Sets the value to specify for the DATSEP keyword for this field.
   *@param dateSeparator The value to specify for DATSEP for
   *        this field.  The <i>dateSeparator</i> cannot be null.
  **/
  public void setDATSEP(String dateSeparator)
  {
    if (dateSeparator == null)
    {
      throw new NullPointerException("dateSeparator");
    }
    dateSeparator_ = dateSeparator;
  }

  //@B0C - javadoc
  /**
   *Sets the value for the DFT keyword for this field.
   *@param defaultValue The default value for this
   *                   field.  The <i>defaultValue</i>cannot be null.
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
    DFTCurrentValue_ = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
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

}
