///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.net.UnknownHostException;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The JobList class represents a list of AS/400 jobs.
 *
 * <p>For example:
 * <pre>
 * JobList jobList = new JobList( as400 );
 * Enumeration e = jobList.getJobs ();
 * while (e.hasMoreElements ())
 * {
 *   Job j = (Job) e.nextElement ();
 *   System.out.println (j);
 * }
 * </pre>
 *
 * <p>JobList objects generate the following events:
 * <ul>
 * <li>PropertyChangeEvent
 * </ul>
 *
 * @see Job
**/
public class JobList implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final boolean DEBUG = false;

//@A1C add some property indicators which will be used in sort method.
/**
Property identifier for the subsystem.
**/
    private static final int    SUBSYSTEM_PROPERTY = 0;

/**
Property identifier for the subsystem.
**/
    private static final int    NAME_PROPERTY = 1;

/**
 * Constant indicating that the all jobs are returned.
**/
  public static final String ALL = "*ALL";

  private static AS400Bin4    intType = new AS400Bin4();

  // * Properties
  private AS400   as400_;
  private String  jobName_               = this.ALL;
  private String  jobNameCasePreserved_  = this.ALL;         // @A3A
  private String  userName_              = this.ALL;
  private String  userNameCasePreserved_ = this.ALL;         // @A3A
  private String  jobNumber_             = this.ALL;
  transient private JobListEnumeration   lastEnumeration_    = null; //@B0A

  transient private PropertyChangeSupport changes = new PropertyChangeSupport(this);
  transient private VetoableChangeSupport vetos = new VetoableChangeSupport(this);

/**
 * Constructs a JobList object.
 *
 * The system property needs to be set before using
 * any method requiring a connection to the AS/400.
**/
  public JobList()
  {
  }

/**
 * Constructs a JobList object.
 *
 * <p>Depending on how the AS400 object was constructed, the user
 * may be prompted for the system name, user ID, or password
 * when any method requiring a connection to the AS/400 is used.
 *
 * @param  system  The AS/400 system from which the list of jobs will
 *                 be retrieved.  This value cannot be null.
**/
  public JobList( AS400 system )
  {
    if (system == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'system' is null.");
      throw new NullPointerException("system");
    }

    this.as400_ = system;
  }

/**
 * Adds a listener to be notified when the value of any bound
 * property is changed. The <i>propertyChange()</i> method will be be called.
 *
 * @param listener The PropertyChangeListener.
**/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
      throw new NullPointerException("listener");
    }
    this.changes.addPropertyChangeListener(listener);
  }

/**
 * Adds a listener to be notified when the value of any constrained
 * property is changed. The <i>vetoableChange()</i> method will be called.
 *
 * @param listener The VetoableChangeListener.
**/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
      throw new NullPointerException("listener");
    }
    this.vetos.addVetoableChangeListener(listener);
  }

/**
 * Copyright.
**/
  private static String getCopyright ()
  {
    return Copyright.copyright;
  }

/**
 * Returns a list of jobs defined on the AS/400.
 * A valid AS/400 system must be provided before this call is made.
 *
 * @return An Enumeration of <i>Job</i> objects.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public Enumeration getJobs ()
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
  {
    if (this.as400_ == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }


    ProgramCall pgm = new ProgramCall( this.as400_ );

    ProgramParameter[] parms = new ProgramParameter[9];

    // 1 receiver variable
    parms[0] = new ProgramParameter( 5120 );
    // 2 receiver len
    byte[] msgsize = intType.toBytes(new Integer(5120) );
    parms[1] = new ProgramParameter( msgsize );

    // 3 list information
    parms[2] = new ProgramParameter( 80 );

    // 4 number of records to return
    parms[3] = new ProgramParameter( intType.toBytes(new Integer(13)) );

    // 5 sort info
    parms[4] = new ProgramParameter( new byte[4] );

    // 6 job and status info
    JobListParser parser = new JobListParser ();
    byte[] jobInfo = parser.buildJobSelectionInfo(this.as400_, jobName_, userName_, jobNumber_);
    parms[5] = new ProgramParameter( jobInfo );

    // 7 number of fields to return
    parms[6] = new ProgramParameter( intType.toBytes(new Integer(16)) );

    // 8 array of key of fields to return
    AS400Array array = new AS400Array( intType, 16 );
    Object[] objs = new Object[]
    {
      new Integer(101),   // active job status
      new Integer(304),   //  cpu used
      new Integer(402),   // date
      new Integer(502),   // end
      new Integer(601),   // function name
      new Integer(602),   // function type
      new Integer(1004),  // job q/lib
      new Integer(1005),  // jobq priority
      new Integer(1401),  //  aux IO req
      new Integer(1402),  //  interactive transactions
      new Integer(1502),  // outq priority
      new Integer(1801),  //  total response time
      new Integer(1802),  //  run priority
      new Integer(1903),  // status on jobQ
      new Integer(1906),  // subsys/lib
      new Integer(1907)   // pool id
    };
    parms[7] = new ProgramParameter(array.toBytes(objs));

    // 9 error code ? inout, char*
    parms[8] = new ProgramParameter( intType.toBytes( new Integer(0) ));

    // do it
    byte[] listInfoData = null;
    byte[] receiverData = null;
    try
    {
      if (pgm.run( "/QSYS.LIB/QGY.LIB/QGYRTVJ.PGM", parms )==false)
      {
        // error on run
        throw new AS400Exception( pgm.getMessageList() );
      }

      listInfoData = parms[2].getOutputData();
      receiverData = parms[0].getOutputData();
    }
    catch (PropertyVetoException e)
    {
      // Ignore.
    }

    //@A1C sort jobs and let jobs without subsystem at the bottom.
    // Create and return the enumeration.
    lastEnumeration_ = new JobListEnumeration(as400_, parser, listInfoData, receiverData); //@B0A
    return lastEnumeration_;  //@B0A


  }


/**
 * Returns the number of jobs in the list that was most recently
 * retrieved from the AS/400 (the last call to <i>getJobs()</i>).
 *
 * @return The number of jobs, or 0 if no list has been retrieved.
**/
  public int getLength()
  {
  
        if (lastEnumeration_ == null)             //@B0A
                return 0;                         //@B0A
        else                                      //@B0A
            return lastEnumeration_.getLength (); //@B0A
  }

/**
 * Returns the job name that describes which jobs are returned.
 *
 * @return The job name.
**/
  public String getName()
  {
    return jobNameCasePreserved_;                    // @A3C
  }

/**
 * Returns the job number that describes which jobs are returned.
 *
 * @return The job number.
**/
  public String getNumber()
  {
    return jobNumber_;
  }

/**
 * Returns the AS/400 system from which the list of jobs will be
 * retrieved.
 *
 * @return The AS/400 system from which the list of jobs will be
 *         retrieved.
**/
  public AS400 getSystem()
  {
    return this.as400_;
  }

/**
 * Returns the user name that describes which jobs are returned.
 *
 * @return The user name.
**/
  public String getUser()
  {
    return userNameCasePreserved_;                    // @A3C
  }

  // Deserializes and initializes transient data.
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();

    this.changes = new PropertyChangeSupport(this);
    this.vetos = new VetoableChangeSupport(this);
  }

/**
 * Removes a property change listener from the listener list.
 * @param listener The PropertyChangeListener.
**/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
      throw new NullPointerException("listener");
    }
    this.changes.removePropertyChangeListener(listener);
  }

/**
 * Removes a vetoable change listener from the listener list.
 * @param listener The VetoableChangeListener.
**/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
      throw new NullPointerException("listener");
    }
    this.vetos.removeVetoableChangeListener(listener);
  }

/**
 * Sets the job name that describes which jobs are returned.
 * The default is ALL.  This takes effect the next time that
 * <i>getJobs()</i> is called.
 *
 * @param name The job name, or ALL for all job names.
 *             This value cannot be null.
 *
 * @exception PropertyVetoException If the change is vetoed.
**/
  public void setName( String name ) throws PropertyVetoException
  {
    if (name == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'name' is null.");
      throw new NullPointerException("name");
    }

    String old = this.jobNameCasePreserved_;                // @A3C
    this.vetos.fireVetoableChange("name", old, name );

    jobNameCasePreserved_ = name;                           // @A3A
    jobName_ = name.toUpperCase();                          // @A3C
    this.changes.firePropertyChange("name", old, name );
  }

/**
 * Sets the job number that describes which jobs are returned.
 * The default is ALL.  This takes effect the next time that
 * <i>getJobs()</i> is called.
 *
 * @param number The job number, or ALL for all job numbers.
 *             This value cannot be null.
 *
 * @exception PropertyVetoException If the change is vetoed.
**/
  public void setNumber( String number ) throws PropertyVetoException
  {
    if (number == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'number' is null.");
      throw new NullPointerException("number");
    }

    String old = this.jobNumber_;
    this.vetos.fireVetoableChange("number", old, number );

    jobNumber_ = number;
    this.changes.firePropertyChange("number", old, number );
  }

/**
 * Sets the AS/400 system from which the list of jobs will be
 * retrieved.
 *
 * @param  system  The AS/400 system from which the list of jobs
 *                 will be retrieved.  This value cannot be null.
 *
 * @exception PropertyVetoException If the change is vetoed.
**/
  public void setSystem( AS400 system ) throws PropertyVetoException
  {
    if (system == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'system' is null.");
      throw new NullPointerException("system");
    }

    AS400 old = this.as400_;
    this.vetos.fireVetoableChange("system", old, system );

    this.as400_ = system;
    this.changes.firePropertyChange("system", old, system );
  }

/**
 * Sets the user name value that describes which jobs are returned.
 * The default is ALL.  This takes effect the next time that
 * <i>getUsers()</i> is called.
 *
 * @param user The user name, or ALL for all user names.
 *             This value cannot be null.
 *
 * @exception PropertyVetoException If the change is vetoed.
**/
  public void setUser( String user ) throws PropertyVetoException
  {
    if (user == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'user' is null.");
      throw new NullPointerException("user");
    }

    String old = this.userNameCasePreserved_;               // @A3C
    this.vetos.fireVetoableChange("user", old, user );

    userName_ = user.toUpperCase();                         // @A3C
    userNameCasePreserved_ = user;                          // @A3A
    this.changes.firePropertyChange("user", old, user );
  }


  //@A1A To sort jobs.
  /**
  Recursively sorts vectors of Job objects by subsystem and name.
    @param vec The objects to sort.
    @param vec The property by which to sort objects.
    @return The Vector of sorted objects.
  **/
  private Vector sort(Vector vec,int property)
  {
    int len = vec.size();
    int comparison;
    if (len < 2)
      return vec;
    Job currentJob = (Job)vec.elementAt(len/2);
    Vector lessthan = new Vector(len/2);
    Vector equalto = new Vector(len/2);
    Vector greaterthan = new Vector(len/2);
    Enumeration enum = vec.elements();
    while(enum.hasMoreElements())
    {
      Job job = (Job)enum.nextElement();
      switch (property)
      {
        case SUBSYSTEM_PROPERTY :
          try                                                                       //@B1A
          {                                                                         //@B1A
            comparison = job.getSubsystem().compareTo(currentJob.getSubsystem());
          }                                                                         //@B1A
          catch(Exception e)                                                        //@B1A
          {                                                                         //@B1A
            Trace.log(Trace.ERROR, "Exception while sorting jobs.", e);             //@B1A
            return vec;                                                             //@B1A
          }                                                                         //@B1A
          break;
        case NAME_PROPERTY :
        default:
            comparison = job.getName().compareTo(currentJob.getName());
      }
      if (comparison < 0)
        lessthan.addElement(job);
      else if (comparison > 0)
        greaterthan.addElement(job);
      else
        equalto.addElement(job);
    }
    lessthan.trimToSize();
    equalto.trimToSize();
    greaterthan.trimToSize();
    Vector lefthalf = sort(lessthan,property);
    Vector righthalf = sort(greaterthan,property);
    Vector middlehalf = equalto;

    if (property == SUBSYSTEM_PROPERTY)
        middlehalf = sort(equalto,NAME_PROPERTY);

    Vector whole = new Vector(lefthalf.size()+righthalf.size()+middlehalf.size());
    enum = lefthalf.elements();
    while (enum.hasMoreElements())
    {
      whole.addElement(enum.nextElement());
    }
    enum = middlehalf.elements();
    while (enum.hasMoreElements())
    {
      whole.addElement(enum.nextElement());
    }
    enum = righthalf.elements();
    while (enum.hasMoreElements())
    {
      whole.addElement(enum.nextElement());
    }
    return whole;
  }
}

