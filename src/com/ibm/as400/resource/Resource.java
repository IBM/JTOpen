///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Resource.java
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
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
The Resource class represents a system resource.  This is an abstract
class which provides generic access to the resource's attributes.
Every attribute is identified using an attribute ID.  Any given subclass
of Resource will normally document the attribute IDs that it supports.

<p>One example of a concrete subclass of Resource is 
{@link com.ibm.as400.resource.RUser RUser}
which represents a system user.  RUser supports
many <a href="{@docRoot}/com/ibm/as400/resource/RUser.html#attributeIDs">attribute IDs</a>, 
each of which can be used to get
attribute values.  Here is an example which retrieves an attribute
value from an RUser:

<blockquote><pre>
// Create an RUser object to refer to a specific user.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RUser user = new RUser(system, "AUSERID");
<br>
// Get the text description attribute value.
String textDescription = (String)user.getAttributeValue(RUser.TEXT_DESCRIPTION);
</pre></blockquote>

<p>In addition to using concrete subclasses directly, you 
can write generic code to work with any Resource subclass.
Such code may improve reusability and maintainability and will
work with future Resource subclasses without modification.
Every attribute has an associated attribute
{@link com.ibm.as400.resource.ResourceMetaData meta data}
object which describes various properties of the attribute.  
The attribute meta data can come in handy when writing generic
code.  Here is an example of generic code which prints the 
value of every attribute supported by a Resource:

<blockquote><pre>
void printAllAttributeValues(Resource resource) throws ResourceException
{
    // Get the attribute meta data.
    ResourceMetaData[] attributeMetaData = resource.getAttributeMetaData();
    <br>
    // Loop through all attributes and print the values.
    for(int i = 0; i &lt; attributeMetaData.length; ++i)
    {
        Object attributeID = attributeMetaData[i].getID();
        Object value = resource.getAttributeValue(attributeID);
        System.out.println("Attribute " + attributeID + " = " + value);
    }
}
</pre></blockquote>

<p>Every Resource and attribute meta data object has an associated
{@link com.ibm.as400.resource.Presentation Presentation}
object which provides translated information about the Resource
or attribute.  You can use the Presentation information to
present Resources and attributes to end users.  This example
prints a Resource and attribute using their Presentations:

<blockquote><pre>
void printSingleAttributeValue(Resource resource, Object attributeID) throws ResourceException
{
    // Get the presentation for the Resource and print its full name.
    Presentation resourcePresentation = resource.getPresentation();
    System.out.println(resourcePresentation.getFullName());
    <br>
    // Get the attribute meta data and the associated Presention.
    ResourceMetaData attributeMetaData = resource.getAttributeMetaData(attributeID);
    Presentation attributePresentation = attributeMetaData.getPresentation();
    <br>
    // Get the attribute value and print it.
    Object value = resource.getAttributeValue(attributeID);
    System.out.println(attributePresentation.getName() + " = " + value);
}
</pre></blockquote>

<p>The Resource abstract class only provides read access to the attribute
values.  {@link com.ibm.as400.resource.ChangeableResource ChangeableResource},
which is a subclass of Resource, adds methods which provide write access to
the attribute values. 

<a name="subclass"><p><b>Subclass notes:</b></a>
<p>If you are extending this class to override the mechanism for getting
attribute values, consider whether you need to support bidirectional
character conversion.  If you do not plan to support bidirectional character
conversion, then you only need to override 
{@link #getAttributeValue(java.lang.Object) getAttributeValue(Object)}.
If you do plan to support bidirectional character conversion, then you need
to override {@link #isBidiEnabled() isBidiEnabled()} to return true
and {@link #getAttributeValue(java.lang.Object, int) getAttributeValue(Object, int)}.

<p>In either case, the overriding method should call the superclass's
method of the same name and perform extra processing only when null
is returned:

<blockquote><pre>
    public Object getAttributeValue(Object attributeID)
    throws ResourceException
    {
        // Call the superclass first.
        Object value = super.getAttributeValue(attributeID);
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

@see ResourceList
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
public abstract class Resource
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


    static final long serialVersionUID = 4L;



    // Private data.
    private static Object                           counterLock_        = new Object();
    private static long                             counter_;

    private ResourceMetaDataTable                   attributes_;
    private Dictionary                              attributeValues_;
    private boolean                                 connectionEstablished_  = false;
    private String                                  currentLevel_           = null;
    private int                                     defaultBidiStringType_  = -1;           // @A2A
    private Presentation                            presentation_;
    private boolean                                 propertiesFrozen_       = false;
    private Object                                  resourceKey_;
    private AS400                                   system_                 = null;

    private transient Vector                        activeStatusListeners_;
    private transient PropertyChangeSupport         propertyChangeSupport_;
    private transient VetoableChangeSupport         vetoableChangeSupport_;

            transient Vector                        resourceListeners_;  // @A1a


    
/**
Constructs a Resource object.  
**/
    public Resource()
    {
        presentation_               = new Presentation();

        synchronized(counterLock_) {
            resourceKey_            = new Long(counter_++);
        }

        attributes_                 = new ResourceMetaDataTable();
        attributeValues_            = new Hashtable();
        initializeTransient();
    }



/**
Constructs a Resource object.

@param presentation         The presentation.
@param resourceKey          The resource key.
@param attributeMetaData    The attribute meta data, or null if not applicable.
**/
    public Resource(Presentation presentation, 
                    Object resourceKey,
                    ResourceMetaData[] attributeMetaData)
    {
        if(presentation == null)
            throw new NullPointerException("presentation");
        if(resourceKey == null)
            throw new NullPointerException("resourceKey");

        presentation_               = presentation;  
        resourceKey_                = resourceKey;
        attributes_                 = new ResourceMetaDataTable(attributeMetaData);
        attributeValues_            = new Hashtable();
        initializeTransient();
    }



/**
Constructs a Resource object.

@param presentation         The presentation.
@param resourceKey          The resource key.
@param attributeMetaData    The attribute meta data, or null if not applicable.
@param values               The attribute values.  The keys are attribute IDs and
                            the elements are attribute values.  The attribute IDs
                            and values must be consistent according to the attribute
                            meta data.
**/
    public Resource(Presentation presentation, 
                    Object resourceKey,
                    ResourceMetaData[] attributeMetaData,
                    Dictionary values)
    {
        this(presentation, resourceKey, attributeMetaData);
        
        // Validate the attribute values.
        synchronized(values) {
            Enumeration keys = values.keys();
            while(keys.hasMoreElements()) {
                Object attributeID = keys.nextElement();
                Object value = values.get(attributeID);

                if (Trace.isTraceOn())
                    Trace.log(Trace.DIAGNOSTIC, "Initializing attribute value " + attributeID + " (for "
                              + this + ") to " + value + "(" + value.getClass() + ")");

                ResourceMetaData singleAttributeMetaData = attributes_.validateID(attributeID);        
                values.put(attributeID, singleAttributeMetaData.validateValue(value));
            }
        }

        attributeValues_ = values;
        initializeTransient();
    }



/**
Constructs a Resource object.

@param presentation         The presentation.
@param resourceKey          The resource key, or null if it is not set.
@param attributes           The attribute meta data.
**/
    public Resource(Presentation presentation, 
                    Object resourceKey,
                    ResourceMetaDataTable attributes)
    {
        if(presentation == null)
            throw new NullPointerException("presentation");
        if(attributes == null)
            throw new NullPointerException("attributes");

        presentation_               = presentation;  
        resourceKey_                = resourceKey;
        attributes_                 = attributes;
        attributeValues_            = new Hashtable();
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



// @A1a - Formerly in ChangeableResource.
/**
Adds a ResourceListener.

@param listener The listener.
**/
    public void addResourceListener(ResourceListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        resourceListeners_.addElement(listener);
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
Indicates if this resource is equal to an object.

@param  other   The object.                                                 
@return         true if this resource is equal to the object, 
                false otherwise.
**/
    public boolean equals(Object other)
    {
        if (other instanceof Resource) {

            // If the resource keys are set, then use them to compare.
            if ((resourceKey_ != null) && (((Resource)other).resourceKey_ != null))
                return resourceKey_.equals(((Resource)other).resourceKey_);

            // Otherwise, just use the default.
            else
                return super.equals(other);
        }
        else
            return false;
    }



/**
Establishes the connection to the system, if any.  Subclasses can override
this method and put all connection initialization code here.
It is assumed that all properties have been set when this
method is called.  Any subclass that overrides this method
should include a call to super.establishConnection().

@exception ResourceException                If an error occurs.           
**/
    protected void establishConnection()
    throws ResourceException
    {
        if (system_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Establishing a connection for " + this + ".");

        if (!propertiesFrozen_)
            freezeProperties();
        connectionEstablished_ = true;
    }



// @A1a - Moved here from ChangeableResource.
/**
Fires an attributeValuesRefreshed() ResourceEvent.
**/
    protected void fireAttributeValuesRefreshed()
    {
        ResourceEvent event = new ResourceEvent(this, ResourceEvent.ATTRIBUTE_VALUES_REFRESHED);
        Vector temp = (Vector)resourceListeners_.clone();
        Enumeration list = temp.elements();
        while(list.hasMoreElements())
            ((ResourceListener)list.nextElement()).attributeValuesRefreshed(event);
    }



/**
Fires a busy active status event.  This indicates that a potentially
long-running operation has started.
**/
    protected void fireBusy()
    {        
        ActiveStatusEvent event = new ActiveStatusEvent(this, ActiveStatusEvent.BUSY);
        Vector temp = (Vector)activeStatusListeners_.clone();
        Enumeration list = temp.elements();
        while(list.hasMoreElements())
            ((ActiveStatusListener)list.nextElement()).busy(event);
    }


/**
Fires a idle active status event.  This indicates that a potentially
long-running operation has ended.
**/
    protected void fireIdle()
    {        
        ActiveStatusEvent event = new ActiveStatusEvent(this, ActiveStatusEvent.IDLE);
        Vector temp = (Vector)activeStatusListeners_.clone();
        Enumeration list = temp.elements();
        while(list.hasMoreElements())
            ((ActiveStatusListener)list.nextElement()).idle(event);
    }


/**
Fires a property change event.

@param propertyName     The property name.
@param oldValue         The old value.
@param newValue         The new value.
**/
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        propertyChangeSupport_.firePropertyChange(propertyName, oldValue, newValue);
    }



/**
Fires a vetoable change event.

@param propertyName     The property name.
@param oldValue         The old value.
@param newValue         The new value.

@exception PropertyVetoException    If the property change is vetoed.
**/
    protected void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
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
// 1.  This will be called when a resource is added to a 
//     resource pool, to prevent its key from being changed.
//
// 2.  This is separate from establishConnection() because
//     there are times when we want to freeze the properties
//     without incurring the performance hit of setting up
//     the connection.  For example, getting a list of Users.
//     Each User's properties should be frozen, but we don't
//     want to establish a connection for each user unless
//     the calling program needs to.
//
    protected void freezeProperties()
    throws ResourceException
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Freezing properties for " + this + ".");

        propertiesFrozen_ = true;
    }



/**
Returns the attribute meta data for a specific attribute.

@param attributeID  Identifies the attribute.   
@return             The attribute meta data.
**/
	public ResourceMetaData getAttributeMetaData(Object attributeID)
    {        
        return attributes_.getMetaData(attributeID);
    }



/**
Returns the attribute meta data.  The array will contain an element
for every supported attribute.

@return The attribute meta data.  The array has zero elements if
        there are no attributes.
**/
    public ResourceMetaData[] getAttributeMetaData()
    {
        return attributes_.getMetaData(getCurrentLevel());
    }



    // @A2C
/**
Returns the current value of an attribute.

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
            return getAttributeValueImplementation(attributeID);
    }



// @A2A
/**
Returns the current value of an attribute.

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
        else {
            // In the default implementation, we don't use the string type,
            // since there is no conversion being done.
            return getAttributeValueImplementation(attributeID);
        }
    }


// @A2A
    Object getAttributeValueImplementation(Object attributeID)
    throws ResourceException
    {
        synchronized(this) {
            attributes_.validateID(attributeID);
            return attributeValues_.get(attributeID);
        }
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
Returns the presentation information.

@return The presentation information.
**/
    public Presentation getPresentation()
    {
        return presentation_;
    }


    
/**
Returns the resource key.  The resource key uniquely identifies the resource.
The resource key may not be set if the resource's properties are not set.

@return The resource key, or null if the resource key has not been set.
**/
//
// Implementation note:
//
// The resource key is used for identifying reusable objects in the resource
// pool.  If there is already an object in the pool with a specified resource
// key, then we can reuse it.  Subclasses of this class should define resource
// keys so that if any 2 objects can be used interchangeably, then their resource
// keys should be the same.
//
    public Object getResourceKey()
    {
        return resourceKey_;
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
Initializes an attribute value.  This is intended for use by the
subclass when it is initializing attribute values.

@param attributeID  Identifies the attribute.
@param value        The attribute value.  This cannot be null.
**/
    protected void initializeAttributeValue(Object attributeID, Object value)
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Initialized attribute value " + attributeID + " (for "
                      + this + ") to " + value + "(" + value.getClass() + ")");

        // Validation.
        ResourceMetaData attributeMetaData = attributes_.validateID(attributeID);        
        if (value == null)
            throw new NullPointerException("value");
        value = attributeMetaData.validateValue(value);
        
        // Make the change.
        synchronized(this) {
            attributeValues_.put(attributeID, value);
        }
    }



/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
        activeStatusListeners_      = new Vector();
        propertyChangeSupport_      = new PropertyChangeSupport(this);
        vetoableChangeSupport_      = new VetoableChangeSupport(this);

        resourceListeners_          = new Vector();  // @A1a
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
Indicates if a connection to the system is established, if any.  

@return     true if a connection is established, false otherwise.
**/
    protected boolean isConnectionEstablished()
    {
        return connectionEstablished_;
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
Refreshes the values for all attributes.

@exception ResourceException                If an error occurs.           
**/
    public void refreshAttributeValues()
    throws ResourceException
    {
        fireAttributeValuesRefreshed();  // @A1a
        // Do nothing else by default.
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
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        propertyChangeSupport_.removePropertyChangeListener(listener);
    }



// @A1a - Moved here from ChangeableResource.
/**
Removes a ResourceListener.

@param listener The listener.
**/
    public void removeResourceListener(ResourceListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        resourceListeners_.removeElement(listener);
    }



/**
Removes a VetoableChangeListener. 

@param listener The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        vetoableChangeSupport_.removeVetoableChangeListener(listener);
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
Sets the resource key.  

@param resourceKey   The resource key.
**/
//
// Implementation note: 
//
// Resources should never change their key once they have 
// been added to the resource pool.
//
    protected void setResourceKey(Object resourceKey)
    {
        if(resourceKey == null)
            throw new NullPointerException("resourceKey");

        resourceKey_ = resourceKey;        
    }



/**
Sets the system.  This does not change the job on 
the system.  Instead, it changes the system to which 
this object references.  This cannot be changed 
if the object has established a connection to the system.

@param system    The system.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setSystem(AS400 system)
        throws PropertyVetoException
    {
        if (system == null)
            throw new NullPointerException("system");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

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
Validates the attribute ID.

@param attributeID      The attribute ID.
@return                 The attribute meta data.
**/
    ResourceMetaData validateAttributeID(Object attributeID)
    {
        return attributes_.validateID(attributeID);
    }



}
