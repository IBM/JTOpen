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
package com.ibm.jtopenlite.command.program.object;

import com.ibm.jtopenlite.command.program.openlist.*;

public interface OpenListOfObjectsFormatListener extends ListFormatListener
{
  public void newObjectEntry(String objectNameUsed, String objectLibraryUsed,
                             String objectTypeUsed, String informationStatus, int numberOfFieldsReturned);

  public void newObjectFieldData(int lengthOfFieldInformation, int keyField, String typeOfData, int lengthOfData, int offsetToData, byte[] tempDataBuffer);
}
