///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VUtilities.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.Collator;
import java.util.Date;
import java.util.Vector;



/**
The VUtilities class provides miscellanous utilities.
**/
class VUtilities
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";





    // Private data.
    private static Collator             collator_;



/**
Static initializer.
**/
    static
    {
        // If the locale is Korean, then this throws
        // an ArrayIndexOutOfBoundsException.  This is
        // a bug in the JDK.  The workarond in that case
        // is just to use String.compareTo().
        try {
            collator_ = Collator.getInstance ();
            collator_.setStrength (Collator.PRIMARY);
        }
        catch (Exception e) {
            collator_ = null;
        }
    }


/**
Adds the component and sets its grid bag constraints.

@param  component   The component.
@param  panel       The panel.
@param  layout      The grid bag layout.
@param  gridx       The grid x position.
@param  gridwidth   The grid width.
@param  weightx     How much extra horizontal space the component will get.
@param  gridy       The grid y position.
@param  gridheight  The grid height.
@param  weighty     How much extra vertical space the component will get.
@param  fill        Whether the component should expand to fill its space.
@param  anchor      Where to place the component if it doesn't fill its space.
**/
    public static void constrain (Component component,
                                  JPanel panel,
                                  GridBagLayout layout,
                                  GridBagConstraints constraints,
                                  int gridx, int gridwidth, int weightx,
                                  int gridy, int gridheight, int weighty,
                                  int fill, int anchor)
    {
        panel.add (component);

        constraints.gridx       = gridx;
        constraints.gridy       = gridy;
        constraints.gridwidth   = gridwidth;
        constraints.gridheight  = gridheight;
        constraints.fill        = fill;
        constraints.anchor      = anchor;
        constraints.weightx     = weightx;
        constraints.weighty     = weighty;

        layout.setConstraints (component, constraints);
    }


/**
Adds the component and sets its grid bag constraints.

@param  component   The component.
@param  panel       The panel.
@param  layout      The grid bag layout.
@param  gridx       The grid x position.
@param  gridy       The grid y position.
@param  gridwidth   The grid width.
@param  gridheight  The grid height.
**/
    public static void constrain (Component component,
                            JPanel panel,
                            GridBagLayout layout,
                            int gridx, int gridy, int gridwidth, int gridheight)
    {
        panel.add (component);

        GridBagConstraints constraints = new GridBagConstraints ();

        constraints.gridx       = gridx;
        constraints.gridy       = gridy;
        constraints.gridwidth   = gridwidth;
        constraints.gridheight  = gridheight;
        constraints.fill        = GridBagConstraints.BOTH;
        constraints.ipady       = 2;
        constraints.insets      = new Insets (2, 0, 2, 0);
        constraints.anchor      = GridBagConstraints.WEST;
        constraints.weightx     = 1;
        constraints.weighty     = 1;

        layout.setConstraints (component, constraints);
    }


/**
Adds 2 component as a row and sets its grid bag constraints.

@param  component   The left component.
@param  component2  The right component.
@param  panel       The panel.
@param  layout      The grid bag layout.
@param  gridy       The grid y position.
**/
    public static void constrain (Component component,
                                  Component component2,
                                  JPanel panel,
                                  GridBagLayout layout,
                                  int gridy )
    {
        constrain( component,  panel, layout, 0, gridy, 1,1 );
        constrain( component2, panel, layout, 1, gridy, 1,1 );
    }


/**
Adds 2 strings as a row and sets its grid bag constraints.

@param  string1     The left component.
@param  string2     The right component.
@param  panel       The panel.
@param  layout      The grid bag layout.
@param  gridy       The grid y position.
**/
    public static void constrain (String string1,
                                  String string2,
                                  JPanel panel,
                                  GridBagLayout layout,
                                  int gridy )
    {
        constrain( new JLabel(string1), new JLabel(string2),
                   panel, layout, gridy );
    }


/**
Adds the component as a row and sets its grid bag constraints.

@param  component   The component.
@param  panel       The panel.
@param  layout      The grid bag layout.
@param  gridy       The grid y position.
**/
    public static void constrain (Component component,
                                  JPanel panel,
                                  GridBagLayout layout,
                                  int gridy )
    {
        constrain( component,  panel, layout, 0, gridy, 2,1 );
    }


/**
Formats help text from an AS/400 Message.  This text may have
imbed \n's in it.

<p>Remember that JLabels and Labels do not handle
these as expected.  Instead, use a JTextArea, setEditable (false),
and set its background color to the same as the panel.  This
gives you the same effect as a JLabel.
**/
    public static String formatHelp(String helpText, int width)
    {
        StringBuffer  newHelp = new StringBuffer();
        int len = helpText.length();

        int j=0;
        for (int i=0; i<len; i++,j++)
            {
            // if we have reached the width append a lf
            if (j == width)
                {
                newHelp.append("\n");
                j=0;
                }
            // if we get a formatting character append a lf and skip next char
            if (helpText.charAt(i) == '&')
                {
                newHelp.append("\n");
                j=0;
                i++;
                }
            // just a character so append it to new string
            else newHelp = newHelp.append(helpText.charAt(i));
            }
        return newHelp.toString();
    }


// @A1A
/**
Formats help text from an AS/400 Message.  This text may have
imbed \n's in it at word breaks only.

<p>Remember that JLabels and Labels do not handle
these as expected.  Instead, use a JTextArea, setEditable (false),
and set its background color to the same as the panel.  This
gives you the same effect as a JLabel.
**/
    public static String formatHelp2 (String input, int width)
    {
        StringBuffer output = new StringBuffer (input);

        int current = width;
        int i = width;

        while (i < output.length ()) {
            if (current >= width) {
                while (output.charAt (i) != ' ')
                    --i;
                output.setCharAt (i, '\n');
                current = 0;
            }

            else if (output.charAt (i) == '\n') {
                current = 0;
            }

            ++current;
            ++i;
        }

        return output.toString ();
    }


/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Returns the text associated with an exception.  This handles
exceptions with "" and null text, in which case the exception
class name will be used.
**/
    public static String getExceptionText (Exception e)
    {
        String text = e.getMessage ();
        if (text == null)
            text = e.getClass ().toString ();
        else if (text.length() == 0)
            text = e.getClass ().toString ();
        return text;
    }



/**
Returns the frame which contains a component.  This only works if
the component has already been added to a frame.

@return The frame, or null if the component has not yet been added
        to a frame.
**/
    public static Frame getFrame (Component component)
    {
        Component c;
        for (c = component;
             (c != null) && (! (c instanceof Frame));
             c = getParentComponent (c))
             ;
        return (Frame) c;
    }



/**
Returns the parent component (used to trace up to a frame).

@param  compnent    The component.
@return             The parent component.
**/
    private static Component getParentComponent (Component component)
    {
        Component parent;

        // JPopupMenus don't have parents.  I reported this to
        // Swing and they responded that I should use getInvoker()
        // instead.
        if (component instanceof JPopupMenu)
            parent = ((JPopupMenu) component).getInvoker ();

        else
            parent = component.getParent ();

        return parent;
    }



/**
Sorts an array of objects.

@param  objects             The objects.
@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            means to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier, true for ascending order,
                            false for descending order.
**/
    public static void sort (VObject[] objects,
                             Object[] propertyIdentifiers,
                             boolean[] orders)
    {
        // Normalize the orders array.
        boolean[] orders2;
        if (propertyIdentifiers.length > orders.length) {
            orders2 = new boolean[propertyIdentifiers.length];
            System.arraycopy (orders, 0, orders2, 0, orders.length);
        }
        else
            orders2 = orders;

        // Put out a trace.
        if (Trace.isTraceOn()) {
            StringBuffer b1 = new StringBuffer ();
            for (int i = 0; i < propertyIdentifiers.length; ++i) {
                b1.append (propertyIdentifiers[i]);
                b1.append (" (");
                b1.append (orders2[i] ? "ascending" : "descending");
                b1.append (")");
                if (i != propertyIdentifiers.length - 1)
                    b1.append (", ");
            }
            Trace.log (Trace.INFORMATION, "Sorting by " + b1 + ".");
        }

        // This uses a bubble sort for now, which is not the most
        // efficient sort in the world, but most of the time the array size
        // is small.
        //
        // Apparantly, something is coming along in the JDK 2.0 Collection
        // classes to do sorts (java.util.Array).
        //
        VObject temp;
        int length = objects.length;
        for (int i = 0; i < length; ++i) {
            for (int j = i + 1; j < length; ++j) {
                if (sortCompare (objects[i],
                                 objects[j],
                                 propertyIdentifiers,
                                 orders)) {
                    temp = objects[i];
                    objects[i] = objects[j];
                    objects[j] = temp;
                }
            }
        }
    }



/**
Sorts a vector of objects.

@param  objects             The objects.
@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            means to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier, true for ascending order,
                            false for descending order.
**/
    public static void sort (Vector objects,
                             Object[] propertyIdentifiers,
                             boolean[] orders)
    {
        VObject[] objectArray = new VObject[objects.size ()];
        objects.copyInto (objectArray);

        sort (objectArray, propertyIdentifiers, orders);

        objects.removeAllElements ();
        for (int i = 0; i < objectArray.length; ++i)
            objects.addElement (objectArray[i]);
    }



/**
Compares two objects for the sort.

@param  objectI             The ith object.
@param  objectJ             The jth object.
@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            means to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier, true for ascending order,
                            false for descending order.
@return                     true if the ith object is before the
                            jth object, false otherwise.
**/
    private static boolean sortCompare (VObject objectI,
                                        VObject objectJ,
                                        Object[] propertyIdentifiers,
                                        boolean[] orders)
    {
        Object valueI;
        Object valueJ;

        for (int i = 0; i < propertyIdentifiers.length; ++i) {

            // Determine the values to compare.
            if (propertyIdentifiers[i] != null) {
                valueI = objectI.getPropertyValue (propertyIdentifiers[i]);
                valueJ = objectJ.getPropertyValue (propertyIdentifiers[i]);
            }
            else {
                valueI = objectI.toString ();
                valueJ = objectJ.toString ();
            }

            // Check for nulls.
            if (valueI == null)
                valueI = "";
            if (valueJ == null)
                valueJ = "";
            boolean comparison;

            // If we are comparing Date's, toString() doesn't report msec's. @A3C
            if (valueI instanceof Date)
                comparison = ((Date) valueI).after ((Date) valueJ);

            // If they are equal, then use the next column.
            else if (valueI.toString ().equals (valueJ.toString ()))     // @A2C
                continue;

            // Otherwise, do the comparison using this column.

            else if ((valueI instanceof Number) || (valueJ instanceof Number)) {
                long li = (valueI instanceof Number) ? ((Number) valueI).longValue () : -1;
                long lj = (valueJ instanceof Number) ? ((Number) valueJ).longValue () : -1;
                comparison = (li < lj);
            }

            else if (collator_ != null)
                comparison = (collator_.compare (valueI.toString (), valueJ.toString ()) < 0);

            else
                comparison = (valueI.toString ().compareTo (valueJ.toString ()) < 0);

            // Return the value.
            return (comparison != orders[i]);
        }

        // All columns were equal.
        return true;
    }

}

