///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400Certificate.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
    <p>The AS400Certificate class represents an X.509 ASN.1 encoded certificate.

 **/
public class AS400Certificate extends Object
    implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // The ASN.1 encoded certificate.
  private byte[] certificate_;

  // Free form additional information about this certificate.
  private String info_;

  // Free form additional byte data about this certificate.
  private byte[] byteData_;


  /**
    *Constructs an AS400Certificate object.
   **/
  public AS400Certificate()
  {
  }

  /**
    *Constructs an AS400Certificate object.
    *
    *@param  certificate  The ASN.1 encoded X.509 certificate.
   **/
  public AS400Certificate(byte[] certificate)
  {
    certificate_ = certificate;
  }





  /**
   * Returns the free form byte data.
   * @return  A byte array with free form information.
   */
  public byte[] getByteInfo()
  {
      return byteData_;
  }



  /**
    * Returns the ASN.1 encoded certificate value.
    *
    * @return  The ASN.1 encoded X.509 certificate.
    **/
  public byte[] getEncoded()
  {
      int len;
      byte[] tempBytes;

      len = getLength(certificate_);

      if (len != certificate_.length)
      {
       tempBytes = certificate_;
       certificate_ = new byte[len];

       System.arraycopy(tempBytes, 0,
                  certificate_, 0,
                  len);
      }

      return certificate_;
  }

  /**
   * Returns the free form information.
   * @return  The free form string information.
   */
  public String getInfo()
  {
      return info_;
  }








  /**
   * Sets the free form byte data.
   * @param byteData The free form byte array information.
   */
  public void setByteInfo(byte[] byteData)
  {
      byteData_ = byteData;
  }


  /**
    * Sets the ASN.1 encoded certificate value.
    *
    * @param  certificate  The ASN.1 encoded certificate.
    **/
  public void setEncoded(byte[] certificate)
  {
    certificate_ = certificate;
  }

  /**
   * Sets the free form string information.
   * @param information The free form information.
   */
  public void setInfo(String information)
  {
      info_ = information;
  }


  /**
   * Converts DER length field to int and returns total length of certificate. Assumes first input byte is sequence tag and certificate length is less than 3.2 Gigabyte.
   */
  private int getLength (byte[] derSeq)
  {

      int length, i;
      int lengthByteCount = derSeq[1];
      int addlen = 2;


      if (0x00 == (derSeq[1] & 0x80))
      {
       length =  derSeq[1];
      }

      else
      {
       lengthByteCount &= 0x07f;

       for (length = 0, i = 2;
            lengthByteCount > 0;
            lengthByteCount--, ++i )
       {
           length <<= 8;
           length +=  derSeq[i] & 0x0ff;
           addlen +=      1;
       }
      }

      return length + addlen;
  }

  /**
   *Returns the copyright for the class.
   *@return The copyright for this class.
  **/
  private static String getCopyright()
  {
    return Copyright.copyright;
  }


}

