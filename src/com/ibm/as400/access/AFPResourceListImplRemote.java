///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AFPResourceListImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * The AFPResourceList class is used to build a list of server AFP resource objects of type AFPResource.
  * The list can be filtered by library and resource name,
  * by resource type and by spooled file (list only resources
  * used by a particular spooled file).  In addition,
  * font resources may be filtered by pel density.
  *
  * To list and use AFP resources, your server operating system must
  * be at V3R7 or later.
  *
  *@see AFPResource
  **/


class AFPResourceListImplRemote extends PrintObjectListImplRemote
// @A5D implements AFPResourceListImpl
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /** Font pel density of NONE, removes pel density filter. **/
    // private static final int PELDENSITYNONE = 0;

    /** Font pel density of 240x240 dpi. **/
    // private static final int PELDENSITY240 = 1;

    /** Font pel density of 300x300 dpi. **/
    // private static final int PELDENSITY300 = 2;
    
    // private static final String FONT_PEL_DENSITY_FILTER = "fontPelDensityFilter";
    
    // static private binary data for default attribute to
    // retrieve on an AFP Resource when listing AFP Resources
    // format is:
    //    ---------------------------------------------------
    //    |nn | LEN | ID1 | ID2 | ID3 | ID4 | ....... | IDnn|
    //   ---------------------------------------------------
    //       nn   - two byte total # of attributes in code point
    //       LEN  - two byte length of each attribute entry, right
    //              now this will be 2 (0x02).
    //       IDx  - two byte attribute ID

    private static final byte[] attrIDToList_ =
    {
        0x00, 0x07,           // big endian(BE), number of attrs
        0x00, 0x02,           // BE - size in bytes of each ID
        0x00, (byte)0xAE,     // ATTR_RSCLIB
        0x00, (byte)0xAF,     // ATTR_RSCNAME
        0x00, (byte)0xB0,     // ATTR_RSCTYPE
        0x00, (byte)0xB1,     // ATTR_OBJEXTATTR
        0x00, 0x6D,           // ATTR_DESCRIPTION
        0x00, 0x22,           // ATTR_DATE
        0x00, 0x6E            // ATTR_TIME
    };

    private static final NPCPAttributeIDList defaultAttrIDsToList_ = new NPCPAttributeIDList(attrIDToList_);

    // register the AFP resource return datastream for listing resources
    static
    {
        NPDataStream ds;
        NPCodePoint  cp;
        
        ds = new NPDataStream(NPConstants.RESOURCE); // @B1C
        cp = new NPCPIDAFPResource();
        ds.addCodePoint(cp);
        cp = new NPCPAttribute();
        ds.addCodePoint(cp);
        AS400Server.addReplyStream(ds, "as-netprt");
    }
    
    
    
    /**
      * Returns the resource list filter for font resources by
      * their pel density.
      *
      **/
      // @A1D - Removed this method 
/*  private int getFontPelDensityFilter()
    {
        // The selection code point is always present, the fontPelDensity
        // Filter may not have been set.

        NPCPSelRes selectionCP = (NPCPSelRes)getSelectionCP();
        String pelDensity = selectionCP.getPelDensity();

        if( pelDensity.equals(NPCPSelRes.PEL240) )
        {
            return PELDENSITY240;
        }
        else if( pelDensity.equals(NPCPSelRes.PEL300) )
        {
            return PELDENSITY300;
        }
        else
        {
            return PELDENSITYNONE;
        }
    } 
    */
    

    /**
      * Returns the default attributes to list.
      **/
    NPCPAttributeIDList getDefaultAttrsToList()
    {
        return defaultAttrIDsToList_;
    }



    /**
      * Constructs a new AFPResource object.
      **/
    /* @A5D
     PrintObject newNPObject(AS400 system, NPDataStream reply)
     {
        AFPResource npObj = null;
        NPCPIDAFPResource cpid;
        NPCPAttribute cpAttrs;
        cpid = (NPCPIDAFPResource)reply.getCodePoint(NPCodePoint.RESOURCE_ID);  // never should return null
        cpAttrs = (NPCPAttribute)reply.getCodePoint(NPCodePoint.ATTRIBUTE_VALUE);   // may return null
        npObj = new AFPResource(system, cpid, cpAttrs);
        return npObj;
    }
    */


    NPCPID newNPCPID(NPDataStream reply)
    {
        return (NPCPIDAFPResource)reply.getCodePoint(NPCodePoint.RESOURCE_ID);  // never should return null
    }


    /**
      * Sets the resource list filter for font resources by their
      * pel density.  This filter should be used if 
      * listing font resources and there is a need to get them
      * in a particular pel density.
      *
      * @param fontPelDensityFilter The fonts will be listed
      * based on their pel densities.  Allowed values are:
      * <ul>
      * <li> PELDENSITY240 - only fonts with 240x240 dpi are listed.
      * <li> PELDENSITY300 - only fonts with 300x300 dpi are listed.
      * </ul>
      *
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
      // @A1D - Removed this method   
/*  private void setFontPelDensityFilter(int fontPelDensityFilter)
    throws PropertyVetoException
    {
        int oldFontPelDensityFilter = getFontPelDensityFilter();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( FONT_PEL_DENSITY_FILTER,
                                   new Integer(oldFontPelDensityFilter),
                                   new Integer(fontPelDensityFilter) );

        // No one vetoed, make the change.
        NPCPSelRes selectionCP = (NPCPSelRes)getSelectionCP();
        if (fontPelDensityFilter == PELDENSITY240)
        {
            selectionCP.setPelDensity(NPCPSelRes.PEL240);
        }
        else if (fontPelDensityFilter == PELDENSITY300)
        {
            selectionCP.setPelDensity(NPCPSelRes.PEL300);
        }
        else if (fontPelDensityFilter == PELDENSITYNONE )
        {
            selectionCP.setPelDensity(NPCPSelRes.emptyString);
        }
        else
        {
            Trace.log(Trace.ERROR, "Parameter 'fontPelDensityFilter' is invalid.");
            throw new ExtendedIllegalArgumentException(
              "fontPelDensityFilter("+fontPelDensityFilter+")",
              ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Notify any property change listeners.
        changes.firePropertyChange( FONT_PEL_DENSITY_FILTER,
                                     new Integer(oldFontPelDensityFilter),
                                     new Integer(fontPelDensityFilter) );
    }
    */
    

    /**
      * Sets resource list filter by library, name, and resource type.
      *
      * @param resourceFilter The resources to list.
      *  The format of the resourceFilter string must be in the
      *  format of "/QSYS.LIB/libname.LIB/resource.type", where
      * <br>
      *   <I>libname</I> is the library name that contains the resources to search.
      *     Resources listed will be restricted to those found in these libraries.  
      *
      *     The library name can be a specific name or one of these special values:
      * <ul>
      * <li> %ALL%     - All libraries are searched.
      * <li> %ALLUSR%  - All user-defined libraries, plus libraries containing user data
      *                 and having names starting with the letter Q.
      * <li> %CURLIB%  - The server job's current library.
      * <li> %LIBL%    - The server job's library list.
      * <li> %USRLIBL% - The user portion of the server job's library list.
      * </ul>
      * <p> 
      *   <I>resource</I> is the name of the resource(s) to list.
      *     It can be a specific name, a generic name, or the special value %ALL%.
      * <br>
      *   <I>type</I> is the type of resource to list.  It can be any of these
      *  special values:
      *  <ul>
      *  <li> %ALL%     - All resources are listed.
      *  <li> FNTRSC    - Only font resources are listed.
      *  <li> FORMDF    - Only form definitions are listed.
      *  <li> OVL       - Only overlays are listed.
      *  <li> PAGEDFN   - Only page definitions are listed.
      *  <li> PAGESEG   - Only page segments are listed.
      *  </ul>
      *  The default for the library is %LIBL%; for resource, it is %ALL%; and for
      *  type, it is %ALL%.
      **/
    public void setResourceFilter(String resourceFilter)
    {
        NPCPSelRes selectionCP = (NPCPSelRes)getSelectionCP();
        selectionCP.setResource( resourceFilter );
    }

} // APFResourceList class

