///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SystemPool.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Hashtable;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeSupport;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Represents a system pool on a server. It provides
 * facilities for retrieving and changing system pool information.
 *
 * Here is a example:
 *
 * <p><blockquote><pre>
 *  try {
 *      // Creates AS400 object.
 *      AS400 as400 = new AS400("systemName");
 *      // Constructs a SystemPool object
 *      SystemPool systemPool = new SystemPool(as400,"*SPOOL");
 *      // Gets system pool attributes.
 *      System.out.println("Paging option : "+systemPool.getPagingOption());
 *
 *  } catch (Exception e)
 *  {
 *      System.out.println("error : "+e)
 *  }
 * </pre></blockquote></p>
 *
 **/
public class SystemPool
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final boolean DEBUG = false;


     /**
      * Indicates that the server should calculate
      * a system pool attribute.
     **/
     public static final float CALCULATE = -2;

     /**
      * Indicates that the server should calculate
      * a system pool attribute.
     **/
     public static final int CALCULATE_INT = -2;

     private static final Integer CALCULATE_OBJ = new Integer(-2);
     private static final Integer NO_CHANGE = new Integer(-1);

     // Private variable representing the server.
     private AS400 system_;

     // Private variable representing the array of the system pool attribute.
//     private SystemPoolAttribute[] attributes_;

     // Private variable representing the object of the SystemPoolAccess.
//@B0D     private SystemPoolAccess access_;

     // Private variable representing the pool's name.  In the case of a private (subsystem) pool, this field will contain a number from 1-10.
     private String poolName_;

     // Private variable representing the system pool identifier.  The number is assigned by the system, and is unique across system at any given moment.  Shared system pools that are not in use by a subsystem, will not have a system pool identifier assigned.
     private Integer poolIdentifier_;

     private boolean isSharedPool_; // true if it's a shared system pool; false if private to a subsystem

     // Attributes that are meaningful only if the pool is a subsystem pool.
     private String subsystemLibrary_;
     private String subsystemName_;
     private int poolSequenceNumber_;  // pool ID (sequence number) of private pool (1-10)

     private transient boolean connected_;
     private /*transient*/ boolean cacheChanges_;

     // Private variables representing event support.
     private transient PropertyChangeSupport changes_;
     private transient VetoableChangeSupport vetos_;

     // This format maps the API getter.
     private SSTS0300Format format0300_;
     // This format maps the pool information portion of the 0300 format.
     private PoolInformationFormat poolFormat_;
     // This record holds the data for one system pool -- us.
     private Record poolRecord_;

     // This table holds the data to be set on the API.
     private transient Hashtable changesTable_;

     /**
      * Constructs a SystemPool object.
      **/
     public SystemPool()
     {
     }

     /**
      * Constructs a SystemPool object, to represent a shared system pool.
      *
      * @param system The system.
      * @param poolName The name of the shared system pool.
      **/
     public SystemPool(AS400 system, String poolName)
     {
         if (system == null)
            throw new NullPointerException ("system");
         if (poolName == null)
            throw new NullPointerException ("poolName");

         system_ = system;
         poolName_ = poolName;
         isSharedPool_ = true;
     }

     /**
      * Constructs a SystemPool object, to represent a subsystem (private) pool.
      *
      * @param system The system.
      * @param subsystemLibrary The name of the library where the subsystem resides.
      * @param subsystemName The name of the subsystem that "owns" the pool.
      * @param sequenceNumber The ID number of the pool (a value from 1 to 10).
      * @param size The size of the system pool, in kilobytes.
      * @param activityLevel The activity level of the pool.
      **/
     public SystemPool(Subsystem subsystem, int sequenceNumber, int size, int activityLevel)
     {
       if (subsystem == null) throw new NullPointerException ("subsystem");

       system_ = subsystem.getSystem();
       poolName_ = Integer.toString(sequenceNumber);
       subsystemLibrary_ = subsystem.getLibrary();
       subsystemName_ = subsystem.getName();
       poolSequenceNumber_ = sequenceNumber;  // pool ID within the subsystem (1-10)
       isSharedPool_ = false;  // not a (shared) system pool

       cacheChanges_ = true; // don't send values to the system yet
       try {
         set("poolSize", new Integer(size));
         set("activityLevel", new Integer(activityLevel));
       }
       catch (Exception e) { // should never happen, no system contact
         Trace.log(Trace.ERROR, e);
         throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
       }
       cacheChanges_ = false;
     }

     /**
      * Adds a listener to be notified when the value of any bound property
      * changes.
      *
      * @param listener The listener.
      **/
     public void addPropertyChangeListener(PropertyChangeListener listener)
     {
       if (listener == null)
       {
         throw new NullPointerException("listener");
       }
       if (changes_ == null) changes_ = new PropertyChangeSupport(this);
       changes_.addPropertyChangeListener(listener);
     }

     /**
      * Adds a listener to be notified when the value of any constrained
      * property changes.
      *
      * @param listener The listener.
      **/
     public void addVetoableChangeListener(VetoableChangeListener listener)
     {
       if (listener == null)
       {
         throw new NullPointerException("listener");
       }
       if (vetos_ == null) vetos_ = new VetoableChangeSupport(this);
       vetos_.addVetoableChangeListener(listener);
     }

    /**
     * Commits any cached system pool information changes to the server.
     * If caching is not enabled, this method does nothing.
     * @see #isCaching
     * @see #refreshCache
     * @see #setCaching
     * @exception AS400Exception If the server returns an error
     *            message.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ConnectionDroppedException If the connection is dropped
     *            unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the object does not exist on the server.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public synchronized void commitCache()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
      // If there are no changes, don't do anything!
      if (changesTable_ == null || changesTable_.isEmpty()) return;

      if (!connected_) connect();

      int poolIdentifier;
      boolean gotPoolIdentifier = false;
      try {
        poolIdentifier = getIdentifier();
        gotPoolIdentifier = true;
      }
      catch (ObjectDoesNotExistException e) {
        // This probably indicates that it's a shared pool that's not currently in use by any subsystem.
        if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn()) {
          Trace.log(Trace.DIAGNOSTIC, "No pool identifier is assigned to pool.", e);
        }
      }

      String messageLogging_pending = null;  // We might need to leave this change uncommitted, if we end up calling CHGSHRPOOL.

      // Note: "Shared" pools that are not currently in use by any subsystem, do _not_ have a system pool identifier assigned, and therefore are beyond the reach of QUSCHGPA.  To modify such pools, we must use CHGSHRPOOL.

      if (!gotPoolIdentifier && isSharedPool_)
      { // We need to use the CL command, since the API requires a (unique) pool ID.

        StringBuffer cmdBuf = new StringBuffer("QSYS/CHGSHRPOOL POOL("+poolName_+")");
        Object obj;  // attribute value

        obj = changesTable_.get("poolSize");
        if (obj != null) {
          cmdBuf.append(" SIZE("+obj+")");
        }

        obj = changesTable_.get("activityLevel");
        if (obj != null) {
          cmdBuf.append(" ACTLVL("+obj+")");
        }

        obj = changesTable_.get("pagingOption");
        if (obj != null) {
          cmdBuf.append(" PAGING("+obj+")");
        }

        obj = changesTable_.get("priority");
        if (obj != null) {
          cmdBuf.append(" PTY("+obj+")");
        }

        obj = changesTable_.get("minimumPoolSize");
        if (obj != null) {
          cmdBuf.append(" MINPCT("+obj+")");
        }

        obj = changesTable_.get("maximumPoolSize");
        if (obj != null) {
          cmdBuf.append(" MAXPCT("+obj+")");
        }

        obj = changesTable_.get("minimumFaults");
        if (obj != null) {
          cmdBuf.append(" MINFAULT("+obj+")");
        }

        obj = changesTable_.get("perThreadFaults");
        if (obj != null) {
          cmdBuf.append(" JOBFAULT("+obj+")");
        }

        obj = changesTable_.get("maximumFaults");
        if (obj != null) {
          cmdBuf.append(" MAXFAULT("+obj+")");
        }

        if (DEBUG) System.out.println("Running command: " + cmdBuf.toString());
        CommandCall cmd = new CommandCall(system_, cmdBuf.toString());
        if (!cmd.run()) {
          throw new AS400Exception(cmd.getMessageList());
        }

        // See if we were asked to change the message logging attribute.
        // The CL command doesn't have a "message logging" parameter.
        messageLogging_pending = (String)changesTable_.get("messageLogging");

        // Future enhancement: The CL command also has a TEXT() parameter, which can be specified to change the pool's text description.
      }

      else // The pool identifier is known, so we can use the API.
      {
        QSYSObjectPathName prgName = new QSYSObjectPathName("QSYS","QUSCHGPA","PGM");
        AS400Bin4 bin4 = new AS400Bin4();
        AS400Text text;

        ProgramParameter[] parmList = new ProgramParameter[12];

        parmList[0] = new ProgramParameter(bin4.toBytes(getIdentifier()));

        Object obj = changesTable_.get("poolSize");
        if (obj == null) obj = NO_CHANGE;
        parmList[1] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("activityLevel");
        if (obj == null) obj = NO_CHANGE;
        parmList[2] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("messageLogging");
        if (obj == null) obj = "Y"; // no change
        AS400Text text1 = new AS400Text(1, system_.getCcsid(), system_);
        parmList[3] = new ProgramParameter(text1.toBytes(obj));

        byte[] errorInfo = new byte[32];
        parmList[4] = new ProgramParameter(errorInfo, 0); // don't care about error info

        obj = changesTable_.get("pagingOption");
        if (obj == null) obj = "*SAME"; // no change
        AS400Text text10 = new AS400Text(10, system_.getCcsid(), system_);
        parmList[5] = new ProgramParameter(text10.toBytes(obj));

        obj = changesTable_.get("priority");
        if (obj == null) obj = NO_CHANGE;
        parmList[6] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("minimumPoolSize");
        if (obj == null) obj = NO_CHANGE;
        parmList[7] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("maximumPoolSize");
        if (obj == null) obj = NO_CHANGE;
        parmList[8] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("minimumFaults");
        if (obj == null) obj = NO_CHANGE;
        parmList[9] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("perThreadFaults");
        if (obj == null) obj = NO_CHANGE;
        parmList[10] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("maximumFaults");
        if (obj == null) obj = NO_CHANGE;
        parmList[11] = new ProgramParameter(bin4.toBytes(obj));

        ProgramCall pgm = new ProgramCall(system_);
        pgm.setThreadSafe(false);  // QUSCHGPA isn't threadsafe.   @B2A @B3C

        try
        {
          pgm.setProgram(prgName.getPath(), parmList);
        }
        catch(PropertyVetoException pve) {} // Quiet the compiler

        if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
        {
          Trace.log(Trace.DIAGNOSTIC, "Setting system pool information.");
        }
        if (pgm.run() != true)
        {
          AS400Message[] msgList = pgm.getMessageList();
          if (Trace.isTraceOn() && Trace.isTraceErrorOn())
          {
            Trace.log(Trace.ERROR, "Error setting system pool information:");
            for (int i=0; i<msgList.length; ++i)
            {
              Trace.log(Trace.ERROR, msgList[i].toString());
            }
          }
          throw new AS400Exception(msgList);
        }
      }

      changesTable_.clear();  // clear all pending changes

      if (messageLogging_pending != null) {
        // Put the (uncommitted) messageLogging value back in to the changes table.
        changesTable_.put("messageLogging", messageLogging_pending);
        Trace.log(Trace.ERROR, "Unable to update 'message logging' attribute, since shared pool " + poolName_ + " is not in use.  The change remains pending.");
      }
    }

    /**
     * Connects to the 400 by loading system status information.
     * Does nothing if we have already connected.
     *
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the object does not exist on the server.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    private void connect()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      if (system_ == null)
      {
        Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
        throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      connected_ = true;
    }


  /**
   Determines whether this SystemPool object is equal to another object.
   @return <tt>true</tt> if the two instances are equal
   **/
  public boolean equals(Object obj)
  {
    try
    {
      SystemPool other = (SystemPool)obj;

      if (system_ == null) {
        if (other.getSystem() != null) return false;
      }
      else if (!system_.equals(other.getSystem())) return false;

      if (poolName_ == null) {
        if (other.getName() != null) return false;
      }
      else if (!poolName_.equals(other.getName())) return false;

      if (!isSharedPool_) {  // it's a subsystem pool
        if (!subsystemLibrary_.equals(other.getSubsystemLibrary())) return false;
        if (!subsystemName_.equals(other.getSubsystemName())) return false;
      }
      else if (other.isShared()) return false;
      return true;
    }
    catch (Throwable e) {
      return false;
    }
  }


    /**
     * Gets the value for the specified field out of the
     * appropriate record in the format cache. If the particular
     * format has not been loaded yet, it is loaded from the 400.
    **/
    private Object get(String field)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
      // Retrieve the info out of our own record
      Object obj = null;
      if (!cacheChanges_) {
        refreshCache(); // Load the info if we're not caching.
      }
      else {
        if (changesTable_ != null) obj = changesTable_.get(field);
      }

      if (obj == null)
      {
        retrieveInformation();  // this will do the connect() if needed
        obj = poolRecord_.getField(field);
      }
      return obj;
    }


     /**
      * Returns the rate, in transitions per minute, of transitions
      * of threads from an active condition to an ineligible condition.
      *
      * @return The rate.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
     **/
     public float getActiveToIneligible()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (float)((Integer)get("activeToIneligible")).intValue()/(float)10.0;
     }

     /**
      * Returns the rate, in transitions per minute, of transitions
      * of threads from an active condition to a waiting condition.
      *
      * @return The rate.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public float getActiveToWait()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (float)((Integer)get("activeToWait")).intValue()/(float)10.0;
     }

     /**
      * Returns the rate, shown in page faults per second, of
      * database page faults against pages containing either database data
      * or access paths. A page fault is a program notification that occurs
      * when a page that is marked as not in main storage is referred to be
      * an active program. An access path is the means by which the system
      * provides a logical organization to the data in a database file.
      *
      * @return The rate.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public float getDatabaseFaults()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (float)((Integer)get("databaseFaults")).intValue()/(float)10.0;
     }

     /**
      * Returns the rate, in pages per second, at which database
      * pages are brought into the storage pool.
      *
      * @return The rate.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public float getDatabasePages()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (float)((Integer)get("databasePages")).intValue()/(float)10.0;
     }

     /**
      * Returns the description of the system pool.
      *
      * @return The description of the system pool.
      **/
     public String getDescription()
     {
         if(poolName_.trim().equals("*MACHINE"))
            return ResourceBundleLoader.getText("SYSTEM_POOL_MACHINE");
         else if(poolName_.trim().equals("*BASE"))
            return ResourceBundleLoader.getText("SYSTEM_POOL_BASE");
         else if(poolName_.trim().equals("*INTERACT"))
            return ResourceBundleLoader.getText("SYSTEM_POOL_INTERACT");
         else if(poolName_.trim().equals("*SPOOL"))
            return ResourceBundleLoader.getText("SYSTEM_POOL_SPOOL");
         else
            return ResourceBundleLoader.getText("SYSTEM_POOL_OTHER");
     }

     /**
      * Returns the maximum number of threads that can be active in the pool
      * at any one time.
      *
      * @return The maximum number of threads.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @deprecated Use getActivityLevel() instead.
      **/
     public int getMaximumActiveThreads()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return getActivityLevel();
     }


     /**
      * Returns the activity level for the pool.  This is the maximum number of
      * threads that can be active in the pool at any one time.
      *
      * @return The pool activity level.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public int getActivityLevel()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return ((Integer)get("activityLevel")).intValue();
     }

     /**
      * Returns the rate, in page faults per second, of
      * nondatabase page faults against pages other than those designated
      * as database pages.
      *
      * @return The rate.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public float getNonDatabaseFaults()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (float)((Integer)get("nonDatabaseFaults")).intValue()/(float)10.0;
     }

     /**
      * Returns the rate, in page per second, at which non-database
      * pages are brought into the storage pool.
      *
      * @return The rate.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public float getNonDatabasePages()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (float)((Integer)get("nonDatabasePages")).intValue()/(float)10.0;
     }

     /**
      * Returns the value indicating whether the system will dynamically
      * adjust the paging characteristics of the storage pool for optimum
      * performance. The following special values may be returned.
      *
      *   *FIXED:   The system does not dynamically adjust the paging
      *             characteristics.
      *   *CALC:    The system dynamically adjusts the paging
      *             characteristics.
      *   USRDFN:   The system does not dynamically adjust the paging
      *             characteristics for the storage pool but uses values
      *             that have been defined through an API.
      *
      * @return The value indicating whether the system will dynamically adjust
      *         the paging characteristics of the storage pool for optimum performance.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public String getPagingOption()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (String)get("pagingOption");
     }

     /**
      * Returns the pool identifier.
      *
      * @return The pool identifier.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @deprecated Use getIdentifier() instead.
      **/
     public int getPoolIdentifier()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return getIdentifier();
     }

     /**
      * Returns the pool identifier.
      *
      * @return The pool identifier.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public int getIdentifier()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       if (poolIdentifier_ == null) {
         synchronized(this) {
           if (poolIdentifier_ == null) {
             boolean oldVal = cacheChanges_;
             cacheChanges_ = true;
             poolIdentifier_ = (Integer)get("poolIdentifier");
             cacheChanges_ = oldVal;
           }
         }
       }
       return poolIdentifier_.intValue();
     }

     /**
      * Returns the name of this storage pool. The name may be a number, in
      * which case it is a private pool associated with a subsystem.
      * The following special values may be returned:
      *<p>
      *<li> *MACHINE  The specified pool definition is defined to be the
      *   machine pool.
      *<li> *BASE     The specified pool definition is defined to be the base
      *   system pool, which can be shared with other subsystems.
      *<li> *INTERACT The specified pool definition is defined to be shared
      *   pool used for interactive work.
      *<li> *SPOOL    The specified pool definition is defined to be the
      *   shared pool used for spooled writers.
      *<li> *SHRPOOL1 - *SHRPOOL10  The specified pool definition is defined
      *   to be a shared pool. For v4r3, this is *SHRPOOL60.
      *</p>
      *
      * @return The pool name.
      * @deprecated Use getName() instead.
      **/
     public String getPoolName()
     {
       //return (String)get("poolName");
       return getName();
     }

     /**
      * Returns the name of this storage pool. The name may be a number, in
      * which case it is a private pool associated with a subsystem.
      * The following special values may be returned:
      *<p>
      *<li> *MACHINE  The specified pool definition is defined to be the
      *   machine pool.
      *<li> *BASE     The specified pool definition is defined to be the base
      *   system pool, which can be shared with other subsystems.
      *<li> *INTERACT The specified pool definition is defined to be shared
      *   pool used for interactive work.
      *<li> *SPOOL    The specified pool definition is defined to be the
      *   shared pool used for spooled writers.
      *<li> *SHRPOOL1 - *SHRPOOL10  The specified pool definition is defined
      *   to be a shared pool. For v4r3, this is *SHRPOOL60.
      *</p>
      *
      * @return The pool name.
      **/
     public String getName()
     {
       //return (String)get("poolName");
       return poolName_;
     }

     /**
      * Returns the amount of main storage, in kilobytes, in the pool.
      *
      * @return The pool size.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @deprecated Use getSize() instead.
      **/
     public int getPoolSize()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return getSize();
     }

     /**
      * Returns the amount of main storage, in kilobytes, in the pool.
      *
      * @return The pool size.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public int getSize()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return ((Integer)get("poolSize")).intValue();
     }

     /**
      * Returns the amount of storage, in kilobytes, in the pool reserved for
      * system use.  (For example, for save and restore operations.)  The system
      * calculates this amount by using storage pool sizes and activity levels.
      *
      * @return The reserved size.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public int getReservedSize()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return ((Integer)get("reservedSize")).intValue();
     }

     /**
      * Returns the library of the subsystem with which this storage pool is associated.
      * The field will be blank for shared pools.
      *
      * @return The subsystem library.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public String getSubsystemLibrary()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (String)get("subsystemLibraryName");
     }

     /**
      * Returns the subsystem with which this storage pool is associated.
      * The field will be blank for shared pools.
      *
      * @return The subsystem name.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public String getSubsystemName()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (String)get("subsystemName");
     }

     /**
      * Returns the system.
      *
      * @return The system.
      **/
     public AS400 getSystem()
     {
       return system_;
     }

     /**
      * Returns the rate, in transitions per minute, of transitions
      * of threads from a waiting condition to an ineligible condition.
      *
      * @return The rate.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public float getWaitToIneligible()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
     {
       return (float)((Integer)get("waitToIneligible")).intValue()/(float)10.0;
     }

    /**
     * Returns the current cache status.
     * The default behavior is no caching.
     * @return true if caching is enabled, false otherwise.
     * @see #commitCache
     * @see #refreshCache
     * @see #setCaching
    **/
    public boolean isCaching()
    {
      return cacheChanges_;
    }


    /**
     * Indicates whether the pool is a shared system pool, as opposed to a subsystem (private) pool.
     * @return true if it's a shared system pool, false if it's a subsystem pool.
    **/
    public boolean isShared()
    {
      return isSharedPool_;
    }


     /**
      * Loads the system pool information. The system and the system pool
      * name should be set before this method is invoked.
      *
      * Note: This method is equivalent to the refreshCache() method.
      *
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
     **/
     public void loadInformation()
            throws AS400SecurityException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
     {
       refreshCache();
     }

    /*
     * Refreshes the current system pool information. The
     * currently cached data is cleared and new data will
     * be retrieved from the system when needed. That is,
     * after a call to refreshCache(), a call to one of the get() or set()
     * methods will go to the system to retrieve or set the value.
     *
     * Any cached (uncommitted) changes will be lost if not committed by a prior call
     * to commitCache().
     *
     * If caching is not enabled, this method does nothing.
     * @see #commitCache
     * @see #isCaching
     * @see #setCaching
    **/
    public synchronized void refreshCache()
    {
      // The next time a get() is done, the record data
      // will be reloaded.
      poolRecord_ = null;

      // Clear the current cache of (uncommitted) changes.
      if (changesTable_ != null) changesTable_.clear();
    }

     /**
      * Removes a property change listener.
      *
      * @param listener The listener.
      **/

     public void removePropertyChangeListener(PropertyChangeListener listener)
     {
        if(listener==null)
           throw new NullPointerException("listener");
        if (changes_ != null) changes_.removePropertyChangeListener(listener);
     }

     /**
      * Removes a vetoable change listener.
      *
      * @param listener The listener.
      **/
     public void removeVetoableChangeListener(VetoableChangeListener listener)
     {
        if(listener==null)
           throw new NullPointerException("listener");
        if (vetos_ != null) vetos_.removeVetoableChangeListener(listener);
     }

  /**
   * Loads system pool data from the server using the SSTS0300 format.
   * If the information is cached, this method does nothing.
  **/
  private void retrieveInformation()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    // Check to see if the format has been loaded already
    if (poolRecord_ != null) return;

    if (!connected_) connect();

    QSYSObjectPathName prgName = new QSYSObjectPathName("QSYS","QWCRSSTS","PGM");
    AS400Bin4 bin4 = new AS400Bin4();
    AS400Text text;

    ProgramParameter[] parmList = new ProgramParameter[5];

    if (format0300_ == null) format0300_ = new SSTS0300Format(system_);

    int receiverLength = format0300_.getNewRecord().getRecordLength();
    parmList[0] = new ProgramParameter(receiverLength);
    parmList[1] = new ProgramParameter(bin4.toBytes(receiverLength));

    text = new AS400Text(8, system_.getCcsid(), system_);
    parmList[2] = new ProgramParameter(text.toBytes(format0300_.getName()));

    // Reset statistics parm
    text = new AS400Text(10, system_.getCcsid(), system_);
    parmList[3] = new ProgramParameter(text.toBytes("*NO")); // this parm is ignored for SSTS0100

    byte[] errorInfo = new byte[32];
    parmList[4] = new ProgramParameter(errorInfo, 0);

    ProgramCall pgm = new ProgramCall(system_);
    pgm.setThreadSafe(false);  // QWCRSSTS isn't threadsafe.     @B2A @B3C
    try
    {
      pgm.setProgram(prgName.getPath(), parmList);
    }
    catch(PropertyVetoException pve) {} // Quiet the compiler

    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Retrieving system pool information.");
    }
    if (pgm.run() != true)
    {
      AS400Message[] msgList = pgm.getMessageList();
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.ERROR, "Error retrieving system pool information:");
        for (int i=0; i<msgList.length; ++i)
        {
          Trace.log(Trace.ERROR, msgList[i].toString());
        }
      }
      throw new AS400Exception(msgList);
    }
    byte[] retrievedData = parmList[0].getOutputData();
    Record rec = format0300_.getNewRecord(retrievedData);
    // It's possible we didn't retrieve all of the pools.
    // We may need to increase the fields in the record format to
    // hold more pool information and then do the API call again.
    if (resizeFormat(rec))
    {
      // Now hopefully we have a big enough byte array.
      // Try the API call again.
      retrieveInformation();
      return;
    }

    // Now determine which system pool we are out of the list
    // This is the data returned on the API.
    int offsetToInfo = ((Integer)rec.getField("offsetToPoolInformation")).intValue();
    int numPools = ((Integer)rec.getField("numberOfPools")).intValue();
    int entryLength = ((Integer)rec.getField("lengthOfPoolInformationEntry")).intValue();
    byte[] data = rec.getContents();
    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Parsing out "+numPools+" system pools with "+entryLength+" bytes each starting at offset "+offsetToInfo+" for a maximum length of "+data.length+".");
    }

    if (poolFormat_ == null) poolFormat_ = new PoolInformationFormat(system_);

    // Get each of the pools out of the data and check to see which one is us.
    String poolName = ( poolName_ == null ? null : poolName_.trim() );
    for (int i=0; i<numPools; ++i)
    {
      int offset = offsetToInfo + i*entryLength;
      Record pool = poolFormat_.getNewRecord(data, offset);
      if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Parsed pool at offset "+offset+": "+pool.toString());
      }
      String ret = ((String)pool.getField("poolName")).trim();

      if (isSharedPool_)
      { // It's a shared system pool, so it's uniquely identified by the pool name.
        if (DEBUG) {
          System.out.println("Looking for poolName=="+poolName+", got: " + ret);
        }
        if (ret.equals(poolName))
        {
          if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
          {
            Trace.log(Trace.DIAGNOSTIC, "Found matching system pool '"+poolName+"'");
          }

          poolRecord_ = pool;
          return;
        }
      }
      else
      { // It's a subsystem pool, so poolName is actually just a sequence number (1-10).
        // Need to match subsystem library and name, and poolName.
        String subsysName = ((String)pool.getField("subsystemName")).trim();
        String subsysLib = ((String)pool.getField("subsystemLibraryName")).trim();

        if (subsysName.equalsIgnoreCase(subsystemName_) &&
            subsysLib.equalsIgnoreCase(subsystemLibrary_))
        {
          // We've matched the subsystem.  Now match the poolName to the poolID.
          if (DEBUG) System.out.println("Matched the subsystem.  Looking for subsys pool ID " + poolSequenceNumber_ + ", got " + poolName);
          if (Integer.parseInt(poolName) == poolSequenceNumber_) {
            poolRecord_ = pool;
            return;
          }
          else {
            if (DEBUG) {
              System.out.println("Mismatched subystem library/name.  Expected: " + subsystemLibrary_+"/"+subsystemName_ + ", got " + subsysLib+"/"+subsysName + " (poolName == " + ret + ")");
            }
            continue;  // not a match, so keep looking
          }
        }
      }
    }  // 'for' loop

    Trace.log(Trace.ERROR, "System pool '"+poolName_+"' not found.");
    throw new ObjectDoesNotExistException(poolName_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
  }

  /**
   * Adjusts the 0300 format to be large enough to hold all of the
   * information returned on the API call.
   * @return true if the format was resized.
  **/
  private boolean resizeFormat(Record rec)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    int available = ((Integer)rec.getField("numberOfBytesAvailable")).intValue();
    int returned = ((Integer)rec.getField("numberOfBytesReturned")).intValue();
    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Size check of SSTS0300 format: "+available+", "+returned);
      int numPools = ((Integer)rec.getField("numberOfPools")).intValue();
      int offset = ((Integer)rec.getField("offsetToPoolInformation")).intValue();
      int entryLength = ((Integer)rec.getField("lengthOfPoolInformationEntry")).intValue();
      Trace.log(Trace.DIAGNOSTIC, "  Old pool information: "+numPools+", "+offset+", "+entryLength+", "+rec.getRecordLength());
    }
    // Only resize the format if we didn't get all the info.
    // The resize should happen no more than once per API call.
    if (available > returned)
    {
      int numPools = ((Integer)rec.getField("numberOfPools")).intValue();
      int offset = ((Integer)rec.getField("offsetToPoolInformation")).intValue();
      int entryLength = ((Integer)rec.getField("lengthOfPoolInformationEntry")).intValue();
      if (format0300_ == null) format0300_ = new SSTS0300Format(system_);
      // Make the byte array big enough to hold the pool information.
      int baseLength = format0300_.getNewRecord().getRecordLength();
      int newLength = numPools*entryLength + (offset-baseLength);
      format0300_.addFieldDescription(new HexFieldDescription(new AS400ByteArray(newLength), "poolInformation"));
      if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Resizing SSTS0300 format to hold more system pool information.");
        Trace.log(Trace.DIAGNOSTIC, "  New pool information: "+baseLength+", "+newLength+", "+format0300_.getNewRecord().getRecordLength());
      }
      return true;
    }
    return false;
  }

    /**
     * Sets the value for the specified field to value in the
     * appropriate record in the format cache.
    **/
    private void set(String field, Object value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException

    {
      // Set the info into our own record
      Object oldValue = null;
      if ((vetos_ != null || changes_ != null) &&
          changesTable_ != null) {
        oldValue = changesTable_.get(field);
      }
      if (vetos_ != null) vetos_.fireVetoableChange(field, oldValue, value);
      if (changesTable_ == null) changesTable_ = new Hashtable(11);
      changesTable_.put(field, value);
      if (changes_ != null) changes_.firePropertyChange(field, oldValue, value);
      if (!cacheChanges_) commitCache();
    }


    /**
     * Turns caching on or off.
     * If caching is turned off, the next get() or set() will go to the system.
     * @param cache true if caching should be used when getting
     *              and setting information to and from the server; false
     *              if every get or set should communicate with the server
     *              immediately. Any cached changes that are not committed
     *              when caching is turned off will be lost.
     *              The default behavior is no caching.
     * @see #commitCache
     * @see #isCaching
     * @see #refreshCache
    **/
    public synchronized void setCaching(boolean cache)
    {
      cacheChanges_ = cache;
    }


     /**
      * Sets the minimum faults-per-second guideline, the faults per second for each active thread,
      * and the maximum faults-per-second guideline for this storage pool.
      * The sum of minimum faults and per-thread faults must be less than the
      * value of the maximum faults parameter. Each value is used by the
      * system if the performance adjustment (QPFRADJ) system value is set to
      * 2 or 3. If you want the system to calculate the priority, you must
      * specify SystemPool.CALCULATE for each parameter.
      * <br>Note: This method is not supported for private (subsystem) pools.
      *
      * @param minValue The new minimum faults-per-second guideline.
      * @param perValue The new faults per second for each active thread.
      * @param maxValue The new maximum faults-per-second guideline.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @see #isShared
      **/
     public void setFaults(float minValue, float perValue, float maxValue)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       if (!isSharedPool_) throwUnsupported();
       if (!cacheChanges_)
       {
         synchronized(this)
         {
           cacheChanges_ = true;
           setMinimumFaults(minValue);
           setPerThreadFaults(perValue);
           setMaximumFaults(maxValue);
           commitCache();
           cacheChanges_ = false;
         }
       }
       else
       {
         setMinimumFaults(minValue);
         setPerThreadFaults(perValue);
         setMaximumFaults(maxValue);
       }
     }

     /**
      * Sets the maximum faults-per-second guideline to use for this storage
      * pool. The sum of minimum faults and per-thread faults must be less than the
      * value of the maximum faults parameter. This value is used by the
      * system if the performance adjustment (QPFRADJ) system value is set to
      * 2 or 3. If you want the system to calculate the priority, you must
      * specify SystemPool.CALCULATE for this parameter.
      * <br>Note: This method is not supported for private (subsystem) pools.
      *
      * @param value The new maximum faults-per-second guideline.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @see #isShared
      **/
     public void setMaximumFaults(float value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       if (!isSharedPool_) throwUnsupported();
       Integer maxFaults;
       if (value == CALCULATE) maxFaults = CALCULATE_OBJ;
       else maxFaults = new Integer((int)(value*100));

       set("maximumFaults", maxFaults);
     }

     /**
      * Sets the maximum amount of storage to allocate to this storage pool
      * (as a percentage of total main storage). This value cannot be
      * less than the minimum pool size % parameter value. This value is used
      * by the system if the performance adjustment (QPFRADJ) system value
      * is set to 2 or 3. If you want the system to calculate the priority,
      * you must specify SystemPool.CALCULATE for this parameter.
      * <br>Note: This method is not supported for private (subsystem) pools.
      *
      * @param value The new maximum pool size.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @see #isShared
      **/
     public void setMaximumPoolSize(float value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       if (!isSharedPool_) throwUnsupported();
       Integer obj;
       if (value == CALCULATE) obj = CALCULATE_OBJ;
       else obj = new Integer((int)(value*100));

       set("maximumPoolSize", obj);
     }

     /**
      * Sets the value indicating whether messages reporting that a change was
      * made are written to the current job's job log and to the QHST message
      * log. This affects the logging of change-related messages only; it does
      * not affect the logging of error messages. Valid values are:
      *<p>
      *<li> true - Log change messages.
      *<li> false - Do not log change messages.
      *</p>
      *    The default value for messages logging is true.
      *
      * @param log The value indicating whether messages reporting that a
      *              change was made are written to the current job's job log
      *              and to the QHST message log.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public void setMessageLogging(boolean log)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       set("messageLogging", log ? "Y" : "N");
     }

     /**
      * Sets the minimum faults-per-second guideline to use for this storage
      * pool. This value is used by the system if the performance adjustment
      * (QPFRADJ) system value is set to 2 or 3. If you want the system to
      * calculate the priority, you must specify -2 for this parameter. If
      * you do not want this value to change, you may specify -1 for this
      * parameter.
      * <br>Note: This method is not supported for private (subsystem) pools.
      *
      * @param value The new minumum faults-per-second guideline.
      *
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @see #isShared
      **/
     public void setMinimumFaults(float value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       if (!isSharedPool_) throwUnsupported();
       Integer obj;
       if (value == CALCULATE) obj = CALCULATE_OBJ;
       else obj = new Integer((int)(value*100));

       set("minimumFaults", obj);
     }

     /**
      * Sets the minimum and maximum amount of storage to allocate to this storage pool
      * (as a percentage of total main storage). Maximum value cannot be
      * less than the minimum pool size % parameter value. Each value is used
      * by the system if the performance adjustment (QPFRADJ) system value
      * is set to 2 or 3. If you want the system to calculate the priority,
      * you must specify SystemPool.CALCULATE for each parameter.
      * <br>Note: This method is not supported for private (subsystem) pools.
      *
      * @param minValue The new minimum pool size.
      * @param maxValue The new maximum pool size.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @see #isShared
      **/
     public void setMinAndMaxPoolSize(float minValue, float maxValue)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       if (!isSharedPool_) throwUnsupported();
       if (!cacheChanges_)
       {
         synchronized(this)
         {
           cacheChanges_ = true;
           setMinimumPoolSize(minValue);
           setMaximumPoolSize(maxValue);
           commitCache();
           cacheChanges_ = false;
         }
       }
       else
       {
         setMinimumPoolSize(minValue);
         setMaximumPoolSize(maxValue);
       }
     }


     /**
      * Sets the minimum amount of storage to allocate to this storage pool
      * (as a percentage of total main storage).  This value cannot be
      * greater than the maximum pool size % parameter value. This value is
      * used by the system if the performance adjustment (QPFRADJ) system
      * value is set to 2 or 3. If you want the system to calculate the
      * priority, you must specify SystemPool.CALCULATE for this parameter.
      * <br>Note: This method is not supported for private (subsystem) pools.
      *
      * @param value  The new minimum pool size.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @see #isShared
      **/

     public void setMinimumPoolSize(float value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       if (!isSharedPool_) throwUnsupported();
       Integer obj;
       if (value == CALCULATE) obj = CALCULATE_OBJ;
       else obj = new Integer((int)(value*100));

       set("minimumPoolSize", obj);
     }

     /**
      * Sets the value indicating whether the system dynamically adjust the
      * paging characteristics of the storage pool for optimum performance.
      * Valid values are:
      *<p>
      *<li> *SAME  - The paging option for the storage pool is not changed.
      *<li> *FIXED - The system will not dynamically adjust the paging
      *               characteristics; system default values are used.
      *<li> *CALC  - The system will dynamically adjust the paging
      *               characteristics.
      *
      * @param value The value indicating whether the system dynamically adjust
      *              the paging characteristics of the sorage pool for optimum
      *              performance.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public void setPagingOption(String value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       set("pagingOption", value);
     }

     /**
      * Sets the faults per second for each active thread in this storage
      * pool. Each job is comprised of one or more threads. The system multiples
      * this number by the number of active threads that it finds in the
      * pool. This result is added to the minimum faults parameter to
      * calculate the faults-per-second guideline to use for this pool. This
      * value is used by the system if the performance adjustment (QPFRADJ)
      * system value is set to 2 or 3. If you want the system to calculate
      * the priority, you must specify SystemPool.CALCULATE for this parameter.
      * <br>Note: This method is not supported for private (subsystem) pools.
      *
      * @param value The new faults.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @see #isShared
      **/
     public void setPerThreadFaults(float value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       if (!isSharedPool_) throwUnsupported();
       Integer obj;
       if (value == CALCULATE) obj = CALCULATE_OBJ;
       else obj = new Integer((int)(value*100));

       set("perThreadFaults", obj);
     }

     /**
      * Sets the activity level for the pool. The activity level of a
      * "machine" pool (*MACHINE) cannot be changed.
      *
      * @param value The new activity level for the pool.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @deprecated Use setActivityLevel() instead.
      **/
     public void setPoolActivityLevel(int value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       setActivityLevel(value);
     }

     /**
      * Sets the activity level for the pool. The activity level of a
      * "machine" pool (*MACHINE) cannot be changed.
      *
      * Recommended coding pattern:
      *  systemPool.setCaching(true);
      *  systemPool.setSize(size);
      *  systemPool.setActivityLevel(level);
      *  systemPool.commitCache();
      *
      * @param value The new activity level for the pool.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public void setActivityLevel(int value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       set("activityLevel", new Integer(value));
     }

     /**
      * Sets the system pool name.
      *
      * @param poolName The name of the system pool.
      * @exception PropertyVetoException If the change is vetoed.
      * @deprecated Use setName() instead.
      **/
     public void setPoolName(String poolName)
            throws PropertyVetoException
     {
       setName(poolName);
     }

     /**
      * Sets the system pool name.
      *
      * @param poolName The name of the system pool.
      * @exception PropertyVetoException If the change is vetoed.
      *
      **/
     public void setName(String poolName)
            throws PropertyVetoException
     {
        if (poolName == null)
            throw new NullPointerException("poolName");
        if (connected_)
            throw new ExtendedIllegalStateException("poolName",
                    ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = poolName_;
        String newValue = poolName;
        if (vetos_ != null) vetos_.fireVetoableChange("poolName", oldValue, newValue);
        poolName_ = poolName;
        if (changes_ != null) changes_.firePropertyChange("poolName", oldValue, newValue);
     }

     /**
      * Sets the size of the system pool in kilobytes, where one kilobyte is
      * 1024 bytes. The minimum value is 32 kilobytes. For V4R3 and later, the
      * minimum is 256.  To indicate that no storage or activity level is defined
      * for the pool, specify 0.
      *
      * @param value The new size of the system pool.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @deprecated Use setSize() instead.
      **/
     public void setPoolSize(int value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       setSize(value);
     }

     /**
      * Sets the size of the system pool in kilobytes, where one kilobyte is
      * 1024 bytes. The minimum value is 32 kilobytes. For V4R3 and later, the
      * minimum is 256.  To indicate that no storage or activity level is defined
      * for the pool, specify 0.
      *
      * Recommended coding pattern:
      *  systemPool.setCaching(true);
      *  systemPool.setSize(size);
      *  systemPool.setActivityLevel(level);
      *  systemPool.commitCache();
      *
      * @param value The new size of the system pool.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      **/
     public void setSize(int value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       set("poolSize", new Integer(value));
     }

     /**
      * Sets the priority of this pool relative the priority of the other
      * storage pools. Valid values are 1 through 14. The priority for the
      * *MACHINE pool must be 1. This value is used by the system if the
      * performance adjustment (QPFRADJ) system value is set to 2 or 3. If
      * you want the system to calculate the priority, you must specify
      * SystemPool.CALCULATE_INT for this parameter.
      * <br>Note: This method is not supported for private (subsystem) pools.
      *
      * @param value The new priority.
      * @exception AS400Exception If the server returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the server.
      * @exception ObjectDoesNotExistException If the object does not exist on the server.
      * @exception PropertyVetoException If the change is vetoed.
      * @exception UnsupportedEncodingException If the character encoding is
      *            not supported.
      * @see #isShared
      **/
     public void setPriority(int value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   PropertyVetoException,
                   UnsupportedEncodingException
     {
       if (!isSharedPool_) throwUnsupported();
       set("priority", new Integer(value));
     }

     /**
      * Sets the system.
      * @param system The system.
      * @exception PropertyVetoException If the change is vetoed.
      **/
     public void setSystem(AS400 system)
            throws PropertyVetoException
     {
        if (system == null)
            throw new NullPointerException("system");
        if (connected_)
            throw new ExtendedIllegalStateException("system",
                    ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        AS400 oldValue = system_;
        AS400 newValue = system;
        if (vetos_ != null) vetos_.fireVetoableChange("system", oldValue,newValue);
        system_ = system;
        if (changes_ != null) changes_.firePropertyChange("system", oldValue,newValue);
     }

     /**
      * Return the pool name.
      *
      * @return The pool name.
      **/
     public String toString()
     {
         return poolName_;
     }

     private static final void throwUnsupported()
     {
       Trace.log(Trace.ERROR, "Method not supported for private pools.");
       throw new UnsupportedOperationException();
     }

}
