///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RootPermission.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
<P>The RootPermission class represents the permissions for the specified user of an 
object contained in the root directory structure.  
<P>An object on the root directory structure can set the <B>data authority</B> or the <B>object
authority</B>.  The <B>data authority</B> can be set to one of the following values:  *none, *RWX, 
*RW, *RX, *WX, *R, *W, *X, *EXCLUDE, or *AUTL.  Use <i>getDataAuthority()</i> to display
the current values and the <i>setDataAuthority()</i> to set the data authority to one of
the valid values.  Use <i>commit()</i> from the Permission class to send the changes
to the server.

<P>The <B>object authority</B> can be set to one or more of the following values: 
alter, existence, management, or reference. Use the appropriate
set methods (<i>setAlter()</i>, <i>setExistence()</i>, <i>setManagement()</i>,
or <i>setReference()</i>) to turn the value on or off. After all the values 
are set, use the <i>commit()</i> method from the Permissions class to send the changes 
to the server. 
<P>For more information, refer the iSeries Advance Series Security Basic Manual (SC41-5301-00).
**/
public class RootPermission extends UserPermission
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


    /**
     * Constructs a RootPermission object. This is the permission of the specified user.
     * @param userProfileName The name of the user profile.
     *
    **/
    public RootPermission(String userProfileName)
    {
        super(userProfileName);
        return;
    }

    
    /**
     * Returns the data authority of the user.
     * @return The data authority of the user. The possible values are:
         <UL>
        <LI>*RWX The user has object
        operational, read, add, update, delete, and execute authorities to the
        object.

        <LI>*RW The user has object operational, read, add, delete authorities to the object.

        <LI>*RX
        The user has object operational, read, and execute authorities to the object.

        <LI>*WX The
        user has object operational, add, update, delete, and execute authorities
        to the object.

        <LI>*R
        The user has object operational and read authorities to the object.

        <LI>*W
        The user has object operational, add, update, delete authorities to the
        object.

        <LI>*X
        The user has object operational and execute authorities to the object.

        <LI>*EXCLUDE The user cannot access the object.

        <LI>*AUTL The public authorities
        to the object comes from the public authority on the authorization list
        that secures the object. The value can be returned only if there an authorization
        list that secures the object and the authorized user is *PUBLIC.</DD>

        <LI>*NONE The user has no authority to the object.
    </UL>
     * @see #setDataAuthority(String)
     *    
    **/
    public String getDataAuthority()
    {
        return dataAuthority_;
    }

   
    /**
     * Indicates if the user has the object authority of alter.
     * @return true if the user has the object authority of alter;
     * false otherwise.
     *
    **/
    public boolean isAlter()
    {
        return authorities_[OBJECT_ALTER];
    }

    /**
     * Indicates if the user has the object authority of existence.
     * @return true if the user has the object authority of existence;
     * false otherwise.
    **/
     public boolean isExistence()
    {
        return authorities_[OBJECT_EXIST];
    }
    

    /**
     * Indicates if the user has the object authority of management.
     * @return true if the user has the object authority of management;
     * false otherwise.
    **/
    public boolean isManagement()
    {
        return authorities_[OBJECT_MANAGEMENT];
    }

    /**
     * Indicates if the user has the object authority of reference.
     * @return true if the user has the object authority of reference;
     * false otherwise.
     *
    **/
    public boolean isReference()
    {
        return authorities_[OBJECT_REFERENCE];
    }

    
   /**
     * Sets the alter object authority.
     * @param authority  true to set the object authority of alter on; 
     * false to set the object authority of alter off.
     * @see #isAlter
     *
    **/
    public synchronized void setAlter(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_ALTER] = authority;
        if (dataAuthority_.equals("*EXCLUDE")&&authority==true)
            dataAuthority_ = "*NONE";
        return;
    }


    /**
     * Sets the data authority of the user.
     * @param authority The data authority of the user.
    <P>Valid values are:
    <UL>
        <LI>*RWX The user has object
        operational, read, add, update, delete, and execute authorities to the
        object.

        <LI>*RW The user has object operational, read, add, delete authorities to the object.

        <LI>*RX
        The user has object operational, read, and execute authorities to the object.

        <LI>*WX The
        user has object operational, add, update, delete, and execute authorities
        to the object.

        <LI>*R
        The user has object operational and read authorities to the object.

        <LI>*W
        The user has object operational, add, update, delete authorities to the
        object.

        <LI>*X
        The user has object operational and execute authorities to the object.

        <LI>*EXCLUDE The user cannot access the object.

        <LI>*AUTL The public authorities
        to the object comes from the public authority on the authorization list
        that secures the object. The value can be returned only if there an authorization
        list that secures the object and the authorized user is *PUBLIC.</DD>

    <LI>*NONE The user has no authority to the object.
    </UL>
    **/
    public synchronized void setDataAuthority(String authority)
    {
        if (authority == null)
            throw new NullPointerException("authority");
        changeAuthority();
        dataAuthority_ = authority.toUpperCase().trim();
        if (dataAuthority_.equals("*EXCLUDE"))
        {
            authorities_[OBJECT_ALTER] = false;
            authorities_[OBJECT_EXIST] = false;
            authorities_[OBJECT_MANAGEMENT] = false;
            authorities_[OBJECT_REFERENCE] = false; 
        }
        return;
    }

    /**
     * Sets the existence object authority.
     * @param authority  true to set the object authority of existence on;
     * false to set the object authority of existence off.
     * @see #isExistence
    **/
    public synchronized void setExistence(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_EXIST] = authority;
        if (dataAuthority_.equals("*EXCLUDE")&&authority==true)
            dataAuthority_ = "*NONE";
        return;
    }


   /**
     * Sets the management object authority.
     * @param authority  true to set the object authority of management on;
     * false to set the object authority of management off.
     * @see #isManagement
    **/
    public synchronized void setManagement(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_MANAGEMENT] = authority;
        if (dataAuthority_.equals("*EXCLUDE")&&authority==true)
            dataAuthority_ = "*NONE";
        return;
    }


    /**
     * Sets the reference object authority.
     * @param authority true to set the object authority of reference on;
     * false to set the object authority of reference off.
     * @see #isReference
     *
    **/
    public synchronized void setReference(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_REFERENCE] = authority; 
        if (dataAuthority_.equals("*EXCLUDE")&&authority==true)
            dataAuthority_ = "*NONE";
        return;
    }

}
     
