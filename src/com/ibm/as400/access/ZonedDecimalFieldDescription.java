///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ZonedDecimalFieldDescription.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Vector;

/**
 *The ZonedDecimalFieldDescription class represents the description of the data in a zoned decimal field.
 *The ZonedDecimalFieldDescription class allows:
 *<ul>
 *<li>The user to describe a zoned decimal field to the RecordFormat object.
 *<li>The RecordFormat object to describe a zoned decimal field to the user.
 *</ul>
 *<b><a href="recordxmp.html">Examples</a></b>
**/
public class ZonedDecimalFieldDescription extends FieldDescription implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  static final long serialVersionUID = -7940284323549335189L;
  // Number of decimal positions for this field
  private int decimalPositions_;

  /**
   *Constructs a ZonedDecimalFieldDescription object.
  **/
  public ZonedDecimalFieldDescription()
  {
  }

  /**
   *Constructs a ZonedDecimalFieldDescription object. It uses the data
   *type and name of the field specified.
   *The number of digits and the number of decimal positions will be determined from
   *<i>dataType</i>.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   **/
  public ZonedDecimalFieldDescription(AS400ZonedDecimal dataType, String name)
  {
    super(dataType, name);
    length_ = dataType.getNumberOfDigits();
    decimalPositions_ = dataType.getNumberOfDecimalPositions();
  }

  /**
   *Constructs a ZonedDecimalFieldDescription object. It uses the
   *data type, name, and DDS name of the field specified.
   *The number of digits and the number of decimal positions will be determined from
   *<i>dataType</i>.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   **/
  public ZonedDecimalFieldDescription(AS400ZonedDecimal dataType, String name, String ddsName)
  {
    super(dataType, name, ddsName);
    length_ =  dataType.getNumberOfDigits();
    decimalPositions_ = dataType.getNumberOfDecimalPositions();
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
    StringBuffer len = new StringBuffer(new Integer(length_).toString());
    if (len.length() < 5)
    {
      int blanksNeeded = 5 - len.length();
      for (short i = 0; i < blanksNeeded; ++i)
      {
        len.insert(0, " ");
      }
    }
    // Length columns (5)
    desc.append(len.toString());
    // Type column (1)
    desc.append("S");
    // Decimal positions columns (2)
    StringBuffer decPos = new StringBuffer(new Integer(decimalPositions_).toString());
    if (decPos.length() == 1)
    {
      decPos.insert(0, " ");
    }
    desc.append(decPos.toString());
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
    // Add field type specific keywords
    if (defaultValue_ != null)
    {
      v.addElement("DFT(" + defaultValue_.toString() + ") ");
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
   *Returns the number of digits after the decimal point for this field.
   *@return The number of digits after the decimal point.
  **/
  public int getDecimalPositions()
  {
    return decimalPositions_;
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400ZonedDecimal dataType)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    dataType_ = dataType;
    length_ = dataType.getNumberOfDigits();
    decimalPositions_ = dataType.getNumberOfDecimalPositions();
  }

  //@B0C - javadoc
  /**
   *Sets the value for the DFT keyword for this field.
   *@param defaultValue The default value for this
   *                   field.  The <i>defaultValue</i> cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(BigDecimal defaultValue)
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
   *Sets the value for the DFT keyword to be *NULL for this field.
   *Calling this method will replace the DFT keyword that was previously
   *set on a call to setDFT(). Note: This field
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
