///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PagedListBuffer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.Trace;
import java.io.Serializable;



/**
The PagedListBuffer class represents a buffer that stores resources in
a set of pages.  Each page contains a portion of a resource list.  If all
pages are being used and another is needed, the least recently used page is
cleared and reused.

<p>This class can optionally manage a <a href="ResourcePool.html">
ResourcePool</a>.  It will add and remove entries to the pool as
they are stored and cleared from the buffer.
**/
//
// Implementation note:
//
// 1.  All resource pool registering and deregistering happens in this
//     class, because this class is the only one that knows exactly when
//     such resources are being paged in/out.
//
class PagedListBuffer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private boolean                                 clear_;
    private int                                     numberOfPages_;
    private Resource[][]                            pages_;
    private long[]                                  pageIndices_;
    private int                                     pageSize_;
    private long[]                                  pageTimestamps_;

    private ResourcePool                            pool_           = ResourcePool.GLOBAL_RESOURCE_POOL;



/**
Constructs a PagedListBuffer object.

@param numberOfPages        The number of pages.
@param pageSize             The page size.
**/
    public PagedListBuffer(int numberOfPages, int pageSize)
    {
        numberOfPages_ = numberOfPages;
        pageSize_ = pageSize;
        initialize();
    }



/**
Clears the buffer.
**/
    public void clear()
    {
        synchronized(this) {

            // Remove all entries from the pool.
            if (pool_ != null) {
                for (int i = 0; i < numberOfPages_; ++i)
                    for (int j = 0; j < pageSize_; ++j) {
                        if (pages_ != null) {
                            if (pages_[i] != null)
                                if (pages_[i][j] != null)
                                    pool_.deregister(pages_[i][j]);
                        }
                    }
            }

            // Re-initialize the buffer.
            initialize();

            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "Paged list buffer " + this + " cleared.");
        }
    }



/**
Finds the page, if any, which should contain the index.

@param index    The resource index.
@return         The page number, or -1 if such a page was
                not found.
**/
    private int findPage(long index)
    {
        for (int i = 0; i < numberOfPages_; ++i) {
            if ((pageIndices_[i] != -1)
                && (pageIndices_[i] <= index)
                && (index < pageIndices_[i] + pageSize_)) {
                return i;
            }
        }
        return -1;
    }



/**
Returns a resource from the buffer.

@param  index The resource index.
@return The resource, or null if the index does not
        refer to a resource that is loaded in the
        buffer.
**/
     public Resource getResource(long index)
    {
        synchronized(this) {
            int page = findPage(index);

            // If a page was found, then return it.  This value could
            // still be null in the case where the page was not full.
            if (page != -1)
                return pages_[page][(int)(index - pageIndices_[page])];

            // No such page was found, so the resource is not loaded.
            return null;
        }
    }



/**
Initializes the buffer.
**/
    private void initialize()
    {
        clear_ = true;
        pages_ = new Resource[numberOfPages_][];
        pageIndices_ = new long[numberOfPages_];
        pageTimestamps_ = new long[numberOfPages_];
        for (int i = 0; i < numberOfPages_; ++i) {
            pages_[i] = null;
            pageIndices_[i] = -1;
            pageTimestamps_[i] = -1;
        }
    }



/**
Sets a resource in the buffer.

@param index    The resource index.
@param resource The resource.
**/
    public void setResource(long index, Resource resource)
    {
        if (resource == null)
            throw new NullPointerException("resource");

        synchronized(this) {
            clear_ = false;

            int page = findPage(index);

            // If a page for this index already exists, then just
            // set the resource and update its timestamp.
            if (page >= 0) {
                pages_[page][(int)(index - pageIndices_[page])] = resource;
                pageTimestamps_[page] = System.currentTimeMillis();
                if (pool_ != null)
                    pool_.register(resource);
                return;
            }

            // If no page exists, check to see if there are any
            // pages not being used.
            for (int i = 0; i < numberOfPages_; ++i) {
                if (pages_[i] == null) {
                    page = i;
                    break;
                }
            }

            // If no pages are available, then remove the least recently
            // used page.
            if (page < 0) {
                long lruTimestamp = System.currentTimeMillis();
                for (int i = 0; i < numberOfPages_; ++i) {
                    if (pageTimestamps_[i] <= lruTimestamp) {           // @A1C
                        page = i;
                        lruTimestamp = pageTimestamps_[i];
                    }
                }

                // Remove the resources on the least recently used page
                // from the pool.
                if (pool_ != null) {
                    for (int i = 0; i < pageSize_; ++i)
                        if (pages_[page][i] != null)
                            pool_.deregister(pages_[page][i]);
                }

                // Force garbage collection for good measure.
                for (int i = 0; i < pageSize_; ++i)
                    pages_[page][i] = null;
                pages_[page] = null;
//@B0D                System.gc();

                if (Trace.isTraceOn())
                    Trace.log(Trace.INFORMATION, "Paged list buffer " + this + " swapped out page " + page + ".");
            }

            // Create a new page.
            pages_[page] = new Resource[pageSize_];
            pages_[page][0] = resource;
            pageIndices_[page] = index;
            pageTimestamps_[page] = System.currentTimeMillis();
            if (pool_ != null)
                pool_.register(resource);

            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "Paged list buffer " + this + " created page " + page + ".");
        }
    }

}
