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

package com.ibm.as400.access;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.util.Date;
import java.util.Locale;

import com.ibm.as400.data.*;

/**
<p>The MEServer class is used to fulfill requests from programs
using the jt400ME jar file.  The ME server is responsible for
creating and invoking methods on Toolbox objects on behalf of the
program.  The ME server is intended for use when the client
is running on a Tier 0 device.

<p>If there is already a ME server active for the specified
port, then a new ME server will not be started.

<p>Alternately, the proxy server can be run as an
application, as follows:

<blockquote>
<pre>
<strong>java com.ibm.as400.access.ProxyServer</strong> [ options ]
</pre>
</blockquote>

<p>Options:
<dl>

<dt><b><code>-pcml</b></code> pcml file1 [;pcml file2;...]</dt>
<dd>
Specifies the PCML files to pre-load and parse. This option may be abbreviated
<code>-pc</code>.  
</dd>

<dt><b><code>-port</b></code> port</dt>
<dd>
Specifies the port to use for accepting connections from clients. This option may be abbreviated -po.
     The default port is 3470. 
<code>-po</code>.  
</dd>

<dt><b><code>-reload</b></code> reload</dt>
<dd>
Specifies the PCML file to reload. This option may be abbreviated -rl.
<code>-rl</code>.  
</dd>

<dt><b><code>-verbose</b></code> [true|false]</dt>
<dd>
Specifies whether to print status and connection
information to System.out. This option may be abbreviated
<code>-v</code>.  
</dd>

<dt><b><code>-help</b></code></dt>
<dd>
Prints usage information to System.out.  This option may be abbreviated
<code>-h</code> or <code>-?</code>.  The default is not to print usage
information.
</dd>

</dl>

<p>Example usage:

<p>To start the proxy server from the command line as follows:
<pre>
java com.ibm.as400.access.MEServer
</pre>
**/
public class MEServer implements Runnable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // Private data.
    private Socket socket_;
    private DataInputStream input_;
    private DataOutputStream output_;
    private AS400 system_;
    private Connection connection_;

    private final AS400JDBCDriver driver_ = new AS400JDBCDriver();

    // Table of ProgramCallDocument objects  and RecordFormatDocuments
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
        expectedOptions_.addElement("-reload");
        expectedOptions_.addElement("-verbose");
        expectedOptions_.addElement("-help");

        // Shortcuts for the ProxyServer application.
        // Note: These are also listed in usage().
        shortcuts_.put("-po", "-port");
        shortcuts_.put("-pc", "-pcml");
        shortcuts_.put("-r", "-reload");
        shortcuts_.put("-v", "-verbose");
        shortcuts_.put("-h", "-help");
        shortcuts_.put("-?", "-help");
    }


    /**
     *  Constructs a MEServer object.
     **/
    private MEServer(Socket s) throws IOException
    {
        socket_ = s;
        input_ = new DataInputStream(socket_.getInputStream());
        output_ = new DataOutputStream(socket_.getOutputStream());
    }


    /**
     *  Cache the JDBC Result Set.
     **/
    private int cacheTransaction(ResultSet rs)
    {
        Integer key = new Integer(nextTransactionID_++);
        cachedJDBCTransactions_.put(key, rs);

        return key.intValue();
    }


    /**
     *  Close the streams and socket associated with this ME Server.
     **/
    private void close()
    {
        try
        {
            if (connection_ != null)
                connection_.close();

            system_.disconnectAllServices();
            system_ = null;
        }
        catch (Exception e)
        { /* Ignore */
        }

        system_ = null;

        try
        {
            input_.close();
            input_ = null;
        }
        catch (Exception ioe)
        { /* Ignore */
        }

        try
        {
            output_.close();
            output_ = null;
        }
        catch (Exception ioe)
        { /* Ignore */
        }

        try
        {
            socket_.close();
            socket_ = null;

            Verbose.println ( ResourceBundleLoader.getText ("ME_CONNECTION_CLOSED", Thread.currentThread().getName()/*new Integer(threadIndex_).toString()*/ ) );
        }
        catch (Exception ioe)
        { /* Ignore */
        }
    }


    // Unscramble some bytes.
    private static char[] decode(char[] adder, char[] mask, char[] bytes)
    {
        int length = bytes.length;
        char[] buf = new char[length];

        for (int i = 0; i < length; ++i)
        {
            buf[i] = (char)(mask[i % 7] ^ bytes[i]);
        }

        for (int i = 0; i < length; ++i)
        {
            buf[i] = (char)(buf[i] - adder[i % 9]);
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

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Performing ToolboxME CommandCall...");

        CommandCall cc = new CommandCall(system_, command);
        boolean retVal = false;

        try
        {
            retVal = cc.run();
        }
        catch (AS400SecurityException ase)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ase);

            return;
        }
        catch (ConnectionDroppedException cde)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(cde);

            return;
        }
        catch (IOException ioe)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ioe);

            return;
        }

        int numReplies = 0;
        AS400Message[] messages = cc.getMessageList();

        if (!retVal)
            numReplies = messages.length;

        output_.writeInt(numReplies);

        for (int i=0; i<numReplies; ++i)
        {
            output_.writeUTF(messages[i].getID()+": "+messages[i].getText());
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
    private void doDataQueueRead() throws IOException, ErrorCompletingRequestException, InterruptedException
    {
        String queueName = input_.readUTF();
        int dataType = input_.readInt();

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Performing ToolboxME Data Queue Read...");

        DataQueue dq = new DataQueue(system_, queueName);
        DataQueueEntry entry = null;

        try
        {
            entry = dq.read();
            output_.writeInt(MEConstants.DATA_QUEUE_WRITE_READ_SUCCESSFUL);
        }
        catch (AS400SecurityException ase)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ase);

            return;
        }
        catch (ConnectionDroppedException cde)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(cde);

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
                output_.write(b, 0, b.length);
            } 
            //output_.writeUTF(l ? "" : entry.getString());
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
    private void doDataQueueWrite() throws IOException, ErrorCompletingRequestException, InterruptedException
    {
        String queueName = input_.readUTF();
        int dataType = input_.readInt();
        //String data = input_.readUTF();

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Performing ToolboxME Data Queue Write...");

        DataQueue dq = new DataQueue(system_, queueName);

        try
        {
            if (dataType == MEConstants.DATA_QUEUE_BYTES)
            {
                int len = input_.readInt();
                byte[] data = new byte[len];
                
                for (int i=0; i<len; ++i)
                {
                    data[i] = input_.readByte();
                }
                
                dq.write(data);
            }
            else if (dataType == MEConstants.DATA_QUEUE_STRING)
            {
                String data = input_.readUTF();
                dq.write(data);
            }
        }
        catch (AS400SecurityException ase)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ase);

            return;
        }
        catch (ConnectionDroppedException cde)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(cde);

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

        output_.writeInt(MEConstants.DATA_QUEUE_WRITE_READ_SUCCESSFUL);
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
        int numParmsToSet = input_.readInt(); // Number of parameter/object pairs to set

        // Read in the parms to set
        String[] tagNamesToSet = new String[numParmsToSet];
        String[] valuesToSet = new String[numParmsToSet];

        for (int i=0; i<numParmsToSet; ++i)
        {
            tagNamesToSet[i] = input_.readUTF();
            valuesToSet[i] = input_.readUTF();
        }

        // Check the parms
        if (tagNamesToSet == null)
        {
            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();
            sendException( new ExtendedIllegalArgumentException("parmsToSet", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID) );
            return;
        }
        else
        {
            if (valuesToSet == null)
            {
                output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
                output_.flush();
                sendException( new ExtendedIllegalArgumentException("parmValues", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID) );
                return;
            }

            if (tagNamesToSet.length != valuesToSet.length)
            {
                output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
                output_.flush();
                sendException( new ExtendedIllegalArgumentException("parmsToSet | parmValues", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID) );
                return;
            }

        }

        // Read in the parms to get
        int numParmsToGet = input_.readInt(); // Number of parameters to get
        String[] tagNamesToGet = new String[numParmsToGet];

        for (int i=0; i<numParmsToGet; ++i)
        {
            tagNamesToGet[i] = input_.readUTF();
        }

        ProgramCallDocument pc = loadDocument(pcmlName, false);

        if (pc == null)
            return;

        output_.writeInt(MEConstants.XML_DOCUMENT_REGISTERED);
        output_.flush();


        boolean retVal = false;

        try
        {
            pc.setSystem(system_);

            // Set.
            for (int i=0; i<numParmsToSet; ++i)
            {
                pc.setStringValue(tagNamesToSet[i], valuesToSet[i], BidiStringType.DEFAULT);
            }

            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "Performing ToolboxME ProgramCall...");

            // Call the program.
            retVal = pc.callProgram(apiName);

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

                output_.writeInt(13);
                output_.writeUTF(messages[0].getID()+": "+messages[0].getText());
                output_.flush();
            }

            // Get.
            for (int i=0; i<numParmsToGet; ++i)
            {
                String value = pc.getStringValue(tagNamesToGet[i], BidiStringType.DEFAULT);
                output_.writeUTF(value == null ? "" : value);
                output_.flush();
            }
        }
        catch (PcmlException pe)
        {
            Verbose.forcePrintln( pe.getMessage() );

            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, pe);

            output_.writeBoolean(false);
            output_.flush();

            sendException( pe.getException() );

            return;
        }
    }


    /**
     *  Close the JDBC Result Set.
     **/
    private void doJDBCClose() throws IOException
    {
        int transactionID = input_.readInt();
        Integer key = new Integer(transactionID);
        ResultSet rs = (ResultSet)cachedJDBCTransactions_.get(key);

        if (rs != null)
        {
            try
            {
                rs.close();
                output_.writeInt(MEConstants.JDBC_RESULT_SET_CLOSED);
                output_.flush();
            }
            catch (SQLException e)
            {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, e);

                output_.writeInt(MEConstants.JDBC_EXCEPTION);
                output_.writeUTF( e.getMessage() );
                output_.flush();
            }
        }
        else
        {
            output_.writeInt(MEConstants.JDBC_TRANSACTION_NOT_FOUND);
            output_.flush();
        }
    }


    /**
     * Receives the JDBC command datastream from the client and executes the statement.
     *
     * Datastream request format:
     *  String - SQL statement to execute
     *
     * Datastream reply format:
     *  int - command status (e.g. successful)
     *
     * And if it is successful:
     *  int - transaction ID for this statement
     **/
    private void doJDBCCommand() throws IOException
    {
        String statement = input_.readUTF();
        try
        {
            if (connection_ == null)
                connection_ = driver_.connect(system_);

            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "Performing ToolboxME JDBC...");

            // The statement has to be scrollable for the user to be able to call next(), previous(), or absolute( x )
            // in any order.  Otherwise an invalid cursor state will be received.
            Statement s = connection_.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = s.executeQuery(statement);
            int transactionID = cacheTransaction(rs);

            output_.writeInt(MEConstants.JDBC_COMMAND_SUCCEEDED);
            output_.writeInt(transactionID);
            output_.flush();
        }
        catch (SQLException e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, e);

            output_.writeInt(MEConstants.JDBC_EXCEPTION);
            output_.writeUTF( e.getMessage() );
            output_.flush();
        }
    }


    /**
     *  Retrieve the next record, move to the previous record, or go to 
     *  an absolute cursor position in the JDBC Result Set.
     **/
    private void doJDBCMove(int jdbcCommand) throws IOException
    {
        int transactionID = input_.readInt();

        Integer key = new Integer(transactionID);
        ResultSet rs = (ResultSet)cachedJDBCTransactions_.get(key);
        
        if (rs == null)
        {
            output_.writeInt(MEConstants.JDBC_TRANSACTION_NOT_FOUND);
            output_.flush();
        }
        else
        {
            try
            {
                boolean b = false;
                
                if (jdbcCommand == MEConstants.JDBC_NEXT)
                    b = rs.next();
                else if (jdbcCommand == MEConstants.JDBC_PREVIOUS)
                    b = rs.previous();
                else if (jdbcCommand == MEConstants.JDBC_ABSOLUTE)
                {
                    int abs = input_.readInt();
                    b = rs.absolute(abs);
                }

                if (!b)
                {   
                    output_.writeInt(MEConstants.JDBC_POSITION_CURSOR_FAILED);
                    output_.flush();
                }
                else
                {
                    output_.writeInt(MEConstants.JDBC_NEXT_RECORD);
                    int numColumns = rs.getMetaData().getColumnCount();                  
                    output_.writeInt(numColumns);
                    for (int i=1; i<=numColumns; ++i)
                    {
                        String s = rs.getString(i);
                        
                        output_.writeUTF(s);
                        output_.flush();
                    }
                }
            }
            catch (SQLException e)
            {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, e);

                output_.writeInt(MEConstants.JDBC_EXCEPTION);
                output_.writeUTF( e.getMessage() );
                output_.flush();
            }
        }
    }


    /**
     *  Add a new ProgramCallDocument to the cache for each file if one
     *  does not already exist.
     **/
    private static ProgramCallDocument getPCMLDocument(String pcml, boolean reload) throws PcmlException
    {
        ProgramCallDocument prog = null;

        if (!reload)
            prog = (ProgramCallDocument)registeredDocuments_.get(pcml);

        if (prog != null)
        {
            Verbose.println( ResourceBundleLoader.getText("ME_PCML_CACHE", pcml) );
            return prog;
        }

        if (reload)
            Verbose.println( ResourceBundleLoader.getText("ME_PCML_RELOADING", pcml) );
        else
            Verbose.println( ResourceBundleLoader.getText("ME_PCML_LOADING", pcml) );

        prog = new ProgramCallDocument();
        prog.setDocument(pcml);

        registeredDocuments_.put(pcml, prog);

        return prog;
    }


    /**
     *  Load a ProgramCallDocument.
     **/
    private ProgramCallDocument loadDocument(String pcml, boolean reload)
    {
        try 
        {
            return getPCMLDocument(pcml, reload);
        }
        catch (PcmlException pe)
        {
            Verbose.forcePrintln( ResourceBundleLoader.getText("ME_PCML_ERROR") );

            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, pe);

            if (output_ != null)
            {
                try
                {
                    output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
                    output_.writeInt(12);
                    output_.writeUTF( pe.getException().getMessage() );
                    output_.flush();
                }
                catch (IOException e)
                { /* Ignore */
                }
            }

            return null;
        }
        catch (NoClassDefFoundError n)
        {
            if (pcml == null)
                Verbose.forcePrintln( ResourceBundleLoader.getText("ME_PCML_ERROR") );
            
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, n);

            if (output_ != null)
            {
                try
                {
                    output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
                    output_.writeInt(12);
                    output_.writeUTF( n.getClass().getName()+ ": " + n.getMessage() );
                    output_.flush();
                }
                catch (IOException e)
                { /* Ignore */
                }
            }

            return null;
        }
    }


    /**
     *  Runs the ME server as an application.
     *
     *  @param args The command line arguments.
     **/
    public static void main(String[] args)
    {
        // Just to be safe...
        System.runFinalization();

        try
        {
            if ( parseArgs(args) )
            {
                ServerSocket ss = new ServerSocket(port_);

                Verbose.forcePrintln (ResourceBundleLoader.getText("ME_SERVER_STARTED"));

                Verbose.println( ResourceBundleLoader.getText("ME_SERVER_LISTENING", ResourceBundleLoader.getText("ME_SERVER_CONTAINER"), Integer.toString (port_)) );

                while (true)
                {
                    Socket s = ss.accept();

                    MEServer server = new MEServer(s);

                    Thread t = new Thread(server, "ToolboxMEThread-"+(threadIndex_++));
                    t.setDaemon(true);
                    t.start();
                }
            }
            else
            {
                usage (System.err);
            }
        }
        catch (BindException e)
        {
            Verbose.forcePrintln( ResourceBundleLoader.getText("ME_ALREADY_LISTENING", Integer.toString(port_)) );
            
            try 
            {
                Socket configSckt = new Socket(InetAddress.getLocalHost ().getHostName (), port_);
                DataOutputStream output = new DataOutputStream (configSckt.getOutputStream());
                
                output.writeInt(MEConstants.RELOAD);
                output.writeInt(args.length);
                for (int i=0; i<args.length; ++i)
                {
                    output.writeUTF( args[i] );
                }
                output.flush();

                configSckt.close ();
                
                return;
            }
            catch (UnknownHostException e1) 
            {
                if (Trace.isTraceErrorOn ())
                    Trace.log (Trace.ERROR, "Peer host is unknown.", e);

                System.err.println (e.getMessage ());
            }
            catch (IOException ioe)
            {
                if (Trace.isTraceErrorOn ())
                    Trace.log (Trace.ERROR, "Error opening ToolboxME server socket.", ioe);
            }
        }
        catch (IOException e)
        {
            if (Trace.isTraceErrorOn ())
                Trace.log (Trace.ERROR, "Error opening ToolboxME server socket.", e);
        }
    }


    /**
     *  Parses the command line arguments and sets the properties accordingly.
     *
     *  @param args     The command line arguments.
     *  @return         true if the combination of command line arguments is valid, false otherwise.
     **/
    private static boolean parseArgs (String[] args)
    {
        Trace.loadTraceProperties ();

        String optionValue;

        CommandLineArguments cla = new CommandLineArguments (args, expectedOptions_ , shortcuts_);

        if (cla.getOptionValue ("-help") != null)
            return false;

        optionValue = cla.getOptionValue("-verbose");
        if (optionValue != null)
        {
            if ((optionValue.length () == 0) || (optionValue.equalsIgnoreCase ("true")))
                Verbose.setVerbose (true);
            else if (optionValue.equalsIgnoreCase ("false"))
                Verbose.setVerbose (false);

            else
                throw new IllegalArgumentException( ResourceBundleLoader.getText("ME_OPTION_VALUE_NOT_VALID", new String[] { "verbose", optionValue}) );
        }

        optionValue = cla.getOptionValue ("-pcml");
        if (optionValue != null)
        {
            StringTokenizer msc = new StringTokenizer(optionValue, ";");
            while (msc.hasMoreTokens())
            {
                try
                {
                    getPCMLDocument(msc.nextToken(), false);
                }
                catch (PcmlException pe)
                {
                    Verbose.forcePrintln( ResourceBundleLoader.getText("ME_PCML_ERROR") );
                    Verbose.println( pe.getMessage() );

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

        optionValue = cla.getOptionValue ("-reload");
        if (optionValue != null)
        {
            StringTokenizer msc = new StringTokenizer(optionValue, ";");
            while (msc.hasMoreTokens())
            {
                try
                {
                    getPCMLDocument(msc.nextToken(), true);
                }
                catch (PcmlException pe)
                {
                    Verbose.forcePrintln( ResourceBundleLoader.getText("ME_PCML_ERROR") );
                    Verbose.println( pe.getMessage() );

                    if (Trace.isTraceOn())
                        Trace.log(Trace.ERROR, pe);
                }
            }
        }

        // Extra options.
        Enumeration enum = cla.getExtraOptions ();
        while (enum.hasMoreElements ())
        {
            String extraOption = enum.nextElement().toString();
            System.err.println (ResourceBundleLoader.getText ("ME_OPTION_NOT_VALID", extraOption));
        }

        return true;
    }


    // Get clear password bytes back.
    private static String resolve(char[] info)
    {
        char[] adder = new char[9];
        System.arraycopy(info, 0, adder, 0, 9);

        char[] mask = new char[7];
        System.arraycopy(info, 9, mask, 0, 7);

        char[] infoBytes = new char[info.length - 16];
        System.arraycopy(info, 16, infoBytes, 0, info.length - 16);

        return new String( decode(adder, mask, infoBytes) );
    }


    /**
     *  Process the datastream from the Tier 0 device.
     **/
    public void run()
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "New MEServer daemon running.");

        try
        {
            boolean disconnected = false;
            while (!disconnected)
            {
                // Process next datastream from client.
                int cmd = input_.readInt();
                
                if (Trace.isTraceOn())
                    Trace.log(Trace.DATASTREAM, "ME Datastream Request: " + Integer.toHexString(cmd));

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
                case MEConstants.JDBC_COMMAND:
                    doJDBCCommand();
                    break;
                case MEConstants.JDBC_ABSOLUTE:
                    doJDBCMove(MEConstants.JDBC_ABSOLUTE);
                    break;
                case MEConstants.JDBC_PREVIOUS:
                    doJDBCMove(MEConstants.JDBC_PREVIOUS);
                    break;
                case MEConstants.JDBC_NEXT:
                    doJDBCMove(MEConstants.JDBC_NEXT);
                    break;
                case MEConstants.JDBC_CLOSE:
                    doJDBCClose();
                    break;
                case MEConstants.DISCONNECT:
                    if (Trace.isTraceOn())
                        Trace.log(Trace.INFORMATION, "ToolboxME disconnect received.");
                    close();
                    disconnected = true;
                    break;
                case MEConstants.RELOAD:
                    int l = input_.readInt();
                    String[] s = new String[l];
                    for (int i=0; i<l; ++i)
                    {
                        s[i] = input_.readUTF();
                    }
                    if ( parseArgs( s ) )
                        Verbose.forcePrintln (ResourceBundleLoader.getText ("PROXY_CONFIGURATION_UPDATED"));
                    break;
                default:
                    output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
                    output_.writeInt(MEConstants.REQUEST_NOT_SUPPORTED);
                    output_.writeUTF("ToolboxME Request not supported: " +  Integer.toHexString(cmd) );
                    output_.flush();
                }
            }
        }
        catch (SocketException se)
        {
            // socket probably got shut down
            stop(se.getMessage());
        }
        catch (IOException ioe)
        {
            stop(ioe.getMessage());
        }
        catch (ErrorCompletingRequestException ecre)
        {
            stop(ecre.getMessage());
        }
        catch (InterruptedException ie)
        {
            stop(ie.getMessage());
        }

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

        String serverName = input_.readUTF();
        String userid = input_.readUTF();
        char[] bytes = input_.readUTF().toCharArray();

        if (system_ != null)
        {
            // Disconnect if we were previously signed on.
            // We only allow one AS400 object per thread/connection.
            system_.disconnectAllServices();
        }

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Performing ToolboxME Signon...");

        boolean retVal = false;

        try
        {
            system_ = new AS400(serverName, userid, resolve(bytes));

            // Wireless devices don't have NLS support in the KVM yet.
            // So we only want to display english messages.
            system_.setLocale(Locale.US);

            retVal = system_.validateSignon();
        }
        catch (Exception ase)
        {
            system_ = null;

            output_.writeInt(MEConstants.EXCEPTION_OCCURRED);
            output_.flush();

            sendException(ase);

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
        { /* Ignore */
        }

        try
        {
            output_.flush();
        }
        catch (Exception e)
        { /* Ignore */
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
            Trace.log(Trace.ERROR, e);

        // Determine what the exception was and then map it
        // the the MEException return code and send it back
        // to the Tier 0 client.
        if (e instanceof AS400SecurityException)
        {
            int rc = ((AS400SecurityException)e).getReturnCode();

            switch (rc)
            {
            case AS400SecurityException.PASSWORD_ERROR:
                output_.writeInt(2);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.PASSWORD_EXPIRED:
                output_.writeInt(3);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.PASSWORD_INCORRECT:
                output_.writeInt(4);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.USERID_NOT_SET:
                output_.writeInt(5);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.USERID_DISABLE:
                output_.writeInt(6);
                output_.writeUTF( e.getMessage() );
                break;

            case AS400SecurityException.USERID_UNKNOWN:
                output_.writeInt(7);
                output_.writeUTF( e.getMessage() );
                break;

            default:
                output_.writeInt(1);
                output_.writeUTF( e.getMessage() );
                break;
            }
        }
        else if (e instanceof ObjectDoesNotExistException)
        {
            output_.writeInt(9);
            output_.writeUTF( e.getMessage() );
        }
        else if (e instanceof ExtendedIllegalArgumentException)
        {
            output_.writeInt(10);
            output_.writeUTF( e.getMessage() );
        }
        else if (e instanceof ConnectionDroppedException)
        {
            if ( ( (ConnectionDroppedException) e).getReturnCode() == ConnectionDroppedException.CONNECTION_DROPPED)
            {
                output_.writeInt(94);
                output_.writeUTF( e.getMessage() );
            }
            else
            {
                output_.writeInt(99);
                output_.writeUTF( e.getMessage() );
            }
        }
        else if (e instanceof IllegalObjectTypeException)
        {
            output_.writeInt(93);
            output_.writeUTF( e.getMessage() );
        }
        else if (e instanceof ServerStartupException)
        {
            if ( ( (ServerStartupException) e).getReturnCode() == ServerStartupException.SERVER_NOT_STARTED)
            {
                output_.writeInt(95);
                output_.writeUTF( e.getMessage() );
            }
            else
            {
                output_.writeInt(99);
                output_.writeUTF( e.getMessage() );
            }
        }
        else if (e instanceof UnknownHostException)
        {
            output_.writeInt(96);
            output_.writeUTF( e.toString() );
        }
        else if (e instanceof IOException)
        {
            output_.writeInt(99);
            output_.writeUTF( e.toString() );
        }
        else
        {
            output_.writeInt(99);
            output_.writeUTF( (new InternalErrorException(InternalErrorException.UNKNOWN)).getMessage() );
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
        final String usage      = ResourceBundleLoader.getText("ME_SERVER_USAGE");
        final String optionslc  = ResourceBundleLoader.getText("ME_SERVER_OPTIONSLC");
        final String optionsuc  = ResourceBundleLoader.getText("ME_SERVER_OPTIONSUC");
        final String shortcuts  = ResourceBundleLoader.getText("ME_SERVER_SHORTCUTS");  

        out.println (usage + ":");
        out.println ();
        out.println ("  com.ibm.as400.access.MEServer [ " + optionslc + " ]");
        out.println ();
        out.println (optionsuc + ":");
        out.println ();
        out.println ("  -pcml     [pcml file]");
        out.println ("  -port     port");
        out.println ("  -reload   [pcml or rfml file]");
        out.println ("  -rfml       [rfml file]");
        out.println ("  -verbose  [true | false]");
        out.println ("  -help");
        out.println ();                                                    
        out.println (shortcuts + ":");                                     
        out.println ("  -v   [true | false]");  
        out.println ("  -pc  pcml file1 [;pcml file2;...]");
        out.println ("  -po  port");                                         
        out.println ("  -rl  <file1>.pcml [;<file2>.rfml;...]");
        out.println ("  -rf  rfml  file1 [;rfml file2;...]");
        out.println ("  -h");                                               
        out.println ("  -?");                                               
    }
}
