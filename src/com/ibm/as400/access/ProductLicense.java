///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProductLicense.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeSupport;                        // @A2A
import java.beans.PropertyChangeListener;                       // @A2A
import java.io.IOException;
import java.util.Vector;                                        // @A2A



/**
* <p>
*   A ProductLicense object represents a license for an AS/400 product. To request
*   a license, construct a ProductLicense object then invoke the request() method.
*   The caller must keep a reference to the ProductLicense object until the
*   license is no longer needed since the ProductLicense object will release
*   the license when it is garbage collected.  Licenses are managed on a
*   per-connection basis.  Each ProductLicense object has a separate connection to
*   the an AS/400 server.  If the connection ends unexpectedly the server
*   releases the license.  To maintain an accurate count, the application should
*   call release() instead of relying on the license being released when the object
*   is garbage collected or when the connection ends.  Cleanup during garbage
*   collection and/or server cleanup is not as reliable as calling release().
*
*  <P>
*   The ProductLicense class does not enforce the license policy. It is up to the
*   application to enforce the policy based on information contained in the ProductLicense
*   object after the license is requested.  If a license is not granted, indicated by a
*   LicenseException, it is up to the application to notify the user and not perform the
*   behavior that requires a license.
*
* <P>
*   The ProductLicense object may successfully get a license even though there was an error
*   retrieving the product license. These "soft" errors are usage limit exceeded, but license
*   limit not strictly enforced (CONDITION_EXCEEDED_OK), usage limit exceeded, but within grace
*   period (CONDITION_EXCEED_GRACE_PERIOD) and usage limit exceeded and grace period expired
*   but not strictly enforced (CONDITION_GRACE_PERIOD_EXPIRED.) The application must decide
*   to continue or end the application based on this information.
*
* <P>
*   The request() method will throw a LicenseException if no license is available.  If a license
*   is granted, the ProductLicense object contains information about the license such
*   as the compliance type and license condition.
*
* <h4>Example</h4>
*
* <pre>
*
*        AS400 system = new AS400("myas400");
*        // request a license for "myproductID" and "myfeatureID" for "myrelease"
*        try
*        {
*            ProductLicense license = new ProductLicense(system,
*                                                        "myproductID",
*                                                        "myfeatureID",
*                                                        "myrelease");
*            license.request();
*            switch (license.getCondition())
*            {
*                case ProductLicense.CONDITION_OK:
*                    // license retrieved successfully
*                    break;
*                case ProductLicense.CONDITION_EXCEEDED_OK:
*                    // usage limit exceeded, but license limit not strictly enforced
*                    // issue message but allow to proceed.
*                    sendMessage("Usage limit exceeded, but license limit not strictly enforced");
*                    break;
*                case ProductLicense.CONDITION_EXCEEDED_GRACE_PERIOD:
*                    // usage limit exceeded, but within grace period
*                    // issue message but allow to proceed
*                    sendMessage("Usage limit exceeded, but within grace period");
*                    break;
*                case ProductLicense.CONDITION_GRACE_PERIOD_EXPIRED:
*                    // usage limit exceeded and grace period expired but not strictly enforced
*                    // issue message but allow to proceed
*                    sendMessage("Usage limit exceeded and grace period expired but not strictly enforced");
*                    break;
*            }
*            ..
*            ..
*            // product code...
*            ..
*            ..
*            // release the license
*            license.release();
*        }
*        catch (LicenseException le)
*        {
*            // handle license failures such as license expired...
*        }
*        catch (Exception e)
*        {
*            // handle general failures (security error, communication error, etc.)
*        }
*
* </pre>
**/


public class ProductLicense implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
    *   Value for license usage type, concurrent usage license type.
    **/
    public final static int USAGE_CONCURRENT = 1;
    /**
    *   Value for license usage type, registered usage license type.
    **/
    public final static int USAGE_REGISTERED = 2;

    /**
    *   Value for compliance type, operator action compliance indicates a form
    *   of soft compliance that will not allow a license in the usage limit exceeded
    *   case until the operator increases the maximum number of licenses on the system
    *   (this does not require a license key to increase.)
    **/
    public final static int COMPLIANCE_OPERATOR_ACTION = 1;
    /**
    *   Value for compliance type, warning compliance indicates that a
    *   warning message will be sent to the system operators message queue
    *   when a license violation, such as usage limit exceeded is encountered.
    **/
    public final static int COMPLIANCE_WARNING = 2;
    /**
    *   Value for compliance type, keyed compliance indicates a license
    *   that requires a license key to activate the license.
    **/
    public final static int COMPLIANCE_KEYED = 3;

    /**
    *   Value for license condition, license granted.
    **/
    public static final int CONDITION_OK = 0;
    /**
    *   Value for license condition, usage limit exceeded, but not enforced.
    **/
    public static final int CONDITION_EXCEEDED_OK = 0x002b;
    /**
    *   Value for license condition, usage limit exceeded, but within grace period.
    **/
    public static final int CONDITION_EXCEEDED_GRACE_PERIOD = 0x002c;
    /**
    *   Value for license condition, usage limit exceeded and grace period expired, but not
    *   enforced.
    **/
    public static final int CONDITION_GRACE_PERIOD_EXPIRED = 0x002e;

    static
    {
        AS400Server.addReplyStream(new NLSExchangeAttrReply(), "as-central");
        AS400Server.addReplyStream(new LicenseGetReply(), "as-central");
        AS400Server.addReplyStream(new LicenseReleaseReply(), "as-central");
        AS400Server.addReplyStream(new LicenseGetInformationReply(), "as-central");
    }

    transient private PropertyChangeSupport changes_;   // @A2A
    transient private Vector productLicenseListeners_;  // @A2A

    static final long serialVersionUID = 4L;            // @A2A

    transient private boolean released_;                // @A2C
    transient private int condition_;                   // @A2C

    transient private AS400 sys_;                       // @A2C
    transient private AS400Server server_;              // @A2C
    transient private AS400ImplRemote sysImpl_;         // @A2C

    private String productID_;
    private String featureID_;
    private String releaseLevel_;                       // @A1C

    transient private int usageLimit_;                  // @A2C
    transient private int usageCount_;                  // @A2C
    transient private int usageType_;                   // @A2C
    transient private int complianceType_;              // @A2C
    transient private String licenseTerm_;              // @A2C



    /**
    *   Constructs a default ProductLicense object. The AS400 system,
    *   product, feature and release must be set for requesting a license.
    **/
    public ProductLicense()
    {
        initializeTransient();                          // @A2A

        productID_ = null;
        featureID_ = null;

    }

    /**
    *   Constructs a ProductLicense object for an AS400 system, product, feature,
    *   and release.
    *   @param  system  the AS/400 from which the license will be requested.
    *   @param  productID the product identifier.  For example, "5769JC1".
    *   @param  featureID the product feature.  For example, "5050".
    *   @param  release the product release.  For example, "V4R5M0".
    **/
    public ProductLicense(AS400 system, String productID, String featureID, String release)
    {
        this();

        // get a new AS400 object to make sure we get a separate connection
        sys_ = new AS400(system);
        if(sys_ == null)
        {
            throw new NullPointerException("system");
        }

        if(productID == null)
        {
            throw new NullPointerException("productID");
        }
        else
        {
            productID_ = productID;
        }

        if(featureID == null)
        {
            throw new NullPointerException("featureID");
        }
        else
        {
            featureID_ = featureID;
        }

        if(release == null)
        {
            releaseLevel_ = "      ";         // set correct release variable @A1C
        }
        else
        {
            releaseLevel_ = release;         // set correct release variable @A1C
        }
    }


    /**
     Adds a file listener to receive file events from this IFSFile.
     @param listener The file listener.
    **/
    // This function added to enable beans                                   @A2A
    public void addProductLicenseListener(ProductLicenseListener listener)
    {
      if (listener == null)
        throw new NullPointerException("listener");

      productLicenseListeners_.addElement(listener);
    }

    /**
     Adds a property change listener.
     @param listener The property change listener to add.
     **/
    // This function added to enable beans                                   @A2A
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
      if (listener == null)
        throw new NullPointerException("listener");

      changes_.addPropertyChangeListener(listener);
    }




    /**
     * Disconnect from the host server.
     **/
    void disconnect()
    {
        synchronized(sys_)
        {

            if(server_ != null)
            {

                if(Trace.isTraceOn())
                {
                    Trace.log(Trace.DIAGNOSTIC, "Disconnecting from server");
                }
                try
                {
                    sysImpl_.disconnectServer(server_);
                    server_ = null;
                }
                catch(Exception e)
                {
                    if(Trace.isTraceOn())
                    {
                        Trace.log(Trace.ERROR, "Exception encountered while disconnecting", e);
                    }
                }
            }   // (server_ != null)
        }   // synchronized(sys_)
    }   // disconnect()

    /**
    *   The finalizer.
    **/
    protected void finalize()
    {
        if(Trace.isTraceOn())
        {
            Trace.log(Trace.DIAGNOSTIC, "finalize() - ProductLicense");
        }

        try
        {
            release();
        }
        catch(Exception e)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "release license failed", e);
            }
        }
    }

    // Fire connect events here so source is public object.
    private void fireProductLicenseEvent(int status)
    {
        ProductLicenseEvent event = new ProductLicenseEvent(this, status);

        Vector targets = (Vector)productLicenseListeners_.clone();
        for (int i = 0; i < targets.size(); ++i)
        {
            ProductLicenseListener target = (ProductLicenseListener)targets.elementAt(i);
            switch(status)
            {
            case ProductLicenseEvent.PRODUCT_LICENSE_RELEASED:
                target.licenseReleased(event);
                break;
            case ProductLicenseEvent.PRODUCT_LICENSE_REQUESTED:
                target.licenseRequested(event);
                break;
            }
        }
    }


    /**
    *   Returns the compliance type for this license.  Possible values are
    *   COMPLIANCE_OPERATOR_ACTION, COMPLIANCE_WARNING and COMPLIANCE_KEYED.
    *   A license must have been requested prior to calling this method.
    *   @return The compliance type.
    **/
    public int getComplianceType()
    {
        if(released_)
        {
            throw new ExtendedIllegalStateException(ExtendedIllegalStateException.INFORMATION_NOT_AVAILABLE);
        }

        return complianceType_;
    }

    /**
    *   Returns the condition of the license.  Possible values are CONDITION_OK,
    *   CONDITION_EXCEEDED_OK, CONDITION_EXCEEDED_GRACE_PERIOD,
    *   and CONDITION_GRACE_EXPIRED. A license must have been requested prior to
    *   calling this method.
    *   @return The license condition.
    **/
    public int getCondition()
    {
        if(released_)
            throw new ExtendedIllegalStateException(("Object= "+ sys_.getSystemName()),
                                                    ExtendedIllegalStateException.INFORMATION_NOT_AVAILABLE);

        return condition_;
    }


    /**
    *   Returns the feature identifier for this license.
    *   @return The feature identifier.
    **/
    public String getFeature()
    {
        return featureID_;
    }

    /**
    *   Returns the license term for this license. A license must
    *   have been requested prior to calling this method.
    *   @return The license term.
    **/
    public String getLicenseTerm()
    {
        if(released_)
            throw new ExtendedIllegalStateException(("Object= "+ sys_.getSystemName()),
                                                    ExtendedIllegalStateException.INFORMATION_NOT_AVAILABLE);

        return licenseTerm_;
    }

    /**
    *   Returns the product identifier for this license.
    *   @return The product identifier.
    **/
    public String getProductID()
    {
        return productID_;
    }

    /**
    *   Returns the release level for this license.
    *   @return The release level.
    **/
    public String getReleaseLevel()
    {
        return releaseLevel_;
    }

    /**
    *   Return the name of the AS400 for this license. A license must have been
    *   requested prior to calling this method.
    *   @return The name of the AS400.
    **/
 /*   String getSystemName()
    {
        if(released_)
            throw new ExtendedIllegalStateException(ExtendedIllegalStateException.INFORMATION_NOT_AVAILABLE);

        return sys_.getSystemName();
    }  */

    /**
    *   Returns the AS/400 object for this license.
    *   @return The AS/400.
    **/
    public AS400 getSystem()
    {
        return sys_;
    }

    /**
    *   Returns the usage count for this license.  The count returned is the number
    *   of licenses that are in use on the AS400 for that product ID, feature, and
    *   release when this license was requested. A license must have been requested
    *   prior to calling this method.
    *   @return The usage count when this license was retrieved.
    **/
    public int getUsageCount()
    {
        if(released_)
            throw new ExtendedIllegalStateException(ExtendedIllegalStateException.INFORMATION_NOT_AVAILABLE);

        return usageCount_;
    }

    /**
    *   Returns the usage limit for this license.A license must have been requested prior to
    *   calling this method.
    *   @return The usage limit.
    **/
    public int getUsageLimit()
    {
        if(released_)
            throw new ExtendedIllegalStateException(("Object= "+ sys_.getSystemName()),
                                                    ExtendedIllegalStateException.INFORMATION_NOT_AVAILABLE);

        return usageLimit_;
    }

    /**
    *   Returns the usage type for this license.  Possible values are USAGE_CONCURRENT and
    *   USAGE_REGISTERED. A license must have been requested prior to calling this method.
    *   @return The usage type.
    **/
    public int getUsageType()
    {
        if(released_)
            throw new ExtendedIllegalStateException("UsageType",
                                                    ExtendedIllegalStateException.INFORMATION_NOT_AVAILABLE);

        return usageType_;
    }

    /**
    Provided to initialize transient data if this object is de-serialized.  // @A2A
    **/
    private void initializeTransient()
    {

        changes_ = new PropertyChangeSupport(this);
        productLicenseListeners_ = new Vector();

        sys_ = null;
        sysImpl_ = null;

        condition_ = 0;
        released_ = true;

        usageLimit_ = 0;
        usageCount_ = -1;
        usageType_ = -1;
        complianceType_ = -1;
        licenseTerm_ = null;
        releaseLevel_ = null;

    }

    /**
    *   Release this license.  This method must be called to release the license.  Failure
    *   to do so may result in incorrect license usage count. Calling this method will
    *   disconnect from the AS400 Optimized License Management server.
    *   @exception  IOException  If an error occurs while communicating with the AS/400.
    *   @exception  ConnectionDroppedException If the connection was dropped while trying to communicate with the AS/400.
    *   @exception  InterruptedException  If this thread is interrupted.
    *   @exception  LicenseException  If a license error occurs.
    **/
    public  void release()
    throws IOException, ConnectionDroppedException, InterruptedException, LicenseException
    {

        // Verify that the bean properties are set prior to attempting the release.
        if (sys_ == null)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "System not set yet.");
            }
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (productID_ == null)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Product ID not set yet.");
            }
            throw new ExtendedIllegalStateException("productID", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (featureID_ == null)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Feature ID not set yet.");
            }
            throw new ExtendedIllegalStateException("featureID", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (releaseLevel_ == null)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Release not set yet.");
            }
            throw new ExtendedIllegalStateException("release", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        synchronized(sys_)
        {
            if(released_)  return;


            if(Trace.isTraceOn())
            {
                // release license from server
                Trace.log(Trace.DIAGNOSTIC, "Releasing license from server");
            }

            if(server_ != null)
            {
                LicenseReleaseRequest releaseRequest = new LicenseReleaseRequest(sys_);
                releaseRequest.setProductID(productID_);
                releaseRequest.setFeature(featureID_);
                releaseRequest.setRelease(releaseLevel_);
                DataStream baseReply = server_.sendAndReceive(releaseRequest);
                if(baseReply instanceof LicenseReleaseReply)
                {
                    LicenseReleaseReply releaseReply = (LicenseReleaseReply)baseReply;
                    released_ = true;
                    disconnect();
                    if(releaseReply.getPrimaryRC() != 0)
                    {
                        if(Trace.isTraceOn())
                        {
                            Trace.log(Trace.DIAGNOSTIC, "Release license failed, primary return code = ", releaseReply.getPrimaryRC());
                            Trace.log(Trace.DIAGNOSTIC, "Release license failed, secondary return code = ", releaseReply.getSecondaryRC());
                        }

                        throw new LicenseException(releaseReply.getPrimaryRC(), releaseReply.getSecondaryRC());
                    }

                    // Fire the license released event.
                    fireProductLicenseEvent(ProductLicenseEvent.PRODUCT_LICENSE_RELEASED);

                }   // baseReply instanceof LicenseReleaseReply
            }   // server_ != null
        }   // synchronized(sys_)
    }  // public  void release()

    /**
    *   Request a license.
    *   @exception  IOException  If an error occurs while communicating with the AS/400.
    *   @exception  AS400SecurityException Unable to connect due to some problem with the user ID or password used to authenticate.
    *   @exception  InterruptedException  If this thread is interrupted.
    *   @exception  LicenseException  If a license error occurs.
    *   @exception  ExtendedIllegalStateException  If a license is requested a second time for the same ProductLicense object.
    **/
    public int request()
    throws IOException, AS400SecurityException, InterruptedException, LicenseException
    {
        // Verify that the bean properties are set prior to attempting the release.
        if (sys_ == null)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "System not set yet.");
            }
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (productID_ == null)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Product ID not set yet.");
            }
            throw new ExtendedIllegalStateException("productID", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (featureID_ == null)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Feature ID not set yet.");
            }
            throw new ExtendedIllegalStateException("featureID", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        if (releaseLevel_ == null)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Release not set yet.");
            }
            throw new ExtendedIllegalStateException("release", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        synchronized(sys_)
        {

            if(!released_)
                throw new ExtendedIllegalStateException(("Object= "+ sys_.getSystemName()),
                                                        ExtendedIllegalStateException.LICENSE_CAN_NOT_BE_REQUESTED);
            if(Trace.isTraceOn())
            {
                // request license from server
                Trace.log(Trace.DIAGNOSTIC, "retrieving license from server");
            }

            sys_.connectService(AS400.CENTRAL);

            sysImpl_= (AS400ImplRemote) sys_.getImpl();

            server_ = sysImpl_.getConnection(AS400.CENTRAL, false);

            NLSExchangeAttrRequest request = new NLSExchangeAttrRequest();
            try
            {
                server_.sendExchangeAttrRequest(request);
            }
            catch(IOException e)
            {
                if(Trace.isTraceOn())
                {
                    Trace.log(Trace.ERROR, "IOException After Exchange Attribute Request", e);
                }
                disconnect();
                throw e;
            }
            DataStream baseReply = server_.getExchangeAttrReply();

            if(baseReply instanceof NLSExchangeAttrReply)
            {
                // means request completed CONDITION_OK
                NLSExchangeAttrReply NLSReply = (NLSExchangeAttrReply)baseReply;
                if(NLSReply.primaryRC_ != 0)
                {
                    if(Trace.isTraceOn())
                    {
                        Trace.log(Trace.DIAGNOSTIC, ("Exchange attribute failed, primary return code =" +
                                                     NLSReply.primaryRC_ +
                                                     "secondary return code =" +
                                                     NLSReply.secondaryRC_) );
                    }
                    disconnect();
                    throw new IOException();
                }
            }
            else
            { // unknown data stream
                if(Trace.isTraceOn())
                {
                    Trace.log(Trace.ERROR, "Unknown instance returned from Exchange Attribute Reply");
                }

                throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }



            LicenseGetRequest getLicReq = new LicenseGetRequest(sys_);
            getLicReq.setProductID(productID_);
            getLicReq.setFeature(featureID_);
            getLicReq.setRelease(releaseLevel_);

            try
            {
                baseReply = server_.sendAndReceive(getLicReq);
            }
            catch(IOException e)
            {
                if(Trace.isTraceOn())
                {
                    Trace.log(Trace.ERROR, "IOException occured - Request license", e);
                }
                disconnect();
                throw e;
            }

            if(baseReply instanceof LicenseGetReply)
            {
                LicenseGetReply getReply = (LicenseGetReply)baseReply;

                if(getReply.getPrimaryRC() == 0)
                {
                    usageLimit_ = getReply.getUsageLimit();
                    usageCount_ = getReply.getUsageCount();
                    // bump usage count by one, because server gets usage count before retrieving
                    // license, so the count is off by 1.
                    usageCount_++;
                    usageType_ = getReply.getUsageType();
                    complianceType_ = getReply.getComplianceType();
                    licenseTerm_ = getReply.getLicenseTerm();
                    releaseLevel_ = getReply.getReleaseLevel();

                    condition_ = getReply.getSecondaryRC();
                    released_ = false;

                    // Fire the license released event.
                    fireProductLicenseEvent(ProductLicenseEvent.PRODUCT_LICENSE_REQUESTED);

                }
                else
                {
                    if(Trace.isTraceOn())
                    {
                        Trace.log(Trace.DIAGNOSTIC, ("Request license failed, primary return code =" +
                                                     getReply.getPrimaryRC() +
                                                     "secondary return code =" +
                                                     getReply.getSecondaryRC()) );
                    }
                    throw new LicenseException(getReply.getPrimaryRC(), getReply.getSecondaryRC());
                }
            }
            else
            {
                if(Trace.isTraceOn())
                {
                    Trace.log(Trace.ERROR, "Unknown instance returned from Get License Reply");
                }

                throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }

            return condition_;

        }   //  synchronized(sys_)

    } // public void request()





    /**
      Removes this listener from being notified when a bound property changes.

        @param listener The PropertyChangeListener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
       if (listener == null)
       {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
       }

       changes_.removePropertyChangeListener(listener);
    }

    /**
     Removes a listener from the ProductLicense listeners list.

        @param listener The product license listener.
    **/
    public void removeProductLicenseListener(ProductLicenseListener listener)
    {
       if (listener == null)
       {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
       }

       productLicenseListeners_.removeElement(listener);
    }








    /**
    *   Sets the feature identifier for this license.
    *   @param  featureID the product feature.  For example, "5050".
    **/
    public void setFeature(String featureID)
    {
        if(featureID == null)                             // @A2C
        {
            throw new NullPointerException("featureID");  // @A2C
        }

        // Ensure that the featureID is not altered after the connection is
        // established.
        if (sysImpl_ != null)
        {
            throw new ExtendedIllegalStateException("featureID",
                      ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);   // @A2A
        }

        String oldFeatureID = featureID_;                  // @A2A
        featureID_ = featureID;

        // Fire the property change event having null as the name to
        // indicate that the path, parent, etc. have changed.
        changes_.firePropertyChange("featureID",
                                    oldFeatureID,
                                    featureID_);           // @A2A


    }

    /**
    *   Sets the product identifier for this license.
    *   @param  productID the product identifier.  For example, "5769JC1".
    **/
    public void setProductID(String productID)
    {
        if(productID == null)
        {
            throw new NullPointerException("productID");
        }

        // Ensure that the productID is not altered after the connection is
        // established.
        if (sysImpl_ != null)
        {
            throw new ExtendedIllegalStateException("productID",
                      ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);   // @A2A
        }

        String oldProductID = productID_;                  // @A2A
        productID_ = productID;

        // Fire the property change event having null as the name to
        // indicate that the path, parent, etc. have changed.
        changes_.firePropertyChange("productID",
                                    oldProductID,
                                    productID_);           // @A2A

    }

    /**
    *   Sets the product release for this license.
    *   @param  releaseLevel the product release.  For example, "V4R5M0".
    **/
    public void setReleaseLevel(String releaseLevel)
    {
        if(releaseLevel == null)
        {
            throw new NullPointerException("releaseLevel");
        }

        // Ensure that the releaseLevel is not altered after the connection is
        // established.
        if (sysImpl_ != null)
        {
            throw new ExtendedIllegalStateException("releaseLevel",
                      ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);   // @A2A
        }

        String oldReleaseLevel = releaseLevel_;            // @A2A
        releaseLevel_ = releaseLevel;

        // Fire the property change event having null as the name to
        // indicate that the path, parent, etc. have changed.
        changes_.firePropertyChange("releaseLevel",
                                    oldReleaseLevel,
                                    releaseLevel_);        // @A2A

    }

    /**
    *   Sets the AS/400 object for this license.
    *   @param  system the AS/400 from which the license will be requested.
    **/
    public void setSystem(AS400 system)                    // @A2C
    {
        if(system == null)                                 // @A2C
        {
            throw new NullPointerException("system");      // @A2C
        }

        // Ensure that the system is not altered after the connection is
        // established.
        if (sysImpl_ != null)
        {
            throw new ExtendedIllegalStateException("system",
                      ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);   // @A2A
        }

        AS400 oldSystem = sys_;                        // @A2A
        sys_ = system;                                 // @A2C

        // Fire the property change event having null as the name to
        // indicate that the path, parent, etc. have changed.
        changes_.firePropertyChange("path", oldSystem, sys_);

    }

} // public class ProductLicense

