///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: TableModelEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.Vector;



/**
The TableModelEventSupport class represents a list of
TableModelListeners.  This is also a TableModelListener and
will dispatch all table model events.
**/
class TableModelEventSupport
implements TableModelListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private             TableModel              actualSource_   = null;
    private transient   TableModelListener[]    listeners_      = new TableModelListener[0]; // For speed.
    private transient   Vector                  listenersV_     = new Vector ();
    private             Object                  source_;



/**
Constructs a TableModelEventSupport object.

@param  source          The source of the events.
@param  actualSource    The actual source of the events.  This is
                        necessary because only table models
                        can be the event source.
**/
    public TableModelEventSupport (Object source,
                                   TableModel actualSource)
    {
        source_ = source;
        actualSource_ = actualSource;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addTableModelListener (TableModelListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new TableModelListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



// @C1A
/**
Fires a table changed event.
**/
    public void fireTableChanged()
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].tableChanged (new TableModelEvent (actualSource_));
        }
    }



/**
Fires a table changed event.

@param  rowIndex            The row index.
**/
    public void fireTableChanged (int rowIndex)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].tableChanged (new TableModelEvent (actualSource_, rowIndex));
        }
    }



/**
Fires a table changed event.

@param  firstRowIndex            The first row index.
@param  lastRowIndex             The last row index.
@param  columnIndex              The column index.
@param  type                     The type.
**/
    public void fireTableChanged (int firstRowIndex,
                                  int lastRowIndex,
                                  int columnIndex,
                                  int type)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].tableChanged (new TableModelEvent (actualSource_,
                    firstRowIndex, lastRowIndex, columnIndex, type));
        }
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeTableModelListener (TableModelListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new TableModelListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }


/**
Processes a table changed event.

@param  event       The event.
**/
    public void tableChanged (TableModelEvent event)
    {
        fireTableChanged (event.getFirstRow (), event.getLastRow (),
            event.getColumn (), event.getType ());
    }



}



