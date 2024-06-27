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

/**
 * The ServerVersion class provides several useful methods 
 * to manipulate the IBM i operating system release, modification, and modification level as an integer.
 * <P>
 * The high 16 bits represent version next 8 bits represent release, low 8 bits represent modification.  
 * For example, Version 3, release 1, modification level 0 is 0x00030100.
 */

public class ServerVersion implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2024 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;
    private int vrm_;

    /**
     * Constructs a ServerVersion object.
     * 
     * @param vrm Integer representing the version, modification, and modification level of the IBM i operating system. 
     */
    public ServerVersion(int vrm) {
        vrm_ = vrm;
    }
    
    /**
     * Constructs a ServerVersion object.
     * 
     * @param vrm String representing the version, modification, and modification level of the IBM i operating system. 
     *            For example, "V7R3M0".
     */
    public ServerVersion(String vrm)
    {
        if (vrm == null)
            throw new NullPointerException("vrm");
        
        if (vrm.length() < 6)
            throw new ExtendedIllegalArgumentException("vrm (" + vrm + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        
        vrm = vrm.toUpperCase();
        int vi = vrm.indexOf('V');
        int ri = vrm.indexOf('R');
        int mi = vrm.indexOf('M');
        
        if ((vi != 0) || (ri <2) || (mi < ri + 2) || (vrm.indexOf('+') != -1))
            throw new ExtendedIllegalArgumentException("vrm (" + vrm + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        int version      = getInteger(vrm.substring(1, ri));
        int release      = getInteger(vrm.substring(ri+1, mi));
        int modification = getInteger(vrm.substring(mi+1));
        
        if (version < 0 || release < 0 || modification < 0)
            throw new ExtendedIllegalArgumentException("vrm (" + vrm + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        vrm_ = (version << 16) + (release << 8) + modification;
    }

    /**
     * Returns integer representing the version of the IBM i operating system. 
     * 
     * @return the version of the IBM i operating system.
     */
    public int getVersion() {
        return vrm_ >> 16 & 0x0000ffff;
    }

    /**
     * Returns integer representing the release of the IBM i operating system. 
     * 
     * @return the release of the IBM i operating system.
     */
    public int getRelease() {
        return vrm_ >> 8 & 0x000000ff;
    }

    /**
     * Returns integer representing the modification level of the IBM i operating system. 
     * 
     * @return the modification level of the IBM i operating system.
     */
    public int getModificationLevel() {
        return vrm_ & 0x000000ff;
    }

    /**
     * Returns integer representing the version, release, and modification level of the IBM i operating system. 
     * 
     * @return integer representing the version, release, and modification level of the IBM i operating system.
     */
    public int getVersionReleaseModification() {
        return vrm_;
    }
    
    public void setVersionReleaseModification(int vrm) {
         vrm_ = vrm;
    }
    
    private int getInteger(String n)
    {
        try {
            return ((n == null || n.isEmpty()) ? -1 : Integer.parseInt(n));
        }
        catch (NumberFormatException ex) { }
        
        return -1;
    }
    
    /**
     * Returns a string representation of the version, release, and modification level of the IBM i operating system.
     * 
     * @return string representation of the object.
     */
    @Override
    public String toString() {
        return String.format("V%dR%dM%d", getVersion(), getRelease(), getModificationLevel());
    }
}
