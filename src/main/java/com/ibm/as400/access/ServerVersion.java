///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ServerVersion.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

class ServerVersion implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;
    int vrm_;

    // Create a version object.
    // @param  int  version/release/modification.  High 16 bits represent version next 8 bits represent release, low 8 bits represent modification.  Thus Version 3, release 1, modification level 0 is 0x00030100.
    ServerVersion(int vrm)
    {
        vrm_ = vrm;
    }

    // Get the server version.
    // @return  Version of the server.
    int getVersion()
    {
        return vrm_ >> 16 & 0x0000ffff;
    }

    // Get the server release.
    // @return  Release of the server.
    int getRelease()
    {
        return vrm_ >> 8 & 0x000000ff;
    }

    // Get the modification level of the server.
    // @return  Modification level of the server.
    int getModificationLevel()
    {
        return vrm_ & 0x000000ff;
    }

    // Get the combined version, release and modification level of the server.
    // @return Version/release/modification level where the high 16 bits is the version, next 8 bits the release, and low 8 bits the modification level.
    int getVersionReleaseModification()
    {
        return vrm_;
    }
}
