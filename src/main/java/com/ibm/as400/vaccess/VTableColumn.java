///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VTableColumn.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.table.TableColumn;



/**
The VTableColumn class is an extension of Swing's TableColumn
that allows us to specify:

<ul>
<li>a property identifier to which the column is associated
<li>the minimum and maximum column width in terms of characters,
    rather than pixels
</ul>
**/
class VTableColumn
extends TableColumn
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private int     preferredCharWidth_     = 10;



/**
Constructs a VTableColumn object.

@param columnIndex        The column index.
@param propertyIdentifier The property identifier.
**/
    public VTableColumn (int columnIndex, Object propertyIdentifier)
    {
        super (columnIndex);
        setIdentifier (propertyIdentifier);
    }



/**
Returns the preferred column width in characters.

@return The preferred column width in characters.
**/
    public int getPreferredCharWidth ()
    {
        return preferredCharWidth_;
    }



/**
Sets the preferred column width in characters.

@param preferredCharWidth The preferred column width in characters.
**/
    public void setPreferredCharWidth (int preferredCharWidth)
    {
        preferredCharWidth_ = preferredCharWidth;
    }



}
