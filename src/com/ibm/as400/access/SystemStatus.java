///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemStatus.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.access;


import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeSupport;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The SystemStatus class represents the system status on the server.
 * It provides facilities for retrieving system status information.
**/
public class SystemStatus implements Serializable //@B0C - made Serializable
{
    static final long serialVersionUID = 4L;

    private AS400 as400_;
    private Vector poolsVector_; //@B0C
//    private SystemStatusAttribute[] attributes_;
    
    transient private PropertyChangeSupport changes_; //@B0C
    transient private VetoableChangeSupport vetos_; //@B0C    
    transient private boolean connected_; //@B0A
    transient private boolean cacheChanges_; //@B6A
    
    // The formats can't be static because each SystemStatus object
    // could have different AS400 objects.
    // They are initialized in the initializeFormats() method.
    private SSTS0100Format format0100_;
    private SSTS0200Format format0200_;
    private SSTS0300Format format0300_;
    // The 0300 format changes size since the number of pools
    // isn't known until runtime.
    // Programmer's note: The 0300 format of the QWCRSSTS API is not useful for
    // our purposes.  The 0700 JobFormat had a similar problem
    // until the maximum possible size of a library list was discovered.
    // (See the JobI0700Format class.)
    // Unfortunately, no maximum possible size can be determined for this API since
    // the maximum number of storage pools has no known limit. Although, it is known
    // that as of V4R4, the maximum number of storage pools per subsystem is 10.
    
    // The fieldToFormatMap_ maps field names to their respective record formats.
    private Hashtable fieldToFormatMap_ = new Hashtable();
    private void mapFormat(SystemStatusFormat format)
    {
      String[] arr = format.getFieldNames();
      for (int i=0; i<arr.length; ++i)
      {
        fieldToFormatMap_.put(arr[i], format);
      }
    }
  
    // The formatToRecordMap_ maps record formats to the associated record
    // objects in this SystemStatus object. The system status data is stored in
    // each Record object whose key is a particular RecordFormat object.
    // Note that the Record objects that hold the values for each
    // format are not defined explicitly as members of this class.
    // They are saved as values inside the hashtable.
    private Hashtable formatToRecordMap_ = new Hashtable(3);
      
    
    /**
     * Constructs a SystemStatus object.
    **/
    public SystemStatus()
    {
      initializeTransient(); //@B0A
    }
    
    //@B0C - changed the ctor to not go to the system.
    /**
     * Constructs a SystemStatus object.
     * @param as400 The AS400 system.
    **/
    public SystemStatus(AS400 as400)
    {
        if (as400 == null)
          throw new NullPointerException("system");
        as400_ = as400;
        initializeTransient(); //@B0A
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
      changes_.addPropertyChangeListener(listener);
    }

    /**
     * Adds a listener to be notified when the value of any constrained property 
     * changes.
     *
     * @param listener The listener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener) 
    {
      if (listener == null)
      {  
        throw new NullPointerException("listener");
      }
      vetos_.addVetoableChangeListener(listener);
    }

    
    //@B0A
    /**
     * Connects to the server by loading system status information.
     * Does nothing if we have already connected.
     *
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
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
      if (as400_ == null)
      {
        Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
        throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }
      if (!connected_)
      {
        initializeFormats();
        refreshCache();
        connected_ = true;
      }
    }

    
    /**
     * Gets the value for the specified field out of the
     * appropriate record in the format cache. If the particular
     * format has not been loaded yet, it is loaded from the server.
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
      if (!connected_) connect();
      if (!cacheChanges_) refreshCache();
      SystemStatusFormat ssf = (SystemStatusFormat)fieldToFormatMap_.get(field);
      loadInformation(ssf); // Load the info.
      return ((Record)formatToRecordMap_.get(ssf)).getField(field);
    }
    
  
    /** 
     * Returns the number of completed batch jobs that produced printer
     * output that is waiting to print.
     *
     * @return The number of completed batch jobs that produced printer
     *         output that is waiting to print.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getBatchJobsEndedWithPrinterOutputWaitingToPrint()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("batchJobsEndedWithPrinterOutputWaitingToPrint")).intValue();
    }

    /** 
     * Returns the number of batch jobs that are in the process of ending.
     * This may be due to one of the following conditions: 
     * <UL>
     *   <LI>The job finishes processing normally. 
     *   <LI>The job ends before its normal completion point and is being
     *       removed from the system.
     * </UL>
     *
     * @return The number of batch jobs that are in the process of ending.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getBatchJobsEnding()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("batchJobsEnding")).intValue();
    }

    /** 
     * Returns the number of batch jobs that were submitted, but were held
     * before they could begin running.
     *
     * @return The number of batch jobs that were submitted, but were held
     *         before they could begin running.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getBatchJobsHeldOnJobQueue()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("batchJobsHeldOnAJobQueue")).intValue();
    }

    /** 
     * Returns the number of batch jobs that had started running, but are
     * now held.
     *
     * @return The number of batch jobs that had started running, but are
     *         now held.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getBatchJobsHeldWhileRunning()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("batchJobsHeldWhileRunning")).intValue();
    }

    /** 
     * Returns the number of batch jobs on job queues that have not been
     * assigned to a subsystem.
     *
     * @return The number of batch jobs on job queues that have not been
     *         assigned to a subsystem.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getBatchJobsOnUnassignedJobQueue()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("batchJobsOnAnUnassignedJobQueue")).intValue();
    }

    /** 
     * Returns the number of batch jobs currently running on the system.
     *
     * @return The number of batch jobs currently running on the system.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getBatchJobsRunning()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("batchJobsRunning")).intValue();
    }

    /** 
     * Returns the number of batch jobs waiting for a reply to a message
     * before they can continue to run.
     *
     * @return The number of batch jobs waiting for a reply to a message
     *         before they can continue to run.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getBatchJobsWaitingForMessage()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("batchJobsWaitingForMessages")).intValue();
    }

    /** 
     * Returns the number of batch jobs on the system that are currently
     * waiting to run, including those that were submitted to run at a
     * future date and time. Jobs on the job schedule that have not been
     * submitted are not included.
     *
     * @return The number of batch jobs on the system that are currently
     *         waiting to run.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getBatchJobsWaitingToRunOrAlreadyScheduled()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("batchJobsWaitingToRunOrAlreadyScheduled")).intValue();
    }

    /** 
     * Returns the amount (in number of physical processors) of current processing capacity of the partition.  For a partition sharing physical processors, this attribute represents the share of the physical processors in the pool it is executing.
     *
     * @return The amount of current processing capacity of the partition.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public float getCurrentProcessingCapacity()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return (float)((Integer)get("currentProcessingCapacity")).intValue()/(float)100.0;
    }

    /** 
     * Returns the current amount of storage in use for temporary objects.
     * This value is in millions of bytes.
     *
     * @return The current amount of storage in use for temporary objects.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getCurrentUnprotectedStorageUsed()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("currentUnprotectedStorageUsed")).intValue();
    }

    /** 
     * Returns the date and time when the status was gathered.
     * This is in system timestamp format.
     *
     * @return The date and time when the status was gathered.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public Date getDateAndTimeStatusGathered()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      byte[] date = (byte[])get("currentDateAndTime");
      DateTimeConverter converter = new DateTimeConverter(as400_);
      return converter.convert(date, "*DTS");
    }

    /** 
     * Returns the time (in seconds) that has elapsed between the
     * measurement start time and the current system time.
     *
     * @return The time (in seconds) that has elapsed between the
     *         measurement start time and the current system time.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getElapsedTime()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      // The String is in the format HHMMSS where HH is the hour, MM is the minute,
      // and SS is the second.
      String str = ((String)get("elapsedTime")).trim();
      int hours, minutes, seconds;
      try
      {
        hours = Integer.parseInt(str.substring(0,2));
        minutes = Integer.parseInt(str.substring(2,4));
        seconds = Integer.parseInt(str.substring(4,6));
      }
      catch(NumberFormatException e)
      {
        Trace.log(Trace.ERROR, "Error parsing system status elapsed time string: '"+str+"'", e);
        throw e;
      }
      return seconds + (minutes*60) + (hours*3600);
    }

    /** 
     * Returns the total number of user jobs and system jobs that are
     * currently in the system. The total includes: 
     * <UL>
     *   <LI>All jobs on job queues waiting to be processed. 
     *   <LI>All jobs currently active (being processed).
     *   <LI>All jobs that have completed running but still have output on
     *       output queues to be produced.
     * </UL>
     *
     * @return The total number of user jobs and system jobs that are
     *         currently in the system.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getJobsInSystem()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("jobsInSystem")).intValue();
    }

    /** 
     * Returns the largest amount of storage for temporary object used at any
     * one time since the last IPL. This value is in millions (M) of bytes.
     *
     * @return The largest amount of storage for temporary object used at any
     *         one time since the last IPL.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getMaximumUnprotectedStorageUsed()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("maximumUnprotectedStorageUsed")).intValue();
    }

    /** 
     * Returns the number of processors that are currently active in this partition.
     *
     * @return The number of processors that are currently active in this partition.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getNumberOfProcessors()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("numberOfProcessors")).intValue();
    }

    /** 
     * Returns the percentage of interactive performance assigned to this logical partition. This value is a percentage of the total interactive performance available to the entire physical system.
     *
     * @return The percentage of interactive performance assigned to this logical partition.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public float getPercentCurrentInteractivePerformance()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return (float)((Integer)get("percentCurrentInteractivePerformance")).intValue();
    }

    /** 
     * Returns the percentage of processor database capability that was used during the elapsed time. Database capability is the maximum CPU utilization available for database processing on this server. -1 is returned if this server does not report the amount of CPU used for database processing.
     *
     * @return The percentage of processor database capability that was used during the elapsed time.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public float getPercentDBCapability()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      int val = ((Integer)get("percentDBCapability")).intValue();
      if (val == -1) return (float)val;
      else return (float)val/(float)10.0;
    }

    /** 
     * Returns the percentage of the maximum possible addresses for
     * permanent objects that have been used.
     *
     * @return The percentage of the maximum possible addresses for
     *         permanent objects that have been used.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public float getPercentPermanentAddresses()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return (float)((Integer)get("percentPermanentAddresses")).intValue()/(float)1000.0;
    }

    /** 
     * Returns the average of the elapsed time during which the 
     * processing units were in use. 
     * @return The average of the elapsed time during which the 
     *         processing units were in use.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public float getPercentProcessingUnitUsed()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return (float)((Integer)get("percentProcessingUnitUsed")).intValue()/(float)10.0;
    }

    /** 
     * Returns the percentage of the system storage pool currently in use.
     *
     * @return The percentage of the system storage pool currently in use.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public float getPercentSystemASPUsed()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return (float)((Integer)get("percentSystemASPUsed")).intValue()/(float)10000.0;
    }
      
    /** 
     * Returns the percentage of the maximum possible addresses for
     * temporary objects that have been used. 
     *
     * @return The percentage of the maximum possible addresses for
     *         temporary objects that have been used.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public float getPercentTemporaryAddresses()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return (float)((Integer)get("percentTemporaryAddresses")).intValue()/(float)1000.0;
    } 

    /**
     * Returns the number of system pools.
     *
     * @return The number of system pools.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getPoolsNumber()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {   
      // Have to do this manually since the 0300 format is built
      // dynamically and not added to the hashtables.
      connect();
      loadInformation(format0300_);
      
      // Parse the pool data.
      // This is the data returned on the API.
      Record rec = (Record)formatToRecordMap_.get(format0300_);
      return ((Integer)rec.getField("numberOfPools")).intValue();
    }    

    /**
     * Returns the processor sharing attribute.  This attribute indicates whether this partition is sharing processors. The following values are returned:
     * <ul>
     * <li>0: Partition does not share processors.
     * <li>1: Partition shares processors (capped). The partition is limited to using its configured capacity.
     * <li>2: Partition shares processors (uncapped). The partition can use more than its configured capacity.
     * <ul>
     *
     * @return The processor sharing attribute.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getProcessorSharingAttribute()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      String str = ((String)get("processorSharingAttribute")).trim();
      try
      {
        return Integer.parseInt(str);
      }
      catch (NumberFormatException e)
      {
        Trace.log(Trace.ERROR, "Error parsing system status 'processor sharing attribute' string: '"+str+"'", e);
        throw e;
      }
    }    




    /** 
     * Returns the value indicating whether the system is in restricted state.
     *
     * @return true if the system is in restricted state; false otherwise.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public boolean getRestrictedStateFlag()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((String)get("restrictedStateFlag")).trim().equals("1");
    }

    /** 
     * Returns the server.
     *
     * @return The server.
    **/
    public AS400 getSystem()
    {
      return as400_;
    }

    /** 
     * Returns the storage capacity of the system auxiliary storage
     * pool(ASP1). This value is in millions(M) of bytes.
     *
     * @return The storage capacity of the system auxiliary storage
     *         pool(ASP1).
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getSystemASP()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("systemASP")).intValue();
    }

    /** 
     * Returns the enumeration containing a SystemPool object for each
     * system pool.
     *
     * @return The enumeration containing a SystemPool object for each
     *         system pool.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public Enumeration getSystemPools()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      if (!cacheChanges_) refreshCache();
      // If we've already retrieved the pools, just return it.
      if (poolsVector_ != null) return poolsVector_.elements();
      // Otherwise, make sure we're connected.
      connect(); //@B0A
      // Make sure the pool data has been retrieved.
      loadInformation(format0300_);
      
      // Parse the pool data.
      
      poolsVector_ = new Vector();
      // This is the data returned on the API.
      Record rec = (Record)formatToRecordMap_.get(format0300_);
      int offsetToInfo = ((Integer)rec.getField("offsetToPoolInformation")).intValue();
      int poolNumber = ((Integer)rec.getField("numberOfPools")).intValue();
      int entryLength = ((Integer)rec.getField("lengthOfPoolInformationEntry")).intValue();
      byte[] data = rec.getContents();
      // Get each of the pools out of the data and add them to the vector.
      PoolInformationFormat poolFormat = new PoolInformationFormat(as400_);
      for (int i=0; i<poolNumber; ++i)
      {
        int offset = offsetToInfo + i*entryLength;
        Record pool = poolFormat.getNewRecord(data, offset);
        SystemPool systemPool = new SystemPool(as400_, (String)pool.getField("poolName"));
        poolsVector_.addElement(systemPool);
      }
      return poolsVector_.elements();
    }

    /** 
     * Returns the total auxiliary storage (in millions of bytes) on
     * the system.
     *
     * @return The total auxiliary storage (in millions of bytes) on
     *         the system.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getTotalAuxiliaryStorage()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("totalAuxiliaryStorage")).intValue();
    }

    /** 
     * Returns the number of users currently signed on the system.
     * System request jobs and group jobs are not included in this number.
     *
     * @return The number of users currently signed on the system.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getUsersCurrentSignedOn()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("usersCurrentlySignedOn")).intValue();
    } 

    /** 
     * Returns the number of sessions that have ended with printer output
     * files waiting to print.
     *
     * @return The number of sessions that have ended with printer output
     *         files waiting to print.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getUsersSignedOffWithPrinterOutputWaitingToPrint()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("usersSignedOffWithPrinterOutputWaitingToPrint")).intValue();
    }

    /** 
     * Returns the number of user jobs that have been temporarily suspended
     * by system request jobs so that another job may be run.
     *
     * @return The number of user jobs that have been temporarily suspended
     *         by system request jobs so that another job may be run.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getUsersSuspendedBySystemRequest()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("usersSuspendedBySystemRequest")).intValue();
    }

    /** 
     * Returns the number of interactive jobs that are disconnected plus
     * the number of disconnected jobs.
     *
     * @return The number of interactive jobs that are disconnected plus
     * the number of disconnected jobs.
     * @exception AS400SecurityException If a security or authority error
     *            occurs.
     * @exception ErrorCompletingRequestException If an error occurs before
     *            the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with
     *            the server.
     * @exception ObjectDoesNotExistException If the server object does not
     *            exist.
     * @exception UnsupportedEncodingException If the character encoding is
     *            not supported.
    **/
    public int getUsersTemporarilySignedOff()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
      return ((Integer)get("usersTemporarilySignedOff")).intValue();
    }

    /**
     * Initializes the record formats used to format the data
     * to/from the API call.
    **/
    private void initializeFormats()
    {
      format0100_ = new SSTS0100Format(as400_);
      format0200_ = new SSTS0200Format(as400_);
      format0300_ = new SSTS0300Format(as400_);
      // We just add a large byte array and hope that it will
      // be big enough to hold the information. If not, we'll have to
      // resize it.
      // This sets the length for the API call.
      // Since the 0300 format is adjusting dynamically depending on the number
      // of system pools that currently exist on the server, this length could change.
      // Use an initial size of 1000 bytes for the pool information.
      // Each pool information is 84 bytes (11 bin4's and 4 char10's)
      // but the reserved field is of unknown length.
      format0300_.addFieldDescription(new HexFieldDescription(new AS400ByteArray(1000), "poolInformation"));
      
      // Map all of the field names to their respective formats
      // so we can easily lookup the format for a given field name out of
      // the hashtable.
      mapFormat(format0100_);
      mapFormat(format0200_);
      // We don't map the 0300 format since it would store itself
      // in the hashtable. If the 0300 format gets recreated, the 
      // hashtable would contain an outdated version of the object.
    }
    
    //@B0A
    /**
     * Initialize transient data.
    **/
    private void initializeTransient()
    {
      connected_ = false;
      cacheChanges_ = false;
      changes_ = new PropertyChangeSupport(this);
      vetos_ = new VetoableChangeSupport(this);
    }
    
    //@B6A
    /**
     * Returns the current cache status.
     * The default behavior is no caching.
     * @return true if caching is enabled, false otherwise.
     * @see #refreshCache
     * @see #setCaching
    **/
    public boolean isCaching()
    {
      return cacheChanges_;
    }
    
  /**
   * Loads system status data from the server for the specified
   * format. (It does not load all of the formats). If the
   * specified format was previously loaded, this method
   * does nothing. To always load the specified format, you
   * should call refreshCache() first to clear the format
   * cache; or, you could manually remove the specific format
   * from the format cache.
  **/
  private void loadInformation(SystemStatusFormat format)
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
    if (((Record)formatToRecordMap_.get(format)) != null) return;
    QSYSObjectPathName prgName = new QSYSObjectPathName("QSYS","QWCRSSTS","PGM");
    AS400Bin4 bin4 = new AS400Bin4();
    AS400Text text;
    int ccsid = as400_.getCcsid();
    
    ProgramParameter[] parmList = new ProgramParameter[5];
    
    int receiverLength = format.getNewRecord().getRecordLength();
    parmList[0] = new ProgramParameter(receiverLength);           
    parmList[1] = new ProgramParameter(bin4.toBytes(receiverLength));
    
    text = new AS400Text(8, ccsid, as400_);
    parmList[2] = new ProgramParameter(text.toBytes(format.getName()));
    
    // Reset statistics parm
    text = new AS400Text(10, ccsid, as400_);
    parmList[3] = new ProgramParameter(text.toBytes("*NO")); // this parm is ignored for SSTS0100
    
    byte[] errorInfo = new byte[32];
    parmList[4] = new ProgramParameter(errorInfo, 0);
    
    ProgramCall pgm = new ProgramCall(as400_);
    try
    {
      pgm.setProgram(prgName.getPath(), parmList);
    }
    catch(PropertyVetoException pve) {} // Quiet the compiler
        
    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Retrieving system status information.");
    }
    if (pgm.run() != true)
    {
      AS400Message[] msgList = pgm.getMessageList();
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.ERROR, "Error retrieving system status information:");
        for (int i=0; i<msgList.length; ++i)
        {
          Trace.log(Trace.ERROR, msgList[i].toString());
        }
      }
      throw new AS400Exception(msgList);
    }
    byte[] retrievedData = parmList[0].getOutputData();
    Record rec = format.getNewRecord(retrievedData);    
    if (format.getName().equals("SSTS0300"))
    {
      // It's possible we didn't retrieve all of the pools.
      // We may need to increase the fields in the record format to
      // hold more pool information and then do the API call again.
      if (resizeFormat(rec))
      {
        // Now hopefully we have a big enough byte array.
        // Try the API call again.
        loadInformation(format0300_);
        return;
      }
    }
    formatToRecordMap_.put(format, format.getNewRecord(retrievedData));
  }    
        
    /**
     * Used for de-serialization.
    **/
    private void readObject(java.io.ObjectInputStream is)
        throws IOException, ClassNotFoundException
    {
      is.defaultReadObject();
      initializeTransient();
    }
    
    
    /**
     * Refreshes the current system status information. The 
     * currently cached data is cleared and new data will
     * be retrieved from the system when needed. That is,
     * after a call to refreshCache(), a call to one of the get()
     * methods will go to the system to retrieve the value.
     *
     * If caching is not enabled, this method does nothing.
     * @see #isCaching
     * @see #setCaching
    **/
    public void refreshCache()
    {  
      // Refresh all record formats
      formatToRecordMap_.clear();
      // Clear the vector of pools
      poolsVector_ = null;
      // The next time a get() or set() is done, the record data for the
      // particular format will be reloaded.
    }

  /**
   * Adjusts the 0300 format to be large enough to hold all of the
   * information returned on the API call.
   * @param rec The Record of the previous failed API call. The information
   *            in the record is used to determine the new size of the format.
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
    // Only resize the format if we didn't get all the info.
    // The resize should happen no more than once per API call.
    if (available > returned)
    {
      int numPools = ((Integer)rec.getField("numberOfPools")).intValue();
      int offset = ((Integer)rec.getField("offsetToPoolInformation")).intValue();
      int entryLength = ((Integer)rec.getField("lengthOfPoolInformationEntry")).intValue();
      if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Resizing SSTS0300 format to hold more system status pool information.");
        Trace.log(Trace.DIAGNOSTIC, "  Old pool information: "+numPools+", "+offset+", "+entryLength);
      }
      // This was the old length
      //int oldLength = ((byte[])rec.getField("poolInformation")).length;
      
      // Before we recreate the 0300 format, we need to remove
      // it from the hashtable, otherwise we'll have 2 copies of it.
      formatToRecordMap_.remove(format0300_);
      format0300_ = new SSTS0300Format(as400_);
      // Make the byte array big enough to hold the pool information.
      int baseLength = format0300_.getNewRecord().getRecordLength();
      int newLength = numPools*entryLength + (offset-baseLength);
      format0300_.addFieldDescription(new HexFieldDescription(new AS400ByteArray(newLength), "poolInformation"));
      return true;
    }
    return false;
  }  
    
    /**
     * Removes a property change listener.
     *
     * @param listener The listener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        changes_.removePropertyChangeListener(listener);
    }

    /**
     * Removes a vetoable change listener.
     *
     * @param listener The listener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        vetos_.removeVetoableChangeListener(listener);
    }

    
    //@B6A
    /**
     * Turns caching on or off.
     * @param cache true if caching should be used when getting
     *              and setting information to and from the server; false
     *              if every get or set should communicate with the server
     *              immediately. Any cached changes that are not committed
     *              when caching is turned off will be lost.
     *              The default behavior is no caching.
     * @see #isCaching
     * @see #refreshCache
    **/
    public void setCaching(boolean cache)
    {
      cacheChanges_ = cache;
    }
     
    
    //@B0C - changed this method to not go to the system
    /** 
     * Sets the server. 
     *
     * @param system The server from which the system status information
     *              will be retrieved.
     * @exception PropertyVetoException If the change is vetoed.
    **/
    public void setSystem(AS400 system)
        throws PropertyVetoException
    {
        if (system == null)
        {
          throw new NullPointerException("system");
        }
        if (connected_)
        {
          throw new ExtendedIllegalStateException("system",
              ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
        vetos_.fireVetoableChange("system", as400_, system);
        AS400 oldValue = as400_;
        as400_ = system;
        changes_.firePropertyChange("system", oldValue, as400_);        
    }
    
}
