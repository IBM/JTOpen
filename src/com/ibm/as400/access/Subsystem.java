///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SystemPool.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2004-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.beans.PropertyVetoException;
import java.util.Vector;
import java.util.Enumeration;

/**
 Represents a subsystem on the server.
 Most of the getter methods simply return the cached attribute value obtained by the most recent {@link #refresh() refresh()}.  Other than getObjectDescription(), none of the getters will themselves go to the system to get the current attribute value.  Prior to the first refresh(), many of the getters will return null or zero.
 <br>Example:
 <pre>
 Subsystem sbs = new Subsystem(system, library, name);
 if (!sbs.exists()) {
   System.out.println("No such subsystem: "+sbs.getPath());
 }
 else {
   sbs.refresh();
   System.out.println("Status: " + sbs.getStatus());
   System.out.println("Number of jobs: " + sbs.getCurrentActiveJobs());
   System.out.println("Maximum jobs: " + sbs.getMaximumActiveJobs());
   SystemPool[] pools = sbs.getPools();
   System.out.print("Pools: ");
   if (pools == null) System.out.println("null");
   else for (int i=0; i<pools.length; i++) {
     System.out.print(" " + (pools[i] == null ? "null" : pools[i].getName()));
   }
   System.out.println();
 }
 System.out.println("All subsystems on "+system.getSystemName());
 Subsystem[] list = Subsystem.listAllSubsystems(system);
 for (int i=0; i<list.length; i++) {
   System.out.println(list[i].getLibrary()+"/"+list[i].getName());
 }
 </pre>
 **/
public class Subsystem
{
  static final long serialVersionUID = 4L;

  private static final boolean DEBUG = false;

  /**
   Value for the maximumActiveJobs property, indicating "no maximum".
   **/
  public static final int NO_MAX = -1;

  /**
   Value for the timeLimit parameter of the end() methods, indicating "no time limit".
   **/
  public static final int NO_TIME_LIMIT = -1;


  private AS400 system_;
  private String path_;

  private String name_;
  private String library_;
  private String extendedStatus_;
  private int maxActiveJobs_;
  private int currentActiveJobs_;
  private String monitorJobName_;
  private String monitorJobUser_;
  private String monitorJobNumber_;
  private String descriptionText_;

  private String dspFileName_;
  private String dspFileLibrary_;
  private String langLibrary_;
  private SystemPool[] pools_;  // Exactly 10 elements.  Null-valued elements signify an empty (unassigned) slot in the pool sequence for the subsystem.

  private transient ObjectDescription objectDescription_;

  private transient boolean refreshed_;

  /**
   Constructs a Subsystem object.
   <br>Note: Does not create a subsystem on the server.
   @param system The system.
   @param library The name of the library where the subsystem resides.
   @param name The simple name of the subsystem.
   @see #create()
   **/
  public Subsystem(AS400 system, String library, String name)
  {
    if (system == null) throw new NullPointerException("system");
    if (library == null) throw new NullPointerException("library");
    if (name == null) throw new NullPointerException("name");

    system_ = system;
    path_ = QSYSObjectPathName.toPath(library, name, "SBSD");
    library_ = library;
    name_ = name;
  }

  /**
   Constructs a Subsystem object.
   <br>Note: Does not create a subsystem on the server.
   @param system The system.
   @param path The qualified path. For example, "/QSYS.LIB/MYSUBSYS.SBSD".
   @see #create()
   **/
  public Subsystem(AS400 system, String path)
  {
    if (system == null) throw new NullPointerException("system");
    if (path == null) throw new NullPointerException("path");

    QSYSObjectPathName qsys = new QSYSObjectPathName(path);
    if (!qsys.getObjectType().equalsIgnoreCase("SBSD"))
    {
      throw new ExtendedIllegalArgumentException("path", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    system_ = system;
    path_ = path;
    library_ = qsys.getLibraryName();
    name_ = qsys.getObjectName();
  }



  /**
   Adds a shared system pool to the list of storage pools defined for the subsystem.
   Current pool definitions, other than the one specified, remain unchanged.
   @param sequenceNumber The number of the pool, within the pool list for the subsystem.  Valid values are 1-10.
   @param poolName The name of a shared system storage pool.  Valid values include: *BASE, *NOSTG, *INTERACT, *SPOOL, and *SHRPOOLnn, where nn is an integer from 1 to 60.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void assignPool(int sequenceNumber, String poolName)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (poolName == null) throw new NullPointerException("poolName");
    if (sequenceNumber<1 || sequenceNumber>10) {
      throw new ExtendedIllegalArgumentException("sequenceNumber", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    CommandCall cmd = new CommandCall(system_, "QSYS/CHGSBSD SBSD("+library_+"/"+name_+") POOLS(("+sequenceNumber+" "+poolName+"))");
    if (DEBUG) System.out.println("Command string: " + cmd.getCommand());
    // Note: Pool size must be at least 256 (meaning 256 kilobytes).
    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        throw new AS400Exception(msgs);
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.", InternalErrorException.UNKNOWN);
      }
    }
  }


  /**
   Defines a private storage pool and adds it to the list of storage pools defined for the subsystem.
   Current pool definitions, other than the one specified, remain unchanged.
   @param sequenceNumber The number of the pool, within the pool list for the subsystem.  Valid values are 1-10.
   @param size The size of the storage pool, in kilobytes.  Must be at least 256 (meaning 256 kilobytes).
   @param activityLevel The activity level of the pool.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void assignPool(int sequenceNumber, int size, int activityLevel)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (sequenceNumber<1 || sequenceNumber>10) {
      throw new ExtendedIllegalArgumentException("sequenceNumber", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    CommandCall cmd = new CommandCall(system_, "QSYS/CHGSBSD SBSD("+library_+"/"+name_+") POOLS(("+sequenceNumber+" "+size+" "+activityLevel+"))");
    if (DEBUG) System.out.println("Command string: " + cmd.getCommand());
    // Note: Pool size must be at least 256 (meaning 256 kilobytes).
    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        throw new AS400Exception(msgs);
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.", InternalErrorException.UNKNOWN);
      }
    }
  }

  /**
   Sets the subsystem description text.  The default is "".
   @param text The subsystem description text.  To clear the description, specify "".
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void changeDescriptionText(String text)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (text == null) throw new NullPointerException("text");

    if (text.length() == 0) text = "*BLANK";
    CommandCall cmd = new CommandCall(system_, "QSYS/CHGSBSD SBSD("+library_+"/"+name_+") TEXT('"+text+"')");
    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        throw new AS400Exception(msgs);
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.", InternalErrorException.UNKNOWN);
      }
    }
  }


  /**
   Specifies the signon display file that is used when showing signon displays at work stations allocated to the subsystem.  By default, QSYS/QDSIGNON (path /QSYS.LIB/QDSIGNON.FILE) is used.
   @param path The fully qualified pathname of the signon display file.  See {@link com.ibm.as400.access.QSYSObjectPathName#toPath(String,String,String) QSYSObjectPathName.toString()}
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void changeDisplayFilePath(String path)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (path == null) throw new NullPointerException("path");

    QSYSObjectPathName qpath = new QSYSObjectPathName(path);

    CommandCall cmd = new CommandCall(system_, "QSYS/CHGSBSD SBSD("+library_+"/"+name_+") SGNDSPF("+qpath.getLibraryName()+"/"+qpath.getObjectName()+")");
    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        throw new AS400Exception(msgs);
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.", InternalErrorException.UNKNOWN);
      }
    }
  }

  /**
   Specifies a library to enter ahead of other libraries in the system portion of the library list. This method allows you to use a secondary language library.
   @param library The name of the library.  Specify "" or "*NONE" to remove the current secondary language library from the system library list.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void changeLanguageLibrary(String library)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (library == null) throw new NullPointerException("library");

    if (library.trim().length() == 0) library = "*NONE";

    CommandCall cmd = new CommandCall(system_, "QSYS/CHGSBSD SBSD("+library_+"/"+name_+") SYSLIBLE("+library+")");

    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        throw new AS400Exception(msgs);
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.", InternalErrorException.UNKNOWN);
      }
    }
  }


  /**
   Sets the maximum number of active jobs that can run or use resources within the subsystem at one time.
   @param maxJobs The maximum number of active jobs. Valid values are 0-1000.  Special value {@link #NO_MAX NO_MAX} is the default, and indicates no maximum.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void changeMaximumActiveJobs(int maxJobs)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    String max = (maxJobs == NO_MAX ? "*NOMAX" : Integer.toString(maxJobs));
    CommandCall cmd = new CommandCall(system_, "QSYS/CHGSBSD SBSD("+library_+"/"+name_+") MAXJOBS("+max+")");
    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        throw new AS400Exception(msgs);
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.", InternalErrorException.UNKNOWN);
      }
    }
  }

  /**
   Creates the subsystem on the server.
   <br>More precisely, this method creates a subsystem <em>description</em> on the server.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   @exception ObjectAlreadyExistsException If the object already exists on the server.
   **/
  public void create()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ObjectAlreadyExistsException
  {
    create("*LIBCRTAUT");
  }

  /**
   Creates the subsystem on the server.
   <br>More precisely, this method creates a subsystem <em>description</em> on the server.
   @param authority The authority to give to users who do not have specific authority for the object, who are not on an authorization list, and whose group profile or supplemental group profiles do not have specific authority for the object.  The default is *LIBCRTAUT.  The "base system pool" (*BASE) is used.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   @exception ObjectAlreadyExistsException If the object already exists on the server.
   **/
  public void create(String authority)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, ObjectAlreadyExistsException
  {
    if (authority == null) throw new NullPointerException("authority");

    // Note: The AUT parameter is the only parameter unique to CRTSBSD, and not available with CHGSBSD.
    if (exists()) {
      throw new ObjectAlreadyExistsException(path_, ObjectAlreadyExistsException.OBJECT_ALREADY_EXISTS);
    }

    String cmdString = "QSYS/CRTSBSD SBSD("+library_+"/"+name_+") AUT(" + authority + ") POOLS((1 *BASE))";

    CommandCall cmd = new CommandCall(system_, cmdString);
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  /**
   Deletes the subsystem from the server.
   <br>More precisely, this method deletes a subsystem <em>description</em> on the server.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public void delete()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
  {
    CommandCall cmd = new CommandCall(system_, "QSYS/DLTSBSD SBSD("+library_+"/"+name_+")");
    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        if (msgs[0].getID() != null &&
            !msgs[0].getID().equals("CPF2105") &&  // object not found
            !msgs[0].getID().equals("CPF2110"))    // library not found
        {
          throw new AS400Exception(msgs);
        }
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.",
                                         InternalErrorException.UNKNOWN);
      }
    }
  }

  /**
   Ends the subsystem on the server, in a controlled manner.
   This allows the programs that are running in the subsystem, to perform cleanup (end of job processing). When a job being ended has a signal handling procedure for the asynchronous signal SIGTERM, the SIGTERM signal is generated for that job. The application has <tt>timeLimit</tt> seconds to complete cleanup before the job is ended. 
   @param timeLimit The amount of time (in seconds) that is allowed to complete the controlled subsystem end operation. If this amount of time is exceeded and the end operation is not complete, any jobs still being processed in the subsystem are ended immediately.  Special value {@link #NO_TIME_LIMIT NO_TIME_LIMIT} indicates no time limit.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void end(int timeLimit)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    // Possible future enhancements (if requested):
    //   Support setting additional ENDSBS parameters:
    //     ENDSBSOPT (end subsystem option), and BCHTIMLMT (batch time limit).
    end(system_, name_, false, timeLimit);
  }

  /**
   Ends all active subsystems on the server, in a controlled manner.
   @param system The system.
   @param timeLimit The amount of time (in seconds) that is allowed to complete the controlled subsystem end operation. If this amount of time is exceeded and the end operation is not complete, any jobs still being processed in the subsystem are ended immediately.  Special value {@link #NO_TIME_LIMIT NO_TIME_LIMIT} indicates no time limit.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public static void endAllSubsystems(AS400 system, int timeLimit)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
  {
    if (system == null) throw new NullPointerException("system");
    end(system, "*ALL", false, timeLimit);
  }

  /**
   Ends all active subsystems on the server, immediately.
   @param system The system.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public static void endAllSubsystemsImmediately(AS400 system)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
  {
    if (system == null) throw new NullPointerException("system");
    end(system, "*ALL", true, NO_TIME_LIMIT);
  }

  /**
   Ends the subsystem on the server, immediately.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void endImmediately()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    end(system_, name_, true, NO_TIME_LIMIT);
  }


  /**
   Determines whether this Subsystem object is equal to another object.
   @return <tt>true</tt> if the two instances are equal
   **/
  public boolean equals(Object obj)
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");

    try
    {
      Subsystem other = (Subsystem)obj;

      if (!system_.equals(other.getSystem())) return false;
      if (!library_.equals(other.getLibrary())) return false;
      if (!name_.equals(other.getName())) return false;
      else return true;
    }
    catch (Throwable e) {
      return false;
    }
  }

  /**
   Determines if the subsystem currently exists on the system.
   <br>More precisely, this method reports if the subsystem <em>description</em> exists on the server.
   @return true if the subsystem exists; false if the subsystem does not exist.
   @exception  AS400Exception  If the program call returns error messages.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the server API (that queries subsystem description information) is missing.
  **/
  public boolean exists()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (objectDescription_ == null) { objectDescription_ = getObjDesc(); }
    return objectDescription_.exists();
  }

  /**
   Returns the number of jobs currently active in the subsystem.
   @return The number of currently active jobs. 0 if refresh() has not been called, or if the subsystem status is *INACTIVE.
   **/
  public int getCurrentActiveJobs()
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");

    return currentActiveJobs_;
  }

  /**
   Returns the subsystem description text.
   @return The subsystem description text. <tt>null</tt> if refresh() has not been called.  "" if description is blank.
   **/
  public String getDescriptionText()
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");

    return descriptionText_;
  }

  /**
   Returns the path of the signon display file that is used when showing signon displays at work stations allocated to the subsystem.  By default, QSYS/QDSIGNON (path /QSYS.LIB/QDSIGNON.FILE) is used.
   @return The path of the signon display file. <tt>null</tt> if refresh() has not been called.
   **/
  public String getDisplayFilePath()
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");

    if (dspFileLibrary_ == null) return null;
    else return QSYSObjectPathName.toPath(dspFileLibrary_, dspFileName_, "FILE");
  }

  /**
   Returns the library that is entered ahead of other libraries in the system portion of the library list. This library typically specifies a secondary language library.
   @return The name of the secondary language library. <tt>null</tt> if refresh() has not been called.  "*NONE" if no secondary language library is set.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public String getLanguageLibrary()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");

    return langLibrary_;
  }

  /**
   Returns the name of the library where the subsystem resides on the server.
   @return The name of the library.
   **/
  public String getLibrary()
  {
    return library_;
  }

  /**
   Returns the maximum number of active jobs that can run or use resources within the subsystem at one time.
   @return The maximum number of active jobs. 0 if refresh() has not been called. {@link #NO_MAX NO_MAX} if no maximum. 
   **/
  public int getMaximumActiveJobs()
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");

    return maxActiveJobs_;
  }

  /**
   Returns the subsystem monitor job.
   @return The subsystem monitor job. <tt>null</tt> if refresh() has not been called, or if subsystem status is *INACTIVE.
   **/
  public Job getMonitorJob()
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");

    if (monitorJobName_ == null || monitorJobUser_ == null ||
        monitorJobNumber_ == null || monitorJobName_.length() == 0) {
      return null;
    }

    return new Job(system_, monitorJobName_, monitorJobUser_, monitorJobNumber_);
  }

  /**
   Returns the subsystem name.
   @return The subsystem name.
   **/
  public String getName()
  {
    return name_;
  }

  /**
   Returns an ObjectDescription instance representing the subsystem.
   @return An ObjectDescription for the subsystem.
   **/
  public ObjectDescription getObjectDescription()
  {
    if (objectDescription_ == null) { objectDescription_ = getObjDesc(); }
    return objectDescription_;
  }

  /**
   Returns the fully-qualifed IFS pathname of the subsystem.
   For example: "/QSYS.LIB/MYSUBSYS.SBSD".
   @return The path of the subsystem.
   **/
  public String getPath()
  {
    return path_;
  }

  /**
   Returns the storage pool defined at the specified position for the subsystem.
   @param sequenceNumber The number of the pool, within the pool list for the subsystem.  Valid values are 1-10.
   @return The pool used by the subsystem, at the specified sequence position. <tt>null</tt> if refresh() has not been called, or if no pool has been assigned at that position.
   **/
  public SystemPool getPool(int sequenceNumber)
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");
    if (sequenceNumber<1 || sequenceNumber>10) {
      throw new ExtendedIllegalArgumentException("sequenceNumber", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    return pools_[sequenceNumber-1];
  }

  /**
   Returns the list of storage pools that are defined for the subsystem.  That is, the list of storage pool definitions that are in the subsystem description.
   The element at offset 0 represents subsystem pool #1, element at offset 1 represents pool #2, and so on.
   In positions where no pool assignment has been made, the array element will be null.
   @return The pools that are used by the subsystem. <tt>null</tt> if refresh() has not been called.  Otherwise, a 10-element array is returned.
   **/
  public SystemPool[] getPools()
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");

    return pools_;
  }

  /**
   Returns the extended status of the subsystem.
   Possible values are: *ACTIVE, *ENDING, *INACTIVE, *RESTRICTED, and *STARTING.
   @return The subsystem status. <tt>null</tt> if refresh() has not been called.
   **/
  public String getStatus()
  {
    if (Trace.isTraceOn() && !refreshed_) Trace.log(Trace.WARNING, "The refresh() method has not yet been called.");

    return extendedStatus_;
  }

  /**
   Returns the system where the subsystem resides.
   @return The system.
   **/
  public AS400 getSystem()
  {
    return system_;
  }


  /**
   Lists all subsystems on the system.
   @return A list of all defined subsystems, both active and inactive.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   @exception RequestNotSupportedException    If the requested function is not supported because the server is not at the correct level.
   **/
  public static Subsystem[] listAllSubsystems(AS400 system)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException,
  RequestNotSupportedException
  {
    Vector sbsList = new Vector(20);
    ObjectList objList = new ObjectList(system, "*ALL", "*ALL", "*SBSD");
    Enumeration list = objList.getObjects();
    while (list.hasMoreElements()) {
      ObjectDescription objDesc = (ObjectDescription)list.nextElement();
      Subsystem sbs = new Subsystem(system, objDesc.getPath());
      sbsList.addElement(sbs);
    }
    Subsystem[] sbsArray = new Subsystem[sbsList.size()];
    sbsList.toArray(sbsArray);
    return sbsArray;
  }


  /**
   Refreshes the attributes of this Subsystem object, to reflect the current state of the subsystem on the server.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public synchronized void refresh()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    final int ccsid = system_.getCcsid();
    final ConvTable conv = ConvTable.getTable(ccsid, null);

    // Call the API, specifying format SBSI0200.  This will retrieve most of the attributes.

    ProgramParameter[] parms = new ProgramParameter[5];
    int outputSize = 200;
    parms[0] = new ProgramParameter(outputSize);
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(outputSize));
    parms[2] = new ProgramParameter(conv.stringToByteArray("SBSI0200"));
    QSYSObjectPathName qsys = new QSYSObjectPathName(path_);
    AS400Text text10 = new AS400Text(10, ccsid);
    byte[] qualifiedSubsystemName = new byte[20];
    text10.toBytes(qsys.getObjectName(), qualifiedSubsystemName, 0);
    text10.toBytes(qsys.getLibraryName(), qualifiedSubsystemName, 10);
    parms[3] = new ProgramParameter(qualifiedSubsystemName);
    parms[4] = new ProgramParameter(new byte[4]);

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QWDRSBSD.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    byte[] data = parms[0].getOutputData();
    int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
    int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
    while (bytesReturned < bytesAvailable)
    {
      outputSize += bytesAvailable*2;
      try
      {
        parms[0].setOutputDataLength(outputSize);
        parms[1].setInputData(BinaryConverter.intToByteArray(outputSize));
      }
      catch (PropertyVetoException pve) {} // this will never happen
      if (!pc.run())
      {
        throw new AS400Exception(pc.getMessageList());
      }
      data = parms[0].getOutputData();
      bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
      bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
    }
    int offset = BinaryConverter.byteArrayToInt(data, 8);
    int numEntries = BinaryConverter.byteArrayToInt(data, 12); // This had better be 1.
    int entrySize = BinaryConverter.byteArrayToInt(data, 16);
    name_ = conv.byteArrayToString(data, offset, 10).trim();
    library_ = conv.byteArrayToString(data, offset+10, 10).trim();
    extendedStatus_ = conv.byteArrayToString(data, offset+20, 12).trim();
    maxActiveJobs_ = BinaryConverter.byteArrayToInt(data, offset+32);
    currentActiveJobs_ = BinaryConverter.byteArrayToInt(data, offset+36);
    monitorJobName_ = conv.byteArrayToString(data, offset+40, 10).trim();
    monitorJobUser_ = conv.byteArrayToString(data, offset+50, 10).trim();
    monitorJobNumber_ = conv.byteArrayToString(data, offset+60, 6).trim();
    descriptionText_ = conv.byteArrayToString(data, offset+66, 50).trim();


    // Call the API again, specifying format SBSI0100.  This will retrieve some additional attributes.

    outputSize = 500;
    parms[0] = new ProgramParameter(outputSize);
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(outputSize));
    parms[2] = new ProgramParameter(conv.stringToByteArray("SBSI0100"));

    pc = new ProgramCall(system_, "/QSYS.LIB/QWDRSBSD.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    data = parms[0].getOutputData();
    bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
    bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
    while (bytesReturned < bytesAvailable)
    {
      outputSize += bytesAvailable*2;
      try
      {
        parms[0].setOutputDataLength(outputSize);
        parms[1].setInputData(BinaryConverter.intToByteArray(outputSize));
      }
      catch (PropertyVetoException pve) {} // this will never happen
      if (!pc.run())
      {
        throw new AS400Exception(pc.getMessageList());
      }
      data = parms[0].getOutputData();
      bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
      bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
    }

    dspFileName_ = conv.byteArrayToString(data, 38, 10).trim();
    dspFileLibrary_ = conv.byteArrayToString(data, 48, 10).trim();
    langLibrary_ = conv.byteArrayToString(data, 58, 10).trim();
    int numPools = BinaryConverter.byteArrayToInt(data, 76);
    pools_ = new SystemPool[10];
    if (DEBUG) System.out.println("SubSystem.refresh(): Number of pools reported: " + numPools);

    int poolID = 0;
    for (int i=0; i<numPools; i++)
    {
      // Note: Offset to start of pool list is 80.  Each entry is 28 bytes long.
      int offsetToEntry = 80 + 28*i;
      poolID = BinaryConverter.byteArrayToInt(data, offsetToEntry);
      if (DEBUG) {
        System.out.println("Pool ID: " + poolID);
        if (poolID < 1 || poolID > 10) System.out.println("ERROR: Unexpected poolID value: " + poolID);  // This should never happen.
      }
      String poolName = conv.byteArrayToString(data, offsetToEntry+4, 10).trim();
      if (DEBUG) System.out.println("Subsystem.refresh(): Returned poolName is: " + poolName);
      int size = BinaryConverter.byteArrayToInt(data, offsetToEntry+20);
      int activityLevel = BinaryConverter.byteArrayToInt(data, offsetToEntry+24);
      SystemPool pool = null;
      if (poolName.equals("*USERPOOL")) {  // it's a private pool
        pool = new SystemPool(this, poolID, size, activityLevel);
      }
      else {  // it's a shared pool
        pool = new SystemPool(system_, poolName);
      }
      pool.setCaching(true);
      pools_[poolID-1] = pool;
    }
    for (int i=poolID; i<10; i++) { pools_[i] = null; }  // fill remaining slots with nulls

    refreshed_ = true;
  }


  /**
   Removes a storage pool from the list of pools defined for the subsystem.
   You should first end the subsystem before removing a pool.
   @param sequenceNumber The number of the pool, within the pool list for the subsystem.  Valid values are 1-10.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void removePool(int sequenceNumber)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (sequenceNumber<1 || sequenceNumber>10) {
      throw new ExtendedIllegalArgumentException("sequenceNumber", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    CommandCall cmd = new CommandCall(system_, "QSYS/CHGSBSD SBSD("+library_+"/"+name_+") POOLS(("+sequenceNumber+" *RMV))");
    if (DEBUG) System.out.println("Command string: " + cmd.getCommand());
    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        throw new AS400Exception(msgs);
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.", InternalErrorException.UNKNOWN);
      }
    }
  }

  /**
   Starts the subsystem on the server.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   @exception ObjectDoesNotExistException If the object does not exist on the server.
   **/
  public void start()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    CommandCall cmd = new CommandCall(system_, "QSYS/STRSBS SBSD("+library_+"/"+name_+")");
    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        if (msgs[0].getID() != null &&
            !msgs[0].getID().equals("CPF1010"))    // subsystem already active
        {  
          throw new AS400Exception(msgs);
        }
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.",
                                         InternalErrorException.UNKNOWN);
      }
    }
  }



  // Private utility methods.



  /**
   Ends the subsystem on the server.
   @exception AS400Exception If the server returns an error message.
   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the server.
   **/
  private static void end(AS400 system, String subsystemName, boolean immediate, int timeLimit)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
  {
    String endOption = (immediate ? "*IMMED" : "*CNTRLD");
    StringBuffer cmdBuf = new StringBuffer("QSYS/ENDSBS SBS("+subsystemName+") OPTION("+endOption+")");
    if (!immediate) cmdBuf.append(" DELAY("+timeLimit+")");
    CommandCall cmd = new CommandCall(system, cmdBuf.toString());
    if (!cmd.run())
    {
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        if (msgs[0].getID() != null &&
            !msgs[0].getID().equals("CPF1032") &&  // system ending *CNTRLD
            !msgs[0].getID().equals("CPF1033") &&  // system ending *IMMED
            !msgs[0].getID().equals("CPF1034") &&  // subsys's ending *CNTRLD
            !msgs[0].getID().equals("CPF1035") &&  // subsys's ending *IMMED
            !msgs[0].getID().equals("CPF1036") &&  // sys powering-down *CNTRLD
            !msgs[0].getID().equals("CPF1037") &&  // sys powering-down *IMMED
            !msgs[0].getID().equals("CPF1054") &&  // subsys not active
            !msgs[0].getID().equals("CPF1055") &&  // subsys ending *CNTRLD
            !msgs[0].getID().equals("CPF1056"))    // subsys ending *IMMED
        {  
          throw new AS400Exception(msgs);
        }
      }
      else {
        throw new InternalErrorException("No messages returned from failed command.",
                                         InternalErrorException.UNKNOWN);
      }
    }
  }


  /**
   Gets an ObjectDescription object representing the subsystem.
   **/
  private ObjectDescription getObjDesc()
  {
    return new ObjectDescription(system_, library_, name_, "SBSD");
  }

}

