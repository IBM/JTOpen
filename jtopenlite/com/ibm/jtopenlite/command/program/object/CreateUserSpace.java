///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  CreateUserSpace.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.object;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;

/**
 * <p> Call the QUSCRTUS API, 
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/quscrtus.htm">QUSCRTUS</a>
 * 
 * <p>This class is used with a CommandConnection to create a user space
 * 
 * <p> Sample code
 * <pre>
 	public static void main(String[] args) {
			try { 
				CommandConnection connection = CommandConnection.getConnection(args[0],  args[1],  args[2]);
				CreateUserSpace createUserSpace = new CreateUserSpace(
						args[4], // userSpaceName 
						args[3], // userSpaceLibrary 
						CreateUserSpace.EXTENDED_ATTRIBUTE_NONE,      // extendedAttribute 
						100,     // initialSize       
						CreateUserSpace.INITIAL_VALUE_BEST_PERFORMANCE, // initialValue 
						CreateUserSpace.PUBLIC_AUTHORITY_USE, // publicAuthority 
						"", //textDescription 
						CreateUserSpace.REPLACE_NO,  //replace 
						CreateUserSpace.DOMAIN_DEFAULT, //domain 
						CreateUserSpace.TRANSFER_SIZE_REQUEST_DEFAULT, //transferSizeRequest 
						CreateUserSpace.OPTIMUM_SPACE_ALIGNMENT_YES // optimumSpaceAlignment 
						);
				
				CommandResult result = connection.call(createUserSpace); 
				System.out.println("Command completed with "+result); 
			} catch (Exception e) {
				e.printStackTrace(System.out); 
				usage(); 
			}
	}

 * </pre> 
**/
public class CreateUserSpace implements Program
{
  private static final byte[] ZERO = new byte[4];

  public static final String EXTENDED_ATTRIBUTE_NONE = "";

  public static final int INITIAL_SIZE_MAX = 16776704;

  public static final byte INITIAL_VALUE_BEST_PERFORMANCE = 0;

  public static final String PUBLIC_AUTHORITY_ALL = "*ALL";
  public static final String PUBLIC_AUTHORITY_CHANGE = "*CHANGE";
  public static final String PUBLIC_AUTHORITY_EXCLUDE = "*EXCLUDE";
  public static final String PUBLIC_AUTHORITY_LIBCRTAUT = "*LIBCRTAUT";
  public static final String PUBLIC_AUTHORITY_USE = "*USE";

  public static final String REPLACE_YES = "*YES";
  public static final String REPLACE_NO = "*NO";

  public static final String DOMAIN_DEFAULT = "*DEFAULT";
  public static final String DOMAIN_SYSTEM = "*SYSTEM";
  public static final String DOMAIN_USER = "*USER";

  public static final int TRANSFER_SIZE_REQUEST_DEFAULT = 0;

  public static final String OPTIMUM_SPACE_ALIGNMENT_YES = "1";
  public static final String OPTIMUM_SPACE_ALIGNMENT_NO = "0";

  private final byte[] tempData_ = new byte[50];

  private String userSpaceName_;
  private String userSpaceLibrary_;
  private String extendedAttribute_;
  private int initialSize_;
  private byte initialValue_;
  private String publicAuthority_;
  private String textDescription_;
  private String replace_;
  private String domain_;
  private int transferSizeRequest_;
  private String optimumSpaceAlignment_;

  public CreateUserSpace(String userSpaceName, 
		                 String userSpaceLibrary, 
		                 String extendedAttribute,
                         int initialSize, 
                         byte initialValue, 
                         String publicAuthority,
                         String textDescription, 
                         String replace, 
                         String domain, 
                         int transferSizeRequest,
                         String optimumSpaceAlignment)
  {
    userSpaceName_ = userSpaceName;
    userSpaceLibrary_ = userSpaceLibrary;
    extendedAttribute_ = extendedAttribute;
    initialSize_ = initialSize;
    initialValue_ = initialValue;
    publicAuthority_ = publicAuthority;
    textDescription_ = textDescription;
    replace_ = replace;
    domain_ = domain;
    transferSizeRequest_ = transferSizeRequest;
    optimumSpaceAlignment_ = optimumSpaceAlignment;
  }

  public String getUserSpaceName()
  {
    return userSpaceName_;
  }

  public void setUserSpaceName(String name)
  {
    userSpaceName_ = name;
  }

  public String getUserSpaceLibrary()
  {
    return userSpaceLibrary_;
  }

  public void setUserSpaceLibrary(String lib)
  {
    userSpaceLibrary_ = lib;
  }

  public int getInitialSize()
  {
    return initialSize_;
  }

  public void setInitialSize(int size)
  {
    initialSize_ = size;
  }

  public byte getInitialValue()
  {
    return initialValue_;
  }

  public void setInitialValue(byte val)
  {
    initialValue_ = val;
  }

  public String getPublicAuthority()
  {
    return publicAuthority_;
  }

  public void setPublicAuthority(String auth)
  {
    publicAuthority_ = auth;
  }

  public String getTextDescription()
  {
    return textDescription_;
  }

  public void setTextDescription(String text)
  {
    textDescription_ = text;
  }

  public String getReplace()
  {
    return replace_;
  }

  public void setReplace(String replace)
  {
    replace_ = replace;
  }

  public String getDomain()
  {
    return domain_;
  }

  public void setDomain(String domain)
  {
    domain_ = domain;
  }

  public int getTransferSizeRequest()
  {
    return transferSizeRequest_;
  }

  public void setTransferSizeRequest(int size)
  {
    transferSizeRequest_ = size;
  }

  public String getOptimumSpaceAlignment()
  {
    return optimumSpaceAlignment_;
  }

  public void setOptimumSpaceAlignment(String align)
  {
    optimumSpaceAlignment_ = align;
  }

  public void newCall()
  {
  }

  public int getNumberOfParameters()
  {
    return 11;
  }

  public int getParameterInputLength(int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 20;
      case 1: return 10;
      case 2: return 4;
      case 3: return 1;
      case 4: return 10;
      case 5: return 50;
      case 6: return 10;
      case 7: return 4;
      case 8: return 10;
      case 9: return 4;
      case 10: return 1;
    }
    return 0;
  }

  public int getParameterOutputLength(int parmIndex)
  {
    return (parmIndex == 7 ? 4 : 0);
  }

  public int getParameterType(int parmIndex)
  {
    return (parmIndex == 7 ? Parameter.TYPE_INPUT_OUTPUT : Parameter.TYPE_INPUT);
  }

  public byte[] getParameterInputData(int parmIndex)
  {
    switch (parmIndex)
    {
      case 0:
        Conv.stringToBlankPadEBCDICByteArray(userSpaceName_, tempData_, 0, 10);
        Conv.stringToBlankPadEBCDICByteArray(userSpaceLibrary_, tempData_, 10, 10);
        break;
      case 1:
        Conv.stringToBlankPadEBCDICByteArray(extendedAttribute_, tempData_, 0, 10);
        break;
      case 2:
        Conv.intToByteArray(initialSize_, tempData_, 0);
        break;
      case 3:
        tempData_[0] = initialValue_;
        break;
      case 4:
        Conv.stringToBlankPadEBCDICByteArray(publicAuthority_, tempData_, 0, 10);
        break;
      case 5:
        Conv.stringToBlankPadEBCDICByteArray(textDescription_, tempData_, 0, 50);
        break;
      case 6:
        Conv.stringToBlankPadEBCDICByteArray(replace_, tempData_, 0, 10);
        break;
      case 7:
        return ZERO;
      case 8:
        Conv.stringToBlankPadEBCDICByteArray(domain_, tempData_, 0, 10);
        break;
      case 9:
        Conv.intToByteArray(transferSizeRequest_, tempData_, 0);
        break;
      case 10:
        Conv.stringToBlankPadEBCDICByteArray(optimumSpaceAlignment_, tempData_, 0, 1);
        break;
    }
    return tempData_;
  }


  public byte[] getTempDataBuffer()
  {
    return tempData_;
  }

  public void setParameterOutputData(int parmIndex, byte[] tempData, int maxLength)
  {
  }

  public String getProgramName()
  {
    return "QUSCRTUS";
  }

  public String getProgramLibrary()
  {
    return "QSYS";
  }
}

