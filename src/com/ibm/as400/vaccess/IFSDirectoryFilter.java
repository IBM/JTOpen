///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSDirectoryFilter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileFilter;
import java.io.IOException;
import java.io.Serializable;



/**
The IFSDirectoryFilter class provides support that
optionally weeds out files along with another filter.
**/
class IFSDirectoryFilter
implements IFSFileFilter, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private int             include_;
    private IFSFileFilter   otherFilter_;



/**
Constructs an IFSDirectoryFilter object.

@param include    One of the constants: INCLUDE_FILES,
                  INCLUDE_DIRECTORIES, or INCLUDE_BOTH.
@param filter     The other filter, or null if none.
**/
    public IFSDirectoryFilter (int include,
                               IFSFileFilter otherFilter)
    {
        include_        = include;
        otherFilter_    = otherFilter;
    }



/**
Indicates if the file is accepted by the filter.  The filter
will accept the file if and only if it is a directory.

@param  file    The file.
@return         true if the file is accepted by the filter,
                false otherwise.
**/
    public boolean accept (IFSFile file)
    {
        boolean accept = true;

        // If we do not want to include files, then check
        // to see if this is a directory.
        try {
            switch (include_) {
                case VIFSDirectory.INCLUDE_FILES:
                    accept = ! file.isDirectory ();
                    break;
                case VIFSDirectory.INCLUDE_DIRECTORIES:
                    accept = file.isDirectory ();
                    break;
                case VIFSDirectory.INCLUDE_BOTH:
                default:
                    accept = true;
                    break;
            }
        }
        catch (IOException e) {
            // Ignore.  The file will not be accepted.
            accept = false;
        }

        // Check the other filter if necessary.
        if ((accept == true) && (otherFilter_ != null))
            accept = otherFilter_.accept (file);

        return accept;
    }



/**
Indicates if files or directories are contained in the list of
children.

@return  One of the constants: INCLUDE_FILES, INCLUDE_DIRECTORIES,
         or INCLUDE_BOTH.
**/
    public int getInclude ()
    {
        return include_;
    }



/**
Returns the other filter which determines which files are
accepted.

@return  The other filter, or null if none.
**/
    public IFSFileFilter getOtherFilter ()
    {
        return otherFilter_;
    }



/**
Sets whether files or directories are contained in the list of
children.

@param   include    One of the constants: INCLUDE_FILES,
                    INCLUDE_DIRECTORIES, or INCLUDE_BOTH.
**/
    public void setInclude (int include)
    {
        include_ = include;
    }



/**
Sets the other filter which determines which files are
accepted.

@param filter  The other filter, or null if none.
**/
    public void setOtherFilter (IFSFileFilter otherFilter)
    {
        otherFilter_ = otherFilter;
    }


}
