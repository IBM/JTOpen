///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RunJavaApplication.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package utilities;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.JavaApplicationCall;
import com.ibm.as400.access.Trace;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

/**
  * <P>
  * RunJavaApplication demonstrates the use of com.ibm.as400.access.JavaApplicationCall.
  * It gathers information from the user about the class to run, then uses a
  * JavaApplicationCall object to run the program on the i5/OS system's JVM.  It
  * uses the capabilities of JavaApplicationCall to send input to the Java
  * program and displays output the Java program writes to standard out and
  * standard error.
  *
  * <P>
  * RunJavaApplication has three command line parameters.  All parameters are optional.
  *
  * <UL>
  * <i>system</i> - the name of the system.
  * <i>userid</i> - run the Java program under this userid.<BR>
  * <i>password</i> - the password of the user. <BR>
  * </UL>
  *
  * <p>
  * Once started, four commands can be run:
  * <UL>
  * <li><i>set</i> - set options to define the JVM environment on the system.
  * <li><i>java</i> - run a Java application on i5/OS system.
  * <li><i>help</i> - display help.
  * <li><i>quit</i> - end the application.
  * </UL>
  *
  * <p>
  * You can set the following options via the set command.  For more information
  * on each option display the on-line help for the Java command on the system.
  * <UL>
  * <li>Classpath - the CLASSPATH environment variable on the system.
  * <li>DefaultPort - the default port for communicating standard in, standard out
  * and standard error between the client and the server Java program.
  * <li>FindPort - Indicates to search for a port if the specified port is in use
  * <li>Interpret - Indicates if all Java class files should be run interpretively.
  * <li>Optimize - Optimization level of the Java classes that are not already optimized.
  * <li>Options - Options to pass to the i5/OS system's JVM.
  * <li>SecurityCheckLevel - The level of warnings for writable directories in CLASSPATH.
  * <li>GarbageCollectionFrequency  - The relative frequency that garbage collection runs.
  * <li>GarbageCollectionInitialSize - The initial size, in kilobytes, of the garbage collection heap.
  * <li>GarbageCollectionMaximumSize - The maximum size, in kilobytes, that the garbage collection heap can grow to.
  * <li>GarbageCollectionPriority - The priority of the tasks running garbage collection.
  * </UL>
  *
  * <p>
  * The format of the java command is:
  * <a name="ex"> </a>
  * <UL>
  * java [-classpath=<value>] [-verbose] [-D<property>=<value> [...]] class [<programParameter1> [...]]
  * </UL>
  *
 **/

public class RunJavaApplication
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static JavaApplicationCall runMain_ = null;
    // Where MRI comes from.
    private static ResourceBundle resources_ = ResourceBundle.getBundle("utilities.UTMRI");
    private static RunJavaApplicationThread errorThread_ = null;
    private static RunJavaApplicationThread inputThread_ = null;
    private static RunJavaApplicationThread outputThread_ = null;
    private static RunJavaApplicationThread runApplication_ = null;
    private static Vector inputLines_ = new Vector();
    private static String classpath_ = "";
    private static String[] option_ = {"*NONE"};

    /**
      * Runs Java programs on the i5/OS system's JVM.
      * @param parameters The command line parameters.  See the prolog
      *                   of this class for information on the command line parameters.
     **/
    public static void main(String[] parameters)
    {
        RunJavaApplication rja = null;
        String inputString = null;

        AS400 as400 = new AS400();

        try
        {
           if (parameters.length > 0)
              as400.setSystemName(parameters[0]);

           if (parameters.length > 1)
              as400.setUserId(parameters[1]);

           if (parameters.length > 2)
              as400.setPassword(parameters[2]);

           runMain_ = new JavaApplicationCall(as400);

           // starts input,output,error thread
           inputThread_ = new RunJavaApplicationThread(runMain_, RunJavaApplicationThread.INPUT);
           inputThread_.start();

           outputThread_ = new RunJavaApplicationThread(runMain_, RunJavaApplicationThread.OUTPUT);
           outputThread_.start();

           errorThread_ = new RunJavaApplicationThread(runMain_, RunJavaApplicationThread.ERROR);
           errorThread_.start();

           runApplication_ = new RunJavaApplicationThread(runMain_, RunJavaApplicationThread.RUN_APPLICATION);

           System.out.print(resources_.getString("REMOTE_PROMPT"));

           while(true)
           {
               String is = null;

               if (inputLines_.size() > 0)
               {
                   is = (String)inputLines_.elementAt(0);
                   inputLines_.removeElementAt(0);
               }

               if (is != null)
               {
                   if (!runApplication_.isAlive())
                   {
                       inputString = is.toUpperCase().trim();

                       if ((inputString.equals("QUIT")) || (inputString.equals("Q")))    // quit
                       {
                           System.exit(0);
                       }

                       else if (inputString.startsWith("JAVA "))   // java
                       {
                           if (preProcessJavaCommand(runMain_, is.substring(4)))
                           {
                               runApplication_ = new RunJavaApplicationThread(runMain_, RunJavaApplicationThread.RUN_APPLICATION);
                               runApplication_.start();
                           }
                           else
                               showPrompt();
                       }

                       else if (inputString.startsWith("SET "))   // set properties
                       {
                           String pair = is.substring((new String("SET ")).length());
                           String property = null;
                           String value = null;
                           int index = pair.indexOf("=");
                           if ((index > 0) && (!pair.endsWith("=")))
                           {
                               property = pair.substring(0,index);
                               value = pair.substring(index+1);
                               property = property.trim();
                               setProperty(runMain_, property.toUpperCase(), value);
                           }
                           else
                           {
                               System.out.println(resources_.getString("REMOTE_ERR_SET"));
                           }
                           showPrompt();
                       }

                       else if ((inputString.equals("HELP")) ||
                                (inputString.equals("?"))    ||
                                (inputString.equals("H")))       // help
                       {
                           showHelp();
                           showPrompt();
                       }

                       else if (inputString.equals("D"))         // display properties
                       {
                           displayProperties();
                           showPrompt();
                       }

                       else
                       {
                           if (inputString.equals(""));          // enter

                           else if (inputString.equals("JAVA"))
                               System.out.println(resources_.getString("REMOTE_ERR_JAVA"));

                           else if (inputString.equals("SET"))
                           {
                               displayProperties();
                               // System.out.println(resources_.getString("REMOTE_ERR_SET"));
                           }

                           else
                               System.out.println(getMRIResource().getString("REMOTE_BAD_COMMAND"));

                           showPrompt();
                       }
                   }
                   else
                   {   // send the string to system
                       runMain_.sendStandardInString(is);
                   }
               }

               Thread.sleep(100);

           } // end of the loop
        }
        catch (Exception e)
        {
           e.printStackTrace();
        }
    }

    /**
        Displays the properties.
    **/
    private static void displayProperties()
    {
        String optionString = "";
        for (int i = 0 ; i < option_.length ; i++)
            optionString = optionString + option_[i] + " ";

        System.out.println(getMRIResource().getString("REMOTE_D_LINE1"));
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE2")  + runMain_.getSecurityCheckLevel());
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE3")  + classpath_);
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE4")  + runMain_.getGarbageCollectionFrequency());
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE5")  + runMain_.getGarbageCollectionInitialSize());
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE6")  + runMain_.getGarbageCollectionMaximumSize());
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE7")  + runMain_.getGarbageCollectionPriority());
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE8")  + runMain_.getInterpret());
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE9")  + runMain_.getOptimization());
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE10") + optionString);
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE11") + runMain_.getDefaultPort());
        System.out.println("   "+getMRIResource().getString("REMOTE_D_LINE12") + runMain_.isFindPort());

    }

    /**
       Loads the resource bundle if not already done.

       @return The resource bundle for this class.
    **/
    private static ResourceBundle getMRIResource()
    {
        if (resources_ == null)
            resources_ = ResourceBundle.getBundle("utilities.UTMRI");
        return resources_;
    }

    /**
       Returns input buffer.

       @return The input buffer used by command line input.
    **/
    protected static Vector getInputBuffer()
    {
        return inputLines_;
    }

    /**
        Preprocess the java command.
    **/
    private static boolean preProcessJavaCommand(JavaApplicationCall jac, String inputStr)
    {

        StringTokenizer strToken = new StringTokenizer(inputStr.trim() , " ");
        int paramNum = strToken.countTokens();

        // save strings of strToken into Vector command.
        Vector command = new Vector();
        for (int i = 0 ; i < paramNum ; i++)
            command.addElement(strToken.nextToken());

        // 1. find class name
        int indexOfClass = 0;
        boolean classExist = false;
        while(indexOfClass < paramNum)
        {
            String str = (String)command.elementAt(indexOfClass);
            if (str.indexOf("-") == -1)
            {
                classExist = true;
                break;
            }
            indexOfClass++;
        }

        if (!classExist)
        {
            System.out.println(resources_.getString("REMOTE_ERR_JAVA"));
            return false;
        }
        else
        {
            // 2. handle -classpath, -verbose, -D
            int numOfClasspath = 0;
            int numOfVerbose = 0;
            int numOfProperty = 0;
            boolean tempClasspathExist = false;
            boolean verboseExist = false;

            // String properties = "";
            Properties properties = new Properties();;

            for (int i = 0 ; i < indexOfClass ; i++)
            {
                String env = (String)command.elementAt(0);
                command.removeElementAt(0);
                String upperEnv = env.toUpperCase();
                if (upperEnv.indexOf("-CLASSPATH=") == 0)
                {
                    numOfClasspath++;
                    if (numOfClasspath > 1)
                    {
                        System.out.println(resources_.getString("REMOTE_ERR_JAVA"));
                        return false;
                    }
                    try
                    {
                        int index = env.indexOf("=");
                        String path = env.substring(index);
                        if (!(path.endsWith("=") || index <0))
                            path = path.replace((char)('='),(char)(' ')) + " ";
                        else
                            throw new Exception("classPath");
                        jac.setClassPath(path.trim());
                        tempClasspathExist = true;
                    }
                    catch (Exception e)
                    {
                        System.out.println(resources_.getString("REMOTE_ERR_JAVA"));
                        return false;
                    }
                }
                else if (upperEnv.indexOf("-VERBOSE") == 0)
                {
                    numOfVerbose++;
                    if (numOfVerbose > 1)
                    {
                        System.out.println(resources_.getString("REMOTE_ERR_JAVA"));
                        return false;
                    }

                    String[] curOption = jac.getOptions();
                    boolean alreadyExist = false;
                    for (int j = 0 ; j < curOption.length ; j++)
                        if (curOption[j].equals("*VERBOSE"))
                        {
                            alreadyExist = true;
                            verboseExist = true;
                            break;
                        }

                    try
                    {
                        if (!alreadyExist)
                        {
                            String[] newOpt = new String[curOption.length + 1];
                            for (int optidx = 0 ; optidx < curOption.length ; optidx++)
                                newOpt[optidx] = curOption[optidx];
                            newOpt[newOpt.length-1] = "*VERBOSE";
                            jac.setOptions(newOpt);
                            verboseExist = true;
                        }
                    }
                    catch (PropertyVetoException e)
                    {
                        System.out.println(resources_.getString("REMOTE_ERR_JAVA"));
                        return false;
                    }
                }
                else if (upperEnv.indexOf("-D") == 0)
                {
                    try
                    {
                        int len = new String("-D").length();
                        String prop =env.substring(len);
                        int index = prop.indexOf("=");
                        if (!(prop.endsWith("=") || index <=0))
                            properties.put(prop.substring(0,index), prop.substring(index+1));
                        else
                            throw new Exception("properties");
                    }
                    catch(Exception e)
                    {
                        System.out.println(resources_.getString("REMOTE_ERR_JAVA"));
                        return false;
                    }
                }
                else
                {
                    System.out.println(resources_.getString("REMOTE_ERR_JAVA"));
                    return false;
                    }

                }

                // set java application, -D
                String application = (String)command.elementAt(0);
                command.removeElementAt(0);
                try
                {
                    if (!tempClasspathExist)
                        jac.setClassPath(classpath_);

                    if (!verboseExist)
                        jac.setOptions(option_);

                    if (!properties.isEmpty())
                            jac.setProperties(properties);

                    jac.setJavaApplication(application);
                }
                catch(Exception e)
                {
                }

            // 3. handle parameters of java application
            Vector appParm = new Vector();
            for (int parmNum = 0 ; parmNum < command.size(); parmNum++)
                appParm.addElement((String)command.elementAt(parmNum));

            try
            {
                if (appParm.size() > 0)
                {
                    String[] parmArray = new String[appParm.size()];
                    for (int i = 0 ; i < appParm.size() ; i++)
                        parmArray[i] = (String)appParm.elementAt(i);
                    jac.setParameters(parmArray);
                }
            }
            catch (Exception e)
            {
            }
        }
        return true;
    }


    /**
        Sets JavaApplicationCall object's property.
    **/
    private static void setProperty(JavaApplicationCall jac, String property, String value)
    {
        try
        {
            value = value.trim();

            if (value.length() == 0)
            {
               System.out.println(resources_.getString("REMOTE_ERR_SET"));
            }
            else
            {
               if (property.equals("CLASSPATH"))
               {
                   jac.setClassPath(value);
                   classpath_ = value;
               }

               else if (property.equals("SECURITYCHECKLEVEL"))
                   jac.setSecurityCheckLevel(value);

               else if (property.equals("OPTION"))
               {
                   StringTokenizer st = new StringTokenizer(value.trim()," ");
                   String[] options = new String[st.countTokens()];

                   for (int i = 0 ; i < options.length ; i++)
                       options[i] = st.nextToken();

                   jac.setOptions(options);
                   option_ = options;
               }

               else if (property.equals("INTERPRET"))
                   jac.setInterpret(value);

               else if (property.equals("GARBAGECOLLECTIONINITIALSIZE"))
               {
                   Integer val = new Integer(value);
                   jac.setGarbageCollectionInitialSize(val.intValue());
               }

               else if (property.equals("GARBAGECOLLECTIONMAXIMUMSIZE"))
                   jac.setGarbageCollectionMaximumSize(value);

               else if (property.equals("GARBAGECOLLECTIONFREQUENCY"))
               {
                   Integer val = new Integer(value);
                   jac.setGarbageCollectionFrequency(val.intValue());
               }

               else if (property.equals("GARBAGECOLLECTIONPRIORITY"))
               {
                   Integer val = new Integer(value);
                   jac.setGarbageCollectionPriority(val.intValue());
               }

               else if (property.equals("OPTIMIZE"))
                   jac.setOptimization(value);

               else if (property.equals("DEFAULTPORT"))
               {
                   Integer val = new Integer(value);
                   jac.setDefaultPort(val.intValue());
               }

               else if (property.equals("FINDPORT"))
               {
                   String val = value.toUpperCase();

                   if (val.equals("TRUE"))
                     jac.setFindPort(true);
                   else if (val.equals("FALSE"))
                     jac.setFindPort(false);
                   else
                      System.out.println(getMRIResource().getString("REMOTE_PORT_VALUE_ERROR"));
               }

               else
                   System.out.println(getMRIResource().getString("REMOTE_SET_PROPERTY_1")
                                      + " "+property + " "
                                      + getMRIResource().getString("REMOTE_SET_PROPERTY_2"));
           }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    /**
        Displays command syntax.
    **/
    private static void showHelp()
    {
      //System.out.println(getMRIResource().getString("REMOTE_HELP_LINE0"));
        System.out.println(getMRIResource().getString("REMOTE_HELP_LINE1"));
        System.out.println("  " + getMRIResource().getString("REMOTE_HELP_LINE2"));
        System.out.println("       " + getMRIResource().getString("REMOTE_HELP_LINE3"));
        System.out.println(getMRIResource().getString("REMOTE_HELP_LINE4"));
        System.out.println("  " + getMRIResource().getString("REMOTE_HELP_LINE5"));
        System.out.println("  " + getMRIResource().getString("REMOTE_HELP_LINE6"));
        System.out.println("  " + getMRIResource().getString("REMOTE_HELP_LINE7"));
        System.out.println("  " + getMRIResource().getString("REMOTE_HELP_LINE8"));
        System.out.println("  " + getMRIResource().getString("REMOTE_HELP_LINE9"));
        System.out.println("  " + getMRIResource().getString("REMOTE_HELP_LINE10"));
        System.out.println(getMRIResource().getString("REMOTE_HELP_LINE11"));
        System.out.println(getMRIResource().getString("REMOTE_HELP_LINE12"));
        System.out.println(getMRIResource().getString("REMOTE_HELP_LINE13"));
    }

    private static void showPrompt()
    {
        System.out.println();
        System.out.print(resources_.getString("REMOTE_PROMPT"));
    }
}
