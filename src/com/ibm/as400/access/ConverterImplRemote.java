///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConverterImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;


class ConverterImplRemote implements ConverterImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


//@E3D  private static Hashtable pool_ = new Hashtable();  // Keep a pool of Converter Objects

  ConvTable table_;

  
  ConverterImplRemote()
  {
  }

  
  ConverterImplRemote(ConvTable table)
  {
    table_ = table;
  }

  
  String byteArrayToString(byte[] source)
  {                                                                           
    return byteArrayToString(source, 0, source.length, getStringType(getCcsid()));                    //$E0C  $E2C
  }

  
  String byteArrayToString(byte[] source, int offset)
  {
    return byteArrayToString(source, offset, source.length-offset, getStringType(getCcsid()));        //$E0C  $E2C
  }

  
  public String byteArrayToString(byte[] source, int offset, int length)
  {
    return byteArrayToString(source, offset, length, getStringType(getCcsid()));                      //$E0C  $E2C
  }                                                                     


  public String byteArrayToString(byte[] source, int offset, int length, int type)  //$E0A
  {
    return table_.byteArrayToString(source, offset, length, type);
  }

  
  static ConverterImplRemote getConverter(int ccsid, AS400ImplRemote system) throws UnsupportedEncodingException
  {
    //@E1D String ccsidstr = String.valueOf(ccsid);
//@E3D    Integer ccsidint = new Integer(ccsid); //@E1A
//@E3D    ConvTable table = (ConvTable)pool_.get(ccsidint);  // look in pool for object @E1C
    ConvTable table = ConvTable.getTable(ccsid, system); //@E3A
//@E3D    if(table != null)  // if object in pool
//@E3D    {
    return new ConverterImplRemote(table);
//@E3D    }
//@E3D    table = ConvTable.getTable(ccsid, system);  // else construct new object
//@E3D    pool_.put(ccsidint, table); // and put it in pool @E1C
//@E3D    return new ConverterImplRemote(table);
  }

  
  public String getEncoding()
  {
    return table_.getEncoding();
  }

  
  public int getCcsid()
  {
    return table_.getCcsid();
  }

  // Returns the string type of the text based on the ccsid.  If the text is bidi, then the string type
  // will be set accordingly.  If not, the default it LTR (left-to-right) text.
  // @return the strng type.
  int getStringType(int ccsid)        //$E1A
  {
     if (AS400BidiTransform.isBidiCcsid(ccsid))
        return AS400BidiTransform.getStringType((char)ccsid);
     else
        return BidiStringType.DEFAULT;
  }

  
  public void setCcsid(int ccsid, AS400Impl system) throws UnsupportedEncodingException
  {
    //@E1D String ccsidstr = String.valueOf(ccsid);
//@E3D    Integer ccsidint = new Integer(ccsid); //@E1A
//@E3D    table_ = (ConvTable)pool_.get(ccsidint);  // look in pool for object @E1C
    table_ = ConvTable.getTable(ccsid, null); //@E3A
//@E3D    if(table_ == null)  // if object not in pool @E1C
//@E3D    {
//@E3D      table_ = ConvTable.getTable(ccsid, (AS400ImplRemote)system);  // construct new object
//@E3D      pool_.put(ccsidint, table_); // and put it in pool @E1C
//@E3D    }
  }

  
  public void setEncoding(String encoding) throws UnsupportedEncodingException
  {
//@E3D    String ccsidstr = ConversionMaps.encodingToCcidString(encoding); //@B0C
//@E3D    if(ccsidstr == null)
//@E3D    {
//@E3D      table_ = ConvTable.getTable(encoding);  // try, in case this is a new encoding we don't know about yet, otherwise this line will throw the UnsupportedEncodingException
//@E3D      return;
//@E3D    }
//@E3D    Integer ccsidint = new Integer(ccsidstr); //@E1A
//@E3D    table_ = (ConvTable)pool_.get(ccsidint);  // look in pool for object @E1C
    table_ = ConvTable.getTable(encoding); //@E3A
//@E3D    if(table_ == null)  // if object not in pool
//@E3D    {
//@E3D      table_ = ConvTable.getTable(encoding);  // else construct new object
//@E3D      pool_.put(ccsidint, table_); // and put it in pool @E1C
//@E3D    }
  }

  
  public byte[] stringToByteArray(String source)
  {
    return stringToByteArray(source, getStringType(getCcsid()));                      //$E0C  $E2C
  }


  public byte[] stringToByteArray(String source, int type)   //$E0A
  {
    return table_.stringToByteArray(source, type);
  }

  
  void stringToByteArray(String source, byte[] destination) throws CharConversionException
  {
    byte[] convertedBytes = stringToByteArray(source, getStringType(getCcsid()));     //$E0C  $E2C
    if(convertedBytes.length > destination.length)
    {
      // Copy as much as will fit
      System.arraycopy(convertedBytes, 0, destination, 0, destination.length);
      if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@B0A
      {
        Trace.log(Trace.CONVERSION, "Destination not large enough during conversion: "+source.length()+","+destination.length); //@B0A
      }  
      throw new CharConversionException();
    }
    System.arraycopy(convertedBytes, 0, destination, 0, convertedBytes.length);
  }

  
  void stringToByteArray(String source, byte[] destination, int offset) throws CharConversionException
  {
    byte[] convertedBytes = stringToByteArray(source, getStringType(getCcsid()));      //$E0C  $E2C
    if(convertedBytes.length > destination.length - offset)
    {
      // Copy as much as will fit
      System.arraycopy(convertedBytes, 0, destination, offset, destination.length - offset);
      if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@B0A
      {
        Trace.log(Trace.CONVERSION, "Destination not large enough during conversion: "+source.length()+","+destination.length+","+offset); //@B0A
      }  
      throw new CharConversionException();
    }
    System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
  }

  
  /**
   * Converts the specified String into bytes.
   * @param  source  the String to convert.
   * @param  destination  the destination byte array.
   * @param  offset  the offset into the destination array for the start of the data.
   * @param  length  the length of data to write into the array.
   * @exception  CharConversionException  If destination is not large enough to hold the converted string.
  **/
  void stringToByteArray(String source, byte[] destination, int offset, int length) throws CharConversionException
  {
     stringToByteArray(source, destination, offset, length, getStringType(getCcsid()));       //@E2C
    
  }


  //@E2A
  /**
   * Converts the specified String into bytes.
   * @param  source  the String to convert.
   * @param  destination  the destination byte array.
   * @param  offset  the offset into the destination array for the start of the data.
   * @param  length  the length of data to write into the array.
   * @param  type    the bidi string type.
   * @exception  CharConversionException  If destination is not large enough to hold the converted string.
  **/
  void stringToByteArray(String source, byte[] destination, int offset, int length, int type) throws CharConversionException
  {
    byte[] convertedBytes = stringToByteArray(source, type);      //$E0C  $E2C
    if(convertedBytes.length > length)
    {
      // Copy as much as will fit
      System.arraycopy(convertedBytes, 0, destination, offset, length);
      if (Trace.isTraceOn() && Trace.isTraceConversionOn()) //@B0A
      {
        Trace.log(Trace.CONVERSION, "Destination not large enough during conversion: "+source.length()+","+destination.length+","+offset+","+length); //@B0A
      }  
      throw new CharConversionException();
    }
    System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
  }
}
