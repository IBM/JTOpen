///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceProperties.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;



/**
The ResourceProperties class represents the arrangement of properties on
a properties pane for the graphical user interface representation of a 
{@link com.ibm.as400.resource.Resource Resource}
or {@link com.ibm.as400.resource.ResourceList ResourceList} object.

<p>The properties pane is presented as a set of tabs, each of which can
present multiple properties.  The tabs are identified by their index,
starting at 0, in the order they are added.  There is always at least
one tab, created by default.  The first tab always displays the icon 
and full name of the object being represented.

<a name="#properties">                                  
<p>Properties are specified using either attribute IDs (for Resource objects),
or selection or sort IDs (for ResourceList objects).  Each implementation
of Resource or ResourceList documents its set of valid attribute, selection,
and sort IDs.  
</a>

<p>This class alone does not present the properties pane.  It is
intended for use in conjunction with 
{@link com.ibm.as400.vaccess.ResourceListDetailsPane ResourceListDetailsPane}
or {@link com.ibm.as400.vaccess.ResourceListPane ResourceListPane}.

<p>Here is an example which creates a ResourceProperties object for
a list of jobs:

<code><pre>
// Create the resource list.  This example creates
// a list of all jobs on the system.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RJobList resourceList = new RJobList(system);
<br>
// Create and initialize a ResourceProperties object
// which defines the arrangement of job attributes
// to be presented when the end user views the 
// properties for a specific job.
ResourceProperties properties = new ResourceProperties();
<br>
// Add the job number, user name, and job type to the first tab.
properties.addProperty(RJob.JOB_NUMBER);
properties.addProperty(RJob.USER_NAME);
properties.addProperty(RJob.JOB_TYPE);
<br>
// Add a new "Special" tab which contains the job status and subtype.
int specialTab = properties.addTab("Special");
properties.addProperty(specialTab, RJob.JOB_STATUS);
properties.addProperty(specialTab, RJob.JOB_SUBTYPE);
<br>
// Create the ResourceListDetailsPane.  In this example,
// there are four columns in the table.  The first column
// contains the icons and names for each job.  The remaining
// columns contain the status, type, and subtype, respectively,
// for each job.   In addition, we specify the ResourceProperties
// object defined earlier.
Object[] columnAttributes = new Object[] { null, RJob.JOB_NUMBER, RJob.USER_NAME, RJob.JOB_STATUS, RJob.JOB_TYPE, RJob.JOB_SUBTYPE };
ResourceListDetailsPane detailsPane = new ResourceListDetailsPane(resourceList, columnAttributes, properties);
<br>
// Add the ResourceListDetailsPane to a JFrame and show it.
JFrame frame = new JFrame("My Window");
frame.getContentPane().add(detailsPane);
frame.pack();
frame.show();
<br>
// The ResourceListDetailsPane will appear empty until
// we load it.  This gives us control of when the list
// of jobs is retrieved from the AS/400.
detailsPane.load();
</pre></code>
**/
public class ResourceProperties
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String RESOURCE_GENERAL_TAB_       = ResourceLoader.getText("RESOURCE_GENERAL_TAB");



    // Private data.
    private static final Object FIRST_TAB_KEY_              = new Integer(0);

    private boolean   editable_         = false;
    private int       tabCounter_       = 0;
    private Hashtable tabPropertyIDs_   = new Hashtable();
    private Hashtable tabLabels_        = new Hashtable();




/**
Constructs the ResourceProperties object.
**/
    public ResourceProperties()
    {
        this(new Object[0], RESOURCE_GENERAL_TAB_, true);
    }



/**
Constructs the ResourceProperties object.

@param firstTabLabel    The label for the first tab.
@param editable         true if the properties should be editable,
                        false otherwise.                    
**/
    public ResourceProperties(String firstTabLabel, boolean editable)
    {
        this(new Object[0], firstTabLabel, editable);
    }



/**
Constructs the ResourceProperties object.

@param propertyIDs      The property IDs to add to the first tab.
@param firstTabLabel    The label for the first tab.
@param editable         true if the properties should be editable,
                        false otherwise.                    
**/
    public ResourceProperties(Object[] propertyIDs, String firstTabLabel, boolean editable)
    {
        if (propertyIDs == null)
            throw new NullPointerException("propertyIDs");
        if (firstTabLabel == null)
            throw new NullPointerException("firstTabLabel");

        Vector v = new Vector(propertyIDs.length);
        for(int i = 0; i < propertyIDs.length; ++i) {
            if (propertyIDs[i] == null)
                throw new NullPointerException("propertyIDs[" + i + "]");
            v.addElement(propertyIDs[i]);
        }

        tabPropertyIDs_.put(FIRST_TAB_KEY_, v);
        tabLabels_.put(FIRST_TAB_KEY_, firstTabLabel);

        editable_ = editable;
    }



/**
Adds a property to the first tab.  The new property
is placed after all existing properties on the first
tab.

@param propertyID   The property ID to add to the first tab.
**/
    public void addProperty(Object propertyID)
    {
        if (propertyID == null)
            throw new NullPointerException("propertyID");

        synchronized(this) {
            ((Vector)tabPropertyIDs_.get(FIRST_TAB_KEY_)).addElement(propertyID);
        }
    }



/**
Adds a property to the specified tab.    The new property
is placed after all existing properties on the tab.

@param tab      The tab. 
                <ul>
                <li>0 - indicates the first tab.
                <li>A tab value returned by a previous call to <a href="#addTab(java.lang.String)">addTab()</a>.
                </ul>
@param propertyID   The property ID to add to the specified tab.
**/
    public void addProperty(int tab, Object propertyID)
    {
        if (propertyID == null)
            throw new NullPointerException("propertyID");

        synchronized(this) {
            Object key = new Integer(tab);
            if (!tabPropertyIDs_.containsKey(key))
                throw new ExtendedIllegalArgumentException("tab", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            ((Vector)tabPropertyIDs_.get(key)).addElement(propertyID);
        }
    }



/**
Adds multiple properties to the first tab.  The new properties
are placed after all existing properties on the first tab.

@param propertyIDs   The property IDs to add to the first tab.
**/
    public void addProperties(Object[] propertyIDs)
    {
        if (propertyIDs == null)
            throw new NullPointerException("propertyIDs");

        synchronized(this) {
            Vector v = (Vector)tabPropertyIDs_.get(FIRST_TAB_KEY_);
            for(int i = 0; i < propertyIDs.length; ++i) {
                if (propertyIDs[i] == null)
                    throw new NullPointerException("propertyIDs[" + i + "]");
                v.addElement(propertyIDs[i]);
            }
        }
    }



/**
Adds multiple properties to the specified tab.  The new properties
are placed after all existing properties on the tab.

@param tab      The tab. 
                <ul>
                <li>0 - indicates the first tab.
                <li>A tab value returned by a previous call to <a href="#addTab(java.lang.String)">addTab()</a>.
                </ul>
@param propertyIDs   The property IDs to add to the specified tab.
**/
    public void addProperties(int tab, Object[] propertyIDs)
    {
        if (propertyIDs == null)
            throw new NullPointerException("propertyIDs");

        synchronized(this) {
            Object key = new Integer(tab);
            if (!tabPropertyIDs_.containsKey(key))
                throw new ExtendedIllegalArgumentException("tab", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            Vector v = (Vector)tabPropertyIDs_.get(key);
            for(int i = 0; i < propertyIDs.length; ++i) {
                if (propertyIDs[i] == null)
                    throw new NullPointerException("propertyIDs[" + i + "]");
                v.addElement(propertyIDs[i]);
            }
        }
    }




/**
Adds a tab.  The new tab is placed after all existing tabs.

@param label    The tab label.
@return         The tab index.
**/
    public int addTab(String label)
    {
        if (label == null)
            throw new NullPointerException("label");

        synchronized(this) {
            ++tabCounter_;
            Object key = new Integer(tabCounter_);
            tabPropertyIDs_.put(key, new Vector());
            tabLabels_.put(key, label);
            return tabCounter_;
        }
    }



/**
Adds a tab.  The new tab is placed after all existing tabs.

@param label         The tab label.
@param propertyIDs   The property IDs to add to the tab.
@return              The tab index.
**/
    public int addTab(String label, Object[] propertyIDs)
    {
        if (label == null)
            throw new NullPointerException("label");
        if (propertyIDs == null)
            throw new NullPointerException("propertyIDs");

        synchronized(this) {
            Vector v = new Vector(propertyIDs.length);
            for(int i = 0; i < propertyIDs.length; ++i) {
                if (propertyIDs[i] == null)
                    throw new NullPointerException("propertyIDs[" + i + "]");
                v.addElement(propertyIDs[i]);
            }

            ++tabCounter_;
            Object key = new Integer(tabCounter_);
            tabPropertyIDs_.put(key, v);
            tabLabels_.put(key, label);
            return tabCounter_;
        }
    }



/**
Returns the label for the specified tab.

@param tab      The tab. 
                <ul>
                <li>0 - indicates the first tab.
                <li>A tab value returned by a previous call to <a href="#addTab(java.lang.String)">addTab()</a>.
                </ul>
@return         The label for the specified tab.                
**/
    public String getLabel(int tab)
    {
        synchronized(this) {
            Object key = new Integer(tab);
            if (!tabLabels_.containsKey(key))
                throw new ExtendedIllegalArgumentException("tab", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            else
                return (String)tabLabels_.get(key);
        }
    }



/**
Returns the property IDs that have been added to the specified tab.

@param tab      The tab. 
                <ul>
                <li>0 - indicates the first tab.
                <li>A tab value returned by a previous call to <a href="#addTab(java.lang.String)">addTab()</a>.
                </ul>
@return         The property IDs that have been added to the specified tab.
**/
    public Object[] getProperties(int tab)
    {
        synchronized(this) {
            Object key = new Integer(tab);
            if (!tabPropertyIDs_.containsKey(key))
                throw new ExtendedIllegalArgumentException("tab", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            else {
                Vector asVector = (Vector)tabPropertyIDs_.get(key);
                Object[] propertyIDs = new Object[asVector.size()];
                asVector.copyInto(propertyIDs);
                return propertyIDs;
            }
        }
    }



/**
Returns the number of tabs.

@return The number of tabs.
**/
    public int getTabCount()
    {
        return tabCounter_ + 1;
    }



/**
Indicates if the properties are editable.

@return true if the properties should be editable, false otherwise.
**/
    public boolean isEditable()
    {
        return editable_;
    }



/**
Sets whether the properties are editable.  Even when the properties
are set to editable, specific properties may remain not editable.  This
is the case if it does not make sense to change a property.

@param editable     true if the properties should be editable,
                    false otherwise.  The default is false.
**/
    public void setEditable(boolean editable)
    {
        editable_ = editable;
    }



}
