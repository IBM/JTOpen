///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DataArea.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.io.IOException;
import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.net.UnknownHostException;

/**
The DataArea class is an abstract base class that represents an AS/400
data area object.
<p>DataArea objects generate the following events:
<ul>
<li>DataAreaEvent
  <ul>
  <li>DA_CLEARED
  <li>DA_CREATED
  <li>DA_DELETED
  <li>DA_READ
  <li>DA_WRITTEN
  </ul>
<li>PropertyChangeEvent
<li>VetoableChangeEvent
</ul>
**/

public abstract class DataArea implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;
  /**
   Constants
   **/

  // Type of data area.
  static final int UNINITIALIZED       = 0;
  static final int CHARACTER_DATA_AREA = 1;
  static final int DECIMAL_DATA_AREA   = 2;
  static final int LOCAL_DATA_AREA     = 3;
  static final int LOGICAL_DATA_AREA   = 4;

  /**
   Variables
   **/

  private AS400 system_ = null;              // The AS/400 where the data area is located.
  private String dataAreaPathName_ = null;   // The full path name of the data area,
                                             // as specified by the user.
  private String name_ = null;               // The name of the data area.
  private QSYSObjectPathName ifsPathName_ = null; // The full path name of the data area.

  // Should be "private protected" but are really package scope
  // so the subclasses can get at them.

  //@B0 It was decided that the data area length should NOT be a bean property
  //    because the property getter getLength() needs to go the system. Bean property
  //    constructors, getters, and setters should not make connections to the system
  //    because of the way a visual builder environment manipulates bean objects.
  int length_ = 0;                      // The maximum number of bytes the data area can contain.
  String textDescription_ = "*BLANK";   // A text description of the data area.
  String authority_ = "*LIBCRTAUT";     // The authority level that public has to the data area.
  int dataAreaType_ = UNINITIALIZED;

  private transient Vector daListeners_;
  private transient PropertyChangeSupport changes_ ;
  private transient VetoableChangeSupport vetos_;
  transient DataAreaImpl impl_;


   /**
    Constructs a DataArea object.
    It creates a default DataArea object.  The <i>system</i> and <i>path</i>
    properties must be set before attempting a connection.
   **/
   public DataArea()
   {
     super();
     initializeTransient();
   }


   /**
   Constructs a DataArea object.
   It creates a DataArea instance that represents the data area <i>path</i>
   on <i>system</i>.
      @param system The AS/400 that contains the data area.
      @param path The fully qualified integrated file system path name. The
             integrated file system file extension for a data area is DTAARA. An example of a
             fully qualified integrated file system path to a data area "MYDATA" in library
             "MYLIB" is: /QSYS.LIB/MYLIB.LIB/MYDATA.DTAARA
   **/
   public DataArea(AS400 system, String path)
   {
     super();
     // Validate system parm.
     if (system == null)
       throw new NullPointerException("system");
     // Validate path parm.
     if (path == null)
       throw new NullPointerException("path");

     initializeTransient();
     ifsPathName_ = new QSYSObjectPathName(path, "DTAARA");
     name_ = ifsPathName_.getObjectName();
     system_ = system;
     dataAreaPathName_ = path;
   }


   /**
   Adds a data area listener to receive data area events from this data area.
     @see #removeDataAreaListener
     @param listener The data area listener.
   **/
   public void addDataAreaListener(DataAreaListener listener)
   {
     if (listener == null)
       throw new NullPointerException("listener");

     this.daListeners_.addElement(listener);
   }


   /**
   Adds a PropertyChangeListener.  The specified PropertyChangeListener's
   <b>propertyChange</b> method will be called each time the value of any
   bound property is changed.
     @see #removePropertyChangeListener
     @param listener The PropertyChangeListener.
   **/
   public void addPropertyChangeListener(PropertyChangeListener listener)
   {
     if (listener == null)
       throw new NullPointerException("listener");

     this.changes_.addPropertyChangeListener(listener);
   }


   /**
   Adds the VetoableChangeListener.  The specified VetoableChangeListener's
   <b>vetoableChange</b> method will be called each time the value of any
   constrained property is changed.
     @see #removeVetoableChangeListener
     @param listener The VetoableChangeListener.
   **/
   public void addVetoableChangeListener(VetoableChangeListener listener)
   {
     if (listener == null)
       throw new NullPointerException("listener");

     this.vetos_.addVetoableChangeListener(listener);
   }


   /**
    Chooses the appropriate implementation.
    This method is available for use by subclasses.
    Subclasses must first set dataAreaType_ to a valid value:
    either CHARACTER_DATA_AREA, DECIMAL_DATA_AREA, LOCAL_DATA_AREA,
    or LOGICAL_DATA_AREA.
    **/
   void chooseImpl ()
     throws AS400SecurityException, IOException
   {
     // Verify required attributes have been set.
     if (system_ == null)
     {
       Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
       throw new ExtendedIllegalStateException("System", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     if (ifsPathName_ == null)
     {
       Trace.log(Trace.ERROR, "Attempt to connect before setting data area pathname.");
       throw new ExtendedIllegalStateException("Path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     // Assume that dataAreaType_ is a valid.

     impl_ = (DataAreaImpl) system_.loadImpl2
                               ("com.ibm.as400.access.DataAreaImplRemote",
                                "com.ibm.as400.access.DataAreaImplProxy");
     system_.connectService(AS400.COMMAND);
     impl_.setAttributes(system_.getImpl(), ifsPathName_, dataAreaType_);
   }


   /**
   Removes the data area from the system. Note this method is NOT public.
   It is overridden as a public method in the subclasses that use it.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the object does not exist.
     @exception ServerStartupException          If the AS/400 server cannot be started.
     @exception UnknownHostException            If the AS/400 system cannot be located.
   **/
   void delete()
       throws AS400SecurityException,
              ConnectionDroppedException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException,
              ServerStartupException,
              UnknownHostException
   {
     if (impl_ == null)
       chooseImpl();

     impl_.delete();

     // Fire the DELETED event.
     fireDeleted();
   }


   /**
   Fires a DA_CLEARED event.
     @param obj The source object from which the event originated.
   **/
   void fireCleared()
   {
     Vector targets = (Vector) daListeners_.clone();
     DataAreaEvent event = new DataAreaEvent(this, DataAreaEvent.DA_CLEARED);
     for (int i=0; i<targets.size(); i++)
     {
       DataAreaListener target = (DataAreaListener)targets.elementAt(i);
       target.cleared(event);
     }
   }


   /**
   Fires a DA_CREATED event.
     @param obj The source object from which the event originated.
   **/
   void fireCreated()
   {
     Vector targets = (Vector) daListeners_.clone();
     DataAreaEvent event = new DataAreaEvent(this, DataAreaEvent.DA_CREATED);
     for (int i=0; i<targets.size(); i++)
     {
       DataAreaListener target = (DataAreaListener)targets.elementAt(i);
       target.created(event);
     }
   }


   /**
   Fires a DA_DELETED event.
     @param obj The source object from which the event originated.
   **/
   void fireDeleted()
   {
     Vector targets = (Vector) daListeners_.clone();
     DataAreaEvent event = new DataAreaEvent(this, DataAreaEvent.DA_DELETED);
     for (int i=0; i<targets.size(); i++)
     {
       DataAreaListener target = (DataAreaListener)targets.elementAt(i);
       target.deleted(event);
     }
   }


   /**
   Fires a DA_READ event.
     @param obj The source object from which the event originated.
   **/
   void fireRead()
   {
     Vector targets = (Vector) daListeners_.clone();
     DataAreaEvent event = new DataAreaEvent(this, DataAreaEvent.DA_READ);
     for (int i=0; i<targets.size(); i++)
     {
       DataAreaListener target = (DataAreaListener)targets.elementAt(i);
       target.read(event);
     }
   }


   /**
   Fires a DA_WRITTEN event.
     @param obj The source object from which the event originated.
   **/
   void fireWritten()
   {
     Vector targets = (Vector) daListeners_.clone();
     DataAreaEvent event = new DataAreaEvent(this, DataAreaEvent.DA_WRITTEN);
     for (int i=0; i<targets.size(); i++)
     {
       DataAreaListener target = (DataAreaListener)targets.elementAt(i);
       target.written(event);
     }
   }


   /**
     Returns the size of the data area.
        @return The size of the data area.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the AS/400 object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   **/
   public int getLength()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
   {
  //@B0 It was decided that the data area length should NOT be a bean property
  //    because the property getter getLength() needs to go the system. Bean property
  //    constructors, getters, and setters should not make connections to the system
  //    because of the way a visual builder environment manipulates bean objects.

     if (impl_ == null)
       chooseImpl();

     return impl_.getLength();
   }


   /**
   Returns the data area name.
     @return The name of the data area.
   **/
   public String getName()
   {
     return name_;
   }


   /**
   Returns the integrated file system path name of the object represented
   by the data area. Note this method is NOT public.
   It is overridden as a public method in the subclasses that use it.
      @return The integrated file system path name of the object represented by the data area.
   **/
   String getPath()
   {
     return dataAreaPathName_;
   }


   /**
   Returns the AS400 system object for the data area.
     @return The AS400 system object for the data area.
   **/
   public AS400 getSystem()
   {
     return system_;
   }


   /**
   Provided to initialize transient data if this object is de-serialized.
   **/
   void initializeTransient()
   {
     impl_        = null;

     daListeners_ = new Vector();
     changes_     = new PropertyChangeSupport(this);
     vetos_       = new VetoableChangeSupport(this);
   }


   /**
    *Deserializes and initializes transient data.
    */
   private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
   {
     in.defaultReadObject();
     initializeTransient();
   }


   /**
   Refreshes the attributes of the data area.
   This method should be called if the underlying AS/400 data area has changed
   and it is desired that this object should reflect those changes.
     @exception AS400SecurityException          If a security or authority error occurs.
     @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     @exception IllegalObjectTypeException      If the AS/400 object is not the required type.
     @exception InterruptedException            If this thread is interrupted.
     @exception IOException                     If an error occurs while communicating with the AS/400.
     @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   **/
   public void refreshAttributes()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               IllegalObjectTypeException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
   {
     if (impl_ == null)
       chooseImpl();

     impl_.refreshAttributes();
   }


   /**
   Removes the DataAreaListener from the internal list.
   If the DataAreaListener is not on the list, nothing is done.
     @see #addDataAreaListener
     @param listener The data area listener.
   **/
   public void removeDataAreaListener(DataAreaListener listener)
   {
     if (listener == null)
       throw new NullPointerException("listener");

     daListeners_.removeElement(listener);
   }


   /**
   Removes the PropertyChangeListener from the internal list.
   If the PropertyChangeListener is not on the list, nothing is done.
     @see #addPropertyChangeListener
     @param listener The PropertyChangeListener.
   **/
   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
     if (listener == null)
       throw new NullPointerException("listener");

     changes_.removePropertyChangeListener(listener);
   }


   /**
   Removes the VetoableChangeListener from the internal list.
   If the VetoableChangeListener is not on the list, nothing is done.
     @see #addVetoableChangeListener
     @param listener The VetoableChangeListener.
   **/
   public void removeVetoableChangeListener(VetoableChangeListener listener)
   {
     if (listener == null)
       throw new NullPointerException("listener");

     vetos_.removeVetoableChangeListener(listener);
   }


   /**
    Sets the type of data area.
    This method is only used internally, by subclasses.
    Valid values are: CHARACTER_DATA_AREA, DECIMAL_DATA_AREA,
    LOCAL_DATA_AREA, LOGICAL_DATA_AREA.
    **/
   void setImplType(int dataAreaType)
   {
     dataAreaType_ = dataAreaType; // Trust that the argument is valid.
   }


   /**
   Sets the fully qualified data area name. Note this method is NOT public.
   It is overridden as a public method in the subclasses that use it.
     @exception PropertyVetoException If the change is vetoed.
   **/
   void setPath(String path) throws PropertyVetoException
   {
     // check parm
     if (path == null)
       throw new NullPointerException("path");

     // Make sure we have not already connected.
     if (impl_ != null)
     {
       Trace.log(Trace.ERROR, "Cannot set property after connect.");
       throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
     }

     // Ask for any vetos.
     String old = dataAreaPathName_;
     vetos_.fireVetoableChange("path", old, path );

     // Verify name is valid IFS path name.
     ifsPathName_ = new QSYSObjectPathName(path, "DTAARA");

     // Set instance vars.
     name_ = ifsPathName_.getObjectName();
     dataAreaPathName_ = path;

     changes_.firePropertyChange("path", old, path );
   }


   /**
   Sets the system on which the data area exists. The system cannot be set
   if a connection has already been established.
     @param system The AS/400 system on which the data area exists.
     @exception PropertyVetoException If the change is vetoed.
   **/
   public void setSystem(AS400 system) throws PropertyVetoException
   {
     // check parm
     if (system == null)
       throw new NullPointerException("system");

     // Make sure we have not already connected.
     if (impl_ != null)
     {
       Trace.log(Trace.ERROR, "Cannot set property after connect.");
       throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
     }

     AS400 old = system_;
     vetos_.fireVetoableChange("system", old, system);

     // Set instance var.
     system_ = system;

     changes_.firePropertyChange("system", old, system_);
   }

}
