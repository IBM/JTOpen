///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VSystemStatusPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;


import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.SystemStatus;
import com.ibm.as400.access.SystemPool;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ConnectionDroppedException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;


import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import java.beans.PropertyVetoException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * The VSystemStatusPane class represents a visual pane which shows
 * the system status information. 
**/
public class  VSystemStatusPane extends JPanel implements Serializable //@B0C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   AS400DetailsPane detailsPane_;
   private AS400 system_;
   private SystemStatus systemStatus_;
   private VSystemStatus vSystemStatus_;
   private JLabel systemLabel_=new JLabel();
   private JLabel dateTimeLabel_=new JLabel();
   private JLabel utilizationLabel_=new JLabel();
   private JLabel systemASPLabel_=new JLabel();
   private JLabel systemASPUsedLabel_=new JLabel();
   private JLabel totalAuxiliaryStorageLabel_=new JLabel();
   private JLabel jobsInSystemLabel_=new JLabel();
   private JLabel batchJobsRunningLabel_=new JLabel();
   private JLabel usersCurrentlySignedOnLabel_=new JLabel();
   private boolean allowModifyAllPools_ = false;

   // Static data.
   private static DateFormat dateFormat_ = DateFormat.getDateInstance();
   private static DateFormat timeFormat_ = DateFormat.getTimeInstance();

   // MRI start.
   private static final String systemStatusText_  
   = ResourceLoader.getText ("SYSTEM_STATUS_SYSTEM_STATUS");
   private static final String systemText_ 
   = ResourceLoader.getText ("SYSTEM_STATUS_SYSTEM")+":";
   private static final String datetimeText_  
   = ResourceLoader.getText ("SYSTEM_STATUS_DATE_TIME")+":";
   private static final String cpuText_  
   = ResourceLoader.getText ("SYSTEM_STATUS_CPU");
   private static final String utilizationText_  
   =  "     " + ResourceLoader.getText ("SYSTEM_STATUS_UTILIZATION")+":";
   private static final String auxiliaryStorageText_ 
   = ResourceLoader.getText ("SYSTEM_STATUS_AUXILIARY_STORAGE");
   private static final String systemAspText_   
   =  "     " + ResourceLoader.getText ("SYSTEM_STATUS_SYSTEM_ASP")+":";
   private static final String systemAspUsedText_ 
   =  "     " + ResourceLoader.getText ("SYSTEM_STATUS_SYSTEM_ASP_USED")+":";
   private static final String totalAuxiliaryStorageText_ 
   =  "     " + ResourceLoader.getText ("SYSTEM_STATUS_TOTAL_AUXILIARY_STORAGE")+":";
   private static final String jobsText_  
   = ResourceLoader.getText ("SYSTEM_STATUS_JOBS");
   private static final String jobsInSystemText_   
   =  "     " + ResourceLoader.getText ("SYSTEM_STATUS_JOBS_IN_SYSTEM")+":";
   private static final String batchJobsRunningText_   
   =  "     " + ResourceLoader.getText ("SYSTEM_STATUS_BATCH_JOBS_RUNNING")+":";
   private static final String usersText_    
   = ResourceLoader.getText ("SYSTEM_STATUS_USERS");
   private static final String usersCurrentlySignedOnText_
   =  "     " + ResourceLoader.getText ("SYSTEM_STATUS_USERS_CURRENTLY_SIGNED_ON")+":";
   private static final String storagePoolsText_ 
   = ResourceLoader.getText ("SYSTEM_STATUS_STORAGE_POOLS");
   //MRI end

   private transient ErrorEventSupport errorEventSupport_;
   private transient ListSelectionEventSupport listSelectionEventSupport_;
   private transient PropertyChangeSupport propertyChangeSupport_;

   private transient VetoableChangeSupport vetoableChangeSupport_;

   private transient WorkingEventSupport workingEventSupport_; //@B0A

   /**
    * Constructs a VSystemStatusPane object.
    *
    * @param system The AS/400 system in which the system status information
    *               resides.
   **/
   public VSystemStatusPane(AS400 system)
   {
      if (system == null)
         throw new NullPointerException("system");
      system_ = system;

      GridBagLayout panelLayout = new GridBagLayout();
      setLayout(panelLayout);
      setBorder(new EmptyBorder(10,10,10,10));

      GridBagConstraints gbc = new GridBagConstraints(); //@B0A
      VUtilities.constrain (getPanel0(), //@B0C
                            this, panelLayout, gbc, //@B0C
                            0, 1, 1,  0, 1, 0, //@B0A
                            GridBagConstraints.NONE, //@B0A
                            GridBagConstraints.CENTER); //@B0A
      VUtilities.constrain (getPanel1(), //@B0C
                            this, panelLayout, gbc, //@B0C
                            0, 1, 1,  1, 1, 0, //@B0A
                            GridBagConstraints.NONE, //@B0A
                            GridBagConstraints.CENTER); //@B0A
      VUtilities.constrain (new JLabel(storagePoolsText_),
                            this, panelLayout, gbc, //@B0C
                            0, 1, 1,  2, 1, 0, //@B0A
                            GridBagConstraints.NONE, //@B0A
                            GridBagConstraints.WEST); //@B0A
      VUtilities.constrain (getPanel2(), //@B0C
                            this, panelLayout, gbc, //@B0C
                            0, 1, 1,  3, 1, 1, //@B0A
                            GridBagConstraints.BOTH, //@B0A
                            GridBagConstraints.CENTER); //@B0A

      initializeTransient();
   }

   /**
    * Adds a listener to be notified when an error occurs.
    *
    * @param listener The listener.
   **/
   public void addErrorListener( ErrorListener listener )
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      errorEventSupport_.addErrorListener(listener); 
   }

   /**
    * Adds a listener to be notified when a list selection occurs in the
    * details pane.
    *
    * @param  listener  The listener.
   **/
   public void addListSelectionListener( ListSelectionListener listener )
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      listSelectionEventSupport_.addListSelectionListener(listener); 
   }

   /**
    * Adds a listener to be notified when the value of any bound property
    * changes.
    *
    * @param  listener  The listener.
   **/
   public void addPropertyChangeListener( PropertyChangeListener listener )
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A

      propertyChangeSupport_.addPropertyChangeListener(listener); 
   }

   /**
    * Adds a listener to be notified when the value of any constrained
    * property changes.
    *
    * @param  listener  The listener.
   **/
   public void addVetoableChangeListener( VetoableChangeListener listener )
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A

      vetoableChangeSupport_.addVetoableChangeListener(listener); 
   }


   /**    
    * Returns the context in which actions will be performed.
    *
    * @return  The context.
   **/
   public VActionContext getActionContext()
   {
      return detailsPane_.getActionContext(); 
   }

   /**
    * Returns the value indicating if actions can be invoked on objects.
    *
    * @return true if actions can be invoked; false otherwise. 
   **/
   public boolean getAllowActions()
   {
      return detailsPane_.getAllowActions(); 
   }

   /**
    * Returns the column model that is used to maintain the columns
    * of the details.
    *
    * @return The column model.
   **/
   public TableColumnModel getDetailsColumnModel()
   {
      return detailsPane_.getColumnModel(); 
   }

   /**
    * Returns the details model.
    *
    * @return The details model.
   **/
   public TableModel getDetailsModel()
   {
      return detailsPane_.getModel(); 
   }

   /**    
    * Returns the selection model that is used to maintain selection state
    * in the details. 
    *
    * @return The selection model, or null if selections are not allowed.
   **/
   public ListSelectionModel getDetailsSelectionModel()
   {
      return detailsPane_.getSelectionModel(); 
   }

   /**
    * Returns a title panel of the system status.
    *
    * @return The panel of the title of system status.
   **/
   private JPanel getPanel0()
   {
      JPanel panel0= new JPanel();
      panel0.add(new JLabel(systemStatusText_));
      return panel0;
   }

   /**
    * Returns a details panel of the system status.
    *
    * @return The panel of the system status.
   **/
   private JPanel getPanel1()
   {
      JPanel panel1= new JPanel();
      GridBagLayout layout = new GridBagLayout ();
      GridBagConstraints constraints;
      panel1.setLayout (layout);

      int row=0;
      VUtilities.constrain (new JLabel(systemText_), 
                            systemLabel_,
                            panel1, layout, row++);
      VUtilities.constrain (new JLabel(datetimeText_), 
                            dateTimeLabel_,
                            panel1, layout, row++);
      VUtilities.constrain (new JLabel(cpuText_),
                            panel1, layout, row++);                 
      VUtilities.constrain (new JLabel(utilizationText_), 
                            utilizationLabel_,
                            panel1, layout, row++);      
      VUtilities.constrain (new JLabel(auxiliaryStorageText_),
                            panel1, layout, row++);                 
      VUtilities.constrain (new JLabel(systemAspText_), 
                            systemASPLabel_,
                            panel1, layout, row++); 
      VUtilities.constrain (new JLabel(systemAspUsedText_), 
                            systemASPUsedLabel_,
                            panel1, layout, row++);   
      VUtilities.constrain (new JLabel(totalAuxiliaryStorageText_), 
                            totalAuxiliaryStorageLabel_,
                            panel1, layout, row++);   
      VUtilities.constrain (new JLabel(jobsText_),
                            panel1, layout, row++);                 
      VUtilities.constrain (new JLabel(jobsInSystemText_), 
                            jobsInSystemLabel_,
                            panel1, layout, row++); 
      VUtilities.constrain (new JLabel(batchJobsRunningText_), 
                            batchJobsRunningLabel_,
                            panel1, layout, row++);    
      VUtilities.constrain (new JLabel(usersText_),
                            panel1, layout, row++);                 
      VUtilities.constrain (new JLabel(usersCurrentlySignedOnText_), 
                            usersCurrentlySignedOnLabel_,
                            panel1, layout, row++);  
      return panel1;                 
   } 

   /**
    * Returns a details panel of the system pools.
    *
    * @return The panel of the system pools.
   **/
   private JPanel getPanel2()
   {
      JPanel panel2 = new JPanel();

      detailsPane_= new AS400DetailsPane();

      panel2.setLayout(new BorderLayout()); //@B0A
      panel2.add("Center", detailsPane_); //@B0A
      return panel2;
   } 


   /**
    * Returns the first selected object.
    *
    * @return The first selected object. 
   **/
   public VObject getSelectedObject()
   {
      VObject vObjec1= detailsPane_.getSelectedObject(); 

      return vObjec1; 
   }

   /**
    * Returns the selected objects.
    *
    * @return The selected objects. 
   **/
   public VObject[] getSelectedObjects()
   {
      VObject[] vObjec1= detailsPane_.getSelectedObjects(); 

      return vObjec1; 
   }

   /**
    * Returns the VSystemStatus contained in VSystemStatusPane.
    *
    * @return The VSystemStatus contained in VSystemStatusPane. 
   **/
   public VSystemStatus getVSystemStatus()
   {
      return vSystemStatus_; 
   }

   /**
    * Initializes the transient data.
   **/
   private void initializeTransient()
   {
      errorEventSupport_= new ErrorEventSupport(this); 
      listSelectionEventSupport_= new ListSelectionEventSupport(this); 
      propertyChangeSupport_= new PropertyChangeSupport(this); 

      vetoableChangeSupport_= new VetoableChangeSupport(this); 
      workingEventSupport_ = new WorkingEventSupport(this); //@B0A

      detailsPane_.addErrorListener(((ErrorListener)errorEventSupport_)); 
      detailsPane_.addListSelectionListener(((ListSelectionListener)listSelectionEventSupport_)); 
      detailsPane_.addPropertyChangeListener(((PropertyChangeListener)propertyChangeSupport_)); 
      detailsPane_.addVetoableChangeListener(((VetoableChangeListener)vetoableChangeSupport_)); 
   }

   /**
    * Return true if the modify action to all system pools is allowed; false otherwise.
    * 
    * @return true if the modify action to all system pools is allowed; false otherwise.
   **/

   public boolean isAllowModifyAllPools()
   {
      return allowModifyAllPools_;
   }

   /**
    * Loads system status information from the AS/400.
   **/
   public void load()

   {
      workingEventSupport_.fireStartWorking(); //@B0A
      if (Trace.isTraceOn() && Trace.isTraceInformationOn()) //@B0C
         Trace.log(Trace.INFORMATION, "VSystemStatusPane: loading"); //@B0C
      try
      {
         if (systemStatus_ == null) //@B0A
            systemStatus_ = new SystemStatus(system_); //@B0A
         else //@B0A
            systemStatus_.refreshCache(); //@B0C //@B0C - 06/17/1999
         for (Enumeration e=systemStatus_.getSystemPools();e.hasMoreElements();)
         {
            ((SystemPool)e.nextElement()).addPropertyChangeListener(propertyChangeSupport_); //@B0C
         }
      }
      catch (Exception ex)
      {
         Trace.log(Trace.ERROR, ex.toString());
         errorEventSupport_.fireError(ex);
      }

      try
      {

         if (vSystemStatus_ == null) //@B0A
            vSystemStatus_ = new VSystemStatus(systemStatus_); //@B0A
         else //@B0A
            vSystemStatus_.load(); //@B0A
      }
      catch (Exception ex)
      {
         Trace.log(Trace.ERROR, ex.toString());
         errorEventSupport_.fireError(ex);
      }

      vSystemStatus_.setAllowModifyAllPools(allowModifyAllPools_);

      systemLabel_.setText(systemStatus_.getSystem().getSystemName());

      try //@B0A
      {
         //@B0A
         Date date = systemStatus_.getDateAndTimeStatusGathered();

         String dateStr = dateFormat_.format(date);
         String timeStr = timeFormat_.format(date);
         dateTimeLabel_.setText(dateStr+"  "+timeStr);   

         utilizationLabel_.setText(Float.toString(systemStatus_.getPercentProcessingUnitUsed()));
         systemASPLabel_.setText(Integer.toString(systemStatus_.getSystemASP()));
         systemASPUsedLabel_.setText(Float.toString(systemStatus_.getPercentSystemASPUsed()));
         totalAuxiliaryStorageLabel_.setText(Integer.toString(systemStatus_.getTotalAuxiliaryStorage()));
         jobsInSystemLabel_.setText(Integer.toString(systemStatus_.getJobsInSystem()));
         batchJobsRunningLabel_.setText(Integer.toString(systemStatus_.getBatchJobsRunning()));
         usersCurrentlySignedOnLabel_.setText(Integer.toString(systemStatus_.getUsersCurrentSignedOn()));

      }   //@B0A
      catch (Exception e) //@B0A
      {
         //@B0A
         if (Trace.isTraceOn() && Trace.isTraceErrorOn()) //@B0A
            Trace.log(Trace.ERROR, e);                     //@B0A
         errorEventSupport_.fireError(e);                 //@B0A
      }                                                  //@B0A

      try
      {
         detailsPane_.setRoot(vSystemStatus_); 
      }
      catch (java.beans.PropertyVetoException PropertyVetoException0)
      {
      }
      detailsPane_.load(); 

      if (Trace.isTraceOn() && Trace.isTraceInformationOn()) //@B0A
         Trace.log(Trace.INFORMATION, "VSystemStatusPane: load finished"); //@B0C
      workingEventSupport_.fireStopWorking(); //@B0A
   }


   /**
    * Removes an error listener.
    *
    * @param listener The listener.
   **/
   public void removeErrorListener( ErrorListener listener )
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      errorEventSupport_.removeErrorListener(listener); 
   }

   /**
    * Removes a list selection listener.
    *
    * @param listener The listener.
   **/
   public void removeListSelectionListener( ListSelectionListener listener )
   {
      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      listSelectionEventSupport_.removeListSelectionListener(listener); 
   }

   /**
    * Removes a property change listener.
    *
    * @param listener The listener.
   **/
   public void removePropertyChangeListener( PropertyChangeListener listener )
   {
      if (listener == null)                         //@B0A
         throw new NullPointerException("listener"); //@B0A

      propertyChangeSupport_.removePropertyChangeListener(listener); 
   }

   /**
    * Removes a vetoable change listener.
    *
    * @param listener The listener.
   **/
   public void removeVetoableChangeListener( VetoableChangeListener listener )
   {

      if (listener == null) //@B0A
         throw new NullPointerException("listener"); //@B0A
      vetoableChangeSupport_.removeVetoableChangeListener(listener ); 
   }


   /**
    * Sets the value indicating whether actions are allowed. 
    *
    * @param allowAction true if actions are allowed; false otherwise.
   **/
   public void setAllowActions( boolean allowAction )
   {
      detailsPane_.setAllowActions(allowAction); 
   }
   /**
    * Set the state of modify action of all the pools.
    *
    * @param allow The boolean value.
   **/
   public void setAllowModifyAllPools(boolean allow)
   {
      allowModifyAllPools_ = allow;
   }

   /**
    * Sets the value indicating whether certain actions are confirmed
    * with the user. 
    *
    * @param confirm true if certain actions are confirmed with the user;
    *                false otherwise. 
   **/
   public void setConfirm( boolean confirm )
   {
      detailsPane_.setConfirm(confirm); 
   }

   /**
    * Sets the selection model that is used to maintain selection state
    * in the details. 
    *
    * @param listSe1 The selection model, or null if selections are not allowed.
   **/
   public void setDetailsSelectionModel( ListSelectionModel listSe1 )
   {
      detailsPane_.setSelectionModel(listSe1); 
   }


   /**
    * Sets the root, or the AS/400 resource, from which all information
    * for the model is gathered.
    *
    * @param root The root, or the AS/400 resource, from which all
    *              information for the model is gathered. 
    * @exception PropertyVetoException If the proposed change to root
    *            is unacceptable.
   **/
   public void setRoot( VNode root )
   throws PropertyVetoException
   {
      if (root!=null)
         detailsPane_.setRoot(root);
      else
         throw new NullPointerException("root"); 
   }

   /**
    * Sorts the contents. 
    *
    * @param propertyIdentifiers The property identifiers. 
    * @param orders The sort orders for each property identifier.
   **/
   public void sort( Object[] propertyIdentifiers, boolean[] orders )
   {
      detailsPane_.sort(propertyIdentifiers, orders); 
   }

}
