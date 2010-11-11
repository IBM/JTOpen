///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: CommandCall.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2007 International Business Machines Corporation and
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
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.as400.resource.RJob;  // Remove when getJob() is removed.

/**
 Represents an IBM i command object.  This class allows the user to call any non-interactive CL command.  
 Results of the command are returned in a message list.
 <P>Note: CommandCall is not designed to return interactive (screen-oriented) results, such as from 
    "WRK..." and "DSP..." commands.  The recommended approach in such cases is to identify an equivalent 
    IBM i API or program, and use {@link ProgramCall ProgramCall} instead.
 <P>The following example demonstrates the use of CommandCall:
 <br>
 <pre>
 *    // Work with commands on system named "Hal."
 *    AS400 system = new AS400("Hal");
 *    CommandCall command = new CommandCall(system);
 *    try
 *    {
 *        // Run the command "CRTLIB FRED."
 *        if (command.run("CRTLIB FRED") != true)
 *        {
 *            // Note that there was an error.
 *            System.out.println("Command failed!");
 *        }
 *        // Show the messages (returned whether or not there was an error.)
 *        AS400Message[] messagelist = command.getMessageList();
 *        for (int i = 0; i < messagelist.length; ++i)
 *        {
 *            // Show each message.
 *            System.out.println(messagelist[i].getText());
 *        }
 *    }
 *    catch (Exception e)
 *    {
 *        System.out.println("Command " + command.getCommand() + " issued an exception!");
 *        e.printStackTrace();
 *    }
 *    // Done with the system.
 *    system.disconnectService(AS400.COMMAND);
 </pre>
 <p>NOTE:  When getting the message list from commands, users no longer have to create a 
    <a href="MessageFile.html">MessageFile</a> to obtain the message help text.  
    The load() method can be used to retrieve additional message information. 
    Then the getHelp() method can be called directly on the <a href="AS400Message.html">AS400Message</a> 
    object returned from getMessageList().  Here is an example:
 <pre>
 *    if (command.run("myCmd") != true)
 *    {
 *        // Show messages.
 *        AS400Message[] messageList = command.getMessageList();
 *        for (int i = 0; i < messageList.length; ++i)
 *        {
 *            // Show each message.
 *            System.out.println(messageList[i].getText());
 *            // Load additional message information.
 *            messageList[i].load();
 *            //Show help text.
 *            System.out.println(messageList[i].getHelp());
 *        }
 *    }
 </pre>
 @see Command
 **/
public class CommandCall implements Serializable
{
    private static final String CLASSNAME = "com.ibm.as400.access.CommandCall";
    static
    {
        if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
    }

    static final long serialVersionUID = 4L;

    // Constants that indicate how thread safety is specified/determined.
    private static final int UNSPECIFIED = 0;
                     // property not specified; setThreadSafe() not called
    private static final int SPECIFIED_BY_PROPERTY = 1;
                     // property was set
    private static final int SPECIFIED_BY_SETTER = 2;
                     // setThreadSafe() was called

    /**
     Indicates that the command should be assumed to be non-threadsafe.
     **/
    public static final Boolean THREADSAFE_FALSE  = Boolean.FALSE;

    /**
     Indicates that the command should be assumed to be threadsafe.
     **/
    public static final Boolean THREADSAFE_TRUE = Boolean.TRUE;

    /**
     Indicates that the command's threadsafety should be looked-up at runtime.
     This setting should be used with caution, especially if this CommandCall object will be used to 
     call a sequence of different commands that need to use the same QTEMP library or the same modified 
     LIBLIST.
     **/
    public static final Boolean THREADSAFE_LOOKUP = null;

    // The system where the command is located.
    private AS400 system_ = null;
    // The command to run.
    private String command_ = "";
    // The messages returned by the command.
    private AS400Message[] messageList_ = new AS400Message[0];

    // The assumed thread safety of command.
    private transient Boolean threadSafetyValue_ = THREADSAFE_FALSE; // default: assume not threadsafe

    // The following field is needed in order to preserve cross-release serializability between 
    // JTOpen 6.4 and later releases.
    // Thread safety of command.
    private boolean threadSafety_ = false; // must be kept in sync with threadSafetyValue_

    // Indicates whether threadsafety is to be looked-up at run time.
    private transient boolean threadSafetyIsLookedUp_ = false;
    // How thread safety was specified by user.
    private int threadSafetyDetermined_ = UNSPECIFIED;

    // The number of messages to retrieve.
    private int messageOption_ = AS400Message.MESSAGE_OPTION_UP_TO_10;  // Default for compatibility.

    // Implementation object shared with program call, interacts with host server or native methods.
    private transient RemoteCommandImpl impl_ = null;

    // List of action completed event bean listeners.
    private transient Vector actionCompletedListeners_ = null;  // Set on first add.
    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a CommandCall object.  The system and the command properties must be set before using 
     any method requiring a connection to the system.
     **/
    public CommandCall()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing CommandCall object.");
        applyThreadSafetyProperty();
    }

    /**
     Constructs a CommandCall object.  It uses the specified system.  The command must be set later.
     @param  system  The system on which to run the command.
     **/
    public CommandCall(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing CommandCall object, system: " + system);
        if (system == null) {
            throw new NullPointerException("system");
        }
        system_ = system;
        applyThreadSafetyProperty();
    }

    /**
     Constructs a CommandCall object.  It uses the specified system and command.
     @param  system  The system on which to run the command.
     @param  command  The command to run on the system.  If the command is not library qualified, 
             the library list will be used to find the command.
     **/
    public CommandCall(AS400 system, String command)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing CommandCall object, system: " + system + " command: " + command);
        if (system == null) {
            throw new NullPointerException("system");
        }
        if (command == null) {
            throw new NullPointerException("command");
        }

        system_  = system;
        command_ = command;
        applyThreadSafetyProperty();
    }

    /**
     Adds an ActionCompletedListener.  The specified ActionCompletedListener's <b>actionCompleted</b> 
     method will be called each time a command has run.  The ActionCompletedListener object is 
     added to a list of ActionCompletedListeners managed by this CommandCall.  It can be removed 
     with removeActionCompletedListener.
     @param  listener  The listener object.
     **/
    public void addActionCompletedListener(ActionCompletedListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding action completed listener.");
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (actionCompletedListeners_ == null)
            {
                actionCompletedListeners_ = new Vector();
            }
            actionCompletedListeners_.addElement(listener);
        }
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> method 
     will be called each time the value of any bound property is changed.  The PropertyChangeListener 
     object is added to a list of PropertyChangeListeners managed by this CommandCall.  It can be removed 
     with removePropertyChangeListener.
     @param  listener  The listener object.
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null) {
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
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange</b> 
     method will be called each time the value of any constrained property is changed.
     @param  listener  The listener object.
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null) {
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

    // Chooses the appropriate implementation, synchronize to protect impl_ object.
    private synchronized void chooseImpl() throws AS400SecurityException, ErrorCompletingRequestException, 
          IOException, InterruptedException
    {
        if (system_ != null) system_.signon(false);
        if (impl_ == null)
        {
            if (system_ == null)
            {
                /*
                 if (AS400.onAS400)
                 {
                 impl_ = (RemoteCommandImpl)AS400.loadImpl("com.ibm.as400.access.RemoteCommandImplNative");
                 if (impl_ != null) return;
                 }*/
                Trace.log(Trace.ERROR, "Attempt to connect to command server before setting system." );
                throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }

            impl_ = (RemoteCommandImpl)system_.loadImpl3("com.ibm.as400.access.RemoteCommandImplNative", 
            		"com.ibm.as400.access.RemoteCommandImplRemote", 
            		"com.ibm.as400.access.RemoteCommandImplProxy");
            impl_.setSystem(system_.getImpl());
        }

        // If needed, look up the threadsafety indicator on the system.
        if (threadSafetyValue_ == THREADSAFE_LOOKUP)
        {
          if (command_ == null || command_.length() == 0) {
            // Until the command is set, assume it will be non-threadsafe.
            threadSafetyValue_ = THREADSAFE_FALSE;
          }
          else if (!impl_.isNative()) {
            // If not running natively, don't bother to lookup the threadsafe indicator.
            threadSafetyValue_ = THREADSAFE_FALSE;
          }
          else {
            int indicator = impl_.getThreadsafeIndicator(command_); // look it up
            if (indicator == RemoteCommandImpl.THREADSAFE_INDICATED_YES) {
              threadSafetyValue_ = THREADSAFE_TRUE;
            }
            else {
              threadSafetyValue_ = THREADSAFE_FALSE; // treat *COND the same as *NO
            }
          }
          threadSafety_ = (THREADSAFE_TRUE.equals(threadSafetyValue_ ));
        }
    }

    // Fires the action completed event.
    private void fireActionCompleted()
    {
        Vector targets = (Vector)actionCompletedListeners_.clone();
        ActionCompletedEvent event = new ActionCompletedEvent(this);
        for (int i = 0; i < targets.size(); ++i)
        {
            ActionCompletedListener target = (ActionCompletedListener)targets.elementAt(i);
            target.actionCompleted(event);
        }
    }

    /**
     Returns the command to run.  It will return an empty string ("") if the command has not been previously 
     set.
     @return  The command to run.
     **/
    public String getCommand()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting command: " + command_);
        return command_;
    }


    // 2010-02-16: Temporarily reinstated the getJob() method, since it is used by V5R4 IPP Server, 
    // which is still in service.
    // The getJob() method will be deleted, either:
    //  (1) when IBM i V5R4 goes out-of-service, or
    //  (2) when V5R4 IPP Server is PTF'd to not call this method,
    // whichever comes first.

    // Removed this obsolete method.  Deprecated on 2003-01-22.
//    /**
//     Returns an RJob object which represents the system job in which the command will be run.  The information contained in the RJob object is invalidated by <code>AS400.disconnectService()</code> or <code>AS400.disconnectAllServices()</code>.
//     <br>Typical uses include:
//     <br>(1) before run() to identify the job before calling the command;
//     <br>(2) after run() to see what job the command ran under (to identify the job log, for example).
//     <p><b>Note:</b> This method is not supported in the Toolbox proxy environment.
//     @return  The job in which the command will be run.
//     @exception  AS400SecurityException  If a security or authority error occurs.
//     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
//     @exception  IOException  If an error occurs while communicating with the system.
//     @exception  InterruptedException  If this thread is interrupted.
//     @deprecated  Use getServerJob() instead.
//     **/
    /**
     Do not use this method. <b>It is obsolete and will be removed in a future release.</b>
     @deprecated  Use getServerJob() instead.
     **/
    public RJob getJob() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting job.");
        chooseImpl();
        String jobInfo = impl_.getJobInfo(threadSafetyValue_);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing RJob for job: " + jobInfo);
        // Contents of the "job information" string:  The name of the user job that the thread is associated with.  The format of the job name is a 10-character simple job name, a 10-character user name, and a 6-character job number.
        return new RJob(system_, jobInfo.substring(0, 10).trim(), jobInfo.substring(10, 20).trim(), jobInfo.substring(20, 26).trim());
    }

    /**
     Returns the option for how many messages will be retrieved.
     @return  A constant indicating how many messages will be retrieved.  Valid values are:
     <ul>
     <li>{@link AS400Message#MESSAGE_OPTION_UP_TO_10 MESSAGE_OPTION_UP_TO_10}
     <li>{@link AS400Message#MESSAGE_OPTION_NONE MESSAGE_OPTION_NONE}
     <li>{@link AS400Message#MESSAGE_OPTION_ALL MESSAGE_OPTION_ALL}
     </ul>
     **/
    public int getMessageOption()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message option:", messageOption_);
        return messageOption_;
    }

    /**
     Returns the list of messages returned from running the command.  It will return an empty list if the 
     command has not been run yet or if there are no messages.
     @return  The array of messages returned by the command.
     **/
    public AS400Message[] getMessageList()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message list.");
        return messageList_;
    }

    /**
     Returns a message returned from running the command.
     @param  index  The index into the list of messages returned by the command.  It must be greater than 
     or equal to zero and less than the number of messages in the list.
     @return  The message at the requested index returned by the command.
     **/
    public AS400Message getMessageList(int index)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message from message list:", index);
        return messageList_[index];
    }

    /**
     Returns a Job object which represents the system job in which the command will be run.
     The information contained in the Job object is invalidated by <code>AS400.disconnectService()</code>
      or <code>AS400.disconnectAllServices()</code>.
     <br>Typical uses include:
     <br>(1) before run() to identify the job before calling the command;
     <br>(2) after run() to see what job the command ran under (to identify the job log, for example).
     <p><b>Note:</b> This method is not supported in the Toolbox proxy environment.
     @return  The job in which the command will be run.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     **/
    public Job getServerJob() throws AS400SecurityException, ErrorCompletingRequestException, 
    IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting job.");
        chooseImpl();
        String jobInfo = impl_.getJobInfo(threadSafetyValue_);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing Job for job: " + jobInfo);
        // Contents of the "job information" string:  The name of the user job that the thread is 
        // associated with.  The format of the job name is a 10-character simple job name, 
        // a 10-character user name, and a 6-character job number.
        return new Job(system_, jobInfo.substring(0, 10).trim(), jobInfo.substring(10, 20).trim(), jobInfo.substring(20, 26).trim());
    }

    /**
     Returns the system on which the command is to be run.
     @return  The system on which the command is to be run.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns the thread on which the command would be run, if it were to be called on-thread.
     @return  The thread on which the command would be run.
     Returns null if either:
     <ul compact>
     <li> The client is communicating with the system through sockets.
     <li> The command has not been marked as thread safe.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  IOException  If an error occurs while communicating with the system.
     **/
    public Thread getSystemThread() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system thread.");
        chooseImpl();
        Thread currentThread = impl_.isNative() ? Thread.currentThread() : null;
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "System thread: " + currentThread);
        return currentThread;
    }

    static final String getThreadSafetyProperty()
    {
      String val = SystemProperties.getProperty(SystemProperties.COMMANDCALL_THREADSAFE);
      return (val == null || val.length()==0 ? null : val.toLowerCase());
    }

    // Apply the value of the thread safety system property (if it has been set).
    // Note: This method is reserved for use by the constructors.
    // Caution: If you call this method elsewhere, you might override a value set by setThreadSafe().
    private void applyThreadSafetyProperty()
    {
      String property = getThreadSafetyProperty();
      if (property == null)  // Property not set.
      {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Thread safe system property not set; thread safety property remains unspecified.");
      }
      else
      {
        if (property.equals("true")) {
          threadSafetyValue_ = THREADSAFE_TRUE;
          threadSafetyDetermined_ = SPECIFIED_BY_PROPERTY;
        }
        else if (property.equals("false")) {
          threadSafetyValue_ = THREADSAFE_FALSE;
          threadSafetyDetermined_ = SPECIFIED_BY_PROPERTY;
        }
        else if (property.equals("lookup")) {
          threadSafetyValue_ = THREADSAFE_LOOKUP;
          threadSafetyDetermined_ = SPECIFIED_BY_PROPERTY;
          threadSafetyIsLookedUp_ = true;
        }
        else {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unrecognized value for CommandCall.threadSafe property: " + property + ". Defaulting to 'false'.");
        }
        threadSafety_ =  THREADSAFE_TRUE.equals(threadSafetyValue_);
      }
    }


    /**
     Returns the value of the "Threadsafe" attribute of the CL command on the system.
     @return The value of the command's Threadsafe attribure.  Valid values are:
     <ul>
     <li>0 - The command is not threadsafe and should not be used in a multithreaded job.
     <li>1 - The command is threadsafe and can be used safely in a multithreaded job.
     <li>2 - The command is threadsafe under certain conditions. See the documentation for the command to determine the conditions under which the command can be used safely in a multithreaded job.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     **/
    public int getThreadsafeIndicator() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (command_ == null || command_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Attempt to retrieve Threadsafe indicator before setting command.");
            throw new ExtendedIllegalStateException ("command", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        chooseImpl();
        return impl_.getThreadsafeIndicator(command_);
    }


    /**
     Indicates whether or not the command will actually get run on the current thread.
     <br>Note: If the command is run on-thread, it will run in a different job (with different QTEMP library and different job log) than if it were run off-thread.
     <br>Note: If the threadsafety behavior is set to {@link #THREADSAFE_LOOKUP THREADSAFE_LOOKUP}, then the value returned by this method will depend on the command string that has been specified, in either the constructor or in {@link #setCommand setCommand()}.
     @return  true if the command will be run on the current thread; false otherwise.
     If the application is not running on IBM i, false is always returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @see #setThreadSafe(Boolean)
     **/
    public boolean isStayOnThread() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if command will actually get run on the current thread.");
        chooseImpl();
        boolean isStayOnThread = ((THREADSAFE_TRUE.equals(threadSafetyValue_)) &&
                                  impl_.isNative());
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Will command actually get run on the current thread:", isStayOnThread);
        return isStayOnThread;
    }

    /**
     Indicates whether or not the command will be assumed thread-safe. The determination is based upon the settings specified by {@link #setThreadSafe(Boolean) setThreadSafe()} or the <code>com.ibm.as400.access.CommandCall.threadSafe</code> property.
     <br>Note: If the CL command on the system is not actually threadsafe (as indicated by its "threadsafe indicator" attribute), then the results of attempting to run the command on-thread will depend on the command's "multithreaded job action" attribute, in combination with the setting of system value QMLTTHDACN ("Multithreaded job action").
     Possible results are:
     <ul>
     <li> Run the command. Do not send a message.
     <li> Send an informational message and run the command.
     <li> Send an escape message, and do not run the command.
     </ul>
     <br>Note: If the command is run on-thread, it will run in a different job (with different QTEMP library and different job log) than if it were run off-thread.
     @return  true if the command will be assumed thread-safe; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @deprecated The name of this method is misleading. Use {@link #isStayOnThread isStayOnThread()} or {@link #getThreadsafeIndicator getThreadsafeIndicator()} instead.
     **/
    public boolean isThreadSafe() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if command will be assumed thread-safe.");

        chooseImpl();

        return threadSafetyValue_.booleanValue();
    }


    // Deserializes and initializes the transient data.
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "De-serializing CommandCall object.");
      in.defaultReadObject();

      // impl_ remains null.
      // actionCompletedListeners_ remains null.
      // propertyChangeListeners_ remains null.
      // vetoableChangeListeners_ remains null.

      threadSafetyIsLookedUp_ = false; // to maintain cross-version serializability

      // Prior to JTOpen 6.5, threadSafetyDetermined_ could have an additional value: BY_LOOK_UP = 3.  If we get that obsolete value, convert it to UNSPECIFIED.
      if (threadSafetyDetermined_ == 3)  // old value that indicated "threadsafety is looked up"
      {
        threadSafetyDetermined_ = UNSPECIFIED;
        // Note: Don't set threadSafetyIsLookedUp_ here; let applyThreadSafetyProperty() set it.  We will tolerate the potential loss of "lookup" behavior during serialization/deserialization.
      }


      // See how we determined this object's thread-safety before it was serialized.
      if (threadSafetyDetermined_ == SPECIFIED_BY_SETTER)
      {
        threadSafetyValue_ = (threadSafety_ == true ? THREADSAFE_TRUE : THREADSAFE_FALSE);
      }
      else  // the thread-safety value was not explicitly specified by the application
      {
        // (Re)initialize the thread-safe attribute.  Note:  The threadSafety_ attribute is persistent, not transient.  First see if object was previously serialized when its thread-safe behavior was determined by a system property (and not specified via setThreadSafe()).  This system property may have since changed.
        // Disregard any previous settings, since they were derived from properties.
        threadSafetyValue_ = THREADSAFE_FALSE;
        threadSafety_ = false;
        threadSafetyIsLookedUp_ = false;
        String property = getThreadSafetyProperty();
        if (property == null) // The property is not set in the current environment.
        {
          threadSafetyDetermined_ = UNSPECIFIED;
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Thread safe system property not set, so thread safety property changed to unspecified.");
        }
        else  // The property is set in the current environment, so use it.
        {
          applyThreadSafetyProperty();
        }
      }
    }

    /**
     Removes the ActionCompletedListener.  If the ActionCompletedListener is not on the list, nothing is done.
     @param  listener  The listener object.
     **/
    public void removeActionCompletedListener(ActionCompletedListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing action completed listener.");
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (actionCompletedListeners_ != null)
        {
            actionCompletedListeners_.removeElement(listener);
        }
    }

    /**
     Removes the PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
     @param  listener  The listener object.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null) {
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
     @param  listener  The listener object.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (vetoableChangeListeners_ != null)
        {
            vetoableChangeListeners_.removeVetoableChangeListener(listener);
        }
    }

    /**
     Runs the command on the system.  The command must be set prior to this call.
     <br>Note: Interactive (screen-oriented) results are not returned.
     @return  true if command is successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     **/
    public boolean run() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Running command: " + command_);
        if (command_ == null || command_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Attempt to run before setting command.");
            throw new ExtendedIllegalStateException ("command", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        chooseImpl();

        // Run the command.
        boolean result = impl_.runCommand(command_, threadSafetyValue_, messageOption_);
        // Retrieve the messages.
        messageList_ = impl_.getMessageList();
        // Set our system into each of the messages.
        if (system_ != null)
        {
            for (int i = 0; i < messageList_.length; ++i)
            {
                messageList_[i].setSystem(system_);
            }
        }

        // Fire action completed event.
        if (actionCompletedListeners_ != null) fireActionCompleted();
        return result;
    }

    /**
     Sets the command and runs it on the system.
     <br>Note: Interactive (screen-oriented) results are not returned.
     @param  command  The command to run.  If the command is not library qualified, the library list will be used to find the command.
     @return  true if command is successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public boolean run(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, PropertyVetoException
    {
        setCommand(command);
        return run();
    }

    /**
     Runs the command on the system.  This method takes the command to run as a byte array instead of a String.  The most common use of CommandCall is to supply the command to run as a String and let the Toolbox convert the string to IBM i format (EBCDIC) before sending it to the system for processing.  Use this method if the default conversion of the command to EBCDIC is not correct.  In certain cases, especially bi-directional languages, the Toolbox conversion may not be correct.  In this case the application can construct their own command and supply it to CommandCall as a byte array.
     <p>Unlike the run method that takes a string, this method cannot look up the thread safety of the command, and will assume that the command is not thread-safe.  If this command is to be run on-thread when running on the system's JVM, setThreadSafe(true) must be called by the application.
     <br>Note: Interactive (screen-oriented) results are not returned.
     @param  command  The command to run.
     @return  true if command is successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public boolean run(byte[] command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Running command:", command);
        if (command == null) {
            throw new NullPointerException("command");
        }
        if (command.length == 0)
        {
            throw new ExtendedIllegalArgumentException("command.length (" + command.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        chooseImpl();

        // Run the command.
        boolean success = impl_.runCommand(command, threadSafetyValue_, messageOption_);

        // Retrieve the messages.
        messageList_ = impl_.getMessageList();

        // Set our system into each of the messages.
        if (system_ != null)
        {
            for (int i = 0; i < messageList_.length; ++i)
            {
                messageList_[i].setSystem(system_);
            }
        }

        // Fire action completed event.
        if (actionCompletedListeners_ != null) fireActionCompleted();
        return success;
    }

    /**
     Sets the command to run.
     @param  command  The command to run on the system.  If the command is not library qualified, the library list will be used to find the command.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setCommand(String command) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting command: " + command);
        if (command == null) {
            throw new NullPointerException("command");
        }
        if (Trace.traceOn_ && command.length() == 0)
        {
            Trace.log(Trace.WARNING, "Parameter 'command' is has length of 0.");
        }

        // See if we need to force another lookup of command's threadsafety.
        boolean forceLookup = false;
        if (threadSafetyIsLookedUp_ && (threadSafetyValue_ != THREADSAFE_LOOKUP))
        {
          // If the command name is different from before, force another lookup of threadsafety.
          // We can disregard the command argument string when doing the compare.
          if (!firstToken(command).equalsIgnoreCase(firstToken(command_)))
          {
            forceLookup = true;
          }
        } 

        String oldValue = command_;
        String newValue = command;

        if (vetoableChangeListeners_ != null)
        {
          vetoableChangeListeners_.fireVetoableChange("command", oldValue, newValue);
        }
        command_ = newValue;
        if (propertyChangeListeners_ != null)
        {
          propertyChangeListeners_.firePropertyChange("command", oldValue, newValue);
        }

        if (forceLookup) {
          threadSafetyValue_ = THREADSAFE_LOOKUP;
          threadSafety_ = false;
        }
    }

    // Returns the first token in the String, or "" if no first token.
    private static final String firstToken(String string)
    {
      String token = null;
      try
      {
        StringTokenizer tokenizer = new StringTokenizer(string);
        token = tokenizer.nextToken();
      }
      catch (java.util.NoSuchElementException e) {
        token = "";
      }
      return token;
    }

    /**
     Specifies the option for how many messages should be retrieved.  By default, to preserve compatability, only the messages sent to the command caller and only up to ten messages are retrieved.  This property will only take affect on systems that support the new option.
     @param  messageOption  A constant indicating how many messages to retrieve.  Valid values are:
     <ul>
     <li>{@link AS400Message#MESSAGE_OPTION_UP_TO_10 MESSAGE_OPTION_UP_TO_10}
     <li>{@link AS400Message#MESSAGE_OPTION_NONE MESSAGE_OPTION_NONE}
     <li>{@link AS400Message#MESSAGE_OPTION_ALL MESSAGE_OPTION_ALL}
     </ul>
     **/
    public void setMessageOption(int messageOption)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message option:", messageOption);
        // Validate the messageOption parameter.
        if (messageOption < 0 || messageOption > 2)
        {
            throw new ExtendedIllegalArgumentException("messageOption (" + messageOption + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        messageOption_ = messageOption;
    }

    /**
     Sets the system to run the command.  The system cannot be changed once a connection is made to the system.
     @param  system  The system on which to run the command.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);
        if (system == null) {
            throw new NullPointerException("system");
        }
        if (impl_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        AS400 oldValue = system_;
        AS400 newValue = system;

        if (vetoableChangeListeners_ != null)
        {
          vetoableChangeListeners_.fireVetoableChange("system", oldValue, newValue);
        }
        system_ = newValue;
        if (propertyChangeListeners_ != null)
        {
          propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
        }
    }

    /**
     Specifies whether or not the command should be assumed thread-safe.  If not specified, the default is false; that is, the command will be assumed to be not thread-safe.
     <br>This method is an alternative to {@link #setThreadSafe(Boolean) setThreadSafe(Boolean)}.  For example, calling <tt>setThreadSafe(true)</tt> is equivalent to calling <tt>setThreadSafe(Boolean.TRUE)</tt>.
     <br>Note: This method has no effect if the Java application is running remotely, that is, is not running "natively" on an IBM i system.  When running remotely, the Toolbox submits all command calls through the Remote Command Host Server, regardless of the value of the <tt>threadSafe</tt> attribute.
     <br>Note:  This method does not modify the actual command object on the system.
     <br>Note: If the command is run on-thread, it will run in a different job (with different QTEMP library and different job log) than if it were run off-thread.
     @param  threadSafe  true if the command should be assumed to be thread-safe; false otherwise.
     @see #setThreadSafe(Boolean)
     **/
    public void setThreadSafe(boolean threadSafe)
    {
        // Note to maintenance programmer:
        // Currently all host server jobs are single-threaded.  If that ever changes, then we'll need to communicate the threadsafety of the called command to the host server.
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting thread safe: " + threadSafe);
        Boolean newValue = (threadSafe ? THREADSAFE_TRUE : THREADSAFE_FALSE);
        if (propertyChangeListeners_ != null)
        {
            Boolean oldValue = threadSafetyValue_;
            propertyChangeListeners_.firePropertyChange ("threadSafe", oldValue, newValue);
        }

        threadSafetyValue_ = newValue;
        threadSafety_ = threadSafe;
        threadSafetyDetermined_ = SPECIFIED_BY_SETTER;
        threadSafetyIsLookedUp_ = false;
    }

    /**
     Specifies whether or not the command should be assumed thread-safe.  If not specified, the default is {@link #THREADSAFE_FALSE THREADSAFE_FALSE}.
     <br>Note: This method has no effect if the Java application is running remotely, that is, is not "natively" on an IBM i system.  When running remotely, all command calls are submitted through the Remote Command Host Server, regardless of the value of the <tt>threadSafe</tt> attribute.
     <br>Note: This method does not modify the actual command object on the system.
     <br>Note: If the command is run on-thread, it will run in a different job (with different QTEMP library and different job log) than if it were run off-thread.
     @param threadSafe
     Valid values are:
     <ul>
     <li>{@link #THREADSAFE_TRUE THREADSAFE_TRUE}
     <li>{@link #THREADSAFE_FALSE THREADSAFE_FALSE}
     <li>{@link #THREADSAFE_LOOKUP THREADSAFE_LOOKUP}
     </ul>
     @see #setThreadSafe
     @see #isStayOnThread
     **/
    public void setThreadSafe(Boolean threadSafe)
    {
        // Note to maintenance programmer:
        // Currently all host server jobs are single-threaded.  If that ever changes, then we'll need to communicate the threadsafety of the called command to the host server.
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting thread safe: " + threadSafe);

        Boolean newValue;

        // Note: Assign the static constants, so that later we can use "==" rather than "equals()".
        if (threadSafe.equals(THREADSAFE_TRUE)) {
          newValue = THREADSAFE_TRUE;
          threadSafetyIsLookedUp_ = false;
        }
        else if (threadSafe.equals(THREADSAFE_FALSE)) {
          newValue = THREADSAFE_FALSE;
          threadSafetyIsLookedUp_ = false;
        }
        else {
          newValue = THREADSAFE_LOOKUP;
          threadSafetyIsLookedUp_ = true;
        }

        if (propertyChangeListeners_ != null)
        {
            Boolean oldValue = threadSafetyValue_;
            propertyChangeListeners_.firePropertyChange ("threadSafe", oldValue, newValue);
        }

        threadSafetyValue_ = newValue;
        threadSafety_ = (threadSafetyValue_ == THREADSAFE_TRUE);
        threadSafetyDetermined_ = SPECIFIED_BY_SETTER;
    }

    /**
     Specifies whether or not the command should be assumed to be thread-safe.
     If the system property <tt>com.ibm.as400.access.CommandCall.threadSafe</tt> has been set
     to a value other than "lookup", this method does nothing.
     This method is typically used in order to suppress runtime lookups of
     the CL command's "Threadsafe Indicator" attribute.
     @param  threadSafe  true if the command should be assumed to be thread-safe; false if the command should be assumed to be not thread-safe.
     **/
    public void suggestThreadsafe(boolean threadSafe)
    {
      String property = getThreadSafetyProperty();
      if (property == null || property.equals("lookup"))
      {
        setThreadSafe(Boolean.valueOf(threadSafe));
      }
    }

    /**
     Returns the string representation of this command call object.
     @return  The string representing this command call object.
     **/
    public String toString()
    {
        return "CommandCall (system: " + system_ + " command: " + command_ + "):" + super.toString();
    }
}
