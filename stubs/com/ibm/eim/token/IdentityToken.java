// This is a "stub" for the IdentityToken class.
package com.ibm.eim.token;

import java.io.Serializable;
import javax.security.auth.Destroyable;
import javax.security.auth.DestroyFailedException;

/**
 Represents an identity token.
 Identity tokens may be submitted to {@link com.ibm.as400.access.AS400#AS400(String,IdentityToken) AS400()} or {@link com.ibm.as400.access.AS400#setIdentityToken(IdentityToken) setIdentityToken()}
 to authenticate to an iSeries or AS/400 server.
 <p><i>Note: Authentication via IdentityToken is not currently supported.  Support will become available in a future PTF for OS/400 V5R2 and V5R1.</i>
 **/
public class IdentityToken implements Serializable, Destroyable {

  private IdentityToken() {}


  /**
   Converts this identity token into a new byte array, which can then be sent in a datastream.
   @return The identity token in the form of a byte array.
   **/
  public byte[] toBytes()
  {
    return null;
  }


  // Method required by Destroyable.
  public void destroy() throws DestroyFailedException
  {
  }

  // Method required by Destroyable.
  public boolean isDestroyed()
  {
    return false;
  }

}
