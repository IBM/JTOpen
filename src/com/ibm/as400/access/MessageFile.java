///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessageFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.Serializable;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;


/**
  * The MessageFile class
  * allows a user to get a message from an OS/400 message file.
  * It returns an AS400Message object which contains the message.
  * The calling program can optionally supply substitution text for the message.
  *
  * <P>
  * MessageFile will optionally format the message's associated help text.  Three
  * options are available for help text formatting:
  *
  * <OL>
  * <LI>No formatting - the help text is returned as a string of characters.
  * This is the default.
  *
  * <LI>Include formatting characters - the help text contains AS/400
  * formatting characters.  The formatting characters are:
  * <UL>
  * &N -- Force a new line <BR>
  * &P -- Force a new line and indent the new line six characters <BR>
  * &B -- Force a new line and indent the new line four characters
  * </UL>
  *
  * <LI>Substitute formatting characters - the MessageFile class replaces
  * AS/400 formatting characters with newline and space characters.
  * </OL>
  *
  * The difference between options 2 and 3 are with line wrapping.
  * If the formatting characters remain the application can handle line
  * wrapping and indentation.  If the MessageFile class inserts newline and space
  * characters, Java components will handle line wrapping.
  *
  * <P>
  * For example, to retrieve and print a message:
  * <PRE>
  * AS400 system = new AS400("mysystem.mycompany.com");
  * MessageFile messageFile = new MessageFile(system);
  * messageFile.setPath("/QSYS.LIB/QCPFMSG.MSGF");
  * AS400Message message = messageFile.getMessage("CPD0170");
  * System.out.println(message.getText());
  * </PRE>
  *
  *@see AS400Message
  *@see CommandCall
  *@see ProgramCall
  *@see QSYSObjectPathName
  **/

public class MessageFile extends Object implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


    transient private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
    transient private VetoableChangeSupport vetos_   = new VetoableChangeSupport(this);

          // ifsName is the fully qualified name,
          // libString contains only the lib,
          // messageFileString contains only the file name
    private String ifsName_="";
    private String libString_;
    private String messageFileString_;

    private AS400 sys_;

    private boolean connected_ = false;



    /**
      * Constant indicating help text should not be formatted.
      **/
    public static final int NO_FORMATTING = 0;                         //@D1a

    /**
      * Constant indicating formatting characters are left in the help text.
      **/
    public static final int RETURN_FORMATTING_CHARACTERS = 1;          //@D1a

    /**
      * Constant indicating MessageFile should replace formatting
      * characters with newline and space characters.
      **/
    public static final int SUBSTITUTE_FORMATTING_CHARACTERS = 2;      //@D1a

    static final int DEFAULT_FORMATTING = NO_FORMATTING;               //@D5a
    private int helpTextFormatting_     = NO_FORMATTING;               //@D1a

          // Retrieve up to 5K of message info
    private static final int MAX_MESSAGE_SIZE = 5102;                  //@D1a



    /**
      *Constructs a MessageFile object.
      *The system and message file name must be provided later.
    **/
    public MessageFile()
    {
    }





    /**
      *Constructs a MessageFile object. It uses the specified AS/400.
      *The message file name must be provided later.
      *
      *@param  system  The AS/400 which contains the message file.
    **/
    public MessageFile(AS400 system)
    {
        setSystem_(system);
    }





    /**
      *Constructs a message file object. It uses the specified AS/400 and
      *message file name.
      *
      *@param system   The AS/400 which contains the message file
      *@param path     The integrated file system pathname for the message file.
      *                That is, the message file name as a fully qualified path name
      *                in the library file system.
      *                The library and message file name must each be
      *                10 characters or less.  The extension for message files is .msgf.
      *                For example, /QSYS.LIB/MYLIB.LIB/MSGFILE.MSGF.
      **/
    public MessageFile(AS400 system, String path)
    {
        setSystem_(system);
        setPath_(path);
    }





    /**
     * Adds a PropertyChangeListener.
     * The specified PropertyChangeListener's <b>propertyChange</b> method will
     * be called each time the value of any bound property is changed.
     * The PropertyChangeListener object is added to a list of PropertyChangeListeners
     * managed by this MessageFile.  It can be removed with removePropertyChangeListener.
     *
     * @see #removePropertyChangeListener
     * @param listener The PropertyChangeListener.
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        changes_.addPropertyChangeListener(listener);
    }




    /**
     * Adds a VetoableChangeListener.
     * The specified VetoableChangeListener's <b>vetoableChange</b> method will
     * be called each time the value of any constrained property is about to be changed.
     * The VetoableChangeListener object is added to a list of listeners
     * managed by this MessageFile, it can be removed with removeVetoableChangeListener.
     *
     * @see #removeVetoableChangeListener
     * @param listener The VetoableChangeListener.
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        vetos_.addVetoableChangeListener(listener);
    }



// @E1A
    /**
    Substitutes formatting characters with appropriate new line and indent
    characters.  The formatting characters are:
            <UL>
            &N -- Force a new line <BR>
            &P -- Force a new line and indent the new line six characters <BR>
            &B -- Force a new line and indent the new line four characters
            </UL>

    @param sourceText   The source text.
    @return             The formatted text.
    **/
    public static String substituteFormattingCharacters(String sourceText)
    {
        // To preserve behavior, assume not BiDi
        return substituteFormattingCharacters(sourceText, false);       // @D6a 
        // String targetText = sourceText;                              // @D6d
        // targetText = replaceText(targetText, "&N", "\n");            // @D6d
        // targetText = replaceText(targetText, "&P", "\n      ");      // @D6d
        // targetText = replaceText(targetText, "&B", "\n    ");        // @D6d
        // return targetText;                                           // @D6d
    }

                                                                                 
    // @D6 new method                                                                              
    static String substituteFormattingCharacters(String sourceText, boolean bidi)
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
      * Returns the status of help text formatting.  Possible values are:
      * <UL>
      * <LI>NO_FORMATTING - The help text is returned as a string of characters.
      * This is the default.
      * <LI>RETURN_FORMATTING_CHARACTERS - The help text contains AS/400
      * formatting characters.  The formatting characters are:
      * <UL>
      * &N -- Force a new line <BR>
      * &P -- Force a new line and indent the new line six characters <BR>
      * &B -- Force a new line and indent the new line four characters
      * </UL>
      * <LI>SUBSTITUTE_FORMATTING_CHARACTERS - The MessageFile class replaces
      * AS/400 formatting characters with newline and space characters.
      * </UL>
      * @return  The status of help text formatting.
      **/
    public int getHelpTextFormatting()                                 //@D1a
    {                                                                  //@D1a
        return(helpTextFormatting_);                                   //@D1a
    }                                                                  //@D1a





    /**
      *Returns the integrated file system pathname for the message file.
      *It will return null if not previously set.
      *
      *@return  The integrated file system pathname for the program.
      **/
    public String getPath()
    {
        return(ifsName_);
    }




    /**
      *Returns an AS400Message object containing the object.  The system and message file name must be
      *set before calling this method.
      *
      *@param  ID The message identifier.
      *@return An AS400Message object containing the message.
      *
      *@exception AS400SecurityException If a security or authority error occurs.
      *@exception ErrorCompletingRequestException If an error occurs before the request is completed.
      *@exception InterruptedException If this thread is interrupted.
      *@exception IOException If an error occurs while communicating with the AS/400.
      *@exception PropertyVetoException If a change is vetoed.
      *@exception ObjectDoesNotExistException If the AS/400 object does not exist.
      **/
    public AS400Message getMessage(String ID)                         //$D4C
                        throws AS400SecurityException,
                               ErrorCompletingRequestException,
                               InterruptedException,
                               IOException,
                               ObjectDoesNotExistException,
                               PropertyVetoException
    {
       if (sys_ == null)
           throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

       if(AS400BidiTransform.isBidiCcsid(sys_.getCcsid()))
          return getMessage(ID, (byte []) null, AS400BidiTransform.getStringType((char)sys_.getCcsid()));
       else
          return getMessage(ID, (byte []) null, BidiStringType.DEFAULT);
    }


    /**
      *Returns an AS400Message object containing the object.  The system and message file name must be
      *set before calling this method.
      *
      *@param  ID The message identifier.
      *@param type The bidi message string type, as defined by the CDRA (Character
      *            Data Representataion Architecture). See <a href="BidiStringType.html">
      *            BidiStringType</a> for more information and valid values.
      *@return An AS400Message object containing the message.
      *
      *@exception AS400SecurityException If a security or authority error occurs.
      *@exception ErrorCompletingRequestException If an error occurs before the request is completed.
      *@exception InterruptedException If this thread is interrupted.
      *@exception IOException If an error occurs while communicating with the AS/400.
      *@exception PropertyVetoException If a change is vetoed.
      *@exception ObjectDoesNotExistException If the AS/400 object does not exist.
      **/
    public AS400Message getMessage(String ID, int type)               //$D4A
                        throws AS400SecurityException,
                               ErrorCompletingRequestException,
                               InterruptedException,
                               IOException,
                               ObjectDoesNotExistException,
                               PropertyVetoException
    {
        return getMessage(ID, (byte []) null, type);
    }



    /**
      *Returns an AS400Message object containing the message.  The system and message file name must be
      *set before calling this method.  Up to 1024 bytes of substitution
      *text can be supplied to this method.  The calling program is
      *responsible for correctly formatting the string containing the
      *substitution text for the specified message.
      *<P>For example, using
      *AS/400 command DSPMSGD, we see the format of the substitution text for
      *message CPD0170 is char 4, char 10, char 10.  Passing string
      *<PRE>"12  abcd      xyz"</PRE> as the substitution text on this call means
      *"12" will be substituted for &1, "abcd" will be substituted for &2, and
      *"xyz" will be substituted for &3.
      *
      *@param  ID The message identifier.
      *@param  substitutionText The substitution text.
      *@return An AS400Message object containing the message.
      *
      *@exception AS400SecurityException If a security or authority error occurs.
      *@exception ErrorCompletingRequestException If an error occurs before the request is completed.
      *@exception InterruptedException If this thread is interrupted.
      *@exception IOException If an error occurs while communicating with the AS/400.
      *@exception PropertyVetoException If a change is vetoed.
      *@exception ObjectDoesNotExistException If the AS/400 object does not exist.
      **/
    public AS400Message getMessage(String ID, String substitutionText)               //$D4C
                        throws AS400SecurityException,
                               ErrorCompletingRequestException,
                               InterruptedException,
                               IOException,
                               ObjectDoesNotExistException,
                               PropertyVetoException
    {
       if (sys_ == null)
           throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

       if(AS400BidiTransform.isBidiCcsid(sys_.getCcsid()))
          return getMessage(ID, substitutionText, AS400BidiTransform.getStringType((char)sys_.getCcsid()));
       else
          return getMessage(ID, substitutionText, BidiStringType.DEFAULT);
    }


    /**
      *Returns an AS400Message object containing the message.  The system and message file name must be
      *set before calling this method.  Up to 1024 bytes of substitution
      *text can be supplied to this method.  The calling program is
      *responsible for correctly formatting the string containing the
      *substitution text for the specified message.
      *<P>For example, using
      *AS/400 command DSPMSGD, we see the format of the substitution text for
      *message CPD0170 is char 4, char 10, char 10.  Passing string
      *<PRE>"12  abcd      xyz"</PRE> as the substitution text on this call means
      *"12" will be substituted for &1, "abcd" will be substituted for &2, and
      *"xyz" will be substituted for &3.
      *
      *@param  ID The message identifier.
      *@param  substitutionText The substitution text.
      *@param type The bidi message string type, as defined by the CDRA (Character
      *            Data Representataion Architecture). See <a href="BidiStringType.html">
      *            BidiStringType</a> for more information and valid values.
      *@return An AS400Message object containing the message.
      *
      *@exception AS400SecurityException If a security or authority error occurs.
      *@exception ErrorCompletingRequestException If an error occurs before the request is completed.
      *@exception InterruptedException If this thread is interrupted.
      *@exception IOException If an error occurs while communicating with the AS/400.
      *@exception PropertyVetoException If a change is vetoed.
      *@exception ObjectDoesNotExistException If the AS/400 object does not exist.
      **/
    public AS400Message getMessage(String ID, String substitutionText, int type)  //$D4A
                        throws AS400SecurityException,
                               ErrorCompletingRequestException,
                               InterruptedException,
                               IOException,
                               ObjectDoesNotExistException,
                               PropertyVetoException
    {
        if ((ifsName_ == null) || (ifsName_.length() == 0))
           throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (ID == null)
           throw new NullPointerException("ID");

        // @D2 most of this routine was moved into getMessage(id, byte[] text).
        // This routine now converts the string into a byte array and
        // calls the other routine.
        AS400Text text1024Type = new AS400Text(1024, sys_.getCcsid(), sys_);

        byte[] subst;

        if ((substitutionText == null) || (substitutionText.length() == 0))
           subst = new byte[0];
        else
        {  subst = new byte[1024];
           text1024Type.toBytes(substitutionText, subst, type);                              //$D4C
        }

        return getMessage(ID, subst, type);
    }


    /**
      *Returns an AS400Message object containing the message.  The system and
      *message file name must be
      *set before calling this method.  Up to 1024 bytes of substitution
      *text can be supplied to this method.  <B>The byte array is not changed
      *or converted before being sent to the AS/400</B>.
      *
      *@param  ID The message identifier.
      *@param  substitutionText The substitution text.
      *@return An AS400Message object containing the message.
      *
      *@exception AS400SecurityException If a security or authority error occurs.
      *@exception ErrorCompletingRequestException If an error occurs before the request is completed.
      *@exception InterruptedException If this thread is interrupted.
      *@exception IOException If an error occurs while communicating with the AS/400.
      *@exception PropertyVetoException If a change is vetoed.
      *@exception ObjectDoesNotExistException If the AS/400 object does not exist.
      **/
    public AS400Message getMessage(String ID, byte[] substitutionText)         //$D4C
                        throws AS400SecurityException,
                               ErrorCompletingRequestException,
                               InterruptedException,
                               IOException,
                               ObjectDoesNotExistException,
                               PropertyVetoException
    {
       if (sys_ == null)
           throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

       if(AS400BidiTransform.isBidiCcsid(sys_.getCcsid()))
          return getMessage(ID, substitutionText, AS400BidiTransform.getStringType((char)sys_.getCcsid()));
       else
          return getMessage(ID, substitutionText, BidiStringType.DEFAULT);
    }



    /**
      *Returns an AS400Message object containing the message.  The system and
      *message file name must be
      *set before calling this method.  Up to 1024 bytes of substitution
      *text can be supplied to this method.  <B>The byte array is not changed
      *or converted before being sent to the AS/400</B>.
      *
      *@param  ID The message identifier.
      *@param  substitutionText The substitution text.
      *@param type The bidi message string type, as defined by the CDRA (Character
      *            Data Representataion Architecture). See <a href="BidiStringType.html">
      *            BidiStringType</a> for more information and valid values.
      *@return An AS400Message object containing the message.
      *
      *@exception AS400SecurityException If a security or authority error occurs.
      *@exception ErrorCompletingRequestException If an error occurs before the request is completed.
      *@exception InterruptedException If this thread is interrupted.
      *@exception IOException If an error occurs while communicating with the AS/400.
      *@exception PropertyVetoException If a change is vetoed.
      *@exception ObjectDoesNotExistException If the AS/400 object does not exist.
      **/
    public AS400Message getMessage(String ID, byte[] substitutionText, int type)          //$D4A
                        throws AS400SecurityException,                           // @D2a
                               ErrorCompletingRequestException,                  // @D2a
                               InterruptedException,                             // @D2a
                               IOException,                                      // @D2a
                               ObjectDoesNotExistException,                      // @D2a
                               PropertyVetoException                             // @D2a
    {
        if (sys_ == null)                                                        // @D2a
           throw new ExtendedIllegalStateException("system",
                             ExtendedIllegalStateException.PROPERTY_NOT_SET);    // @D2m
                                                                                 // @D2m
        if ((ifsName_ == null) || (ifsName_.length() == 0))                      // @D2m
           throw new ExtendedIllegalStateException("path",
                             ExtendedIllegalStateException.PROPERTY_NOT_SET);    // @D2m
                                                                                 // @D2m
        if (ID == null)                                                          // @D2m
           throw new NullPointerException("ID");                                 // @D2m
                                                                                 // @D2m
               // "connected" is used to prevent changing the system or path     // @D2m
               // after retrieving a message.                                    // @D2m
        connected_ = true;                                                       // @D2m
                                                                                 // @D2m
        ID.toUpperCase();                                                        // @D2m
                                                                                 // @D2m
        AS400Message msg = new AS400Message();                                   // @D2m
        ProgramCall  pgm = new ProgramCall(sys_);                                // @D2m
                                                                                 // @D2m
        AS400Bin4 intType      = new AS400Bin4();                                // @D2m
        int ccsid = sys_.getCcsid(); //@B6A                                      // @D2m
        AS400Text text7Type    = new AS400Text(7, ccsid, sys_); //@B6C           // @D2m
        AS400Text text8Type    = new AS400Text(8, ccsid, sys_); //@B6C           // @D2m
        AS400Text text10Type   = new AS400Text(10, ccsid, sys_); //@B6C          // @D2m
        AS400Text text1024Type = new AS400Text(1024, ccsid, sys_); //@B6C        // @D2m
                                                                                 // @D2m
        byte[] substLen;                                                         // @D2m
        byte[] replace;                                                          // @D2m
                                                                                 // @D2m
        if ((substitutionText == null) ||( substitutionText.length == 0))        // @D2c
        {                                                                        // @D2m
           substLen = new byte[] {0,0,0,0};                                      // @D2m
           replace  = text10Type.toBytes( "*NO" );                               // @D2m
        }                                                                        // @D2m
        else                                                                     // @D2m
        {                                                                        // @D2m
           // @D7d substLen = new byte[] {0,0,4,0};                              // @D2m
           substLen = BinaryConverter.intToByteArray(substitutionText.length);   // @D7a
           replace  = text10Type.toBytes( "*YES" );                              // @D2m
        }                                                                        // @D2m
                                                                                 // @D2m
        ProgramParameter[] parms = new ProgramParameter[10];                     // @D2m
                                                                                 // @D2m
        // 1: create an area to hold the message                                 // @D2m
        parms[0] = new ProgramParameter(MAX_MESSAGE_SIZE);                       // @D2m
                                                                                 // @D2m
        // 2: tell the AS/400 the size of our output buffer                      // @D2m
        byte[] msgsize = intType.toBytes(new Integer(MAX_MESSAGE_SIZE) );        // @D2m
        parms[1] = new ProgramParameter( msgsize );                              // @D2m
                                                                                 // @D2m
        // 3: tell the AS/400 the format of the data we want returned            // @D2m
        byte[] format = new byte[8];                                             // @D2m
        text8Type.toBytes("RTVM0200", format, 0);                                // @D2m
        parms[2] = new ProgramParameter( format );                               // @D2m
                                                                                 // @D2m
        // 4: the message ID                                                     // @D2m
        byte[] msgId = new byte[7];                                              // @D2m
        text7Type.toBytes(ID, msgId, 0);                                         // @D2m
        parms[3] = new ProgramParameter( msgId );                                // @D2m
                                                                                 // @D2m
        // 5: message file(10 chars),  message file library (10 chars)           // @D2m
        byte[] file = new byte[20];                                              // @D2m
        AS400Text text10 = new AS400Text(10, ccsid, sys_); //@B6C                // @D2m
        text10.toBytes(messageFileString_, file, 0, type );                      // @D2m  //$D4C
        text10.toBytes(libString_, file, 10, type );                             // @D2m  //$D4C
        parms[4] = new ProgramParameter( file );                                 // @D2m
                                                                                 // @D2m
        // 6: substitution text if supplied                                      // @D2m
        parms[5] = new ProgramParameter( substitutionText );                     // @D2m
                                                                                 // @D2m
        // 7: length of substitution text                                        // @D2m
        parms[6] = new ProgramParameter( substLen );                             // @D2m
                                                                                 // @D2m
        // 8: replace value                                                      // @D2m
        parms[7] = new ProgramParameter( replace );                              // @D2m
                                                                                 // @D2m
        // 9: set format control chars to *NO                                    // @D2m
        if (helpTextFormatting_ > NO_FORMATTING)                       //@D1a    // @D2m
        {                                                              //@D1a    // @D2m
           byte[] yes = text10Type.toBytes( "*YES" );                  //@D1a    // @D2m
           parms[8]  = new ProgramParameter(yes);                      //@D1a    // @D2m
        }                                                              //@D1a    // @D2m
        else                                                           //@D1a    // @D2m
        {                                                              //@D1a    // @D2m
           byte[] no = text10Type.toBytes( "*NO" );                              // @D2m
           parms[8]  = new ProgramParameter(no);                                 // @D2m
        }                                                              //@D1a    // @D2m
                                                                                 // @D2m
        // 10: error code                                                        // @D2m
        byte [] errorcode = new byte[100];                                       // @D2m
        intType.toBytes( new Integer(0), errorcode, 0 );                         // @D2m
        parms[9] = new ProgramParameter(errorcode, 100);                         // @D2m
                                                                                 // @D2m
        pgm.setThreadSafe(true);  // @B2A
        if (!pgm.run("/QSYS.LIB/QMHRTVM.PGM", parms))                    // @D2m
        {                                                                        // @D2m
//            AS400Message message = pgm.getMessageList()[0];                      // @D2m
//            throw new IOException(message.toStringM2());                         // @D2m @D3c
          throw new AS400Exception(pgm.getMessageList());
        }                                                                        // @D2m
                                                                                 // @D2m
        byte[] retData = parms[0].getOutputData();                               // @D2m
                                                                                 // @D2m
        // get severity out of returned data                                     // @D2m
        msg.setSeverity( BinaryConverter.byteArrayToInt( retData, 8 ) );         // @D2m
                                                                                 // @D2m
        // base pos for variable length strings                                  // @D2m
        int basepos = 52;                                                        // @D2m
                                                                                 // @D2m
        // get reply text with replacement data substitued                       // @D2m
        Integer intval = (Integer)intType.toObject( retData, 28 );               // @D2m
        int replen = intval.intValue();                                          // @D2m
        /*AS400DataType*/ AS400Text repTextType = new AS400Text(replen, ccsid, sys_);    //@B6C   // @D2m   //$D4C
        msg.setDefaultReply((String)repTextType.toObject(retData,basepos, type));                 // @D2m   //$D4C
                                                                                 // @D2m
        // get message text                                                      // @D2m
        intval = (Integer)intType.toObject( retData, 36 );                       // @D2m
        int msglen = intval.intValue();                                          // @D2m
        /*AS400DataType*/ AS400Text msgTextType = new AS400Text(msglen, ccsid, sys_);    //@B6C   // @D2m   //$D4C
        msg.setText((String)msgTextType.toObject(retData, basepos+replen, type));                 // @D2m   //$D4C
                                                                                 // @D2m
        // get help text                                                         // @D2m
        intval = (Integer)intType.toObject( retData, 44 );                       // @D2m
        int helplen = intval.intValue();                                         // @D2m
        /*AS400DataType*/ AS400Text helpTextType = new AS400Text(helplen, ccsid, sys_);        //@B6C // @D2m   //$D4C
        String helpText = (String)helpTextType.toObject(retData, basepos+replen+msglen, type); //@D1C // @D2m  //$D4C
        if (helpTextFormatting_ == SUBSTITUTE_FORMATTING_CHARACTERS)   //@D1a    // @D2m
        {                                                              //@D1a    // @D2m
           // @E1D helpText = replaceText(helpText, "&N", "\n");               //@D1a    // @D2m
           // @E1D helpText = replaceText(helpText, "&P", "\n      ");         //@D1a    // @D2m
           // @E1D helpText = replaceText(helpText, "&B", "\n    ");           //@D1a    // @D2m
           
            boolean bidi = false;                                        // @D6a
            if (sys_ != null)                                            // @D6a
               bidi = AS400BidiTransform.isBidiCcsid(sys_.getCcsid());   // @D6a
            helpText = substituteFormattingCharacters(helpText, bidi);   // @D6c // @E1A  
        }                                                              //@D1a    // @D2m
        msg.setHelp(helpText);                                         //@D1c    // @D2m
                                                                                 // @D2m
        msg.setID(ID);                                                           // @D2m
                                                                                 // @D2m
        return msg;                                                              // @D2m
    }                                                                            // @D2m




    /**
      *Returns the AS/400 which contains the message file.
      *
      *@return        The AS/400 which contains the message file.
      **/
    public AS400 getSystem()
    {
        return(sys_);
    }




    /**
      *Deserializes and initializes the transient data.
      */
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        changes_ = new PropertyChangeSupport(this);
        vetos_ = new VetoableChangeSupport(this);
    }





    /**
     * Removes this PropertyChangeListener from the internal list.
     * If the PropertyChangeListener is not on the list, nothing is done.
     *
     * @see #addPropertyChangeListener
     * @param listener The PropertyChangeListener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        changes_.removePropertyChangeListener(listener);
    }




    /**
     * Removes this VetoableChangeListener from the internal list.
     * If the VetoableChangeListener is not on the list, nothing is done.
     *
     * @see #addVetoableChangeListener
     * @param listener The VetoableChangeListener.
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetos_.removeVetoableChangeListener(listener);
    }




    // replace one phrase for another inside a string

    static String replaceText(String s, String oldPhrase, String newPhrase)       //@D1a @E1C
    {                                                                             //@D1a
       int index = s.indexOf(oldPhrase);                                          //@D1a
                                                                                  //@D1a
       while (index >= 0)                                                         //@D1a
       {                                                                          //@D1a
          if (index == 0)                                                         //@D1a
             s = newPhrase + s.substring(3);                                      //@D1a
          else                                                                    //@D1a
          {                                                                       //@D1a
             // s = s.substring(0, index - 1) + newPhrase + s.substring(index + 3);  //@D1a
             StringBuffer b = new StringBuffer(s.substring(0, index - 1));                          //@D1a
             b.append(newPhrase);                                                 //@D1a
             b.append(s.substring(index + 3));                                    //@D1a
             s = b.toString();                                                    //@D1a
          }                                                                       //@D1a
                                                                                  //@D1a
          index = s.indexOf(oldPhrase);                                           //@D1a
       }                                                                          //@D1a
                                                                                  //@D1a
       return s;                                                                  //@D1a
    }                                                                             //@D1a



    /**
      * Sets the help text formatting value.  Possible values are:
      * <UL>
      * <LI>NO_FORMATTING - the help text is returned as a string of characters.
      * This is the default.
      * <LI>RETURN_FORMATTING_CHARACTERS - the help text contains AS/400
      * formatting characters.  The formatting characters are:
      * <UL>
      * &N -- Force a new line <BR>
      * &P -- Force a new line and indent the new line six characters <BR>
      * &B -- Force a new line and indent the new line four characters
      * </UL>
      * <LI>SUBSTITUTE_FORMATTING_CHARACTERS - the MessageFile class replaces
      * AS/400 formatting characters with new line and space characters.
      * </UL>
      * @param value The help text formatting value.
      * @exception PropertyVetoException If a change is vetoed.
      **/
    public void setHelpTextFormatting(int value)                       //@D1a
        throws PropertyVetoException                                   //@D1a
    {                                                                  //@D1a
                                                                       //@D1a
        if ((value < NO_FORMATTING) ||                                 //@D1a
            (value > SUBSTITUTE_FORMATTING_CHARACTERS))                //@D1a
           throw new ExtendedIllegalArgumentException("helpTextFormatting", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID); //@D1a
                                                                       //@D1a
        int old = helpTextFormatting_;                                 //@D1a
        Integer oldValue = new Integer(old);                           //@D1A
        Integer newValue = new Integer(value);                         //@D1A
                                                                       //@D1a
        vetos_.fireVetoableChange("helpTextFormatting",                //@D1A
                                   oldValue, newValue );               //@D1a
                                                                       //@D1a
        helpTextFormatting_ = value;                                   //@D1a
                                                                       //@D1a
        changes_.firePropertyChange("helpTextFormatting",              //@D1a
                                    oldValue, newValue );              //@D1a
    }                                                                  //@D1a




    /**
      *Sets the message file name.  The name cannot be changed after
      *retrieving a message from the AS/400.
      *
      *@param path  The integrated file system pathname for the message file.
      *             That is, the message file name as a fully qualified path name
      *             in the library file system.
      *             The library and file name must each be
      *             10 characters or less.  The extension for message files is .msgf.
      *             For example, /QSYS.LIB/MyLib.LIB/MyFile.MSGF.
      *@exception PropertyVetoException If a change is vetoed.
      **/
    public void setPath(String path)
        throws PropertyVetoException
    {
        String old = ifsName_;

        vetos_.fireVetoableChange("path", old, path );

        setPath_(path);

        changes_.firePropertyChange("path", old, path );
    }




    void setPath_(String path)
    {
        if (path == null)
        {
            throw new NullPointerException("path");
        }

               // cannot change the path once we retrieve the first message.
        if (connected_)
        {
            throw new ExtendedIllegalStateException( "path", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED );
        }


        QSYSObjectPathName ifsPath = new QSYSObjectPathName(path, "MSGF");

        ifsName_ = path;
        libString_ = ifsPath.getLibraryName();
        messageFileString_ = ifsPath.getObjectName();
    }




    void setSystem_(AS400 system)
        throws ExtendedIllegalStateException
    {
        if (system == null)
        {
            throw new NullPointerException("system");
        }

               // cannot change the system once we retrieve the first message.
        if (connected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException( "system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED );
        }

        sys_ = system;
    }




    /**
      *Sets the AS/400 which contains the message file. The system cannot be changed after
      *retrieving a message from the AS/400.
      *
      *@param system  The AS/400 which contains the message file.
      *@exception PropertyVetoException If a change is vetoed.
      **/
    public void setSystem(AS400 system)
        throws ExtendedIllegalStateException,
               PropertyVetoException
    {
        AS400 old = sys_;

        vetos_.fireVetoableChange("system", old, system );

        setSystem_(system);

        changes_.firePropertyChange("system", old, system );
    }
}


