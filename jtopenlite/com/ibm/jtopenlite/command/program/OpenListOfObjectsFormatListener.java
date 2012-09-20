///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfObjectsFormatListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

/**
 * 
 * @deprecated Use classes in package jtopnlite.command.program.object instead
 *
 */
public interface OpenListOfObjectsFormatListener
{
  public void newObjectEntry(String objectNameUsed, String objectLibraryUsed,
                             String objectTypeUsed, String informationStatus, int numberOfFieldsReturned);

  public void newObjectFieldData(int lengthOfFieldInformation, int keyField, String typeOfData, int lengthOfData, int offsetToData, byte[] tempDataBuffer);
}
