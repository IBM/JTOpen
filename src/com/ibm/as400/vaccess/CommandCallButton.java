///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CommandCallButton.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.ActionCompletedListener;
import com.ibm.as400.access.ActionCompletedEvent;
import com.ibm.as400.access.CommandCall;
import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;



/**
The CommandCallButton class represents a button
that calls a server CL command when pressed.
Results of the command are returned in a message list.

<p>CommandCallButton objects generate the following events:
<ul>
  <li>ActionCompletedEvent
  <li>ErrorEvent
  <li>PropertyChangeEvent
</ul>

@see com.ibm.as400.access.CommandCall
@see com.ibm.as400.access.AS400Message
**/
public class CommandCallButton
extends JButton
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    CommandCall       command_    = null; // Private.



    // Event support.
    transient         ActionCompletedEventSupport   actionCompletedEventSupport_; // Private.
    transient         ErrorEventSupport             errorEventSupport_; // Private.
    transient private PropertyChangeSupport         propertyChangeSupport_;
    transient private VetoableChangeSupport         vetoableChangeSupport_;




    /**
    Constructs a CommandCallButton object.
    **/
    public CommandCallButton()
    {
        command_ = new CommandCall ();
        initializeTransient ();
    }


    /**
    Constructs a CommandCallButton object.

    @param  text  The button text, or null if there is no text.
    **/
    public CommandCallButton(String text)
    {
        super(text);
        command_ = new CommandCall ();
        initializeTransient ();
    }


    /**
    Constructs a CommandCallButton object.

    @param  text  The button text, or null if there is no text.
    @param  icon  The button icon, or null if there is no icon.
    **/
    public CommandCallButton(String text,  Icon icon)
    {
        super(text,icon);
        command_ = new CommandCall ();
        initializeTransient ();
    }


    /**
    Constructs a CommandCallButton object.

    @param  text    The button text, or null if there is no text.
    @param  icon    The button icon, or null if there is no icon.
    @param  system  The server on which commands are run.
    **/
    public CommandCallButton(String text,
                             Icon icon,
                             AS400 system)
    {
        super(text, icon);
        command_ = new CommandCall (system);
        initializeTransient ();
    }


    /**
    Constructs a CommandCallButton object.

    @param  text    The button text, or null if there is no text.
    @param  icon    The button icon, or null if there is no icon.
    @param  system  The server on which commands are run.
    @param  command The command.
    **/
    public CommandCallButton(String text,
                             Icon icon,
                             AS400 system,
                             String command)
    {
        super(text, icon);
        command_ = new CommandCall (system, command);
        initializeTransient ();
    }


    /**
    Adds a listener to be notified when a command has been run on the server.

    @param  listener  The listener.
    **/
    public void addActionCompletedListener(ActionCompletedListener listener)
    {
        actionCompletedEventSupport_.addActionCompletedListener (listener);
    }


/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        super.addPropertyChangeListener (listener);         // @A1A
        if (propertyChangeSupport_ != null)                 // @A1A
            propertyChangeSupport_.addPropertyChangeListener (listener);
    }



/**
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }


    /**
    Returns the command that is run when the button is pressed.

    @return  The command that is run when the button is pressed.
    **/
    public String getCommand()
    {
        return command_.getCommand();
    }


    /**
    Returns the message list resulting from the last command
    that was run.

    @return  The message list.
    **/
    public AS400Message[] getMessageList()
    {
        return command_.getMessageList();
    }


    /**
    Returns the text from the first message resulting from the
    last command that was run.

    @return  The message text.
    **/
    public String getMessageText()
    {
        AS400Message[] msglist = command_.getMessageList();
        if (msglist.length==0)
            return "";
        else
            return msglist[0].getText();
    }


    /**
    Returns the system on which commands are run.

    @return  The system on which commands are run.
    **/
    public AS400 getSystem()
    {
        return command_.getSystem();
    }


/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        actionCompletedEventSupport_    = new ActionCompletedEventSupport (this);
        errorEventSupport_              = new ErrorEventSupport (this);
        propertyChangeSupport_          = new PropertyChangeSupport (this);
        vetoableChangeSupport_          = new VetoableChangeSupport (this);

        command_.addActionCompletedListener (actionCompletedEventSupport_);
        command_.addPropertyChangeListener (propertyChangeSupport_);
        command_.addVetoableChangeListener (vetoableChangeSupport_);

        addActionListener (new ActionListener_ ());
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
    }



/**
Removes an action completed listener.

@param  listener  The listener.
**/
    public void removeActionCompletedListener (ActionCompletedListener listener)
    {
        actionCompletedEventSupport_.removeActionCompletedListener (listener);
    }



/**
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a property change listener.

@param  listener  The listener.
**/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }



/**
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



    /**
    Sets the command that is run when the button is pressed.

    @param  command The command that is run when the button is pressed.

    @exception PropertyVetoException If the change is vetoed.
    **/
    public void setCommand(String command)
        throws PropertyVetoException
    {
        command_.setCommand( command );
    }


    /**
    Sets the system on which commands are run.

    @param  system The system on which commands are run.

    @exception PropertyVetoException If the change is vetoed.
    **/
    public void setSystem(AS400 system)
        throws PropertyVetoException
    {
        command_.setSystem( system );
    }



    private class ActionListener_
    implements ActionListener
    {
        public void actionPerformed (ActionEvent event)
        {
            // Set the cursor to a wait cursor.
            WorkingCursorAdapter cursorAdapter = new WorkingCursorAdapter (CommandCallButton.this);
            cursorAdapter.startWorking (new WorkingEvent (this));

            // Run the command.
            try {
                command_.run();
            }
            catch (Exception e) {
                errorEventSupport_.fireError (e);
            }

            // Set the cursor back.
            cursorAdapter.stopWorking (new WorkingEvent (this));
        }

    }


}
