///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProductList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 * Represents a list of OS/400 licensed products.
 * @see com.ibm.as400.access.Product
**/
public class ProductList
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private AS400 system_;
  private static final AS400Bin4 bin4_ = new AS400Bin4();
  private String[] productIDs_ = new String[1];
  private String[] productOptions_ = new String[1];
  private String[] releaseLevels_ = new String[1];
  private int currentProduct_ = 0;

  private String filter_ = ALL;
  private String option_ = ALL;

  // Retrieve 1000 products at a time since we don't
  // know how many are actually installed on the system
  // until we make the first API call.
  private int chunkSize_ = 1000;

  // Public constants.

  /**
   * Constant representing the value *ALL.
  **/
  public static final String ALL = "*ALL";

  /**
   * Constant representing the value *BASE.
  **/
  public static final String BASE = "*BASE";
  
  /**
   * Constant representing the value *INSTLD.
  **/
  public static final String INSTALLED = "*INSTLD";
  
  /**
   * Constant representing the value *INSSPT.
  **/
  public static final String INSTALLED_OR_SUPPORTED = "*INSSPT";
  
  /**
   * Constant representing the value *LIST.
  **/
  public static final String LIST = "*LIST";
  
  /**
   * Constant representing the value *SUPPTD.
  **/
  public static final String SUPPORTED = "*SUPPTD";


  /**
   * Constructs a ProductList. The system must be set before
   * calling {@link #getProducts getProducts()}.
  **/
  public ProductList()
  {
  }


  /**
   * Constructs a ProductList for the specified system. The default product filter is ALL.
   * The default product option is ALL.
   * @param system The system from which to retrieve the list of products.
  **/
  public ProductList(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
  }


  /**
   * Adds a product to the list of products to retrieve when the product
   * filter is set to LIST. If the product filter is not set to LIST,
   * then the products added via this method are ignored.
   * @param productID The product ID, for example: "5722SS1" or "5769JC1". The length must be 7 characters.
   * @param productOption The product option, for example: {@link #BASE ProductList.BASE} or "00001". The length must be 5 characters.
   * @param releaseLevel The product release level, for example "V5R1M0" or "V4R5M0". The length must be 6 characters.
   * @see #clearProductsToRetrieve
  **/
  public void addProductToRetrieve(String productID, String productOption, String releaseLevel)
  {
    if (productID == null) throw new NullPointerException("productID");
    if (productOption == null) throw new NullPointerException("productOption");
    if (releaseLevel == null) throw new NullPointerException("releaseLevel");

    String id = productID.toUpperCase().trim();
    if (id.length() != 7)
    {
      throw new ExtendedIllegalArgumentException("productID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    String option = productOption.toUpperCase().trim();
    if (option.length() != 5)
    {
      throw new ExtendedIllegalArgumentException("productOption", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    String level = releaseLevel.toUpperCase().trim();
    if (level.length() != 6)
    {
      throw new ExtendedIllegalArgumentException("releaseLevel", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    if (currentProduct_ >= productIDs_.length)
    {
      String[] temp = productIDs_;
      int len = temp.length;
      int len2 = temp.length*2;
      productIDs_ = new String[len2];
      System.arraycopy(temp, 0, productIDs_, 0, len);
      temp = productOptions_;
      productOptions_ = new String[len2];
      System.arraycopy(temp, 0, productOptions_, 0, len);
      temp = releaseLevels_;
      releaseLevels_ = new String[len2];
      System.arraycopy(temp, 0, releaseLevels_, 0, len);
    }
    productIDs_[currentProduct_] = id;
    productOptions_[currentProduct_] = option;
    releaseLevels_[currentProduct_++] = level;
  }


  /**
   * Clears the list of products to retrieve and sets the product filter
   * to {@link #ALL ProductList.ALL}.
   * @see #addProductToRetrieve
  **/
  public void clearProductsToRetrieve()
  {
    productIDs_ = new String[1];
    productOptions_ = new String[1];
    releaseLevels_ = new String[1];
    currentProduct_ = 0;
    filter_ = ALL;
  }


  /**
   * Retrieves the list of products from the system.
   * Use {@link #setProductFilter setProductFilter()} and
   * {@link #setProductOption setProductOption()} to change the types of
   * products that are returned.
   * Use {@link #addProductToRetrieve addProductToRetrieve()} to add a 
   * specific product to retrieve and specify {@link #LIST ProductList.LIST}
   * for the product filter.
   * @return The array of Product objects.
  **/
  public Product[] getProducts()
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
  {
    if (filter_ == LIST && currentProduct_ == 0)
    {
      if (Trace.traceOn_)
      {
        Trace.log(Trace.ERROR, "ProductList filter is set to LIST but no products have been added.");
      }
      throw new ExtendedIllegalArgumentException("filter", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    int ccsid = system_.getCcsid();
    final ConvTable conv = ConvTable.getTable(ccsid, null);

    final ProgramParameter[] parms = new ProgramParameter[6];
    parms[0] = new ProgramParameter(197*chunkSize_); // output list
    final byte[] inputInformation = new byte[40];
    BinaryConverter.intToByteArray(chunkSize_, inputInformation, 0); // number of records to return
    AS400Text text10 = new AS400Text(10, ccsid, system_);
    text10.toBytes(ALL, inputInformation, 4); // number of products to select
    inputInformation[14] = (byte)0xF1; // '1' but it doesn't matter 
    inputInformation[15] = (byte)0xF1; // '1' but it doesn't matter
    text10.toBytes(option_, inputInformation, 16); // product options to display
    text10.toBytes(filter_, inputInformation, 26); // product
    if (currentProduct_ > 0 && filter_ == LIST)
    {
      BinaryConverter.intToByteArray(currentProduct_, inputInformation, 36); // records in list
    }
    // records in list is 0 since we are not using *LIST for the product

    parms[1] = new ProgramParameter(inputInformation); // input information
    parms[2] = new ProgramParameter(conv.stringToByteArray("PRDS0200")); // format name
    
    byte[] inputList = null;
    if (currentProduct_ > 0 && filter_ == LIST)
    {
      inputList = new byte[18*currentProduct_];
      AS400Text text5 = new AS400Text(5, ccsid, system_);
      AS400Text text6 = new AS400Text(6, ccsid, system_);
      AS400Text text7 = new AS400Text(7, ccsid, system_);
      int offset = 0;
      for (int i=0; i<currentProduct_; ++i)
      {
        text7.toBytes(productIDs_[i], inputList, offset);
        offset += 7;
        text5.toBytes(productOptions_[i], inputList, offset);
        offset += 5;
        text6.toBytes(releaseLevels_[i], inputList, offset);
        offset += 6;
      }
    }
    else
    {
      inputList = new byte[18]; // input list is ignored since we are not using *LIST for the product
    }
    parms[3] = new ProgramParameter(inputList); // input list
    parms[4] = new ProgramParameter(12); // output information
    parms[5] = new ProgramParameter(bin4_.toBytes(0)); // error code

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QSZSLTPR.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    final byte[] outputInformation = parms[4].getOutputData();
    int recordSize = BinaryConverter.byteArrayToInt(outputInformation, 0);
    int numRecords = BinaryConverter.byteArrayToInt(outputInformation, 4);
    //int action = BinaryConverter.byteArrayToInt(outputInformation, 8);
    // action should be 0 (no display was shown).

    if (numRecords > chunkSize_)
    {
      // Need to retrieve more.
      if (Trace.traceOn_)
      {
        Trace.log(Trace.DIAGNOSTIC, "Increasing ProductList chunk size from "+chunkSize_+" to "+numRecords+" and re-retrieving.");
      }
      chunkSize_ = numRecords;
      return getProducts();
    }

    byte[] outputList = parms[0].getOutputData();
    int offset = 0;
    Product[] products = new Product[numRecords];
    for (int i=0; i<numRecords && offset < outputList.length; ++i)
    {
      offset = i*recordSize;
      String productID = conv.byteArrayToString(outputList, offset, 7);
      offset += 7;
      String productOption = conv.byteArrayToString(outputList, offset, 5);
      offset += 5;
      String releaseLevel = conv.byteArrayToString(outputList, offset, 6);
      offset += 8;
      String descriptionID = conv.byteArrayToString(outputList, offset, 7);
      offset += 7;
      String descriptionObject = conv.byteArrayToString(outputList, offset, 10);
      offset += 10;
      String descriptionLibrary = conv.byteArrayToString(outputList, offset, 10);
      offset += 10;
      String messageFile = QSYSObjectPathName.toPath(descriptionLibrary, descriptionObject, "MSGF");
      boolean installed = conv.byteArrayToString(outputList, offset, 1).equals("1");
      offset += 1;
      boolean supported = conv.byteArrayToString(outputList, offset, 1).equals("1");
      offset += 1;
      String registrationType = conv.byteArrayToString(outputList, offset, 2);
      offset += 2;
      String registrationValue = conv.byteArrayToString(outputList, offset, 14);
      offset += 14;
      String descriptionText = conv.byteArrayToString(outputList, offset, 132);
      products[i] = new Product(system_, productID, productOption, releaseLevel, descriptionID, descriptionText, messageFile, installed, supported, registrationType, registrationValue);
    }
    return products;
  }

  
  /**
   * Sets the product filter used to filter the list.
   * Valid values are INSTALLED, SUPPORTED, INSTALLED_OR_SUPPORTED, ALL, and LIST.
   * @param filter The product filter.
  **/
  public void setProductFilter(String filter)
  {
    if (filter == null) throw new NullPointerException("filter");
    if (filter != INSTALLED && filter != SUPPORTED && filter != INSTALLED_OR_SUPPORTED &&
        filter != ALL && filter != LIST)
    {
      throw new ExtendedIllegalArgumentException("filter", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    filter_ = filter;
  }

  
  /**
   * Sets the product option used to filter the list.
   * Valid values are ALL and BASE.
   * @param The product option.
  **/
  public void setProductOption(String option)
  {
    if (option == null) throw new NullPointerException("option");
    if (option != ALL && option != BASE)
    {
      throw new ExtendedIllegalArgumentException("option", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    option_ = option;
  }


  /**
   * Sets the system.
   * @param system The system.
  **/
  public void setSystem(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");

    system_ = system;
  }
}

