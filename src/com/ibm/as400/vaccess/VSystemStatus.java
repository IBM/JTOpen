///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemStatus.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;


import com.ibm.as400.access.AS400;
import com.ibm.as400.access.SystemPool;
import com.ibm.as400.access.SystemStatus;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ConnectionDroppedException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;


import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

/**
 * The VSystemStatus class defines the representation of system status on an
 * AS/400 for use in various models and panes in this package.
 * The method load() must be explicitly called to load the information from
 * the AS/400.

 * <p>Most errors are reported as ErrorEvents rather than throwing exceptions.
 *    Users should listen for ErrorEvents in order to diagnose and recover
 *    from error conditions.
 *
 * <p>VSystemStatus objects generate the following events:
 *  <ul>
 *      <li>ErrorEvent
 *      <li>PropertyChangeEvent
 *      <li>VObjectEvent
 *      <li>WorkingEvent
 *  </ul>
**/
public class VSystemStatus implements VNode, Serializable //@B0C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   // Static data.
   private static TableColumnModel detailsColumnModel_ = null;

   // MRI.
   private static String description_ =
   ResourceLoader.getText ("SYSTEM_STATUS_SYSTEM_STATUS");

   private static final Icon icon16_ =
   ResourceLoader.getIcon ("VSystemStatus16.gif", description_);

   private static final Icon icon32_ = 
   ResourceLoader.getIcon ("VSystemStatus32.gif", description_);

   private static String nameColumnHeader_ =
   ResourceLoader.getText ("SYSTEM_POOL_POOL_NAME");

   private static String descriptionColumnHeader_ =
   ResourceLoader.getText ("SYSTEM_POOL_POOL_DESCRIPTION");

   private static String identifierColumnHeader_ =
   ResourceLoader.getText ("SYSTEM_POOL_IDENTIFIER");

   private static String poolSizeColumnHeader_ =
   ResourceLoader.getText ("SYSTEM_POOL_POOL_SIZE");

   private static String reservedSizeColumnHeader_ =
   ResourceLoader.getText ("SYSTEM_POOL_RESERVED_SIZE");
   // MRI - bottom.

   private SystemStatus systemStatus_ = null;
   private VNode        parent_       = null;
   private boolean allowModifyAllPools_ = false;

   transient private VObject[]         detailsChildren_;
   transient private Enumeration       enum_;
   transient private int               loaded_;
   transient private VPropertiesPane   propertiesPane_;

   // Event support.
   transient private ErrorEventSupport           errorEventSupport_;
   transient private PropertyChangeSupport       propertyChangeSupport_;
   transient private VetoableChangeSupport       vetoableChangeSupport_;
   transient private VObjectEventSupport         objectEventSupport_;
   transient private WorkingEventSupport         workingEventSupport_;

   // Static initializer.
   static
   {
      // The column widths are completely arbitrary.
      detailsColumnModel_= null; 
      detailsColumnModel_= new DefaultTableColumnModel(); 
      int i= 0; 

      VTableColumn vTable1= new VTableColumn(i++, VSystemPool.IDENTIFIER_PROPERTY); 
      vTable1.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
      vTable1.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
      vTable1.setHeaderValue(identifierColumnHeader_); 
      vTable1.setPreferredCharWidth(5); 
      detailsColumnModel_.addColumn(((TableColumn)vTable1));

      VTableColumn vTable2= new VTableColumn(i++, VSystemPool.POOL_SIZE_PROPERTY); 
      vTable2.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
      vTable2.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
      vTable2.setHeaderValue(poolSizeColumnHeader_); 
      vTable2.setPreferredCharWidth(8); 
      detailsColumnModel_.addColumn(((TableColumn)vTable2));

      VTableColumn vTable3= new VTableColumn(i++, VSystemPool.RESERVED_SIZE_PROPERTY); 
      vTable3.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
      vTable3.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
      vTable3.setHeaderValue(reservedSizeColumnHeader_); 
      vTable3.setPreferredCharWidth(8); 
      detailsColumnModel_.addColumn(((TableColumn)vTable3));

      VTableColumn vTable4= new VTableColumn(i++, NAME_PROPERTY); 
      vTable4.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
      vTable4.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
      vTable4.setHeaderValue(nameColumnHeader_); 
      vTable4.setPreferredCharWidth(10); 
      detailsColumnModel_.addColumn(((TableColumn)vTable4));

      VTableColumn vTable5= new VTableColumn(i++, DESCRIPTION_PROPERTY); 
      vTable5.setCellRenderer(((TableCellRenderer)new VObjectCellRenderer())); 
      vTable5.setHeaderRenderer(((TableCellRenderer)new VObjectHeaderRenderer())); 
      vTable5.setHeaderValue(descriptionColumnHeader_); 
      vTable5.setPreferredCharWidth(50); 
      detailsColumnModel_.addColumn(((TableColumn)vTable5));
   }

   /**
    * Constructs a VSystemStatus object.
    * @exception AS400Exception If the AS/400 system returns an error
                 message.
    * @exception AS400SecurityException If a security or authority error
                 occurs.
    * @exception ConnectionDroppedException If the connection is dropped
                 unexpectedly.
    * @exception ErrorCompletingRequestException If an error occurs before
                 the request is completed.
    * @exception InterruptedException If this thread is interrupted.
    * @exception IOException If an error occurs while communicating with
                 the AS/400.
    * @exception ObjectDoesNotExistException If the AS/400 object does not
                 exist.
    * @exception PropertyVetoException If the change is vetoed.
    * @exception UnsupportedEncodingException If the character encoding is
                 not supported.
   **/
   public VSystemStatus()
   throws AS400Exception,
   AS400SecurityException,
   ConnectionDroppedException,
   ErrorCompletingRequestException,
   InterruptedException,
   ObjectDoesNotExistException,
   IOException,
   UnsupportedEncodingException,
   PropertyVetoException

   {
      systemStatus_ = new SystemStatus ();
      initializeTransient ();
   }


   /**
    * Constructs a VSystemStatus object.
    *
    * @param system The AS/400 system in which the system status information
    *               resides.
    * @exception AS400Exception If the AS/400 system returns an error
                 message.
    * @exception AS400SecurityException If a security or authority error
                 occurs.
    * @exception ConnectionDroppedException If the connection is dropped
                 unexpectedly.
    * @exception ErrorCompletingRequestException If an error occurs before
                 the request is completed.
    * @exception InterruptedException If this thread is interrupted.
    * @exception IOException If an error occurs while communicating with
                 the AS/400.
    * @exception ObjectDoesNotExistException If the AS/400 object does not
                 exist.
    * @exception PropertyVetoException If the change is vetoed.
    * @exception UnsupportedEncodingException If the character encoding is
                 not supported.
   **/
   public VSystemStatus (AS400 system)
   throws AS400Exception,
   AS400SecurityException,
   ConnectionDroppedException,
   ErrorCompletingRequestException,
   InterruptedException,
   ObjectDoesNotExistException,
   IOException,
   UnsupportedEncodingException,
   PropertyVetoException
   {
      if (system == null)
         throw new NullPointerException ("system");

      systemStatus_ = new SystemStatus (system);

      initializeTransient ();
   }



   /**
       * Constructs a VSystemStatus object.
       *
       * @param parent The parent.
       * @param system The AS/400 system from which the user will be retrieved.
       * @exception AS400Exception If the AS/400 system returns an error
                    message.
       * @exception AS400SecurityException If a security or authority error
                    occurs.
       * @exception ConnectionDroppedException If the connection is dropped
                    unexpectedly.
       * @exception ErrorCompletingRequestException If an error occurs before
                    the request is completed.
       * @exception InterruptedException If this thread is interrupted.
       * @exception IOException If an error occurs while communicating with
                    the AS/400.
       * @exception ObjectDoesNotExistException If the AS/400 object does not
                    exist.
       * @exception PropertyVetoException If the change is vetoed.
       * @exception UnsupportedEncodingException If the character encoding is
                    not supported.
      **/
   public VSystemStatus(SystemStatus systemStatus)
   throws AS400Exception,
   AS400SecurityException,
   ConnectionDroppedException,
   ErrorCompletingRequestException,
   InterruptedException,
   ObjectDoesNotExistException,
   IOException,
   UnsupportedEncodingException,
   PropertyVetoException
   {
      systemStatus_ = systemStatus;
      initializeTransient();
   }    

   /**
    * Constructs a VSystemStatus object.
    *
    * @param parent The parent.
    * @param system The AS/400 system from which the user will be retrieved.
    * @exception AS400Exception If the AS/400 system returns an error
                 message.
    * @exception AS400SecurityException If a security or authority error
                 occurs.
    * @exception ConnectionDroppedException If the connection is dropped
                 unexpectedly.
    * @exception ErrorCompletingRequestException If an error occurs before
                 the request is completed.
    * @exception InterruptedException If this thread is interrupted.
    * @exception IOException If an error occurs while communicating with
                 the AS/400.
    * @exception ObjectDoesNotExistException If the AS/400 object does not
                 exist.
    * @exception PropertyVetoException If the change is vetoed.
    * @exception UnsupportedEncodingException If the character encoding is
                 not supported.
   **/
   public VSystemStatus (VNode parent, AS400 system)
   throws AS400Exception,
   AS400SecurityException,
   ConnectionDroppedException,
   ErrorCompletingRequestException,
   InterruptedException,
   ObjectDoesNotExistException,
   IOException,
   UnsupportedEncodingException,
   PropertyVetoException
   {
      if (parent == null)
         throw new NullPointerException ("parent");
      if (system == null)
         throw new NullPointerException ("system");

      parent_   = parent;

      systemStatus_ = new SystemStatus (system);

      initializeTransient ();
   }

   /**
    * Adds a listener to be notified when an error occurs.
    *
    * @param  listener  The listener.
   **/
   public void addErrorListener (ErrorListener listener)
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      errorEventSupport_.addErrorListener (listener);
   }

   /**
    * Adds a listener to be notified when the value of any
    * bound property changes.
    *
    * @param  listener  The listener.
   **/
   public void addPropertyChangeListener (PropertyChangeListener listener)
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      propertyChangeSupport_.addPropertyChangeListener (listener);
   }

   /**
    * Adds a listener to be notified when the value of any
    * constrained property changes.
    *
    * @param  listener  The listener.
   **/
   public void addVetoableChangeListener (VetoableChangeListener listener)
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      vetoableChangeSupport_.addVetoableChangeListener (listener);
   }

   /**
    * Adds a listener to be notified when a VObject is changed,
    * created, or deleted.
    *
    * @param  listener  The listener.
   **/
   public void addVObjectListener (VObjectListener listener)
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      objectEventSupport_.addVObjectListener (listener);
   }

   /**
    * Adds a listener to be notified when work starts and stops
    * on potentially long-running operations.
    *
    * @param  listener  The listener.
   **/
   public void addWorkingListener (WorkingListener listener)
   {
      workingEventSupport_.addWorkingListener (listener);
   }

   /**
    * Returns the children of the node.
    *
    * @return The children.
   **/
   public Enumeration children ()
   {
      return new VEnumeration (this);
   }


   /**
    * Returns the list of actions that can be performed.
    *
    * @return Always null. There are no actions.
   **/
   public VAction[] getActions ()
   {
      return null;
   }

   /**
    * Indicates if the node allows children.
    *
    * @return  Always false.
   **/
   public boolean getAllowsChildren ()
   {
      return false;
   }

   /**
    * Returns the child node at the specified index.
    *
    * @param  index   The index.
    * @return         Always null.
   **/
   public TreeNode getChildAt (int index)
   {
      return null;
   }

   /**
    * Returns the number of children.
    *
    * @return  Always 0.
   **/
   public /*@B0D synchronized */ int getChildCount ()
   {
      return 0;
   }

   /**
    * Returns the default action.
    *
    * @return Always null.  There is no default action.
   **/
   public VAction getDefaultAction ()
   {
      return null;
   }

   /**
    * Returns the child for the details at the specified index.
    *
    * @param index The index.
    * @return The child, or null if the index is not valid.
   **/
   public VObject getDetailsChildAt (int index)
   {
      if ((index < 0) || (index >= detailsChildren_.length))
         return null;

      loadMore (index);
      return detailsChildren_[index];
   }

   /**
    * Returns the number of children for the details.
    *
    * @return  The number of children for the details.
   **/
   public /*@B0D synchronized */ int getDetailsChildCount ()
   {
      return detailsChildren_.length;
   }

   /**
    * Returns the table column model to use in the details when
    * representing the children.  
    * This column model describes the details values for the children.
    *
    * @return The details column model.
   **/
   public TableColumnModel getDetailsColumnModel ()
   {
      return detailsColumnModel_;
   }

   /**
    * Returns the index of the specified child for the details.
    *
    * @param  detailsChild   The details child.
    * @return                The index, or -1 if the child is not found
    *                        in the details.
   **/
   public /*@B0D synchronized */ int getDetailsIndex (VObject detailsChild)
   {
      for (int i = 0; i < loaded_; ++i)
         if (detailsChildren_[i] == detailsChild)
            return i;
      return -1;
   }

   /**
    * Returns the icon.
    *
    * @param  size    The icon size, either 16 or 32.  If any other
    *                 value is given, then return a default.
    * @param  open    This parameter has no effect.
    * @return         The icon.
   **/
   public Icon getIcon (int size, boolean open)
   {
      if (size == 32)
         return icon32_;
      else
         return icon16_;
   }

   /**
    * Returns the index of the specified child.
    *
    * @param  child   The child.
    * @return         Always -1.
   **/
   public /*@B0D synchronized */ int getIndex (TreeNode child)
   {
      return -1;
   }

   /**
    * Returns the parent node.
    *
    * @return The parent node, or null if there is no parent.
   **/
   public TreeNode getParent ()
   {
      return parent_;
   }

   /**
    * Returns the properties pane.
    *
    * @return The properties pane.
   **/
   public VPropertiesPane getPropertiesPane ()
   {
      return propertiesPane_;
   }

   /**
    * Returns a property value.
    *
    * @param  propertyIdentifier  The property identifier.
    * @return                     The property value, or null if the
    *                             property identifier is not recognized.
   **/
   public Object getPropertyValue(Object object)
   {    
      return null;
   }

   /**
    * Returns the AS/400 system in which the system status information resides.
    *
    * @return The AS/400 system in which the system status information resides.
    *
    * @see com.ibm.as400.access.SystemStatus#getSystem
   **/
   public AS400 getSystem ()
   {
      return systemStatus_.getSystem ();
   }

   /**
    * Returns the description text.
    *
    * @return The description text.
   **/
   public String getText ()
   {
      return description_;
   }

   /**
    * Initializes the transient data.
   **/
   private synchronized void initializeTransient ()
   {   
      // Initialize the event support.
      errorEventSupport_      = new ErrorEventSupport (this);
      objectEventSupport_     = new VObjectEventSupport (this);
      propertyChangeSupport_  = new PropertyChangeSupport (this);
      vetoableChangeSupport_  = new VetoableChangeSupport (this);
      workingEventSupport_    = new WorkingEventSupport (this);

      systemStatus_.addPropertyChangeListener (propertyChangeSupport_); //@B0C
      systemStatus_.addVetoableChangeListener (vetoableChangeSupport_);
      // Initialize the private data.
      detailsChildren_        = new VObject[0];
      enum_                   = null;
      loaded_                 = -1;
   }

   /**
    * Return true if the modify action to all system pools is allowed, otherwise false.
    * 
    * @return True if the modify action to all system pools is allowed, otherwise false.
   **/
   public boolean isAllowModifyAllPools()
   {
      return allowModifyAllPools_;
   }

   /**
    * Indicates if the node is a leaf.
    *
    * @return  Always true.
   **/
   public boolean isLeaf ()
   {
      return true;
   }

   /**
    * Indicates if the details children are sortable.
    *
    * @return Always false.
   **/
   public boolean isSortable ()
   {
      return false;
   }

   /**
    * Loads information about the object from the AS/400.
   **/
   public void load ()
   {   
      if (Trace.isTraceOn() && Trace.isTraceInformationOn()) //@B0C
         Trace.log(Trace.INFORMATION, "VSystemStatus: loading"); //@B0C

      workingEventSupport_.fireStartWorking ();

      Exception error = null;
      synchronized (this) {
         // Stop listening to the previous children.
         for (int i = 0; i < loaded_; ++i)
         {
            detailsChildren_[i].removeErrorListener (errorEventSupport_);
            detailsChildren_[i].removeVObjectListener (objectEventSupport_);
            detailsChildren_[i].removeWorkingListener (workingEventSupport_);
         }

         // Refresh the children based on the user list.
         loaded_ = 0;
         try
         {
            systemStatus_.refreshCache(); //@B0A //@B0C - 06/17/1999
            enum_ = systemStatus_.getSystemPools();

            detailsChildren_ = new VSystemPool[systemStatus_.getPoolsNumber()];

         }
         catch (Exception e)
         {
            error = e;
            detailsChildren_ = new VSystemPool[0];
         }
      }
      if (error != null)
         errorEventSupport_.fireError (error);

      workingEventSupport_.fireStopWorking ();
   }

   /**
    * Loads more messages from the enumeration, if needed.
    *
    * @param index  The index needed.
   **/
   private void loadMore (int index)
   {
      if (index >= loaded_)
      {

         workingEventSupport_.fireStartWorking ();

         Exception error = null;
         synchronized (this) {

            for (int i = loaded_; i <= index; ++i)
            {
               SystemPool systemPool = (SystemPool) enum_.nextElement ();

               VSystemPool vSystemPool = new VSystemPool (systemPool);
               vSystemPool.setAllowModify(allowModifyAllPools_);
               detailsChildren_[i] = vSystemPool;
               detailsChildren_[i].addErrorListener (errorEventSupport_);
               detailsChildren_[i].addVObjectListener (objectEventSupport_);
               detailsChildren_[i].addWorkingListener (workingEventSupport_);
            }
         }

         loaded_ = index + 1;

         if (error != null)
            errorEventSupport_.fireError (error);

         workingEventSupport_.fireStopWorking ();
      }
   }


   /**
    * Restores the state of the object from an input stream.
    * This is used when deserializing an object.
    *
    * @param in  The input stream.
   **/
   private void readObject (ObjectInputStream in)
   throws IOException, ClassNotFoundException
   {
      in.defaultReadObject ();
      initializeTransient ();
   }

   /**
    * Removes an error listener.
    *
    * @param  listener  The listener.
   **/
   public void removeErrorListener (ErrorListener listener)
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      errorEventSupport_.removeErrorListener (listener);
   }

   /**
    * Removes a property change listener.
    *
    * @param  listener  The listener.
   **/
   public void removePropertyChangeListener (PropertyChangeListener listener)
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      propertyChangeSupport_.removePropertyChangeListener (listener);
   }

   /**
    * Removes a vetoable change listener.
    *
    * @param  listener  The listener.
   **/
   public void removeVetoableChangeListener(VetoableChangeListener listener)
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      vetoableChangeSupport_.removeVetoableChangeListener (listener);
   }

   /**
    * Removes a VObjectListener.
    *
    * @param  listener  The listener.
   **/
   public void removeVObjectListener (VObjectListener listener)
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      objectEventSupport_.removeVObjectListener (listener);
   }

   /**
    * Removes a working listener.
    *
    * @param  listener  The listener.
   **/
   public void removeWorkingListener (WorkingListener listener)
   {
      workingEventSupport_.removeWorkingListener (listener);
   }

   /**
    * Sets the AS/400 system in which the system status information resides.
    *
    * @see com.ibm.as400.access.SystemStatus#setSystem
    * @param system The AS/400 system in which the system status information
    *               resides.
    * @exception PropertyVetoException If the change is vetoed.
   **/
   public void setSystem (AS400 system)
   throws PropertyVetoException //@B0C
   {
      systemStatus_.setSystem (system);
   }
   /**
    * Set the state of modify action of all the pools.
    *
    * @param allowModify The boolean value.
   **/
   public void setAllowModifyAllPools(boolean allow)
   {
      allowModifyAllPools_ = allow;
   }

   /**
    * Sorts the children for the details.  Since sorting is not supported,
      this method does nothing.
    *
    * @param  propertyIdentifiers The property identifiers.
    * @param  orders The sort orders for each property identifier:
    *                true for ascending order; false for descending order.
   **/
   public void sortDetailsChildren (
                                   Object[] propertyIdentifiers, boolean[] orders)
   {
      // No sorting here!
   }

   /**
    * Returns the string representation of the description.
    *
    * @return The string representation of the description.
   **/
   public String toString ()
   {
      return description_;
   }

}
