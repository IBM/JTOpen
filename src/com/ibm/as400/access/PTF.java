///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PTF.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class PTF
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private AS400 system_;
  private String productID_;
  private boolean actionPending_;
  private String actionRequired_;
  private boolean hasCoverLetter_;
  private String iplAction_;
  private String iplRequired_;
  private String loadedStatus_;
  private String maximumLevel_;
  private String minimumLevel_;
  private boolean ptfOnOrder_;
  private String ptfProductLoad_;
  private String ptfProductOption_;
  private String ptfID_;
  private boolean isPTFReleased_;
  private String ptfReleaseLevel_;
  private boolean saveFileExists_;
  private Date statusDate_;

  private String currentIPLSource_;
  private String licGroup_;
  private String ptfType_;
  private String saveFile_;  
  private String supersedingPTF_;
  private String targetRelease_;

  private boolean loaded_ = false;
  private boolean partiallyLoaded_ = false;
  private boolean loaded200_ = false;
  private int chunkSize200_ = 8192;

  // PTFR0200
  private PTFCoverLetter[] coverLetters_;

  public static final String PRODUCT_ID_ONLY = "*ONLY";
  public static final String PRODUCT_RELEASE_ONLY = "*ONLY";

  public PTF(AS400 system, String ptfID, String productID, String releaseLevel)
  {
    if (system == null) throw new NullPointerException("system");
    if (ptfID == null) throw new NullPointerException("ptfID");
    if (productID == null) throw new NullPointerException("productID");
    if (releaseLevel == null) throw new NullPointerException("releaseLevel");

    system_ = system;
    String id = ptfID.toUpperCase().trim();
    if (id.length() != 7)
    {
      throw new ExtendedIllegalArgumentException("ptfID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    String prod = productID.toUpperCase().trim();
    if (prod.length() != 7 && !prod.equals(PRODUCT_ID_ONLY))
    {
      throw new ExtendedIllegalArgumentException("productID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    String release = releaseLevel.toUpperCase().trim();
    if (!prod.equals(PRODUCT_RELEASE_ONLY) && release.length() != 6)
    {
      throw new ExtendedIllegalArgumentException("releaseLevel", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    ptfID_ = id;
    productID_ = prod;
    ptfReleaseLevel_ = release;
  }


  PTF(AS400 system, String productID, String ptfID, String ptfReleaseLevel, String ptfProductOption, String ptfProductLoad,
      String loadedStatus, boolean saveFileExists, boolean hasCoverLetter, boolean ptfOnOrder,
      String iplAction, boolean actionPending, String actionRequired, String iplRequired,
      boolean isPTFReleased, String minimumLevel, String maximumLevel, Date statusDate)
  {
    system_ = system;
    productID_ = productID;
    ptfID_ = ptfID;
    ptfReleaseLevel_ = ptfReleaseLevel;
    ptfProductOption_ = ptfProductOption;
    ptfProductLoad_ = ptfProductLoad_;
    loadedStatus_ = loadedStatus;
    saveFileExists_ = saveFileExists;
    hasCoverLetter_ = hasCoverLetter;
    ptfOnOrder_ = ptfOnOrder;
    iplAction_ = iplAction;
    actionPending_ = actionPending;
    actionRequired_ = actionRequired;
    iplRequired_ = iplRequired;
    isPTFReleased_ = isPTFReleased;
    minimumLevel_ = minimumLevel;
    maximumLevel_ = maximumLevel;
    statusDate_ = statusDate;
    partiallyLoaded_ = true;
  }

  public String getActionRequired()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return actionRequired_;
  }

  public PTFCoverLetter[] getCoverLetters()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded200_) refresh(200);
    return coverLetters_;
  }


  public String getCurrentIPLSource()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh();
    return currentIPLSource_;
  }

  public String getID()
  {
    return ptfID_;
  }

  public String getIPLAction()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return iplAction_;
  }

  public String getIPLRequired()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return iplRequired_;
  }

  public String getLICGroup()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh();
    return licGroup_;
  }

  public String getLoadedStatus()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return loadedStatus_;
  }

  public String getMaximumLevel()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return maximumLevel_;
  }

  public String getMinimumLevel()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return minimumLevel_;
  }

  public String getProductFeature()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return ptfProductLoad_;
  }

  public String getProductID()
  {
    return productID_;
  }

  public String getProductOption()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return ptfProductOption_;
  }

  public String getReleaseLevel()
  {
    return ptfReleaseLevel_;
  }


  public String getSaveFile()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh();
    return saveFile_;
  }


  public Date getStatusDate()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return statusDate_;
  }

  public String getSupersedingPTF()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh();
    return supersedingPTF_;
  }

  public String getTargetRelease()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh();
    return targetRelease_;
  }

  public String getType()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh();
    return ptfType_;
  }

  public boolean hasCoverLetter()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return hasCoverLetter_;
  }

  public boolean hasSaveFile()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return saveFileExists_;
  }

  public boolean isActionPending()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return actionPending_;
  }

  public boolean isOnOrder()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return ptfOnOrder_;
  }

  public boolean isReleased()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh();
    return isPTFReleased_;
  }


  public void refresh()
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
  {
    refresh(200);
  }


  private void refresh(int whichFormat)
    throws AS400Exception,
           AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
  {
    String format = null;
    int len = 0;
    switch(whichFormat)
    {
      case 100:
        format = "PTFR0100";
        len = 108;
        break;
      case 200:
        format = "PTFR0200";
        len = 108+12+chunkSize200_; // 108+12+(36*numberOfCoverLetters)
        break;
      default:
        format = "PTFR0100";
        len = 108;
        break;
    }
    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);

    ProgramParameter[] parms = new ProgramParameter[5];
    parms[0] = new ProgramParameter(len); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
    byte[] ptfInfo = new byte[50];
    AS400Text text7 = new AS400Text(7, ccsid, system_);
    AS400Text text6 = new AS400Text(6, ccsid, system_);
    AS400Text text25 = new AS400Text(25, ccsid, system_);
    text7.toBytes(ptfID_, ptfInfo, 0);
    text7.toBytes(productID_, ptfInfo, 7);
    text6.toBytes(ptfReleaseLevel_, ptfInfo, 14);
    ptfInfo[24] = (byte)0xF0; // '0' means close the PTF database when query is done.
    text25.toBytes(" ", ptfInfo, 25);
    parms[2] = new ProgramParameter(ptfInfo); // PTF information
    parms[3] = new ProgramParameter(conv.stringToByteArray(format)); // format name
    parms[4] = new ProgramParameter(BinaryConverter.intToByteArray(0)); // error code

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QPZRTVFX.PGM", parms);
    pc.setThreadSafe(false);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] output = parms[0].getOutputData();
    ptfProductOption_ = conv.byteArrayToString(output, 32, 4);
    ptfProductLoad_ = conv.byteArrayToString(output, 36, 4);
    loadedStatus_ = conv.byteArrayToString(output, 40, 1);
    hasCoverLetter_ = (output[41] == (byte)0xF1);
    ptfOnOrder_ = (output[42] == (byte)0xF1);
    saveFileExists_ = (output[43] == (byte)0xF1);
    if (saveFileExists_)
    {
      String fileName = conv.byteArrayToString(output, 44, 10).trim();
      String fileLibrary = conv.byteArrayToString(output, 54, 10).trim();
      saveFile_ = QSYSObjectPathName.toPath(fileLibrary, fileName, "SAVF");
    }
    else
    {
      saveFile_ = "";
    }
    ptfType_ = conv.byteArrayToString(output, 64, 1);
    iplAction_ = conv.byteArrayToString(output, 65, 1);
    actionPending_ = (output[66] == (byte)0xF1);
    actionRequired_ = conv.byteArrayToString(output, 67, 1);
    isPTFReleased_ = (output[68] == (byte)0xF1);
    targetRelease_ = conv.byteArrayToString(output, 69, 6);
    supersedingPTF_ = conv.byteArrayToString(output, 75, 7);
    currentIPLSource_ = conv.byteArrayToString(output, 82, 1);
    minimumLevel_ = conv.byteArrayToString(output, 83, 2);
    maximumLevel_ = conv.byteArrayToString(output, 85, 2);
    // formatInformationAvailable_;
    String d = conv.byteArrayToString(output, 88, 13);
    // Parse the date
    if (d.trim().length() == 13)
    {
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(Integer.parseInt(d.substring(0,3)) + 1900, // year
              Integer.parseInt(d.substring(3,5))-1,     // month is zero based
              Integer.parseInt(d.substring(5,7)),       // day
              Integer.parseInt(d.substring(7,9)),       // hour
              Integer.parseInt(d.substring(9,11)),      // minute
              Integer.parseInt(d.substring(11,13)));    // second
      statusDate_ = cal.getTime();
    }
    else
    {
      statusDate_ = null;
    }
    licGroup_ = conv.byteArrayToString(output, 101, 7);
    loaded_ = true;

    if (whichFormat == 200)
    {
      int offset = BinaryConverter.byteArrayToInt(output, 8);
      int entryOffset = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int numberOfNLVs = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int entryLength = BinaryConverter.byteArrayToInt(output, offset);
      int newSize = numberOfNLVs*entryLength;
      if (newSize > chunkSize200_)
      {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "Increasing PTF cover letter chunk size from "+chunkSize200_+" to "+newSize+" and re-retrieving.");
        }
        chunkSize200_ = newSize;
        refresh(200);
        return;
      }
      PTFCoverLetter[] records = new PTFCoverLetter[numberOfNLVs];
      for (int i=0; i<numberOfNLVs; ++i)
      {
        offset = entryOffset + (i*entryLength);
        String nlv = conv.byteArrayToString(output, offset, 4);
        offset += 4;
        String fileName = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        String fileLibrary = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        String fileMember = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        String preInstructions = conv.byteArrayToString(output, offset++, 1);
        String postInstructions = conv.byteArrayToString(output, offset, 1);
        String path = QSYSObjectPathName.toPath(fileLibrary, fileName, fileMember, "MBR");
        records[i] = new PTFCoverLetter(system_, nlv, path, preInstructions, postInstructions);
      }
      coverLetters_ = records;
      loaded200_ = true;
    }
  }


  public String toString()
  {
    StringBuffer buf = new StringBuffer(ptfID_);
    buf.append('/');
    buf.append(ptfReleaseLevel_);
    buf.append('/');
    buf.append(productID_);
    return buf.toString();
  }
}


