///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Point;
import java.beans.PropertyVetoException;



/**
The VPane interface defines information about and access
to the various panes in this package.
**/
//
// Implementation note:
//
// The reasons for this class are:
//
// 1.  To encapsulate all common interface stuff from the related
//     panes.  Originally, I just had all of the panes implement
//     this class.  But since this commonality is really only
//     convenient from an internal implementation point of view,
//     I did not want to make this public.  I felt that that would
//     would show that there is some cosmic connection between
//     trees, details, etc., when there really is not.
//
interface VPane
{



/**
Returns the root.

@return The root, or null if none.
**/
    public abstract VNode getRoot ();



/**
Returns the object at a given point on the pane.

@param point    The point.
@return         The object, or null if there is none.
**/
    public abstract VObject getObjectAt (Point point);



/**
Sets the root.

@param root The root, or null if none.
**/
    public abstract void setRoot (VNode root)
        throws PropertyVetoException;



}


