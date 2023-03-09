///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMRecordFormatReader.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;

import com.ibm.jtopenlite.*;

final class DDMRecordFormatReader implements DDMReadCallback
{
  private final int serverCCSID_;

  private boolean eof_ = false;

  private String library_;
  private String file_;
  private String name_;
  private String type_;
  private String text_;
  private DDMField[] fields_;

  private int totalLength_ = 0;

  DDMRecordFormatReader(final int serverCCSID)
  {
    serverCCSID_ = serverCCSID;
  }

  String getLibrary()
  {
    return library_;
  }

  String getFile()
  {
    return file_;
  }

  String getName()
  {
    return name_;
  }

  String getType()
  {
    return type_;
  }

  String getText()
  {
    return text_;
  }

  DDMField[] getFields()
  {
    return fields_;
  }

  int getLength()
  {
    return totalLength_;
  }

  public void newRecord(final DDMCallbackEvent event, final DDMDataBuffer dataBuffer)
  {
    if (eof_) return;
//    int rfCount = Integer.valueOf(Conv.zonedDecimalToString(tempData, 28, 5, 0)); // WHCNT
    final byte[] tempData = dataBuffer.getRecordDataBuffer();
    final int recordNumber = dataBuffer.getRecordNumber();
    if (fields_ == null)
    {
      int numFields = Integer.valueOf(Conv.zonedDecimalToString(tempData, 361, 5, 0)); // WHNFLD
      fields_ = new DDMField[numFields];
      name_ = Conv.ebcdicByteArrayToString(tempData, 46, 10); // WHNAME
      library_ = Conv.ebcdicByteArrayToString(tempData, 10, 10); // WHLIB
      file_ = Conv.ebcdicByteArrayToString(tempData, 0, 10); // WHFILE
      type_ = Conv.ebcdicByteArrayToString(tempData, 27, 1); // WHFTYP
      text_ = Conv.ebcdicByteArrayToString(tempData, 69, 50); // WHTEXT
      if (numFields == 0) return; // Save files have no fields, for example.
    }
    else if (recordNumber > fields_.length)
    {
      //TODO - More than one record format, we'll support that later.
      eof_ = true;
      return;
    }

    String fieldName = Conv.ebcdicByteArrayToString(tempData, 139, 10).trim(); // WHFLDE;
    int fieldByteLength = Integer.valueOf(Conv.zonedDecimalToString(tempData, 159, 5, 0)); // WHFLDB
    int numDigits = Integer.valueOf(Conv.zonedDecimalToString(tempData, 164, 2, 0)); // WHFLDO
    int decimalPositions = Integer.valueOf(Conv.zonedDecimalToString(tempData, 166, 2, 0)); // WHFLDP
    String fieldText = Conv.ebcdicByteArrayToString(tempData, 168, 50); // WHFTXT
    final char fieldType = Conv.ebcdicByteArrayToString(tempData, 321, 1).charAt(0); // WHFLDT
    int defaultValueLength = Integer.valueOf(Conv.zonedDecimalToString(tempData, 402, 2, 0)); // WHDFTL
    String defaultValue = Conv.ebcdicByteArrayToString(tempData, 404, defaultValueLength > 30 ? 30 : defaultValueLength); // WHDFT
    int ccsid = Integer.valueOf(Conv.packedDecimalToString(tempData, 491, 5, 0)); // WHCCSID
    String dateTimeFormat = Conv.ebcdicByteArrayToString(tempData, 494, 4); // WHFMT
    String dateTimeSeparator = Conv.ebcdicByteArrayToString(tempData, 498, 1); // WHSEP
    String variableLengthField = Conv.ebcdicByteArrayToString(tempData, 499, 1); // WHVARL
    int allocatedLength = Integer.valueOf(Conv.packedDecimalToString(tempData, 500, 5, 0)); // WHALLC
    String allowNulls = Conv.ebcdicByteArrayToString(tempData, 503, 1); // WHNULL;

    if (ccsid == 65535) ccsid = serverCCSID_;

    fields_[recordNumber-1] = new DDMField(totalLength_, fieldName, fieldByteLength, numDigits, decimalPositions,
                                         fieldText, fieldType, defaultValue, ccsid, variableLengthField,
                                         allocatedLength, allowNulls, dateTimeFormat, dateTimeSeparator);
    totalLength_ += fieldByteLength;
//    String alias = Conv.ebcdicByteArrayToString(tempData, 370,30); // WHALIS
  }

  public void recordNotFound(final DDMCallbackEvent event)
  {
    eof_ = true;
  }

  public void endOfFile(final DDMCallbackEvent event)
  {
    eof_ = true;
  }

  final boolean eof()
  {
    return eof_;
  }
}
