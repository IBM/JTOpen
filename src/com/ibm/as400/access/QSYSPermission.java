///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QSYSPermission.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
<P>The QSYSPermission class represents the permissions for the specified user 
of an object in the traditional AS/400 library structure stored in QSYS.LIB.  
<P>A object stored in QSYS.LIB can set its authorities by setting a single
 object authority value or by setting the individual object and data authorities.
<P>Use <i>getObjectAuthority()</i> to display the current object authority or
<i>setObjectAuthority()</i> to set the current object authority using a single value.
Valid values are: *ALL, *AUTL, *CHANGE, *EXCLUDE, and *USE.  

<P>The detailed object authority can be set to one or more of the following
values: alter, exist, management, operational, reference.  Use the 
appropriate set methods (<i>setAlter()</i>, <i>setExistence()</i>, 
<i>setManamagement()</i>, <i>setOperational()</i>, or <i>setReference()</i>) 
to set the value on or off.  After all values are set, use the <i>commit()</i> 
method from the Permission class to send the changes to the AS/400.


<P>The data authority can be set to one or more of the following values: 
add, delete, execute, read, or update. Use the appropriate
set methods (<i>setAdd()</i>, <i>setDelete()</i>, <i>setExecute()</i>, 
<i>setRead()</i>, or <i>setUpdate()</i>) to set the value on or off. After all 
the values are set, use the <i>commit()</i> method from the Permission class 
to send the changes to the AS/400. 

<P>The single authority actually represents a combination of the detailed object
authorities and the data authorities.  Selecting a single authority will 
automatically turn on the appropriate detailed authorities.  Likewise, selecting
 various detailed authotiries will change the appropriate single authority values.


<P>For more information on object authority commands, refer AS/400 CL 
commands GRTOBJAUT (Grant object authority) and EDTOBJAUT (Edit object authority).
**/

public class QSYSPermission extends UserPermission
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    

    static final long serialVersionUID = 4L;


    /**
     * Constructs a QSYSPermission object. This is the permission of the 
     * specified user.
     * @param userProfileName The name of the user profile.
     *
    **/
    public QSYSPermission(String userProfileName)
    {
        super(userProfileName);
        dataAuthority_ = "*EXCLUDE";
        objectAuthority_ = BASIC_EXCLUDE;
        authorities_ = new boolean[10];
        for (int i=0;i<10;i++)
        {
            authorities_[i] = false;
        }
        return;
    }

   /** 
     * Returns the copyright.
    **/
    private static String getCopyright()
    {
        return Copyright.copyright;
    }
    
    /**
     * Returns the object authority of the user specified as a single value.
     * @return The object authority of the user specified as a single value. 
     * The possible values are:
       <UL>
       <LI>*ALL The user can perform all operations on the object
           except for those limited to the owner or controlled by 
           authorization list management authority.  The user can control
           the object's existence, specify the security for the object, change
           the object, and perform basic functions on the object.  The user
           can also change ownership of the object.  
       <LI>*AUTL The public authority of the authorization list securing the
           object will be used. This is valid only if *PUBLIC is the user
           specified.
       <LI>*CHANGE The user can perform all operations on the object
           except those limited to the owner or controlled by object existence
           authority and object management authority.
       <LI>*EXCLUDE The user cannot access the object.
       <LI>*USE The user has object operational authority, read authority,
       and execute authority.
       </UL>
     * @see #setObjectAuthority(String)   
    **/
    public String getObjectAuthority()
    {
        parseBasic();
        if (isFromAuthorizationList())
            return "*AUTL";
        switch (objectAuthority_)
        {
            case BASIC_ALL :
                return "*ALL";
            case BASIC_EXCLUDE :
                return "*EXCLUDE";
            case BASIC_USE :
                return "*USE";
            case BASIC_CHANGE :
                return "*CHANGE";
            default :
                return "USER DEFINED";
        }
    }


   /**
     * Indicates if the user has the data authority of add.
     * @return true if the user has the data authority of add; 
     * false otherwise.
     *
    **/
    public boolean isAdd()
    {
        return authorities_[DATA_ADD];
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
     * Indicates if the user has the data authority of delete.
     * @return true if the user has the data authority of delete;
     * false otherwise.
     *
    **/
    public boolean isDelete()
    {
        return authorities_[DATA_DELETE];
    }    

  /**
     * Indicates if the user has the data authority of execute.
     * @return true if the user has the data authority of execute;
     * false otherwise.
     *
    **/
    public boolean isExecute()
    {
        return authorities_[DATA_EXECUTE];
    }
    /**
     * Indicates if the user has the object authority of existence.
     * @return true if the user has the object authority of existence;
     * false otherwise.
     *
    **/
     public boolean isExistence()
    {
        return authorities_[OBJECT_EXIST];
    }
    

    /**
     * Indicates if the user has the object authority of management.
     * @return true if the user has the object authority of management;
     * false otherwise.
     *
    **/
    
    public boolean isManagement()
    {
        return authorities_[OBJECT_MANAGEMENT];
    }

    
   /**
     * Indicates if the user has the object authority of operational.
     * @return true if the user has the object authority of operational;
     * false otherwise.
     *
    **/
    
    public boolean isOperational()
    {
        return authorities_[OBJECT_OPERATION];
    }

  /**
     * Indicates if the user has the data authority of read.
     * @return true if the user has the data authority of read;
     * false otherwise.
     *
    **/
    public boolean isRead()
    {
        return authorities_[DATA_READ];
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
     * Indicates if the user has the data authority of update.
     * @return true if the user has the data authority of update;
     * false otherwise.
     *
    **/
    public boolean isUpdate()
    {
        return authorities_[DATA_UPDATE];
    }
    
    /**
     * Parses the basic authority type.
    **/
    private synchronized void parseBasic()
    {
        for (int j=0;j<4;j++)
        {
            boolean match = true;
            for (int i=0;i<10;i++)
            {
                if (authorities_[i] != basicAutMapping[j][i])
                {
                    match = false;
                    break;
                }
            }
            if (match == true)
            {
                objectAuthority_= j;
                return;
            }
        }
        objectAuthority_= -1;
        return;
    }

   /**
     * Sets the add data authority.
     * @param authority  true to set the data authority of add on; 
     * false to set the data authority of add off.
     *
    **/
    public synchronized void setAdd(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_ADD] = authority;
        return;
    }


   /**
     * Sets the alter object authority.
     * @param authority  true to set the object authority of alter on; 
     * false to set the object authority of alter off.
     *
    **/
    public synchronized void setAlter(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_ALTER] = authority;
        return;
    }

    /**
     * Sets the delete data authority.
     * @param authority  true to set the data authority of delete on;
     * false to set the data authority of delete off.
     *
    **/
    public synchronized void setDelete(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_DELETE] = authority;
        return;
    }


 /**
     * Sets the execute data authority.
     * @param authority  true to set the data authority of execute on;
     * false to set the data authority of execute off.
     *
    **/
    public synchronized void setExecute(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_EXECUTE] = authority;
        return;
    }


    /**
     * Sets the existence object authority.
     * @param authority  true to set the object authority of existence on;
     * false to set the object authority of existence off.
     *
    **/
    
    public synchronized void setExistence(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_EXIST] = authority;
        return;
    }


   /**
     * Sets the management object authority.
     * @param authority  true to set the object authority of management on;
     * false to set the object authority of management off.
     *
    **/
    
    public synchronized void setManagement(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_MANAGEMENT] = authority;
        return;
    }

  /**
     * Sets the object authority of the user using a single value.
     * @param authority The object authority of the user.
       <P>Valid values are:
       <UL>
       <LI>*ALL The user can perform all operations on the object
           except for those limited to the owner or controlled by 
           authorization list management authority.  The user can control
           the object's existence, specify the security for the object, change
           the object, and perform basic functions on the object.  The user
           can also change ownership of the object.  
       <LI>*AUTL The public authority of the authorization list securing the
           object will be used. This is valid only if *PUBLIC is the user
           specified.
       <LI>*CHANGE The user can perform all operations on the object
           except those limited to the owner or controlled by object existence
           authority and object management authority.
       <LI>*EXCLUDE The user cannot access the object.
       <LI>*USE The user has object operational authority, read authority,
       and execute authority.
       </UL>
    
**/
    public synchronized void setObjectAuthority(String authority)
    {
        if (authority == null)
            throw new NullPointerException("authority");
        String aut = authority.trim().toUpperCase();
        changeAuthority();
        if (getObjectAuthority().equals(aut) == true)
            return;
        if (aut.equals("*ALL"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_ALL][i];
            }
            objectAuthority_ = BASIC_ALL;
        } else if (aut.equals("*EXCLUDE"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_EXCLUDE][i];
            }
            objectAuthority_ = BASIC_EXCLUDE;
        } else if (aut.equals("*USE"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_USE][i];
            }
            objectAuthority_ = BASIC_USE;
        } else if (aut.equals("*CHANGE"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_CHANGE][i];
            }
            objectAuthority_ = BASIC_CHANGE;
        }
      
        return;
    }

  /**
     * Sets the operational object authority.
     * @param authority  true to set the object authority of operational on;
     * false to set the object authority of operational off.
     *
    **/
    
    public synchronized void setOperational(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_OPERATION] = authority;
        return;
    }

   /**
     * Sets the read data authority.
     * @param authority  true to set the data authority of read on;
     * false to set the data authority of read off.
     *
    **/
    public synchronized void setRead(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_READ] = authority;
        return;
    }

    /**
     * Sets the reference object authority.
     * @param authority true to set the object authority of reference on;
     * false to set the object authority of reference off.
     *
    **/
    public synchronized void setReference(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_REFERENCE] = authority;
        return;
    }

   
    /**
     * Sets the update data authority.
     * @param authority  true to set the data authority of update on;
     * false to set the data authority of update off.
     *
    **/
    public synchronized void setUpdate(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_UPDATE] = authority;
        return;
    }

}
     
