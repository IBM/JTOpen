///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPIDAFPResource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * NPCPIDAFPResource is used to contain an AFP resource ID code point.
 * This code point has 3 values in it:
 *     ATTR_RSCNAME  - resource name
 *     ATTR_RSCLIB   - resource library
 *     ATTR_RSCTYPE  - resource type
 **/

class NPCPIDAFPResource extends NPCPID implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


   /**
    * copy constructor
    **/
    NPCPIDAFPResource(NPCPIDAFPResource cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    **/
    NPCPIDAFPResource()
    {
       super(NPCodePoint.RESOURCE_ID);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    **/
    NPCPIDAFPResource( byte[] data )
    {
       super(NPCodePoint.RESOURCE_ID, data);
    }

   /**
    * constructor that takes the ID as seperate items
    **/
    NPCPIDAFPResource(String resourceName,
                      String resourceLib,
                      String resourceType
                      )
    {
       super(NPCodePoint.RESOURCE_ID);
       int intResourceType = 0;
       setAttrValue(PrintObject.ATTR_RSCNAME, resourceName);
       setAttrValue(PrintObject.ATTR_RSCLIB, resourceLib);
       // convert resource type from string to int - this conversion will
       // throw an exception if it is invalid
       //
       // try to map the string type into an integer type
       // if it fails it will throw ExtendedIllegalArgumentException which
       // we will map to IllegalPathNameException with a rc of type not valid
       //
       try
           {
           intResourceType = NPCPSelRes.stringTypeToIntType(resourceType);
           }
       catch (ExtendedIllegalArgumentException e)
           {
           Trace.log(Trace.ERROR, "Parameter 'resource' has a invalid object type.");
           throw new IllegalPathNameException(resourceType,
                                              IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
           }

       setAttrValue(PrintObject.ATTR_RSCTYPE, intResourceType);
    }


    protected Object clone()
    {
       NPCPIDAFPResource cp = new NPCPIDAFPResource(this);
       return cp;
    }

    

   /**
    * get the resource type as a string
    **/
    String getResourceType()
    {
        int type = getIntValue(PrintObject.ATTR_RSCTYPE).intValue();
        return (NPCPSelRes.intTypeToStringType(type));
    }



  /**
   * get the resource library
   **/
   String library()
   {
      return getStringValue(PrintObject.ATTR_RSCLIB);
   }

  /**
   * get the resource name
   **/
   String name()
   {
      return getStringValue(PrintObject.ATTR_RSCNAME);
   }

} // NPCPIDAFPResource

