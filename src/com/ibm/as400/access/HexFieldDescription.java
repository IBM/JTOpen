///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HexFieldDescription.java
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
 *The HexFieldDescription class represents the description of the data in a hexadecimal field.
 *It allows:
 *<ul>
 *<li>The user to describe a hexadecimal field to the RecordFormat object.
 *<li>The RecordFormat object to describe a hexadecimal field to the user.
 *</ul>
 *Click <a href="../../../../recordxmp.html">here</a>to see an example.
**/
public class HexFieldDescription extends FieldDescription implements VariableLengthFieldDescription, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;


  // Value specified for the VARLEN keyword
  private int variableLength_;
  // Is the field a variable length field
  private boolean isVariableLength_;

  /**
   *Constructs a HexFieldDescription object.
  **/
  public HexFieldDescription()
  {
  }

  /**
   *Constructs a HexFieldDescription object. It uses the specified data type
   *and name of the field.
   *The length of the field will be the length specified on the AS400ByteArray object.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   **/
  public HexFieldDescription(AS400ByteArray dataType, String name)
  {
    super(dataType, name);
  }

  /**
   *Constructs a HexFieldDescription object. It uses the specified data type,
   *name, and DDS name of the field.
   *The length of the field will be the length specified on the AS400ByteArray object.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
   **/
  public HexFieldDescription(AS400ByteArray dataType, String name, String ddsName)
  {
    super(dataType, name, ddsName);
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
    desc.append("H");
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
    // Add field type specific keywords
    if (isVariableLength_)
    {
      if (variableLength_ > 0)
      {
        v.addElement("VARLEN(" + new Integer(variableLength_).toString() + ") ");
      }
      else
      {
        v.addElement("VARLEN ");
      }
    }
    //@B0A
    if (defaultValue_ != null)
    {
      v.addElement("DFT('" + new String((byte[])defaultValue_) + "') ");
    }
    else if (isDFTNull_)
    {
      v.addElement("DFT(*NULL) ");
    }

    String[] s = new String[v.size()];
    v.copyInto(s);
    return s;
  }

  /**
   *Returns the value specified for the VARLEN keyword for this field.
   *@return The value specified for VARLEN for this field.  If VARLEN was not
   *        specified for this field, 0 is returned.
  **/
  public int getVARLEN()
  {
    return variableLength_;
  }

  /**
   *Indicates if the field is a variable-length field.  
   *@return true if the field is a variable-length field; false otherwise.
  **/
  public boolean isVariableLength()
  {
    return isVariableLength_;
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  public void setDataType(AS400ByteArray dataType)
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
   *                    field.  The length of <i>defaultValue</i>
   *                    must be greater than zero.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(byte[] defaultValue)
  {
    if (defaultValue == null)
    {
      throw new NullPointerException("defaultValue");
    }
    if (defaultValue.length < 1)
    {
      throw new ExtendedIllegalArgumentException("defaultValue (" + String.valueOf(defaultValue) + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
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
   *Indicates that the field is a variable-length field.
   *@param value true if the field is a variable-length field; false otherwise.
  **/
  public void setVariableLength(boolean value)
  {
    isVariableLength_ = value;
  }

  /**
   *Sets the value to specify for the VARLEN keyword for this field.
   *@param varLen The value to specify for the VARLEN keyword for this field.
   *The <i>varLen</i> cannot be less than zero.
  **/
  public void setVARLEN(int varLen)
  {
    if (varLen < 0)
    {
      throw new ExtendedIllegalArgumentException("varLen (" + String.valueOf(varLen) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    variableLength_ = varLen;
    isVariableLength_ = true;
  }
}
