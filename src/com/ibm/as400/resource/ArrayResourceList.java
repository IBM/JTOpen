///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ArrayResourceList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;



/**
The ArrayResourceList class represents a subclass
of the {@link com.ibm.as400.resource.ResourceList ResourceList}
class which manages a list of resources predefined as an array
of {@link com.ibm.as400.resource.Resource Resource} objects.
**/
public class ArrayResourceList
extends ResourceList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;


    // Private data.
    private Resource[]                              resources_;



/**
Constructs a ArrayResourceList object.

@param resources            The array of Resource objects.
@param presentation         The presentation.
@param attributeMetaData    The attribute meta data, or null if not applicable.
**/
    public ArrayResourceList(Resource[] resources,
                             Presentation presentation,
                             ResourceMetaData[] attributeMetaData)
    {
        super(presentation, attributeMetaData, null, null);
        setArray(resources);
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
        super.open();
        for(int i = 0; i < resources_.length; ++i)
            fireResourceAdded(resources_[i], i);
        fireLengthChanged(resources_.length);
        fireListCompleted();
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
        super.resourceAt(index);
        return resources_[(int)index];
    }



    protected void setArray(Resource[] resources)
    {
        if (resources == null)
            throw new NullPointerException("resources");
        for (int i = 0; i < resources.length; ++i)
            if (resources[i] == null)
                throw new NullPointerException("resources[" + i + "]");

        resources_ = resources;
    }


}
