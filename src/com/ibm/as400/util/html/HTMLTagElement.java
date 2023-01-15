///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLTagElement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

 
/**
  The HTMLTagElement class represents an HTML tag element.
**/
public interface HTMLTagElement
{
    /**
    *  Returns the element tag.
    *  @return The tag.
    **/
    public abstract String getTag();

    /**
    *  Returns the element tag in XSL-FO.  Returns a comment tag if there is no associated XSL-FO tag for the element.
    *  @return The tag.
    **/
    public abstract String getFOTag();  //@B1A

    
}
