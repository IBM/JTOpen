///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DQAttsAuthorityEditor.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyEditorSupport;

/**
 The DQAttsAuthorityEditor class provides the list of data queue editor authorities.
 **/
public class DQAttsAuthorityEditor extends PropertyEditorSupport
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    /**
     Returns the list of data queue editor authorities.
     @return  The list of data queue editor authorities.
     **/
    public String[] getTags()
    {
        return new String[] { "*ALL", "*CHANGE", "*EXCLUDE", "*USE", "*LIBCRTAUT" };
    }
}
