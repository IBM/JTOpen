//////////////////////////////////////////////////////////////////////////////////
//
// ToolboxME for iSeries example. This program is an example MIDlet that shows how
// you might code a JdbcMe application for the MIDP profile. Refer to the 
// startApp, pauseApp, destroyApp and commandAction methods to see how it handles
// each requested transition.
//
//////////////////////////////////////////////////////////////////////////////////

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.sql.*;
import javax.microedition.rms.*;

import com.ibm.as400.micro.*;


public class JdbcMidpBid extends MIDlet implements CommandListener
{
    private static int BID_PROPERTY = 0;
    private Display   display;

    private TextField urlText = new TextField("urltext", "jdbc:as400://mySystem;user=myUid;password=myPwd;", 65, TextField.ANY);
    private TextField jdbcmeText = new TextField("jdbcmetext", "meserver=myMEServer", 40, TextField.ANY);
    private TextField jdbcmeTraceText = new TextField("jdbcmetracetext", "0", 10, TextField.ANY);
    private final static String GETBIDS = "No bids are available, select here to download bids";
    private List      main = new List("JdbcMe Bid Demo", Choice.IMPLICIT);
    private List      listings = null;
    private Form      aboutBox;
    private Form      bidForm;
    private Form      settingsForm;
    private int       bidRow = 0;
    private String    bidTarget = null;
    private String    bidTargetKey = null;
    private TextField bidText = new TextField("bidtext", "", 10, TextField.NUMERIC);
    private Form      errorForm = null;

    private Command exitCommand   = new Command("Exit", Command.SCREEN, 0);
    private Command backCommand   = new Command("Back", Command.SCREEN, 0);
    private Command cancelCommand = new Command("Cancel", Command.SCREEN, 0);
    private Command goCommand     = new Command("Go", Command.SCREEN, 1);
    private Displayable   onErrorGoBackTo = null;

    /*
     * Construct a new JdbcMidpBid.
     */
    public JdbcMidpBid()
    {
        display = Display.getDisplay(this);
    }

    /**
     * Show the main screen
     */
    public void startApp()
    {
        main.append("Show Bids", null);
        main.append("Get New Bids", null);
        main.append("Settings", null);
        main.append("About", null);
        main.addCommand(exitCommand);
        main.setCommandListener(this);

        display.setCurrent(main);
    }

    public void commandAction(Command c, Displayable s)
    {
        // All exitCommand processing is the same.
        if (c == exitCommand)
        {
            destroyApp(false);
            notifyDestroyed();
            return;
        }
        if (s instanceof List)
        {
            List     current = (List)s;

            // An action occurred on the main page
            if (current == main)
            {
                int   idx = current.getSelectedIndex();
                switch (idx)
                {
                case 0:     // Show current bids
                    showBids();
                    break;
                case 1:     // Get New Bids
                    getNewBids();
                    break;
                case 2:     // Settings
                    doSettings();
                    break;
                case 3:     // About
                    aboutBox();
                    break;
                default : 
                    break;
                }
                return;
            } // current == main

            // An action occurred on the listings page
            if (current == listings)
            {
                if (c == backCommand)
                {
                    display.setCurrent(main);
                    return;
                }
                if (c == List.SELECT_COMMAND)
                {
                    int idx = listings.getSelectedIndex();
                    String stext = listings.getString(idx);
                    if (stext.equals(GETBIDS))
                    {
                        getNewBids();
                        return;
                    }
                    int   commaIdx = stext.indexOf(',');
                    bidTargetKey = stext.substring(0, commaIdx);
                    bidTarget = stext.substring(commaIdx+1) + "\n";
                    // Also keep track of which offline result set row
                    // This is. It happens to be the same as the index
                    // in the list.
                    bidRow = idx;

                    bidOnProperty();
                }
            } // current == listings
            return;
        } // instanceof List
        if (s instanceof Form)
        {
            Form     current = (Form)s;
            if (current == errorForm)
            {
                if (c == backCommand)
                    display.setCurrent(onErrorGoBackTo);

                return;
            } // errorForm
            if (current == settingsForm)
            {
                if (c == backCommand)
                {
                    // Done with settings.
                    display.setCurrent(main);
                    settingsForm = null;
                    return;
                }
            } // settingsForm
            if (current == aboutBox)
            {
                if (c == backCommand)
                {
                    // Done with about box.
                    display.setCurrent(main);
                    aboutBox = null;
                    return;
                }
            }
            if (current == bidForm)
            {
                if (c == cancelCommand)
                {
                    display.setCurrent(listings);
                    bidForm = null;
                    return;
                }
                if (c == goCommand)
                {
                    submitBid();
                    if (display.getCurrent() != bidForm)
                    {
                        // If we're no longer positioned at the
                        // bidForm, we will get rid of it.
                        bidForm = null;
                    }
                    return;
                }
                return;
            } // current == bidForm
        } // instanceof Form
    }

    public void aboutBox()
    {
        aboutBox = new Form("aboutbox");
        aboutBox.setTitle("About");
        aboutBox.append(new StringItem("", "Midp RealEstate example for JdbcMe "));
        aboutBox.addCommand(backCommand);
        aboutBox.setCommandListener(this);
        display.setCurrent(aboutBox);
    }

    /**
     *  The settings form.
     */
    public void doSettings()
    {
        settingsForm = new Form("settingsform");
        settingsForm.setTitle("Settings");
        settingsForm.append(new StringItem("", "DB URL"));
        settingsForm.append(urlText);
        settingsForm.append(new StringItem("", "JdbcMe server"));
        settingsForm.append(jdbcmeText);
        settingsForm.append(new StringItem("", "Trace"));

        settingsForm.addCommand(backCommand);
        settingsForm.setCommandListener(this);
        display.setCurrent(settingsForm);
    }


    /**
     * Show the bid screen for the bid target
     * that we selected.
     */
    public void bidOnProperty()
    {
        StringItem item = new StringItem("", bidTarget);

        bidText = new TextField("bidtext", "", 10, TextField.NUMERIC);
        bidText.setString("");

        bidForm = new Form("bidform");
        bidForm.setTitle("Submit a bid for:");
        BID_PROPERTY = 0;
        bidForm.append(item);
        bidForm.append(new StringItem("", "Your bid:"));
        bidForm.append(bidText);
        bidForm.addCommand(cancelCommand);
        bidForm.addCommand(goCommand);
        bidForm.setCommandListener(this);
        display.setCurrent(bidForm);
    }

    /**
     * Update the listings card with the
     * current list of bids that we're interested in.
     */
    public void getNewBids()
    {
        // Reset the old listing
        listings = null;
        listings = new List("JdbcMe Bids", Choice.IMPLICIT);
        java.sql.Connection     conn = null;
        Statement               stmt = null;
        try
        {
            conn = DriverManager.getConnection(urlText.getString() + ";" + jdbcmeText.getString());

            stmt = conn.createStatement();

            // Since we don't want the prepared statement to persist,
            // a normal statement is really better in this environemnt.
            String sql = "select mls, address, currentbid from qjdbcme.realestate where currentbid <> 0";

            boolean results =((JdbcMeStatement)stmt).executeToOfflineData(sql,"JdbcMidpBidListings",0, 0);
            if (results)
            {
                setupListingsFromOfflineData();
            }
            else
            {
                listings.append("No bids found", null);
                listings.addCommand(backCommand);
                listings.setCommandListener(this);
            }
        }
        catch (Exception e)
        {
            // Currently no valid listings retrieved, so lets
            // reset it to empty.
            listings = new List("JdbcMe Bids", Choice.IMPLICIT);
            listings.append(GETBIDS, null);
            listings.addCommand(backCommand);
            listings.setCommandListener(this);

            // Return to main after showing the error.
            showError(main, e);
            return;
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception e)
                {
                }
            }
            conn = null;
            stmt = null;
        }
        showBids();
    }

    public void setupListingsFromOfflineData()
    {
        // Skip the first four rows in the record store
        // (eyecatcher, version, num columns, sql column
        // types)
        // and each subsequent row in the record store is
        // a single column. Our query returns 3 columns which
        // we'll return concatenated as a single string.
        ResultSet         rs = null;
        listings.addCommand(backCommand);
        listings.setCommandListener(this);
        try
        {
            int                     i = 5;
            int                     max = 0;
            StringBuffer   buf  = new StringBuffer(20);

            // Creator and dbtype unused in MIDP
            rs = new JdbcMeOfflineResultSet("JdbcMidpBidListings", 0, 0);
            if (rs == null)
            {
                // New listings...
                listings = new List("JdbcMe Bids", Choice.IMPLICIT);
                listings.append(GETBIDS, null);
                listings.addCommand(backCommand);
                listings.setCommandListener(this);
                return;
            }

            i = 0;
            String      s = null;
            while (rs.next())
            {
                ++i;

                s = rs.getString(1);
                buf.append(s);

                buf.append(",");
                s = rs.getString(2);
                buf.append(s);

                buf.append(", $");
                s = rs.getString(3);
                buf.append(s);

                listings.append(buf.toString(), null);
                buf.setLength(0);
            } 

            if (i == 0)
            {
                listings.append("No bids found", null);
                return;
            }
        }
        catch (Exception e)
        {
            // Currently no valid listings retrieved, so lets
            // reset it to empty.
            listings = new List("JdbcMe Bids", Choice.IMPLICIT);
            listings.append(GETBIDS, null);
            listings.addCommand(backCommand);
            listings.setCommandListener(this);

            // Return to main after showing the error.
            showError(main, e);
            return;
        }
        finally
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
            }
            System.gc();
        }
    }

    /**
     * Update the listings card with the
     * current list of bids that we're interested in.
     */
    public void submitBid()
    {
        java.sql.Connection     conn = null;
        Statement               stmt = null;
        try
        {
            conn = DriverManager.getConnection(urlText.getString() + ";" + jdbcmeText.getString());

            stmt = conn.createStatement();

            // Since we don't want the prepared statement to persist,
            // a normal statement is really better in this environemnt.
            StringBuffer   buf = new StringBuffer(100);
            buf.append("Update QJdbcMe.RealEstate Set CurrentBid = ");
            buf.append(bidText.getString());
            buf.append(" Where MLS = '");
            buf.append(bidTargetKey);
            buf.append("' and CurrentBid < ");
            buf.append(bidText.getString());
            String      sql = buf.toString();

            int   updated = stmt.executeUpdate(sql);
            if (updated == 1)
            {
                // BID Accepted.
                String oldS = listings.getString(bidRow);
                int   commaIdx = bidTarget.indexOf(',');
                String bidAddr = bidTarget.substring(0, commaIdx);

                String newS = bidTargetKey + "," + bidAddr + ", $" + bidText.getString();

                ResultSet         rs = null;
                try
                {
                    // Creator and dbtype unused in MIDP
                    rs = new JdbcMeOfflineResultSet("JdbcMidpBidListings", 0, 0);
                    rs.absolute(bidRow+1);
                    rs.updateString(3, bidText.getString());
                    rs.close();
                }
                catch (Exception e)
                {
                    if (rs != null)
                        rs.close();
                }

                // Also update our live list of that result set.
                listings.set(bidRow, newS, null);
                display.setCurrent(listings);
                conn.commit();
            }
            else
            {
                conn.rollback();
                throw new SQLException("Failed to bid, someone beat you to it");
            }
        }
        catch (SQLException e)
        {
            // Return to the bid form after showing the error.
            showError(bidForm, e);
            return;
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception e)
                {
                }
            }
            conn = null;
            stmt = null;
        }

        // Exit without exception, then show the current bids
        showBids();
    }

    /**
     * Show an error condition.
     */
    public void showError(Displayable d, Exception e)
    {
        String s = e.toString();

        onErrorGoBackTo = d;
        errorForm = new Form("Error");
        errorForm.setTitle("SQL Error");
        errorForm.append(new StringItem("", s));
        errorForm.addCommand(backCommand);
        errorForm.setCommandListener(this);
        display.setCurrent(errorForm);
    }

    /**
     * Show the current bids.
     */
    public void showBids()
    {
        if (listings == null)
        {
            // If we have no current listings, lets set
            // them up.
            listings = new List("JdbcMe Bids", Choice.IMPLICIT);
            setupListingsFromOfflineData();
        }
        display.setCurrent(listings);
    }

    /**
     * Time to pause, free any space we don't need right now.
     */
    public void pauseApp()
    {
        display.setCurrent(null);
    }

    /**
     * Destroy must cleanup everything.
     */
    public void destroyApp(boolean unconditional)
    {
    }
}
