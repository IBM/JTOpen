///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: CommandCall.java
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
import java.util.Vector;

/**
 The CommandCall class represents an AS/400 command call object.  This class allows the user to call an AS/400 CL command.  Results of the command are returned in a message list.
 <P>The messages returned by CommandCall do not include help text.  MessageFile provides an easy way to get help for a messsage.
 <P>The following example demonstrates the use of CommandCall:
 <br>
 <pre>
    // Work with commands on system "Hal"
    AS400 system = new AS400("Hal");
    CommandCall cmd = new CommandCall(system);
    try
    {
        // Run the command "CRTLIB FRED"
        if (cmd.run("CRTLIB FRED") != true)
        {
            // Note that there was an error
            System.out.println("Program failed!");
        }
        // Show the messages (returned whether or not there was an error)
        AS400Message[] messagelist = cmd.getMessageList();
        for (int i = 0; i < messagelist.length; ++i)
        {
            // Show each message
            System.out.println(messagelist[i].getText());
        }
    }
    catch (Exception e)
    {
        System.out.println("Command " + cmd.getCommand() + " did not run!");
    }
    // done with the system
    system.disconnectAllServices();
 </pre>

 @see AS400Message
 @see MessageFile
 **/
public class CommandCall implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String command_ = "";
    private AS400Message[] messageList_ = new AS400Message[0];
    private AS400 system_ = null;

    transient private RemoteCommandImpl impl_;

    transient private Vector actionCompletedListeners_;
    transient private PropertyChangeSupport propertyChangeListeners_;
    transient private VetoableChangeSupport vetoableChangeListeners_;

    /**
     Constructs a CommandCall object.  The system and the command string must be set later.
     **/
    public CommandCall()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing CommandCall object.");
        initializeTransient();
    }

    /**
     Constructs a CommandCall object.  It uses the specified system.  The command string must be set later.
     @param  system  The AS/400 on which to issue the command.
     **/
    public CommandCall(AS400 system)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing CommandCall object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
        initializeTransient();
    }

    /**
     Constructs a CommandCall object.  It uses the specified system and command.
     @param  system  The AS/400 on which to issue the command.
     @param  command  The command to run on the AS/400.  The library list will be used to find the command.
     **/
    public CommandCall(AS400 system, String command)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing CommandCall object, system: " + system + " command: " + command);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (command == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'command' is null.");
            throw new NullPointerException("command");
        }

        system_  = system;
        command_ = command;

        initializeTransient();
    }

    /**
     Adds an ActionCompletedListener.  The specified ActionCompletedListeners <b>actionCompleted</b> method will be called each time a command has run.  The ActionCompletedListener object is added to a list of ActionCompletedListeners managed by this CommandCall; it can be removed with removeActionCompletedListener.
     @param  listener  The ActionCompletedListener.
     @see  #removeActionCompletedListener
     **/
    public void addActionCompletedListener(ActionCompletedListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding action completed listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        actionCompletedListeners_.addElement(listener);
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListeners <b>propertyChange</b> method will be called each time the value of any bound property is changed.  The PropertyListener object is added to a list of PropertyChangeListeners managed by this CommandCall; it can be removed with removePropertyChangeListener.
     @param  listener  The PropertyChangeListener.
     @see  #removePropertyChangeListener
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        propertyChangeListeners_.addPropertyChangeListener(listener);
    }

    /**
     Adds a VetoableChangeListener.  The specified VetoableChangeListeners <b>vetoableChange</b> method will be called each time the value of any constrained property is changed.
     @param  listener  The VetoableChangeListener.
     @see  #removeVetoableChangeListener
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        vetoableChangeListeners_.addVetoableChangeListener(listener);
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
     Returns the command to run.  It may return an empty string ("") if the command has not been previously set by the constructor, setCommand, or run.
     @return  The command to run.
     **/
    public String getCommand()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting command: " + command_);
        return command_;
    }

    /**
     Returns the list of AS/400 messages returned from running the command.  It will return an empty list if the command has not been run yet.
     @return  The array of messages returned by the AS/400 for the command.
     **/
    public AS400Message[] getMessageList()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting message list.");
        return messageList_;
    }

    /**
     Returns an AS/400 message returned from running the command.
     @param  index  The index into the list of messages returned by the AS/400 for the command.  It must be greater than or equal to zero and less than the number of messages in the list.
     @return  The message at the requested index returned by the AS/400 for the command.
     **/
    public AS400Message getMessageList(int index)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting message from message list:", index);
        return messageList_[index];
    }

    /**
     Returns the AS/400 on which the command is to be run.
     @return  The AS/400 on which the command is to be run.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting system.");
        return system_;
    }

    // Initializes all transient data.
    private void initializeTransient()
    {
        // impl_ remains null
        actionCompletedListeners_ = new Vector();
        propertyChangeListeners_ = new PropertyChangeSupport(this);
        vetoableChangeListeners_ = new VetoableChangeSupport(this);
    }

    // Deserializes and initializes transient data.
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "De-serializing CommandCall object.");
        in.defaultReadObject();
        initializeTransient();
    }

    /**
     Removes this ActionCompletedListener.  If the ActionCompletedListener is not on the list, nothing is done.
     @param  listener  The ActionCompletedListener.
     @see  #addActionCompletedListener
     **/
    public void removeActionCompletedListener(ActionCompletedListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Removing action completed listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        actionCompletedListeners_.removeElement(listener);
    }

    /**
     Removes this PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
     @param  listener  The PropertyChangeListener.
     @see  #addPropertyChangeListener
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        propertyChangeListeners_.removePropertyChangeListener(listener);
    }

    /**
     Removes this VetoableChangeListener.  If the VetoableChangeListener is not on the list, nothing is done.
     @param  listener  The VetoableChangeListener.
     @see  #addVetoableChangeListener
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        vetoableChangeListeners_.removeVetoableChangeListener(listener);
    }

    /**
     Runs the command on the AS/400.  The command must be set prior to this call.
     @return  true if command is successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public boolean run() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Running command: " + command_);
        if (command_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Attempt to run before setting command.");
            throw new ExtendedIllegalStateException ("command", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Synchronize to protect impl_ object.
        synchronized (this)
        {
            if (impl_ == null)
            {
                if (system_ == null)
                {
                    Trace.log(Trace.ERROR, "Attempt to run before setting system.");
                    throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
                }
                impl_ = (RemoteCommandImpl)system_.loadImpl2("com.ibm.as400.access.RemoteCommandImplRemote", "com.ibm.as400.access.RemoteCommandImplProxy");
                system_.connectService(AS400.COMMAND);
                impl_.setSystem(system_.getImpl());
            }
        }

        boolean success = impl_.runCommand(command_);
        messageList_ = impl_.getMessageList();

        fireActionCompleted();
        return success;
    }

    /**
     Sets the command string and runs it on the AS/400.
     @param  command  The command to run.
     @return  true if command is successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  PropertyVetoException  If the change is vetoed.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public boolean run(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, PropertyVetoException
    {
        setCommand(command);
        return run();
    }

    /**
     Sets the command to run.
     @param  command  The command to run on the AS/400.  The library list will be used to find the command.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setCommand(String command) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting command: " + command);
        if (command == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'command' is null.");
            throw new NullPointerException("command");
        }

        String old = command_;
        vetoableChangeListeners_.fireVetoableChange("command", old, command);
        command_ = command;
        propertyChangeListeners_.firePropertyChange("command", old, command);
    }

    /**
     Sets the AS/400 to run the command.  The system cannot be changed once a connection is made to the server.
     @param  system  The AS/400 to run the command.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }

        if (impl_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        AS400 old = system_;
        vetoableChangeListeners_.fireVetoableChange("system", old, system);
        system_ = system;
        propertyChangeListeners_.firePropertyChange("system", old, system);
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
