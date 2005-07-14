///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ChangeableResource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.Trace;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
The ChangeableResource represents an server resource.
This is an abstract subclass of {@link com.ibm.as400.resource.Resource Resource}
which adds the ability to change attribute values of an server resource.
Attribute changes are cached internally until they are committed
or canceled.  This allows you to change many attribute values at
once.  Every attribute is identified using an attribute ID.  Any given
subclass of ChangeableResource will normally document the attribute IDs
that it supports.

<p>One example of a concrete subclass of ChangeableResource is
{@link com.ibm.as400.resource.RJob RJob}, which represents
a server job.  RJob supports many <a href="{@docRoot}/com/ibm/as400/resource/RJob.html#attributeIDs">attribute IDs</a>,
each of which can be used to access attribute values.
Here is an example which sets two attribute values for an RJob:

<blockquote><pre>
// Create an RJob object to refer to a specific job.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RJob job = new RJob(system, "AJOBNAME", "AUSERID", "AJOBNUMBER");
<br>
// Set the date format attribute value.
job.setAttributeValue(RJob.DATE_FORMAT, RJob.DATE_FORMAT_JULIAN);
<br>
// Set the country ID attribute value.
job.setAttributeValue(RJob.COUNTRY_ID, RJob.USER_PROFILE);
<br>
// Commit both attribute changes.
job.commitAttributeChanges();
</pre></blockquote>

<p>In addition to using concrete subclasses directly, you
can write generic code to work with any ChangeableResource
subclass.  Such code may improve reusability and maintainability
and will work with future ChangeableResource subclasses without
modification.  Every attribute has an associated attribute
{@link com.ibm.as400.resource.ResourceMetaData meta data}
object which describes various properties of the attribute.
These properties include whether or not the attribute is
read only and what the default and possible values are.
Here is an example of generic code which resets all attributes
of a ChangeableResource to their default values:

<blockquote><pre>
void resetAttributeValues(ChangeableResource resource) throws ResourceException
{
    // Get the attribute meta data.
    ResourceMetaData[] attributeMetaData = resource.getAttributeMetaData();
    <br>
    // Loop through all attributes.
    for(int i = 0; i &lt; attributeMetaData.length; ++i)
    {
        // If the attribute is changeable (not read only), then
        // reset its value to the default.
        if (! attributeMetaData[i].isReadOnly())
        {
            Object attributeID = attributeMetaData[i].getID();
            Object defaultValue = attributeMetaData[i].getDefaultValue();
            resource.setAttributeValue(attributeID, defaultValue);
        }
    }
    <br>
    // Commit all of the attribute changes.
    resource.commitAttributeChanges();
}
</pre></blockquote>

<a name="subclass"><p><b>Subclass notes:</b></a>
<p>If you are extending this class to override the mechanism for getting
attribute values, you need to override either 
{@link #getAttributeUnchangedValue(java.lang.Object) getAttributeUnchangedValue(Object)}
or {@link #getAttributeUnchangedValue(java.lang.Object, int) getAttributeUnchangedValue(Object, int)},
but <em>not</em> {@link #getAttributeValue(java.lang.Object) getAttributeValue(Object)}
or {@link #getAttributeValue(java.lang.Object, int) getAttributeValue(Object, int)}.
This is because getAttributeValue() will automatically call getAttributeUnchangedValue()
only when needed.

<p>Consider whether you need to support bidirectional character conversion.  If you do 
not plan to support bidirectional character conversion, then you only need to override 
{@link #getAttributeUnchangedValue(java.lang.Object) getAttributeUnchangedValue(Object)}.
If you do plan to support bidirectional character conversion, then you need
to override {@link #isBidiEnabled() isBidiEnabled()} to return true
and {@link #getAttributeUnchangedValue(java.lang.Object, int) getAttributeUnchangedValue(Object, int)}.

<p>In either case, the overriding method should call the superclass's
method of the same name and perform extra processing only when null
is returned:

<blockquote><pre>
    public Object getAttributeUnchangedValue(Object attributeID)
    throws ResourceException
    {
        // Call the superclass first.
        Object value = super.getAttributeUnchangedValue(attributeID);
        if (value == null) {
<br>
            // Establish the connection if needed.
            if (! isConnectionEstablished())
                establishConnection();
<br>                       
            // Go get the attribute value.
            value = ...; 
        }
        return value;
    }
</pre></blockquote>

<p>If you are extending this class to override the mechanism for setting
attribute values, you need to override either 
{@link #commitAttributeChanges(java.lang.Object[], java.lang.Object[]) commitAttributeChanges(Object[], Object[])}
or {@link #commitAttributeChanges(java.lang.Object[], java.lang.Object[], int[]) commitAttributeChanges(Object[], Object[], int[])},
but <em>not</em> {@link #commitAttributeChanges() commitAttributeChanges()},
{@link #setAttributeValue(java.lang.Object, java.lang.Object) setAttributeValue(Object, Object)}, or
{@link #setAttributeValue(java.lang.Object, java.lang.Object, int) setAttributeValue(Object, Object, int)}.

<p>If you do not plan to support bidirectional character conversion, then you only need 
to override {@link #commitAttributeChanges(java.lang.Object[], java.lang.Object[]) commitAttributeChanges(Object[], Object[])}.
If you do plan to support bidirectional character conversion, then you need
to override {@link #isBidiEnabled() isBidiEnabled()} to return true
and {@link #commitAttributeChanges(java.lang.Object[], java.lang.Object[], int[]) commitAttributeChanges(Object[], Object[], int[])}}.

<p>In either case, the overriding method should call the superclass's
method of the same name and then perform extra processing:

<blockquote><pre>
    protected void commitAttributeChanges(Object[] attributeIDs, Object[] values)
    throws ResourceException
    {
        // Call the superclass first.
        super.commitAttributeChanges(attributeIDs, values);
<br>
        // Establish the connection if needed.
        if (! isConnectionEstablished())
            establishConnection();
<br>
        // Commit the attribute changes.
        // ...
    }
</pre></blockquote>
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
//
// Implementation notes:
//
// 1.  I have chosen to restrict attribute values from being null.
//     This will enable Hashtable implementations to be more
//     straightforward (Hashtables cannot have null elements).
//
// 2.  There was some discussion as to whether to combine this
//     with Resource.  The reasons it is left separate are:
//
//     *  For simple (read-only) resources, their sum of methods
//        remains simple.
//     *  The compiler can enforce the read-only characteristic
//        of some resources.  Combining this class with Resource
//        would leave such checks in run-time.
//
public abstract class ChangeableResource
extends Resource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.

    // This hashtable keeps a buffer of values that have been loaded as a
    // result of a previous call to getAttributeValue().
    // Note: This buffer excludes any uncommitted changes.
    private transient Hashtable                     bufferedValues_;

    // This hashtable keeps a list of values that have been set as a result
    // of a previous call to setAttributeValue() but have not yet been
    // committed.  When the changes are committed, the contents of this
    // hashtable will be moved to bufferedValues_.
    private transient Hashtable                     uncommittedChanges_;
    private transient Hashtable                     uncommittedChangeBidiStringTypes_;      // @A2A

//  private transient Vector                        resourceListeners_; // @A1d - Moved to Resource.



/**
Constructs a ChangeableResource object.
**/
    public ChangeableResource()
    {
        super();
        initializeTransient();
    }



/**
Constructs a ChangeableResource object.

@param presentation         The presentation.
@param resourceKey          The resource key.
@param attributeMetaData    The attribute meta data, or null if not applicable.
**/
    public ChangeableResource(Presentation presentation,
                              Object resourceKey,
                              ResourceMetaData[] attributeMetaData)
    {
        super(presentation, resourceKey, attributeMetaData);
        initializeTransient();
    }



/**
Constructs a ChangeableResource object.

@param presentation         The presentation.
@param resourceKey          The resource key.
@param attributes           The attribute meta data, or null if not applicable.
**/
    public ChangeableResource(Presentation presentation,
                              Object resourceKey,
                              ResourceMetaDataTable attributes)
    {
        super(presentation, resourceKey, attributes);
        initializeTransient();
    }



// @A1d - Moved to Resource.
// /**
// Adds a ResourceListener.
// 
// @param listener The listener.
// **/
//     public void addResourceListener(ResourceListener listener)
//     {
//         if (listener == null)
//             throw new NullPointerException("listener");
// 
//         resourceListeners_.addElement(listener);
//     }



/**
Cancels all uncommitted attribute changes.  This method
fires an attributeChangesCanceled() ResourceEvent.

@exception ResourceException                If an error occurs.
**/
    public void cancelAttributeChanges()
    throws ResourceException
    {
        synchronized(this) {
            uncommittedChanges_.clear();
        }

        fireAttributeChangesCanceled();
    }



/**
Commits all attribute changes.  This method
fires an attributeChangesCommitted() ResourceEvent.

<p>This method gathers information about which attribute
changes need to be committed and calls {@link #commitAttributeChanges(java.lang.Object[], java.lang.Object[]) commitAttributeChanges(Object[], Object[])}.
Subclasses should override
commitAttributeChanges(Object[], Object[]) to define how attribute
changes are physically committed.

@exception ResourceException                If an error occurs.
**/
    public void commitAttributeChanges()
    throws ResourceException
    {
        fireBusy();

        try {

            // Gather the changes.
            int uncommittedChangesCount = uncommittedChanges_.size();
            Object[] attributeIDs = new Object[uncommittedChangesCount];
            Object[] values = new Object[uncommittedChangesCount];
            int[] bidiStringTypes = new int[uncommittedChangesCount];                   // @A2A
            boolean bidiEnabled = isBidiEnabled();                                      // @A2A
            Enumeration list = uncommittedChanges_.keys();
            int i = 0;
            while(list.hasMoreElements()) {
                attributeIDs[i] = list.nextElement();
                values[i] = uncommittedChanges_.get(attributeIDs[i]);
                if (bidiEnabled) {                                                                  // @A2A
                    bidiStringTypes[i] = ((Integer)uncommittedChangeBidiStringTypes_.get(attributeIDs[i])).intValue();    // @A2A
                    if (bidiStringTypes[i] == -1)                                                   // @A2A
                        bidiStringTypes[i] = getDefaultBidiStringType();                            // @A2A
                }                                                                                   // @A2A
                ++i;
            }

            // Validation.
            for(int j = 0; j < attributeIDs.length; ++j) {
                if (attributeIDs[j] == null)
                    throw new NullPointerException("attributeIDs[" + j + "]");
                ResourceMetaData attributeMetaData = validateAttributeID(attributeIDs[j]);
                if (attributeMetaData.isReadOnly()) {
                    Trace.log(Trace.ERROR, "Attempted to set read-only attribute: " + attributeIDs[j]);           // @A3A
                    throw new ResourceException(ResourceException.ATTRIBUTE_READ_ONLY); // @A3C
                }
                if (values[j] == null)
                    throw new NullPointerException("values[" + j + "]");
                values[j] = attributeMetaData.validateValue(values[j]);
            }

            // Physically set the uncommitted changes.
            if (bidiEnabled)                                                        // @A2A
                commitAttributeChanges(attributeIDs, values, bidiStringTypes);      // @A2A
            else                                                                    // @A2A
                commitAttributeChanges(attributeIDs, values);

            // Assuming that went okay, move the uncommitted changes
            // to the buffered values.
            for(int j = 0; j < attributeIDs.length; ++j)
                bufferedValues_.put(attributeIDs[j], values[j]);
            uncommittedChanges_.clear();
            uncommittedChangeBidiStringTypes_.clear();                              // @A2A

            fireAttributeChangesCommitted();
        }
        finally {
            fireIdle();
        }
    }



// @A2C
/**
Commits the specified attribute changes.

<p>Subclasses should override this method to define how attribute
changes are physically committed.

@param attributeIDs     The attribute IDs for the specified attribute changes.
@param values           The specified attribute changes

@exception ResourceException                If an error occurs.

@see <a href="#subclass">Subclass notes</a>
**/
    protected void commitAttributeChanges(Object[] attributeIDs, Object[] values)
    throws ResourceException
    {
        // Do nothing for the default implementation.
    }


// @A2A
/**
Commits the specified attribute changes.

<p>Subclasses should override this method to define how attribute
changes are physically committed.

@param attributeIDs     The attribute IDs for the specified attribute changes.
@param values           The specified attribute changes
@param bidiStringTypes  The bidi string types as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
                        
@exception ResourceException                If an error occurs.

@see <a href="#subclass">Subclass notes</a>
**/
    protected void commitAttributeChanges(Object[] attributeIDs, Object[] values, int[] bidiStringTypes)
    throws ResourceException
    {
        // Do nothing for the default implementation.
    }



/**
Fires an attributeChangesCanceled() ResourceEvent.
**/
    protected void fireAttributeChangesCanceled()
    {
        ResourceEvent event = new ResourceEvent(this, ResourceEvent.ATTRIBUTE_CHANGES_CANCELED);
        Vector temp = (Vector)resourceListeners_.clone();
        Enumeration list = temp.elements();
        while(list.hasMoreElements())
            ((ResourceListener)list.nextElement()).attributeChangesCanceled(event);
    }



/**
Fires an attributeChangesCommitted() ResourceEvent.
**/
    protected void fireAttributeChangesCommitted()
    {
        ResourceEvent event = new ResourceEvent(this, ResourceEvent.ATTRIBUTE_CHANGES_COMMITTED);
        Vector temp = (Vector)resourceListeners_.clone();
        Enumeration list = temp.elements();
        while(list.hasMoreElements())
            ((ResourceListener)list.nextElement()).attributeChangesCommitted(event);
    }



/**
Fires an attributeValueChanged() ResourceEvent.

@param attributeID  Identifies the attribute.
@param value        The attribute value.
**/
    protected void fireAttributeValueChanged(Object attributeID, Object value)
    {
        ResourceEvent event = new ResourceEvent(this, ResourceEvent.ATTRIBUTE_VALUE_CHANGED, attributeID, value);
        Vector temp = (Vector)resourceListeners_.clone();
        Enumeration list = temp.elements();
        while(list.hasMoreElements())
            ((ResourceListener)list.nextElement()).attributeValueChanged(event);
    }



// @A1d - Moved to Resource.
// /**
// Fires an attributeValuesRefreshed() ResourceEvent.
// **/
//     protected void fireAttributeValuesRefreshed()
//     {
//         ResourceEvent event = new ResourceEvent(this, ResourceEvent.ATTRIBUTE_VALUES_REFRESHED);
//         Vector temp = (Vector)resourceListeners_.clone();
//         Enumeration list = temp.elements();
//         while(list.hasMoreElements())
//             ((ResourceListener)list.nextElement()).attributeValuesRefreshed(event);
//     }



/**
Fires an resourceCreated() ResourceEvent.
**/
    protected void fireResourceCreated()
    {
        ResourceEvent event = new ResourceEvent(this, ResourceEvent.RESOURCE_CREATED);
        Vector temp = (Vector)resourceListeners_.clone();
        Enumeration list = temp.elements();
        while(list.hasMoreElements())
            ((ResourceListener)list.nextElement()).resourceCreated(event);
    }



/**
Fires an resourceDeleted() ResourceEvent.
**/
    protected void fireResourceDeleted()
    {
        ResourceEvent event = new ResourceEvent(this, ResourceEvent.RESOURCE_DELETED);
        Vector temp = (Vector)resourceListeners_.clone();
        Enumeration list = temp.elements();
        while(list.hasMoreElements())
            ((ResourceListener)list.nextElement()).resourceDeleted(event);
    }



// @A2C
/**
Returns the value of an attribute, disregarding any uncommitted
changes.

<p>Subclasses should override this method to implement
how attribute values are physically retrieved.

@param attributeID  Identifies the attribute.
@return             The attribute value, or null if the attribute
                    value is not available.

@exception ResourceException                If an error occurs.

@see <a href="#subclass">Subclass notes</a>
**/
    public Object getAttributeUnchangedValue(Object attributeID)
    throws ResourceException
    {
        // If this resource is bidi enabled, then punt to the bidi flavor
        // of getAttributeUnchangedValue().  This will ensure that the subclass's
        // bidi-enabled getAttributeUnchangedValue() method gets called even though
        // no string type was passed - - and therefore the subclass only
        // needs to override the bidi-enabled getAttributeUnchangedValue().
        if (isBidiEnabled())
            return getAttributeUnchangedValue(attributeID, getDefaultBidiStringType());
        else
            return getAttributeUnchangedValueImplementation(attributeID, getDefaultBidiStringType());
    }



// @A2A    
/**
Returns the value of an attribute, disregarding any uncommitted
changes.

<p>Subclasses should override this method to implement
how attribute values are physically retrieved.

@param attributeID      Identifies the attribute.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
@return                 The attribute value, or null if the attribute
                        value is not available.

@exception ResourceException                If an error occurs.

@see <a href="#subclass">Subclass notes</a>
**/
    public Object getAttributeUnchangedValue(Object attributeID, int bidiStringType)
    throws ResourceException
    {
        // If this resource is not bidi enabled, then punt to the non-bidi flavor
        // of getAttributeUnchangedValue().  This will ensure that the subclass's
        // non-bidi-enabled getAttributeUnchangedValue() method gets called even though
        // a string type was passed - - and therefore the subclass only
        // needs to override the non-bidi-enabled getAttributeUnchangedValue().
        if (!isBidiEnabled())
            return getAttributeUnchangedValue(attributeID);
        else 
            return getAttributeUnchangedValueImplementation(attributeID, bidiStringType);
    }



// @A2A
    private Object getAttributeUnchangedValueImplementation(Object attributeID, int bidiStringType)
    throws ResourceException
    {
        validateAttributeID(attributeID);
        synchronized(this) {
            if (bufferedValues_.containsKey(attributeID))
                return bufferedValues_.get(attributeID);
            else
                return super.getAttributeValueImplementation(attributeID);
        }
    }



// @A2C
/**
Returns the current value of an attribute.  If the attribute value
has an uncommitted change, this returns the changed (uncommitted) value.

@param attributeID  Identifies the attribute.
@return             The attribute value, or null if the attribute
                    value is not available.

@exception ResourceException                If an error occurs.

@see <a href="#subclass">Subclass notes</a>
**/
    public Object getAttributeValue(Object attributeID)
    throws ResourceException
    {
        // If this resource is bidi enabled, then punt to the bidi flavor
        // of getAttributeValue().  This will ensure that the subclass's
        // bidi-enabled getAttributeValue() method gets called even though
        // no string type was passed - - and therefore the subclass only
        // needs to override the bidi-enabled getAttributeValue().
        if (isBidiEnabled())
            return getAttributeValue(attributeID, getDefaultBidiStringType());
        else
            return getAttributeValueImplementation(attributeID, getDefaultBidiStringType());
    }



// @A2A
/**
Returns the current value of an attribute.  If the attribute value
has an uncommitted change, this returns the changed (uncommitted) value.

@param attributeID      Identifies the attribute.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
@return                 The attribute value, or null if the attribute
                        value is not available.
                                                                  
@exception ResourceException                If an error occurs.           

@see <a href="#subclass">Subclass notes</a>
**/
    public Object getAttributeValue(Object attributeID, int bidiStringType)
    throws ResourceException
    {                        
        // If this resource is not bidi enabled, then punt to the non-bidi flavor
        // of getAttributeValue().  This will ensure that the subclass's
        // non-bidi-enabled getAttributeValue() method gets called even though
        // a string type was passed - - and therefore the subclass only
        // needs to override the non-bidi-enabled getAttributeValue().
        if (!isBidiEnabled())
            return getAttributeValue(attributeID);
        else 
            return getAttributeValueImplementation(attributeID, bidiStringType);
    }




// @A2A
    private Object getAttributeValueImplementation(Object attributeID, int bidiStringType)
    throws ResourceException
    {
        synchronized(this) {
            if (uncommittedChanges_.containsKey(attributeID))
                return uncommittedChanges_.get(attributeID);
            else
                return getAttributeUnchangedValue(attributeID, bidiStringType);
        }
    }



/**
Indicates if an attribute has an uncommitted change.

@param attributeID  Identifies the attribute.
@return             true if the attribute has a uncommitted change,
                    false otherwise.
**/
    public boolean hasUncommittedAttributeChanges(Object attributeID)
    {
        validateAttributeID(attributeID);
        return uncommittedChanges_.containsKey(attributeID);
    }


// @A4A
/**
Initializes an attribute value.  This is intended for use by the
subclass when it is initializing attribute values.

@param attributeID  Identifies the attribute.
@param value        The attribute value.  This cannot be null.
**/
    protected void initializeAttributeValue(Object attributeID, Object value)
    {
      super.initializeAttributeValue(attributeID, value);
      bufferedValues_.put(attributeID, value);   // @A4A
    }



/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
        uncommittedChanges_         = new Hashtable();
        uncommittedChangeBidiStringTypes_ = new Hashtable();                // @A2A
        bufferedValues_             = new Hashtable();

        //resourceListeners_          = new Vector();  // @A1d - Moved to Resource.
    }



/**
Deserializes the resource.
**/
    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient ();
    }



/**
Refreshes the values for all attributes.  This does not cancel
uncommitted changes.  This method fires an attributeValuesRefreshed()
ResourceEvent.

@exception ResourceException                If an error occurs.
**/
    public void refreshAttributeValues()
    throws ResourceException
    {
        bufferedValues_.clear();
        super.refreshAttributeValues();
        //fireAttributeValuesRefreshed();  // @A1d - Moved to Resource.
    }



// @A1d - Moved to Resource.
 ///**
 //Removes a ResourceListener.
 //
 //@param listener The listener.
 //**/
 //    public void removeResourceListener(ResourceListener listener)
 //    {
 //        if (listener == null)
 //            throw new NullPointerException("listener");
 //
 //        resourceListeners_.removeElement(listener);
 //    }



/**
Sets the current value of an attribute.  The changed value will
be uncommitted until changes are committed.  This method
fires an attributeValueChanged() ResourceEvent.

@param attributeID  Identifies the attribute.
@param value        The attribute value.  This cannot be null.

@exception ResourceException                If an error occurs.
**/
    public void setAttributeValue(Object attributeID, Object value)
    throws ResourceException
    {
        // If this resource is bidi enabled, then punt to the bidi flavor
        // of setAttributeValue().  This will ensure that the subclass's
        // bidi-enabled setAttributeValue() method gets called even though
        // no string type was passed - - and therefore the subclass only
        // needs to override the bidi-enabled setAttributeValue().
        if (isBidiEnabled())
            setAttributeValue(attributeID, value, getDefaultBidiStringType());
        else
            setAttributeValueImplementation(attributeID, value, getDefaultBidiStringType());
    }



// @A2A
/**
Sets the current value of an attribute.  The changed value will
be uncommitted until changes are committed.  This method
fires an attributeValueChanged() ResourceEvent.

@param attributeID  Identifies the attribute.
@param value        The attribute value.  This cannot be null.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 

@exception ResourceException                If an error occurs.
**/
    public void setAttributeValue(Object attributeID, Object value, int bidiStringType)
    throws ResourceException
    {
        // If this resource is not bidi enabled, then punt to the non-bidi flavor
        // of setAttributeValue().  This will ensure that the subclass's
        // non-bidi-enabled setAttributeValue() method gets called even though
        // a string type was passed - - and therefore the subclass only
        // needs to override the non-bidi-enabled setAttributeValue().
        if (!isBidiEnabled())
            setAttributeValue(attributeID, value);
        else {
            // In the default implementation, we don't use the string type,
            // since there is no conversion being done.
            setAttributeValueImplementation(attributeID, value, bidiStringType);
        }
    }



// @A2A
    private void setAttributeValueImplementation(Object attributeID, Object value, int bidiStringType)
    throws ResourceException
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Setting attribute value " + attributeID + " (for "
                      + this + ") to " + value + "(" + value.getClass() + "), bidi string type = "
                      + bidiStringType);                                                        // @A2C

        // Validation.
        ResourceMetaData attributeMetaData = validateAttributeID(attributeID);
        if (attributeMetaData.isReadOnly()) {
            Trace.log(Trace.ERROR, "Attempted to set read-only attribute: " + attributeID); // @A3A
            throw new ResourceException(ResourceException.ATTRIBUTE_READ_ONLY);             // @A3C
        }
        if (value == null)
            throw new NullPointerException("value");
        value = attributeMetaData.validateValue(value);

        // Make the change.
        synchronized(this) {
            uncommittedChanges_.put(attributeID, value);
            uncommittedChangeBidiStringTypes_.put(attributeID, new Integer(bidiStringType));    // @A2A
        }

        // Fire the event.
        fireAttributeValueChanged(attributeID, value);
    }



}
