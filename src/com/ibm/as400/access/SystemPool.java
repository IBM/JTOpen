///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SystemPool.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
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
 * Represents a system pool.  It provides
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
  private static final boolean DEBUG = false;

     /**
      * Indicates that the system should calculate
      * a system pool attribute.
      * @deprecated
     **/
     public static final float CALCULATE = -2;
     private static final Float CALCULATE_FLOAT = new Float(-2);

     /**
      * Indicates that the system should calculate
      * a system pool attribute.
      * @deprecated
     **/
     public static final int CALCULATE_INT = -2;

     private static final Integer CALCULATE_INTEGER = new Integer(-2);
     private static final Integer NO_CHANGE = new Integer(-1);
     private static final String DEFAULT = "*DFT";

     private AS400 system_;

     // The pool's name.  In the case of a subsystem (non-shared) pool, this field will contain a number from 1-10.
     private String poolName_;

     // The system pool identifier.  The number is assigned by the system, and is unique across the system at any given moment.  Shared system pools that are not in use by a subsystem, will not have a system pool identifier assigned; in which case this variable will be null.  Otherwise, this variable will contain a number from 1-64.
     private Integer poolIdentifier_;

     private boolean indicatedSharedPool_; // true if caller indicated it's a shared system pool; false otherwise

     // Attributes that are meaningful only if the pool is a subsystem (non-shared) pool.
     private String subsystemLibrary_;
     private String subsystemName_;
     private int poolSequenceNumber_;  // subsystem pool sequence number (within the subsystem) (1-10)

     private transient boolean connected_;
     private boolean cacheChanges_ = false;

     private transient PropertyChangeSupport changes_;
     private transient VetoableChangeSupport vetos_;

     // This format maps the API getter.
     private SystemStatusFormat systemStatusFormat_;
     // This format maps the pool information portion of the format.
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
      * Constructs a SystemPool object, to represent a <i>shared</i> system pool.
      *
      * @param system The system.
      * @param poolName The name of the shared system pool.
      * Valid values are *MACHINE, *BASE, *INTERACT, *SPOOL, and *SHRPOOL1-60.
      **/
     public SystemPool(AS400 system, String poolName)
     {
         if (system == null)
            throw new NullPointerException ("system");
         if (poolName == null)
            throw new NullPointerException ("poolName");
         if (Trace.isTraceOn() && !isValidNameForSharedPool(poolName)) {
           Trace.log(Trace.WARNING, "Invalid name for shared pool: " + poolName);
         }

         system_ = system;
         poolName_ = poolName.trim();
         indicatedSharedPool_ = true;
     }

     /**
      * Constructs a SystemPool object, to represent a <i>subsystem (non-shared)</i> pool.
      *
      * @param subsystem The subsystem that "owns" the pool.
      * @param sequenceNumber The pool's sequence number within the subsystem.
      * Valid values are 1 through 10.
      **/
     public SystemPool(Subsystem subsystem, int sequenceNumber)
     {
       if (subsystem == null) throw new NullPointerException ("subsystem");

       system_ = subsystem.getSystem();
       poolName_ = Integer.toString(sequenceNumber);
       subsystemLibrary_ = subsystem.getLibrary();
       subsystemName_ = subsystem.getName();
       poolSequenceNumber_ = sequenceNumber;  // pool sequence number within the subsystem (1-10)
       indicatedSharedPool_ = false;  // not a shared system pool
     }

     /**
      * Constructs a SystemPool object, to represent a <i>subsystem (non-shared)</i> pool.
      *
      * @param subsystem The subsystem that "owns" the pool.
      * @param sequenceNumber The pool's sequence number within the subsystem.
      * Valid values are 1 through 10.
      * @param size The size of the system pool, in kilobytes.
      * @param activityLevel The activity level of the pool.
      **/
     public SystemPool(Subsystem subsystem, int sequenceNumber, int size, int activityLevel)
     {
       this(subsystem, sequenceNumber);

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

     SystemPool(AS400 system, byte[] poolInformation) throws AS400SecurityException, IOException
     {
         this(system, new CharConverter(system.getJobCcsid(), system).byteArrayToString(poolInformation, 44, 10));
     }

     SystemPool(AS400 system, byte[] poolInformation, int poolIdentifier) throws AS400SecurityException, IOException
     {
         this(system, new CharConverter(system.getJobCcsid(), system).byteArrayToString(poolInformation, 44, 10), poolIdentifier);
     }

     /**
      * Constructs a SystemPool object, to represent a <i>shared</i> system pool.
      *
      * @param system The system.
      * @param poolName The name of the shared system pool.
      * Valid values are *MACHINE, *BASE, *INTERACT, *SPOOL, and *SHRPOOL1-60.
      * @param poolIdentifier The system pool identifier.
      * Valid values are 1 through 64.
      * @deprecated Use {@link #SystemPool(AS400,int) SystemPool(system, poolIdentifier)} instead.
      **/
     public SystemPool(AS400 system, String poolName, int poolIdentifier)
     {
         if (system == null)
            throw new NullPointerException ("system");
         if (poolName == null)
            throw new NullPointerException ("poolName");

         system_ = system;
         poolName_ = poolName;
         poolIdentifier_ = new Integer(poolIdentifier);
         indicatedSharedPool_ = true;

         if (Trace.isTraceOn() && !isValidNameForSharedPool(poolName)) {
           Trace.log(Trace.WARNING, "Invalid name for shared pool: " + poolName);
         }
     }

     /**
      * Constructs a SystemPool object, to represent a pool identified
      * by its unique system pool identifier.
      * Either a <i>shared</i> pool or a <i>non-shared</i> pool can be specified.
      *
      * @param system The system.
      * @param poolIdentifier The system pool identifier.
      * Valid values are 1 through 64.
      **/
     public SystemPool(AS400 system, int poolIdentifier)
     {
         if (system == null)
            throw new NullPointerException ("system");

         system_ = system;
         poolIdentifier_ = new Integer(poolIdentifier);
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


     // Converts a Float value to hundredths, and returns it as an Integer.
     // Assumes that obj is never null.
     private static final Integer convertToHundredths(Object obj)
     {
       float floatVal = ((Float)obj).floatValue();
       Integer obj1;

       // For some fields, negative values have special meanings.
       // It never makes sense to convert a negative value to hundredths.
       if (floatVal <= 0) obj1 = new Integer((int)floatVal);
       else obj1 = new Integer((int)(floatVal*100));

       return obj1;
     }


    /**
     * Commits any cached system pool information changes to the system.
     * If caching is not enabled, this method does nothing.
     * @see #isCaching
     * @see #refreshCache
     * @see #setCaching
     * @exception AS400Exception If the system returns an error
     *            message.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ConnectionDroppedException If the connection is dropped
     *            unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the system.
     * @exception ObjectDoesNotExistException If the object does not exist on the system.
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

      int poolIdentifier = 0;
      try {
        poolIdentifier = getIdentifier();
      }
      catch (ObjectDoesNotExistException e) {
        // This may indicate that it's a shared pool that's not currently in use by any subsystem.
        poolIdentifier = 0;
        if (Trace.isTraceOn()) {
          Trace.log(Trace.DIAGNOSTIC, "No pool identifier is assigned to pool.", e);
        }
      }

      String messageLogging_pending = null;  // We might need to leave the change uncommitted, if we end up calling CHGSHRPOOL.

      // Note: "Shared" pools that are not currently in use by any subsystem, do not have a system pool identifier assigned, and therefore are beyond the reach of QUSCHGPA.
      // QUSCHGPA works only on active pools that have been allocated by system pood ID.
      // To modify such pools, we must use CHGSHRPOOL.

      if (poolIdentifier == 0)
      {
        // We need to use CHGSHRPOOL, since QUSCHGPA requires a unique system pool identifier.
        StringBuffer cmdBuf = new StringBuffer("QSYS/CHGSHRPOOL POOL("+poolName_+")");
        Object obj;  // attribute value

        obj = changesTable_.get("poolSize");
        if (obj != null) {
          cmdBuf.append(" SIZE("+obj.toString()+")");
        }

        obj = changesTable_.get("activityLevel");
        if (obj != null) {
          if (obj.equals(CALCULATE_INTEGER)) { // this constant has been deprecated
            obj = "*SAME";
            if (Trace.isTraceOn()) {
              Trace.log(Trace.WARNING, "Setting activityLevel to SAME.");
            }
          }
          cmdBuf.append(" ACTLVL("+obj.toString()+")");
        }

        obj = changesTable_.get("pagingOption");
        if (obj != null) {
          cmdBuf.append(" PAGING("+obj.toString()+")");
        }

        obj = changesTable_.get("priority");
        if (obj != null) {
          if (obj.equals(CALCULATE_INTEGER)) {
            obj = DEFAULT;
            if (Trace.isTraceOn()) {
              Trace.log(Trace.WARNING, "Setting priority to DEFAULT.");
            }
          }
          cmdBuf.append(" PTY("+obj.toString()+")");
        }

        obj = changesTable_.get("minimumPoolSize");
        if (obj != null) {
          if (obj.equals(CALCULATE_FLOAT)) { // this constant has been deprecated
            obj = DEFAULT;
            if (Trace.isTraceOn()) {
              Trace.log(Trace.WARNING, "Setting minimumPoolSize to DEFAULT.");
            }
          }
          cmdBuf.append(" MINPCT("+obj.toString()+")");
        }

        obj = changesTable_.get("maximumPoolSize");
        if (obj != null) {
          if (obj.equals(CALCULATE_FLOAT)) {
            obj = DEFAULT;
            if (Trace.isTraceOn()) {
              Trace.log(Trace.WARNING, "Setting maximumPoolSize to DEFAULT.");
            }
          }
          cmdBuf.append(" MAXPCT("+obj.toString()+")");
        }

        obj = changesTable_.get("minimumFaults");
        if (obj != null) {
          if (obj.equals(CALCULATE_FLOAT)) {
            obj = DEFAULT;
            if (Trace.isTraceOn()) {
              Trace.log(Trace.WARNING, "Setting minimumFaults to DEFAULT.");
            }
          }
          cmdBuf.append(" MINFAULT("+obj.toString()+")");
        }

        obj = changesTable_.get("perThreadFaults");
        if (obj != null) {
          if (obj.equals(CALCULATE_FLOAT)) {
            obj = DEFAULT;
            if (Trace.isTraceOn()) {
              Trace.log(Trace.WARNING, "Setting perThreadFaults to DEFAULT.");
            }
          }
          cmdBuf.append(" JOBFAULT("+obj.toString()+")");
        }

        obj = changesTable_.get("maximumFaults");
        if (obj != null) {
          if (obj.equals(CALCULATE_FLOAT)) {
            obj = DEFAULT;
            if (Trace.isTraceOn()) {
              Trace.log(Trace.WARNING, "Setting maximumFaults to DEFAULT.");
            }
          }
          cmdBuf.append(" MAXFAULT("+obj.toString()+")");
        }

        if (DEBUG) System.out.println("Running command: " + cmdBuf.toString());
        CommandCall cmd = new CommandCall(system_, cmdBuf.toString());
        // CHGSHRPOOL is not thread safe.
        if (!CommandCall.isThreadSafetyPropertySet()) cmd.setThreadSafe(false);
        if (!cmd.run()) {
          throw new AS400Exception(cmd.getMessageList());
        }

        // See if we were asked to change the message logging attribute.
        // The CL command doesn't have a "message logging" parameter.
        messageLogging_pending = (String)changesTable_.get("messageLogging");

        // Future enhancement: The CL command also has a TEXT() parameter, which can be specified to change the pool's text description.
      }

      else // The pool identifier is known, therefore we can use the QUSCHGPA API.
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
        else if (obj.equals(CALCULATE_FLOAT)) obj = CALCULATE_INTEGER;
        else obj = convertToHundredths(obj); // the API expect units of hundredths
        parmList[7] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("maximumPoolSize");
        if (obj == null) obj = NO_CHANGE;
        else if (obj.equals(CALCULATE_FLOAT)) obj = CALCULATE_INTEGER;
        else obj = convertToHundredths(obj);
        parmList[8] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("minimumFaults");
        if (obj == null) obj = NO_CHANGE;
        else if (obj.equals(CALCULATE_FLOAT)) obj = CALCULATE_INTEGER;
        else obj = convertToHundredths(obj);
        parmList[9] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("perThreadFaults");
        if (obj == null) obj = NO_CHANGE;
        else if (obj.equals(CALCULATE_FLOAT)) obj = CALCULATE_INTEGER;
        else obj = convertToHundredths(obj);
        parmList[10] = new ProgramParameter(bin4.toBytes(obj));

        obj = changesTable_.get("maximumFaults");
        if (obj == null) obj = NO_CHANGE;
        else if (obj.equals(CALCULATE_FLOAT)) obj = CALCULATE_INTEGER;
        else obj = convertToHundredths(obj);
        parmList[11] = new ProgramParameter(bin4.toBytes(obj));

        ProgramCall pgm = new ProgramCall(system_);
        // Assumption of thread-safety defaults to false, or to the value of the "threadSafe" system property (if it has been set).

        try
        {
          pgm.setProgram(prgName.getPath(), parmList);
        }
        catch(PropertyVetoException pve) {} // Quiet the compiler

        if (Trace.isTraceOn())
        {
          Trace.log(Trace.DIAGNOSTIC, "Setting system pool information.");
        }
        if (pgm.run() != true)
        {
          AS400Message[] msgList = pgm.getMessageList();
          if (Trace.isTraceOn())
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
     *            the system.
     * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
   @return <tt>true</tt> if the two instances are equal.
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

      if (subsystemLibrary_ == null) {
        if (other.getSubsystemLibrary() != null) return false;
      }
      else if (!subsystemLibrary_.equals(other.getSubsystemLibrary())) return false;

      if (subsystemName_ == null) {
        if (other.getSubsystemName() != null) return false;
      }
      else if (!subsystemName_.equals(other.getSubsystemName())) return false;

      // If both objects have non-zero identifiers, they must match.
      if (poolIdentifier_ != null &&
          poolIdentifier_.intValue() != 0 &&
          other.getIdentifier() != 0)
      {
        if (poolIdentifier_.intValue() != other.getIdentifier()) return false;
      }

      return true;
    }
    catch (Throwable e) {
      return false;
    }
  }


    /**
     * Gets the value for the specified field out of the
     * appropriate record in the format cache.  If the particular
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((Integer)get("activeToIneligible")).floatValue()/(float)10.0;
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((Integer)get("activeToWait")).floatValue()/(float)10.0;
     }

     /**
      * Returns the rate, shown in page faults per second, of
      * database page faults against pages containing either database data
      * or access paths.  A page fault is a program notification that occurs
      * when a page that is marked as not in main storage is referred to be
      * an active program.  An access path is the means by which the system
      * provides a logical organization to the data in a database file.
      *
      * @return The rate.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((Integer)get("databaseFaults")).floatValue()/(float)10.0;
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((Integer)get("databasePages")).floatValue()/(float)10.0;
     }

     /**
      * Returns the description of the system pool.
      *
      * @return The description of the system pool.
      **/
     public String getDescription()
     {
         if(poolName_ == null) return "";
         if(poolName_.equals("*MACHINE"))
            return ResourceBundleLoader.getText("SYSTEM_POOL_MACHINE");
         else if(poolName_.equals("*BASE"))
            return ResourceBundleLoader.getText("SYSTEM_POOL_BASE");
         else if(poolName_.equals("*INTERACT"))
            return ResourceBundleLoader.getText("SYSTEM_POOL_INTERACT");
         else if(poolName_.equals("*SPOOL"))
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((Integer)get("nonDatabaseFaults")).floatValue()/(float)10.0;
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((Integer)get("nonDatabasePages")).floatValue()/(float)10.0;
     }

     /**
      * Returns the value indicating whether the system will dynamically
      * adjust the paging characteristics of the storage pool for optimum
      * performance.  The following special values may be returned.
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((String)get("pagingOption")).trim();
     }

     /**
      * Returns the system pool identifier.
      *
      * @return The system pool identifier.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      * Returns the system pool identifier.
      * The system assigns a unique identifier (from 1 through 64) to each
      * system storage pool that currently has main storage allocated.
      * If the pool is inactive, 0 is returned.
      *
      * @return The system pool identifier.  0 indicates that the pool is inactive.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return (poolIdentifier_ == null ? 0 : poolIdentifier_.intValue());
     }

     /**
      * Returns the name of this storage pool.  The name may be a number, in
      * which case it is a non-shared pool associated with a subsystem.
      * The following special values may be returned, in the case of shared pools:
      *<p>
      *<li> *MACHINE  The specified pool definition is defined to be the
      *   machine pool.
      *<li> *BASE     The specified pool definition is defined to be the base
      *   system pool, which can be shared with other subsystems.
      *<li> *INTERACT The specified pool definition is defined to be shared
      *   pool used for interactive work.
      *<li> *SPOOL    The specified pool definition is defined to be the
      *   shared pool used for spooled writers.
      *<li> *SHRPOOL1 - *SHRPOOL60  The specified pool definition is defined
      *   to be a shared pool.
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
      * Returns the name of this storage pool.  The name may be a number, in
      * which case it is a non-shared pool associated with a subsystem.
      * The following special values may be returned, in the case of shared pools:
      *<p>
      *<li> *MACHINE  The specified pool definition is defined to be the
      *   machine pool.
      *<li> *BASE     The specified pool definition is defined to be the base
      *   system pool, which can be shared with other subsystems.
      *<li> *INTERACT The specified pool definition is defined to be shared
      *   pool used for interactive work.
      *<li> *SPOOL    The specified pool definition is defined to be the
      *   shared pool used for spooled writers.
      *<li> *SHRPOOL1 - *SHRPOOL60  The specified pool definition is defined
      *   to be a shared pool.
      *</p>
      *
      * @return The pool name.  If the pool name cannot be determined, null is returned.
      **/
     public String getName()
     {
       if (poolName_ != null) return poolName_;
       else {
         try
         {
           poolName_ = ((String)get("poolName")).trim();
           return poolName_;
         }
         catch (Exception e)
         {
           if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Unable to get pool name.", e);
           return null;
         }
       }
     }

     /**
      * Returns the amount of main storage, in kilobytes, currently allocated to the pool.
      * Note: Depending on system storage availability, this may be less than
      * the pool's requested ("defined") size.
      *
      * @return The pool size, in kilobytes.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      * Returns the amount of main storage, in kilobytes, currently allocated to the pool.
      * Note: Depending on system storage availability, this may be less than
      * the pool's requested ("defined") size.
      *
      * @return The pool size, in kilobytes.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((String)get("subsystemLibraryName")).trim();
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((String)get("subsystemName")).trim();
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
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       return ((Integer)get("waitToIneligible")).floatValue()/(float)10.0;
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
     * Indicates whether the pool is a shared system pool.
     * <br>Note: In some cases, this method may incorrectly report <tt>false</tt> for a shared pool; for example, if the object was constructed using {@link #SystemPool(AS400, int) SystemPool(system, poolIdentifier)}, and the pool name is not *MACHINE, *BASE, *INTERACT, *SPOOL, or *SHRPOOL1-60.
     * @return true if it's a shared system pool, false otherwise.
    **/
    public boolean isShared()
    {
      return (indicatedSharedPool_ || isValidNameForSharedPool(poolName_)) &&
        (subsystemLibrary_ == null && subsystemName_ == null &&
         poolSequenceNumber_ == 0);
    }


     /**
      * Loads the system pool information.  The system and the system pool
      * name or identifier should be set before this method is invoked.
      *
      * Note: This method is equivalent to the refreshCache() method.
      *
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
     * Refreshes the current system pool information.  The
     * currently cached data is cleared and new data will
     * be retrieved from the system when needed.  That is,
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
   * Creates a parameter list for the call to QWCRSSTS.
  **/
  private ProgramParameter[] buildParameterList()
  {
    AS400Bin4 bin4 = new AS400Bin4();
    AS400Text text;

    // Note: If we have a pool identifier, or if the pool is identified as a "shared" pool, we can use the 7-parameter call.
    // Otherwise we must use the 5-parameter call.

    int numParms;
    if (indicatedSharedPool_ || poolIdentifier_ != null)
    {
      numParms = 7;
      if (systemStatusFormat_ == null ||
          systemStatusFormat_.getClass() != SSTS0400Format.class)
      {
        systemStatusFormat_ = new SSTS0400Format(system_);
        poolFormat_ = new PoolInformationFormat0400(system_);
      }
    }
    else
    {
      numParms = 5;
      if (systemStatusFormat_ == null ||
          systemStatusFormat_.getClass() != SSTS0300Format.class)
      {
        systemStatusFormat_ = new SSTS0300Format(system_);
        poolFormat_ = new PoolInformationFormat(system_);
      }
    }
    ProgramParameter[] parmList = new ProgramParameter[numParms];

    // Required parameters:

    // Receiver variable
    int receiverLength = systemStatusFormat_.getNewRecord().getRecordLength();
    parmList[0] = new ProgramParameter(receiverLength);

    // Length of receiver variable
    parmList[1] = new ProgramParameter(bin4.toBytes(receiverLength));
    if (DEBUG) System.out.println("QWCRSSTS parm 2: " + receiverLength);

    // Format name
    text = new AS400Text(8, system_.getCcsid(), system_);
    parmList[2] = new ProgramParameter(text.toBytes(systemStatusFormat_.getName()));
    if (DEBUG) System.out.println("QWCRSSTS parm 3: " + systemStatusFormat_.getName());

    // Reset status statistics
    text = new AS400Text(10, system_.getCcsid(), system_);
    parmList[3] = new ProgramParameter(text.toBytes("*NO"));
    // Note that this parm is ignored for formats SSTS0100 and SSTS0500.
    if (DEBUG) System.out.println("QWCRSSTS parm 4: " + "*NO");

    // Error code
    byte[] errorInfo = new byte[32];
    parmList[4] = new ProgramParameter(errorInfo, 0);
    if (DEBUG) System.out.println("QWCRSSTS parm 5: " + "0");

    if (numParms > 5)
    {
      // Optional parameters:

      // Pool selection information.
      // 3 subfields:
      // typeOfPool (CHAR10) - Possible values are:  *SHARED, *SYSTEM.
      // sharedPoolName (CHAR10) - Possible values are:  *ALL, *MACHINE, *BASE, *INTERACT, *SPOOL, *SHRPOOL1-60.  If type of pool is *SYSTEM, then this field must be blank.
      // systemPoolIdentifier (BIN4) - If typeOfPool is *SHARED, must be zero.  Otherwise: -1 for "all active pools"; 1-64 to specify an active pool.  If the pool is not active, CPF186B is sent.

      // Note: The typeOfPool field simply indicates _how_ we are identifying the pool in this API: Either by shared-pool name, or by system pool identifier.
      String typeOfPool = (indicatedSharedPool_ ? "*SHARED   " : "*SYSTEM   ");

      StringBuffer sharedPoolName = new StringBuffer(indicatedSharedPool_ ? poolName_ : "");
      if (sharedPoolName.length() < 10) {
        int numPadBytes = 10 - sharedPoolName.length();
        sharedPoolName.append(new String("          ").substring(10-numPadBytes));  // pad field to a length of 10 chars
      }

      int systemPoolIdentifier = (indicatedSharedPool_ ? 0 : poolIdentifier_.intValue());

      byte[] poolType = text.toBytes(typeOfPool);
      byte[] poolNam  = text.toBytes(sharedPoolName.toString());
      byte[] poolId = BinaryConverter.intToByteArray(systemPoolIdentifier);
      byte[] poolSelectionInformation = new byte[24];
      System.arraycopy(poolType, 0, poolSelectionInformation,  0, 10);
      System.arraycopy(poolNam,  0, poolSelectionInformation, 10, 10);
      System.arraycopy(poolId,   0, poolSelectionInformation, 20,  4);
      parmList[5] = new ProgramParameter(poolSelectionInformation);
      if (DEBUG) System.out.println("QWCRSSTS parm 6: type==|" + typeOfPool +"| , name==|"+ sharedPoolName.toString() +"| , ID==|"+ systemPoolIdentifier +"|");

      // Size of pool selection information.
      // Valid values are 0, 20, or 24
      parmList[6] = new ProgramParameter(bin4.toBytes(24));  // size is 24 bytes
      if (DEBUG) System.out.println("QWCRSSTS parm 7: " + "24");
    }

    return parmList;
  }


    /**
     * Indicates whether the pool name is a valid name for a shared system pool.
     * <br>Note: In some cases, this method may incorrectly report <tt>false</tt> for a name that actually specifies a shared pool; for example, for a shared pool with a non-standard name.
     * @return true if poolName is *MACHINE, *BASE, *INTERACT, *SPOOL, or *SHRPOOLxx; false otherwise.
    **/
  public static final boolean isValidNameForSharedPool(String name)
  {
    if (name == null) return false;
    if (name.equals("*ALL") ||
        name.equals("*MACHINE") ||
        name.equals("*BASE") ||
        name.equals("*INTERACT") ||
        name.equals("*SPOOL") ||
        name.startsWith("*SHRPOOL"))
      return true;
    else return false;
  }


  /**
   * Loads pool data from the system using the SSTS0300 or SSTS0400 format.
   * If the information is already cached, this method does nothing.
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
    ProgramParameter[] parmList = buildParameterList();

    ProgramCall pgm = new ProgramCall(system_);
    // Assumption of thread-safety defaults to false, or to the value of the "threadSafe" system property (if it has been set).
    try
    {
      pgm.setProgram(prgName.getPath(), parmList);
    }
    catch(PropertyVetoException pve) {} // Quiet the compiler

    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Retrieving system pool information.");
    }
    if (pgm.run() != true)
    {
      AS400Message[] msgList = pgm.getMessageList();
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.ERROR, "Error retrieving system pool information:");
        for (int i=0; i<msgList.length; ++i)
        {
          Trace.log(Trace.ERROR, msgList[i].toString());
        }
      }
      if ((poolIdentifier_==null) && !isValidNameForSharedPool(poolName_)) {
        Trace.log(Trace.ERROR, "Invalid name for shared pool: "+poolName_);
        throw new ObjectDoesNotExistException(poolName_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
      }
      else throw new AS400Exception(msgList);
    }
    byte[] retrievedData = parmList[0].getOutputData();
    Record rec = systemStatusFormat_.getNewRecord(retrievedData);
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

    // Now determine which system pool we are out of the list.
    // This is the data returned on the API.
    int offsetToInfo = ((Integer)rec.getField("offsetToPoolInformation")).intValue();
    int numPools = ((Integer)rec.getField("numberOfPools")).intValue();
    int entryLength = ((Integer)rec.getField("lengthOfPoolInformationEntry")).intValue();
    byte[] data = rec.getContents();
    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Parsing out "+numPools+" system pools with "+entryLength+" bytes each starting at offset "+offsetToInfo+" for a maximum length of "+data.length+".");
    }

    // Get each of the pools out of the data, and check to see which one is me.

    for (int i=0; i<numPools; ++i)
    {
      int offset = offsetToInfo + i*entryLength;
      Record pool = poolFormat_.getNewRecord(data, offset);
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Parsed pool at offset "+offset+": "+pool.toString());
      }
      String returnedName = ((String)pool.getField("poolName")).trim();
      Integer poolIdentifier = ((Integer)pool.getField("poolIdentifier"));

      // If we know the system pool identifier, just need to match that.
      if (poolIdentifier_ != null)
      {
        if (poolIdentifier_.equals(poolIdentifier))
        {
          if (Trace.isTraceOn()) {
            Trace.log(Trace.DIAGNOSTIC, "Found matching system pool '"+poolName_+"'");
          }

          poolRecord_ = pool;
          return;
        }
      }
      else  // The caller didn't specify the system pool identifier, so we need to uniquely identify the pool some other way.
      {
        if (indicatedSharedPool_)
        {
          // It's a shared system pool, so it's uniquely identified by the pool name.
          if (DEBUG) {
            System.out.println("Looking for poolName=="+poolName_+", got: " + returnedName);
          }
          if (returnedName.equals(poolName_))
          {
            if (Trace.isTraceOn()) {
              Trace.log(Trace.DIAGNOSTIC, "Found matching system pool '"+poolName_+"'");
            }

            poolRecord_ = pool;
            if (poolIdentifier_ == null) poolIdentifier_ = poolIdentifier;
            return;
          }
        }
        else  // The caller didn't indicate it's a shared pool, and didn't specify a system pool identifier.
        {
          // It's a subsystem pool, so poolName is actually the pool's sequence number within the subsystem (1-10).
          // Need to match subsystem library and name, and pool name (pool sequence number).
          String subsysName = ((String)pool.getField("subsystemName")).trim();
          String subsysLib = ((String)pool.getField("subsystemLibraryName")).trim();
          if (DEBUG) {
            System.out.println("Returned subsysLib: |" + subsysLib + "|");
            System.out.println("Returned subsysName: |" + subsysName + "|");
            System.out.println("Returned poolName: |" + returnedName + "|");
          }

          if (subsysName.equalsIgnoreCase(subsystemName_) &&
              subsysLib.equalsIgnoreCase(subsystemLibrary_))
          {
            // We've matched the subsystem.  Now match the sequence number.
            if (DEBUG) System.out.println("Matched the subsystem.  Looking for subsys pool sequence number " + poolName_ + ", got " + returnedName);
            if (returnedName.equals(poolName_))
            {
              poolRecord_ = pool;
              if (poolIdentifier_ == null) poolIdentifier_ = poolIdentifier;
              return;
            }
            else {
              if (DEBUG) {
                System.out.println("Mismatched subsystem library/name.  Expected: " + subsystemLibrary_+"/"+subsystemName_+"/"+poolName_ + ", got " + subsysLib+"/"+subsysName+"/"+returnedName + ")");
              }
              continue;  // not a match, so keep looking through the list
            }
          }
        }
      }

    }  // 'for' loop

    Trace.log(Trace.ERROR, "System pool '"+poolName_+"' not found.");
    throw new ObjectDoesNotExistException(poolName_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
  }


  /**
   * Adjusts the 0300 or 0400 format to be large enough to hold all of the
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
    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Size check of System Status format: "+available+", "+returned);
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
      // Make the byte array big enough to hold the pool information.
      int baseLength = systemStatusFormat_.getNewRecord().getRecordLength();
      int newLength = numPools*entryLength + (offset-baseLength);
      systemStatusFormat_.addFieldDescription(new HexFieldDescription(new AS400ByteArray(newLength), "poolInformation"));
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Resizing System Status format to hold more system pool information.");
        Trace.log(Trace.DIAGNOSTIC, "  New pool information: "+baseLength+", "+newLength+", "+systemStatusFormat_.getNewRecord().getRecordLength());
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
     *              and setting information to and from the system; false
     *              if every get or set should communicate with the system
     *              immediately.  Any cached changes that are not committed
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
      * value of the maximum faults parameter.  Each value is used by the
      * system if the performance adjustment (QPFRADJ) system value is set to
      * 2 or 3.
      * <br>Note: This method is supported only for shared pools,
      * not for subsystem (non-shared) pools.
      *
      * @param minValue The new minimum faults-per-second guideline.
      * @param perValue The new faults per second for each active thread.
      * @param maxValue The new maximum faults-per-second guideline.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       if (!indicatedSharedPool_) throwUnsupported();
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
      * pool.  The sum of minimum faults and per-thread faults must be less than the
      * value of the maximum faults parameter.  This value is used by the
      * system if the performance adjustment (QPFRADJ) system value is set to
      * 2 or 3.
      * <br>Note: This method is supported only for shared pools,
      * not for subsystem (non-shared) pools.
      *
      * @param value The new maximum faults-per-second guideline.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       if (!indicatedSharedPool_) throwUnsupported();
       set("maximumFaults", new Float(value));
     }

     /**
      * Sets the maximum amount of storage to allocate to this storage pool
      * (as a percentage of total main storage).  This value cannot be
      * less than the minimum pool size % parameter value.  This value is used
      * by the system if the performance adjustment (QPFRADJ) system value
      * is set to 2 or 3.
      * <br>Note: This method is supported only for shared pools,
      * not for subsystem (non-shared) pools.
      *
      * @param value The new maximum pool size.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       if (!indicatedSharedPool_) throwUnsupported();
       set("maximumPoolSize", new Float(value));
     }


     /**
      * Sets the value indicating whether messages reporting that a change was
      * made are written to the current job's job log and to the QHST message
      * log.  This affects the logging of change-related messages only; it does
      * not affect the logging of error messages.  Valid values are:
      *<p>
      *<li> true - Log change messages.
      *<li> false - Do not log change messages.
      *</p>
      *    The default value for messages logging is true.
      *
      * @param log The value indicating whether messages reporting that a
      *              change was made are written to the current job's job log
      *              and to the QHST message log.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      * pool.  This value is used by the system if the performance adjustment
      * (QPFRADJ) system value is set to 2 or 3.  If you want the system to
      * calculate the priority, you must specify -2 for this parameter.  If
      * you do not want this value to change, you may specify -1 for this
      * parameter.
      * <br>Note: This method is supported only for shared pools,
      * not for subsystem (non-shared) pools.
      *
      * @param value The new minumum faults-per-second guideline.
      *
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       if (!indicatedSharedPool_) throwUnsupported();
       set("minimumFaults", new Float(value));
     }


     /**
      * Sets the minimum and maximum amount of storage to allocate to this storage pool
      * (as a percentage of total main storage).  Maximum value cannot be
      * less than the minimum pool size % parameter value.  Each value is used
      * by the system if the performance adjustment (QPFRADJ) system value
      * is set to 2 or 3.
      * <br>Note: This method is supported only for shared pools,
      * not for subsystem (non-shared) pools.
      *
      * @param minValue The new minimum pool size.
      * @param maxValue The new maximum pool size.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       if (!indicatedSharedPool_) throwUnsupported();
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
      * greater than the maximum pool size % parameter value.  This value is
      * used by the system if the performance adjustment (QPFRADJ) system
      * value is set to 2 or 3.
      * <br>Note: This method is supported only for shared pools,
      * not for subsystem (non-shared) pools.
      *
      * @param value  The new minimum pool size.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       if (!indicatedSharedPool_) throwUnsupported();
       set("minimumPoolSize", new Float(value));
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
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      * pool.  Each job is comprised of one or more threads.  The system multiples
      * this number by the number of active threads that it finds in the
      * pool.  This result is added to the minimum faults parameter to
      * calculate the faults-per-second guideline to use for this pool.  This
      * value is used by the system if the performance adjustment (QPFRADJ)
      * system value is set to 2 or 3.
      * <br>Note: This method is supported only for shared pools,
      * not for subsystem (non-shared) pools.
      *
      * @param value The new faults.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       if (!indicatedSharedPool_) throwUnsupported();
       set("perThreadFaults", new Float(value));
     }


     /**
      * Sets the activity level for the pool.  The activity level of a
      * "machine" pool (*MACHINE) cannot be changed.
      *
      * @param value The new activity level for the pool.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      * Sets the activity level for the pool.  The activity level of a
      * "machine" pool (*MACHINE) cannot be changed.
      *
      * Recommended coding pattern:
      *  systemPool.setCaching(true);
      *  systemPool.setSize(size);
      *  systemPool.setActivityLevel(level);
      *  systemPool.commitCache();
      *
      * @param value The new activity level for the pool.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      * 1024 bytes.
      * For shared pools, this specifies the requested ("defined") size.
      * The minimum value is 256 kilobytes.
      * To indicate that no storage or activity level is defined
      * for the pool, specify 0.
      *
      * @param value The new size of the system pool.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      * 1024 bytes.
      * For shared pools, this specifies the requested ("defined") size.
      * The minimum value is 256 kilobytes.
      * To indicate that no storage or activity level is defined
      * for the pool, specify 0.
      *
      * Recommended coding pattern:
      *  systemPool.setCaching(true);
      *  systemPool.setSize(size);
      *  systemPool.setActivityLevel(level);
      *  systemPool.commitCache();
      *
      * @param value The new size of the system pool.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
      * storage pools.  Valid values are 1 through 14.  The priority for the
      * *MACHINE pool must be 1.  This value is used by the system if the
      * performance adjustment (QPFRADJ) system value is set to 2 or 3.
      * <br>Note: This method is supported only for shared pools,
      * not for subsystem (non-shared) pools.
      *
      * @param value The new priority.
      * @exception AS400Exception If the system returns an error
      *            message.
      * @exception AS400SecurityException If a security or authority error
      *            occurs.
      * @exception ConnectionDroppedException If the connection is dropped
      *            unexpectedly.
      * @exception ErrorCompletingRequestException If an error occurs before
      *            the request is completed.
      * @exception InterruptedException If this thread is interrupted.
      * @exception IOException If an error occurs while communicating with
      *            the system.
      * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
       if (!indicatedSharedPool_) throwUnsupported();
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
       StringBuffer buf = new StringBuffer(super.toString());
       if (poolName_ != null || subsystemName_ != null || poolIdentifier_ != null)
       {
         if (subsystemName_ != null) {
           buf.append("["+subsystemLibrary_+"/"+subsystemName_+"/"+poolSequenceNumber_+"]");
         }
         else if (poolName_ != null) {
           buf.append("["+poolName_+"]");
         }
         else if (poolIdentifier_ != null) {
           buf.append("[poolID="+poolIdentifier_.toString()+"]");
         }
       }
       return buf.toString();
     }

     private static final void throwUnsupported()
     {
       Trace.log(Trace.ERROR, "Method not supported for subsystem (non-shared) pools.");
       throw new UnsupportedOperationException();
     }

}
