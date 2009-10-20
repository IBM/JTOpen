///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemStatus.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 Provides access to a group of statistics that represent the current status of the system.
 **/
public class SystemStatus implements Serializable
{
    static final long serialVersionUID = 4L;

    // Shared error code parameter.
    private static final ProgramParameter ERROR_CODE = new ProgramParameter(new byte[8]);

    // The system from which status is being retrieved.
    private AS400 system_;

    // The receiver variables retrieved for each format.  Index zero will be set to the last format retrieved.
    byte[][] receiverVariables_ = new byte[4][];
    // A vector of SystemPool object retrieved from the system.
    private Vector poolsVector_;

    // Flag indicating if we have connected to the system yet.
    private transient boolean connected_ = false;
    // Flag indicating if we are caching stats or retrieving everytime.
    private transient boolean caching_ = false;

    // List of property change event bean listeners, set on first add.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;
    // List of vetoable change event bean listeners, set on first add.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;

    /**
     Constructs a SystemStatus object.
     **/
    public SystemStatus()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemStatus object.");
    }

    /**
     Constructs a SystemStatus object.
     @param  system  The system object representing the system from which the status statistics should be retrieved.
     **/
    public SystemStatus(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemStatus object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's {@link java.beans.PropertyChangeListener#propertyChange propertyChange()} method will be called each time the value of any bound property is changed.
     @param  listener  The listener.
     @see  #removePropertyChangeListener
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (propertyChangeListeners_ == null)
            {
                propertyChangeListeners_ = new PropertyChangeSupport(this);
            }
            propertyChangeListeners_.addPropertyChangeListener(listener);
        }
    }

    /**
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's {@link java.beans.VetoableChangeListener#vetoableChange vetoableChange()} method will be called each time the value of any constrained property is changed.
     @param  listener  The listener.
     @see  #removeVetoableChangeListener
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (vetoableChangeListeners_ == null)
            {
                vetoableChangeListeners_ = new VetoableChangeSupport(this);
            }
            vetoableChangeListeners_.addVetoableChangeListener(listener);
        }
    }

    /**
     Returns the number of jobs active in the system (jobs that have been started, but have not yet ended), including both user and system jobs.
     @return  The number of jobs active in the system (jobs that have been started, but have not yet ended), including both user and system jobs.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getActiveJobsInSystem() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 100);
    }

    /**
     Returns the number of initial and secondary threads in the system (threads that have been started, but have not yet ended), including both user and system threads.
     @return  The number of initial and secondary threads in the system (threads that have been started, but have not yet ended), including both user and system threads.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getActiveThreadsInSystem() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 104);
    }

    /**
     Returns the number of completed batch jobs that produced printer output that is waiting to print.
     @return  The number of completed batch jobs that produced printer output that is waiting to print.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getBatchJobsEndedWithPrinterOutputWaitingToPrint() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 76);
    }

    /**
     Returns the number of batch jobs that are in the process of ending due to one of the following conditions:
     <ul>
     <li>The job finishes processing normally.
     <li>The job ends before its normal completion point and is being
     removed from the system.
     </ul>
     @return  The number of batch jobs that are in the process of ending.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getBatchJobsEnding() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 56);
    }

    /**
     Returns the number of batch jobs that were submitted, but were held before they could begin running.
     @return  The number of batch jobs that were submitted, but were held before they could begin running.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getBatchJobsHeldOnJobQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 64);
    }

    /**
     Returns the number of batch jobs that had started running, but are now held.
     @return  The number of batch jobs that had started running, but are now held.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getBatchJobsHeldWhileRunning() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 52);
    }

    /**
     Returns the number of batch jobs on job queues that have been assigned to a subsystem, but are being held.
     @return  The number of batch jobs on job queues that have been assigned to a subsystem, but are being held.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getBatchJobsOnAHeldJobQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 68);
    }

    /**
     Returns the number of batch jobs on job queues that have not been assigned to a subsystem.
     @return  The number of batch jobs on job queues that have not been assigned to a subsystem.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getBatchJobsOnUnassignedJobQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 72);
    }

    /**
     Returns the number of batch jobs currently running on the system.
     @return  The number of batch jobs currently running on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getBatchJobsRunning() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 48);
    }

    /**
     Returns the number of batch jobs waiting for a reply to a message before they can continue to run.
     @return  The number of batch jobs waiting for a reply to a message before they can continue to run.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getBatchJobsWaitingForMessage() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 44);
    }

    /**
     Returns the number of batch jobs on the system that are currently waiting to run, including those that were submitted to run at a future date and time.  Jobs on the job schedule that have not been submitted are not included.
     @return  The number of batch jobs on the system that are currently waiting to run.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getBatchJobsWaitingToRunOrAlreadyScheduled() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 60);
    }

    /**
     Returns the amount (in number of physical processors) of current processing capacity of the partition.  For a partition sharing physical processors, this attribute represents the share of the physical processors in the pool it is executing.
     @return  The amount of current processing capacity of the partition.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getCurrentProcessingCapacity() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 88) / 100.0f;
    }

    /**
     Returns the current amount of storage in use for temporary objects.  This value is in millions of bytes.
     @return  The current amount of storage in use for temporary objects.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getCurrentUnprotectedStorageUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 60);
    }

    /**
     Returns the date and time when the status was gathered.
     @return  The date and time when the status was gathered.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getDateAndTimeStatusGathered() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(0);
        byte[] currentDateAndTime = new byte[8];
        System.arraycopy(receiverVariables_[0], 8, currentDateAndTime, 0, 8);
        DateTimeConverter converter = new DateTimeConverter(system_);
        return converter.convert(currentDateAndTime, "*DTS");
    }

    /**
     Returns the time (in seconds) that has elapsed between the measurement start time and the current system time.
     @return  The time (in seconds) that has elapsed between the measurement start time and the current system time.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getElapsedTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!caching_) refreshCache();

        int format;
        if (receiverVariables_[2] == null)
        {
            if (receiverVariables_[3] == null)
            {
                loadInformation(2);
                format = 2;
            }
            else
            {
                format = 3;
            }
        }
        else
        {
            format = 2;
        }

        // The value is in the format HHMMSS where HH is the hour, MM is the minute, and SS is the second.
        int hours = (receiverVariables_[format][24] & 0x0F) * 10 + (receiverVariables_[format][25] & 0x0F);
        int minutes = (receiverVariables_[format][26] & 0x0F) * 10 + (receiverVariables_[format][27] & 0x0F);
        int seconds = (receiverVariables_[format][28] & 0x0F) * 10 + (receiverVariables_[format][29] & 0x0F);
        return hours * 3600 + minutes * 60 + seconds;
    }

    /**
     Returns the total number of user jobs and system jobs that are currently in the system.  The total includes:
     <ul>
     <li>All jobs on job queues waiting to be processed.
     <li>All jobs currently active (being processed).
     <li>All jobs that have completed running but still have pending job logs or output on output queues to be produced.
     </ul>
     @return  The total number of user jobs and system jobs that are currently in the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getJobsInSystem() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 36);
    }

    /**
     Returns the amount of main storage, in kilobytes, in the system.  On a partitioned system, the main storage size can change while the system is active.
     @return  The amount of main storage, in kilobytes, in the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getMainStorageSize() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        if (system_.getVRM() < 0x00050400)
        {
            return BinaryConverter.byteArrayToInt(receiverVariables_[2], 72);
        }
        return BinaryConverter.byteArrayToLong(receiverVariables_[2], 140);
    }

    /**
     Returns the maximum number of jobs that are allowed on the system.  When the number of jobs reaches this maximum, you can no longer submit or start more jobs on the system.  The total includes:
     <ul>
     <li>All jobs on job queues waiting to be processed.
     <li>All jobs currently active (being processed).
     <li>All jobs that have completed running but still have output on output queues to be produced.
     </ul>
     @return  The maximum number of jobs that are allowed on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getMaximumJobsInSystem() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 108);
    }

    /**
     Returns the largest amount of storage for temporary object used at any one time since the last IPL.  This value is in millions (M) of bytes.
     @return  The largest amount of storage for temporary object used at any one time since the last IPL.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getMaximumUnprotectedStorageUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 64);
    }

    /**
     Returns the number of partitions on the system.  This includes partitions that are currently powered on (running) and partitions that are powered off.
     @return  The number of partitions on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getNumberOfPartitions() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 76);
    }

    /**
     Returns the number of processors that are currently active in this partition.
     @return  The number of processors that are currently active in this partition.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getNumberOfProcessors() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 96);
    }

    /**
     Returns the identifier for the current partition in which the API is running.
     @return  The identifier for the current partition in which the API is running.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getPartitionIdentifier() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 80);
    }

    /**
     Returns the percentage of interactive performance assigned to this logical partition.  This value is a percentage of the total interactive performance available to the entire physical system.
     @return  The percentage of interactive performance assigned to this logical partition.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentCurrentInteractivePerformance() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return (float)BinaryConverter.byteArrayToInt(receiverVariables_[2], 128);
    }

    /**
     Returns the percentage of processor database capability that was used during the elapsed time.  Database capability is the maximum CPU utilization available for database processing on this system.  -1 is returned if this system does not report the amount of CPU used for database processing.
     @return  The percentage of processor database capability that was used during the elapsed time.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentDBCapability() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        int intValue = BinaryConverter.byteArrayToInt(receiverVariables_[2], 68);
        return intValue == -1 ? -1f : intValue / 10.0f;
    }

    /**
     Returns the percentage of the maximum possible addresses for permanent objects that have been used.
     @return  The percentage of the maximum possible addresses for permanent objects that have been used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentPermanentAddresses() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 40) / 1000.0f;
    }

    /**
     Returns the percentage of the maximum possible permanent 256MB segments that have been used.
     @return  The percentage of the maximum possible permanent 256MB segments that have been used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentPermanent256MBSegmentsUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 120) / 1000.0f;
    }

    /**
     Returns the percentage of the maximum possible permanent 4GB segments that have been used.
     @return  The percentage of the maximum possible permanent 4GB segments that have been used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentPermanent4GBSegmentsUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 124) / 1000.0f;
    }

    /**
     Returns the average of the elapsed time during which the processing units were in use.  For an uncapped partition, this is the percentage of the configured uncapped shared processing capacity for the partition that was used during the elapsed time.  This percentage could be greater than 100% for an uncapped partition.
     @return  The average of the elapsed time during which the processing units were in use.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentProcessingUnitUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 32) / 10.0f;
    }

    /**
     Returns the percentage of the total shared processor pool capacity used by all partitions using the pool during the elapsed time. -1 is returned if this partition does not share processors.
     @return  The percentage of the total shared processor pool capacity used by all partitions using the pool during the elapsed time.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentSharedProcessorPoolUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        int intValue = BinaryConverter.byteArrayToInt(receiverVariables_[2], 136);
        return system_.getVRM() < 0x00050300 || intValue == -1 ? -1f : intValue / 10.0f;
    }

    /**
     Returns the percentage of the system storage pool currently in use.
     @return  The percentage of the system storage pool currently in use.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentSystemASPUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 52) / 10000.0f;
    }

    /**
     Returns the percentage of the maximum possible addresses for temporary objects that have been used.
     @return  The percentage of the maximum possible addresses for temporary objects that have been used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentTemporaryAddresses() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 44) / 1000.0f;
    }

    /**
     Returns the percentage of the maximum possible temporary 256MB segments that have been used.
     @return  The percentage of the maximum possible temporary 256MB segments that have been used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentTemporary256MBSegmentsUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 112) / 1000.0f;
    }

    /**
     Returns the percentage of the maximum possible temporary 4GB segments that have been used.
     @return  The percentage of the maximum possible temporary 4GB segments that have been used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentTemporary4GBSegmentsUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 116) / 1000.0f;
    }

    /**
     Returns the percentage of the uncapped shared processing capacity for the partition that was used during the elapsed time.  -1 is returned if this partition can not use more than its configured processing capacity.
     @return  The percentage of the uncapped shared processing capacity for the partition that was used during the elapsed time.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public float getPercentUncappedCPUCapacityUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        int intValue = BinaryConverter.byteArrayToInt(receiverVariables_[2], 132);
        return system_.getVRM() < 0x00050300 || intValue == -1 ? -1f : intValue / 10.0f;
    }

    /**
     Returns the number of system pools.
     @return  The number of system pools.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getPoolsNumber() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(3);
        return BinaryConverter.byteArrayToInt(receiverVariables_[3], 32);
    }

    /**
     Returns the processor sharing attribute.  This attribute indicates whether this partition is sharing processors.  If the value indicates the partition does not share physical processors, then this partition uses only dedicated processors.  If the value indicates the partition shares physical processors, then this partition uses physical processors from a shared pool of physical processors.  The following values are returned:
     <ul>
     <li>0: Partition does not share processors.
     <li>1: Partition shares processors (capped).  The partition is limited to using its configured capacity.
     <li>2: Partition shares processors (uncapped).  The partition can use more than its configured capacity.
     </ul>
     @return  The processor sharing attribute.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getProcessorSharingAttribute() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return receiverVariables_[2][92] & 0x0F;
    }

    /**
     Returns the value indicating whether the system is in restricted state.
     @return  true if the system is in restricted state; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean getRestrictedStateFlag() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return receiverVariables_[2][30] == (byte)0xF1;
    }

    /**
     Returns the system object representing the system from which the system status information will be retrieved.
     @return  The system object representing the system from which the system status information will be retrieved.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns the storage capacity of the system auxiliary storage pool (ASP1).  This value is in millions (M) of bytes.
     @return  The storage capacity of the system auxiliary storage pool (ASP1).
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getSystemASP() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 48);
    }

    /**
     Returns the name of the system where the statistics were collected.
     @return  The name of the system where the statistics were collected.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getSystemName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(0);
        Converter conv = new Converter(system_.getJobCcsid(), system_);
        return conv.byteArrayToString(receiverVariables_[0], 16, 8).trim();
    }

    /**
     Returns an enumeration containing a SystemPool object for each system pool.
     @return  An enumeration containing a SystemPool object for each system pool.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Enumeration getSystemPools() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(3);

        // If we've already retrieved the pools, just return it.
        if (poolsVector_ != null) return poolsVector_.elements();
        poolsVector_ = new Vector();

        // Parse the pool information.
        int number = BinaryConverter.byteArrayToInt(receiverVariables_[3], 32);
        int offset = BinaryConverter.byteArrayToInt(receiverVariables_[3], 36);
        int length = BinaryConverter.byteArrayToInt(receiverVariables_[3], 40);

        for (int i = 0; i < number; ++i)
        {
            byte[] poolInformation = new byte[length];
            System.arraycopy(receiverVariables_[3], offset, poolInformation, 0, length);
            SystemPool systemPool = null;
            int poolIdentifier = BinaryConverter.byteArrayToInt(poolInformation, 0);
            if (poolIdentifier != 0) // this will usually (maybe always) be true
            {
              systemPool = new SystemPool(system_, poolIdentifier);
            }
            else {
              String poolName = new CharConverter(system_.getJobCcsid(), system_).byteArrayToString(poolInformation, 44, 10);
              systemPool = new SystemPool(system_, poolName);
            }
            poolsVector_.addElement(systemPool);
            offset += length;
        }
        return poolsVector_.elements();
    }

    /**
     Returns the total auxiliary storage (in millions of bytes) on the system.
     @return  The total auxiliary storage (in millions of bytes) on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getTotalAuxiliaryStorage() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(2);
        return BinaryConverter.byteArrayToInt(receiverVariables_[2], 56);
    }

    /**
     Returns the number of users currently signed on the system.  System request jobs and group jobs are not included in this number.
     @return  The number of users currently signed on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getUsersCurrentSignedOn() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 24);
    }

    /**
     Returns the number of sessions that have ended with printer output files waiting to print.
     @return  The number of sessions that have ended with printer output files waiting to print.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getUsersSignedOffWithPrinterOutputWaitingToPrint() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 40);
    }

    /**
     Returns the number of user jobs that have been temporarily suspended by group jobs so that another job may be run.
     @return  The number of user jobs that have been temporarily suspended by group jobs so that another job may be run.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getUsersSuspendedByGroupJobs() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 36);
    }

    /**
     Returns the number of user jobs that have been temporarily suspended by system request jobs so that another job may be run.
     @return  The number of user jobs that have been temporarily suspended by system request jobs so that another job may be run.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getUsersSuspendedBySystemRequest() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 32);
    }

    /**
     Returns the number of jobs that have been disconnected due to either the selection of option 80 (Temporary sign-off) or the entry of the Disconnect Job (DSCJOB) command.
     @return  The number of jobs that have been disconnected due to either the selection of option 80 (Temporary sign-off) or the entry of the Disconnect Job (DSCJOB) command.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getUsersTemporarilySignedOff() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        loadInformation(1);
        return BinaryConverter.byteArrayToInt(receiverVariables_[1], 28);
    }

    /**
     Returns the current cache status.  The default behavior is no caching.
     @return  true if caching is enabled, false otherwise.
     @see  #refreshCache
     @see  #setCaching
     **/
    public boolean isCaching()
    {
        return caching_;
    }

    // If necessary, call the API and retrieve the information for the specified format.
    private void loadInformation(int format) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!caching_) refreshCache();

        // Check to see if the format has been loaded already.
        if (receiverVariables_[format] != null) return;
        if (format == 0) format = 1;

        if (!connected_)
        {
            if (system_ == null)
            {
                Trace.log(Trace.ERROR, "Cannot connect before setting system.");
                throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            connected_ = true;
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving system status.");
        int receiverVariableLength = format == 1 ? 80 : format == 2 ? 148 : 2048;

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(receiverVariableLength),
            // Receiver variable length, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(receiverVariableLength)),
            // Format name, input, char(8), EBCDIC 'SSTS0X00'.
            new ProgramParameter(new byte[] { (byte)0xE2, (byte)0xE2, (byte)0xE3, (byte)0xE2, (byte)0xF0, (byte)(0xF0 | format), (byte)0xF0, (byte)0xF0 } ),
            // Reset status statistics, input, char(10), EBCDIC '*NO'.
            new ProgramParameter(new byte[] { 0x5C, (byte)0xD5, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 } ),
            // Error code, I/O, char(*).
            ERROR_CODE
        };

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QWCRSSTS.PGM", parameters);
        // QWCRSSTS is not thread safe.
        boolean repeatRun;
        do
        {
            repeatRun = false;
            if (!pc.run())
            {
                throw new AS400Exception(pc.getMessageList());
            }

            receiverVariables_[format] = parameters[0].getOutputData();

            int bytesAvailable = BinaryConverter.byteArrayToInt(receiverVariables_[format], 0);
            int bytesReturned = BinaryConverter.byteArrayToInt(receiverVariables_[format], 4);
            if (bytesReturned < bytesAvailable)
            {
                repeatRun = true;
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieve system status receiver variable too small, bytes returned: " + bytesReturned + ", bytes available: " + bytesAvailable);
                parameters[0] = new ProgramParameter(bytesAvailable);
                parameters[1] = new ProgramParameter(BinaryConverter.intToByteArray(bytesAvailable));
            }
        }
        while (repeatRun);
        receiverVariables_[0] = receiverVariables_[format];
    }

    /**
     Refreshes the current system status information.  The currently cached data is cleared and new data will be retrieved from the system when needed.  That is, after a call to refreshCache(), a call to one of the get() methods will go to the system to retrieve the value.  If caching is not enabled, this method does nothing.
     @see  #isCaching
     @see  #setCaching
     **/
    public void refreshCache()
    {
        // Clear the receiver variables;
        receiverVariables_ = new byte[4][];
        // Clear the vector of pools.
        poolsVector_ = null;
    }

    /**
     Removes the PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
     @param  listener  The listener.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (propertyChangeListeners_ != null)
        {
            propertyChangeListeners_.removePropertyChangeListener(listener);
        }
    }

    /**
     Removes the VetoableChangeListener.  If the VetoableChangeListener is not on the list, nothing is done.
     @param  listener  The listener.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (vetoableChangeListeners_ != null)
        {
            vetoableChangeListeners_.removeVetoableChangeListener(listener);
        }
    }

    /**
     Turns caching on or off.
     @param  caching  true if caching should be used when getting information from the system; false if every get should communicate with the system immediately.  The default behavior is no caching.
     @see  #isCaching
     @see  #refreshCache
     **/
    public void setCaching(boolean caching)
    {
        caching_ = caching;
    }

    /**
     Sets the system object representing the system from which the system status information will be retrieved.  This property cannot be changed if the object has established a connection to the system.
     @param  system  The system object representing the system from which the system status information will be retrieved.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);

        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (connected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            system_ = system;
        }
        else
        {
            AS400 oldValue = system_;
            AS400 newValue = system;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("system", oldValue, newValue);
            }
            system_ = system;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
            }
        }
    }
}
