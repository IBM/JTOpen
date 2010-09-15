///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400File.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport; //@B0A
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 *Represents a physical or logical file on the system.
 *This class allows the user to do the following:
 *<ul>
 *<li>Create a physical file on the system by:
 *<ul>
 *<li>Specifying a record length.
 *<li>Specifying an existing DDS source file.
 *<li>Specifying a RecordFormat object that contains a description of the
 *record format for the file.
 *</ul>
 *<li>Access the records in a system file sequentially, by record number, or by
 *key.
 * Note: To read a keyed physical or logical file sequentially and have the records
 * returned in key order, use the <tt>read...()</tt> methods of {@link KeyedFile KeyedFile}.
 *<li>Write records to a system file sequentially or by key.
 *<li>Update records in a system file sequentially, by record number or by key.
 *<li>Lock a system file for different types of access.
 *<li>Use commitment control when accessing a system file.  The user can:
 *<ul>
 *<li>Start commitment control for the connection.
 *<li>Specify different commitment control lock levels for the individual
 *    files being accessed.
 *<li>Commit and rollback transactions for the connection.
 *</ul>
 *<li>Delete a physical or logical file or member on the system.
 *</ul>
 *AS400File objects generate the following events:
 *<ul>
 *<li><a href="FileEvent.html">FileEvent</a>
 *<br>The events fired are:
 *<ul>
 *<li>FILE_CLOSED
 *<li>FILE_CREATED
 *<li>FILE_DELETED
 *<li>FILE_MODIFIED
 *<li>FILE_OPENED
 *</ul>
 *<li>PropertyChangeEvent
 *<li>VetoableChangeEvent
 *</ul>
 *@see AS400FileRecordDescription
 *@see MemberList
 **/
abstract public class AS400File implements Serializable
{
    private static final String CLASSNAME = "com.ibm.as400.access.AS400File";
    static
    {
        if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
    }

    static final long serialVersionUID = 4L;

    //////////////////////////////////////////////////////////////////////////
    // CONSTANTS
    //////////////////////////////////////////////////////////////////////////
    //@C2A
    /**
     * Constant indicating a text description of *BLANK.
     *@see AS400File#create
     **/
    static public final String BLANK = AS400FileConstants.BLANK;

    /**
     *Constant indicating a commit lock level of *ALL.
     *Every record accessed in the file is locked until the
     *transaction is committed or rolled back.
     *@see AS400File#startCommitmentControl
     **/
    static public final int COMMIT_LOCK_LEVEL_ALL = AS400FileConstants.COMMIT_LOCK_LEVEL_ALL; //@B1C

    /**
     *Constant indicating a commit lock level of *CHANGE.
     *Every record read for update is locked. If a record
     *is updated, added, or deleted, that record remains locked
     *until the transaction is committed or rolled back.  Records
     *that are accessed for update but are released without being
     *updated are unlocked.
     *@see AS400File#startCommitmentControl
     **/
    static public final int COMMIT_LOCK_LEVEL_CHANGE = AS400FileConstants.COMMIT_LOCK_LEVEL_CHANGE; //@B1C
    /**
     *Constant indicating a commit lock level of *CS.
     *Every record accessed is locked.  Records that are not
     *updated or deleted are locked only until a different record
     *is accessed.  Records that are updated, added, or deleted are locked
     *until the transaction is committed or rolled back.
     *@see AS400File#startCommitmentControl
     **/
    static public final int COMMIT_LOCK_LEVEL_CURSOR_STABILITY = AS400FileConstants.COMMIT_LOCK_LEVEL_CURSOR_STABILITY; //@B1C
    /**
     *Constant indicating that the commit lock level specified on the
     *startCommitmentControl() method should be used.
     *The record locking specified by the commitLockLevel parameter on the
     *startCommitmentControl() method will apply to transactions using this file.
     **/
    static public final int COMMIT_LOCK_LEVEL_DEFAULT = AS400FileConstants.COMMIT_LOCK_LEVEL_DEFAULT; //@B1C
    /**
     *Constant indicating that no commitment control should be used for the file.
     *No commitment control will apply to this file.
     *@see AS400File#startCommitmentControl
     **/
    static public final int COMMIT_LOCK_LEVEL_NONE = AS400FileConstants.COMMIT_LOCK_LEVEL_NONE; //@B1C

    /**
     *Constant indicating lock type of read willing to share with
     *other readers.  This is the equivalent of specifying *SHRNUP
     *on the Allocate Object (ALCOBJ) command.
     *@see AS400File#lock
     **/
    static public final int READ_ALLOW_SHARED_READ_LOCK = AS400FileConstants.READ_ALLOW_SHARED_READ_LOCK; //@B1C
    /**
     *Constant indicating lock type of read willing to share with
     *updaters.  This is the equivalent of specifying *SHRRD
     *on the Allocate Object (ALCOBJ) command.
     *@see AS400File#lock
     **/
    static public final int READ_ALLOW_SHARED_WRITE_LOCK = AS400FileConstants.READ_ALLOW_SHARED_WRITE_LOCK; //@B1C
    /**
     *Constant indicating lock type of read willing to share with no one.
     *This is the equivalent of specifying *EXCL on the Allocate Object (ALCOBJ)
     *command.
     *@see AS400File#lock
     **/
    static public final int READ_EXCLUSIVE_LOCK = AS400FileConstants.READ_EXCLUSIVE_LOCK; //@B1C
    /**
     *Constant indicating open type of read only.
     *@see AS400File#open
     **/
    static public final int READ_ONLY = AS400FileConstants.READ_ONLY; //@B1C
    /**
     *Constant indicating open type of read/write.
     *@see AS400File#open
     **/
    static public final int READ_WRITE = AS400FileConstants.READ_WRITE; //@B1C

    //@C2A
    /**
     * Constant indicating a text description of *SRCMBRTXT.
     *@see AS400File#create
     **/
    static public final String SOURCE_MEMBER_TEXT = AS400FileConstants.SOURCE_MEMBER_TEXT;

    //@C2A
    /**
     * Constant indicating a file type of *DATA.
     *@see AS400File#create
     **/
    static public final String TYPE_DATA = AS400FileConstants.TYPE_DATA;

    //@C2A
    /**
     * Constant indicating a file type of *SRC.
     *@see AS400File#create
     **/
    static public final String TYPE_SOURCE = AS400FileConstants.TYPE_SOURCE;


    /**
     *Constant indicating lock type of update willing to share with
     *readers.  This is the equivalent of specifying *EXCLRD
     *on the Allocate Object (ALCOBJ) command.
     *@see AS400File#lock
     **/
    static public final int WRITE_ALLOW_SHARED_READ_LOCK = AS400FileConstants.WRITE_ALLOW_SHARED_READ_LOCK; //@B1C
    /**
     *Constant indicating lock type of update willing to share with
     *updaters.  This is the equivalent of specifying *SHRUPD
     *on the Allocate Object (ALCOBJ) command.
     *@see AS400File#lock
     **/
    static public final int WRITE_ALLOW_SHARED_WRITE_LOCK = AS400FileConstants.WRITE_ALLOW_SHARED_WRITE_LOCK; //@B1C
    /**
     *Constant indicating lock type of update willing to share with
     *no one.  This is the equivalent of specifying *EXCL
     *on the Allocate Object (ALCOBJ) command.
     *@see AS400File#lock
     **/
    static public final int WRITE_EXCLUSIVE_LOCK = AS400FileConstants.WRITE_EXCLUSIVE_LOCK; //@B1C
    /**
     *Constant indicating open type of write only.
     *@see AS400File#open
     **/
    static public final int WRITE_ONLY = AS400FileConstants.WRITE_ONLY; //@B1C

    //////////////////////////////////////////////////////////////////////////
    // VARIABLES
    //////////////////////////////////////////////////////////////////////////

    // EVENT SUPPORT
    // Use default property change support
    transient PropertyChangeSupport changes_;
    // Use default vetoable change support
    transient VetoableChangeSupport vetos_ ; //@B0A
    // The list of FileEvent listeners
    transient Vector fileListeners_;

    // Is the file open
    transient boolean isOpen_;
    // The readNoUpdate flag is treated like a bean property,
    // even though it's not.
    boolean readNoUpdate_ = false; //@B5A

    boolean ssp_ = false; // Treat the file as an SSP file or a regular DDM file.

    // The library name of the file
    String library_ = "";

    // BEAN PROPERTIES
    // The file name
    String file_ = "";
    // The member name of the file
    String member_ = "";
    // The IFS pathname of the file.
    String name_ = "";
    // Record format for the file.
    RecordFormat recordFormat_;
    // The system connection information
    AS400 system_;

    // The implementation object.
    transient AS400FileImpl impl_; //@B0C


    /**
     *Constructs an AS400File object.
     **/
    public AS400File()
    {
        initializeTransient();
    }

    /**
     *Constructs an AS400File object. It uses the system and file name specified.
     *If the <i>name</i> for the file does not include a member, the
     *first member of the file will be used.
     *@param system The system to which to connect. The <i>system</i> cannot
     *be null.
     *@param name The integrated file system pathname of the file. The <i>name</i>
     *cannot be null.
     **/
    public AS400File(AS400 system, String name)
    {
        // Verify parameters
        if (system == null)
        {
            throw new NullPointerException("system");
        }
        if (name == null)
        {
            throw new NullPointerException("name");
        }

        initializeTransient();

        // Set the instance data
        setName(name); //@B0A
        system_ = system;
    }

    /**
     *Adds a listener to be notified when a FileEvent is fired.
     *@see #removeFileListener
     *@param listener The FileListener.
     **/
    public void addFileListener(FileListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException("listener");
        }
        fileListeners_.addElement(listener);
    }


    /**
     *Adds a physical file member to the file represented by this object.
     *@param name The name of the member to create.  The <i>name</i> cannot
     *exceed 10 characters in length.  The <i>name</i> cannot be null.
     *@param textDescription The text description with which to create the file.
     *This value must be 50 characters or less.  If this value is null, the
     *text description will be blank.<br>
     *The name of the file and the system to which to connect must be set
     *prior to invoking this method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped
     * unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the
     * system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void addPhysicalFileMember(String name, String textDescription)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify parameters
        if (name == null)
        {
            throw new NullPointerException("name");
        }
        if (name.length() > 10)
        {
            throw new ExtendedIllegalArgumentException("name (" + name + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (textDescription != null)
        {
            if (textDescription.length() > 50)
            {
                throw new ExtendedIllegalArgumentException("textDescription (" + textDescription + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
            }
        }
        chooseImpl();
        impl_.doIt("addPhysicalFileMember",
                   new Class[] { String.class, String.class},
                   new Object[] { name, textDescription });
    }


    /**
     *Adds a listener to be notified when the value of any bound
     *property is changed.  The <b>propertyChange</b> method will be
     *be called.
     *@see #removePropertyChangeListener
     *@param listener The PropertyChangeListener.
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
     *Adds a listener to be notified when the value of any constrained
     *property is changed.
     *The <b>vetoableChange</b> method will be called.
     *@see #removeVetoableChangeListener
     *@param listener The VetoableChangeListener.
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException("listener");
        }
        vetos_.addVetoableChangeListener(listener); //@B0C
    }


    //@B0A
    /**
     * Internal method to verify file is open.
     * Tastes great, less bytes.
     **/
    void checkOpen()
    {
        if (!isOpen_)
        {
            throw new ExtendedIllegalStateException(ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
        }
    }


    /**
     Creates the proper implementation.
     **/
    synchronized void chooseImpl() throws AS400SecurityException, IOException
    {
        if (impl_ == null)
        {
            // Verify object state
            if (system_ == null)
            {
                throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            if (name_ == null || name_.length() == 0)
            {
                throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            //@B0C
            impl_ = (AS400FileImpl)system_.loadImpl3("com.ibm.as400.access.AS400FileImplNative",
                                                     "com.ibm.as400.access.AS400FileImplRemote",
                                                     "com.ibm.as400.access.AS400FileImplProxy");


            //      system_.connectService(AS400.RECORDACCESS); //@B5A

            system_.signon(false);

            // This line replaces the following 4 lines.
            impl_.doItNoExceptions("setAll",
                                   new Class[] { AS400Impl.class, //@B5C
                                   String.class,
                                   RecordFormat.class,
                                   Boolean.TYPE, //@B5A
                                   Boolean.TYPE,
                                   Boolean.TYPE },
                                   new Object[] { system_.getImpl(), //@B5C
                                   name_,
                                   recordFormat_,
                                   new Boolean(readNoUpdate_), //@B5A
                                   new Boolean(this instanceof KeyedFile),
                                   new Boolean(ssp_) });

            //      impl_.doItNoExceptions("setSystem", new Class[] { AS400.class }, new Object[] { system_ }); //@B0A
            //      impl_.doItNoExceptions("setPath", new Class[] { String.class }, new Object[] { name_ }); //@B0A
            //      impl_.doItNoExceptions("setRecordFormat", new Class[] { RecordFormat.class }, new Object[] { recordFormat_ }); //@B0A

            // The following line is provided so the remote class knows
            // if it is a keyed file or a sequential file.
            //      impl_.doItNoExceptions("setIsKeyed", new Class[] { Boolean.TYPE }, new Object[] { new Boolean(this instanceof KeyedFile) }); //@B0A
        }
    }


    /**
     *Closes the file on the system.  All file locks held by this connection
     *are released.  All uncommitted transactions against the file are
     *rolled back if commitment control has been started.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the
     *system.
     **/
    public synchronized void close()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        if (isOpen_) //@B0C
        {
            //@C1 - In mod3, there is no convenient way for the proxy server
            //      to inform the client that the connection has been dropped
            //      and that we should set our AS400File object to be closed.
            //      So, we leave it up to the user to call close() and we will
            //      just swallow any ConnectionDroppedExceptions we encounter.
            try //@C1A
            {   //@C1A
                // Release any explicit locks
                try
                {
                    releaseExplicitLocks();
                }
                catch(AS400SecurityException e)
                {
                    // This will only occur during connect().  We are already connected so
                    // this exception will never occur.  Therefore we do a try...catch here
                    // to shut the compiler up.
                }

                // Close the file.
                doIt("close");
            } //@C1A
            catch(ConnectionDroppedException x) //@C1A
            { //@C1A
            } //@C1A

            // Reset the open flag
            isOpen_ = false;

            //@B0C
            // Fire the FILE_CLOSED FileEvent
            fireEvent(FileEvent.FILE_CLOSED);
        }
    }

    private void doIt(String x)
      throws AS400Exception, AS400SecurityException, InterruptedException, IOException
    {
        impl_.doIt(x, new Class[0], new Object[0]);
    }

    /**
     *Commits all transactions since the last commit boundary.  Invoking this
     *method will cause all transactions under commitment control for this
     *connection to be committed.  This means that any AS400File object opened
     *under this connection, for which a commit lock level was specified, will
     *have outstanding transactions committed.  If commitment control has not been
     *started for the connection, no action is taken.<br>
     *The system to which to connect must be set prior to invoking this
     *method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setSystem
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped
     *unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the
     *system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void commit()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        if (isCommitmentControlStarted()) // if returns true, then impl has been chosen.
        {
            // Commit.
            doIt("commit");
        }
    }


    //@E2A
    /**
     *Commits all transactions since the last commit boundary for the specified system.
     *Invoking this method will cause all transactions under commitment control for the
     *specified connection to be committed.  This means that any AS400File object opened
     *under this connection, for which a commit lock level was specified, will
     *have outstanding transactions committed.  If commitment control has not been
     *started for the connection, no action is taken.<br>
     *@param system The system for which transactions will be committed.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setSystem
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped
     *unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the
     *system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public static void commit(AS400 system)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        if (system == null) throw new NullPointerException("system");
        system.signon(false);
        //    system.connectService(AS400.RECORDACCESS);
        AS400FileImpl impl = (AS400FileImpl)system.loadImpl3("com.ibm.as400.access.AS400FileImplNative",
                                                             "com.ibm.as400.access.AS400FileImplRemote",
                                                             "com.ibm.as400.access.AS400FileImplProxy");

        impl.doIt("commit", new Class[] { AS400Impl.class }, new Object[] { system.getImpl() });
    }


    /**
     *Creates a physical file with the specified record length and file type.
     *The record format for this object will be set by this method.  The
     *record format for the file is determined as follows:
     *<ul>
     *<li>If <i>fileType</i> is AS400File.TYPE_DATA,
     *<ul>
     *<li>The format name of the file is the name of the file as specified on the
     *constructor
     *<li>The record format contains one field whose name is the name of the file,
     *whose type is CHARACTER, and whose length is <i>recordLength</i>
     *</ul>
     *<li>If <i>fileType</i> is AS400File.TYPE_SOURCE,
     *<ul>
     *<li>The format name of the file is the name of the file as specified on the
     *constructor
     *<li>The record format contains three fields:
     *<ul>
     *<li>SRCSEQ whose type is ZONED(6, 2)
     *<li>SRCDAT whose type is ZONED(6, 0)
     *<li>SRCDTA whose type is CHARACTER and whose length is
     *<i>recordLength</i> - 12
     *</ul>
     *</ul>
     *</ul>
     *<b>Note:</b> The file is created using the default values for the
     * Create Physical File (CRTPF) command.
     * To change the file after it has been created, use the
     * <a href="CommandCall.html">CommandCall</a> class to issue a CHGPF
     * command.<br>
     *The name of the file and the system to which to connect must be set prior
     *to invoking this method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@param recordLength The record length with which to create the file.  Valid values
     *                    are 1 through 32766 inclusive.
     *@param fileType The file type with which to create the file.  Valid values are
     *                AS400File.TYPE_DATA or AS400File.TYPE_SOURCE.
     *                If AS400File.TYPE_DATA is specified, the record
     *                format for the file contains one field.  If AS400File.TYPE_SOURCE is
     *                specified, the record format for the file contains three
     *                fields: source sequence number, date, and source statement.
     *@param textDescription The text description with which to create the file.
     *This value must be 50 characters or less.
     *If this value is null, the empty string, or AS400File.BLANK,
     *the text description is blank.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void create(int recordLength, String fileType, String textDescription)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify parameters
        if (recordLength < 1 || recordLength > 32766)
        {
            throw new ExtendedIllegalArgumentException("recordLength (" + String.valueOf(recordLength) + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (fileType == null)
        {
            throw new NullPointerException("fileType");
        }
        if (!(fileType.equalsIgnoreCase(TYPE_DATA) ||  //@C2C
              fileType.equalsIgnoreCase(TYPE_SOURCE))) //@C2C
        {
            throw new ExtendedIllegalArgumentException("fileType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (textDescription != null)
        {
            if (textDescription.length() > 50)
            {
                throw new ExtendedIllegalArgumentException("textDescription", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
            }
        }

        chooseImpl();
        impl_.doIt("create", new Class[] { Integer.TYPE, String.class, String.class },
        new Object[] { new Integer(recordLength), fileType, textDescription }); //@B0A

        // Create was successful.  Set the record format for the file.
        RecordFormat old = recordFormat_; // Save the old format for when we fire the
        // property change event
        recordFormat_ = new RecordFormat(file_);  // The format name is the name of the file
        if (fileType.equalsIgnoreCase(TYPE_DATA)) //@C2C
        { // There is one field whose ddsName is the name of the file and whose type is
            // CHAR(recordLength)
            recordFormat_.addFieldDescription(new CharacterFieldDescription(new AS400Text(recordLength, system_.getCcsid(), system_), file_)); //@B6C
        }
        else
        { // There are three fields,
            //    SRCSEQ ZONED(6, 2)
            //    SRCDAT ZONED(6, 0)
            //    SRCDTA CHAR(recordLength - 12)
            recordFormat_.addFieldDescription(new ZonedDecimalFieldDescription(new AS400ZonedDecimal(6, 2), "SRCSEQ"));
            recordFormat_.addFieldDescription(new ZonedDecimalFieldDescription(new AS400ZonedDecimal(6, 0), "SRCDAT"));
            recordFormat_.addFieldDescription(new CharacterFieldDescription(new AS400Text(recordLength - 12, system_.getCcsid(), system_), "SRCDTA")); //@B6C
        }

        impl_.doIt("setRecordFormat", new Class[] { RecordFormat.class },
        new Object[] { recordFormat_ }); //@B0A

        //@B0C
        // Fire the FILE_CREATED FileEvent
        fireEvent(FileEvent.FILE_CREATED);

        // Indicate that the record format has been set.
        changes_.firePropertyChange("recordFormat", old, recordFormat_);
    }

    /**
     *Creates a physical file using the specified DDS source file.
     *<b>Note:</b> The file is created using the default values for the
     * Create Physical File (CRTPF) command.
     * To change the file after it has been created, use the
     * <a href="CommandCall.html">CommandCall</a> class to issue a CHGPF
     * command.<br>
     *The name of the file and the system to which to connect must be set prior
     *to invoking this method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@param ddsSourceFile The integrated file system pathname of the file containing the
     *DDS source for the file being created.
     *@param textDescription The text description with which to create the file.
     *This value must be between 1 and 50 characters inclusive.
     *If this value is null, the empty string, or AS400File.BLANK,
     *the text description will be blank.
     *Specify AS400File.SOURCE_MEMBER_TEXT for the text description if the text
     *description from <i>ddsSourceFile</i> is to be used.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void create(String ddsSourceFile, String textDescription)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify parameters
        if (ddsSourceFile == null)
        {
            throw new NullPointerException("ddsSourceFile");
        }
        if (textDescription != null && textDescription.length() > 50)
        {
            throw new ExtendedIllegalArgumentException("textDescription", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        chooseImpl();
        impl_.doIt("create", new Class[] { String.class, String.class },
        new Object[] { ddsSourceFile, textDescription }); //@B0A

        // Fire the FILE_CREATED FileEvent
        fireEvent(FileEvent.FILE_CREATED);
    }

    /**
     *Creates a physical file using the specified record format.  The record format
     *for this object will be set by this method.
     *<b>Note:</b> The file is created using the default values for the
     * Create Physical File (CRTPF) command.
     * To change the file after it has been created, use the
     * <a href="CommandCall.html">CommandCall</a> class to issue a CHGPF
     * command.<br>
     *The name of the file and the system to which to connect must be set prior
     *to invoking this method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@param recordFormat The record format for the file.
     *@param textDescription The text description with which to create the file.
     *This value must be between 1 and 50 characters inclusive.
     *If this value is null, the empty string, or AS400File.BLANK,
     *the text description will be blank.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void create(RecordFormat recordFormat, String textDescription)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        create(recordFormat, textDescription, null, null, null, null, false, null, null);
    }

    /**
     *Creates a physical file using the specified record format and any specified
     *keywords.  The record format for this object will be set by this method.
     *<b>Note:</b> The file is created using the default values for the
     * Create Physical File (CRTPF) command.
     * To change the file after it has been created, use the
     * <a href="CommandCall.html">CommandCall</a> class to issue a CHGPF
     * command.<br>
     *The name of the file and the system to which to connect must be set prior
     *to invoking this method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@param recordFormat The record format of the file.
     *@param textDescription The text description with which to create the file.
     *This value must be between 1 and 50 characters inclusive.
     *If this value is null, the empty string, or AS400File.BLANK,
     *the text description will be blank.
     *@param altSeq The value to be specified for the file-level keyword ALTSEQ.  If no
     *value is to be specified, null may be specified.
     *@param ccsid The value to be specified for the file-level keyword CCSID.  If no
     *value is to be specified, null may be specified.
     *@param order The value to be specified to indicate in which order records will be
     *retrieved from the file.  Valid values are one of the following file-level keywords:
     *<ul>
     *<li>FIFO
     *<li>LIFO
     *<li>FCFO
     *</ul>
     *If no ordering value is to be specified, null may be specified.
     *@param ref The value to be specified for the file-level keyword REF.  If no
     *value is to be specified, null may be specified.
     *@param unique The value that indicates if the file-level keyword UNIQUE is to be specified. true if
     *UNIQUE should be specified; false otherwise.
     *@param format The value to be specified for the record-level keyword FORMAT.  If no
     *value is to be specified, null may be specified.
     *@param text The value to be specified for the record-level keyword TEXT.  If no
     *value is to be specified, null may be specified.  The single quotes required to
     *surround the TEXT keyword value are added by this class.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void create(RecordFormat recordFormat,
                       String textDescription,
                       String altSeq,
                       String ccsid,
                       String order,
                       String ref,
                       boolean unique,
                       String format,
                       String text)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify parameters
        if (recordFormat == null)
        {
            throw new NullPointerException("recordFormat");
        }
        if (textDescription != null && textDescription.length() > 50)
        {
            throw new ExtendedIllegalArgumentException("textDescription (" + textDescription + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        chooseImpl(); //@B0A
        // Create a DDS source file to hold the DDS for the file to be created
        impl_.doIt("createDDSSourceFile",
                   new Class[] {
                   RecordFormat.class, String.class, String.class,
                   String.class, String.class, Boolean.TYPE,
                   String.class, String.class },
                   new Object[] { recordFormat, altSeq, ccsid, order, ref, new Boolean(unique), //@B0C
                   format, text });

        // Create the file based on the newly create DDS source file
        create("/QSYS.LIB/QTEMP.LIB/JT400DSSRC.FILE/JT400DSSRC.MBR", textDescription);

        // Set the record format for this object
        RecordFormat old = recordFormat_; // Set the record format for when we fire
        // the property change event
        recordFormat_ = recordFormat;

        impl_.doIt("setRecordFormat", new Class[] { RecordFormat.class },
        new Object[] { recordFormat_ }); //@B0A

        // Fire the FILE_CREATED FileEvent
        fireEvent(FileEvent.FILE_CREATED);

        changes_.firePropertyChange("recordFormat", old, recordFormat_);
    }


    /**
     *Deletes the file.  The object cannot be open when calling this method.  The file
     *and all its members will be deleted.
     *Use deleteMember() to delete only the member associated with this object.<br>
     *The name of the file and the system to which to connect must be set prior
     *to invoking this method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@see AS400File#deleteMember
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void delete()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify object state
        if (isOpen_)
        {
            throw new ExtendedIllegalStateException(ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
        }

        chooseImpl();
        // Delete this file.
        doIt("delete"); //@B0A

        // Fire the FILE_DELETED FileEvent
        fireEvent(FileEvent.FILE_DELETED);
    }

    /**
     *Deletes the record at the current cursor position.  The file must be open and
     *the cursor must be positioned on an active record.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void deleteCurrentRecord()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify object state
        checkOpen();

        //@B0 - Don't need to do chooseImpl() since the file must
        // be open, which means our impl has already been chosen.

        // Delete the current record.
        doIt("deleteCurrentRecord");

        // Fire the FILE_MODIFIED FileEvent
        fireEvent(FileEvent.FILE_MODIFIED);
    }


    /**
     *Deletes the member associated with this object from the file.  The object cannot
     *be open when invoking this method.<br>
     *The name of the file and the system to which to connect must be set prior
     *to invoking this method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void deleteMember()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify object state
        if (isOpen_)
        {
            throw new ExtendedIllegalStateException(ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
        }

        chooseImpl();
        // Delete this member.
        doIt("deleteMember"); //@B0A
    }


    /**
     *Ends commitment control for this connection.
     *If commitment control has not been started for the connection, no action
     *is taken.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void endCommitmentControl()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        chooseImpl(); //@B0A
        doIt("endCommitmentControl"); //@B0A
    }


    //@E2A
    /**
     *Ends commitment control for the specified connection.
     *If commitment control has not been started for the connection, no action
     *is taken.
     *@param system The system for which commitment control should be ended.
     *
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public static void endCommitmentControl(AS400 system)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        if (system == null) throw new NullPointerException("system");
        system.signon(false);
        //    system.connectService(AS400.RECORDACCESS);
        AS400FileImpl impl = (AS400FileImpl)system.loadImpl3("com.ibm.as400.access.AS400FileImplNative",
                                                             "com.ibm.as400.access.AS400FileImplRemote",
                                                             "com.ibm.as400.access.AS400FileImplProxy");
        impl.doIt("endCommitmentControl", new Class[] { AS400Impl.class }, new Object[] { system.getImpl() });
    }


    //@D0A
    /**
     * Make sure the text objects are filled in for a Record object.
     **/
    Record fillInRecord(Record r)
    {
        if (r != null) r.initializeTextObjects(system_);
        return r;
    }

    /**
     *Closes the file when this object is garbage collected.
     *@exception Throwable If an exception is thrown while cleaning up.
     **/
    protected void finalize()
      throws Throwable
    {
        if (isOpen_)
        {
            doIt("discardReplies"); //@D1A - make sure we discard replies if we are in the finalizer thread!
            close();
        }
        super.finalize();
    }


    /**
     * Fire the appropriate event.
     **/
    private void fireEvent(int eIndex)
    {
        Vector targets = (Vector)fileListeners_.clone();
        FileEvent event = new FileEvent(this, eIndex);
        for (int i=0; i<targets.size(); ++i)
        {
            FileListener target = (FileListener)targets.elementAt(i);
            switch(eIndex)
            {
                case FileEvent.FILE_CLOSED:
                    target.fileClosed(event);
                    break;
                case FileEvent.FILE_CREATED:
                    target.fileCreated(event);
                    break;
                case FileEvent.FILE_DELETED:
                    target.fileDeleted(event);
                    break;
                case FileEvent.FILE_MODIFIED:
                    target.fileModified(event);
                    break;
                case FileEvent.FILE_OPENED:
                    target.fileOpened(event);
                default:
                    break;
            }
        }
    }


    /**
     *Returns the blocking factor being used for this file.
     *@see AS400File#open
     *@return The blocking factor for this file.  Zero will be returned if the file is not
     *open.
     **/
    public int getBlockingFactor()
    {
        if (!isOpen_) //@B0A
            return 0; //@B0A

        // @B5 - Don't need to choose an impl because if we're open,
        // an impl has already been chosen.
        return impl_.doItInt("getBlockingFactor"); //@B0C
    }


    /**
     *Returns the commit lock level for this file as specified on open.
     *@return The commit lock level for this file.
     *If commitment control has not been started for the connection or if
     *file has not been opened, -1 is returned.
     *Possible return values are:
     *                       <ul>
     *                       <li>COMMIT_LOCK_LEVEL_ALL
     *                       <li>COMMIT_LOCK_LEVEL_CHANGE
     *                       <li>COMMIT_LOCK_LEVEL_CURSOR_STABILITY
     *                       <li>COMMIT_LOCK_LEVEL_DEFAULT
     *                       <li>COMMIT_LOCK_LEVEL_NONE
     *                       <li>-1
     *                       </ul>
     **/
    public int getCommitLockLevel()
    {
        if (impl_ == null) //@B0A - the file is not opened if the impl_ is null
            return -1; //@B0A

        return impl_.doItInt("getCommitLockLevel");
    }


    /**
     *Returns the explicit locks that have been obtained for the file using this AS400File object.
     *Any locks that have been obtained through the lock(int) method on this AS400File
     *object are returned. Note that this method does not return any locks that have
     *been obtained by using the ALCOBJ CL command or by using a different AS400File
     *object created to reference the same physical file.
     *@see AS400File#lock
     *@return The explicit file locks held for the file by this AS400File object.
     *        Possible lock values are:
     *        <ul>
     *        <li>READ_EXCLUSIVE_LOCK
     *        <li>READ_ALLOW_SHARED_READ_LOCK
     *        <li>READ_ALLOW_SHARED_WRITE_LOCK
     *        <li>WRITE_EXCLUSIVE_LOCK
     *        <li>WRITE_ALLOW_SHARED_READ_LOCK
     *        <li>WRITE_ALLOW_SHARED_WRITE_LOCK
     *        </ul>
     *If no explicit locks have been obtained for the file using this AS400File object, an array of size zero
     *is returned. Note that this does not necessarily indicate that the actual physical file has not itself been locked by some other means.
     **/
    public int[] getExplicitLocks()
    {
        if (impl_ == null) //@B0A - the file is not opened if the impl_ is null
            return new int[0]; //@B0A

        return impl_.getExplicitLocks();
    }


    /**
     *Returns the file name.
     *@return The file name.  If the integrated file system pathname has not been set
     *for the object, an empty string is returned.
     **/
    public String getFileName()
    {
        return file_;
    }


    /**
     *Returns the library name.
     *@return The library name.  If the integrated file system pathname has not
     *been set for the object, an empty string is returned.
     **/
    public String getLibraryName()
    {
        return library_;
    }


    /**
     *Returns the member name.
     *@return The member name.  If the special value %FIRST% or %LAST% was specified
     *for the member portion of the file name and the file is not open, the special
     *value is returned.  If the special value %FIRST% or %LAST% was specified
     *for the member portion of the file name and the file is open, the member
     *name is returned.  If the integrated file system pathname has not been set
     *for the object, an empty string is returned.
     **/
    public String getMemberName()
    {
        return member_;
    }


    /**
     *Returns the integrated file system pathname for the file as specified on the
     *constructor or the setPath() method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@return The integrated file system pathname associated with this object.
     *If the integrated file system pathname has not been set for the object,
     *an empty string is returned.
     **/
    public String getPath()
    {
        return name_;
    }


    /**
     *Returns the record format of this file.
     *@see AS400File#create(com.ibm.as400.access.RecordFormat, java.lang.String)
     *@see AS400File#setRecordFormat
     *@return The record format of the file.  If the record format has
     *not been set, null is returned.
     **/
    public RecordFormat getRecordFormat()
    {
        return recordFormat_;
    }


    /**
     *Returns the system object for this object.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setSystem
     *@return The system for this object.  If the system has not been set,
     *null is returned.
     **/
    public AS400 getSystem()
    {
        return system_;
    }


    /**
     *Resets the state instance variables of this object to the appropriate
     *values for the file being closed.  This method is used to reset the
     *the state of the object when the connection has been ended abruptly.
     **/
    void initializeTransient() //@B0C
    {
        // Reset the open flag
        isOpen_ = false;

        // Take good care of our listeners
        fileListeners_ = new Vector();
        vetos_ = new VetoableChangeSupport(this); //@B0C
        changes_ = new PropertyChangeSupport(this);

        impl_ = null;
    }


    /**
     *Indicates if commitment control is started for
     *the connection.
     *@return true if commitment control has been started; false otherwise.
     **/
    public boolean isCommitmentControlStarted()
    {
        //@B0A - Need to go to the proxy server to see if
        // commitment control is started. But it is possible
        // that our system_ hasn't been set yet, so we need
        // to catch that exception and just return false.
        try
        {
            chooseImpl();
        }
        catch(ExtendedIllegalStateException e)
        {
            if (Trace.isTraceOn()) //@E2A
            {                      //@E2A
                Trace.log(Trace.ERROR, "Ignoring illegal state on isCommitmentControlStarted().", e); //@E2A
            }                      //@E2A
            return false;
        }
        catch(AS400SecurityException e) //@B5A
        {
            if (Trace.isTraceOn()) //@E2A
            {                      //@E2A
                Trace.log(Trace.ERROR, "Ignoring security exception on isCommitmentControlStarted().", e); //@E2A
            }                      //@E2A
            return false;                 //@B5A
        }
        catch(IOException e)            //@B5A
        {
            if (Trace.isTraceOn()) //@E2A
            {                      //@E2A
                Trace.log(Trace.ERROR, "Ignoring I/O exception on isCommitmentControlStarted().", e); //@E2A
            }                      //@E2A
            return false;                 //@B5A
        }
        return impl_.doItBoolean("isCommitmentControlStarted");
    }


    //@E2A
    /**
     *Indicates if commitment control is started for the specified system.
     *@param system The system that is checked to determine if commitment control is started.
     *@return true if commitment control has been started for the connection; false otherwise.
     **/
    public static boolean isCommitmentControlStarted(AS400 system)
    {
        if (system == null) throw new NullPointerException("system");
        try
        {
            system.signon(false);
            //      system.connectService(AS400.RECORDACCESS);
            AS400FileImpl impl = (AS400FileImpl)system.loadImpl3("com.ibm.as400.access.AS400FileImplNative",
                                                                 "com.ibm.as400.access.AS400FileImplRemote",
                                                                 "com.ibm.as400.access.AS400FileImplProxy");
            return impl.doItBoolean("isCommitmentControlStarted", new Class[] { AS400Impl.class }, new Object[] { system.getImpl() });
        }
        catch(IOException e)
        {
            if (Trace.isTraceOn()) //@E2A
            {                      //@E2A
                Trace.log(Trace.ERROR, "Ignoring I/O exception on isCommitmentControlStarted(AS400).", e); //@E2A
            }                      //@E2A
            return false;
        }
        catch(AS400SecurityException e)
        {
            if (Trace.isTraceOn()) //@E2A
            {                      //@E2A
                Trace.log(Trace.ERROR, "Ignoring security exception on isCommitmentControlStarted(AS400).", e); //@E2A
            }                      //@E2A
            return false;
        }
    }


    /**
     *Indicates if the file is open.
     *@return true if the file is open; false otherwise.
     **/
    public boolean isOpen()
    {
        return isOpen_;
    }


    // @A5A
    /**
     *Indicates if the records should be locked for update when doing reads in a READ_WRITE open mode. By
     *default, the records will be locked for update when doing reads in a READ_WRITE open mode.
     *@return <code>true</code> if the records should not be locked for update when doing reads in a
     *READ_WRITE open mode; <code>false</code> otherwise.
     *@see AS400File#setReadNoUpdate
     **/
    public boolean isReadNoUpdate()
    {
        if (impl_ != null) //@B0A @B5C
            readNoUpdate_ = impl_.doItBoolean("isReadNoUpdate"); //@B0C @B5C
        return readNoUpdate_; //@B5A
    }


    /**
     *Indicates if this object is open for read only.
     *@return true if the file is open for read only; false otherwise.
     **/
    public boolean isReadOnly()
    {
        if (impl_ == null) //@B0A
            return false; //@B0A

        return impl_.doItInt("getOpenType") == READ_ONLY; //@B0C
    }


    /**
     *Indicates if this object is open for read/write.
     *@return true if the file is open for read/write; false otherwise.
     **/
    public boolean isReadWrite()
    {
        if (impl_ == null) //@B0A
            return false; //@B0A

        return impl_.doItInt("getOpenType") == READ_WRITE; //@B0C
    }


    /**
     * Indicates if this object is being treated as an SSP file.
     * This method just returns the value set using {@link #setSSPFile setSSPFile()}.
     * @return true if the file is being treated as an SSP file; false
     * if it is being treated as a normal DDM file. The default is false.
     * @see #setSSPFile
    **/
    public boolean isSSPFile()
    {
      return ssp_;
    }


    /**
     *Indicates if this object is open for write only.
     *@return true if the file is open for write only; false otherwise.
     **/
    public boolean isWriteOnly()
    {
        if (impl_ == null) //@B0A
            return false; //@B0A

        return impl_.doItInt("getOpenType") == WRITE_ONLY; //@B0C
    }


    /**
     *Obtains a lock on the file.
     *The name of the file and the system to which to connect must be set prior
     *to invoking this method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@param lockToObtain The type of lock to acquire on the file.
     *                     Valid lock values are:
     *                     <ul>
     *                     <li>READ_EXCLUSIVE_LOCK
     *                     <li>READ_ALLOW_SHARED_READ_LOCK
     *                     <li>READ_ALLOW_SHARED_WRITE_LOCK
     *                     <li>WRITE_EXCLUSIVE_LOCK
     *                     <li>WRITE_ALLOW_SHARED_READ_LOCK
     *                     <li>WRITE_ALLOW_SHARED_WRITE_LOCK
     *                     </ul>
     *If <i>lockToObtain</i> has already been obtained, no action is taken.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void lock(int lockToObtain)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        if (lockToObtain < READ_ALLOW_SHARED_WRITE_LOCK ||
            lockToObtain > WRITE_EXCLUSIVE_LOCK)
        {
            throw new ExtendedIllegalArgumentException("lockToObtain", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        chooseImpl();
        impl_.doIt("lock", new Class[] { Integer.TYPE }, new Object[] { new Integer(lockToObtain) });
    }


    //@B2A
    /**
     *Opens the file.  The file must not be open when invoking this method.
     *The file cursor is positioned prior to the first record.<br>
     *The record format for the file must be set prior to calling this method.<br>
     *The name of the file and the system to which to connect must be set prior
     *to invoking this method.<br>
     *A value of READ_WRITE is used for the open type.<br>
     *A value of 1 is used for the blocking factor.<br>
     *A value of COMMIT_LOCK_LEVEL_DEFAULT is used for the commit lock level.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@see AS400File#setRecordFormat
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void open()
      throws AS400Exception, AS400SecurityException, InterruptedException, IOException
    {
        open(AS400File.READ_WRITE, 1, AS400File.COMMIT_LOCK_LEVEL_DEFAULT);
    }


    /**
     *Opens the file.  The file must not be open when invoking this method.
     *If commitment control is not started for the connection,
     *<i>commitLockLevel</i> is ignored.  The file cursor is positioned prior
     *to the first record.  If <i>blockingFactor</i> is greater than one (or
     *if zero is specified and a blocking factor greater than one is determined
     *by the object) and the file is opened for READ_ONLY, the record cache will
     *be filled with an initial set of records.<br>
     *The record format for the file must be set prior to calling this method.<br>
     *The name of the file and the system to which to connect must be set prior
     *to invoking this method.
     *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
     *@see AS400File#setPath
     *@see AS400File#setSystem
     *@see AS400File#refreshRecordCache
     *@see AS400File#setRecordFormat
     *@param openType The manner in which to open the file.  Valid values are:
     *                <ul>
     *                <li>READ_ONLY
     *                <li>READ_WRITE
     *                <li>WRITE_ONLY
     *                </ul>
     *If the <i>openType</i> is WRITE_ONLY, the various positionCursor() methods 
     *will fail since write() operations are appended to the end of the file. <br>
     *@param blockingFactor The number of records to retrieve or to write during a
     *read or write operation.<br>
     *The AS400File object will attempt to anticipate the need for data by accessing
     *blocks of records if the <i>openType</i> is READ_ONLY.  <br>
     *If the <i>openType</i>
     *is WRITE_ONLY, <i>blockingFactor</i> number of records will be written at one
     *time when writing an array of records.   <br>
     *If the open type is READ_WRITE, <i>blockingFactor</i> is ignored and a
     *blocking factor of 1 will be used for data integrity reasons.
     *Specify an appropriate <i>blockingFactor</i> for your performance needs.<br>
     *If 0 is specified for <i>blockingFactor</i>, a default value will be calculated
     *by taking the integer result of dividing 2048 by the byte length of the record
     *plus 16.<br>
     *If the user specifies a blocking factor greater than 1 or specifies 0,
     *which will cause a blocking factor to be calculated, there is the risk of
     *obtaining stale data when doing multiple read operations.
     *Invoke the refreshRecordCache() method prior to reading a record to cause the object
     *to read from the system if this is a problem.<br>
     *@param commitLockLevel Used to control record locking during a transaction if
     *commitment control has been started for the connection.
     *Valid values are:
     *<ul>
     *<li>COMMIT_LOCK_LEVEL_ALL
     *<li>COMMIT_LOCK_LEVEL_CHANGE
     *<li>COMMIT_LOCK_LEVEL_CURSOR_STABILITY
     *<li>COMMIT_LOCK_LEVEL_DEFAULT
     *<li>COMMIT_LOCK_LEVEL_NONE
     *</ul>
     *The <i>commitLockLevel</i> is ignored if commitment control is not started for
     *the connection.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public /* @B0C abstract */ void open(int openType,
                                         int blockingFactor,
                                         int commitLockLevel)
      throws AS400Exception, AS400SecurityException, InterruptedException, IOException
    //@B0A - moved code out of subclasses
    {
        // Verify the object state
        if (recordFormat_ == null)
        {
            throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        // Verify the parameters
        if (openType != READ_ONLY &&
            openType != READ_WRITE &&
            openType != WRITE_ONLY)
        {
            throw new ExtendedIllegalArgumentException("openType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (blockingFactor < 0)
        {
            throw new ExtendedIllegalArgumentException("blockingFactor", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (commitLockLevel < 0 || commitLockLevel > 4)
        {
            throw new ExtendedIllegalArgumentException("commitLockLevel", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        openFile(openType, blockingFactor, commitLockLevel, this instanceof KeyedFile); //@B0C
    }

    /**
     *Opens the file.  Helper function to open file for keyed or
     *sequential files.
     *@param openType The manner in which to open the file.
     *@param blockingFactor The number of records to retrieve or to write during a
     *                      read or write operation.
     *@param commitLockLevel Used to control record locking during a transaction if
     *commitment control has been started for the connection.
     *@param access The type of file access for which to open the file.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started..
     *@exception UnknownHostException If the system cannot be located.
     **/
    synchronized void openFile(int openType,
                               int blockingFactor,
                               int commitLockLevel,
                               boolean access) //@B0C
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify object state.  Note: we don't just close the file and re-open for the
        // specified open type because the user may have uncommitted changes.  To be
        // consistent, always throw exception if user tries to open when already open.
        if (isOpen_)
        {
            throw new ExtendedIllegalStateException(ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
        }

        // Set commit lock level to -1 if commitment control is not started
        chooseImpl();
        // Set the blocking factor for the file
        int bf; //@D0A

        // Before we calculate, make sure the record format has had
        // its text objects initialized.
        recordFormat_.initializeTextObjects(system_); //@D0A

        //@D0M - moved out of AS400FileImplBase
        if (openType != AS400File.READ_WRITE)
        {
            // Calculate the blocking factor if 0 was specified
            if (blockingFactor == 0)
            {
                //@E0M
                int block = 2048/(recordFormat_.getNewRecord().getRecordLength() + 16);
                bf = block > 0 ? block : 1;
            }
            else
            {
                bf = blockingFactor;
            }

            // Estimate the record increment.
            int recordIncrement = recordFormat_.getNewRecord().getRecordLength() +
              recordFormat_.getNumberOfFields() +
              recordFormat_.getNumberOfKeyFields() + 16;

            // We can only retrieve 16Mb of record data per GET so limit the
            // blocking factor appropriately.
            bf = (bf * recordIncrement >= 16777216 ? 16777216 / recordIncrement : bf);
        }
        else
        { // For open type of READ_WRITE or if the file is a KeyedFile, blocking
            // factor is set to 1 for data integrity
            // reasons (read_write implies we are reading and updating and therefore
            // want up-to-date data.
            bf = 1;
        }

        String[] toSet = impl_.openFile2(openType, bf, commitLockLevel, access); //@C0A @D0C

        //@C0A
        // index 0 is the new library name, null if it wasn't changed
        // index 1 is the new member name, null if it wasn't changed
        if (toSet[0] != null)
            library_ = toSet[0];
        if (toSet[1] != null)
        {
            String old = member_;
            member_ = toSet[1];
            changes_.firePropertyChange("member", old, member_);
        }

        isOpen_ = true;

        // Fire the FILE_OPENED FileEvent
        fireEvent(FileEvent.FILE_OPENED);
    }


    /**
     *Positions the file cursor to after the last record.  The file must be open when
     *invoking this method.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void positionCursorAfterLast()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        doIt("positionCursorAfterLast");
    }


    /**
     *Positions the file cursor to before the first record.  The file must be open
     *when invoking this method.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void positionCursorBeforeFirst()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        doIt("positionCursorBeforeFirst");
    }


    /**
     *Positions the file cursor to the first record.  The file must be open when
     *invoking this method.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void positionCursorToFirst()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        doIt("positionCursorToFirst");
    }


    /**
     *Positions the file cursor to the last record.  The file must be open when
     *invoking this method.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void positionCursorToLast()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        doIt("positionCursorToLast");
    }


    /**
     *Positions the file cursor to the next record.  The file must be open when
     *invoking this method.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void positionCursorToNext()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        doIt("positionCursorToNext");
    }


    /**
     *Positions the file cursor to the previous record.  The file must be open when
     *invoking this method.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void positionCursorToPrevious()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        doIt("positionCursorToPrevious");
    }


    /**
     *Reads the record at the current cursor position.  The file must be open when
     *invoking this method.  The cursor position does not change when this method is
     *invoked.
     *@return The record read.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public Record read()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        return fillInRecord(impl_.doItRecord("read", new Class[0], new Object[0])); //@D0C
    }


    /**
     *Reads all the records in the file. The file must be closed when invoking this method.
     *The record format for the file must have been set prior to invoking this method.
     *@return The records read.  If no records are read, an array of
     *size zero is returned.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    abstract public Record[] readAll()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException;


    /**
     *Reads the first record in the file.  The file must be open when invoking this
     *method.  The cursor is positioned to the first record of the file
     *as a result of invoking this method.
     *@return The record read.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public Record readFirst()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        return fillInRecord(impl_.doItRecord("readFirst", new Class[0], new Object[0])); //@D0C
    }


    /**
     *Reads the last record in the file.  The file must be open when invoking this
     *method.  The cursor is positioned to the last record of the file
     *as a result of invoking this method.
     *@return The record read.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public Record readLast()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        return fillInRecord(impl_.doItRecord("readLast", new Class[0], new Object[0])); //@D0C
    }


    /**
     *Reads the next record in the file from the current cursor position.  The file must
     *be open when invoking this method.  The cursor is positioned to the first active
     *record after the current cursor position as a result of invoking this method.
     *If this method is invoked when the cursor is positioned at the last record of the
     *file, null will be returned and the cursor is positioned after the last record
     *of the file.
     *@return The record read.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public Record readNext()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        return fillInRecord(impl_.doItRecord("readNext", new Class[0], new Object[0])); //@D0C
    }


    /**
     *Overrides the ObjectInputStream.readObject() method in order to return any
     *transient parts of the object to there properly initialized state.  We also
     *generate a declared file name for the object.  I.e we in effect
     *call the null constructor.  By calling ObjectInputStream.defaultReadObject()
     *we restore the state of any non-static and non-transient variables.  We
     *then continue on to restore the state (as necessary) of the remaining varaibles.
     *@param in The input stream from which to deserialize the object.
     *@exception ClassNotFoundException If the class being deserialized is not found.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    private void readObject(java.io.ObjectInputStream in)
      throws ClassNotFoundException,
    IOException
    {
        in.defaultReadObject();
        initializeTransient(); //@B0C
    }


    /**
     *Reads the previous record in the file from the current cursor position.  The file
     *must be open when invoking this method.  The cursor is positioned to the first active
     *record prior to the current cursor position as a result of invoking this method.
     *If this method is invoked when the cursor is positioned at the first record of the
     *file, null is returned and the cursor is positioned before the first record
     *of the file.
     *@return The record read.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public Record readPrevious()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        return fillInRecord(impl_.doItRecord("readPrevious", new Class[0], new Object[0])); //@D0C
    }


    /**
     *Refreshes the record cache for this file.  Invoking this method will cause the
     *retrieval of records from the system.  The cursor position is set to the
     *first record of the file.  This method only needs to
     *be invoked if a blocking factor greater than 1 is being used, and the user
     *wants to refresh the records in the cache.  The file must be open when invoking
     *this method.  No action is taken if records are not being cached (for example, the
     *blocking factor is set to one).
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void refreshRecordCache()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify object state
        checkOpen();
        doIt("refreshRecordCache");
    }


    /**
     *Releases all locks acquired via the lock() method.  If no locks have been
     *explicitly obtained, no action is taken.
     *@see AS400File#lock
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void releaseExplicitLocks()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        chooseImpl();
        doIt("releaseExplicitLocks");
    }


    /**
     *Removes a listener from the file listeners list.
     *If the listener is not on the list, does nothing.
     *@see #addFileListener
     *@param listener The FileListener.
     **/
    public void removeFileListener(FileListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException("listener");
        }
        fileListeners_.removeElement(listener);
    }


    /**
     *Removes a listener from the change list.
     *If the listener is not on the list, does nothing.
     *@see #addPropertyChangeListener
     *@param listener The PropertyChangeListener.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException("listener");
        }
        changes_.removePropertyChangeListener(listener);
    }


    /**
     *Removes a listener from the veto change listeners list.
     *If the listener is not on the list, does nothing.
     *@see #addVetoableChangeListener
     *@param listener The VetoableChangeListener.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException("listener");
        }
        vetos_.removeVetoableChangeListener(listener); //@B0C
    }


    /**
     *Rolls back any transactions since the last commit/rollback boundary.  Invoking this
     *method will cause all transactions under commitment control for this connection
     *to be rolled back.  This means that any AS400File object for which a commit
     *lock level was specified and that was opened under this connection will have
     *outstanding transactions rolled back.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void rollback()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        if (isCommitmentControlStarted()) // if returns true, then impl has been chosen.
        {
            doIt("rollback");
        }
    }


    //@E2A
    /**
     *Rolls back any transactions since the last commit/rollback boundary for the specified system.
     *Invoking this method will cause all transactions under commitment control for this connection
     *to be rolled back.  This means that any AS400File object for which a commit
     *lock level was specified and that was opened under this connection will have
     *outstanding transactions rolled back.
     *@param system The system for which transactions will be rolled back.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public static void rollback(AS400 system)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        if (system == null) throw new NullPointerException("system");
        system.signon(false);
        //    system.connectService(AS400.RECORDACCESS);
        AS400FileImpl impl = (AS400FileImpl)system.loadImpl3("com.ibm.as400.access.AS400FileImplNative",
                                                             "com.ibm.as400.access.AS400FileImplRemote",
                                                             "com.ibm.as400.access.AS400FileImplProxy");
        impl.doIt("rollback", new Class[] { AS400Impl.class }, new Object[] { system.getImpl() });
    }

    /**
     * Runs a CL command in the DDM host server job. This is useful for changing the IASP
     * used by the DDM server for the currently connected AS400 object, changing library lists,
     * etc.
     * <p>
     * Note: If using Toolbox native optimizations, the CL command will run in
     * the current job, since there will not be an associated DDM host server job.
     * @param command The CL command to run.
     * @return The list of AS400Message objects output by the CL command (if any).
     * @see com.ibm.as400.access.CommandCall
    **/ 
    public AS400Message[] runCommand(String command) throws AS400SecurityException, InterruptedException, IOException
    {
      if (command == null) throw new NullPointerException("command");
      chooseImpl();
      return impl_.execute(command);
    }

    //@B0A
    /**
     * Used internally to parse the pathname and set the individual
     * library, filename, and member strings.
     **/
    private void setName(String name)
    {
        // Construct a QSYSObjectPathName object and parse out the library,
        // file and member names
        QSYSObjectPathName ifs = new QSYSObjectPathName(name);
        if (!(ifs.getObjectType().equals("FILE") || ifs.getObjectType().equals("MBR")))
        { // Invalid object type
            throw new IllegalPathNameException(name, IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
        }
        // Set the instance data as appropriate
        library_ = ifs.getLibraryName();
        file_ = ifs.getObjectName();
        if (ifs.getObjectType().equals("FILE"))
        { // No member specified; default member to *FIRST
            member_ = "*FIRST";
        }
        else
        { // Member specified; if special value %FILE% was specified, member name
            // is the file name
            member_ = (ifs.getMemberName().equalsIgnoreCase("*FILE") ? file_ :
                       ifs.getMemberName());
        }
        name_ = name;
    }


    /**
     *Sets the integrated file system pathname for the file.
     *@param name The integrated file system pathname of the file.
     *@exception PropertyVetoException If a change is vetoed.
     **/
    public void setPath(String name)
      throws PropertyVetoException
    {
        // Verify parameters
        if (name == null)
        {
            throw new NullPointerException("name");
        }
        // Verify object state
        if (isOpen_)
        { // Cannot set after we have connected
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        //@B0M
        String oldName = name_;
        vetos_.fireVetoableChange("path", oldName, name); //@B0A

        //@B0C
        // Notify veto listeners of the change
        // Construct a QSYSObjectPathName object and parse out the library, file and member names
        setName(name); //@B0A

        if (impl_ != null) impl_.doItNoExceptions("setPath", new Class[] { String.class }, new Object[] { name }); //@B0A
        changes_.firePropertyChange("path", oldName, name); //@B0C
    }


    // @A5A
    /**
     *Sets the readNoUpdate flag, which determines whether the records should be locked for update
     *when doing reads or positionCursor operations in a READ_WRITE open mode. 
     *@param readNoUpdate <code>true</code> if the records should not be locked for update when doing
     *reads in a READ_WRITE open mode; <code>false</code> otherwise.
     *@see AS400File#isReadNoUpdate
     **/
    public void setReadNoUpdate(boolean readNoUpdate)
    {
        //@B5 - chooseImpl() throws exceptions now, so what we will do
        // is save a copy of readNoUpdate and set it to the proxy server
        // when an impl gets chosen. (Like bean properties get set).
        readNoUpdate_ = readNoUpdate; //@B5A
        if (impl_ != null) //@B5A
        {
            impl_.doItNoExceptions("setReadNoUpdate", new Class[] { Boolean.TYPE },
            new Object[] { new Boolean(readNoUpdate) }); //@B0C
        }
    }


    //@B2A
    /**
     *Sets the record format to be used for this file. Retrieves the record
     *format(s) from the file on the system and sets the record format to
     *be the first format retrieved from the file. Calling this method is
     *the same as calling setRecordFormat(0).
     *The record format must be set prior to invoking open() or readAll().
     *@see AS400FileRecordDescription#retrieveRecordFormat
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception PropertyVetoException If a change is vetoed.
     **/
    public void setRecordFormat()
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException,
    PropertyVetoException
    {
        setRecordFormat(0);
    }


    //@B2A
    /**
     *Sets the record format to be used for this file. Retrieves the record
     *format(s) from the file on the system and sets the record format to
     *the <i>recordFormat</i> one.
     *The record format must be set prior to invoking open() or readAll().
     *@see AS400FileRecordDescription#retrieveRecordFormat
     *@param recordFormat The index of the record format to be used.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception PropertyVetoException If a change is vetoed.
     **/
    public void setRecordFormat(int recordFormat)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException,
    PropertyVetoException
    {
        if (recordFormat < 0)
        {
            throw new ExtendedIllegalArgumentException("recordFormat (" + String.valueOf(recordFormat) + ") too small", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        chooseImpl();
        setRecordFormat(impl_.doItRecordFormat("setRecordFormat", new Class[] { Integer.TYPE }, new Object[] { new Integer(recordFormat) }));
    }


    //@B2A
    /**
     *Sets the record format to be used for this file. Retrieves the record
     *format(s) from the file on the system and sets the record format to
     *be <i>recordFormat</i>.
     *The record format must be set prior to invoking open() or readAll().
     *@see AS400FileRecordDescription#retrieveRecordFormat
     *@param recordFormat The name of the record format to be used.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception PropertyVetoException If a change is vetoed.
     **/
    public void setRecordFormat(String recordFormat)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException,
    PropertyVetoException
    {
        if (recordFormat == null)
        {
            throw new NullPointerException("recordFormat");
        }
        chooseImpl();
        setRecordFormat(impl_.doItRecordFormat("setRecordFormat", new Class[] { String.class }, new Object[] { recordFormat }));
    }


    /**
     *Sets the record format to be used for this file.
     *The record format must be set prior to invoking open() or readAll().
     *<p><b>Note:</b> This method is not supported for multi-format logical files.  
     *Multi-format logical files must use one of the other setRecordFormat() methods.
     *@see #setRecordFormat(int)
     *@see #setRecordFormat(java.lang.String)
     *@param recordFormat The record format for this file.
     *@exception PropertyVetoException If a change is vetoed.
     **/
    public void setRecordFormat(RecordFormat recordFormat)
      throws PropertyVetoException
    {
        // Verify parameters
        if (recordFormat == null)
        {
            throw new NullPointerException("recordFormat");
        }
        //@B0C
        // Notify veto listeners of the change
        RecordFormat old = recordFormat_;
        vetos_.fireVetoableChange("recordFormat", old, recordFormat); //@B0A
        recordFormat_ = recordFormat;
        if (impl_ != null) impl_.doItNoExceptions("setRecordFormat", new Class[] { RecordFormat.class }, new Object[] { recordFormat_ }); //@B0A
        changes_.firePropertyChange("recordFormat", old, recordFormat_);
    }



    /**
     * Sets the SSP flag for this file. This flag indicates
     * whether or not to treat the file on the system
     * as a System/36 SSP file. When set to true, the record
     * format name is ignored. When set to false, the file
     * is treated as a normal physical or logical DDM file.
     * The default is false.
     * @param treatAsSSP The flag indicating how to treat the file.
     * @see #isSSPFile
    **/
    public void setSSPFile(boolean treatAsSSP)
    {
      if (isOpen_)
      { // Cannot set after we have connected
          throw new ExtendedIllegalStateException("treatAsSSP", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
      }

      ssp_ = treatAsSSP;

      if (impl_ != null)
      {
        impl_.doItNoExceptions("setSSPFile", new Class[] { Boolean.TYPE },
        new Object[] { new Boolean(treatAsSSP) });
      }
    }


    /**
     *Sets the system to which to connect.
     *@param system The system to which to connect.
     *@exception PropertyVetoException If a change is vetoed.
     **/
    public void setSystem(AS400 system)
      throws PropertyVetoException
    {
        // Verify parameters
        if (system == null)
        {
            throw new NullPointerException("system");
        }
        // Verify state
        if (isOpen_ || impl_ != null)  // THIS USED TO CHECK IF WE WERE CONNECTED
        { // Cannot set after we have connected
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
        //@B0C
        // Notify veto listeners of the change
        AS400 old = system_;
        vetos_.fireVetoableChange("system", old, system); //@B0A
        system_ = system;
        changes_.firePropertyChange("system", old, system_);
    }


    /**
     *Starts commitment control on this file (for this connection).  If commitment control
     *has already been started for the connection, an exception is thrown.
     *@param commitLockLevel The type of commitment control
     *                  to exercise.  Valid values are:
     *                  <ul>
     *                  <li>COMMIT_LOCK_LEVEL_ALL
     *                  <li>COMMIT_LOCK_LEVEL_CHANGE
     *                  <li>COMMIT_LOCK_LEVEL_CURSOR_STABILITY
     *                  </ul>
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public void startCommitmentControl(int commitLockLevel)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify parameters
        if (commitLockLevel < COMMIT_LOCK_LEVEL_ALL || commitLockLevel > COMMIT_LOCK_LEVEL_CURSOR_STABILITY)
        {
            throw new ExtendedIllegalArgumentException("commitLockLevel", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Verify that commitment control isn't already started.
        if (isCommitmentControlStarted())
        {
            throw new ExtendedIllegalStateException(ExtendedIllegalStateException.COMMITMENT_CONTROL_ALREADY_STARTED);
        }

        // Start commitment control.
        chooseImpl();
        impl_.doIt("startCommitmentControl", new Class[] { Integer.TYPE }, new Object[] { new Integer(commitLockLevel) });
    }


    //@E2A
    /**
     *Starts commitment control for the specified system.  If commitment control
     *has already been started for the connection, an exception is thrown.
     *@param system The system for which commitment control should be started.
     *@param commitLockLevel The type of commitment control
     *                  to exercise.  Valid values are:
     *                  <ul>
     *                  <li>COMMIT_LOCK_LEVEL_ALL
     *                  <li>COMMIT_LOCK_LEVEL_CHANGE
     *                  <li>COMMIT_LOCK_LEVEL_CURSOR_STABILITY
     *                  </ul>
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     *@exception ServerStartupException If the host server cannot be started.
     *@exception UnknownHostException If the system cannot be located.
     **/
    public static void startCommitmentControl(AS400 system, int commitLockLevel)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        if (system == null)
        {
            throw new NullPointerException("system");
        }

        if (commitLockLevel < COMMIT_LOCK_LEVEL_ALL || commitLockLevel > COMMIT_LOCK_LEVEL_CURSOR_STABILITY)
        {
            throw new ExtendedIllegalArgumentException("commitLockLevel", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Verify that commitment control isn't already started.
        //@E2 - This now happens in AS400FileImplBase for performance.

        // Start commitment control.
        system.signon(false);
        //    system.connectService(AS400.RECORDACCESS);
        AS400FileImpl impl = (AS400FileImpl)system.loadImpl3("com.ibm.as400.access.AS400FileImplNative",
                                                             "com.ibm.as400.access.AS400FileImplRemote",
                                                             "com.ibm.as400.access.AS400FileImplProxy");

        impl.doIt("startCommitmentControl", new Class[] { AS400Impl.class, Integer.TYPE },
        new Object[] { system.getImpl(), new Integer(commitLockLevel) });
    }


    /**
     *Updates the record at the current cursor position.  The file must be open when
     *invoking this method.  The cursor must be positioned to an active record.  The
     *last operation on the file must have been a cursor positioning operation or a
     *read operation.  If an attempt is made to update a record more than once without
     *reading the record or positioning the cursor to the record in between updates, an
     *AS400Exception is thrown.  The cursor position is not changed when this method
     *is invoked.
     *@param record The record with which to update.  The record must be a record whose
     *format matches the record format of this object.  To ensure that this
     *requirement is met, use the
     *<a href="RecordFormat.html">RecordFormat.getNewRecord()</a>
     *method to obtain a default record whose fields can be set appropriately by
     *the Java program and then written to the file.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void update(Record record)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        // Verify parameters
        if (record == null)
        {
            throw new NullPointerException("record");
        }
        impl_.doIt("update", new Class[] { Record.class }, new Object[] { record });

        // Fire the FILE_MODIFIED FileEvent
        fireEvent(FileEvent.FILE_MODIFIED);
    }


    /**
     *Writes a record to the file.  The file must be open when invoking this
     *method.  The record is written to the end of the file.
     *The cursor is positioned to after the last record of the file as a result
     *of invoking this method.
     *@param record The record to write.  The record must be a record whose
     *format matches the record format of this object.  To ensure that this
     *requirement is met, use the
     *<a href="RecordFormat.html">RecordFormat.getNewRecord()</a>
     *method to obtain a default record whose fields can be set appropriately by
     *the Java program and then written to the file.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void write(Record record)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        // Verify parameters
        if (record == null)
        {
            throw new NullPointerException("record");
        }

        // Call write(Record[]) to do the work
        write(new Record[] { record }); //@B0C
    }


    /**
     *Writes an array of records to the file.  The file must be open when invoking
     *this method.  The records are written to the end of the file.
     *The cursor is positioned to after the last record of the file as a result
     *of invoking this method.
     *@param records The records to write.  The records must have a format
     *which matches the record format of this object.  To ensure that this
     *requirement is met, use the
     *<a href="RecordFormat.html">RecordFormat.getNewRecord()</a>
     *method to obtain default records whose fields can be set appropriately by
     *the Java program and then written to the file.
     *@exception AS400Exception If the system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the system.
     **/
    public void write(Record[] records)
      throws AS400Exception,
    AS400SecurityException,
    InterruptedException,
    IOException
    {
        // Verify the object state
        checkOpen();
        // Verify parameters
        if (records == null)
        {
            throw new NullPointerException("records");
        }
        if (records.length == 0)
        {
            throw new ExtendedIllegalArgumentException("records", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        impl_.doIt("write", new Class[] { Record[].class }, new Object[] { records });

        // Fire the FILE_MODIFIED FileEvent
        fireEvent(FileEvent.FILE_MODIFIED);
    }
}
