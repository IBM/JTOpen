///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectListEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * The PrintObjectListEvent class represents a PrintObjectList event.
 *
 * @see PrintObjectListListener
 *
 **/
public class PrintObjectListEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    /**
     * The print object list closed event ID.
     **/
    public static final int CLOSED = 1;

    /**
     * The print object list completed event ID.
     **/
    public static final int COMPLETED = 2;

    /**
     * The print object list exception occurred event ID.
     **/
    public static final int ERROR_OCCURRED = 3;

    /**
     * The print object list opened event ID.
     **/
    public static final int OPENED = 4;

    /**
     * The print object list object added event ID.
     **/
    public static final int OBJECT_ADDED = 5;

    private int id_;
    private Exception exception_;

    // Either this:                                         // @A5A
    private PrintObject printObject_;
    // or these:                                            // @A5A
    private NPCPID cpid_;                                   // @A5A
    private NPCPAttribute cpattr_;                          // @A5A
    // But not both.                                        // @A5A


    /**
     * Constructs an PrintObjectListEvent object. It uses the specified
     * source and ID.
     *
     * @param source The object sourcing the event.
     * @param id The event identifier.
     **/
    public PrintObjectListEvent( Object source, int id )
    {
        super( source );

        if( (id < CLOSED) || (id > OBJECT_ADDED) )
        {
            throw new ExtendedIllegalArgumentException("id",
              ExtendedIllegalArgumentException.RANGE_NOT_VALID );
        }

        id_ = id;
        printObject_ = null;
        exception_ = null;
    }

    /**
     * Constructs an PrintObjectListEvent object. It uses the specified
     * source and exception.
     *
     * @param source The object sourcing the event.
     * @param e The exception that occurred while retrieving the list.
     **/
    public PrintObjectListEvent( Object source, Exception e )
    {
        super( source );

        id_ = PrintObjectListEvent.ERROR_OCCURRED;
        printObject_ = null;
        exception_ = e;
    }

    /**
     * Constructs an PrintObjectListEvent object. It uses the specified
     * source and print object.
     *
     * @param source The object sourcing the event.
     * @param printObject The print object that was added to the list.
     **/
    public PrintObjectListEvent( Object source, PrintObject printObject)
    {
        super( source );

        id_ = PrintObjectListEvent.OBJECT_ADDED;
        printObject_ = printObject;
        exception_ = null;
    }


    // @A5A
    PrintObjectListEvent(Object source, NPCPID cpid, NPCPAttribute cpattr)
    {
        super(source);
        id_ = PrintObjectListEvent.OBJECT_ADDED;
        cpid_ = cpid;
        cpattr_ = cpattr;
    }


    
    /**
     * Returns the exception that occurred while retrieving      
     * the list. If there was no exception, null is returned.
     * @return The exception that occurred while retrieving the list.
     **/
    public Exception getException()
    {
        return exception_;
    }

    /**
     * Returns the print object list event identifier.
     * @return The event identifier.
     *
     **/
    public int getID()
    {
        return id_;
    }

    /**
     * Returns the print object added to the list. If an object
     * was not added to the list for this event, null is returned.
     *
     * @return The print object added to the list.
     */

     public PrintObject getObject()
     {
         if (printObject_ == null)                                                          // @A5A
             printObject_ = ((PrintObjectList)source).newNPObject(cpid_, cpattr_);          // @A5A
         return printObject_;
     }



     // @A5A
     void setSource(Object s)
     {
         source = s;
     }
}
