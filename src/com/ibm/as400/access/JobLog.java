///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobLog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.access.AS400Message;
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
 * The JobLog class represents an AS/400 job log.  This is used
 * to get a list of messages in a job log or to write messages to a job log.
 *
 * <p>For example:
 * <pre>
 * JobLog log = new JobLog (as400, jobName, jobUser, jobNumber);
 * Enumeration e = log.getMessages ();
 * while (e.hasMoreElements ())
 * {
 *    QueuedMessage message = (QueuedMessage) e.nextElement ();
 *    System.out.println (message.getText ());
 * }
 * </pre>
 *
 * <p>JobLog objects generate the following events:
 * <ul>
 * <li>PropertyChangeEvent
 * </ul>
 *
 * @see QueuedMessage
**/
public class JobLog implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final boolean DEBUG = false;

  private static AS400Bin4    intType = new AS400Bin4();

  // * Properties

  private AS400   as400_;
  private String  jobName_               = "";
  private String  jobNameCasePreserved_  = "";
  private String  userName_              = "";
  private String  userNameCasePreserved_ = "";
  private String  jobNumber_ = "";
  

  transient private JobLogEnumeration lastEnumeration_ = null;

  transient PropertyChangeSupport changes = new PropertyChangeSupport(this);
  transient VetoableChangeSupport vetos = new VetoableChangeSupport(this);
  
  
/**
 * Constructs a JobLog object.  The system, name, user, and number
 * properties need to be set before using any method requiring a
 * connection to the AS/400.
**/
  public JobLog()
  {
  }

/**
 * Constructs a JobLog object.  The name, user, and number properties
 * need to be set before using any method requiring a connection to
 * the AS/400.
 *
 * @param system The AS/400 system.
**/
  public JobLog( AS400 system )
  {
    if (system == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'system' is null.");
      throw new NullPointerException("system");
    }
    this.as400_ = system;
  }

/**
 * Constructs a JobLog object.
 *
 * @param system The AS/400 system.
 * @param name   The job name.
 * @param user   The job user.
 * @param number The job number.
**/
  public JobLog( AS400 system, String name, String user, String number)
  {
    
    if (system == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'system' is null.");
      throw new NullPointerException("system");
    }
    if (name == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'name' is null.");
      throw new NullPointerException("name");
    }
    if (user == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'user' is null.");
      throw new NullPointerException("user");
    }
    if (number == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'number' is null.");
      throw new NullPointerException("number");
    }
    this.as400_                 = system;
    this.jobNameCasePreserved_  = name;
    this.jobName_               = name.toUpperCase();
    this.userNameCasePreserved_ = user;
    this.userName_              = user.toUpperCase();
    this.jobNumber_             = number;
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

private static String getCopyright ()
  {
    return Copyright.copyright;
  }

/**
 * Returns the job name.
 *
 * @return The job name, or "" if none has been set.
**/
  public String getName()
  {
    return jobNameCasePreserved_;
  }

/**
 * Returns the job number.
 *
 * @return The job number, or "" if none has been set.
**/
  public String getNumber()
  {
    return jobNumber_;
  }

/**
 * Returns the number of messages in the list that was most recently
 * retrieved from the AS/400 (the last call to <i>getMessages()</i>).
 *
 * @return The number of messages, or 0 if no list has been retrieved.
**/
  public int getLength()
  {
    if (lastEnumeration_ == null)
        return 0;
    else
        return lastEnumeration_.getLength ();
  }

/**
 * Returns a list of messages in the job log.
 * A valid AS/400 system, job name, user, and number must be provided
 * before this call is made.
 *
 * @return An Enumeration of <i>QueuedMessage</i> objects.
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
  public Enumeration getMessages()
      throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException,
             InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (this.as400_ == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (this.jobName_.equals(""))
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting name.");
      throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (this.jobNumber_.equals(""))
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting number.");
      throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (this.userName_.equals(""))
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting user.");
      throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    ProgramCall pgm = new ProgramCall( this.as400_ );

    ProgramParameter[] parms = new ProgramParameter[7];

    // 1 receiver variable
    parms[0] = new ProgramParameter( 5120 );
    // 2 receiver len
    byte[] msgsize = intType.toBytes(new Integer(5120) );
    parms[1] = new ProgramParameter( msgsize );

    // 3 list information
    parms[2] = new ProgramParameter( 80 );

    // 4 number of records to return
    parms[3] = new ProgramParameter( intType.toBytes(new Integer(13)) );

    // 5 * message selection information
    JobLogParser parser = new JobLogParser ();
    byte[] selinfo = parser.buildSelectionInfo(this.as400_, jobName_, userName_, jobNumber_ );
    parms[4] = new ProgramParameter( selinfo );

    // 6 size of message selection information
    parms[5] = new ProgramParameter( intType.toBytes(new Integer(selinfo.length)) );

    // 7 error code ? inout, char*
    parms[6] = new ProgramParameter( intType.toBytes( new Integer(0) ));

    // do it
    byte[] listInfoData = null;
    byte[] OLJLData = null;
    try
    {
      if (pgm.run( "/QSYS.LIB/QGY.LIB/QGYOLJBL.PGM", parms )==false)
      {
        // error on run
        throw new AS400Exception( pgm.getMessageList() );
      }

      listInfoData = parms[2].getOutputData();
      OLJLData = parms[0].getOutputData();
    }
    catch (PropertyVetoException e)
    {
        // Ignore.
    }

    // Create and return the enumeration.
    lastEnumeration_ = new JobLogEnumeration (as400_, parser, listInfoData,
        OLJLData);
    return lastEnumeration_;
  }


/**
 * Returns the AS/400 system on which the job log exists.
 *
 * @return The AS/400 system on which the job log exists.
**/
  public AS400 getSystem()
  {
    return this.as400_;
  }

/**
 * Returns the user name.
 *
 * @return The user name, or "" if none has been set.
**/
  public String getUser()
  {
    return userNameCasePreserved_;
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
 * Sets the job name.  This takes effect the next time that
 * <i>getMessages()</i> is called.
 *
 * @param name The job name.
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

    String old = this.jobNameCasePreserved_;
    this.vetos.fireVetoableChange("name", old, name );

    jobNameCasePreserved_ = name;
    jobName_              = name.toUpperCase();

    this.changes.firePropertyChange("name", old, name );
  }

/**
 * Sets the job number. This takes effect the next time that
 * <i>getMessages()</i> is called.
 *
 * @param number The job number.
 *               This value cannot be null.
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
 * Sets the AS/400 system on which the job log exists.
 *
 * @param system The AS/400 system on which the job log exists.
 *               This value cannot be null.
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
 * Sets the user name. This takes effect the next time that
 * <i>getMessages()</i> is called.
 *
 * @param user The user name.
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

    String old = this.userNameCasePreserved_;
    this.vetos.fireVetoableChange("user", old, user );

    userNameCasePreserved_ = user;
    userName_              = user.toUpperCase();

    this.changes.firePropertyChange("user", old, user );
  }

/**
 *Writes a program message to the job log for the job in which the program is running.
 *The AS400 system, message id and message type must be set before calling this method.  
 *
 *@param system   The AS400 object that specifies the AS/400 to write the message to. If the AS400 object
 *specifies localhost, the message will be written to the job log of the process this method is called 
 *from.  Otherwise the message will be written to the QZRCSRVS job on the AS/400 specified by the AS400 object.
 *@param id   The message ID.  The message must be in the default message file /QSYS.LIB/QCPFMSG.MSGF.
 *@param type  The message type. Valid types are:
 *<ul>
 *<li>   AS400Message.COMPLETION
 *<li>   AS400Message.DIAGNOSTIC
 *<li>   AS400Message.INFORMATIONAL
 *<li>   AS400Message.ESCAPE
 *</ul>
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
      
public static void writeMessage(AS400 system, String id, int type)
                        throws AS400SecurityException,
                               ErrorCompletingRequestException,
                               InterruptedException,
                               IOException,
                               ObjectDoesNotExistException,
                               AS400Exception
    {
        writeMessage(system, id, type, "/QSYS.LIB/QCPFMSG.MSGF", (byte[])null);
    }
    
/**
 *Writes a program message to the job log for the job in which the program is running.  
 *The AS400 system, message ID, message type and substitution text must be set before calling this method.  
 *
 *@param system   The AS400 object that specifies the AS/400 to write the message to. If the AS400 object
 *specifies localhost, the message will be written to the job log of the process this method is called 
 *from.  Otherwise the message will be written to the QZRCSRVS job on the AS/400 specified by the AS400 object.
 *@param id   The message ID. The message must be in the default message file /QSYS.LIB/QCPFMSG.MSGF.
 *If this is an immediate message the message ID must be blanks.
 *@param type  The message type. Valid types are:
 *<ul>
 *<li>   AS400Message.COMPLETION
 *<li>   AS400Message.DIAGNOSTIC
 *<li>   AS400Message.INFORMATIONAL
 *<li>   AS400Message.ESCAPE
 *</ul>
 *For an immediate message the message type must be AS400Message.INFORMATIONAL.
 *@param substitutionText The message substitution text supplied as a byte array.  The substitution text 
 *can be from 0-32767 bytes for a conventional message and from 1-6000 bytes for an immediate message.
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

public static void writeMessage(AS400 system, String id, int type, byte[] substitutionText)
                        throws AS400SecurityException,
                               ErrorCompletingRequestException,
                               InterruptedException,
                               IOException,
                               ObjectDoesNotExistException,
                               AS400Exception
    {
        writeMessage(system, id, type, "/QSYS.LIB/QCPFMSG.MSGF", substitutionText);
    }

/**
 *Writes a program message to the job log for the job in which the program is running.
 *The AS400 system, message ID, message type and message path must be set before calling this method.  
 *
 *@param system   The AS400 object that specifies the AS/400 to write the message to. If the AS400 object
 *specifies localhost, the message will be written to the job log of the process this method is called 
 *from.  Otherwise the message will be written to the QZRCSRVS job on the AS/400 specified by the AS400 object.
 *@param id   The message ID.  The message ID must be for a message in a QSYS file system message file.
 *@param type  The message type. Valid types are:
 *<ul>
 *<li>   AS400Message.COMPLETION
 *<li>   AS400Message.DIAGNOSTIC
 *<li>   AS400Message.INFORMATIONAL
 *<li>   AS400Message.ESCAPE
 *</ul>
 *@param path   The path must be a valid integrated file system path to a message file in the QSYS 
 *file system.  A path such as /QSYS.LIB/MYLIB.LIB/MYMSG.MSGF can be specified.  *LIBL and *CURLIB are 
 *not accepted.
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

public static void writeMessage(AS400 system, String id, int type, String path)
                        throws AS400SecurityException,
                               ErrorCompletingRequestException,
                               InterruptedException,
                               IOException,
                               ObjectDoesNotExistException,
                               AS400Exception
    {
        writeMessage(system, id, type, path, (byte[])null);
    }
    
/**
 *Writes a program message to the job log for the job in which the program is running. 
 *The AS400 system, message ID, message type, message path and substitution text must be set 
 *before calling this method.  
 *
 *@param system   The AS400 object that specifies the AS/400 to write the message to. If the AS400 object
 *specifies localhost, the message will be written to the job log of the process this method is called 
 *from.  Otherwise the message will be written to the QZRCSRVS job on the AS/400 specified by the AS400 object.
 *@param id   The message ID.  The message ID must be for a message in a QSYS file system message file.
 *If this is an immediate message the message ID must be blanks.
 *@param type  The message type. Valid types are:
 *<ul>
 *<li>   AS400Message.COMPLETION
 *<li>   AS400Message.DIAGNOSTIC
 *<li>   AS400Message.INFORMATIONAL
 *<li>   AS400Message.ESCAPE
 *</ul>
 *For an immediate message the message type must be AS400Message.INFORMATIONAL.
 *@param path   The path must be a valid integrated file system path to a message file in the QSYS 
 *file system.  A path such as /QSYS.LIB/MYLIB.LIB/MYMSG.MSGF can be specified.  *LIBL and *CURLIB are 
 *not accepted. 
 *@param substitutionText The message substitution text supplied as a byte array.  The substitution text 
 *can be from 0-32767 bytes for a conventional message and from 1-6000 bytes for an immediate message.
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

public static void writeMessage(AS400 system, String id, int type, String path, byte[] substitutionText)
                        throws AS400SecurityException,
                               ErrorCompletingRequestException,
                               InterruptedException,
                               IOException,
                               ObjectDoesNotExistException,
                               AS400Exception
{
    if (system == null)
           throw new NullPointerException("system");
        
	if (id == null)
           throw new NullPointerException("id");
        
    if (path == null)
           throw new NullPointerException("path");
      else
        if (path.length() == 0)
           throw new IllegalArgumentException("path");
            
           byte[] substLen = {0,0,0,0 };
    if (substitutionText == null)
            substitutionText = new byte[0]; //substLen remains 0;
        else
            BinaryConverter.intToByteArray(substitutionText.length, substLen, 0);
            //substLen is the length of the substitution or immediate text. 
            
        ProgramCall  pgm = new ProgramCall(system);
        int ccsid = system.getCcsid(); 
       
        ProgramParameter[] parms = new ProgramParameter[9];

        // 1: the message ID
        id = id.toUpperCase();
        byte[] msgId = new byte[7];
        AS400Text text7Type = new AS400Text(7, ccsid, system); 
        text7Type.toBytes(id, msgId, 0 );
        parms[0] = new ProgramParameter( msgId );

        // 2: From path get message file(10 chars)and message file library (10 chars).
        byte[] file = new byte[20];
        String messageFileString = "";
        String libString = "";
        QSYSObjectPathName ifsPath = new QSYSObjectPathName(path, "MSGF"); 
        libString = ifsPath.getLibraryName();
        messageFileString = ifsPath.getObjectName();
        AS400Text text10 = new AS400Text(10, ccsid, system);
        text10.toBytes(messageFileString, file, 0);
        text10.toBytes(libString, file, 10);
        parms[1] = new ProgramParameter(file);

        // 3: Substitution text.
        parms[2] = new ProgramParameter(substitutionText);
            
        // 4: Length of substitution text. 0-32767 bytes for a conventional message, 1-6000 bytes for an immediate message.
        parms[3] = new ProgramParameter(substLen);

        // 5: message type(10 chars), verify type is one of supported values if not throw exception.
        text10 = new AS400Text(10, ccsid, system);
        
        byte[] typest = null;
    switch (type)
	{
	    case AS400Message.COMPLETION:
	    typest = text10.toBytes("*COMP");
        break;
        
        case AS400Message.DIAGNOSTIC:
        typest = text10.toBytes("*DIAG");
        break;
        
        case AS400Message.INFORMATIONAL:
        typest = text10.toBytes("*INFO");
        break;
	    
        case AS400Message.ESCAPE:
        typest = text10.toBytes("*ESCAPE");
        break;
        
        default:
        throw new ExtendedIllegalArgumentException("type", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
     } 
        parms[4]  = new ProgramParameter(typest); 
	
        // 6: Call stack entry
        AS400Text text21 = new AS400Text(21, ccsid, system); // This was char 21 in C++, doing the same
        byte[] callste = text21.toBytes("*"); 
        parms[5]  = new ProgramParameter(callste);

        // 7: Call stack counter
        byte[] callcst = new byte[] {0,0,0,0};
        parms[6] = new ProgramParameter(callcst); //This is always 0 input as Binary(4)

        // 8: create an area to hold the returned message key, max size is 4
        parms[7] = new ProgramParameter(4);

        // 9: error code
        byte[] errorcode = new byte[100];
        intType.toBytes(new Integer(0), errorcode, 0);
        parms[8] = new ProgramParameter(errorcode, 100);
      try
      {
        if (pgm.run("/QSYS.LIB/QMHSNDPM.PGM", parms)==false)
            throw new AS400Exception(pgm.getMessageList());
      }        
      catch(PropertyVetoException pve) {} //quiet the compiler  
     }
}




