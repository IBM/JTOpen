///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceLevel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;
                       
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import java.io.Serializable;



/**
The ResourceLevel class represents a range of supported levels.
A level is a string which describes some level of support.

<p>In most cases within the IBM Toolbox for Java, the level
is the version, release, and modification level of the server
to which you are connected, in the form <code>VxRxMx</code>.
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
// 
// Design notes:  
//
// 1. I originally wanted to use Object for the level, but I needed
//    something for comparisions, i.e., a type that has the notion
//    of less than.  So I chose what I thought was the next most
//    generic type, String.  (Number would have been another possible
//    choice, but not as usable in my opinion.)
//
public class ResourceLevel
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



/**
Constant value for the level representing V4R4M0.
**/
    public static final String V4R4M0                           = "V4R4M0";

    
    
/**
Constant value for the level representing V4R5M0.
**/
    public static final String V4R5M0                           = "V4R5M0";

    
    
/**
Constant value for the level representing V5R1M0.
**/
    public static final String V5R1M0                           = "V5R1M0";



    // Private data.
    private String          minLevel_                           = null;
    private String          maxLevel_                           = null;



/**
Constructs a ResourceLevel object which indicates that all levels are supported.
**/
    public ResourceLevel()
    {
    }



/**
Constructs a ResourceLevel object.

@param minLevel     The minimum supported level, or null if all levels are supported.
**/
    public ResourceLevel(String minLevel)
    {
        minLevel_ = minLevel;
    }



/**
Constructs a ResourceLevel object.

@param minLevel     The minimum supported level, or null if all previous levels are supported.
@param maxLevel     The maximum supported level, or null if all following levels are supported.
**/
    public ResourceLevel(String minLevel, String maxLevel)
    {
        if ((minLevel != null) && (maxLevel != null))
            if (minLevel.compareTo(maxLevel) < 0)
                throw new ExtendedIllegalArgumentException("maxLevel", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);        
        minLevel_ = minLevel;
        maxLevel_ = maxLevel;
    }



/**
Indicates if the VRM is within the range of this level.

@param vrm  The VRM for a server.  
@return     true if the VRM is within the range of this level,
            false otherwise.
            
@see com.ibm.as400.access.AS400#getVRM()            
**/
    public boolean checkLevel(int vrm)
    {
        return checkLevel(vrmToLevel(vrm));
    }



/**
Indicates if the specified level is within the range of this level.

@param level The specified level.                                
@return     true if the specified level is within the range of this level,
            false otherwise.
**/
    public boolean checkLevel(String level)
    {
        if (level == null)
            return true;
        else if (level.length() == 0)
            return true;
        else if (minLevel_ == null) {
            if (maxLevel_ == null)
                return true;
            else
                return (level.compareTo(maxLevel_) <= 0);
        }
        else {
            if (maxLevel_ == null)
                return (minLevel_.compareTo(level) <= 0);
            else
                return ((minLevel_.compareTo(level) <= 0)
                        && (level.compareTo(maxLevel_) <= 0));
        }
    }



/**
Returns the minimum supported level.

@return  The minimum supported level, or null if all previous levels are supported.
**/
    public String getMinLevel()
    {
        return minLevel_;
    }



/**
Returns the maximum supported level.

@return  The maximum supported level, or null if all following levels are supported.
**/
    public String getMaxLevel()
    {
        return maxLevel_;
    }



/**
Converts the VRM to a level.

@param vrm  The VRM for a server.  
@return         The level.

@see com.ibm.as400.access.AS400#getVRM()            
**/
    public static String vrmToLevel(int vrm)
    {
        StringBuffer buffer = new StringBuffer("V");
        buffer.append((int)((vrm & 0xFFFF0000) >> 16));
        buffer.append("R");
        buffer.append((int)((vrm & 0x0000FF00) >> 8));
        buffer.append("M");
        buffer.append((int)((vrm & 0x000000FF)));
        return buffer.toString();
    }



/**
Returns a String representation of the level.

@return The String representation of the level.
**/
    public String toString()
    {
        StringBuffer buffer = new StringBuffer("[");
        if (minLevel_ != null)
            buffer.append(minLevel_);
        buffer.append(',');
        if (maxLevel_ != null)
            buffer.append(maxLevel_);
        buffer.append(']');
        return buffer.toString();
    }



}
