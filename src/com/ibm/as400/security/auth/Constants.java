package com.ibm.as400.security.auth;

import com.ibm.eim.*;


/**
 Contains constants defining return codes for use in EimException objects.
 **/
public interface Constants
{

  // TBD: Note to maintainer: The "original" of these return codes is file /osxpf/v5r2m0f.xpf/cur/cmvc/base.pgm/sy.xpf/itkn/qsyitkn.h

  /**
   EimException return code, indicating "No memory available. Unable to allocate required space."
   **/
  public static final int ITKNERR_NOMEM = 1;

  /**
   EimException return code, indicating "Error occurred when converting data between code pages."
   **/
  public static final int ITKNERR_DATA_CONVERSION = 2;

  /**
   EimException return code, indicating "Error occurred using cryptographic interfaces."
   **/
  public static final int ITKNERR_CRYPTO = 3;

  /**
   EimException return code, indicating "Error occurred using EIM interfaces."
   **/
  public static final int ITKNERR_EIM = 4;

  /**
   EimException return code, indicating "Missing required parameter.  Please check API documentation."
   **/
  public static final int ITKNERR_PARM_REQ = 5;

  /**
   EimException return code, indicating "Key size is not valid."
   **/
  public static final int ITKNERR_KEYSIZE_INVALID = 6;

  /**
   EimException return code, indicating "Validity period is not valid."
   **/
  public static final int ITKNERR_VAL_PERIOD_INVALID = 7;

  /**
   EimException return code, indicating "More that one EIM entry found."
   **/
  public static final int ITKNERR_AMBIGUOUS = 8;

  /**
   EimException return code, indicating "Length in specified token and specified length do not match."
   **/
  public static final int ITKNERR_TKN_LEN_MISMATCH = 9;

  /**
   EimException return code, indicating "EIM identifier in specified token and specified EIM identifier do not match."
   **/
  public static final int ITKNERR_TKN_EIMID_MISMATCH = 10;

  /**
   EimException return code, indicating "Application instance identifier in specified token and specified application instance identifier do not match."
   **/
  public static final int ITKNERR_TKN_APP_INST_MISMATCH = 11;

  /**
   EimException return code, indicating "Token signature is not valid.  Token may have been modified."
   **/
  public static final int ITKNERR_SIGNATURE_INVALID = 12;

  /**
   EimException return code, indicating "Identity token is not valid."
   **/
  public static final int ITKNERR_TKN_INVALID = 13;

  /**
   EimException return code, indicating "Target user not found for identity token."
   **/
  public static final int ITKNERR_TARGET_USER_NOT_FOUND = 14;

  /**
   EimException return code, indicating "EIM identifier not found."
   **/
  public static final int ITKNERR_EIMID_NOT_FOUND = 15;

  // Note: The C version defines ITKNERR_KEYHANDLE_INVALID = 16.
  // That return code is irrelevant to us on the Java side of the world.

  /**
   EimException return code, indicating "The version of the token is not supported by this version of the API."
   **/
  public static final int ITKNERR_TKN_VERSION_NOT_SUPPORTED = 17;

  /**
   EimException return code, indicating "Public key was not found, so unable to check signature."
   **/
  public static final int ITKNERR_PUBLIC_KEY_NOT_FOUND = 18;




  /**
   EimException return code, indicating "Internal error."
   **/
  public static final int INTERNAL_ERROR = 99;


  static final int CCSID = 1200;  // CCSID for CHAR(*) fields in identity tokens

}


