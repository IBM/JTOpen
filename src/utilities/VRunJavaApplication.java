///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VRunJavaApplication.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package utilities;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.JavaApplicationCall;
import com.ibm.as400.vaccess.ErrorDialogAdapter;
import com.ibm.as400.vaccess.VJavaApplicationCall;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.*;

/**
 * <P>
 * VRunJavaApplication demonstrates the use of the Toolbox
 * com.ibm.as400.vaccess.VJavaApplicationCall component.
 * The component (VJavaApplicationCall) does processing necessary to
 * call a Java program on the server but it is not a complete
 * application.  This class is a complete application.  It does
 * setup work such as parsing command line parameters and
 * creating a swing frame, then uses VJavaApplicationCall
 * to call the Java program.
 *
 * <P>
 * VRunJavaApplication has three parameters command line parameters.  All
 * parameters are optional.  The parameters are:
 * <UL>
 *   <i>System</i> - the server that contains the Java program <BR>
 *   <i>UserID</i> - run the Java program under this userid    <BR>
 *   <i>Password</i> - the password for the userid             <BR>
 * </UL>
 *
 * <P>
 * For example, to run Java programs on server "mySystem":
 * <a name="ex"> </a>
 * <UL>
 * java utilities.VRunJavaApplication mySystem
 * </UL>
 *
 * <P>
 * See the javadoc for Toolbox class VJavaApplicationCall and
 * JavaApplicationCall for a list of commands to run once
 * the program is started.
 *
**/

public class VRunJavaApplication
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private VJavaApplicationCall vJavaAppCall = null;
    private static ResourceBundle resources_ = null;

    /**
     * Loads the resource bundle if not already done.
     * @return The resource bundle for this class.
    **/
    private static ResourceBundle getMRIResource()
    {
      if (resources_ == null)
          resources_ = ResourceBundle.getBundle("utilities.UTMRI");
      return resources_;
    }

    /**
     *  Starts the utility.
     *
     *  @param args Command line arguments.  See the prolog of this
     *             class for information on the command line parameters.
    **/
    public static void main (String[] args)
    {
        try
        {
            // Create an AS400 object.  The system name was passed
            // as the first command line argument.
            AS400 system = new AS400 ();

            if(args.length > 3)
            {
                System.out.println(getMRIResource().getString("REMOTE_MORE_PARAMETER"));
                return;
            }
            else
            {
                if( args.length > 0 )
                    system.setSystemName(args[0]);

                if( args.length > 1 )
                    system.setUserId(args[1]);

                if( args.length > 2 )
                    system.setPassword(args[2]);

                 JavaApplicationCall  javaAppCall = new  JavaApplicationCall(system);
                VJavaApplicationCall vJavaAppCall = new VJavaApplicationCall(javaAppCall);

                JFrame f = new JFrame (getMRIResource().getString("REMOTE_WINDOW_TITLE"));

                // Create an error dialog adapter.  This will display
                // any errors to the user.
                ErrorDialogAdapter errorHandler = new ErrorDialogAdapter (f);
                f.addWindowListener (
                   new WindowAdapter()
                      { public void windowClosed(WindowEvent event)
                           { System.exit(0); }
                        public void windowClosing(WindowEvent event)
                           {System.exit(0); }
                      });

                f.getContentPane().setLayout (new BorderLayout ());
                f.getContentPane().add ("Center", vJavaAppCall);

                f.pack();
                f.show ();
            }
        }
        catch (Exception e)
        {
           System.out.println (e.getMessage ());
           System.exit(0);
        }
    }
}

