///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  JobCCSID.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 The JobCCSID class represents an iSeries Job CCSID.
 **/
public class JobCCSID
{
    // The server where the job is located.
    private AS400 system_ = null;
    private int ccsid_ = -1;

    /**
     Constructs a JobCCSID object.
     **/
    public JobCCSID()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing JobCCSID object.");
    }

    /**
     Constructs a JobCCSID object.  It uses the specified server.
     @param  system  The server on which to retrieve the CCSID.
     **/
    public JobCCSID(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing JobCCSID object, system: " + system);
        system_ = system;
    }

    /**
     Returns the server on which the Job CCSID is to be retrieved.
     @return  The server on which the Job CCSID is to be retrieved.  If the server has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Retrieves the CCSID for this object.
     @return  The Job CCSID of the corresponding job.
     **/
    public int retrieveCcsid() throws AS400SecurityException, ErrorCompletingRequestException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving CCSID.");
        if (ccsid_ == -1)
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving CCSID from server...");
            if (system_ == null)
            {
                try
                {
                    ccsid_ = JobCCSIDNative.retrieveCcsid();
                }
                catch (Throwable e)
                {
                    Trace.log(Trace.ERROR, "Attempt to connect to server before setting system.");
                    throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
                }
            }
            else
            {
                ccsid_ = system_.getJobCcsid();
            }
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "CCSID:", ccsid_);
        return ccsid_;
    }

    /**
     Sets the server on which to retrieve the Job CCSID.  The server cannot be changed once a connection is made to the server.
     @param  system  The server on which to retrieve the Job CCSID.
     **/
    public void setSystem(AS400 system)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);
        if (ccsid_ != -1)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        system_ = system;
    }
}
