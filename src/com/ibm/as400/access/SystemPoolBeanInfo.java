///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemPoolBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;

/**
 * The SystemPoolBeanInfo class provides bean information for the 
 * SystemPool class.
**/
public class SystemPoolBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private final static Class beanClass_ = SystemPool.class;
    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;
    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;

    // Static initializer.
    static
    {
        try 
        {
            // Property change events
            EventSetDescriptor changed = new EventSetDescriptor(beanClass_,
                                                               "propertyChange",
                                                               java.beans.PropertyChangeListener.class,
                                                               "propertyChange");

            changed.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
            changed.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));
           
            // Vetoable change events
            EventSetDescriptor veto = new EventSetDescriptor(beanClass_, 
                                                            "vetoableChange",
                                                            java.beans.VetoableChangeListener.class,
                                                            "vetoableChange");

            veto.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
            veto.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

            events_ = new EventSetDescriptor[] { changed, veto };

            // Properties.
            PropertyDescriptor poolName = new PropertyDescriptor("poolName", beanClass_, 
                                                                 "getPoolName", null);
            poolName.setBound(false);
            poolName.setConstrained(false);
            poolName.setDisplayName(loader_.getText("PROP_NAME_SP_POOLNAME"));
            poolName.setShortDescription(loader_.getText("PROP_DESC_SP_POOLNAME"));
            
            PropertyDescriptor poolIdentifier = new PropertyDescriptor("poolIdentifier", beanClass_, 
                                                                       "getPoolIdentifier", null);
            poolIdentifier.setBound(false);
            poolIdentifier.setConstrained(false);
            poolIdentifier.setDisplayName(loader_.getText("PROP_NAME_SP_POOLID"));
            poolIdentifier.setShortDescription(loader_.getText("PROP_DESC_SP_POOLID"));

            PropertyDescriptor activeToIneligible = new PropertyDescriptor("activeToIneligible", 
                                                                           beanClass_, 
                                                                           "getActiveToIneligible", 
                                                                           null);
            activeToIneligible.setBound(false);
            activeToIneligible.setConstrained(false);
            activeToIneligible.setDisplayName(loader_.getText("PROP_NAME_SP_ATE"));
            activeToIneligible.setShortDescription(loader_.getText("PROP_DESC_SP_ATE"));
            
            PropertyDescriptor activeToWait = new PropertyDescriptor("activeToWait", beanClass_, 
                                                                     "getActiveToWait", null);
            activeToWait.setBound(false);
            activeToWait.setConstrained(false);
            activeToWait.setDisplayName(loader_.getText("PROP_NAME_SP_ATW"));
            activeToWait.setShortDescription(loader_.getText("PROP_DESC_SP_ATW"));
            
            PropertyDescriptor databaseFaults = new PropertyDescriptor("databaseFaults", beanClass_, 
                                                                       "getDatabaseFaults", null);
            databaseFaults.setBound(false);
            databaseFaults.setConstrained(false);
            databaseFaults.setDisplayName(loader_.getText("PROP_NAME_SP_DBFAULTS"));
            databaseFaults.setShortDescription(loader_.getText("PROP_DESC_SP_DBFAULTS"));
            
            PropertyDescriptor databasePages = new PropertyDescriptor("databasePages", beanClass_, 
                                                                      "getDatabasePages", null);
            databasePages.setBound(false);
            databasePages.setConstrained(false);
            databasePages.setDisplayName(loader_.getText("PROP_NAME_SP_DBPAGES"));
            databasePages.setShortDescription(loader_.getText("PROP_DESC_SP_DBPAGES"));
            
            PropertyDescriptor maxAT = new PropertyDescriptor("maximumActiveThreads", 
                                                                             beanClass_, 
                                                                             "getMaximumActiveThreads", 
                                                                             null);
            maxAT.setBound(false);
            maxAT.setConstrained(false);
            maxAT.setDisplayName(loader_.getText("PROP_NAME_SP_MAXAT"));
            maxAT.setShortDescription(loader_.getText("PROP_DESC_SP_MAXAT"));
            
            PropertyDescriptor nonDatabaseFaults = new PropertyDescriptor("nonDatabaseFaults", 
                                                                          beanClass_, 
                                                                          "getNonDatabaseFaults", 
                                                                          null);
            nonDatabaseFaults.setBound(false);
            nonDatabaseFaults.setConstrained(false);
            nonDatabaseFaults.setDisplayName(loader_.getText("PROP_NAME_SP_NONDBFLTS"));
            nonDatabaseFaults.setShortDescription(loader_.getText("PROP_DESC_SP_NONDBFLTS"));
            
            PropertyDescriptor nonDatabasePages = new PropertyDescriptor("nonDatabasePages", 
                                                                         beanClass_, 
                                                                         "getNonDatabasePages", 
                                                                         null);
            nonDatabasePages.setBound(false);
            nonDatabasePages.setConstrained(false);
            nonDatabasePages.setDisplayName(loader_.getText("PROP_NAME_SP_NONDBPGS"));
            nonDatabasePages.setShortDescription(loader_.getText("PROP_DESC_SP_NONDBPGS"));
            
            PropertyDescriptor pagingOption = new PropertyDescriptor("pagingOption", beanClass_, 
                                                                     "getPagingOption", 
                                                                     "setPagingOption");  
            pagingOption.setBound(true);
            pagingOption.setConstrained(true);
            pagingOption.setDisplayName(loader_.getText("PROP_NAME_SP_PAGINGOPTION"));
            pagingOption.setShortDescription(loader_.getText("PROP_DESC_SP_PAGINGOPTION"));
            
            PropertyDescriptor poolSize = new PropertyDescriptor("poolSize", beanClass_, 
                                                                 "getPoolSize", "setPoolSize"); 
            poolSize.setBound(true);
            poolSize.setConstrained(true);
            poolSize.setDisplayName(loader_.getText("PROP_NAME_SP_POOLSIZE"));
            poolSize.setShortDescription(loader_.getText("PROP_DESC_SP_POOLSIZE"));
            
            PropertyDescriptor reservedSize = new PropertyDescriptor("reservedSize", beanClass_, 
                                                                     "getReservedSize", null); 
            reservedSize.setBound(false);
            reservedSize.setConstrained(false);
            reservedSize.setDisplayName(loader_.getText("PROP_NAME_SP_RSVDSIZE"));
            reservedSize.setShortDescription(loader_.getText("PROP_DESC_SP_RSVDSIZE"));
            
            PropertyDescriptor subsystemName = new PropertyDescriptor("subsystemName", beanClass_, 
                                                                      "getSubsystemName", null); 
            subsystemName.setBound(false);
            subsystemName.setConstrained(false);
            subsystemName.setDisplayName(loader_.getText("PROP_NAME_SP_SUBSYSNAME"));
            subsystemName.setShortDescription(loader_.getText("PROP_DESC_SP_SUBSYSNAME"));
            
            PropertyDescriptor waitToIneligible = new PropertyDescriptor("waitToIneligible", 
                                                                         beanClass_, 
                                                                         "getWaitToIneligible",
                                                                         null);   
            waitToIneligible.setBound(false);
            waitToIneligible.setConstrained(false);
            waitToIneligible.setDisplayName(loader_.getText("PROP_NAME_SP_WTI"));
            waitToIneligible.setShortDescription(loader_.getText("PROP_DESC_SP_WTI"));
            
            PropertyDescriptor maximumFaults = new PropertyDescriptor("maximumFaults", beanClass_, 
                                                                      null, "setMaximumFaults"); 
            maximumFaults.setBound(true);
            maximumFaults.setConstrained(true);
            maximumFaults.setDisplayName(loader_.getText("PROP_NAME_SP_MAXFAULTS"));
            maximumFaults.setShortDescription(loader_.getText("PROP_DESC_SP_MAXFAULTS"));
            
            PropertyDescriptor maximumPoolSize = new PropertyDescriptor("maximumPoolSize", 
                                                                        beanClass_, 
                                                                        null,
                                                                        "setMaximumPoolSize"); 
            maximumPoolSize.setBound(true);
            maximumPoolSize.setConstrained(true);
            maximumPoolSize.setDisplayName(loader_.getText("PROP_NAME_SP_MAXPOOLSIZE"));
            maximumPoolSize.setShortDescription(loader_.getText("PROP_DESC_SP_MAXPOOLSIZE"));
            
            PropertyDescriptor messageLogging = new PropertyDescriptor("messageLogging", beanClass_, 
                                                                       null, "setMessageLogging"); 
            messageLogging.setBound(true);
            messageLogging.setConstrained(true);
            messageLogging.setDisplayName(loader_.getText("PROP_NAME_SP_MSGLOGGING"));
            messageLogging.setShortDescription(loader_.getText("PROP_DESC_SP_MSGLOGGING"));
            
            PropertyDescriptor minimumFaults = new PropertyDescriptor("minimumFaults", beanClass_, 
                                                                      null, "setMinimumFaults"); 
            minimumFaults.setBound(true);
            minimumFaults.setConstrained(true);
            minimumFaults.setDisplayName(loader_.getText("PROP_NAME_SP_MINFAULTS"));
            minimumFaults.setShortDescription(loader_.getText("PROP_DESC_SP_MINFAULTS"));
            
            PropertyDescriptor minimumPoolSize = new PropertyDescriptor("minimumPoolSize", beanClass_, 
                                                                        null, "setMinimumPoolSize"); 
            minimumPoolSize.setBound(true);
            minimumPoolSize.setConstrained(true);
            minimumPoolSize.setDisplayName(loader_.getText("PROP_NAME_SP_MINPOOLSIZE"));
            minimumPoolSize.setShortDescription(loader_.getText("PROP_DESC_SP_MINPOOLSIZE"));
            
            PropertyDescriptor perThreadFaults = new PropertyDescriptor("perThreadFaults", 
                                                                        beanClass_, 
                                                                        null,
                                                                        "setPerThreadFaults"); 
            perThreadFaults.setBound(true);
            perThreadFaults.setConstrained(true);
            perThreadFaults.setDisplayName(loader_.getText("PROP_NAME_SP_PERTHRDFLTS"));
            perThreadFaults.setShortDescription(loader_.getText("PROP_DESC_SP_PERTHRDFLTS"));
            
            PropertyDescriptor poolActivityLevel = new PropertyDescriptor("poolActivityLevel", 
                                                                          beanClass_, 
                                                                          null,
                                                                          "setPoolActivityLevel"); 
            poolActivityLevel.setBound(true);
            poolActivityLevel.setConstrained(true);
            poolActivityLevel.setDisplayName(loader_.getText("PROP_NAME_SP_POOLACTLVL"));
            poolActivityLevel.setShortDescription(loader_.getText("PROP_DESC_SP_POOLACTLVL"));
            
            PropertyDescriptor priority = new PropertyDescriptor("priority", beanClass_, 
                                                                 null, "setPriority");              
            priority.setBound(true);
            priority.setConstrained(true);
            priority.setDisplayName(loader_.getText("PROP_NAME_SP_PRIORITY"));
            priority.setShortDescription(loader_.getText("PROP_DESC_SP_PRIORITY"));

            PropertyDescriptor system = new PropertyDescriptor("system", beanClass_, 
                                                               "getSystem", "setSystem"); 
            system.setBound(true);
            system.setConstrained(true);
            system.setDisplayName(loader_.getText("PROP_NAME_AS400_SYSTEM"));
            system.setShortDescription(loader_.getText("PROP_DESC_AS400_SYSTEM"));
            

            properties_ =  new PropertyDescriptor[]{
                                           activeToIneligible,
                                           activeToWait,
                                           databaseFaults,
                                           databasePages,
                                           maxAT,
                                           maximumFaults,
                                           maximumPoolSize,
                                           messageLogging,
                                           minimumFaults,
                                           minimumPoolSize,
                                           nonDatabaseFaults,
                                           nonDatabasePages,
                                           pagingOption,
                                           perThreadFaults,
                                           poolActivityLevel,
                                           poolIdentifier,
                                           poolName,
                                           poolSize,
                                           priority,
                                           reservedSize,
                                           subsystemName,
                                           system,
                                           waitToIneligible
                                           };
        }
        catch (IntrospectionException e) 
        {   
            throw new Error(e.toString());
        } 
    }
    
    /**
     * Returns the bean descriptor.
     * 
     * @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor ()
    {
        return new BeanDescriptor (beanClass_);
    }

    /** 
     * Returns the default event index.
     * @return The default event index (always 1).
     **/
    public int getDefaultEventIndex() 
    {
        return 1; 
    }

    /** 
     * Returns the default property index.
     * @return The default property index (always 0).
     **/
    public int getDefaultPropertyIndex() 
    {
        return 0; 
    }

    /** 
     * Returns the descriptors for all events. 
     * @return The descriptors for all events.
     **/ 
    public EventSetDescriptor[] getEventSetDescriptors() 
    {
        return events_;
    } 

    /**
     * Returns an Image for this bean's icon.
     * @param icon The desired icon size and color. 
     * @return The Image for the icon.
     */
    public Image getIcon(int icon) 
    {
        if(icon== BeanInfo.ICON_MONO_16x16) 
        {
            java.awt.Image img = loadImage("SystemPool16.gif");
            return img;
        }
        else if(icon== BeanInfo.ICON_COLOR_16x16) 
        {
            java.awt.Image img = loadImage("SystemPool16.gif");
            return img;
        }
        else if(icon == BeanInfo.ICON_MONO_32x32) 
        {
            java.awt.Image img = loadImage("SystemPool32.gif");
            return img;
        }
        else if(icon == BeanInfo.ICON_COLOR_32x32) 
        {
            java.awt.Image img = loadImage("SystemPool32.gif");
            return img;
        }        
        return null;
    }


    /** 
     * Returns the descriptors for all properties.
     * @return The descriptors for all properties. 
     **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }

}
