///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveCurrentAttributesLibraryListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

public interface RetrieveCurrentAttributesLibraryListener
{
  public void newSystemLibrary(String library);

  public void newProductLibrary(String library);

  public void currentLibrary(String library);

  public void newUserLibrary(String library);

}


