///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListEnumeration.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.Trace;
import java.util.Enumeration;
import java.util.NoSuchElementException;



/**
The ResourceListEnumeration class enumerates the
{@link com.ibm.as400.resource.Resource Resource} objects in a
{@link com.ibm.as400.resource.ResourceList ResourceList}.
This may be a more convenient mechanism to iterate through
the Resource objects in a ResourceList, and is provided as
an alternative to using the methods defined in ResourceList.

<p>If the contents of the ResourceList are changed while the
ResourceListEnumeration is in use, the enumerated Resource objects
may not be consistent.

<code><pre>
// Create an RUserList object to represent a list of users.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RUserList userList = new RUserList(system);
<br>
// Create a ResourceListEnumeration to iterate through
// the users in the list.
ResourceListEnumeration enum = new ResourceListEnumeration(userList);
while(enum.hasMoreElements())
{
    RUser user = (RUser)enum.nextElement();
    System.out.println(user.getAttributeValue(RUser.USER_PROFILE_NAME);
    System.out.println(user.getAttributeValue(RUser.TEXT_DESCRIPTION);
    System.out.println();
}
</pre></code>
**/
class ResourceListEnumeration
implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private boolean      done_;
    private long         index_;
    private ResourceList list_;



/**
Constructs the ResourceListEnumeration object.

@param list The resource list.

@exception ResourceException    If an error occurs.
**/
    public ResourceListEnumeration(ResourceList list)
    throws ResourceException
    {
        if (list == null)
            throw new NullPointerException("list");

        done_   = false;
        index_  = 0;
        list_   = list;

        // We should not automatically refresh since that is            @A1C
        // expensive.                                                   @A1C
        if (! list_.isOpen())                           // @A1C
            // @A1D list_.refreshContents();
        // @A1D else
            list_.open();
    }



/**
Indicates if there are more elements.

@return true if there are more elements, false otherwise.
**/
    public boolean hasMoreElements()
    {
        if (done_)
            return false;

        try {

            // If the next index is less then the current length, true.
            if (index_ < list_.getListLength())
                return true;

            // If the list is complete... then we are done.
            if (list_.isComplete()) {
                done_ = true;
                return false;
            }

            // Otherwise, lets wait for the index and try again.
            else {
                list_.waitForResource(index_);
                if (index_ < list_.getListLength())
                    return true;
                else {
                    done_ = true;
                    list_.close();
                    return false;
                }
            }
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when enumerating a resource list", e);
            done_ = true;
            return false;
        }
    }



/**
Returns the next {@link com.ibm.as400.resource.Resource Resource}
in the list.

@return The next Resource in the list.
**/
    public Object nextElement()
    {
        if (done_)
            throw new NoSuchElementException();

        try {
            return list_.resourceAt(index_++);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when enumerating a resource list", e);
            done_ = true;
            throw new NoSuchElementException();
        }
    }



}
