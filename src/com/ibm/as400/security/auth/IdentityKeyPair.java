package com.ibm.as400.security.auth;

import com.ibm.eim.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 Contains information used by {@link IdentityDomain IdentityDomain} when creating and processing instances of {@link IdentityToken IdentityToken}.
 <br>This class is instantiated by {@link IdentityDomain#publishPublicKey(Eid,String,long,int) publishPublicKey()}.
 **/
public final class IdentityKeyPair {

  // The encapsulated KeyPair object.
  private KeyPair innerKeyPair_;

  // Time when the keypair was published (milliseconds since midnight, January 1, 1970 UTC)
  private long publishTime_;

  // The keypair's timeout value (in milliseconds).
  private long lifeSpan_;

  // Application EIM ID.
  private Eid eid_;  // Implementation note: Eid is not serializable.

  // Application instance ID.
  private String appInstanceID_;

  // Key size, for use when generating a new inner keypair.
  private int keySize_;


  IdentityKeyPair(java.security.KeyPair keyPair, Eid appEimID, String appInstanceID, long lifeSpan, int keySize)
  {
    // Assume caller has validated args.
    innerKeyPair_ = keyPair;
    eid_ = appEimID;
    appInstanceID_ = appInstanceID;
    lifeSpan_ = lifeSpan;
    keySize_ = keySize;
  }


  /**
   Returns a reference to the public key component of this key pair.
   @return A reference to the public key.
   **/
  PublicKey getPublic() {
    return innerKeyPair_.getPublic();
  }


  /**
   Returns a reference to the private key component of this key pair.
   @return a reference to the private key.
   **/
  PrivateKey getPrivate() {
    return innerKeyPair_.getPrivate();
  }


  /**
   Resets the encapsulated java.security.KeyPair object.
   This is necessary when an old public key expires and we (re)publish a new public key.
   **/
  void setInnerKeyPair(java.security.KeyPair keyPair) {
    // Assume caller has validated arg.
    // Note: We call the encapsulated KeyPair the "inner" KeyPair to differentiate it from the enclosing "outer" IdentityKeyPair.
    innerKeyPair_ = keyPair;
  }

  String getAppInstanceID() {
    return appInstanceID_;
  }


  Eid getEid() {
    return eid_;
  }

  int getKeySize() {
    return keySize_;
  }

  // Milliseconds elapsed since midnight, January 1, 1970 UTC.
  void setPublishTime(long publishTime) {
    // Assume caller has validated arg.
    publishTime_ = publishTime;
  }


  /**
   Returns the timestamp, in format "YYYYMMDDHHMMSS".
   **/
  String getFormattedTimestamp()
  {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    return formatter.format(new Date(publishTime_));
  }

  /**
   Determines if this keypair is expired.
   A keypair is expired if the difference between the current time and the publish time exceeds the keypair's lifespan setting.
   **/
  final boolean isExpired()
  {
    return ((System.currentTimeMillis() - publishTime_) > lifeSpan_);
  }

}
