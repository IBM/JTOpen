///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DoubleClickList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.BorderLayout;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;


/**
The DoubleClickList class represents a JList which allows
only single selections and fires an item event when
an item is double clicked.
**/
class DoubleClickList
extends JComponent
implements ItemSelectable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    JList                   list_;
    JScrollPane             scrollPane_;

    transient Vector        itemListeners_;



    public DoubleClickList(String[] items)
    {
        super();

        list_ = new JList(items);
        list_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane_ = new JScrollPane(list_);

        setLayout(new BorderLayout());
        add("Center", scrollPane_);

        initializeTransient();
    }



    public DoubleClickList(ListModel listModel)
    {
        super();

        list_ = new JList(listModel);
        list_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane_ = new JScrollPane(list_);

        setLayout(new BorderLayout());
        add("Center", scrollPane_);

        initializeTransient();
    }



    public void addItemListener(ItemListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");        
        itemListeners_.addElement(listener);
    }


    public int getSelectedIndex()
    {
        return list_.getSelectedIndex();
    }


    public Object[] getSelectedObjects()
    {
        return list_.getSelectedValues();
    }


    private void initializeTransient()
    {
        itemListeners_ = new Vector();

        list_.addMouseListener(new MouseAdapter() {
            public void mouseClicked (MouseEvent event) {
                // If double click.
                if (event.getClickCount() > 1) {
                    Object value = list_.getSelectedValue();
                    if ((value != null) && (list_.isEnabled())) {
                        ItemEvent event2 = new ItemEvent(DoubleClickList.this, 
                                                         ItemEvent.ITEM_STATE_CHANGED, 
                                                         value, 
                                                         ItemEvent.SELECTED);
                        Enumeration enum = itemListeners_.elements();
                        while(enum.hasMoreElements()) {
                            ((ItemListener)enum.nextElement()).itemStateChanged(event2);
                        }
                    }
                }
            }
        });
    }



    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient();
    }



    public void removeItemListener(ItemListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");        
        itemListeners_.removeElement(listener);
    }



    public void setEnabled(boolean enabled)
    {
        list_.setEnabled(enabled);
    }



    public void setListData(Object[] listData)
    {
        list_.setListData(listData);
    }


    public void setSelectedIndex(int selectedIndex)
    {
        list_.setSelectedIndex(selectedIndex);
    }


    public void setVisibleRowCount(int visibleRowCount)
    {
        list_.setVisibleRowCount(visibleRowCount);
    }

}
