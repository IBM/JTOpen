///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VActionContext.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.CellEditor;
import java.awt.Frame;



/**
The VActionContext interface defines the context within which an
action is performed.  An action context provides various aspects
of a graphical user interface to an action, so that the action can
interact with the user.

<p>An action context is always needed when peforming actions.
Many graphical user interface components provided in this package
provide a getActionContext() method which will return an action
context.

@see VAction
@see VActionAdapter
@see AS400DetailsPane#getActionContext
@see AS400ExplorerPane#getActionContext
@see AS400ListPane#getActionContext
@see AS400TreePane#getActionContext
**/
public interface VActionContext
{



/**
Indicates if certain actions should be confirmed with
the user.  Such actions will likely prompt the user
before continuing.

@return true if certain actions should be confirmed with
        the user; false otherwise.
**/
    public abstract boolean getConfirm ();



/**
Returns the frame.  This is useful for actions that need
to create dialogs.

@return The frame.
**/
    public abstract Frame getFrame ();



/**
Prompts the user to edit the text for a server resource.
This will start an edit session usually within
a graphical component.

@param  object              The object to be edited.
@param  propertyIdentifier  The property identifier of the object which will be edited.
@return                     The cell editor, or null if editing
                            is not allowed.
**/
    public abstract CellEditor startEditing (VObject object,
                                             Object propertyIdentifier);



}
