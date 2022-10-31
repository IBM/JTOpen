///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPSelRes.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPCPSelRes class - class for an attribute value list code point used with
  * the network print server's data stream.
  * This class is derived from NPCPSelection and will be used to build a code
  * point that has as its data a list of any attributes that can filter a
  * AFP resource list.  These include the resource library name, the resource
  * name, the resource object type and the pel density for font resources.
  *
 **/

class NPCPSelRes extends NPCPSelection implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    static final int FNTRSC = 0x0001;
    static final int FORMDF = 0x0002;
    static final int OVL    = 0x0004;
    static final int PAGSEG = 0x0008;
    static final int PAGDFN = 0x0010;

    static final int ALLRSC = 0x001F;   // update this if you add any new resources!

    static final String STR_ALL     = "%ALL%";
    static final String STR_UNKNOWN = "UNKNOWN";

    static final String PEL240  = "1";
    static final String PEL300  = "2";

   /**
    * copy constructor
    **/
    NPCPSelRes(NPCPSelRes cp)
    {
       super(cp);
    }

   /**
    * basic constructor that creates an empty printer selection codepoint
    **/
    NPCPSelRes()
    {
        super();
    }

    protected Object clone()
    {
        NPCPSelRes cp = new NPCPSelRes(this);
        return cp;
    }

    

   /**
    * Gets font pel density
    * @returns pelDensity a string that maps which pel density of
    * font you want.  "1" for 240, "2" for 300, and "" if the filter
    * has not been set.
    **/
    String getPelDensity()
    {
        // if font pel density has not been set, return an empty string.
        String pelDensity = getStringValue(PrintObject.ATTR_PELDENSITY);
        if( pelDensity == null )
        {
            return emptyString;
        } else {
            return pelDensity;
        }
    }

    /** gets the IFS path of the filter
      * @returns the IFS path or an empty string if the filter
      * has not been set.
      **/
    String getResource()
    {
        // Note, we cannot use the simple getStringValue(PrintObject.ATTR_AFP_RESOURCE
        // here because the type is an integer, not a string and it is cmplex to convert it
        // back and forth, so we do it here, instead of in NPCPAttributeValue.

        // if both the name and library have not been set,
        // return an empty string.
        String rscName = getStringValue(PrintObject.ATTR_RSCNAME);
        if (rscName == null)
        {
            return emptyString;
        }

        String rscLib = getStringValue(PrintObject.ATTR_RSCLIB);
        if (rscLib == null)
        {
            return emptyString;  // could set it to *LIBL here too, I suppose
        }

        String rscType = null;
        Integer i = getIntValue(PrintObject.ATTR_RSCTYPE);
        if (i == null)
        {
            rscType = STR_ALL;
        } else {
            rscType = intTypeToStringType(i.intValue());
        }

        return QSYSObjectPathName.toPath(rscLib, rscName, rscType);
    }

    /**
      * static utility function to convert a int resource type into its
      * String value (ie. 0x0001 maps to "FNTRSC").
      **/
    static String intTypeToStringType(int resourceType)
    {
        switch (resourceType)
        {
            case PAGSEG:
               return AFPResource.STR_PAGSEG;
            case OVL:
               return AFPResource.STR_OVL;
            case FNTRSC:
               return AFPResource.STR_FNTRSC;
            case FORMDF:
               return AFPResource.STR_FORMDF;
            case PAGDFN:
               return AFPResource.STR_PAGDFN;
            default:                               // I do not throw an exception here because
              return NPCPSelRes.STR_UNKNOWN;       // the host may define new resource types in
                                                   // the future and I think we still want to work
        }
    } // intTypeToStringType()

   /**
    * Sets the resource name, library and type using an IFS path
    * Removes the filter if resource is "".
    **/
    void setResource(String resource)
    {
        if( resource.length() == 0 ) 
        {
            // this will remove all 3 attribute values
            removeAttribute(PrintObject.ATTR_AFP_RESOURCE);
        } else {
            // construct a QSYSObjectPathName object
            QSYSObjectPathName ifsPath = new QSYSObjectPathName(resource);
            String strRes = ifsPath.getObjectName();
            String strLib = ifsPath.getLibraryName();
            String type = ifsPath.getObjectType();

            if (type.equals(STR_ALL))
            {
                setResourceType(NPCPSelRes.ALLRSC);
            } else {

                //
                // try to map the string type into an integer type
                // if it fails it will throw ExtendedIllegalArgumentException which
                // we will map to IllegalPathNameException with a rc of type not valid
                //
                try
                {
                   setResourceType( stringTypeToIntType(type) );
                }
                catch (ExtendedIllegalArgumentException e)
                {
                     Trace.log(Trace.ERROR, "Parameter 'resource' has a invalid object type.");
                     throw new IllegalPathNameException(resource,
                                       IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
                }
            }
            setAttrValue(PrintObject.ATTR_RSCLIB, strLib);
            setAttrValue(PrintObject.ATTR_RSCNAME, strRes);
        }
    }

   /**
    * Sets resource type filter
    * @param resourceType an bitmasked int that can have any of these
    * values turned on:
    * <ul>
    * <li> 0x0001 - *FNTRSC
    * <li> 0x0002 - *FORMDF
    * <li> 0x0004 - *OVL
    * <li> 0x0008 - *PAGSEG
    * <li> 0x0010 - *PAGDFN
    * </ul>
    **/
    void setResourceType(int resourceType)
    {
       setAttrValue(PrintObject.ATTR_RSCTYPE, resourceType);
    }

   /**
    * Sets font pel density
    * @param pelDensity  a string that maps which pel density of
    * font you want.  "1" for 240 and "2" for 300. Removes the
    * filter if the pel density is "".
    **/
    void setPelDensity(String  pelDensity)
    {
       if( pelDensity.length() == 0 )
       {
           removeAttribute(PrintObject.ATTR_PELDENSITY);
       } else {
           setAttrValue(PrintObject.ATTR_PELDENSITY, pelDensity);
       }
    }

    /**
      * static utility function to convert a String resource type into its
      * int value (ie. "FNTRSC" maps to 0x0001).
      **/
    static int stringTypeToIntType(String resourceType)
    {
        int intResourceType = 0;
        if (resourceType.equals(AFPResource.STR_PAGSEG))
        {
            intResourceType = NPCPSelRes.PAGSEG;
        } else if (resourceType.equals(AFPResource.STR_OVL))    {
            intResourceType = NPCPSelRes.OVL;
        } else if (resourceType.equals(AFPResource.STR_FNTRSC)) {
            intResourceType = NPCPSelRes.FNTRSC;
        } else if (resourceType.equals(AFPResource.STR_FORMDF)) {
            intResourceType = NPCPSelRes.FORMDF;
        } else if (resourceType.equals(AFPResource.STR_PAGDFN)) {
            intResourceType = NPCPSelRes.PAGDFN;
        } else {
            Trace.log(Trace.ERROR, "Parameter 'resourceType' has a invalid object type.");
                      throw new ExtendedIllegalArgumentException("resourceType ("+resourceType+")",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        return intResourceType;
    }
} // NPCPSelRes class
