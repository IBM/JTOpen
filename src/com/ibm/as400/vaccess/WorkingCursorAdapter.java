///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: WorkingCursorAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;



/**
The WorkingCursorAdapter class represents an object that listens
for working events and sets the cursor as needed.  When an event
source starts working, the cursor is changed to the "wait" cursor.
When an event source stops working, the cursor is changed back to the
original cursor.

<p>If multiple start events are fired, then the same number of stop
events must be fired to get the cursor back to its original state.

<p>The component property is used to determine the frame where
the cursor is changed.  If no component is set, or a parent frame
is not available, then the cursor will not be changed.

<p>The following example creates a tree model filled with
the contents of a directory in the integrated file system of an
AS/400.  It will use a WorkingCursorAdapter object to change the
cursor as needed.

<pre>
// Set up the tree model in a JTree.
AS400TreeModel treeModel = new AS400TreeModel ();
JTree tree = new JTree (treeModel);
<br>
// Set up the working cursor adapter.
treeModel.addWorkingListener (new WorkingCursorAdapter (tree));
<br>
// Set up the tree model to contain the contents of
// a directory.
AS400 system = new AS400 ();
VIFSDirectory directory = new VIFSDirectory (system, "/myDirectory");
treeModel.setRoot (directory);
<br>
// Create a frame and add the tree.
JFrame frame = new JFrame ();
frame.getContentPane ().add (directory);
</pre>
**/
//
// Implementation notes:
//
// 1. This sometimes does not work as you might expect because the
//    component does not redraw until after you have already stopped
//    working.  The solution to this is to do the work in a background
//    thread, giving the component a chance to redraw.
//
// 2. We always set the cursor on the enclosing frame, because some
//    components (even thought they are documented as such) do not
//    handle cursors correctly.  If the component is not part of a
//    frame, then we do not need to set it, because the component is
//    not visible anyway.
//
public class WorkingCursorAdapter
implements WorkingListener, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Static data.
    private static final        Cursor      defaultCursor_  = Cursor.getDefaultCursor();                // @C1A
    private static final        Cursor      waitCursor_     = Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR);



    // Properties.
    private                     Component   component_      = null;



    // Private data.
    transient private           Component   disabledComponent_;
    transient private           Frame       frame_;
    transient private           Cursor      originalCursor_;
    transient private           int         startCount_;



/**
Constructs a WorkingCursorAdapter object.
**/
    public WorkingCursorAdapter ()
    {
        initializeTransient ();
    }



/**
Constructs a WorkingCursorAdapter object.

@param  component   The component.
**/
    public WorkingCursorAdapter (Component component)
    {
        if (component == null)
            throw new NullPointerException ("component");

        component_ = component;
        initializeTransient ();
    }



/**
Returns the component that determines the frame for
cursor changes.

@return The component, or null if none has been set.
**/
    public Component getComponent ()
    {
        return component_;
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        disabledComponent_  = null;
        frame_              = null;
        originalCursor_     = null;
        startCount_         = 0;
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
    }



/**
Sets the component that determines the frame for
cursor changes.

@param  component   The component.
**/
    public void setComponent (Component component)
    {
        if (component == null)
            throw new NullPointerException ("component");

        component_ = component;
    }



/**
Processes a start working event.  If the component has
been added to a frame, then this will set the cursor to the
wait cursor.

@param  event   The event.
**/
    public /* @C2D synchronized */ void startWorking (WorkingEvent event)
    {
        Component componentToDisable = null;                                    // @C2A
        Cursor    cursorToSet = null;                                           // @C2A

        synchronized(this) {                                                    // @C2A
            // If this is the first working event, or at least
            // the first one since the component was set, then
            // disable the component to inhibit input while
            // working.
            if ((component_ != null)
                && ((startCount_ == 0) || (disabledComponent_ == null))) {
                disabledComponent_ = component_;
                componentToDisable = disabledComponent_;                            // @C2C
            }
    
            // If this is the first working event, or at least
            // the first one since the component was added to
            // a frame, then set the cursor to a wait cursor.
            if ((startCount_ == 0) || (frame_ == null)) {
                frame_ = VUtilities.getFrame (component_);
                if (frame_ != null) {
                    originalCursor_ = frame_.getCursor ();
                    if (originalCursor_.getType() == waitCursor_.getType())         // @C1A
                        originalCursor_ = defaultCursor_;                           // @C1A
                    cursorToSet = waitCursor_;                                      // @C2C
                }
            }
    
            ++startCount_;
        }                                                                       // @C2A
        
        // Set these outside of the synchronized block to avoid deadlock           @C2A
        // within Swing code.                                                      @C2A
        if (componentToDisable != null)                                         // @C2A
            componentToDisable.setEnabled(false);                               // @C2A
        if (cursorToSet != null)                                                // @C2A
            frame_.setCursor(cursorToSet);                                      // @C2A
    }



/**
Processes a stop working event.  This will set the cursor
back to its previous form.  If there are, however,
multiple calls to startWorking(), the cursor is not changed back
until all starts have matching stops. 

@param  event   The event.
**/
    public /* @C2D synchronized */ void stopWorking (WorkingEvent event)
    {
        Component componentToEnable = null;                                     // @C2A
        Cursor    cursorToSet = null;                                           // @C2A
        Frame     frameToSet = null;                                            // @C2A

        synchronized(this) {                                                    // @C2A
            --startCount_;
    
            // If nobody is working anymore and the cursor
            // had been set to a wait cursor, then set it
            // back to its original.
            if ((startCount_ == 0) && (frame_ != null)) {
                cursorToSet = originalCursor_;                                  // @C2C
                frameToSet = frame_;                                            // @C2A
                frame_ = null;
            }
    
            // If nobody is working anymore and a component was
            // disabled, then re-enable the component.
            if ((startCount_ == 0) && (disabledComponent_ != null)) {
                componentToEnable = disabledComponent_;                         // @C2C
                disabledComponent_ = null;
            }
        }                                                                       // @C2A
        
        // Set these outside of the synchronized block to avoid deadlock           @C2A
        // within Swing code.                                                      @C2A
        if (componentToEnable != null)                                          // @C2A
            componentToEnable.setEnabled(true);                                 // @C2A
        if ((cursorToSet != null) && (frameToSet != null))                      // @C2A
            frameToSet.setCursor(cursorToSet);                                  // @C2A
    }



}

