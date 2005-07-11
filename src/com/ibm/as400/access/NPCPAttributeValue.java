///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPAttributeValue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Enumeration;

/**
  * NPCPAttributeValue class - class for an attribute value list code point used with
  * the network print server's data stream.
  * This class is derived from NPCodePoint and will be used to build a code
  * point that has as its data a list of attribute.  Each attribute consist of
  * its ID, its length, its type and an offset to its data.
  *
  * The layout of an Attribute value codepoint in memory is:
  *
  *       ------------------------------
  *       | LLLL | CPID |     data     |
  *       ------------------------------
  *       LLLL - four byte code point length
  *       CPID - code point ID (2 bytes)
  *       data - code point data as follows:
  *
  *       ----------------------------------------------------------------------------------ÝÝ
  *       |nn | LEN | ID1 | tt | llll | ofof | ...... | IDnn| tt | llll | ofof |  Ývalues  |ÝÝ
  *       ----------------------------------------------------------------------------------ÝÝ
  *
  *         nn   - two byte total # of attributes in code point
  *         LEN  - two byte length of each attribute entry, right
  *                now this will be 12 (0x0C).
  *         IDx  - two byte attribute ID
  *         tt   - two byte type of attribute
  *         llll - four byte length of attribute value
  *         ofof - four byte offset from beginning of code point to
  *                attribute value.
  *         values - list of values for attributes.
  *
  *  There are many codepoints that fall into this structure for their data.  This
  *   class - NPCPAttributeValue - is an Abstract base class for these other classes.
  *   This class contains all the logic to build the raw data from a list of NPAttributes
  *   and to build a list of NPAttribute from the raw data.
  *
*/

abstract class NPCPAttributeValue extends NPCodePoint
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


    // private data members
    private Hashtable attrTable_;
    private boolean   fDataOutOfDate_;  // is base codepoint raw data out of date?
    private boolean   fListOutOfDate_;  // is our list out of date?

    // package scope data members
    final static String emptyString ="";

    // public methods and members
    private final static int LEN_ATTR_HEADER = 4;   // size of header on this data
    private final static int LEN_ATTR_ENTRY  = 12;  // size of each entry

    NPCPAttributeValue(NPCPAttributeValue cp)
    {
       super(cp);                             // call parent's copy ctor
       fDataOutOfDate_ = cp.fDataOutOfDate_;
       fListOutOfDate_ = cp.fListOutOfDate_;
       attrTable_ = new Hashtable(41);
       if (fDataOutOfDate_)
       {
          int i;
          NPAttribute attr;
          // if the data is out of date, then the list must be up to date
          // so we must copy the list over attribute by attribute
          for (Enumeration list = cp.attrTable_.elements(); list.hasMoreElements();)
          {
             attr = (NPAttribute)list.nextElement();
             if (attr != null)
             {
                try
                {
                   attrTable_.put(new Integer(attr.getID()), attr.clone());
                }
                catch(java.lang.CloneNotSupportedException e )
                {
                   // should never happen unless a certain NPAttribute subclass
                   // didn't override the clone() method!  We will throw a runtime error here
                   Trace.log(Trace.ERROR, " NPCPAttribributeValue: Error cloning new attribute" + e);
                   throw new InternalErrorException(e.toString(), InternalErrorException.UNKNOWN);
                }
             }
          }
       } else {
          // the raw data is upto date we'll rebuild the list of attrs when we
          // need to
          fListOutOfDate_ = true;
       }
    }
   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    */
    NPCPAttributeValue(int ID)
    {
       super(ID);                            // construct codepoint with this ID
       fDataOutOfDate_ = false;
       fListOutOfDate_ = false;
       attrTable_ = new Hashtable(41);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    */
    NPCPAttributeValue( int ID, byte[] data )
    {
       super(ID, data);                     // construct codepoint with this ID
       fDataOutOfDate_ = false;
       fListOutOfDate_ = true;
       attrTable_ = new Hashtable(41);
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
    
    // @B2A - Added method below to be package scope only! (for use by PrintObject.java)
    boolean getfListOutOfDate()         // @B2A
    {                                   // @B2A
        return fListOutOfDate_;         // @B2A
    }                                   // @B2A
    

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
        zeroAttrTable();
        fListOutOfDate_ = false;
        fDataOutOfDate_ = false;
        super.reset();
    }

    /**
      * setAttrValue - add or change the attribute associated with this ID to this
      *                string value
      **/
    void setAttrValue(int attrID, String value)
    {
       if (!NPAttribute.idIsValid(attrID))
       {
          throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
             ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
       }

       if (fListOutOfDate_)
       {
          updateList();
       }

       // if the attribute ID is less than 0 this is an IFS type attribute
       // and must be broken down into its object name and library.
       if (attrID < 0)
       {
           NPAttributeIFS ifsAttr =
              NPAttributeIFS.ifsAttrs[java.lang.Math.abs(attrID) - 1];

           // if the string starts with a "*" that means it is a special
           // value and we should just set the name field to this
           if (value.startsWith("*"))
           {
               attrTable_.put(new Integer(ifsAttr.nameID_),
                              new NPAttrString(ifsAttr.nameID_, value));            // @B1C
           } else {

              QSYSObjectPathName ifsPath = null;
              // if the type static, check it in QSYSObjectPathName ctor
              if (ifsAttr.typeID_ != 0)
              {
                 ifsPath = new QSYSObjectPathName(value);
              } else {
                 ifsPath = new QSYSObjectPathName(value, ifsAttr.typeString_);
              }

              attrTable_.put(new Integer(ifsAttr.nameID_),
                             new NPAttrString(ifsAttr.nameID_,
                                              ifsPath.getObjectName())); // @B1C

              attrTable_.put(new Integer(ifsAttr.libraryID_),
                             new NPAttrString(ifsAttr.libraryID_,
                                              ifsPath.getLibraryName()));// @B1C

               // if the type is dynamic, set this also
               if (ifsAttr.typeID_ != 0)
               {
                   // convert the type from "OVL" to "*OVL" first
                   String strType = "*";
                   strType += ifsPath.getObjectType();
                   attrTable_.put(new Integer(ifsAttr.typeID_),
                                  new NPAttrString(ifsAttr.typeID_,
                                                   strType));            // @B1C
               }
           }
       } else {
          attrTable_.put(new Integer(attrID), new NPAttrString(attrID, value));  // @B1C
       }
       fDataOutOfDate_ = true;

    }

    /**
      * getStringValue - get the attribute value associated with this ID.
      *            Throws an exception if the attribute ID is not a
      *              valid attribute ID.
      *            Throws an execption if the attribute is not a String
      *              type attribute.
      *
      * @param  attrID attribute ID from 1 to NPAttribute.MAX_ATTR_ID  or one of
      *                the special negative attribute IDs for IFS Path attributes.
      * @return String reference if successful.  If this reference is null
      *           the attribute wasn't found in this code point.
      *             represents an attribute that is not a String type attribute.
      * @see NPObject class
      **/
    String getStringValue(int attrID)
    {
       boolean fRC = false;
       String  rcString = null;
       Object attr;

       if (!NPAttribute.idIsValid(attrID))
       {
          throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
             ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
       }

       if (fListOutOfDate_)
       {
          updateList();
       }

       // if the attribute ID is less than 0 then it is an IFS type
       // attribute ID and we must build the string up from its components
       if (attrID < 0)
       {
           String name = null, lib = null, type = null;
           // attribute is an IFS attribute, we must build the string here
           NPAttributeIFS ifsAttr =
                NPAttributeIFS.ifsAttrs[java.lang.Math.abs(attrID) - 1];


           // get the string for the object name
           // if this is null we'll end up returning null for the IFS path
           attr = attrTable_.get(new Integer(ifsAttr.nameID_));
           if (attr != null)
           {
              name = ((NPAttrString)attr).get();
           }

           // get the string for the object lib
           if (name != null)
           {/* check for default case when resource are *INLINE   @B5A@B6C */
            //  if (0 == name.compareTo("F1DFLT"))                 /* @B5A@B6C */
          //    {
            //    lib = "          "; /* Library will be blank in this case @B5A@B6D */
              //} else {                                           /* @B5A@B6D */
                 attr = attrTable_.get(new Integer(ifsAttr.libraryID_));
                 if (attr != null)
                {
                   lib  = ((NPAttrString)attr).get();
                   if ( 0 == lib.compareTo("")){
                     lib = "          ";
                   }
                }
             // }    /*  end Else            @B5A@B6D */
           }

           // get the string for the object type
           if (ifsAttr.typeID_ != 0)
           {
               attr = attrTable_.get(new Integer(ifsAttr.typeID_));
               if (attr != null)
               {
                  // check that attr is a String type
                  // WHAT to do when it is not?  Resource Type is an int!
                  type = ((NPAttrString)attr).get();
                  if ( (type != null) && type.startsWith("*"))
                  {
                    type = type.substring(1, type.length());// @B2A correct subs
                 // type = type.substring(1, type.length()-1); @B3D
                  }
               }
           } else {
               type = ifsAttr.typeString_;
           }

           // if the name is a special value (like *NONE or *FRONTOVL)
           // then don't return an IFS path name because the library
           // would be blank and the type might not make sense.
           if (name != null)
           {
               if (name.startsWith("*"))
               {
                   rcString = name;
               } else {
                  rcString = QSYSObjectPathName.toPath(lib, name, type);
               }
           }

       } else {

          attr = attrTable_.get(new Integer(attrID));
          if (attr != null)
          {
              // make sure this an NPAttrString class object
              if (attr instanceof NPAttrString)
              {
                 NPAttrString attrString = (NPAttrString)attr;
                 // rcString = new String(attrString.get());
                 rcString = attrString.get();
              } else {
                 throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
                    ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
              }
          }
       }
       return rcString;         // may be null
    }

    /**
      * setAttrValue - add or change the attribute associated with this ID to this
      *                int value
      **/
    void setAttrValue(int attrID, int value)
    {
       if (!NPAttribute.idIsValid(attrID))
       {
          throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
             ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
       }

       if (fListOutOfDate_)
       {
          updateList();
       }
       attrTable_.put(new Integer(attrID), new NPAttrBin4(attrID, value));
       fDataOutOfDate_ = true;
    }

    /**
      * getIntValue - get the attribute value associated with this ID.
      *            Throws an exception if the attribute ID is not a
      *             valid attribute ID.
      *            Throws an execption if the attribute is not a int
      *             type attribute.
      *
      * @param  attrID attribute ID from 1 to NPAttribute.MAX_ATTR_ID
      * @return Int reference if successful.  If this reference is null
      *           the attribute wasn't found in this code point.
      *             represents an attribute that is not a Integer type attribute.
      * @see NPObject class
      **/
    Integer getIntValue(int attrID)
    {
       boolean fRC = false;
       Integer   rcInt = null;
       Object attr;

       if (!NPAttribute.idIsValid(attrID))
       {
          throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
             ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
       }

       if (fListOutOfDate_)
       {
          updateList();
       }
       attr = attrTable_.get(new Integer(attrID));
       if (attr != null)
       {
          // make sure this an NPAttrBin4 class object
          if (attr instanceof NPAttrBin4)
          {
             NPAttrBin4 attrBin4= (NPAttrBin4)attr;
             rcInt = new Integer(attrBin4.get());
          } else {
             throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
          }
       }

       return rcInt;
    }

    /**
      * setAttrValue - add or change the attribute associated with this ID to this
      *                float value
     **/
    void setAttrValue(int attrID, float value)
    {
       if (!NPAttribute.idIsValid(attrID))
       {
          throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
             ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
       }

       if (fListOutOfDate_)
       {
          updateList();
       }
       attrTable_.put(new Integer(attrID), new NPAttrFloat(attrID, value));
       fDataOutOfDate_ = true;
    }


    /**
      * getFloatValue - get the attribute value associated with this ID.
      *            Throws an exception if the attribute ID is not a
      *             valid attribute ID.
      *            Throws an execption if the attribute is not a float
      *             type attribute.
      *
      * @param  attrID attribute ID from 1 to NPAttribute.MAX_ATTR_ID
      * @return Float reference if successful.  If this reference is null
      *           the attribute wasn't found in this code point.
      *             represents an attribute that is not a Float type attribute.
      * @see NPObject class
      **/
    Float getFloatValue(int attrID)
    {
       boolean fRC = false;
       Float   rcFloat = null;
       Object attr;

       if (!NPAttribute.idIsValid(attrID))
       {
          throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
             ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
       }

       if (fListOutOfDate_)
       {
          updateList();
       }
       attr = attrTable_.get(new Integer(attrID));
       if (attr != null)
       {
          // make sure this an NPAttrFloat class object
          if (attr instanceof NPAttrFloat)
          {
             NPAttrFloat attrFloat= (NPAttrFloat)attr;
             rcFloat = new Float(attrFloat.get());
          } else {
             throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
          }
       }
       return rcFloat;
    }

    /**
      * addUpdateAttributes - merges the passed in AttributeValue codepoint with this one
      *                   by adding any attributes that aren't already in this codepoint
      *                   and replacing any that are with the ones from the parameter.
     **/
     void addUpdateAttributes(NPCPAttributeValue cp)
     {
        if (fListOutOfDate_)
        {
           updateList();
        }
        if (cp.fListOutOfDate_)
        {
            cp.updateList();
        }
        NPAttribute attr;
        for (Enumeration e = cp.attrTable_.elements(); e.hasMoreElements(); )
        {
            attr = (NPAttribute)e.nextElement();
            if (attr != null)
            {
                attrTable_.put(new Integer(attr.getID()), attr);
            }
        }

        fDataOutOfDate_ = true;   /* @A1A */

     }

     /**
       * removeAttribute - removes the attribute with the specified ID from the code point.
       * Nothing happens if the attribute currently is not in the code point.
       * runtime exception is thrown if the attribute ID is not valid
       **/
     void removeAttribute(int attrID)
     {
         if (!NPAttribute.idIsValid(attrID))
         {
             throw(new ExtendedIllegalArgumentException(PrintObject.getAttributeName(attrID),
                 ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID));
         }
         if (fListOutOfDate_)
         {
             updateList();
         }
         // if the attribute ID is less than 0 then it is an IFS type
         // attribute ID and we must remove all parts of the IFS name (name, lib and maybe type)
         if (attrID < 0)
         {

             // attribute is an IFS attribute, we must build the string here
             NPAttributeIFS ifsAttr =
                  NPAttributeIFS.ifsAttrs[java.lang.Math.abs(attrID) - 1];


             // remove the string for the object name
             attrTable_.remove(new Integer(ifsAttr.nameID_));
             // remove the string for the object library
             attrTable_.remove(new Integer(ifsAttr.libraryID_));

             // get the string for the object type
             if (ifsAttr.typeID_ != 0)
             {
                 attrTable_.remove(new Integer(ifsAttr.typeID_));
             }
       } else {
           // just a simple attribute, remove it from the table
           attrTable_.remove(new Integer(attrID));
       }
     }


    // private methods
    private void updateData()
    {
       int dataLength, elements, dataOffset, valueOffset;
       byte[] data;
       NPAttribute attr;

       elements = 0;
       dataLength =  LEN_ATTR_HEADER;
       for (Enumeration e = attrTable_.elements(); e.hasMoreElements(); )
       {
          attr = (NPAttribute)e.nextElement();

          if (attr != null)
          {
             dataLength += LEN_ATTR_ENTRY;   // add the 12 bytes needed to describe this attr
             dataLength += attr.getHostLength(converter_);  // add bytes for host length     @B1C
             elements++;                          // bump number of found elements
          }
       }


       data = super.getDataBuffer(dataLength);   // get buffer for the raw data

       // set the number of attributes in the codepoint
       dataOffset = super.getOffset();
       BinaryConverter.unsignedShortToByteArray(elements, data, dataOffset);
       dataOffset += 2;

       // set the length (12) of each attribute header
       BinaryConverter.unsignedShortToByteArray(LEN_ATTR_ENTRY, data, dataOffset);
       dataOffset += 2;

       // initialize where the first value will be placed in the buffer (after all
       // of the attribute header entries)
       valueOffset = dataOffset + LEN_ATTR_ENTRY * elements;

       // add each attribute to the data
       for (Enumeration e = attrTable_.elements(); e.hasMoreElements() && (elements != 0);)
       {
          attr = (NPAttribute)e.nextElement();
          if (attr != null)
          {
             // set 2 byte attr ID
             BinaryConverter.unsignedShortToByteArray(attr.getID(), data, dataOffset);
             dataOffset += 2;

             // set 2 byte type
             BinaryConverter.unsignedShortToByteArray(attr.getType(), data, dataOffset);
             dataOffset += 2;

             // set 4 byte length
             BinaryConverter.intToByteArray(attr.getHostLength(converter_), data, dataOffset);       // @B1C
             dataOffset += 4;

             // set 4 byte value offset
             BinaryConverter.intToByteArray(valueOffset+NPCodePoint.LEN_HEADER, data, dataOffset);
             dataOffset += 4;

             // set actual value
             System.arraycopy(attr.getHostData(converter_), 0,    // source                          // @B1C
                              data, valueOffset,        // dest
                              attr.getHostLength(converter_));    // len                             // @B1C
             valueOffset += attr.getHostLength(converter_);                                          // @B1C

             elements--;
          }
       }
       fDataOutOfDate_ = false;
    }  // updateData()

    private void updateList()
    {
       byte[] data;
       int i;
       byte nullbyte = (byte)'\0';
      
       NPAttribute attr = null;
       
       // zero out table and the rebuild based on data
       zeroAttrTable();
       data = super.getDataBuffer();
       if ( (data != null) && (data.length >= LEN_ATTR_HEADER) )
       {
          long dataLength;
          int  elements, offset;
          // @B1D Converter converterObj;
          // @B1D try
          // @B1D {
          // @B1D   // @A2C changed below from g.getConverter(hostCCSID_);
          // @B1D    converterObj = new Converter(hostCCSID_);
          // @B1D }
          // @B1D catch (java.io.UnsupportedEncodingException e)
          // @B1D {
          // @B1D     Trace.log(Trace.ERROR,
          // @B1D             " UnsupportedEncodingException for ccsid = " + hostCCSID_ +
          // @B1D             ".  Exception text = " + e);
          // @B1D     // @A2C changed below from Converter.getConverter()
          // @B1D     converterObj = new Converter();
          // @B1D }
          dataLength = (long)super.getDataLength();
          offset = super.getOffset();
          if (dataLength > LEN_ATTR_HEADER)
          {
             elements = BinaryConverter.byteArrayToShort(data, offset);
             offset += 2;
             if (elements != 0)
             {
                int size;
                size = BinaryConverter.byteArrayToShort(data, offset);
                offset += 2;
                if ( (size >= LEN_ATTR_ENTRY) && (dataLength >= (offset+elements*size)) )
                {
                   int ID, type, length, valueOffset;
                   while (elements != 0)
                   {
                      ID = BinaryConverter.byteArrayToShort(data, offset);
                      offset += 2;

                      type = BinaryConverter.byteArrayToShort(data, offset);
                      offset += 2;

                      length = BinaryConverter.byteArrayToInt(data, offset);
                      offset += 4;

                      // valueOffset is from the beginning of the code point
                      // since we only have the codepoint data here, we have
                      // to adjust it
                      valueOffset = BinaryConverter.byteArrayToInt(data, offset);
                      offset += 4;
                      valueOffset -= NPCodePoint.LEN_HEADER;

                      switch (type)
                      {
                         case NPAttribute.FOUR_BYTE:
                         case NPAttribute.FOUR_BYTE_ENU:
                            attr = new NPAttrBin4(ID, data, valueOffset,
                                                   length);
                            break;
                         case NPAttribute.STRING:
                         case NPAttribute.STRING_ENU:
                            attr = new NPAttrString(ID, data, valueOffset,
                                                     length, converter_);                    // @B1C
                            break;
                         case NPAttribute.FLOAT:
                            attr = new NPAttrFloat(ID, data, valueOffset,
                                                    length);
                            break;
                         case NPAttribute.LISTSTRING:                                        // @B4A
                            // lists of strings will contain single null per field
                            // and double null to signify list end                              @B4A
                            
                            for ( i = 0; i < length; i++){                                   // @B4A
                                if ((data[i + valueOffset] == nullbyte)&& (i < length -1)){
                                     if (data[i + valueOffset + 1] != nullbyte){
                                        data[i + valueOffset] = (byte)'\u007A';
                            // uses ':' as a delimiter between fields            @B4A                                                                               
                                     }
                                 }
                                 }
                            attr = new NPAttrString(ID, data, valueOffset,
                                                    length, converter_);
                            break;
                         default:
                            // unknown attribute type - could be new?
                            // System.out.println(" unknown type = " + type);
                      }
                      attrTable_.put(new Integer(ID), attr);

                      elements--;
                      offset += size - LEN_ATTR_ENTRY;  // if the attribute entry was bigger than we know it
                   }
                }
             }
          }
       }
       fListOutOfDate_ = false;
    }  // updateList()

    private void zeroAttrTable()
    {

       attrTable_.clear();
    }

    
}
