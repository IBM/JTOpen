/*
 * QCVNodeDisplayer.java
 *
 * Created on July 16, 2000, 2:03 AM
 *
 * This is free open source software distributed under the IBM Public License found
 * on the World Wide Web at http://oss.software.ibm.com/developerworks/opensource/license10.html
 * Copyright *C* 2000, Jack J. Woehr, PO Box 51, Golden, CO 80402-0051 USA jax@well.com
 * Copyright *C* 2000, International Business Machines Corporation and others. All Rights Reserved.
 */

package com.SoftWoehr.JTOpenContrib.QCDemo;

/** Another interface substituting for dual inheritance in Java. This
 * allows a little polymorphism with AS400 panes.
 * @author jax
 * @version 1.0
 */
public interface QCVNodeDisplayer {
  /** AS400 pane sets its root VNode.
   * @param v The VNode with data for the view pane.
   */
  public void setRoot(com.ibm.as400.vaccess.VNode v);
  /** Add this AS400 pane to an AWT Component.
   * @param c The Component to which this pane is to be added.
   */
  public void addTo(java.awt.Container c);
  /** Set the ErrorDialogAdapter handling errors for this AS400 pane.
   * @param eda An ErrorDialogAdapter attached to Frame.
   */
  public void addErrorListener(com.ibm.as400.vaccess.ErrorDialogAdapter eda);
}
