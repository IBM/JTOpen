///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: UserInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/** 
 * The UserInfo class stores information that can be retrieved from AS400
 * by calling QSYRUSRI.PGM in certain format.
 *
**/

class UserInfo extends Object
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

     private int fieldIndex_=0;
     private String fieldDescription_="";
     private int valueType_=0;
     private int valueLength_=0;
     
     public UserInfo(int fieldIndex,String fieldDescription,int valueType,int valueLength )
     {
         fieldIndex_=fieldIndex;
         fieldDescription_=fieldDescription;
         valueType_=valueType;
         valueLength_=valueLength;
     }    
    /**
    * Returns the copyright.
    **/
    private static String getCopyright()
    {
       return Copyright.copyright;
    }
    /**
	 * Returns the field description.
	 *
	 * @return The field description.
	**/
     public String getFieldDescription()
     {
         return fieldDescription_;
     } 

     /**
	 * Returns the value length.
	 *
	 * @return The value length.
	**/
     public int getValueLength()
     {
         return valueLength_;
     }    
     /**
	 * Returns the value type.
	 *
	 * @return The value type.
	**/
     public int getValueType()
     {
         return valueType_;
     }    

}     
