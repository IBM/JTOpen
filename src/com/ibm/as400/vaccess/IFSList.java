///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.*;
import java.util.*;
import java.beans.*;
import com.ibm.as400.access.*;
import java.io.IOException;

/**
  * The IFSList class represents a
  *  AWT list class extension that can display a list of files, directories
  *  or both from a server.
  *
  *  Error events are generated if errors are encountered.
  *
  *  @see com.ibm.as400.access.ErrorEvent
  *  @see com.ibm.as400.access.ErrorListener
  *  @see com.ibm.as400.access.IFSFile
  *  @see com.ibm.as400.access.IFSFileFilter
  *  @see java.awt.List
  **/
class IFSList extends java.awt.List
                     implements IFSFileFilter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private AS400  sys_;
    private String path_ = "/";
    private String filter_ = "*";
    private boolean sort_ = true;
    private IFSFile ifsFile_;
    private int directory_ = DIRECTORYONLY;
    private Vector fList;
    private Vector actionEventList = new Vector();
    private Vector errorEventList = new Vector();
    private PropertyChangeSupport propertyList = new PropertyChangeSupport(this);

    /**
      * Display directory only.
      **/
    public static final int DIRECTORYONLY = 1;
    /**
      * Display files only.
      **/
    public static final int FILEONLY = 2;
    /**
      * Display files and directories.
      **/
    public static final int BOTH = 3;

    /**
      * Constructs an IFSList object.  It is a default list object.  If the system is not set,
      * the user will be prompted for system name and signon information.
      **/
    public IFSList()
    {
        super();
    }

    /**
      * Receives file information from the file classes
      * during processing.
      *
      * This method should not be called by the application.
      * @param file The file to process.
      * @return true if the file should be added to the list; false otherwise.
      **/
    public boolean accept(IFSFile file)
    {
        boolean fAccept = false;
        int i;

        try
        {
            switch (directory_)
            {
                case 1:
                    if (file.isDirectory())
                    {
                        fAccept = true;
                    }
                    else
                    {
                        fAccept = false;
                    }
                    break;
                case 2:
                    if (file.isFile())
                    {
                        fAccept = true;
                    }
                    else
                    {
                        fAccept = false;
                    }
                    break;
                case 3:
                    fAccept = true;
                    break;
                default:
                    fAccept = false;
                    break;
            }
        }
        catch (IOException e)
        {
            // fire error event
            fireError(e, "accept");
            return false;
        }

        if (fAccept)
        {
            if (sort_)
            {
                String temp;

                for (i=0; i<fList.size(); i++)
                {
                    temp = (String)(fList.elementAt(i));
                    if (temp.toUpperCase().compareTo(file.getName().toUpperCase()) > 0)
                    {
                        fList.insertElementAt(file.getName(), i);
                        break;
                    }
                }
                if (i == fList.size())
                {
                    fList.addElement(file.getName());
                }
            }
            else
            {
                add(file.getName());
            }
        }

        return fAccept;
    }

    /**
      *Adds a listener to the action completed event list.
      *
      *@param listener The listener object.
      **/
    public synchronized void addActionCompletedListener(ActionCompletedListener listener)
    {
        actionEventList.addElement(listener);
    }

    /**
      *Adds a listener to the error event list.
      *
      *@param listener The listener object.
      **/
    public synchronized void addErrorListener(ErrorListener listener)
    {
        errorEventList.addElement(listener);
    }

    /**
      *Adds a property changed listener to the list
      *
      *@param listener The listener to add.
      **/
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyList.addPropertyChangeListener(listener);
    }

    private void fireAction(String sourceMethod)
    {
        Vector targets;
        synchronized (this)
        {
            targets = (Vector) actionEventList.clone();
        }
        ActionCompletedEvent actionEvt = new ActionCompletedEvent(this);
        for (int i = 0; i < targets.size(); i++)
        {
            ActionCompletedListener target = (ActionCompletedListener)targets.elementAt(i);
            target.actionCompleted(actionEvt);
        }
    }

    private void fireError(Exception e, String sourceMethod)
    {
        Vector targets;
        synchronized (this)
        {
            targets = (Vector) errorEventList.clone();
        }
        ErrorEvent errorEvt = new ErrorEvent(this, e);
        for (int i = 0; i < targets.size(); i++)
        {
            ErrorListener target = (ErrorListener)targets.elementAt(i);
            target.errorOccurred(errorEvt);
        }
    }

    /**
      * Returns the filter to be used with the list.
      * @return The filter to use for the list.
      **/
    public String getFilter()
    {
        return filter_;
    }

    /**
      * Returns the directory to be displayed.
      * @return The directory path to be displayed.
      **/
    public String getPath()
    {
        return path_;
    }

    /**
      * Returns if sorting is done for the list.
      * @return true if list should be sorted; false otherwise.
      **/
    public boolean isSort()
    {
        return sort_;
    }

    /**
      * Returns what should be displayed in the list:  File only, directory
      * only or both.
      * @return The value that represents the list property.
      **/
    public int getListType()
    {
        return directory_;
    }

    /**
      * Returns the system object that represents the server to list.
      * @return The system object that represents the server to list.
      **/
    public AS400 getSystem()
    {
        return sys_;
    }

    /**
      * Fills the list with the requested information.  If the system or
      * system name is not set, the user will be prompted for it.  The
      * list is cleared and repopulated each time this method is called.
      *
      *
      **/
    public void populateList()
        throws IOException
    {
        setVisible(false);
        removeAll();
        setVisible(true);

        if (sys_ == null)
        {
            sys_ = new AS400();
        }

        if (ifsFile_ == null)
        {
            ifsFile_ = new IFSFile(sys_, path_);
        }

//        try
//        {
            if ((directory_ == DIRECTORYONLY) || (directory_ == BOTH))
            {
                add(".");
                add("..");
            }

            fList = new Vector();

            if (filter_ != null)
            {
               ifsFile_.list(this, filter_);
            }
            else
            {
               ifsFile_.list(this);
            }

            for (int i=0; i<fList.size(); i++)
            {
                add((String)(fList.elementAt(i)));
            }

            fireAction("populateList");

//        }
//        catch (Exception e)
//        {
//            // throw error event
//            fireError(e, "populateList");
//        }
    }

    /**
      * Sets the filter for the list.
      *
      * @param filter The filter to use for the list.  If the filter is
      *               not set, then "*" is used as the filter.
      **/
    public void setFilter(String filter)
    {
        String old = filter_;

        if (filter.compareTo("*.*") == 0)
        {
            filter_ = new String("*");
        }
        else
        {
            filter_ = filter;
        }

        propertyList.firePropertyChange("filter", old, filter_);
    }

    /**
      * Sets the list type (directory only, file only or both).
      *
      * @param listType The type to list.
      **/
    public void setListType(int listType)
    {
        Integer old = new Integer(directory_);

        directory_ = listType;

        propertyList.firePropertyChange("listType", old, new Integer(listType));
    }

    /**
      * Sets the path to display.
      *
      * @param path The directory path to list.
      **/
    public void setPath(String path)
    {
        String old = path_;

        path_ = path;
        ifsFile_ = null;

        propertyList.firePropertyChange("path", old, path_);
    }

    /**
      * Sets the sort property of the list.
      *
      * @param sort  true if the list should be sorted; false otherwise.
      **/
    public void setSort(boolean sort)
    {
        Boolean old = new Boolean(sort_);

        sort_ = sort;

        propertyList.firePropertyChange("sort", old, new Boolean(sort_));
    }

    /**
      * Sets the system to list.
      *
      * @param system The system.
      **/
    public void setSystem(AS400 system)
    {
        AS400 old = sys_;

        sys_ = system;

        propertyList.firePropertyChange("system", old, sys_);
    }


}
