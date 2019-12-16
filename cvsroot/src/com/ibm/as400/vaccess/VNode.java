///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VNode.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;


/**
The VNode interface defines the representation of an
system resource that exists in a hierarchy.

<p>In the hierarchy, a node has exactly one parent and
any number of children.  Note that there may be a different
set of children for the "tree" hierarchy than those represented
in the "details" hierarchy.  The tree hierarchy is for displaying
in tree views, while the details hierarchy is for list and details
views.

<p>Many of these methods are not called directly by
programs.  Instead, they are called by the server panes
to respond to the user interface as needed.
                 
<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VNode objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

<p>An implementation of this interface should pass on all
events fired by its children to its listeners.
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
// @A1C - javadoc
public interface VNode
extends VObject, TreeNode
{



/**
Returns the table column model to use in the details
when representing the children.  Each of the columns
in this column model has an identifier that identifies
the property with which it is associated.  The property
values of the children are then displayed in that column.

<p>This is called on the root of an AS400DetailsPane
or AS400DetailsModel to determine the structure of the 
table.

@return The details column model, or null if there is none.

@see VObject#getPropertyValue
**/
// @A1C - javadoc
    abstract public TableColumnModel getDetailsColumnModel ();



/**
Returns the child for the details at the specified index.
This is called in order to determine the list of children
to be displayed in an AS400DetailsPane, AS400DetailsModel,
AS400ListPane, or AS400ListModel.

@param  index   The index.
@return         The child, or null if the index is not valid.
**/
// @A1C - javadoc
    abstract public VObject getDetailsChildAt (int index);



/**
Returns the number of children for the details.
This is called in order to determine the list of children
to be displayed in an AS400DetailsPane, AS400DetailsModel,
AS400ListPane, or AS400ListModel.

@return  The number of children for the details.
**/
// @A1C - javadoc
    abstract public int getDetailsChildCount ();



/**
Returns the index of the specified child for the details.
This is called in order to determine the list of children
to be displayed in an AS400DetailsPane, AS400DetailsModel,
AS400ListPane, or AS400ListModel.

@param  detailsChild   The details child.
@return                The index, or -1 if the child is not
                       found in the details.
**/
// @A1C - javadoc
    abstract public int getDetailsIndex (VObject detailsChild);



/**
Indicates if the details children are sortable.  An
implementation that contains a large number of children or
children that are not all loaded from the system at once
may not want its' children to be sortable.

@return true if the details children are sortable; false
        otherwise.
**/
    abstract public boolean isSortable ();



/**
Sorts the children for the details. The propertyIdentifer[0], 
orders[0] combination  is used to do the sort. If the values 
are equal, propertyIdentifier[1], orders[1]  is used to break 
the tie, and so forth.

<p>An implementation that contains a large number of children
or children that are not all loaded from the system at once may
not want to allow sorting of its children.  In that case, this
method will have no effect.

@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            means to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier. true for ascending order;
                            false for descending order.
**/
    abstract public void sortDetailsChildren (Object[] propertyIdentifiers,
                                              boolean[] orders);



}


