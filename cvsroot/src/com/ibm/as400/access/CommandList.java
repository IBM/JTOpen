///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: CommandList.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 *  The CommandList class represents a list of CL command (*CMD) objects on the system. This class allows the user to retrieve
 *  a list of {@link com.ibm.as400.access.Command Command} objects which can then be
 *  used to retrieve information about each individual CL command in the list.
 *
 *  <P>The following example demonstrates the use of CommandList:
 *  <br>
 *  <pre>
 *
 *    AS400 system = new AS400("mySystem");
 *
 *    // Generate a list of commands that start with "CRT".
 *    CommandList list = new CommandList(system, "QSYS", "CRT*");
 *    try
 *    {
 *        Command[] cmdList = list.generateList();
 *    }
 *    catch (Exception e)
 *    {
 *        e.printStackTrace();
 *    }
 *  </pre>
 * @see com.ibm.as400.access.Command
 **/
public class CommandList implements Serializable
{
    static final long serialVersionUID = 6L;
    private static final String USERSPACE_PATH = "/QSYS.LIB/QTEMP.LIB/CMDLST.USRSPC";

    /**
     *  Constant used to retrieve all commands in a given library.
    **/
    public static final String ALL = "*ALL";

    private AS400 sys_;
    private String lib_;
    private String cmd_;

    // Program Call parameters. These are always the same, so we hardcode them.
    // Parm0: "CMDLST    QTEMP     "
    private static final ProgramParameter parm0_ = new ProgramParameter(new byte[] { (byte)0xC3, (byte)0xD4, (byte)0xC4, (byte)0xD3, (byte)0xE2, (byte)0xE3, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0xD8, (byte)0xE3, (byte)0xC5, (byte)0xD4, (byte)0xD7, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40});
    // Parm1: "OBJL0200"
    private static final ProgramParameter parm1_ = new ProgramParameter(new byte[] { (byte)0xD6, (byte)0xC2, (byte)0xD1, (byte)0xD3, (byte)0xF0, (byte)0xF2, (byte)0xF0, (byte)0xF0});
    // Parm3: "*CMD      "
    private static final ProgramParameter parm3_ = new ProgramParameter(new byte[] { (byte)0x5C, (byte)0xC3, (byte)0xD4, (byte)0xC4, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40});
    // Error code.
    private static final ProgramParameter parm4_ = new ProgramParameter(new byte[4]);

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = new PropertyChangeSupport(this);


    /**
     *  Constructs a CommandList object.
     **/
    public CommandList()
    {
        initializeTransient();
    }


    /**
     *  Constructs a CommandList object.
     *
     *  @param system The system on which the commands resides.
     *  @param library The library in which the commands resides, e.g. "QSYS".
     *  @param command The name of a command or list of commands, e.g. "CRTUSRPRF" or "CRT*".  Wildcards or the {@link #ALL CommandList.ALL} constant can be used.
     **/
    public CommandList(AS400 system, String library, String command)
    {
        if (system == null)
            throw new NullPointerException("system");

        if (library == null)
            throw new NullPointerException("library");

        if (command == null)
            throw new NullPointerException("command");

        sys_ = system;
        lib_ = library;
        cmd_ = command;

        initializeTransient();
    }


    /**
    *  Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> method will be called each time the value of any bound property is changed.
    *  The PropertyChangeListener object is added to a list of PropertyChangeListeners managed by this CommandList.  It can be removed with removePropertyChangeListener.
    *
    *  @param  listener  The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException("listener");
        }
        propertyChangeListeners_.addPropertyChangeListener(listener);
    }


    /**
     *  Returns the command name used to generate a list for.
     *
     *  @return  The command, or null if no command has been set.
     * @see #setCommand
     **/
    public String getCommand()
    {
        return cmd_;
    }


    /**
     *  Returns the library where the command(s) reside.
     *
     *  @return The library, or null if no library has been set.
     * @see #setLibrary
     **/
    public String getLibrary()
    {
        return lib_;
    }


    /**
     *  Returns the system from which to retrieve the command list.
     *
     *  @return  The system from which to retrieve the commands, or null if no system has been set.
     * @see #setSystem
     **/
    public AS400 getSystem()
    {
        return sys_;
    }


    /**
     *  Generate a list of commands. The system, library filter, and command filter must
     * all be set before calling this method.
     * @return The array of Command objects generated by this CommandList. If the command list
     * is empty, an array of size 0 is returned.
     * @see #setCommand
     * @see #setLibrary
     * @see #setSystem
     **/
    public synchronized Command[] generateList() throws AS400Exception, AS400SecurityException,
    ErrorCompletingRequestException, IOException,
    InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Generating list of commands using library filter '"+lib_+"' and command filter '"+cmd_+"'.");

        if (sys_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (lib_ == null)
            throw new ExtendedIllegalStateException("library", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (cmd_ == null)
            throw new ExtendedIllegalStateException("command", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        CharConverter conv37_ = new CharConverter(37);
        StringBuffer commandString = new StringBuffer(cmd_.toUpperCase().trim());
        while (commandString.length() < 10) commandString.append(' ');
        StringBuffer libraryString = new StringBuffer(lib_.toUpperCase().trim());
        while (libraryString.length() < 10) libraryString.append(' ');

        // Create the program parameters for the list object program call.
        ProgramParameter[] parms = new ProgramParameter[]
        {
          parm0_, parm1_,
          new ProgramParameter(conv37_.stringToByteArray(commandString.toString()+libraryString.toString())),
          parm3_, parm4_
        };

        CharConverter textConv = new CharConverter(sys_.getCcsid());
        ProgramCall pgm = new ProgramCall(sys_, "/QSYS.LIB/QUSLOBJ.PGM", parms);
        pgm.suggestThreadsafe();  // the called API is thread-safe
        // Note: The program _must_ be allowed to run in the same job as is used for the UserSpace operations, otherwise a different QTEMP library will be used.

        // Determine the needed scope of synchronization.
        Object lockObject;
        boolean willRunProgramsOnThread = pgm.isStayOnThread();
        if (willRunProgramsOnThread) {
          // The calls will run in the job of the JVM, so lock for entire JVM.
          lockObject = USERSPACE_PATH;
        }
        else {
          // The calls will run in the job of the Remote Command Host Server, so lock on the connection.
          lockObject = sys_;
        }

        byte[] bytes = new byte[140];
        int offsetOfList = 0;
        int numberOfListEntries = 0;
        int sizeOfListEntry = 0;

        synchronized(lockObject) // Synchronize so we don't step on our own user space.
        {
            // The userspace that is used to store the help data before the transformation.
            UserSpace us = new UserSpace(sys_, USERSPACE_PATH);
            us.setMustUseProgramCall(true);
            if (!willRunProgramsOnThread)
            {
              us.setMustUseSockets(true);
              // Force the use of sockets when running natively but not on-thread.
              // We have to do it this way since UserSpace will otherwise make a native ProgramCall, and will use a different QTEMP library than that used by the host server.
            }
            try
            {
              us.create(140, true, " ", (byte)0, "", "*ALL");

              // call the program
              if (!pgm.run())
              {
                AS400Message[] messages = pgm.getMessageList();
                throw new AS400Exception(messages);
              }

              // Read from the userspace.
              us.read(bytes, 0, 0, 140);

              offsetOfList = BinaryConverter.byteArrayToInt(bytes, 124);
              numberOfListEntries = BinaryConverter.byteArrayToInt(bytes, 132);
              sizeOfListEntry = BinaryConverter.byteArrayToInt(bytes, 136);
              int neededSize = offsetOfList+(numberOfListEntries*sizeOfListEntry);

              // Read from the userspace.
              bytes = new byte[neededSize];
              us.read(bytes, 0, 0, neededSize);
            }
            finally
            {
              // Delete the temporary user space, to allow other threads to re-create and use it.
              try { us.delete(); }
              catch (Exception e) {
                Trace.log(Trace.ERROR, "Exception while deleting temporary user space", e);
              }
            }
        }

        // Initialize the string data we plan to retrieve.
        Command[]  cmdList = new Command[numberOfListEntries];
        int offset = offsetOfList;

        // Fill in the string arrays for each command mathing the search criteria.
        for (int i=0; i<numberOfListEntries; ++i)
        {
          String command = textConv.byteArrayToString(bytes, offset, 10).trim();
          String library = textConv.byteArrayToString(bytes, offset+10, 10).trim();
          String desc = textConv.byteArrayToString(bytes, offset+41, 50).trim();
          String path = QSYSObjectPathName.toPath(library, command, "CMD");
          cmdList[i] = new Command(sys_, path, desc);

          offset += sizeOfListEntry;
        }
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Successfully generated command list with "+numberOfListEntries+" entries.");

        return cmdList;
    }


    // Called on construct or after de-serialization
    private void initializeTransient()
    {
        propertyChangeListeners_ = new PropertyChangeSupport(this);
    }


    // Called when this object is de-serialized
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        in.defaultReadObject();
        initializeTransient();
    }


    /**
     *  Removes the PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
     *
     *  @param  listener  The PropertyChangeListener.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException("listener");
        }
        propertyChangeListeners_.removePropertyChangeListener(listener);
    }


    /**
     *  Sets the command name filter used to generate the list.  Wildcards like "CRT*" can be used.
     *
     *  @param  command The command(s) for which to generate a list.
     * @see #getCommand
     **/
    public void setCommand(String command)
    {
        if (command == null) throw new NullPointerException("command");

        synchronized(this)
        {
            String old = cmd_;

            cmd_ = command;

            propertyChangeListeners_.firePropertyChange("command", old, command);
        }
    }


    /**
     *  Sets the library where the command(s) reside.
     *
     *  @param  library The library where the command(s) reside.
     * @see #getLibrary
     **/
    public void setLibrary(String library)
    {
        if (library == null) throw new NullPointerException("library");

        synchronized(this)
        {
            String old = lib_;

            lib_ = library;

            propertyChangeListeners_.firePropertyChange("library", old, library);
        }
    }


    /**
     *  Sets the system from which to retrieve the command list.
     *
     *  @param  system  The system from which to retrieve the commands.
     * @see #getSystem
     **/
    public void setSystem(AS400 system)
    {
        if (system == null) throw new NullPointerException("system");

        synchronized(this)
        {
            AS400 old = sys_;

            sys_ = system;

            propertyChangeListeners_.firePropertyChange("system", old, system);
        }
    }

    /**
     * Returns a String representation of this CommandList.
     * @return The string, which includes the library and command name filter used.
    **/
    public String toString()
    {
        return super.toString()+"["+lib_+";"+cmd_+"]";
    }
}
