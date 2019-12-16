/*
 * QCListPane.java
 *
 * A wrapper around com.ibm.as400.vaccess.AS400ListPane to
 * allow us to polymorphically switch panes in a panel.
 *
 * Created on July 16, 2000, 2:06 AM
 *
 * This is free open source software distributed under the IBM Public License found
 * on the World Wide Web at http://oss.software.ibm.com/developerworks/opensource/license10.html
 * Copyright *C* 2000, Jack J. Woehr, PO Box 51, Golden, CO 80402-0051 USA jax@well.com
 * Copyright *C* 2000, International Business Machines Corporation and others. All Rights Reserved.
 */

package com.SoftWoehr.JTOpenContrib.QCDemo;

/** A wrapper around a JTOpen pane type to allow polymorphism.
 * @author jax
 * @version 1.0
 */
public class QCListPane extends com.ibm.as400.vaccess.AS400ListPane implements QCVNodeDisplayer {

  /** Creates new QCExplorer */
  public QCListPane() {
  }

  /** Sets the node as root for the pane.
   * @param v The VNode to be setRoot by the pane.
   */
  public void setRoot(com.ibm.as400.vaccess.VNode v) {
    try {
      super.setRoot(v);
    }
    catch (java.beans.PropertyVetoException e) {
      e.printStackTrace();
    }
  }

  /** Add the pane to the Container.
   * @param c The Container to be added to.
   */
  public void addTo(java.awt.Container c) {
    c.add(this);
  }

  /** Add error dialog as listener.
    * @param eda The ErrorDialogAdapter to add.
    */
  public void addErrorListener(com.ibm.as400.vaccess.ErrorDialogAdapter eda) {
  }
}
