///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400CertificateUtil.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.Beans;
import java.util.EventListener;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.net.UnknownHostException;
import java.util.Vector;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;


/**
   <p>The AS400CertificateUtil class provides the methods common to AS400CertificateVldlUtil and AS400CertificateUserProfileUtil.
  * The following example demonstrates the use of AS400CertificateUtil, AS400CertificateVldlUtil, and AS400CertificateUserProfileUtil. It copies an arbitrary number of X.509 certificates from an i5/OS user profile to an i5/OS validation list (vldl) object. The user profile certificates are first placed into a user space and then added to the validation list:<br>
  * <PRE>
      // Get certificates from the local system
    AS400 as400 = new AS400();
<BR>
      // Local variables
    AS400Certificate   as400certificate;
    AS400Certificate[] certs;
<BR>
    Vector  certVector = new Vector();
    byte[]  handle;
    int     numberCerts;
    String  userName;
<BR>
<BR>
    try
    {
<BR>
    AS400CertificateUserProfileUtil usrprf =
       new AS400CertificateUserProfileUtil(as400, "/QSYS.LIB/MYNAME.USRPRF");
<BR>
    AS400CertificateVldlUtil vldl =
       new AS400CertificateVldlUtil(as400, "/QSYS.LIB/MYLIB.LIB/TEST.VLDL");
<BR>
    AS400CertificateAttribute[] certAttribute = new AS400CertificateAttribute[2];
<BR>
<BR>
      // Copy certificates that belong to both "US" and "myname".
    certAttribute[0] =
       new AS400CertificateAttribute(AS400CertificateAttribute.SUBJECT_COUNTRY, "US");
<BR>
    certAttribute[1] =
       new AS400CertificateAttribute(AS400CertificateAttribute.SUBJECT_COMMON_NAME, "myname");
<BR>
<BR>
      // Copy matching certificates from the user profile to user space, MYSPACE.
    numberCerts = usrprf.listCertificates(certAttribute, "/QSYS.LIB/MYLIB.LIB/MYSPACE.USRSPC");
<BR>
    System.out.println("Number of certificates found => " +  numberCerts);
<BR>
<BR>
      // Start reading certificates from the user space into AS400Certificate[].
      // All complete certificates in the 8 Kbyte buffer will be returned.
    certs = usrprf.getCertificates("/QSYS.LIB/MYLIB.LIB/MYSPACE.USRSPC", 0, 8);
<BR>
       // Continue to read the entire user space using 8 Kbyte buffer
     while (null != certs)
     {
            // Gather certificates in a vector
          for (int i = 0; i < certs.length; ++i)
          {
            certVector.addElement(certs[i]);
          }
<BR>
            certs = usrprf.getNextCertificates(8);
     }
<BR>
<BR>
      // Add all the certificates to validation list object
     for (int i = 0; i < certVector.size(); ++i)
     {
       as400certificate =  (AS400Certificate)certVector.elementAt(i);
       vldl.addCertificate(as400certificate.getEncoded());
     }
<BR>
       // Delete first certificate added to vldl using its handle
     as400certificate =  (AS400Certificate)certVector.elementAt(0);
     handle = usrprf.getCertificateHandle(as400certificate.getEncoded());
     vldl.deleteCertificateByHandle(handle);
<BR>
       // Delete 2nd certificate added to vldl using entire ASN.1 certificate
     as400certificate =  (AS400Certificate)certVector.elementAt(1);
     vldl.deleteCertificate(as400certificate.getEncoded());
<BR>
       // Display user profile name associated with the 1st certificate
     userName = usrprf.findCertificateUserByHandle(handle);
<BR>
     System.out.println("User profile name => " + userName);
    }
<BR>
    catch (Exception e)
    {
        System.out.println(e.toString());
    }
  * </PRE>
  *
  *@see AS400CertificateVldlUtil
  *@see AS400CertificateUserProfileUtil
 **/

abstract public class AS400CertificateUtil implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



  /**
   * Recommended buffer size in kilobytes.  Used when returning certificates found during a get certificate operation.
   */
  public static final int DEFAULT_BUFFER_SIZE = 128;

  /**
   * Maximum buffer size in kilobytes.  Used when returning certificates found during a get certificate operation.
   */
  public static final int MAX_BUFFER_SIZE = 16384;

  /**
   * Minimum buffer size in kilobytes.  Used when returning certificates found during a get certificate operation.
   */
  public static final int MIN_BUFFER_SIZE = 8;


  // Object's fully qualified IFS name
  String ifsPathName_;

  // 10 char i5/OS lib name
  String libName_;

  // 10 char i5/OS object name
  String objectName_;

  // IFS object type.
  String objectType_;

  // User space 20 char "object || lib" name
  String usrSpaceName_;

  // The i5/OS connection information
  AS400 system_ = null;

  transient  boolean connected_ = false;

  // Java beans support
  transient PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
  transient Vector certListeners_ = new Vector();
  transient VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);

  // String certificate attributes to search on
  String[]     as400AttrS_;

  // Byte[] certificate attributes to search on, eg, public key
  byte[] []    as400AttrB_;

  // Next cert to return for getCertificates()
  int nextCertificateToReturn_ = 0;

  // User space offset of next cert to return for getNextCertificates()
  int nextCertificateOffset_ = -1;

  //total number of certificates in user space
  int numberCertificatesFound_ = 0;

  //the base class implementation, either native or remote
  transient AS400CertificateUtilImpl impl_ = null;

  //exceptions thrown by base and derived classes
  final static int EXTENDED_IO_EXCP     = 1;
  final static int ILLEGAL_ARG_EXCP     = 2;
  final static int ACCESS_EXCP          = 3;
  final static int DOES_NOT_EXIST_EXCP  = 4;
  final static int INTERNAL_ERR_EXCP    = 5;
  final static int SUCCESS         = 0;

  // Free form additional information about this i5/OS Object.
  private String info_;



  /**
   * Constructs an AS400CertificateUtil object.
  **/
  public AS400CertificateUtil()
  {
  }

  /**
   * Constructs an AS400CertificateUtil object.
   *
   * @param  system  The server on which the certificate repository exists.
   * @param  path  The fully qualified integrated file system path name of the validation list or user profile. For example, /QSYS.LIB/MYLIB.LIB/MYVLDL.VLDL or /QSYS.LIB/MYPROFILE.USRPRF.
   */
  public AS400CertificateUtil(AS400 system, String path)
  {
      system_ = system;
      if (null == system_)
      {
       Trace.log(Trace.ERROR, "Parameter 'system' is null.");
       throw new NullPointerException("system");
      }

      ifsPathName_ = path;
      QSYSObjectPathName ifs = new QSYSObjectPathName(path);
      libName_ = ifs.getLibraryName();
      objectName_ = ifs.getObjectName();
      objectType_ = ifs.getObjectType();

      if (null == libName_ || null == objectName_)
      {
       throw new ExtendedIllegalArgumentException("path", ExtendedIllegalArgumentException.PATH_NOT_VALID);
      }

  }



  /**
   * Adds the certificate to the repository.  Throws an ExtendedIOException if the certificate is already a member of the repository.
   *
   * @param certificate The ASN.1 Certificate to be added to the repository.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate.
   * @exception ExtendedIOException If certificate already added and  other i5/OS certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the i5/OS object does not exist.
   */
  abstract public void addCertificate(byte[] certificate)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIllegalArgumentException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;



    /**
     * Adds an AS400Certificate listener to receive AS400Certificate events.
     *
     * @see #removeAS400CertificateListener
     * @param listener The object listener.
     */
    public synchronized void addAS400CertificateListener(AS400CertificateListener listener) {
        certListeners_.addElement(listener);
    }



    /**
     * Adds a property change listener.
     * The specified property change listeners <b>propertyChange</b> method will
     * be called each time the value of any bound property is changed.
     * The property listener object is added to a list of property change listeners.
     * It can be removed with the removePropertyChangeListener() method.
     *
     * @param l The property change listener.
     * @see #removePropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes_.addPropertyChangeListener(l);
    }

    /**
     * Adds the VetoableChangeListener.
     * The specified VetoableChangeListeners <b>vetoableChange</b> method will
     * be called each time the value of any constrained property is changed.
     *
     * @see #removeVetoableChangeListener
     * @param l The VetoableChangeListener.
     */
    public void addVetoableChangeListener(VetoableChangeListener l) {
        vetos_.addVetoableChangeListener(l);
    }




  /**
   * Deletes the certificate from the repository. Throws ExtendedIOException
   * if the certificate is not present in the repository.
   *
   * @param certificate The ASN.1 Certificate to be deleted from the repository.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate.
   * @exception ExtendedIOException If certificate not found and  other i5/OS certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the i5/OS object does not exist.
   */
  abstract public void deleteCertificate(byte[] certificate)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIllegalArgumentException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;



  /**
   * Deletes the certificate from the repository. Throws ExtendedIOException
   * if the certificate is not present in the repository.
   *
   * @param certificatehandle The i5/OS certificate handle of the certificate to be deleted from the repository.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate handle.
   * @exception ExtendedIOException If certificate not found and other i5/OS certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the i5/OS object does not exist.
   */
  abstract public void deleteCertificateByHandle(byte[] certificatehandle)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIllegalArgumentException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;


  /**
   * Retrieves the certificate placed in a user space by listCertificates. The certificates are not deleted from the user space.
   *   Returns certificates starting from firstCertificateToReturn(), inclusive.
   *   The first certificate in the user space is at location 0.
   *
   * @param userSpaceName  The fully qualified integrated file system path name of the user space to get the certificates, for example, /QSYS.LIB/MYLIB.LIB/MYUSRSPC.USRSPC.  The ten character AS4/00 library of the user space may also be specified as %CURLIB% or %LIBL%.
   * See {@link com.ibm.as400.access.QSYSObjectPathName QSYSObjectPathName}
   * @param firstCertificateToReturn  The first certificate in the user space to return. The first certificate in the user space is at location 0.
   * @param buffSize  The number of kilobytes allocated for the returned certificates.  Increasing this value for remote invocations will require more client memory and longer transmission times. The recommended default buffer size is 128 kilobytes. The minimum buffer size allowed is 8 kilobytes.
   *
   * @return  An array of AS400Certificates which fit in a buffer of size bufferSize.
   *
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If buffer size out of range or too small for one certificate, firstCertificateToReturn set to more than the total number of certificates in user space, and other invalid input parameters.
   * @exception ExtendedIOException If no certificate returned, user space certificates not stored in format "CERT0100", and other i5/OS certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the i5/OS object does not exist.
   */
  public AS400Certificate [] getCertificates(
                         String userSpaceName,
                         int firstCertificateToReturn,
                         int buffSize)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIllegalArgumentException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

    {

    int rc;


    if (isConnected() == false)
     connect();

      // set usrSpaceName_ instance var. check for nonnull values.
    setAS400UserSpaceName(userSpaceName);

    if (firstCertificateToReturn < 0)
     throw new ExtendedIllegalArgumentException("firstCertificateToReturn (" + firstCertificateToReturn + ")",                                ExtendedIllegalArgumentException.RANGE_NOT_VALID);


    if (buffSize < MIN_BUFFER_SIZE || buffSize > MAX_BUFFER_SIZE)
     throw new ExtendedIllegalArgumentException("buffSize (" + buffSize + ")",                             ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    buffSize = buffSize * 1024;

      //makes either remote or local call
    rc = impl_.callgetCertificates(usrSpaceName_,
                        buffSize,
                        firstCertificateToReturn,
                        -1);

    if (rc != SUCCESS)
    {
     throwException(impl_.cpfError_, ifsPathName_, rc);
    }

      //nothing returned
    if (impl_.certificates_ == null)
     throw new ExtendedIOException("userSpaceName (" + userSpaceName + ")", ExtendedIOException.CERTIFICATE_NOT_FOUND);

      //update counters
    nextCertificateToReturn_ = impl_.certificates_.length + firstCertificateToReturn;
    numberCertificatesFound_ = impl_.numberCertificatesFound_;
    nextCertificateOffset_ = impl_.nextCertificateOffsetOut_;


    if (nextCertificateToReturn_ >= numberCertificatesFound_)
    {
       //finished
     nextCertificateToReturn_ = -1;
    }

    return impl_.certificates_;

}



  /**
   * Returns the i5/OS certificate handle which uniquely identifies this certificate.
   *
   * @return  The i5/OS certificate handle.
   *
   * @param certificate The ASN.1 Certificate used to generate the handle.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIOException If invalid certificate and  other i5/OS certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the i5/OS object does not exist.
   */
  public byte[] getCertificateHandle(byte[] certificate)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

  {
      int rc;
      int exception;
      int msgNumber;

      if (null == certificate)
      {
       Trace.log(Trace.ERROR, "Parameter 'certificate' is null.");
       throw new NullPointerException("certificate");
      }

      if (isConnected() == false)
       connect();

      //makes either remote or local call
      rc = impl_.callgetHandle(certificate,
                    certificate.length);

      if (rc != SUCCESS)
      {
       //determine which exception to throw.
       //allows 1000 msgs for each exception type.
       exception = rc / 1000;
       msgNumber = rc % 1000;

       if (null == impl_.cpfError_ || 0 == impl_.cpfError_.length())
       {
           impl_.cpfError_ = ifsPathName_;
       }
       else
           impl_.cpfError_ = impl_.cpfError_.trim()+  ": " + ifsPathName_;

       switch(exception)
       {
           case EXTENDED_IO_EXCP:
            throw new ExtendedIOException(impl_.cpfError_, msgNumber);

           case ILLEGAL_ARG_EXCP:

           default:
            throw new ExtendedIOException(impl_.cpfError_, ExtendedIOException.INVALID_CERTIFICATE);

       } // End of switch
      }

      return impl_.handle_;
  }




  /**
   *  Retrieves certificates placed in the user space by listCertificates starting at the first certificate in the user space.
   *
   * @param userSpaceName  The fully qualified integrated file system path name of the user space to get the certificates, for example, /QSYS.LIB/MYLIB.LIB/MYUSRSPC.USRSPC.  The ten character library of the user space may also be specified as %CURLIB% or %LIBL%.
   * See {@link com.ibm.as400.access.QSYSObjectPathName QSYSObjectPathName}
   * @param buffSize  The number of kilobytes allocated for the returned certificates.
   *   Increasing this value for remote invocations will require more client memory and longer transmission times. The recommended default buffer size is 128 kilobytes. The minimum buffer size allowed is 8 kilobytes.
   *
   * @return  An array of AS400Certificates which fit in a buffer of size bufferSize.
   *
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If buffer size out of range or too small for one certificate and other invalid input parameters.
   * @exception ExtendedIOException If no certificate returned, user space certificates not stored in format "CERT0100", and other i5/OS certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the i5/OS object does not exist.
   */
  public AS400Certificate [] getFirstCertificates(String userSpaceName,
                                int buffSize)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIllegalArgumentException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

    {

    int rc;


    if (isConnected() == false)
     connect();

      // set usrSpaceName_ instance var. check for nonnull values.
    setAS400UserSpaceName(userSpaceName);

    if (buffSize < MIN_BUFFER_SIZE || buffSize > MAX_BUFFER_SIZE)
     throw new ExtendedIllegalArgumentException("buffSize (" + buffSize + ")",                             ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    buffSize = buffSize * 1024;

      //makes either remote or local call
    rc = impl_.callgetCertificates(usrSpaceName_,
                        buffSize,
                        0,
                        -1);
    if (rc != SUCCESS)
    {
     throwException(impl_.cpfError_, ifsPathName_, rc);
    }

      //nothing returned
    if (impl_.certificates_ == null)
     throw new ExtendedIOException("userSpaceName (" + userSpaceName + ")", ExtendedIOException.CERTIFICATE_NOT_FOUND);

      //update counters from impl object
    nextCertificateToReturn_ = impl_.certificates_.length;
    numberCertificatesFound_ = impl_.numberCertificatesFound_;
    nextCertificateOffset_ = impl_.nextCertificateOffsetOut_;

      //check for done
    if (nextCertificateToReturn_ >= numberCertificatesFound_)
    {
     //finished
     nextCertificateToReturn_ = -1;
    }

    return impl_.certificates_;

}






  /**
    *Returns the name of the i5/OS certificate repository.
    *
    *@return  The i5/OS object name.  If the name has not been set, an empty string is returned.
    **/
  public String getName()
  {
    if (ifsPathName_==null)
        return "";
    return objectName_;
  }



  /**
   * Retrieves the next certificates placed in a user space by listCertificates.
   *   getCertificates or getFirstCertificates must be invoked first to set the user space name
   *   and initial certificate to return or unpredicatable results will occur.
   *   Returns certificates starting from the last call to getNextCertificates.
   * @see AS400CertificateUtil#listCertificates
   *
   * @param buffSize   The number of kiloBytes allocated for the returned certificates. Increasing this value for remote invocations will require more client memory and longer transmission times. The minimum buffer size allowed is 8 kilobytes.
   *
   * @return  An array of AS400Certificates which fit in a buffer of size bufferSize.
   *  Null is returned if all certificates have been successfully retrieved.
   *
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If buffer size out of range or too small for one certificate.
   * @exception ExtendedIOException If certificates are not in "CERT0100" format in the user space, user space and initial certificate to return are not set by calling getCertificates or getFirstCertificates, and other i5/OS certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the i5/OS object does not exist.
   */
  public AS400Certificate [] getNextCertificates(int buffSize)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIllegalArgumentException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

{

    int rc;

    if (isConnected() == false)
     connect();

      //get/getFirstCertificates() not done first
    if (null == usrSpaceName_ || -1 == nextCertificateOffset_)
     throw new ExtendedIOException(ExtendedIOException.INVALID_REQUEST);

      //already done
    if (0 >  nextCertificateToReturn_)
     return null;

    if (buffSize < MIN_BUFFER_SIZE || buffSize > MAX_BUFFER_SIZE)
     throw new ExtendedIllegalArgumentException("buffSize (" + buffSize + ")",                             ExtendedIllegalArgumentException.RANGE_NOT_VALID);


    buffSize = buffSize * 1024;

      //makes either remote or local call
    rc = impl_.callgetCertificates(usrSpaceName_,
                        buffSize,
                        nextCertificateToReturn_,
                        nextCertificateOffset_);

    if (rc != SUCCESS)
    {
     throwException(impl_.cpfError_, ifsPathName_, rc);
    }

      //nothing returned but more left
    if (nextCertificateToReturn_ < numberCertificatesFound_)
    {
      if (impl_.certificates_ == null)
      {
        throw new ExtendedIOException("userSpaceName_ (" + usrSpaceName_ + ")", ExtendedIOException.CERTIFICATE_NOT_FOUND);
      }

      //update next certificate to return
      nextCertificateToReturn_ = nextCertificateToReturn_ + impl_.certificates_.length;

      //update next certificate offset to return
      nextCertificateOffset_ = impl_.nextCertificateOffsetOut_;
    }

    if (nextCertificateToReturn_ >= numberCertificatesFound_)
    {
     //finished
     nextCertificateToReturn_ = -1;
    }

    return impl_.certificates_;
}



  /**
   * Returns free form object info.
   * @return  The free form string info.
   */
  public String getObjectInfo()
  {
      return info_;
  }


  /**
    *Returns the integrated file system path name of the i5/OS certificate repository.
    *
    *@return  The fully qualified i5/OS object name.  If the name as not been set, an empty string is returned.
    **/
  public String getPath()
  {
    if (ifsPathName_==null)
        return "";
    return ifsPathName_;
  }



  /**
   *Returns the system object.
   *
   *@return The system object. If the system has not been set, null is returned.
  **/
  public AS400 getSystem()
  {
    return system_;
  }



  /**
   * Returns certificates which match the specified attributes in the specified existing user space.
   *   The underlying certificate repositories are not locked during the listCertificates operation.
   *   Certificates are stored into the user space with CERT0100 format.
   *   See the i5/OS QsyListVldlCertificates (QSYLSTVC) and QsyListUserCertificates (QSYLSTUC) API's for further information.
   *
   * @param certificateAttributes  The list of attributes the certificate must match.
   *   A value of null places all certificates from the repository into the user space.
   *   An empty String or empty byte array search attribute will search for certificates
   *   that do not have this attribute. For example, SUBJECT_ORGANIZATION = new String("") will
   *   search for certificates without the subject organization field.
   *   Null search attributes are ignored.
   *
   * @param userSpaceName The fully qualified integrated file system path name of the user space to put the list results, for example, /QSYS.LIB/MYLIB.LIB/MYUSRSPC.USRSPC.  The ten character library of the user space may also be specified as %CURLIB% or %LIBL%.
   * See {@link com.ibm.as400.access.QSYSObjectPathName QSYSObjectPathName}
   *
   * @return  The number of certificates found.
   *
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid search attributes or input parameter.
   * @exception ExtendedIOException If i5/OS certificate access error.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the i5/OS object does not exist.
   */
  abstract public int listCertificates(
                 AS400CertificateAttribute[] certificateAttributes,
                 String userSpaceName)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIllegalArgumentException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;



    /**
     * Removes this listener.
     *
     * @param l The AS400CertificateListener.
     * @see #addAS400CertificateListener
     */
    public synchronized void removeAS400CertificateListener(AS400CertificateListener l) {
        certListeners_.removeElement(l);
    }


    /**
     * Removes this property change listener.
     *
     * @param l The property change listener.
     * @see #addPropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes_.removePropertyChangeListener(l);
    }

    /**
     * Removes this vetoable change listener.
     *
     * @see #addVetoableChangeListener
     * @param l The VetoableChangeListener.
     */
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        vetos_.removeVetoableChangeListener(l);
    }


  /**
   * Set free form object information
   * @param information The free form info.
   */
  public void setObjectInfo(String information)
  {
      info_ = information;
  }



   /**
     Sets the path for the user space.

        @param path  The fully qualified integrated file system path name.
        @exception PropertyVetoException If the change is vetoed.
   **/
  abstract  public void setPath(String path)
           throws PropertyVetoException;


  /**
    *Sets the system on which the certificate repository exists.
    *
    *@param  system    The server on which the repository exists.
    *@exception PropertyVetoException If the change is vetoed.
    **/
  public void setSystem(AS400 system)
        throws PropertyVetoException
  {
    // check parm
    if (system == null)
    {
        Trace.log(Trace.ERROR, "Parameter 'system' is null.");
        throw new NullPointerException("system");
    }

    // set system parameter for first time.
    if (system_ == null)
     system_ = system;
    else
    {
       // Verify that connection has not been made.
     if (isConnected())
     {
         Trace.log(Trace.ERROR, "Parameter 'system' is not changed (Connected=true).");
         throw new ExtendedIllegalStateException("system",
                                  ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
     }

     AS400 old = system_;
     vetos_.fireVetoableChange("system", old, system );

        // set instance var
     system_ = system;
     changes_.firePropertyChange("system", old, system );
    }

  }


    /**
     * Throws exception with error text based on input return code.
     *
     * @param cpfError The 7 char CPF error id.
     * @param objectName More info about the object incurring the error.
     * @param rc The non-zero return code.
     *
     * @exception Various exceptions are thrown based on the input return code.
     */
    void throwException(String cpfError, String objectName, int rc)
      throws AS400SecurityException,
             ExtendedIllegalArgumentException,
             ExtendedIOException,
             ObjectDoesNotExistException,
             InternalErrorException
    {
     //exception type
     int exception;
     //msg number
     int msgNumber;

     //determine which exception to throw.
     //allows 1000 msgs for each exception type.
     exception = rc / 1000;
     msgNumber = rc % 1000;


     //msg format -
     //"CPF227D:/MYLIB.LIB/MYVLDL.VLDL:Certificate was not found."
     if (null == cpfError ||
         (null != cpfError && 0 == cpfError.length()))
     {
         cpfError = objectName;
     }
     else
     {
         cpfError = cpfError.trim()  + ": " + objectName;
     }


     switch(exception)
     {

         case EXTENDED_IO_EXCP:
              throw new ExtendedIOException(cpfError, msgNumber);

         case ILLEGAL_ARG_EXCP:
              throw new ExtendedIllegalArgumentException(cpfError, msgNumber);

         case ACCESS_EXCP:
          throw new AS400SecurityException(cpfError, msgNumber);

         case DOES_NOT_EXIST_EXCP:
          throw new ObjectDoesNotExistException(cpfError,
                                  msgNumber);

         case INTERNAL_ERR_EXCP:
              throw new InternalErrorException(cpfError, msgNumber);

         default:
              //Other unexpected errors
          throw new ExtendedIOException(cpfError, ExtendedIOException.UNKNOWN_ERROR);

     } // End of switch
    }


    /**
     * Sets instance variable, usrSpacename_, in i5/OS format, "10 char user space name || 10 char user space lib name". Checks for non-null values.
     *
     * @param userSpaceName  The ifs path name of the user space.
     *
     * @exception Various exceptions are thrown based on the input.
     */
    void setAS400UserSpaceName(String userSpaceName)
    {

     String uSpaceName;
     String uSpaceLib;

        // check name
     if (userSpaceName == null)
     {
         Trace.log(Trace.ERROR, "Parameter 'userSpaceName' is null.");
      throw new NullPointerException("userSpaceName");
     }


     QSYSObjectPathName ifs = new QSYSObjectPathName(userSpaceName, "USRSPC");

        // set instance vars
     uSpaceLib = ifs.getLibraryName();
     uSpaceName = ifs.getObjectName();

        // get 20 char "object || lib" i5/OS name
     usrSpaceName_ = uSpaceName + "          ";
     usrSpaceName_ = usrSpaceName_.substring(0, 10) + uSpaceLib +
       "          ";

        // trim back to 20 chars
     usrSpaceName_ = usrSpaceName_.substring(0, 20);
     usrSpaceName_ = usrSpaceName_.toUpperCase();

    }



    /**
     * Returns user space name in i5/OS format, "10 char user space name || 10 char user space lib name". Checks for non-null values.
     *
     * @param userSpaceName  The ifs path name of the user space.
     * @return The user space name in i5/OS format.
     *
     * @exception Various exceptions are thrown based on the input.
     */
    String getAS400UserSpaceName(String userSpaceName)
    {

     String as400usrSpaceName;
     String uSpaceName;
     String uSpaceLib;

        // check name
     if (userSpaceName == null)
     {
         Trace.log(Trace.ERROR, "Parameter 'userSpaceName' is null.");
         throw new NullPointerException("userSpaceName");
     }

     QSYSObjectPathName ifs = new QSYSObjectPathName(userSpaceName, "USRSPC");

        // set instance vars
     uSpaceLib = ifs.getLibraryName();
     uSpaceName = ifs.getObjectName();

        // get 20 char "object || lib" i5/OS name
     as400usrSpaceName = uSpaceName + "          ";
     as400usrSpaceName = as400usrSpaceName.substring(0, 10) + uSpaceLib +
       "          ";

        // trim back to 20 chars
     as400usrSpaceName = as400usrSpaceName.substring(0, 20);
     as400usrSpaceName = as400usrSpaceName.toUpperCase();

     return as400usrSpaceName;

    }


  /**
   * Load the String and byte search attrs into separate, ordered arrays.
   * Verify each attribute type is listed once.
   * Empty search attr's can be passed in as new String() or new byte[0].
   * Null attributes are ignored.
   */
  boolean[] setSearchAttributes(AS400CertificateAttribute[] attributes)
    throws ExtendedIllegalArgumentException
  {
    boolean    badAttr = false;
    boolean    dupAttr = false;
    int i;

    boolean [] alreadyFound = new boolean[AS400CertificateAttribute.LAST_STRING_ATTR];

    //string form search items, order dependent
    as400AttrS_ = new String[6];
    //byte form search items, order dependent
    as400AttrB_ = new byte[1] [0];

    //null returns all certs from repository
    if (null == attributes)
     return alreadyFound;

    //*************************************************************
    //Total up the attrs. If more then 7, the server does not
    //support the search. Only allow supported attrs.
    //*************************************************************
    forloop:
      for (i = 0; i <  attributes.length; ++i)
      {
       //non-null search attr slot
       if (null != attributes[i])
       {

           if (AS400CertificateAttribute.SUBJECT_COMMON_NAME == attributes[i].getAttributeType())
           {
            if (false == alreadyFound[0])
            {
                alreadyFound[0] = true;
            }
            else
            {
                dupAttr = true;
                break forloop;
            }

           //save the i5/OS attr. as400Attrs array is order dependent.
            as400AttrS_[0] = (String) attributes[i].getAttributeValue();
           }

           else
            if (AS400CertificateAttribute.SUBJECT_ORGANIZATION_UNIT == attributes[i].getAttributeType())
            {
                if (false == alreadyFound[1])
                {
                 alreadyFound[1] = true;
                }
                else
                {
                 dupAttr = true;
                 break forloop;
                }

                as400AttrS_[1] = (String) attributes[i].getAttributeValue();
            }

          else
              if (AS400CertificateAttribute.SUBJECT_ORGANIZATION == attributes[i].getAttributeType())
              {
               if (false == alreadyFound[2])
               {
                   alreadyFound[2] = true;
               }
               else
               {
                   dupAttr = true;
                   break forloop;
               }

               as400AttrS_[2] = (String) attributes[i].getAttributeValue();

              }

            else
             if (AS400CertificateAttribute.SUBJECT_LOCALITY == attributes[i].getAttributeType())
                {
                 if (false == alreadyFound[3])
                 {
                     alreadyFound[3] = true;
                 }
                 else
                 {
                     dupAttr = true;
                     break forloop;
                 }

                 as400AttrS_[3] = (String) attributes[i].getAttributeValue();

                }

          else
           if (AS400CertificateAttribute.SUBJECT_STATE == attributes[i].getAttributeType())
           {
               if (false == alreadyFound[4])
               {
                alreadyFound[4] = true;
               }
               else
               {
                dupAttr = true;
                break forloop;
               }

               as400AttrS_[4] = (String) attributes[i].getAttributeValue();
           }

         else
          if (AS400CertificateAttribute.SUBJECT_COUNTRY == attributes[i].getAttributeType())
          {
              if (false == alreadyFound[5])
              {
               alreadyFound[5] = true;
              }
              else
              {
               dupAttr = true;
               break forloop;
              }

              as400AttrS_[5] = (String) attributes[i].getAttributeValue();

          }

         else
          if (AS400CertificateAttribute.PUBLIC_KEY_BYTES == attributes[i].getAttributeType())
          {
              if (false == alreadyFound[6])
              {
               alreadyFound[6] = true;
              }
              else
              {
               dupAttr = true;
               break forloop;
              }

                //save the i5/OS attr. as400AttrB array is order dependent.
              as400AttrB_[0] = (byte[]) attributes[i].getAttributeValue();
          }

       else
           {
               //unsupported attr found
            badAttr = true;
            break forloop;
           }
       }//end non-null search attr slot

       }//end for loop


      if (dupAttr == true || badAttr == true)
      {
       throw new  ExtendedIllegalArgumentException("AS400CertificateAttribute (" + Integer.toString(i) + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }

      return alreadyFound;
  }


   /**
     Connect()

     Determines the type of implementation that will be used.
     System and Path parameters are committed at this time.
   **/
   abstract void connect() throws IOException;



   /**
     Indicates if a connection has been established.
   **/
   boolean isConnected()
   {
       return connected_;
   }



    /**
      *Fire add certificate event
      **/
    void fireAdd() {
        Vector targets;
        targets = (Vector) certListeners_.clone();
        AS400CertificateEvent event = new AS400CertificateEvent( this, AS400CertificateEvent.CERTIFICATE_ADDED);
     for (int i = 0; i < targets.size(); i++)
     {
            AS400CertificateListener target = (AS400CertificateListener)targets.elementAt(i);
            target.added(event);
        }
    }


    /**
      *Fires delete certificate event
      **/
    void fireDelete() {
        Vector targets;
        targets = (Vector) certListeners_.clone();
        AS400CertificateEvent event = new AS400CertificateEvent( this, AS400CertificateEvent.CERTIFICATE_DELETED);
     for (int i = 0; i < targets.size(); i++)
     {
            AS400CertificateListener target = (AS400CertificateListener)targets.elementAt(i);
            target.deleted(event);
        }
    }


   /**
   *Overrides the ObjectInputStream.readObject() method in order to return any
   *transient parts of the object to there properly initialized state.  We also
   *generate a declared file name for the object.  I.e we in effect
   *call the null constructor.  By calling ObjectInputStream.defaultReadObject()
   *we restore the state of any non-static and non-transient variables.  We
   *then continue on to restore the state (as necessary) of the remaining varaibles.
   *@param in The input stream from which to deserialize the object.
   *@exception ClassNotFoundException If the class being deserialized is not found.
   *@exception IOException If an error occurs while communicating with the server.
   **/
   private void readObject(java.io.ObjectInputStream in)
      throws ClassNotFoundException,
             IOException
   {
      in.defaultReadObject();

      // Reset the connected flag.
      connected_ = false;

      // Reset the listeners.
      PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
      VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);
      Vector certListeners_ = new Vector();

      // Reset some flags.
      nextCertificateToReturn_ = 0;

      // User space offset of next cert to return for getNextCertificates()
      nextCertificateOffset_ = -1;

      //total number of certificates in user space
      numberCertificatesFound_ = 0;
   }



} // End of AS400CertificateUtil class





