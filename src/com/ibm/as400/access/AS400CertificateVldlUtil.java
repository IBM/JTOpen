///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400CertificateVldlUtil.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.Beans;
import java.beans.PropertyVetoException;
import java.util.EventListener;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Vector;


/**
   <p>The AS400CertificateVldlUtil class provides the implementation of the methods for accessing certificates in an AS400 validation list object.
**/
public class AS400CertificateVldlUtil extends AS400CertificateUtil implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  /**
   * The validation list class implementation, either native or remote.
  **/
  AS400CertificateVldlUtilImpl vldlImpl_ = null;


  /**
   * Constructs an AS400CertificateVldlUtil object.
  **/
  public AS400CertificateVldlUtil()
  {
  }

  /**
   * Constructs an AS400CertificateVldlUtil object.
   *
   * @param system  The AS/400 system on which the validation list exists.
   * @param path    The fully qualified integrated file system path name of the validation list.  For example, /QSYS.LIB/MYLIB.LIB/MYVLDL.VLDL.
   */
  public AS400CertificateVldlUtil(AS400 system, String path)
  {
      super(system, path);

      if (!(objectType_.equalsIgnoreCase("VLDL")) ||
       objectName_.length() > 10)
      {
       throw new ExtendedIllegalArgumentException("path (" + path + ")", ExtendedIllegalArgumentException.PATH_NOT_VALID);
      }
  }


   /**
     Connect()

     Determines the type of implementation that will be used.
     System and Path parameters are committed at this time.
   **/
   void connect() throws IOException
   {

     // Ensure that the system has been set.
     if (system_ == null)
     {
      Trace.log(Trace.ERROR, "Parameter 'system' is null at connect.");
         throw new ExtendedIllegalStateException("system",
                          ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }

     // Ensure that the path has been set.
     if (ifsPathName_ == null)
     {
      Trace.log(Trace.ERROR, "Parameter 'path' is null.");
      throw new ExtendedIllegalStateException("ifsPathName",
                          ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }

     // Determine if we are running on the AS/400 or some remote system.
     vldlImpl_ = (com.ibm.as400.access.AS400CertificateVldlUtilImpl)
       system_.loadImpl(
          "com.ibm.as400.access.AS400CertificateVldlUtilImplNative",
          "com.ibm.as400.access.AS400CertificateVldlUtilImplRemote");

     impl_ = (com.ibm.as400.access.AS400CertificateUtilImpl)
       system_.loadImpl(
               "com.ibm.as400.access.AS400CertificateUtilImplNative",
               "com.ibm.as400.access.AS400CertificateUtilImplRemote");

     //update vldl and base class impl references
     vldlImpl_.system_ = system_;
     impl_.system_ = system_;

     Converter conv = new Converter(system_.getCcsid(), system_);  // @C1A
     vldlImpl_.setConverter(conv);  // @C0C @C1C
     impl_.setConverter(conv);

     // Set the connection flag, commits system and path parameters.
     connected_ = true;

   }


  /**
   * Add the certificate to the repository.  Throws an ExtendedIOException if the certificate is already a member of the repository.
   *
   * @param certificate The complete ASN.1 X.509 certificate to be added to the validation list.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate.
   * @exception ExtendedIOException If certificate already added and other AS400 certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException If the AS400 object does not exist.
   */
   public void addCertificate(byte[] certificate)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

   {
      int rc;


      if (null == certificate)
      {
       Trace.log(Trace.ERROR, "Parameter 'certificate' is null.");
       throw new NullPointerException("certificate");
      }

      if (isConnected() == false)
       connect();

      //makes either remote or local call
      rc = vldlImpl_.calladdCertificate(certificate, certificate.length,
                        ifsPathName_, ifsPathName_.length());

      if (rc != SUCCESS)
      {
       throwException(vldlImpl_.cpfError_, ifsPathName_, rc);
      }

      fireAdd();
  }



  /**
   * Determines if the certificate is in the validation list.
   *
   * @param certificate  The ASN.1 encoded X.509 certificate to search for in the validation list.
   *
   * @return true if the certificate is found in the validation list; false otherwise.
   *
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate.
   * @exception ExtendedIOException If other AS400 certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException If the AS400 object does not exist.
   */
   public boolean checkCertificate(byte[] certificate)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

  {
      int rc;


      if (null == certificate)
      {
       Trace.log(Trace.ERROR, "Parameter 'certificate' is null.");
       throw new NullPointerException("certificate");
      }

      if (isConnected() == false)
         connect();

      //makes either remote or local call
      rc = vldlImpl_.callcheckCertificate(
                       certificate,
                       certificate.length,
                       ifsPathName_, ifsPathName_.length(),
                       1);

      if (rc != SUCCESS)
      {
       throwException(vldlImpl_.cpfError_, ifsPathName_, rc);
      }

      return  ((vldlImpl_.present_ == 1) ? true : false);
  }

  /**
   * Determines if a certificate matching the handle exists in the validation list.
   *
   * @param certificateHandle The AS400 certificate handle matching the certificate.
   *
   * @return true if a certificate matching the handle is found in the validation list; false otherwise.
   *
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate handle.
   * @exception ExtendedIOException If other AS400 certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException If the AS400 object does not exist.
   */
   public boolean checkCertificateByHandle(byte[] certificateHandle)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

  {
       int rc;

       if (null == certificateHandle)
       {
        Trace.log(Trace.ERROR, "Parameter 'certificateHandle' is null.");
        throw new NullPointerException("certificateHandle");
       }

       if (isConnected() == false)
        connect();

      //makes either remote or local call
       rc = vldlImpl_.callcheckCertificate(
                        certificateHandle, certificateHandle.length,
                        ifsPathName_, ifsPathName_.length(),
                        2);

       if (rc != SUCCESS)
       {
        throwException(vldlImpl_.cpfError_, ifsPathName_, rc);
       }

       return  ((vldlImpl_.present_ == 1) ? true : false);
  }



  /**
   * Deletes the certificate from the validation list. Throws an ExtendedIOException if the certificate is not present in the validation list.
   *
   * @param certificate The complete ASN.1 X.509 Certificate to be deleted from the repository.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate.
   * @exception ExtendedIOException If certificate not found and other AS400 certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException If the AS400 object does not exist.
   */
   public void deleteCertificate(byte[] certificate)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

  {
       int rc;

      if (null == certificate)
      {
       Trace.log(Trace.ERROR, "Parameter 'certificate' is null.");
       throw new NullPointerException("certificate");
      }

      if (isConnected() == false)
       connect();

      //makes either remote or local call
      rc = vldlImpl_.calldeleteCertificate(
                        certificate, certificate.length,
                        ifsPathName_, ifsPathName_.length(),
                        1);

      if (rc != SUCCESS)
      {
       throwException(vldlImpl_.cpfError_, ifsPathName_, rc);
      }

     fireDelete();
  }



  /**
   * Deletes the certificate matching the certificate handle from the validation list. Throws ExtendedIOException if the certificate is not present in the repository.
   *
   * @param certificateHandle The AS400 certificate handle matching the certificate to be deleted from the repository.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate handle.
   * @exception ExtendedIOException If certificate not found and other AS400 certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException If the AS400 object does not exist.
   */
   public void deleteCertificateByHandle(byte[] certificateHandle)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

  {
      int rc;


      if (null == certificateHandle)
      {
       Trace.log(Trace.ERROR, "Parameter 'certificateHandle' is null.");
       throw new NullPointerException("certificateHandle");
      }

      if (isConnected() == false)
       connect();

      //makes either remote or local call
      rc = vldlImpl_.calldeleteCertificate(
                    certificateHandle, certificateHandle.length,
                    ifsPathName_, ifsPathName_.length(),
                    2);

      if (rc != SUCCESS)
      {
       throwException(vldlImpl_.cpfError_, ifsPathName_, rc);
      }

      fireDelete();
  }


  /**
   * Lists certificates which match the specified attributes are copied from the validation list into the specified user space.
   * The validation list is not locked during the listCertificates operation.
   * Certificates are stored into the user space with CERT0100 format. See the AS400 QsyListVldlCertificates (QSYLSTVC) and QsyListUserCertificates (QSYLSTUC) API's for further information.
   *
   * @param certificateAttributes  The list of attributes the certificate should match.
   *    A value of null places all certificates from the validation list into the user space.
   *    An empty String or empty byte array search attribute will search for certificates that do not have this attribute.
   *    For example, SUBJECT_ORGANIZATION = new String("") will search for certificates without the subject organization field.
   *    Null search attributes are ignored.
   * @param userSpaceName  The fully qualified integrated file system path name of the user space to put the list results, for example, /QSYS.LIB/MYLIB.LIB/MYUSRSPC.USRSPC. The 10 char AS400 library of the user space may also be specified as %CURLIB% or %LIBL%. @see QSYSObjectPathName
   *
   * @return  The number of certificates found.
   *
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid search attributes or input parameter.
   * @exception ExtendedIOException If AS400 certificate access error.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException If the AS400 object does not exist.
   */

   public int listCertificates(
                     AS400CertificateAttribute[] certificateAttributes,
                     String userSpaceName)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           ExtendedIOException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException

{
    int rc;

    String as400usrSpaceName;

    if (isConnected() == false)
     connect();

      //orders and saves the user's search attrs
    boolean[] parmsEntered = setSearchAttributes(certificateAttributes);

      //get usrSpaceName in as400 format. check for nonnull values.
    as400usrSpaceName = getAS400UserSpaceName(userSpaceName);

       //makes either remote or local call
    rc = vldlImpl_.calllistCertificates(
                     ifsPathName_, ifsPathName_.length(),
                     as400usrSpaceName,
                     parmsEntered,
                     as400AttrS_,
                     as400AttrB_);

    if (rc != SUCCESS)
    {
     throwException(vldlImpl_.cpfError_, ifsPathName_, rc);
    }

    return vldlImpl_.numberCertificatesFound_;
}


   /**
     Sets the path for the validation list.

        @param path  The fully qualified integrated file system path name of the validation list.
        @exception PropertyVetoException If the change is vetoed.
   **/
  public void setPath(String path)
    throws PropertyVetoException
  {

      String libName;
      String objectName;
      String objectType;

      // check parm
      if (path == null)
      {
       Trace.log(Trace.ERROR, "Parameter 'path' is null.");
          throw new NullPointerException("path");
      }

      QSYSObjectPathName ifs = new QSYSObjectPathName(path);
      libName = ifs.getLibraryName();
      objectName = ifs.getObjectName();
      objectType = ifs.getObjectType();

      if (null == libName || null == objectName ||
       objectName.length() > 10              ||
       !(objectType.equalsIgnoreCase("VLDL")) )
      {
       throw new ExtendedIllegalArgumentException("path", ExtendedIllegalArgumentException.PATH_NOT_VALID);
      }

      // Set path the first time.
      if (ifsPathName_ == null)
      {
       ifsPathName_ = path;
       libName_ = libName;
       objectName_ = objectName;
       objectType_ = objectType;
      }
      else
      {
         // If system property is set, make sure we have not already connected.
         if (system_ != null)
         {
             if (isConnected() )
             {
                 Trace.log(Trace.ERROR, "Parameter 'path' is not changed (Connected=true).");
                 throw new ExtendedIllegalStateException("path",
                     ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
             }
         }

          // Remember the current path value.
          String oldPath = ifsPathName_;

          // Fire a vetoable change event for the path.
          vetos_.fireVetoableChange("path", oldPath, path);

          // Update the path value.
       ifsPathName_ = path;
       libName_ = libName;
       objectName_ = objectName;
       objectType_ = objectType;

       // Fire the property change event.
          changes_.firePropertyChange("path", oldPath, path);
       }

  }


  // Returns the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }


} // End of AS400CertificateVldlUtil class





