 ///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Record.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;
import java.io.Serializable;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Vector;

/**
 *The Record class represents the data described by a RecordFormat object.
 *It can represent:
 *<ul>
 *<li>An entry in a data queue.
 *<li>The parameter data provided to or returned by a program call.
 *<li>A record to be written to or read from an AS/400 file.
 *<li>Any data returned from the AS/400 that needs to be converted
 *between AS/400 format and Java format.
 *</ul>
 *Record objects generate the following events:
 *<ul>
 *<li>{@link com.ibm.as400.access.RecordDescriptionEvent RecordDescriptionEvent}
 *<br>The events fired are:
 *<ul>
 *<li>fieldModified()
 *</ul>
 *<li>PropertyChangeEvent
 *<li>VetoableChangeEvent
 *</ul>
 *<b>Examples</b>
 *<ul>
 *<li><a href="../../../../recordxmp.html">Using the Record class with the Data queue classes</a>
 *<li><a href="../../../../RLReadFileExample.html">Using the Record class with the record-level database access classes</a>
 *</ul>
**/
public class Record implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    static final long serialVersionUID = 4L;


  // The AS400 data for this record.
  private byte[] as400Data_;
  // Array of the field descriptions that describe the fields in this record.
  // We initialize from RecordFormat.getFieldDescriptions() during construction
  // or setRecordFormat().
  private FieldDescription[] fieldDescriptions_;
  // Array of the fields that make up this record.
  private Object[] fields_;
  // Array containing the offset in as400Data_ for each field.
  // These values are only guaranteed to be valid if the record
  // does not contain dependent fields.
  private int[] fieldOffsets_;
  // Indicates if the record has dependent fields
  private boolean hasDependentFields_;
  // Indicates which fields have been converted to Java objects
  private boolean[] isConvertedToJava_;
  // Indicates which fields have been converted to AS400 data .in the as400Data_ array
  private boolean[] isConvertedToAS400_;
  // Name of the record
  private String name_ = "";
  // Array indicating if a field is null
  private boolean[] nullFieldMap_;
  // Use default property change support
  private transient PropertyChangeSupport changes_; //@B5C
  // The record format object with which this record is associated
  private RecordFormat recordFormat_ = null;
  // The length of the record, in bytes
  int recordLength_;
  // The record number of this record
  int recordNumber_;
  // Array to hold fields returned by getFields.  This allows us to not
  // instantiate a new array every time getFields is done, which allows
  // us to cut down on garbage collection overhead.
  Object[] returnFields_;
  // The list of current veto listeners
  transient private VetoableChangeSupport vetos_; //@B5A
//@B5D  transient private Vector currentVetoListeners_ = new Vector();
  // The list of current RecordDescriptionEvent listeners
//@B5D  transient private Vector currentRecordDescriptionListeners_ = new Vector();
  // The list of RecordDescriptionEvent listeners
  transient private Vector recordDescriptionListeners_; //@B5C
  // The list of veto listeners
//@B5D  transient private Vector vetoListeners_ = new Vector();

  /**
   *Constructs a Record object.
  **/
  public Record()
  {
    initializeTransient(); //@B5A
  }

  /**
   *Constructs a Record object. It uses the RecordFormat specified.
   *The contents of the record will be initialized to their default values. The default
   *values are determined as follows:
   *<ol>
   *<li>Use the value specified for the DFT keyword on the FieldDescription object
   *contained in the RecordFormat object for a particular field.
   *<li>If no value was specified for the DFT keyword, use the default value from the
   *AS400DataType object specified when constructing the FieldDescription object for
   *a particular field.
   *</ol>
   *@param recordFormat Describes the contents of this record.
   *@see com.ibm.as400.access.RecordFormat
  **/
  public Record(RecordFormat recordFormat)
  {
    initializeTransient(); //@B5A
    initializeRecord(recordFormat);
  }

  /**
   *Constructs a Record object. It uses the record's name and RecordFormat specified.
   *The contents of the record will be initialized to their default values. The default
   *values are determined as follows:
   *<ol>
   *<li>Use the value specified for the DFT keyword on the FieldDescription object for
   *a particular field.
   *<li>If no value was specified for the DFT keyword, use the default value from the
   *AS400DataType object specified when constructing the FieldDescription object for
   *a particular field.
   *</ol>
   *@param recordFormat Describes the contents of this record.
   *@param recordName The name to assign to the record.
  **/
  public Record(RecordFormat recordFormat, String recordName)
  {
    this(recordFormat);
    // Verify parameters
    if (recordName == null)
    {
      throw new NullPointerException("recordName");
    }
    // Set the name of the record
    name_ = recordName;
  }

  /**
   *Constructs a Record object. It uses the specified RecordFormat and a byte array
   *with which to initialize the contents of the record.
   *@param recordFormat Describes the contents of this record.
   *@param contents The contents to which to initialize the record.
   *<br>
   *<b>Note:</b> When using this object for the record level access classes, if
   *isVariableLength()
   *returns true for a field, the first two bytes of the data provided for
   *that field must contain the length of the valid data.  However, the number of bytes
   *provided for
   *the data for the field must equal the maximum field length for the field.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public Record(RecordFormat recordFormat, byte[] contents)
    throws UnsupportedEncodingException
  {
    this(recordFormat, contents, 0);
  }

  /**
   *Constructs a Record object. It uses the  specified the RecordFormat, a byte array
   *from which to initialize the contents of the record and the name of the record.
   *@param recordFormat Describes the contents of this record.
   *@param contents The contents to which to initialize the record.
   *<br>
   *<b>Note:</b> When using this object for the record level access classes, if isVariableLength()
   *returns true for a field, the first two bytes of the data provided for
   *that field must contain the length of the valid data.  However, the number of bytes provided for
   *the data for the field must equal the maximum field length for the field.
   *@param recordName The name to assign to the record.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public Record(RecordFormat recordFormat, byte[] contents, String recordName)
    throws UnsupportedEncodingException
  {
    this(recordFormat, contents, 0, recordName);
  }

  /**
   *Constructs a Record object. It uses the specified RecordFormat and a byte array
   *from which to initialize the contents of the record.
   *@param recordFormat Describes the contents of this record.  The <i>recordFormat</i>
   *must contain at least one field description.
   *@param contents The contents to which to initialize the record.
   *<br>
   *<b>Note:</b> When using this object for the record level access classes, if isVariableLength()
   *returns true for a field, the first two bytes of the data provided for
   *that field must contain the length of the valid data.  However, the number of bytes provided for
   *the data for the field must equal the maximum field length for the field.
   *@param offset The offset in <i>contents</i> at which to start.  The <i>offset</i> cannot
   *be less than zero.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public Record(RecordFormat recordFormat, byte[] contents, int offset)
    throws UnsupportedEncodingException
  {
    initializeTransient(); //@B5A
    // Verify parameters
    if (recordFormat == null)
    {
      throw new NullPointerException("recordFormat");
    }
    if (recordFormat.getNumberOfFields() == 0)
    {
      throw new ExtendedIllegalArgumentException("recordFormat", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (contents == null)
    {
      throw new NullPointerException("contents");
    }
    if (contents.length == 0)
    {
      throw new ExtendedIllegalArgumentException("contents", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    if (offset < 0)
    {
      throw new ExtendedIllegalArgumentException("offset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    // Set the instance data
    recordFormat_ = recordFormat;
    fieldDescriptions_ = recordFormat.getFieldDescriptions();
    hasDependentFields_ = recordFormat_.getHasDependentFields();
    fields_ = new Object[fieldDescriptions_.length];
    returnFields_ = new Object[fieldDescriptions_.length];
    fieldOffsets_ = new int[fieldDescriptions_.length];
    // Initialize the offset values
    isConvertedToJava_ = new boolean[fieldDescriptions_.length];
    isConvertedToAS400_ = new boolean[fieldDescriptions_.length];
    nullFieldMap_ = new boolean[fields_.length];
    FieldDescription fd = null;
    int fieldOffset = 0;
    int length = 0;
    // Reset the record length
    recordLength_ = 0;

    // This loop sets the offsets for the fields and also determines the
    // record length of the record.
    for (int i = 0; i < fields_.length; ++i)
    {
      fd = fieldDescriptions_[i];
      fieldOffsets_[i] = fieldOffset;
      if (fd instanceof VariableLengthFieldDescription)
      {
        if (((VariableLengthFieldDescription)fd).isVariableLength())
        { // Add two bytes for the length of the field
          fieldOffset += 2;
          recordLength_ += 2;
        }
      }
      length = fd.getDataType().getByteLength();
      fieldOffset += length;
      recordLength_ += length;
      if (!hasDependentFields_)
      {
        isConvertedToAS400_[i] = true;
      }
      isConvertedToJava_[i] = false;
    }
    // Allocate the space for as400Data_ now that we know teh record length
    as400Data_ = new byte[recordLength_];
    if (!hasDependentFields_)
    { // Go ahead and copy the contents to as400Data_
      try
      {
        System.arraycopy(contents, offset, as400Data_, 0, as400Data_.length);
      }
      catch(ArrayIndexOutOfBoundsException e)
      { // Need to reset isConverted... state
        for (int i = 0; i < isConvertedToAS400_.length; ++i)
        {
          isConvertedToAS400_[i] = false;
        }
        throw e;
      }
    }
    else
    {
      // Initialize the contents of the record with the specified byte array
      setContents(contents, offset);
    }
  }

  /**
   *Constructs a Record object. It uses the  specified RecordFormat, a byte array
   *from which to initialize the record's contents and the record's name.
   *@param recordFormat Describes the contents of this record.
   *@param contents The contents to which to initialize the record.
   *<br>
   *<b>Note:</b> When using this object for the record level access classes, if isVariableLength()
   *returns true for a field, the first two bytes of the data provided for
   *that field must contain the length of the valid data.  However, the number of bytes provided for
   *the data for the field must equal the maximum field length for the field.
   *@param offset The offset in <i>contents</i> at which to start.  The <i>offset</i> cannot
   *be less than zero.
   *@param recordName The name to assign to the record.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public Record(RecordFormat recordFormat, byte[] contents, int offset, String recordName)
    throws UnsupportedEncodingException
  {
    this(recordFormat, contents, offset);
    // Verify parameters
    if (recordName == null)
    {
      throw new NullPointerException("recordName");
    }

    // Set the name
    name_ = recordName;
  }

  /**
   *Adds a listener to be notified when the value of any bound
   *property is changed.  The <b>propertyChange</b> method will be
   *be called.
   *@see #removePropertyChangeListener
   *@param listener The PropertyChangeListener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener) //@B5C
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    changes_.addPropertyChangeListener(listener);
  }

  /**
   *Adds a listener to be notified when a RecordDescriptionEvent is fired.
   *@see #removeRecordDescriptionListener
   *@param listener The RecordDescriptionListener.
  **/
  public void addRecordDescriptionListener(RecordDescriptionListener listener) //@B5C
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    recordDescriptionListeners_.addElement(listener);
//@B5D    currentRecordDescriptionListeners_ = (Vector)recordDescriptionListeners_.clone();
  }

  /**
   *Adds a listener to be notified when the value of any constrained
   *property is changed.
   *The <b>vetoableChange</b> method will be called.
   *@see #removeVetoableChangeListener
   *@param listener The VetoableChangeListener.
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener) //@B5C
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
//@B5D    vetoListeners_.addElement(listener);
//@B5D    currentVetoListeners_ = (Vector)vetoListeners_.clone();
    vetos_.addVetoableChangeListener(listener); //@B5A
  }

  //@G0A
  /**
   * Tests this Record object for equality with the given object.
   * @param obj The Object to compare.
   * @return true if obj is a Record object and its record length, record number,
   * record name, dependent fields, field values, and key field values equal this Record's; 
   * false otherwise. Since there are so many pieces of data that determine whether
   * or not one Record equals another, the programmer may also want to consider using
   * Record.toString() and comparing the Strings for equality.
  **/
  public boolean equals(Object obj)
  {
    try
    {
      Record cmp = (Record)obj;
      if (cmp.recordLength_ == recordLength_ &&
          cmp.recordNumber_ == recordNumber_ &&
          cmp.hasDependentFields_ == hasDependentFields_ &&
          (cmp.name_ == null ? (name_ == null) : cmp.name_.equals(name_)))
      {
        int n = getNumberOfFields();
        int cn = cmp.getNumberOfFields();
        if (n == cn)
        {
          int nk = getNumberOfKeyFields();
          int cnk = getNumberOfKeyFields();
          if (nk == cnk)
          {
            for (int i=0; i<n; ++i)
            {
              Object obj1 = getField(i);
              Object obj2 = cmp.getField(i);
              if (obj1 == null)
              {
                if (obj2 != null) return false;
              }
              else
              {
                if (obj2 == null) return false;
                if (!obj1.equals(obj2)) return false;
              }
            }
          }
          else
          {
            return false;
          }
        }
        else
        {
          return false;
        }
      }
      else
      {
        return false;
      }
    }
    catch(Throwable t) // ClassCastException
    {
      return false;
    }
    return true;
  }


  /**
   *Fires the RecordDescriptionEvent.FIELD_MODIFIED event.
  **/
  private void fireFieldModifiedEvent()
  {
    //@B5C - fire events the "new" way
    Vector targets = (Vector)recordDescriptionListeners_.clone();
    RecordDescriptionEvent event = new RecordDescriptionEvent(this, RecordDescriptionEvent.FIELD_MODIFIED);
    for (int i=0; i<targets.size(); ++i)
    {
      RecordDescriptionListener target = (RecordDescriptionListener)targets.elementAt(i);
      target.fieldModified(event);
    }
  }

  /**
   *Returns the contents of this record as a byte array of AS400 data.
   *Each field's contents will be placed into the byte array
   *based on the field description for the
   *field that is provided by the record format specified on construction of this object.
   *The data type object for the field description will be used to do any necessary conversion
   *of the contents of the field to the byte array.<br>
   *<b>Note:</b> If a field is a variable-length field, the first two bytes of data for the field
   *contain the length of the valid data for the field.  However, the number of bytes provided for the
   *field is the maximum length of the field as specified by the FieldDescription object for the
   *field in the record format for this object.<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@return The contents of this record.
   *@exception CharConversionException If an error occurs when converting
   *the contents of a field to AS400 data.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the contents of a field to AS400 data.
  **/
  public byte[] getContents()
    throws CharConversionException,
           UnsupportedEncodingException
  {
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (hasDependentFields_)
    { // If the record has dependent fields, we do the conversion now and return the contents.

      // For each field in fields_, use the recordFormat_ object to get the field
      // description.  Use the data type object associated with the field description to translate
      // the Object representing the field into bytes to be placed in the byte array.
      // Repeat this process for each field.
      // If the field is a variable length field (i.e the VARLEN value is greater than 0), the first
      // two bytes of the data is the length of the data.

      // Determine the number of bytes that make of the record
      AS400DataType dType;
      int offset = 0;
      Trace.log(Trace.INFORMATION, "recordLength_: " + String.valueOf(recordLength_));
      byte[] toBytes = new byte[recordLength_];
      FieldDescription f;
      int variableFieldLength;
      for (int i = 0; i < fields_.length; ++i)
      { // Convert each field to AS400 data
        f = fieldDescriptions_[i];
        // Check for possible variable length field
        if (f instanceof VariableLengthFieldDescription)
        {
          if (((VariableLengthFieldDescription)f).isVariableLength())
          { // Set the two byte length portion of the contents
            if (fields_[i] == null)
            {
              variableFieldLength = 0;
            }
            else
            {
              variableFieldLength = (f instanceof HexFieldDescription)?
                ((byte[])fields_[i]).length : ((String)fields_[i]).length();
            }
            BinaryConverter.unsignedShortToByteArray(variableFieldLength, toBytes, offset);
            offset += 2;
          }
        }
        // Whether the field is variable length or not we still write out the maximum number
        // of bytes for the field.  This is the required format for variable length data for record
        // level access.
        dType = f.getDataType();
        if (fields_[i] != null)
        { // Field has a value; convert it to AS400 data
          offset += dType.toBytes(fields_[i], toBytes, offset);
        }
        else
        { // Field is null; use the default value for the AS400 data to serve as a place holder for
          // the field.
          offset += dType.toBytes(dType.getDefaultValue(), toBytes, offset);
        }
      }
      return toBytes;
    }
    // No dependent fields;
    // return as400Data_.
    return as400Data_;
  }

  /**
   *Writes the contents of this record to the specified output stream.
   *Each field's contents will be written to <i>out</i>
   *based on the field description for the
   *field that is provided by the record format specified on construction of this object.
   *The data type object for the field description will be used to do any necessary conversion
   *of the contents of the field.<br>
   *<b>Note:</b> If a field is a variable-length field, the first two bytes of data for the field
   *contain the length of the valid data for the field.  However, the number of bytes provided for the
   *field is the maximum length of the field as specified by the FieldDescription object for the
   *field in the record format for this object.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param out The stream to which to write the contents of the record.
   *@exception IOException If an I/O error occurs while communicating with the AS/400.
  **/
  public void getContents(OutputStream out)
    throws IOException
  {
    if (out == null)
    {
      throw new NullPointerException("out");
    }
    out.write(getContents());
  }

  /**
   *Returns the value of the field by index.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param index The position of the field in the record.  This value must
   *             be between 0 and getNumberOfFields() - 1 inclusive.
   *@return The contents of the requested field.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public Object getField(int index)
    throws UnsupportedEncodingException
  {
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    if (index < 0 || index > fields_.length - 1)
    {
      throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (isConvertedToJava_[index])
    { // Field already converted; just return it
      return fields_[index];
    }
    // Field has not yet been converted.  We will only get here if
    // the record has no dependent fields so we can go ahead and count
    // on the fieldOffsets_ array being valid.
    FieldDescription f = fieldDescriptions_[index];
    AS400DataType dType = f.getDataType();
    int variableFieldLength;
    int length = dType.getByteLength();
    int offset = fieldOffsets_[index];
    // Check for possible variable length field
    if (f instanceof VariableLengthFieldDescription)
    {
      if (((VariableLengthFieldDescription)f).isVariableLength())
      { // Get the number of bytes returned for the field
        variableFieldLength = BinaryConverter.byteArrayToUnsignedShort(as400Data_, offset);
        offset += 2;
        // Convert the AS400 data to a Java object
        if ((f instanceof HexFieldDescription))
        { // Field is a hex field, no conversion is done on the data
          byte[] b = new byte[variableFieldLength];
          System.arraycopy(as400Data_, offset, b, 0, variableFieldLength);
          fields_[index] = b;
        }
        else
        { // Field requires conversion based on ccsid
          //@B5D Converter c = new Converter(((AS400Text)dType).getCcsid()); //@B5C
          ConverterImpl c = ((AS400Text)dType).getConverter(); //@B5A @F0C
          fields_[index] = c.byteArrayToString(as400Data_, offset, variableFieldLength);
        }
      }
      else
      {
        // Field is not variable length
        fields_[index] = dType.toObject(as400Data_, offset);
      }
    }
    else
    {
      // Not a VariableLengthFieldDescription
      fields_[index] = dType.toObject(as400Data_, offset);
    }
    fireFieldModifiedEvent();
    isConvertedToJava_[index] = true;
    return fields_[index];
  }

  /**
   *Returns the value of the field by name.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param name The name of the field.
   *@return The contents of the requested field.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public Object getField(String name)
    throws UnsupportedEncodingException
  {
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    return getField(recordFormat_.getIndexOfFieldName(name));
  }

  /**
   *Returns the values of the fields in the record.
   *@return The values of the fields in the record.  An array of size zero is
   *returned if the record format has not been set.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public Object[] getFields()
    throws UnsupportedEncodingException
  {
    if (recordFormat_ == null)
    {
      return new Object[0];
    }

    // Get each field individually in the event that conversion has not yet been done.
    for (int i = 0; i < fields_.length; ++i)
    {
      returnFields_[i] = getField(i);
    }
    return returnFields_;
  }

  /**
   *Returns the values of the key fields in the record.
   *@return The values of the key fields in the record.
   *An array of length 0 is returned if the record format has not been set
   *or if no key fields exist.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public Object[] getKeyFields()
    throws UnsupportedEncodingException
  {
    if (recordFormat_ == null)
    {
      return new Object[0];
    }

    Object[] keyFields = new Object[recordFormat_.getNumberOfKeyFields()];
    String[] keyFieldNames = recordFormat_.getKeyFieldNames();
    for (int i = 0; i < keyFields.length; ++i)
    {
      keyFields[i] = getField(keyFieldNames[i]);
    }
    return keyFields;
  }


  // @A2A
  /**
   *Returns the values of the key fields in a byte array.
   *@return The values of the key fields in a byte array.
   *A byte array of length 0 is returned if the record format has not been set
   *or if no key fields exist.
  **/
  public byte[] getKeyFieldsAsBytes()
  {
    if (recordFormat_ == null)
    {
      return new byte[0];
    }

    ByteArrayOutputStream keyAsBytes = new ByteArrayOutputStream();
    String[] keyFieldNames = recordFormat_.getKeyFieldNames();
    AS400DataType dt = null;
    FieldDescription fd = null;
    int length;
    int index;
    for (int i = 0; i < keyFieldNames.length; ++i)
    {
      index = recordFormat_.getIndexOfFieldName(keyFieldNames[i]);
      fd = fieldDescriptions_[index];
      dt = fd.getDataType();
      length = dt.getByteLength();
      if (fd instanceof VariableLengthFieldDescription && ((VariableLengthFieldDescription)fd).isVariableLength())
      {
        length += 2;
      }
      keyAsBytes.write(as400Data_, fieldOffsets_[index], length);
    }
    return keyAsBytes.toByteArray();
  }



  /**
   *Returns the number of fields in this record.
   *@return The number of fields in this record.  Zero is returned if the record
   *format has not been set.
  **/
  public int getNumberOfFields()
  {
    return (recordFormat_ == null)? 0 : fields_.length;
  }

  /**
   *Returns the number of key fields in this record.
   *@return The number of key fields in this record.  Zero is returned if the record
   *format has not been set.
  **/
  public int getNumberOfKeyFields()
  {
    return  (recordFormat_ == null)? 0 : recordFormat_.getNumberOfKeyFields();
  }

  /**
   *Returns the record format for this record.
   *@return The record format for this record.  If the record format has
   *not been set, null is returned.
  **/
  public RecordFormat getRecordFormat()
  {
    return recordFormat_;
  }

  /**
   *Returns the record length of this record.
   *@return The record length of this record. Zero is returned if the record format
   *for this object has not been set.
  **/
  public int getRecordLength()
  {
    return recordLength_;
  }

  /**
   *Returns the record name for this record.
   *@return The name of this record.  If the name has not been set,
   *an empty string is returned.
  **/
  public String getRecordName()
  {
    return name_;
  }

  /**
   *Returns the record number of this record.  This method only pertains to the
   *record level access classes.
   *@return The record number of this record.  Zero is returned if no record number has been set.
  **/
  public int getRecordNumber()
  {
    return recordNumber_;
  }

  //@G0A
  /**
   * Returns a hash code value for this Record. This is useful if Record objects
   * need to be placed into Hashtables.
   * @return The hash code.
  **/
  public int hashCode()
  {
    return recordNumber_ == 0 ? recordLength_ : recordNumber_;
  }

  /**
   *Initializes this record to its default values based on the specified record format.
   *@param recordFormat The record format for this record.
  **/
  private void initializeRecord(RecordFormat recordFormat)
  {
    // Verify parameters
    if (recordFormat == null)
    {
      throw new NullPointerException("recordFormat");
    }
    if (recordFormat.getNumberOfFields() == 0)
    {
      throw new ExtendedIllegalArgumentException("recordFormat", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    // Set the instance data
    recordFormat_ = recordFormat;
    fieldDescriptions_ = recordFormat.getFieldDescriptions();
    hasDependentFields_ = recordFormat_.getHasDependentFields();
    fields_ = new Object[fieldDescriptions_.length];
    returnFields_ = new Object[fieldDescriptions_.length];
    fieldOffsets_ = new int[fieldDescriptions_.length];
    isConvertedToJava_ = new boolean[fieldDescriptions_.length];
    isConvertedToAS400_ = new boolean[fieldDescriptions_.length];
    nullFieldMap_ = new boolean[fieldDescriptions_.length];
    // Initialize the contents of the record to the fieldDescription's default value.
    // Initialize the nullFieldMap_ to false values.  Initialize the fieldOffsets_ array
    // to the appropriate offset.
    Object obj;
    FieldDescription fd = null;
    AS400DataType dType = null;
    byte[] lenAsBytes = new byte[2];
    int offset = 0;
    int variableFieldLength;
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    // Reset recordLength_
    recordLength_ = 0;
    for (int i = 0; i < fields_.length; ++i)
    {
      // Get the default value for this field
      fd = fieldDescriptions_[i];
      obj = fd.getDFT();
      // Set the offset of this field
      fieldOffsets_[i] = offset;
      // Set the Java value of the field
      // @B0C: Check for DFT of current or null
      if (obj == null) // DFT was not set, or was set to null or current
      {
        if (fd.isDFTNull()) // DFT was set to null => write 'null' to the field
        {
          fields_[i] = fd.getDataType().getDefaultValue();
          if (fieldDescriptions_[i].getALWNULL())
            nullFieldMap_[i] = true;
          else
            nullFieldMap_[i] = false;
        }
        else if (fd.isDFTCurrent()) // DFT was set to current => write the appropriate value
        {
          fields_[i] = fd.getDFTCurrentValue();
        }
        else // DFT was not set => write 'null' to the field
        {
          fields_[i] = fd.getDataType().getDefaultValue();
          if (fieldDescriptions_[i].getALWNULL())
          {
            nullFieldMap_[i] = true;
            fields_[i] = null; //@B1A Set the field value to null as well.
          }
          else
            nullFieldMap_[i] = false;
        }
      }
      else
      {
        fields_[i] = obj; // DFT was set => write the default value
      }
      // Check for possible variable length field
      if (fd instanceof VariableLengthFieldDescription)
      {
        if (((VariableLengthFieldDescription)fd).isVariableLength())
        { // Set the two byte length portion of the contents
          if (obj == null)
          {
            variableFieldLength = 0;
          }
          else
          {
            variableFieldLength = (fd instanceof HexFieldDescription)?
              ((byte[])obj).length : ((String)obj).length();
          }
          BinaryConverter.unsignedShortToByteArray(variableFieldLength, lenAsBytes, 0);
          b.write(lenAsBytes, 0, 2);
          offset += 2;
          recordLength_ += 2;
        }
      }
      // Whether the field is variable length or not we still write out the maximum number
      // of bytes for the field.  This is the required format for variable length data for record
      // level access.
      dType = fd.getDataType();
      if (fields_[i] != null) //@B1A
      { // Field has a value; convert it to AS400 data
        b.write(dType.toBytes(fields_[i]), 0, dType.getByteLength());
      }
      else //@B1A
      { // Field is null; use the default value for the AS400 data to serve as a place holder for
        // the field.
        b.write(dType.toBytes(dType.getDefaultValue()), 0, dType.getByteLength()); //@B1A
      }

      // Indicate the field is in a converted state
      isConvertedToJava_[i] = true;
      isConvertedToAS400_[i] = true;

      // Determine the next offset value and increment the record length appropriately
      offset += fd.getDataType().getByteLength();
      recordLength_ += fd.getDataType().getByteLength();
    }
    // Allocate the space for as400Data_
    as400Data_ = b.toByteArray();
    fireFieldModifiedEvent();
  }

  //@D0A
  void initializeTextObjects(AS400 system)
  {
    // First do our record format
    if (recordFormat_ != null) recordFormat_.initializeTextObjects(system);
    // Then do all of our internal fields
    if (fieldDescriptions_ != null)
    {
      for (int i=0; i<fieldDescriptions_.length; ++i)
      {
        AS400DataType dt = fieldDescriptions_[i].dataType_;
        if (dt instanceof AS400Text)
        {
          ((AS400Text)dt).setConverter(((AS400Text)dt).getConverter()); //@F0C
        }
      }
    }
  }

  //@B5A
  /**
   * Initialize transient data.
  **/
  private void initializeTransient()
  {
    recordDescriptionListeners_ = new Vector();
    vetos_ = new VetoableChangeSupport(this);
    changes_ = new PropertyChangeSupport(this);
  }

  /**
   *Indicates if the field is null.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param index The index of the field.  The <i>index</i> must be between
   *0 and getNumberOfFields() - 1.
   *@return true if the field is null; false otherwise.
  **/
  public boolean isNullField(int index)
  {
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    if (index < 0 || index > fields_.length - 1)
    {
      throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    return nullFieldMap_[index];
  }

  /**
   *Indicates if the field is null.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param name The name of the field.
   *@return true if the field is null; false otherwise.
  **/
  public boolean isNullField(String name)
  {
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    return nullFieldMap_[recordFormat_.getIndexOfFieldName(name)];
  }

  /**
   *Reads the number of bytes requested. This method will loop until
   *either <i>length</i> bytes have been read or until the end of file is reached.
   *@param in The Inputstream from which to read.
   *@param buf The buffer from which to read the data.
   *@param offset The offset within buf array from which to start reading.
   *@param length The number of bytes to read.
   *@returns The number of bytes read.
   *@exception IOException If an I/O error occurs while communicating with the AS/400.
   **/
  private int  readFromStream(InputStream in, byte[] buf, int offset, int length)
    throws IOException
  {
    boolean endOfFile = false;
    int     bytesRead = 0;
    while ((bytesRead < length) && !endOfFile)
    {
      int temp = in.read(buf, offset+bytesRead, length - bytesRead);
      if (temp == -1)
      {
        endOfFile = true;
      }
      else
      {
        bytesRead += temp;
      }
    }
    return bytesRead;
  }

  /**
   *Overrides the ObjectInputStream.readObject() method in order to return any
   *transient parts of the object to there properly initialized state.
   * I.e we in effect
   *call the null constructor.  By calling ObjectInputStream.defaultReadObject()
   *we restore the state of any non-static and non-transient variables.  We
   *then continue on to restore the state (as necessary) of the remaining varaibles.
   *@param in The input stream from which to deserialize the object.
   *@exception ClassNotFoundException If the class being deserialized is not found.
   *@exception IOException If an error occurs while communicating to the AS/400.
  **/

  private void readObject(java.io.ObjectInputStream in)
    throws ClassNotFoundException,
           IOException
  {
    in.defaultReadObject();
    initializeTransient(); //@B5A
//@B5D    currentVetoListeners_ = new Vector();
//@B5D    currentRecordDescriptionListeners_ = new Vector();
//@B5D    recordDescriptionListeners_ = new Vector();
//@B5D    vetoListeners_ = new Vector();
  }

  /**
   *Removes a listener from the change list.
   *If the listener is not on the list, do nothing.
   *@see #addPropertyChangeListener
   *@param listener The PropertyChangeListener.
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener) //@B5C
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    changes_.removePropertyChangeListener(listener);
  }

  /**
   *Removes a listener from the  record description listeners list.
   *If the listener is not on the list, do nothing.
   *@see #addRecordDescriptionListener
   *@param listener The RecordDescriptionListener.
  **/
  public void removeRecordDescriptionListener(RecordDescriptionListener listener) //@B5C
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    recordDescriptionListeners_.removeElement(listener);
//@B5D    currentRecordDescriptionListeners_ = (Vector)recordDescriptionListeners_.clone();
  }

  /**
   *Removes a listener from the veto change listeners list.
   *If the listener is not on the list, do nothing.
   *@see #addVetoableChangeListener
   *@param listener The VetoableChangeListener.
  **/
  public void removeVetoableChangeListener(VetoableChangeListener listener) //@B5C
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
//@B5D    vetoListeners_.removeElement(listener);
//@B5D    currentVetoListeners_ = (Vector)vetoListeners_.clone();
    vetos_.removeVetoableChangeListener(listener); //@B5A
  }

  /**
   *Sets the contents of this record from the specified byte array.
   *The contents of each field will be set from <i>contents</i>
   *based on the field description for the
   *field that is provided by the record format specified on construction of this object.
   *The data type object for the field description will be used to do any necessary conversion
   *of the data from the byte array.<br>
   *<b>Note:</b> When using this object for the record level access classes, if isVariableLength()
   *returns true for a field, the first two bytes of the data provided for
   *that field must contain the length of the valid data.  However, the number of bytes provided for
   *the data for the field must equal the maximum field length for the field.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param contents The data with which to set the contents of this record.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public void setContents(byte[] contents)
    throws UnsupportedEncodingException
  {
    setContents(contents, 0);
  }

  /**
   *Sets the contents of this record from the specified byte array.
   *The contents of each field will be set from <i>contents</i>
   *based on the field description for the
   *field that is provided by the record format specified on construction of this object.
   *The data type object for the field description will be used to do any necessary conversion
   *of the data from the byte array.<br>
   *<b>Note:</b> When using this object for the record level access classes, if isVariableLength()
   *returns true for a field, the first two bytes of the data provided for
   *that field must contain the length of the valid data.  However, the number of bytes provided for
   *the data for the field must equal the maximum field length for the field.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param contents The data with which to set the contents of this record.
   *@param offset The offset in <i>contents</i> at which to start.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  public void setContents(byte[] contents, int offset)
    throws UnsupportedEncodingException
  {
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (contents == null)
    {
      throw new NullPointerException("contents");
    }
    if (contents.length == 0)
    {
      throw new ExtendedIllegalArgumentException("contents", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    if (offset < 0)
    {
      throw new ExtendedIllegalArgumentException("offset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (hasDependentFields_)
    { // Record has dependent fields; convert now.
      // For each field description in the recordFormat_ object, use the field description's
      // data type object to translate the appropriate number of bytes in contents to an Object.
      // Repeat this process for each field description in the record format.
      // If the field is a variable length field, we must account for the two bytes worth of length
      // data which precede the actual data for the field.
      AS400DataType dType;
      int numFields = recordFormat_.getNumberOfFields();
      FieldDescription f;
      int lengthDependField;
      int offsetDependField;
      int variableFieldLength;
      recordLength_ = 0; // Reset the record length
      for (int i = 0; i < numFields; ++i)
      {
        f = fieldDescriptions_[i];
        lengthDependField = recordFormat_.getLengthDependency(i);
        offsetDependField = recordFormat_.getOffsetDependency(i);
        dType = f.getDataType();
        int length = dType.getByteLength();

        // Check for offset dependent field
        if (offsetDependField != -1)
        { // Set offset to value specified in the field this field depends on
          offset = ((Number)fields_[offsetDependField]).intValue();
        }
        if (lengthDependField != -1)
        {
          AS400DataType newDataType = null;
          // The length of this field is contained in the field that this field depends on
          // Because the field depended on must exist prior to this field in the byte array,
          // its value has already been determined.
          length = ((Number)fields_[lengthDependField]).intValue();
          // @A1A: Check if f is variable length..  If so
          // we need to bump the offset by two
          // to get to the actual data and bump the record length by two.
          boolean varLen = false;
          if (f instanceof VariableLengthFieldDescription && ((VariableLengthFieldDescription)f).isVariableLength())
          {
            offset += 2;
            recordLength_ += 2;
            varLen = true;
          }
          // End @A1A

          // Convert the AS400 data to a Java object
          if (f instanceof HexFieldDescription)
          { // Field is a hex field, setDataType to indicate correct length
            newDataType = new AS400ByteArray(length);
          }
          else if (f instanceof ArrayFieldDescription)
          {
            newDataType = new AS400Array(((AS400Array)dType).getType(), length);
          }
          else
          { // character field - setDataType to indicate correct length
            AS400Text dtText = (AS400Text)dType; //@B6A
            //@B6D newDataType = new AS400Text(length, ((AS400Text)dType).getCcsid());
            newDataType = new AS400Text(length, dtText.getCcsid()); //@F0C
            ((AS400Text)newDataType).setConverter(dtText.getConverter()); //@F0C
          }
          if (!varLen)  //@A1A: If field is variable length need to preserve
          {             //      maximum field length
            f.setDataType(newDataType);
          }
          fields_[i] = newDataType.toObject(contents, offset);
          recordLength_ += f.getDataType().getByteLength();
        }
        // Check for possible variable length field
        else if (f instanceof VariableLengthFieldDescription)
        {
          if (((VariableLengthFieldDescription)f).isVariableLength())
          { // Get the number of bytes returned for the field
            variableFieldLength = BinaryConverter.byteArrayToUnsignedShort(contents, offset);
            offset += 2;
            recordLength_ += 2;
            // Convert the AS400 data to a Java object
            if ((f instanceof HexFieldDescription))
            { // Field is a hex field, no conversion is done on the data
              byte[] b = new byte[variableFieldLength];
              System.arraycopy(contents, offset, b, 0, variableFieldLength);
              fields_[i] = b;
            }
            else
            { // Field requires conversion based on ccsid
              //@B5D Converter c = new Converter(((AS400Text)dType).getCcsid()); //@B5C
              ConverterImpl c = ((AS400Text)dType).getConverter(); //@B5A @F0C
              fields_[i] = c.byteArrayToString(contents, offset, variableFieldLength);
            }
            recordLength_ += dType.getByteLength();
          }
          else
          { // Field is not variable length
            fields_[i] = dType.toObject(contents, offset);
            recordLength_ += dType.getByteLength();
          }
        }
        else
        { // Not a VariableLengthFieldDescription
          fields_[i] = dType.toObject(contents, offset);
          recordLength_ += dType.getByteLength();
        }
        // Whether the field is variable length or not, we are always given the maximum
        // number of bytes for the field in the byte array.
        offset += length;
        isConvertedToJava_[i] = true;
        isConvertedToAS400_[i] = false;
      }
      fireFieldModifiedEvent();
    }
    else
    { // No dependent fields; we can convert on the fly as necessary
      System.arraycopy(contents, offset, as400Data_, 0, as400Data_.length);
      // Indicate that no fields have been converted yet
      for (int i = 0; i < isConvertedToJava_.length; ++i)
      {
        isConvertedToJava_[i] = false;
        isConvertedToAS400_[i] = true;
      }
    }
  }

  /**
   *Sets the contents of this record from the specified input stream.
   *The contents of each field will be set from <i>in</i> based on the field description for the
   *field that is provided by the record format specified on construction of this object.
   *The data type object for the field description will be used to do any necessary conversion
   *of the data from the input stream.<br>
   *<b>Note:</b> When using this object for the record level access classes, if isVariableLength()
   *returns true for a field, the first two bytes of the data provided for
   *that field must contain the length of the data.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param in The stream from which to read the data.
   *@exception IOException If an I/O error occurs while communicating with the AS/400.
  **/
  public void setContents(InputStream in)
    throws IOException
  {
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (in == null)
    {
      throw new NullPointerException("in");
    }

    AS400DataType dType;
    byte[] contents = new byte[recordLength_];
    if (readFromStream(in, contents, 0, recordLength_) != recordLength_)
    {
      throw new ExtendedIOException("Unable to read " + String.valueOf(recordLength_) + "bytes", ExtendedIOException.UNKNOWN_ERROR);
    }
    setContents(contents, 0);
  }

  /**
   *Sets the contents of the field at <i>index</i> to <i>value</i>.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param index The position in this record of the field whose contents are to be set.  The <i>index</i> must be between 0 and getNumberOfFields() - 1.
   *@param value The value to which to set the contents of the field.  Specify null for
   *<i>value</i> to indicate that the field is null.
  **/
  public void setField(int index, Object value)
  {
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    if (index < 0 || index > fields_.length - 1)
    {
      throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }


    fields_[index] = value;
    fireFieldModifiedEvent();
    if (value == null)
    {
      nullFieldMap_[index] = true;
    }
    //@B0A
    else
    {
      nullFieldMap_[index] = false;
    }
    isConvertedToJava_[index] = true;
    if (!hasDependentFields_)
    { // Keep the contents of as400Data_ up to date so that
      // getContents does not need to do any translation
      int offset = fieldOffsets_[index];
      int variableFieldLength;
      FieldDescription f = fieldDescriptions_[index];
      // Check for possible variable length field
      if (f instanceof VariableLengthFieldDescription)
      {
        if (((VariableLengthFieldDescription)f).isVariableLength())
        { // Set the two byte length portion of the contents
          if (fields_[index] == null)
          {
            variableFieldLength = 0;
          }
          else
          {
            variableFieldLength = (f instanceof HexFieldDescription)?
              ((byte[])fields_[index]).length : ((String)fields_[index]).length();
          }
          BinaryConverter.unsignedShortToByteArray(variableFieldLength, as400Data_, offset);
          offset += 2;
        }
      }
      // Whether the field is variable length or not we still write out the maximum number
      // of bytes for the field.  This is the required format for variable length data for record
      // level access.
      AS400DataType dType = f.getDataType();
      if (fields_[index] != null)
      { // Field has a value; convert it to AS400 data
        dType.toBytes(fields_[index], as400Data_, offset);
      }
      else
      { // Field is null; use the default value for the AS400 data to serve as a place holder for
        // the field.
        dType.toBytes(dType.getDefaultValue(), as400Data_, offset);
      }
      // Indicate the field is in a converted state
      isConvertedToAS400_[index] = true;
    }
    else
    {
      isConvertedToAS400_[index] = false;
    }
  }

  /**
   *Sets the contents of the field with the specified name to <i>value</i>.
   *<br>
   *The record format for the record must be set prior to invoking this method.
   *@see Record#Record(com.ibm.as400.access.RecordFormat)
   *@see Record#setRecordFormat
   *@param name The name of the field whose contents are to be set.
   *@param value The value to which to set the contents of the field.  Specify null for
   *<i>value</i> to indicate that the field is null.
  **/
  public void setField(String name, Object value)
  {
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    setField(recordFormat_.getIndexOfFieldName(name), value);
  }

  /**
   *Sets the record format for this record.
   *@param recordFormat The record format for this record.
   *@exception PropertyVetoException If a change is vetoed.
  **/
  public void setRecordFormat(RecordFormat recordFormat)
    throws PropertyVetoException
  {
    if (recordFormat == null)
    {
      throw new NullPointerException("recordFormat");
    }
    //@B5C - fire events the "new" way
    // Notify veto listeners of the change
    vetos_.fireVetoableChange("recordFormat", recordFormat_, recordFormat);
    RecordFormat old = recordFormat_;
    initializeRecord(recordFormat);
    changes_.firePropertyChange("recordFormat", old, recordFormat_);
  }

  /**
   *Sets the name for this record.
   *@param name The name for this record.
   *@exception PropertyVetoException If a change is vetoed.
  **/
  public void setRecordName(String name)
    throws PropertyVetoException
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    //@B5C - fire events the "new" way
    // Notify veto listeners of the change
    vetos_.fireVetoableChange("recordName", name_, name);
    String old = name_;
    name_ = name;
    changes_.firePropertyChange("recordName", old, name_);
  }

  /**
   *Sets the record number of this record.  This method only pertains to the record
   *level access class SequentialFile when a write or update by record number
   *is being done.
   *@param recordNumber The record number of this record.  The
   *<i>recordNumber</i> must be greater than 0.
   *@exception PropertyVetoException If a change is vetoed.
  **/
  public void setRecordNumber(int recordNumber)
    throws PropertyVetoException
  {
    if (recordNumber < 0)
    {
      throw new ExtendedIllegalArgumentException("recordNumber", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    //@B5C - fire events the "new" way
    // Notify veto listeners of the change
    Integer old = new Integer(recordNumber_);
    Integer newnum = new Integer(recordNumber);
    vetos_.fireVetoableChange("recordNumber", old, newnum);
    recordNumber_ = recordNumber;
    changes_.firePropertyChange("recordNumber", old, newnum);
  }

  /**
   *Returns the contents of this record formatted as a String.  If a field is null,
   *"null" is substituted for the contents of the field in the string representation
   *of this record.
   *@return The contents of this record.  The empty string is returned if the
   *record has not contents.
  **/
  public String toString()
  {
    if (recordFormat_ == null)
    {
      return "";
    }
    StringBuffer theRecord = new StringBuffer(0);
    Object obj = null;
    for (int i = 0; i < fields_.length; ++i)
    { // Append each field as a String to theRecord.  Separate each field's
      // contents with a single space.
      try
      {
        obj = getField(i);
      }
      catch(UnsupportedEncodingException e)
      {
        // Unable to convert field; set to null.  We do this because
        // we can't throw an exception from toString()
        obj = null;
      }
      catch(Exception e) //@D0A
      {
        // Some other exception; probably due to the fact that
        // this Record object hasn't had its AS400Text objects
        // filled in.
        obj = "???"; //@D0A
      }
      if (obj == null)
      {
        theRecord.append("null");
      }
      else
      {
        if (obj instanceof byte[])
        {
          theRecord.append(new String((byte[])obj));
        }
        else
        {
          theRecord.append(obj.toString());
        }
      }
      theRecord.append(" ");
    }
    // We return all but the last character which is the last blank we added
    return theRecord.toString().substring(0, theRecord.length() - 1);
  }
}
