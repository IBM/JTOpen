///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FileRecordDescriptionImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.math.BigDecimal;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 *The AS400FileRecordDescription class represents the record descriptions of an AS/400 physical
 *or logical file.  This class is used to retrieve the file field description
 *of an AS/400 physical or logical file, and to create Java source code
 *for a class extending from
 *<a href = "RecordFormat.html">RecordFormat</a> that
 *can then be compiled and used as input to the
 *<a href="AS400File.html#setRecordFormat()">AS400File.setRecordFormat()</a>
 *method.
 *This allows the record format to be created statically during
 *development time and then reused when needed.
 *The class also provides a method for returning RecordFormat objects
 *that can be used as input to the AS400File.setRecordFormat() method.
 *This method can be used to create the record format dynamically.
 *<p>The output from the <a href="#createRecordFormatSource">createRecordFormatSource()</a>
 *and
 *<a href="#retrieveRecordFormat">retrieveRecordFormat()</a> methods
 *contains enough information to use to describe the record format of the
 *existing AS/400 file from which it was generated.  The record formats
 *generated are not meant for creating files with the same format as the
 *file from which they are retrieved.  Use the AS/400 Copy File (CPYF) command to create
 *a file with the same format as an existing file.
 *<br>
 *AS400FileRecordDescription objects generate the following events:
 *<ul>
 *<li><a href="AS400FileRecordDescriptionEvent.html">AS400FileRecordDescriptionEvent</a>
 *<br>The events fired are:
 *<ul>
 *<li>recordFormatRetrieved
 *<li>recordFormatSourceCreated
 *</ul>
 *<li>PropertyChangeEvent
 *<li>VetoableChangeEvent
 *</ul>
**/
class AS400FileRecordDescriptionImplRemote implements AS400FileRecordDescriptionImpl, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

  // File name
  private String file_ = "";
  // Library name
  private String library_ = "";
  // member name
  private String member_ = "";
  // The IFS path name of the file
  private String name_ = "";
  // SequentialFile object representing the file whose record description
  // is being retrieved
  private AS400FileImplBase theFile_ = null; //@C0C @B5C
  // The path name for the SequentialFile

  // The AS/400 the file is on
  private AS400ImplRemote system_ = null; //@B5C

  // Used for synchronizing the QTEMP/JT4FFD and QTEMP/JT4FD files across threads
  private static Object lockJT4FFD_ = new Object(); //@E0A
  private static Object lockJT4FD_ = new Object(); //@E0A

  /**
   *Adds a field description to the specified RecordFormat object.  The field description
   *is created with information extracted from the Record object provided.
   *@param rf The record format to which to add the field description.
   *@param record The record from which to obtain the field information.
   *@exception UnsupportedEncodingException If an error occurs during conversion.
  **/
  void addFieldDescription(RecordFormat rf, Record record)
  throws UnsupportedEncodingException
  {
    char fieldType = ((String)record.getField("WHFLDT")).charAt(0);
    FieldDescription fd = null;
    int digits;
    int decimalPositions;
    int byteLength;              //@A1A: For float field descriptions
    String fieldName = ((String)record.getField("WHFLDE")).trim();
    int ccsid;
    switch(fieldType)
    {
      case 'A': // Character field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        fd = new CharacterFieldDescription(
                                          new AS400Text((((String)record.getField("WHVARL")).equals("Y"))?
                                                        ((BigDecimal)record.getField("WHFLDB")).intValue() - 2:
                                                        ((BigDecimal)record.getField("WHFLDB")).intValue(),
                                                        ccsid, system_), //@D0C
                                          fieldName);
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            ((CharacterFieldDescription)fd).setVARLEN(((BigDecimal)record.getField("WHALLC")).intValue());
          }
          else
          { // No length was specified on the VARLEN keyword
            ((CharacterFieldDescription)fd).setVariableLength(true);
          }
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((CharacterFieldDescription)fd).setDFTNull();
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            ((CharacterFieldDescription)fd).setDFT(adjDft);
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'B': // Binary field
        digits = ((BigDecimal)record.getField("WHFLDO")).intValue();
//      if (digits < 6)
        if(digits < 5) // @A2C
        {
          fd = new BinaryFieldDescription(new AS400Bin2(),
                                          fieldName,
                                          fieldName,
                                          digits);
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              ((BinaryFieldDescription)fd).setDFTNull();
            }
            // Handle value
            else
            {
              ((BinaryFieldDescription)fd).setDFT(new Short(dft));
            }
          }
        }
        else if (digits < 10) //@F0C
        {
          fd = new BinaryFieldDescription(new AS400Bin4(),
                                          fieldName,
                                          fieldName,
                                          digits);
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              ((BinaryFieldDescription)fd).setDFTNull();
            }
            // Handle value
            else
            {
              ((BinaryFieldDescription)fd).setDFT(new Integer(dft));
            }
          }
        }
        else //@F0A
        {
          fd = new BinaryFieldDescription(new AS400Bin8(),
                                          fieldName,
                                          fieldName,
                                          digits);
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              ((BinaryFieldDescription)fd).setDFTNull();
            }
            // Handle value
            else
            {
              ((BinaryFieldDescription)fd).setDFT(new Long(dft));
            }
          }
        }
        break;
      case 'E': // DBCS-Either field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        fd = new DBCSEitherFieldDescription(
                                           new AS400Text((((String)record.getField("WHVARL")).equals("Y"))?
                                                         ((BigDecimal)record.getField("WHFLDB")).intValue() - 2:
                                                         ((BigDecimal)record.getField("WHFLDB")).intValue(),
                                                         ccsid, system_), //@D0C
                                           fieldName);
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            ((DBCSEitherFieldDescription)fd).setVARLEN(((BigDecimal)record.getField("WHALLC")).intValue());
          }
          else
          { // No length was specified on the VARLEN keyword
            ((DBCSEitherFieldDescription)fd).setVariableLength(true);
          }
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((DBCSEitherFieldDescription)fd).setDFTNull();
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            ((DBCSEitherFieldDescription)fd).setDFT(adjDft);
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'F': // Float field
        digits = ((BigDecimal)record.getField("WHFLDO")).intValue();
        decimalPositions = ((BigDecimal)record.getField("WHFLDP")).intValue();
        //@A1A: Retrieve byte length to determine if field is single or double
        // precision
        byteLength = ((BigDecimal)record.getField("WHFLDB")).intValue(); //@A1A
        if(byteLength == 4)      //@A1A
        {
          fd = new FloatFieldDescription(new AS400Float4(),
                                         fieldName,
                                         fieldName,
                                         digits,
                                         decimalPositions);
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              ((FloatFieldDescription)fd).setDFTNull();
            }
            // Handle value
            else
            {
              ((FloatFieldDescription)fd).setDFT(new Float(dft));
            }
          }
        }
        else if(byteLength == 8)  //@A1A
        {
          fd = new FloatFieldDescription(new AS400Float8(),
                                         fieldName,
                                         fieldName,
                                         digits,
                                         decimalPositions);
          // Set the FLTPCN keyword to *DOUBLE
          ((FloatFieldDescription)fd).setFLTPCN("*DOUBLE");
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              ((FloatFieldDescription)fd).setDFTNull();
            }
            // Handle value
            else
            {
              ((FloatFieldDescription)fd).setDFT(new Double(dft));
            }
          }
        }
        else    //@A1A: This should never occur, but for completeness....
        {
          //@A1A
          throw new InternalErrorException("FloatFieldDescription error in byte length", InternalErrorException.UNKNOWN);  //@A1A
        }       //@A1A
        break;
      case 'G': // DBCS-Graphic field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        fd = new DBCSGraphicFieldDescription(
                                            new AS400Text((((String)record.getField("WHVARL")).equals("Y"))?
                                                          ((BigDecimal)record.getField("WHFLDB")).intValue() - 2:
                                                          ((BigDecimal)record.getField("WHFLDB")).intValue(),
                                                          ccsid, system_), //@D0C
                                            fieldName);
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            ((DBCSGraphicFieldDescription)fd).setVARLEN(((BigDecimal)record.getField("WHALLC")).intValue());
          }
          else
          { // No length was specified on the VARLEN keyword
            ((DBCSGraphicFieldDescription)fd).setVariableLength(true);
          }
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((DBCSGraphicFieldDescription)fd).setDFTNull();
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            ((DBCSGraphicFieldDescription)fd).setDFT(adjDft);
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'H': // Hex field
        // Need to get the length in bytes of the field
        // when creating the AS400ByteArray object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        fd = new HexFieldDescription(
                                    new AS400ByteArray((((String)record.getField("WHVARL")).equals("Y"))?
                                                       ((BigDecimal)record.getField("WHFLDB")).intValue() - 2 :
                                                       ((BigDecimal)record.getField("WHFLDB")).intValue()), fieldName);
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            ((HexFieldDescription)fd).setVARLEN(((BigDecimal)record.getField("WHALLC")).intValue());
          }
          else
          { // No length was specified on the VARLEN keyword
            ((HexFieldDescription)fd).setVariableLength(true);
          }
        }
        // @B0A: Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((HexFieldDescription)fd).setDFTNull();
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            byte[] dftBytes = adjDft.getBytes();
            ((HexFieldDescription)fd).setDFT(dftBytes);
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'J': // DBCS-Only field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        fd = new DBCSOnlyFieldDescription(
                                         new AS400Text((((String)record.getField("WHVARL")).equals("Y"))?
                                                       ((BigDecimal)record.getField("WHFLDB")).intValue() - 2:
                                                       ((BigDecimal)record.getField("WHFLDB")).intValue(),
                                                       ccsid, system_), //@D0C
                                         fieldName);
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            ((DBCSOnlyFieldDescription)fd).setVARLEN(((BigDecimal)record.getField("WHALLC")).intValue());
          }
          else
          { // No length was specified on the VARLEN keyword
            ((DBCSOnlyFieldDescription)fd).setVariableLength(true);
          }
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((DBCSOnlyFieldDescription)fd).setDFTNull();
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            ((DBCSOnlyFieldDescription)fd).setDFT(adjDft);
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'L': // Date field
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        fd = new DateFieldDescription(
                                     new AS400Text(((BigDecimal)record.getField("WHFLDB")).intValue(),
                                                   ccsid, system_), //@D0C
                                     fieldName);
        // Set date format and date separator values
        ((DateFieldDescription)fd).setDATFMT((String)record.getField("WHFMT"));
        if(!((String)record.getField("WHFMT")).equals(" "))
        {
          ((DateFieldDescription)fd).setDATSEP((String)record.getField("WHSEP"));
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((DateFieldDescription)fd).setDFTNull();
          }
          // Handle CURRENT_DATE (an SQL special value)
          else if(dft.indexOf("CURRENT_DATE") != -1)
          {
            ((DateFieldDescription)fd).setDFTCurrent();
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            ((DateFieldDescription)fd).setDFT(adjDft);
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'O': // DBCS-Open field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        fd = new DBCSOpenFieldDescription(
                                         new AS400Text((((String)record.getField("WHVARL")).equals("Y"))?
                                                       ((BigDecimal)record.getField("WHFLDB")).intValue() - 2:
                                                       ((BigDecimal)record.getField("WHFLDB")).intValue(),
                                                       ccsid, system_), //@D0C
                                         fieldName);
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            ((DBCSOpenFieldDescription)fd).setVARLEN(((BigDecimal)record.getField("WHALLC")).intValue());
          }
          else
          { // No length was specified on the VARLEN keyword
            ((DBCSOpenFieldDescription)fd).setVariableLength(true);
          }
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((DBCSOpenFieldDescription)fd).setDFTNull();
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            ((DBCSOpenFieldDescription)fd).setDFT(adjDft);
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'P': // Packed decimal field
        digits = ((BigDecimal)record.getField("WHFLDO")).intValue();
        decimalPositions = ((BigDecimal)record.getField("WHFLDP")).intValue();
        fd = new PackedDecimalFieldDescription(new AS400PackedDecimal(digits, decimalPositions),
                                               fieldName);
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          String dft = ((String)record.getField("WHDFT")).trim();
          if(dft.charAt(0) == '+')
          {
            dft = dft.substring(1);
          }
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((PackedDecimalFieldDescription)fd).setDFTNull();
          }
          // Handle value
          else
          {
            ((PackedDecimalFieldDescription)fd).setDFT(new BigDecimal(dft));
          }
        }
        break;
      case 'S': // Zoned decimal field
        digits = ((BigDecimal)record.getField("WHFLDO")).intValue();
        decimalPositions = ((BigDecimal)record.getField("WHFLDP")).intValue();
        fd = new ZonedDecimalFieldDescription(new AS400ZonedDecimal(digits, decimalPositions),
                                              fieldName);
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          String dft = ((String)record.getField("WHDFT")).trim();
          if(dft.charAt(0) == '+')
          {
            dft = dft.substring(1);
          }
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((ZonedDecimalFieldDescription)fd).setDFTNull();
          }
          // Handle value
          else
          {
            ((ZonedDecimalFieldDescription)fd).setDFT(new BigDecimal(dft));
          }
        }
        break;
      case 'T': // Time field
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        fd = new TimeFieldDescription(
                                     new AS400Text(((BigDecimal)record.getField("WHFLDB")).intValue(),
                                                   ccsid, system_), //@D0C
                                     fieldName);
        // Set date format and date separator values
        ((TimeFieldDescription)fd).setTIMFMT((String)record.getField("WHFMT"));
        if(!((String)record.getField("WHSEP")).equals(" "))
        {
          ((TimeFieldDescription)fd).setTIMSEP((String)record.getField("WHSEP"));
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((TimeFieldDescription)fd).setDFTNull();
          }
          // Handle CURRENT_TIME (an SQL special value)
          else if(dft.indexOf("CURRENT_TIME") != -1)
          {
            ((TimeFieldDescription)fd).setDFTCurrent();
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            ((TimeFieldDescription)fd).setDFT(adjDft);
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'Z': // Timestamp field
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        fd = new TimestampFieldDescription(
                                          new AS400Text(((BigDecimal)record.getField("WHFLDB")).intValue(),
                                                        ccsid, system_), //@D0C
                                          fieldName);
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            ((TimestampFieldDescription)fd).setDFTNull();
          }
          // Handle CURRENT_TIMESTAMP (an SQL special value)
          else if(dft.indexOf("CURRENT_TIMESTAMP") != -1)
          {
            ((TimestampFieldDescription)fd).setDFTCurrent();
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            ((TimestampFieldDescription)fd).setDFT(adjDft);
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
    }
    // Set if null values are allowed
    if(((String)record.getField("WHNULL")).equals("Y"))
    {
      fd.setALWNULL(true);
    }
    // Add the field description
    rf.addFieldDescription(fd);
  }

  /**
   *Retrieves the file description for the file, and creates a file containing the Java source for
   *a class extending from RecordFormat that represents the record format for the file.  If the
   *file contains more than one record format (for example, is a multiple format logical file), a Java
   *source file for each record format in the file is created; each file will contain the class
   *definition for a single record format.<br>
   *The name of the class is the name of the record format retrieved with the string "Format"
   *appended to it.  The name of the file is the name of the class with the extension .java.<br>
   *The source files generated can be compiled and used as input to the
   *<a href="AS400File.html#setRecordFormat()">AS400File.setRecordFormat()</a> method.<br>
   *The AS/400 system to which to connect and the integrated file system
   *pathname for the file must be set prior to invoking this method.
   *@see AS400FileRecordDescription#AS400FileRecordDescription(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400FileRecordDescription#setPath
   *@see AS400FileRecordDescription#setSystem
   *@param filePath The path in which to create the file.  If <i>filePath</i> is null,
   *the file is created in the current working directory.
   *@param packageName The name of the package in which the class belongs. The <i>packageName</i>
   *is used to specify the package statement in the source code for the class.
   * If this value is null, no package statement is specified in the source code for the class.

   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   *@exception InterruptedException If this thread is interrupted.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
   *@return An array of Strings: { rfName0, contents0, rfName1, contents1, etc... }.
  **/
  public /*@E0D synchronized */ String[] createRecordFormatSource(String packageName) //@C0C
  throws AS400Exception,
      AS400SecurityException,
      IOException,
      InterruptedException
  {
    //////////////////////////////////////////////////////////////////////////////////////////
    // Retrieve the field information for the file
    //////////////////////////////////////////////////////////////////////////////////////////
    // Generate file on the AS/400 containing the file field description
    //////////////////////////////////////////////////////////////////////////////////////////
    String cmd = "DSPFFD FILE(" + library_ + "/" + file_ + ") OUTPUT(*OUTFILE) OUTFILE(QTEMP/JT4FFD)";
    //@B5D theFile_.chooseImpl();
    Record[] records = null;  //@E0A
    synchronized(lockJT4FFD_) //@E0A
    {                         //@E0A
      AS400Message[] msgs = theFile_.execute(cmd); //@B5C

      if(msgs.length > 0)
      {
        if(!(msgs[0].getID().equals("CPF9861") || msgs[0].getID().equals("CPF3030")))
        {
          throw new AS400Exception(msgs);
        }
      }
      else
      { // Unexpected reply
        throw new InternalErrorException("DSPFFD failed to return success message", InternalErrorException.UNKNOWN);
      }

      // Read all the records from the file so we can extract the field information locally
      //@B5D SequentialFile dspffd = new SequentialFile(system_, "/QSYS.LIB/QTEMP.LIB/JT4FFD.FILE");
      AS400FileImplBase dspffd = (AS400FileImplBase)system_.loadImpl("com.ibm.as400.access.AS400FileImplNative",  //@B5A
                                                                     "com.ibm.as400.access.AS400FileImplRemote"); //@B5A
      dspffd.setAll(system_, "/QSYS.LIB/QTEMP.LIB/JT4FFD.FILE",             //@B5A
                    new QWHDRFFDFormat(system_.getCcsid()), false, false, false);  //@B5A
      //@B5D try
      //@B5D {
      //@B5D   dspffd.setRecordFormat(new QWHDRFFDFormat(system_.getCcsid()));
      //@B5D }
      //@B5D catch(PropertyVetoException e)
      //@B5D { // Quiet the compiler
      //@B5D }
      records = dspffd.readAll("seq", 100); //@B5C @D1C @E0C
      dspffd.delete(); //@E0A
    }                  //@E0A

    //////////////////////////////////////////////////////////////////////////////////////////
    // Retrieve the key field information for the file
    //////////////////////////////////////////////////////////////////////////////////////////
    // Generate file on the AS/400 containing the key field description
    cmd = "DSPFD FILE(" + library_ + "/" + file_ + ") TYPE(*ACCPTH) OUTPUT(*OUTFILE) OUTFILE(QTEMP/JT4FD)";
    Record[] keyRecords = null; //@E0A
    synchronized(lockJT4FD_)    //@E0A
    {                           //@E0A
      AS400Message[] msgs = theFile_.execute(cmd); //@B5C @E0C
      if(msgs.length > 0)
      {
        if(!(msgs[0].getID().equals("CPF9861") || msgs[0].getID().equals("CPF3030")))
        {
          throw new AS400Exception(msgs);
        }
      }
      else
      { // Unexpected reply
        throw new InternalErrorException("DSPFD failed to return success message", InternalErrorException.UNKNOWN);
      }

      // Read all the records from the file so we can extract the key field information locally
      //@B5D dspffd = new SequentialFile(system_, "/QSYS.LIB/QTEMP.LIB/JT4FD.FILE");
      AS400FileImplBase dspffd = (AS400FileImplBase)system_.loadImpl("com.ibm.as400.access.AS400FileImplNative",  //@B5A @E0C
                                                                     "com.ibm.as400.access.AS400FileImplRemote"); //@B5A
      dspffd.setAll(system_, "/QSYS.LIB/QTEMP.LIB/JT4FD.FILE",             //@B5A
                    new QWHFDACPFormat(system_.getCcsid()), false, false, false); //@B5A
      //@B5D try
      //@B5D {
      //@B5D   dspffd.setRecordFormat(new QWHFDACPFormat(system_.getCcsid()));
      //@B5D }
      //@B5D catch(PropertyVetoException e)
      //@B5D { // Quiet the compiler
      //@B5D }
      keyRecords = dspffd.readAll("key", 100); //@B5C @D1C @E0C
      dspffd.delete(); //@E0A
    }                  //@E0A

    // Determine the number of record formats contained in the file
    int numberOfRecordFormats = ((BigDecimal)records[0].getField("WHCNT")).intValue();
    int numberOfFields = 0;
    int numberOfKeyFields = 0;
    int recordNumber = 0;
    int keyRecordNumber = 0;
    PrintWriter sourceFile;
    StringWriter sourceString; //@C0A
    String rfNameUntrimmed;
    String rfName;
    String[] filesToWrite = new String[numberOfRecordFormats*2]; //@C0A
    for(int i = 0; i < numberOfRecordFormats; ++i)
    { // Create source file for a particular record format
      // Determine the name of the record format and hence the name of the file and class
      rfNameUntrimmed = (String)records[recordNumber].getField("WHNAME");
      // If the record format is a quoted name, replace the quotes with blanks
      rfName = rfNameUntrimmed.replace('"', ' ').trim();
      filesToWrite[i] = rfName + "Format.java"; //@C0A
      sourceString = new StringWriter(/* 3000 */); //@C0A
      sourceFile = new PrintWriter(sourceString); //@C0C
      // Write the initial information to the file:
      //    Nonexclusive license, diclaimer and copyright
      //    Prolog
      //    package statement
      //    import statements
      //    beginning of class definition
      sourceFile.println("/*******************************************************************************");
      sourceFile.println(" This source is an example of the Java source necessary ");
      sourceFile.println(" to generate a RecordFormat subclass for file " + name_ + ".");
      sourceFile.println(" IBM grants you a nonexclusive license to use this source.");
      sourceFile.println(" You may change and use this souce as necessary.");
      sourceFile.println();
      sourceFile.println();
      sourceFile.println("                            DISCLAIMER");
      sourceFile.println("                            ----------");
      sourceFile.println();
      sourceFile.println(" This source code is provided by IBM for illustrative purposes only.");
      sourceFile.println(" The source has not been thoroughly tested under all conditions.");
      sourceFile.println(" IBM, therefore, cannot guarantee or imply reliability, serviceability,");
      sourceFile.println(" or function of the source.  All source contained herein are provided to you \"AS IS\"");
      sourceFile.println(" without any warranties of any kind.");
      sourceFile.println(" ALL WARRANTIES, INCLUDING BUT NOT LIMITED TO THE IMPLIED");
      sourceFile.println(" WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR");
      sourceFile.println(" PURPOSE, ARE EXPRESSLY DISCLAIMED.");
      sourceFile.println();
      sourceFile.println();
      sourceFile.println(" Your license to this source code provides you no right or licenses to");
      sourceFile.println(" any IBM patents.  IBM has no obligation to defend or indemnify against");
      sourceFile.println(" any claim of infringement, including but not limited to: patents,");
      sourceFile.println(" copyright, trade secret, or intellectual property rights of any kind.");
      sourceFile.println();
      sourceFile.println();
      sourceFile.println("                            COPYRIGHT");
      sourceFile.println("                            ---------");
      sourceFile.println("           5769-JC1 (C) Copyright IBM CORP. 1997, 1999");
      sourceFile.println("           All rights reserved.");
      sourceFile.println("           US Government Users Restricted Rights -");
      sourceFile.println("           Use, duplication or disclosure restricted");
      sourceFile.println("           by GSA ADP Schedule Contract with IBM Corp.");
      sourceFile.println("           Licensed Material - Property of IBM");
      sourceFile.println("********************************************************************************/");
      sourceFile.println();
      sourceFile.println("// Created by AS400FileRecordDescription on " + new Date());
      sourceFile.println();
      if(packageName != null)
      {
        sourceFile.println("package " + packageName + ";");
      }
      sourceFile.println();
      sourceFile.println("import java.math.BigDecimal;");
      sourceFile.println("import com.ibm.as400.access.RecordFormat;");
      sourceFile.println("import com.ibm.as400.access.AS400Bin2;");
      sourceFile.println("import com.ibm.as400.access.AS400Bin4;");
      sourceFile.println("import com.ibm.as400.access.AS400Bin8;"); //@F0A
      sourceFile.println("import com.ibm.as400.access.AS400ByteArray;");
      sourceFile.println("import com.ibm.as400.access.AS400Float4;");
      sourceFile.println("import com.ibm.as400.access.AS400Float8;");
      sourceFile.println("import com.ibm.as400.access.AS400PackedDecimal;");
      sourceFile.println("import com.ibm.as400.access.AS400Text;");
      sourceFile.println("import com.ibm.as400.access.AS400ZonedDecimal;");
      sourceFile.println("import com.ibm.as400.access.BinaryFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.CharacterFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.DateFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.DBCSEitherFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.DBCSGraphicFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.DBCSOnlyFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.DBCSOpenFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.FieldDescription;");
      sourceFile.println("import com.ibm.as400.access.FloatFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.HexFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.PackedDecimalFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.TimeFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.TimestampFieldDescription;");
      sourceFile.println("import com.ibm.as400.access.ZonedDecimalFieldDescription;");
      sourceFile.println();
      // Determine the name of the class from the file name
      sourceFile.println("public class " + rfName  + "Format extends RecordFormat");
      sourceFile.println("{");
      sourceFile.println();
      sourceFile.println("  public " + rfName + "Format()");
      sourceFile.println("  {");


      // Write out the call to super with the record format name
      sourceFile.println("    super(\"" + rfName + "\");");
      // Get the field descriptions.
      numberOfFields = ((BigDecimal)records[recordNumber].getField("WHNFLD")).intValue();
      if(numberOfFields > 0)
      { // Number of fields is greater than 0
        sourceFile.println("    // Add field descriptions to this record format");
        for(int j = 0; j < numberOfFields; ++j, ++recordNumber)
        { // Add a field description to the record format
          writeFieldDescription(sourceFile, records[recordNumber]);
        }
      }

      // Get the key field descriptions for this record format if there are any
      numberOfKeyFields = ((BigDecimal)keyRecords[keyRecordNumber].getField("APNKYF")).intValue();
      if(numberOfKeyFields > 0)
      { // Number of key fields is greater than 0
        sourceFile.println("    // Add key field descriptions to this record format");
        for(int j = 0; j < numberOfKeyFields; ++j, ++keyRecordNumber)
        { // Add a key field description to the record format
          sourceFile.println("    addKeyFieldDescription(\"" + ((String)keyRecords[keyRecordNumber].getField("APKEYF")).trim() + "\");");
        }
      }
      // End the class
      sourceFile.println("  }");
      sourceFile.println("}");
      if(sourceFile.checkError())
      {
        sourceFile.close();
        throw new InternalErrorException("Error writing to sourceFile.", InternalErrorException.UNKNOWN);
      }
      sourceFile.close();
      filesToWrite[i+1] = sourceString.toString(); //@C0A
    }
    return filesToWrite; //@C0A
  }


  //@B5A
  /**
   * Used internally to parse the pathname and set the individual
   * library, filename, and member strings.
  **/
  private void parseName(String name)
  {
    // Construct a QSYSObjectPathName object and parse out the library,
    // file and member names
    QSYSObjectPathName ifs = new QSYSObjectPathName(name);
    if(!(ifs.getObjectType().equals("FILE") || ifs.getObjectType().equals("MBR")))
    { // Invalid object type
      throw new IllegalPathNameException(name, IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
    }
    // Set the instance data as appropriate
    library_ = ifs.getLibraryName();
    file_ = ifs.getObjectName();
    if(ifs.getObjectType().equals("FILE"))
    { // No member specified; default member to *FIRST
      member_ = "*FIRST";
    }
    else
    { // Member specified; if special value %FILE% was specified, member name
      // is the file name
      member_ = (ifs.getMemberName().equalsIgnoreCase("*FILE") ? file_ :
                 ifs.getMemberName());
    }
    name_ = name;
  }


  /**
   *Retrieves the file description for the file, and creates a RecordFormat
   *object for each record format, which can be used as input to the
   *<a href="AS400File.html#setRecordFormat()">AS400File.setRecordFormat()</a>
   *method.  If the file is a physical file, the RecordFormat array returned
   *contains one
   *RecordFormat object.  If the file is a multiple format logical file, the
   *RecordFormat array may contain
   *more than one RecordFormat object.
   *The AS/400 system to which to connect and the integrated file system
   *pathname for the file must be set prior to invoking this method.
   *@see AS400FileRecordDescription#AS400FileRecordDescription(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400FileRecordDescription#setPath
   *@see AS400FileRecordDescription#setSystem

   *@return The record format(s) for the file.

   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   *@exception InterruptedException If this thread is interrupted.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.

  **/
  public /*@E0D synchronized */ RecordFormat[] retrieveRecordFormat()
  throws AS400Exception,
      AS400SecurityException,
      InterruptedException,
      IOException
  {
    //////////////////////////////////////////////////////////////////////////////////////////
    // Retrieve the field information for the file
    //////////////////////////////////////////////////////////////////////////////////////////
    // Generate file on the AS/400 containing the file field description
    //////////////////////////////////////////////////////////////////////////////////////////
    String cmd = "DSPFFD FILE(" + library_ + "/" + file_ + ") OUTPUT(*OUTFILE) OUTFILE(QTEMP/JT4FFD)";
    //@B5D theFile_.chooseImpl();
    Record[] records = null;  //@E0A
    synchronized(lockJT4FFD_) //@E0A
    {                         //@E0A
      AS400Message[] msgs = theFile_.execute(cmd); //@B5C

      if(msgs.length > 0)
      {
        if(!(msgs[0].getID().equals("CPF9861") || msgs[0].getID().equals("CPF3030")))
        {
          throw new AS400Exception(msgs);
        }
      }
      else
      { // Unexpected reply
        throw new InternalErrorException("DSPFFD failed to return success message", InternalErrorException.UNKNOWN);
      }

      // Read all the records from the file so we can extract the field information locally
      //@B5D SequentialFile dspffd = new SequentialFile(system_, "/QSYS.LIB/QTEMP.LIB/JT4FFD.FILE");
      AS400FileImplBase dspffd = (AS400FileImplBase)system_.loadImpl("com.ibm.as400.access.AS400FileImplNative",  //@B5A
                                                                     "com.ibm.as400.access.AS400FileImplRemote"); //@B5A
      dspffd.setAll(system_, "/QSYS.LIB/QTEMP.LIB/JT4FFD.FILE",             //@B5A
                    new QWHDRFFDFormat(system_.getCcsid()), false, false, false);  //@B5A

      //@B5D try
      //@B5D {
      //@B5D   dspffd.setRecordFormat(new QWHDRFFDFormat(system_.getCcsid()));
      //@B5D }
      //@B5D catch(PropertyVetoException e)
      //@B5D { // Quiet the compiler
      //@B5D }
      records = dspffd.readAll("seq", 100); //@B5C @D1C @E0C
      dspffd.delete(); //@E0A
    }                  //@E0A

    //////////////////////////////////////////////////////////////////////////////////////////
    // Retrieve the key field information for the file
    //////////////////////////////////////////////////////////////////////////////////////////
    // Generate file on the AS/400 containing the key field description
    cmd = "DSPFD FILE(" + library_ + "/" + file_ + ") TYPE(*ACCPTH) OUTPUT(*OUTFILE) OUTFILE(QTEMP/JT4FD)";
    Record[] keyRecords = null; //@E0A
    synchronized(lockJT4FD_)    //@E0A
    {                           //@E0A
      AS400Message[] msgs = theFile_.execute(cmd); //@B5C @E0C

      if(msgs.length > 0)
      {
        if(!(msgs[0].getID().equals("CPF9861") || msgs[0].getID().equals("CPF3030")))
        {
          throw new AS400Exception(msgs);
        }
      }
      else
      { // Unexpected reply
        throw new InternalErrorException("DSPFFD failed to return success message", InternalErrorException.UNKNOWN);
      }

      // Read all the records from the file so we can extract the key field information locally
      //@B5D dspffd = new SequentialFile(system_, "/QSYS.LIB/QTEMP.LIB/JT4FD.FILE");
      AS400FileImplBase dspffd = (AS400FileImplBase)system_.loadImpl("com.ibm.as400.access.AS400FileImplNative",  //@B5A @E0C
                                                                     "com.ibm.as400.access.AS400FileImplRemote"); //@B5A
      dspffd.setAll(system_, "/QSYS.LIB/QTEMP.LIB/JT4FD.FILE",              //@B5A
                    new QWHFDACPFormat(system_.getCcsid()), false, false, false);  //@B5A
      //@B5D try
      //@B5D {
      //@B5D   dspffd.setRecordFormat(new QWHFDACPFormat(system_.getCcsid()));
      //@B5D }
      //@B5D catch(PropertyVetoException e)
      //@B5D { // Quiet the compiler
      //@B5D }
      keyRecords = dspffd.readAll("key", 100); //@B5C @D1C @E0C
      dspffd.delete(); //@E0A
    }                  //@E0A

    // Determine the number of record formats contained in the file
    int numberOfRecordFormats = ((BigDecimal)records[0].getField("WHCNT")).intValue();
    int numberOfFields = 0;
    int numberOfKeyFields = 0;
    int recordNumber = 0;
    int keyRecordNumber = 0;
    String rfNameUntrimmed;
    String rfName;
    RecordFormat[] rfs = new RecordFormat[numberOfRecordFormats];
    for(int i = 0; i < numberOfRecordFormats; ++i)
    { // Create source file for a particular record format
      // Determine the name of the record format and hence the name of the file and class
      rfNameUntrimmed = (String)records[recordNumber].getField("WHNAME");
      rfName = rfNameUntrimmed.trim();
      rfs[i] = new RecordFormat(rfName);
      // Get the field descriptions.
      numberOfFields = ((BigDecimal)records[recordNumber].getField("WHNFLD")).intValue();
      if(numberOfFields > 0)
      { // Number of fields is greater than 0
        for(int j = 0; j < numberOfFields; ++j, ++recordNumber)
        { // Add a field description to the record format
          addFieldDescription(rfs[i], records[recordNumber]);
        }
      }

      // Get the key field descriptions for this record format if there are any
      numberOfKeyFields = ((BigDecimal)keyRecords[keyRecordNumber].getField("APNKYF")).intValue();
      if(numberOfKeyFields > 0)
      { // Number of key fields is greater than 0
        for(int j = 0; j < numberOfKeyFields; ++j, ++keyRecordNumber)
        { // Add a key field description to the record format
          rfs[i].addKeyFieldDescription(((String)keyRecords[keyRecordNumber].getField("APKEYF")).trim());
        }
      }
    }
    return rfs;
  }


  /**
   *Sets the <a href="ipnpgmgd.html">integrated file system path name</a> for
   *the file.
   *@param name The <a href="ipnpgmgd.html">integrated file system path name</a>
   *of the file.  If a member is not specified in <i>name</i>, the first
   *member of the file is used.
  **/
  public void setPath(String name)
  {
    if(theFile_ != null) //@B5A
    {
      theFile_.setPath(name);
    }
    parseName(name); //@B5A
    name_ = name;
  }

  /**
   *Sets the system to which to connect.
   *@param system The system to which to conenct.
  **/
  public void setSystem(AS400Impl system) //@B5C
  {
    if(theFile_ == null) //@B5A
    {
      theFile_ = (AS400FileImplBase)((AS400ImplRemote)system).loadImpl("com.ibm.as400.access.AS400FileImplNative",  //@B5A
                                                                       "com.ibm.as400.access.AS400FileImplRemote"); //@B5A
      if(!name_.equals("")) //@B5A
      {
        theFile_.setPath(name_); //@B5A
      }
    }
    theFile_.setSystem(system);

    system_ = (AS400ImplRemote)system; //@B5C
  }

  /**
   *Writes a field description to the specified file.  The field description
   *is created with information extracted from the Record object provided.
   *@param sourceFile The file to which to write the field description.
   *@param record The record from which to obtain the field information.
   *@exception If an error occurs during conversion.
  **/
  public void writeFieldDescription(PrintWriter sourceFile, Record record)
  throws UnsupportedEncodingException
  {
    char fieldType = ((String)record.getField("WHFLDT")).charAt(0);
    FieldDescription fd = null;
    int digits;
    int decimalPositions;
    int byteLength;              //@A1A: For float field descriptions
    String fieldName = ((String)record.getField("WHFLDE")).trim();
    int ccsid;
    switch(fieldType)
    {
      case 'A': // Character field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        sourceFile.println("    addFieldDescription(new CharacterFieldDescription(new AS400Text(" + String.valueOf((((String)record.getField("WHVARL")).equals("Y"))?
                                                                                                                   ((BigDecimal)record.getField("WHFLDB")).intValue() - 2 :
                                                                                                                   ((BigDecimal)record.getField("WHFLDB")).intValue()) + ", " +
                           ccsid + "), \"" +
                           fieldName + "\"));");
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            sourceFile.println("    ((CharacterFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVARLEN(" + ((BigDecimal)record.getField("WHALLC")).intValue() + ");");
          }
          else
          { // No length was specified on the VARLEN keyword
            sourceFile.println("    ((CharacterFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVariableLength(true);");
          }
        }
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((CharacterFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            sourceFile.println("    ((CharacterFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(\"" + adjDft + "\");");
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'B': // Binary field
        digits = ((BigDecimal)record.getField("WHFLDO")).intValue();
//      if (digits < 6)
        if(digits < 5) // @A2C
        {
          sourceFile.println("    addFieldDescription(new BinaryFieldDescription(new AS400Bin2(), \"" +
                             fieldName + "\", \"" +
                             fieldName + "\", " + String.valueOf(digits) + "));");
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              sourceFile.println("    ((BinaryFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFTNull();");
            }
            // Handle value
            else
            {
              sourceFile.println("    ((BinaryFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFT(new Short(\"" + dft + "\"));");
            }
          }
        }
        else if (digits < 10) //@F0C
        {
          sourceFile.println("    addFieldDescription(new BinaryFieldDescription(new AS400Bin4(), \"" +
                             fieldName + "\", \"" +
                             fieldName + "\", " + String.valueOf(digits) + "));");
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              sourceFile.println("    ((BinaryFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFTNull();");
            }
            // Handle value
            else
            {
              sourceFile.println("    ((BinaryFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFT(new Integer(\"" + dft + "\"));");
            }
          }
        }
        else //@F0A
        {
          sourceFile.println("    addFieldDescription(new BinaryFieldDescription(new AS400Bin8(), \"" +
                             fieldName + "\", \"" +
                             fieldName + "\", " + String.valueOf(digits) + "));");
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              sourceFile.println("    ((BinaryFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFTNull();");
            }
            // Handle value
            else
            {
              sourceFile.println("    ((BinaryFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFT(new Long(\"" + dft + "\"));");
            }
          }
        }
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        break;
      case 'E': // DBCS-Either field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        sourceFile.println("    addFieldDescription(new DBCSEitherFieldDescription(new AS400Text("
                           + String.valueOf((((String)record.getField("WHVARL")).equals("Y"))?
                                            ((BigDecimal)record.getField("WHFLDB")).intValue() - 2 :
                                            ((BigDecimal)record.getField("WHFLDB")).intValue()) + ", " +
                           ccsid + "), \"" +
                           fieldName + "\"));");
        // Set if variable length
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            sourceFile.println("    ((DBCSEitherFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVARLEN(" + ((BigDecimal)record.getField("WHALLC")).intValue() + ");");
          }
          else
          { // No length was specified on the VARLEN keyword
            sourceFile.println("    ((DBCSEitherFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVariableLength(true);");
          }
        }
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((DBCSEitherFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            sourceFile.println("    ((DBCSEitherFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(\"" + adjDft + "\");");
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'F': // Float field
        digits = ((BigDecimal)record.getField("WHFLDO")).intValue();
        decimalPositions = ((BigDecimal)record.getField("WHFLDP")).intValue();
        //@A1A: Retrieve byte length to determine if field is single or double
        // precision
        byteLength = ((BigDecimal)record.getField("WHFLDB")).intValue(); //@A1A
        if(byteLength == 4)      //@A1A
        {
          sourceFile.println("    addFieldDescription(new FloatFieldDescription(new AS400Float4(), \"" +
                             fieldName + "\", \"" +
                             fieldName + "\", " + String.valueOf(digits) +
                             ", " + String.valueOf(decimalPositions) + "));");
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              sourceFile.println("    ((FloatFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFTNull();");
            }
            // Handle value
            else
            {
              sourceFile.println("    ((FloatFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFT(new Float(\"" + dft + "\"));");
            }
          }
        }
        else if(byteLength == 8)  //@A1A
        {
          sourceFile.println("    addFieldDescription(new FloatFieldDescription(new AS400Float8(), \"" +
                             fieldName + "\", \"" +
                             fieldName + "\", " + String.valueOf(digits) +
                             ", " + String.valueOf(decimalPositions) + "));");
          // Set the FLTPCN keyword to *DOUBLE
          sourceFile.println("    ((FloatFieldDescription)getFieldDescription(\"" +
                             fieldName + "\")).setFLTPCN(\"*DOUBLE\");");
          // Set the DFT keyword value if specified
          if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
          {
            String dft = ((String)record.getField("WHDFT")).trim();
            if(dft.charAt(0) == '+')
            {
              dft = dft.substring(1);
            }
            // @B0C
            // Check for any special values that could be specified as the default.
            // Handle *NULL
            if(dft.indexOf("*NULL") != -1)
            {
              sourceFile.println("    ((FloatFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFTNull();");
            }
            // Handle value
            else
            {
              sourceFile.println("    ((FloatFieldDescription)getFieldDescription(\"" +
                                 fieldName + "\")).setDFT(new Double(\"" + dft + "\"));");
            }
          }
        }
        else    //@A1A: This should never occur, but for completeness....
        {
          //@A1A
          throw new InternalErrorException("FloatFieldDescription error in byte length", InternalErrorException.UNKNOWN);  //@A1A
        }       //@A1A
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        break;
      case 'G': // DBCS-Graphic field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        sourceFile.println("    addFieldDescription(new DBCSGraphicFieldDescription(new AS400Text("
                           + String.valueOf((((String)record.getField("WHVARL")).equals("Y"))?
                                            ((BigDecimal)record.getField("WHFLDB")).intValue() - 2 :
                                            ((BigDecimal)record.getField("WHFLDB")).intValue()) + ", " +
                           ccsid + "), \"" +
                           fieldName + "\"));");
        // Set if variable length
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            sourceFile.println("    ((DBCSGraphicFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVARLEN(" + ((BigDecimal)record.getField("WHALLC")).intValue() + ");");
          }
          else
          { // No length was specified on the VARLEN keyword
            sourceFile.println("    ((DBCSGraphicFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVariableLength(true);");
          }
        }
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((DBCSGraphicFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            sourceFile.println("    ((DBCSGraphicFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(\"" + adjDft + "\");");
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'H': // Hex field
        // Need to get the length in bytes of the field
        // when creating the AS400ByteArray object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        sourceFile.println("    addFieldDescription(new HexFieldDescription(new AS400ByteArray("
                           + String.valueOf((((String)record.getField("WHVARL")).equals("Y"))?
                                            ((BigDecimal)record.getField("WHFLDB")).intValue() - 2 :
                                            ((BigDecimal)record.getField("WHFLDB")).intValue()) + "), \"" +
                           fieldName + "\"));");
        // Set if variable length
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            sourceFile.println("    ((HexFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVARLEN(" + ((BigDecimal)record.getField("WHALLC")).intValue() + ");");
          }
          else
          { // No length was specified on the VARLEN keyword
            sourceFile.println("    ((HexFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVariableLength(true);");
          }
        }
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // @B0A: Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((HexFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            byte[] byteDft = adjDft.getBytes();
            sourceFile.print("    ((HexFieldDescription)getFieldDescription(\"" +
                             fieldName + "\")).setDFT(new byte[] { ");
            sourceFile.print(byteDft[0]);
            for(int i=1; i<byteDft.length; ++i)
            {
              sourceFile.print(", ");
              sourceFile.print(byteDft[i]);
            }
            sourceFile.println(" });");
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'J': // DBCS-Only field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        sourceFile.println("    addFieldDescription(new DBCSOnlyFieldDescription(new AS400Text("
                           + String.valueOf((((String)record.getField("WHVARL")).equals("Y"))?
                                            ((BigDecimal)record.getField("WHFLDB")).intValue() - 2 :
                                            ((BigDecimal)record.getField("WHFLDB")).intValue()) + ", " +
                           ccsid + "), \"" +
                           fieldName + "\"));");
        // Set if variable length
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            sourceFile.println("    ((DBCSOnlyFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVARLEN(" + ((BigDecimal)record.getField("WHALLC")).intValue() + ");");
          }
          else
          { // No length was specified on the VARLEN keyword
            sourceFile.println("    ((DBCSOnlyFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVariableLength(true);");
          }
        }
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((DBCSOnlyFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            sourceFile.println("    ((DBCSOnlyFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(\"" + adjDft + "\");");
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'L': // Date field
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        sourceFile.println("    addFieldDescription(new DateFieldDescription(new AS400Text("
                           + String.valueOf(((BigDecimal)record.getField("WHFLDB")).intValue()) + ", " +
                           ccsid + "), \"" +
                           fieldName + "\"));");
        // Set date format and date separator values
        sourceFile.println("    ((DateFieldDescription)getFieldDescription(\"" +
                           fieldName + "\")).setDATFMT(\"" + (String)record.getField("WHFMT") + "\");");
        if(!((String)record.getField("WHSEP")).equals(" "))
        {
          sourceFile.println("    ((DateFieldDescription)getFieldDescription(\"" +
                             fieldName + "\")).setDATSEP(\"" + (String)record.getField("WHSEP") + "\");");
        }
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((DateFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle CURRENT_DATE (an SQL special value)
          else if(dft.indexOf("CURRENT_DATE") != -1)
          {
            sourceFile.println("    ((DateFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTCurrent();");
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            sourceFile.println("    ((DateFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(\"" + adjDft + "\");");
          }
        }
        break;
      case 'O': // DBCS-Open field
        // Need to get the length in bytes of the field and the ccsid of the field
        // when creating the AS400Text object.  The name of the field is set to be
        // the DDS name of the field (which causes the DDS name of the field description
        // to be the DDS name as well).
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        sourceFile.println("    addFieldDescription(new DBCSOpenFieldDescription(new AS400Text("
                           + String.valueOf((((String)record.getField("WHVARL")).equals("Y"))?
                                            ((BigDecimal)record.getField("WHFLDB")).intValue() - 2 :
                                            ((BigDecimal)record.getField("WHFLDB")).intValue()) + ", " +
                           ccsid + "), \"" +
                           fieldName + "\"));");
        // Set if variable length
        if(((String)record.getField("WHVARL")).equals("Y"))
        {
          if(((BigDecimal)record.getField("WHALLC")).intValue() > 0)
          { // A length was specified on the VARLEN keyword
            sourceFile.println("    ((DBCSOpenFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVARLEN(" + ((BigDecimal)record.getField("WHALLC")).intValue() + ");");
          }
          else
          { // No length was specified on the VARLEN keyword
            sourceFile.println("    ((DBCSOpenFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setVariableLength(true);");
          }
        }
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((DBCSOpenFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            sourceFile.println("    ((DBCSOpenFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(\"" + adjDft + "\");");
          }
          // Do nothing if a special value was present and we didn't know what
          // it was.
        }
        break;
      case 'P': // Packed decimal field
        digits = ((BigDecimal)record.getField("WHFLDO")).intValue();
        decimalPositions = ((BigDecimal)record.getField("WHFLDP")).intValue();
        sourceFile.println("    addFieldDescription(new PackedDecimalFieldDescription(new AS400PackedDecimal(" +
                           String.valueOf(digits) + ", " + String.valueOf(decimalPositions) + "), \"" +
                           fieldName + "\"));");
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          String dft = ((String)record.getField("WHDFT")).trim();
          if(dft.charAt(0) == '+')
          {
            dft = dft.substring(1);
          }
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((PackedDecimalFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle value
          else
          {
            sourceFile.println("    ((PackedDecimalFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(new BigDecimal(\"" + dft + "\"));");
          }
        }
        break;
      case 'S': // Zoned decimal field
        digits = ((BigDecimal)record.getField("WHFLDO")).intValue();
        decimalPositions = ((BigDecimal)record.getField("WHFLDP")).intValue();
        sourceFile.println("    addFieldDescription(new ZonedDecimalFieldDescription(new AS400ZonedDecimal(" +
                           String.valueOf(digits) + ", " + String.valueOf(decimalPositions) + "), \"" +
                           fieldName + "\"));");
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          String dft = ((String)record.getField("WHDFT")).trim();
          if(dft.charAt(0) == '+')
          {
            dft = dft.substring(1);
          }
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((ZonedDecimalFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle value
          else
          {
            sourceFile.println("    ((ZonedDecimalFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(new BigDecimal(\"" + dft + "\"));");
          }
        }
        break;
      case 'T': // Time field
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        sourceFile.println("    addFieldDescription(new TimeFieldDescription(new AS400Text("
                           + String.valueOf(((BigDecimal)record.getField("WHFLDB")).intValue()) + ", " +
                           ccsid + "), \"" +
                           fieldName + "\"));");
        // Set date format and date separator values
        sourceFile.println("    ((TimeFieldDescription)getFieldDescription(\"" +
                           fieldName + "\")).setTIMFMT(\"" + (String)record.getField("WHFMT") + "\");");
        if(!((String)record.getField("WHSEP")).equals(" "))
        {
          sourceFile.println("    ((TimeFieldDescription)getFieldDescription(\"" +
                             fieldName + "\")).setTIMSEP(\"" + (String)record.getField("WHSEP") + "\");");
        }
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((TimeFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle CURRENT_TIME (an SQL special value)
          else if(dft.indexOf("CURRENT_TIME") != -1)
          {
            sourceFile.println("    ((TimeFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTCurrent();");
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            sourceFile.println("    ((TimeFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(\"" + adjDft + "\");");
          }
        }
        break;
      case 'Z': // Timestamp field
        ccsid = ((BigDecimal)record.getField("WHCCSID")).intValue();
        if(ccsid == 65535)
        {
          // 65535 is not a valid ccsid, retrieve the default system ccsid
          ccsid = system_.getCcsid();
        }
        sourceFile.println("    addFieldDescription(new TimestampFieldDescription(new AS400Text("
                           + String.valueOf(((BigDecimal)record.getField("WHFLDB")).intValue()) + ", " +
                           ccsid + "), \"" +
                           fieldName + "\"));");
        // Set if null values are allowed
        if(((String)record.getField("WHNULL")).equals("Y"))
        {
          sourceFile.println("    getFieldDescription(\"" +
                             fieldName + "\").setALWNULL(true);");
        }
        // Set the DFT keyword value if specified
        if(((BigDecimal)record.getField("WHDFTL")).intValue() > 0)
        {
          // Need to strip off the beginning and ending apostrophes
          String dft = (String)record.getField("WHDFT");
          // @B0C
          // Check for any special values that could be specified as the default.
          // Handle *NULL
          if(dft.indexOf("*NULL") != -1)
          {
            sourceFile.println("    ((TimestampFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTNull();");
          }
          // Handle CURRENT_TIMESTAMP (an SQL special value)
          else if(dft.indexOf("CURRENT_TIMESTAMP") != -1)
          {
            sourceFile.println("    ((TimestampFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFTCurrent();");
          }
          // Handle 'value'
          else if(dft.indexOf("'") != -1)
          {
            String adjDft = dft.substring(dft.indexOf("'") + 1, dft.lastIndexOf("'"));
            sourceFile.println("    ((TimestampFieldDescription)getFieldDescription(\"" +
                               fieldName + "\")).setDFT(\"" + adjDft + "\");");
          }
        }
        break;
    }
  }
}

/**
 *Class representing the record format of the data returned from the
 *DSPFD command (Display File Description) when the type of data requested
 *is *ACCPTH.  *ACCPTH returns data describing the key fields of the file
 *if any exist.
**/
class QWHFDACPFormat extends RecordFormat
{
  private String x = Copyright.copyright;

  QWHFDACPFormat(int ccsid)
  {
    super("QWHFDACP");

    AS400Text txt1 = new AS400Text(1, ccsid);
    AS400Text txt3 = new AS400Text(3, ccsid);
    AS400Text txt4 = new AS400Text(4, ccsid);
    AS400Text txt6 = new AS400Text(6, ccsid);
    AS400Text txt8 = new AS400Text(8, ccsid);
    AS400Text txt10 = new AS400Text(10, ccsid);
    AS400PackedDecimal p30 = new AS400PackedDecimal(3, 0);

    addFieldDescription(new CharacterFieldDescription(txt1, "APRCEN"));
    addFieldDescription(new CharacterFieldDescription(txt6, "APRDAT"));
    addFieldDescription(new CharacterFieldDescription(txt6, "APRTIM"));
    addFieldDescription(new CharacterFieldDescription(txt10, "APFILE"));
    addFieldDescription(new CharacterFieldDescription(txt10, "APLIB"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APFTYP"));
    addFieldDescription(new CharacterFieldDescription(txt4, "APFILA"));
    addFieldDescription(new CharacterFieldDescription(txt3, "APMXD"));
    addFieldDescription(new CharacterFieldDescription(txt6, "APFATR"));
    addFieldDescription(new CharacterFieldDescription(txt8, "APSYSN"));
    addFieldDescription(new PackedDecimalFieldDescription(p30, "APASP"));
    addFieldDescription(new CharacterFieldDescription(txt4, "APRES"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APMANT"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APUNIQ"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APKEYO"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APSELO"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APACCP"));
    addFieldDescription(new PackedDecimalFieldDescription(p30, "APNSCO"));
    addFieldDescription(new CharacterFieldDescription(txt10, "APBDF"));
    addFieldDescription(new CharacterFieldDescription(txt10, "APBOL"));
    addFieldDescription(new CharacterFieldDescription(txt10, "APBOLF"));
    addFieldDescription(new PackedDecimalFieldDescription(p30, "APNKYF"));
    addFieldDescription(new CharacterFieldDescription(txt10, "APKEYF"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APKSEQ"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APKSIN"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APKZD"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APKASQ"));
    addFieldDescription(new PackedDecimalFieldDescription(p30, "APKEYN"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APJOIN"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APACPJ"));
    addFieldDescription(new CharacterFieldDescription(txt1, "APRIKY"));
  }
}

/**
 *Class representing the record format of the data returned from the
 *DSPFD command (Display File Description) when the type of data requested
 *is *RCDFMT.  *RCDFMT returns data describing the record format of the file.
**/
class QWHFDFMTFormat extends RecordFormat
{
  private String x = Copyright.copyright;

  QWHFDFMTFormat(int ccsid)
  {
    super("QWHFDFMT");

    AS400Text txt1 = new AS400Text(1, ccsid);
    AS400Text txt3 = new AS400Text(3, ccsid);
    AS400Text txt4 = new AS400Text(4, ccsid);
    AS400Text txt6 = new AS400Text(6, ccsid);
    AS400Text txt8 = new AS400Text(8, ccsid);
    AS400Text txt10 = new AS400Text(10, ccsid);
    AS400Text txt13 = new AS400Text(13, ccsid);
    AS400Text txt50 = new AS400Text(50, ccsid);
    AS400PackedDecimal p10 = new AS400PackedDecimal(1, 0);
    AS400PackedDecimal p30 = new AS400PackedDecimal(3, 0);
    AS400PackedDecimal p31 = new AS400PackedDecimal(3, 1);
    AS400PackedDecimal p41 = new AS400PackedDecimal(4, 1);
    AS400PackedDecimal p40 = new AS400PackedDecimal(4, 0);
    AS400PackedDecimal p50 = new AS400PackedDecimal(5, 0);

    addFieldDescription(new CharacterFieldDescription(txt1, "RFRCEN"));
    addFieldDescription(new CharacterFieldDescription(txt6, "RFRDAT"));
    addFieldDescription(new CharacterFieldDescription(txt6, "RFRTIM"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFFILE"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFLIB"));
    addFieldDescription(new CharacterFieldDescription(txt1, "RFFTYP"));
    addFieldDescription(new CharacterFieldDescription(txt4, "RFFILA"));
    addFieldDescription(new CharacterFieldDescription(txt1, "RFMXDD"));
    addFieldDescription(new CharacterFieldDescription(txt1, "RFMXDC"));
    addFieldDescription(new CharacterFieldDescription(txt1, "RFMXDB"));
    addFieldDescription(new CharacterFieldDescription(txt6, "RFFATR"));
    addFieldDescription(new CharacterFieldDescription(txt8, "RFSYSN"));
    addFieldDescription(new PackedDecimalFieldDescription(p30, "RFASP"));
    addFieldDescription(new CharacterFieldDescription(txt4, "RFRES"));
    addFieldDescription(new PackedDecimalFieldDescription(p40, "RFTOTF"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFNAME"));
    addFieldDescription(new PackedDecimalFieldDescription(p50, "RFFLDN"));
    addFieldDescription(new PackedDecimalFieldDescription(p50, "RFLEN"));
    addFieldDescription(new CharacterFieldDescription(txt13, "RFID"));
    addFieldDescription(new CharacterFieldDescription(txt1, "RFTYPE"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFASFN"));
    addFieldDescription(new CharacterFieldDescription(txt50, "RFFTXT"));
    addFieldDescription(new PackedDecimalFieldDescription(p40, "RFHIGH"));
    addFieldDescription(new PackedDecimalFieldDescription(p50, "RFWIDE"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFFONT"));
    addFieldDescription(new PackedDecimalFieldDescription(p30, "RFPGRT"));
    addFieldDescription(new PackedDecimalFieldDescription(p31, "RFLPI"));
    addFieldDescription(new PackedDecimalFieldDescription(p31, "RFCHWD"));
    addFieldDescription(new PackedDecimalFieldDescription(p31, "RFCHHI"));
    addFieldDescription(new PackedDecimalFieldDescription(p41, "RFPNTS"));
    addFieldDescription(new CharacterFieldDescription(txt1, "RFWIN"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFFCSN"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFFCSL"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFFCPN"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFFCPL"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFCDFN"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFCDFL"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFDCDF"));
    addFieldDescription(new CharacterFieldDescription(txt10, "RFDCDL"));
    addFieldDescription(new CharacterFieldDescription(txt1, "RFMNB"));
    addFieldDescription(new CharacterFieldDescription(txt1, "RFPLD"));
  }
}

/**
 *Class representing the record format of the data returned from the
 *DSPFFD command (Display File Field Description).
**/
class QWHDRFFDFormat extends RecordFormat
{
  private String x = Copyright.copyright;

  QWHDRFFDFormat(int ccsid)
  {
    super("QWHDRFFD");

    AS400Text txt1 = new AS400Text(1, ccsid);
    AS400Text txt2 = new AS400Text(2, ccsid);
    AS400Text txt3 = new AS400Text(3, ccsid);
    AS400Text txt4 = new AS400Text(4, ccsid);
    AS400Text txt6 = new AS400Text(6, ccsid);
    AS400Text txt7 = new AS400Text(7, ccsid);
    AS400Text txt8 = new AS400Text(8, ccsid);
    AS400Text txt10 = new AS400Text(10, ccsid);
    AS400Text txt13 = new AS400Text(13, ccsid);
    AS400Text txt20 = new AS400Text(20, ccsid);
    AS400Text txt30 = new AS400Text(30, ccsid);
    AS400Text txt32 = new AS400Text(32, ccsid);
    AS400Text txt50 = new AS400Text(50, ccsid);
    AS400PackedDecimal p10 = new AS400PackedDecimal(1, 0);
    AS400PackedDecimal p20 = new AS400PackedDecimal(2, 0);
    AS400PackedDecimal p30 = new AS400PackedDecimal(3, 0);
    AS400PackedDecimal p40 = new AS400PackedDecimal(4, 0);
    AS400PackedDecimal p50 = new AS400PackedDecimal(5, 0);
    AS400PackedDecimal p31 = new AS400PackedDecimal(3, 1);
    AS400PackedDecimal p41 = new AS400PackedDecimal(4, 1);
    AS400ZonedDecimal z20 = new AS400ZonedDecimal(2, 0);
    AS400ZonedDecimal z30 = new AS400ZonedDecimal(3, 0);
    AS400ZonedDecimal z40 = new AS400ZonedDecimal(4, 0);
    AS400ZonedDecimal z50 = new AS400ZonedDecimal(5, 0);
    AS400ZonedDecimal z31 = new AS400ZonedDecimal(3, 1);

    addFieldDescription(new CharacterFieldDescription(txt10, "WHFILE"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHLIB"));
    addFieldDescription(new CharacterFieldDescription(txt7, "WHCRTD"));
    addFieldDescription(new CharacterFieldDescription(txt1, "WHFTYP"));
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHCNT"));
    addFieldDescription(new CharacterFieldDescription(txt13, "WHDTTM"));
    // Record format name
    addFieldDescription(new CharacterFieldDescription(txt10, "WHNAME"));
    addFieldDescription(new CharacterFieldDescription(txt13, "WHSEQ"));
    addFieldDescription(new CharacterFieldDescription(txt50, "WHTEXT"));
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHFLDN"));
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHRLEN"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHFLDI"));
    // External field name
    addFieldDescription(new CharacterFieldDescription(txt10, "WHFLDE"));
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHFOBO"));
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHFIBO"));
    // Field length in bytes
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHFLDB"));
    // Number of digits
    addFieldDescription(new ZonedDecimalFieldDescription(z20, "WHFLDO"));
    // Number of decimal positions
    addFieldDescription(new ZonedDecimalFieldDescription(z20, "WHFLDP"));
    // Field text description
    addFieldDescription(new CharacterFieldDescription(txt50, "WHFTXT"));
    addFieldDescription(new ZonedDecimalFieldDescription(z30, "WHRCDE"));
    // Reference file
    addFieldDescription(new CharacterFieldDescription(txt10, "WHRFIL"));
    // Reference library
    addFieldDescription(new CharacterFieldDescription(txt10, "WHRLIB"));
    // Reference record format
    addFieldDescription(new CharacterFieldDescription(txt10, "WHRFMT"));
    // Reference field
    addFieldDescription(new CharacterFieldDescription(txt10, "WHRFLD"));
    // Column heading 1
    addFieldDescription(new CharacterFieldDescription(txt20, "WHCHD1"));
    // Column heading 2
    addFieldDescription(new CharacterFieldDescription(txt20, "WHCHD2"));
    // Column heading 3
    addFieldDescription(new CharacterFieldDescription(txt20, "WHCHD3"));
    // Field type
    addFieldDescription(new CharacterFieldDescription(txt1, "WHFLDT"));
    addFieldDescription(new CharacterFieldDescription(txt1, "WHFIOB"));
    addFieldDescription(new CharacterFieldDescription(txt2, "WHECDE"));
    addFieldDescription(new CharacterFieldDescription(txt32, "WHEWRD"));
    addFieldDescription(new ZonedDecimalFieldDescription(z40, "WHVCNE"));
    // Number of fields
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHNFLD"));
    addFieldDescription(new ZonedDecimalFieldDescription(z20, "WHNIND"));
    addFieldDescription(new CharacterFieldDescription(txt1, "WHSHFT"));
    addFieldDescription(new CharacterFieldDescription(txt1, "WHALTY"));
    // ALIAS
    addFieldDescription(new CharacterFieldDescription(txt30, "WHALIS"));
    addFieldDescription(new ZonedDecimalFieldDescription(z20, "WHJREF"));
    addFieldDescription(new ZonedDecimalFieldDescription(z20, "WHDFTL"));
    // Default value
    addFieldDescription(new CharacterFieldDescription(txt30, "WHDFT"));
    addFieldDescription(new CharacterFieldDescription(txt1, "WHCHRI"));
    addFieldDescription(new CharacterFieldDescription(txt1, "WHCTNT"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHFONT"));
    addFieldDescription(new ZonedDecimalFieldDescription(z31, "WHCSWD"));
    addFieldDescription(new ZonedDecimalFieldDescription(z31, "WHCSHI"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHBCNM"));
    addFieldDescription(new ZonedDecimalFieldDescription(z31, "WHBCHI"));
    addFieldDescription(new CharacterFieldDescription(txt1, "WHMAP"));
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHMAPS"));
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHMAPL"));
    addFieldDescription(new CharacterFieldDescription(txt8, "WHSYSN"));
    addFieldDescription(new CharacterFieldDescription(txt2, "WHRES1"));
    addFieldDescription(new CharacterFieldDescription(txt1, "WHSQLT"));
    addFieldDescription(new CharacterFieldDescription(txt1, "WHHEX"));
    addFieldDescription(new PackedDecimalFieldDescription(p41, "WHPNTS"));
    // CCSID
    addFieldDescription(new PackedDecimalFieldDescription(p50, "WHCCSID"));
    // Date/time format
    addFieldDescription(new CharacterFieldDescription(txt4, "WHFMT"));
    // Date/time separator
    addFieldDescription(new CharacterFieldDescription(txt1, "WHSEP"));
    // Variable length field
    addFieldDescription(new CharacterFieldDescription(txt1, "WHVARL"));
    addFieldDescription(new PackedDecimalFieldDescription(p50, "WHALLC"));
    // Allow null value
    addFieldDescription(new CharacterFieldDescription(txt1, "WHNULL"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHFCSN"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHFCSL"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHFCPN"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHFCPL"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHCDFN"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHCDFL"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHDCDF"));
    addFieldDescription(new CharacterFieldDescription(txt10, "WHDCDL"));
    addFieldDescription(new PackedDecimalFieldDescription(p30, "WHTXRT"));
    addFieldDescription(new ZonedDecimalFieldDescription(z50, "WHFLDG"));
  }
}
