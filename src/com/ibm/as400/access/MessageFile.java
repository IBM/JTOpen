///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  MessageFile.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
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

/**
 Represents a message file object on the system.  This class allows a user to get a message from a message file, returning an AS400Message object which contains the message.  The calling program can optionally supply substitution text for the message.
 <p>MessageFile will optionally format the message's associated help text.  Three options are available for help text formatting:
 <ol>
 <li>No formatting - the help text is returned as a string of characters.  This is the default.
 <li>Include formatting characters - the help text contains formatting characters.  The formatting characters are:
 <ul>
 <br>&N -- Force a new line.
 <br>&P -- Force a new line and indent the new line six characters.
 <br>&B -- Force a new line and indent the new line four characters.
 </ul>
 <li>Substitute formatting characters - the MessageFile class replaces system formatting characters with newline and space characters.
 </ol>
 The difference between options 2 and 3 are with line wrapping.  If the formatting characters remain the application can handle line wrapping and indentation.  If the MessageFile class inserts newline and space characters, Java components will handle line wrapping.
 <p>For example, to retrieve and print a message:
 <pre>
 AS400 system = new AS400("mysystem.mycompany.com");
 MessageFile messageFile = new MessageFile(system);
 messageFile.setPath("/QSYS.LIB/QCPFMSG.MSGF");
 AS400Message message = messageFile.getMessage("CPD0170");
 System.out.println(message.getText());
 </pre>
 <p>You can also sequentially retrieve messages from a message file by using the {@link #FIRST FIRST} and {@link #NEXT NEXT} message id values.
 <pre>
 AS400Message msg = messageFile.getMessage(MessageFile.FIRST);

 while (msg != null) {
    System.out.println(msg.getID() + " = " + msg.getText());
    msg = messageFile.getMessage(MessageFile.NEXT);
 }
</pre>
 @see  AS400Message
 @see  CommandCall
 @see  ProgramCall
 @see  QSYSObjectPathName
 **/
public class MessageFile implements Serializable
{
    static final long serialVersionUID = 4L;

    /**
     Constant indicating help text should not be formatted.
     **/
    public static final int NO_FORMATTING = 0;

    /**
     Constant indicating formatting characters are left in the help text.
     **/
    public static final int RETURN_FORMATTING_CHARACTERS = 1;

    /**
     Constant indicating MessageFile should replace formatting characters with newline and space characters.
     **/
    public static final int SUBSTITUTE_FORMATTING_CHARACTERS = 2;

    /**
     Constant indicating "the CCSID of the job".
     **/
    public static final int CCSID_OF_JOB = 0;

    // The type of help text formatting.
    private int helpTextFormatting_ = NO_FORMATTING;

    // The system where the message file is located.
    private AS400 system_ = null;
    // The full IFS path name of the message file.
    private String path_ = "";
    // The library that contains the message file.
    private String library_ = null;
    // The name of the message file.
    private String name_ = null;

    // Track if a connection to the system has been made.
    private transient boolean connected_ = false;

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    // The previously returned message ID. (For reference when specifying *NEXT)
    private String previousMessageId_ = null;

    /**
     * Constant indicating we are going to retrieve the next message
     * (using the previous message as a starting point).
     */
    public static final String NEXT = "*NEXT";

    /**
     * Constant indicating we are going to retrieve the first message
     * in the message file.
     */
    public static final String FIRST = "*FIRST";

    // Special values for the "message option" parameter of the QMHRTVM API:

    // "*MSGID    " (in EBCDIC)
    private static final byte[] OPTION_MSGID = 
        new byte[] { (byte)0x5C, (byte)0xD4, (byte)0xE2, (byte)0xC7, (byte)0xC9, (byte)0xC4, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 };

    // "*NEXT     " (in EBCDIC)
    private static final byte[] OPTION_NEXT = 
        new byte[] { (byte)0x5C, (byte)0xD5, (byte)0xC5, (byte)0xE7, (byte)0xE3, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 };

    // "*FIRST    " (in EBCDIC)
    private static final byte[] OPTION_FIRST = 
        new byte[] { (byte)0x5C, (byte)0xC6, (byte)0xC9, (byte)0xD9, (byte)0xE2, (byte)0xE3, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 };
    
    /**
     Constructs a MessageFile object.  The system and message file name must be provided later.
     **/
    public MessageFile()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing MessageFile object.");
    }

    /**
     Constructs a MessageFile object.  It uses the specified system.  The message file name must be provided later.
     @param  system  The system object representing the system on which the message file exists.
     **/
    public MessageFile(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing MessageFile object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     Constructs a MessageFile object.  It uses the specified system and message file name.
     @param  system  The system object representing the system on which the message file exists.
     @param  path  The integrated file system path name for the message file.  That is, the message file name as a fully qualified path name in the library file system.  The library and message file name must each be 10 characters or less.  The extension for message files is .msgf.  For example, /QSYS.LIB/MYLIB.LIB/MYFILE.MSGF.
     **/
    public MessageFile(AS400 system, String path)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing MessageFile object, system: " + system + " path: " + path);

        // Check parameters.
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (path == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'path' is null.");
            throw new NullPointerException("path");
        }
        QSYSObjectPathName ifs = new QSYSObjectPathName(path, "MSGF");

        // Set instance variables.
        library_ = ifs.getLibraryName();
        name_ = ifs.getObjectName();
        path_ = path;
        system_ = system;
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange()</b> method will be called each time the value of any bound property is changed.
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
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange()</b> method will be called each time the value of any constrained property is changed.
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
     Substitutes formatting characters with appropriate new line and indent characters.  The formatting characters are:
     <ul>
     <br>&N -- Force a new line.
     <br>&P -- Force a new line and indent the new line six characters.
     <br>&B -- Force a new line and indent the new line four characters.
     </ul>
     @param  sourceText  The source text.
     @return  The formatted text.
     **/
    public static String substituteFormattingCharacters(String sourceText)
    {
        // To preserve behavior, assume not BiDi.
        return substituteFormattingCharacters(sourceText, false);
    }

    private static String substituteFormattingCharacters(String sourceText, boolean bidi)
    {
        String targetText = sourceText;
        targetText = replaceText(targetText, "&N", "\n");
        targetText = replaceText(targetText, "&P", "\n      ");
        targetText = replaceText(targetText, "&B", "\n    ");

        if (bidi)
        {
            targetText = replaceText(targetText, "N&", "\n");
            targetText = replaceText(targetText, "P&", "\n      ");
            targetText = replaceText(targetText, "B&", "\n    ");
        }
        return targetText;
    }

    /**
     Returns the status of help text formatting.  Possible values are:
     <ul>
     <li>NO_FORMATTING - The help text is returned as a string of characters.  This is the default.
     <li>RETURN_FORMATTING_CHARACTERS - The help text contains formatting characters.  The formatting characters are:
     <ul>
     <br>&N -- Force a new line.
     <br>&P -- Force a new line and indent the new line six characters.
     <br>&B -- Force a new line and indent the new line four characters.
     </ul>
     <li>SUBSTITUTE_FORMATTING_CHARACTERS - The MessageFile class replaces formatting characters with newline and space characters.
     </ul>
     @return  The status of help text formatting.
     **/
    public int getHelpTextFormatting()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting help text formatting:", helpTextFormatting_);
        return helpTextFormatting_;
    }

    /**
     Returns the integrated file system path name of the message file.
     @return  The fully-qualified message file name, or an empty string ("") if not set.
     **/
    public String getPath()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting path: " + path_);
        return path_;
    }

    /**
     Returns an AS400Message object containing the object.  The system and message file name must be set before calling this method.
     @param  ID  The message identifier, {@link #FIRST FIRST}, or {@link #NEXT NEXT}.
     @return  An AS400Message object containing the message.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public AS400Message getMessage(String ID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving message from message file, ID: " + ID);
        return getMessage(ID, (byte[])null);
    }

    /**
     Returns an AS400Message object containing the object.  The system and message file name must be set before calling this method.
     @param  ID  The message identifier, {@link #FIRST FIRST}, or {@link #NEXT NEXT}.
     @param  type  The bidi message string type, as defined by the CDRA (Character Data Representataion Architecture).  See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @return  An AS400Message object containing the message.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public AS400Message getMessage(String ID, int type) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving message from message file, ID: " + ID + ", type:", type);
        return getMessage(ID, (byte[])null, type);
    }

    /**
     Returns an AS400Message object containing the message.  The system and message file name must be set before calling this method.  Up to 1024 bytes of substitution text can be supplied to this method.  The calling program is responsible for correctly formatting the string containing the substitution text for the specified message.
     <p>For example, using CL command DSPMSGD, we see the format of the substitution text for message CPD0170 is char 4, char 10, char 10.  Passing string <pre>"12  abcd      xyz"</pre> as the substitution text on this call means "12" will be substituted for &1, "abcd" will be substituted for &2, and "xyz" will be substituted for &3.
     @param  ID  The message identifier, {@link #FIRST FIRST}, or {@link #NEXT NEXT}.
     @param  substitutionText  The substitution text.
     @return  An AS400Message object containing the message.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public AS400Message getMessage(String ID, String substitutionText) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving message from message file, ID: " + ID + ", substitutionText: " + substitutionText);
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (AS400BidiTransform.isBidiCcsid(system_.getCcsid()))
        {
            return getMessage(ID, substitutionText, AS400BidiTransform.getStringType(system_.getCcsid()));
        }
        return getMessage(ID, substitutionText, BidiStringType.DEFAULT);
    }

    /**
     Returns an AS400Message object containing the message.  The system and message file name must be set before calling this method.  Up to 1024 bytes of substitution text can be supplied to this method.  The calling program is responsible for correctly formatting the string containing the substitution text for the specified message.
     <p>For example, using CL command DSPMSGD, we see the format of the substitution text for message CPD0170 is char 4, char 10, char 10.  Passing string <pre>"12  abcd      xyz"</pre> as the substitution text on this call means "12" will be substituted for &1, "abcd" will be substituted for &2, and "xyz" will be substituted for &3.
     @param  ID  The message identifier, {@link #FIRST FIRST}, or {@link #NEXT NEXT}.
     @param  substitutionText  The substitution text.
     @param  type  The bidi message string type, as defined by the CDRA (Character Data Representataion Architecture).  See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @return  An AS400Message object containing the message.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public AS400Message getMessage(String ID, String substitutionText, int type) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving message from message file, ID: " + ID + ", substitutionText: " + substitutionText + ", type:", type);
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        return getMessage(ID, substitutionText != null ? new Converter(system_.getCcsid(), system_).stringToByteArray(substitutionText, type) : null, type);
    }

    /**
     Returns an AS400Message object containing the message.  The system and message file name must be set before calling this method.  Up to 1024 bytes of substitution text can be supplied to this method.  <b>The byte array is not changed or converted before being sent to the system</b>.
     @param  ID  The message identifier, {@link #FIRST FIRST}, or {@link #NEXT NEXT}.
     @param  substitutionText  The substitution text.  The bytes are assumed to be in the CCSID of the job.
     @return  An AS400Message object containing the message.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public AS400Message getMessage(String ID, byte[] substitutionText) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving message from message file, ID: " + ID + ", substitutionText:", substitutionText);
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (AS400BidiTransform.isBidiCcsid(system_.getCcsid()))
        {
            return getMessage(ID, substitutionText, AS400BidiTransform.getStringType(system_.getCcsid()));
        }
        return getMessage(ID, substitutionText, BidiStringType.DEFAULT);
    }

    /**
     Returns an AS400Message object containing the message.  The system and message file name must be set before calling this method.  Up to 1024 bytes of substitution text can be supplied to this method.  <b>The byte array is not changed or converted before being sent to the system</b>.
     @param  ID  The message identifier, {@link #FIRST FIRST}, or {@link #NEXT NEXT}.
     @param  substitutionText  The substitution text.  The bytes are assumed to be in the CCSID of the job.
     @param  type  The bidi message string type, as defined by the CDRA (Character Data Representataion Architecture).  See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @return  An AS400Message object containing the message.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public AS400Message getMessage(String ID, byte[] substitutionText, int type) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving message from message file, ID: " + ID + ", type:" + type + ", substitutionText:", substitutionText);
        return getMessage(ID, substitutionText, type, CCSID_OF_JOB, CCSID_OF_JOB);
    }


    /**
     Returns an AS400Message object containing the message.  The system and message file name must be set before calling this method.  Up to 1024 bytes of substitution text can be supplied to this method.  <b>The byte array is not changed or converted before being sent to the system</b>.
     @param  ID  The message identifier, {@link #FIRST FIRST}, or {@link #NEXT NEXT}.
     @param  substitutionText  The substitution text.
     @param  type  The bidi message string type, as defined by the CDRA (Character Data Representataion Architecture).  The default value is {@link BidiStringType#DEFAULT BidiStringType.DEFAULT}. See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @param  ccsidOfSubstitutionText  The CCSID of the substitution text.  The default value is {@link #CCSID_OF_JOB CCSID_OF_JOB}.
     @param  ccsidToConvertTo  The CCSID in which the system should return the message text. The Toolbox then converts from that CCSID to Unicode when constructing the AS400Message.  The default value is {@link #CCSID_OF_JOB CCSID_OF_JOB}.
     @return  An AS400Message object containing the message.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public AS400Message getMessage(String ID, byte[] substitutionText, int type, int ccsidOfSubstitutionText, int ccsidToConvertTo) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving message from message file, ID: " + ID + ", type:" + type+ ", ccsidOfSubstitutionText:" + ccsidOfSubstitutionText + ", substitutionText:", substitutionText);
        if (ID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'ID' is null.");
            throw new NullPointerException("ID");
        }
        if (ID.length() > 7)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'ID' is not valid: '" + ID + "'");
            throw new ExtendedIllegalArgumentException("ID (" + ID + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (path_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting path.");
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Prevent changing the system or path after retrieving a message.
        connected_ = true;

        Converter conv = new Converter(system_.getCcsid(), system_);

        byte[] nameBytes = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };  // 20 blanks (in EBCDIC)
        conv.stringToByteArray(name_, nameBytes, 0, 10, type);
        conv.stringToByteArray(library_, nameBytes, 10, 10, type);

        byte[] optionBytes = OPTION_MSGID;  // "*MSGID" (in EBCDIC)
        if (ID.equals(FIRST))
        {
            optionBytes = OPTION_FIRST;
            ID = "";
        } else if (ID.equals(NEXT))
        {
          if (previousMessageId_ == null)
          {
            // No previous message, so assume they want the first message.
            optionBytes = OPTION_FIRST;
            ID = "";
          }
          else
          {
            optionBytes = OPTION_NEXT;
            ID = previousMessageId_;
          }
        }
        
        byte[] idBytes = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };  // 7 blanks (in EBCDIC)
        conv.stringToByteArray(ID, idBytes, 0, 7);

        if (substitutionText == null) substitutionText = new byte[0];

        byte[] starNoBytes = new byte[] { 0x5C, (byte)0xD5, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };  // "*NO       " (in EBCDIC)
        byte[] starYesBytes = new byte[] { 0x5C, (byte)0xE8, (byte)0xC5, (byte)0xE2, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };  // "*YES      " (in EBCDIC)

        byte[] replace = (substitutionText.length == 0) ? starNoBytes : starYesBytes;
        byte[] format = (helpTextFormatting_ == 0) ? starNoBytes : starYesBytes;

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Message information, output, char(*).
            new ProgramParameter(5120),
            // Length of message information, input, binary(4). (0x1400 == 5120)
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x14, 0x00 } ),
            // Format name, input, char(8), "RTVM0300" (in EBCDIC).
            new ProgramParameter(new byte[] { (byte)0xD9, (byte)0xE3, (byte)0xE5, (byte)0xD4, (byte)0xF0, (byte)0xF3, (byte)0xF0, (byte)0xF0 } ),
            // Message identifier, input, char(7).
            new ProgramParameter(idBytes),
            // Qualified message file name, input, char(20).
            new ProgramParameter(nameBytes),
            // Replacement data, input, char(*).
            new ProgramParameter(substitutionText),
            // Length of replacement data, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(substitutionText.length)),
            // Replace substitution values, input, char(10).
            new ProgramParameter(replace),
            // Return format control characters, input, char(10).
            new ProgramParameter(format),
            // Error code, I/0, char(*).
            new ProgramParameter(new byte[8]),
            // Retrieve option, input, char(10)
            new ProgramParameter(optionBytes),
           // CCSID to convert to, input, binary(4).  0 == "ccsid of job".
            new ProgramParameter(BinaryConverter.intToByteArray(ccsidToConvertTo)),
            // CCSID of replacement data, input, binary(4).  0 == "ccsid of job".
            new ProgramParameter(BinaryConverter.intToByteArray(ccsidOfSubstitutionText))
        };

        // Design note: The 3 "optional parameters" existed in V5R1, and probably even earlier.
        // In order to implement the *FIRST and *NEXT options, the three optional
        // parameters are now always included.
        
        // Call the program.
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHRTVM.PGM", parameters);
        pc.setThreadSafe(true);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList()[0]);
        }

        // Prepare to convert returned character bytes to Unicode.
        int ccsidOfTextBytes;  // CCSID of returned character fields
        if (ccsidToConvertTo == CCSID_OF_JOB) {
          ccsidOfTextBytes = system_.getCcsid();
        }
        else {
          ccsidOfTextBytes = ccsidToConvertTo;
          conv = new Converter(ccsidOfTextBytes, system_);
        }

        byte[] messageInformation = parameters[0].getOutputData();
        ID = conv.byteArrayToString(messageInformation, 26, 7, type).trim();
        
        AS400Message msg = null;
        
        // If we got a message (assuming ID isn't blank), create and populate the
        // message object.  If we didn't get a message, leave the message null.
        if (ID.length() > 0)
        {
            msg = new AS400Message();
            msg.setID(ID);
            msg.setSeverity(BinaryConverter.byteArrayToInt(messageInformation, 8));
            int defaultReplyOffset = BinaryConverter.byteArrayToInt(messageInformation, 52);
            int defaultReplyLength = BinaryConverter.byteArrayToInt(messageInformation, 56);
            int messageOffset = BinaryConverter.byteArrayToInt(messageInformation, 64);
            int messageLength = BinaryConverter.byteArrayToInt(messageInformation, 68);
            int helpOffset = BinaryConverter.byteArrayToInt(messageInformation, 76);
            int helpLength = BinaryConverter.byteArrayToInt(messageInformation, 80);

            msg.setDefaultReply(conv.byteArrayToString(messageInformation, defaultReplyOffset, defaultReplyLength, type));
            msg.setText(conv.byteArrayToString(messageInformation, messageOffset, messageLength, type));
            String helpText = conv.byteArrayToString(messageInformation, helpOffset, helpLength, type);
            if (helpTextFormatting_ == SUBSTITUTE_FORMATTING_CHARACTERS)
            {
                helpText = substituteFormattingCharacters(helpText, AS400BidiTransform.isBidiCcsid(ccsidOfTextBytes));
            }
            msg.setHelp(helpText);
            previousMessageId_ = ID;
        } 

        return msg;
    }

    /**
     Returns the system object representing the system on which the message file exists.
     @return  The system object representing the system on which the message file exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Removes the PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
     @param  listener  The listener object.
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
     @param  listener  The listener object.
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

    // Replace one phrase for another inside a string.
    private static String replaceText(String s, String oldPhrase, String newPhrase)
    {
        int index = s.indexOf(oldPhrase);

        while (index >= 0)
        {
            if (index == 0)
            {
                s = newPhrase + s.substring(3);
            }
            else
            {
                // Copy from beginning of String to location of oldPhrase.
                StringBuffer b = new StringBuffer(s.substring(0, index));
                b.append(newPhrase);
                // Start at the 2nd position after where oldPhrase occurred.
                b.append(s.substring(index + 2));
                s = b.toString();
            }

            index = s.indexOf(oldPhrase);
        }

        return s;
    }

    /**
     Sets the help text formatting value.  Possible values are:
     <ul>
     <li>NO_FORMATTING - the help text is returned as a string of characters.  This is the default.
     <li>RETURN_FORMATTING_CHARACTERS - the help text contains formatting characters.  The formatting characters are:
     <ul>
     <br>&N -- Force a new line.
     <br>&P -- Force a new line and indent the new line six characters.
     <br>&B -- Force a new line and indent the new line four characters.
     </ul>
     <li>SUBSTITUTE_FORMATTING_CHARACTERS - the MessageFile class replaces formatting characters with new line and space characters.
     </ul>
     @param  helpTextFormatting  The help text formatting value.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setHelpTextFormatting(int helpTextFormatting) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting help text formatting:", helpTextFormatting);
        if (helpTextFormatting < NO_FORMATTING || helpTextFormatting > SUBSTITUTE_FORMATTING_CHARACTERS)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'helpTextFormatting' is not valid: " + helpTextFormatting);
            throw new ExtendedIllegalArgumentException("helpTextFormatting (" + helpTextFormatting + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            helpTextFormatting_ = helpTextFormatting;
        }
        else
        {
            Integer oldValue = new Integer(helpTextFormatting_);
            Integer newValue = new Integer(helpTextFormatting);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("helpTextFormatting", oldValue, newValue);
            }
            helpTextFormatting_ = helpTextFormatting;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("helpTextFormatting", oldValue, newValue);
            }
        }
    }

    /**
     Sets the message file name.  The name cannot be changed after retrieving a message from the system.
     @param  path  The integrated file system path name for the message file.  That is, the message file name as a fully qualified path name in the library file system.  The library and message file name must each be 10 characters or less.  The extension for message files is .msgf.  For example, /QSYS.LIB/MYLIB.LIB/MYFILE.MSGF.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setPath(String path) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting path: " + path);
        if (path == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'path' is null.");
            throw new NullPointerException("path");
        }
        // Cannot change the path once we retrieve the first message.
        if (connected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'path' after connect.");
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
        QSYSObjectPathName ifs = new QSYSObjectPathName(path, "MSGF");

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            library_ = ifs.getLibraryName();
            name_ = ifs.getObjectName();
            path_ = path;
        }
        else
        {
            String oldValue = path_;
            String newValue = path;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("path", oldValue, newValue);
            }
            library_ = ifs.getLibraryName();
            name_ = ifs.getObjectName();
            path_ = path;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("path", oldValue, newValue);
            }
        }
    }

    /**
     Sets the system object representing the system on which the message file exists.  The system cannot be changed after retrieving a message from the system.
     @param  system  The system object representing the system on which the message file exists.
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
        // Cannot change the system once we retrieve the first message.
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
