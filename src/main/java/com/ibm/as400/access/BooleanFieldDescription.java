///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BinaryFieldDescription.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2025 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.util.Vector;

/**
 *Represents the description of the data in a boolean field.
 *This class allows:
 *<ul>
 *<li>The user to describe a boolean field to the <a href="RecordFormat.html">RecordFormat</a> object.
 *<li>The RecordFormat object to describe a boolean field to the user.
 *</ul>
 *Click <a href="doc-files/recordxmp.html">here</a>to see an example.
 **/
public class BooleanFieldDescription extends FieldDescription implements Serializable
{
  static final long serialVersionUID = -2040945751371810257L;
  /**
   *Constructs a BinaryFieldDescription object.
  **/
  public BooleanFieldDescription()
  {
  }

  /**
   *Constructs a BooleanFieldDescription object. It uses the specified data type and name
   *of the field.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public BooleanFieldDescription(AS400Boolean  dataType, String name)
  {
    // When no length is specified, we pass length of 9 in case field will be
    // used for DDS.  If this is the case, the length represents the number of
    // digits in the field, not the byte length of the field.
    super(dataType, name);
   length_ = 1; 
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
    Vector<String> v = new Vector<String>();
    // Name columns (10)
    StringBuilder desc = new StringBuilder(ddsName_);
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

    String len = String.valueOf(length_);
    int numSpaces = 5-len.length();
    for (int i=0; i<numSpaces; ++i) desc.append(' ');
    // Length columns (5)
    desc.append(len);
    // Type column (1)
    desc.append("8");
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
  public void setDataType(AS400Boolean dataType)
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
      length_ = 4; 
    }
  }

  
  /**
   *Sets the value for the DFT keyword for this field.  
   *@param defaultValue The default value for this
   *field.  The <i>defaultValue</i>cannot be null.
   *To set a default value of *NULL, use the setDFTNull() method.
  **/
  public void setDFT(Boolean defaultValue)
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
