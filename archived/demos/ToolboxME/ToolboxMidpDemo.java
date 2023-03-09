//////////////////////////////////////////////////////////////////////////////////
//
// ToolboxME for iSeries example. This program is an example that shows how
// ToolboxME for iSeries can use PCML to access data and services on an
// iSeries server.
//
// This application requires that the qsyrusri.pcml file is present in the
// CLASSPATH of the MEServer.
//
//////////////////////////////////////////////////////////////////////////////////

import java.io.*;
import java.sql.*;
import java.util.Hashtable;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

import com.ibm.as400.micro.*;


public class ToolboxMidpDemo extends MIDlet implements CommandListener
{
    private Display   display_;

    // A ToolboxME system object.
    private AS400 system_;

    private List      main_ = new List("Toolbox MIDP Demo", Choice.IMPLICIT);

    // Create a form for each component.
    private Form      signonForm_;
    private Form      cmdcallForm_;
    private Form      pgmcallForm_;
    private Form      dataqueueForm_;
    private Form      aboutForm_;

    // Visable Text for each component.
    static final String SIGN_ON       = "SignOn";
    static final String COMMAND_CALL  = "CommandCall";
    static final String PROGRAM_CALL  = "ProgramCall";
    static final String DATA_QUEUE    = "DataQueue";
    static final String ABOUT         = "About";

    static final String NOT_SIGNED_ON = "Not signed on.";
    static final String DQ_READ       = "Read";
    static final String DQ_WRITE      = "Write";

    // A ticker to display the signon status.
    private Ticker    ticker_ = new Ticker(NOT_SIGNED_ON);

    // Commands that can be performed.
    private static final Command actionExit_   = new Command("Exit", Command.SCREEN, 0);  
    private static final Command actionBack_   = new Command("Back", Command.SCREEN, 0);
    private static final Command actionGo_     = new Command("Go", Command.SCREEN, 1);
    private static final Command actionClear_  = new Command("Clear", Command.SCREEN, 1);
    private static final Command actionRun_    = new Command("Run", Command.SCREEN, 1);
    private static final Command actionSignon_ = new Command(SIGN_ON, Command.SCREEN, 1);
    private static final Command actionSignoff_= new Command("SignOff", Command.SCREEN, 1);

    private Displayable   onErrorGoBackTo_;  // the form to return to when done displaying the error form

    // TextFields for the SignOn form.
    private TextField signonSystemText_ = new TextField("System", "rchasdm3", 20, TextField.ANY);
    private TextField signonUidText_ = new TextField("UserId", "JAVA", 10, TextField.ANY);
    private TextField signonPwdText_ = new TextField("Password", "JTEAM1", 10, TextField.PASSWORD);  // TBD temporary
    private TextField signonServerText_ = new TextField("MEServer", "localhost", 10, TextField.ANY);
    private StringItem signonStatusText_ = new StringItem("Status", NOT_SIGNED_ON);

    // TextFields for the CommandCall form.
    private TextField cmdText_ = new TextField("Command", "CRTLIB FRED", 256, TextField.ANY);  // TBD: max size; TBD: TextBox???
    private StringItem cmdMsgText_ = new StringItem("Messages", null);
    private StringItem cmdStatusText_ = new StringItem("Status", null);

    // TextFields for the ProgramCall form.
    private StringItem pgmMsgDescription_ = new StringItem("Messages", null);
    private StringItem pgmMsgText_ = new StringItem("Messages", null);

    // TextFields for the DataQueue form.
    private TextField dqInputText_ = new TextField("Data to write", "Hi there", 30, TextField.ANY);
    private StringItem dqOutputText_ = new StringItem("DQ contents", null);
    private ChoiceGroup dqReadOrWrite_ = new ChoiceGroup("Action", Choice.EXCLUSIVE, new String[] { DQ_WRITE, DQ_READ}, null);
    private StringItem dqStatusText_ = new StringItem("Status", null);


    /**
     * Creates a new ToolboxMidpDemo.
     **/
    public ToolboxMidpDemo()
    {
        display_ = Display.getDisplay(this);
        // Note: The KVM-based demo used TabbedPane for the main panel.  MIDP has no similar class, so we use a List instead.
    }

    /**
     * Show the main screen.
     * Implements abstract method of class Midlet.
     **/
    protected void startApp()
    {
        main_.append(SIGN_ON, null);
        main_.append(COMMAND_CALL, null);
        main_.append(PROGRAM_CALL, null);
        main_.append(DATA_QUEUE, null);
        main_.append(ABOUT, null);

        main_.addCommand(actionExit_);
        main_.setCommandListener(this);

        display_.setCurrent(main_);
    }

    // Implements method of interface CommandListener.
    public void commandAction(Command action, Displayable dsp)
    {
        // All 'exit' and 'back' processing is the same.
        if (action == actionExit_)
        {
            destroyApp(false);

            notifyDestroyed();
        }
        else if (action == actionBack_)
        {
            // Return to main menu.
            display_.setCurrent(main_); 
        }
        else if (dsp instanceof List)
        {
            List current = (List)dsp;

            // An action occurred on the main page
            if (current == main_)
            {
                int   idx = current.getSelectedIndex();

                switch (idx)
                {
                case 0:     // SignOn
                    showSignonForm();
                    break;
                case 1:     // CommandCall
                    showCmdForm();
                    break;
                case 2:     // ProgramCall
                    showPgmForm();
                    break;
                case 3:     // DataQueue
                    showDqForm();
                    break;
                case 4:     // About
                    showAboutForm();
                    break;
                default:    // None of the above
                    feedback("Internal error: Unhandled selected index in main: " + idx, AlertType.ERROR);
                    break;
                }         
            } // current == main
            else
                feedback("Internal error: The Displayable object is a List but is not main_.", AlertType.ERROR);
        } // instanceof List
        else if (dsp instanceof Form)
        {
            Form current = (Form)dsp;

            if (current == signonForm_)
            {
                if (action == actionSignon_)
                {
                    // Create a ToolboxME system object.
                    system_ = new AS400(signonSystemText_.getString(), signonUidText_.getString(), signonPwdText_.getString(), signonServerText_.getString());

                    try
                    {
                        // Connect to the iSeries.
                        system_.connect();

                        // Set the signon status text.
                        signonStatusText_.setText("Signed on.");

                        // Display a confirmation dialog that the user is signed on.
                        feedback("Successfully signed on.", AlertType.INFO, main_);

                        // Replace the SignOn button with SignOff.
                        signonForm_.removeCommand(actionSignon_);
                        signonForm_.addCommand(actionSignoff_);

                        // Update the ticker.
                        ticker_.setString("... Signed on to '" + signonSystemText_.getString() + "' as '" + signonUidText_.getString() + "' via '" + signonServerText_.getString() + "' ... ");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();

                        // Set the signon status text.
                        signonStatusText_.setText(NOT_SIGNED_ON);

                        feedback("Signon failed.  " + e.getMessage(), AlertType.ERROR);
                    }
                }
                else if (action == actionSignoff_)
                {
                    if (system_ == null)
                        feedback("Internal error: System is null.", AlertType.ERROR);
                    else
                    {
                        try
                        {
                            // Disconnect from the iSeries.
                            system_.disconnect();
                            system_ = null;

                            // Set the signon status text.
                            signonStatusText_.setText(NOT_SIGNED_ON);

                            // Display a confirmation dialog that the user is no longer signed on.
                            feedback("Successfully signed off.", AlertType.INFO, main_);

                            // Replace the SignOff button with SignOn.
                            signonForm_.removeCommand(actionSignoff_);
                            signonForm_.addCommand(actionSignon_);

                            // Update the ticker.
                            ticker_.setString(NOT_SIGNED_ON);
                        }
                        catch (Exception e)
                        {
                            feedback(e.toString(), AlertType.ERROR);

                            e.printStackTrace();

                            signonStatusText_.setText("Error.");

                            feedback("Error during signoff.", AlertType.ERROR);
                        }
                    }
                }
                else  // None of the above.
                {
                    feedback("Internal error: Action is not recognized.", AlertType.INFO);
                }
            } // signonForm_
            else if (current == cmdcallForm_)
            {
                if (action == actionRun_)
                {
                    // If the user has not signed on, display an alert.
                    if (system_ == null)
                    {
                        feedback(NOT_SIGNED_ON, AlertType.ERROR);
                        return;
                    }

                    // Get the command the user entered in the wireless device.
                    String cmdString = cmdText_.getString();

                    // If the command was not specified, display an alert.
                    if (cmdString == null || cmdString.length() == 0)
                        feedback("Specify command.", AlertType.ERROR);
                    else
                    {
                        try
                        {
                            // Run the command.
                            String[] messages = CommandCall.run(system_, cmdString);

                            StringBuffer status = new StringBuffer("Command completed with ");

                            // Check to see if their are any messages.
                            if (messages.length == 0)
                            {
                                status.append("no returned messages.");

                                cmdMsgText_.setText(null);

                                cmdStatusText_.setText("Command completed successfully.");
                            }
                            else
                            {
                                if (messages.length == 1)
                                    status.append("1 returned message.");
                                else
                                    status.append(messages.length + " returned messages.");

                                // If there are messages, display only the first message.
                                cmdMsgText_.setText(messages[0]);

                                cmdStatusText_.setText(status.toString());
                            }

                            repaint();
                        }
                        catch (Exception e)
                        {
                            feedback(e.toString(), AlertType.ERROR);

                            e.printStackTrace();

                            feedback("Error when running command.", AlertType.ERROR);
                        }
                    }
                }
                else if (action == actionClear_)
                {
                    // Clear the command text and messages.
                    cmdText_.setString("");

                    cmdMsgText_.setText(null);

                    cmdStatusText_.setText(null);

                    repaint();
                }
                else  // None of the above.
                {
                    feedback("Internal error: Action is not recognized.", AlertType.INFO);
                }
            } // cmdcallForm_
            else if (current == pgmcallForm_)
            {
                if (action == actionRun_)
                {
                    // If the user is not signed on before doing a program call, display an alert.
                    if (system_ == null)
                    {
                        feedback(NOT_SIGNED_ON, AlertType.ERROR);
                        return;
                    }

                    pgmMsgText_.setText(null);

                    // See the PCML example in the Toolbox programmer's guide.
                    String pcmlName = "qsyrusri.pcml"; // The PCML file we want to use.
                    String apiName = "qsyrusri";

                    // Create a hashtable that contains the input parameters for the program call.
                    Hashtable parmsToSet = new Hashtable(2);
                    parmsToSet.put("qsyrusri.receiverLength", "2048"); 
                    parmsToSet.put("qsyrusri.profileName", signonUidText_.getString().toUpperCase());

                    // Create a string array that contains the output parameters to retrieve.
                    String[] parmsToGet = { "qsyrusri.receiver.userProfile",  
                        "qsyrusri.receiver.previousSignonDate", 
                        "qsyrusri.receiver.previousSignonTime", 
                        "qsyrusri.receiver.daysUntilPasswordExpires"};

                    // A string array containing the descriptions of the parameters to display.
                    String[] displayParm = { "Profile", "Last signon Date", "Last signon Time", "Password Expired (days)"};

                    try
                    {
                        // Run the program.
                        String[] valuesToGet = ProgramCall.run(system_, pcmlName, apiName, parmsToSet, parmsToGet);

                        // Create a StringBuffer and add each of the parameters we retreived.
                        StringBuffer txt = new StringBuffer();
                        txt.append(displayParm[0] + ": " + valuesToGet[0] + "\n");

                        char[] c = valuesToGet[1].toCharArray();
                        txt.append(displayParm[1] + ": " + c[3]+c[4]+"/"+c[5]+c[6]+"/"+c[1]+c[2]  + "\n");

                        char[] d = valuesToGet[2].toCharArray();
                        txt.append(displayParm[2] + ": " + d[0]+d[1]+":"+d[2]+d[3] + "\n");
                        txt.append(displayParm[3] + ": " + valuesToGet[3] + "\n");

                        // Set the displayable text of the program call results.
                        pgmMsgText_.setText(txt.toString());

                        StringBuffer status = new StringBuffer("Program completed with ");

                        if (valuesToGet.length == 0)
                        {
                            status.append("no returned values.");

                            feedback(status.toString(), AlertType.INFO);
                        }
                        else
                        {
                            if (valuesToGet.length == 1)
                                status.append("1 returned value.");
                            else
                                status.append(valuesToGet.length + " returned values.");

                            feedback(status.toString(), AlertType.INFO);
                        }
                    }
                    catch (Exception e)
                    {
                        feedback(e.toString(), AlertType.ERROR);

                        e.printStackTrace();

                        feedback("Error when running program.", AlertType.ERROR);
                    }
                }
                else if (action == actionClear_)
                {
                    // Clear the program call results.
                    pgmMsgText_.setText(null);

                    repaint();
                }
            }  // pgmcallForm_
            else if (current == dataqueueForm_)  // DataQueue
            {
                if (action == actionGo_)
                {
                    // If the user has not signed on before performing Data Queue actions, display an alert.
                    if (system_ == null)
                    {
                        feedback(NOT_SIGNED_ON, AlertType.ERROR);

                        return;
                    }

                    // Create a library to create the data queue in.
                    try
                    {
                        CommandCall.run(system_, "CRTLIB FRED");
                    }
                    catch (Exception e)
                    {
                    }

                    // Run a command to create a data queue.
                    try
                    {
                        CommandCall.run(system_, "CRTDTAQ FRED/MYDTAQ MAXLEN(2000)");
                    }
                    catch (Exception e)
                    {
                        feedback("Error when creating data queue.  " + e.getMessage(), AlertType.WARNING);
                    }

                    try
                    {
                        // See which action was selected (Read or Write).
                        if (dqReadOrWrite_.getString(dqReadOrWrite_.getSelectedIndex()).equals(DQ_WRITE))
                        {
                            // Write
                            dqOutputText_.setText(null);

                            // Get the text from the wireless device input to be written to the data queue.
                            if (dqInputText_.getString().length() == 0)
                                dqStatusText_.setText("No data specified.");
                            else
                            {
                                // Write to the data queue.
                                DataQueue.write(system_, "/QSYS.LIB/FRED.LIB/MYDTAQ.DTAQ", dqInputText_.getString().getBytes() );

                                dqInputText_.setString(null);

                                // Display the status.
                                dqStatusText_.setText("The 'write' operation completed.");
                            }
                        }
                        else  // Read
                        {
                            // Read from the data queue.
                            byte[] b = DataQueue.readBytes(system_, "/QSYS.LIB/FRED.LIB/MYDTAQ.DTAQ");

                            // Determine if the data queue contained entries or not and display the appropriate message.
                            if (b == null)
                            {
                                dqStatusText_.setText("No dataqueue entries are available.");

                                dqOutputText_.setText(null);
                            }
                            else if (b.length == 0)
                            {
                                dqStatusText_.setText("Dataqueue entry has no data.");

                                dqOutputText_.setText(null);
                            }
                            else
                            {
                                dqStatusText_.setText("The 'read' operation completed.");

                                dqOutputText_.setText(new String(b));
                            }
                        }

                        repaint();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();

                        feedback(e.toString(), AlertType.ERROR);

                        feedback("Error when running command.  " + e.getMessage(), AlertType.ERROR);
                    }
                }  // actionGo_
                else if (action == actionClear_)
                {
                    // Clear the data queue form.
                    dqInputText_.setString("");

                    dqOutputText_.setText(null);

                    dqReadOrWrite_.setSelectedFlags(new boolean[] { true, false});

                    dqStatusText_.setText(null);

                    repaint();
                }
                else  // None of the above.
                {
                    feedback("Internal error: Action is not recognized.", AlertType.INFO);
                }
            } // dataqueueForm_
            else if (current == aboutForm_)  // "About".
            {
                // Should never reach here, since the only button is "Back".
            }  // None of the above.
            else
                feedback("Internal error: Form is not recognized.", AlertType.ERROR);
        } // instanceof Form
        else
            feedback("Internal error: Displayable object not recognized.", AlertType.ERROR);
    }



    /**
     *  Displays the "About" form.
     **/
    private void showAboutForm()
    {
        // If the about form is null, create and append it.
        if (aboutForm_ == null)
        {
            aboutForm_ = new Form(ABOUT);
            aboutForm_.append(new StringItem(null, "This is a MIDP example application that uses the Toolbox Micro Edition (ToolboxME)."));

            aboutForm_.addCommand(actionBack_);
            aboutForm_.setCommandListener(this);
        }

        display_.setCurrent(aboutForm_);
    }



    /**
     *  Displays the "SignOn" form.
     **/
    private void showSignonForm()
    {
        // Create the signon form.
        if (signonForm_ == null)
        {
            signonForm_ = new Form(SIGN_ON);
            signonForm_.append(signonSystemText_);
            signonForm_.append(signonUidText_);
            signonForm_.append(signonPwdText_);
            signonForm_.append(signonServerText_);
            signonForm_.append(signonStatusText_);
            signonForm_.addCommand(actionBack_);
            signonForm_.addCommand(actionSignon_);
            signonForm_.setCommandListener(this);
            signonForm_.setTicker(ticker_);
        }

        display_.setCurrent(signonForm_);
    }



    /**
     *  Displays the "CommandCall" form.
     **/
    private void showCmdForm()
    {
        // Create the command call form.
        if (cmdcallForm_ == null)
        {
            cmdcallForm_ = new Form(COMMAND_CALL);
            cmdcallForm_.append(cmdText_);
            cmdcallForm_.append(cmdMsgText_);
            cmdcallForm_.append(cmdStatusText_);
            cmdcallForm_.addCommand(actionBack_);
            cmdcallForm_.addCommand(actionClear_);
            cmdcallForm_.addCommand(actionRun_);
            cmdcallForm_.setCommandListener(this);
            cmdcallForm_.setTicker(ticker_);
        }

        display_.setCurrent(cmdcallForm_);
    }



    /**
     *  Displays the "ProgramCall" form.
     **/
    private void showPgmForm()
    {
        // Create the program call form.
        if (pgmcallForm_ == null)
        {
            pgmcallForm_ = new Form(PROGRAM_CALL);
            pgmcallForm_.append(new StringItem(null, "This calls the Retrieve User Information (QSYRUSRI) API, and returns information about the current user profile."));
            pgmcallForm_.append(pgmMsgText_);
            pgmcallForm_.addCommand(actionBack_);
            pgmcallForm_.addCommand(actionClear_);
            pgmcallForm_.addCommand(actionRun_);
            pgmcallForm_.setCommandListener(this);
            pgmcallForm_.setTicker(ticker_);
        }

        display_.setCurrent(pgmcallForm_);
    }


    /**
     *  Displays the "DataQueue" form.
     **/
    private void showDqForm()
    {
        // Create the data queue form.
        if (dataqueueForm_ == null)
        {
            dataqueueForm_ = new Form(DATA_QUEUE);
            dataqueueForm_.append(dqInputText_);
            dataqueueForm_.append(dqOutputText_);
            dataqueueForm_.append(dqReadOrWrite_);
            dataqueueForm_.append(dqStatusText_);
            dataqueueForm_.addCommand(actionBack_);
            dataqueueForm_.addCommand(actionClear_);
            dataqueueForm_.addCommand(actionGo_);
            dataqueueForm_.setCommandListener(this);
            dataqueueForm_.setTicker(ticker_);
        }

        display_.setCurrent(dataqueueForm_);
    }


    private void feedback(String text, AlertType type)
    {
        feedback(text, type, display_.getCurrent());
    }

    /**
     *  This method is used to create a dialog and display feedback information using an Alert to the user.
     **/
    private void feedback(String text, AlertType type, Displayable returnToForm)
    {
        System.err.flush();
        System.out.flush();

        Alert alert = new Alert("Alert", text, null, type);

        if (type == AlertType.INFO)
            alert.setTimeout(3000);  // milliseconds
        else
            alert.setTimeout(Alert.FOREVER);  // Require user to dismiss the alert.

        display_.setCurrent(alert, returnToForm);
    }


    // Force a repaint of the current form.  
    private void repaint()
    {
        Alert alert = new Alert("Updating display ...", null, null, AlertType.INFO);
        alert.setTimeout(1000);  // milliseconds

        display_.setCurrent(alert, display_.getCurrent());
    }



    /**
     * Time to pause, free any space we don't need right now.
     * Implements abstract method of class Midlet.
     **/
    protected void pauseApp()
    {
        display_.setCurrent(null);
    }



    /**
     * Destroy must cleanup everything.
     * Implements abstract method of class Midlet.
     **/
    protected void destroyApp(boolean unconditional)
    {
        // Disconnect from the iSeries if the Midlet is being destroyed or exited.
        if (system_ != null)
        {
            try
            {
                system_.disconnect();
            }
            catch (Exception e)
            {
            }
        }
    }
}

