///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FloatFieldDescription.java
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
 *The FloatFieldDescription class represents the description of the data in a float field.
 *It allows:
 *<ul>
 *<li>The user to describe a float field to the RecordFormat object.
 *<li>The RecordFormat object to describe a float field to the user.
 *</ul>
 *Click <a href="doc-files/recordxmp.html">here</a>to see an example.
**/
public class FloatFieldDescription extends FieldDescription implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;
  // Number of decimal positions for this field
  private int decimalPositions_;
  // Value specified for the FLTPCN keyword
  private String floatPrecision_ = "";

  /**
   *Constructs a FloatFieldDescription object.
  **/
  public FloatFieldDescription()
  {
  }

  /**
   *Constructs a FloatFieldDescription object. It uses the specified
   *data type and name of the field.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   **/
  public FloatFieldDescription(AS400Float8 dataType, String name)
  {
    // We pass 9 for the length in case this field will be used for DDS.  We pass 9
    // because if the length is greater than 9, then keyword FLTPCN must be specified
    // with a value of *DOUBLE.  The user should have called the appropriate constructor
    // if that is what they wished.
    super(dataType, name);
    length_ = 9;
  }

  /**
   *Constructs a FloatFieldDescription object. It uses the specified
   *data type, name, DDS name, and length of the field.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   *@param length The number of digits that the field will hold.  This is the length of
   *              the field as it would appear in a DDS description.  The <i>length</i>
   *              must be greater than zero.
   **/
  public FloatFieldDescription(AS400Float8 dataType, String name, String ddsName, int length)
  {
    super(dataType, name, ddsName);
    if (length < 1)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    length_ = length;
  }

  /**
   *Constructs a FloatFieldDescription object. It uses the specified data type,
   *name, DDS name, length and decimal positions of the field.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   *@param length The number of digits that the field will hold.  This is the length of
   *              the field as it would appear in a DDS description.  The <i>length</i>
   *              must be greater than zero.
   *@param decimalPositions The number of digits to the right of the decimal point.
   *                        The <i>decimalPositions</i> cannot be negative.
   **/
  public FloatFieldDescription(AS400Float8 dataType, String name, String ddsName, int length,
                               int decimalPositions)
  {
    super(dataType, name, ddsName);
    if (length < 1)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (decimalPositions < 0)
    {
      throw new ExtendedIllegalArgumentException("decimalPositions", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    length_ = length;
    decimalPositions_ = decimalPositions;
  }

  /**
   *Constructs a FloatFieldDescription object. It uses the specified
   *data type and name of the field.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   **/
  public FloatFieldDescription(AS400Float4 dataType, String name)
  {
    // We pass 9 for the length in case this field will be used for DDS.  We pass 9
    // because if the length is greater than 9, then keyword FLTPCN must be specified
    // with a value of *DOUBLE.  The user should have called the appropriate constructor
    // if that is what they wished.
    super(dataType, name);
    length_ = 9;
  }

  /**
   *Constructs a FloatFieldDescription object. It uses the specified data type, name,
   *DDS name, and length of the field.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   *@param length The number of digits that the field will hold.  This is the length of
   *              the field as it would appear in a DDS description.  The <i>length</i>
   *              must be greater than zero.
   **/
  public FloatFieldDescription(AS400Float4 dataType, String name, String ddsName, int length)
  {
    super(dataType, name, ddsName);
    if (length < 1)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    length_ = length;
  }

  /**
   *Constructs a FloatFieldDescription object. It uses the specified data type, name,
   *DDS name, length and decimal positions of the field.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   *@param length The number of digits that the field will hold.  This is the length of
   *              the field as it would appear in a DDS description.  The <i>length</i> must be
   *              greater than zero.
   *@param decimalPositions The number of digits to the right of the decimal point.  The
   *<i>decimalPositions</i> cannot be negative.
   **/
  public FloatFieldDescription(AS400Float4 dataType, String name, String ddsName, int length,
                               int decimalPositions)
  {
    super(dataType, name, ddsName);
    if (length < 1)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (decimalPositions < 0)
    {
      throw new ExtendedIllegalArgumentException("decimalPositions", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    length_ = length;
    decimalPositions_ = decimalPositions;
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
/*    StringBuffer len = new StringBuffer(new Integer(length_).toString());
    if (len.length() < 5)
    {
      int blanksNeeded = 5 - len.length();
      for (short i = 0; i < blanksNeeded; ++i)
      {
        len.insert(0, " ");
      }
    }
*/    
    String len = String.valueOf(length_);
    int numSpaces = 5-len.length();
    for (int i=0; i<numSpaces; ++i) desc.append(' ');
    // Length columns (5)
    desc.append(len.toString());
    // Type column (1)
    desc.append("F");
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
    if (!floatPrecision_.equals(""))
    {
      v.addElement("FLTPCN(" + floatPrecision_ + ") ");
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
   *Returns the value specified for the FLTPCN keyword for this field.
   *@return The value specified for FLTPCN for
   *        this field.  Possible values are *SINGLE or *DOUBLE.
   *        If FLTPCN was not specified for this field,
   *        an empty string is returned.
  **/
  public String getFLTPCN()
  {
    return floatPrecision_;
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400Float4 dataType)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    dataType_ = dataType;
    // Set the length of the field based on the data type
    length_ = 9;  // 9 is the maximum number of digits for a single precision number in DDS
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400Float8 dataType)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    dataType_ = dataType;
    // Set the length of the field based on the data type
    length_ = 17;  // 17 is the maximum number of digits for a double precision number in DDS
  }

  /**
   *Sets the number of digits after the decimal point for this field.
   *@param decimalPositions The number of digits after the decimal point.
   *The <i>decimalPositions</i> cannot be less than zero.
  **/
  public void setDecimalPositions(int decimalPositions)
  {
    if (decimalPositions < 0)
    {
      throw new ExtendedIllegalArgumentException("decimalPositions (" + String.valueOf(decimalPositions) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    decimalPositions_ = decimalPositions;
  }

  /**
   *Sets the value for the FLTPCN keyword for this field.
   *@param floatPrecision The value to set for the FLTPCN keyword for
   *        this field.  Possible values are *SINGLE or *DOUBLE.
   *The <i>floatPrecision</i> cannot be null.
  **/
  public void setFLTPCN(String floatPrecision)
  {
    if (floatPrecision == null)
    {
      throw new NullPointerException("floatPrecision");
    }
    if (!(floatPrecision.equalsIgnoreCase("*SINGLE") || floatPrecision.equalsIgnoreCase("*DOUBLE")))
    {
      throw new ExtendedIllegalArgumentException("floatPrecision (" + floatPrecision + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    floatPrecision_ = floatPrecision;
  }

  //@B0C - javadoc
  /**
   *Sets the value for the DFT keyword for this field.  Use this version
   *of setDFT() when an AS400Float8 was used to construct the object.
   *@param defaultValue The default value for this
   *                   field.  The <i>defaultValue</i> cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(Double defaultValue)
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
   *Sets the value for the DFT keyword for this field.  Use this version
   *of setDFT() when an AS400Float4 was used to construct the object.
   *@param defaultValue The default value for this
   *                   field.  The <i>defaultValue</i> cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(Float defaultValue)
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
   *@param length The length of this field.  The <i>length</i> must be greater than
   *zero.
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

