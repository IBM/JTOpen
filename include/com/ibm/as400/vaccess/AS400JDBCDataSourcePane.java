///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCDataSourcePane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.JComponent;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable; 
import javax.swing.text.JTextComponent;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLWarning;
import java.sql.Types;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.as400.access.AS400JDBCDataSource;
import com.ibm.as400.ui.framework.java.*; // Panel Manager
import com.ibm.as400.ui.framework.ResourceLoader; 
import javax.swing.*;
import java.awt.*;
import com.ibm.as400.vaccess.*;

/**
 * The AS400JDBCDataSourcePane class represents a set of tabs that contain
 * property values for a JDBC data source.  Changes made to the pane are
 * applied to the data source when <i> applyChanges() </i> is called.
 * 
 * <P>For example:
 * <pre>
 * import com.ibm.as400.access.*;                       
 * import com.ibm.as400.vaccess.*;                       
 * import javax.swing.*;
 * import java.awt.*;
 * import java.awt.event.*;
 * 
 * public class DataSourceGUIExample
 * {                                              
 *    static AS400JDBCDataSourcePane dataSourcePane = null;
 *    static AS400JDBCDataSource     myDataSource   = null;
 *    
 *    public static void main(String[] args)
 *    {
 *       // Like other Java classes the Toolbox classes throw 
 *       // exceptions when something goes wrong.  These must be 
 *       // caught by programs that use the Toolbox.
 *       try                                           
 *       {
 *       
 *          // Create a data source.
 *          myDataSource = new AS400JDBCDataSource();
 * 
 *          // Create a window to hold the pane and an OK button.
 *          JFrame frame = new JFrame ("JDBC Data Source Properties");
 * 
 *          // Create a data source pane.
 *          dataSourcePane = new AS400JDBCDataSourcePane(myDataSource);
 * 
 *          // Create an OK button
 *          JButton okButton = new JButton("OK");
 * 
 *          // Add an ActionListener to the OK button.  When OK is 
 *          // pressed, applyChanges() will be called to commit any
 *          // changes to the data source.
 *          okButton.addActionListener(new ActionListener()
 *             {    
 *                public void actionPerformed(ActionEvent ev)
 *                {
 *                   // Copy all changes made on the data source pane
 *                   // to the data source.  
 *                   if (dataSourcePane.applyChanges())
 *                   {
 *                      System.out.println("ok pressed");
 *                      myDataSource = dataSourcePane.getDataSource();
 *                      System.out.println(myDataSource.getServerName());
 *                   }   
 *                }
 *             }
 *          );
 *        
 *          // Setup the frame to show the pane and OK button.
 *          frame.getContentPane ().setLayout (new BorderLayout ());
 *          frame.getContentPane ().add ("Center", dataSourcePane);
 *          frame.getContentPane ().add ("South", okButton);
 *        
 *          // Pack the frame.
 *          frame.pack ();
 *        
 *          //Display the pane and OK button.
 *          frame.show ();
 *       }
 *       catch (Exception e)
 *       {
 *          e.printStackTrace();
 *       }   
 *    }   
 * }       
 * 
 * </pre>
**/

public class AS400JDBCDataSourcePane
extends JComponent
implements Serializable
{
  
private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

// The data source being displayed
private AS400JDBCDataSource m_currentDataSource; 
// The databean being used by the GUI
private AS400JDBCDataSourcePaneDataBean m_dataBean;  
// Our instance of tabbed pane manager 
private TabbedPaneManager m_tabbedPaneManager = null;
// Indicates that we are just creating the tabs
private boolean m_justBeingCreated = true;
// Frequently through out the code we need the pane
// manager for a particular tab.  They are declared 
// here and set as soon as we have an instance of a
// tabbed pane manager.
private PanelManager m_generalPaneManager = null;
private PanelManager m_serverPaneManager = null;
private PanelManager m_packagePaneManager = null;
private PanelManager m_performancePaneManager = null;
private PanelManager m_languagePaneManager = null;
private PanelManager m_otherPaneManager = null;
private PanelManager m_translationPaneManager = null;
private PanelManager m_formatPaneManager = null;
private PanelManager m_connoptPaneManager = null;

/** The index of the General tab. */
public static final int TAB_GENERAL = 0;
/** The index of the Server tab. */
public static final int TAB_SERVER = 1;
/** The index of the Package tab. */
public static final int TAB_PACKAGE = 2;
/** The index of the Performance tab. */
public static final int TAB_PERFORMANCE = 3;
/** The index of the Language tab. */
public static final int TAB_LANGUAGE = 4;
/** The index of the Other tab. */
public static final int TAB_OTHER = 5;
/** The index of the Translation tab. */
public static final int TAB_TRANSLATION = 6;
/** The index of the Format tab. */
public static final int TAB_FORMAT = 7;
/** The index of the Connection Options tab. */
public static final int TAB_CONNECTIONOPTIONS = 8;

private transient PropertyChangeSupport propertyChangeSupport_ = new PropertyChangeSupport(this);

static ResourceLoader resource_Loader = new ResourceLoader();
static
{
    // Load the resource bundle for this package
    resource_Loader.setResourceName("com.ibm.as400.vaccess.AS400JDBCDataSourcePaneGUI");
}


/**
Constructs an AS400JDBCDataSourcePane object.  A default data source will be displayed.
**/
public AS400JDBCDataSourcePane()
{
    super();
    
    // Since no data source was passed in we need to create one
    AS400JDBCDataSource defaultDataSource = new AS400JDBCDataSource();
    
    createPaneAndSetDataSource(defaultDataSource);
}


/**
Constructs an AS400JDBCDataSourcePane object.

@param       dataSource   The JDBC data source to display.
**/
public AS400JDBCDataSourcePane (AS400JDBCDataSource dataSource)
{
    super();
    
    createPaneAndSetDataSource(dataSource);
}

private void createPaneAndSetDataSource(AS400JDBCDataSource dataSource)
{
    // Instantiate the object which supplies data to the panel.  Note that
    // this must be done before the call to setDataSource.
    m_dataBean = new AS400JDBCDataSourcePaneDataBean();
    
    // Load default values and combo box choices in the GUI databean
    m_dataBean.load();
                
    // Set up to pass the objects to the UI framework
    DataBean[] dataBeans = {m_dataBean};
    JFrame frame = new JFrame();
    Container container = frame.getContentPane();
    
    try 
    {    
      m_tabbedPaneManager = new TabbedPaneManager("com.ibm.as400.vaccess.AS400JDBCDataSourcePaneGUI",
                                                 "AJDSP_TABBEDPANE",
                                                 dataBeans,
                                                 container);
    }
    catch (DisplayManagerException e) {
        e.displayUserMessage(null);
        return;
    }
    
    // Set the "PaneManager" variables.  These are used through out this program.
    m_generalPaneManager = (PanelManager)m_tabbedPaneManager.getDelegateManager("AJDSP_GENERAL");
    m_serverPaneManager = (PanelManager)m_tabbedPaneManager.getDelegateManager("AJDSP_SERVER");
    m_packagePaneManager = (PanelManager)m_tabbedPaneManager.getDelegateManager("AJDSP_PACKAGE");
    m_performancePaneManager = (PanelManager)m_tabbedPaneManager.getDelegateManager("AJDSP_PERFORMANCE");
    m_languagePaneManager = (PanelManager)m_tabbedPaneManager.getDelegateManager("AJDSP_LANGUAGE");
    m_otherPaneManager = (PanelManager)m_tabbedPaneManager.getDelegateManager("AJDSP_OTHER");
    m_translationPaneManager = (PanelManager)m_tabbedPaneManager.getDelegateManager("AJDSP_TRANSLATION");
    m_formatPaneManager = (PanelManager)m_tabbedPaneManager.getDelegateManager("AJDSP_FORMAT");
    m_connoptPaneManager = (PanelManager)m_tabbedPaneManager.getDelegateManager("AJDSP_CONNOPT");
    
    
    // Most GUI behavior is handled by the GUI builder.  Situations that the 
    // GUI builder can't handled are controlled by the following method.
        
    enableListenersForComponentsWeCareAbout();
    
    // set the current values from the data source into the pane.    
    setDataSource(dataSource);
    
    // Build the GUI
    setLayout(new BorderLayout());
    add("Center",container);   
    
    m_justBeingCreated = false;
}

private void enableListenersForComponentsWeCareAbout()
{
    // The GUI builder/PDML handles most field validation and enabling/disabling for us.  For example, the GUI
    // builder knows to enable/disable the fields on the Package tab when the user checks or unchecks the 
    // extended dynamic check box.  The GUI builder also knows to require a package and package library when
    // the components are enabled.
    // This routine is used to set up action listeners for situations that the GUI builder can't automatically      
    // handle.  Currently, the only one is enabling/disabling components on the Language tab when the user
    // switches between the various sort types.
        
    JComboBox sortTypeCB = (JComboBox)((m_languagePaneManager).getComponent("AJDSP_SORTTYPE_COMBOBOX"));    
    sortTypeCB.addActionListener(new ActionListener()
    {
        public void actionPerformed(ActionEvent ev)
        {
            enableComponentsBasedOnSortType(ev);
        }
    });
}

private void enableComponentsBasedOnSortType(ActionEvent ev)
{
    // This method gets control whenever the sort type is changed.  This method looks at the new sort type
    // and enables/disables components on the Language tab.
    
    // Get the sort type combo box
    JComboBox sortTypeCB = (JComboBox)((m_languagePaneManager).getComponent("AJDSP_SORTTYPE_COMBOBOX"));
    // Get the current choice.
    ChoiceDescriptor sortTypeNewValue = (ChoiceDescriptor)sortTypeCB.getSelectedItem();
    
    // When we invoke languagePaneManager.loadData() in the setDataSource method the framework actually
    // clears the combo box and starts over... then this happens we end up here with an empty list
    // which means there is no selected text.  The following "if" keeps us from choking on a
    // NullPointerException.
    if (sortTypeNewValue == null)
      return;
    
    // Get the title associated with the choice
    String sortTypeNewValueText = sortTypeNewValue.getTitle();
    // Based on the choice title, enable/disable the appropriate controls
    if (sortTypeNewValueText.equals(resource_Loader.getString("AJDSP_SORTHEX")) ||
        sortTypeNewValueText.equals(resource_Loader.getString("AJDSP_SORTJOB")))
    {
        (m_languagePaneManager).getComponent("AJDSP_SORTTABLE_LABEL").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_SORTTABLE_TEXTBOX").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_SORTWEIGHT_LABEL").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_SHAREDWEIGHT_RADIOBUTTON").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_UNIQUEWEIGHT_RADIOBUTTON").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_LANGUAGE_LABEL").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_LANGUAGE_COMBOBOX").setEnabled(false);
    }
    else if (sortTypeNewValueText.equals(resource_Loader.getString("AJDSP_SORTLANGID")))
    {
        (m_languagePaneManager).getComponent("AJDSP_SORTTABLE_LABEL").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_SORTTABLE_TEXTBOX").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_SORTWEIGHT_LABEL").setEnabled(true);
        (m_languagePaneManager).getComponent("AJDSP_SHAREDWEIGHT_RADIOBUTTON").setEnabled(true);
        (m_languagePaneManager).getComponent("AJDSP_UNIQUEWEIGHT_RADIOBUTTON").setEnabled(true);
        (m_languagePaneManager).getComponent("AJDSP_LANGUAGE_LABEL").setEnabled(true);
        (m_languagePaneManager).getComponent("AJDSP_LANGUAGE_COMBOBOX").setEnabled(true);
    }
    else if (sortTypeNewValueText.equals(resource_Loader.getString("AJDSP_SORTTABLE")))
    {
        (m_languagePaneManager).getComponent("AJDSP_SORTTABLE_LABEL").setEnabled(true);
        (m_languagePaneManager).getComponent("AJDSP_SORTTABLE_TEXTBOX").setEnabled(true);
        (m_languagePaneManager).getComponent("AJDSP_SORTWEIGHT_LABEL").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_SHAREDWEIGHT_RADIOBUTTON").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_UNIQUEWEIGHT_RADIOBUTTON").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_LANGUAGE_LABEL").setEnabled(false);
        (m_languagePaneManager).getComponent("AJDSP_LANGUAGE_COMBOBOX").setEnabled(false);
    }
}

/**
Returns the data source currently being displayed.

@return  Data source for this pane.
**/

public AS400JDBCDataSource getDataSource()
{
    return m_currentDataSource;   
}

/**
Sets the data source to be displayed.

@param       dataSource   The JDBC data source to display.
**/

public void setDataSource(AS400JDBCDataSource dataSource)
{
    AS400JDBCDataSource oldDataSource = m_currentDataSource;
    m_currentDataSource = dataSource;
    setDataSourcePreLoadData(m_currentDataSource);
    
    // For some reason the framework won't allow loadData to be called on the
    // tabbedPaneManager more than once (actually it allows it but it won't
    // call the databean gettor methods).  So, the first time through we
    // call loadData on the tabbedPaneManager and each time after that
    // we call loadData on the individual tabs.  Note that if you
    // don't call loadData on the tabbedPaneManager the first time
    // bad things seem to happen.....
    
    if (m_justBeingCreated)
    {
      m_tabbedPaneManager.loadData();
    }
    else
    {
      m_generalPaneManager.loadData();
      m_serverPaneManager.loadData();
      m_packagePaneManager.loadData();
      m_performancePaneManager.loadData();
      m_languagePaneManager.loadData();
      m_otherPaneManager.loadData();
      m_translationPaneManager.loadData();
      m_formatPaneManager.loadData();
      m_connoptPaneManager.loadData();
    }
    
    propertyChangeSupport_.firePropertyChange("dataSource", oldDataSource, m_currentDataSource);
}

/**
Commits the JDBC property values being displayed to the data source.

@return true if changes were applied successfully; false otherwise.
**/

public boolean applyChanges()
{
    // The following try/catch block calls the applyChanges method on the tabbed pane and handles the
    // IllegalUserDataExceptions that occur.  An IllegalUserDataException exception is thrown when
    // a rule defined by the GUI builder is broken.  For instance, if a field is defined to be
    // required and no value is provided the exception will be thrown.
    // Assuming no exceptions are thrown, our databean will be updated with the current GUI values
    // when the tabPaneManager applyChanges method returns.  That is, the tabPaneManager applyChanges
    // method calls all of the databean settor methods.
    
    try
    {
	    m_tabbedPaneManager.applyChanges();
    }
    catch (IllegalUserDataException  e) 
    {   // This will put up the error message and put focus on the offending control
        Container c = (Container)e.getComponent();
	    PanelManager.handleDataException(e,c);
	    return false;
    }
        
    applyChangesToCurrentDataSource(m_currentDataSource);    
    return true;
}

private void applyChangesToCurrentDataSource(AS400JDBCDataSource dataSource)
{
    // This method transfers the values from the databean to the data source.    
    
    // General Tab
        
    dataSource.setDataSourceName(m_dataBean.getDataSourceName()); 
    
    dataSource.setDescription(m_dataBean.getDescription());
    
    dataSource.setServerName(m_dataBean.getAS400Server());
    
    // Server Tab
    
    // The Toolbox expects the SQL library, if any, as the first entry of the libraries string.
    // If there is no SQL library, the string should (and will) have a leading comma.       //@A1A
    String defaultLibs = m_dataBean.getDefaultLibraries().trim();                           //@A1A
    if (defaultLibs.length() > 0)                                                           //@A1A
        dataSource.setLibraries(m_dataBean.getSQLlibrary().trim() + ", " + defaultLibs);    //@A1A
    else                                                                                    //@A1A
        dataSource.setLibraries(m_dataBean.getSQLlibrary().trim());                         //@A1C
        
    //System.out.println(">>>>>>>>>>> SQL LIB into ds:  " + m_dataBean.getSQLlibrary().trim());
    //System.out.println(">>>>>>>>>>> DEFAULT LIBS into ds:  " + defaultLibs);
    
    String scratchName = ((ChoiceDescriptor)m_dataBean.getCommitMode()).getName();
    if (scratchName.equals("AJDSP_COMMIT_NONE"))
    {
        dataSource.setTransactionIsolation("none");
    }
    else if (scratchName.equals("AJDSP_COMMIT_CS"))
    {
        dataSource.setTransactionIsolation("read committed");
    }
    else if (scratchName.equals("AJDSP_COMMIT_CHG"))
    {
        dataSource.setTransactionIsolation("read uncommitted");
    }
    else if (scratchName.equals("AJDSP_COMMIT_ALL"))
    {
        dataSource.setTransactionIsolation("repeatable read");
    }
    else if (scratchName.equals("AJDSP_COMMIT_RR"))
    {
        dataSource.setTransactionIsolation("serializable");
    }

    scratchName = ((ChoiceDescriptor)m_dataBean.getMaxPrecision()).getTitle();              //@A4A
    if (scratchName.equals("31"))                                                           //@A4A
        dataSource.setMaximumPrecision(31);                                                 //@A4A
    else                                                                                    //@A4A
        dataSource.setMaximumPrecision(63);                                                 //@A4A
    
    scratchName = ((ChoiceDescriptor)m_dataBean.getMaxScale()).getTitle();                  //@A4A
    if (scratchName.equals("0"))                                                            //@A4A
        dataSource.setMaximumPrecision(0);                                                  //@A4A
    else                                                                                    //@A4A
    if (scratchName.equals("31"))                                                           //@A4A
        dataSource.setMaximumPrecision(31);                                                 //@A4A
    else                                                                                    //@A4A
        dataSource.setMaximumPrecision(63);                                                 //@A4A

    scratchName = ((ChoiceDescriptor)m_dataBean.getMinDivideScale()).getTitle();            //@A4A
    if (scratchName.equals("0"))                                                            //@A4A
        dataSource.setMinimumDivideScale(0);                                                //@A4A
    else                                                                                    //@A4A
    if (scratchName.equals("1"))                                                            //@A4A
        dataSource.setMinimumDivideScale(1);                                                //@A4A
    else                                                                                    //@A4A
    if (scratchName.equals("2"))                                                            //@A4A
        dataSource.setMinimumDivideScale(2);                                                //@A4A
    else                                                                                    //@A4A
    if (scratchName.equals("3"))                                                            //@A4A
        dataSource.setMinimumDivideScale(3);                                                //@A4A
    else                                                                                    //@A4A
    if (scratchName.equals("4"))                                                            //@A4A
        dataSource.setMinimumDivideScale(4);                                                //@A4A
    else                                                                                    //@A4A
    if (scratchName.equals("5"))                                                            //@A4A
        dataSource.setMinimumDivideScale(5);                                                //@A4A
    else                                                                                    //@A4A
    if (scratchName.equals("6"))                                                            //@A4A
        dataSource.setMinimumDivideScale(6);                                                //@A4A
    else                                                                                    //@A4A
    if (scratchName.equals("7"))                                                            //@A4A
        dataSource.setMinimumDivideScale(7);                                                //@A4A
    else                                                                                    //@A4A
    if (scratchName.equals("8"))                                                            //@A4A
        dataSource.setMinimumDivideScale(8);                                                //@A4A
    else                                                                                    //@A4A
        dataSource.setMinimumDivideScale(9);                                                //@A4A
    
    // Package Tab
    dataSource.setExtendedDynamic(m_dataBean.isEnableExtDynamic());
    dataSource.setPackageCriteria("select");                                                //@A3A
    
    dataSource.setPackage(m_dataBean.getPackage());
    
    dataSource.setPackageLibrary(m_dataBean.getPackageLibrary());
    
    if (m_dataBean.getUsageGroup().equals("AJDSP_USE_RADIOBUTTON"))
    {
        dataSource.setPackageAdd(false);
    }
    else if (m_dataBean.getUsageGroup().equals("AJDSP_USEADD_RADIOBUTTON"))
    {
        dataSource.setPackageAdd(true);
    }
    
    if (m_dataBean.getUnusablePkgActionGroup().equals("AJDSP_SEND_EXCEP_RADIOBUTTON"))
    {
        dataSource.setPackageError("exception");
    }
    else if (m_dataBean.getUnusablePkgActionGroup().equals("AJDSP_POST_WARN_RADIOBUTTON"))
    {
        dataSource.setPackageError("warning");
    }
    else if (m_dataBean.getUnusablePkgActionGroup().equals("AJDSP_IGNORE_RADIOBUTTON"))
    {
        dataSource.setPackageError("none");
    }
        
    dataSource.setPackageCache(m_dataBean.isCachePackageLocally());
    
    // Performance Tab
    
    dataSource.setLazyClose(m_dataBean.isEnableLazyClose());
    
    dataSource.setPrefetch(m_dataBean.isEnablePrefetch());
    
    dataSource.setDataCompression(m_dataBean.isEnableDataCompression());
    
    scratchName = ((ChoiceDescriptor)m_dataBean.getRecordBlockingCriteria()).getName();
    if (scratchName.equals("AJDSP_RECBLK_DISABLE"))
    {
        dataSource.setBlockCriteria(0);
    }
    else if (scratchName.equals("AJDSP_RECBLK_FORFETCH"))
    {
        dataSource.setBlockCriteria(1);
    }
    else if (scratchName.equals("AJDSP_RECBLK_NOTUPDT"))
    {
        dataSource.setBlockCriteria(2);
    }
    
    scratchName = ((ChoiceDescriptor)m_dataBean.getRecordBlockingSize()).getName();
    if (scratchName.equals("AJDSP_BLOCK_SIZE_0"))
    {
        dataSource.setBlockSize(0);
    }
    else if (scratchName.equals("AJDSP_BLOCK_SIZE_8"))
    {
        dataSource.setBlockSize(8);
    }
    else if (scratchName.equals("AJDSP_BLOCK_SIZE_16"))
    {
        dataSource.setBlockSize(16);
    }
    else if (scratchName.equals("AJDSP_BLOCK_SIZE_32"))
    {
        dataSource.setBlockSize(32);
    }
    else if (scratchName.equals("AJDSP_BLOCK_SIZE_64"))
    {
        dataSource.setBlockSize(64);
    }
    else if (scratchName.equals("AJDSP_BLOCK_SIZE_128"))
    {
        dataSource.setBlockSize(128);
    }
    else if (scratchName.equals("AJDSP_BLOCK_SIZE_256"))
    { 
        dataSource.setBlockSize(256);
    }
    else if (scratchName.equals("AJDSP_BLOCK_SIZE_512"))
    {
        dataSource.setBlockSize(512);
    }
    
    Object lobThreshInKBObject = m_dataBean.getLOBThreshold();
    // Class lobThreshInKBClass = lobThreshInKBObject.getClass();
    // if (lobThreshInKBClass.getName().equals("com.ibm.as400.ui.framework.java.ChoiceDescriptor"))
    if (lobThreshInKBObject instanceof com.ibm.as400.ui.framework.java.ChoiceDescriptor)
    {     
        ChoiceDescriptor lobThreshInKBChoice = (ChoiceDescriptor)lobThreshInKBObject;
        String lobThreshInKBString = lobThreshInKBChoice.getTitle();
        Double lobThreshInKBDouble = new Double(lobThreshInKBString);
        double lobThreshInKBLittleD = lobThreshInKBDouble.doubleValue();
        int lobThreshInBytesInt = (int)(lobThreshInKBLittleD * 1024.0);
        dataSource.setLobThreshold(lobThreshInBytesInt);         
    }
    // else if (lobThreshInKBClass.getName().equals("java.lang.Double"))
    else if (lobThreshInKBObject instanceof java.lang.Double)
    {        
        Double lobThreshInKBDouble = (Double)lobThreshInKBObject;  
        double lobThreshInKBLittleD = lobThreshInKBDouble.doubleValue();
        int lobThreshInBytesInt = (int)(lobThreshInKBLittleD * 1024.0);
        dataSource.setLobThreshold(lobThreshInBytesInt); 
    }    
    
    // Language Tab
    
    scratchName = ((ChoiceDescriptor)m_dataBean.getSortType()).getName();
    if (scratchName.equals("AJDSP_SORTHEX"))
    {
        dataSource.setSort("hex");
    }
    else if (scratchName.equals("AJDSP_SORTJOB"))
    {
        dataSource.setSort("job");
    }
    else if (scratchName.equals("AJDSP_SORTLANGID"))
    {
        dataSource.setSort("language");
    }
    else if (scratchName.equals("AJDSP_SORTTABLE"))
    {
        dataSource.setSort("table");
    }
    
    dataSource.setSortTable(m_dataBean.getSortTable());
    
    if (m_dataBean.getSortWeightGroup().equals("AJDSP_SHAREDWEIGHT_RADIOBUTTON"))
    {
        dataSource.setSortWeight("shared");
    }
    else if (m_dataBean.getSortWeightGroup().equals("AJDSP_UNIQUEWEIGHT_RADIOBUTTON"))
    {
        dataSource.setSortWeight("unique");
    }
    
    Object languageObject = m_dataBean.getLanguage();
    // Class languageClass = languageObject.getClass();
    // if (languageClass.getName().equals("com.ibm.as400.ui.framework.java.ChoiceDescriptor"))
    if (languageObject instanceof com.ibm.as400.ui.framework.java.ChoiceDescriptor)
    
    {     
        // If the user selected a language from the dropdown list we'll end up here.
        // Since the choice descriptor contains both the name and title, for example,
        // AJDSP_SORTLG_CAT_TEXT and Catalan, we can extract the name and get the
        // 3 character language id.  Note that for this to work ALL languages in the
        // string table must follow the same naming convention.  That is, they must all
        // be named "AJDSP_SORTLG_" plus the 3 character id plus "_TEXT".
    
        ChoiceDescriptor languageChoice = (ChoiceDescriptor)languageObject;
        String languageTitle = languageChoice.getName();
        String sortLanguage = languageTitle.substring(13,16).toLowerCase();
        dataSource.setSortLanguage(sortLanguage);
    }
    // else if (languageClass.getName().equals("java.lang.String"))
    else if (languageObject instanceof java.lang.String)
    {        
        // If the user entered thier own 3 character language id we'll end up here.
        // Since we really can't validate the id we just need to set it in the data
        // source.
        dataSource.setSortLanguage(((String)languageObject).toLowerCase());
    }    
    
    // Other Tab
    
    if (m_dataBean.getAccessTypeGroup().equals("AJDSP_ACCESSTYPE_RW"))
    {
        dataSource.setAccess("all");
    }
    else if (m_dataBean.getAccessTypeGroup().equals("AJDSP_ACCESSTYPE_RC"))
    {
        dataSource.setAccess("read call");
    }
    else if (m_dataBean.getAccessTypeGroup().equals("AJDSP_ACCESSTYPE_RO"))
    {
        dataSource.setAccess("read only");
    }
    
    if (m_dataBean.getRemarksSourceGroup().equals("AJDSP_SQLDESC_RADIOBUTTON"))
    {
        dataSource.setRemarks("sql");
    }
    else if (m_dataBean.getRemarksSourceGroup().equals("AJDSP_OS400DESC_RADIOBUTTON"))
    {
        dataSource.setRemarks("system");
    }
    
    // Translation Tab
    
    dataSource.setTranslateBinary(m_dataBean.isTranslate65535());
    dataSource.setTranslateHex(m_dataBean.isTranslateHex());                                //@A4A
    
    // Format Tab
        
    scratchName = ((ChoiceDescriptor)m_dataBean.getNamingConvention()).getName();
    if (scratchName.equals("AJDSP_NAMING_SQL"))
    {
        dataSource.setNaming("sql");
    }
    else if (scratchName.equals("AJDSP_NAMING_SYSTEM"))
    {
        dataSource.setNaming("system");
    }
  
    scratchName = ((ChoiceDescriptor)m_dataBean.getDecimalSeparator()).getName();
    if (scratchName.equals("AJDSP_USE_SERVER_JOB"))
    {
        dataSource.setDecimalSeparator("");
    }
    else if (scratchName.equals("AJDSP_DECIMAL_PERIOD"))
    {
        dataSource.setDecimalSeparator(".");
    }
    else if (scratchName.equals("AJDSP_DECIMAL_COMMA"))
    {
        dataSource.setDecimalSeparator(",");
    }
    
    scratchName = ((ChoiceDescriptor)m_dataBean.getTimeFormat()).getName();
    if (scratchName.equals("AJDSP_USE_SERVER_JOB"))
    {
        dataSource.setTimeFormat("");
    }
    else if (scratchName.equals("AJDSP_TIMEFMT_HMS"))
    {
        dataSource.setTimeFormat("hms");
    }
    else if (scratchName.equals("AJDSP_TIMEFMT_USA"))
    {
        dataSource.setTimeFormat("usa");
    }
    else if (scratchName.equals("AJDSP_TIMEFMT_ISO"))
    {
        dataSource.setTimeFormat("iso");
    }
    else if (scratchName.equals("AJDSP_TIMEFMT_EUR"))
    {
        dataSource.setTimeFormat("eur");
    }
    else if (scratchName.equals("AJDSP_TIMEFMT_JIS"))
    {
        dataSource.setTimeFormat("jis");
    }
    
    scratchName = ((ChoiceDescriptor)m_dataBean.getTimeSeparator()).getName();
    if (scratchName.equals("AJDSP_USE_SERVER_JOB"))
    {
        dataSource.setTimeSeparator("");
    }
    else if (scratchName.equals("AJDSP_TIMESEP_COLON"))
    {
        dataSource.setTimeSeparator(":");
    }
    else if (scratchName.equals("AJDSP_TIMESEP_PERIOD"))
    {
        dataSource.setTimeSeparator(".");
    }
    else if (scratchName.equals("AJDSP_TIMESEP_COMMA"))
    {
        dataSource.setTimeSeparator(",");
    }
    else if (scratchName.equals("AJDSP_TIMESEP_BLANK"))
    {
        dataSource.setTimeSeparator("b");
    }
    
    scratchName = ((ChoiceDescriptor)m_dataBean.getDateFormat()).getName();
    if (scratchName.equals("AJDSP_USE_SERVER_JOB"))
    {
        dataSource.setDateFormat("");
    }
    else if (scratchName.equals("AJDSP_DATEFMT_JULIAN"))
    {
        dataSource.setDateFormat("julian");
    }
    else if (scratchName.equals("AJDSP_DATEFMT_MDY"))
    {
        dataSource.setDateFormat("mdy");
    }
    else if (scratchName.equals("AJDSP_DATEFMT_DMY"))
    {
        dataSource.setDateFormat("dmy");
    }
    else if (scratchName.equals("AJDSP_DATEFMT_YMD"))
    {
        dataSource.setDateFormat("ymd");
    }
    else if (scratchName.equals("AJDSP_DATEFMT_USA"))
    {
        dataSource.setDateFormat("usa");
    }
    else if (scratchName.equals("AJDSP_DATEFMT_ISO"))
    {
        dataSource.setDateFormat("iso");
    }
    else if (scratchName.equals("AJDSP_DATEFMT_EUR"))
    {
        dataSource.setDateFormat("eur");
    }
    else if (scratchName.equals("AJDSP_DATEFMT_JIS"))
    {
        dataSource.setDateFormat("jis");
    }
    
    scratchName = ((ChoiceDescriptor)m_dataBean.getDateSeparator()).getName();
    if (scratchName.equals("AJDSP_USE_SERVER_JOB"))
    {
        dataSource.setDateSeparator("");
    }
    else if (scratchName.equals("AJDSP_DATESEP_FORWARDSLASH"))
    {
        dataSource.setDateSeparator("/");
    }
    else if (scratchName.equals("AJDSP_DATESEP_DASH"))
    {
        dataSource.setDateSeparator("-");
    }
    else if (scratchName.equals("AJDSP_DATESEP_PERIOD"))
    {
        dataSource.setDateSeparator(".");
    }
    else if (scratchName.equals("AJDSP_DATESEP_COMMA"))
    {
        dataSource.setDateSeparator(",");
    }
    else if (scratchName.equals("AJDSP_DATESEP_BLANK"))
    {
        dataSource.setDateSeparator("b");
    }
  
    // Connection Options Tab
    
    dataSource.setUser(m_dataBean.getDefaultUserID());
    dataSource.setSecure(m_dataBean.isUseSSL());    
    
}


private void setDataSourcePreLoadData(AS400JDBCDataSource dataSource)
{
  String scratchString;
  boolean scratchBoolean;
      
  // General Tab  
    
  m_dataBean.setDataSourceName(dataSource.getDataSourceName()); 
  m_dataBean.setDescription(dataSource.getDescription());
  m_dataBean.setAS400Server(dataSource.getServerName());
    
  // Server Tab
  
    scratchString = dataSource.getLibraries().trim();                                   //@A1A
    //System.out.println(">>>>>>>>>>> dataSource.getLibraries() returned:  " + scratchString);
  
    if (scratchString.length() > 0)                                                     //@A1A
    {                                                                                   //@A1A
        // The Toolbox stores the SQL library, if any, as the 1st entry of
        // the default libraries string.  If the string has a leading comma,
        // that means there is no SQL library.                                          //@A1A
        if (scratchString.charAt(0) == ',')                                             //@A1A
            // There is no SQL lib, only the default list                               //@A1A
            m_dataBean.setDefaultLibraries(scratchString.substring(1).trim());          //@A1A
        else                                                                            //@A1A
        {                                                                               //@A1A
            // Find first comma and/or blank                                            //@A1A
            int iComma = scratchString.indexOf(',');                                    //@A1A
            int iSpace = scratchString.indexOf(' ');                                    //@A1A
                
            if (iComma == -1 && iSpace == -1)                                           //@A1A
                // No comma or space found, so there is no default list, only the SQL lib
                m_dataBean.setSQLlibrary(scratchString);                                //@A1A
            else                                                                        //@A1A
            {                                                                           //@A1A
                // A comma and/or space was found. Set iComma to lowest index of comma or space.
                if (iComma >= 0)                                                        //@A1A
                {                                                                       //@A1A
                    if (iSpace >= 0 && iSpace < iComma)                                 //@A1A
                        iComma = iSpace;                                                //@A1A
                }                                                                       //@A1A
                else                                                                    //@A1A
                if (iSpace >= 0)                                                        //@A1A
                    iComma = iSpace;                                                    //@A1A

                m_dataBean.setSQLlibrary(scratchString.substring(0,iComma));            //@A1A
                m_dataBean.setDefaultLibraries(scratchString.substring(iComma + 1));    //@A1A
            }                                                                           //@A1A
        }                                                                               //@A1A
    }                                                                                   //@A1A

  if (dataSource.getTransactionIsolation().equals("none"))
  {
      scratchString = resource_Loader.getString("AJDSP_COMMIT_NONE");
      m_dataBean.setCommitMode(new ChoiceDescriptor("AJDSP_COMMIT_NONE", scratchString));  
  }
  else if (dataSource.getTransactionIsolation().equals("read committed"))
  {
      scratchString = resource_Loader.getString("AJDSP_COMMIT_CS");
      m_dataBean.setCommitMode(new ChoiceDescriptor("AJDSP_COMMIT_CS", scratchString));  
  }
  else if (dataSource.getTransactionIsolation().equals("read uncommitted"))
  {
      scratchString = resource_Loader.getString("AJDSP_COMMIT_CHG");
      m_dataBean.setCommitMode(new ChoiceDescriptor("AJDSP_COMMIT_CHG", scratchString));  
  }
  else if (dataSource.getTransactionIsolation().equals("repeatable read"))
  {
      scratchString = resource_Loader.getString("AJDSP_COMMIT_ALL");
      m_dataBean.setCommitMode(new ChoiceDescriptor("AJDSP_COMMIT_ALL", scratchString));  
  }
  else if (dataSource.getTransactionIsolation().equals("serializable"))
  {
      scratchString = resource_Loader.getString("AJDSP_COMMIT_RR");
      m_dataBean.setCommitMode(new ChoiceDescriptor("AJDSP_COMMIT_RR", scratchString));  
  }
  
  int nValue = dataSource.getMaximumPrecision();                                        //@A4A
  if (nValue == 63)                                                                     //@A4A
    m_dataBean.setMaxPrecision(new ChoiceDescriptor("63", "63"));                       //@A4A
  else                                                                                  //@A4A
    m_dataBean.setMaxPrecision(new ChoiceDescriptor("31", "31"));                       //@A4A
  
  nValue = dataSource.getMaximumScale();                                                //@A4A
  if (nValue == 63)                                                                     //@A4A
    m_dataBean.setMaxScale(new ChoiceDescriptor("63", "63"));                           //@A4A
  else                                                                                  //@A4A
  if (nValue >= 31)                                                                     //@A4A
    m_dataBean.setMaxScale(new ChoiceDescriptor("31", "31"));                           //@A4A
  else                                                                                  //@A4A
    m_dataBean.setMaxScale(new ChoiceDescriptor("0", "0"));                             //@A4A

  int nValue = dataSource.getMinimumDivideScale();                                      //@A4A
  if (nValue == 0)                                                                      //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("0", "0"));                       //@A4A
  else                                                                                  //@A4A
  if (nValue == 1)                                                                      //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("1", "1"));                       //@A4A
  else                                                                                  //@A4A
  if (nValue == 2)                                                                      //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("2", "2"));                       //@A4A
  else                                                                                  //@A4A
  if (nValue == 3)                                                                      //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("3", "3"));                       //@A4A
  else                                                                                  //@A4A
  if (nValue == 4)                                                                      //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("4", "4"));                       //@A4A
  else                                                                                  //@A4A
  if (nValue == 5)                                                                      //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("5", "5"));                       //@A4A
  else                                                                                  //@A4A
  if (nValue == 6)                                                                      //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("6", "6"));                       //@A4A
  else                                                                                  //@A4A
  if (nValue == 7)                                                                      //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("7", "7"));                       //@A4A
  else                                                                                  //@A4A
  if (nValue == 8)                                                                      //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("8", "8"));                       //@A4A
  else                                                                                  //@A4A
    m_dataBean.setMinDivideScale(new ChoiceDescriptor("9", "9"));                       //@A4A
  
  // Package Tab
  m_dataBean.setEnableExtDynamic(dataSource.isExtendedDynamic());
  m_dataBean.setPackage(dataSource.getPackage());
  m_dataBean.setPackageLibrary(dataSource.getPackageLibrary());
  if (dataSource.isPackageAdd())
  {
    m_dataBean.setUsageGroup("AJDSP_USEADD_RADIOBUTTON");
  }
  else
  {
    m_dataBean.setUsageGroup("AJDSP_USE_RADIOBUTTON");
  }
  if (dataSource.getPackageError().equals("exception"))
  {
    m_dataBean.setUnusablePkgActionGroup("AJDSP_SEND_EXCEP_RADIOBUTTON");
  }
  else if (dataSource.getPackageError().equals("warning"))
  {
    m_dataBean.setUnusablePkgActionGroup("AJDSP_POST_WARN_RADIOBUTTON");
  }
  else if (dataSource.getPackageError().equals("none"))
  {
    m_dataBean.setUnusablePkgActionGroup("AJDSP_IGNORE_RADIOBUTTON");
  }
  m_dataBean.setCachePackageLocally(dataSource.isPackageCache());
  
  // Performance Tab
  m_dataBean.setEnableLazyClose(dataSource.isLazyClose());
  m_dataBean.setEnablePrefetch(dataSource.isPrefetch());
  m_dataBean.setEnableDataCompression(dataSource.isDataCompression());
    
  if (dataSource.getBlockCriteria() == 0)
  {
      scratchString = resource_Loader.getString("AJDSP_RECBLK_DISABLE");
      m_dataBean.setRecordBlockingCriteria(new ChoiceDescriptor("AJDSP_RECBLK_DISABLE", scratchString));  
  }
  else if (dataSource.getBlockCriteria() == 1)
  {
      scratchString = resource_Loader.getString("AJDSP_RECBLK_FORFETCH");
      m_dataBean.setRecordBlockingCriteria(new ChoiceDescriptor("AJDSP_RECBLK_FORFETCH", scratchString));  
  }
  else if (dataSource.getBlockCriteria() == 2)
  {
      scratchString = resource_Loader.getString("AJDSP_RECBLK_NOTUPDT");
      m_dataBean.setRecordBlockingCriteria(new ChoiceDescriptor("AJDSP_RECBLK_NOTUPDT", scratchString));  
  }
  
  switch(dataSource.getBlockSize())
  {
      case(0):
        scratchString = resource_Loader.getString("AJDSP_BLOCK_SIZE_0");
        m_dataBean.setRecordBlockingSize(new ChoiceDescriptor("AJDSP_BLOCK_SIZE_0", scratchString));  
        break;
      case(8):
        scratchString = resource_Loader.getString("AJDSP_BLOCK_SIZE_8");
        m_dataBean.setRecordBlockingSize(new ChoiceDescriptor("AJDSP_BLOCK_SIZE_8", scratchString));  
        break;
      case(16):
        scratchString = resource_Loader.getString("AJDSP_BLOCK_SIZE_16");
        m_dataBean.setRecordBlockingSize(new ChoiceDescriptor("AJDSP_BLOCK_SIZE_16", scratchString));  
        break;
      case(32):
        scratchString = resource_Loader.getString("AJDSP_BLOCK_SIZE_32");
        m_dataBean.setRecordBlockingSize(new ChoiceDescriptor("AJDSP_BLOCK_SIZE_32", scratchString));  
        break;
      case(64):
        scratchString = resource_Loader.getString("AJDSP_BLOCK_SIZE_64");
        m_dataBean.setRecordBlockingSize(new ChoiceDescriptor("AJDSP_BLOCK_SIZE_64", scratchString));  
        break;
      case(128):
        scratchString = resource_Loader.getString("AJDSP_BLOCK_SIZE_128");
        m_dataBean.setRecordBlockingSize(new ChoiceDescriptor("AJDSP_BLOCK_SIZE_128", scratchString));  
        break;
      case(256):
        scratchString = resource_Loader.getString("AJDSP_BLOCK_SIZE_256");
        m_dataBean.setRecordBlockingSize(new ChoiceDescriptor("AJDSP_BLOCK_SIZE_256", scratchString));  
        break;
      case(512):
        scratchString = resource_Loader.getString("AJDSP_BLOCK_SIZE_512");
        m_dataBean.setRecordBlockingSize(new ChoiceDescriptor("AJDSP_BLOCK_SIZE_512", scratchString));  
        break;
  }
   
  double lobThresholdInKBLittleD = dataSource.getLobThreshold() / 1024;
  Double lobThresholdInKBBigD = new Double(lobThresholdInKBLittleD);
  m_dataBean.setLOBThreshold(lobThresholdInKBBigD);                                         //@A2C
  //m_dataBean.setLOBThreshold(lobThresholdInKBBigD.toString());            <----- ORIGINALLY
     
  // Language Tab
  
  if (dataSource.getSort().equals("hex"))
  {
      scratchString = resource_Loader.getString("AJDSP_SORTHEX");
      m_dataBean.setSortType(new ChoiceDescriptor("AJDSP_SORTHEX", scratchString));  
  }
  else if (dataSource.getSort().equals("job"))
  {
      scratchString = resource_Loader.getString("AJDSP_SORTJOB");
      m_dataBean.setSortType(new ChoiceDescriptor("AJDSP_SORTJOB", scratchString));  
  }
  else if (dataSource.getSort().equals("language"))
  {
      scratchString = resource_Loader.getString("AJDSP_SORTLANGID");
      m_dataBean.setSortType(new ChoiceDescriptor("AJDSP_SORTLANGID", scratchString));  
  }
  else if (dataSource.getSort().equals("table"))
  {
      scratchString = resource_Loader.getString("AJDSP_SORTTABLE");
      m_dataBean.setSortType(new ChoiceDescriptor("AJDSP_SORTTABLE", scratchString));
  }
  
  m_dataBean.setSortTable(dataSource.getSortTable()); 
  
  if (dataSource.getSortWeight().equals("shared"))
  {
      m_dataBean.setSortWeightGroup("AJDSP_SHAREDWEIGHT_RADIOBUTTON");
  }
  else if (dataSource.getSortWeight().equals("unique"))
  {
      m_dataBean.setSortWeightGroup("AJDSP_UNIQUEWEIGHT_RADIOBUTTON");
  }
  
  String sortLanguage = dataSource.getSortLanguage();
  if (sortLanguage.equalsIgnoreCase("afr") || sortLanguage.equalsIgnoreCase("ara") ||
      sortLanguage.equalsIgnoreCase("bel") || sortLanguage.equalsIgnoreCase("bgr") ||
      sortLanguage.equalsIgnoreCase("cat") || sortLanguage.equalsIgnoreCase("chs") ||
      sortLanguage.equalsIgnoreCase("cht") || sortLanguage.equalsIgnoreCase("csy") ||
      sortLanguage.equalsIgnoreCase("dan") || sortLanguage.equalsIgnoreCase("des") ||
      sortLanguage.equalsIgnoreCase("deu") || sortLanguage.equalsIgnoreCase("ell") ||
      sortLanguage.equalsIgnoreCase("ena") || sortLanguage.equalsIgnoreCase("enb") ||
      sortLanguage.equalsIgnoreCase("eng") || sortLanguage.equalsIgnoreCase("enp") ||
      sortLanguage.equalsIgnoreCase("enu") || sortLanguage.equalsIgnoreCase("esp") ||
      sortLanguage.equalsIgnoreCase("est") || sortLanguage.equalsIgnoreCase("far") ||
      sortLanguage.equalsIgnoreCase("fin") || sortLanguage.equalsIgnoreCase("fra") ||
      sortLanguage.equalsIgnoreCase("frb") || sortLanguage.equalsIgnoreCase("frc") ||
      sortLanguage.equalsIgnoreCase("frs") || sortLanguage.equalsIgnoreCase("gae") ||
      sortLanguage.equalsIgnoreCase("heb") || sortLanguage.equalsIgnoreCase("hrv") ||
      sortLanguage.equalsIgnoreCase("hun") || sortLanguage.equalsIgnoreCase("isl") ||
      sortLanguage.equalsIgnoreCase("ita") || sortLanguage.equalsIgnoreCase("its") ||
      sortLanguage.equalsIgnoreCase("jpn") || sortLanguage.equalsIgnoreCase("kor") ||
      sortLanguage.equalsIgnoreCase("lao") || sortLanguage.equalsIgnoreCase("ltu") ||
      sortLanguage.equalsIgnoreCase("lva") || sortLanguage.equalsIgnoreCase("mkd") ||
      sortLanguage.equalsIgnoreCase("nlb") || sortLanguage.equalsIgnoreCase("nld") ||
      sortLanguage.equalsIgnoreCase("non") || sortLanguage.equalsIgnoreCase("nor") ||
      sortLanguage.equalsIgnoreCase("plk") || sortLanguage.equalsIgnoreCase("ptb") ||
      sortLanguage.equalsIgnoreCase("ptg") || sortLanguage.equalsIgnoreCase("rms") ||
      sortLanguage.equalsIgnoreCase("rom") || sortLanguage.equalsIgnoreCase("rus") ||
      sortLanguage.equalsIgnoreCase("sky") || sortLanguage.equalsIgnoreCase("slo") ||
      sortLanguage.equalsIgnoreCase("sqi") || sortLanguage.equalsIgnoreCase("srb") ||
      sortLanguage.equalsIgnoreCase("srl") || sortLanguage.equalsIgnoreCase("sve") ||
      sortLanguage.equalsIgnoreCase("tha") || sortLanguage.equalsIgnoreCase("trk") ||
      sortLanguage.equalsIgnoreCase("ukr") || sortLanguage.equalsIgnoreCase("urd") ||
      sortLanguage.equalsIgnoreCase("vie"))
    
     {
        // The following builds the name of the string table entry using the
        // 3 character language id.  Note that for this to work ALL languages in the
        // string table must follow the same naming convention.  That is, they must all
        // be named "AJDSP_SORTLG_" plus the 3 character id plus "_TEXT".
 
        String languageChoice = "AJDSP_SORTLG_" + sortLanguage.toUpperCase() + "_TEXT";
        scratchString = resource_Loader.getString(languageChoice);
        m_dataBean.setLanguage(new ChoiceDescriptor(languageChoice, scratchString));
     } 
     else
     {
        m_dataBean.setLanguage(sortLanguage);
     }
     int x = 1; //Without this allocation of storage, the Cafe' debugger traps if you try
             // to step thru this code....  It will execute just fine, you just can't
             // debug it.
     
  // Other Tab
      
  if (dataSource.getAccess().equals("all"))
  {
      m_dataBean.setAccessTypeGroup("AJDSP_ACCESSTYPE_RW");
  }
  else if (dataSource.getAccess().equals("read call"))
  {
      m_dataBean.setAccessTypeGroup("AJDSP_ACCESSTYPE_RC");
  }
  else if (dataSource.getAccess().equals("read only"))
  {
      m_dataBean.setAccessTypeGroup("AJDSP_ACCESSTYPE_RO");
  }
     
  if (dataSource.getRemarks().equals("sql"))
  {
      m_dataBean.setRemarksSourceGroup("AJDSP_SQLDESC_RADIOBUTTON");
  }
  else if (dataSource.getRemarks().equals("system"))
  {
      m_dataBean.setRemarksSourceGroup("AJDSP_OS400DESC_RADIOBUTTON");
  }
     
  // Translation Tab
  m_dataBean.setTranslate65535(dataSource.isTranslateBinary());
  m_dataBean.setTranslateHex(dataSource.getTranslateHex().equals("binary"));                        //@A4A
  
  // Format Tab
  
  if (dataSource.getNaming().equals("sql"))
  {
      scratchString = resource_Loader.getString("AJDSP_NAMING_SQL");
      m_dataBean.setNamingConvention(new ChoiceDescriptor("AJDSP_NAMING_SQL", scratchString));  
  }
  else if (dataSource.getNaming().equals("system"))
  {
      scratchString = resource_Loader.getString("AJDSP_NAMING_SYSTEM");
      m_dataBean.setNamingConvention(new ChoiceDescriptor("AJDSP_NAMING_SYSTEM", scratchString));  
  }
  
  if (dataSource.getDecimalSeparator().equals(""))
  {
      scratchString = resource_Loader.getString("AJDSP_USE_SERVER_JOB");
      m_dataBean.setDecimalSeparator(new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString));  
  }
  else if (dataSource.getDecimalSeparator().equals("."))
  {
      scratchString = resource_Loader.getString("AJDSP_DECIMAL_PERIOD");
      m_dataBean.setDecimalSeparator(new ChoiceDescriptor("AJDSP_DECIMAL_PERIOD", scratchString));  
  }
  else if (dataSource.getDecimalSeparator().equals(","))
  {
      scratchString = resource_Loader.getString("AJDSP_DECIMAL_COMMA");
      m_dataBean.setDecimalSeparator(new ChoiceDescriptor("AJDSP_DECIMAL_COMMA", scratchString));  
  }
  
  if (dataSource.getTimeFormat().equals(""))
  {
      scratchString = resource_Loader.getString("AJDSP_USE_SERVER_JOB");
      m_dataBean.setTimeFormat(new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString));  
  }
  else if (dataSource.getTimeFormat().equals("hms"))
  {
      scratchString = resource_Loader.getString("AJDSP_TIMEFMT_HMS");
      m_dataBean.setTimeFormat(new ChoiceDescriptor("AJDSP_TIMEFMT_HMS", scratchString));  
  }
  else if (dataSource.getTimeFormat().equals("usa"))
  {
      scratchString = resource_Loader.getString("AJDSP_TIMEFMT_USA");
      m_dataBean.setTimeFormat(new ChoiceDescriptor("AJDSP_TIMEFMT_USA", scratchString));  
  }
  else if (dataSource.getTimeFormat().equals("iso"))
  {
      scratchString = resource_Loader.getString("AJDSP_TIMEFMT_ISO");
      m_dataBean.setTimeFormat(new ChoiceDescriptor("AJDSP_TIMEFMT_ISO", scratchString));  
  }
  else if (dataSource.getTimeFormat().equals("eur"))
  {
      scratchString = resource_Loader.getString("AJDSP_TIMEFMT_EUR");
      m_dataBean.setTimeFormat(new ChoiceDescriptor("AJDSP_TIMEFMT_EUR", scratchString));  
  }
  else if (dataSource.getTimeFormat().equals("jis"))
  {
      scratchString = resource_Loader.getString("AJDSP_TIMEFMT_JIS");
      m_dataBean.setTimeFormat(new ChoiceDescriptor("AJDSP_TIMEFMT_JIS", scratchString));  
  }
  
  if (dataSource.getTimeSeparator().equals(""))
  {
      scratchString = resource_Loader.getString("AJDSP_USE_SERVER_JOB");
      m_dataBean.setTimeSeparator(new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString));  
  }
  else if (dataSource.getTimeSeparator().equals(":"))
  {
      scratchString = resource_Loader.getString("AJDSP_TIMESEP_COLON");
      m_dataBean.setTimeSeparator(new ChoiceDescriptor("AJDSP_TIMESEP_COLON", scratchString));  
  }
  else if (dataSource.getTimeSeparator().equals("."))
  {
      scratchString = resource_Loader.getString("AJDSP_TIMESEP_PERIOD");
      m_dataBean.setTimeSeparator(new ChoiceDescriptor("AJDSP_TIMESEP_PERIOD", scratchString));  
  }
  else if (dataSource.getTimeSeparator().equals(","))
  {
      scratchString = resource_Loader.getString("AJDSP_TIMESEP_COMMA");
      m_dataBean.setTimeSeparator(new ChoiceDescriptor("AJDSP_TIMESEP_COMMA", scratchString));  
  }
  else if (dataSource.getTimeSeparator().equals("b"))
  {
      scratchString = resource_Loader.getString("AJDSP_TIMESEP_BLANK");
      m_dataBean.setTimeSeparator(new ChoiceDescriptor("AJDSP_TIMESEP_BLANK", scratchString));  
  }
  
  if (dataSource.getDateFormat().equals(""))
  {
      scratchString = resource_Loader.getString("AJDSP_USE_SERVER_JOB");
      m_dataBean.setDateFormat(new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString));  
  }
  else if (dataSource.getDateFormat().equals("julian"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATEFMT_JULIAN");
      m_dataBean.setDateFormat(new ChoiceDescriptor("AJDSP_DATEFMT_JULIAN", scratchString));  
  }
  else if (dataSource.getDateFormat().equals("mdy"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATEFMT_MDY");
      m_dataBean.setDateFormat(new ChoiceDescriptor("AJDSP_DATEFMT_MDY", scratchString));  
  }
  else if (dataSource.getDateFormat().equals("dmy"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATEFMT_DMY");
      m_dataBean.setDateFormat(new ChoiceDescriptor("AJDSP_DATEFMT_DMY", scratchString));  
  }
  else if (dataSource.getDateFormat().equals("ymd"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATEFMT_YMD");
      m_dataBean.setDateFormat(new ChoiceDescriptor("AJDSP_DATEFMT_YMD", scratchString));  
  }
  else if (dataSource.getDateFormat().equals("usa"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATEFMT_USA");
      m_dataBean.setDateFormat(new ChoiceDescriptor("AJDSP_DATEFMT_USA", scratchString));  
  }
  else if (dataSource.getDateFormat().equals("iso"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATEFMT_ISO");
      m_dataBean.setDateFormat(new ChoiceDescriptor("AJDSP_DATEFMT_ISO", scratchString));  
  }
  else if (dataSource.getDateFormat().equals("eur"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATEFMT_EUR");
      m_dataBean.setDateFormat(new ChoiceDescriptor("AJDSP_DATEFMT_EUR", scratchString));  
  }
  else if (dataSource.getDateFormat().equals("jis"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATEFMT_JIS");
      m_dataBean.setDateFormat(new ChoiceDescriptor("AJDSP_DATEFMT_JIS", scratchString));
  }
  
  if (dataSource.getDateSeparator().equals(""))
  {
      scratchString = resource_Loader.getString("AJDSP_USE_SERVER_JOB");
      m_dataBean.setDateSeparator(new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString));  
  }
  else if (dataSource.getDateSeparator().equals("/"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATESEP_FORWARDSLASH");
      m_dataBean.setDateSeparator(new ChoiceDescriptor("AJDSP_DATESEP_FORWARDSLASH", scratchString));  
  }
  else if (dataSource.getDateSeparator().equals("-"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATESEP_DASH");
      m_dataBean.setDateSeparator(new ChoiceDescriptor("AJDSP_DATESEP_DASH", scratchString));  
  }
  else if (dataSource.getDateSeparator().equals("."))
  {
      scratchString = resource_Loader.getString("AJDSP_DATESEP_PERIOD");
      m_dataBean.setDateSeparator(new ChoiceDescriptor("AJDSP_DATESEP_PERIOD", scratchString));  
  }
  else if (dataSource.getDateSeparator().equals(","))
  {
      scratchString = resource_Loader.getString("AJDSP_DATESEP_COMMA");
      m_dataBean.setDateSeparator(new ChoiceDescriptor("AJDSP_DATESEP_COMMA", scratchString));  
  }
  else if (dataSource.getDateSeparator().equals("b"))
  {
      scratchString = resource_Loader.getString("AJDSP_DATESEP_BLANK");
      m_dataBean.setDateSeparator(new ChoiceDescriptor("AJDSP_DATESEP_BLANK", scratchString));  
  }
 
  // Connection Options Tab
  m_dataBean.setDefaultUserID(dataSource.getUser());
  m_dataBean.setUseSSL(dataSource.isSecure());
  
}

/** Removes the specified tab from the pane.  The following constants 
should be used to specify the tab number:
<ul>
  <li>TAB_GENERAL
  <li>TAB_SERVER
  <li>TAB_PACKAGE
  <li>TAB_PERFORMANCE
  <li>TAB_LANGUAGE
  <li>TAB_OTHER
  <li>TAB_TRANSLATION
  <li>TAB_FORMAT
  <li>TAB_CONNECTIONOPTIONS
</ul>
**/

public void removeTabAt(int tabNumber)
{
    // This method removes the specified tab
    
  switch(tabNumber)
  {
    case(TAB_GENERAL):
      m_tabbedPaneManager.removePane("AJDSP_GENERAL");      
      break;
    case(TAB_SERVER):
      m_tabbedPaneManager.removePane("AJDSP_SERVER");      
      break;
    case(TAB_PACKAGE):
      m_tabbedPaneManager.removePane("AJDSP_PACKAGE");      
      break;
    case(TAB_PERFORMANCE):
      m_tabbedPaneManager.removePane("AJDSP_PERFORMANCE");      
      break;
    case(TAB_LANGUAGE):
      m_tabbedPaneManager.removePane("AJDSP_LANGUAGE");      
      break;
    case(TAB_OTHER):
      m_tabbedPaneManager.removePane("AJDSP_OTHER");      
      break;
    case(TAB_TRANSLATION):
      m_tabbedPaneManager.removePane("AJDSP_TRANSLATION");      
      break;
    case(TAB_FORMAT):
      m_tabbedPaneManager.removePane("AJDSP_FORMAT");      
      break;
    case(TAB_CONNECTIONOPTIONS):
      m_tabbedPaneManager.removePane("AJDSP_CONNOPT");      
      break;    
  }
    
}

public void addPropertyChangeListener(PropertyChangeListener listener) 
{
    if (listener == null)
        throw new NullPointerException("listener");
    propertyChangeSupport_.addPropertyChangeListener(listener);
}
                   
public void removePropertyChangeListener(PropertyChangeListener listener) 
{
    if (listener == null)
        throw new NullPointerException("listener");
    propertyChangeSupport_.removePropertyChangeListener(listener);
}

private void readObject(java.io.ObjectInputStream in)
throws IOException, ClassNotFoundException
{
    in.defaultReadObject();
    propertyChangeSupport_ = new PropertyChangeSupport(this);
}
         

}
