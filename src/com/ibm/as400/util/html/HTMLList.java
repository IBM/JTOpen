///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalArgumentException;


import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;


/**
*  The HTMLList class represents a list.  The list can either be an ordered
*  list &lt;ol&gt; or an unordered list &lt;ul&gt;.
*
*  <p>HTMLList objects generate the following events:
*  <ul>
*  <LI><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*    <li>elementAdded
*    <li>elementRemoved
*    </ul>
*  <li>PropertyChangeEvent
*  </ul>
*  <P>
**/
public abstract class HTMLList extends HTMLTagAttributes implements java.io.Serializable     // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private boolean compact_ = false;
    private Vector listItems_;

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A

    transient private Vector elementListeners;      // The list of element listeners @CRS


    /**
    *  Constructs a default HTMLList object.
    **/
    public HTMLList()
    {
        super();
        listItems_ = new Vector();
    }


    /**
    *  Constructs an HTMLList object with the specified <i>itemList</i>.
    *  @param itemList The items in the HTMLList.
    **/
    public HTMLList(Vector itemList)
    {
        super();

        setItems(itemList);
    }


    /**
     *  Adds an HTMLListItem <i>item</i> to the HTMLList.
     *  @param item The HTMLTagElement.
     **/
    public void addListItem(HTMLListItem item)
    {
        //@C1D

        if (item == null)
            throw new NullPointerException("item");

        listItems_.addElement(item);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }


    /**
     *  Adds an HTML <i>list</i> to the HTMLList.
     *  @param item The HTMLList.
     **/
    public void addList(HTMLList list)
    {
        //@C1D

        if (list == null)
            throw new NullPointerException("list");

        listItems_.addElement(list);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }


    /**
    *  Adds an ElementListener.
    *
    *  @param listener The ElementListener.
    **/
    public void addListItemElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        if (elementListeners == null) elementListeners = new Vector(); //@CRS
        elementListeners.addElement(listener);
    }



    /**
    *  Fires the element event.
    **/
    private void fireElementEvent(int evt)
    {
      if (elementListeners == null) return; //@CRS
        Vector targets;
        targets = (Vector) elementListeners.clone();
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
    *  Returns the <i>direction</i> of the text interpretation.
    *  @return The direction of the text.
    **/
    public String getDirection()                               //$B1A
    {
        return dir_;
    }


    /**
    *  Returns the direction attribute tag.
    *  @return The direction tag.
    **/
    String getDirectionAttributeTag()                                                 //$B1A
    {
        //@C1D

        if ((dir_ != null) && (dir_.length() > 0))
        {
            StringBuffer buffer = new StringBuffer(" dir=\"");
            buffer.append(dir_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
    }


    /**
    *  Returns the number of items in the HTMLList.
    *  @return The number of items.
    **/
    public int getItemCount()
    {
        return listItems_.size();
    }



    /**
    *  Returns the list of items.
    *  @return The items.
    **/
    public Vector getItems()
    {
        return listItems_;
    }


    /**
    *  Returns the item attribute tags.
    *  @return The item tags.
    **/
    String getItemAttributeTag()
    {
        StringBuffer s = new StringBuffer("");
        for (int i=0; i < listItems_.size(); i++)
        {
            HTMLTagElement item = (HTMLTagElement)listItems_.elementAt(i);
            s.append(item.getTag());
        }

        return s.toString();
    }


    /**
    *  Returns the <i>language</i> of the input element.
    *  @return The language of the input element.
    **/
    public String getLanguage()                                //$B1A
    {
        return lang_;
    }


    /**
    *  Returns the language attribute tag.
    *  @return The language tag.
    **/
    String getLanguageAttributeTag()                                                  //$B1A
    {
        //@C1D

        if ((lang_ != null) && (lang_.length() > 0))
        {
            StringBuffer buffer = new StringBuffer(" lang=\"");
            buffer.append(lang_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
    }


    /**
    *  Indicates if the list is initialized to compact.
    *  @return true if compact; false otherwise.
    **/
    public boolean isCompact()
    {
        return compact_;
    }


    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        //@CRS changes_ = new PropertyChangeSupport(this);
        //@CRS elementListeners = new Vector();
    }


    /**
     *  Removes an HTMLListItem <i>item</i> from the HTMLList.
     *  @param item The HTMLTagElement.
     **/
    public void removeListItem(HTMLListItem item)
    {
        //@C1D

        if (item == null)
            throw new NullPointerException("item");

        if (listItems_.removeElement(item))
            fireElementEvent(ElementEvent.ELEMENT_REMOVED);
    }


    /**
     *  Removes an HTML <i>list</i> from the HTMLList.
     *  @param list The HTMLList.
     **/
    public void removeList(HTMLList list)
    {
        //@C1D

        if (list == null)
            throw new NullPointerException("list");

        if (listItems_.removeElement(list))
            fireElementEvent(ElementEvent.ELEMENT_REMOVED);
    }


    /**
     *  Removes this ElementListener.
     *
     *  @param listener The ElementListener.
     **/
    public void removeListItemElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        if (elementListeners != null) elementListeners.removeElement(listener); //@CRS
    }



    /**
    *  Sets whether the list is initialized to being compact.  The <i>compact</i> attribute
    *  instructs the browser to reduce the space occupied by the list.
    *  @param compact true if initialized to compact; false otherwise.  The default is false.
    *
    **/
    public void setCompact(boolean compact)
    {
        //@C1D

        boolean old = compact_;

        compact_ = compact;

        if (changes_ != null) changes_.firePropertyChange("compact", new Boolean(old), new Boolean(compact) ); //@CRS
    }


    /**
    *  Sets the <i>direction</i> of the text interpretation.
    *  @param dir The direction.  One of the following constants
    *  defined in HTMLConstants:  LTR or RTL.
    *
    *  @see com.ibm.as400.util.html.HTMLConstants
    *
    **/
    public void setDirection(String dir)                                     //$B1A
    {
        if (dir == null)
            throw new NullPointerException("dir");

        // If direction is not one of the valid HTMLConstants, throw an exception.
        if ( !(dir.equals(HTMLConstants.LTR))  && !(dir.equals(HTMLConstants.RTL)) )
            throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String old = dir_;

        dir_ = dir;

        if (changes_ != null) changes_.firePropertyChange("dir", old, dir ); //@CRS
    }



    /**
    *  Sets the items in the HTMLList.
    *  @param itemList The list of items.
    *
    **/
    public void setItems(Vector itemList)
    {
        if (itemList == null)
            throw new NullPointerException("items");

        //@C1D

        Vector old = listItems_;

        listItems_ = itemList;

        if (changes_ != null) changes_.firePropertyChange("items", old, itemList ); //@CRS
    }


    /**
    *  Sets the <i>language</i> of the input tag.
    *  @param lang The language.  Example language tags include:
    *  en and en-US.
    *
    **/
    public void setLanguage(String lang)                                      //$B1A
    {
        if (lang == null)
            throw new NullPointerException("lang");

        String old = lang_;

        lang_ = lang;

        if (changes_ != null) changes_.firePropertyChange("lang", old, lang ); //@CRS
    }


    /**
    *  Returns a String representation for the HTMLList tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
