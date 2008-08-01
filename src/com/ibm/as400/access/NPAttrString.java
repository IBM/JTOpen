///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPAttrString.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * For a list of valid attribute IDs, see the NPObject class.
  *
  **/

class NPAttrString extends NPAttribute implements Cloneable,
                                                  java.io.Serializable  
{
    static final long serialVersionUID = 4L;

    private String attrValue_;    // stored string byte value in PC terms

    NPAttrString(NPAttrString attr)
    {
       super(attr);
       attrValue_ = attr.attrValue_;
    }

    NPAttrString(int ID)
    {
       super(ID, STRING);
    }

    /**
      * construct a new NPAttrString with the specified ID and value.
      * the value will be copied (you can modify it later and it won't
      * affect this attribute's value
      **/
    NPAttrString(int ID, String value)
    {
       super(ID, STRING);
       set(value);
    }

    NPAttrString(int ID,
                   byte[] hostDataStream,
                   int offset,
                   int length,
                   ConverterImpl hostConverter)                                                 // @B1C
    {
       super(ID, STRING, hostDataStream, offset, length, hostConverter);
       buildStringFromHostData(hostConverter);                                                  // @B1C
    }

    private void buildHostString(ConverterImpl converter)                                       // @B1C
    {
       byte[] hostValue;
       hostValue = new byte[attrValue_.length() + 1];   // EBCDIC string must be null-terminated
       // @B1D // ??? what should we do if we get a character conversion exception here?
       // @B1D try
       // @B1D {
       byte[] temp = converter.stringToByteArray(attrValue_);   // move string into byte array // @B1C
       System.arraycopy(temp, 0, hostValue, 0, hostValue.length-1);                            // @B1C
       // @B1D }
       // @B1D catch (java.io.CharConversionException e)
       // @B1D {
       // @B1D     Trace.log(Trace.ERROR,
       // @B1D                " CharConversionException translating " + attrValue_ +
       // @B1D                ".  Exception text = " + e);
       // @B1D }
       hostValue[hostValue.length-1] = 0;              // null terminate
       super.setHostData(hostValue, converter);
    }

    private void buildStringFromHostData(ConverterImpl converter)                               // @B1C
    {
       byte[] hostValue = super.getHostData(converter);                                         // @B1C
       if ((hostValue == null) || (hostValue.length == 0))
       {
          attrValue_ = "";
       } else {
          attrValue_ = converter.byteArrayToString(hostValue, 0, hostValue.length-1);           // @B1C
          attrValue_ = attrValue_.trim();
       }

    }

    protected Object  clone()
    {
       NPAttrString attr;
       attr = new NPAttrString(this);
       return attr;
    }

    String get()
    {
       return attrValue_;
    }


    
    byte[] getHostData(ConverterImpl converter)                                             // @B1C
    {
       buildHostString(converter);                // update host data with our string       // @B1C
       return super.getHostData(converter);                                                 // @B1C
    }

    int getHostLength(ConverterImpl converter)                                              // @B1C
    {
       buildHostString(converter);                // update host data with our string       // @B1C
       return super.getHostLength(converter);                                               // @B1C
    }

    void set(String value)
    {
       attrValue_ = value;
       // don't set host value in super yet - wait until we need to
       // so that it will be built with the correct conversion object

       /////////////////////////////////////////////////
       // We must upper case object names, but we can't
       //  uppercase everything (USERCMT, for example,
       //   shouldn't be uppercased).
       // upper case any names that don't start with a "
       // If the string has any length to it at all
       //   and if the first character isn't a "
       //   THEN
       //     uppercase it
       /////////////////////////////////////////////////
       if ( (getID() != PrintObject.ATTR_USERCMT)   &&
            (getID() != PrintObject.ATTR_USERDATA)  &&
            (getID() != PrintObject.ATTR_RMTPRTQ)   &&
            (getID() != PrintObject.ATTR_IPP_ATTR_NL)   &&       //@A1A
            (getID() != PrintObject.ATTR_IPP_PRINTER_NAME)   &&  //@A1A
            (getID() != PrintObject.ATTR_IPP_JOB_NAME)   &&      //@A1A
            (getID() != PrintObject.ATTR_IPP_JOB_NAME_NL)   &&   //@A1A
            (getID() != PrintObject.ATTR_IPP_JOB_ORIGUSER)   &&  //@A1A
            (getID() != PrintObject.ATTR_IPP_JOB_ORIGUSER_NL)  &&//@A1A
            (getID() != PrintObject.ATTR_RMTSYSTEM)  &&          //@A2C
            (getID() != PrintObject.ATTR_FORMTYPE)   &&          //@A2A
            (getID() != PrintObject.ATTR_USRDEFDATA) &&          //@A2A@A3C
            (getID() != PrintObject.ATTR_USRDEFOPT)  &&          //@A3C
            (getID() != PrintObject.ATTR_DESCRIPTION)
            )
           {
           if (attrValue_.length() != 0)
               {
               if (!attrValue_.startsWith("\""))
                   {
                   attrValue_ = value.toUpperCase();

                   }
               }
           }
    }

    void setHostData(byte[] data, ConverterImpl converter)                          // @B1C
    {
       super.setHostData(data, converter);                                          // @B1C
       buildStringFromHostData(converter);                                          // @B1C
    }


}  // end of class NPAttrString
