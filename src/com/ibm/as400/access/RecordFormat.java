///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Serializable;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport; //@B0A
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 *The RecordFormat class represents the format of data returned from an AS/400 system.
 *It contains FieldDescription objects that describe the
 *data returned from an AS/400.  The RecordFormat class is used to generate a Record
 *object that can be used to access the data returned from the AS/400 as Java objects or
 *as byte arrays of AS/400 data.
 *For instance, the entries on an AS/400 data queue
 *may have a specific format.  This format could be represented by a
 *RecordFormat object. The RecordFormat object could be used to generate a Record
 *object containing the data read from the data queue.  Based on the description of the
 *data contained in the RecordFormat object, the Record object could be used by the Java
 *program to handle the data as Java objects. As another example, a parameter for a program
 *may be
 *an array of bytes representing several different types of data.  Such a parameter
 *could be described by a RecordFormat object.
 *<p>
 *The RecordFormat class is also used to describe the record format of a file when using
 *the record-level database access classes.  The record format of the file must be set prior
 *to invoking the open() method on an AS400File object.
 *<p>
 *The RecordFormat class is also used to describe the record format of a record when using
 *the LineDataRecordWriter class.  The following record format attributes are required to be
 *set.
 *<ul>
 *<li>Record format ID
 *<li>Record format type
 *<li>Field descriptions that make up the record format
 *<li>The delimiter, when the record format type is VARIABLE_LAYOUT_LENGTH
 *<li>Field description layout attributes,length and alignment, when the record format
 *is FIXED_LAYOUT_LENGTH
 *</ul>
 *<p>
 *The RecordFormat class allows the user to do the following:
 *<ul>
 *<li>Describe the data returned from an AS/400.
 *<li>Retrieve a Record object containing data that is described by the RecordFormat.
 *</ul>
 *RecordFormat objects generate the following events:
 *<ul>
 *<li>{@link com.ibm.as400.access.RecordDescriptionEvent RecordDescriptionEvent}
 *<br>The events fired are:
 *<ul>
 *<li>fieldDescriptionAdded()
 *<li>keyFieldDescriptionAdded()
 *</ul>
 *<li>PropertyChangeEvent
 *<li>VetoableChangeEvent
 *</ul>
 *<b>Examples</b>
 *<ul>
 *<li><a href="doc-files/recordxmp.html">Using the RecordFormat class with the Data queue classes</a>
 *<li><a href="doc-files/RLReadFileExample.html">Using the RecordFormat class with the record-level database access classes</a>
 *<li><a href="doc-files/LDRWExample.html">Using the RecordFormat class with the LineDataRecordWriter class</a>
 *</ul>
 *@see AS400FileRecordDescription
**/
public class RecordFormat implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  /** Constant indicating the layout length of all fields is fixed. **/ // @C1A
  /** This constant is only used for record level writing.          **/ // @C1A
  public static final int FIXED_LAYOUT_LENGTH    = 1;       // @C1A
  /** Constant indicating the layout length of all fields is variable. **/ //@C1A
  /** This constant is only used for record level writing.          **/ // @C1A
  public static final int VARIABLE_LAYOUT_LENGTH = 2;       // @C1A
  // The delimiter used for printing variable length field records  @C1A
  private char delimiter_;   // @C1A

  // The fieldDescriptions that make up this record format.
  private Vector fieldDescriptions_ = new Vector();
  // Hashtable mapping the field names to their index in fieldDescriptions_
  private Hashtable fieldNameToIndexMap_ = new Hashtable();
  // Indicates if this format contains dependent fields
  private boolean hasDependentFields_;
  // The keyFieldDescriptions that make up this record format.
  private Vector keyFieldDescriptions_ = new Vector();
  // Hashtable mapping the key field names to their index in keyFieldDescriptions_
  private Hashtable keyFieldNameToIndexMap_ = new Hashtable();
  // Contains the index of the field depended on for length by the field description specified by the
  // the index into this Vector.
  private Vector lengthDependentFields_ = new Vector();
  // Name of this record format
  private String name_ = "";
  // Constant used to indicate that a field is not a dependent field
  private static final Integer NOT_DEPENDENT_ = new Integer(-1);
  // Contains the index of the field depended on for offset by the field description specified by the
  // the index into this Vector.
  private Vector offsetDependentFields_ = new Vector();
  // The record format type   @C1A
  private int recordFormatType_;  // @C1A
  // The record format ID     @C1A
  private String recordFormatID_ = "";             // @C1A

  // Transient data.
  transient private PropertyChangeSupport changes_; //@B0C
//@B0D  transient private Vector currentVetoListeners_ = new Vector(); //@B0C
//@B0D  transient private Vector currentRecordDescriptionListeners_ = new Vector(); //@B0C
  transient private Vector rdListeners_; //@B0C
  transient private VetoableChangeSupport vetos_; //@B0C

  /**
   *Constructs a RecordFormat object.<br>
   *<b>Note:</b> When using this object with the record level access classes,
   *the version of the constructor that takes <i>name</i> must be used.
   *@see RecordFormat#RecordFormat(java.lang.String)
  **/
  public RecordFormat()
  {
    initializeTransient(); //@B0A
  }

  /**
   *Constructs a RecordFormat object. It uses the name specified.<br>
   *<b>Note:</b> Use this version of the constructor when the object is
   *being used with the record level access classes.
   *@param name The name of the record format.  The <i>name</i> is converted
   *to uppercase by this method.  When using this object with the record level
   *access classes, the <i>name</i> must be the name of the record format for
   *the AS/400 file that is being described.
  **/
  public RecordFormat(String name)
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    name_ = name.toUpperCase();
    initializeTransient(); //@B0A
  }

  /**
   *Adds a field description to this record format.  The field description
   *is added to the end of the field descriptions in this object.
   *@param field The field description to be added.
  **/
  public void addFieldDescription(FieldDescription field)
  {
    if (field == null)
    {
      throw new NullPointerException("field");
    }
    // Map the name to the appropriate index
    fieldNameToIndexMap_.put(field.getFieldName(), new Integer(fieldDescriptions_.size()));
    // Add the field to the field descriptions
    fieldDescriptions_.addElement(field);
    // Indicate that this field is not a dependent field
    lengthDependentFields_.addElement(NOT_DEPENDENT_);
    // Indicate that this field is not a dependent field
    offsetDependentFields_.addElement(NOT_DEPENDENT_);

    // Fire FIELD_DESCRIPTION_ADDED event
    //@B0D - removed event firing code block
    fireEvent(RecordDescriptionEvent.FIELD_DESCRIPTION_ADDED); //@B0A
  }

  /**
   *Adds a key field description to this record format.
   *The key field description is determined by the index of a field description
   *that was already added to this object. The key field description
   *is added to the end of the key field descriptions in this object.
   *The order in which the key field descriptions are added must match
   *the order of the key fields in the files for which this record format
   *is meant.
   *@param index The index of a field description that was already
   *added to this object via addFieldDescription().  The <i>index</i> must
   *be in the range zero to getNumberOfFields() - 1.
  **/
  public void addKeyFieldDescription(int index)
  {
    if (index < 0 || index > fieldDescriptions_.size() - 1)
    {
      throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    FieldDescription f = (FieldDescription)fieldDescriptions_.elementAt(index);
    // Map the name to the appropriate index
    keyFieldNameToIndexMap_.put(f.getFieldName(), new Integer(keyFieldDescriptions_.size()));
    // Add the field to the key field descriptions
    keyFieldDescriptions_.addElement(f);

    // Fire KEY_FIELD_DESCRIPTION_ADDED event
    //@B0D - removed event firing code block
    fireEvent(RecordDescriptionEvent.KEY_FIELD_DESCRIPTION_ADDED); //@B0A
  }


  /**
   *Adds a key field description to this record format.
   *The key field description is determined by the name of a field description
   *that was already added to this object. The key field description
   *is added to the end of the key field descriptions in this object.
   *The order in which the key field descriptions are added must match
   *the order of the key fields in the files for which this record format
   *is meant.
   *@param name The name of a field description that was already
   *added to this object via addFieldDescription().  The <i>name</i> is
   *case sensitive.
  **/
  public void addKeyFieldDescription(String name)
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    addKeyFieldDescription(getIndexOfFieldName(name));
  }

  /**
   *Adds a listener to be notified when the value of any bound
   *property is changed.  The <b>propertyChange</b> method will be
   *called.
   *@see #removePropertyChangeListener
   *@param listener The PropertyChangeListener.
  **/
  public /*@B0D synchronized*/ void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    if (changes_ == null) changes_ = new PropertyChangeSupport(this);
    changes_.addPropertyChangeListener(listener);
  }

  /**
   *Adds a listener to be notified when a RecordDescriptionEvent is fired.
   *@see #removeRecordDescriptionListener
   *@param listener The RecordDescriptionListener.
  **/
  public /*@B0D synchronized*/ void addRecordDescriptionListener(RecordDescriptionListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    if (rdListeners_ == null) rdListeners_ = new Vector();
    rdListeners_.addElement(listener); //@B0C
//@B0D    currentRecordDescriptionListeners_ = (Vector)recordDescriptionListeners_.clone();
  }

  /**
   *Adds a listener to be notified when the value of any constrained
   *property is changed.
   *The <b>vetoableChange</b> method will be called.
   *@see #removeVetoableChangeListener
   *@param listener The VetoableChangeListener.
  **/
  public /*@B0D synchronized*/ void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    if (vetos_ == null) vetos_ = new VetoableChangeSupport(this);
    vetos_.addVetoableChangeListener(listener); //@B0C
//@B0D    currentVetoListeners_ = (Vector)vetoListeners_.clone();
  }

//@B0D - removed getCopyright() method.


  //@B0A
  /**
   * Fire the appropriate event.
  **/
  private void fireEvent(int index)
  {
    if (rdListeners_ == null) return;
    Vector targets = (Vector)rdListeners_.clone();
    RecordDescriptionEvent event = new RecordDescriptionEvent(this, index);
    for (int i=0; i<targets.size(); ++i)
    {
      RecordDescriptionListener target = (RecordDescriptionListener)targets.elementAt(i);
      switch(index)
      {
        case RecordDescriptionEvent.FIELD_DESCRIPTION_ADDED:
          target.fieldDescriptionAdded(event);
          break;
        case RecordDescriptionEvent.KEY_FIELD_DESCRIPTION_ADDED:
          target.keyFieldDescriptionAdded(event);
          break;
        default:
          break;
      }
    }
  }

  //@C1A
  /**
   * Returns the delimiter.  The delimiter is the character
   * used to separate variable length fields when the record is
   * written using the line data record writer class.  This value is only
   * valid when the record format type is VARIABLE_LAYOUT_LENGTH.
   *
   * @return  The delimiter.
  **/
  public char getDelimiter()
  {
    return delimiter_;
  }


  /**
   *Returns the field description at the specified index.
   *@param index The index of the field description.  The <i>index</i> must
   *be in the range zero to getNumberOfFields() - 1.
   *@return The field description.
  **/
  public FieldDescription getFieldDescription(int index)
  {
    if (index < 0 || index > fieldDescriptions_.size() - 1)
    {
      throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    return (FieldDescription)fieldDescriptions_.elementAt(index);
  }

  /**
   *Returns the field description with the specified name.
   *@param name The name of the field description.  The <i>name</i> is
   *case sensitive.
   *@return The field description.
  **/
  public FieldDescription getFieldDescription(String name)
  {
    return (FieldDescription)fieldDescriptions_.elementAt(getIndexOfFieldName(name));
  }

  /**
   *Returns the field descriptions that make up this object.
   *@return The field descriptions.  An array of size zero is returned if no
   *fields have been added to this object.
  **/
  public FieldDescription[] getFieldDescriptions()
  {
    FieldDescription[] fds = new FieldDescription[fieldDescriptions_.size()];
    fieldDescriptions_.copyInto(fds);
    return fds;
  }

  /**
   *Returns the names of the field descriptions that make up this record format.
   *@return The names of the field descriptions.  An array of size zero is
   *returned if no fields have been added to this object.
  **/
  public String[] getFieldNames()
  {
    int size = fieldDescriptions_.size();
    String[] names = new String[size];
    for (int i = 0; i < size; ++i)
    {
      names[i] = ((FieldDescription)fieldDescriptions_.elementAt(i)).getFieldName();
    }
    return names;
  }

  /**
   *Indicates if this record format contains dependent fields.
   *@return true if this record format contains dependent fields; false otherwise
  **/
  boolean getHasDependentFields()
  {
    return hasDependentFields_;
  }

  /**
   *Returns the index of the field description named <i>name</i>.
   *@param name The name of the field description.  The <i>name</i>
   *is case sensitive.
   *@return The index of the field description.
  **/
  public int getIndexOfFieldName(String name)
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    Integer i = (Integer)fieldNameToIndexMap_.get(name);
    if (i == null)
    {
      throw new ExtendedIllegalArgumentException("name (" + name + ")", ExtendedIllegalArgumentException.FIELD_NOT_FOUND);
    }
    return i.intValue();
  }

  /**
   *Returns the index of the field description of the key field named <i>name</i>.
   *@param name The name of the key field description.  The <i>name</i> is
   *case sensitive.
   *@return The index of the key field description.  This is the index of the key field description
   *in the key field descriptions for this object.  It is not the index of the field description in the
   *field descriptions for this object.
  **/
  public int getIndexOfKeyFieldName(String name)
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    Integer i = (Integer)keyFieldNameToIndexMap_.get(name);
    if (i == null)
    {
      throw new ExtendedIllegalArgumentException("name (" + name + ")", ExtendedIllegalArgumentException.FIELD_NOT_FOUND);
    }
    return i.intValue();
  }

  /**
   *Returns the field description of the key field at the specified index.
   *@param index The index of the key field description in the key field descriptions for this object.
   *@return The key field description.
  **/
  public FieldDescription getKeyFieldDescription(int index)
  {
    if (index < 0 || index > fieldDescriptions_.size() - 1)
    {
      throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    return (FieldDescription)keyFieldDescriptions_.elementAt(index);
  }

  /**
   *Returns the field description of the key field with the specified name.
   *@param name The name of the key field description.  The <i>name</i> is
   *case sensitive.
   *@return The key field description.
  **/
  public FieldDescription getKeyFieldDescription(String name)
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    return (FieldDescription)keyFieldDescriptions_.elementAt(getIndexOfKeyFieldName(name));
  }

  /**
   *Returns the key field descriptions that make up this object.
   *@return The key field descriptions.
  **/
  public FieldDescription[] getKeyFieldDescriptions()
  {
    FieldDescription[] fds = new FieldDescription[keyFieldDescriptions_.size()];
    keyFieldDescriptions_.copyInto(fds);
    return fds;
  }

  /**
   *Returns the names of the field descriptions of the keys that make up this record format.
   *@return The names of the key field descriptions.  If no key field descriptions exist,
   *an array of size 0 is returned.
  **/
  public String[] getKeyFieldNames()
  {
    int size = keyFieldDescriptions_.size();
    String[] names = new String[size];
    for (int i = 0; i < size; ++i)
    {
      names[i] = ((FieldDescription)keyFieldDescriptions_.elementAt(i)).getFieldName();
    }
    return names;
  }

  /**
   *Returns the index of the field description on which the field description at the specified
   *index depends.
   *@param index The index of the field description.  The <i>index</i> must be in the range 0 to
   *getNumberOfFields() - 1.
   *@return The index of the field description on which the field description at the specified
   *index depends.
   *If <i>index</i> is not the index of a dependent field, -1 is returned.
  **/
  public int getLengthDependency(int index)
  {
    if (index < 0 || index > fieldDescriptions_.size() - 1)
    {
      throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    return  ((Integer)lengthDependentFields_.elementAt(index)).intValue();
  }

  /**
   *Returns the index of the field description on which the field description with the specified
   *name depends.
   *@param name The name of the field description.  The <i>name</i> is
   *case sensitive.
   *@return The index of the field description on which the field description with the specified
   *name depends.
   *If <i>name</i> is not the name of a dependent field, -1 is returned.
  **/
  public int getLengthDependency(String name)
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    return ((Integer)lengthDependentFields_.elementAt(getIndexOfFieldName(name))).intValue();
  }

  /**
   *Returns the name of this record format.
   *@return The name of this record format.  If the name has not been
   *set, an empty string is returned.
  **/
  public String getName()
  {
    return name_;
  }

  /**
   *Returns a new record based on this record format, which contains default values for the
   *contents of the fields.  The default values are determined as follows:
   *<ol>
   *<li>Use the value specified for the DFT keyword on the field description object for
   *a particular field.
   *<li>If no value was specified for the DFT keyword, use the default value from the
   *AS400DataType object specified when constructing the field description object for
   *a particular field.
   *</ol>
   *@return A record based on this record format.  If no field descriptions have been
   *added to this object, null is returned.
  **/
  public Record getNewRecord()
  {
    if (fieldDescriptions_.size() == 0)
    {
      return null;
    }
    return new Record(this);
  }

  /**
   *Returns a new record based on this record format, which contains default values for the
   *contents of the fields.  The default values are determined as follows:
   *<ol>
   *<li>Use the value specified for the DFT keyword on the field description object for
   *a particular field.
   *<li>If no value was specified for the DFT keyword, use the default value from the
   *AS400DataType object specified when constructing the field description object for
   *a particular field.
   *</ol>
   *@param recordName The name to assign to the Record object being returned.
   *@return A record based on this record format.
   *If no field descriptions have been added to this object, null is returned.
  **/
  public Record getNewRecord(String recordName)
  {
    if (recordName == null)
    {
      throw new NullPointerException("recordName");
    }
    if (fieldDescriptions_.size() == 0)
    {
      return null;
    }
    return new Record(this, recordName);
  }

  /**
   *Returns a new record based on this record format, which contains data from
   *the specified byte array.
   *@param contents The data with which to initialize the contents of the record.
   *The length of <i>contents</i> must be greater than zero.
   *@return A record based on this record format.
   *If no field descriptions have been added to this object, null is returned.
   *@exception UnsupportedEncodingException If an error occurs during conversion.
  **/
  public Record getNewRecord(byte[] contents)
    throws UnsupportedEncodingException
  {
    return getNewRecord(contents, 0);
  }

  /**
   *Returns a new record based on this record format, which contains data from
   *the specified byte array.
   *@param contents The data with which to initialize the contents of the record.
   *The length of <i>contents</i> must be greater than zero.
   *@param recordName The name to assign to the Record object being returned.
   *@return A record based on this record format.
   *If no field descriptions have been added to this object, null is returned.
   *@exception UnsupportedEncodingException If an error occurs during conversion.
  **/
  public Record getNewRecord(byte[] contents, String recordName)
    throws UnsupportedEncodingException
  {
    return getNewRecord(contents, 0, recordName);
  }

  /**
   *Returns a new record based on this record format, which contains data from
   *the specified byte array.
   *@param contents The data with which to initialize the contents of the record.
   *The length of <i>contents</i> must be greater than zero.
   *@param offset The offset in <i>contents</i> at which to start.  The <i>offset</i>
   *cannot be less than zero.
   *@return A record based on this record format.
   *If no field descriptions have been added to this object, null is returned.
   *@exception UnsupportedEncodingException If an error occurs during conversion.
  **/
  public Record getNewRecord(byte[] contents, int offset)
    throws UnsupportedEncodingException
  {
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
      throw new ExtendedIllegalArgumentException("offset", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (fieldDescriptions_.size() == 0)
    {
      return null;
    }
    return new Record(this, contents, offset);
  }

  /**
   *Returns a new record based on this record format, which contains data from
   *the specified byte array.
   *@param contents The data with which to initialize the contents of the record.
   *The length of <i>contents</i> must be greater than zero.
   *@param offset The offset in <i>contents</i> at which to start.  The <i>offset</i>
   *cannot be less than zero.
   *@param recordName The name to assign to the Record object being returned.
   *@return A record based on this record format.
   *If no field descriptions have been added to this object, null is returned.
   *@exception UnsupportedEncodingException If an error occurs during conversion.
  **/
  public Record getNewRecord(byte[] contents, int offset, String recordName)
    throws UnsupportedEncodingException
  {
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
      throw new ExtendedIllegalArgumentException("offset", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (recordName == null)
    {
      throw new NullPointerException("recordName");
    }
    if (fieldDescriptions_.size() == 0)
    {
      return null;
    }
    return new Record(this, contents, offset, recordName);
  }

  /**
   *Returns the number of field descriptions in this record format.
   *@return The number of field descriptions in this record format.
  **/
  public int getNumberOfFields()
  {
    return fieldDescriptions_.size();
  }

  /**
   *Returns the number of key field descriptions in this record format.
   *@return The number of key field descriptions in this record format.
  **/
  public int getNumberOfKeyFields()
  {
    return keyFieldDescriptions_.size();
  }

  /**
   *Returns the index of the field description on which the field description at the specified
   *index depends.
   *@param index The index of the field description.  The <i>index</i> must be in the range 0 to
   *getNumberOfFields() - 1.
   *@return The index of the field description on which the field description at the specified
   *index depends.
   *If <i>index</i> is not the index of a dependent field, -1 is returned.
  **/
  public int getOffsetDependency(int index)
  {
    if (index < 0 || index > fieldDescriptions_.size() - 1)
    {
      throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    return  ((Integer)offsetDependentFields_.elementAt(index)).intValue();
  }

  /**
   *Returns the index of the field description on which the field description with the specified
   *name depends.
   *@param name The name of the field description.  The <i>name</i> is
   *case sensitive.
   *@return The index of the field description on which the field description with the specified
   *name depends.
   *If <i>name</i> is not the name of a dependent field, -1 is returned.
  **/
  public int getOffsetDependency(String name)
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    return ((Integer)offsetDependentFields_.elementAt(getIndexOfFieldName(name))).intValue();
  }

  // @C1A - added method
 /**
   * Returns the record format ID.
   * The record format ID corresponds to a record format ID within a page definition
   * defined on the AS/400.
   *
   * @return  The record format ID.
  **/
  public String getRecordFormatID()
  {
    return recordFormatID_;
  }

  // @C1A - added method
  /**
   * Returns the record format type.
   *
   * @return  The record format type.
  **/
  public int getRecordFormatType()
  {
    return recordFormatType_;
  }

  //@D0A
  /**
   * This should be called by any class that is running proxified and retrieves
   * a RecordFormat from the server side.
  **/
  void initializeTextObjects(AS400 system)
  {
    //@D0A - need to finish filling in the AS400Text objects
    // now that we're back on the client
    for (int i=0; i<fieldDescriptions_.size(); ++i)
    {
      AS400DataType dt = ((FieldDescription)fieldDescriptions_.elementAt(i)).dataType_;
//      if (dt instanceof AS400Text)
      if (dt.getInstanceType() == AS400DataType.TYPE_TEXT)
      {
        ((AS400Text)dt).setConverter(system);
      }
    }
  }



  //@B0A
  /**
   * Initialize transient data.
  **/
  private void initializeTransient()
  {
//    rdListeners_ = new Vector();
//    vetos_ = new VetoableChangeSupport(this);
//    changes_ = new PropertyChangeSupport(this);
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
   *@exception IOException If an error occurs during deserialization.
  **/

  private void readObject(java.io.ObjectInputStream in)
    throws ClassNotFoundException,
           IOException
  {
    in.defaultReadObject();
    initializeTransient(); //@B0A
  }

  /**
   *Removes a listener from the change list.
   *If the listener is not on the list, do nothing.
   *@see #addPropertyChangeListener
   *@param listener The PropertyChangeListener.
  **/
  public /*@B0D synchronized*/ void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    if (changes_ != null) changes_.removePropertyChangeListener(listener);
  }

  /**
   *Removes a listener from the  record description listeners list.
   *If the listener is not on the list, do nothing.
   *@see #addRecordDescriptionListener
   *@param listener The RecordDescriptionListener.
  **/
  public /*@B0D synchronized*/ void removeRecordDescriptionListener(RecordDescriptionListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    if (rdListeners_ != null) rdListeners_.removeElement(listener); //@B0C
//@B0D    currentRecordDescriptionListeners_ = (Vector)recordDescriptionListeners_.clone();
  }

  /**
   *Removes a listener from the veto change listeners list.
   *If the listener is not on the list, do nothing.
   *@see #addVetoableChangeListener
   *@param listener The VetoableChangeListener.
  **/
  public /*@B0D synchronized*/ void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    if (vetos_ != null) vetos_.removeVetoableChangeListener(listener); //@B0C
//@B0D    currentVetoListeners_ = (Vector)vetoListeners_.clone();
  }

  //@D0A
  /**
   * This should be called by any class that is running on the proxy server and retrieves
   * a RecordFormat from the client side.
  **/
//@E0: This function has been moved to AS400FileImplBase.
/*@E0D  void setConverter(ConverterImpl conv)
  {
    //@D0A - need to finish filling in the AS400Text objects
    // now that we're back on the client
    for (int i=0; i<fieldDescriptions_.size(); ++i)
    {
      AS400DataType dt = ((FieldDescription)fieldDescriptions_.elementAt(i)).dataType_;
      if (dt instanceof AS400Text)
      {
        ((AS400Text)dt).setConverter(conv);
      }
    }
  }
*/

  // @C1A - added method
  /**
   * Sets the delimiter.  The delimiter is the character
   * used to separate variable length fields when the record is
   * written using the line data record writer class.  This value is only
   * valid when the record format type is VARIABLE_LAYOUT_LENGTH.
   *
   * @param delimiter The delimiter.
  **/
  public void setDelimiter(char delimiter)
  {
    delimiter_ = delimiter;
  }


  /**
   *Sets the field on which a dependent field depends.  Both fields must have been added already
   *to this RecordFormat.  The <i>fieldDependedOn</i> must have been added prior to adding the
   *<i>dependentField</i>.
   *@param dependentField The index of the dependent field.  The
   *<i>dependentField</i> must be in the range 1 to getNumberOfFields() - 1.
   *@param fieldDependedOn The index of a field on which this field depends.  The <i>fieldDependedOn</i> must
   *be in the range 0 to <i>dependentField</i>.
  **/
  public void setLengthDependency(int dependentField, int fieldDependedOn)
  {
    if (dependentField < 0)
    {
      throw new ExtendedIllegalArgumentException("dependentField", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if (dependentField > fieldDescriptions_.size() - 1)
    {
      throw new ExtendedIllegalArgumentException("dependentField", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if (fieldDependedOn < 0)
    {
      throw new ExtendedIllegalArgumentException("fieldDependedOn", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if (dependentField > fieldDescriptions_.size() - 1)
    {
      throw new ExtendedIllegalArgumentException("dependentField", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if (fieldDependedOn >= dependentField)
    {
      throw new ExtendedIllegalArgumentException("fieldDependedOn", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    lengthDependentFields_.setElementAt(new Integer(fieldDependedOn), dependentField);
    hasDependentFields_ = true;
  }

  /**
   *Sets the field on which a dependent field depends.  Both fields must have been added already
   *to this RecordFormat.  The <i>fieldDependedOn</i> must have been added prior to adding the
   *<i>dependentField</i>.  The names of the fields are
   *case sensitive.
   *@param dependentField The name of the dependent field.
   *@param fieldDependedOn The name of a field on which this field depends.  The index of
   *<i>fieldDependedOn</i> in this RecordFormat must be less than the index of <i>dependentField</i>.
  **/
  public void setLengthDependency(String dependentField, String fieldDependedOn)
  {
    if (dependentField == null)
    {
      throw new NullPointerException("dependentField");
    }
    if (fieldDependedOn == null)
    {
      throw new NullPointerException("fieldDependedOn");
    }
    int depOnIndex = getIndexOfFieldName(fieldDependedOn);
    int depIndex = getIndexOfFieldName(dependentField);
    setLengthDependency(depIndex, depOnIndex);
  }

  /**
   *Sets the name of this record format.
   *@param name The name of this record format.
   *The <i>name</i> is converted to uppercase by this method.
   *@exception PropertyVetoException If a change is vetoed.
  **/
  public void setName(String name)
    throws PropertyVetoException
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }

    //@B0D: removed old veto-listener code block

    String old = name_;
    if (vetos_ != null) vetos_.fireVetoableChange("name", old, name.toUpperCase()); //@B0A
    name_ = name.toUpperCase();
    if (changes_ != null) changes_.firePropertyChange("name", old, name_);
  }

  /**
   *Sets the field on which a dependent field depends.  Both fields must have been added already
   *to this RecordFormat.  The <i>fieldDependedOn</i> must have been added prior to adding the
   *<i>dependentField</i>.
   *@param dependentField The index of the dependent field.  The
   *<i>dependentField</i> must be in the range 1 to getNumberOfFields() - 1.
   *@param fieldDependedOn The index of a field on which this field depends.  The <i>fieldDependedOn</i> must
   *be in the range 0 to <i>dependentField</i>.
  **/
  public void setOffsetDependency(int dependentField, int fieldDependedOn)
  {
    if (dependentField < 0)
    {
      throw new ExtendedIllegalArgumentException("dependentField", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if (fieldDependedOn < 0)
    {
      throw new ExtendedIllegalArgumentException("fieldDependedOn", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if (dependentField > fieldDescriptions_.size() - 1)
    {
      throw new ExtendedIllegalArgumentException("dependentField", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if (fieldDependedOn >= dependentField)
    {
      throw new ExtendedIllegalArgumentException("fieldDependedOn", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    offsetDependentFields_.setElementAt(new Integer(fieldDependedOn), dependentField);
    hasDependentFields_ = true;
  }

  /**
   *Sets the field on which a dependent field depends.  Both fields must have been added already
   *to this RecordFormat.  The <i>fieldDependedOn</i> must have been added prior to adding the
   *<i>dependentField</i>.  The names of the fields are
   *case sensitive.
   *@param dependentField The name of the dependent field.
   *@param fieldDependedOn The name of a field on which this field depends.  The index of
   *<i>fieldDependedOn</i> in this RecordFormat must be less than the index of <i>dependentField</i>.
  **/
  public void setOffsetDependency(String dependentField, String fieldDependedOn)
  {
    if (dependentField == null)
    {
      throw new NullPointerException("dependentField");
    }
    if (fieldDependedOn == null)
    {
      throw new NullPointerException("fieldDependedOn");
    }
    int depOnIndex = getIndexOfFieldName(fieldDependedOn);
    int depIndex = getIndexOfFieldName(dependentField);
    setOffsetDependency(depIndex, depOnIndex);
  }

  // @C1A - added method
  /**
   * Sets the record format ID. The length of the record format ID must be 10 characters
   * or less.  The record format ID corresponds to a record format ID within a page
   * definition on the AS/400.  If the record format ID is less than 10 characters,
   * it is padded to 10 characters in length with spaces.
   *
   * @param type  The record format ID.
  **/
  public void setRecordFormatID(String id)
  {
    String pad = "          " ;
    if (id.length() > 10) {
        throw new ExtendedIllegalArgumentException("id",
            ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    if (id.length() < 10) {
      int padl = 10 - id.length();
      recordFormatID_ = id + pad.substring(0,padl);
    }
    else
       recordFormatID_ = id;


  }

// @C1A - added method
  /**
   * Sets the record format type. Valid values are FIXED_LAYOUT_LENGTH and
   * VARIABLE_LAYOUT_LENGTH.  This attribute is only valid when using the
   * line record writer class.
   *
   * @param type  The record format type.
  **/

  public void setRecordFormatType(int type)
  {
    if ((type != VARIABLE_LAYOUT_LENGTH) && (type != FIXED_LAYOUT_LENGTH)) {
        throw new ExtendedIllegalArgumentException("type",
            ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    recordFormatType_ = type;
  }

}
