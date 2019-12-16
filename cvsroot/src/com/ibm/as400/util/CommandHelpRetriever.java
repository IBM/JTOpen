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
 *  Generates IBM-formatted CL command help documentation.
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
 *  <dt><b><code>-library </b></code><var>IBM i library.</var></dt>
 *  <dd>Specifies the IBM i library.
 *  This parameter may be abbreviated <code>-l</code> or <code>-lib</code>.
 *  </dd>
 *
 *  <dt><b><code>-command </b></code><var>IBM i command.</var></dt>
 *  <dd>Specifies the IBM i command.
 *  This parameter may be abbreviated <code>-c</code> or <code>-cmd</code>.
 *  </dd>
 *
 *  <dt><b><code>-system </b></code><var>IBM i system name</var></dt>
 *  <dd>Specifies the IBM i system.  If an IBM i system name is not provided, a signon dialog will be displayed.
 *  This optional parameter may be abbreviated <code>-s</code> or <code>-sys</code>.
 *  </dd>
 *  
 *  <dt><b><code>-userid </b></code><var>IBM i userID.</var></dt>
 *  <dd>Specifies the IBM i userId.    If an IBM i userID is not provided, a signon dialog will be displayed.
 *  This optional parameter may be abbreviated <code>-u</code> or <code>-uid</code>.
 *  </dd>
 *
 *  <dt><b><code>-password </b></code><var>IBM i password.</var></dt>
 *  <dd>Specifies the IBM i password.  If an IBM i password is not provided, a signon dialog will be displayed.
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
  private boolean debug_ = false;

  // The Templates object is threadsafe. We use it to pre-compile
  // the XSL code and generate new Transformer objects.
  private static Templates htmlTemplate_;
  private static Templates uimTemplate_;
  private static URIResolver defaultResolver_;

  private static String library_ = null;
  private static String command_ = null;

  private boolean showChoices_ = false;
  private String outputDirectory_ = "."; // Default to current directory

  // This instance has its own copy of the XSL transformer,
  // SAX parser, URI resolver, and entity resolver/handler.
  // Hence, the generateHTML() method is synchronized.
  private Transformer htmlTransformer_;
  private Transformer uimTransformer_;
  private final HelpResolver resolver_ = new HelpResolver();

  // Objects used to load the MRI and transform it into something
  // that the output HTML document can use.
  private static final String[] mriHTMLTags_ = new String[]
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

  // Objects used to load the MRI and transform it into something
  // that the output UIM document can use.
  private static final String[] mriUIMTags_ = new String[]
  {
    "HELP", 
    "HELP_FOR_COMMAND", 
    "INTRO_COMMAND_HELP", 
    "DESCRIBE_COMMAND", 
    "RESTRICTIONS_HEADING", 
    "RESTRICTION_AUT", 
    "RESTRICTION_THREADSAFE", 
    "LIST_SPECIAL_AUT", 
    "LIST_OTHER_AUT", 
    "LIST_THREADSAFE_RESTRICTIONS", 
    "DESCRIBE_OTHER_RESTRICTION", 
    "RESTRICTION_COMMENT", 
    "NO_PARAMETERS", 
    "EXAMPLES_HEADING", 
    "EXAMPLE_1_TITLE", 
    "DESCRIBE_EXAMPLE_1", 
    "EXAMPLE_2_TITLE", 
    "DESCRIBE_EXAMPLE_2", 
    "INTRO_EXAMPLE_HELP", 
    "ERROR_MESSAGES_HEADING", 
    "ERROR_MESSAGES_COMMENT_1", 
    "ERROR_MESSAGES_COMMENT_2", 
    "ERROR_MESSAGES_COMMENT_3", 
    "HELP_FOR_PARAMETER", 
    "INTRO_PARAMETER_HELP", 
    "DESCRIBE_PARAMETER", 
    "REQUIRED_PARAMETER", 
    "ELEMENT", 
    "QUALIFIER", 
    "VALUES_OTHER", 
    "VALUES_OTHER_REPEAT", 
    "VALUES_REPEAT", 
    "VALUES_SINGLE", 
    "VALUE_CHARACTER", 
    "VALUE_CL_VARIABLE_NAME", 
    "VALUE_COMMAND_STRING", 
    "VALUE_COMMUNICATIONS_NAME", 
    "VALUE_DATE", 
    "VALUE_DECIMAL_NUMBER", 
    "VALUE_GENERIC_NAME", 
    "VALUE_HEX", 
    "VALUE_INTEGER", 
    "VALUE_LOGICAL", 
    "VALUE_NAME", 
    "VALUE_NOT_RESTRICTED", 
    "VALUE_PATH_NAME", 
    "VALUE_SIMPLE_NAME", 
    "VALUE_TIME", 
    "VALUE_UNSIGNED_INTEGER", 
    "SPECIFY_VALUE", 
    "SPECIFY_NAME", 
    "SPECIFY_GENERIC_NAME", 
    "SPECIFY_PATH_NAME", 
    "SPECIFY_NUMBER", 
    "SPECIFY_CL_VARIABLE_NAME", 
    "SPECIFY_COMMAND_STRING", 
    "SPECIFY_DATE", 
    "SPECIFY_TIME", 
    "MULTIPLE_PARAMETER_VALUES_ALLOWED", 
    "MULTIPLE_ELEMENT_VALUES_ALLOWED", 
    "DESCRIBE_PREDEFINED_PARAMETER_VALUE", 
    "DESCRIBE_USERDEFINED_PARAMETER_VALUE", 
    "DESCRIBE_PARAMETER_DEFAULT", 
    "DESCRIBE_PARAMETER_VALUE_WITH_RANGE" 
  };

  private static final String[][] transformedHTMLParms_ = getTransformedHTMLParms(Locale.getDefault());
  // Don't ask for UIM MRI unless someone calls the UIM methods.  Do this so this tool
  // works with V5R2 MRI.
//  private static final String[][] transformedUIMParms_ = getTransformedUIMParms(Locale.getDefault());
  //private static String[][] transformedUIMParms_;

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

      String docXSLURI = CommandHelpRetriever.class.getClassLoader().getResource("com/ibm/as400/util/gencmddoc.xsl").toString();

      if (Trace.isTraceOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Loading gencmdhlp.xsl.");
      }

      String hlpXSLURI = CommandHelpRetriever.class.getClassLoader().getResource("com/ibm/as400/util/gencmdhlp.xsl").toString();

      if (Trace.isTraceOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Loading XSL templates.");
      }

      TransformerFactory factory = TransformerFactory.newInstance();
      defaultResolver_ = factory.getURIResolver();
      htmlTemplate_ = factory.newTemplates(new StreamSource(docXSLURI));
      uimTemplate_ = factory.newTemplates(new StreamSource(hlpXSLURI));
    }
    catch (Exception e)
    {
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.ERROR, "Unable to initialize CommandHelpRetriever XSL and MRI.", e);
      }

      throw new RuntimeException(e.toString());
    }
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
      
      // If the transformer happens to load the __NO_HELP document, make something
      // available.  In Sun's later JVMs this gets called.  For the J9 JVM this
      // doesn't get called. @C2A
      if (href.indexOf("__NO_HELP") > -1)
      {
        return new StreamSource(new StringReader("<help>NO HELP AVAILABLE</help>"));
      }
      
      return defaultResolver_.resolve(href, base);
    }
  }


  private static boolean genUIM_ = false;
  private static boolean genHTML_ = true;

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
          if (genHTML_)
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
            try { out.write(html); }
            finally { if (out != null) out.close(); }
          }
          if (genUIM_)
          {
            String uim = utility.generateUIM(cmds[i]);

            File outFile = null;
            if (isDir)
            {
              QSYSObjectPathName path = new QSYSObjectPathName(cmds[i].getPath());
              String filename = path.getLibraryName()+"_"+path.getObjectName()+".uim";
              outFile = new File(outDir, filename);
            }
            else
            {
              outFile = outDir;
            }

            FileWriter out = new FileWriter(outFile);
            try { out.write(uim); }
            finally { if (out != null) out.close(); }
          }
        }
        catch (Exception e1)
        {
          e1.printStackTrace();
        }
      }
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
   * We don't want to HTML-encode the UIM strings, but we still
   * need to replace the ampersands so the XSL doesn't get confused.
  **/
  private static final String encodeAmp(String source)
  {
    StringBuffer dest = new StringBuffer();
    char[] buf = source.toCharArray();
    for (int i=0; i<buf.length; ++i)
    {
      if (buf[i] == '&') dest.append("&amp;");
      else dest.append(buf[i]);
    }
    return dest.toString();
  }


  /**
   * Generates IBM-formatted command help documentation for the specified CL command.
   * Portions of the resulting HTML will contain strings that were translated using
   * the {@link java.util.Locale Locale} specified on the {@link com.ibm.as400.access.AS400 AS400}
   * object for the given {@link com.ibm.as400.access.Command Command}.
   *
   * <p>Note: While the String being returned is a typical UTF-16 Java String, the contents
   * of the String is an HTML file with a META tag that defines the charset as UTF-8.
   * Applications that use the String returned by this method should then be sure to convert
   * the contents to UTF-8 bytes, or replace the charset tag inside the HTML with whichever
   * encoding the application chooses to convert to. See {@link #generateHTMLBytes generateHTMLBytes()}.
   *
   * @param command The command.
   * @return An HTML string consisting of the help documentation for the command.
   * @see java.util.Locale
   * @see com.ibm.as400.access.AS400
   * @see com.ibm.as400.access.Command
  **/
  public synchronized String generateHTML(Command command) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException,
  SAXException,
  ParserConfigurationException, 
  TransformerConfigurationException, TransformerException
  {
    if (command == null)
      throw new NullPointerException("command");

    return generate(command, null);
  }

  /**
   * Generates IBM-formatted command help documentation for the specified CL command.
   * Portions of the resulting HTML will contain strings that were translated using
   * the {@link java.util.Locale Locale} specified on the {@link com.ibm.as400.access.AS400 AS400}
   * object for the given {@link com.ibm.as400.access.Command Command}.
   *
   * <p>Note: While the String being returned is a typical UTF-16 Java String, the contents
   * of the String is an HTML file with a META tag that defines the charset as UTF-8.
   * Applications that use the String returned by this method should then be sure to convert
   * the contents to UTF-8 bytes, or replace the charset tag inside the HTML with whichever
   * encoding the application chooses to convert to. See {@link #generateHTMLBytes generateHTMLBytes()}.
   *
   * @param command The command.
   * @param panelGroup The panel group used to generate the help text, instead of the Command's defined panel group.
   * @return An HTML string consisting of the help documentation for the command.
   * @see java.util.Locale
   * @see com.ibm.as400.access.AS400
   * @see com.ibm.as400.access.Command
   * @see com.ibm.as400.access.PanelGroup
  **/
  public synchronized String generateHTML(Command command, PanelGroup panelGroup) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException,
  SAXException,
  ParserConfigurationException, 
  TransformerConfigurationException, TransformerException
  {
    if (command == null)
      throw new NullPointerException("command");
    if (panelGroup == null)
      throw new NullPointerException("panelGroup");

    return generate(command, panelGroup);
  }

  /**
   * Generates IBM-formatted command help documentation for the specified CL command.
   * Portions of the resulting HTML will contain strings that were translated using
   * the {@link java.util.Locale Locale} specified on the {@link com.ibm.as400.access.AS400 AS400}
   * object for the given {@link com.ibm.as400.access.Command Command}.
   *
   * <p>Note: The byte array returned by this method has already been encoded in UTF-8 in order to
   * match the charset tag within the HTML document.
   *
   * @param command The command.
   * @param panelGroup The panel group used to generate the help text. Specify null to use the Command's defined panel group.
   * @return An HTML document encoded in UTF-8 bytes, consisting of the help documentation for the command.
   * @see java.util.Locale
   * @see com.ibm.as400.access.AS400
   * @see com.ibm.as400.access.Command
   * @see com.ibm.as400.access.PanelGroup
  **/
  public synchronized byte[] generateHTMLBytes(Command command, PanelGroup panelGroup) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException,
  SAXException,
  ParserConfigurationException, 
  TransformerConfigurationException, TransformerException
  {
    if (command == null)
      throw new NullPointerException("command");

    return generate(command, panelGroup).getBytes("UTF-8");
  }

  /**
   * Generates IBM-formatted command help documentation for the specified CL command.
   * Portions of the resulting HTML will contain strings that were translated using
   * the {@link java.util.Locale Locale} specified on the {@link com.ibm.as400.access.AS400 AS400}
   * object for the given {@link com.ibm.as400.access.Command Command}.
   * @param command The command.
   * @param panelGroup The panel group used to generate the help text, instead of the Command's defined panel group.
   * @return An HTML string consisting of the help documentation for the command.
   * @see java.util.Locale
   * @see com.ibm.as400.access.AS400
   * @see com.ibm.as400.access.Command
   * @see com.ibm.as400.access.PanelGroup
  **/
  private synchronized String generate(Command command, PanelGroup panelGroup) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException,
  SAXException,
  ParserConfigurationException, 
  TransformerConfigurationException, TransformerException
  {
     if (Trace.isTraceOn())
      Trace.log(Trace.DIAGNOSTIC, "Generating HTML documentation for '"+command+"' and panel group '"+panelGroup+"'.");

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
      try { fw.write(xml); }
      finally { if (fw != null) fw.close(); }
    }

    if (Trace.isTraceOn())
      Trace.log(Trace.DIAGNOSTIC, "Retrieved command XML:\n"+xml+"\n");

    String threadSafe = null;
    String whereAllowedToRun = null;

    try
    {
      switch (command.getThreadSafety())
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

    String helpResults = null;
    try
    {
      helpResults = (panelGroup == null ? command.getXMLHelpText() : command.getXMLHelpText(panelGroup));
    }
    catch (AS400Exception e)
    {
      AS400Message[] msgs = e.getAS400MessageList();

      // 
      // Note:  It is also possible to get a CPF6E3B when the user is not authorized to the help text.
      //        However, we do not see the underlying message that the user is not authorized. 
      //       @C2A
      // CPF6250 - Can't retrieve info for command. Probably because it is a system command.
      if (msgs.length != 1 || 
    		  ( !msgs[0].getID().toUpperCase().trim().equals("CPF6250") &&
    			!msgs[0].getID().toUpperCase().trim().equals("CPF6E3B")	  ))
        throw new AS400Exception(msgs);
    }

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
      try { fw.write(helpResults); }
      finally { if (fw != null) fw.close(); }
    }

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
    setupHTMLTransformer(command.getSystem().getLocale());

    htmlTransformer_.setParameter("CommandHelp", helpResults == null || helpResults.trim().length() == 0 ? "__NO_HELP" : "myCommandHelpResolver");
    htmlTransformer_.setParameter("ShowChoicePgmValues", showChoices_ ? "1" : "0");
    htmlTransformer_.setParameter("ThreadSafe", threadSafe == null ? "0" : threadSafe);
    htmlTransformer_.setParameter("WhereAllowed", whereAllowedToRun == null ? "000000000000000" : whereAllowedToRun);

    resolver_.helpResults_ = helpResults;

    StreamSource sourceXML = new StreamSource(new StringReader(xml));
    StringWriter buf = new StringWriter();
    StreamResult output = new StreamResult(buf);

    if (Trace.isTraceOn()) {
      Trace.log(Trace.DIAGNOSTIC, "Performing XSL transform.");
      Trace.log(Trace.DIAGNOSTIC, "Help Results is "+helpResults); 
    }
    try { 
      htmlTransformer_.transform(sourceXML, output);
    } catch (TransformerException e) {
    	 if (Trace.isTraceOn()) {
    	      Trace.log(Trace.DIAGNOSTIC, "Exception from transform.");
    	      Trace.log(Trace.DIAGNOSTIC, e); 
    	 }
    	throw e; 
    }
    if (Trace.isTraceOn())
      Trace.log(Trace.DIAGNOSTIC, "Successfully generated help documentation.");
 
    return buf.toString();
  }


  /**
   * Generates an IBM-formatted UIM template for the specified CL command.
   * Portions of the resulting UIM output will contain strings that were translated using
   * the {@link java.util.Locale Locale} specified on the {@link com.ibm.as400.access.AS400 AS400}
   * object for the given {@link com.ibm.as400.access.Command Command}.
   * @param command The command.
   * @return A UIM string consisting of the UIM template for the command.
   * @see java.util.Locale
   * @see com.ibm.as400.access.AS400
   * @see com.ibm.as400.access.Command
  **/
  public synchronized String generateUIM(Command command) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException,
  SAXException,
  ParserConfigurationException, 
  TransformerConfigurationException, TransformerException
  {
    if (command == null)
      throw new NullPointerException("command");

    if (Trace.isTraceOn())
      Trace.log(Trace.DIAGNOSTIC, "Generating UIM documentation for "+command+".");

    String xml = null;
    try
    {
      xml = command.getXMLExtended();
    }
    catch(AS400Exception ae)
    {
      AS400Message[] msgs = ae.getAS400MessageList();
      if (msgs.length == 1 && msgs[0].getID().equalsIgnoreCase("CPF3C21"))
      {
        if (Trace.isTraceOn())
        {
          Trace.log(Trace.WARNING, "Extended command XML format not supported by this system. Using older version.");
        }
        xml = command.getXML();
      }
      else
      {
        throw ae;
      }
    }

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
        outFile = new File(outDir, lib + "_" + name + "_XML_X.xml");
      else
        outFile = new File(outputDirectory_ + "_XML_X.xml");

      FileWriter fw = new FileWriter(outFile);
      try { fw.write(xml); }
      finally { if (fw != null) fw.close(); }
    }

    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Retrieved extended command XML:\n"+xml+"\n");
    }

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
    setupUIMTransformer(command.getSystem().getLocale());

    StreamSource sourceXML = new StreamSource(new StringReader(xml));
    StringWriter buf = new StringWriter();
    StreamResult output = new StreamResult(buf);

    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Performing XSL transform of UIM.");
    }

    uimTransformer_.transform(sourceXML, output);

    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Successfully generated UIM documentation.");
    }

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
  private static final String[][] getTransformedHTMLParms(Locale locale)
  {
    String[][] parms = new String[mriHTMLTags_.length+4][];
    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Transforming CommandHelpRetriever HTML MRI for locale: "+locale);
    }
    ResourceBundle bundle = ResourceBundle.getBundle("com.ibm.as400.access.MRI", locale);
    ResourceBundle bundle2 = ResourceBundle.getBundle("com.ibm.as400.access.MRI2", locale);

    int i=0;
    for (; i<mriHTMLTags_.length; ++i)
    {
      parms[i] = new String[] { "_"+mriHTMLTags_[i], encode(bundle2.getString("GENCMDDOC_"+mriHTMLTags_[i]))};
    }
    parms[i++] = new String[] { "_DESCRIPTION", encode(bundle2.getString("NETSERVER_DESCRIPTION_NAME"))};
    parms[i++] = new String[] { "_THREADSAFE_NO", encode(bundle.getString("DLG_NO_BUTTON"))};
    parms[i++] = new String[] { "_THREADSAFE_YES", encode(bundle.getString("DLG_YES_BUTTON"))};
    parms[i] = new String[] { "_TYPE_NAME", encode(bundle2.getString("NETSERVER_NAME_NAME"))};

    return parms;
  }


  /**
   * Used to get the translated MRI for a given Locale.
  **/
  private static final String[][] getTransformedUIMParms(Locale locale)
  {
    String[][] parms = new String[mriUIMTags_.length][];
    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Transforming CommandHelpRetriever UIM MRI for locale: "+locale);
    }
    ResourceBundle bundle2 = ResourceBundle.getBundle("com.ibm.as400.access.MRI2", locale);

    for (int i=0; i<mriUIMTags_.length; ++i)
    {
      parms[i] = new String[] { "_"+mriUIMTags_[i], encodeAmp(bundle2.getString("GENCMDDOC_"+mriUIMTags_[i]))};
    }

    return parms;
  }


  /**
   *  Parse out the command line arguments for the CommandHelpRetriever command.
  **/
  private static AS400 parseParms(String args[], CommandHelpRetriever utility) throws Exception
  {
    String s,u,p,l,c,pv,o,d,uim,html;

    Vector v = new Vector();
    v.addElement("-system");
    v.addElement("-userid");
    v.addElement("-password");                  
    v.addElement("-library");
    v.addElement("-command");
    v.addElement("-showChoicePgmValues");
    v.addElement("-output");
    v.addElement("-debug");
    v.addElement("-uim");
    v.addElement("-html");

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
      utility.setOutputLocation(o);

    d = arguments.getOptionValue("-debug");
    if (d != null)
    {
      if (d.length() == 0 || d.equalsIgnoreCase("true"))
        utility.setDebug(true);
    }

    genUIM_ = false;
    genHTML_ = true;
    uim = arguments.getOptionValue("-uim");
    if (uim != null && (uim.length() == 0 || uim.equalsIgnoreCase("true")))
    {
      genUIM_ = true;
      genHTML_ = false;
    }
    html = arguments.getOptionValue("-html");
    if (html != null && (html.length() == 0 || html.equalsIgnoreCase("true")))
    {
      genHTML_ = true;
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
    if (location == null)         //@A1A
      throw new NullPointerException("location");   //@A1A

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
  private void setupHTMLTransformer(Locale locale) throws TransformerConfigurationException
  {
    htmlTransformer_ = htmlTemplate_.newTransformer();
    htmlTransformer_.setURIResolver(resolver_);
    String[][] transformedParms = null;
    if (locale == null) // Just use the default locale of the JVM. We already pre-loaded that MRI.
    {
      transformedParms = transformedHTMLParms_;
    }
    else
    {
      transformedParms = getTransformedHTMLParms(locale);
    }
    for (int i=0; i<transformedParms.length; ++i)
    {
      htmlTransformer_.setParameter(transformedParms[i][0], transformedParms[i][1]);
    }
  }


  /**
   * Resets our internal XSL Transformer object and makes it ready
   * for the next call to transform().
  **/
  private void setupUIMTransformer(Locale locale) throws TransformerConfigurationException
  {
    uimTransformer_ = uimTemplate_.newTransformer();
    String[][] transformedParms = null;
    if (locale == null) // Just use the default locale of the JVM. We already pre-loaded that MRI.
    {
      //transformedParms = transformedUIMParms_;
      transformedParms = getTransformedUIMParms(Locale.getDefault());
    }
    else
    {
      transformedParms = getTransformedUIMParms(locale);
    }
    for (int i=0; i<transformedParms.length; ++i)
    {
      uimTransformer_.setParameter(transformedParms[i][0], transformedParms[i][1]);
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
    System.out.println ("  -library library");
    System.out.println ("  -command command");
    System.out.println ();
    System.out.println (optionsuc + ":");
    System.out.println ();
    System.out.println ("  [ -html ]");
    System.out.println ("  [ -uim ]");
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

