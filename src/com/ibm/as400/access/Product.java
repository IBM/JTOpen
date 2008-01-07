///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  Product.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;


/**
 * Represents a licensed product on the system. The {@link #isInstalled isInstalled()}
 * method should be called to verify the
 * product is installed on the system. If it is not, other information returned by getters in this
 * class may not be valid.
**/
public class Product
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  // Also use this to synchronize access to the user space
  private static final String userSpace_ = "JT4PTF    QTEMP     ";

  private boolean loaded_ = false; // Have we retrieved values from the system yet
  private boolean partiallyLoaded_ = false; // Were we constructed from a ProductList
  private boolean loadedOptions_ = false; // Have we loaded our option information
  private boolean loadedDescriptionText_ = false; // Have we loaded the description yet
  private boolean loaded500_ = false; // Have we loaded the PRDR0500 format values
  private boolean loaded800_ = false; // Have we loaded the PRDR0500 format values
  private boolean error100_ = false; // Did we get an error retrieving the PRDR0100 format values
  private boolean error500_ = false; // Did we get an error retrieving the PRDR0500 format values
  private boolean error800_ = false; // Did we get an error retrieving the PRDR0800 format values

  private AS400 system_;            // never null
  private String productID_;        // never null
  private String productOption_;    // never null
  private String releaseLevel_;     // never null
  private String descriptionID_;
  private String descriptionText_;
  private String messageFile_;
  private boolean installed_ = true; // Default to true; if the API throws an exception, switch to false.
  private boolean supported_;
  private String registrationType_;
  private String registrationValue_;
  private String loadID_;

  private String loadType_;
  private String symbolicLoadState_;
  private boolean loadErrorIndicator_;
  private String loadState_;
  private String primaryLanguageLoadID_;
  private String minimumTargetRelease_;
  private String minimumBaseVRM_;
  private int requirementsMet_;
  private String level_;

  // PRDR0500 format
  private boolean allowsMultipleReleases_;
  private Date releaseDate_;
  private String firstCopyright_;
  private String currentCopyright_;
  // messageFile_;
  // product options and their properties
  private boolean allowsDynamicNaming_;
  private String minimumVRM_;

  private boolean allowsMixedReleases_;

  // PRDR0800 format
  private ProductDirectoryInformation[] directories_;
  private Product[] options_;
  private int chunkSize_ = 8192;



  /**
   * Constant indicating that the product load is defined but
   * the product load object for this load does not exist. When
   * a product definition is created, a code load is defined for
   * each product option, and language loads can be defined.
  **/
  public static final String LOAD_STATE_DEFINED_NO_OBJECT = "10";

  /**
   * Constant indicating that the product load object exists, but
   * before it can be saved using the Save Licensed Program (SAVLICPGM)
   * command, it must be packaged with either the Package Product
   * Option (PKGPRDOPT) command or the Package Product Option
   * (QSZPKGPO) API.
  **/
  public static final String LOAD_STATE_DEFINED_OBJECT_EXISTS = "20";

  /**
   * Constant indicating that a Restore Licensed Program (RSTLICPGM)
   * command did not complete successfully. A preoperation exit program
   * failed. The product being replaced had been packaged, but not installed.
  **/
  public static final String LOAD_STATE_PACKAGED_RESTORE_FAILED_EXIT_PROGRAM_FAILED = "3E";

  /**
   * Constant indicating that a Restore Licensed Program (RSTLICPGM)
   * command failed. A preoperation exit program did not fail. The product
   * being replaced had been packaged, but not installed.
  **/
  public static final String LOAD_STATE_PACKAGED_RESTORE_FAILED = "3F";

  /**
   * Constant indicating that the product load object for this load has
   * been packaged with the Package Product Option (PKGPRDOPT) command
   * or the Package Product Option (QSZPKGPO) API.
  **/
  public static final String LOAD_STATE_PACKAGED = "30";

  /**
   * Constant indicating that the product load object for this load has
   * been packaged with the Package Product Option (PKGPRDOPT) command
   * or the Package Product Option (QSZPKGPO) API, but either a
   * development library or folder was renamed when the product does
   * not allow dynamic naming, or the product definition or product load
   * for a packaged load was renamed or moved to another library.
  **/
  public static final String LOAD_STATE_PACKAGED_RENAMED = "32";

  /**
   * Constant indicating that the product load object for this load has
   * been packaged with the Package Product Option (PKGPRDOPT) command
   * or the Package Product Option (QSZPKGPO) API, but an object was
   * found to be damaged the last time that the Check Product Option
   * (CHKPRDOPT) command or Save Licensed Program (SAVLICPGM) command
   * was used for this load.
  **/
  public static final String LOAD_STATE_PACKAGED_DAMAGED = "33";

  /**
   * Constant indicating that the product load object for this load has
   * been packaged with the Package Product Option (PKGPRDOPT) command
   * or the Package Product Option (QSZPKGPO) API, but either an attempt
   * was made to delete the product load using the Delete Licensed Program
   * (DLTLICPGM) command and the attempt failed, or a
   * packaged object was missing the last time the Check Product Option
   * (CHKPRDOPT) command or Save Licensed Program (SAVLICPGM) command was
   * used for this load.
  **/
  public static final String LOAD_STATE_PACKAGED_DELETED = "34";

  /**
   * Constant indicating that a Restore Licensed Program (RSTLICPGM) command
   * is in progress. The product being replaced had been packaged, but
   * not installed.
  **/
  public static final String LOAD_STATE_PACKAGED_RESTORE_IN_PROGRESS = "35";

  /**
   * Constant indicating that a Delete Licensed Program (DLTLICPGM) command
   * is in progress. The product being deleted had been packaged, but
   * not installed.
  **/
  public static final String LOAD_STATE_PACKAGED_DELETE_IN_PROGRESS = "38";

  /**
   * Constant indicating that a Restore Licensed Program (RSTLICPGM) command
   * is in progress. The product being replaced had been installed.
  **/
  public static final String LOAD_STATE_INSTALLED_RESTORE_IN_PROGRESS = "50";

  /**
   * Constant indicating that a Delete Licensed Program (DLTLICPGM) command
   * is in progress. The product being deleted had been installed.
  **/
  public static final String LOAD_STATE_INSTALLED_DELETE_IN_PROGRESS = "53";

  /**
   * Constant indicating that this product is an IBM-supplied product and
   * it is not compatible with the currently installed release level of the System i operating system.
   * An error occurred when the product was restored or when the operating system was installed.
   * The IBM-supplied product is at a release level earlier than V2R2M0, which
   * is not supported by the Save Licensed Program (SAVLICPGM) command.
  **/
  public static final String LOAD_STATE_IBM_SUPPLIED_NOT_COMPATIBLE = "59";

  /**
   * Constant indicating that a Restore Licensed Program (RSTLICPGM) command
   * did not complete successfully. A preoperation exit program failed. The
   * product being replaced had been installed.
  **/
  public static final String LOAD_STATE_INSTALLED_RESTORE_FAILED_EXIT_PROGRAM_FAILED = "6E";

  /**
   * Constant indicating that a Restore Licensed Program (RSTLICPGM) command
   * failed. The failure was not a preoperation exit program or postoperation
   * exit program. The product being replaced had been installed.
  **/
  public static final String LOAD_STATE_INSTALLED_RESTORE_FAILED = "6F";

  /**
   * Constant indicating that the product load object for this load was loaded
   * on to the system by the Restore Licensed Program (RSTLICPGM) command.
  **/
  public static final String LOAD_STATE_RESTORED = "60";

  /**
   * Constant indicating that the product load object for this load was loaded
   * on to the system by the Restore Licensed Program (RSTLICPGM) command but
   * a postoperation exit program failed.
  **/
  public static final String LOAD_STATE_RESTORED_EXIT_PROGRAM_FAILED = "61";

  /**
   * Constant indicating that an installed library or folder was renamed, but
   * the product does not allow dynamic naming.
  **/
  public static final String LOAD_STATE_RESTORED_RENAMED = "62";

  /**
   * Constant indicating that the product load object for this load was installed
   * by the Restore Licensed Program (RSTLICPGM) command, but an object is
   * damaged.
  **/
  public static final String LOAD_STATE_RESTORED_DAMAGED = "63";

  /**
   * Constant indicating that the product load object for this load was installed
   * by the Restore Licensed Program (RSTLICPGM) command, but either an object
   * was found to be missing when the Check Product Option (CHKPRDOPT) command
   * or the Save Licensed Program (SAVLICPGM) command was used, or an error
   * occurred while the Delete Licensed Program (DLTLICPGM) command was being
   * used.
  **/
  public static final String LOAD_STATE_RESTORED_DELETED = "64";

  /**
   * Constant indicating that the Check Product Option (CHKPRDOPT) command was
   * used for this product load, but the postoperation exit program failed or
   * indicated that an error was found.
  **/
  public static final String LOAD_STATE_CHECK_ERROR = "67";

  /**
   * Constant indicating that the product load was installed successfully. If an
   * object was missing or was damaged, but the problem was corrected, using
   * the Check Product Option (CHKPRDOPT) command sets the state back to this.
  **/
  public static final String LOAD_STATE_INSTALLED = "90";



  /**
   * Constant indicating a product load type of *CODE.
  **/
  public static final String LOAD_TYPE_CODE = "*CODE";

  /**
   * Constant indicating a product load type of *LNG.
  **/
  public static final String LOAD_TYPE_LANGUAGE = "*LNG";



  /**
   * Constant indicating a product option of *BASE.
  **/
  public static final String PRODUCT_OPTION_BASE = "*BASE";

  /**
   * Constant indicating a feature ID of *CODE.
  **/
  public static final String PRODUCT_FEATURE_CODE = "*CODE";

  /**
   * Constant indicating a product ID of *OPSYS.
  **/
  public static final String PRODUCT_ID_OPERATING_SYSTEM = "*OPSYS";

  /**
   * Constant indicating a release level of *CUR.
  **/
  public static final String PRODUCT_RELEASE_CURRENT = "*CUR";

  /**
   * Constant indicating a release level of *ONLY.
  **/
  public static final String PRODUCT_RELEASE_ONLY = "*ONLY";

  /**
   * Constant indicating a release level of *PRV.
  **/
  public static final String PRODUCT_RELEASE_PREVIOUS = "*PRV";

  /**
   * Constant indicating that the release level of the product
   * should be determined at runtime by the system.
  **/
  public static final String PRODUCT_RELEASE_ANY = "ANY";



  /**
   * Constant indicating that the registration type *PHONE was
   * specified when the product load or product definition was
   * created.
  **/
  public static final String REGISTRATION_TYPE_PHONE = "02";

  /**
   * Constant indicating that the registration type is the
   * same as the registration type for the operating system.
  **/
  public static final String REGISTRATION_TYPE_SYSTEM = "04";

  /**
   * Constant indicating that the registration type *CUSTOMER was
   * specified when the product load or product definition was
   * created.
  **/
  public static final String REGISTRATION_TYPE_CUSTOMER = "08";



  /**
   * Constant indicating that there is not enough information
   * available to determine if the release requirements have
   * been met. This will be the value if the load type is
   * LOAD_TYPE_LANGUAGE.
  **/
  public static final int REQUIREMENTS_UNKNOWN = 0;

  /**
   * Constant indicating that the releases of the *BASE and option
   * meet all requirements.
  **/
  public static final int REQUIREMENTS_MET = 1;

  /**
   * Constant indicating that the release of the option is too
   * old compared to the *BASE.
  **/
  public static final int REQUIREMENTS_OPTION_TOO_OLD = 2;

  /**
   * Constant indicating that the release of the *BASE is too
   * old compared to the option.
  **/
  public static final int REQUIREMENTS_BASE_TOO_OLD = 3;



  /**
   * Constant indicating that the load is defined but the product load
   * object for this load does not exist.
  **/
  public static final String SYMBOLIC_LOAD_STATE_DEFINED = "*DEFINED";

  /**
   * Constant indicating that the product load object for this load
   * exists. It must be packaged with the Package Product Option (PKGPRDOPT)
   * command before it can be saved using the Save Licensed Program (SAVLICPGM) command.
  **/
  public static final String SYMBOLIC_LOAD_STATE_CREATED = "*CREATED";

  /**
   * Constant indicating that the product load object for this load has
   * been packaged with the Package Product Option (PKGPRDOPT) command.
  **/
  public static final String SYMBOLIC_LOAD_STATE_PACKAGED = "*PACKAGED";

  /**
   * Constant indicating that either the product load object has been damaged
   * (if this option is something other than the base option or the load type
   * is a language load), or the product definition for this product ID and
   * release level has been damaged or the product load object has been damaged
   * (if this option is for the base option and code load type).
  **/
  public static final String SYMBOLIC_LOAD_STATE_DAMAGED = "*DAMAGED";

  /**
   * Constant indicating that either a Restore Licensed Program (RSTLICPGM)
   * function is in progress, a Delete Licensed Program (DLTLICPGM) function
   * is in progress, or the product was created previous to V2R2M0 and there
   * was an error during the process of converting product information.
  **/
  public static final String SYMBOLIC_LOAD_STATE_LOADED = "*LOADED";

  /**
   * Constant indicating that the product load object for this load was loaded
   * on to the system by the Restore Licensed Program (RSTLICPGM) command.
  **/
  public static final String SYMBOLIC_LOAD_STATE_INSTALLED = "*INSTALLED";



  /**
   * Constructs a Product object. The following default values are used:
   * <UL>
   * <LI>productOption  -  PRODUCT_OPTION_BASE
   * <LI>releaseLevel  -  PRODUCT_RELEASE_ANY
   * <LI>featureID  -  PRODUCT_FEATURE_CODE
   * </UL>
   * @param system The system.
   * @param productID The product identifier.
  **/
  public Product(AS400 system, String productID)
  {
    this(system, productID, PRODUCT_OPTION_BASE, PRODUCT_RELEASE_ANY, PRODUCT_FEATURE_CODE);
  }



  /**
   * Constructs a Product object. The following default values are used:
   * <UL>
   * <LI>releaseLevel  -  PRODUCT_RELEASE_ANY
   * <LI>featureID  -  PRODUCT_FEATURE_CODE
   * </UL>
   * @param system The system.
   * @param productID The product identifier.
   * @param productOption The product option.
  **/
  public Product(AS400 system, String productID, String productOption)
  {
    this(system, productID, productOption, PRODUCT_RELEASE_ANY, PRODUCT_FEATURE_CODE);
  }


  /**
   * Constructs a Product object.
   * @param system The system.
   * @param productID The product identifier.
   * @param productOption The product option.
   * @param releaseLevel The release level of the product.
   * @param featureID The product feature identifier.
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
    if (option.length() > 4)
    {
      throw new ExtendedIllegalArgumentException("productOption", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    while (option.length() < 4)
    {
      option = "0"+option;
    }

    String level = releaseLevel.toUpperCase().trim();
    if (level.length() != 6 && level.length() != 0 && !level.equals(PRODUCT_RELEASE_CURRENT) &&
        !level.equals(PRODUCT_RELEASE_ONLY) && !level.equals(PRODUCT_RELEASE_PREVIOUS) &&
        !level.equals(PRODUCT_RELEASE_ANY))
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


  /**
   * Constructs a Product object.  Sets featureID to PRODUCT_FEATURE_CODE.
   * @param system The system.
   * @param productID The product identifier.
   * @param productOption The product option.
   * @param releaseLevel The release level of the product.
   * @param loadType The type of product load.
   * @param languageID The language feature ID for the product.
  **/
  public Product(AS400 system, String productID, String productOption, String releaseLevel, String loadType, String languageID)
  {
    this(system, productID, productOption, releaseLevel, PRODUCT_FEATURE_CODE);

    if (loadType == null) throw new NullPointerException("loadType");
    if (languageID == null) throw new NullPointerException("languageID");

    loadType_ = loadType;
    primaryLanguageLoadID_ = languageID;
  }


  /**
   * This constructor is used by the 0500 format.
  **/
  Product(AS400 system, String id, String option, String level, String feature, boolean allow, String msgID, String minVRM)
  {
    this(system, id, option, level, feature);
    allowsDynamicNaming_ = allow;
    descriptionID_ = msgID;
    minimumVRM_ = minVRM;
    loadedOptions_ = true;
  }


  /**
   * This constructor is used by ProductList.
  **/
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


  /**
   * Indicates if the names of product libraries and root folders
   * for this product option can be dynamically changed without
   * causing a product error.
   * @return true if the product can by dynamically named, false if it cannot.
  **/
  public boolean allowsDynamicNaming()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loadedOptions_) fillInOptionInformation();
    return allowsDynamicNaming_;
  }


  /**
   * Indicates if this product allows mixed releases between its *BASE
   * and options.
   * @return true if the *BASE option and other options of this product
   * can be at different release levels, false if they must all be at
   * the same release level.
  **/
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


  /**
   * Indicates if this product can be installed at a release level different
   * from the current release level without installing over a current release.
   * @return true if the product can be installed at a different release level,
   * false if it cannot be installed at a different release level without
   * installing over the current release.
  **/
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


  /**
   * Helper method.
  **/
  private void fillInOptionInformation()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    Product[] products = getProductOptions();
    for (int i=0; i<products.length; ++i)
    {
      if (products[i].getProductOption().equals(productOption_))
      {
        allowsDynamicNaming_ = products[i].allowsDynamicNaming_;
        descriptionID_ = products[i].descriptionID_;
        minimumVRM_ = products[i].minimumVRM_;
        return;
      }
    }
  }


  /**
   * Returns the value specified for the copyright current year when
   * the product definition for this product load was created. If no
   * copyright year was specified, then this method returns "".
   * @return The copyright current year.
   * @see #getFirstCopyright
  **/
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


  /**
   * Returns the message ID associated with this product option. The
   * message ID was specified when the product definition was created.
   * @return The message ID representing the product option description.
   * @see #getDescriptionMessageFile
   * @see #getDescriptionText
  **/
  public String getDescriptionID()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loadedOptions_) fillInOptionInformation();
    return descriptionID_;
  }


  /**
   * Returns the full pathname of the message file that contains the
   * messages describing the product and its options.
   * @return The message file.
   * @see #getDescriptionID
   * @see #getDescriptionText
  **/
  public String getDescriptionMessageFile()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!partiallyLoaded_ && !loaded500_) refresh(500);
    return messageFile_;
  }


  /**
   * Retrieves the description text for this product's message ID out of
   * this product's message file.
   * @return The description text, or null if an underlying error occurred while
   * trying to load the description text from the message file.
   * @see #getDescriptionID
   * @see #getDescriptionMessageFile
  **/
  public String getDescriptionText()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!partiallyLoaded_ && !loadedDescriptionText_)
    {
      String fileName = getDescriptionMessageFile();
      String id = getDescriptionID();
      try
      {
        MessageFile mf = new MessageFile(system_, fileName);
        AS400Message msg = mf.getMessage(id);
        descriptionText_ = msg.getText();
      }
      catch(Exception e)
      {
        // Couldn't find the message file, or some other error.
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Unable to retrieve product description text for "+fileName+" and "+id+": ", e);
      }
      loadedDescriptionText_ = true;
    }
    return descriptionText_;
  }


  /**
   * Returns the list of product directories for this product.
   * @return The array of product directories and associated information.
  **/
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


  /**
   * Returns the feature ID for this product.
   * @return The feature ID.
  **/
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


  /**
   * Returns the value specified for the copyright first year when
   * the product definition for this product load was created. If no
   * copyright year was specified, then this method returns "".
   * @return The copyright first year.
   * @see #getCurrentCopyright
  **/
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


  /**
   * Returns the release level of this product.
   * @return The release level.
  **/
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


  /**
   * Returns a ProductLicense object representing license information
   * for this product.
   * @return The ProductLicense object.
  **/
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


  /**
   * Returns the state of the product load for which information was retrieved.
   * Possible values are:
   * <UL>
   * <LI>{@link #LOAD_STATE_DEFINED_NO_OBJECT LOAD_STATE_DEFINED_NO_OBJECT}
   * <LI>{@link #LOAD_STATE_DEFINED_OBJECT_EXISTS LOAD_STATE_DEFINED_OBJECT_EXISTS}
   * <LI>{@link #LOAD_STATE_PACKAGED_RESTORE_FAILED_EXIT_PROGRAM_FAILED LOAD_STATE_PACKAGED_RESTORE_FAILED_EXIT_PROGRAM_FAILED}
   * <LI>{@link #LOAD_STATE_PACKAGED_RESTORE_FAILED LOAD_STATE_PACKAGED_RESTORE_FAILED}
   * <LI>{@link #LOAD_STATE_PACKAGED LOAD_STATE_PACKAGED}
   * <LI>{@link #LOAD_STATE_PACKAGED_RENAMED LOAD_STATE_PACKAGED_RENAMED}
   * <LI>{@link #LOAD_STATE_PACKAGED_DAMAGED LOAD_STATE_PACKAGED_DAMAGED}
   * <LI>{@link #LOAD_STATE_PACKAGED_DELETED LOAD_STATE_PACKAGED_DELETED}
   * <LI>{@link #LOAD_STATE_PACKAGED_RESTORE_IN_PROGRESS LOAD_STATE_PACKAGED_RESTORE_IN_PROGRESS}
   * <LI>{@link #LOAD_STATE_PACKAGED_DELETE_IN_PROGRESS LOAD_STATE_PACKAGED_DELETE_IN_PROGRESS}
   * <LI>{@link #LOAD_STATE_INSTALLED_RESTORE_IN_PROGRESS LOAD_STATE_INSTALLED_RESTORE_IN_PROGRESS}
   * <LI>{@link #LOAD_STATE_INSTALLED_DELETE_IN_PROGRESS LOAD_STATE_INSTALLED_DELETE_IN_PROGRESS}
   * <LI>{@link #LOAD_STATE_IBM_SUPPLIED_NOT_COMPATIBLE LOAD_STATE_IBM_SUPPLIED_NOT_COMPATIBLE}
   * <LI>{@link #LOAD_STATE_INSTALLED_RESTORE_FAILED_EXIT_PROGRAM_FAILED LOAD_STATE_INSTALLED_RESTORE_FAILED_EXIT_PROGRAM_FAILED}
   * <LI>{@link #LOAD_STATE_INSTALLED_RESTORE_FAILED LOAD_STATE_INSTALLED_RESTORE_FAILED}
   * <LI>{@link #LOAD_STATE_RESTORED LOAD_STATE_RESTORED}
   * <LI>{@link #LOAD_STATE_RESTORED_EXIT_PROGRAM_FAILED LOAD_STATE_RESTORED_EXIT_PROGRAM_FAILED}
   * <LI>{@link #LOAD_STATE_RESTORED_RENAMED LOAD_STATE_RESTORED_RENAMED}
   * <LI>{@link #LOAD_STATE_RESTORED_DAMAGED LOAD_STATE_RESTORED_DAMAGED}
   * <LI>{@link #LOAD_STATE_RESTORED_DELETED LOAD_STATE_RESTORED_DELETED}
   * <LI>{@link #LOAD_STATE_CHECK_ERROR LOAD_STATE_CHECK_ERROR}
   * <LI>{@link #LOAD_STATE_INSTALLED LOAD_STATE_INSTALLED}
   * </UL>
   * @return The load state.
   * @see #getSymbolicLoadState
  **/
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


  /**
   * Returns the type of product load for which information was retrieved.
   * Possible values are:
   * <UL>
   * <LI>{@link #LOAD_TYPE_CODE LOAD_TYPE_CODE}
   * <LI>{@link #LOAD_TYPE_LANGUAGE LOAD_TYPE_LANGUAGE}
   * </UL>
   * @return The load type.
  **/
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


  /**
   * Returns the minimum release level that is allowed for the option
   * that will run with the current level of the *BASE option for the product.
   * This method is only applicable if mixed releases are allowed.
   * Possible values are a release level (e.g. "V5R1M0") or "*MATCH"
   * which indicates the release of the option matches that of *BASE.
   * @return The minimum required release level of this product option.
   * @see #allowsMixedReleases
   * @see #getMinimumRequiredReleaseForBase
  **/
  public String getMinimumRequiredRelease()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loadedOptions_) fillInOptionInformation();
    return minimumVRM_;
  }


  /**
   * Returns the minimum release level that is allowed for the *BASE option
   * that will run with the current level of the option for the product. This
   * method is only applicable if mixed releases are allowed and if the
   * load type is *CODE. Possible values are a release level (e.g. "V5R1M0")
   * or "*MATCH" which indicates the release of the option matches that
   * of *BASE.
   * @return The minimum required release level of the base product option.
   * @see #allowsMixedReleases
   * @see #getMinimumRequiredRelease
  **/
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


  /**
   * Returns the minimum operating system release to which the Save Licensed Program
   * (SAVLICPGM) command will allow the product to be saved.
   * @return The minimum target release (e.g. "V5R1M0").
  **/
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


  /**
   * Returns the primary language feature ID for this product.
   * <P>
   * For code loads, this is the primary language of the product option;
   * that is, it is the National Language Version (NLV) of the language
   * that is installed in the libraries. It is "" if no language is
   * installed in the libraries for the code load.
   * <P>
   * For language loads (e.g. "2938"), it is "".
   * @return The primary language feature ID, or "" if no language is
   * installed or this product is a language load.
   * @see #getLoadType
  **/
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


  /**
   * Returns the product ID for this product.
   * @return The product ID (e.g. "5722JC1").
  **/
  public String getProductID()
  {
    return productID_;
  }


  /**
   * Returns the product option for this product.
   * @return The product option (e.g. "*BASE" or "0012").
  **/
  public String getProductOption()
  {
    return productOption_;
  }


  /**
   * Returns the list of product options for this product ID.
   * @return The array of products.
  **/
  public Product[] getProductOptions()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded500_) refresh(500);
    return options_;
  }


  /**
   * Returns the list of Program Temporary Fixes (PTFs) on
   * the system for this product.
   * @param includeSupersededPTFs Specify true to include any
   * superseded PTFs in the list; false otherwise.
   * @return The array of PTFs. If there are no PTFs for the
   * product, this method returns an array of size 0.
  **/
  public PTF[] getPTFs(boolean includeSupersededPTFs)
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!isInstalled()) return new PTF[0];
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
    parms[3] = new ProgramParameter(new byte[4]); // error code
    try { parms[3].setParameterType(ProgramParameter.PASS_BY_REFERENCE); } catch(PropertyVetoException pve) {}

    ServiceProgramCall pc = new ServiceProgramCall(system_, "/QSYS.LIB/QPZLSTFX.SRVPGM", "QpzListPTF", ServiceProgramCall.NO_RETURN_VALUE, parms);
    byte[] buf = null;
    synchronized(userSpace_)
    {
      UserSpace us = new UserSpace(system_, "/QSYS.LIB/QTEMP.LIB/JT4PTF.USRSPC");
      us.setMustUseProgramCall(true);
      us.setMustUseSockets(true); // We have to do it this way since UserSpace will otherwise make a native ProgramCall.
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
      int iplAction = (int)(buf[offset++] & 0x000F); // EBCDIC 0xF0 = '0', 0xF1 = '1', etc.
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


  /**
   * Returns the registration type associated with this product. The
   * registration type and registration value together make up the
   * registration ID for the product. Possible values are:
   * <UL>
   * <LI>{@link #REGISTRATION_TYPE_PHONE REGISTRATION_TYPE_PHONE}
   * <LI>{@link #REGISTRATION_TYPE_SYSTEM REGISTRATION_TYPE_SYSTEM}
   * <LI>{@link #REGISTRATION_TYPE_CUSTOMER REGISTRATION_TYPE_CUSTOMER}
   * </UL>
   * @return The registration type.
   * @see #getRegistrationValue
  **/
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


  /**
   * Returns the registration value associated with this product. The
   * registration type and registration value together make up the
   * registration ID for the product.
   * @return The registration value.
   * @see #getRegistrationType
  **/
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


  /**
   * Returns the value specified for the release date when the product
   * definition for this product load was created. If no release date
   * was specified for the product, then null is returned.
   * @return The release date, or null if there is no release date.
  **/
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


  /**
   * Returns the release level for this product.  For example: "V5R1M0".
   * If any of the special values were specified when this object was constructed,
   * the real release level will be retrieved from the system.
   * @return The release level.
  **/
  public String getReleaseLevel()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (releaseLevel_.equals(PRODUCT_RELEASE_CURRENT) || releaseLevel_.equals(PRODUCT_RELEASE_PREVIOUS) ||
        releaseLevel_.equals(PRODUCT_RELEASE_ONLY) || releaseLevel_.equals(PRODUCT_RELEASE_ANY))
    {
      refresh(100);
    }
    return releaseLevel_;
  }


  /**
   * Returns the reason why the release requirements between the base and
   * option may or may not be in error. (When a product allows mixed releases
   * between its base and option, certain requirements must be met). If the
   * load type is LOAD_TYPE_LANGUAGE, then this method returns REQUIREMENTS_UNKNOWN.
   * Possible values are:
   * <UL>
   * <LI>{@link #REQUIREMENTS_UNKNOWN REQUIREMENTS_UNKNOWN}
   * <LI>{@link #REQUIREMENTS_MET REQUIREMENTS_MET}
   * <LI>{@link #REQUIREMENTS_OPTION_TOO_OLD REQUIREMENTS_OPTION_TOO_OLD}
   * <LI>{@link #REQUIREMENTS_BASE_TOO_OLD REQUIREMENTS_BASE_TOO_OLD}
   * </UL>
   * @return The reason why requirements are met or not.
   * @see #getLoadType
  **/
  public int getRequirementsMet()
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


  /**
   * Returns the symbolic load state for which information was retrieved.
   * This value, in conjunction with the load error, can be used to determine
   * if the load is installed correctly. Possible values are:
   * <UL>
   * <LI>{@link #SYMBOLIC_LOAD_STATE_DEFINED SYMBOLIC_LOAD_STATE_DEFINED}
   * <LI>{@link #SYMBOLIC_LOAD_STATE_CREATED SYMBOLIC_LOAD_STATE_CREATED}
   * <LI>{@link #SYMBOLIC_LOAD_STATE_PACKAGED SYMBOLIC_LOAD_STATE_PACKAGED}
   * <LI>{@link #SYMBOLIC_LOAD_STATE_DAMAGED SYMBOLIC_LOAD_STATE_DAMAGED}
   * <LI>{@link #SYMBOLIC_LOAD_STATE_LOADED SYMBOLIC_LOAD_STATE_LOADED}
   * <LI>{@link #SYMBOLIC_LOAD_STATE_INSTALLED SYMBOLIC_LOAD_STATE_INSTALLED}
   * </UL>
   * @return The symbolic load state.
   * @see #getLoadState
   * @see #isLoadInError
  **/
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


  /**
   * Returns the system.
   * @return The system.
  **/
  public AS400 getSystem()
  {
    return system_;
  }


  /**
   * Indicates whether or not this product is installed on the system.
   * @return true if the product is installed, false if it is not.
  **/
  public boolean isInstalled()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (partiallyLoaded_) return installed_;
    try
    {
      return getSymbolicLoadState().equals(SYMBOLIC_LOAD_STATE_INSTALLED);
    }
    catch(AS400Exception e)
    {
      AS400Message[] messages = e.getAS400MessageList();
      if (messages.length == 1 && messages[0].getID().equalsIgnoreCase("CPF0C1F")) // Product info not found
      {
        return false;
      }
      throw e;
    }
  }


  /**
   * Indicates if there is a known error for this product load. This does
   * not mean that product is necessarily installed. Check the symbolic load
   * state to determine if the product load is installed or not.
   * @return true if an error was found the last time that the state of
   * this load was checked or updated, false if no error was found.
   * @see #getSymbolicLoadState
  **/
  public boolean isLoadInError()
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


  /**
   * Indicates whether this product feature is currently supported. A feature
   * can be supported by using the Work with Supported Products (WRKSPTPRD)
   * command in the System Manager.
   * @return true if the feature is supported, false if it is not.
  **/
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
    if (!error500_)
    {
      loadedDescriptionText_ = false;
      getDescriptionText();
    }
    fillInOptionInformation();
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
    if (releaseLevel_.equals(PRODUCT_RELEASE_ANY))
    {
      // First try ONLY, then PREVIOUS, then CURRENT, since there will
      // almost always be a product definition for CURRENT.
      error100_ = false;
      releaseLevel_ = PRODUCT_RELEASE_ONLY;
      try
      {
        refresh(whichFormat);
        return;
      }
      catch(AS400Exception x) {}
      if (error100_)
      {
        error100_ = false;
        releaseLevel_ = PRODUCT_RELEASE_PREVIOUS;
        try
        {
          refresh(whichFormat);
          return;
        }
        catch(AS400Exception x) {}
        if (error100_)
        {
          error100_ = false;
          releaseLevel_ = PRODUCT_RELEASE_CURRENT;
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
        len = 108+49+chunkSize_; // Don't need all the info.
        break;
      case 800:
        if (error800_) return; // No point in trying again.
        format = "PRDR0800";
        len = 108+17+chunkSize_; // In real life, len = 108+17 + (50+primaryPath+installPath+(10*numberOfObjectAuthorities)*numberOfEntries
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
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
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
    parms[4] = new ProgramParameter(new byte[4]); // error code
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
        installed_ = false;
        if (whichFormat != 100)
        {
          refresh(100);
          if (whichFormat == 500)
          {
            releaseDate_ = null;
            firstCopyright_ = null;
            currentCopyright_ = null;
            options_ = new Product[0];
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
        }
      }
      throw new AS400Exception(messages);
    }

    byte[] outputData = parms[0].getOutputData();
    int bytesReturned = BinaryConverter.byteArrayToInt(outputData, 0);
    int bytesAvailable = BinaryConverter.byteArrayToInt(outputData, 4);
    if (bytesReturned < bytesAvailable)
    {
      chunkSize_ = bytesAvailable;
      refresh(whichFormat);
      return;
    }
    releaseLevel_ = conv.byteArrayToString(outputData, 19, 6);
    if (whichFormat != 500) loadID_ = conv.byteArrayToString(outputData, 29, 4); // Since 500 uses *CODE, we don't want to reset it.
    loadType_ = conv.byteArrayToString(outputData, 33, 10).trim();
    symbolicLoadState_ = conv.byteArrayToString(outputData, 43, 10);
    loadErrorIndicator_ = !conv.byteArrayToString(outputData, 53, 10).trim().equals("*NONE"); // *ERROR for error, *NONE for not.
    loadState_ = conv.byteArrayToString(outputData, 63, 2);
    supported_ = conv.byteArrayToString(outputData, 65, 1).equals("1");
    registrationType_ = conv.byteArrayToString(outputData, 66, 2);
    registrationValue_ = conv.byteArrayToString(outputData, 68, 14);
    primaryLanguageLoadID_ = conv.byteArrayToString(outputData, 88, 4);
    minimumTargetRelease_ = conv.byteArrayToString(outputData, 92, 6);
    minimumBaseVRM_ = conv.byteArrayToString(outputData, 98, 6);
    requirementsMet_ = (int)(outputData[104] & 0x000F); // 0xF0 = 0, 0xF1 = 1, etc...
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
      currentCopyright_ = conv.byteArrayToString(outputData, offset, 4).trim();
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
      int numOptions = BinaryConverter.byteArrayToInt(outputData, offset);
      offset += 4;
      int entryLength = BinaryConverter.byteArrayToInt(outputData, offset);
      offset += 4;
      int offsetToOptions = BinaryConverter.byteArrayToInt(outputData, offset);
      offset += 4;
      allowsMixedReleases_ = (outputData[offset] == (byte)0xF1); // '1' allows, '0' doesn't.

      options_ = new Product[numOptions];
      for (int i=0; i<numOptions; ++i)
      {
        offset = offsetToOptions + (entryLength*i);
        String prodOption = conv.byteArrayToString(outputData, offset, 4);
        offset += 4;
        boolean allow = (outputData[offset] == (byte)0xF1); // '1' allows dynamic naming, '0' does not.
        offset += 1;
        String msgID = conv.byteArrayToString(outputData, offset, 7);
        offset += 7;
        String minVRM = conv.byteArrayToString(outputData, offset, 6);
//        options_[i] = new Product(system_, productID_, prodOption, releaseLevel_, loadID_, allow, msgID, minVRM);
        options_[i] = new Product(system_, productID_, prodOption, releaseLevel_, getFeatureID(), allow, msgID, minVRM);
      }
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
        //offset += 4;
        //int primaryHomeLength = BinaryConverter.byteArrayToInt(outputData, offset);
        //offset += 4;
        //int primaryHomeOffset = BinaryConverter.byteArrayToInt(outputData, offset);
        //offset += 4;
        offset += 12;
        int installedPathLength = BinaryConverter.byteArrayToInt(outputData, offset);
        offset += 4;
        int installedPathOffset = BinaryConverter.byteArrayToInt(outputData, offset);
        //offset += 4;
        //int installedHomeLength = BinaryConverter.byteArrayToInt(outputData, offset);
        //offset += 4;
        //int installedHomeOffset = BinaryConverter.byteArrayToInt(outputData, offset);
        //offset += 4;
        offset += 12;
        int numAuthorities = BinaryConverter.byteArrayToInt(outputData, offset);
        offset += 4;
        int offsetAuthorities = BinaryConverter.byteArrayToInt(outputData, offset);
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


  /**
   * Returns a String representation of this product in the format "product ID/product option/release level".
   * @return The product String.
  **/
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
