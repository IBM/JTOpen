///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputViewAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SpooledFile;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
The OutputViewAction class represents the action
of viewing a spooled file.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>OutputViewAction objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>WorkingEvent
</ul>
**/


class OutputViewAction
implements VAction, ActionListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // MRI.
    private static final String actualSizeText_     = ResourceLoader.getText("MENU_ACTUAL_SIZE");
    private static final String cancelText_         = ResourceLoader.getText("DLG_CANCEL");
    private static final String exitConfirmText_    = ResourceLoader.getText("DLG_CONFIRM_EXIT");
    private static final String exitConfirmTitle_   = ResourceLoader.getText("DLG_CONFIRM_EXIT_TITLE");
    private static final String exitText_           = ResourceLoader.getText("MENU_EXIT");
    private static final String fidelityText_       = ResourceLoader.getPrintText("VIEWING_FIDELITY");
    private static final String fileText_           = ResourceLoader.getText("MENU_FILE");
    private static final String firstPageText_      = ResourceLoader.getText("MENU_FIRST_PAGE");
    private static final String fitPageText_        = ResourceLoader.getText("MENU_FIT_PAGE");
    private static final String fitWidthText_       = ResourceLoader.getText("MENU_FIT_WIDTH");
    private static final String flashPageText_      = ResourceLoader.getText("MENU_FLASH_PAGE");
    private static final String goToText_           = ResourceLoader.getText("MENU_GO_TO_PAGE");
    private static final String hideStatusBarText_  = ResourceLoader.getText("MENU_HIDE_STATUS_BAR");
    private static final String hideToolBarText_    = ResourceLoader.getText("MENU_HIDE_TOOL_BAR");
    private static final String lastPageText_       = ResourceLoader.getText("MENU_LAST_PAGE");
    private static final String nextPageText_       = ResourceLoader.getText("MENU_NEXT_PAGE");
    private static final String okText_             = ResourceLoader.getText("DLG_OK");
    private static final String optionsText_        = ResourceLoader.getText("MENU_OPTIONS");
    private static final String paperSizeText_      = ResourceLoader.getPrintText("PAPER_SIZE");
    private static final String prevPageText_       = ResourceLoader.getText("MENU_PREVIOUS_PAGE");
    private static final String showStatusBarText_  = ResourceLoader.getText("MENU_SHOW_STATUS_BAR");
    private static final String showToolBarText_    = ResourceLoader.getText("MENU_SHOW_TOOL_BAR");
    private static final String viewActionText_     = ResourceLoader.getText("ACTION_VIEW");
    private static final String viewText_           = ResourceLoader.getText("MENU_VIEW");
    private static final String zoomText_           = ResourceLoader.getText("MENU_ZOOM");
    private static final Icon   SPLFVIcon           = ResourceLoader.getIcon("SpooledFileViewer16.gif");


    // Private data.
    private int                 currentPageNumber_  = 0;    // current page
    private boolean             enabled_            = true; // action enabled?
    private JFrame              frame_              = null; // OutputViewAction frame
    private int                 numberOfPages_      = 0;    // total number of pages
    private boolean             numberOfPagesEst_   = false;// is numberOfPages valid?
    private SpooledFile         spooledFile_        = null; // the spooled file
    private SpooledFileViewer   spooledFileViewer_  = null; // the spooled file viewer


    // Specialized menu items.
    private JMenuItem firstPageMI_  = null; // first page menu item
    private JMenuItem lastPageMI_   = null; // last page menu item
    private JMenuItem nextPageMI_   = null; // next page menu item
    private JMenuItem prevPageMI_   = null; // previous page menu item
    private JMenuItem statusBarMI_  = null; // status bar menu item
    private JMenuItem toolBarMI_    = null; // tool bar menu item


    // Event support.
    private ErrorEventSupport    errorEventSupport_     = new ErrorEventSupport(this);
    private VObjectEventSupport  objectEventSupport_    = new VObjectEventSupport(this);
    private WorkingEventSupport  workingEventSupport_   = new WorkingEventSupport(this);



/**
Constructs an OutputViewAction object.

@param object       The object.
@param spooledFile  The spooled file to view.
**/
    public OutputViewAction(VObject object, SpooledFile spooledFile) {

        // store spooled file
        if (spooledFile == null) {
            throw new NullPointerException("spooledFile");
        }
        else {
            spooledFile_ = spooledFile;
        }
    }



/**
Process the action event specified by <i>action</i>.

@param  action The ActionEvent to process.
**/
    public void actionPerformed(ActionEvent action) {

        // retrieve action command
        String command = action.getActionCommand();

        try {
            // perform requested action
            if (command.equals("actualSize")) {  // set page view to actual size
                spooledFileViewer_.actualSize();
            }
            else if (command.equals("fitWidth")) {  // set page view to width of frame
                spooledFileViewer_.fitWidth();
            }
            else if (command.equals("fitPage")) {  // set page view so page fits in frame
                spooledFileViewer_.fitPage();
            }
            else if (command.equals("zoom")) {  // set page view to arbitrary zoom value
                spooledFileViewer_.changeZoom();
            }
            else if (command.equals("firstPage")) {  // set view to first page
	            spooledFileViewer_.setCurrentPage(1);
	            spooledFileViewer_.loadPage();
	        }
	        else if (command.equals("prevPage")) {  // set view to previous page
                spooledFileViewer_.pageBack();
            }
            else if (command.equals("nextPage")) {  // set view to next page
                spooledFileViewer_.pageForward();
            }
	        else if (command.equals("lastPage")) {  // set view to last page
                spooledFileViewer_.setCurrentPage(numberOfPages_);
                spooledFileViewer_.loadPage();
	        }
            else if (command.equals("goToPage")) { // set view to specific page
                spooledFileViewer_.changeCurrentPage();
            }
            else if (command.equals("flashPage")) { //  set view to previously viewed page
                spooledFileViewer_.loadFlashPage();
            }
            else if (command.equals("hideToolBar")) {  // hide the tool bar
                spooledFileViewer_.removeToolBar();
                toolBarMI_.setText(showToolBarText_);
                toolBarMI_.setActionCommand("showToolBar");
                frame_.setVisible(true);
            }
            else if (command.equals("showToolBar")) {  // show the tool bar
                spooledFileViewer_.addToolBar();
                toolBarMI_.setText(hideToolBarText_);
                toolBarMI_.setActionCommand("hideToolBar");
                frame_.setVisible(true);
            }
            else if (command.equals("hideStatusBar")) { // hide the status bar
                spooledFileViewer_.removeStatusBar();
                statusBarMI_.setText(showStatusBarText_);
                statusBarMI_.setActionCommand("showStatusBar");
                frame_.setVisible(true);
            }
            else if (command.equals("showStatusBar")) {  // show the status bar
                spooledFileViewer_.addStatusBar();
                statusBarMI_.setText(hideStatusBarText_);
                statusBarMI_.setActionCommand("hideStatusBar");
                frame_.setVisible(true);
            }
            else if (command.equals("paperSize")) { // select/change pagesize
                spooledFileViewer_.changePaperSize();
            }
            else if (command.equals("viewingFidelity")) { // select/change viewing fidelity
                spooledFileViewer_.changeViewingFidelity();
            }
            else if (command.equals("exit")) {  // exit the OutputViewAction
                Object[] options = { okText_, cancelText_ };
                int exitOpt = JOptionPane.showOptionDialog(frame_, exitConfirmText_,
                                exitConfirmTitle_, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, options, options[0]);
                if (exitOpt == 0) {
	                frame_.setVisible(false);
	                spooledFileViewer_.close();
	                frame_.dispose();
	            }
            }

        }
        catch (Exception e) {  // if the SpooledFileViewer object throws an error.
            errorEventSupport_.fireError(e);
        }
    }



/**
Adds an ErrorListener.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a VObjectListener.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a WorkingListener.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Creates the menu bar.

@return A menu bar
**/
    private JMenuBar createMenuBar() {

        JMenu menu;
        JMenuItem menuItem;

        // create an instance of a menuBar
        JMenuBar menuBar = new JMenuBar();

	    // create 'File' menu
	    menu = new JMenu(fileText_);

	    // create 'Exit' menu item and add it to the menu
	    menuItem = new JMenuItem(exitText_);
	    menuItem.addActionListener(this);
	    menuItem.setActionCommand("exit");
	    menuItem.setEnabled(true);
	    menu.add(menuItem);

	    // add 'File' menu to menu bar
	    menuBar.add(menu);

        // create 'View' menu
        menu = new JMenu(viewText_);

        // create 'Actual Size' menu item and add it to the menu
	    menuItem = new JMenuItem(actualSizeText_);
	    menuItem.addActionListener(this);
    	menuItem.setActionCommand("actualSize");
    	menuItem.setEnabled(true);
	    menu.add(menuItem);

        // create 'Fit Width' menu item and add it to the menu
	    menuItem = new JMenuItem(fitWidthText_);
	    menuItem.addActionListener(this);
	    menuItem.setActionCommand("fitWidth");
	    menuItem.setEnabled(true);
	    menu.add(menuItem);

        // create 'Fit Page' menu item and add it to the menu
	    menuItem = new JMenuItem(fitPageText_);
	    menuItem.addActionListener(this);
        menuItem.setActionCommand("fitPage");
        menuItem.setEnabled(true);
	    menu.add(menuItem);

        // create 'Zoom' menu item and add it to the menu
	    menuItem = new JMenuItem(zoomText_);
	    menuItem.addActionListener(this);
	    menuItem.setActionCommand("zoom");
	    menuItem.setEnabled(true);
	    menu.add(menuItem);

    	menu.addSeparator();

        // create 'First Page' menu item and add it to the menu
	    firstPageMI_= new JMenuItem(firstPageText_);
	    firstPageMI_.addActionListener(this);
    	firstPageMI_.setActionCommand("firstPage");
        menu.add(firstPageMI_);

        // create 'Previous Page' menu item and add it to the menu
	    prevPageMI_ = new JMenuItem(prevPageText_);
	    prevPageMI_.addActionListener(this);
    	prevPageMI_.setActionCommand("prevPage");
	    menu.add(prevPageMI_);

        // create 'Next Page' menu item and add it to the menu
	    nextPageMI_ = new JMenuItem(nextPageText_);
	    nextPageMI_.addActionListener(this);
    	nextPageMI_.setActionCommand("nextPage");
    	menu.add(nextPageMI_);

        // create 'Last Page' menu item and add it to the menu
    	lastPageMI_ = new JMenuItem(lastPageText_);
    	lastPageMI_.addActionListener(this);
    	lastPageMI_.setActionCommand("lastPage");
        menu.add(lastPageMI_);

	    menu.addSeparator();

        // create 'Go To Page' menu item and add it to the menu
    	menuItem = new JMenuItem(goToText_);
    	menuItem.addActionListener(this);
    	menuItem.setActionCommand("goToPage");
    	menuItem.setEnabled(true);
	    menu.add(menuItem);

        // create 'Flash Page' menu item and add it to the menu
        menuItem = new JMenuItem(flashPageText_);
    	menuItem.addActionListener(this);
    	menuItem.setActionCommand("flashPage");
    	menuItem.setEnabled(true);
	    menu.add(menuItem);

        // add the 'View' menu to the menu bar
	    menuBar.add(menu);

        // create 'Options' menu
        menu = new JMenu(optionsText_);

        // create 'ToolBar' menu item, initializing text
        // to read 'Hide Toolbar', and add it to the menu
    	toolBarMI_ = new JMenuItem(hideToolBarText_);
    	toolBarMI_.addActionListener(this);
    	toolBarMI_.setActionCommand("hideToolBar");
    	toolBarMI_.setEnabled(true);
	    menu.add(toolBarMI_);

        // create 'StatusBar' menu item, initializing text
        // to read 'Hide Status Bar', and add it to the menu
        statusBarMI_ = new JMenuItem(hideStatusBarText_);
        statusBarMI_.addActionListener(this);
    	statusBarMI_.setActionCommand("hideStatusBar");
    	statusBarMI_.setEnabled(true);
	    menu.add(statusBarMI_);

	    menu.addSeparator();

        // create 'Paper Size' menu item and add it to the menu
        menuItem = new JMenuItem(paperSizeText_);
    	menuItem.addActionListener(this);
    	menuItem.setActionCommand("paperSize");
    	menuItem.setEnabled(true);
	    menu.add(menuItem);

	    // create 'Viewing Fidelity' menu item and add it to the menu
        menuItem = new JMenuItem(fidelityText_);
    	menuItem.addActionListener(this);
    	menuItem.setActionCommand("viewingFidelity");
    	menuItem.setEnabled(true);
	    menu.add(menuItem);

        // add 'Options' menu to menu bar
        menuBar.add(menu);

        // invoke updateMenu, which determines what menu items to enable/not enable
        updateMenu();

        // return the menu bar
	    return menuBar;
	}



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Returns the text for this action.

@return The text for the action.
**/
    public String getText()
    {
        return viewActionText_;
    }



/**
Indicates if the action is enabled.

@return true if the action is enabled, false otherwise.
**/
    public boolean isEnabled ()
    {
        return enabled_;
    }



/**
Invokes the method to perform the action of viewing a spooled file.

@param  actionContext The action context.
**/
    public void perform (VActionContext actionContext)
    {
        try {
            // fire start working event
            workingEventSupport_.fireStartWorking();

            // create an instance of a SpooledFileViewer
            spooledFileViewer_  = new SpooledFileViewer(spooledFile_, 1);

            // create local error listener
            SFVErrorListener_ errListener = new SFVErrorListener_();

            // add listeners
            spooledFileViewer_.addErrorListener (errListener);
            spooledFileViewer_.addWorkingListener(workingEventSupport_);
            spooledFileViewer_.addErrorListener(errorEventSupport_);

            // load the data
            spooledFileViewer_.load();

            // if (errListener.lastEvent.getException() instanceof IOException
            if (errListener.lastEvent_ == null) {
                // create and add property change listener
                // (This is required for notification of when the viewer has changed
                //  its page view, number of pages, or pages estimated value.)
                SFVPropertyListener_ propListener = new SFVPropertyListener_();
                spooledFileViewer_.addPropertyChangeListener(propListener);

                // update monitored property variables
                currentPageNumber_  = spooledFileViewer_.getCurrentPage();
                numberOfPages_      = spooledFileViewer_.getNumberOfPages();
                numberOfPagesEst_   = spooledFileViewer_.isNumberOfPagesEstimated();

                // create the frame to put the SpooledFileViewer in
                frame_ = new JFrame(spooledFile_.getName());
                frame_.addWindowListener(new WindowListener_());
                if (SPLFVIcon != null) {                                  // @A1A
                     frame_.setIconImage(((ImageIcon)(SPLFVIcon)).getImage());
                }                                                         // @A1A

                // create and set the menu bar
                frame_.setJMenuBar(createMenuBar());

                // add the spooled file viewer to the content pane
                frame_.getContentPane().add(spooledFileViewer_);

                // pack and show the frame
                frame_.pack();
	            frame_.setLocation(50, 50);
	            frame_.setVisible(true);
	        }
	    }
	    catch (Exception e) {
	        errorEventSupport_.fireError(e);
	    }
        finally {
            // fire stop working event
            workingEventSupport_.fireStopWorking();
        }
     }



/**
Removes an ErrorListener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }



/**
Removes a WorkingListener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



/**
Sets the enabled state of the action.

@param enabled true if the action is enabled, false otherwise.
**/
    public void setEnabled(boolean enable)
    {
        enabled_ = enable;
    }



/**
Performs the action of updating (enabling/disabling) the menu items.
**/
    private synchronized void updateMenu()
    {
        // enabled/disable 'first' and 'previous' page buttons
	    if (currentPageNumber_ > 1) {  // not on first page
            firstPageMI_.setEnabled(true);
            prevPageMI_.setEnabled(true);
        }
        else {
    	    firstPageMI_.setEnabled(false);
    	    prevPageMI_.setEnabled(false);
    	}

        // enable/disable 'next' and 'last' page buttons

        // All spooled files have a 'number of pages' attribute. However,
        // for some spooled files, this value is an 'estimated' quantity.
        // Another attribute associated with the spooled file allows us
        // to know whether this total 'number of pages' is real or estimated.
        // We have stored that attribute value in numberOfPagesEst_.
        if (numberOfPagesEst_ == false) {
            if (currentPageNumber_ < numberOfPages_) { // not on last page
    	        nextPageMI_.setEnabled(true);
    	        lastPageMI_.setEnabled(true);
    	    }
    	    else {
    	        nextPageMI_.setEnabled(false);
    	        lastPageMI_.setEnabled(false);
	        }
	    }
	    else {
	        nextPageMI_.setEnabled(true);
            lastPageMI_.setEnabled(false);
        }
    }



/*************************************************************************/



/**
The WindowListener_ class processes window events.
**/
    private class WindowListener_ extends WindowAdapter {

        public void windowClosing (WindowEvent event)
        {
            frame_.setVisible(false);
            spooledFileViewer_.close();
            frame_.dispose();
        }

    }



/*************************************************************************/



/**
The SFVErrorListener_ class listens for error events.
**/
    private class SFVErrorListener_ implements ErrorListener {

        public ErrorEvent lastEvent_ = null;
        public void errorOccurred (ErrorEvent event)
        {
            lastEvent_ = event;
        }
    }



/*************************************************************************/



/**
The SFVPropertyListener class processes property changes by implementing a
PropertyChangeListener.
**/
    private class SFVPropertyListener_ implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent event)
        {
            String changedProperty = event.getPropertyName();

            if (changedProperty.equals("currentPage")) {
                currentPageNumber_ = spooledFileViewer_.getCurrentPage();
                updateMenu();
            }
            else if (changedProperty.equals("numberOfPages")) {
                numberOfPages_ = spooledFileViewer_.getNumberOfPages();
                updateMenu();
            }
            else if (changedProperty.equals("numberOfPagesEstimated")) {
                numberOfPagesEst_ = spooledFileViewer_.isNumberOfPagesEstimated();
                updateMenu();
            }
            else if (changedProperty.equals("spooledFile")) {
                spooledFile_ = spooledFileViewer_.getSpooledFile();
            }
        }


        /**
        Copyright.
        **/
        private String getCopyright ()
        {
            return Copyright_v.copyright;
        }

    }

}

