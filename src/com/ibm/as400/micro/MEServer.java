///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: MEServer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.util.Date;
import java.util.Locale;
import java.beans.PropertyVetoException;

import com.ibm.as400.data.*;
import com.ibm.as400.access.*;

/**
 *  The MEServer class is used to fulfill requests from programs
 *  that are using the ToolboxME for iSeries jar file.  The MEServer is responsible for
 *  creating and invoking methods on Toolbox objects on behalf of the
 *  program.  The MEServer is intended for use when the client
 *  is running on a Tier 0 wireless device.
 *
 *  <p>If there is already a server active on the specified
 *  port, then an MEServer will not be started.
 *
 *  <p>The MEServer can be run as an
 *  application, as follows:
 *
 *  <blockquote>
 *  <pre>
 *  <strong>java com.ibm.as400.micro.MEServer</strong> [ options ]
 *  </pre>
 *  </blockquote>
 *
 *  <p>Options:
 *  <dl>
 *
 *  <dt><b><code>-pcml</b></code> pcml doc1 [;pcml doc2;...]</dt>
 *  <dd>
 *  Specifies the PCML document to pre-load and parse. This option may be abbreviated -pc.<p>
 *
 *  The PCML document will be loaded from the classpath. The classpath will first be searched 
 *  for a serialized document. If a serialized document is not found, the classpath will be searched 
 *  for a PCML source file.<p>
 *
 *  Using this option allows the MEServer to load, parse, and cache the ProgramCallDocument before 
 *  it is used.  When a ToolboxME ProgramCall request is made, the MEServer can than
 *  use the cached ProgramCallDocument to call the program.
 *
 *  If this option is not specified, the PCML will be loaded when the ToolboxME ProgramCall
 *  request is made.  The PCML will be loaded during runtime and will decrease performance.
 *
 *  This option may be abbreviated
 *  <code>-pc</code>.  
 *  </dd>
 *
 *  <dt><b><code>-port</b></code> port</dt>
 *  <dd>
 *  Specifies the port to use for accepting connections from clients. This option may be abbreviated -po.
 *  The default port is 3470.   This option may be abbreviated
 *  <code>-po</code>.  
 *  </dd>
 *  
 *  <dt><b><code>-verbose</b></code> [true|false]</dt>
 *  <dd>
 *  Specifies whether to print status and connection
 *  information to System.out. This option may be abbreviated
 *  <code>-v</code>.  
 *  </dd>
 *
 *  <dt><b><code>-help</b></code></dt>
 *  <dd>
 *  Prints usage information to System.out.  This option may be abbreviated
 *  <code>-h</code> or <code>-?</code>.  The default is not to print usage
 *  information.
 *  </dd>
 *
 *  </dl>
 *  
 *  <p>Example usage:
 *  
 *  <p>Start the server from the command line as follows:
 *  <pre>
 *  java com.ibm.as400.micro.MEServer
 *  </pre>
 **/
public class MEServer implements Runnable
{
    // MRI
    private static final String ME_CONNECTION_ACCEPTED_ = ResourceBundleLoader_m.getText("ME_CONNECTION_ACCEPTED");

    // Private data.
    private Socket socket_;
    //private DataInputStream input_;
    private MicroDataInputStream input_;
    //private DataOutputStream output_;
    private MicroDataOutputStream output_;
    private com.ibm.as400.access.AS400 system_;
    private com.ibm.as400.access.CommandCall cc_;
    private Connection connection_;

    // increment datastream level whenever a client/server datastream change is made.
    private static final int DATASTREAM_LEVEL = 0;  

    private static PrintStream verbose_ = System.out;
    private static boolean verboseState_ = false;

    // Table of ProgramCallDocument objects.
    private static final Hashtable registeredDocuments_ = new Hashtable(); 

    private static int port_;
    private static int threadIndex_ = 0;

    private static final Vector           expectedOptions_  = new Vector();
    private static final Hashtable      shortcuts_             = new Hashtable();

    // These aren't static since they belong to a specific MEServer thread.
    private int nextTransactionID_ = 0x0000;
    private Hashtable cachedJDBCTransactions_ = new Hashtable(); // Table of ResultSets

    static
    {
        // Expected options for the ProxyServer application.
        expectedOptions_.addElement("-port");
        expectedOptions_.addElement("-pcml");
        expectedOptions_.addElement("-verbose");
        expectedOptions_.addElement("-help");

        // Shortcuts for the ProxyServer application.
        // Note: These are also listed in usage().
        shortcuts_.put("-po", "-port");
        shortcuts_.put("-pc", "-pcml");
        shortcuts_.put("-v", "-verbose");
        shortcuts_.put("-h", "-help");
        shortcuts_.put("-?", "-help");
    }


    /**
     *  Constructs an MEServer object.
     **/
    private MEServer(Socket s) throws IOException
    {
        socket_ = s;
        //input_ = new DataInputStream(socket_.getInputStream());
        input_ = new MicroDataInputStream( new BufferedInputStream( socket_.getInputStream() ) );
        //output_ = new DataOutputStream(socket_.getOutputStream());
        output_ = new MicroDataOutputStream( new BufferedOutputStream( socket_.getOutputStream() ) );
    }


    /**
     *  Cache the SQL Result Set.
     **/
    private int cacheTransaction(ResultSet rs)
    {
        Integer key = new Integer(nextTransactionID_++);
        cachedJDBCTransactions_.put(key, rs);

        return key.intValue();
    }


    // From com.ibm.as400.access.BinaryConverter
    static char[] byteArrayToCharArray(byte[] byteValue)
    {
        if (byteValue == null)
            return null;

        char[] charValue = new char[byteValue.length / 2];
        int inPos = 0;
        int outPos = 0;

        while (inPos < byteValue.length)
        {
            charValue[outPos++] = (char)(((byteValue[inPos++] & 0xFF) << 8) + (byteValue[inPos++] & 0xFF));
        }

        return charValue;
    }


    /**
     *  Close the streams and socket associated with this MEServer.
     **/
    private void close()
    {
        try
        {
            if (connection_ != null)
                connection_.close();
        }
        catch (Exception e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error closing MEServer SQL Connection.", e);
        }

        try
        {
            if (system_ != null)
            {
                system_.disconnectAllServices();
                system_ = null;
            }
        }
        catch (Exception e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error disconnecting all services on MEServer.", e);
        }

        system_ = null;

        try
        {
            if (input_ != null)
            {
                input_.in_.close();
                input_ = null;
            }
        }
        catch (Exception e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error closing MEServer input stream.", e);
        }

        try
        {
            if (output_ != null)
            {
                output_.out_.close();
                output_ = null;
            }
        }
        catch (Exception e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error closing MEServer output stream.", e);
        }

        try
        {
            if (socket_ != null)
            {
                socket_.close();
                socket_ = null;

                if (verboseState_)
                    verbose_.println ( ResourceBundleLoader_m.getText ("ME_CONNECTION_CLOSED", Thread.currentThread().getName() ) );
            }
        }
        catch (Exception e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error closing MEServer socket.", e);
        }

        try
        {
            for (Enumeration e = cachedJDBCTransactions_.keys() ; e.hasMoreElements() ;)
            {
                ((AS400JDBCResultSet) cachedJDBCTransactions_.get(e)).close();
                cachedJDBCTransactions_.remove(e);
            }
        }
        catch (Exception e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error closing SQL Result Set.", e);
        }
    }
    
    
    // Unscramble some bytes.
    private static byte[] decode(byte[] adder, byte[] mask, byte[] bytes)
    {
        int length = bytes.length;
        byte[] buf = new byte[length];
        
        for (int i = 0; i < length; ++i)
        {
            buf[i] = (byte)(mask[i % mask.length] ^ bytes[i]);
        }

        for (int i = 0; i < length; ++i)
        {
            buf[i] = (byte)(buf[i] - adder[i % adder.length]);
        }

        return buf;
    }



    /**
     * Receives the command call datastream from the client, runs the command, and sends a reply.
     *
     * Datastream request format:
     *  String - command string including CL command name and all of its parameters
     *
     * Datastream reply format:
     *  int - number of messages; 0 if there are no messages.
     *  String[] - messages, consisting of CPF ID and message text
     **/
    private void doCommandCall() throws ErrorCompletingRequestException, InterruptedException, IOException
    {
        String command = input_.readUTF();

        if (cc_ == null)
            cc_ = new CommandCall(system_);

        boolean retVal = false;

        try
        {
            retVal = cc_.run(command);
        }
        catch (PropertyVetoException pve)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "PropertyVetoException on run(command)", pve);
        }
        catch (Exception e)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(e);

            return;
        }
        
        int numReplies = 0;
        AS400Message[] messages = cc_.getMessageList();

        // Only failed messages are sent back to the client.
        if (!retVal)
            numReplies = messages.length;

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, "Number of messages returned from ToolboxME CommandCall: " + numReplies);

        output_.writeInt(numReplies);

        for (int i=0; i<numReplies; ++i)
        {
            output_.writeUTF(messages[i].getID() + ": " + messages[i].getText());

            if (Trace.isTraceOn())
                Trace.log(Trace.DIAGNOSTIC, messages[i].getText());
        }

        output_.flush();
    }


    /**
     * Receives the data queue read datastream from the client, reads the data queue, and sends back the entry.
     *
     * Datastream request format:
     *  String - data queue name
     *
     * Datastream reply format:
     *  int - data queue status (e.g. data queue exists, etc.)
     *
     * And if it exists:
     *  String - value of entry read from data queue
     **/
    private void doDataQueueRead() throws IOException, InterruptedException
    {
        String queueName = input_.readUTF();
        int dataType = input_.readInt();

        DataQueue dq = new DataQueue(system_, queueName);
        DataQueueEntry entry = null;

        try
        {
            entry = dq.read();
            output_.writeInt(MEConstants.DATA_QUEUE_ACTION_SUCCESSFUL);
        }
        catch (AS400SecurityException ase)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ase);

            return;
        }
        catch (ErrorCompletingRequestException ecr)                                                         
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ecr);

            return;
        }
        catch (IOException ioe)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ioe);

            return;
        }
        catch (IllegalObjectTypeException iote)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(iote);

            return;
        }
        catch (ObjectDoesNotExistException odnee)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(odnee);

            return;
        }

        if (dataType == MEConstants.DATA_QUEUE_STRING)
        {
            output_.writeUTF(entry == null ? "" : entry.getString());
        }
        else if (dataType == MEConstants.DATA_QUEUE_BYTES)
        {
            if (entry == null)
                output_.writeInt(0);
            else
            {
                byte[]  b = entry.getData();
                output_.writeInt(b.length);
                output_.writeBytes(b);
            } 
        }

        output_.flush();
    }


    /**
     * Receives the data queue write datastream from the client and writes to the data queue.
     *
     * Datastream request format:
     *  String - data queue name
     *  String - entry data to write
     *
     * Datastream reply format:
     *  int - data queue status (e.g. data queue exists, etc.)
     **/
    private void doDataQueueWrite() throws IOException, InterruptedException
    {
        String queueName = input_.readUTF();
        int dataType = input_.readInt();

        DataQueue dq = new DataQueue(system_, queueName);

        try
        {
            if (dataType == MEConstants.DATA_QUEUE_BYTES)
            {
                int len = input_.readInt();
                byte[] data = new byte[len];

                input_.readBytes(data);

                dq.write(data);
            }
            else if (dataType == MEConstants.DATA_QUEUE_STRING)
            {
                String data = input_.readUTF();
                dq.write(data);
            }
        }
        catch (ErrorCompletingRequestException ecr)                                                         
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ecr);

            return;
        }
        catch (ExtendedIllegalArgumentException ecr)                                                         
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ecr);

            return;
        }
        catch (AS400SecurityException ase)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ase);

            return;
        }
        catch (IOException ioe)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ioe);

            return;
        }
        catch (IllegalObjectTypeException iote)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(iote);

            return;
        }
        catch (ObjectDoesNotExistException odnee)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(odnee);

            return;
        }

        output_.writeInt(MEConstants.DATA_QUEUE_ACTION_SUCCESSFUL);
        output_.flush();
    }


    /**
     * Receives the program call datastream from the client, sets parameters, calls the program, and sends parameter values on the reply.
     *
     * Datastream request format:
     *  String - API name
     *  int - number of parameters and values to set before the API is called
     *  String[][2] - array of { PCML tag name, value } strings to set
     *  int - number of parameters to retrieve after the API is called
     *  String[] - PCML tag names of parameters to retrieve
     *
     * Datastream reply format:
     *  int - program call status (e.g. program not registered, program successfully called, etc.)
     *
     * And if program is registered:
     *  boolean - result of callProgram()
     *  String[] - values of previously specified PCML tag names to retrieve
     **/
    private void doProgramCall() throws IOException, ErrorCompletingRequestException, InterruptedException 
    {
        String pcmlName = input_.readUTF();
        String apiName = input_.readUTF();

        // Read in the number of parameters the user is setting.
        int numParmsToSet = input_.readInt(); 
        
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, "Number of PCML input Parms:" + numParmsToSet);

        // initialize string arrays used to set the pcml values.
        String[] tagNamesToSet = new String[numParmsToSet];
        String[] valuesToSet = new String[numParmsToSet];

        // Read in each parameter name and value.
        for (int i=0; i<numParmsToSet; ++i)
        {
            tagNamesToSet[i] = input_.readUTF();
            valuesToSet[i] = input_.readUTF();

            if (Trace.isTraceOn())
                Trace.log(Trace.PROXY, "ToolboxME PCML input Parm[" + i + "] " + tagNamesToSet[i] + ": " + valuesToSet[i]);
        }

        // Read in the number of parameters to get or return to caller.
        int numParmsToGet = input_.readInt(); 
        String[] tagNamesToGet = new String[numParmsToGet];

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, "Number of PCML output Parms:" + numParmsToGet);
        // Read in each parameter name to get.
        for (int i=0; i<numParmsToGet; ++i)
        {
            tagNamesToGet[i] = input_.readUTF();
        }

        ProgramCallDocument pc = loadDocument(pcmlName);

        // loadDocument() will send any error information to the client.
        if (pc == null)
            return;

        output_.writeInt(MEConstants.XML_DOCUMENT_REGISTERED);
        output_.flush();

        boolean retVal = false;

        try
        {
            pc.setSystem(system_);

            // For each parameter name/value pair, set the pcml string value.
            for (int i=0; i<numParmsToSet; ++i)
            {
                pc.setStringValue(tagNamesToSet[i], valuesToSet[i], BidiStringType.DEFAULT);
            }

            // Call the program.
            retVal = pc.callProgram(apiName);

            // Get output parameters.
            String[] values = null;
            if (retVal)
            {
                values = new String[numParmsToGet];
                for (int i=0; i<numParmsToGet; ++i)
                {
                    values[i] = pc.getStringValue(tagNamesToGet[i], BidiStringType.DEFAULT);                

                    if (Trace.isTraceOn())
                        Trace.log(Trace.PROXY, "ToolboxME PCML ouput Parm[" + i + "] " + tagNamesToGet[i] + ": " + values[i]);
                }
            }
            output_.writeBoolean(retVal);
            output_.flush();

            if (!retVal)
            {
                AS400Message[] messages = pc.getMessageList(apiName);

                if (Trace.isTraceOn())
                {
                    for (int j=0; j<messages.length; ++j)
                    {
                        Trace.log(Trace.ERROR, messages[j].getText());
                    }
                }

                output_.writeInt(MEException.PROGRAM_FAILED);
                output_.writeUTF(messages[0].getID()+": "+messages[0].getText());
                output_.flush();
            }
            else
            {
                for (int j=0; j<numParmsToGet; ++j)
                {
                    output_.writeUTF(values[j] == null ? "" : values[j]);
                    output_.flush();
                }
            }
        }
        catch (PcmlException pe)
        {
            Exception e2;
            if ( (e2 = ((PcmlException) pe).getException()) != null)
            {
                verbose_.println( e2.getMessage() );
                output_.writeBoolean(false);
                sendException(e2);
            }
            else
            {
                if (verboseState_)
                    verbose_.println( pe.getLocalizedMessage() );
                sendException( pe );
            }
            
            return;
        }
    }


    /**
     *  Add a new ProgramCallDocument to the cache for each file if one
     *  does not already exist.
     **/
    private static ProgramCallDocument getPCMLDocument(String pcml) throws PcmlException
    {
        ProgramCallDocument prog = (ProgramCallDocument)registeredDocuments_.get(pcml);

        if (prog != null)
        {
            if (verboseState_)
                verbose_.println( ResourceBundleLoader_m.getText("ME_PCML_CACHE", pcml) );
            return prog;
        }

        if (verboseState_)
            verbose_.println( ResourceBundleLoader_m.getText("ME_PCML_LOADING", pcml) );

        prog = new ProgramCallDocument();
        prog.setDocument(pcml);

        registeredDocuments_.put(pcml, prog);

        return prog;
    }


    /**
     *  Load a ProgramCallDocument.
     **/
    private ProgramCallDocument loadDocument(String pcml)
    {
        try
        {
            return getPCMLDocument(pcml);
        }
        catch (PcmlException pe)
        {
            verbose_.println( ResourceBundleLoader_m.getText("ME_PCML_ERROR") );

            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, pe);

            if (output_ != null)
            {
                try
                {
                    output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
                    output_.writeInt(MEException.PROGRAM_NOT_REGISTERED);
                    output_.writeUTF(pe.getLocalizedMessage());
                    output_.flush();
                }
                catch (Exception e)
                {
                    if (Trace.isTraceErrorOn ())
                        Trace.log (Trace.ERROR, "Error returning exception, from PCML load, to ME client.", e);
                }
            }

            return null;
        }
        catch (NoClassDefFoundError n)
        {
            if (pcml == null)
                verbose_.println( ResourceBundleLoader_m.getText("ME_PCML_ERROR") );

            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, n);

            if (output_ != null)
            {
                try
                {
                    output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
                    output_.writeInt(MEException.PROGRAM_NOT_REGISTERED);
                    output_.writeUTF( n.getClass().getName()+ ": " + n.getMessage() );
                    output_.flush();
                }
                catch (Exception e)                                                                                                                     // UPDATED
                {
                    if (Trace.isTraceErrorOn ())
                        Trace.log (Trace.ERROR, "Error returning exception, from PCML load, to ME client.", e);
                }
            }

            return null;
        }
        catch (MissingResourceException mre)          
        {
            if (pcml == null)
                verbose_.println( ResourceBundleLoader_m.getText("ME_PCML_ERROR") );

            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, mre);

            if (output_ != null)
            {
                try
                {
                    output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
                    if (pcml.length() == 0)
                        output_.writeInt(MEException.PARAMETER_VALUE_NOT_VALID);
                    else
                        output_.writeInt(MEException.PROGRAM_NOT_REGISTERED);
                    output_.writeUTF( mre.getClass().getName()+ ": " + mre.getMessage() );
                    output_.flush();
                }
                catch (Exception e)                                                                                                                         // UPDATED
                {
                    if (Trace.isTraceErrorOn ())
                        Trace.log (Trace.ERROR, "Error returning exception, from PCML load, to ME client.", e);
                }
            }

            return null;
        }
    }


    /**
     *  Runs the MEServer as an application.
     *
     *  @param args The command line arguments.
     **/
    public static void main(String[] args)
    {
        try
        {
            if ( parseArgs(args) )
            {
                ServerSocket ss = new ServerSocket(port_);

                verbose_.println (ResourceBundleLoader_m.getText("ME_SERVER_STARTED"));

                if (verboseState_)
                    verbose_.println( ResourceBundleLoader_m.getText("ME_SERVER_LISTENING", ResourceBundleLoader_m.getText("ME_SERVER_CONTAINER"), Integer.toString (port_)) );

                while (true)
                {
                    Socket s = ss.accept();

                    MEServer server = new MEServer(s);

                    Thread t = new Thread(server, "ToolboxMEThread-"+(threadIndex_++));
                    t.setDaemon(true);
                    t.start();

                    if (verboseState_)
                        verbose_.println( ResourceBundleLoader_m.substitute(ME_CONNECTION_ACCEPTED_, new Object[] { "MEServer", s.getInetAddress().toString(), t.getName()} ));
                }
            }
            else
            {
                usage (System.out);
            }
        }
        catch (BindException e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error opening ToolboxME server socket.", e);

            verbose_.println( ResourceBundleLoader_m.getText("ME_ALREADY_LISTENING", Integer.toString(port_)) );
        }
        catch (IOException e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error opening ToolboxME server socket.", e);
        }
    }


    // Randomly generate bytes and put them into a new byte array.
    static byte[] nextBytes(Random r, int numBytes)
    {
        byte[] buf = new byte[numBytes];
        long feeder = r.nextLong();
        int feederIndex = 0;  // We will step through the bytes of the long, 1 byte at a time.

        for (int i=0; i<buf.length; i++)
        {
            if (feederIndex > 7)  // time to grab another long
            {
                feeder = r.nextLong();
                feederIndex = 0;
            }

            buf[i] = (byte)(0xFF & (feeder >> 8*feederIndex++));
        }

        return buf;
    }


    /**
     *  Parses the command line arguments and sets the properties accordingly.
     *
     *  @param args     The command line arguments.
     *  @return         true if the combination of command line arguments is valid, false otherwise.
     **/
    private static boolean parseArgs (String[] args)
    {
        String optionValue;

        CommandLineArguments cla = new CommandLineArguments (args, expectedOptions_ , shortcuts_);

        if (cla.getOptionValue ("-help") != null)
            return false;

        optionValue = cla.getOptionValue("-verbose");
        if (optionValue != null)
        {
            if ((optionValue.length () == 0) || (optionValue.equalsIgnoreCase ("true")))
                verboseState_ = true;
            else if (optionValue.equalsIgnoreCase ("false"))
                verboseState_ = false;

            else
                throw new IllegalArgumentException( ResourceBundleLoader_m.getText("ME_OPTION_VALUE_NOT_VALID", new String[] { "verbose", optionValue}) );
        }

        optionValue = cla.getOptionValue ("-pcml");
        if (optionValue != null)
        {
            StringTokenizer msc = new StringTokenizer(optionValue, ";");
            while (msc.hasMoreTokens())
            {
                try
                {
                    getPCMLDocument(msc.nextToken());
                }
                catch (PcmlException pe)
                {
                    verbose_.println( ResourceBundleLoader_m.getText("ME_PCML_ERROR") );

                    if (verboseState_)
                        verbose_.println( pe.getMessage() );

                    if (Trace.isTraceOn())
                        Trace.log(Trace.ERROR, pe);
                }
            }
        }

        optionValue = cla.getOptionValue ("-port");
        if (optionValue != null)
        {
            if (optionValue.length() > 0)
                port_ = Integer.parseInt (optionValue);
        }
        else
            port_ = MEConstants.ME_SERVER_PORT;

        // Extra options.
        Enumeration enum = cla.getExtraOptions ();
        while (enum.hasMoreElements ())
        {
            String extraOption = enum.nextElement().toString();
            verbose_.println (ResourceBundleLoader_m.getText ("ME_OPTION_NOT_VALID", extraOption));
        }

        return true;
    }


     // Get clear password bytes back.
    private static String resolve(byte[] info)
    {
        byte[] adder = new byte[MEConstants.ADDER_LENGTH];
        System.arraycopy(info, 0, adder, 0, MEConstants.ADDER_LENGTH);

        byte[] mask = new byte[MEConstants.MASK_LENGTH];
        System.arraycopy(info, MEConstants.ADDER_LENGTH, mask, 0, MEConstants.MASK_LENGTH);

        byte[] infoBytes = new byte[info.length - MEConstants.ADDER_PLUS_MASK_LENGTH];
        System.arraycopy(info, MEConstants.ADDER_PLUS_MASK_LENGTH, infoBytes, 0, info.length - MEConstants.ADDER_PLUS_MASK_LENGTH);

        return new String(byteArrayToCharArray(decode(adder, mask, infoBytes)));
    }



    /**
     *  Process the datastream from the Tier 0 device.
     **/
    public void run()
    {
        boolean jdbc = false;
        try
        {
            boolean disconnected = false;

            // If the client disconnects or is performing JdbcMe functions, then
            // we exit this Thread and continue to listen for connections on the
            // Server socket.
            while (!disconnected & !jdbc)
            {
                // Process next datastream from client.
                int cmd = input_.readInt();

                if (Trace.isTraceOn())
                    Trace.log(Trace.PROXY, "ME Datastream Request: " + Integer.toHexString(cmd));

                switch (cmd)
                {
                case MEConstants.SIGNON:
                    signon();
                    break;
                case MEConstants.COMMAND_CALL:
                    doCommandCall();
                    break;
                case MEConstants.PROGRAM_CALL:
                    doProgramCall();
                    break;
                case MEConstants.DATA_QUEUE_READ:
                    doDataQueueRead();
                    break;
                case MEConstants.DATA_QUEUE_WRITE:
                    doDataQueueWrite();
                    break;
                case MEConstants.CONN_CLOSE:
                case MEConstants.CONN_COMMIT:
                case MEConstants.CONN_CREATE_STATEMENT:
                case MEConstants.CONN_CREATE_STATEMENT2:
                case MEConstants.CONN_NEW:
                case MEConstants.CONN_PREPARE_STATEMENT:
                case MEConstants.CONN_ROLLBACK:
                case MEConstants.CONN_SET_AUTOCOMMIT:
                case MEConstants.CONN_SET_TRANSACTION_ISOLATION:
                case MEConstants.STMT_CLOSE:
                case MEConstants.STMT_EXECUTE:
                case MEConstants.STMT_GET_RESULT_SET:
                case MEConstants.STMT_GET_UPDATE_COUNT:
                case MEConstants.PREP_EXECUTE:
                case MEConstants.RS_ABSOLUTE:
                case MEConstants.RS_AFTER_LAST:
                case MEConstants.RS_BEFORE_FIRST:
                case MEConstants.RS_CLOSE:
                case MEConstants.RS_DELETE_ROW:
                case MEConstants.RS_FIRST:
                case MEConstants.RS_INSERT_ROW:
                case MEConstants.RS_IS_AFTER_LAST:
                case MEConstants.RS_IS_BEFORE_FIRST:
                case MEConstants.RS_IS_FIRST:
                case MEConstants.RS_IS_LAST:
                case MEConstants.RS_LAST:
                case MEConstants.RS_NEXT:
                case MEConstants.RS_PREVIOUS:
                case MEConstants.RS_RELATIVE:
                case MEConstants.RS_UPDATE_ROW:
                    ClientHandler jdbcHandler = new ClientHandler(socket_, input_, output_, cmd);
                    jdbcHandler.start();
                    jdbc = true;
                    break;
                case MEConstants.DISCONNECT:
                    if (Trace.isTraceOn())
                        Trace.log(Trace.PROXY, "ToolboxME disconnect received.");
                    close();
                    disconnected = true;
                    break;
                default:
                    output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
                    output_.writeInt(MEConstants.REQUEST_NOT_SUPPORTED);
                    output_.writeUTF("ToolboxME Request not supported: " +  Integer.toHexString(cmd) );
                    output_.flush();
                }
            }
        }
        catch (Exception e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, e);

            stop(e.getMessage());
        }

        // We don't want to close the socket if we are performing JDBC since we
        // hand it off to the JdbcMe ClientHandler class.
        if (!jdbc)
            close();
    }


    /**
     * Receives the signon datastream from the client, connects to the iSeries, and sends a reply.
     *
     * Datastream request format:
     *  String - server name
     *  String - user profile
     *  String - password
     *
     * Datastream reply format:
     *  int - signon return code (e.g. failed, succeeded, etc.)
    **/
    private void signon() throws IOException
    {
        int clientDataStreamLevel = input_.readInt();

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, "Micro client datastream level: " + clientDataStreamLevel);

        byte[] proxySeed = new byte[MEConstants.ADDER_LENGTH];

        input_.readBytes(proxySeed);         

        // Tell them what our datastream level is.
        output_.writeInt(DATASTREAM_LEVEL);
        
        // Generate, hold, and send them our seed.
        byte[] remoteSeed = nextBytes(new Random(), MEConstants.MASK_LENGTH);
        output_.writeBytes(remoteSeed);
        output_.flush();
        
        String serverName = input_.readUTF();
        String userid = input_.readUTF();

        int numBytes = input_.readInt();  // Get length of buffer to allocate.
        byte[] bytes = new byte[numBytes];

        input_.readBytes(bytes); // Note: This value is both twiddled and encoded.            
        
        // Disconnect if we were previously signed on.
        // We only allow one AS400 object per thread/connection.
        if (system_ != null)
            system_.disconnectAllServices();

        boolean retVal = false;

        try
        {
            system_ = new com.ibm.as400.access.AS400(serverName, userid, resolve(decode(proxySeed, remoteSeed, bytes)));

            // Wireless devices don't have NLS support in the KVM yet.
            // So we only want to display english messages.
            system_.setLocale(Locale.US);

            retVal = system_.validateSignon();
        }
        catch (Exception e)
        {
            system_ = null;

            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(e);

            return;
        }

        int retCode = MEConstants.SIGNON_SUCCEEDED;

        if (!retVal)
        {
            system_ = null;
            retCode = MEConstants.SIGNON_FAILED;
        }

        // We can differentiate more return codes here later on.

        output_.writeInt(retCode);
        output_.flush();
    }


    /**
     *  Stop the ME server.
     **/
    private final void stop(String failure)
    {
        try
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.writeUTF(failure);
        }
        catch (Exception e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error returning exception, during stop, to ME client.", e);
        }

        try
        {
            output_.flush();
        }
        catch (Exception e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error flushing output stream during MEServer stop.", e);
        }

        close();
    }


    /**
     *  Map a Toolbox exception constant to the corresponding MEException constant and
     *  send the text message of that exception to the client.
     *
     *  @param e The exception.
     **/
    private void sendException(Exception e) throws IOException
    {
        if ( Trace.isTraceOn() )
            Trace.log(Trace.PROXY, "ME Exception Occurred: \n", e);

        // Determine what the exception was and then map it
        // the the MEException return code and send it back
        // to the Tier 0 client.
        if (e instanceof AS400SecurityException)
        {
            int rc = ((AS400SecurityException)e).getReturnCode();

            if (Trace.isTraceOn())
                Trace.log(Trace.DIAGNOSTIC, "Exception return code: " + rc);

            switch (rc)
            {
            case AS400SecurityException.PASSWORD_ERROR:
                output_.writeInt(MEException.PASSWORD_ERROR);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.PASSWORD_EXPIRED:
                output_.writeInt(MEException.PASSWORD_EXPIRED);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.PASSWORD_INCORRECT:
                output_.writeInt(MEException.PASSWORD_INCORRECT);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.USERID_NOT_SET:
                output_.writeInt(MEException.USERID_NOT_SET);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.USERID_DISABLE:
                output_.writeInt(MEException.USERID_DISABLE);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.USERID_UNKNOWN:
                output_.writeInt(MEException.USERID_UNKNOWN);
                output_.writeUTF( e.getMessage() );
                break;

            default:
                output_.writeInt(MEException.AS400_SECURITY_EXCEPTION);
                output_.writeUTF( e.getMessage() );
                break;
            }
        }
        else if (e instanceof PcmlException)
        {
            output_.writeBoolean(false);
            output_.writeInt(MEException.PCML_EXCEPTION);
            output_.writeUTF( e.getLocalizedMessage() );
        }
        else if (e instanceof ObjectDoesNotExistException)
        {
            output_.writeInt(MEException.OBJECT_DOES_NOT_EXIST);
            output_.writeUTF( e.getMessage() );
        }
        else if (e instanceof ExtendedIllegalArgumentException)
        {
            int rc = ((ExtendedIllegalArgumentException)e).getReturnCode();                 
            if (rc == ExtendedIllegalArgumentException.LENGTH_NOT_VALID)
                output_.writeInt(MEException.LENGTH_NOT_VALID);
            else
            output_.writeInt(MEException.PARAMETER_VALUE_NOT_VALID);
            output_.writeUTF( e.getMessage() );
        }
        else if (e instanceof ExtendedIllegalStateException)
        {
            output_.writeInt(MEException.PROPERTY_NOT_SET);
            output_.writeUTF( e.getMessage() );
        }
        else if (e instanceof ErrorCompletingRequestException)                  
        {
            int rc = ((ErrorCompletingRequestException)e).getReturnCode();

            if (rc == ErrorCompletingRequestException.LENGTH_NOT_VALID)
                output_.writeInt(MEException.LENGTH_NOT_VALID);
            else
                output_.writeInt(MEException.UNKNOWN);
            output_.writeInt(MEException.PROPERTY_NOT_SET);
            output_.writeUTF( e.getMessage() );
        }
        else if (e instanceof ConnectionDroppedException)
        {
            int rc = ((ConnectionDroppedException)e).getReturnCode();

            if (Trace.isTraceOn())
                Trace.log(Trace.DIAGNOSTIC, "Exception return code: " + rc);

            if (rc == ConnectionDroppedException.CONNECTION_DROPPED)
            {
                output_.writeInt(MEException.CONNECTION_DROPPED);
                output_.writeUTF( e.getMessage() );
            }
            else
            {
                output_.writeInt(MEException.UNKNOWN);
                output_.writeUTF( e.getMessage() );
            }
        }
        else if (e instanceof IllegalObjectTypeException)
        {
            output_.writeInt(MEException.ILLEGAL_OBJECT_TYPE);
            output_.writeUTF( e.getMessage() );
        }
        else if (e instanceof ServerStartupException)
        {
            int rc = ((ServerStartupException) e).getReturnCode();

            if (Trace.isTraceOn())
                Trace.log(Trace.DIAGNOSTIC, "Exception return code: " + rc);

            if ( rc == ServerStartupException.SERVER_NOT_STARTED)
            {
                output_.writeInt(MEException.SERVER_NOT_STARTED);
                output_.writeUTF( e.getMessage() );
            }
            else
            {
                output_.writeInt(MEException.UNKNOWN);
                output_.writeUTF( e.getMessage() );
            }
        }
        else if (e instanceof UnknownHostException)
        {
            output_.writeInt(MEException.UNKNOWN_HOST);
            output_.writeUTF( e.toString() );
        }
        else if (e instanceof IOException)
        {
            output_.writeInt(MEException.UNKNOWN);
            output_.writeUTF( e.toString() );
        }
        else
        {
            output_.writeInt(MEException.UNKNOWN);
            output_.writeUTF( e.getMessage() );
        }

        output_.flush();
    }


    /**
     *  Prints the application usage information.
     * 
     *  @param out  The print stream for usage information.
     **/
    static void usage (PrintStream out)
    {
        final String usage      = ResourceBundleLoader_m.getText("ME_SERVER_USAGE");
        final String optionslc  = ResourceBundleLoader_m.getText("ME_SERVER_OPTIONSLC");
        final String optionsuc  = ResourceBundleLoader_m.getText("ME_SERVER_OPTIONSUC");
        final String shortcuts  = ResourceBundleLoader_m.getText("ME_SERVER_SHORTCUTS");  

        out.println (usage + ":");
        out.println ();
        out.println ("  com.ibm.as400.access.MEServer [ " + optionslc + " ]");
        out.println ();
        out.println (optionsuc + ":");
        out.println ();
        out.println ("  -pcml     [pcml doc]");
        out.println ("  -port     port");
        out.println ("  -verbose  [true | false]");
        out.println ("  -help");
        out.println ();                                                    
        out.println (shortcuts + ":");                                     
        out.println ("  -v   [true | false]");  
        out.println ("  -pc  pcml doc1 [;pcml doc2;...]");
        out.println ("  -po  port");                                         
        out.println ("  -h");                                               
        out.println ("  -?");                                               
    }
}
