///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

    int vrm_;

    // Create a version object.
    // @param  int  version/release/modification.  High 16 bits represent version next 8 bits represent release, low 8 bits represent modification.  Thus Version 3, release 1, modification level 0 is 0x00030100.
    ServerVersion(int vrm)
    {
        vrm_ = vrm;
    }

    // Get the AS/400 version.
    // @return  Version of the AS/400.
    int getVersion()
    {
        return vrm_ >> 16 & 0x0000ffff;
    }

    // Get the AS/400 release.
    // @return  Release of the AS/400.
    int getRelease()
    {
        return vrm_ >> 8 & 0x000000ff;
    }

    // Get the modification level of the AS/400.
    // @return  Modification level of the AS/400.
    int getModificationLevel()
    {
        return vrm_ & 0x000000ff;
    }

    // Get the combined version, release and modification level of the AS/400.
    // @return Version/release/modification level where the high 16 bits is the version, next 8 bits the release, and low 8 bits the modification level.
    int getVersionReleaseModification()
    {
        return vrm_;
    }
}
