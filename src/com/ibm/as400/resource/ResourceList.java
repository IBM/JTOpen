///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400BidiTransform;
import com.ibm.as400.access.BidiStringType;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/**
The ResourceList class represents a list of AS/400 resources.  This
is an abstract class which provides generic access to the list's
contents.

<p>A ResourceList is always either open or closed.  The ResourceList
must be open in order to access its contents.  Call <a href="#open()">
open()</a> to explicitly open the ResourceList.  Otherwise, most
access methods will implicitly open the ResourceList if needed.
When you are finished using the ResourceList, call
<a href="#close()">close()</a> to ensure that it cleans up system
resources as needed.

<p>The contents of a ResourceList are 0 or more
{@link com.ibm.as400.resource.Resource Resource} objects.
Use <a href="#resourceAt(long)">resourceAt()</a> to access a specific
Resource from the list.  All indices are 0-based.

<p>In order to provide immediate access to the ResourceList's
contents and manage memory efficiently, most ResourceLists are
loaded incrementally.  This means that neither all of the contents
nor the exact <a href="#getListLength()">length</a> is available when the ResourceList
is first opened.  Some subclasses may load the contents on demand,
others may load them asynchronously.  At some point, depending on
the subclass implementation, the ResourceList will be
<a href="#isComplete()">complete</a>.  This means that all of its contents
are available and the exact length is known.

<p>Call <a href="#waitForResource(long)">waitForResource()</a> to ensure that a
particular Resource is made available.  Call <a href="#waitForComplete()">
waitForComplete()</a> to ensure that all of the contents and the exact length
are made available.

<p>ResourceLists can be filtered using selection values.  Every selection
value is identified using a selection ID.  Similarly, ResourceLists can be
sorted using sort values.  Every sort value is identified using a sort ID.
Any given subclass of ResourceList will normally document the selection IDs
and sort IDs that it supports.

<p>One example of a concrete subclass of ResourceList is
<a href="RJobList.html">RJobList</a>, which
represents a list of AS/400 jobs.  RJobList supports many
<a href="RJobList.html#selectionIDs">selection
IDs</a> and <a href="RJobList.html#sortIDs">sort
IDs</a>, each of which can be used to filter or sort the list.
Here is an example which prints the contents of an RJobList:

<blockquote><pre>
// Create an RJobList object to represent a list of jobs.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RJobList jobList = new RJobList(system);
<br>
// Filter the list to include only interactive jobs.
jobList.setSelectionValue(RJobList.JOB_TYPE, RJob.JOB_TYPE_INTERACTIVE);
<br>
// Sort the list by user name, then job name.
Object[] sortValue = new Object[] { RJob.USER_NAME, RJob.JOB_NAME };
jobList.setSortValue(sortValue);
<br>
// Open the list and wait for it to complete.
jobList.open();
jobList.waitForComplete();
<br>
// Read and print the contents of the list.
long length = jobList.getListLength();
for(long i = 0; i &lt; length; ++i)
{
    System.out.println(jobList.resourceAt(i));
}
<br>
// Close the list.
jobList.close();
</pre></blockquote>

<p>In addition to using concrete subclasses directly, you
can write generic code to work with any ResourceList subclass.
Such code may improve reusability and maintainability and
will work with future ResourceList subclasses without
modification.  Here is an example of generic code which
prints the some of the contents of a ResourceList:

<blockquote><pre>
void printContents(ResourceList resourceList, long numberOfItems) throws ResourceException
{
    // Open the list and wait for the requested number of items
    // to become available.
    resourceList.open();
    resourceList.waitForResource(numberOfItems);
    <br>
    for(long i = 0; i &lt; numberOfItems; ++i)
    {
        System.out.println(resourceList.resourceAt(i));
    }
}
</pre></blockquote>

<p>Every selection, sort, and resource attribute has an
associated <a href="ResourceMetaData.html">meta data</a>
object which describes various properties such as the
default value and possible values.   In addition, every ResourceList and
meta data object has an associated <a href="Presentation.html">
Presentation</a> object which provides translated information about the ResourceList,
selection, sort, or attribute.  You can use the Presentation information to
present this information to end users.  This example prints a ResourceList
and its sort values using their Presentations:

<blockquote><pre>
void printCurrentSort(ResourceList resourceList) throws ResourceException
{
    // Get the presentation for the ResourceList and print its full name.
    Presentation resourceListPresentation = resourceList.getPresentation();
    System.out.println(resourceListPresentation.getFullName());
    <br>
    // Get the current sort value.
    Object[] sortIDs = resourceList.getSortValue();
    <br>
    // Print each sort ID.
    for(int i = 0; i &lt; sortIDs.length; ++i)
    {
        ResourceMetaData sortMetaData = resourceList.getSortMetaData(sortIDs[i]);
        System.out.println("Sorting by " + sortMetaData.getName());
    }
}
</pre></blockquote>

<p>Use {@link com.ibm.as400.vaccess.ResourceListDetailsPane ResourceListDetailsPane }
or {@link com.ibm.as400.vaccess.ResourceListPane ResourceListPane }
to present a ResourceList in a graphical user interface.  Use
{@link com.ibm.as400.util.servlet.ResourceListRowData ResourceListRowData }
to present a ResourceList in a servlet.

<a name="subclass"><p><b>Subclass notes:</b></a>
<p>If you are extending this class to override the mechanism for getting
selection values, consider whether you need to support bidirectional
character conversion.  If you do not plan to support bidirectional character
conversion, then you only need to override 
{@link #getSelectionValue(java.lang.Object) getSelectionValue(Object)}.
If you do plan to support bidirectional character conversion, then you need
to override {@link #isBidiEnabled() isBidiEnabled()} to return true
and {@link #getSelectionValue(java.lang.Object, int) getSelectionValue(Object, int)}.

<p>In either case, the overriding method should call the superclass's
method of the same name and perform extra processing only when null
is returned:

<blockquote><pre>
    public Object getSelectionValue(Object selectionID)
    throws ResourceException
    {
        // Call the superclass first.
        Object value = super.getSelectionValue(selectionID);
        if (value == null) {
<br>
            // Establish the connection if needed.
            if (! isConnectionEstablished())
                establishConnection();
<br>                       
            // Go get the selection value.
            value = ...; 
        }
        return value;
    }
</pre></blockquote>

<p>Extending this class to override the mechanism for setting
selection values works in a similar fashion. If you do not plan to support bidirectional character
conversion, then you only need to override 
{@link #setSelectionValue(java.lang.Object, java.lang.Object) setSelectionValue(Object, Object)}.
If you do plan to support bidirectional character conversion, then you need
to override {@link #isBidiEnabled() isBidiEnabled()} to return true
and {@link #setSelectionValue(java.lang.Object, java.lang.Object, int) setSelectionValue(Object, Object, int)}.

<p>Again, the overriding method should call the superclass's
method of the same name and then perform extra processing:

<blockquote><pre>
    public void setSelectionValue(Object selectionID, Object value)
    throws ResourceException
    {
        // Call the superclass first.
        super.setSelectionValue(selectionID, values);
<br>
        // Establish the connection if needed.
        if (! isConnectionEstablished())
            establishConnection();
<br>
        // Set the selection value.
        // ...
    }
</pre></blockquote>

@see Resource
**/
public abstract class ResourceList
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private ResourceMetaDataTable                   attributes_;
    private String                                  currentLevel_           = null;
    private int                                     defaultBidiStringType_  = -1;           // @A2A
    private Presentation                            presentation_;
    private ResourceMetaDataTable                   selections_;
    private Hashtable                               selectionValues_        = new Hashtable();
    private ResourceMetaDataTable                   sorts_;
    private Hashtable                               sortOrders_             = new Hashtable();
    private Object[]                                sortValue_              = new Object[0];
    private AS400                                   system_                 = null;

    private transient boolean                       complete_               = false;
    private transient boolean                       connectionEstablished_  = false;
    private transient boolean                       inError_                = false;
    private transient long                          length_                 = -1;
    private transient boolean                       open_                   = false;
    private transient boolean                       propertiesFrozen_       = false;

    private transient Vector                        activeStatusListeners_;
    private transient PropertyChangeSupport         propertyChangeSupport_;
    private transient Vector                        resourceListListeners_;
    private transient VetoableChangeSupport         vetoableChangeSupport_;



/**
Constructs a ResourceList object.
**/
    public ResourceList()
    {
        presentation_   = new Presentation();
        attributes_     = new ResourceMetaDataTable();
        selections_     = new ResourceMetaDataTable();
        sorts_          = new ResourceMetaDataTable();

        initializeTransient();
    }



/**
Constructs a ResourceList object.

@param presentation         The presentation.
@param attributeMetaData    The attribute meta data, or null if not applicable.
@param selectionMetaData    The selection meta data, or null if not applicable.
@param sortMetaData         The sort meta data, or null if not applicable.
**/
    public ResourceList(Presentation presentation,
                        ResourceMetaData[] attributeMetaData,
                        ResourceMetaData[] selectionMetaData,
                        ResourceMetaData[] sortMetaData)
    {
        if (presentation == null)
            throw new NullPointerException("presentation");

        presentation_   = presentation;
        attributes_     = new ResourceMetaDataTable(attributeMetaData);
        selections_     = new ResourceMetaDataTable(selectionMetaData);
        sorts_          = new ResourceMetaDataTable(sortMetaData);

        initializeTransient();
    }



/**
Constructs a ResourceList object.

@param presentation         The presentation.
@param attributes           The attribute meta data, or null if not applicable.
@param selections           The selection meta data, or null if not applicable.
@param sorts                The sort meta data, or null if not applicable.
**/
//
// Design note:  This method is not public, since it exposes ResourceMetaDataTable,
//               which is not a public class.  This is intended as a "back-door"
//               just for use by the BufferedResourceList class.
//
    ResourceList(Presentation presentation,
                 ResourceMetaDataTable attributes,
                 ResourceMetaDataTable selections,
                 ResourceMetaDataTable sorts)
    {
        if (presentation == null)
            throw new NullPointerException("presentation");

        presentation_   = presentation;
        attributes_     = (attributes == null) ? new ResourceMetaDataTable() : attributes;
        selections_     = (selections == null) ? new ResourceMetaDataTable() : selections;
        sorts_          = (sorts == null) ? new ResourceMetaDataTable() : sorts;

        initializeTransient();
    }



/**
Adds an ActiveStatusListener.

@param listener The listener.
**/
    public void addActiveStatusListener(ActiveStatusListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        activeStatusListeners_.addElement(listener);
    }



/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
*/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        propertyChangeSupport_.addPropertyChangeListener(listener);
    }



/**
Adds a ResourceListListener.

@param listener The listener.
*/
    public void addResourceListListener(ResourceListListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        resourceListListeners_.addElement(listener);
    }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
<b>vetoableChange()</b> method will be called each time the value of
any constrained property is changed.

@param listener The listener.
*/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        vetoableChangeSupport_.addVetoableChangeListener(listener);
    }



/**
Indicates if properties are frozen.  If this is true, property
changes should not be made.  Properties are not the same thing
as attributes.  Properties are basic pieces of information
which must be set to make the object usable, such as the system
or other properties that identify the resource on the system.

@return     true if properties are frozen, false otherwise.
**/
    protected boolean arePropertiesFrozen()
    {
        return propertiesFrozen_;
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
        if (open_) {
            synchronized(this) {
                complete_   = false;
                length_     = -1;
                open_       = false;
                inError_    = false;
            }

            fireListClosed();
        }
    }



/**
Establishes the connection to the AS/400, if any.  Subclasses can override
this method and put all connection initialization code here.
It is assumed that all properties have been set when this
method is called.  Any subclass that overrides this method
should include a call to super.establishConnection().

@exception ResourceException                If an error occurs.
**/
    protected void establishConnection()
    throws ResourceException
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Establishing a connection for " + this + ".");

        // Validate if we can establish the connection.
        if (system_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (!propertiesFrozen_)
            freezeProperties();
        connectionEstablished_ = true;
    }




/**
Closes the list when the list is garbage collected.

@exception Throwable                If an error occurs.
**/
/* @A1D - Removed this because it causes problems when the AS400 object
          gets disconnected or swapped before this object gets garbage
          collector.  We leave it to the caller to call close().
          
    protected void finalize()
        throws Throwable
    {
        super.finalize();
        close();
    }
*/    



/**
Fires a busy active status event.  This indicates that a potentially
long-running operation has started.
**/
    protected void fireBusy()
    {
        ActiveStatusEvent event = new ActiveStatusEvent(this, ActiveStatusEvent.BUSY);
        Vector temp = (Vector)activeStatusListeners_.clone();
        Enumeration enum = temp.elements();
        while(enum.hasMoreElements())
            ((ActiveStatusListener)enum.nextElement()).busy(event);
    }


/**
Fires a idle active status event.  This indicates that a potentially
long-running operation has ended.
**/
    protected void fireIdle()
    {
        ActiveStatusEvent event = new ActiveStatusEvent(this, ActiveStatusEvent.IDLE);
        Vector temp = (Vector)activeStatusListeners_.clone();
        Enumeration enum = temp.elements();
        while(enum.hasMoreElements())
            ((ActiveStatusListener)enum.nextElement()).idle(event);
    }


/**
Fires a lengthChanged() resource list event.

@param length   The length.
**/
    protected void fireLengthChanged(long length)
    {
        // Take note of the change.
        length_ = length;

        // Fire the event.
        ResourceListEvent event = new ResourceListEvent(this, ResourceListEvent.LENGTH_CHANGED, length);
        Vector temp = (Vector)resourceListListeners_.clone();
        Enumeration enum = temp.elements();
        while(enum.hasMoreElements())
            ((ResourceListListener)enum.nextElement()).lengthChanged(event);
    }



/**
Fires a listClosed() ResourceListEvent.
**/
    protected void fireListClosed()
    {
        ResourceListEvent event = new ResourceListEvent(this, ResourceListEvent.LIST_CLOSED);
        Vector temp = (Vector)resourceListListeners_.clone();
        Enumeration enum = temp.elements();
        while(enum.hasMoreElements())
            ((ResourceListListener)enum.nextElement()).listClosed(event);
    }



/**
Fires a listCompleted() ResourceListEvent.
**/
    protected void fireListCompleted()
    {
        // Take note of the completion.
        complete_ = true;

        // Fire the event.
        ResourceListEvent event = new ResourceListEvent(this, ResourceListEvent.LIST_COMPLETED);
        Vector temp = (Vector)resourceListListeners_.clone();
        Enumeration enum = temp.elements();
        while(enum.hasMoreElements())
            ((ResourceListListener)enum.nextElement()).listCompleted(event);
    }



/**
Fires a listInError() ResourceListEvent.
**/
    protected void fireListInError()
    {
        // Take note.
        inError_ = true;

        // Fire the event.
        ResourceListEvent event = new ResourceListEvent(this, ResourceListEvent.LIST_IN_ERROR);
        Vector temp = (Vector)resourceListListeners_.clone();
        Enumeration enum = temp.elements();
        while(enum.hasMoreElements())
            ((ResourceListListener)enum.nextElement()).listInError(event);
    }



/**
Fires a listOpened() ResourceListEvent.
**/
    protected void fireListOpened()
    {
        ResourceListEvent event = new ResourceListEvent(this, ResourceListEvent.LIST_OPENED);
        Vector temp = (Vector)resourceListListeners_.clone();
        Enumeration enum = temp.elements();
        while(enum.hasMoreElements())
            ((ResourceListListener)enum.nextElement()).listOpened(event);
    }



/**
Fires a property change event.

@param propertyName     The property name.
@param oldValue         The old value.
@param newValue         The new value.
**/
    protected void firePropertyChange(String propertyName,
                            Object oldValue,
                            Object newValue)
    {
        propertyChangeSupport_.firePropertyChange(propertyName, oldValue, newValue);
    }




/**
Fires a resourceAdded() ResourceListEvent.

@param resource The resource.
@param index    The index.
**/
    protected void fireResourceAdded(Resource resource, long index)
    {
        // Add the new resource to the buffer.
        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Resource " + resource + " (index=" + index + ") loaded by list " + this + ".");

        // Fire the event.
        ResourceListEvent event = new ResourceListEvent(this, ResourceListEvent.RESOURCE_ADDED, resource, index);
        Vector temp = (Vector)resourceListListeners_.clone();
        Enumeration enum = temp.elements();
        while(enum.hasMoreElements())
            ((ResourceListListener)enum.nextElement()).resourceAdded(event);
    }



/**
Fires a vetoable change event.

@param propertyName     The property name.
@param oldValue         The old value.
@param newValue         The new value.

@exception PropertyVetoException    If the property change is vetoed.
**/
    protected void fireVetoableChange(String propertyName,
                            Object oldValue,
                            Object newValue)
    throws PropertyVetoException
    {
        vetoableChangeSupport_.fireVetoableChange(propertyName, oldValue, newValue);
    }



/**
Freezes any property changes.  After this is called, property
changes should not be made.  Properties are not the same thing
as attributes.  Properties are basic pieces of information
which must be set to make the object usable, such as the system
or other properties that identify the resource on the system.

<p>Subclasses can override this method and put initialization
code here that is dependent on properties being set.
Any subclass that overrides this method should include a
call to super.freezeProperties().

@exception ResourceException                If an error occurs.
**/
//
// Implementation notes:
//
// 1.  This is separate from establishConnection() because
//     there are times when we want to freeze the properties
//     without incurring the performance hit of setting up
//     the connection.
//
    protected void freezeProperties()
    throws ResourceException
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Freezing properties for " + this + ".");

        propertiesFrozen_ = true;
    }



/**
Returns the attribute meta data for a specific attribute of a
resource in the contents of the list.

@param attributeID  Identifies the attribute.
@return             The attribute meta data.
**/
     public ResourceMetaData getAttributeMetaData(Object attributeID)
    {
        return attributes_.getMetaData(attributeID);
    }



/**
Returns the attribute meta data for the resources in the contents
of the list.  If there is more than one type of resource in the list,
this returns the union of all resources' attributes.  The array
will contain an element for every supported attribute.

@return The attribute meta data.  The array has zero elements if
        there are no attributes.
**/
     public ResourceMetaData[] getAttributeMetaData()
    {
        return attributes_.getMetaData(getCurrentLevel());
    }



/**
Returns the current level.  This is the system VRM, or
"" if the system has not been set.

@return The current level.
**/
    String getCurrentLevel()
    {
        if (system_ == null)
            return "";
        if (currentLevel_ != null)
            return currentLevel_;

        try {
            currentLevel_ = ResourceLevel.vrmToLevel(system_.getVRM());
        }
        catch(Exception e) {
            return "";
        }
        return currentLevel_;
    }



// @A2A
    int getDefaultBidiStringType()
    {
        if (system_ == null)
            return BidiStringType.DEFAULT;
        if (defaultBidiStringType_ == -1)
            defaultBidiStringType_ = AS400BidiTransform.getStringType((char)system_.getCcsid());
        return defaultBidiStringType_;
    }



/**
Returns the total number of resources in the list.  This length
reflects the most accurate estimate known.  In many cases the
length is not known until the list is completely loaded.

<p>This will implicitly open the list if needed.

@return The total number of resources in the list.

@exception ResourceException                If an error occurs.
**/
//
// Design note:
//
// * I would have rather named this getLength(), but some previously
//   existing components being retrofitted into this framework already
//   declared a getLength() method that returns an int.  Replacing that
//   with this would cause customers to get compile errors in existing
//   code.
//
     public long getListLength()
        throws ResourceException
    {
        if (! open_)
            open();

        return length_;
    }



/**
Returns the presentation information.

@return The presentation information.
**/
    public Presentation getPresentation()
    {
        return presentation_;
    }



/**
Returns the selection meta data for a specific selection.

@param selectionID  Identifies the selection.
@return             The selection meta data.
**/
     public ResourceMetaData getSelectionMetaData(Object selectionID)
    {
        return selections_.getMetaData(selectionID);
    }



/**
Returns the selection meta data.  The array will contain an element
for every supported selection.

@return The selection meta data.  The array has zero elements if
        there are no selections.
**/
     public ResourceMetaData[] getSelectionMetaData()
    {
        return selections_.getMetaData(getCurrentLevel());
    }



// @A2C
/**
Returns the current value of a selection.

@param selectionID  Identifies the selection.
@return             The selection value, or null if the selection
                    value has not been set.

@exception ResourceException                If an error occurs.

@see <a href="#subclass">Subclass notes</a>
**/
    public Object getSelectionValue(Object selectionID)
        throws ResourceException
    {
        // If this resource is bidi enabled, then punt to the bidi flavor
        // of getSelectionValue().  This will ensure that the subclass's
        // bidi-enabled getSelectionValue() method gets called even though
        // no string type was passed - - and therefore the subclass only
        // needs to override the bidi-enabled getSelectionValue().
        if (isBidiEnabled())
            return getSelectionValue(selectionID, getDefaultBidiStringType());
        else
            return getSelectionValueImplementation(selectionID);
    }



// @A2A
/**
Returns the current value of a selection.

@param selectionID      Identifies the selection.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
@return                 The selection value, or null if the selection
                        value has not been set.

@exception ResourceException                If an error occurs.

@see <a href="#subclass">Subclass notes</a>
**/
    public Object getSelectionValue(Object selectionID, int bidiStringType)
        throws ResourceException
    {
        // If this resource is not bidi enabled, then punt to the non-bidi flavor
        // of getSelectionValue().  This will ensure that the subclass's
        // non-bidi-enabled getSelectionValue() method gets called even though
        // a string type was passed - - and therefore the subclass only
        // needs to override the non-bidi-enabled getSelectionValue().
        if (!isBidiEnabled())
            return getSelectionValue(selectionID);
        else {
            // In the default implementation, we don't use the string type,
            // since there is no conversion being done.
            return getSelectionValueImplementation(selectionID);
        }
    }



// @A2A
    private Object getSelectionValueImplementation(Object selectionID)
        throws ResourceException
    {
        selections_.validateID(selectionID);
        Object value = selectionValues_.get(selectionID);
        if (value == null)
            value = selections_.getMetaData(selectionID).getDefaultValue();
        return value;
    }



/**
Returns the sort meta data for a specific sort.

@param sortID       Identifies the sort.
@return             The sort meta data.
**/
     public ResourceMetaData getSortMetaData(Object sortID)
    {
        return sorts_.getMetaData(sortID);
    }



/**
Returns the sort meta data.  The array will contain an element
for every supported sort.

@return The sort meta data.  The array has zero elements if
        there are no sorts.
**/
     public ResourceMetaData[] getSortMetaData()
    {
        return sorts_.getMetaData(getCurrentLevel());
    }



/**
Returns the current order for a particular sort.

@param sortID       The sort ID.
@return             true for ascending, false for descending.

@exception ResourceException                If an error occurs.
**/
    public boolean getSortOrder(Object sortID)
        throws ResourceException
    {
        sorts_.validateID(sortID);
        if (sortOrders_.containsKey(sortID))
            return ((Boolean)sortOrders_.get(sortID)).booleanValue();
        else
            return true;
    }



/**
Returns the current value of the sort.

@return             The array of sort IDs.

@exception ResourceException                If an error occurs.
**/
    public Object[] getSortValue()
        throws ResourceException
    {
        return sortValue_;
    }



/**
Returns the system.

@return The system.
**/
    public AS400 getSystem()
    {
        return system_;
    }



/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
        activeStatusListeners_      = new Vector();
        propertyChangeSupport_      = new PropertyChangeSupport(this);
        resourceListListeners_      = new Vector();
        vetoableChangeSupport_      = new VetoableChangeSupport(this);
    }



// @A2A
/**
Indicates if this resource is enabled for bidirectional character conversion.
The default implementation always returns false.  Subclasses that are enabled
for bidirectional character conversion should override this method to return
true.

@return Always false.

@see <a href="#subclass">Subclass notes</a>
**/
    protected boolean isBidiEnabled()
    {
        return false;
    }



/**
Indicates if the list is completely loaded.  A list is not considered to
be complete until all of its resources are loaded.  Implementations will
differ as to when the list is completely loaded.  For example, some
implementations may load all resources in a background thread, others
may load resources on demand.

@return true if the list is completely loaded, false if it is not
        completely loaded, or if the list is not open.
**/
     public boolean isComplete()
    {
        return complete_;
    }


/**
Indicates if a connection to the AS/400 is established.  This means that the
resource is in a state where certain properties can no longer be
changed.

@return     true if a connection is established, false otherwise.
**/
    protected boolean isConnectionEstablished()
    {
        return connectionEstablished_;
    }



/**
Indicates if the list has not been completely loaded due
to an error.  If an unrecoverable error occurs while loading
the resources, then the list is in error and no further resources
are loaded.

@return true if the list has not been completely loaded
        due to an error, false if the list is not in error
        or if the list is not open.
**/
    public boolean isInError()
    {
        return inError_;
    }



/**
Indicates if the list is open.

@return true if the list is open, false if the list is not open.
**/
    public boolean isOpen()
    {
        return open_;
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
        if (index < 0)
            throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        // We don't need to check an upper bounds, if they pass too high,
        // we just let it go.  This allows them to just say they want at most 50...
        // even if they don't know the length ahead of time.

        // Otherwise, check with the loader to see
        // if it has loaded that part yet.
        return (index < length_);
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
        if (open_)
            return;

        synchronized(this) {

            complete_   = false;
            inError_    = false;
            open_       = true;
            length_     = 0;

            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "Opening list " + this + ".");

            fireListOpened();
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
        synchronized(this) {
            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "Refreshing list contents for " + this + ".");

            if (!open_)
                open();
        }
    }



/**
Refreshes the status of the list.  The status includes
the length and whether the list is completed or in error.
If the list is complete, this method has no effect.

<p>This method does not refresh the contents of the list.  Use
<a href="#refreshContents()">refreshContents()</a> to refresh
the contents of the list.

<p>This will implicitly open the list if needed.

@exception ResourceException                If an error occurs.
**/
    public void refreshStatus()
        throws ResourceException
    {
        if (! open_)
            open();

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Refreshing status for list " + this + ".");

    }


/**
Removes an ActiveStatusListener.

@param listener The listener.
**/
    public void removeActiveStatusListener(ActiveStatusListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        activeStatusListeners_.removeElement(listener);
    }



/**
Removes a PropertyChangeListener.

@param listener The listener.
*/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        propertyChangeSupport_.removePropertyChangeListener(listener);
    }


/**
Removes a ResourceListListener.

@param listener The listener.
*/
    public void removeResourceListListener(ResourceListListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        resourceListListeners_.removeElement(listener);
    }



/**
Removes a VetoableChangeListener.

@param listener The listener.
*/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        vetoableChangeSupport_.removeVetoableChangeListener(listener);
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
        if (! open_)
            open();
        if ((index < 0) || ((complete_) && (index >= length_)))
            throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        return null;
    }


/**
Returns an Enumeration of the <a href="Resource.html">Resource</a>
objects.  This may be a more convenient mechanism to iterate through
the Resource objects, and is provided as an alternative to using the
other methods in this class.

<p>If the contents of the ResourceList are changed while the
Enumeration is in use, the enumerated Resource objects
may not be consistent.

@return     The Enumeration of Resource objects.

@exception ResourceException                If an error occurs.
**/
    public Enumeration resources()
    throws ResourceException
    {
        return new ResourceListEnumeration(this);
    }



/**
Sets the attribute meta data.

@param attributeMetaData    The attribute meta data.
**/
    protected void setAttributeMetaData(ResourceMetaData[] attributeMetaData)
    {
        if(attributeMetaData == null)
            throw new NullPointerException("attributeMetaData");

        attributes_ = new ResourceMetaDataTable(attributeMetaData);
    }




// @A2C
/**
Sets the current value of a selection.  The changed selection
value will take effect the next time the list is opened
or refreshed.

@param selectionID     Identifies the selection.
@param value        The selection value, or null to remove
                    the selection.

@exception ResourceException                If an error occurs.

@see <a href="#subclass">Subclass notes</a>
**/
    public void setSelectionValue(Object selectionID, Object value)
        throws ResourceException
    {
        // If this resource is bidi enabled, then punt to the bidi flavor
        // of setSelectionValue().  This will ensure that the subclass's
        // bidi-enabled setSelectionValue() method gets called even though
        // no string type was passed - - and therefore the subclass only
        // needs to override the bidi-enabled setSelectionValue().
        if (isBidiEnabled())
            setSelectionValue(selectionID, value, getDefaultBidiStringType());
        else
            setSelectionValueImplementation(selectionID, value);
    }



// @A2A
/**
Sets the current value of a selection.  The changed selection
value will take effect the next time the list is opened
or refreshed.

@param selectionID      Identifies the selection.
@param value            The selection value, or null to remove
                        the selection.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
                        
@exception ResourceException                If an error occurs.

@see <a href="#subclass">Subclass notes</a>
**/
    public void setSelectionValue(Object selectionID, Object value, int bidiStringType)
        throws ResourceException
    {
        // If this resource is not bidi enabled, then punt to the non-bidi flavor
        // of setSelectionValue().  This will ensure that the subclass's
        // non-bidi-enabled setSelectionValue() method gets called even though
        // a string type was passed - - and therefore the subclass only
        // needs to override the non-bidi-enabled setSelectionValue().
        if (!isBidiEnabled())
            setSelectionValue(selectionID, value);
        else {
            // In the default implementation, we don't use the string type,
            // since there is no conversion being done.
            setSelectionValueImplementation(selectionID, value);
        }
    }



    private void setSelectionValueImplementation(Object selectionID, Object value)
        throws ResourceException
    {
        ResourceMetaData selectionMetaData = selections_.validateID(selectionID);
        if (value == null)
            selectionValues_.remove(selectionID);
        else
            selectionValues_.put(selectionID, selectionMetaData.validateValue(value));
    }



/**
Sets the presentation.

@param presentation  The presentation.
**/
    protected void setPresentation(Presentation presentation)
    {
        if(presentation == null)
            throw new NullPointerException("presentation");

        presentation_ = presentation;
    }




/**
Sets the order for a sort.  The changed sort
order will take effect the next time the list is opened
or refreshed.

@param sortID       The sort ID.
@param sortOrder    true for ascending, false for descending.

@exception ResourceException                If an error occurs.
**/
    public void setSortOrder(Object sortID, boolean sortOrder)
        throws ResourceException
    {
        sorts_.validateID(sortID);
        sortOrders_.put(sortID, sortOrder ? Boolean.TRUE : Boolean.FALSE);
    }



/**
Sets the current value of the sort.  The changed sort
value will take effect the next time the list is opened
or refreshed.

@param sortValue      An array of sort IDs.

@exception ResourceException                If an error occurs.
**/
    public void setSortValue(Object[] sortValue)
        throws ResourceException
    {
        if (sortValue == null)
            throw new NullPointerException("sortValue");
        sorts_.validateIDs(sortValue);
        sortValue_ = sortValue;
    }


/**
Sets the system.   This cannot be changed
if the object has established a connection
to the AS/400.

@param  system  The system.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setSystem(AS400 system)
        throws PropertyVetoException
    {
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        if (system == null)
            throw new NullPointerException("system");

        AS400 oldValue = system_;
        fireVetoableChange("system", oldValue, system);
        system_ = system;
        firePropertyChange("system", oldValue, system);
    }




/**
Returns the presentation full name, if any.

@return The presentation full name, if any.
**/
    public String toString()
    {
        if (presentation_ == null)
            return super.toString();
        else {
            String fullName = presentation_.getFullName();
            if (fullName.length() > 0)
                return fullName;
            else
                return super.toString();
        }
    }



/**
Waits until the list is completely loaded.

<p>This will implicitly open the list if needed.

@exception ResourceException                If an error occurs.
**/
     public void waitForComplete()
    throws ResourceException
    {
        if (! open_)
            open();

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Waiting for the list to complete.");
    }



/**
Waits until the resource is available or the list is
complete.

<p>This will implicitly open the list if needed.

@param index    The index.

@exception ResourceException                If an error occurs.
**/
     public void waitForResource(long index)
    throws ResourceException
    {
        if (index < 0)
            throw new ExtendedIllegalArgumentException("index", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        // We don't need to check an upper bounds, if they pass too high,
        // we just let it go.  This allows them to just say they want at most 50...
        // even if they don't know the length ahead of time.

        if (! open_)
            open();

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Waiting for resource " + index + " to become available.");
    }



}
