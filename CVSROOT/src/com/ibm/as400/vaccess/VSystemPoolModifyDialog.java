///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemPoolModifyDialog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;


import com.ibm.as400.access.SystemPool;
import com.ibm.as400.access.Trace;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.lang.NumberFormatException;
import java.util.Vector;



/**
 * VSystemPoolModifiyDialog class represents VSystem Pool Modify Dialog.
**/

class VSystemPoolModifyDialog extends JDialog
        implements ActionListener, WindowListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    //MRI start.
    private static final String invalidInputText_         = ResourceLoader.getText ("DLG_INVALID_INPUT")+": ";

    private static final String maximumFalutsText_        = ResourceLoader.getText ("SYSTEM_POOL_MAXIMUM_FAULTS")+": ";
    private static final String maximumPoolSizeText_      = ResourceLoader.getText ("SYSTEM_POOL_MAXIMUM_POOL_SIZE")+": ";
    private static final String messageLoggingText_       = ResourceLoader.getText ("SYSTEM_POOL_MESSAGE_LOGGING")+": ";
    private static final String minimumFaultsText_        = ResourceLoader.getText ("SYSTEM_POOL_MINIMUM_FAULTS")+": ";
    private static final String minimumPoolSizeText_      = ResourceLoader.getText ("SYSTEM_POOL_MINIMUM_POOL_SIZE")+": ";
    private static final String newActivityLevelText_     = ResourceLoader.getText ("SYSTEM_POOL_ACTIVITY_LEVEL")+": ";
    private static final String newPoolSizeText_          = ResourceLoader.getText ("SYSTEM_POOL_POOL_SIZE")+": ";
    private static final String pagingOptionText_         = ResourceLoader.getText ("SYSTEM_POOL_PAGING_OPTION")+": ";
    private static final String perThreadFaultsText_      = ResourceLoader.getText ("SYSTEM_POOL_PERTHREADS_FAULTS")+": ";
    private static final String priorityText_             = ResourceLoader.getText ("SYSTEM_POOL_PRIORITY")+": ";
    private static final String systemPoolIdentifierText_ = ResourceLoader.getText ("SYSTEM_POOL_IDENTIFIER")+": ";
    private static final String title_                    = ResourceLoader.getText ("DLG_MODIFY");  //@D1A
    //MRI end

    private static final String same_ = "*SAME";
    private static final String calc_ = "*CALC";
    
    // Added event support.
    private ErrorEventSupport errorEventSupport_ = new ErrorEventSupport(this);
    private WorkingEventSupport workingEventSupport_ = new WorkingEventSupport(this); //@B1A
    
    // The system pool object.
    private SystemPool sysPool_ = null;

    // The text field used to display the system pool identifier.
    private String sysPoolID_ = "";

    // The text field used to modify the new pool size.
    private JTextField sysPoolNewPoolSiz_ = null;

    // The text field used to modify the new pool activity level.
    private JComboBox sysPoolNewPoolActLev_ = null;

    // The Choice used to decide whether to log the change messages or not.
    private JComboBox sysPoolMesLog_ = null;

    // The Choice used to decide whether the system should dynamically adjust
    // the paging characteristics of the storage pool for optimum performance.
    private JComboBox sysPoolPagOpt_ = null;

    // The text field used to modify the priority of this pool.
    private JComboBox sysPoolPri_    = null;

    // The text field used to modify the minimum amount of storage
    // to this storage pool.
    private JComboBox sysPoolMinPoolSiz_ = null;

    // The text field used to modify the maximum amount of storage
    // to this storage pool.
    private JComboBox sysPoolMaxPoolSiz_ = null;

    // The text field used to modify the minimum faults-per-second
    // guideline to use for this storage pool.
    private JComboBox sysPoolMinFau_     = null;

    // The text field used to modify the faults per second for each
    // active thread in this storage pool.
    private JComboBox sysPoolPerThrFau_  = null;

    // The text field used to modify the maximum faults-per-second
    // guideline to use for this storage pool.
    private JComboBox sysPoolMaxFau_     = null;


    // The "Apply", "Cancel" and "Ok" button.
    private JButton applyButton_  = null;
    private JButton cancelButton_ = null;
    private JButton okButton_     = null;

    
    private boolean isMachinePool_; //@B2A - indicates if this system pool is *MACHINE
    
    
    /**
     * Constructs a VSystemPoolModifyDialog object.
     *
     * @param frame   The frame.
     * @param sysPool The SystemPool object.
    **/
    public VSystemPoolModifyDialog(Frame frame, SystemPool sysPool)
    {
        super(frame, title_, true);
        sysPool_ = sysPool;
        //@B2 - see CPF1165 for the values that aren't allowed to be set when pool is *MACHINE
        isMachinePool_ = sysPool_.getPoolName().trim().equalsIgnoreCase("*MACHINE"); //@B2A
        init();
    }

    /**
     * The action is performed.
     *
     * @param event The action event.
    **/
    public void actionPerformed(ActionEvent e)
    {
        Object aSource = e.getSource();

        if(aSource == okButton_)
        {
            if(!applyButton_.isEnabled())
              dispose();
            else
              if (setChanges())
                dispose();
        }
        else if(aSource == applyButton_)
        {
            if(setChanges())
            {
              applyButton_.setEnabled(false);
            }
        }
        else if(aSource == cancelButton_)
        {
            dispose();
        }
        sysPool_.refreshCache(); //@B3A better reload our information
    }

    /**
     * Adds a listener to be notified when an error occurs.
     *
     * @param listener The listener.
    **/
    public void addErrorListener(ErrorListener listener)
    {
      if (listener == null) throw new NullPointerException("listener");
      errorEventSupport_.addErrorListener(listener);
    }

    /**
     * Adds a listener to be notified when work starts and stops on
     * potentially long-running operations.
     *
     * @param listener The listener.
    **/
    public void addWorkingListener(WorkingListener listener)
    {
      if (listener == null) throw new NullPointerException("listener");
      workingEventSupport_.addWorkingListener(listener);
    }

    /**
     * Returns the main pane.
     *
     * @return The main pane.
    **/
    private JPanel getMainPane()
    {
        JPanel p = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        p.setLayout(layout);
        p.setBorder(((Border) new EmptyBorder( 5, 5, 5, 5) ));

        int index = 0;

            // System pool identifier.
            VUtilities.constrain (systemPoolIdentifierText_,
                   sysPoolID_,
                   p, layout, index++);

            // New pool size.
            VUtilities.constrain ((Component) new JLabel(newPoolSizeText_),
                   (Component) sysPoolNewPoolSiz_,
                   p, layout, index++);

            // New pool activity level.
            VUtilities.constrain ((Component) new JLabel(newActivityLevelText_),
                   (Component) sysPoolNewPoolActLev_,
                   p, layout, index++);

            // Message logging.
            VUtilities.constrain ((Component) new JLabel(messageLoggingText_),
                   (Component) sysPoolMesLog_,
                   p, layout, index++);

            // Paging option.
            VUtilities.constrain ((Component) new JLabel(pagingOptionText_),
                   (Component) sysPoolPagOpt_,
                   p, layout, index++);

            // Priority.
            VUtilities.constrain ((Component) new JLabel(priorityText_),
                   (Component) sysPoolPri_,
                   p, layout, index++);

            // Minimum pool size.
            VUtilities.constrain ((Component) new JLabel(minimumPoolSizeText_),
                   (Component) sysPoolMinPoolSiz_,
                   p, layout, index++);

            // Maximum pool size.
            VUtilities.constrain ((Component) new JLabel(maximumPoolSizeText_),
                   (Component) sysPoolMaxPoolSiz_,
                   p, layout, index++);

            // Minimum faults
            VUtilities.constrain ((Component) new JLabel(minimumFaultsText_),
                   (Component) sysPoolMinFau_,
                   p, layout, index++);

            // Per-thread faults.
            VUtilities.constrain ((Component) new JLabel(perThreadFaultsText_),
                   (Component) sysPoolPerThrFau_,
                   p, layout, index++);

            // Maximum faults.
            VUtilities.constrain ((Component) new JLabel(maximumFalutsText_),
                   (Component) sysPoolMaxFau_,
                   p, layout, index++);

        return p;
    }

    /**
     * This listener will enable the apply button when a component's
     * value is changed by either selecting a different one from the
     * drop-down list, or by typing a new value.
    **/
    private class VSPMDListener implements KeyListener, ItemListener
    {
      VSPMDListener() {}
      VSPMDListener(JComboBox box)
      {
        box.addItemListener(this);
        box.getEditor().getEditorComponent().addKeyListener(this); // this line makes it possible to receive events when user types in a combo box
      }
      public void itemStateChanged(ItemEvent event)
      {
        applyButton_.setEnabled(true);
      }
      public void keyPressed(KeyEvent event)
      {
        applyButton_.setEnabled(true);
      }
      public void keyReleased(KeyEvent event) {}
      public void keyTyped(KeyEvent event) {}
    }
       
    /**
     * Initializes the transient data.
    **/
    private void init()
    {
      try //@B1A
      {
        sysPoolID_ = Integer.toString(sysPool_.getPoolIdentifier());
        VSPMDListener listener = null;
        
        // New pool size.
        sysPoolNewPoolSiz_ = new JTextField();
        String currentPoolSizeStr = String.valueOf(sysPool_.getPoolSize());
        sysPoolNewPoolSiz_.setText(currentPoolSizeStr);

        if(sysPool_.getPoolName().trim().equals("*BASE"))
            sysPoolNewPoolSiz_.setEditable(false);
        else
            sysPoolNewPoolSiz_.setEditable(true);
        sysPoolNewPoolSiz_.addKeyListener(new VSPMDListener());

        // New pool activity level.
        sysPoolNewPoolActLev_ = new JComboBox();
        sysPoolNewPoolActLev_.addItem(same_);
        sysPoolNewPoolActLev_.setSelectedItem(same_);
        if(isMachinePool_) //@B2C
        {
          sysPoolNewPoolActLev_.setEditable(false);
          sysPoolNewPoolActLev_.setEnabled(false);
        }
        else
        {
          sysPoolNewPoolActLev_.setEditable(true);
          sysPoolNewPoolActLev_.setEnabled(true);
        }
        listener = new VSPMDListener(sysPoolNewPoolActLev_);
        
        // Message logging.
        sysPoolMesLog_ = new JComboBox();
        sysPoolMesLog_.setEditable(false);
        sysPoolMesLog_.addItem(ResourceLoader.getText("DLG_YES")); //@C0C
        sysPoolMesLog_.addItem(ResourceLoader.getText("DLG_NO")); //@C0C
        sysPoolMesLog_.setSelectedItem(ResourceLoader.getText("DLG_YES")); //@C0C
        listener = new VSPMDListener(sysPoolMesLog_);

        // Paging option.
        sysPoolPagOpt_ = new JComboBox();
        sysPoolPagOpt_.setEditable(false);
        String currentPagingOption = sysPool_.getPagingOption().trim();
        if( (!currentPagingOption.equals("*FIXED")) &&
            (!currentPagingOption.equals("*CALC")))
        {
            sysPoolPagOpt_.addItem(currentPagingOption);
        }
        sysPoolPagOpt_.addItem("*FIXED");
        sysPoolPagOpt_.addItem("*CALC");
        sysPoolPagOpt_.setSelectedItem(currentPagingOption);
        if (isMachinePool_)                 //@B2A
        {                                   //@B2A
          sysPoolPagOpt_.setEnabled(false); //@B2A
        }                                   //@B2A
        listener = new VSPMDListener(sysPoolPagOpt_);

        // Priority.
        sysPoolPri_ = new JComboBox();
        if(isMachinePool_) //@B2C
        {
            sysPoolPri_.addItem("1");
            sysPoolPri_.setSelectedItem("1");
            sysPoolPri_.setEditable(false);
            sysPoolPri_.setEnabled(false);
        }
        else
        {
            sysPoolPri_.addItem(same_);
            sysPoolPri_.addItem(calc_);
            sysPoolPri_.setEditable(true);
            sysPoolPri_.setEnabled(true);
        }
        listener = new VSPMDListener(sysPoolPri_);

        // Minimum pool size.
        sysPoolMinPoolSiz_ = new JComboBox();
        sysPoolMinPoolSiz_.setEditable(true);
        sysPoolMinPoolSiz_.addItem(same_);
        sysPoolMinPoolSiz_.addItem(calc_);
        sysPoolMinPoolSiz_.setSelectedItem(same_);
        listener = new VSPMDListener(sysPoolMinPoolSiz_);

        // Maximum pool size.
        sysPoolMaxPoolSiz_ = new JComboBox();
        sysPoolMaxPoolSiz_.setEditable(true);
        sysPoolMaxPoolSiz_.addItem(same_);
        sysPoolMaxPoolSiz_.addItem(calc_);
        sysPoolMaxPoolSiz_.setSelectedItem(same_);
        listener = new VSPMDListener(sysPoolMaxPoolSiz_);

        // Minimum faults
        sysPoolMinFau_ = new JComboBox();
        sysPoolMinFau_.setEditable(true);
        sysPoolMinFau_.addItem(same_);
        sysPoolMinFau_.addItem(calc_);
        sysPoolMinFau_.setSelectedItem(same_);
        listener = new VSPMDListener(sysPoolMinFau_);

        // Per-thread faults.
        sysPoolPerThrFau_ = new JComboBox();
        sysPoolPerThrFau_.setEditable(true);
        sysPoolPerThrFau_.addItem(same_);
        sysPoolPerThrFau_.addItem(calc_);
        sysPoolPerThrFau_.setSelectedItem(same_);
        listener = new VSPMDListener(sysPoolPerThrFau_);        

        // Maximum faults.
        sysPoolMaxFau_ = new JComboBox();
        sysPoolMaxFau_.setEditable(true);
        sysPoolMaxFau_.addItem(same_);
        sysPoolMaxFau_.addItem(calc_);
        sysPoolMaxFau_.setSelectedItem(same_);
        listener = new VSPMDListener(sysPoolMaxFau_);

        okButton_ = new JButton(ResourceLoader.getText("DLG_OK"));
        cancelButton_ = new JButton(ResourceLoader.getText("DLG_CANCEL"));
        applyButton_ = new JButton(ResourceLoader.getText("DLG_APPLY"));
        applyButton_.setEnabled(false);

        okButton_.addActionListener(this);
        cancelButton_.addActionListener(this);
        applyButton_.addActionListener(this);
        addWindowListener(this);

        // Arrange components
        JPanel buttonsPane = new JPanel();
        buttonsPane.add(okButton_);
        buttonsPane.add(cancelButton_);
        buttonsPane.add(applyButton_);
        
        Container c = getContentPane();
        c.add("North", getMainPane());
        c.add("South", buttonsPane);
        pack();
      }
      catch(Exception e) //@B1A
      {
        Trace.log(Trace.ERROR, "Unable to create VSystemPoolModifyDialog.", e); //@B1A
        errorEventSupport_.fireError(e); //@B1A
      }
    }

    /**
     * Removes an error listener.
     *
     * @param listener The listener.
    **/
    public void removeErrorListener(ErrorListener listener)
    {
      if (listener == null) throw new NullPointerException("listener");
      errorEventSupport_.removeErrorListener(listener);
    }

    /**
     * Removes a working listener.
     *
     * @param listener The listener.
    **/
    public void removeWorkingListener(WorkingListener listener)
    {
      if (listener == null) throw new NullPointerException("listener");
      workingEventSupport_.removeWorkingListener(listener);
    }


    /**
     * Returns if the changes are set successfully.
     *
     * @return If the changes are set successfully.
    **/
    private boolean setChanges()
    {
      workingEventSupport_.fireStartWorking();
      boolean caching = sysPool_.isCaching();
      sysPool_.setCaching(true);
      try
      {
        String txt;
        
        // System pool size.
        txt = sysPoolNewPoolSiz_.getText().trim();
        if (!txt.equals(same_) && txt.length() > 0)
          sysPool_.setPoolSize(Integer.parseInt(txt));
        
        // Pool activity level.
        if (!isMachinePool_) //@B2A - CPF1165 occurs
        { //@B2A
          txt = sysPoolNewPoolActLev_.getSelectedItem().toString().trim();
          if (!txt.equals(same_) && txt.length() > 0)
            sysPool_.setPoolActivityLevel(Integer.parseInt(txt));
        } //@B2A
        
        // Message logging.
        txt = sysPoolMesLog_.getSelectedItem().toString();
        sysPool_.setMessageLogging(txt.equals(ResourceLoader.getText("DLG_YES"))); // default is "Y", no *SAME  @C0C
        
        // Paging option.
        if (!isMachinePool_) //@B2A - will get CPF1880 if we set this and we're *MACHINE
        {                    //@B2A
          txt = sysPoolPagOpt_.getSelectedItem().toString();
          sysPool_.setPagingOption(txt); // only have 3 choices, all are valid
        }                    //@B2A
          
        // Pool priority.
        if (!isMachinePool_) //@B2A
        { //@B2A
          txt = sysPoolPri_.getSelectedItem().toString().trim();
          if (txt.equals(calc_))
            sysPool_.setPriority(SystemPool.CALCULATE_INT);
          else if (!txt.equals(same_) && txt.length() > 0)
            sysPool_.setPriority(Integer.parseInt(txt));
        } //@B2A
          
        // Minimum pool size.
        txt = sysPoolMinPoolSiz_.getSelectedItem().toString().trim();
        if (txt.equals(calc_))
          sysPool_.setMinimumPoolSize(SystemPool.CALCULATE);
        else if (!txt.equals(same_) && txt.length() > 0)
          sysPool_.setMinimumPoolSize(Float.valueOf(txt).floatValue());
          
        // Maximum pool size.
        txt = sysPoolMaxPoolSiz_.getSelectedItem().toString().trim();
        if (txt.equals(calc_))
          sysPool_.setMaximumPoolSize(SystemPool.CALCULATE);
        else if (!txt.equals(same_) && txt.length() > 0)
          sysPool_.setMaximumPoolSize(Float.valueOf(txt).floatValue());
          
        // Minimum faults.
        txt = sysPoolMinFau_.getSelectedItem().toString().trim();
        if (txt.equals(calc_))
          sysPool_.setMinimumFaults(SystemPool.CALCULATE);
        else if (!txt.equals(same_) && txt.length() > 0)
          sysPool_.setMinimumFaults(Float.valueOf(txt).floatValue());
          
        // Per thread faults.
        txt = sysPoolPerThrFau_.getSelectedItem().toString().trim();
        if (txt.equals(calc_))
          sysPool_.setPerThreadFaults(SystemPool.CALCULATE);
        else if (!txt.equals(same_) && txt.length() > 0)
          sysPool_.setPerThreadFaults(Float.valueOf(txt).floatValue());
          
        // Maximum faults.
        txt = sysPoolMaxFau_.getSelectedItem().toString().trim();
        if (txt.equals(calc_))
          sysPool_.setMaximumFaults(SystemPool.CALCULATE);
        else if (!txt.equals(same_) && txt.length() > 0)
          sysPool_.setMaximumFaults(Float.valueOf(txt).floatValue());
        
        sysPool_.commitCache();
        
        workingEventSupport_.fireStopWorking();
        sysPool_.setCaching(caching); // reset the caching behavior
        sysPool_.refreshCache(); // better reload our information
      }
      catch(Exception e)
      {
        workingEventSupport_.fireStopWorking();
        sysPool_.setCaching(caching); // reset the caching behavior
        sysPool_.refreshCache(); //@B3A better reload our information
        if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Unable to set values in VSystemPoolModifyDialog.", e);
        errorEventSupport_.fireError(e);
        return false;
      }
      return true;
    }
    


    /**
     * Invoked when a potentially long-running unit of work is about to begin.
     *
     * @param event The event.
    **/
    public void startWorking(WorkingEvent event)
    {
        workingEventSupport_.startWorking(event);
    }

    /**
     * Invoked when a potentially long-running unit of work has completed. 
     *
     * @param event The event.
    **/
    public void stopWorking(WorkingEvent event)
    {
        workingEventSupport_.stopWorking(event);
    }
    
    /**
     * The window is activated.
     *
     * @param event The window event.
    **/
    public void windowActivated(WindowEvent e)
    {
        return;
    }

    /**
     * The window is closed.
     *
     * @param event The window event.
    **/
    public void windowClosed(WindowEvent e)
    {
//@B0: This was causing an infinite loop when the window was
//     closed by the cancel button. When the cancel button was
//     pressed, we call dispose(), which in turn, calls this method.
//     Which calls dispose(). Which calls this method. You get the
//     point.
//     Note that clicking the 'X' in the upper right corner to close
//     the window is not affected by this change, since doing that
//     calls windowClosing(), not windowClosed().
//@B0D        if(e.getSource() == this)
//@B0D            dispose();
//@B0D        return;
    }

    /**
     * The window is closing.
     *
     * @param event The window event.
    **/
   public void windowClosing(WindowEvent e)
    {
        if(e.getSource() == this)
            dispose();
        return;
    }

    /**
     * The window is deactivated.
     *
     * @param event The window event.
    **/
    public void windowDeactivated(WindowEvent e)
    {
        return;
    }

    /**
     * The window is deiconified.
     *
     * @param event The window event.
    **/
    public void windowDeiconified(WindowEvent e)
    {
        return;
    }

    /**
     * The window is iconified.
     *
     * @param event The window event.
    **/
    public void   windowIconified(WindowEvent e)
    {
        return;
    }

    /**
     * The window is opened.
     *
     * @param event The window event.
    **/
    public void   windowOpened(WindowEvent e)
    {
        return;
    }

}
