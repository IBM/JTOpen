///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileViewer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrintObjectPageInputStream;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.SpooledFile;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;    // @A5A
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.*;

/**
The SpooledFileViewer class represents an AS/400 spooled file viewer.
You can create an instance of this class to view an individual
AFPDS or SCS spooled file on the AS/400. Viewer functions such as page forward,
page back, set current page, and so on, are provided.

The following properties can be set directly, but require the
invocation of load() to load the information from the AS/400.
<ul>
    <li> paper size
    <li> spooled file
    <li> viewing fidelity
</ul>

The following properties can be set directly, but require the
invocation of loadPage() to load the information from the AS/400.
<ul>
    <li> current page
</ul>


<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>SpooledFileViewer objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VetoableChangeEvent
    <li>WorkingEvent
</ul>

(To create new spooled files on the AS/400, use the
<b>SpooledFileOutputStream</b> class.

See <a href="../../../../SpooledFileAttrs.html">Spooled File Attributes</a> for
valid attributes.)

<p>The following example creates a spooled file viewer
to display a spooled file previously created on the AS/400.

<pre>
// Assume splf is the spooled file.
// Create the spooled file viewer
SpooledFileViewer splfv = new SpooledFileViewer(splf, 1);
splfv.load();

// Add the spooled file viewer to a frame
JFrame frame = new JFrame("My Window");
frame.getContentPane().add(splfv);
</pre>

@see SpooledFile
**/


public class SpooledFileViewer
extends JComponent
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    /** Represents *ABSOLUTE viewing fidelity **/
    public static final int ABSOLUTE_FIDELITY   =   0;
    /** Represents *CONTENT viewing fidelity **/
    public static final int CONTENT_FIDELITY    =   1;
    /** Represents letter paper size (8.5 x 11 inches) **/
    public static final int LETTER      =   0;
    /** Represents legal paper size (8.5 x 14 inches) **/
    public static final int LEGAL       =   1;
    /** Represents A3 paper size (297 x 420 mm) **/
    public static final int A3          =   2;
    /** Represents A4 paper size (210 x 297 mm) **/
    public static final int A4          =   3;
    /** Represents A5 paper size (148 x 210 mm) **/
    public static final int A5          =   4;
    /** Represents B4 paper size (257 x 364 mm) **/
    public static final int B4          =   5;
    /** Represents B5 paper size (182 x 257 mm) **/
    public static final int B5          =   6;
    /** Represents executive paper size (7.25 x 10.5 inches) **/
    public static final int EXECUTIVE   =   7;
    /** Represents ledger paper size (17 x 11 inches) **/
    public static final int LEDGER      =   8;
    /** Represents continuous feed 80 paper size (8 x 11 inches) **/
    public static final int CONT80      =   9;
    /** Represents continuous feed 132 paper size (13.2 x 11 inches) **/
    public static final int CONT132     =  10;
    /** Represents no paper size **/
    public static final int NONE        =  11;

    // Private constants.
    private static final int    BSIZE_              =  36;  // size of (square) button edge
    private static final int    LISTBOXWIDTH_       = 300;  // width of list box
    private static final int    LISTBOXHEIGHT_      = 175;  // height of list box
    private static final int    PREF_WIDTH          = 465;  // preferred width of viewer
    private static final int    PREF_HEIGHT         = 600;  // preferred height of viewer
    private static final int    TOOLBARSIZE_        =  38;  // size of tool bar
    private static final int    TOPOFFSET_          =   1;  // offset from top
    private static final String WSCSTOBJ   = "/QSYS.LIB/QWPGIF.WSCST"; // wkrstn cust object


    // MRI.
    private static final String a3Text_             = ResourceLoader.getPrintText("PAPER_SIZE_A3");
    private static final String a4Text_             = ResourceLoader.getPrintText("PAPER_SIZE_A4");
    private static final String a5Text_             = ResourceLoader.getPrintText("PAPER_SIZE_A5");
    private static final String absoluteText_       = ResourceLoader.getPrintText("FIDELITY_ABSOLUTE");
    private static final String actualSizeText_     = ResourceLoader.getText("MENU_ACTUAL_SIZE");
    private static final String b4Text_             = ResourceLoader.getPrintText("PAPER_SIZE_B4");
    private static final String b5Text_             = ResourceLoader.getPrintText("PAPER_SIZE_B5");
    private static final String cancelText_         = ResourceLoader.getText("DLG_CANCEL");
    private static final String cont80Text_         = ResourceLoader.getPrintText("PAPER_SIZE_CONT80");
    private static final String cont132Text_        = ResourceLoader.getPrintText("PAPER_SIZE_CONT132");
    private static final String contentText_        = ResourceLoader.getPrintText("FIDELITY_CONTENT");
    private static final String curPaperText_       = ResourceLoader.getPrintText("CURRENT_PAPER_SIZE");
    private static final String curVFidelityText_   = ResourceLoader.getPrintText("CURRENT_VIEWING_FIDELITY");
    private static final String executiveText_      = ResourceLoader.getPrintText("PAPER_SIZE_EXECUTIVE");
    private static final String firstPageText_      = ResourceLoader.getText("MENU_FIRST_PAGE");
    private static final String fitPageText_        = ResourceLoader.getText("MENU_FIT_PAGE");
    private static final String fitWidthText_       = ResourceLoader.getText("MENU_FIT_WIDTH");
    private static final String flashPageText_      = ResourceLoader.getText("MENU_FLASH_PAGE");
    private static final String goToText_           = ResourceLoader.getText("MENU_GO_TO_PAGE");
    private static final String goToPageText_       = ResourceLoader.getPrintText("GO_TO_PAGE");
    private static final String lastPageText_       = ResourceLoader.getText("MENU_LAST_PAGE");
    private static final String ledgerText_         = ResourceLoader.getPrintText("PAPER_SIZE_LEDGER");
    private static final String legalText_          = ResourceLoader.getPrintText("PAPER_SIZE_LEGAL");
    private static final String letterText_         = ResourceLoader.getPrintText("PAPER_SIZE_LETTER");
    private static final String nextPageText_       = ResourceLoader.getText("MENU_NEXT_PAGE");
    private static final String noneText_           = ResourceLoader.getPrintText("NONE");
    private static final String okText_             = ResourceLoader.getText("DLG_OK");
    private static final String paperSizeText_      = ResourceLoader.getPrintText("PAPER_SIZE");
    private static final String paperSizeWarnText_  = ResourceLoader.getPrintText("WARNING_PAPER_SIZE");
    private static final String prevPageText_       = ResourceLoader.getText("MENU_PREVIOUS_PAGE");
    private static final String vFidelityText_      = ResourceLoader.getPrintText("VIEWING_FIDELITY");
    private static final String vFidelityWarnText_  = ResourceLoader.getPrintText("WARNING_FIDELITY");
    private static final String warningText_        = ResourceLoader.getPrintText("WARNING");
    private static final String zoomText_           = ResourceLoader.getText("MENU_ZOOM");


    // Icons.
    private static final Icon   iconActualSize_     = ResourceLoader.getIcon("ResetViewIcon.gif");
    private static final Icon   iconFirstPage_      = ResourceLoader.getIcon("FirstIcon.gif");
    private static final Icon   iconFitPage_        = ResourceLoader.getIcon("FitPageIcon.gif");
    private static final Icon   iconFitWidth_       = ResourceLoader.getIcon("FitWidthIcon.gif");
    private static final Icon   iconFlashPage_      = ResourceLoader.getIcon("FlashIcon.gif");
    private static final Icon   iconGoToPage_       = ResourceLoader.getIcon("GoToIcon.gif");
    private static final Icon   iconLastPage_       = ResourceLoader.getIcon("LastIcon.gif");
    private static final Icon   iconNextPage_       = ResourceLoader.getIcon("NextIcon.gif");
    private static final Icon   iconPaperSize_      = ResourceLoader.getIcon("PaperIcon.gif");
    private static final Icon   iconPrevPage_       = ResourceLoader.getIcon("PreviousIcon.gif");
    private static final Icon   iconViewFidelity_   = ResourceLoader.getIcon("VFIcon.gif");
    private static final Icon   iconZoom_           = ResourceLoader.getIcon("ZoomIcon.gif");


    // Paper sizes.
    private static final String[] paperSizes        = {letterText_, legalText_, a3Text_,
                                                        a4Text_, a5Text_, b4Text_,
                                                        b5Text_, executiveText_, ledgerText_,
                                                        cont80Text_, cont132Text_, noneText_ };

    private static final String[] paperSizeValues   = {"*LETTER", "*LEGAL", "*A3", "*A4",
                                                       "*A5", "*B4", "*B5", "*EXECUTIVE", "*LEDGER",
                                                       "*CONT80", "*CONT132", "*NONE"};
    // Viewing fidelities.
    private static final String[] viewingFidelities = {absoluteText_, contentText_ };
    private static final String[] viewingValues     = {"*ABSOLUTE", "*CONTENT"};


    // Static variables.
    private static int sPaperSize_                  = LETTER;           // default paper size
    private static int sViewingFidelity_            = CONTENT_FIDELITY; // default viewing fidelity
    private static boolean paperSizeChecked_        = false;            // default to false


    // Transient data.
    transient private Image currentPageImage_   = null; // Image of page currently in view
    transient private Image flashPageImage_     = null; // Image of previously viewed page
    transient private PrintObjectPageInputStream spooledFileIS_ = null;   // the input stream


    // Serializable data.
    private boolean initialized_        = false;// indicates if a spooled file has been loaded
    private boolean numberOfPagesEst_   = false;// is numberOfPages estimated?
    private int currentPageNumber_      =   0;  // page number of current page
    private int flashPageNumber_        =   0;  // page number of previously viewed page
    private int knownPages_             =   0;  // total number of pages known to exist
    private int numberOfPages_          =   0;  // total number of pages
    private int oldCurrentPage_         =   0;  // old current page (most recently loaded)
    private int paperSize_              =   0;  // current paper size
    private int viewingFidelity_        =   0;  // current viewing fidelity
    private float zoomPercentage_       = 100;  // zoom percentage  @A1C changed int to float


    // Buttons.
    //@A3C - Made all JButtons transient.
    transient private JButton actualButton_       = null; // 'return page to actual size' button
    transient private JButton firstPageButton_    = null; // 'view first page' button
    transient private JButton fitPageButton_      = null; // 'fit page in view' button
    transient private JButton fitWidthButton_     = null; // 'view flash page' button
    transient private JButton flashButton_        = null; // 'view previously viewed page' button
    transient private JButton gotoButton_         = null; // 'go to specific page' button
    transient private JButton lastPageButton_     = null; // 'view last page' button
    transient private JButton nextPageButton_     = null; // 'view next page' button
    transient private JButton paperSizeButton_    = null; // 'select paper size' button
    transient private JButton prevPageButton_     = null; // 'view previous page' button
    transient private JButton viewingFidelityButton_= null; // 'select viewing fidelity' button
    transient private JButton zoomButton_         = null; // 'zoom' button

    //@A3C - Made the JPanels, JScrollPane, and JTextFields transient.
    transient private JPanel statusBar_           = null; // the status bar (for text fields)
    transient private JPanel toolBar_             = null; // the tool bar (for action buttons)
    transient private JScrollPane scrollView_     = null; // container for the viewport
    private SpooledFile spooledFile_    = null; // the spooled file
    private SpooledFilePageView_ pageView_ = null; // view of a spooled file page (image)
    private String estimateStar_        = "";   // visual indicator that numberOfPages is estimated
    transient private JTextField pageInfo_        = null; // text field indicating current page
    transient private JTextField zoomInfo_        = null; // text field indicating zoom percentage


    // Event support.
    transient private ErrorEventSupport     errorEventSupport_;
    transient private PropertyChangeSupport propertyChangeSupport_;
    transient private VetoableChangeSupport vetoableChangeSupport_;
    transient private WorkingEventSupport   workingEventSupport_;



/**
Constructs a SpooledFileViewer object.  A call to setSpooledFile() must
be done after calling this method in order to set the spooled
file to be viewed.
**/
    public SpooledFileViewer()
    {
        spooledFile_ = null;
        createViewer();
    }



/**
Constructs a SpooledFileViewer object.  By default, the current page is set to
the first page of the spooled file.  A call to load() must be done
after calling this method in order to load the spooled file.

@param  spooledFile The spooled file to view.
**/
    public SpooledFileViewer(SpooledFile spooledFile)
    {
        if (spooledFile == null)
            throw new NullPointerException("spooledFile");
        spooledFile_ = spooledFile;
        createViewer();
        currentPageNumber_ = 1;
    }



/**
Constructs a SpooledFileViewer object.  Page <i>page</i> of the spooled file
is loaded as the initial view.  If <i>page</i> is less than 1, an error is thrown.
A call to load() must be done after calling this method in order to
load the spooled file.

@param  spooledFile The spooled file to view.
@param  page        The initial page to view.
**/
    public SpooledFileViewer(SpooledFile spooledFile, int page)
    {
        if (spooledFile == null)
            throw new NullPointerException("spooledFile");
        if (page < 1) {
            throw new IllegalArgumentException("page");
        }

        spooledFile_ = spooledFile;
        createViewer();
        currentPageNumber_ = page;
    }



/**
Resets the size of the page image to its original size.  This method is only
valid after a spooled file has been loaded.
**/
    public void actualSize()
    {
        if (initialized_ == true) {
            // clear view
            pageView_.clearView();

            // adjust width and height of page image to image's actual size
            Dimension d = new Dimension(currentPageImage_.getWidth(this),
                                        currentPageImage_.getHeight(this));
            pageView_.setViewSize(d);

            // calculate the new viewing zoom percentage
            calculateZoom();

            // update the viewer
            updateViewer();
       }
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener(ErrorListener listener)
    {
        errorEventSupport_.addErrorListener(listener);
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        super.addPropertyChangeListener(listener);
        propertyChangeSupport_.addPropertyChangeListener(listener);
    }



/**
Adds the status bar.
**/
    public void addStatusBar()
    {
        add("South", statusBar_);
        validate();   // @A4A - validate forces redraw
        // paintAll(this.getGraphics());  @A4D
    }



/**
Adds the tool bar.
**/
    public void addToolBar()
    {
        add("North", toolBar_);
        validate();  // @A4A - validate forces redraw
        // paintAll(this.getGraphics());  @A4D
    }



/**
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        super.addVetoableChangeListener(listener);
        vetoableChangeSupport_.addVetoableChangeListener(listener);
    }



/**
Adds a listener to be notified of a working state.

@param  listener    The listener.
**/
    public void addWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener(listener);
    }



/**
Calculate zoom percentage.
**/
    private void calculateZoom()
    {
        float imageWidth = (float)getPageImageSize().width;  // @A1C double to float
        if (imageWidth != 0) {
          /*  zoomPercentage_ = (int)java.lang.Math.ceil((((double)pageView_.getWidth()/
                                                     imageWidth) * 100.0));  */
            zoomPercentage_ = (((float)pageView_.getWidth()/imageWidth) * 100);  // @A1C
        }
        else
            errorEventSupport_.fireError(new ArithmeticException());
    }



/**
Creates and shows a 'Go To Page' dialog.  This dialog will allow
the user to graphically input a page to view.
**/
    void changeCurrentPage()
    {
        GoToBox_ theGoToBox;
        theGoToBox = new GoToBox_((JFrame)VUtilities.getFrame(this));
        theGoToBox.setVisible(true);
    }



/**
Creates and shows a 'Paper Size' dialog.  This dialog will
display a list of valid paper sizes and allow the user to select one.
**/
    void changePaperSize()
    {
        PaperSizeBox_ thePaperSizeBox;
        thePaperSizeBox = new PaperSizeBox_((JFrame)VUtilities.getFrame(this));
        thePaperSizeBox.setVisible(true);
    }



/**
Creates and shows a 'Viewing Fidelity' dialog Box.  This dialog will
display a list of valid viewing fidelities and allow the user to select
one.
**/
    void changeViewingFidelity()
    {
        ViewingFidelityBox_ theViewingFidelityBox;
        theViewingFidelityBox = new ViewingFidelityBox_((JFrame)VUtilities.getFrame(this));
        theViewingFidelityBox.setVisible(true);
    }



/**
Creates and shows a 'Zoom' Dialog Box.  This dialog box will
display a set of radio buttons and a data entry field for entering
a magnification (zoom) percentage.
**/
    void changeZoom()
    {
        ZoomToBox_ theZoomToBox;
        theZoomToBox = new ZoomToBox_((JFrame)VUtilities.getFrame(this));
        theZoomToBox.setVisible(true);
    }



/**
Closes the viewer.
**/
    public void close()
    {
    try {
        // close the page input stream and return the conversation
        if (spooledFileIS_ != null) {
            spooledFileIS_.close();
            spooledFileIS_ = null;   // @A2A
        }
    }
        catch (IOException e) {}
    }



/**
Creates the status bar.

@return The status bar.
**/
    private JPanel createStatusBar()
    {
        // create a panel for the status bar
        JPanel statusBar = new JPanel();

        // create page information field
        pageInfo_ = new JTextField(22);
        pageInfo_.setEditable(false);
        pageInfo_.transferFocus();

        // create zoom percentage field
        zoomInfo_ = new JTextField(8);
        zoomInfo_.setEditable(false);
        zoomInfo_.transferFocus();

        // set layout...
        statusBar.setLayout(new FlowLayout(FlowLayout.LEFT));

        // add both fields to the status bar
        statusBar.add(pageInfo_);
        statusBar.add(zoomInfo_);

        // return the status bar
        return statusBar;
    }



/**
Creates the tool bar

@return The tool bar.
**/
    private JPanel createToolBar()
    {
        // create an instance of a JPanel for a tool bar
        JPanel toolBar   = new JPanel();
        Insets insets = toolBar.getInsets();
        ViewerActionListener_ listener = new ViewerActionListener_();

        // set preferred size
        toolBar.setPreferredSize(new Dimension(TOOLBARSIZE_,TOOLBARSIZE_));

        // set null layout...we will position the buttons manually
        toolBar.setLayout(null);

        // create the 'Actual size' button and add it to the tool bar
        actualButton_ = new JButton(iconActualSize_);
        toolBar.add(actualButton_);
        actualButton_.setBounds(insets.left + 1, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        actualButton_.addActionListener(listener);
        actualButton_.setToolTipText(actualSizeText_);
        actualButton_.setActionCommand("actualSize");

        // create the 'Fit Width' button and add it to the tool bar
        fitWidthButton_ = new JButton(iconFitWidth_);
        toolBar.add(fitWidthButton_);
        fitWidthButton_.setBounds(insets.left + 37, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        fitWidthButton_.addActionListener(listener);
        fitWidthButton_.setToolTipText(fitWidthText_);
        fitWidthButton_.setActionCommand("fitWidth");

        // create the 'Fit Page' button and add it to the tool bar
        fitPageButton_ = new JButton(iconFitPage_);
        toolBar.add(fitPageButton_);
        fitPageButton_.setBounds(insets.left + 73, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        fitPageButton_.addActionListener(listener);
        fitPageButton_.setToolTipText(fitPageText_);
        fitPageButton_.setActionCommand("fitPage");

        // create the 'Zoom' button and add it to the tool bar
        zoomButton_ = new JButton(iconZoom_);
        toolBar.add(zoomButton_);
        zoomButton_.setBounds(insets.left + 109, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        zoomButton_.addActionListener(listener);
        zoomButton_.setToolTipText(zoomText_);
        zoomButton_.setActionCommand("zoom");

        // create the 'Go To Page' button and add it to the tool bar
        gotoButton_ = new JButton(iconGoToPage_);
        toolBar.add(gotoButton_);
        gotoButton_.setBounds(insets.left + 152, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        gotoButton_.addActionListener(listener);
        gotoButton_.setToolTipText(goToText_);
        gotoButton_.setActionCommand("goToPage");

        // create the 'First Page' button and add it to the tool bar
        firstPageButton_= new JButton(iconFirstPage_);
        toolBar.add(firstPageButton_);
        firstPageButton_.setBounds(insets.left + 195, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        firstPageButton_.addActionListener(listener);
        firstPageButton_.setToolTipText(firstPageText_);
        firstPageButton_.setActionCommand("firstPage");

        // create the 'Previous Page' button and add it to the tool bar
        prevPageButton_= new JButton(iconPrevPage_);
        toolBar.add(prevPageButton_);
        prevPageButton_.setBounds(insets.left + 231,insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        prevPageButton_.addActionListener(listener);
        prevPageButton_.setToolTipText(prevPageText_);
        prevPageButton_.setActionCommand("prevPage");

        // create the 'Next Page' button and add it to the tool bar
        nextPageButton_= new JButton(iconNextPage_);
        toolBar.add(nextPageButton_);
        nextPageButton_.setBounds(insets.left + 267, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        nextPageButton_.addActionListener(listener);
        nextPageButton_.setToolTipText(nextPageText_);
        nextPageButton_.setActionCommand("nextPage");

        // create the 'Last Page' button and add it to the tool bar
        lastPageButton_= new JButton(iconLastPage_);
        toolBar.add(lastPageButton_);
        lastPageButton_.setBounds(insets.left + 303,insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        lastPageButton_.addActionListener(listener);
        lastPageButton_.setToolTipText(lastPageText_);
        lastPageButton_.setActionCommand("lastPage");

        // create the 'Flash Page' button and add it to the tool bar
        flashButton_ = new JButton(iconFlashPage_);
        toolBar.add(flashButton_);
        flashButton_.setBounds(insets.left + 346, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        flashButton_.addActionListener(listener);
        flashButton_.setToolTipText(flashPageText_);
        flashButton_.setActionCommand("flashPage");

        // create the 'Paper Size' button and add it to the tool bar
        paperSizeButton_ = new JButton(iconPaperSize_);
        toolBar.add(paperSizeButton_);
        paperSizeButton_.setBounds(insets.left + 389, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        paperSizeButton_.addActionListener(listener);
        paperSizeButton_.setToolTipText(paperSizeText_);
        paperSizeButton_.setActionCommand("changePaperSize");

        // create the 'Viewing Fidelity' button and add it to the tool bar
        viewingFidelityButton_ = new JButton(iconViewFidelity_);
        toolBar.add(viewingFidelityButton_);
        viewingFidelityButton_.setBounds(insets.left + 425, insets.top + TOPOFFSET_, BSIZE_, BSIZE_);
        viewingFidelityButton_.addActionListener(listener);
        viewingFidelityButton_.setToolTipText(vFidelityText_);
        viewingFidelityButton_.setActionCommand("changeViewingFidelity");

        // return the tool bar
        return toolBar;
    }



/**
Constructs the viewer for the spooled file object.  All visual aspects of the
viewer (toolbar, scrolling view window, status bar) are created.  This
method also calls initializeTransient() to initialize the transient data.
**/
    private void createViewer()
    {
        // initialize transient data
        initializeTransient();

        // initialize paper size
        if (paperSizeChecked_ == false) {  // if paper size hasn't been initialized
            try {
                // get default locale for determining paper size
                Locale country = Locale.getDefault();

                String cntry = country.getISO3Country();
                if ((cntry.equals("USA")) || (cntry.equals("CAN")) ||
                    (cntry.equals("BRA")) || (cntry.equals("MEX"))) {
                    // non metric country, select LETTER paper size
                    paperSize_    = LETTER;
                    }
                else {
                    // metric country, select A4 paper size
                    paperSize_    = A4;
                }
            }
            // catch MissingResourceException
            catch (MissingResourceException mre) {
                // trace the error
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Default paper size selected");
                    paperSize_    = A4;
            }
            finally {
                paperSizeChecked_ = true;  // paper size has been initialized
            }
        }
        else {
            paperSize_ = sPaperSize_;
        }

        viewingFidelity_ = sViewingFidelity_;

        // set border layout for the frame
        setLayout(new BorderLayout());

        // create the tool bar and add it
        toolBar_ = createToolBar();
        addToolBar();

        // create the scolling view pane and add it
        scrollView_ = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        add("Center", scrollView_);

        // create the staus bar and add it
        statusBar_ = createStatusBar();
        addStatusBar();

        // disable the viewer buttons
        disableViewerButtons();

        // show the viewer
        repaint();
    }



/**
Disables the viewer buttons.
**/
    private void disableViewerButtons()
    {
        actualButton_.setEnabled(false);            // 'return page to actual size' button
        firstPageButton_.setEnabled(false);         // 'view first page' button
        fitPageButton_.setEnabled(false);           // 'fit page in view' button
        fitWidthButton_.setEnabled(false);          // 'view flash page' button
        flashButton_.setEnabled(false);             // 'view previously viewed page' button
        gotoButton_.setEnabled(false);              // 'go to specific page' button
        lastPageButton_.setEnabled(false);          // 'view last page' button
        nextPageButton_.setEnabled(false);          // 'view next page' button
        paperSizeButton_.setEnabled(false);         // 'select paper size' button
        prevPageButton_.setEnabled(false);          // 'view previous page' button
        viewingFidelityButton_.setEnabled(false);   // 'viewing fidelity' button
        zoomButton_.setEnabled(false);              // 'zoom' button
    }



/**
Creates and shows a 'Warning' dialog.  This dialog is displayed to warn
that the property being changed will cause the spooled file being viewed to
be closed and re-opened.  The appropriate 'set <property>' method is then invoked, depending
on the property being changed.

@param property The property requested to be changed.
    <br>
                   May be any of the following values:
    <ul>
        <li> PAPER_SIZE - The paper size
        <li> VIEWING_FIDELITY - The viewing fidelity
    </ul>
<p>
@param value    The new property value.
    <p>
    For PAPER_SIZE, this parameter may be any of the
    following values:
    <ul>
        <li> LETTER    - Letter (8.5 x 11 inches)
        <li> LEGAL     - Legal (8.5 x 14 inches)
        <li> A3        - A3 (297 x 420 mm)
        <li> A4        - A4 (210 x 297 mm)
        <li> A5        - A5 (148 x 210 mm)
        <li> B4        - B4 (257 x 364 mm)
        <li> B5        - B5 (182 x 257 mm)
        <li> EXECUTIVE - Executive (7.25 x 10.5 inches)
        <li> LEDGER    - Ledger (17 x 11 inches)
        <li> CONT80    - Continuous feed 80 (8 x 11 inches)
        <li> CONT132   - Continuous feed 132  (12.2 x 11 inches)
        <li> NONE      - None
    </ul>
    <p>
    For VIEWING_FIDLEITY, this parameter may be any
    of the following values:
    <ul>
        <li> ABSOLUTE
        <li> CONTENT
    </ul>
**/
    void displayPropertyChangeWarning(String property,
                                      int value)
    {
        boolean internetExplorer = false;                                       // @A5A
        int selectedValue;
        Object[] options = { okText_, cancelText_ };

        Class policyEngineClass = null;                                         // @A5A
        try {                                                                   // @A5A
            policyEngineClass = Class.forName("com.ms.security.PolicyEngine");  // @A5A
        }                                                                       // @A5A
        catch (Throwable e) {}                                                  // @A5A
        if (policyEngineClass != null) {                                        // @A5A
            internetExplorer = true;                                            // @A5A
        }                                                                       // @A5A

        if (property.equals("viewingFidelity")) {
          // display viewing fidelity warning and set accordingly
          if (!internetExplorer) {                                              // @A5A
              selectedValue = JOptionPane.showOptionDialog(this, vFidelityWarnText_,
              warningText_, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
              null, options, options[0]);
              if (selectedValue == 0) {
                try {
                    setViewingFidelity(value);
                    load();
                }
                catch (Exception e) {}  // absorb all exceptions - if an error
                                        // occurred - an error event was fired.
              }
          }                                                                                     // @A5A
          else {                                                                                // @A5A
            WarningDialogBox_ wDialog = new WarningDialogBox_((JFrame)VUtilities.getFrame(this),
                                                        vFidelityWarnText_, property, value);   // @A5A
            wDialog.setVisible(true);                                                           // @A5A
          }                                                                                     // @A5A
        }
        else if (property.equals("paperSize")) {
           // display paper size warning and set accordingly
           if (!internetExplorer) {                                             // @A5A
              selectedValue = JOptionPane.showOptionDialog(this, paperSizeWarnText_,
              warningText_, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
              null, options, options[0]);
              if (selectedValue == 0) {
                try {
                    setPaperSize(value);
                    load();
                }
                catch (Exception e) {}  // absorb all exceptions - if an error
                                        // occurred - an error event was fired.
            }
          }                                                                                     // @A5A
          else {                                                                                // @A5A
            WarningDialogBox_ wDialog = new WarningDialogBox_((JFrame)VUtilities.getFrame(this),
                                                        paperSizeWarnText_, property, value);   // @A5A
            wDialog.setVisible(true);                                                           // @A5A
          }                                                                                     // @A5A
        }
        else {
            if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Property for 'displayPropertyChangeWarning' not valid");
        }
    }



/**
Enables the viewer buttons.
**/
    private void enableViewerButtons()
    {
        actualButton_.setEnabled(true);             // 'return page to actual size' button
        firstPageButton_.setEnabled(true);          // 'view first page' button
        fitPageButton_.setEnabled(true);            // 'fit page in view' button
        fitWidthButton_.setEnabled(true);           // 'view flash page' button
        flashButton_.setEnabled(true);              // 'view previously viewed page' button
        gotoButton_.setEnabled(true);               // 'go to specific page' button
        lastPageButton_.setEnabled(true);           // 'view last page' button
        nextPageButton_.setEnabled(true);           // 'view next page' button
        paperSizeButton_.setEnabled(true);          // 'select paper size' button
        prevPageButton_.setEnabled(true);           // 'view previous page' button
        viewingFidelityButton_.setEnabled(true);    // 'viewing fidelity' button
        zoomButton_.setEnabled(true);               // 'zoom' button
    }



/**
Closes the viewer.
@exception Throwable  If an error occurs during cleanup.
**/
    protected void finalize() throws Throwable
    {
        try {
            // close the page input stream and return the conversation
            if (spooledFileIS_ != null)
                spooledFileIS_.close();
        }
        catch (IOException e) {}
        super.finalize();
    }



/**
Stretches the size of the page image vertically to the edges
of the viewing window.  This method is only valid after a spooled
file has been loaded.
**/
    public void fitHeight()
    {
        if (initialized_ == true) {
            // clear view
            pageView_.clearView();

            // retrieve viewport from scroll pane
            JViewport viewPort = scrollView_.getViewport();

            // retrieve visible viewport size
            Dimension viewPortSize = viewPort.getExtentSize();

            // ensure viewport size is valid
            if (viewPortSize.width  < 0)  viewPortSize.width  = 0;
            if (viewPortSize.height < 0)  viewPortSize.height = 0;

            // adjust width and height of page image to fit height of viewport
            Dimension d = new Dimension();
            d.height  = viewPortSize.height;
            d.width = ((currentPageImage_.getWidth(this) * viewPortSize.height)/
                            currentPageImage_.getHeight(this));
            pageView_.setViewSize(d);

            // calculate the new viewing zoom percentage
            calculateZoom();

            // update the viewer
            updateViewer();
        }
    }



/**
Stretches the size of the page image horizontally or vertically so the
entire view is contained within the edges of the viewing window.  This method
is only valid after a spooled file has been loaded.
**/
    public void fitPage()
    {
        if (initialized_ == true) {
            Dimension d1 = scrollView_.getSize();
            Dimension d2 = scrollView_.getViewport().getExtentSize();

            // ensure viewport size is valid
            if (d2.width < 0)   d2.width  = 0;
            if (d2.height < 0)  d2.height = 0;

            int viewWidth = pageView_.getWidth();
            int viewHeight = pageView_.getHeight();

            if ((viewWidth <= d2.width) && (viewHeight >= d2.height))
                fitHeight();
            else if ((viewWidth >= d2.width) && (viewHeight <= d2.height))
                fitWidth();
            else if (viewWidth < d2.width) {
                if (java.lang.Math.abs(d1.width - viewWidth) <
                    java.lang.Math.abs(d1.height - viewHeight))
                    fitWidth();
                else
                    fitHeight();
            }
            else {
                if (java.lang.Math.abs(d1.width - viewWidth) <
                    java.lang.Math.abs(d1.height - viewHeight))
                    fitHeight();
                else
                    fitWidth();
            }
        }
    }



/**
Stretches the size of the page image horizontally to the edges of
the viewing window.  This method is only valid after a spooled file
has been loaded.
**/
    public void fitWidth()
    {
        if (initialized_ == true) {
            // clear view
            pageView_.clearView();

            // retrieve viewport from scroll pane
            JViewport viewPort = scrollView_.getViewport();

            // retrieve visible viewport size
            Dimension viewPortSize = viewPort.getExtentSize();

            // ensure viewport size is valid
            if (viewPortSize.width  < 0)  viewPortSize.width  = 0;
            if (viewPortSize.height < 0)  viewPortSize.height = 0;

            // adjust width and height of page image to fit width of viewport
            Dimension d = new Dimension();
            d.width  = viewPortSize.width;
            d.height = ((currentPageImage_.getHeight(this) * viewPortSize.width)/
                                currentPageImage_.getWidth(this));

            pageView_.setViewSize(d);

            // calculate the new viewing zoom percentage
            calculateZoom();

            // update the viewer
            updateViewer();
        }
    }



/**
Returns the page number of the current page.

@return The current page number.
**/
    public int getCurrentPage()
    {
        return currentPageNumber_;
    }



/**
Returns the number of pages in the spooled file.
This value may be estimated, depending on the origin of spooled file.
If the spooled file was created natively on an AS/400, the
value is valid. If the spooled file was created on another
type of system, the value may be estimated.

@see #isNumberOfPagesEstimated

@return The number of pages.
**/
    public int getNumberOfPages()
    {
        return numberOfPages_;
    }



/**
Returns the Image for page <i>page</i>

@param  page    The page to return an Image of.

@return The page Image.

@exception IOException Thrown if the specified page cannot be retrieved.
**/
    private synchronized Image getPageImage(int page) throws IOException
    {
        Image image = null;  // GIF image to create
        if (spooledFileIS_ == null) {
            throw new IOException();
        }

        try {
            // select page in page input stream
            boolean pageSelected = spooledFileIS_.selectPage(page); // @A1C
            if (pageSelected == false) {                            // @A1A
                throw new IOException();                            // @A1A
            }                                                       // @A1A

            // retrieve size of page data in bytes
            int bytesAvailable = spooledFileIS_.available();
            if (bytesAvailable == 0) {
                throw new IOException();
            }

            byte[] imageData = new byte[bytesAvailable];

            // read image data
            spooledFileIS_.read(imageData, 0, bytesAvailable);

            // retrieve the frame's toolkit (needed for 'createImage' method)
            Frame frame  = new Frame();
            Toolkit kit = frame.getToolkit();

            // create imageLoader to track image loading
            MediaTracker imageLoader = new MediaTracker(frame);

            // create the Image
            image = (kit.createImage(imageData));

            if (image != null) {
                // ensure the image loaded correctly in memory
                imageLoader.addImage(image, 0);
                imageLoader.waitForID(0);

                // free imageLoader: it has served its purpose
                imageLoader = null;
            }
        }
        catch (Exception e) {
            // error generating page image
            throw new IOException();
        }

        // return the image
        return image;
    }



/**
Returns the Dimension of the page image being viewed.

@return The Dimension of the current page image.
**/
    private Dimension getPageImageSize()
    {
        if (currentPageImage_ != null) {
            return (new Dimension(currentPageImage_.getWidth(this),
                              currentPageImage_.getHeight(this)));
        }
        else
            return new Dimension(0,0);
    }



/**
Returns the paper size.  The paper size is used to determine how to
process pages from the spooled file.

@return The paper size.
**/
    public int getPaperSize()
    {
        return paperSize_;
    }



/**
Returns the preferred size of the viewer.

@return The preferred size.
**/
    public Dimension getPreferredSize()
    {
        if ((pageView_ != null)
            && ((pageView_.getWidth() < 500) && (pageView_.getHeight() < 700))) {
            return new Dimension(pageView_.getWidth() + 25,
                             pageView_.getHeight() + TOOLBARSIZE_ + statusBar_.getPreferredSize().height);
        }
        else
            return new Dimension(PREF_WIDTH,PREF_HEIGHT);
    }



/**
Returns the spooled file being viewed.

@return The spooled file.
**/
    public SpooledFile getSpooledFile()
    {
        return spooledFile_;
    }



/**
Returns the viewing fidelity.  The viewing fidelity is used to determine how to
process pages from the spooled file.

@return The viewing fidelity.
**/
    public int getViewingFidelity()
    {
        return viewingFidelity_;
    }



/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport(this);
        propertyChangeSupport_  = new PropertyChangeSupport(this);
        vetoableChangeSupport_  = new VetoableChangeSupport(this);
        workingEventSupport_    = new WorkingEventSupport(this);

        addWorkingListener(new WorkingCursorAdapter(this));

        addFocusListener(new SerializationListener(this)); //@A3A
    }



/**
Indicates if the number of pages associated with the spooled file being viewed
is estimated.

@see #getNumberOfPages

@return Returns true if the number of pages is estimated; false otherwise.
**/
    public boolean isNumberOfPagesEstimated()
    {
        return numberOfPagesEst_;
    }



/**
Loads the spooled file for viewing.  A call to this method must be made after
the constructor has been invoked or the spooled file has changed
in order to load the spooled file and properly initialize the viewer.

@exception IOException Thrown if the spooled file cannot be initialized.
@exception PropertyVetoException Thrown if the property change is vetoed.
**/
    public void load() throws IOException, PropertyVetoException
    {
        if (spooledFile_ == null) {
            errorEventSupport_.fireError(new IllegalStateException("spooledFile"));
        }
        else {
          // fire started working event.
          workingEventSupport_.fireStartWorking();

          try {
            // set intialized to false, and disable the viewer buttons
            // until we have successfully retrieved the spooled file
            initialized_ = false;
            disableViewerButtons();

            // set up the print parms for creating the spooled file input stream
            // These parms will ensure we get page-at-a-time data, with each
            // page being represented as a 'block' of GIF binary data

            PrintParameterList printParms = new PrintParameterList();
            printParms.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT, WSCSTOBJ);
            printParms.setParameter(PrintObject.ATTR_MFGTYPE, "*WSCST");
            printParms.setParameter(PrintObject.ATTR_VIEWING_FIDELITY, viewingValues[viewingFidelity_]);
            printParms.setParameter(PrintObject.ATTR_PAPER_SOURCE_1, paperSizeValues[paperSize_]);

            // close previous spooled file page input stream
            if (spooledFileIS_ != null) {
                spooledFileIS_.close();
            }

            // retrieve page input stream from spooled file
            spooledFileIS_ = spooledFile_.getPageInputStream(printParms);

            // throw exception for spooledFileIS_ == null
            if (spooledFileIS_ == null) {
                throw new IOException();
            }

            // warn of numberOfPages property change
            int newValue = spooledFileIS_.getNumberOfPages();

            vetoableChangeSupport_.fireVetoableChange("numberOfPages",
                                        new Integer(numberOfPages_),
                                        new Integer(newValue));

            int oldValue = numberOfPages_;
            numberOfPages_ = newValue;

            // fire numberOfPages property change
            propertyChangeSupport_.firePropertyChange("numberOfPages",
                                        new Integer(oldValue),
                                        new Integer(numberOfPages_));


            // determine if number of pages is estimated
            boolean newEstimated = spooledFileIS_.isPagesEstimated();

            // warn of pagesEstimated property change
            vetoableChangeSupport_.fireVetoableChange("numberOfPagesEstimated",
                                        new Boolean(numberOfPagesEst_),
                                        new Boolean(newEstimated));

            // we know the number of pages is estimated
            boolean oldEstimated = numberOfPagesEst_;
            numberOfPagesEst_ = newEstimated;

            // fire numberOfPagesEstimated property change
            propertyChangeSupport_.firePropertyChange("numberOfPagesEstimated",
                                        new Boolean(oldEstimated),
                                        new Boolean(numberOfPagesEst_));

            // display '*' if the number of pages is estimated
            if (numberOfPagesEst_ == true) {
                estimateStar_ = "*";
            }

            try {
                // retrieve the starting page view
                currentPageImage_ = getPageImage(currentPageNumber_);

                // no exception thrown means the page was found successfully
                // update instance variables accordingly
                oldCurrentPage_ = currentPageNumber_;
                knownPages_ = currentPageNumber_;
            }
            catch (IOException e) {  // error getting page, default to page one...
                if (currentPageNumber_ != 1) {  // if we didn't try to get page one above...

                    // default to setting current page to 1
                    setCurrentPage(1);

                    // retrieve starting page image
                    currentPageImage_ = getPageImage(currentPageNumber_);

                    // no exception thrown means the page was found successfully
                    // update instance variables accordingly
                    oldCurrentPage_ = currentPageNumber_;
                    knownPages_ = currentPageNumber_;
                }
                else {
                    // the page stream doesn't have page 1 available...
                    // trace the error
                    if (Trace.isTraceOn())
                        Trace.log(Trace.ERROR, "Error initializing spooled file viewer");

                    // throw the exception
                    throw(e);

                }
            }

            // if we have gotten here, we successfully retrieved a
            // spooled file page image

            // create the page view for the viewer
            pageView_ = new SpooledFilePageView_();
            scrollView_.setViewportView(pageView_);

            // enable the viewer buttons
            enableViewerButtons();

            // initialization complete
            initialized_ = true;

            // (re)set viewing zoom percentage
            calculateZoom();

            // update the viewer
            updateViewer();

            // repaint all
            validate();   // @A4A - validate forces redraw
            // paintAll(this.getGraphics());  @A4D

          }
          catch (Exception e) {
            initialized_ = false;
            // trace the error
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error initializing spooled file viewer");
            errorEventSupport_.fireError(e);
          }
          finally {
            // fire stopped working event
            workingEventSupport_.fireStopWorking();
          }
        }
    }



/**
Loads the previously viewed page.  If a different page was not previously
viewed (loaded), no action is taken. This method can only be called after
a spooled file has been loaded.  If a spooled file has not been successfully
loaded into the viewer previously, an error event is fired.


@exception PropertyVetoException Thrown if the property change is vetoed.
**/
    public void loadFlashPage()
        throws PropertyVetoException
    {
        if (initialized_ == true) {
            if (flashPageImage_!= null) { // flash page defined
                vetoableChangeSupport_.fireVetoableChange("currentPage",
                                        new Integer(oldCurrentPage_),
                                        new Integer(flashPageNumber_));

                // clear current page view
                pageView_.clearView();

                // perform swap of flash/current page number
                int tempPageNumber      = flashPageNumber_;
                flashPageNumber_    = oldCurrentPage_;
                currentPageNumber_  = tempPageNumber;

                // perform swap of flash/current page image
                Image tempPageImage       = flashPageImage_;
                flashPageImage_     = currentPageImage_;
                currentPageImage_   = tempPageImage;
                oldCurrentPage_     = currentPageNumber_;

                Dimension d1 = getPageImageSize();                          // @A1A
                    Dimension d2 = new Dimension();                             // @A1A
                    d2.width =(int)((float)d1.width * (zoomPercentage_/100));   // @A1A
                    d2.height=(int)((float)d1.height * (zoomPercentage_/100));  // @A1A
                pageView_.setViewSize(d2);                                  // @A1A

                // update the view
                updateViewer();

                propertyChangeSupport_.firePropertyChange("currentPage",
                                    new Integer(flashPageNumber_),
                                    new Integer(currentPageNumber_));
            }
        }
        else {
            errorEventSupport_.fireError(new IllegalStateException());
        }
    }



/**
Loads the current page for viewing. This method can only be called
after a spooled file has been successfully loaded. If the current page
is not a valid page of the spooled file, the current page is set to the
previously viewed page, and an error event is fired.
If a spooled file has not been successfully loaded into the viewer previously,
an error event is fired.

@exception PropertyVetoException Thrown if the property change is vetoed.
**/
    public void loadPage()
        throws PropertyVetoException
    {
        Image newPageImage;

        // fire started working event
        workingEventSupport_.fireStartWorking();

        if (initialized_ == true) { // a spooled file has been loaded
            if (currentPageNumber_ == oldCurrentPage_) {
                // optimization - current page already loaded
            }
            else if (currentPageNumber_ == flashPageNumber_) {
                // optimization - load flash page
                loadFlashPage();
            }
            else { // possible new page to read
              try {
                 // attempt to retrieve current page
                 newPageImage = getPageImage(currentPageNumber_);

                 // no exception thrown means the page was found successfully

                 // update flash page information
                 flashPageNumber_    = oldCurrentPage_;
                 flashPageImage_     = currentPageImage_;

                 // update current page information
                 oldCurrentPage_     = currentPageNumber_;
                 currentPageImage_   = newPageImage;

                 // check number of pages, update known count
                 if ((numberOfPagesEst_ == true) &&
                     (currentPageNumber_ > knownPages_)) {
                     knownPages_ = currentPageNumber_;
                 }
               }
               catch (IOException e) {  // couldn't retrieve currentPageNumber_
                 // check to see if we found the last page if we have
                 // the number of pages estimated!
                 if ((numberOfPagesEst_ == true) &&
                    (currentPageNumber_ == (knownPages_ + 1))) {
                    // we've found the last page!

                    // warn of property change
                    vetoableChangeSupport_.fireVetoableChange("numberOfPages",
                                        new Integer(numberOfPages_),
                                        new Integer(knownPages_));

                    // we now know the number of pages
                    int oldValue = numberOfPages_;
                    numberOfPages_ = knownPages_;

                    // fire numberOfPages property change
                    propertyChangeSupport_.firePropertyChange("numberOfPages",
                                        new Integer(oldValue),
                                        new Integer(numberOfPages_));


                    // warn of property change, only from true to false
                    vetoableChangeSupport_.fireVetoableChange("numberOfPagesEstimated",
                                        new Boolean(true),
                                        new Boolean(false));

                    // we know the number of pages is valid
                    numberOfPagesEst_ = false;
                    estimateStar_ = "";

                    // fire numberOfPagesEstimated property change, only from true to false
                    propertyChangeSupport_.firePropertyChange("numberOfPagesEstimated",
                                        new Boolean(true),
                                        new Boolean(false));

                 }
                 // warn of property change
                 vetoableChangeSupport_.fireVetoableChange("currentPage",
                                        new Integer(currentPageNumber_),
                                        new Integer(oldCurrentPage_));

                 int oldPage         = currentPageNumber_;
                 currentPageNumber_  = oldCurrentPage_;

                 // fire currentPage property change
                 propertyChangeSupport_.firePropertyChange("currentPage",
                                        new Integer(oldPage),
                                        new Integer(currentPageNumber_));
                 // fire error event
                 errorEventSupport_.fireError(new IOException());
               }
            }

            Dimension d1 = getPageImageSize();                          // @A1A
                Dimension d2 = new Dimension();                             // @A1A
                d2.width =(int)((float)d1.width * (zoomPercentage_/100));   // @A1A
                d2.height=(int)((float)d1.height * (zoomPercentage_/100));  // @A1A
            pageView_.setViewSize(d2);                                  // @A1A

            // update the viewer
            updateViewer();
        } // (initialized == false)
        else {
            errorEventSupport_.fireError(new IllegalStateException());
        }
        // fire stopped working event
        workingEventSupport_.fireStopWorking();
    }



/**
Sets the view back one page.

@exception PropertyVetoException Thrown if the property change is vetoed.
**/
    public void pageBack()
        throws PropertyVetoException
    {
        setCurrentPage(currentPageNumber_ - 1);
        loadPage();
    }



/**
Sets the view forward one page.

@exception PropertyVetoException Thrown if the property change is vetoed.
**/
    public void pageForward()
        throws PropertyVetoException
    {
        setCurrentPage(currentPageNumber_ + 1);
        loadPage();
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.

@exception IOException Thrown if an IO error occurs.
@exception ClassNotFoundException Thrown if class is not found.
**/
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient();
        initialized_ = false;
    }



/**
Removes an ErrorListener.

@param  listener    The listener.
**/
    public void removeErrorListener(ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener(listener);
    }



/**
Removes a property change listener.

@param  listener  The listener.
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        super.removePropertyChangeListener(listener);
        propertyChangeSupport_.removePropertyChangeListener(listener);
    }



/**
Removes the tool bar.
**/
    public void removeToolBar()
    {
        remove(toolBar_);
        validate();  // @A4A - validate forces redraw
        // paintAll(this.getGraphics());  @A4D
    }



/**
Removes the status bar.
**/
    public void removeStatusBar()
    {
        remove(statusBar_);
        validate();  // @A4A - validate forces redraw
        // paintAll(this.getGraphics());  @A4D
    }



/**
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        super.removeVetoableChangeListener(listener);
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



/**
Removes a WorkingListener.

@param  listener    The listener.
**/
    public void removeWorkingListener(WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener(listener);
    }



/**
Sets the current page.

@param  newPage   The page to view.

@exception PropertyVetoException Thrown if the property change is vetoed.
**/
    public synchronized void setCurrentPage(int newPage)
        throws PropertyVetoException
    {
        if ((1 <= newPage) && ((newPage <= numberOfPages_) ||
             (numberOfPagesEst_ == true) || (initialized_ == false))) {

             // warn of property change
            vetoableChangeSupport_.fireVetoableChange("currentPage",
                                   new Integer(currentPageNumber_),
                                   new Integer(newPage));

            // update current page
            int oldPage = currentPageNumber_;
            currentPageNumber_ = newPage;

            // fire currentPage property change
            propertyChangeSupport_.firePropertyChange("currentPage",
                                   new Integer(oldPage),
                                   new Integer(currentPageNumber_));
        }
    }



/**
Sets the papersize.  If paper size specified by <i>paperSize</i> is not valid,
no action is taken.

@param size The paper size to be used for processing spooled file pages.
      <p>
            May be any of the following values:
      <ul>
         <li> LETTER    - Letter (8.5 x 11 inches)
         <li> LEGAL     - Legal (8.5 x 14 inches)
         <li> A3        - A3 (297 x 420 mm)
         <li> A4        - A4 (210 x 297 mm)
         <li> A5        - A5 (148 x 210 mm)
         <li> B4        - B4 (257 x 364 mm)
         <li> B5        - B5 (182 x 257 mm)
         <li> EXECUTIVE - Executive (7.25 x 10.5 inches)
         <li> LEDGER    - Ledger (17 x 11 inches)
         <li> CONT80    - Continuous feed 80 (8 x 11 inches)
         <li> CONT132   - Continuous feed 132  (13.2 x 11 inches)
         <li> NONE      - None
      </ul>

@exception PropertyVetoException Thrown if the property change is vetoed.
**/
    public synchronized void setPaperSize(int paperSize)
        throws PropertyVetoException
    {
        if ((LETTER <= paperSize) && (paperSize <= NONE)) {

            // warn of property change
            vetoableChangeSupport_.fireVetoableChange("paperSize",
                                new Integer(paperSize_),
                                new Integer(paperSize));

            // update paper size
            int oldValue = paperSize_;
            paperSize_   = paperSize;

            // set static initializer
            sPaperSize_  = paperSize_;
            paperSizeChecked_ = true;

            // fire property change to all listeners
            propertyChangeSupport_.firePropertyChange("paperSize",
                                new Integer(oldValue),
                                new Integer(paperSize_));
        }
        else
            errorEventSupport_.fireError(new IllegalArgumentException("paperSize"));
    }



/**
Sets the spooled file.

@param spooledFile  The spooled file to view.

@exception PropertyVetoException Thrown if the property change is vetoed.
**/
    public void setSpooledFile(SpooledFile spooledFile)
        throws PropertyVetoException
    {
        if (spooledFile == null)
            throw new NullPointerException("spooledFile");

        // warn of property change
        vetoableChangeSupport_.fireVetoableChange("spooledFile",
                                                  spooledFile_,
                                                  spooledFile);

        // update spooled file
        SpooledFile oldValue = spooledFile_;
        spooledFile_ = spooledFile;

        // fire spooledFile property change
        propertyChangeSupport_.firePropertyChange("spooledFile",
                                                  oldValue,
                                                  spooledFile_);
    }



/**
Sets the viewing fidelity.

@param viewingFidelity The viewing fidelity.  If viewing fidelity specified
by <i>viewingFidelity</i> is not valid, no action is taken.
      <p>
                       May be any of the following values:
      <ul>
         <li> ABSOLUTE  - Absolute.  When this is specified, the spooled file data
                          is scanned and all non-raster data is processed up through
                          the current page.
         <li> CONTENT   - Content.  When this is specified, the spooled file data is
                          processed according to datastream type:
                          <ul>
                          <li> SCS - Process open-time commands and current page commands
                                only.
                          <li> AFPDS - Process the first page without rasterizing, then the
                                current page.
                          </ul>
      </ul>

@exception PropertyVetoException Thrown if the property change is vetoed.
**/
    public synchronized void setViewingFidelity(int viewingFidelity)
        throws PropertyVetoException
    {
        if ((ABSOLUTE_FIDELITY <= viewingFidelity) &&
            (viewingFidelity <= CONTENT_FIDELITY)) {

            // warn of property change
            vetoableChangeSupport_.fireVetoableChange("viewingFidelity",
                                                      new Integer(viewingFidelity_),
                                                      new Integer(viewingFidelity));
            // update viewing fidelity
            int oldValue = viewingFidelity_;
            viewingFidelity_ = viewingFidelity;

            // set static initializer
            sViewingFidelity_  = viewingFidelity_;

            // fire property change to all listeners
            propertyChangeSupport_.firePropertyChange("viewingFidelity",
                                new Integer(oldValue),
                                new Integer(viewingFidelity_));
        }
        else
            errorEventSupport_.fireError(new IllegalArgumentException("viewingFidelity"));
    }



/**
Performs the action of updating viewer information; specifically, updating the
current page number, calculating the zoom ration, and (enabling/disabling)
the viewer buttons items,
**/
    private void updateViewer() {
        if ((pageView_ != null)  && (initialized_ == true)) {
            // update pageview
            pageView_.repaint();

            // currentPageNumber_ = getCurrentPage();
            zoomInfo_.setText((int)zoomPercentage_ + "%");  // @A1C added (int)

            String pageText = ResourceLoader.substitute(ResourceLoader.getPrintText("PAGE_OF"),
                             new String[]{Integer.toString(currentPageNumber_),
                                          Integer.toString(numberOfPages_)});
            pageInfo_.setText(pageText + estimateStar_);

            // enabled/disable 'first' and 'previous' page buttons
                if (currentPageNumber_ > 1) {
                    firstPageButton_.setEnabled(true);
                    prevPageButton_.setEnabled(true);
            }
            else {
                firstPageButton_.setEnabled(false);
                    prevPageButton_.setEnabled(false);
            }

            // enable/disable 'next' and 'last' page buttons
            // NOTE: All spooled files have a 'number of pages' attribute.
            // For some spooled files, this value is an 'estimated' quantity.
            // Another attribute associated with the spooled file allows us
            // to know whether this total 'number of pages' is real or estimated.
            // We have stored the value of this attribute in numberOfPagesEst_.
            if (numberOfPagesEst_ == false) {
                if (currentPageNumber_ < numberOfPages_) {
                    lastPageButton_.setEnabled(true);
                        nextPageButton_.setEnabled(true);
                }
                else {
                    lastPageButton_.setEnabled(false);
                        nextPageButton_.setEnabled(false);
                    }
                }
                else {
                    lastPageButton_.setEnabled(false);
                nextPageButton_.setEnabled(true);
            }

        }
    }



/*************************************************************************/



    private class ViewerActionListener_ implements ActionListener {

    /**
    Performs the action.

    @param  e   The ActionEvent
    **/
        public void actionPerformed(ActionEvent e)
        {
            String command = e.getActionCommand();
            try {
                // perform requested action
                if (command.equals("actualSize")) {
                    actualSize();
                }
                else if (command.equals("fitWidth")) {
                    fitWidth();
                }
                else if (command.equals("fitPage")) {
                    fitPage();
                }
                else if (command.equals("zoom")) {
                    changeZoom();
                }
                else if (command.equals("goToPage")) {
                    changeCurrentPage();
                }
                else if (command.equals("firstPage")) {
                        setCurrentPage(1);
                        loadPage();
                    }
                    else if (command.equals("prevPage")) {
                    pageBack();
                }
                    else if (command.equals("nextPage")) {
                    pageForward();
                }
                    else if (command.equals("lastPage")) {
                    setCurrentPage(numberOfPages_);
                    loadPage();
                    }
                else if (command.equals("flashPage")) {
                    loadFlashPage();
                }
                else if (command.equals("changeViewingFidelity")) {
                    changeViewingFidelity();
                }
                else if (command.equals("changePaperSize")) {
                    changePaperSize();
                }
            }
            catch (Exception err) {
                // do nothing
            }
        }
    }



/*************************************************************************/


/**
The SpooledFilePageView_ class represents the view of one page of
a spooled file.
**/

    private class SpooledFilePageView_ extends JLabel
    {
        Dimension viewSize_;


        public SpooledFilePageView_()
        {
            viewSize_ = new Dimension(currentPageImage_.getWidth(this),
                                      currentPageImage_.getHeight(this));
        }



    /**
    Clears the current view.
    **/
        synchronized public void clearView()
        {
            Rectangle r = getVisibleRect();
            getGraphics().clearRect(r.x, r.y, r.width, r.height);
        }



    /**
    Returns the view height.

    @return The height.
    **/
        public int getHeight()
        {
            // in IE, Super class JLabel needs valid values even before
            // this object is constructed.  To comply, a value
            // of 10 is returned temporarily...
            if (viewSize_ == null)    // @A5A
               return 10;             // @A5A
            else                      // @A5A
               return viewSize_.height;
        }



    /**
    Returns the view width.

    @return The width.
    **/
        public int getWidth()
        {
            // in IE, Super class JLabel needs valid values even before
            // this object is constructed.  To comply, a value
            // of 10 is returned temporarily...
            if (viewSize_ == null)    // @A5A
               return 10;             // @A5A
            else                      // @A5A
               return viewSize_.width;
        }



    /**
    Paints the view.

    @param g The specified Graphics environment.
    **/
        public void paint(Graphics g)
        {
            setSize(viewSize_);
            g.drawImage(currentPageImage_,0,0,viewSize_.width, viewSize_.height, this);
        }



    /**
    Sets the view size.

    @param d The new dimension for the view size.
    **/
        public void setViewSize(Dimension d)
        {
            viewSize_.width = d.width;
            viewSize_.height = d.height;
        }



    /**
    Updates the view.

    @param g The specified Graphics environment.
    **/
        public void update(Graphics g)
        {
            clearView();
            paint(g);
        }

    }



/*************************************************************************/



/**
The PaperSizeBox_ class represents a dialog for selecting a paper size
to be used when rendering the pages of spooled file for viewing.
**/
    private class PaperSizeBox_
    extends JDialog
    implements ActionListener
    {

        JButton okButton, cancelButton;
        final JList dataList;

    /**
    Constructs a PaperSizeBox_ Dialog
    **/
        public PaperSizeBox_(JFrame parent)
        {
            super(parent, paperSizeText_, true);
                setResizable(false);
                getContentPane().setLayout(new BorderLayout());

            // set the size of the list box
                setSize(LISTBOXWIDTH_, LISTBOXHEIGHT_);

            // create text label indicating current paper size
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            textPanel.add(new JLabel(curPaperText_ + " : " + paperSizes[paperSize_]));
            getContentPane().add("North", textPanel);

            // create the data list of available paper sizes
            dataList = new JList(paperSizes);
            JScrollPane scrollPane = new JScrollPane(dataList);
            getContentPane().add("Center", scrollPane);

            // set a default index
            dataList.setSelectedIndex(paperSize_);

            // create the mouse event listener
            MouseListener mouseListener = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 2) {
                                int index = dataList.locationToIndex(e.getPoint());
                               // dispose();
                                setVisible(false);
                                displayPropertyChangeWarning("paperSize", index);
                            }
                    }
            };
            dataList.addMouseListener(mouseListener);

                // create "OK" button
                okButton = new JButton(okText_);
                okButton.addActionListener(this);
                okButton.setActionCommand("ok");

            // create "Cancel" button
                cancelButton = new JButton(cancelText_);
                cancelButton.addActionListener(this);
                cancelButton.setActionCommand("cancel");

            // create button panel and add buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            getContentPane().add("South", buttonPanel);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    setVisible(false);
                }
            });
        }



    /**
    Perform the requested action.

    @param e    The ActionEvent
    **/
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
          //  dispose();
                setVisible(false);
            if (command.equals("ok")) {
                int index = dataList.getSelectedIndex();
                displayPropertyChangeWarning("paperSize", index);
                    updateViewer();
                    }

        }



    /**
    Show the dialog in the middle of the parent frame.
    **/
        public void setVisible(boolean b)
        {
            if (b == true) {
                    Rectangle bounds = getParent().getBounds();
                    Rectangle bounds2 = getBounds();
                    setLocation(bounds.x + (bounds.width - bounds2.width)/ 2,
                        bounds.y + (bounds.height - bounds2.height)/2);
                    super.setVisible(true);
            }
            else
                    super.setVisible(false);
        }

    }



/*************************************************************************/



/**
The ViewingFidelityBox_ class represents a dialog for selecting the
viewing fidelity to be used when rendering the pages of spooled file
for viewing.
**/
    private class ViewingFidelityBox_
    extends JDialog
    implements ActionListener
    {

        JButton okButton, cancelButton;
        final JList dataList;

    /**
    Constructs a ViewingFidelityBox_ Dialog
    **/
        public ViewingFidelityBox_(JFrame parent)
        {
            super(parent, vFidelityText_, true);
                setResizable(false);
                getContentPane().setLayout(new BorderLayout());

            // set the size of the list box
                setSize(LISTBOXWIDTH_, LISTBOXHEIGHT_);

            // create text label indicating current paper size
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            textPanel.add(new JLabel(curVFidelityText_ + " : " +
                          viewingFidelities[viewingFidelity_]));
            getContentPane().add("North", textPanel);

            // create the data list of available viewing fidelities
            dataList = new JList(viewingFidelities);

            // set a default index
            dataList.setSelectedIndex(viewingFidelity_);

            JScrollPane scrollPane = new JScrollPane(dataList);
            getContentPane().add("Center", scrollPane);

            MouseListener mouseListener = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 2) {
                                int index = dataList.locationToIndex(e.getPoint());
                                // dispose();
                                setVisible(false);
                                displayPropertyChangeWarning("viewingFidelity", index);
                            }
                    }
            };
            dataList.addMouseListener(mouseListener);

            // create "OK" button
                okButton = new JButton(okText_);
                okButton.addActionListener(this);
                okButton.setActionCommand("ok");

            // create "Cancel" button
                cancelButton = new JButton(cancelText_);
                cancelButton.addActionListener(this);
                cancelButton.setActionCommand("cancel");

            // create button panel and add buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            getContentPane().add("South", buttonPanel);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    setVisible(false);
                }
            });
        }



    /**
    Perform the requested action.

    @param e    The ActionEvent
    **/
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            setVisible(false);
            if (command.equals("ok")) {
                int index = dataList.getSelectedIndex();
               // dispose();
                displayPropertyChangeWarning("viewingFidelity", index);
                    updateViewer();
                    }
        }



    /**
    Show the dialog in the middle of the parent frame.
    **/
        public void setVisible(boolean b)
        {
            if (b == true) {
                    Rectangle bounds = getParent().getBounds();
                    Rectangle bounds2 = getBounds();
                    setLocation(bounds.x + (bounds.width - bounds2.width)/ 2,
                        bounds.y + (bounds.height - bounds2.height)/2);
                    super.setVisible(true);
            }
            else
                    super.setVisible(false);
        }

    }



/*************************************************************************/



/**
The GoToBox_ class represents a dialog for selecting a page
to view.
**/
    private class GoToBox_
    extends JDialog
    implements ActionListener {

        JButton okButton, cancelButton;
        TextField pageNumber;

    /**
    Constructs a GoToBox_ Dialog
    **/
        public GoToBox_(JFrame parent)
        {
            super(parent, goToPageText_, true);
                setResizable(false);
                getContentPane().setLayout(new BorderLayout());

            // set the size of the dialog
                setSize(250, 100);

            // create text elements
            JLabel desc = new JLabel(goToPageText_);

            pageNumber = new TextField("1", 6);  // default 'go to' page
            pageNumber.setBackground(new Color(255,255,255)); // set background white
            pageNumber.setEditable(true);
            pageNumber.setVisible(true);
            pageNumber.addActionListener(this);

            // create text panel and add above elements
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            textPanel.add(desc);
            textPanel.add(pageNumber);

            // add text panel to dialog box
            getContentPane().add("Center",textPanel);

                // create "OK" button
                okButton = new JButton(okText_);
                okButton.addActionListener(this);
                okButton.setActionCommand("ok");

            // create "Cancel" button
                cancelButton = new JButton(cancelText_);
                cancelButton.addActionListener(this);
                cancelButton.setActionCommand("cancel");

                // create button panel and add buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            getContentPane().add("South", buttonPanel);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    setVisible(false);
                }
            });

        }



    /**
    Perform the requested action.

    @param e    The ActionEvent
    **/
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            setVisible(false);
            if (!(command.equals("cancel"))) {
                try {
                    setCurrentPage(Integer.valueOf(pageNumber.getText()).intValue());
                    loadPage();
                        }
                        catch (Exception error) {}
                    }

        }



    /**
    Show the dialog in the middle of the parent frame.
    **/
        public void setVisible(boolean b)
        {
            if (b == true) {
                    Rectangle bounds = getParent().getBounds();
                    Rectangle bounds2 = getBounds();
                    setLocation(bounds.x + (bounds.width - bounds2.width)/ 2,
                         bounds.y + (bounds.height - bounds2.height)/2);
                    super.setVisible(true);
            }
            else
                    super.setVisible(false);
        }

    }


/*************************************************************************/

// @A5A - Added inner class
/**
The WarningDialogBox_ class represents a dialog for displaying a warning.
**/
    private class WarningDialogBox_
    extends JDialog
    implements ActionListener {

        JButton okButton, cancelButton;
        String property;
        int value;

    /**
    Constructs a WarningDialogBox_ Dialog
    **/
        public WarningDialogBox_(JFrame parent,
                                 String warning,
                                 String prop,
                                 int val)
        {
            super(parent, warningText_, true);
            property = prop;
            value = val;

            getContentPane().setLayout(new BorderLayout());

            // set the size of the dialog
            setSize(250, 150);

            // create text element
            JTextArea desc = new JTextArea(warning);
            desc.setEditable(false);

            // create text panel and add text element
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            textPanel.add(desc);

            // add text panel to dialog box
            getContentPane().add("Center",textPanel);

            // create "OK" button
            okButton = new JButton(okText_);
            okButton.addActionListener(this);
            okButton.setActionCommand("ok");

            // create "Cancel" button
            cancelButton = new JButton(cancelText_);
            cancelButton.addActionListener(this);
            cancelButton.setActionCommand("cancel");

            // create button panel and add buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            getContentPane().add("South", buttonPanel);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    setVisible(false);
                }
            });

        }



    /**
    Perform the requested action.

    @param e    The ActionEvent
    **/
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            setVisible(false);
            if (!(command.equals("cancel"))) {
                if (property.equals("viewingFidelity")) {
                    try {
                        setViewingFidelity(value);
                        load();
                    }
                    catch (Exception e1) {}  // absorb all exceptions - if an error
                                             // occurred - an error event was fired.
                }
                else if (property.equals("paperSize")) {
                    try {
                        setPaperSize(value);
                        load();
                    }
                    catch (Exception e1) {}  // absorb all exceptions - if an error
                                             // occurred - an error event was fired.
                }
            }
        }



    /**
    Show the dialog in the middle of the parent frame.
    **/
        public void setVisible(boolean b)
        {
            if (b == true) {
                    Rectangle bounds = getParent().getBounds();
                    Rectangle bounds2 = getBounds();
                    setLocation(bounds.x + (bounds.width - bounds2.width)/ 2,
                         bounds.y + (bounds.height - bounds2.height)/2);
                    super.setVisible(true);
            }
            else
                    super.setVisible(false);
        }

    }



/*************************************************************************/



/**
The ZoomToBox_ class represents a dialog for selecting a magnification
(zoom) ratio to for displaying the images of the spooled file pages.
**/
    private class ZoomToBox_
    extends JDialog
    implements ActionListener
    {
        private JButton okButton, cancelButton;
        private Checkbox[] radioButtons;
        private float zoomAmount;
        private TextField zoomPerc;

    /**
    Constructs a ZoomToBox_ Dialog
    **/
        public ZoomToBox_(JFrame parent)
        {
            super(parent, zoomText_, true);
            Container zoomDialog = getContentPane();
            setResizable(false);
            zoomDialog.setLayout(new BorderLayout());

            // set the size of the dialog
                setSize(250, 175);

            // create zoom percentages group
                CheckboxGroup zoomPGroup= new CheckboxGroup();

            // set all buttons off
            boolean[] buttonOn = new boolean[5];
            for (int i = 0; i < 5; i++) {
                buttonOn[i] = false;
            }

            switch ((int)zoomPercentage_) {     // @A1C added (int)
                case  50: buttonOn[0] = true;
                          break;
                case  75: buttonOn[1] = true;
                          break;
                case 125: buttonOn[2] = true;
                          break;
                case 150: buttonOn[3] = true;
                          break;
                 default: buttonOn[4] = true;
                          break;
            }

            // The checkbox group is a collection of zoom percentage checkboxes
            // with the special property that no more than one checkbox
            // in the same group can be selected at a time.
            radioButtons    = new Checkbox[5];
            radioButtons[0] = new Checkbox(" 50%", zoomPGroup, buttonOn[0]);
            radioButtons[1] = new Checkbox(" 75%", zoomPGroup, buttonOn[1]);
            radioButtons[2] = new Checkbox("125%", zoomPGroup, buttonOn[2]);
            radioButtons[3] = new Checkbox("150%", zoomPGroup, buttonOn[3]);
            radioButtons[4] = new Checkbox("", zoomPGroup, buttonOn[4]);

            // create blank panel to 'adjust' radio buttons in from right
            JPanel rightBorder = new JPanel();
            zoomDialog.add("West", rightBorder);

            // create the radio button panel and add the buttons
            JPanel radioPanel = new JPanel();
            radioPanel.setLayout(new GridLayout(4,1));
            radioPanel.add(radioButtons[0]);
            radioPanel.add(radioButtons[1]);
            radioPanel.add(radioButtons[2]);
            radioPanel.add(radioButtons[3]);
            zoomDialog.add("Center",radioPanel);

            // create zoom percentage panel
            JPanel zoomPercPanel = new JPanel();
            zoomPercPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

            // create zoom percentage input field
            if (buttonOn[4])
                zoomPerc = new TextField((Integer.toString((int)zoomPercentage_)),4); //@A1C
            else
                zoomPerc = new TextField("100",4);
                zoomPerc.setEditable(true);
            zoomPerc.setBackground(new Color(255,255,255));  // white
            zoomPerc.addActionListener(this);

            // fill zoom percentage panel
            JLabel percentage = new JLabel("%");
            zoomPercPanel.add(radioButtons[4]);
            zoomPercPanel.add(zoomPerc);
            zoomPercPanel.add(percentage);

                // create "OK" button
                okButton = new JButton(okText_);
                okButton.addActionListener(this);
                okButton.setActionCommand("ok");

            // create "Cancel" button
                cancelButton = new JButton(cancelText_);
                cancelButton.addActionListener(this);
                cancelButton.setActionCommand("cancel");

                // create button panel and add buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            // add both panels to the zoomRightPanel
            JPanel zoomRightPanel = new JPanel();
            zoomRightPanel.setLayout(new BorderLayout());
            zoomRightPanel.add("Center", zoomPercPanel);
            zoomRightPanel.add("South", buttonPanel);

            zoomDialog.add("East",zoomRightPanel);

            // set up window listener for window closing event
                addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    setVisible(false);
                }
            });

            }



    /**
    Performs the requested action

    @param  e   The ActionEvent
    **/
        public void actionPerformed(ActionEvent e)
        {
            String command = e.getActionCommand();
            setVisible(false);
            if (!(command.equals("cancel"))) {
                if (radioButtons[0].getState()) {
                    zoomAmount = (float).50;   //  50%
                }
                else if (radioButtons[1].getState()) {
                    zoomAmount = (float).75;   //  75%
                }
                else if (radioButtons[2].getState()) {
                    zoomAmount = (float)1.25;  // 125%
                }
                else if (radioButtons[3].getState()) {
                    zoomAmount = (float)1.50;  // 150%
                }
                else if (radioButtons[4].getState()) {
                    float userValue = Float.valueOf(zoomPerc.getText()).floatValue();
                        if (userValue >= 1.0) {
                            zoomAmount = (float)(userValue/100.0);
                        }
                    else {
                            errorEventSupport_.fireError(new IllegalArgumentException("userValue"));
                            zoomAmount = 0;
                        }
                    }

                // below added to simplify viewer maximum zoom exceeded
                if (zoomAmount > 8) zoomAmount = (float)8.0;

                    if (zoomAmount > 0) {
                        Dimension d1 = getPageImageSize();
                        Dimension d2 = new Dimension();
                        d2.width     = (int)((float)d1.width * zoomAmount);
                        d2.height    = (int)((float)d1.height * zoomAmount);
                    pageView_.setViewSize(d2);

                    // calculate the new viewing zoom percentage
                    zoomPercentage_ = zoomAmount * 100;  // @A1A
                    // calculateZoom(); @A1D

                    // update the viewer
                        updateViewer();
                    }
                }
        }



    /**
    Show the dialog in the middle of the parent frame.
    **/
        public void setVisible(boolean b)
        {
            if (b == true) {
                Rectangle bounds = getParent().getBounds();
                Rectangle bounds2 = getBounds();
                setLocation(bounds.x + (bounds.width - bounds2.width)/ 2,
                        bounds.y + (bounds.height - bounds2.height)/2);
                    super.setVisible(true);
                }
                else
                    super.setVisible(false);
        }

    }

}

