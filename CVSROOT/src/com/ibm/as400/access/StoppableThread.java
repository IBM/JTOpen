///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: StoppableThread.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The StoppableThread class represents a thread
that can be safely stopped.  See the JDK 1.2 
documentation for Thread.stop() to see why 
this is necessary.
**/
abstract class StoppableThread
extends Thread 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    


    // Private data.
    private static boolean DEBUG_   = false;

    private boolean     continue_   = true;

    private static Object   countLock_  = new Object();
    private static int      count_      = 0;



    protected StoppableThread ()
    {
        this("Stoppable-Thread-" + newId());
    }



    protected StoppableThread(String name)
    {
        super(name);

        if (DEBUG_)
            System.out.println ("Thread:start:" + getName() + "(" + getClass() + ").");
    }



/**
Indicates if the thread can continue.
**/
    protected boolean canContinue ()
    {
        return continue_;
    }



    protected static long newId()
    {
        synchronized(countLock_) {
            return ++count_;
        }
    }



/**
Stops the thread safely.
**/
    public void stopSafely ()
    {
        if (DEBUG_)
            System.out.println ("Thread:stop:" + getName() + "(" + getClass() + ").");

        continue_ = false;
    }


    public boolean wasStoppedSafely()
    {
        return (continue_ == false);
    }


}
