///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400CertificateAttribute.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
   <p>The AS400CertificateAttribute class represents a certificate
   attribute.  This attribute is used to identify certificates
   during a list operation.  This class contains a single attribute
   which can be either a String or byte array value.
**/

public class AS400CertificateAttribute extends Object
    implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  private int attrType_; // Certificate attribute type, byte[] or String.
  private byte[] byteAttr_;   // Byte certificate attribute value.
  private String stringAttr_; // String certificate attribute value.
  private boolean isString_;  // Certificate attribute is type String.

  static final int LAST_BYTE_ATTR = 1;   // Last defined byte attribute.
  static final int LAST_STRING_ATTR = 7; // Last defined String attr.



  /**
      The byte array attribute type representing the subjectPublicKeyInfo
      field from the certificate.
  **/

  public final static int PUBLIC_KEY_BYTES = 1;
  /**
      Constant indicating the attribute represents the subject's
      common name in the certificate.  
  **/
  public final static int SUBJECT_COMMON_NAME = 2;
  /**
      Constant indicating the attribute represents the subject's
      country in the certificate.  
  **/
  public final static int SUBJECT_COUNTRY = 3;
  /**
      Constant indicating the attribute represents the subject's
      locality in the certificate.
  **/
  public final static int SUBJECT_LOCALITY = 4;
  /**
      Contant indicating the attribute represents the subject's
      state or province in the certificate.
  **/
  public final static int SUBJECT_STATE = 5;
  /**
      Constant indicating the attribute represents the subject's
      organization in the certificate.
  **/
  public final static int SUBJECT_ORGANIZATION = 6;
  /**
      Constant indicating the attribute represents the subject's
      organizational unit in the certificate.
  **/
  public final static int SUBJECT_ORGANIZATION_UNIT = 7;


  /**
   * Constructs an AS400CertificateAttribute object.
  **/
  public AS400CertificateAttribute()
  {
  }

  /**
   * Constructs an AS400CertificateAttribute object.
   *
   * @param attributeType  The attribute type.
   *                       Valid values are:
   *                       <UL>
   *                       <LI>PUBLIC_KEY_BYTES
   *                       </UL>
   * @param attributeValue The attribute value.
   *
   * @exception ExtendedIllegalArgumentException If the attribute Type is invalid.
   */
  public AS400CertificateAttribute(int attributeType, byte[] attributeValue)
    throws ExtendedIllegalArgumentException
  {
    if (attributeType < 1 || attributeType > LAST_BYTE_ATTR)
     throw new ExtendedIllegalArgumentException("attributeType (" +
       Integer.toString(attributeType) + ")",
       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    attrType_ = attributeType;
    byteAttr_ = attributeValue;
    isString_ = false;
  }

  /**
   * Constructs an AS400CertificateAttribute object.
   *
   * @param attributeType  The attribute type.
   *                       Valid values are:
   *                       <UL>
   *                       <LI>SUBJECT_COMMON_NAME
   *                       <LI>SUBJECT_COUNTRY
   *                       <LI>SUBJECT_LOCALITY
   *                       <LI>SUBJECT_ORGANIZATION
   *                       <LI>SUBJECT_ORGANIZATION_UNIT
   *                       <LI>SUBJECT_STATE
   *                       </UL>
   * @param attributeValue The attribute value.
   *
   * @exception ExtendedIllegalArgumentException If the attribute Type is invalid.
   */
  public AS400CertificateAttribute(int attributeType, String attributeValue)
    throws ExtendedIllegalArgumentException
  {
    if (attributeType <= LAST_BYTE_ATTR || attributeType > LAST_STRING_ATTR)
     throw new ExtendedIllegalArgumentException("attributeType (" +
       Integer.toString(attributeType) + ")",
       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    attrType_  = attributeType;
    stringAttr_ = attributeValue;
    isString_ = true;
  }



  /**
   * Returns the attribute type.
   * @return  The attribute type.
   */
  public int getAttributeType()
  {
    return attrType_;
  }

  /**
   * Returns the attribute value.
   * @return  The attribute value.
   */
  public Object getAttributeValue()
  {
    if (true == isString_)
     return (Object) stringAttr_;
    else
     return (Object) byteAttr_;
  }



  /**
   * Indicates if the attribute is a String.
   * @return  Returns true if the attribute is a String; false otherwise.
   */
  public boolean isString()
  {
    return isString_;
  }




  /**
   * Sets the byte attribute value.
   * @param attributeType  The attribute type.
   *                       Valid values are:
   *                       <UL>
   *                       <LI>PUBLIC_KEY_BYTES
   *                       </UL>
   * @param attributeValue  The attribute value.
   *
   * @exception ExtendedIllegalArgumentException If the attribute Type is invalid.
   */
  public void setAttribute(int attributeType, byte[] attributeValue)
    throws ExtendedIllegalArgumentException
  {
    if (attributeType < 1 || attributeType > LAST_BYTE_ATTR)
     throw new ExtendedIllegalArgumentException("attributeType (" +
       Integer.toString(attributeType) + ")",
       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    attrType_  = attributeType;
    byteAttr_ = attributeValue;
    isString_ = false;
  }

  /**
   * Sets the String attribute value.
   * @param attributeType  The attribute type.
   *                       Valid values are:
   *                       <UL>
   *                       <LI>SUBJECT_COMMON_NAME
   *                       <LI>SUBJECT_COUNTRY
   *                       <LI>SUBJECT_LOCALITY
   *                       <LI>SUBJECT_ORGANIZATION
   *                       <LI>SUBJECT_ORGANIZATION_UNIT
   *                       <LI>SUBJECT_STATE
   *                       </UL>
   * @param attributeValue  The attribute value.
   *
   * @exception ExtendedIllegalArgumentException If the attribute Type is invalid.
   */
  public void setAttribute(int attributeType, String attributeValue)
    throws ExtendedIllegalArgumentException
  {
    if (attributeType <= LAST_BYTE_ATTR || attributeType > LAST_STRING_ATTR)
     throw new ExtendedIllegalArgumentException("attributeType (" +
       Integer.toString(attributeType) + ")",
       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);


    attrType_  = attributeType;
    stringAttr_ = attributeValue;
    isString_ = true;
  }

  // Returns the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }


}  // End of AS400CertificateAttribute class




