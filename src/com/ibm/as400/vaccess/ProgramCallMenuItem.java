///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramCallMenuItem.java
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
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;



/**
The ProgramCallMenuItem class represents a menu item
that calls a server program when pressed.
The results of the program are returned in a message list.

<p>ProgramCallMenuItem objects generate the following events:
<ul>
  <li>ActionCompletedEvent
  <li>ErrorEvent
  <li>PropertyChangeEvent
</ul>

@see com.ibm.as400.access.ProgramCall
@see com.ibm.as400.access.AS400Message
**/
public class ProgramCallMenuItem
extends JMenuItem
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    ProgramCall program_    = null; // Private.



    // Event support.
    transient         ActionCompletedEventSupport   actionCompletedEventSupport_; // Private.
    transient         ErrorEventSupport             errorEventSupport_; // Private.
    transient private PropertyChangeSupport         propertyChangeSupport_;
    transient private VetoableChangeSupport         vetoableChangeSupport_;




    /**
    Constructs a ProgramCallMenuItem object.
    **/
    public ProgramCallMenuItem()
    {
        program_ = new ProgramCall ();
        initializeTransient ();
    }


    /**
    Constructs a ProgramCallMenuItem object.

    @param  text  The menu item text, or null if there is no text.
    **/
    public ProgramCallMenuItem(String text)
    {
        super(text);
        program_ = new ProgramCall ();
        initializeTransient ();
    }


    /**
    Constructs a ProgramCallMenuItem object.

    @param  text  The menu item text, or null if there is no text.
    @param  icon  The menu item icon, or null if there is no icon.
    **/
    public ProgramCallMenuItem(String text,  Icon icon)
    {
        super(text,icon);
        program_ = new ProgramCall ();
        initializeTransient ();
    }


    /**
    Constructs a ProgramCallMenuItem object.

    @param  text        The menu item text, or null if there is no text.
    @param  icon        The menu item icon, or null if there is no icon.
    @param  system      The server on which the programs are run.
    **/
    public ProgramCallMenuItem(String text,
                             Icon icon,
                             AS400 system)
    {
        super(text, icon);
        program_ = new ProgramCall (system);
        initializeTransient ();
    }


    /**
    Constructs a ProgramCallMenuItem object.

    @param  text        The menu item text, or null if there is no text.
    @param  icon        The menu item icon, or null if there is no icon.
    @param  system      The server on which the programs are run.
    @param  program     The program name as a fully qualified path name
                        in the library file system.
                        The library and program name must each be
                        10 characters or less.
    @param  parmlist    A list of up to 35 parameters with which to run the program.
    **/
    public ProgramCallMenuItem(String text,
                             Icon icon,
                             AS400 system,
                             String program,
                             ProgramParameter[] parmlist)
    {
        super(text, icon);
        program_ = new ProgramCall (system, program, parmlist);
        initializeTransient ();
    }


    /**
    Adds a listener to be notified when an action is completed.

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
    Adds a parameter to the parameter list.

    @param  parameter The parameter.

    @exception PropertyVetoException If the change is vetoed.
    **/
    public void addParameter (ProgramParameter parameter)
        throws PropertyVetoException
    {
        program_.addParameter (parameter);
    }


/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        super.addPropertyChangeListener(listener);                      // @C1A
        if (propertyChangeSupport_ != null)                             // @C1A
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
    Returns the message list resulting from the last program call that was run.

    @return  The message list.
    **/
    public AS400Message[] getMessageList()
    {
        return program_.getMessageList();
    }


    /**
     Returns the message text from the last program that was run.  The message will be the first message received.


    @return  The message text.
    **/
    public String getMessageText()
    {
        AS400Message[] msglist = program_.getMessageList();
        if (msglist.length==0)
            return "";
        else
            return msglist[0].getText();
    }



    /**
    Returns the parameter list.

    @return  The parameter list.
    **/
    public ProgramParameter[] getParameterList()
    {
        return program_.getParameterList();
    }


    /**
    Returns the program which will be called when the button is pressed.

    @return  The program which will be called when the button is pressed.
    **/
    public String getProgram()
    {
        return program_.getProgram();
    }


    /**
    
   Returns the system on which programs are run.

    @return  The system on which programs are run.
    **/
    public AS400 getSystem()
    {
        return program_.getSystem();
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

        program_.addActionCompletedListener (actionCompletedEventSupport_);
        program_.addPropertyChangeListener (propertyChangeSupport_);
        program_.addVetoableChangeListener (vetoableChangeSupport_);

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
Removes a action completed listener.

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
   
Sets the list of parameters to pass to the server program.

@param parmlist  A list of up to 35 parameters with which to run the program.
                 It will replace any parameters previously set.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setParameterList(ProgramParameter[] parmlist)
        throws PropertyVetoException
    {
        program_.setParameterList (parmlist);
    }


    /**
    Sets the program.

    @param  program The program.

    @exception PropertyVetoException If the change is vetoed.
    **/
    public void setProgram (String program)
        throws PropertyVetoException
    {
        program_.setProgram (program);
    }


    /**
     Sets the system on which programs are run.

    @param  system The system on which programs are run.

    @exception PropertyVetoException If the change is vetoed.
    **/
    public void setSystem(AS400 system)
        throws PropertyVetoException
    {
        program_.setSystem( system );
    }



    private class ActionListener_
    implements ActionListener
    {
        public void actionPerformed (ActionEvent event)
        {
            // Set the cursor to a wait cursor.
            WorkingCursorAdapter cursorAdapter = new WorkingCursorAdapter (ProgramCallMenuItem.this);
            cursorAdapter.startWorking (new WorkingEvent (this));

            // Run the command.
            try {
                program_.run();
            }
            catch (Exception e) {
                errorEventSupport_.fireError (e);
            }

            // Set the cursor back.
            cursorAdapter.stopWorking (new WorkingEvent (this));
        }

     }


}
