///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BufferedResourceList.java
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
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.io.IOException;
import java.io.ObjectInputStream;



/**
The BufferedResourceList class represents a subclass
of the {@link com.ibm.as400.resource.ResourceList ResourceList}
class which manages a list of resources and buffers them efficiently.
This class is intended to be extended and customized by subclasses.

<p>The list is retrieved in pages, and each page contains multiple
resources (those which make up the list).  The higher that page size, the
more resources are retrieved at once.  While the page size does not
affect functionality of the list, it may influence performance.  For
instance, setting the page size to match the number of rows presented
in a GUI or servlet may improve overall response time.  Call
the {@link #setPageSize(int) setPageSize()} method to set a specific
page size.
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
public class BufferedResourceList
extends ResourceList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    // Private data.
    private int                                     numberOfPages_          = 5;
    private int                                     pageSize_               = 20;

    private transient PagedListBuffer               buffer_                 = null;


/**
Constructs a BufferedResourceList object.
**/
    public BufferedResourceList()
    {
        super();
        initializeTransient();
    }



/**
Constructs a BufferedResourceList object.

@param presentation         The presentation.
@param attributeMetaData    The attribute meta data, or null if not applicable.
@param selectionMetaData    The selection meta data, or null if not applicable.
@param sortMetaData         The sort meta data, or null if not applicable.
**/
    public BufferedResourceList(Presentation presentation,
                                ResourceMetaData[] attributeMetaData,
                                ResourceMetaData[] selectionMetaData,
                                ResourceMetaData[] sortMetaData)
    {
        super(presentation, attributeMetaData, selectionMetaData, sortMetaData);
        initializeTransient();
    }




/**
Constructs a BufferedResourceList object.

@param presentation         The presentation, or null if not applicable.
@param attributes           The attributes, or null if not applicable.
@param selections           The selections, or null if not applicable.
@param sorts                The sorts, or null if not applicable.
**/
//
// Design note:  This method is not public, since it exposes ResourceMetaDataTable,
//               which is not a public class.  This is intended as a "back-door"
//               just for use by the BufferedResourceList class.
//
    BufferedResourceList(Presentation presentation,
                         ResourceMetaDataTable attributes,
                         ResourceMetaDataTable selections,
                         ResourceMetaDataTable sorts)
    {
        super(presentation, attributes, selections, sorts);
        initializeTransient();
    }



/**
Closes the list.  No further resources can be loaded.   The list
must be closed in order to clean up resources appropriately.
This method has no effect if the list is already closed.
This method fires a listClosed() ResourceListEvent.

@exception ResourceException                If an error occurs.
**/
     public void close()
    throws ResourceException
    {
        super.close();
        buffer_.clear();
    }



/**
Fires a resourceAdded() ResourceListEvent.

@param resource The resource.
@param index    The index.
**/
    protected void fireResourceAdded(Resource resource, long index)
    {
        synchronized(this) {
            buffer_.setResource(index, resource);
        }

        super.fireResourceAdded(resource, index);
    }



/**
Returns the number of pages in the list buffer.

@return The number of pages in the list buffer.
**/
    public int getNumberOfPages()
    {
        return numberOfPages_;
    }



/**
Returns the page size of the list buffer.

@return The page size, in number of resources, of the list buffer.
**/
    public int getPageSize()
    {
        return pageSize_;
    }



/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
    }



/**
Indicates if the resource is available.  This means that the
resource has been loaded.

@param index    The index.
@return         true if the resource is available,
                false if the resource is not available
                or the list is not open.

@exception ResourceException                If an error occurs.
**/
    public boolean isResourceAvailable(long index)
    throws ResourceException
    {
        // First check to see if the resource is already buffered in the superclass.
        boolean isAvailable = super.isResourceAvailable(index);

        // If the resource already exists in our buffer,
        // then it is certainly available.
        return (isAvailable && (buffer_.getResource(index) != null));
    }




/**
Opens the list.  The list must be open in order to
perform most operations.  This method has no effect
if the list is already opened.

@exception ResourceException                If an error occurs.
**/
     public void open()
    throws ResourceException
    {
        if (isOpen())
            return;

        super.open();

        synchronized(this) {
            buffer_ = new PagedListBuffer(numberOfPages_, pageSize_);
        }
    }



/**
Deserializes the resource list.
**/
    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient ();
    }



/**
Refreshes the contents of the list.

<p>This will implicitly open the list if needed.

@exception ResourceException                If an error occurs.
**/
     public void refreshContents()
    throws ResourceException
    {
        if (isOpen())
            buffer_.clear();
        super.refreshContents();
    }



/**
Returns the resource specified by the index.

<p>This will implicitly open the list if needed.

@param  index   The index.
@return         The resource specified by the index, or null
                if the resource is not yet available.

@exception ResourceException                If an error occurs.
**/
     public Resource resourceAt(long index)
    throws ResourceException
    {
        synchronized(this) {

            // It may already be here.
            Resource resource = super.resourceAt(index);

            // If not, try to load it.
            if (resource == null) {

                synchronized(this) {
                    return buffer_.getResource(index);
                }

            }

            return resource;
        }
    }



/**
Sets the number of pages in the list buffer.  This cannot
be set when the list is open.

@param numberOfPages The number of pages.
**/
    public void setNumberOfPages(int numberOfPages)
    {
        if (isOpen())
            throw new ExtendedIllegalStateException("open", ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
        if (numberOfPages <= 0)
            throw new ExtendedIllegalArgumentException("numberOfPages", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        numberOfPages_ = numberOfPages;
    }



/**
Sets the page size of the list buffer.  This cannot
be set when the list is open.

@param pageSize The page size, in number of resources.
**/
    public void setPageSize(int pageSize)
    {
        if (isOpen())
            throw new ExtendedIllegalStateException("open", ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
        if (pageSize <= 0)
            throw new ExtendedIllegalArgumentException("pageSize", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        pageSize_ = pageSize;
    }




}
