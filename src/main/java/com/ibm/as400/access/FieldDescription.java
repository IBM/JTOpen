///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FieldDescription.java
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
 *An abstract base class that allows the user to describe the data in a field
 *with an AS400DataType object and a name.  Optionally, the user can specify a
 *data definition specification (DDS) field name and DDS keywords if the field will
 *be used with the record level access classes to define a RecordFormat object
 *with which to create a physical file.
 *The FieldDescription class contains methods to
 *set and get field attributes that are common to all field types.<br>
 *<p> 
 *<b>Examples</b>
 *<ul>
 *<li><a href="doc-files/recordxmp.html">Using the FieldDescription classes with the Data queue classes</a>
 *<li><a href="doc-files/RLReadFileExample.html">Using the FieldDescription classes with the record-level database access classes</a>
 *<li><a href="doc-files/LDRWExample.html">Using the FieldDescription classes with the LineDataRecordWriter class</a>
 *</ul>
**/
abstract public class FieldDescription implements Serializable
{  
    static final long serialVersionUID = 4L;



  // Public constants. 
  /** Constant indicating left alignment of data within the field layout.  **/
  /** This is only used for record level writing.                          **/
  public static final int ALIGN_LEFT    = 1;    // @C1A
  /** Constant indicating right alignment of data within the field layout. **/
  /** This is only used for record level writing.                          **/
  public static final int ALIGN_RIGHT   = 2;    // @C1A
  
  // ALIAS keyword
  String alias_ = "";
  // ALWNUL keyword
  boolean allowNull_ = false;
  // COLHDG keyword
  String columnHeading_ = "";
  // AS400DataType object containing the description of the data as well
  // as methods to convert the data to and from its IBM i format.
  AS400DataType dataType_ = null;
  // The DDS name of the field
  String ddsName_ = "";
  // DFT keyword
  Object defaultValue_ = null;
  // Key field keywords specific to this key field.
  String[] keyFieldFunctions_ = null;
  // The layout length of the field.  @C1A
  int layoutLength_ = 0;        // @C1A
  // The layoutAlignment of the field.  @C1A
  int layoutAlignment_ = 0;     // @C1A
  // The length of the field
  int length_;
  // The name of the field.
  String name_ = "";
  // REFFIL keyword
  String refFil_ = "";
  // REFFLD keyword
  String refFld_ = "";
  // REFFMT keyword
  String refFmt_ = "";
  // REFLIB keyword
  String refLib_ = "";
  // TEXT keyword - must be less than or equal to 50 characters
  String text_ = "";
  //@B0A
  // Is the default value for the field set to be CURRENT_DATE, CURRENT_TIME, or CURRENT_TIMESTAMP?
  boolean isDFTCurrent_ = false;
  //@B0A
  // Is the default value for the field set to be *NULL
  String DFTCurrentValue_ = null;
  //@B0A
  // Is the default value for the field set to be *NULL
  boolean isDFTNull_ = false;

  /**
   *Constructs a FieldDescription object.
  **/
  protected FieldDescription()
  {
  }

  /**
   *Constructs a FieldDescription object. It uses the specified data type and name of the field.
   *The length of the field will be the length
   *specified on the AS400DataType object.  The DDS name of the field will be <i>name</i>
   *if <i>name</i> is 10 characters or less.  The DDS name of the field will be <i>name</i>
   *truncated to 10 characters if <i>name</i> is greater than 10 characters.
   *The layout length will be the length of the field, and the
   *layout alignment will be ALIGN_LEFT.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  protected FieldDescription(AS400DataType dataType, String name)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    dataType_ = dataType;
    name_ = name;
    ddsName_ = (name.length() > 10)? name.substring(0, 10).toUpperCase() : name.toUpperCase();
    length_ = dataType.getByteLength();
  }

  /**
   *Constructs a FieldDescription object. It uses the specified data type,
   *name, and DDS name of the field.
   * The length of the field will be the length
   *specified on the AS400DataType object.
   *The layout length will be the length of the field, and the
   *layout alignment will be ALIGN_LEFT.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
   *@param ddsName The DDS name of this field. This is the
   *               name of the field as it would appear in a DDS description of the
   *               field.  The length of <i>ddsName</i> must be 10 characters or less.
  **/
  protected FieldDescription(AS400DataType dataType, String name, String ddsName)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    if (ddsName == null)
    {
      throw new NullPointerException("ddsName");
    }
    if (ddsName.length() > 10)
    {
      throw new ExtendedIllegalArgumentException("ddsName", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    dataType_ = dataType;
    name_ = name;
    ddsName_ = ddsName.toUpperCase();
    length_ = dataType.getByteLength();
  }

  /**
   *Returns the value specified for the ALIAS keyword for this field.
   *@return The value specified for the ALIAS keyword for this field.  If ALIAS
   *        was not specified for this field, an empty string is returned.
  **/
  public String getALIAS()
  {
    return alias_;
  }

  /**
   *Returns the value specified for the ALWNULL keyword for this field.
   *@return The value specified for the ALWNULL (allow null value) keyword. If ALWNULL was not
   *specified for this field, false is returned.
  **/
  public boolean getALWNULL()
  {
    return allowNull_;
  }

  /**
   *Returns the value specified for the COLHDG keyword for this field.
   *@return The value specified for the COLHDG (column heading)
   *        keyword for this field.  If COLHDG was not specified for this field,
   *        an empty string is returned.
   *        For a description of the format of the 3-part column heading string,
   *        refer to {@link #setCOLHDG setCOLHDG()}.
  **/
  public String getCOLHDG()
  {
    return columnHeading_;
  }

  /**
   *Returns the AS400DataType object describing this field, as specified on construction.
   *@return The AS400DataType object that describes this field.  If the
   *data type has not been specified for this field, null is returned.
  **/
  public AS400DataType getDataType()
  {
    return dataType_;
  }

  /**
   *Returns the value specified for the DFT keyword for this field.
   *@return The value specified for the DFT (default) keyword for this field.
   *If DFT was not specified for this field, null is returned.
  **/
  public Object getDFT()
  {
    return defaultValue_;
  }

  //@B0A
  /**
   *Returns the default value setting based on the current timestamp.
   *If this field has had its default value set by calling setDFTCurrent(),
   *then this method will return the value of the current Date, Time, or
   *Timestamp to be used for that default value; otherwise, it returns null.
   *@return The value being used as the "current" default value for this field.
   *If DFT was not specified for this field, null is returned.
  **/
  public String getDFTCurrentValue()
  {
    return DFTCurrentValue_;
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
  abstract String[] getDDSDescription();

  /**
   *Returns the DDS name of this field, as specified on the construct.
   *@return The DDS name of this field.  If the DDS name for this field
   *has not been specified, an empty string is returned.
  **/
  public String getDDSName()
  {
      return ddsName_;
  }

  /**
   *Returns the common field level key words for the field.
   *This method returns a string containing any keywords common to all fields
   *that have been specified for the field, formatted for entry in a DDS source
   *file.
   *@return The common field level keywords specified for this field, properly
   *formatted for entry into a DDS source file.
  **/
  String[] getFieldFunctions()
  {
    Vector v = new Vector();
    if (!alias_.equals(""))
    {
      v.addElement("ALIAS(" + alias_ + ") ");
    }
    if (allowNull_)
    {
      v.addElement("ALWNULL ");
    }
    if (!columnHeading_.equals(""))
    {
      v.addElement("COLHDG(" + columnHeading_ + ") ");
    }
    if (!refFld_.equals(""))
    {
      v.addElement("REFFLD(" + refFld_ + ") ");
    }
    if (!refFil_.equals("")) 
    {
      v.addElement("REFFIL(" + refFil_ + ") ");
    }
    if (!refFmt_.equals("")) 
    {
      v.addElement("REFFMT(" + refFmt_ + ") ");
    }
    if (!refLib_.equals("")) 
    {
      v.addElement("REFLIB(" + refLib_ + ") ");
    }
    if (!text_.equals(""))
    {
      v.addElement("TEXT('" + text_ + "') ");
    }
    if (v.size() != 0)
    {
      String[] s = new String[v.size()];
      v.copyInto(s);
      return s;
    }
    // No field functions, return null
    return null;
  }

  /**
   *Returns the name of this field.
   *@return The name of this field.  If the field name for this field
   *has not been specified, an empty string is returned.
  **/
  public String getFieldName()
  {
      return name_;
  }

  /**
   *Returns the string specified for any key field-level keywords for this
   *field.
   *@return The key field-level keywords that have
   *        been specified for this key field.  If no key field
   *        functions have been specified, null is returned.
  **/
  public String[] getKeyFieldFunctions()
  {
    return keyFieldFunctions_;
  }



  // @C1A - added method
  /**
   *Returns the layout alignment of this field.  The layout alignment specifies
   *the location of the data as presented within the layout length of the field.
   *This value is only used in conjunction with line data record writer class,
   *and only if the including record format is of type
   *FIXED_LAYOUT_LENGTH.
   *
   *@return The layout alignment of this field.
  **/
  public int getLayoutAlignment()
  {
    return layoutAlignment_;
  }



  // @C1A - added method
  /**
   *Returns the layout length of this field.  The layout length is the 
   *actual character length of the field when written using the line    
   *data record writer class. The layout length is only valid
   *if the including record format is of type
   *FIXED_LAYOUT_LENGTH.   
   *
   *@return The layout length of this field.
  **/
  public int getLayoutLength()
  {
    return layoutLength_;
  }
  

  
  /**
   *Returns the length of this field.  If this field is a character field (single byte or
   *double byte, date, time, timestamp), the length is the number of characters allowed in the field.
   *If this field is a numeric field (binary, float, packed, zoned), the length is the total
   *number of digits allowed in the field.  If this field is a hexadecimal field, the
   *length is the number of bytes allowed in the field.
   *
   *@return The length of the field.
  **/
  public int getLength()
  {
    return length_;
  }

  /**
   *Returns the value specified for the REFFIL keyword for this field.
   *@return The value specified for the REFFIL (reference file) keyword
   *        for this field.  If REFFIL was not specified for this field,
   *        an empty string is returned.
  **/
  public String getREFFIL()
  {
    return refFil_;
  }

  /**
   *Returns the value specified for the REFFLD keyword for this field.
   *@return The value specified for the REFFLD (reference field) keyword
   *        for this field.  If REFFLD was not specified for this field,
   *        an empty string is returned.
  **/
  public String getREFFLD()
  {
    return refFld_;
  }

  /**
   *Returns the value specified for the REFFMT keyword for this field.
   *@return The value specified for the REFFMT (reference record format) keyword
   *        for this field.  If REFFMT was not specified for this field,
   *        an empty string is returned.
  **/
  public String getREFFMT()
  {
    return refFmt_;
  }

  /**
   *Returns the value specified for the REFLIB keyword for this field.
   *@return The value specified for the REFLIB (reference library) keyword
   *        for this field.  If REFLIB was not specified for this field,
   *        an empty string is returned.
  **/
  public String getREFLIB()
  {
    return refLib_;
  }

  /**
   *Returns the value specified for the TEXT keyword for this field.
   *@return The value specified for the TEXT keyword
   *        for this field.  If TEXT was not specified for this field, 
   *        an empty string is returned.
  **/
  public String getTEXT()
  {
    return text_;
  }

  //@B0A
  /**
   *Indicates if the default value for this field is set to one of the
   *SQL special values of CURRENT_DATE, CURRENT_TIME, or CURRENT_TIMESTAMP.
   *@return True if the default value is set to one of the above.
  **/
  public boolean isDFTCurrent()
  {
    return isDFTCurrent_;
  }

  /**
   *Sets the value for the ALIAS keyword for this field.
   *@param alias The alias for this field.
  **/
  public void setALIAS(String alias)
  {
    if (alias == null)
    {
      throw new NullPointerException("alias");
    }
    alias_ = alias;
  }

  /**
   *Sets the value for the ALWNULL keyword for this field.
   *@param allowNull true if a null value is allowed; false otherwise.
  **/
  public void setALWNULL(boolean allowNull)
  {
    allowNull_ = allowNull;
  }

  /**
   *Sets the value for the COLHDG keyword for this field.
   *@param colHdg The value for the COLHDG (column heading) keyword
   *              for this field.<br>
   *                     Format: "'Col heading 1' 'Col heading 2' 'Col heading 3'"<br>
   *                     Examples:
   *                     <pre>
   *                        String colHdg = "'Name'";
   *                        String colHdg = "'Employee' 'Number'";
   *                        String colHdg = "'Name' 'And' 'Address'";
   *                     </pre>
  **/
  public void setCOLHDG(String colHdg)
  {
    if (colHdg == null)
    {
      throw new NullPointerException("colHdg");
    }
    columnHeading_ = colHdg;
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.  The <i>dataType</i>
   *cannot be null.
  **/
  protected void setDataType(AS400DataType dataType)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    dataType_ = dataType;
  }

  /**
   *Sets the DDS name of this field.
   *@param ddsName The DDS name of this field.  The <i>ddsName</i> cannot be
   *more than 10 characters in length.
  **/
  public void setDDSName(String ddsName)
  {
    // Verify parameters
    if (ddsName == null)
    {
      throw new NullPointerException("ddsName");
    }
    if (ddsName.length() > 10)
    {
      throw new ExtendedIllegalArgumentException("ddsName", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    ddsName_ = ddsName.toUpperCase();
  }

  /**
   *Sets the name of this field.
   *@param fieldName The name of this field.  The <i>fieldName</i> cannot be null.
  **/
  public void setFieldName(String fieldName)
  {
    // Verify parameters
    if (fieldName == null)
    {
      throw new NullPointerException("fieldName");
    }
    name_ = fieldName;
  }

  /**
   *Sets the string to be specified for all key field-level keywords for this
   *field.
   *@param keyFunctions The key field-level keywords to be
   *                    specified for this key field.
   *The <i>keyFunctions</i> must contain at least one element.
  **/
  public void setKeyFieldFunctions(String[] keyFunctions)
  {
    if (keyFunctions == null)
    {
      throw new NullPointerException("keyFunctions");
    }
    if (keyFunctions.length == 0)
    {
      throw new ExtendedIllegalArgumentException("keyFunctions", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    keyFieldFunctions_ = keyFunctions;
  }
  
  
  
 // @C1A - added method 
 /**
   *Sets the layout alignment of this field.  The layout alignment specifies
   *the location of the data as presented within the layout length of the field.
   *This value is only used in conjunction with the line data record writer class,
   *and only if the including record format is of type
   *FIXED_LAYOUT_LENGTH.
   <p>
   *The following special values are valid:
   * <UL>
   *   <LI> ALIGN_LEFT  - Left alignment of data within the field layout.
   *   <LI> ALIGN_RIGHT - Right alignment of data within the field layout.
   * </UL>
   *
   *@param layoutAlignment The layout alignment of this field.
  **/
  public void setLayoutAlignment(int layoutAlignment)
  {
        if ((layoutAlignment != ALIGN_LEFT) && (layoutAlignment != ALIGN_RIGHT)) {
            throw new ExtendedIllegalArgumentException("layoutAlignment",
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        layoutAlignment_ = layoutAlignment;
  }
  


 // @C1A - added method 
 /**
   *Sets the layout length and layout alignment of this field. 
   *The layout length is the 
   *actual character length of the field when written using  
   *the line data record writer class. 
   *The layout length must be 50 characters or less.
   *The layout alignment specifies
   *the location of the data as presented within the layout length of the field.
   *These values are only used in conjunction with the line data record writer class,
   *and only if the including record format is of type
   *FIXED_LAYOUT_LENGTH.
   *<p>
   *The following special values for the layout alignment are valid:
   * <UL>
   *   <LI> ALIGN_LEFT  - Left alignment of data within the field layout.
   *   <LI> ALIGN_RIGHT - Right alignment of data within the field layout.
   * </UL>
   *
   *@param layoutLength     The layout length of this field.
   *@param layoutAlignment  The layout alignment of this field.
  **/
  public void setLayoutAttributes(int layoutLength, int layoutAlignment)
  {      
        if ((layoutLength > 50) || (layoutLength < 0)) {
            throw new ExtendedIllegalArgumentException("layoutLength", 
                ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
      
        if ((layoutAlignment != ALIGN_LEFT) && (layoutAlignment != ALIGN_RIGHT)) {
            throw new ExtendedIllegalArgumentException("layoutAlignment", 
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }  
        
        layoutLength_ = layoutLength;
        layoutAlignment_ = layoutAlignment;
  }



  // @C1A - added method
  /**
   *Sets the layout length  of this field.  The layout length is the 
   *actual character length of the field when written using the   
   *line data record writer class.  The layout length is only valid
   *if the including record format is of type
   *FIXED_LAYOUT_LENGTH. 
   *The layout length must be 50 characters or less.
   *NOTE: The layout length does not have to equal the length of the field.  
   *
   *@param layoutLength     The layout length of this field.
  **/
  public void setLayoutLength(int layoutLength)
  {
        if ((layoutLength > 50) || (layoutLength < 0)) {
            throw new ExtendedIllegalArgumentException("layoutLength",
                ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        layoutLength_ = layoutLength;
  }
  


  /**
   *Sets the value to be specified for the REFFIL keyword for this field.
   *@param refFil The value for the REFFIL (reference file) keyword
   *               for this field.
  **/
  public void setREFFIL(String refFil)
  {
    if (refFil == null)
    {
      throw new NullPointerException("refFil");
    }
    refFil_ = refFil;
  }


  /**
   *Sets the value to be specified for the REFFLD keyword for this field.
   *@param refFld The value for the REFFLD (reference field) keyword
   *               for this field.
  **/
  public void setREFFLD(String refFld)
  {
    if (refFld == null)
    {
      throw new NullPointerException("refFld");
    }
    refFld_ = refFld;
  }


  /**
   *Sets the value to be specified for the REFFMT keyword for this field.
   *@param refFmt The value for the REFFMT (reference record format) keyword
   *               for this field.
  **/
  public void setREFFMT(String refFmt)
  {
    if (refFmt == null)
    {
      throw new NullPointerException("refFmt");
    }
    refFmt_ = refFmt;
  }


  /**
   *Sets the value to be specified for the REFLIB keyword for this field.
   *@param refLib The value for the REFLIB (reference library) keyword
   *               for this field.
  **/
  public void setREFLIB(String refLib)
  {
    if (refLib == null)
    {
      throw new NullPointerException("refLib");
    }
    refLib_ = refLib;
  }

  /**
   *Sets the value to be specified for the TEXT keyword for this field.
   *@param text The value for the TEXT keyword
   *            for this field.    The single quotes required to
   *            surround the TEXT keyword value are added by this class.
   *The <i>text</i> must be 50 characters or less in length.
  **/
  public void setTEXT(String text)
  {
    if (text == null)
    {
      throw new NullPointerException("text");
    }
    if (text.length() > 50)
    {
      throw new ExtendedIllegalArgumentException("text", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    text_ = text;
  }

  //@B0A
  /**
   *Indicates if the DFT keyword for this field is set to *NULL.
   *@return True if the DFT keyword is set to *NULL.
  **/
  public boolean isDFTNull()
  {
    return isDFTNull_;
  }
}
