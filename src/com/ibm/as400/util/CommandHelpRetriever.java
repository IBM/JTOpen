///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CommandHelpRetriever.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util;

// JDK classes.
import java.io.*;
import java.util.*;

// XSL classes.
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

// Toolbox classes.
import com.ibm.as400.access.*;

// WebSphere 4.0 ships only a stub XML4J.jar, and XML4J.jar goes away in the future. We need to 
// use xerces.jar to be compatible with the version used by WebSphere.
//import org.apache.xerces.parsers.SAXParser;
//import javax.xml.parsers.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
//import org.xml.sax.helpers.*;


/**
 *  Utility class for generating IBM-formatted CL command help documentation.
 *  This class requires that valid XML and XSL processors (e.g. Xerces and Xalan) be in the CLASSPATH.
 *
 *   CommandHelpRetriever can be run as a command line program, as follows:
 *  <BLOCKQUOTE></PRE>
 *  <strong>java com.ibm.as400.util.CommandHelpRetriever</strong>  -library <library> -command <command> [ -system <systemName> -userid <userid> -password <password> -showChoicePgmValues -output <outputDirectory> -debug ]
 *  </PRE></BLOCKQUOTE> 
 *  <b>Options:</b>
 *  <p> 
 *  <dl>
 *
 *  <dt><b><code>-help </b></code>
 *  <dd>Displays the help text.
 *  The -help option may be abbreviated to -h or -?.
 *  </dl>
 *  <p>
 *  <b>Parameters:</b>
 *  <p>
 *  <dl>
 *
 *  <dt><b><code>-library </b></code><var>iSeries library.</var></dt>
 *  <dd>Specifies the iSeries library.
 *  This parameter may be abbreviated <code>-l</code> or <code>-lib</code>.
 *  </dd>
 *
 *  <dt><b><code>-command </b></code><var>iSeries command.</var></dt>
 *  <dd>Specifies the iSeries command.
 *  This parameter may be abbreviated <code>-c</code> or <code>-cmd</code>.
 *  </dd>
 *
 *  <dt><b><code>-system </b></code><var>iSeries system name</var></dt>
 *  <dd>Specifies the iSeries system.  If an iSeries system name is not provided, a signon dialog will be displayed.
 *  This optional parameter may be abbreviated <code>-s</code> or <code>-sys</code>.
 *  </dd>
 *  
 *  <dt><b><code>-userid </b></code><var>iSeries userId.</var></dt>
 *  <dd>Specifies the iSeries userId.    If an iSeries userid  is not provided, a signon dialog will be displayed.
 *  This optional parameter may be abbreviated <code>-u</code> or <code>-uid</code>.
 *  </dd>
 *
 *  <dt><b><code>-password </b></code><var>iSeries password.</var></dt>
 *  <dd>Specifies the iSeries password.  If an iSeries password is not provided, a signon dialog will be displayed.
 *  This optional parameter may be abbreviated <code>-p</code> or <code>-pwd</code>.
 *  </dd>
 * 
 *  <dt><b><code>-showChoicePgmValues </b></code>[true | false] </dt>
 *  <dd>Specifies whether or not parameter choices returned from choice programs are shown in the Choices cells of the parameter summary table.
 *  The default is false.  
 *  This optional parameter may be abbreviated <code>-scpv</code>.
 *  </dd>
 *
 *  <dt><b><code>-output </b></code><var>output location.</var>
 *  <dd>Specifies the output location for the generated help.  The default is the current directory. The output
 *  cannot be a file when the command (-c) parameter contains a wildcard (*).
 *  This optional parameter may be abbreviated <code>-o</code>.
 *  </dd>
 *
 *  <dt><b><code>-debug </b></code><var></code>[true | false] </var>
 *  <dd>Specifies whether to output the source command help and xml files to the output location specified by the <i>-output</i> parameter.  The default is false.
 *  This optional parameter may be abbreviated <code>-d</code>.
 *  </dd>
 *
 *  </dl>
 *  </PRE></BLOCKQUOTE>
 * @see com.ibm.as400.access.Command
 * @see com.ibm.as400.access.CommandList
**/
public class CommandHelpRetriever
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

    private boolean debug_ = false;

    // The Templates object is threadsafe. We use it to pre-compile
    // the XSL code and generate new Transformer objects.
    private static Templates template_;
    private static URIResolver defaultResolver_;

    //private String systemName_ = null;
    //private String userID_ = null;
    //private String password_ = null;
    private static String library_ = null;
    private static String command_ = null;
    private static String outputDirectory_ = "."; // Default to current directory

    private boolean showChoices_ = false;

    // This instance has its own copy of the XSL transformer,
    // SAX parser, URI resolver, and entity resolver/handler.
    // Hence, the generateHTML() method is synchronized.
    private Transformer transformer_;
//  private final SAXParser parser_ = new SAXParser();
//  private final HelpHandler handler_ = new HelpHandler();
    private final HelpResolver resolver_ = new HelpResolver();

    // Objects used to load the MRI and transform it into something
    // that the output HTML document can use.
    private static final String[] mriTags_ = new String[]
    {
        "ALLOW_ALL",                 
        "ALLOW_COMPILED_CL_OR_REXX1",
        "ALLOW_COMPILED_CL_OR_REXX2",
        "ALLOW_INTERACTIVE1",        
        "ALLOW_INTERACTIVE2",        
        "ALLOW_JOB_BATCH",           
        "ALLOW_JOB_INTERACTIVE",     
        "ALLOW_MODULE_BATCH",        
        "ALLOW_MODULE_INTERACTIVE",  
        "ALLOW_PROGRAM_BATCH",       
        "ALLOW_PROGRAM_INTERACTIVE", 
        "ALLOW_REXX_BATCH",          
        "ALLOW_REXX_INTERACTIVE",    
        "ALLOW_USING_COMMAND_API",   
        "CHOICES",                   
        "ELEMENT",                   
        "ERRORS",                    
        "EXAMPLES",                  
        "KEY",                       
        "KEYWORD",                   
        "NAME_LOWERCASE",            
        "NONE",                      
        "NOTES",                     
        "OPTIONAL",                  
        "PARAMETERS",                
        "POSITIONAL",                
        "QUALIFIER",                 
        "REQUIRED",                  
        "THREADSAFE",                
        "THREADSAFE_CONDITIONAL",    
        "TOP_OF_PAGE",               
        "TYPE_CL_VARIABLE_NAME",     
        "TYPE_COMMAND_STRING",       
        "TYPE_COMMUNICATIONS_NAME",  
        "TYPE_DATE",                 
        "TYPE_DECIMAL_NUMBER",       
        "TYPE_ELEMENT_LIST",         
        "TYPE_GENERIC_NAME",         
        "TYPE_INTEGER",              
        "TYPE_NOT_RESTRICTED",       
        "TYPE_PATH_NAME",            
        "TYPE_QUALIFIED_JOB_NAME",   
        "TYPE_QUALIFIED_OBJECT_NAME",
        "TYPE_QUALIFIER_LIST",       
        "TYPE_SIMPLE_NAME",          
        "TYPE_TIME",                 
        "TYPE_VALUE_LOGICAL",        
        "TYPE_VALUE_CHARACTER",      
        "TYPE_VALUE_HEX",            
        "TYPE_UNSIGNED_INTEGER",     
        "UNKNOWN",                   
        "VALUES_OTHER",              
        "VALUES_OTHER_REPEAT",       
        "VALUES_REPEAT",             
        "VALUES_SINGLE",             
        "WHERE_ALLOWED_TO_RUN"       
    };
//    private static ResourceBundle bundle_, bundle2_;
//    private static final String[][] transformedParms_ = new String[mriTags_.length+4][];
    private static final String[][] transformedParms_ = getTransformedParms(Locale.getDefault());


    // Find the XSL document, generate an XSL template, load the resource bundles,
    // and generate the MRI strings.
    static
    {
        try
        {
            if (Trace.isTraceOn())
            {
                Trace.log(Trace.DIAGNOSTIC, "Loading gencmddoc.xsl.");  
            }

            String xslURI = CommandHelpRetriever.class.getClassLoader().getResource("com/ibm/as400/util/gencmddoc.xsl").toString();

            if (Trace.isTraceOn())
            {
                Trace.log(Trace.DIAGNOSTIC, "Loading XSL template.");
            }

            TransformerFactory factory = TransformerFactory.newInstance();
            defaultResolver_ = factory.getURIResolver();
            template_ = factory.newTemplates(new StreamSource(xslURI));

//            bundle_ = ResourceBundle.getBundle("com.ibm.as400.access.MRI");
//            bundle2_ = ResourceBundle.getBundle("com.ibm.as400.access.MRI2");
        }
        catch (Exception e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Unable to initialize CommandHelpRetriever XSL and MRI.", e);

            throw new RuntimeException(e.toString());
        }

        // Transform the MRI and save it off so we can easily reset it into new Transformer
        // objects when setupTransformer() is called.
// This code moved to getTransformedParms().
/*        if (Trace.isTraceOn())
        {
            Trace.log(Trace.DIAGNOSTIC, "Transforming CommandHelpRetriever MRI.");
        }

        int i=0;
        for (; i<mriTags_.length; ++i)
        {
          transformedParms_[i] = new String[] { "_"+mriTags_[i], encode(bundle2_.getString("GENCMDDOC_"+mriTags_[i]))};
        }
        transformedParms_[i++] = new String[] { "_DESCRIPTION", encode(bundle2_.getString("NETSERVER_DESCRIPTION_NAME"))};
        transformedParms_[i++] = new String[] { "_THREADSAFE_NO", encode(bundle_.getString("DLG_NO_BUTTON"))};
        transformedParms_[i++] = new String[] { "_THREADSAFE_YES", encode(bundle_.getString("DLG_YES_BUTTON"))};
        transformedParms_[i] = new String[] { "_TYPE_NAME", encode(bundle2_.getString("NETSERVER_NAME_NAME"))};
*/
    }


    // Resolver used in XSL transform.
    private static class HelpResolver implements URIResolver
    {
        public String helpResults_;

        public Source resolve(String href, String base) throws TransformerException
        {
            // This gets called when the XSL transformer encounters a document() call.
            if (href.indexOf("myCommandHelpResolver") > -1)
            {
                return new StreamSource(new StringReader(helpResults_));
            }
            return defaultResolver_.resolve(href, base);
        }
    }


    // Handler used in XML parsing.
/*  private static class HelpHandler extends DefaultHandler
  {
        public final Vector keywords_ = new Vector();
        public String panelGroup_;
        public String helpID_;
        public String productLibrary_;

        public void startElement(String namespaceURI, String localName, String name, Attributes attributes) throws SAXException
        {
            if (name.equals("Parm"))
            {
                String kwd = attributes.getValue("Kwd");
                if (kwd != null)
                {
                    keywords_.addElement(kwd);
                }
            }
            else if (name.equals("Cmd")) // Assume there is only one Cmd element in the XML.
            {
                String helpName = attributes.getValue("HlpPnlGrp");
                String helpLib = attributes.getValue("HlpPnlGrpLib");

                if (helpLib != null && helpLib.equals("__LIBL"))
                {
                    helpLib = "*LIBL";
                }

                if (helpLib != null && helpName != null)
                {
                    panelGroup_ = QSYSObjectPathName.toPath(helpLib, helpName, "PNLGRP");
                }

                helpID_ = attributes.getValue("HlpID");
                productLibrary_ = attributes.getValue("PrdLib");
            }
        }
    }
*/

    /**
     * Performs the actions specified in the invocation arguments.
     * @param args The command line arguments.
    **/
    public static void main(String[] args)
    {
        PrintWriter writer = new PrintWriter(System.out, true);    //The PrintWriter used when running via the command line.

        if (args.length == 0)
        {
            writer.println();
            usage();
        }

        try
        {
            CommandHelpRetriever utility = new CommandHelpRetriever();

            AS400 system = parseParms(args, utility);
            CommandList list = new CommandList(system, utility.library_, utility.command_);
            Command[] cmds = list.generateList();

            File outDir = new File(utility.outputDirectory_);
            boolean isDir = outDir.isDirectory();
	    if (!isDir && cmds.length>1)
	    {
		if (Trace.isTraceOn())
		    Trace.log(Trace.DIAGNOSTIC, "The output parameter cannot be a file when a wildcard command is specified");

		throw new ExtendedIllegalArgumentException("output", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	    }

            for (int i=0; i<cmds.length; ++i)
            {
              try
              {
                String html = utility.generateHTML(cmds[i]);

                File outFile = null;
                if (isDir)
                {
                    QSYSObjectPathName path = new QSYSObjectPathName(cmds[i].getPath());
                    String filename = path.getLibraryName()+"_"+path.getObjectName()+".html";
                    outFile = new File(outDir, filename);
                }
                else
                {
                    outFile = outDir;
                }
                
                FileWriter out = new FileWriter(outFile);
                out.write(html);
                out.close();
              }
              catch(Exception e1)
              {
                e1.printStackTrace();
              }
            }
            //system.disconnectAllServices();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	// Don't do this.
        //System.exit(0);
    }


    /**
     * Constructs an instance of the CommandHelpRetriever utility.
    **/
    public CommandHelpRetriever()
    {
/*        try
        {
            parser_.setFeature("http://xml.org/sax/features/namespaces", false);
            parser_.setContentHandler(handler_);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            
            if (Trace.isTraceOn()) 
                Trace.log(Trace.ERROR, "Exception on construct of CommandHelpRetriever: ", e);
        }
*/
  }


    /**
     * This method copied from HTMLTransform to eliminate the dependency
     * on the jt400Servlet.jar.
    **/
    private static final String encode(String source)
    {
        StringBuffer dest = new StringBuffer();
        char[] buf = source.toCharArray();
        for (int i=0; i<buf.length; ++i)
        {
            switch (buf[i])
            {
            case '\"':
                dest.append("&quot;");
                break;
            case '&':
                dest.append("&amp;");
                break;
            case '<':
                dest.append("&lt;");
                break;
            case '>':
                dest.append("&gt;");
                break;
            default:
                dest.append(buf[i]);
                break;
            }
        }
        return dest.toString();
    }


    /**
     * Generates IBM-formatted command help documentation for the specified CL command.
     * Portions of the resulting HTML will contain strings that were translated using
     * the {@link java.util.Locale Locale} specified on the {@link com.ibm.as400.access.AS400 AS400}
     * object for the given {@link com.ibm.as400.access.Command Command}.
     * @param command The command.
     * @return An HTML string consisting of the help documentation for the command.
     * @see java.util.Locale
     * @see com.ibm.as400.access.AS400
     * @see com.ibm.as400.access.Command
    **/
    public synchronized String generateHTML(Command command) throws AS400Exception, AS400SecurityException,
    ErrorCompletingRequestException, IOException,
    InterruptedException, ObjectDoesNotExistException,
/*    SAXNotRecognizedException, SAXNotSupportedException, */ SAXException,
    ParserConfigurationException, 
    TransformerConfigurationException, TransformerException
    {
        if (command == null)
            throw new NullPointerException("command");

        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Generating HTML documentation for "+command+".");

        String xml = command.getXML();

        if (debug_)
        {
            String path = command.getPath();
            QSYSObjectPathName pn = new QSYSObjectPathName(path);
            String lib = pn.getLibraryName();
            String name = pn.getObjectName();

            File outDir = new File(outputDirectory_);
            boolean isDir = outDir.isDirectory();

            File outFile = null;

            if (isDir)
                outFile = new File(outDir, lib + "_" + name + "_XML.xml");
            else
                outFile = new File(outputDirectory_ + "_XML.xml");

            FileWriter fw = new FileWriter(outFile);
            fw.write(xml);
            fw.flush();
            fw.close();
        }

        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Retrieved command XML:\n"+xml+"\n");

        String threadSafe = null;
        String whereAllowedToRun = null;

        try
        {
            switch(command.getThreadSafety())
            {
              case Command.THREADSAFE_NO:
                threadSafe = "0";
                break;
              case Command.THREADSAFE_YES:
                threadSafe = "1";
                break;
              case Command.THREADSAFE_CONDITIONAL:
                threadSafe = "2";
                break;
              default:
                break;
            }
            whereAllowedToRun = command.getWhereAllowedToRun();
        }
        catch (AS400Exception e)
        {
            AS400Message[] msgs = e.getAS400MessageList();

            // CPF6250 - Can't retrieve info for command. Probably because it is a system command.
            if (msgs.length != 1 || !msgs[0].getID().toUpperCase().trim().equals("CPF6250"))
                throw new AS400Exception(msgs);
        }

        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Using command threadsafety = "+threadSafe+" and where allowed to run = "+whereAllowedToRun+".");

        // Grab the helpID and panelGroup out of the XML, not from the Command object,
        // in case the API call threw a CPF6250.
/*    handler_.keywords_.clear();
        handler_.helpID_ = null;
        handler_.panelGroup_ = null;
        handler_.productLibrary_ = null;
        parser_.parse(new InputSource(new StringReader(xml)));

        Vector keywords = handler_.keywords_;
        String helpID = handler_.helpID_;
        String pGroup = handler_.panelGroup_;
        String prdLib = handler_.productLibrary_;

        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Retrieved "+keywords.size()+" keywords. Help ID = '"+helpID+"'; Panel group = '"+pGroup+"'.");
*/
    
/*    String helpResults = null;

    PanelGroupHelpIdentifier[] helpIDs = command.getXMLHelpIdentifiers();
    String prdLib = command.getXMLProductLibrary();
    String pGroup = command.getXMLPanelGroup();

        if (pGroup != null)
        {
            String[] helpIDs = new String[keywords.size()+3];
            helpIDs[0] = helpID;
            helpIDs[1] = helpID+"/ERROR/MESSAGES";
            helpIDs[2] = helpID+"/COMMAND/EXAMPLES";

            for (int i=3; i<helpIDs.length; ++i)
            {
                helpIDs[i] = helpID+"/"+keywords.elementAt(i-3);
            }
            boolean added = false;
            if (prdLib != null)
            {
              prdLib = prdLib.trim();
              // Add the product library to the library list
              // otherwise the API won't find the help text.
              // First, check to see if it's there.
              Job job = new Job(command.getSystem()); // Current job
              String[] userLibraries = job.getUserLibraryList();
              String[] sysLibraries = job.getSystemLibraryList();
              String curLibrary = job.getCurrentLibrary();
              boolean exists = false;
              if (curLibrary.trim().equalsIgnoreCase(prdLib))
              {
                exists = true;
              }
              for (int i=0; i<userLibraries.length && !exists; ++i)
              {
                if (userLibraries[i].trim().equalsIgnoreCase(prdLib))
                {
                  exists = true;
                }
              }
              for (int i=0; i<sysLibraries.length && !exists; ++i)
              {
                if (sysLibraries[i].trim().equalsIgnoreCase(prdLib))
                {
                  exists = true;
                }
              }
              if (!exists)
              {
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "CommandHelpRetriever: Adding "+prdLib+" to library list.");
                // We have to try to add it.
                String addlible = "ADDLIBLE LIB("+prdLib+")";
                CommandCall cc = new CommandCall(command.getSystem(), addlible);
                added = cc.run();
              }
            }
                
            
            PanelGroup panelGroup = new PanelGroup(command.getSystem(), pGroup);
            helpResults = panelGroup.getHelpText(helpIDs);


            // Remove the product library from the library list if we added it.
            if (added)
            {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "CommandHelpRetriever: Removing "+prdLib+" from library list.");
              // We have to try to add it.
              String rmvlible = "RMVLIBLE LIB("+prdLib+")";
              CommandCall cc = new CommandCall(command.getSystem(), rmvlible);
              cc.run();
            }
*/
    String helpResults = command.getXMLHelpText();

      if (debug_ && helpResults != null)
      {
                String path = command.getPath();
                QSYSObjectPathName pn = new QSYSObjectPathName(path);
                String lib = pn.getLibraryName();
                String name = pn.getObjectName();

                File outDir = new File(outputDirectory_);
                boolean isDir = outDir.isDirectory();
                
                File outFile = null;

                if (isDir)
                    outFile = new File(outDir, lib + "_" + name + "_HTMLHelp.html");
                else
                    outFile = new File(outputDirectory_ + "_HTMLHelp.html");
                
                FileWriter fw = new FileWriter(outFile);
                fw.write(helpResults);
                fw.flush();
                fw.close();
            }
//    }

//    if (Trace.isTraceOn())
//      Trace.log(Trace.DIAGNOSTIC, "Retrieved help results:\n"+helpResults+"\n");


        // Reset the transformer. We could re-use the same Transformer object,
        // but the problem is that any document() calls in the XSL are resolved
        // and cached by the transformer, so that the second time through, our
        // help text would still be from the previous command.
        // One way around this is to DOM parse the help text ourselves and pass
        // that in to the transformer, instead of coding a document() call in
        // the XSL; however, this is INCREDIBLY slow since the DOM tree can 
        // consist of a large number of objects, all of which the transformer
        // has to marshal into its own internal format. It turns out that
        // using a document() call in the XSL code is faster, especially since
        // we don't get any re-use out of the help text's DOM tree.
        setupTransformer(command.getSystem().getLocale());

        transformer_.setParameter("CommandHelp", helpResults == null || helpResults.trim().length() == 0 ? "__NO_HELP" : "myCommandHelpResolver");
        transformer_.setParameter("ShowChoicePgmValues", showChoices_ ? "1" : "0");
        transformer_.setParameter("ThreadSafe", threadSafe == null ? "0" : threadSafe);
        transformer_.setParameter("WhereAllowed", whereAllowedToRun == null ? "000000000000000" : whereAllowedToRun);

        resolver_.helpResults_ = helpResults;

        StreamSource sourceXML = new StreamSource(new StringReader(xml));
        StringWriter buf = new StringWriter();
        StreamResult output = new StreamResult(buf);

        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Performing XSL transform.");

        transformer_.transform(sourceXML, output);

        if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC, "Successfully generated help documentation.");

        return buf.toString();
    }


    /**
     *  Returns whether or not the source XML and source command HTML help, which are used in
     *  generating the resulting command help HTML documentation, are saved to the current output location.
     *
     *  @return true if the source XML and source HTML files will be generated; false otherwise.  The default is false.
     *  @see #setDebug
     **/
    public boolean getDebug()
    {
        return debug_;
    }


    /**
     *  Returns the output location for the source XML and source command HTML help when running programatically.  The default is the current directory.
     *
     *  @return The output location of the source files.
     *  @see #setOutputLocation
     **/
    public String getOutputLocation()
    {
        return outputDirectory_;
    }


    /**
     * Returns whether or not the generated HTML documentation will contain parameter values
     * returned by a "choices program".
     * @return true if the extra parameters will be shown; false otherwise. The default is false.
     * @see #setShowChoiceProgramValues
    **/
    public boolean getShowChoiceProgramValues()
    {
        return showChoices_;
    }


    /**
     * Used to get the translated MRI for a given Locale.
    **/
    private static final String[][] getTransformedParms(Locale locale)
    {
      String[][] parms = new String[mriTags_.length+4][];
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Transforming CommandHelpRetriever MRI for locale: "+locale);
      }
      ResourceBundle bundle = ResourceBundle.getBundle("com.ibm.as400.access.MRI", locale);
      ResourceBundle bundle2 = ResourceBundle.getBundle("com.ibm.as400.access.MRI2", locale);

      int i=0;
      for (; i<mriTags_.length; ++i)
      {
        parms[i] = new String[] { "_"+mriTags_[i], encode(bundle2.getString("GENCMDDOC_"+mriTags_[i]))};
      }
      parms[i++] = new String[] { "_DESCRIPTION", encode(bundle2.getString("NETSERVER_DESCRIPTION_NAME"))};
      parms[i++] = new String[] { "_THREADSAFE_NO", encode(bundle.getString("DLG_NO_BUTTON"))};
      parms[i++] = new String[] { "_THREADSAFE_YES", encode(bundle.getString("DLG_YES_BUTTON"))};
      parms[i] = new String[] { "_TYPE_NAME", encode(bundle2.getString("NETSERVER_NAME_NAME"))};

      return parms;
    }


    /**
     *  Parse out the command line arguments for the CommandHelpRetriever command.
    **/
    private static AS400 parseParms(String args[], CommandHelpRetriever utility) throws Exception
    {
        String s,u,p,l,c,pv,o,d;

        Vector v = new Vector();
        v.addElement("-system");
        v.addElement("-userid");
        v.addElement("-password");                  
        v.addElement("-library");
        v.addElement("-command");
        v.addElement("-showChoicePgmValues");
        v.addElement("-output");
        v.addElement("-debug");

        Hashtable shortcuts = new Hashtable();
        shortcuts.put("-help", "-h");
        shortcuts.put("-?", "-h");
        shortcuts.put("-s", "-system");
        shortcuts.put("-sys", "-system");
        shortcuts.put("-u", "-userid");           
        shortcuts.put("-uid", "-userid");
        shortcuts.put("-p", "-password");           
        shortcuts.put("-pwd", "-password");
        shortcuts.put("-l", "-library");           
        shortcuts.put("-lib", "-library");
        shortcuts.put("-c", "-command");           
        shortcuts.put("-cmd", "-command");
        shortcuts.put("-scpv", "-showChoicePgmValues");
        shortcuts.put("-o", "-output");           
        shortcuts.put("-d", "-debug");

        CommandLineArguments arguments = new CommandLineArguments(args, v, shortcuts);

        // If this flag is specified by the user, display the help (usage) text.
        if (arguments.getOptionValue("-h") != null)
            usage();

        AS400 system = new AS400();

        s = arguments.getOptionValue("-system");
        if (s != null)
            system.setSystemName(s);
        
        u = arguments.getOptionValue("-userid");
        if (u != null)
            system.setUserId(u);
        
        p = arguments.getOptionValue("-password");
        if (p != null)
            system.setPassword(p);
        
        l = arguments.getOptionValue("-library");
        if (l != null)
            library_ = l;
        else
            throw new ExtendedIllegalArgumentException("library", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        c = arguments.getOptionValue("-command");
        if (c != null)
            command_ = c;
        else
            throw new ExtendedIllegalArgumentException("command", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        pv = arguments.getOptionValue("-showChoicePgmValues");
        if (pv != null)
        {
            if (pv.length() == 0 || pv.equalsIgnoreCase("true"))
                utility.setShowChoiceProgramValues(true);
        }

        o = arguments.getOptionValue("-output");
        if (o != null)
            outputDirectory_ = o;

        d = arguments.getOptionValue("-debug");
        if (d != null)
        {
            if (d.length() == 0 || pv.equalsIgnoreCase("true"))
                utility.setDebug(true);
        }

        return system;
    }


    /**
     *  Sets whether or not the source XML and source command HTML help, which are used in
     *  generating the resulting command help HTML documentation, are saved to the current output location.
     *
     *  @param debug true if the source XML and source HTML files will be generated; false otherwise.  The default is false.
     *  @see #getDebug
     **/
    public void setDebug(boolean debug)
    {
        synchronized(this)
        {
            debug_ = debug;
        }
    }


    /**
     *  Sets the output location for the source XML and source command HTML help when running programatically.  The default is the current directory.
     *
     *  @param location The output location of the source files.
     *  @see #getOutputLocation
     **/
    public void setOutputLocation(String location)
    {
	if (location == null)					//@A1A
	    throw new NullPointerException("location");		//@A1A

        synchronized(this)
        {
            outputDirectory_ = location;
        }
    }


    /**
     * Sets whether or not parameter values returned by a "choices program" will be generated
     * as part of the HTML documentation.
     * @param show true to show the extra parameter values; false otherwise. The default
     * value is false.
     * @see #getShowChoiceProgramValues
    **/
    public void setShowChoiceProgramValues(boolean show)
    {
        synchronized(this)
        {
            showChoices_ = show;
        }
    }

    
    /**
     * Resets our internal XSL Transformer object and makes it ready
     * for the next call to transform().
    **/
    private void setupTransformer(Locale locale) throws TransformerConfigurationException
    {
        transformer_ = template_.newTransformer();
        transformer_.setURIResolver(resolver_);
        String[][] transformedParms = null;
        if (locale == null) // Just use the default locale of the JVM. We already pre-loaded that MRI.
        {
          transformedParms = transformedParms_;
        }
        else
        {
          transformedParms = getTransformedParms(locale);
        }
        for (int i=0; i<transformedParms.length; ++i)
        {
          transformer_.setParameter(transformedParms[i][0], transformedParms[i][1]);
        }
    }


    /**
    *  Print out the usage for the CommandHelpRetriever command.
    **/
    static void usage() 
    {
        ResourceBundle bundle2 = ResourceBundle.getBundle("com.ibm.as400.access.MRI2");
        final String usage      = bundle2.getString ("PROXY_SERVER_USAGE");
        final String optionslc  = bundle2.getString ("PROXY_SERVER_OPTIONSLC");
        final String optionsuc  = bundle2.getString ("PROXY_SERVER_OPTIONSUC");
        final String shortcuts  = bundle2.getString ("PROXY_SERVER_SHORTCUTS");  

        System.out.println (usage + ":");
        System.out.println ();
        System.out.println ("  com.ibm.as400.util.CommandHelpRetriever [ " + optionslc + " ]");
        System.out.println ();
        System.out.println ("  -libary library");
        System.out.println ("  -command command");
        System.out.println ();
        System.out.println (optionsuc + ":");
        System.out.println ();
        System.out.println ("  [ -system systemName ]");
        System.out.println ("  [ -userID userID ]");
        System.out.println ("  [ -password password ]");       
        System.out.println ("  [ -showChoicePgmValues ]");
        System.out.println ("  [ -output outputLocation ]");
        System.out.println ("  [ -debug ]");
        System.out.println ();                                                     
        System.out.println (shortcuts + ":");                                      
        System.out.println ();                                                     
        System.out.println ("  -h | -?");                                               
        System.out.println ("  -l library");
        System.out.println ("  -c command");
        System.out.println ("  [ -s systemName ]");
        System.out.println ("  [ -u userID ]");
        System.out.println ("  [ -p password ]");       
        System.out.println ("  [ -scpv [true | false] ]");
        System.out.println ("  [ -o outputLocation ]");
        System.out.println ("  [ -d [true | false] ]");

        System.exit(0);
    }

}

