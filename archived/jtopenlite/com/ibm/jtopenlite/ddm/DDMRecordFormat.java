///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMRecordFormat.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;


/**
 * Represents the record format and field information for a file.
**/
public class DDMRecordFormat
{
  private final String library_;
  private final String file_;
  private final String name_;
  private final String type_;
  private final String text_;
  private final DDMField[] fields_;
  private final int totalLength_;

  DDMRecordFormat(final String library,
                  final String file,
                  final String name,
                  final String type,
                  final String text,
                  final DDMField[] fields,
                  final int totalLength)
  {
    library_ = library;
    file_ = file;
    name_ = name;
    type_ = type;
    text_ = text;
    fields_ = fields;
    totalLength_ = totalLength;
  }

  /**
   * Returns a new copy of this record format, which includes a new copy of each DDMField.
   * This is useful if multiple threads need to do field conversions on the same record format definition,
   * since the DDMRecordFormat and DDMField classes are not thread-safe, each thread can be given its
   * own copy of the record format, rather than using synchronization to share a single record format.
  **/
  public DDMRecordFormat newCopy()
  {
    final DDMField[] fields = new DDMField[this.fields_.length];
    for (int i=0; i<this.fields_.length; ++i)
    {
      fields[i] = this.fields_[i].newCopy();
    }
    return new DDMRecordFormat(this.library_,
                               this.file_,
                               this.name_,
                               this.type_,
                               this.text_,
                               fields,
                               this.totalLength_);
  }

  /**
   * Returns the name (WHNAME) of this record format.
  **/
  public String getName()
  {
    return name_;
  }

  /**
   * Returns the library (WHLIB) in which the file resides.
  **/
  public String getLibrary()
  {
    return library_;
  }

  /**
   * Returns the name of the file (WHFILE) for this record format.
  **/
  public String getFile()
  {
    return file_;
  }

  /**
   * Returns the file type (WHFTYP) of record format.
  **/
  public String getType()
  {
    return type_;
  }

  /**
   * Returns the text description (WHTEXT) of this record format.
  **/
  public String getText()
  {
    return text_;
  }

  /**
   * Returns the total length in bytes of this record format.
  **/
  public int getLength()
  {
    return totalLength_;
  }

  /**
   * Returns the recommended batch size to use for reading or writing records with this record format.
  **/
  public int getRecommendedBatchSize()
  {
    int div = totalLength_+16;
    int num = (32768/div) - 1; //TODO - Something's wrong with out DDM data stream if we use an exact number, subtract 1 for now.
    if (num <= 0) return 1;
    return num;
  }

  /**
   * Returns the number of fields in this record format.
  **/
  public int getFieldCount()
  {
    return fields_.length;
  }

  /**
   * Returns the field at the specified index, or null if the index is not valid.
  **/
  public DDMField getField(final int index)
  {
    return (index >= 0 && index < fields_.length) ? fields_[index] : null;
  }

  /**
   * Returns the field with the specified name, or null if no such field exists in this record format.
  **/
  public DDMField getField(final String fieldName)
  {
    return getField(getIndex(fieldName));
  }

  private int getIndex(final String fieldName)
  {
    for (int i=0; i<fields_.length; ++i)
    {
      if (fields_[i].getName().equals(fieldName)) return i;
    }
    return -1;
  }
}
