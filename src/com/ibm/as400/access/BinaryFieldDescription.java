///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: BinaryFieldDescription.java
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

/**
 *The BinaryFieldDescription class represents the description of the data in a binary (integer) field.
 *It allows:
 *<ul>
 *<li>The user to describe a binary field to the <a href="com.ibm.as400.access.RecordFormat.html">RecordFormat</a> object.
 *<li>The RecordFormat object to describe a binary field to the user.
 *</ul>
 *<b><a href="recordxmp.html">Examples</a></b>
 **/
public class BinaryFieldDescription extends FieldDescription implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  static final long serialVersionUID = -2040945751371810257L;
  /**
   *Constructs a BinaryFieldDescription object.
  **/
  public BinaryFieldDescription()
  {
  }

  /**
   *Constructs a BinaryFieldDescription object. It uses the specified data type and name
   *of the field.
   *The length of this field is represented by the number of digits it can contain.
   *This constructor defaults the length (as returned by getLength()) to 9.
   *The length is used by the Record Level Access classes when creating a file from
   *a RecordFormat object. Nine (9) is the maximum number of digits allowed for a binary field by DDS.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public BinaryFieldDescription(AS400Bin4 dataType, String name)
  {
    // When no length is specified, we pass length of 9 in case field will be
    // used for DDS.  If this is the case, the length represents the number of
    // digits in the field, not the byte length of the field.
    super(dataType, name);
    length_ = 9;

  }

  /**
   *Constructs a BinaryFieldDescription object. It uses the specified data type, name,
   *DDS name, and length of the field.  This constructor is used when the field description will
   *be used with the record level access classes.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   *@param length The number of digits that the field will hold.  This is the length of
   *              the field as it would appear in a DDS description.  The <i>length</i>
   *              must be greater than 0.
  **/
  public BinaryFieldDescription(AS400Bin4 dataType, String name, String ddsName, int length)
  {
    super(dataType, name, ddsName);
    if (length < 1)
    {
      throw new ExtendedIllegalArgumentException("length (" + String.valueOf(length) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    length_ = length;
  }

  /**
   *Constructs a BinaryFieldDescription object. It uses the specified data type and name of the field.
   *The length of this field is represented by the number of digits it can contain.
   *This constructor defaults the length (as returned by getLength()) to 4.
   *The length is used by the Record Level Access classes when creating a file from
   *a RecordFormat object. Four (4) is the maximum number of digits allowed for a binary field
   *(when represented by a bin2) by DDS.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public BinaryFieldDescription(AS400Bin2 dataType, String name)
  {
    // When no length is specified, we pass length of 4 in case field will be
    // used for DDS.  If this is the case, the length represents the number of
    // digits in the field, not the byte length of the field.
    super(dataType, name);
    length_ = 4;     // @A2A
    // length_ = 5;  // @A2D
  }

  /**
   *Constructs a BinaryFieldDescription object. It uses the specified data type, name,
   *DDS name, and length of the field.  This constructor is used when the field description will
   *be used with the record level access classes.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   *@param length The number of digits that the field will hold.  This is the length of
   *              the field as it would appear in a DDS description.  The <i>length</i>
   *              must be greater than 0.
  **/
  public BinaryFieldDescription(AS400Bin2 dataType, String name, String ddsName, int length)
  {
    super(dataType, name, ddsName);
    if (length < 1)
    {
      throw new ExtendedIllegalArgumentException("length (" + String.valueOf(length) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    length_ = length;
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
    desc.append(len);
    // Type column (1)
    desc.append("B");
    // Decimal positions columns (2)
    desc.append("  ");
    // Not used columns (7)
    desc.append("       ");
    // Add fixed portion of DDS description to the vector
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
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400Bin2 dataType)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    dataType_ = dataType;
    // Set the length to the default value if it has not already been set via setLength
    if (length_ == 0)
    {
      length_ = 5; // 5 is the maximum length allowed for DDS for a binary field of 2 bytes.
    }
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400Bin4 dataType)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    dataType_ = dataType;
    // Set the length to the default value if it has not already been set via setLength
    if (length_ == 0)
    {
      length_ = 9; // 9 is the maximum length allowed for DDS for a binary field of 4 bytes.
    }
  }

  //@B0C - javadoc
  /**
   *Sets the value for the DFT keyword for this field.  Use this
   *version of setDFT() when an AS400Bin4 object was used to
   *construct the object.
   *@param defaultValue The default value for this
   *field.  The <i>defaultValue</i>cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(Integer defaultValue)
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

  //@B0C - javadoc
  /**
   *Sets the value for the DFT keyword for this field.  Use this
   *version of setDFT() when an AS400Bin2 object was used to
   *construct the object.
   *@param defaultValue The default value for this
   *                   field.  The <i>defaultValue</i>cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(Short defaultValue)
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

  /**
   *Sets the length of this field.
   *@param length The length of this field. The <i>length</i> must be greater than zero.
  **/
  public void setLength(int length)
  {
    if (length < 1)
    {
      throw new ExtendedIllegalArgumentException("length (" + String.valueOf(length) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    length_ = length;
  }

}
