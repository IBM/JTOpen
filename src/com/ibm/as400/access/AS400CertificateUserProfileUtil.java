///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateUserProfileUtil.java
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
   <p>The AS400CertificateUserProfileUtil class accesses certificates in an AS400 user profile object.
**/
public class AS400CertificateUserProfileUtil extends AS400CertificateUtil implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;


  /**
   * The user profile class implementation, either native or remote.
  **/
  AS400CertificateUsrPrfUtilImpl usrprfImpl_ = null;


  /**
   *  Constructs an AS400CertificateUserProfileUtil object.
   **/
  public AS400CertificateUserProfileUtil()
  {
  }

  /**
   * Constructs an AS400CertificateUserProfileUtil object. If the user profile specified is not the user profile that is currently running, adding and deleting certificates require *SECADM special authority for the currently running user profile and *USE and *OBJMGT authorities to the target user profile.
   *
   * @param system  The AS/400 system on which the user profile exists.
   *
   * @param path  The fully qualified integrated file system path name of the user profile, for example, "/QSYS.LIB/MYLIB.LIB/MYUSRPRF.USRPRF".
   */
  public AS400CertificateUserProfileUtil(AS400 system, String path)
  {
      super(system, path);

      if (!(objectType_.equalsIgnoreCase("USRPRF")) ||
       objectName_.length() > 10)
      {
       throw new ExtendedIllegalArgumentException("path (" + path + ")", ExtendedIllegalArgumentException.PATH_NOT_VALID);
      }

      //make a 10 char user profile name
      objectName_ = objectName_ + "          ";
      objectName_ = objectName_.substring(0, 10);
      objectName_ = objectName_.toUpperCase();
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
     usrprfImpl_ = (com.ibm.as400.access.AS400CertificateUsrPrfUtilImpl)
       system_.loadImpl(
          "com.ibm.as400.access.AS400CertificateUsrPrfUtilImplNative",
          "com.ibm.as400.access.AS400CertificateUsrPrfUtilImplRemote");

     impl_ = (com.ibm.as400.access.AS400CertificateUtilImpl)
       system_.loadImpl(
          "com.ibm.as400.access.AS400CertificateUtilImplNative",
          "com.ibm.as400.access.AS400CertificateUtilImplRemote");

     //update user profile and base class impl references
     usrprfImpl_.system_ = system_;
     impl_.system_ = system_;

     Converter conv = new Converter(system_.getCcsid(), system_);  // @C1A
     usrprfImpl_.setConverter(conv);  // @C0C @C1C
     impl_.setConverter(conv);

     // Set the connection flag, commits system and path parameters.
     connected_ = true;

   }


  /**
   * Adds the certificate to the user profile.  Throws an ExtendedIOException if the certificate is already a member of the user profile.
   *
   * @param certificate The complete ASN.1 X.509 certificate to be added to the user profile.
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
      rc = usrprfImpl_.calladdCertificate(
                       certificate, certificate.length,
                        objectName_);

      if (rc != SUCCESS)
      {
       throwException(usrprfImpl_.cpfError_, ifsPathName_, rc);
      }

      fireAdd();
  }


  /**
   * Deletes the certificate from the user profile. Throws an ExtendedIOException if the certificate is not present in the user profile.
   *
   * @param certificate The complete ASN.1 X.509 certificate to be deleted from the user profile.
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
      rc = usrprfImpl_.calldeleteCertificate(
                        certificate, certificate.length,
                        objectName_,                  1);

      if (rc != SUCCESS)
      {
       throwException(usrprfImpl_.cpfError_, ifsPathName_, rc);
      }

     fireDelete();
  }



  /**
   * Deletes the certificate matching the certificate handle from the user profile. Throws ExtendedIOException if the certificate is not present in the user profile.
   *
   * @param certificateHandle The AS400Certificate handle of the certificate to be deleted from the user profile.
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
      rc = usrprfImpl_.calldeleteCertificate(
                    certificateHandle, certificateHandle.length,
                    objectName_,
                    2);

      if (rc != SUCCESS)
      {
       throwException(usrprfImpl_.cpfError_, ifsPathName_, rc);
      }

      fireDelete();
  }


  /**
   * Returns the user profile name which contains the certificate. Throws ExtendedIOException if the certificate is not registered to any user profile on the system.
   *
   * @param certificate  The ASN.1 X.509 encoded certificate to search for in the user profile.
   *
   * @return  The user profile name containing the certificate.
   *
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate.
   * @exception ExtendedIOException If certificate not found and other AS400 certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException If the AS400 object does not exist.
   */
   public String findCertificateUser(byte[] certificate)
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
      rc = usrprfImpl_.callfindCertificateUser(
                            certificate,
                            certificate.length,
                            1);

      if (rc != SUCCESS)
      {
       throwException(usrprfImpl_.cpfError_, ifsPathName_, rc);
      }

      return  ((null == usrprfImpl_.userName_) ? null : usrprfImpl_.userName_.trim());

  }




  /**
   * Returns the user profile which contains the certificate with the specified handle. Throws ExtendedIOException if the certificate is not registered to any user profile on the system.
   *
   * @param certificateHandle The AS400Certificate handle the certificate should match.
   *
   * @return  The user profile name containing the certificate.
   *
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception ExtendedIllegalArgumentException If invalid certificate handle.
   * @exception ExtendedIOException If certificate not found and other AS400 certificate access errors.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException If the AS400 object does not exist.
   */
   public String findCertificateUserByHandle(byte[] certificateHandle)
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
      rc = usrprfImpl_.callfindCertificateUser(
                                certificateHandle,
                                certificateHandle.length,
                                2);

      if (rc != SUCCESS)
      {
       throwException(usrprfImpl_.cpfError_, ifsPathName_, rc);
      }

     return  ((null == usrprfImpl_.userName_) ? null : usrprfImpl_.userName_.trim());
   }



  /**
   * List certificates which match the specified attributes.  The certificates are copied from the user profile to the specified user space.
   * The user profile is not locked during the listCertificates operation. Certificates are stored into the user space with CERT0100 format. See the AS400 QsyListVldlCertificates (QSYLSTVC) and QsyListUserCertificates (QSYLSTUC) API's for further information.
   *
   * @param certificateAttributes  The list of attributes the certificate should match.
   *    A value of null places all certificates from user profile into the user space.
   *    An empty String or empty byte array search attribute will search for certificates that do not have this attribute.
   *    For example, SUBJECT_ORGANIZATION = new String("") will search for certificates without the subject organization field.
   *    Null search attributes are ignored.
   * @param userSpaceName  The fully qualified integrated file system path name of the user space to put the list results, for example, /QSYS.LIB/MYLIB.LIB/MYUSRSPC.USRSPC. The 10 character AS400 library of the user space may also be specified as %CURLIB% or %LIBL%. @see QSYSObjectPathName
   *
   * @return  The number of certificates found matching the search attributes.
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

      // get usrSpaceName in as400 format. check for nonnull values.
    as400usrSpaceName = getAS400UserSpaceName(userSpaceName);

       //makes either remote or local call
    rc = usrprfImpl_.calllistCertificates(
                            objectName_,
                            as400usrSpaceName,
                            parmsEntered,
                            as400AttrS_,
                            as400AttrB_);

    if (rc != SUCCESS)
    {
     throwException(usrprfImpl_.cpfError_, ifsPathName_, rc);
    }

    return usrprfImpl_.numberCertificatesFound_;
}

   /**
     Sets the path for the user profile.

        @param path  The fully qualified integrated file system path name of the user profile.
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
       !(objectType.equalsIgnoreCase("USRPRF")) )
      {
       throw new ExtendedIllegalArgumentException("path", ExtendedIllegalArgumentException.PATH_NOT_VALID);
      }

      //make a 10 char user profile name
      objectName = objectName + "          ";
      objectName = objectName.substring(0, 10);
      objectName = objectName.toUpperCase();

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


} // End of AS400CertificateUserProfileUtil class





