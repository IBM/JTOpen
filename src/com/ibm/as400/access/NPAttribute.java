///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPAttribute.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPAttribute class - abstract base class for the various type of network print
  * attributes. For a list of valid attribute IDs, see the NPObject class.
  **/

abstract class NPAttribute extends Object implements Cloneable,
                                                     java.io.Serializable 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


    /* attribute types                                                  */
    /* These attribute types are known with these numeric IDs by the    */
    /* netprint server - DO NOT CHANGE the values of these IDs          */
    static final int  TWO_BYTE      = 0x0001;
    static final int  TWO_BYTE_ENU  = 0x0002;
    static final int  FOUR_BYTE     = 0x0003;
    static final int  FOUR_BYTE_ENU = 0x0004;
    static final int  STRING        = 0x0005;
    static final int  STRING_ENU    = 0x0006;
    static final int  FLOAT         = 0x0007;   // floating point
    static final int  PACKED_DEC    = 0x0007;   // packed decimal (15,5)
    static final int  BINARY        = 0x0008;   // @ACA missing attribs
    static final int  LISTSTRING    = 0x0009;   // @ACA array of strings

    // @A1C changed below from Converter.getConverter()
    // @B1D static  Converter  defaultConverter = new Converter(); 

    /* private members                                                  */
    private int    attrID_;               // attribute ID, defaults to 0
    private int    attrType_;             // attribute type, defaults to 0
    // @B1D Converter      converterObj_;         // ref to converter, defaults null
    private byte[] hostData_;             // host data array

    /**
     * default constructor is private and never used - all attributes must
     * have their ID set at least
     **/
    private NPAttribute()
    {
    }

    /**
     * copy constructor
     **/
    NPAttribute(NPAttribute attr)
    {
        attrID_ = attr.attrID_;
        attrType_ = attr.attrType_;
        // @B1D converterObj_ = attr.converterObj_;
        if (attr.hostData_ != null)
        {
           hostData_ = new byte[attr.hostData_.length];
           System.arraycopy(attr.hostData_, 0,
                            hostData_, 0,
                            hostData_.length);
        }

    }

    NPAttribute( int ID, int type)
    {
        attrID_ = ID;
        setType(type);
        // @B1D converterObj_ = defaultConverter;
    }

    NPAttribute(int ID, int type,
                       byte[] hostDataStream,
                       int offset,
                       int length,
                       ConverterImpl hostConverter)                             // @B1C
    {
       attrID_ = ID;
       setType(type);

       // @B1D converterObj_ = hostConverter;
       hostData_ = new byte[length];
       System.arraycopy(hostDataStream, offset,
                        hostData_, 0,
                        length);

    }



    // protected members & methods

    /**
     * clone method - must be implemented in subclass
     **/
    protected Object  clone() throws CloneNotSupportedException
    {
	Trace.log(Trace.ERROR, "Clone method must be implemented in " + (getClass()).getName());
	throw new CloneNotSupportedException((getClass()).getName());
    }

    //  public members & methods



    

    byte[] getHostData(ConverterImpl converter) // @B1C
    {
       return hostData_;
    }
    int getHostLength(ConverterImpl converter) // @B1C
    {
       int i = 0;
       if (hostData_ != null)
       {
          i = hostData_.length;
       }
       return(i);
    }

    int  getID()
    {
       return attrID_;
    }

    int getType()
    {
       return attrType_;
    }

    /**
     * method hashCode overrides the hashCode method that comes in the Object class
     * to return the attribute ID as the hashCode.  This is done so that the
     * NPAttributeValue class can use a hashTable to store the attributes in.
     * @see Object
     * @see NPAttributeValue
     * @see Hashtable
     **/
    public  int hashCode()
    {
       return attrID_;                     // our ID is our hash value
    }

    /**
      * returns true if the ID is valid; false otherwise;
      **/
    static boolean idIsValid(int ID)
    {
       boolean fRC = true;
       if ((ID == 0) || (ID > PrintObject.MAX_ATTR_ID))
       {
           fRC = false;
       } else {
           if (ID < 0)
           {
               ID = java.lang.Math.abs(ID) - 1;
               if (ID >= NPAttributeIFS.ifsAttrs.length)
               {
                   fRC = false;
               }
           }
       }
       return fRC;
    }


    // @B1D void setConverter(Converter newConverter )
    // @B1D {
    // @B1D     converterObj_ = newConverter;
    // @B1D }

    void setHostData(byte[] data, ConverterImpl converter)  // @B1C
    {
       // make this a deep copy in case the caller wants to
       // modify or reuse the param passed in later
       hostData_ = new byte[data.length];
       System.arraycopy(data, 0,
                        hostData_, 0,
                        data.length);
    }

    void setType(int newType)
    {
       attrType_ = newType;
    }




}  // end of class NPAttribute

