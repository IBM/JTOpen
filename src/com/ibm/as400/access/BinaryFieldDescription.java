///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BinaryFieldDescription.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Vector;

/**
 *Represents the description of the data in a binary (integer) field.
 *This class allows:
 *<ul>
 *<li>The user to describe a binary field to the <a href="RecordFormat.html">RecordFormat</a> object.
 *<li>The RecordFormat object to describe a binary field to the user.
 *</ul>
 *Click <a href="doc-files/recordxmp.html">here</a>to see an example.
 *<p>
 *As of OS/400 V4R5, DDS supports up to 8-byte (18-digit) binary field descriptions.
 *Using any of the data types that result in a byte length greater than 4 or a number of
 *digits greater than 9 on a release prior to V4R5 may give unexpected results.
 **/
public class BinaryFieldDescription extends FieldDescription implements Serializable
{
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
   *a RecordFormat object. The maximum number of digits allowed for a binary field by DDS is 18.
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
   *Constructs a BinaryFieldDescription object. It uses the specified data type and name
   *of the field.
   *The length of this field is represented by the number of digits it can contain.
   *This constructor defaults the length (as returned by getLength()) to 9.
   *The length is used by the Record Level Access classes when creating a file from
   *a RecordFormat object. The maximum number of digits allowed for a binary field by DDS is 18.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public BinaryFieldDescription(AS400UnsignedBin4 dataType, String name)
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
  public BinaryFieldDescription(AS400UnsignedBin4 dataType, String name, String ddsName, int length)
  {
    super(dataType, name, ddsName);
    if (length < 1)
    {
      throw new ExtendedIllegalArgumentException("length (" + String.valueOf(length) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    length_ = length;
  }

  /**
   *Constructs a BinaryFieldDescription object. It uses the specified data type and name
   *of the field.
   *The length of this field is represented by the number of digits it can contain.
   *This constructor defaults the length (as returned by getLength()) to 18.
   *The length is used by the Record Level Access classes when creating a file from
   *a RecordFormat object. The maximum number of digits allowed for a binary field by DDS is 18.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public BinaryFieldDescription(AS400Bin8 dataType, String name)
  {
    // When no length is specified, we pass length of 18 in case field will be
    // used for DDS.  If this is the case, the length represents the number of
    // digits in the field, not the byte length of the field.
    super(dataType, name);
    length_ = 18;

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
  public BinaryFieldDescription(AS400Bin8 dataType, String name, String ddsName, int length)
  {
    super(dataType, name, ddsName);
    if (length < 1)
    {
      throw new ExtendedIllegalArgumentException("length (" + String.valueOf(length) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    length_ = length;
  }

  /**
   *Constructs a BinaryFieldDescription object. It uses the specified data type and name
   *of the field.
   *The length of this field is represented by the number of digits it can contain.
   *This constructor defaults the length (as returned by getLength()) to 18.
   *The length is used by the Record Level Access classes when creating a file from
   *a RecordFormat object. The maximum number of digits allowed for a binary field by DDS is 18.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public BinaryFieldDescription(AS400UnsignedBin8 dataType, String name)
  {
    // When no length is specified, we pass length of 18 in case field will be
    // used for DDS.  If this is the case, the length represents the number of
    // digits in the field, not the byte length of the field.
    super(dataType, name);
    length_ = 18;

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
  public BinaryFieldDescription(AS400UnsignedBin8 dataType, String name, String ddsName, int length)
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
  public BinaryFieldDescription(AS400UnsignedBin2 dataType, String name)
  {
    // When no length is specified, we pass length of 4 in case field will be
    // used for DDS.  If this is the case, the length represents the number of
    // digits in the field, not the byte length of the field.
    super(dataType, name);
    length_ = 4;
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
  public BinaryFieldDescription(AS400UnsignedBin2 dataType, String name, String ddsName, int length)
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
   *This constructor defaults the length (as returned by getLength()) to 3.
   *The length is used by the Record Level Access classes when creating a file from
   *a RecordFormat object. Three (3) is the maximum number of digits allowed for a binary field
   *(when represented by a bin1) by DDS.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public BinaryFieldDescription(AS400Bin1 dataType, String name)
  {
    // When no length is specified, we pass length of 4 in case field will be
    // used for DDS.  If this is the case, the length represents the number of
    // digits in the field, not the byte length of the field.
    super(dataType, name);
    length_ = 3;
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
  public BinaryFieldDescription(AS400Bin1 dataType, String name, String ddsName, int length)
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
   *This constructor defaults the length (as returned by getLength()) to 3.
   *The length is used by the Record Level Access classes when creating a file from
   *a RecordFormat object. Three (3) is the maximum number of digits allowed for a binary field
   *(when represented by a bin1) by DDS.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public BinaryFieldDescription(AS400UnsignedBin1 dataType, String name)
  {
    // When no length is specified, we pass length of 4 in case field will be
    // used for DDS.  If this is the case, the length represents the number of
    // digits in the field, not the byte length of the field.
    super(dataType, name);
    length_ = 3;
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
  public BinaryFieldDescription(AS400UnsignedBin1 dataType, String name, String ddsName, int length)
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
  public void setDataType(AS400Bin1 dataType)
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
      length_ = 3; // 3 is the maximum #digits allowed for DDS for a binary field of 1 byte.
    }
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400UnsignedBin1 dataType)
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
      length_ = 3; // 3 is the maximum #digits allowed for DDS for a binary field of 1 byte.
    }
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
      length_ = 4; // 4 is the maximum #digits allowed for DDS for a binary field of 2 bytes. @C0C
    }
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400UnsignedBin2 dataType)
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
      length_ = 4; // 4 is the maximum #digits allowed for DDS for a binary field of 2 bytes. @C0C
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
      length_ = 9; // 9 is the maximum #digits allowed for DDS for a binary field of 4 bytes.
    }
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400UnsignedBin4 dataType)
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
      length_ = 9; // 9 is the maximum #digits allowed for DDS for a binary field of 4 bytes.
    }
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400Bin8 dataType)
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
      length_ = 18; // 18 is the maximum #digits allowed for DDS for a binary field of 8 bytes.
    }
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400UnsignedBin8 dataType)
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
      length_ = 18; // 18 is the maximum #digits allowed for DDS for a binary field of 8 bytes.
    }
  }

  /**
   *Sets the value for the DFT keyword for this field.  Use this
   *version of setDFT() when an AS400Bin4 or AS400UnsignedBin2 object was used to
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

  /**
   *Sets the value for the DFT keyword for this field.  Use this
   *version of setDFT() when an UnsignedAS400Bin8 object was used to
   *construct the object.
   *@param defaultValue The default value for this
   *                   field.  The <i>defaultValue</i>cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(BigInteger defaultValue)
  {
    if (defaultValue == null)
    {
      throw new NullPointerException("defaultValue");
    }
    defaultValue_ = defaultValue;
    isDFTNull_ = false;
    isDFTCurrent_ = false;
    DFTCurrentValue_ = null;
  }

  /**
   *Sets the value for the DFT keyword for this field.  Use this
   *version of setDFT() when an AS400UnsignedBin4 or AS400Bin8 object was used to
   *construct the object.
   *@param defaultValue The default value for this
   *field.  The <i>defaultValue</i>cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(Long defaultValue)
  {
    if (defaultValue == null)
    {
      throw new NullPointerException("defaultValue");
    }
    defaultValue_ = defaultValue;
    isDFTNull_ = false;
    isDFTCurrent_ = false;
    DFTCurrentValue_ = null;
  }

  /**
   *Sets the value for the DFT keyword for this field.  Use this
   *version of setDFT() when an AS400UnsignedBin1 or AS400Bin2 object was used to
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

  /**
   *Sets the value for the DFT keyword for this field.  Use this
   *version of setDFT() when an AS400Bin1 object was used to
   *construct the object.
   *@param defaultValue The default value for this
   *                   field.  The <i>defaultValue</i>cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(Byte defaultValue)
  {
    if (defaultValue == null)
    {
      throw new NullPointerException("defaultValue");
    }
    defaultValue_ = defaultValue;
    isDFTNull_ = false;
    isDFTCurrent_ = false;
    DFTCurrentValue_ = null;
  }

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
