///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPAttrBin4.java
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
  **/

class NPAttrBin4 extends NPAttribute implements Cloneable,
                                                java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;



    private int attrValue_;    // stored 4 byte value in PC terms

    NPAttrBin4(NPAttrBin4 attr)
    {
       super(attr);
       attrValue_ = attr.attrValue_;
    }

    NPAttrBin4(int ID)
    {
       super(ID, FOUR_BYTE);
    }

    NPAttrBin4(int ID, int value)
    {
       super(ID, FOUR_BYTE);
       set(value);
    }

    NPAttrBin4(int ID,
               byte[] hostDataStream,
               int offset,
               int length)
     {
       super(ID, FOUR_BYTE, hostDataStream, offset, length, null);
       attrValue_ = BinaryConverter.byteArrayToInt(getHostData(null), 0); // @B1C
    }

    protected Object  clone()
    {
       NPAttrBin4 attr;
       attr = new NPAttrBin4(this);
       return attr;
    }

    int get()
    {
       return attrValue_;
    }

    
    void set(int value)
    {
       byte[] hostValue = new byte[4];
       attrValue_ = value;
       BinaryConverter.intToByteArray(attrValue_, hostValue, 0);  // move int into byte array
       super.setHostData(hostValue, null); // @B1C
    }

    void setHostData(byte[] data, ConverterImpl converter) // @B1C
    {
       super.setHostData(data, converter);                                      // @B1C
       attrValue_ = BinaryConverter.byteArrayToInt(getHostData(converter), 0);  // @B1C
    }

}  // end of class NPAttrBin4


