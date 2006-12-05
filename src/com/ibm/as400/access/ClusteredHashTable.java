///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: ClusteredHashTable.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.net.*;
import java.io.*;
import java.util.*;
import java.beans.*;

/**
The ClusteredHashTable class provides access to an i5/OS highly available Clustered Hash Table.
A Clustered Hash Table is represented as an i5/OS server job.
A Clustered Hash Table is a container for small to medium-sized non-persistent data that is
replicated to the Clustered Hash Table domain. The Clustered Hash Table domain is comprised of
nodes defined in a cluster. The Clustered Hash Table domain is defined using the STRCHTSVR CL command.

<p>For further details on a cluster, see the Cluster Resource Services APIs and the Clustering
topic in the i5/OS Information Center.

<p>The purpose of this class is to provide interfaces to the Clustered Hash Table APIs.
An instance of this class can be used to {@link #put put()} and {@link #get get()}
keyed entries from the Clustered Hash Table. The entries stored in the Clustered Hash Table
are replicated to cluster nodes defined in the Clustered Hash Table domain.

<p>A connection to the Clustered Hash Table server is required to access the Clustered Hash Table.
Call {@link #open open()} to obtain the connection. After the open() is complete, entries defined by the
{@link com.ibm.as400.access.ClusteredHashTableEntry ClusteredHashTableEntry} class can be put into the table or retrieved from the table, using the methods put() or get().
A key is required to put() an entry in the Clustered Hash Table.
Use {@link #generateKey generateKey()} to generate a universally unique key. The {@link #elements elements()} method will return a list of all that keys in the clustered hash table. It is recommended to {@link #close close()} the active connection when done to release system resources
that are no longer needed.

<p>This class uses the {@link com.ibm.as400.access.ClusteredHashTableEntry ClusteredHashTableEntry}
class for the get(), elements() and put() methods.

<p>Example Usage:<p>

import com.ibm.as400.access.*;<br>
import java.io.*;<br>
import java.net.*;<br>
import java.util.*;<p>

public class MyFile extends Object<br>
{<br>
    public static void main(String args[]) { <br><br>

	ClusteredHashTableEntry myEntry = null;<br>
	String myData = new String("This is my data");<br><br>

	try{<br>
	    AS400 the400 = new AS400();<br><br>

            // CHTSVR01 is the clustered hash table server name<br>
	    ClusteredHashTable cht = new ClusteredHashTable(the400,"CHTSVR01");<br><br>

	    cht.open();  // make a connection<br><br>

	    byte[] key = null;<br>
   	    key = cht.generateKey();  // get a key to access data with<br><br>

            // key is the key generated to access the data with<br>
            // myData is a byte array of data to be stored<br>
            // 2400 is the time to live in seconds<br>
            // ENTRY_AUTHORITY_ANY_USER indicates any user can access the data<br>
            // DUPLICATE_KEY_FAIL indicates if the key already exists in the hash table to not allow the request to succeed.<br>
	    myEntry = new ClusteredHashTableEntry(key,myData.getBytes(),2400,ClusteredHashTableEntry.ENTRY_AUTHORITY_ANY_USER,ClusteredHashTableEntry.DUPLICATE_KEY_FAIL);<br><br>

           cht.put(myEntry);  // store the entry in the hash table<br><br>

	   ClusteredHashTableEntry output = cht.get(key);  // retrieve the data<br><br>

	  cht.close();<br>
	}<br>
	catch(Exception e){}<br><br>
}<br>
}<p>

Note: This class uses APIs that are available only when connecting to systems running OS/400 V5R2M0 or later.<p>

**/

public class ClusteredHashTable
implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  /**
     Constants
  **/

  static final long serialVersionUID = 5L;

  /**
    Entry status value to limit retrieved entries to only those which are consistent between nodes.
  **/
  public final static int CONSISTENT_ENTRIES = 0;

  /**
    Entry status value to limit retrieved entries to only those which are inconsistent between nodes.
  **/
  public final static int INCONSISTENT_ENTRIES = 1;

  /**
    Entry status value to retrieve all entries on a node.
  **/
  public final static int ALL_ENTRIES = -1;

  // various sizes of C defined structures
  static final int SIZE_OF_CHTS0100 = 36;
  static final int SIZE_OF_CHTI0100 = 24;
  static final int SIZE_OF_QUS_EC_T = 272;

  static final int SIZE_OF_KEY = 16;

  // max amount of data that can be returned
  static final int MAX_DATA_SIZE = 62000;

  /**
    Variables
  ***/
  transient private boolean connected_ = false;

  private String name_;
  private transient byte[] connectionHandle_;
  private AS400 system_;

  private static final Object userSpaceLock_ = new Object();

  transient private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);

  /**
   Constructs a default ClusteredHashTable object.
   The <i>hash table serevr name</i> and <i>system</i> must be set before opening a connection.
  **/
  public ClusteredHashTable()
  {
  }


  /**
   Constructs a ClusteredHashTable object that represents the i5/OS clustered hash table server.
   @param system The system that contains the clustered hash table server.
   @param name The name of an clustered hash table server.
               This is a 10-byte string that identifies the Clustered Hash Table server to use.
  **/
  public ClusteredHashTable(AS400 system, String name)
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    if ((name.length() == 0) || (name.length() > 10))
    {
      throw new ExtendedIllegalArgumentException("name", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    system_ = system;
    name_ = name;
  }


  /**
    Adds a listener to be notified when the value of any bound property is changed.
    @see #removePropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    changes_.addPropertyChangeListener(listener);
  }


  /**
    Verifies the system and name are set.
  **/
  private final void checkPropertiesSet()
  {
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    if (name_ == null)
    {
      throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
  }


  /**
    Closes a connection to the Clustered Hash Table server.
    After this method is called, the clustered hash table server will not allow any more requests
    with the specified connection handle.  Use {@link #open open()} to establish a connection
    to a new Clustered Hash Table server.
    @exception AS400Exception If the system returns an error message.
    @exception AS400SecurityException  If a security or authority error occurs.
    @exception ErrorCompletingRequestException  If an error occurs before the request is completed.
    @exception IOException  If an error occurs while communicating with the system.
    @exception InterruptedException  If this thread is interrupted.
    @exception ObjectDoesNotExistException  If the object does not exist on the system.
  **/
  synchronized public void close() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (connected_)
    {
      try
      {
        // Construct the parameter list.  It contains the single parameter to the service program.
        ProgramParameter[] parameterList = new ProgramParameter[2];

        // input -- cht server handle
        parameterList[0] = new ProgramParameter(connectionHandle_);
        parameterList[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        // i/o
        byte[] errorCode = new byte[SIZE_OF_QUS_EC_T];

        parameterList[1] = new ProgramParameter(errorCode, SIZE_OF_QUS_EC_T);
        parameterList[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        // Construct the ServiceProgramCall object.
        ServiceProgramCall sPGMCall = new ServiceProgramCall(system_);

        // Set the fully qualified service program and the parameter list.
        sPGMCall.setProgram("/QSYS.LIB/QCSTCHT.SRVPGM", parameterList);

        // Set the procedure to call in the service program.
        sPGMCall.setProcedureName("QcstDisconnectCHT");

        // Set the format of returned value.  The program we call returns an integer.
        sPGMCall.setReturnValueFormat(ServiceProgramCall.NO_RETURN_VALUE);

        // Set the call to be thread safe
        sPGMCall.setThreadSafe(true);

        // Call the service program.  If true is returned the program was successfully called.  If
        // false is returned the program could not be started.  A list of messages is returned when
        // the program cannot be started.
        if (!sPGMCall.run())
        {
          // Get the error messages when the call fails.
          AS400Message[] messageList = sPGMCall.getMessageList();
          throw new AS400Exception(messageList);
        }
        else
        {
          // Indicate success.
          connectionHandle_ = null;
          connected_ = false;
        }
      }
      catch (PropertyVetoException pve)
      {
      } // This won't ever happen; just quiet the compiler.
    }
  }


  /**
    Indicates if the specified key is in the clustered hash table. Expired entries will not be included.
    This method implicitly opens the connection to the clustered hash table server.
    <p>Restrictions:
    <ul>
    <li>The clustered hash table server must be active on the system.
    </ul>
    @param key The possible key.
    @return Returns true if and only if the specified key is in this clustered hash table; false otherwise.
    @exception AS400Exception If the system returns an error message.
  **/
  public boolean containsKey(byte[] key) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    // Validate arguments.
    if (key == null)
    {
      throw new NullPointerException("key");
    }

    if (!connected_)
    {
      open();
    }

    // try to get the key, if it fails throw whether it was an unexpected error
    // or the key doesnt exist.
    try
    {
      // dont keep data, its enough just to know data was returned successfully
      get(key);
    }
    catch (AS400Exception e)
    {
      // Get the error messages when the call fails.
      AS400Message[] messageList = e.getAS400MessageList();
      for (int i = 0; i < messageList.length; ++i)
      {
        // check exception message to see if any elements exist
        // If CPFBD06 is returned it indicates the key does not exist in the hash
        // table.  The method should return false rather than fail with an exception
        // All other exceptions are unexpected
        if (messageList[i].getID().startsWith("CPFBD06"))
          return false;
      }

      // the error was not expected
      throw new AS400Exception(messageList);
    }

    // no failing conditions occured
    return true;
  }


  /**
     Retrieves a list of entries that exist in the clustered hash table for the specified user profile. *ALL can be used for either user profile to retrieve all entries.
    This method will create a temporary user space on the system in the QUSRSYS library.  If the user space exists, it will be deleted and recreated.
    This method implicitly opens the connection to the clustered hash table server.
    <p>Restrictions:
    <ul>
    <li>The Clustered Hash table server must be active on the system.
    </ul>
    @return Returns an array of all the ClusteredHashTableEntry objects including keys but not data.
    @exception AS400Exception If the system returns an error message.
  **/
  public ClusteredHashTableEntry[] elements() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    return elements(null,null,ALL_ENTRIES);
  }


  /**
    Retrieves a list of entries that exist in the clustered hash table for the specified user profile. *ALL can be used for either user profile to retrieve all entries. Only entries that match the specified status will be returned. This method will create a temporary user space on the system in the QUSRSYS library.  If the user space exists, it will be deleted and recreated.
    This method implicitly opens the connection to the clustered hash table server.
    <p>Restrictions:
    <ul>
    <li>The Clustered Hash table server must be active on the system.
    </ul>
    @param userProfile The owner of the entries to be returned.
                       If null is specified, *ALL will be used and the results will depend upon the <I>lastModifiedProfile</I>.
                       If both the userProfile and the lastModifiedProfile profiles are null, all the entries in the clustered hash table that meet the criteria specified for the other parameters will be returned.
    @param lastModifiedProfile The most recent modifier of the entries to be returned.
                       If null is specified, *ALL will be used and the returned entries will depend on the value passed in for
                       the <I>userProfile</I>.
    @param status The type of entries to return. Possible values are:
         <ul>
         <li> CONSISTENT_ENTRIES
         <li> INCONSISTENT_ENTRIES
         <li> ALL_ENTRIES
         </ul>
    @return Returns an array of all the ClusteredHashTableEntry objects including keys but not data.
    @exception AS400Exception If the system returns an error message.
    @exception AS400SecurityException  If a security or authority error occurs.
    @exception ErrorCompletingRequestException  If an error occurs before the request is completed.
    @exception IOException  If an error occurs while communicating with the system.
    @exception InterruptedException  If this thread is interrupted.
  **/
  public ClusteredHashTableEntry[] elements(String userProfile, String lastModifiedProfile, int status) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    // verify name lengths, and pad to a length of 10
    // it is ok if either profile is NULL, the NULL case will be handled later
    if (userProfile != null && userProfile.length() > 10)
    {
      throw new ExtendedIllegalArgumentException("userProfile", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    if (lastModifiedProfile != null && lastModifiedProfile.length() > 10)
    {
      throw new ExtendedIllegalArgumentException("lastModifiedProfile", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    // check status
    if ((status != CONSISTENT_ENTRIES) && (status != INCONSISTENT_ENTRIES) && (status != ALL_ENTRIES))
    {
      throw new ExtendedIllegalArgumentException("status", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if (lastModifiedProfile != null && lastModifiedProfile.length() < 10)
    {
      for (int i = lastModifiedProfile.length(); i < 10; ++i)
        lastModifiedProfile += " ";
    }

    if (!connected_)
    {
      open();
    }

    // Validate arguments.
    if (userProfile == null)
    {
      // set to return all entries
      userProfile = "*ALL      ";
    }
    if (lastModifiedProfile == null)
    {
      // set to return all entries
      lastModifiedProfile = "*ALL      ";
    }

    AS400Bin4 bin4 = new AS400Bin4();

    // Construct the parameter list.  It contains the single parameter to the service program.
    ProgramParameter[] parameterList = new ProgramParameter[7];

    // array to return at the end
    ClusteredHashTableEntry[] entries = null;

    // create a user space to work with, dont handle exceptions from this call
    synchronized(userSpaceLock_)
    {
      try
      {
        // Create the user space
        // initial name and path for the userspace
        // Construct the ServiceProgramCall object.
        ServiceProgramCall sPGMCall = new ServiceProgramCall(system_);

        String jobNumber = sPGMCall.getServerJob().getNumber();
        String userSpacePath = "/QSYS.LIB/QUSRSYS.LIB/QCHT"+jobNumber+".USRSPC";
        UserSpace usrSpc = new UserSpace(system_, userSpacePath);
        usrSpc.setMustUseProgramCall(true); // Need this to make sure we use the same job as the ServiceProgramCall

        // create the user space, automatically overwrite any existing one
        byte[] b = new byte[1];  // generic filler
        usrSpc.create(1, true, " ", b[0], "CHT Wrapper Space", "*ALL");
        if (!usrSpc.isAutoExtendible()) usrSpc.setAutoExtendible(true);
        usrSpc.close();

        // set the parameter list
        // input -- Qualified userspace name
        StringBuffer tempName = new StringBuffer(20);
        tempName.append(usrSpc.getName());
        for (int i = usrSpc.getName().length(); i < 10; ++i)  // pad to 10 characters
          tempName.append(' ');
        tempName.append("QUSRSYS");
        AS400Text text20 = new AS400Text(20, system_.getCcsid(), system_);
        parameterList[0] = new ProgramParameter(text20.toBytes(tempName.toString()));
        parameterList[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        // input -- format name
        AS400Text text8 = new AS400Text(8, system_.getCcsid(), system_);
        parameterList[1] = new ProgramParameter(text8.toBytes("CHTL0100"));
        parameterList[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        // input -- cht server connection handle
        parameterList[2] = new ProgramParameter(connectionHandle_);
        parameterList[2].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        // input -- key selection info
        AS400Text text10 = new AS400Text(10, system_.getCcsid(), system_);
        byte[] keyInfo = new byte[SIZE_OF_CHTI0100];
        bin4.toBytes(status, keyInfo, 0);
        text10.toBytes(lastModifiedProfile, keyInfo, 4);
        text10.toBytes(userProfile, keyInfo, 14);

        parameterList[3] = new ProgramParameter(keyInfo);
        parameterList[3].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        // input -- key selection info size
        parameterList[4] = new ProgramParameter(bin4.toBytes(SIZE_OF_CHTI0100));
        parameterList[4].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        // input -- key selection info format
        parameterList[5] = new ProgramParameter(text8.toBytes("CHTI0100"));
        parameterList[5].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        // i/o -- make an array to hold the i/o for the error code
        byte [] errorCode = new byte[SIZE_OF_QUS_EC_T];
        parameterList[6] = new ProgramParameter(errorCode, SIZE_OF_QUS_EC_T);
        parameterList[6].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        // Set the fully qualified service program and the parameter list.
        sPGMCall.setProgram("/QSYS.LIB/QCSTCHT.SRVPGM", parameterList);

        // Set the procedure to call in the service program.
        sPGMCall.setProcedureName("QcstListCHTKeys");

        // Set the format of returned value.  The program we call returns an integer.
        sPGMCall.setReturnValueFormat(ServiceProgramCall.NO_RETURN_VALUE);

        // Set the call to be thread safe
        sPGMCall.setThreadSafe(true);

        // Call the service program.  If true is returned the program was successfully called.  If
        // false is returned the program could not be started.  A list of messages is returned when
        // the program cannot be started.
        if (!sPGMCall.run())
        {
          // Get the error messages when the call fails.
          AS400Message[] messageList = sPGMCall.getMessageList();
          throw new AS400Exception(messageList);
        }

        // attempt to get a list of entries containing only key info

        // declare variabes to get an array of keys
        byte[] num = new byte[4];

        usrSpc.read(num,0x7C); // location of the number of entries in user space
        int startOfData = bin4.toInt(num);

        usrSpc.read(num,0x84); // location of the number of entries in user space
        int numOfEntries = bin4.toInt(num);

        usrSpc.read(num,0x88); // location of the size of entries
        int entrySize = bin4.toInt(num);

        usrSpc.read(num,0x2A0); // offset of key within structure
        int keyOffset = bin4.toInt(num);

        ClusteredHashTableEntry tempEntry = null;
        entries = new ClusteredHashTableEntry[numOfEntries];

        for (int i = 0; i < numOfEntries; ++i)
        {
          byte[] key = new byte[SIZE_OF_KEY];
          usrSpc.read(key,startOfData+keyOffset+entrySize*i); // offset of data + offset to key + number of previous entries @A3

          byte[] bb = new byte[1];  // dummy data
          entries[i] = new ClusteredHashTableEntry(key,bb,60,0,0);
        }

        // clean up left over storage
        usrSpc.close();
        usrSpc.delete();
      }
      catch (PropertyVetoException pve)
      {
      } // Won't ever happen; just quiet the compiler
    }

    // return list of generated entries only key info correct
    return entries;
  }


  /**
    Generates a 16-byte universally unique key.
    This key can be used to put() information in the Clustered Hash Table.
    This method implicitly opens the connection to the clustered hash table server.
    <p>Restrictions:
    <ul>
    <li>The Clustered Hash table server must be active on the system.
    </ul>
    @return The generated key.
    @exception AS400Exception If the system returns an error message.
  **/
  synchronized public byte[] generateKey() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!connected_)
    {
      open();
    }

    AS400Bin4 bin4 = new AS400Bin4();
    byte[] generatedKey = new byte[SIZE_OF_KEY];

    try
    {
      // Construct the parameter list.  It contains the single parameter to the service program.
      ProgramParameter[] parameterList = new ProgramParameter[4];

      // output -- place to put generated key
      parameterList[0] = new ProgramParameter(generatedKey, SIZE_OF_KEY);
      parameterList[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- key data size, must be 16
      parameterList[1] = new ProgramParameter(bin4.toBytes(SIZE_OF_KEY));
      parameterList[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- cht server handle
      parameterList[2] = new ProgramParameter(connectionHandle_);
      parameterList[2].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // i/o
      byte [] errorCode = new byte[SIZE_OF_QUS_EC_T];
      parameterList[3] = new ProgramParameter(errorCode, SIZE_OF_QUS_EC_T);
      parameterList[3].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // Construct the ServiceProgramCall object.
      ServiceProgramCall sPGMCall = new ServiceProgramCall(system_);

      // Set the fully qualified service program and the parameter list.
      sPGMCall.setProgram("/QSYS.LIB/QCSTCHT.SRVPGM", parameterList);

      // Set the procedure to call in the service program.
      sPGMCall.setProcedureName("QcstGenerateCHTKey");

      // Set the format of returned value.  The program we call returns an integer.
      sPGMCall.setReturnValueFormat(ServiceProgramCall.NO_RETURN_VALUE);

      // Set the call to be thread safe
      sPGMCall.setThreadSafe(true);

      // Call the service program.  If true is returned the program was successfully called.  If
      // false is returned the program could not be started.  A list of messages is returned when
      // the program cannot be started.
      if (!sPGMCall.run())
      {
        // Get the error messages when the call fails.
        AS400Message[] messageList = sPGMCall.getMessageList();
        throw new AS400Exception(messageList);
      }
      else
      {
        // Indicate success.
        generatedKey = parameterList[0].getOutputData();
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Successfully generated key:", generatedKey);
      }
    }
    catch (PropertyVetoException pve)
    {
    } // Won't ever happen; just quiet the compiler

    return generatedKey;
  }


  /**
    Returns information from the clustered hash table for the specified key.
    If the entry exists, is not expired, and the requesting user is authorized, the information will be returned.  The time to live and update option parameters can not be retrieved from the hash table and so will be given defaulted values.
  This method implicitly opens the connection to the clustered hash table server.
    <p>Restrictions:
    <ul>
    <li>The Clustered Hash table server must be active on the i5/OS system.
    </ul>
    <p>For information on the authority considerations, see the Clustered Hash Table APIs.
    @param key The key to use to return information.
    @return The entry for the specified key
    @exception AS400Exception If the system returns an error message.
  **/
  synchronized public ClusteredHashTableEntry get(byte[] key) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    // Validate arguments.
    if (key == null)
    {
      throw new NullPointerException("key");
    }

    if (!connected_)
    {
      open();
    }

    AS400Bin4 bin4 = new AS400Bin4();
    byte[] temp = new byte[MAX_DATA_SIZE];

    // Construct the parameter list.  It contains the single parameter to the service program.
    ProgramParameter[] parameterList = new ProgramParameter[7];

    // forward declared since need in both try block and catcher
    AS400Message[] messageList;

    // Construct the ServiceProgramCall object.
    ServiceProgramCall sPGMCall = new ServiceProgramCall(getSystem());

    ClusteredHashTableEntry entry = null;

    try
    {
      // set the parameter list
      // output -- receiver
      parameterList[0] = new ProgramParameter(temp,MAX_DATA_SIZE);
      parameterList[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- receiver length
      parameterList[1] = new ProgramParameter(bin4.toBytes(MAX_DATA_SIZE));
      parameterList[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- cht server handle
      parameterList[2] = new ProgramParameter(connectionHandle_);
      parameterList[2].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- cht format name
      AS400Text text8 = new AS400Text(8, system_.getCcsid(), system_);
      parameterList[3] = new ProgramParameter(text8.toBytes("CHTR0100"));
      parameterList[3].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- cht key length
      parameterList[4] = new ProgramParameter(bin4.toBytes(key.length));
      parameterList[4].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- cht key
      parameterList[5] = new ProgramParameter(key);
      parameterList[5].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // i/o -- make an array to hold the i/o for the error code
      byte [] errorCode = new byte[SIZE_OF_QUS_EC_T];
      parameterList[6] = new ProgramParameter(errorCode, SIZE_OF_QUS_EC_T);
      parameterList[6].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // Set the fully qualified service program and the parameter list.
      sPGMCall.setProgram("/QSYS.LIB/QCSTCHT.SRVPGM", parameterList);

      // Set the procedure to call in the service program.
      sPGMCall.setProcedureName("QcstRetrieveCHTEntry");

      // Set the format of returned value.  The program we call returns an integer.
      sPGMCall.setReturnValueFormat(ServiceProgramCall.NO_RETURN_VALUE);

      // Set the call to be thread safe
      sPGMCall.setThreadSafe(true);

      // Call the service program.  If true is returned the program was successfully called.  If
      // false is returned the program could not be started.  A list of messages is returned when
      // the program cannot be started.
      if (!sPGMCall.run())
      {
        // Get the error messages when the call fails.
        messageList = sPGMCall.getMessageList();
        throw new AS400Exception(messageList);
      }
      else
      {
        // Indicate success.
        temp = parameterList[0].getOutputData();
      }
      // gather and parse all the data from the returned api call
      int length = bin4.toInt(temp, 12);
      int entryStatus = bin4.toInt(temp, 16);  //@A2C
      int authorityAccess = bin4.toInt(temp, 20);

      // the data
      byte[] theData = new byte[length];
      for (int i = 0; i < length; ++i)
      {
        theData[i] = temp[i+44];  //@A1C
      }

      // the owning profile
      byte[] ownerProfile =  new byte[10];  //@A1A
      for (int i = 0; i < 10; ++i)
      {
        ownerProfile[i] = temp[i+24];
      }

      // last modified profile
      byte[] modifyProfile = new byte[10];  //@A1A
      for (int i = 0; i < 10; ++i)
      {
        modifyProfile[i] = temp[i+34];
      }

      // time-to-live and update-option are unneeded at this point
      entry = new ClusteredHashTableEntry(key,theData,60,authorityAccess,0);
      entry.setOwnerProfile(new String(ownerProfile));  //@A1A
      entry.setModifiedProfile(new String(modifyProfile));  //@A1A
      entry.setEntryStatus(entryStatus);  //@A2A

    }
    catch (PropertyVetoException pve)
    {
    } // Won't ever happen; just quiet the compiler

    // no failing conditions occured
    return entry;
  }


  /**
    Returns the name of the clustered hash table connection handle.
    @return Returns the clustered hash table connection handle name as a 16-byte string, or null if it hasn't been set yet.
  **/
  public String getHandle()
  {
      if(connectionHandle_ == null) return null;
      return new String(connectionHandle_);
  }


  /**
    Returns the name of the clustered hash table server.
    @return Returns the clustered hash table server name.
  **/
  public String getName()
  {
    return name_;
  }


  /**
    Returns the system object for the clustered hash table.
     @return The system object for the clustered hash table.
  **/
  public AS400 getSystem()
  {
    return system_;
  }


  /**
  Provided to initialize transient data if this object is de-serialized.
  **/
  private void initializeTransient()
  {
    changes_ = new PropertyChangeSupport(this);

    connected_ = false;
    connectionHandle_ = null;
  }


  /**
    Indicates if the clustered hash table contains any keys.
    Expired entries will not be included for purposes of determining if the hash table is empty.
  This method implicitly opens the connection to the clustered hash table server.
    <p>Restrictions:
    <ul>
    <li>The Clustered Hash table server must be active on the system.
    </ul>
    @return Returns true if the clustered hash table does not contain any keys;
                   false if the clustered hash table contains keys.
    @exception AS400Exception If the system returns an error message.
  **/
  public boolean isEmpty() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!connected_)
    {
      open();
    }

    return size() == 0;
  }


  /**
    Opens a connection to the Clustered Hash Table server.
    The <i>name</i> and <I>system</I> must be set before invoking this method.
    The <i>name</i> and <i>system</i> are committed at this time. Use {@link #close close()} to close
    the connection from the Clustered Hash Table server.
    <p>Restrictions:
    <ul>
    <li>The Clustered Hash table server must be active on the system.
    </ul>
    @exception AS400Exception If the system returns an error message.
  **/
  synchronized public void open() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    // verify required parameters are set
    checkPropertiesSet();

    // ensure not already connected
    if (connected_)
    {
      return;
    }

    try
    {
      // Construct the parameter list.  It contains the single parameter to the service program.
      ProgramParameter[] parameterList = new ProgramParameter[3];

      // set the parameter list
      // output -- place to put generated connection handle
      parameterList[0] = new ProgramParameter(connectionHandle_, 16);
      parameterList[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- cht server to connect to
      AS400Text text10 = new AS400Text(10, system_.getCcsid(), system_);
      parameterList[1] = new ProgramParameter(text10.toBytes(name_));
      parameterList[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // i/o -- make an array to hold the i/o for the error code
      byte [] errorCode = new byte[SIZE_OF_QUS_EC_T];
      parameterList[2] = new ProgramParameter(errorCode, SIZE_OF_QUS_EC_T);
      parameterList[2].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // Construct the ServiceProgramCall object.
      ServiceProgramCall sPGMCall = new ServiceProgramCall(system_);

      // Set the fully qualified service program and the parameter list.
      sPGMCall.setProgram("/QSYS.LIB/QCSTCHT.SRVPGM", parameterList);

      // Set the procedure to call in the service program.
      sPGMCall.setProcedureName("QcstConnectCHT");

      // Set the format of returned value.  The program we call returns an integer.
      sPGMCall.setReturnValueFormat(ServiceProgramCall.NO_RETURN_VALUE);

      // Set the call to be thread safe
      sPGMCall.setThreadSafe(true);

      // Call the service program.  If true is returned the program was successfully called.  If
      // false is returned the program could not be started.  A list of messages is returned when
      // the program cannot be started.
      if (!sPGMCall.run())
      {
        // Get the error messages when the call fails.
        AS400Message[] messageList = sPGMCall.getMessageList();
        throw new AS400Exception(messageList);
      }
      else
      {
        // Indicate success.
        connectionHandle_ = parameterList[0].getOutputData();
        connected_ = true;
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Connection handle is:", connectionHandle_);
      }
    }
    catch (PropertyVetoException pve)
    {
    } // Won't ever happen; just quiet the compiler
  }


  /**
     Put an entry in the clustered hash table identified by the connection handle. The storage for the entry is not persistent. Not persistent means the storage for the entry is only known to the clustered hash table server on the local node and only available until the clustered hash table server is ended.

     <p>This request to store an entry is replicated to other nodes in the clustered hash table domain.  Control will not be returned until the entry is stored in the clustered hash table on all active nodes in the clustered hash table domain.

     <p>There is no encrypting of the information that is replicated and stored in the clustered hash table.

     <p>When an entry is stored, a time to live value is specified. The entry can become expired, when the time to live value has expired. Expired entries will be removed when processing various functions.

     <p>The user that originally stores the entry will be the owner of the entry. The owning user profile will be used in determining authorization to an entry.

     <p>Information stored in the clustered hash table is associated with a key. The key can be generated using the generateKey() method or the user can generate their own.

     <p>Duplicate keys are not supported. An entry associated with an existing key can be updated if the requesting user is the owner of the entry or is authorized to the entry.

    <p>This method implicitly opens the connection to the clustered hash table server.

    @param entry This object describes the information to put in the clustered hash table.
    @exception AS400Exception If the system returns an error message.
  **/
  synchronized public void put(ClusteredHashTableEntry entry) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    // Validate arguments.
    if (entry == null)
    {
      throw new NullPointerException("entry");
    }

    // ensure the properties are set
    checkPropertiesSet();

    // verify generated data is legal with the CHT code
    if (entry.getUpdateOption() != ClusteredHashTableEntry.DUPLICATE_KEY_FAIL && entry.getUpdateOption() != ClusteredHashTableEntry.DUPLICATE_KEY_UPDATE)
    {
      throw new ExtendedIllegalArgumentException("entry", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // connect if necessary
    if (!connected_)
    {
      open();
    }

    AS400Bin4 bin4 = new AS400Bin4();

    // generate the put format CHTS0100
    byte[] theDescription = new byte[SIZE_OF_CHTS0100 + entry.getKey().length + entry.getUserData().length];

    // offset to the key
    int indx = 0;
    byte[] len = bin4.toBytes(SIZE_OF_CHTS0100);
    for (int i = 0; i < 4; ++i)
      theDescription[indx++] = len[i];

    // length of the key
    len = bin4.toBytes(entry.getKey().length);
    for (int i = 0; i < 4; ++i)
      theDescription[indx++] = len[i];

    // offset to the data
    len = bin4.toBytes(SIZE_OF_CHTS0100 + entry.getKey().length);
    for (int i = 0; i < 4; ++i)
      theDescription[indx++] = len[i];

    // length of the data
    len = bin4.toBytes(entry.getUserData().length);
    for (int i = 0; i < 4; ++i)
      theDescription[indx++] = len[i];

    // length and offset of additional fields (each is 4 bytes)
    for (int i = 0; i < 8; ++i)
      theDescription[indx++] = 0x00;

    // update option
    len = bin4.toBytes(entry.getUpdateOption());
    for (int i = 0; i < 4; ++i)
      theDescription[indx++] = len[i];

    // authority access
    len = bin4.toBytes(entry.getEntryAuthority());
    for (int i = 0; i < 4; ++i)
      theDescription[indx++] = len[i];

    // time to live
    len = bin4.toBytes( (entry.getTimeToLive() / 60) );  // convert to minutes
    for (int i = 0; i < 4; ++i)
      theDescription[indx++] = len[i];

    // key
    len = entry.getKey();
    for (int i = 0; i < entry.getKey().length; ++i)
      theDescription[indx++] = len[i];

    // data
    len = entry.getUserData();
    for (int i = 0; i < entry.getUserData().length; ++i)
      theDescription[indx++] = len[i];

    try
    {
      // Construct the parameter list.  It contains the single parameter to the service program.
      ProgramParameter[] parameterList = new ProgramParameter[4];

      // set the parameter list
      // input -- cht server handle
      parameterList[0] = new ProgramParameter(connectionHandle_);
      parameterList[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- cht format
      parameterList[1] = new ProgramParameter(theDescription);
      parameterList[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // input -- store description
      AS400Text text8 = new AS400Text(8, system_.getCcsid(), system_);
      parameterList[2] = new ProgramParameter(text8.toBytes("CHTS0100"));
      parameterList[2].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // i/o -- make an array to hold the i/o for the error code
      byte [] errorCode = new byte[SIZE_OF_QUS_EC_T];
      parameterList[3] = new ProgramParameter(errorCode, SIZE_OF_QUS_EC_T);
      parameterList[3].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

      // Construct the ServiceProgramCall object.
      ServiceProgramCall sPGMCall = new ServiceProgramCall(system_);

      // Set the fully qualified service program and the parameter list.
      sPGMCall.setProgram("/QSYS.LIB/QCSTCHT.SRVPGM", parameterList);

      // Set the procedure to call in the service program.
      sPGMCall.setProcedureName("QcstStoreCHTEntry");

      // Set the format of returned value.  The program we call returns an integer.
      sPGMCall.setReturnValueFormat(ServiceProgramCall.NO_RETURN_VALUE);

      // Set the call to be thread safe
      sPGMCall.setThreadSafe(true);

      // Call the service program.  If true is returned the program was successfully called.  If
      // false is returned the program could not be started.  A list of messages is returned when
      // the program cannot be started.
      if (!sPGMCall.run())
      {
        // Get the error messages when the call fails.
        AS400Message[] messageList = sPGMCall.getMessageList();
        throw new AS400Exception(messageList); // at this point allow program continuation? or throw an exception to the user?
        // We throw an exception because ProgramCall.run() only returns false if one of the messages is an escape message.
      }
    }
    catch (PropertyVetoException pve)
    {
    } // Won't ever happen; just quiet the compiler
  }


  /**
   Restores the state of this object from an object input stream.
   @param ois The stream of state information.
   @exception IOException
   @exception ClassNotFoundException
   **/
  private void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException
  {
    // Restore the non-static and non-transient fields.
    ois.defaultReadObject();

    // Initialize the transient fields.
    initializeTransient();
  }


  /**
    Removes the listener from being notified when a bound property changes.
    @see #addPropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    changes_.removePropertyChangeListener(listener);
  }


  /**
    Sets the name for the clustered hash table server.
    The name can only be set while a connection is not established.
    @param name The name of the clustered hash table server.
    @exception ExtendedIllegalArgumentException If the user specifies a name longer than 10
  **/
  public void setName(String name)
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    if (name.length() > 10)
    {
      throw new ExtendedIllegalArgumentException("name", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    if (connected_)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    String old = name_;
    name_ = name;
    changes_.firePropertyChange("name", old, name_);
  }


  /**
    Sets the node of the clustered hash table.
    The system can only be set while a connection is not established.
    @param system  The system.
  **/
  public void setSystem(AS400 system)
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }
    if (connected_)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    AS400 old = system_;
    system_ = system;
    changes_.firePropertyChange("system", old, system_);
  }


  /**
    Return the number of entries in the clustered hash table. Expired entries will not be included.
  This method implicitly opens the connection to the clustered hash table server.
    <p>Restrictions:
    <ul>
    <li>The Clustered Hash table server must be active on the system.
    </ul>
    @return The number of entries in the clustered hash table.
    @exception AS400Exception If the system returns an error message.
  **/
  public int size()  throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!connected_)
    {
      open();
    }

    ClusteredHashTableEntry[] entriesList = elements();
    // return the number of entries that exist
    return entriesList.length;
  }


}
