///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: LayoutFormPanel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;

import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeSupport;

/**
*  The LayoutFormPanel class represents a layout of HTML form elements.
*
*  <p>LayoutFormPanel objects generate the following events:
*  <UL>
*  <LI><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*    <li>elementAdded
*    <li>elementRemoved
*    </ul>
*  </UL>
**/
public abstract class LayoutFormPanel implements HTMLTagElement, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private Vector list;         // The list of form elements.
    private int cols;            // The number of columns in the layout.


    transient PropertyChangeSupport changes_; //@CRS
    transient VetoableChangeSupport vetos_; //@CRS
    transient Vector elementListeners_;     // The list of element listeners. @CRS

    /**
    *  Constructs a default LayoutFormPanel.
    **/
    public LayoutFormPanel()
    {
        list = new Vector();
    }

    /**
    *  Adds a form <i>element</i> to the panel.
    *  @param element The form element.
    **/
    public void addElement(HTMLTagElement element)
    {
        //@B1D

        if (element == null)
            throw new NullPointerException("element");

        list.addElement(element);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }

    /**
    *  Adds an ElementListener.
    *  The ElementListener object is added to an internal list of ElementListeners;
    *  it can be removed with removeElementListener.
    *    @see #removeElementListener
    *    @param listener The ElementListener.
    **/
    public void addElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (elementListeners_ == null) elementListeners_ = new Vector(); //@CRS
        elementListeners_.addElement(listener);
    }

    /**
    *  Fires the element event.
    **/
    private void fireElementEvent(int evt)
    {
      if (elementListeners_ == null) return; //@CRS
        Vector targets;
        targets = (Vector) elementListeners_.clone();
        ElementEvent elementEvt = new ElementEvent(this, evt);
        for (int i = 0; i < targets.size(); i++)
        {
            ElementListener target = (ElementListener)targets.elementAt(i);
            if (evt == ElementEvent.ELEMENT_ADDED)
                target.elementAdded(elementEvt);
            else if (evt == ElementEvent.ELEMENT_REMOVED)
                target.elementRemoved(elementEvt);
        }
    }

    /**
    *  Returns the form element at the specified <i>index</i> in the layout.
    *  @param index The index of the form element.
    *  @return The form element.
    **/
    HTMLTagElement getElement(int index)
    {
        return(HTMLTagElement)list.elementAt(index);
    }

    /**
    *  Returns the number of elements in the layout.
    *  @return The number of elements.
    **/
    public int getSize()
    {
        return list.size();
    }


    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)          //$A1A
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        //@CRS changes_ = new PropertyChangeSupport(this);
        //@CRS vetos_ = new VetoableChangeSupport(this);
        //@CRS elementListeners_ = new Vector();
    }

    /**
    *  Removes a form <i>element</i> from the panel.
    *  @param element The form element.
    **/
    public void removeElement(HTMLTagElement element)
    {
        //@B1D

        if (element == null)
            throw new NullPointerException("element");

        if (list.removeElement(element))
            fireElementEvent(ElementEvent.ELEMENT_REMOVED);
    }

    /**
    *  Removes this ElementListener from the internal list.
    *  If the ElementListener is not on the list, nothing is done.
    *  @see #addElementListener
    *  @param listener The ElementListener.
    **/
    public void removeElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (elementListeners_ != null) elementListeners_.removeElement(listener); //@CRS
    }

    /**
    *  Returns a String representation of the panel tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
