//////////////////////////////////////////////////////////////////////////////////
//
// ToolboxME for iSeries example. This program demonstrates how your wireless
// device can connect to an iSeries server and use JDBC to perform work on a
// remote database.
//
//////////////////////////////////////////////////////////////////////////////////

import java.sql.*;               // SQL Interfaces provided by JdbcMe
import com.ibm.as400.micro.*;    // JdbcMe implementation
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.microedition.io.*;  // Part of the CLDC specification
import de.kawt.*;                // Part of the CLDC specification

class DemoConstants
{
    // These constants are actually used mainly by the demo
    // for the JDBC driver. The Jdbc and JDBC  application
    // creator IDs ( http://www.palmos.com/dev )
    // are reserved at palm computing.
    public static final int demoAppID    = 0x4a444243; // JDBC
    // Make the dbCreator something else so that the
    // user can actually see the Palm DB seperately from
    // the JdbcDemo application.
    public static final int dbCreator    = 0x4a444231; // JDB1
    public static final int dbType       = 0x4a444231; // JDB1
}

/**
 * Little configuration dialog box to display the
 * current connections/statements, the
 * URL being used, user id and password
 */
class ConfigurationDialog extends Dialog implements ActionListener
{
    TextField         data;
    ConfigurationDialog(Frame w)
    {
        super(w, "Configuration");

        // Show/Modify current URL connection
        data = new TextField(JdbcDemo.mainFrame.jdbcPanel.url);
        add("Center", data);

        // Ok button.
        Panel panel = new Panel();
        Button button = new Button("Ok");
        button.addActionListener(this);
        panel.add(button);
        add("South", panel);
        pack();
    }

    public void actionPerformed(ActionEvent e)
    {
        JdbcDemo.mainFrame.jdbcPanel.url = data.getText();
        data = null;
        setVisible(false);
    }
}

/**
 * Little configuration dialog box to display the
 * current connections/statements, the
 * URL being used, user id and password
 */
class MultiChoiceDialog extends Dialog implements ActionListener
{
    Choice            task;
    ActionListener    theListener;
    MultiChoiceDialog(Frame w, String title, String prompt, String choices[], ActionListener it)
    {
        super(w, title);
        theListener = it;

        // Show/Modify current URL connection
        Label txt = new Label(prompt);
        add("West", txt);
        task = new Choice();
        for (int i=0; i<choices.length; ++i)
        {
            task.add(choices[i]);
        }
        task.select(0);
        add("Center", task);

        // Ok button.
        Panel panel = new Panel();
        Button button = new Button("Ok");
        button.addActionListener(this);
        panel.add(button);
        button = new Button("Cancel");
        button.addActionListener(this);
        panel.add(button);
        add("South", panel);
        pack();
    }

    /**
     *  Determine the action performed.
     **/
    public void actionPerformed(ActionEvent e)
    {
        int choice = task.getSelectedIndex();
        setVisible(false);
        if (e.getActionCommand().equals("Ok"))
        {
            if (theListener != null)
            {
                ActionEvent ev = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, task.getItem(choice));
                theListener.actionPerformed(ev);
            }
            task = null;
        }
        else
        {
            // No-op
        }
    }
}

/**
 * The JdbcPanel is the main panel of the application.
 * It displays the current connection and statement
 * at the top.
 * A text field for entering SQL statements next.
 * A Results field for displaying each column of data
 * or results.
 * An task list and a 'go' button so that different
 * tasks can be tried.
 */
class JdbcPanel extends Panel implements ActionListener
{
    public final static int TASK_EXIT         = 0;
    public final static int TASK_NEW          = 1;
    public final static int TASK_CLOSE        = 2;
    public final static int TASK_EXECUTE      = 3;
    public final static int TASK_PREV         = 4;
    public final static int TASK_NEXT         = 5;
    public final static int TASK_CONFIG       = 6;
    public final static int TASK_TOPALMDB     = 7;
    public final static int TASK_FROMPALMDB   = 8;
    public final static int TASK_SETAUTOCOMMIT= 9;
    public final static int TASK_SETISOLATION = 10;
    public final static int TASK_COMMIT       = 11;
    public final static int TASK_ROLLBACK     = 12;


    // JDBC objects.
    java.sql.Connection  connObject  = null;
    Statement            stmtObject  = null;
    ResultSet            rs          = null;
    ResultSetMetaData    rsmd        = null;

    String        lastErr     = null;
    String        url         = null;
    Label         connection  = null;
    Label         statement   = null;
    TextField     sql  = null;
    List          data = null;
    final Choice  task;

    /**
     * Build the GUI.
     */
    public JdbcPanel()
    {
        // The JDBC URL
        // Make sure to edit the following line so that it correctly specifies the 
        // the MEServer and the iSeries server to which you want to connect.
        url = "jdbc:as400://mySystem;user=myUidl;password=myPwd;meserver=myMEServer;";

        Panel    p1left = new Panel();
        p1left.setLayout(new BorderLayout());
        connection = new Label("None");
        p1left.add("West", new Label("Conn:"));
        p1left.add("Center", connection);

        Panel    p1right = new Panel();
        p1right.setLayout(new BorderLayout());
        statement = new Label("None");
        p1right.add("West", new Label("Stmt:"));
        p1right.add("Center", statement);

        Panel    p1 = new Panel();
        p1.setLayout(new GridLayout(1,2));
        p1.add(p1left);
        p1.add(p1right);

        Panel    p2 = new Panel();
        p2.setLayout(new BorderLayout());
        p2.add("North", new Label("Sql:"));
        sql = new TextField(25);
        sql.setText("select * from QIWS.QCUSTCDT"); // Default query
        p2.add("Center", sql);

        Panel    p3 = new Panel();
        p3.setLayout(new BorderLayout());
        data = new List();
        data.add("No Results");
        p3.add("North", new Label("Results:"));
        p3.add("Center", data);

        Panel    p4 = new Panel();

        task = new Choice();
        task.add("Exit");             // TASK_EXIT
        task.add("New");              // TASK_NEW
        task.add("Close");            // TASK_CLOSE
        task.add("Execute");          // TASK_EXECUTE
        task.add("Prev");             // TASK_PREV
        task.add("Next");             // TASK_NEXT
        task.add("Config");           // TASK_CONFIGURE
        task.add("RS to PalmDB");     // TASK_TOPALMDB
        task.add("Query PalmDB");     // TASK_FROMPALMDB
        task.add("Set AutoCommit");   // TASK_SETAUTOCOMMIT
        task.add("Set Isolation");    // TASK_SETISOLATION
        task.add("Commit");           // TASK_COMMIT
        task.add("Rollback");         // TASK_ROLLBACK
        task.select(TASK_EXECUTE);  // Start off here.
        p4.add("West", task);

        Button b = new Button("Go");
        b.addActionListener(this);
        p4.add("East", b);

        Panel prest = new Panel();
        prest.setLayout(new BorderLayout());
        prest.add("North", p2);
        prest.add("Center", p3);
        Panel pall = new Panel();
        pall.setLayout(new BorderLayout());
        pall.add("North", p1);
        pall.add("Center", prest);

        setLayout(new BorderLayout());
        add("Center", pall);
        add("South", p4);
    }

    /**
     * Do a task based on whichever task is
     * currently selected in the task list.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() instanceof MultiChoiceDialog)
        {
            String   cmd = e.getActionCommand();
            processExtendedCommand(cmd);
            return;
        }

        switch (task.getSelectedIndex())
        {
        case TASK_EXIT:
            System.exit(0);
            break;
        case TASK_NEW:
            JdbcPanel.this.goNewItems();
            break;
        case TASK_PREV:
            JdbcPanel.this.goPrevRow();
            break;
        case TASK_NEXT:
            JdbcPanel.this.goNextRow();
            break;
        case TASK_EXECUTE:
            if (connObject == null || stmtObject == null)
                JdbcPanel.this.goNewItems();

            JdbcPanel.this.goExecute();
            break;
        case TASK_CONFIG:
            JdbcPanel.this.goConfigure();
            break;
        case TASK_CLOSE:
            JdbcPanel.this.goClose();
            break;
        case TASK_TOPALMDB:
            if (connObject == null || stmtObject == null)
                JdbcPanel.this.goNewItems();

            JdbcPanel.this.goResultsToPalmDB();
            break;
        case TASK_FROMPALMDB:
            JdbcPanel.this.goQueryFromPalmDB();
            break;
        case TASK_SETAUTOCOMMIT:
            JdbcPanel.this.goSetAutocommit();
            break;
        case TASK_SETISOLATION:
            JdbcPanel.this.goSetIsolation();
            break;
        case TASK_COMMIT:
            JdbcPanel.this.goTransact(true);
            break;
        case TASK_ROLLBACK:
            JdbcPanel.this.goTransact(false);
            break;

        default : 
        {
                Dialog dialog = new FeedbackDialog(JdbcDemo.mainFrame, "Error", "Task not implemented");
                dialog.show();
                dialog = null;
            }
        }
    }

    public void processExtendedCommand(String cmd)
    {
        try
        {
            if (cmd.equals("true"))
            {
                connObject.setAutoCommit(true);
                return;
            }
            if (cmd.equals("false"))
            {
                connObject.setAutoCommit(false);
                return;
            }
            if (cmd.equals("read uncommitted"))
            {
                connObject.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
                return;
            }
            if (cmd.equals("read committed"))
            {
                connObject.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
                return;
            }
            if (cmd.equals("repeatable read"))
            {
                connObject.setTransactionIsolation(java.sql.Connection.TRANSACTION_REPEATABLE_READ);
                return;
            }
            if (cmd.equals("serializable"))
            {
                connObject.setTransactionIsolation(java.sql.Connection.TRANSACTION_SERIALIZABLE);
                return;
            }
            throw new IllegalArgumentException("Invalid command: " + cmd);
        }
        catch (Exception e)
        {
            JdbcDemo.mainFrame.exceptionFeedback(e);
            return;
        }
    }

    /**
     * Perform commit or rollback processing.
     */
    public void goTransact(boolean commit)
    {
        if (connObject == null)
        {
            FeedbackDialog dialog = new FeedbackDialog(JdbcDemo.mainFrame, "Skip", "Connection not allocated");
            dialog.show();
            dialog = null;
            return;
        }
        try
        {
            if (commit)
                connObject.commit();
            else
                connObject.rollback();
        }
        catch (Exception e)
        {
            JdbcDemo.mainFrame.exceptionFeedback(e);
        }
    }

    /**
     * Prompt the user for setting the autocommit value
     * Real work handled by the actionPerformed method
     * calling processExtendedCommand().
     */
    public void goSetAutocommit()
    {
        if (connObject == null)
        {
            FeedbackDialog dialog = new FeedbackDialog(JdbcDemo.mainFrame, "Skip", "Connection not allocated");
            dialog.show();
            dialog = null;
            return;
        }
        try
        {
            String currentValue;
            if (connObject.getAutoCommit())
                currentValue = "Now: true";
            else
                currentValue = "Now: false";

            Dialog dialog = new MultiChoiceDialog(JdbcDemo.mainFrame, "Set Autocommit", currentValue, new String[]{ "true", "false"}, this);
            dialog.show();
            dialog = null;
        }
        catch (Exception e)
        {
            JdbcDemo.mainFrame.exceptionFeedback(e);
        }
    }

    /**
     * Prompt the user for setting the isolation level,
     * real work handled by the actionPerformed() method
     * calling processExtendedCommand().
     */
    public void goSetIsolation()
    {
        if (connObject == null)
        {
            FeedbackDialog dialog = new FeedbackDialog(JdbcDemo.mainFrame, "Skip", "Connection not allocated");
            dialog.show();
            dialog = null;
            return;
        }
        try
        {
            int   level = connObject.getTransactionIsolation();
            String currentLevel;
            switch (level)
            {
            case java.sql.Connection.TRANSACTION_READ_UNCOMMITTED:
                currentLevel = "Now: read uncommitted";
                break;
            case java.sql.Connection.TRANSACTION_READ_COMMITTED:
                currentLevel = "Now: read committed";
                break;
            case java.sql.Connection.TRANSACTION_REPEATABLE_READ:
                currentLevel = "Now: repeatable read";
                break;
            case java.sql.Connection.TRANSACTION_SERIALIZABLE:
                currentLevel = "Now: serializable";
                break;
            default : {
                    currentLevel = "error";
                }
            }
            Dialog dialog = new MultiChoiceDialog(JdbcDemo.mainFrame, 
                                                  "Set Isolation Level", 
                                                  currentLevel, 
                                                  new String[]{ "read uncommitted", "read committed", "repeatable read","serializable"}, 
                                                  this);
            dialog.show();
            dialog = null;
        }
        catch (Exception e)
        {
            JdbcDemo.mainFrame.exceptionFeedback(e);
        }
    }

    /**
     * Create a new connection or statement.
     * Only one connection and statement is currently
     * supported.
     */
    public void goNewItems()
    {
        if (connObject != null || stmtObject != null)
        {
            FeedbackDialog dialog = new FeedbackDialog(JdbcDemo.mainFrame, "Skip", "Conn/Stmt already allocated");
            dialog.show();
            dialog = null;
        }
        if (connObject == null)
        {
            try
            {
                connObject = DriverManager.getConnection(url);
                //connection.setText(Integer.toString(((JdbcMeConnection)connObject).getId()));
                connection.repaint();
            }
            catch (Exception e)
            {
                JdbcDemo.mainFrame.exceptionFeedback(e);
                return;
            }
        }
        if (stmtObject == null)
        {
            try
            {
                try
                {
                    stmtObject = connObject.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                }
                catch (Exception e)
                {
                    // Try again...  DB2 NT version 6.1 doesn't support
                    // Scollable result sets, so we'll assume other
                    // JDBC 2.0 databases don't either. We'll attempt
                    // to create another.
                    try
                    {
                        stmtObject = connObject.createStatement();
                    }
                    catch (Exception ex)
                    {
                        // If the second try failed, rethrow the
                        // first exception. Its probably
                        // a more meaninful error.
                        throw e;
                    }
                    FeedbackDialog dialog = new FeedbackDialog(JdbcDemo.mainFrame, "2nd try worked", "Non-scrollable result set");
                    dialog.show();
                    dialog = null;
                }
                
                statement.repaint();
            }
            catch (Exception e)
            {
                JdbcDemo.mainFrame.exceptionFeedback(e);
                return;
            }
        }
    }


    /**
     *  Close the statement and connection.
     **/
    public void goClose()
    {
        // Close the statement.
        if (stmtObject != null)
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (Exception e)
                {
                }
                rs = null;
                rsmd = null;
            }
            try
            {
                stmtObject.close();
            }
            catch (Exception e)
            {
            }
            stmtObject = null;
            statement.setText("None");
            statement.repaint();
        }

        // Clost the connection.
        if (connObject != null)
        {
            try
            {
                connObject.close();
            }
            catch (Exception e)
            {
            }
            connObject = null;
            connection.setText("None");
            connection.repaint();
        }
        data.removeAll();
        data.add("No Results");
        data.repaint();
        sql.repaint();
        return;
    }

    /**
     * display the configuration dialog.
     **/
    public void goConfigure()
    {
        // Note there is no model dialog support in KAWT, this only
        // works because the data to be changed (url) is set before
        // this dialog is used, and the user cannot access the
        // main frame while this is up on the palm (i.e. all dialogs
        // in Kawt are modal).
        ConfigurationDialog dialog = new ConfigurationDialog(JdbcDemo.mainFrame); 
        dialog.show();
        dialog = null;
    }

    /**
     *  Execute the specified query.
     **/
    public void goExecute()
    {
        // Get the currently selected statement.
        try
        {
            if (rs != null)
                rs.close();

            rs = null;
            rsmd = null;
            boolean results = stmtObject.execute(sql.getText());
            if (results)
            {
                rs = stmtObject.getResultSet();
                rsmd = rs.getMetaData();
                // Show the first row
                goNextRow();
            }
            else
            {
                data.removeAll();
                data.add(stmtObject.getUpdateCount() + " rows updated");
                data.repaint();
            }
        }
        catch (Exception e)
        {
            JdbcDemo.mainFrame.exceptionFeedback(e);
        }
    }


    /**
     *  Move to the next row in the result set.
     **/
    public void goNextRow()
    {
        try
        {
            if (rs == null || rsmd == null)
                return;

            int   count = rsmd.getColumnCount();
            int   i;
            data.removeAll();
            if (!rs.next())
                data.add("End of data");
            else
            {
                for (i=1; i>=count; ++i)
                {
                    data.add(rs.getString(i));
                }
            }
            data.repaint();
        }
        catch (Exception e)
        {
            JdbcDemo.mainFrame.exceptionFeedback(e);
        }
    }


    /** 
     *  Move to the previous row in the result set.
     **/
    public void goPrevRow()
    {
        try
        {
            if (rs == null || rsmd == null)
                return;

            int   count = rsmd.getColumnCount();
            int   i;
            data.removeAll();
            if (!rs.previous())
                data.add("Start of data");
            else
            {
                for (i=1; i<=count; ++i)
                {
                    data.add(rs.getString(i));
                }
            }
            data.repaint();
        }
        catch (Exception e)
        {
            JdbcDemo.mainFrame.exceptionFeedback(e);
        }
    }

    /**
     *  Perform a query and store the results in the local devices database
     **/
    public void goResultsToPalmDB()
    {
        try
        {
            if (stmtObject == null)
            {
                FeedbackDialog dialog = new FeedbackDialog(JdbcDemo.mainFrame, "Skip", "No Statement");
                dialog.show();
                dialog = null;
                return;
            }

            boolean results = ((JdbcMeStatement)stmtObject).executeToOfflineData(sql.getText(), "JdbcResultSet", DemoConstants.dbCreator, DemoConstants.dbType);
            if (!results)
            {
                FeedbackDialog dialog = new FeedbackDialog(JdbcDemo.mainFrame, "No Data", "Not a query");
                dialog.show();
                dialog = null;
                return;
            }
            data.removeAll();
            data.add("Updated Palm DB 'JdbcResultSet'");
            data.repaint();
        }
        catch (Exception e)
        {
            JdbcDemo.mainFrame.exceptionFeedback(e);
        }
    }


    /**
     *  Perform a query from the database that resides on the palm device.
     **/
    public void goQueryFromPalmDB()
    {
        try
        {
            if (rs != null)
            {
                rs.close();
                rs = null;
            }
            rs = new JdbcMeOfflineResultSet ("JdbcResultSet", DemoConstants.dbCreator, DemoConstants.dbType);
            rsmd = rs.getMetaData();
            // If we want to debug some output, this
            // method can be used to dump the contents
            // of the PalmDB represented by the result set
            // (Uses System.out so its mostly useful in
            // the Palm emulator when debugging your
            // applications.
            // ((JdbcMeOfflineResultSet)rs).dumpDB(true);

            // show the first row.
            goNextRow();
        }
        catch (SQLException e)
        {
            JdbcDemo.mainFrame.exceptionFeedback(e);
        }
    }
}

public class JdbcDemo extends Frame
{
    /** An ActionListener that ends the application. Only
     * one is required, and can be reused
     */
    private static ActionListener    exitActionListener = null;
    /**
     * The main application in this process.
     */
    static         JdbcDemo mainFrame = null;

    JdbcPanel      jdbcPanel = null;

    public static ActionListener getExitActionListener()
    {
        if (exitActionListener == null)
        {
            exitActionListener = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            };
        }
        return exitActionListener;
    }

    /**
     *  Demo Constructor
     **/
    public JdbcDemo()
    {
        super("Jdbc Demo");
        setLayout(new BorderLayout());

        jdbcPanel = new JdbcPanel();
        add("Center", jdbcPanel);

        addWindowListener(new WindowAdapter()
         {
             public void windowClosing(WindowEvent e)
             {
                 System.exit(0);
             }
         });
        setSize(200,300);
        pack();
    }

    public void exceptionFeedback(Exception e)
    {
        Dialog dialog = new FeedbackDialog(JdbcDemo.mainFrame, e);
        dialog.show();
        dialog = null;
    }

    /**
     * Main method.
     **/
    public static void main(String args[])
    {
        try
        {
            mainFrame = new JdbcDemo();
            mainFrame.show();
            mainFrame.jdbcPanel.goConfigure();
        }
        catch (Exception e)
        {
            System.exit(1);
        }
    }
}
