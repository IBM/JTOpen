///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPAttributeIDList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPAttributeIDList class - class for an attribute ID list code point used with
  * the network print server's data stream.
  * This class is derived from NPCodePoint and will be used to build a code
  * point that has as its data a list of attribute IDs.  Each ID is a 2 byte
  * value defined by the network print server.
  *
  * The layout of an Attribute List codepoint in memory is:
  *
  *       ------------------------------
  *       | LLLL | CPID |     data     |
  *       ------------------------------
  *       LLLL - four byte code point length
  *       CPID - code point ID (2 bytes)
  *       data - code point data as follows:
  *       ---------------------------------------------------
  *       |nn | LEN | ID1 | ID2 | ID3 | ID4 | ....... | IDnn|
  *       ---------------------------------------------------
  *        nn   - two byte total # of attributes in code point
  *        LEN  - two byte length of each attribute entry, right
  *               now this will be 2 (0x02).
  *        IDx  - two byte attribute ID
  *
  *      The base code point class takes care of the first 6 bytes (LLLL and CPID)
  *      and we handle the data part of it
  *
*/
class NPCPAttributeIDList extends NPCodePoint implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


    private final static int LEN_ATTRLIST_HEADER = 4;  // size of header on this data

    private byte[]    idList_;
    private boolean   fDataOutOfDate_;  // is base codepoint raw data out of date?
    private boolean   fListOutOfDate_;  // is our vector out of date?


    protected Object clone()
    {
       NPCPAttributeIDList cp = new NPCPAttributeIDList(this);
       return cp;
    }

   /**
    * copy constructor
    */
    NPCPAttributeIDList(NPCPAttributeIDList cp)
    {
       super(cp);
       fDataOutOfDate_ = cp.fDataOutOfDate_;
       fListOutOfDate_ = cp.fListOutOfDate_;
       idList_ = new byte[PrintObject.MAX_ATTR_ID + 1];
       System.arraycopy(cp.idList_, 0,
                        idList_, 0,
                        idList_.length);
    }

    public NPCPAttributeIDList()
    {
       super(NPCodePoint.ATTRIBUTE_LIST);     // construct codepoint with attribute list ID
       fDataOutOfDate_ = false;
       fListOutOfDate_ = false;
       idList_ = new byte[PrintObject.MAX_ATTR_ID + 1];
    }

    NPCPAttributeIDList(byte[] data )
    {
        super(NPCodePoint.ATTRIBUTE_LIST, data);     // construct codepoint with attribute list ID
        fDataOutOfDate_ = false;
        fListOutOfDate_ = true;
        idList_ = new byte[PrintObject.MAX_ATTR_ID + 1];
    }


    // override getLength from NPCodePoint class
    // returns total length of code point (data and header)
    int getLength()
    {
       if (fDataOutOfDate_)
       {
          updateData();
       }
       return super.getLength();
    }

    void setDataBuffer( byte[] dataBuffer, int datalen, int offset)
    {
        fListOutOfDate_ = true;
        fDataOutOfDate_ = false;
        super.setDataBuffer(dataBuffer, datalen, offset);
    }

    // get current data buffer
    byte[] getDataBuffer()
    {
        if (fDataOutOfDate_)
        {
           updateData();
        }
        return super.getDataBuffer();
    }

    // get current data buffer and make it big enough to handle this many bytes
    byte[] getDataBuffer(int dataLength)
    {
        if (fDataOutOfDate_)
        {
           updateData();
        }
        fListOutOfDate_ = true;
        return super.getDataBuffer(dataLength);
    }

    // override reset() method to wipe out our data
    void reset()
    {
        zeroIDList();
        fListOutOfDate_ = false;
        fDataOutOfDate_ = false;
        super.reset();
    }



    /**
      * addAttrID will add the specified Attribute ID to the list if it's not
      * there already (if it is there - no harm done).
     **/
    void addAttrID(int ID)
    {
       if (!NPAttribute.idIsValid(ID))
       {
          // throw some sorta programmer error exception here
          throw(new ExtendedIllegalArgumentException("ID("+ID+")",
						     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
       } else {

          if (fListOutOfDate_)
          {
             updateList();
          }
          if (ID < 0)
          {
             ID = java.lang.Math.abs(ID) - 1;
             NPAttributeIFS ifsAttr = NPAttributeIFS.ifsAttrs[ID];
             idList_[ifsAttr.nameID_] = (byte)1;
             idList_[ifsAttr.libraryID_] = (byte)1;
             if (ifsAttr.typeID_ != 0)
             {
                 idList_[ifsAttr.typeID_] = (byte)1;
             }

          } else {
             idList_[ID] = (byte)1;
          }
          fDataOutOfDate_ = true;
       }
    }

    /**
     * checks if an ID is contained in the codepoint
     * @return true if this ID is in the list
     **/
    boolean containsID(int ID)
    {
        boolean fRC = false;
        if (NPAttribute.idIsValid(ID))
        {
           if (fListOutOfDate_)
           {
               updateList();
           }
           if (ID < 0)
           {
               ID = java.lang.Math.abs(ID) - 1;
               NPAttributeIFS ifsAttr = NPAttributeIFS.ifsAttrs[ID];
               if ( (idList_[ifsAttr.nameID_] != 0) &&
                    (idList_[ifsAttr.libraryID_] != 0))
               {
                   if (ifsAttr.typeID_ != 0)
                   {
                       if (idList_[ifsAttr.typeID_] != 0)
                       {
                          fRC = true;
                       }
                   } else {
                      fRC = true;
                   }
               }
           } else {
              if (idList_[ID] != 0)
              {
                  fRC = true;
              }
           }
        }
        return fRC;
    }


    // private data members & methods
    private void updateData()
    {
       int dataLength, index, elements, dataOffset;
       byte[] data;

       elements = 0;
       dataLength =  LEN_ATTRLIST_HEADER;
       for (index = 0; index < idList_.length; index++)
       {
          if (idList_[index] != 0)
          {
             elements++;
          }
       }
       dataLength += 2 * elements;
       data = super.getDataBuffer(dataLength);

       // set the number of IDs in the codepoint
       dataOffset = super.getOffset();
       BinaryConverter.unsignedShortToByteArray(elements, data, dataOffset);
       dataOffset += 2;

       // set the length (2) of each ID
       BinaryConverter.unsignedShortToByteArray(2, data, dataOffset);
       dataOffset += 2;

       // add each ID to the data
       for (index = 0; (index < idList_.length) && (elements != 0); index++)
       {
          if (idList_[index] != 0)
          {
             BinaryConverter.unsignedShortToByteArray(index, data, dataOffset);
             dataOffset += 2;
             elements--;
          }
       }
       fDataOutOfDate_ = false;
    }  // updateData()

    private void updateList()
    {
       byte[] data;
       // zero out array and the rebuild based on data
       zeroIDList();
       data = super.getDataBuffer();
       if ( (data != null) && (data.length >= LEN_ATTRLIST_HEADER) )
       {
          long dataLength;
          int  elements, offset;
          dataLength = (long)super.getDataLength();
          offset = super.getOffset();
          if (dataLength > LEN_ATTRLIST_HEADER)
          {
             elements = BinaryConverter.byteArrayToUnsignedShort(data, offset);
             offset += 2;
             if (elements != 0)
             {
                int size;
                size = BinaryConverter.byteArrayToUnsignedShort(data, offset);
                offset += 2;
                if ( (size == 2) && (dataLength >= (offset+elements*2)) )
                {
                   int ID;
                   while (elements != 0)
                   {
                      ID = BinaryConverter.byteArrayToUnsignedShort(data, offset);
                      offset += 2;
                      elements--;
                      if ( (ID <0) || (ID >= idList_.length) )
                      {
                          // ??? throw some sorta programmer error exception here
                      } else {
                         idList_[ID] = (byte)1;
                      }

                   }
                }
             }
          }
       }
       fListOutOfDate_ = false;
    }

    private void zeroIDList()
    {
       int length, index;
       length = idList_.length;
       for (index=0; index < length; index++)
       {
          idList_[index] = 0;
       }
    }

    // add copyright
    static private String getCopyright()
    {
        return Copyright.copyright;
    }


}
