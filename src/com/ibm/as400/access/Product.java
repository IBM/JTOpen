///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Product.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class Product
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  // Also use this to synchronize access to the user space
  private static final String userSpace_ = "JT4PTF    QTEMP     ";
  private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);

  private boolean loaded_ = false; // Have we retrieved values from the system yet?
  private boolean partiallyLoaded_ = false; // Were we constructed from a ProductList?
  private boolean loaded500_ = false; // Have we loaded the PRDR0500 format values?
  private boolean loaded800_ = false; // Have we loaded the PRDR0500 format values?
  private boolean error100_ = false; // Did we get an error retrieving the PRDR0100 format values?
  private boolean error500_ = false; // Did we get an error retrieving the PRDR0500 format values?
  private boolean error800_ = false; // Did we get an error retrieving the PRDR0800 format values?

  private AS400 system_;  
  private static final AS400Bin4 bin4_ = new AS400Bin4();
  private String productID_;
  private String productOption_;
  private String releaseLevel_;
  private String descriptionID_;
  private String descriptionText_;
  private String messageFile_;
  private boolean installed_;
  private boolean supported_;
  private String registrationType_;
  private String registrationValue_;
  private String loadID_;

  private String loadType_;
  private String symbolicLoadState_;
  private String loadErrorIndicator_;
  private String loadState_;
  private String primaryLanguageLoadID_;
  private String minimumTargetRelease_;
  private String minimumBaseVRM_;
  private String requirementsMet_;
  private String level_;

  // PRDR0500 format  
  private boolean allowsMultipleReleases_;
  private Date releaseDate_;
  private String firstCopyright_;
  private String currentCopyright_;
  // messageFile_;
  // product options and their properties
  private boolean allowsMixedReleases_;

  // PRDR0800 format
  private ProductDirectoryInformation[] directories_;
  private int chunkSize800_ = 8192;

  public static final String PRODUCT_OPTION_BASE = "*BASE";  
  public static final String PRODUCT_FEATURE_CODE = "*CODE";
  public static final String PRODUCT_ID_OPERATING_SYSTEM = "*OPSYS";
  public static final String PRODUCT_RELEASE_CURRENT = "*CUR";
  public static final String PRODUCT_RELEASE_ONLY = "*ONLY";
  public static final String PRODUCT_RELEASE_PREVIOUS = "*PRV";
  private static final String PRODUCT_RELEASE_UNKNOWN = "UNK";

  /**
   * Constructs a Product object. The following default values are used:
   * <UL>
   * <LI>productOption  -  BASE
   * <LI>releaseLevel  -  CURRENT
   * <LI>featureID  -  CODE
   * </UL>
   * @param system The system.
   * @param productID The product identifier.
  **/
  public Product(AS400 system, String productID)
  {
    this(system, productID, PRODUCT_OPTION_BASE, PRODUCT_RELEASE_UNKNOWN, PRODUCT_FEATURE_CODE);
  }

  /**
   * Constructs a Product object.
   * @param system The system.
   * @param productID The product identifier.
   * @param productOption The product option.
   * @param releaseLevel The release level of the product.
   * @param loadID The product load identifier.
  **/   
  public Product(AS400 system, String productID, String productOption, String releaseLevel, String featureID)
  {
    if (system == null) throw new NullPointerException("system");
    if (productID == null) throw new NullPointerException("productID");
    if (productOption == null) throw new NullPointerException("productOption");
    if (releaseLevel == null) throw new NullPointerException("releaseLevel");
    if (featureID == null) throw new NullPointerException("featureID");

    String id = productID.toUpperCase().trim();
    if (id.length() != 7 && !id.equals(PRODUCT_ID_OPERATING_SYSTEM))
    {
      throw new ExtendedIllegalArgumentException("productID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    String option = productOption.toUpperCase().trim();
    if (option.equals(PRODUCT_OPTION_BASE))
    {
      option = "0000";
    }
    else if (option.length() != 4 && !option.equals(PRODUCT_OPTION_BASE))
    {
      throw new ExtendedIllegalArgumentException("productOption", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    String level = releaseLevel.toUpperCase().trim();
    if (level.length() != 6 && level.length() != 0 && !level.equals(PRODUCT_RELEASE_CURRENT) &&
        !level.equals(PRODUCT_RELEASE_ONLY) && !level.equals(PRODUCT_RELEASE_PREVIOUS) &&
        !level.equals(PRODUCT_RELEASE_UNKNOWN))
    {
      throw new ExtendedIllegalArgumentException("releaseLevel", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    String load = featureID.toUpperCase().trim();
    if (load.length() != 4 && !load.equals(PRODUCT_FEATURE_CODE))
    {
      throw new ExtendedIllegalArgumentException("featureID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    system_ = system;
    productID_ = id;
    productOption_ = option;
    releaseLevel_ = level;
    loadID_ = load;
  }


  // Package scope constructor used by ProductList.
  Product(AS400 system, String id, String option, String level,
          String descriptionID, String descriptionText,
          String messageFile, boolean installed,
          boolean supported, String regType, String regValue)
  {
    system_ = system;
    productID_ = id;
    if (option.equals(PRODUCT_OPTION_BASE))
    {
      option = "0000";
    }
    else if (option.length() > 4)
    {
      option = option.substring(1, 5); // Chop off the first 0.
    }
    productOption_ = option;
    releaseLevel_ = level;
    descriptionID_ = descriptionID;
    descriptionText_ = descriptionText;
    messageFile_ = messageFile;
    installed_ = installed;
    supported_ = supported;
    registrationType_ = regType;
    registrationValue_ = regValue;
    partiallyLoaded_ = true;
  }
  

  public boolean allowsMixedReleases()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded500_) refresh(500);
    return allowsMixedReleases_;
  }


  public boolean allowsMultipleReleases()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded500_) refresh(500);
    return allowsMultipleReleases_;
  }


  public String getCurrentCopyright()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded500_) refresh(500);
    return currentCopyright_;
  }


  public String getDescriptionID()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return descriptionID_;
  }


  public String getDescriptionMessageFile()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !loaded500_) refresh(500);
    return messageFile_;
  }


  public String getDescriptionText()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return descriptionText_;
  }


  public ProductDirectoryInformation[] getDirectoryInformation()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded800_) refresh(800);
    return directories_;
  }


  public String getFeatureID()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (loadID_ == null && !loaded_) refresh(100);
    return loadID_;
  }

  public String getFirstCopyright()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded500_) refresh(500);
    return firstCopyright_;
  }

  public String getLevel()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return level_;
  }

  
  public ProductLicense getLicense()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    String feature = getFeatureID();
    String level = getReleaseLevel();
    ProductLicense license = new ProductLicense(system_, productID_, feature, level);
    return license;
  }


  public String getLoadError()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return loadErrorIndicator_;
  }

  public String getLoadState()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return loadState_;
  }

  public String getLoadType()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return loadType_;
  }

  public String getMinimumRequiredReleaseForBase()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return minimumBaseVRM_;
  }

  public String getMinimumTargetRelease()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return minimumTargetRelease_;
  }

  public String getPrimaryLanguageFeatureID()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return primaryLanguageLoadID_;
  }

  public String getProductID()
  {
    return productID_;
  }

  public String getProductOption()
  {
    return productOption_;
  }
  

  public PTF[] getPTFs(boolean includeSupersededPTFs)
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);

    ProgramParameter[] parms = new ProgramParameter[4];
    parms[0] = new ProgramParameter(conv.stringToByteArray(userSpace_));
    try { parms[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE); } catch(PropertyVetoException pve) {}
    byte[] prodInfo = new byte[50];
    AS400Text text4 = new AS400Text(4, ccsid, system_);
    AS400Text text6 = new AS400Text(6, ccsid, system_);
    AS400Text text7 = new AS400Text(7, ccsid, system_);
    AS400Text text10 = new AS400Text(10, ccsid, system_);
    text7.toBytes(productID_, prodInfo, 0);
    text6.toBytes(getReleaseLevel(), prodInfo, 7);
    text4.toBytes(productOption_, prodInfo, 13);
    if (loadID_ == null) refresh(100);
    text10.toBytes((loadID_.equals(PRODUCT_FEATURE_CODE) ? "*ALL" : loadID_), prodInfo, 17);
    //text10.toBytes("*ALL", prodInfo, 17);
    prodInfo[27] = includeSupersededPTFs ? (byte)0xF1 : (byte)0xF0; // '1' or '0'
    parms[1] = new ProgramParameter(prodInfo);
    try { parms[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE); } catch(PropertyVetoException pve) {}
    parms[2] = new ProgramParameter(conv.stringToByteArray("PTFL0100"));
    try { parms[2].setParameterType(ProgramParameter.PASS_BY_REFERENCE); } catch(PropertyVetoException pve) {}
    parms[3] = errorCode_;
    try { parms[3].setParameterType(ProgramParameter.PASS_BY_REFERENCE); } catch(PropertyVetoException pve) {}

    ServiceProgramCall pc = new ServiceProgramCall(system_, "/QSYS.LIB/QPZLSTFX.SRVPGM", "QpzListPTF", ServiceProgramCall.NO_RETURN_VALUE, parms);
    byte[] buf = null;
    synchronized(userSpace_)
    {
      UserSpace us = new UserSpace(system_, "/QSYS.LIB/QTEMP.LIB/JT4PTF.USRSPC");
      us.setMustUseProgramCall(true);
      us.create(256*1024, true, "", (byte)0, "User space for PTF list", "*EXCLUDE");
      try
      {
        if (!pc.run())
        {
          AS400Message[] messages = pc.getMessageList();
          if (messages.length == 1 && (messages[0].getID().equalsIgnoreCase("CPF6601") || // No PTF activity exists
                                       messages[0].getID().equalsIgnoreCase("CPF35BE"))) // Product not supported or installed
          {
            return new PTF[0];
          }
          throw new AS400Exception(pc.getMessageList());
        }

        int size = us.getLength();
        buf = new byte[size];
        us.read(buf, 0);
      }
      finally
      {
        us.close();
      }
    }
    int startingOffset = BinaryConverter.byteArrayToInt(buf, 124);
    int numEntries = BinaryConverter.byteArrayToInt(buf, 132);
    int entrySize = BinaryConverter.byteArrayToInt(buf, 136);
    int entryCCSID = BinaryConverter.byteArrayToInt(buf, 140);
    conv = ConvTable.getTable(entryCCSID, null);
    int offset = 0;
    PTF[] ptfs = new PTF[numEntries];
    for (int i=0; i<numEntries; ++i)
    {
      offset = startingOffset + (i*entrySize);
      String ptfID = conv.byteArrayToString(buf, offset, 7);
      offset += 7;
      String ptfReleaseLevel = conv.byteArrayToString(buf, offset, 6);
      offset += 6;
      String ptfProductOption = conv.byteArrayToString(buf, offset, 4);
      offset += 4;
      String ptfProductLoad = conv.byteArrayToString(buf, offset, 4);
      offset += 4;
      String loadedStatus = conv.byteArrayToString(buf, offset++, 1);
      boolean saveFileExists = (buf[offset++] == (byte)0xF1); // '1' if it exists, 0 if not.
      boolean hasCoverLetter = (buf[offset++] == (byte)0xF1); // '1' if there is a cover letter, 0 if not.
      boolean ptfOnOrder = (buf[offset++] == (byte)0xF1); // '1' if the PTF has been ordered, 0 if it has not or has already been received.
      String iplAction = conv.byteArrayToString(buf, offset++, 1);
      boolean actionPending = (buf[offset++] == (byte)0xF1); // '1' if pending, '0' if not.
      String actionRequired = conv.byteArrayToString(buf, offset++, 1);
      String iplRequired = conv.byteArrayToString(buf, offset++, 1);
      boolean isPTFReleased = (buf[offset++] == (byte)0xF1); // '1' if it's release, 0 if not.
      String minimumLevel = conv.byteArrayToString(buf, offset, 2);
      offset += 2;
      String maximumLevel = conv.byteArrayToString(buf, offset, 2);
      offset += 2;
      String d = conv.byteArrayToString(buf, offset, 13);
      // Parse the date
      Date statusDate = null;
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
        statusDate = cal.getTime();
      }
      ptfs[i] = new PTF(system_, productID_, ptfID, ptfReleaseLevel, ptfProductOption, ptfProductLoad, loadedStatus,
                        saveFileExists, hasCoverLetter, ptfOnOrder, iplAction, actionPending,
                        actionRequired, iplRequired, isPTFReleased, minimumLevel, maximumLevel,
                        statusDate);
    }
    return ptfs;
  }
  

  public String getRegistrationType()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return registrationType_;
  }

  public String getRegistrationValue()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return registrationValue_;
  }

  public Date getReleaseDate()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded500_) refresh(500);
    return releaseDate_;
  }

  public String getReleaseLevel()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (releaseLevel_.equals(PRODUCT_RELEASE_CURRENT) || releaseLevel_.equals(PRODUCT_RELEASE_PREVIOUS) ||
        releaseLevel_.equals(PRODUCT_RELEASE_ONLY) || releaseLevel_.equals(PRODUCT_RELEASE_UNKNOWN))
    {
      refresh(100);
    }
    return releaseLevel_;
  }

  public String getRequirementsMet()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return requirementsMet_;
  }

  public String getSymbolicLoadState()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return symbolicLoadState_;
  }

  public AS400 getSystem()
  {
    return system_;
  }

  public boolean isInstalled()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return installed_;
  }

  public boolean isSupported()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return supported_;
  }


  /**
   * Refreshes the current values and settings for this Product by retrieving all of them from the system.
   * The getter methods implicitly refresh the necessary value if the value being sought has not been retrieved yet.
   * @return true if some or all of the values were successfully refreshed; false if the system found no information
   * for this product.
  **/
  public boolean refresh()
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
  {
    if (!error500_ && !error100_) refresh(500);
    if (!error800_ && !error100_) refresh(800);
    if (error500_ && error800_ && !error100_) refresh(100);
    return !error100_;
  }


  /**
   * Does the real work based on the format requested.
   * Formats currently supported are 100, 500, and 800. Note that
   * all formats are supersets of the 100 format.
  **/
  private void refresh(int whichFormat)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
  {
    if (releaseLevel_.equals(PRODUCT_RELEASE_UNKNOWN))
    {
      // First try CURRENT, then PREVIOUS, then ONLY.
      error100_ = false;
      releaseLevel_ = PRODUCT_RELEASE_CURRENT;
      try
      {
        refresh(100);
      }
      catch(AS400Exception x) {}
      if (error100_)
      {
        error100_ = false;
        releaseLevel_ = PRODUCT_RELEASE_PREVIOUS;
        try
        {
          refresh(100);
        }
        catch(AS400Exception x) {}
        if (error100_)
        {
          error100_ = false;
          releaseLevel_ = PRODUCT_RELEASE_ONLY;
          refresh(whichFormat);
          return;
        }
      }
    }
    
    String format = null;
    int len = 0;
    switch(whichFormat)
    {
      case 100:
        if (error100_) return; // No point in trying again.
        format = "PRDR0100";
        len = 108;
        break;
      case 500:
        if (error500_) return; // No point in trying again.
        format = "PRDR0500";
        len = 108+49; // Don't need all the info.
        break;
      case 800:
        if (error800_) return; // No point in trying again.
        format = "PRDR0800";
        len = 108+17+chunkSize800_; // In real life, len = 108+17 + (50+primaryPath+installPath+(10*numberOfObjectAuthorities)*numberOfEntries
        break;
      default:
        format = "PRDR0100";
        whichFormat = 100;
        len = 108;
        break;
    }
    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);

    ProgramParameter[] parms = new ProgramParameter[6];
    parms[0] = new ProgramParameter(len); // receiver variable
    parms[1] = new ProgramParameter(bin4_.toBytes(len)); // length of receiver variable
    parms[2] = new ProgramParameter(conv.stringToByteArray(format)); // format name
    byte[] productInfo = new byte[36];
    AS400Text text4 = new AS400Text(4, ccsid, system_);
    AS400Text text6 = new AS400Text(6, ccsid, system_);
    AS400Text text7 = new AS400Text(7, ccsid, system_);
    AS400Text text10 = new AS400Text(10, ccsid, system_);
    text7.toBytes(productID_, productInfo, 0);
    text6.toBytes(releaseLevel_, productInfo, 7);
    String option = productOption_;
    if (option.equals(PRODUCT_OPTION_BASE) || whichFormat == 500) // PRDR0500 needs 0000
    {
      option = "0000";
    }
    text4.toBytes(option, productInfo, 13);
    text10.toBytes((loadID_ == null || whichFormat == 500) ? PRODUCT_FEATURE_CODE : loadID_, productInfo, 17); // PRDR0500 needs *CODE
    BinaryConverter.intToByteArray(36, productInfo, 28);
    BinaryConverter.intToByteArray(ccsid, productInfo, 32);
    parms[3] = new ProgramParameter(productInfo); // product information
    parms[4] = new ProgramParameter(bin4_.toBytes(0)); // error code
    parms[5] = new ProgramParameter(conv.stringToByteArray("PRDI0200")); // product information format name

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QSZRTVPR.PGM", parms);
    if (!pc.run())
    {
      AS400Message[] messages = pc.getMessageList();
      if (messages.length == 1 && messages[0].getID().equalsIgnoreCase("CPF0C1F"))
      {
        // It's OK, there just wasn't a product definition.
        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "No product definition found for "+toString()+" using format "+whichFormat+".");
        }
        if (whichFormat != 100)
        {
          refresh(100);
          if (whichFormat == 500)
          {
            releaseDate_ = null;
            firstCopyright_ = null;
            currentCopyright_ = null;
            loaded500_ = true;
            error500_ = true;
          }
          else if (whichFormat == 800)
          {
            directories_ = new ProductDirectoryInformation[0];
            loaded500_ = true;
            error800_ = true;
          }
          return;
        }
        else
        {
          // The 0500 format may work since it uses a different product option than what we are set to.
          // But the 0100 or 0800 formats may still fail since we switch back to use the option we were given.
          error100_ = true;
          throw new AS400Exception(messages);
        }
      }
      throw new AS400Exception(messages);
    }

    byte[] outputData = parms[0].getOutputData();
    releaseLevel_ = conv.byteArrayToString(outputData, 19, 6);
    loadID_ = conv.byteArrayToString(outputData, 29, 4);
    loadType_ = conv.byteArrayToString(outputData, 33, 10);
    symbolicLoadState_ = conv.byteArrayToString(outputData, 43, 10);
    loadErrorIndicator_ = conv.byteArrayToString(outputData, 53, 10);
    loadState_ = conv.byteArrayToString(outputData, 63, 2);
    supported_ = conv.byteArrayToString(outputData, 65, 1).equals("1");
    registrationType_ = conv.byteArrayToString(outputData, 66, 2);
    registrationValue_ = conv.byteArrayToString(outputData, 68, 14);
    primaryLanguageLoadID_ = conv.byteArrayToString(outputData, 88, 4);
    minimumTargetRelease_ = conv.byteArrayToString(outputData, 92, 6);
    minimumBaseVRM_ = conv.byteArrayToString(outputData, 98, 6);
    requirementsMet_ = conv.byteArrayToString(outputData, 104, 1);
    level_ = conv.byteArrayToString(outputData, 105, 3);
    loaded_ = true;

    if (whichFormat == 500)
    {
      int offset = BinaryConverter.byteArrayToInt(outputData, 84);
      allowsMultipleReleases_ = (outputData[offset++] == (byte)0xF1); // '1' is yes, '0' is no.
      boolean y2k = (outputData[offset++] == (byte)0xF1); // '1' is 20xx; '0' is 19xx
      String d = conv.byteArrayToString(outputData, offset, 6);
      Date releaseDate = null;
      if (d.trim().length() == 6)
      {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Integer.parseInt(d.substring(0,2)) + (y2k ? 2000 : 1900), // year
                Integer.parseInt(d.substring(2,4))-1,      // month is zero based
                Integer.parseInt(d.substring(4,6)));        // day
        releaseDate = cal.getTime();
      }
      releaseDate_ = releaseDate;
      offset += 6;
      firstCopyright_ = conv.byteArrayToString(outputData, offset, 4);
      offset += 4;
      currentCopyright_ = conv.byteArrayToString(outputData, offset, 4);
      offset += 4;
      String fileName = conv.byteArrayToString(outputData, offset, 10).trim();
      offset += 10;
      String fileLibrary = conv.byteArrayToString(outputData, offset, 10).trim();
      offset += 10;
      if (fileName.length() > 0)
      {
        messageFile_ = QSYSObjectPathName.toPath(fileLibrary, fileName, "MSGF");
      }
      else
      {
        messageFile_ = "";
      }
      offset += 12;
      allowsMixedReleases_ = (outputData[offset] == (byte)0xF1); // '1' allows, '0' doesn't.
      loaded500_ = true;
    }
    else if (whichFormat == 800)
    {
      int offset = BinaryConverter.byteArrayToInt(outputData, 84);
      int numEntries = BinaryConverter.byteArrayToInt(outputData, offset);
      if (numEntries == 0)
      {
        directories_ = new ProductDirectoryInformation[0];
        return;
      }
      offset += 4;
      int entryLength = BinaryConverter.byteArrayToInt(outputData, offset);
      offset += 4;
      int entryOffset = BinaryConverter.byteArrayToInt(outputData, offset);
      int requiredSize = numEntries*entryLength;
      if (entryOffset == 0 || (requiredSize > chunkSize800_))
      {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "Increasing Product format 800 chunk size from "+chunkSize800_+" to "+requiredSize+" and re-retrieving.");
        }
        chunkSize800_ = requiredSize;
        refresh(800);
        return;
      }
      offset += 4;
      int entryCCSID = BinaryConverter.byteArrayToInt(outputData, offset);
      offset += 4;
      boolean ccsidConversionSucceeded = (outputData[offset++] == (byte)0xF0); // '0' means it worked, '1' means it failed and we should use the entryCCSID
      if (!ccsidConversionSucceeded)
      {
        ccsid = entryCCSID;
        conv = ConvTable.getTable(ccsid, null);
      }
      ProductDirectoryInformation[] info = new ProductDirectoryInformation[numEntries];
      for (int i=0; i<numEntries; ++i)
      {
        offset = entryOffset + (i*entryLength);
        int primaryPathLength = BinaryConverter.byteArrayToInt(outputData, offset);
        offset += 4;
        int primaryPathOffset = BinaryConverter.byteArrayToInt(outputData, offset);
        if (primaryPathOffset == 0)
        {
          if (Trace.traceOn_)
          {
            Trace.log(Trace.DIAGNOSTIC, "Increasing Product format 800 chunk size from "+chunkSize800_+" to "+chunkSize800_*2+" and re-retrieving.");
          }
          chunkSize800_ = chunkSize800_*2;
          refresh(800);
          return;
        }
        //offset += 4;
        //int primaryHomeLength = BinaryConverter.byteArrayToInt(outputData, offset);
        //offset += 4;
        //int primaryHomeOffset = BinaryConverter.byteArrayToInt(outputData, offset);
        //offset += 4;
        offset += 12;
        int installedPathLength = BinaryConverter.byteArrayToInt(outputData, offset);
        offset += 4;
        int installedPathOffset = BinaryConverter.byteArrayToInt(outputData, offset);
        if (installedPathOffset == 0)
        {
          if (Trace.traceOn_)
          {
            Trace.log(Trace.DIAGNOSTIC, "Increasing Product format 800 chunk size from "+chunkSize800_+" to "+chunkSize800_*2+" and re-retrieving.");
          }
          chunkSize800_ = chunkSize800_*2;
          refresh(800);
          return;
        }
        //offset += 4;
        //int installedHomeLength = BinaryConverter.byteArrayToInt(outputData, offset);
        //offset += 4;
        //int installedHomeOffset = BinaryConverter.byteArrayToInt(outputData, offset);
        //offset += 4;
        offset += 12;
        int numAuthorities = BinaryConverter.byteArrayToInt(outputData, offset);
        offset += 4;
        int offsetAuthorities = BinaryConverter.byteArrayToInt(outputData, offset);
        if (offsetAuthorities == 0)
        {
          if (Trace.traceOn_)
          {
            Trace.log(Trace.DIAGNOSTIC, "Increasing Product format 800 chunk size from "+chunkSize800_+" to "+chunkSize800_*2+" and re-retrieving.");
          }
          chunkSize800_ = chunkSize800_*2;
          refresh(800);
          return;
        }
        offset += 4;
        String publicDataAuthority = conv.byteArrayToString(outputData, offset, 10);
        String primaryFullPath = conv.byteArrayToString(outputData, primaryPathOffset, primaryPathLength);
        String installedFullPath = conv.byteArrayToString(outputData, installedPathOffset, installedPathLength);
        String[] authorities = new String[numAuthorities];
        for (int j=0; j<numAuthorities; ++j)
        {
          authorities[j] = conv.byteArrayToString(outputData, offsetAuthorities+(j*10), 10);
        }
        info[i] = new ProductDirectoryInformation(publicDataAuthority, primaryFullPath, installedFullPath, authorities);
      }
      directories_ = info;
      loaded800_ = true;
    }
  }


  public String toString()
  {
    StringBuffer buf = new StringBuffer(productID_);
    buf.append('/');
    buf.append(productOption_);
    buf.append('/');
    buf.append(releaseLevel_);
    return buf.toString();
  }
}
