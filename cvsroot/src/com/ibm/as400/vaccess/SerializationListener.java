///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SerializationListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Used to enable serialization of a Container with Swing 1.1.
 * Intended for Toolbox classes that extend JComponent and contain
 * an internal JTable object. (In Swing 1.1, JTable has a bug which
 * makes it un-Serializable).
 * See also: source code for javax.swing.JComponent in JDK 1.2.
 *
 * It should be noted that a SerializationListener is itself
 * Serializable and contains a transient Container. In order for
 * multiple serializations to work, the calling class should add
 * a SerializationListener as a FocusListener to itself in its
 * initializeTransient() method, not in its constructor.
*/
class SerializationListener implements FocusListener, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private String x = Copyright_v.copyright;

  // This is the component that we want to safely serialize.
  transient private Container component_ = null;
  // This is the table model that needs to have its listener removed.
  transient private TableModel model_ = null;
  // This is the listener to be removed from the table model.
  transient private TableModelListener listener_ = null;

  /**
   * Construct a SerializationListener object with the specified component.
   * @parm component The component whose UI will be uninstalled before serialization.
  **/
  public SerializationListener(Container component)
  {
    component_ = component;
  }

  /**
   * Construct a SerializationListener object with the specified model and component.
   * @parm model The table model whose listeners we need to remove before serialization.
   * @parm listener The component to be removed from listening to the model.
  **/
  public SerializationListener(TableModel model, TableModelListener listener)
  {
    listener_ = listener;
    model_ = model;
  }

  /**
   * Does nothing.
   */
  public void focusGained(FocusEvent e) {}


  /**
   * Does nothing.
   */
  public void focusLost(FocusEvent e) {}


  /**
   * When the Container is serialized, this listener will presumably
   * get serialized first. At that time, it removes ALL the sub-components
   * that have been added to the parent Container (including any JTables)
   * before the parent is serialized. By doing so, we avoid the
   * NotSerializableException that would normally get thrown when trying
   * to serialize a component that contains a JTable in Swing 1.1.
   */
  private void writeObject(ObjectOutputStream s)
    throws IOException
  {
    s.defaultWriteObject();
    if (component_ != null)
    {
      component_.removeAll();
    }
    if (model_ != null)
    {
      model_.removeTableModelListener(listener_);
    }
  }
}
