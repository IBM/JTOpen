///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DialogCache.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;
  
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import javax.swing.JDialog;


/**
The DialogCache class maintains a Hashtable of dialogs, each
associated with a particular target object.  This object can
help a GUI reuse dialogs, so that at most one GUI will appear,
even if the end-user selects the same target multiple times.
**/
class DialogCache
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static Hashtable    dialogTable_    = new Hashtable();



/**
Resolves the dialog for a particular target object.

@param target   The target object.
@return         The dialog, or null if there is none.
**/
    public JDialog resolveDialog(Object target)
    {
        synchronized(dialogTable_) {

            // If there is already a dialog for this resource,
            // the use it.
            if (dialogTable_.containsKey(target)) {               
                JDialog dialog = (JDialog)dialogTable_.get(target);
                dialog.toFront();
                dialog.requestFocus();
                return dialog;
            }

            else
                return null;
        }
    }



/**
Adds a new dialog to the cache.

@param target   The target object.
@param dialog   The dialog.
**/
    public void addDialog(Object target, JDialog dialog)
    {
        synchronized(dialogTable_) {
            // Add a listener to the dialog, so that when it closes,
            // it will remove itself from the cache.
            dialog.addWindowListener(new WindowListener_(target));

            // Add the new one to our table and return it.
            dialogTable_.put(target, dialog);
        }
    }



/**
Removes dialogs from the cache when they are closed.
**/
    private class WindowListener_ extends WindowAdapter
    {
        private Object target_;

        public WindowListener_(Object target)
        {
            target_ = target;
        }

        public void windowClosing(WindowEvent event)
        {
            synchronized(dialogTable_) {
                if (dialogTable_.containsKey(target_))
                    dialogTable_.remove(target_);
            }
        }

        public void windowClosed(WindowEvent event)
        {
            synchronized(dialogTable_) {
                if (dialogTable_.containsKey(target_))
                    dialogTable_.remove(target_);
            }
        }
    }


}
