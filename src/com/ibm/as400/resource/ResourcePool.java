///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourcePool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.Trace;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;



/**
The ResourcePool class represents a pool to store Resource objects
for potential lookup and reuse.  For example, if a class is maintaining
several resource lists which may contain multiple objects representing 
the same resource, it can use a ResourcePool object to efficiently
manage and reuse them.  Such reuse may potentially reduce storage 
requirements, but may also improve semantics where changes made in one
context will be reflected in other contexts.

<p>The ResourcePool class maintains reference counts for each 
registered resource.  It keeps a reference to a resource as long as the
reference count is greater than zero.  Therefore, it is important
to carefully manage a ResourcePool and ensure that all resources
are registered and deregistered the same number of times.  Otherwise
a memory leak can result.

<p>The caller can create and manage its own static or non-static 
ResourcePool object or it can use the global static ResourcePool
defined in this class.
**/
//
// Design notes:
//
// 1.  If another class declares its own ResourcePool object, I believe
//     that it should be declared as transient, since it would not really
//     make sense to serialize a pool.
//
class ResourcePool
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    
    // Private data.
    private static final int TRACE_INCREMENT_                   = 100;

    private transient Hashtable                 resources_          = new Hashtable();
    private transient Hashtable                 referenceCounts_    = new Hashtable();



/**
Global static resource pool.
**/
    public static final ResourcePool    GLOBAL_RESOURCE_POOL    = new ResourcePool();



/**
Clears the pool.
**/
    public void clear()
    {
        synchronized(this) {
            resources_.clear();
            referenceCounts_.clear();
        }
    }



/**
Deregisters a resource.  If the resource is registered multiple times,
it is not removed from the pool until it is deregistered the
same number of times.

@param resource     The resource.
**/
    public void deregister(Resource resource)
    {
        if (resource == null)
            throw new NullPointerException("resource");

        Object resourceKey = resource.getResourceKey();
        long referenceCount = -1;
        boolean deregister = false;
        synchronized(resources_) {
            if (resources_.containsKey(resourceKey)) {
                deregister = true;
                referenceCount = --((long[])referenceCounts_.get(resourceKey))[0];
                if (referenceCount == 0) {
                    resources_.remove(resourceKey);
                    referenceCounts_.remove(resourceKey);

                    if (Trace.isTraceOn()) {
                        int size = resources_.size();
                        if (size % TRACE_INCREMENT_ == 0)
                            Trace.log(Trace.INFORMATION, "Resource pool " + this + " contains " + size + " resources.");
                    }
                }
            }
        }
    }



/**
Returns a registered resource.  This does not affect
the reference count.

@param resourceKey  Identifies the resource.
@return             The resource, or null if the identified resource
                    is not found.
**/
    public Resource getResource(Object resourceKey)
    {
        if (resourceKey == null)
            throw new NullPointerException("resourceKey");

        return (Resource)resources_.get(resourceKey);
    }



/**
Registers a resource.  If a resource with the same resource ID already 
exists, it is not replaced, but its reference count is increased.

@param  resource    The resource.
**/
    public void register(Resource resource)
    {
        register(resource, false);
    }



/**
Registers a resource.  If a resource with the same resource ID already
exists, it will be optionally replaced, and its reference count is
increased.

@param  resource    The resource.
@param  replace     true if a resource with the same resource ID 
                    is replaced, false otherwise.
**/
    public void register(Resource resource, boolean replace)
    {
        if (resource == null)
            throw new NullPointerException("resource");

        Object resourceKey = resource.getResourceKey();
        long referenceCount;
        synchronized(this) {
            if (resources_.containsKey(resourceKey)) {
                if (replace)
                    resources_.put(resourceKey, resource);
                referenceCount = ++((long[])referenceCounts_.get(resourceKey))[0];
            }
            else {
                resources_.put(resourceKey, resource);
                referenceCounts_.put(resourceKey, new long[] { 1 });
                referenceCount = 1;

                if (Trace.isTraceOn()) {
                    int size = resources_.size();
                    if (size % TRACE_INCREMENT_ == 0)
                        Trace.log(Trace.INFORMATION, "Resource pool " + this + " contains " + size + " resources.");
                }
            }
        }
    }



}
