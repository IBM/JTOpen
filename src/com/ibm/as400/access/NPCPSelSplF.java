///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPSelSplF.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPCPSelSplF class - class for an attribute value list code point used with
  * the network print server's data stream.
  * This class is derived from NPCPSelection and will be used to build a code
  * point that has as its data a list of any attributes that can filter a splfile list.
**/

class NPCPSelSplF extends NPCPSelection implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    // some strings we use for filtering output queues
    // we accept *ALL for the output queue library and we'll convert it
    // to "" for the host to use
    static final String STR_ALLOUTQLIBS = "*ALL";
    static final String STR_BLANKOUTQLIB = "";

    protected Object clone()
    {
        NPCPSelSplF cp = new NPCPSelSplF(this);
        return cp;
    }

   /**
    * copy constructor
    **/
    NPCPSelSplF(NPCPSelSplF  cp)
    {
        super(cp);
    }

   /**
    * basic constructor that creates an empty splf selection codepoint
    **/
    NPCPSelSplF()
    {
        super();
    }

   /**
    * gets the formtype filter
    * @returns the filter for the form type or an empty
    * string if it isn't set.
    **/
    String getFormType()
    {
        String formType = getStringValue(PrintObject.ATTR_FORMTYPE);
        if( formType == null )
        {
            return emptyString;
        } else {
            return formType;
        }

    }

   /**
    * gets the output queue filter as an IFS path.
    * @returns The IFS path of the output queue filter or
    * an empty string if it isn't set.
    **/
    String getQueue()
    {
        //
        // note: I cannot just call getStringValue(ATTR_OUTPUT_QUEUE) here because
        // of the special case for libraries on spooled files lists where "  " means
        // *ALL.  
        //
        String ifsQueue = emptyString;
        String object = getStringValue(PrintObject.ATTR_OUTQUE);
        String library = getStringValue(PrintObject.ATTR_OUTQUELIB);
        if (object != null)
        {
            if (library != null)
            {
                // change the library name of "" (or any blanks "   ") to *ALL here
                library = library.trim();   // remove any whitespace
                if (library.length() == 0)
                {
                    library = STR_ALLOUTQLIBS;
                }
                ifsQueue = QSYSObjectPathName.toPath(library, object, "OUTQ");
            }
        }

        return ifsQueue;

    }  // get Queue

   /**
    * gets the user filter
    * @returns the filter for the user or an empty string if it isn't set.
    **/
    String getUser()
    {
        String user = getStringValue(PrintObject.ATTR_JOBUSER);
        if( user == null )
        {
            return emptyString;
        } else {
            return user;
        }
    }

  /** @A1A
    * gets the create job system
    * @returns the filter for the user or an empty string if it isn't set.
    **/
    String getJobSystem()
    {
        String jobSystem = getStringValue(PrintObject.ATTR_JOBSYSTEM);
        if( jobSystem == null )
        {
            return emptyString;
        } else {
            return jobSystem;
        }
    }

  /** @A1A
   * @return the filter for start date or blanks
   * if end date isn't set.
   **/
   String getStartDate()
   {
        String startDate = getStringValue(PrintObject.ATTR_DATE);
        if ( startDate == null )
        {
            return emptyString;
        } else {
            return startDate;
        }
   }

   /** @A1A
   * @return the filter for start time or blanks
   * if end time isn't set.
   **/
   String getStartTime()
   {
        String startTime = getStringValue(PrintObject.ATTR_TIME);
        if ( startTime == null )
        {
            return emptyString;
        } else {
            return startTime;
        }
   }
   
   /**
    * gets the user data filter
    * @returns the filter for the user data or
    * an empty string if it isn't set.
    **/
    String getUserData()
    {
        String userData = getStringValue(PrintObject.ATTR_USERDATA);
        if( userData == null )
        {
            return emptyString;
        } else {
            return userData;
        }
    }

   /** @A1A
   * @return the filter for end date or blanks
   * if end date isn't set.
   **/
   String getEndDate()
   {
        String endDate = getStringValue(PrintObject.ATTR_DATE_END);
        if ( endDate == null )
        {
            return emptyString;
        } else {
            return endDate;
        }
   }

   /** @A1A
   * @return the filter for end time or blanks
   * if end time isn't set.
   **/
   String getEndTime()
   {
        String endTime = getStringValue(PrintObject.ATTR_TIME_END);
        if ( endTime == null )
        {
            return emptyString;
        } else {
            return endTime;
        }
   }
        
    
   /**
    * set formtype filter.
    * Removes the filter if formType is "".
    **/
    void setFormType(String formType)
    {
        if( formType.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_FORMTYPE);
        } else {
            setAttrValue(PrintObject.ATTR_FORMTYPE, formType);
        }
    }

   /**
    * set the output queue filter using an IFS Path.
    * Use %ALL% for all queue libraries and it will be converted to "" here for the
    * host (which uses all blanks or "" to represent all libs on this one selection CP).
    * Removes the filter if ifsQueue is "".
    **/
    void setQueue(String ifsQueue)
    {
        // if the ifs path has a length of 0 (emtpy string) then
        // we will remove the filter completely.
        // If it has something in it, it had better be
        // a valid IFS path name.

        if( ifsQueue.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_OUTPUT_QUEUE);
        } else {
            // we must tear the IFS path apart here and convert the library name
            // if necessary because the underlying code will not know to convert
            // %ALL%.LIB to "" (special case for spooled file filters only).
            QSYSObjectPathName ifsPath = new QSYSObjectPathName(ifsQueue, "OUTQ");
            setQueue(ifsPath.getLibraryName(), ifsPath.getObjectName());
        }
    }

   /**
    * set output queue filter
    * Set the queuelib to *ALL if you want all  libraries, it will be converted
    * to "      " here for the host (and converted back to *ALL on the getter)
    **/
    void setQueue(String queueLib, String queue)
    {
        // The host uses "      " to mean "*ALL" for queue lib
        // on this filter.  We can use "" and it'll work, but we
        // have to convert *ALL to "" here.
        // note: I do not uppercase here, so it must be uppercased by caller

       if ((queueLib.trim()).compareTo(STR_ALLOUTQLIBS) == 0)
       {
          queueLib = STR_BLANKOUTQLIB;
       }

       setAttrValue(PrintObject.ATTR_OUTQUELIB, queueLib);
       setAttrValue(PrintObject.ATTR_OUTQUE,    queue);
    }

   /**
    * set user to get spooled files for.  May be *USER, *CURRENT or a user id
    * Removes the filter if user is "".
    **/
    void setUser(String user)
    {
        if( user.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_JOBUSER);
        } else {
            setAttrValue(PrintObject.ATTR_JOBUSER, user);
        }
    }

   /**
    * set user data filter
    * Removes the filter if userData is "".
    **/
    void setUserData(String userData)
    {
        if( userData.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_USERDATA);
        } else {
            setAttrValue(PrintObject.ATTR_USERDATA, userData);
        }
    }

   /** @A1A
    * set create job system
    * Removes the filter if userData is "".
    **/
    void setJobSystem(String jobSystem)
    {
        if( jobSystem.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_JOBSYSTEM);
        } else {
            setAttrValue(PrintObject.ATTR_JOBSYSTEM, jobSystem);
        }
    }

    /** @A1A
    * set startTime and
    * removes the filter if startTime is "".
    **/
    void setStartTime(String startTime)
    {
        if( startTime.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_TIME);
        } else {
            setAttrValue(PrintObject.ATTR_TIME, startTime);
        }
    }

    /** @A1A
    * set startDate and
    * removes the filter if startDate is "".
    **/
    void setStartDate(String startDate)
    {
        if( startDate.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_DATE);
        } else {
            setAttrValue(PrintObject.ATTR_DATE, startDate);
        }
    }
    
   /** @A1A
    * set endTime and
    * removes the filter if endTime is "".
    **/
    void setEndTime(String endTime)
    {
        if( endTime.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_TIME_END);
        } else {
            setAttrValue(PrintObject.ATTR_TIME_END, endTime);
        }
    }

    /** @A1A
    * set endDate filter and
    * removes the filter if userDate is "".
    **/
    void setEndDate(String endDate)
    {
        if( endDate.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_DATE_END);
        } else {
            setAttrValue(PrintObject.ATTR_DATE_END, endDate);
        }
    }

}

