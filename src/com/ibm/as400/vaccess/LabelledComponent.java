///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LabelledComponent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;



/**
The LabelledComponent class represents a GUI component with
a label.
**/
class LabelledComponent
extends JComponent
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    public LabelledComponent(String labelMriKey, Component component)
    {
        this(ResourceLoader.getQueryText(labelMriKey), component, false);
    }




    public LabelledComponent(String text, Component component, boolean overload)
    {
        // Arrange.
        Box labelBox = Box.createHorizontalBox();
        labelBox.add(new JLabel(text, SwingConstants.LEFT));
        labelBox.add(Box.createHorizontalGlue());

        Box componentBox = Box.createHorizontalBox();
        componentBox.add(component);
        componentBox.add(Box.createHorizontalGlue());

        Box overallBox = Box.createVerticalBox();
        overallBox.add(labelBox);
        overallBox.add(componentBox);
        setLayout(new BorderLayout());
        add("Center", overallBox);
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }




}
