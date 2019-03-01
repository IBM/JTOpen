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
                   ConverterImpl hostConverter)                                                 
    {
       super(ID, STRING, hostDataStream, offset, length, hostConverter);
       buildStringFromHostData(hostConverter);                                                  
    }

    private void buildHostString(ConverterImpl converter)                                       
    {
       byte[] hostValue;
       hostValue = new byte[attrValue_.length() + 1];   // EBCDIC string must be null-terminated
       byte[] temp = converter.stringToByteArray(attrValue_);   // move string into byte array 
       System.arraycopy(temp, 0, hostValue, 0, hostValue.length-1);                            
       hostValue[hostValue.length-1] = 0;              // null terminate
       super.setHostData(hostValue, converter);
    }

    private void buildStringFromHostData(ConverterImpl converter)                               
    {
       byte[] hostValue = super.getHostData(converter);                                         
       if ((hostValue == null) || (hostValue.length == 0))
       {
          attrValue_ = "";
       } else {
          attrValue_ = converter.byteArrayToString(hostValue, 0, hostValue.length-1);           
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


    
    byte[] getHostData(ConverterImpl converter)                                             
    {
       buildHostString(converter);                // update host data with our string       
       return super.getHostData(converter);                                                 
    }

    int getHostLength(ConverterImpl converter)                                              
    {
       buildHostString(converter);                // update host data with our string       
       return super.getHostLength(converter);                                               
    }

    void set(String value)
    {
      attrValue_ = value;

       /////////////////////////////////////////////////
       // We must upper case object names, but we can't
       //  uppercase everything. The specified attrs 
       //  below shoudn't be uppercased 
       /////////////////////////////////////////////////
       if ( (getID() != PrintObject.ATTR_USERCMT)   &&
            (getID() != PrintObject.ATTR_USERDATA)  &&
            (getID() != PrintObject.ATTR_RMTPRTQ)   &&
            (getID() != PrintObject.ATTR_IPP_ATTR_NL)   &&       
            (getID() != PrintObject.ATTR_IPP_PRINTER_NAME)   &&  
            (getID() != PrintObject.ATTR_IPP_JOB_NAME)   &&      
            (getID() != PrintObject.ATTR_IPP_JOB_NAME_NL)   &&   
            (getID() != PrintObject.ATTR_IPP_JOB_ORIGUSER)   &&  
            (getID() != PrintObject.ATTR_IPP_JOB_ORIGUSER_NL)  &&
            (getID() != PrintObject.ATTR_RMTSYSTEM)  &&          
            (getID() != PrintObject.ATTR_FORMTYPE)   &&          
            (getID() != PrintObject.ATTR_USRDEFDATA) &&          
            (getID() != PrintObject.ATTR_USRDEFOPT)  &&          
            (getID() != PrintObject.ATTR_DESCRIPTION)
            )
       {
         /////////////////////////////////////////////////
         // If the string is not zero length,
         // upper case names that don't start with a
         // quotation mark
         /////////////////////////////////////////////////
         if (value.length() != 0)
         {
           if (!value.startsWith("\""))
           {
             // only uppercase characters which are lower case alphabetic characters
             String sAlphabet = "abcdefghijklmnopqrstuvwxyz";
             StringBuffer sbOutput = new StringBuffer();
             
             for(int i = 0;i<value.length();i++)
             {
               if (sAlphabet.lastIndexOf(value.substring(i,i+1))== -1)
               {
                 // not found, don't uppercase it
                 // just add it to the buffer
                 sbOutput.append(value.charAt(i));
               }
               else
               {
                 sbOutput.append(Character.toUpperCase(value.charAt(i)));
               }
             }
             attrValue_ = sbOutput.toString();
           }
         }
       }
    }

    void setHostData(byte[] data, ConverterImpl converter)                          
    {
       super.setHostData(data, converter);                                          
       buildStringFromHostData(converter);                                          
    }


}  // end of class NPAttrString
