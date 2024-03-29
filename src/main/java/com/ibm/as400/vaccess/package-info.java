/**
 * <i>Deprecated</i> Provides classes that graphically present IBM i data to the
 * user.
 *
 * <P>
 * <b>This package has been deprecated</b>, and is no longer being enhanced.
 * Users are advised to build their own GUI applications using Java Swing, on
 * top of the classes in package <tt>com.ibm.as400.access</tt>.
 * </P>
 *
 * <P>
 * These classes use the com.ibm.as400.access classes to retrieve data and then
 * present that data to the user.
 * </P>
 *
 * <P>
 * The various pane classes are graphical user interface components that present
 * and allow manipulation of one or more IBM i resources. The behavior of each
 * resource varies depending on its type.
 * </P>
 *
 * <P>
 * All panes extend the javax.swing.JComponent class. As a result, they can be
 * added to any AWT or Swing frame, window, or container.
 * </P>
 *
 * IBM i resources are represented in the graphical user interface with an icon
 * and text. IBM i resources are defined with hierarchical relationships where a
 * resoure might have a parent and zero or more children. These are predefined
 * relationships and are used to specify what resources are displayed in a pane.
 * For example, VJobList is the parent to zero or more VJob objects, and this
 * hierarchical relationship is represented graphically in a pane.
 *
 * <P>
 * Java programs that use the IBM Toolbox for Java GUI (graphical user
 * interface) classes need Swing 1.1. You get Swing 1.1 by running Java 2 or by
 * downloading Swing 1.1 from Sun Microsystems, Inc. In the past, IBM Toolbox
 * for Java has required Swing 1.0.3, and V4R5 is the first release that Swing
 * 1.1 is supported. To move to Swing 1.1, some programming changes were made;
 * therefore, you may have to make some programming changes as well. See the
 * <A HREF="http://java.sun.com/products/jfc/index.jsp">
 * Java Foundation Classes documentation</A> for more information about Swing.
 * </P>
 */
package com.ibm.as400.vaccess;
