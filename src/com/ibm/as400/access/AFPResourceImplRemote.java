///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AFPResourceImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * The AFPResource class represents an AS/400 AFP resource.
 * An instance of this class can be used to manipulate an individual
 * AS/400 AFP resource.
 *
 * See <a href="{@docRoot}/com/ibm/as400/access/doc-files/AFPResourceAttrs.html">AFP Resource Attributes</a> for
 * valid attributes.
 *
 **/

class AFPResourceImplRemote extends PrintObjectImplRemote
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private static final NPCPAttributeIDList attrsToRetrieve_  = new NPCPAttributeIDList();
    private static boolean fAttrIDsToRtvBuilt_ = false;

    private synchronized void buildAttrIDsToRtv()
    {
        if (!fAttrIDsToRtvBuilt_)
        {
            fAttrIDsToRtvBuilt_ = true;
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE);       // date
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DESCRIPTION);// text description
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OBJEXTATTR); // object extended attribute
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RSCLIB);     // resource library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RSCNAME);    // resource name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RSCTYPE);    // resource object type
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_TIME);       // time
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_NUMBYTES);   // number of bytes to read/write
        }
    }



    // Check the run time state
    void checkRunTimeState()
    {
        // check whatever the base class needs to check
        super.checkRunTimeState();

        // AFPResource's need to additionally check the IFS pathname.
        if( getIDCodePoint() == null ) {
            Trace.log(Trace.ERROR, "Parameter 'path' has not been set.");
            throw new ExtendedIllegalStateException(
              "path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
    }



    // This method implements an abstract method of the superclass
    NPCPAttributeIDList getAttrIDsToRetrieve()
    {
        if (!fAttrIDsToRtvBuilt_) {
            buildAttrIDsToRtv();
        }
        return attrsToRetrieve_;
    }


    NPCPAttributeIDList getAttrIDsToRetrieve(int attrToRtv)
    {
      if (!fAttrIDsToRtvBuilt_)
      {
        attrsToRetrieve_.addAttrID(attrToRtv);
      }
      return attrsToRetrieve_;
    }


    private static NPCPIDAFPResource buildIDCodePoint(String IFSResourceName)
    {
        QSYSObjectPathName ifsPath = new QSYSObjectPathName(IFSResourceName);

        return new NPCPIDAFPResource(ifsPath.getObjectName(),
                                     ifsPath.getLibraryName(),
                                     ifsPath.getObjectType());
    }
}
