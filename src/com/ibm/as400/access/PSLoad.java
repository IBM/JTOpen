///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PSLoad.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PSLoad class represents the current load
for a proxy server.
**/
class PSLoad
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private int                     activeConnections_          = 0;
    private int                     balanceThreshold_           = -1;
    private int                     effectiveBalanceThreshold_  = -1;
    private int                     effectiveMaxConnections_    = -1;
    private int                     maxConnections_             = -1;
  


/**
Constructs a PSLoad object.
**/
    public PSLoad ()
    {   
        computeEffective ();
    }
         

    
/**
Constructs a PSLoad object.

@param activeConnections    The number of active connections.
@param balanceThreshold     The balance threshold.  Specify 0 
                            to start load balancing
                            immediately or -1 to never start
                            load balancing.
@param maxConnections       The maximum number of connections
                            which can be active at any particular
                            time.  Specify 0 to not allow any 
                            connections or -1 for unlimited 
                            connections.
**/
    public PSLoad (int activeConnections,
                            int balanceThreshold,
                            int maxConnections)
    {
        activeConnections_  = activeConnections;
        balanceThreshold_   = balanceThreshold;
        maxConnections_     = maxConnections;
        computeEffective ();
    }
         

    
/**
Marks all connections as closed.
**/
    public void allConnectionsClosed ()
    {
        activeConnections_ = 0;
    }



/**
Computes the effective max connections and
balance threshold.  These eliminate the possibility
of -1, which has special meaning in both cases.
**/
    private void computeEffective ()
    {
        effectiveMaxConnections_ = (maxConnections_ < 0) ? Integer.MAX_VALUE : maxConnections_;
        effectiveBalanceThreshold_ = (balanceThreshold_ < 0) ? effectiveMaxConnections_ : balanceThreshold_;
    }



/**
Marks a connection as closed.
**/
    public void connectionClosed ()
    {
        if (activeConnections_ > 0)
            --activeConnections_;
    }



/**
Marks a connection as opened.
**/
    public void connectionOpened ()
    {
        ++activeConnections_;
    }



/**
Returns the number of active connections.

@return The number of active connections.
**/
    public int getActiveConnections ()
    {
        return activeConnections_;
    }



/**                                        
Returns the balance threshold.  This is the number of 
connections that must be active before the peer server starts
load balancing by dispatching requests to peer proxy servers.  
Specify 0 to start load balancing immediately or -1 to never 
start load balancing.  

@return The balance threshold, or 0 to start load 
        balancing immediately or -1 to never start load 
        balancing.
**/
    public int getBalanceThreshold ()
    {
        return balanceThreshold_;
    }



/**
Returns the quantification of how busy the proxy server is.

@return The quantification of how busy the proxy server is.
        This number is between 0 and 1.  The higher the
        number is, the busier the proxy server is.
**/
    public double getBusyFactor ()
    {
        if (effectiveMaxConnections_ != 0)
            return ((double) activeConnections_) / ((double) effectiveMaxConnections_);
        else
            return 1;
    }



/**
Returns the effective balance threshold.

@return The effective balance threshold.  This will
        never be -1.
**/
    public int getEffectiveBalanceThreshold ()
    {
        return effectiveBalanceThreshold_;
    }



/**
Returns the effective max connections.

@return The effective max connections.  This will
        never be -1.
**/
    public int getEffectiveMaxConnections ()
    {
        return effectiveMaxConnections_;
    }



/**
Returns the maximum number of connections which can be
active at any particular time.

@return The maximum number of connections which can be
        active at any particular time, or -1 for 
        unlimited connections.
**/
    public int getMaxConnections ()
    {
        return maxConnections_;
    }



/**
Sets the balance threshold.  This is the number of connections 
that must be active before the peer server starts load balancing 
by dispatching requests to peer proxy servers.

@param balanceThreshold     The balance threshold.    
                            Specify 0 to start load balancing
                            immediately or -1 to never start
                            load balancing.

**/
    public void setBalanceThreshold (int balanceThreshold)
    {        
        if (balanceThreshold < -1)
            throw new ExtendedIllegalArgumentException ("balanceThreshold", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        balanceThreshold_ = balanceThreshold;
        computeEffective ();
    }



/**
Sets the maximum number of connections which can be active
at any particular time.  If the maximum number of connections
are active, then any further connection requests will be
rejected.  The default is to allow an unlimited number of
connections.

@param maxConnections   The maximum number of connections
                        which can be active at any particular
                        time.  Specify 0 to not allow any 
                        connections or -1 for unlimited 
                        connections.
**/
    public void setMaxConnections (int maxConnections)
    {
        if (maxConnections < -1)
            throw new ExtendedIllegalArgumentException ("maxConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        maxConnections_ = maxConnections;
        computeEffective ();
    }



}
