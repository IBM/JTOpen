package com.ibm.as400.security.auth;

import java.util.*;
import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.*;  // for security-related exceptions
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import com.ibm.as400.access.Trace;
import com.ibm.eim.*;

/**
 Encapsulates an instance of class <tt>com.ibm.eim.Domain</tt>, adding methods for creating and processing Identity Tokens.
 **/
public final class IdentityDomain
{
  private static final boolean DEBUG = false;
  private static final String REGISTRY_NAME = "__PKA_REG__";
  private static final String HOST_PREFIX = "__IBM_DEFINED__:eServerID=";  // host alias prefix

  // Constant used by bytesToHexString(), to generate hex string representations of byte arrays.
  private static final char[] CHAR_FOR_NIBBLE = { '0', '1', '2', '3', '4', '5', '6', '7',
                                                  '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  // Constant used by hexStringToBytes(), to convert hex strings to byte arrays.
  // Note that 0x11 is "undefined".
  private static final byte[] NIBBLE_FOR_CHAR =
  {
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11
  };

  private KeyPairGenerator keyPairGenerator_ = null;


  // Design note: Ideally we'd like to just extend com.ibm.eim.jndi.DomainJNDI.
  // However, we would then have the question of how a user instantiates this class.
  // DomainJNDI and Domain are instantiated by DomainManager.createDomain() and
  // DomainManager.getDomain(), which we don't control.
  private Domain domain_;



  /**
   Constructs an IdentityDomain object.
   @param domain The Domain object to be encapsulated.
   **/
  public IdentityDomain(Domain domain)
  {
    if (domain == null) throw new NullPointerException("domain");
    domain_ = domain;
  }

  /**
   Returns ApplicationInfo object containing the application EIM identifier and, if possible, the application instance identifier, for the specified host name and port.
   <p>A search will be made to find the EIM identifier that has an alias defined with the following format:<br>
   <tt>__IBM_DEFINED__:eServerID=<em>hostname</em></tt>
   <br>
   A search will be made on the additional information defined for the EIM
   identifier that is returned, where the additional information has the
   following format:<br>
   <tt>PORT=<em>nn</em>:<em>applicationInstanceID</em></tt>
   <br>
   where <em>nn</em> is the specified port number, and <em>applicationInstanceID</em>
   is the value returned in this parameter.  Note that the search for <tt>PORT=</tt>
   is case sensitive, so the value must be entered in upper case when the additional
   information entry is added to the EIM identifier.  If an application instance
   identifier is not found, the ApplicationInfo's applicationInstanceID field is set to <tt>null</tt>.
   (Note that applications aren't required to 'register' their port under the EIM Identifier.)

   @param tcpipHostName The TCP/IP host name that the application is using.
   If <tt>null</tt> is specified for this parameter, localhost is assumed.
   @param portNumber The port number the application is using.

   @return An ApplicationInfo object containing the appEimID and appInstanceID (possibly <tt>null</tt>) for the application.
   **/
  public ApplicationInfo getApplicationInfo(String tcpipHostName, int portNumber)
    throws EimException
  {
    if (DEBUG) System.out.println("getApplicationInfo("+tcpipHostName+", "+portNumber+")");

    if (tcpipHostName == null) {
      try {
        tcpipHostName = java.net.InetAddress.getLocalHost().getHostName(); // Default to local host.
      }
      catch (java.net.UnknownHostException e) { throw new EimException(e); }
    }

    // Get the EIDs whose alias matches the specified host.  Ideally there will be exactly one.
    if (DEBUG) System.out.println("Domain.getEidsByAlias("+HOST_PREFIX + tcpipHostName+")");
    Set eidList = domain_.getEidsByAlias(HOST_PREFIX + tcpipHostName);
    Eid eid = null;

    switch (eidList.size())
    {
      case 0:   // no match
        throw new EimException("EIM ID not found for host " + tcpipHostName, Constants.ITKNERR_EIMID_NOT_FOUND);
      case 1:   // exactly 1 match, this is what we want
        eid = (Eid)eidList.toArray()[0];
        if (DEBUG) System.out.println("Found 1 EID: name=|"+eid.getName()+"|");
        break;
      default:  // multiple matches; ambiguity
        if (DEBUG) {
          System.out.println("Multiple EIM IDs found for host " + tcpipHostName);
          Eid[] eids = new Eid[eidList.size()];
          eidList.toArray(eids);
          for (int i=0; i<eids.length; i++) {
            Eid eID = eids[i];
            System.out.println("Eid: name="+eID.getName());
            String uuid = null;
            try { uuid = eID.getUuid(); } catch (EimException ee) { System.out.println(ee.getMessage()); }
            System.out.println("UUID="+uuid);
            System.out.print("aliases=");
            Set aliases = eID.getAliases();
            Iterator iter = aliases.iterator();
            while (iter.hasNext()) {
              System.out.print((String)iter.next()+",");
            }
            System.out.println();
          }
        }
        throw new EimException("Multiple EIM IDs found for host " + tcpipHostName, Constants.ITKNERR_AMBIGUOUS);
    }

    //------------------------------------------------------------------
    //  Search for the specified port number in the additional info.
    //  The current layout for this info is defined as:
    //  PORT=xx:appInstanceID
    //------------------------------------------------------------------

    String appInstanceID = null;
    Set addlInfoList = eid.getAdditionalInfo();
    if (addlInfoList.size() == 0) {
      if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "EIM ID for host " + tcpipHostName + " has no additional info.");
    }
    else
    { // Look for the specified port number in the additional info, and grab the app instance ID.

      String portString = "PORT=" + portNumber + ":";
      String addlInfo = null;
      Iterator aiIterator = addlInfoList.iterator();
      int matches = 0;
      while (aiIterator.hasNext()) {
        addlInfo = (String)aiIterator.next();
        if (addlInfo.startsWith(portString)) {  // found one
          // Parse out the app instance ID from the remainder of the string.
          appInstanceID = addlInfo.substring(portString.length());
          matches++;  // count how may matches we found
        }
      }

      // We require exactly 1 match.
      switch (matches) {
        case 0:
          if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "EIM ID for host " + tcpipHostName + " has no entries for port " + portNumber);
          // Tolerate this, just return the EID, and null for app instance ID.
        case 1:  // This is what we want.
          break;
        default:
          throw new EimException("EIM ID for host " + tcpipHostName + " has multiple entries for port " + portNumber, Constants.ITKNERR_AMBIGUOUS);
      }
    }

    return new ApplicationInfo(eid, appInstanceID);
  }


/**
 Publishes an application's public identity key to EIM.  The information is published
 using a target association in the Enterprise Identity Mapping (EIM) domain.
 Any existing published keys for this instance of the application will be
 removed from EIM.

 @param appEimID The EIM identifier that represents this application.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.
 @param appInstanceID The application instance identifier is used to uniquely identify this
 instance of the application in the network.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.
 @param period This is the period of time that each public key pair will be
 considered valid for signing in seconds.  Valid range is 1 - 31,536,000
 seconds (365 days).
 @param keySize The modulus length in bits for the key size.  Valid range is 512 - 2048.

 @return An IdentityKeyPair object that contains information about the published key.
 The IdentityKeyPair should be used on subsequent calls to identity token methods.
**/
 public IdentityKeyPair publishPublicKey(Eid appEimID, String appInstanceID, long period, int keySize)
    throws EimException, IOException, GeneralSecurityException
 {
   if (appEimID == null) throw new NullPointerException("appEimID");
   if (appInstanceID == null) throw new NullPointerException("appInstanceID");
   if (period <= 0 || period > 31536000) throw new IllegalArgumentException("period");
   if ((keySize < 512) || (keySize > 2048)) throw new IllegalArgumentException("keySize");

   // Start with a clean slate, remove any existing target associations for this application.
   removeTargetAssociations(appEimID, appInstanceID+"=cur");
   removeTargetAssociations(appEimID, appInstanceID+"=prev");

   // Generate a key pair and publish it.
   IdentityKeyPair keyPair = new IdentityKeyPair(generateKeyPair(keySize), appEimID, appInstanceID, (long)period*1000, keySize);
   publish(keyPair);
   return keyPair;
 }


/**
 Unpublishes the application's public identity key from EIM.

 @param appEimID The EIM identifier that represents this application.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.
 @param appInstanceID The application instance identifier is used to uniquely identify this
 instance of the application in the network.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.
**/
  public void unpublishPublicKey(Eid appEimID, String appInstanceID)
    throws EimException
  {
   if (appEimID == null) throw new NullPointerException("appEimID");
   if (appInstanceID == null) throw new NullPointerException("appInstanceID");

    removeTargetAssociations(appEimID, appInstanceID+"=cur");
    removeTargetAssociations(appEimID, appInstanceID+"=prev");
  }


  /**
   Builds a new identity token.

   @param keyPair Contains published key information.  It was returned on a previous call to {@link #publishPublicKey(Eid,String,long,int) publishPublicKey}.
   @param authenticatedUser The name of the user that has been authenticated by the caller.  This user name will be the source registry user name for the EIM getTargetFromSource()
   operation for the endpoint application.
   @param userRegistry The name of the EIM user registry for the authenticated user.  This registry name will be the source registry name for the EIM getTargetFromSource() operation for the endpoint application.
   @param rcvEimID The EIM identifier that represents the application
   that is to receive this token.  This information will be added to
   the token to be checked by the receiver to make sure this token was
   intended for the receiver.
   @param rcvInstanceID The application instance identifier that uniquely identifies the
   instance of the application in the network that is to receive this
   identity token.  This information will be added to the token to
   be checked by the receiver to make sure this token was intended for
   the receiver.

   @return A new IdentityToken object.
   **/
  public IdentityToken generate(IdentityKeyPair keyPair, String authenticatedUser, String userRegistry, String rcvEimID, String rcvInstanceID)
    throws EimException, IOException, GeneralSecurityException
  {
    if (authenticatedUser == null) throw new NullPointerException("authenticatedUser");
    if (userRegistry == null) throw new NullPointerException("userRegistry");
    if (rcvEimID == null) throw new NullPointerException("rcvEimID");
    if (rcvInstanceID == null) throw new NullPointerException("rcvInstanceID");
    if (keyPair == null) throw new NullPointerException("keyPair");

    // Build the User Token.
    UserToken userToken = new UserToken(authenticatedUser, userRegistry);

    if (keyPair.isExpired()) renewKeyPair(keyPair);

    // Build the Token Manifest.
    TokenManifest tokenManifest =
      new TokenManifest(1,                               // TM counter (first in chain)
                        keyPair.getEid().getName(),      // sender EIM ID name
                        keyPair.getAppInstanceID(),      // sender App Instance ID
                        keyPair.getFormattedTimestamp(), // sender timestamp
                        rcvEimID,                        // receiver EIM ID name
                        rcvInstanceID);                  // receiver App Instance ID

    // Build the Token Signature Header.
    SignatureHeader sigHeader = SignatureHeader.getInstance(tokenManifest, null, userToken, keyPair.getPrivate());

    if (DEBUG) {
      System.out.println("IdentityDomain.generate():");
      System.out.println("private key:");
      printByteArray(keyPair.getPrivate().getEncoded());
      System.out.println("public key:");
      printByteArray(keyPair.getPublic().getEncoded());
      System.out.println("signature:");
      printByteArray(sigHeader.getSignature());
    }

    // Assemble the various parts into a new Identity Token.
    IdentityToken token = new IdentityToken(sigHeader, tokenManifest, null, userToken);

    return token;
  }


  /**
   Adds (prepends) another token manifest to the identity token.  The new token
   manifest specifies this application's EIM identifier and application instance
   identifier as the sender, and the receiver's EIM identifier and
   application instance identifier as the receiver.

   @param keyPair Contains published key information.  It was returned on a previous call to {@link #publishPublicKey(Eid,String,long,int) publishPublicKey}.
   @param identityToken The identity token to be delegated.
   @param rcvEimID The EIM identifier that represents the application
   that is to receive this token.  This information will be added to
   the token to be checked by the receiver to make sure this token was
   intended for the receiver.
   @param rcvInstanceID The application instance identifier that uniquely identifies the
   instance of the application in the network that is to receive this
   identity token.  This information will be added to the token to
   be checked by the receiver to make sure this token was intended for
   the receiver.
   **/
  public void delegate(IdentityKeyPair keyPair, IdentityToken identityToken, String rcvEimID, String rcvInstanceID)
    throws EimException, IOException, GeneralSecurityException
  {
    if (keyPair == null) throw new NullPointerException("keyPair");
    if (identityToken == null) throw new NullPointerException("identityToken");
    if (rcvEimID == null) throw new NullPointerException("rcvEimID");
    if (rcvInstanceID == null) throw new NullPointerException("rcvInstanceID");

    verify(identityToken, keyPair.getEid(), keyPair.getAppInstanceID());

    if (keyPair.isExpired()) renewKeyPair(keyPair);

    // Build a new Token Manifest.
    int priorCounter = identityToken.getManifest().getCounter();
    TokenManifest newTokenManifest =
      new TokenManifest(priorCounter+1,                  // TM counter
                        keyPair.getEid().getName(),      // sender EIM ID name
                        keyPair.getAppInstanceID(),      // sender App Inst ID
                        keyPair.getFormattedTimestamp(), // sender timestamp
                        rcvEimID,                        // receiver EIM ID name
                        rcvInstanceID);                  // receiver App Inst ID

    // Build a new Token Signature Header.
    byte[] priorManifests = identityToken.getPriorManifests();
    SignatureHeader newSigHeader =
      SignatureHeader.getInstance(newTokenManifest,
                                  identityToken,
                                  keyPair.getPrivate());

    // Add the new Sig Header and Token Manifest to the passed-in Identity Token.
    identityToken.addNewSignatureAndManifest(newSigHeader, newTokenManifest);
  }


/**
 Verifies the identity token, and returns the target user name associated with this token, using the target registry.

 @param identityToken The identity token for which to return the user name.  The
 EIM source registry and source registry user name used to find the
 target user are retrieved from the identity token.
 @param targetRegistry The name of the EIM registry to check for an association.
 @param appEimID The EIM identifier that represents the current application.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.
 @param appInstanceID The application instance identifier that uniquely identifies this
 instance of the application in the network.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.

 @return The target user associated with this identity token.  If the source user name could not be mapped to a target user, <tt>null</tt> is returned.
**/
  public RegistryUser getUser(IdentityToken identityToken, String targetRegistry, Eid appEimID, String appInstanceID)
    throws EimException, IOException, GeneralSecurityException
  {
    if (identityToken == null) throw new NullPointerException("identityToken");
    if (targetRegistry == null) throw new NullPointerException("targetRegistry");
    if (appEimID == null) throw new NullPointerException("appEimID");
    if (appInstanceID == null) throw new NullPointerException("appInstanceID");

    verify(identityToken, appEimID, appInstanceID);

    UserToken userToken = identityToken.getUserToken();

    // Get target from source.
    Set targets = domain_.findTargetFromSource(userToken.getUserName(), userToken.getRegistryName(), targetRegistry);  // Returns a set of RegistryUser objects.

    // Ideally we got exactly 1 match.
    switch (targets.size())
    {
      case 0:  // No matches.
        if (Trace.isTraceOn()) Trace.log(Trace.WARNING, "No target names were found for source name " + userToken.getUserName());
        return null;

      case 1:  // Exactly 1 match.
        return (RegistryUser)targets.toArray()[0];

      default: // Multiple matches.  This is tolerable only if they are duplicates.

        //------------------------------------------------------------------
        //  We got more than 1 target user back.  Conceivably we could get
        //  duplicates back if the source user mapped to 2 separate
        //  identifiers that turned around and mapped to the same target
        //  user.  So check the additional target users.  If they are
        //  duplicates of each other then ignore and return the  user.
        //  If we truly have multiple users then return ambiguous error.
        //------------------------------------------------------------------

        // The RegistryUser class has no equals() method, so just compare targetUserNames.
        RegistryUser[] users = new RegistryUser[targets.size()];
        targets.toArray(users);
        String userName = users[0].getTargetUserName(); // grab first one in list
        for (int i=0; i<users.length; i++) { // compare it to the rest in the list
          if (!userName.equals(users[i].getTargetUserName())) {
            throw new EimException("The source username maps to multiple targets", Constants.ITKNERR_AMBIGUOUS);
          }
        }
        return users[0];  // If we got this far, the multiple matches were duplicates.
    }
  }



  //--------------------------------
  //
  // Private utility methods.
  //
  //--------------------------------



 private java.security.KeyPair generateKeyPair(int keySize)
    throws NoSuchAlgorithmException, NoSuchProviderException
 {
   if (keyPairGenerator_ == null) {
     keyPairGenerator_ = KeyPairGenerator.getInstance("RSA");
   }
   synchronized(keyPairGenerator_) {
     keyPairGenerator_.initialize(keySize);
     return keyPairGenerator_.generateKeyPair();
   }
 }


 private static void publish(IdentityKeyPair keyPair)
    throws EimException
 {
   //------------------------------------------------------------------
   //  Add new association.
   //  - Add target association using hard coded registry and printable
   //    unicode public key.
   //  - Change registry user, add additional info.
   //    - Add cur appInstanceID string.
   //      This is used when handling expired key pairs.
   //    - Add timestamp appInstanceID string.
   //      This is used when searching for the public key.
   //------------------------------------------------------------------

   // Add an Association that maps the EID to the (printable) public key.
   // Note: This sets up a new "registry user" entry, with name == <public key> .
   String publicKeyUnicodeString = bytesToHexString(keyPair.getPublic().getEncoded());
   Eid appEimID = keyPair.getEid();
   appEimID.addAssociation(Association.EIM_TARGET,
                           REGISTRY_NAME,
                           publicKeyUnicodeString);

   // Get the Registry User object for the Association that we just added.

   Set targets = appEimID.findTarget(REGISTRY_NAME);
   if (targets.isEmpty()) {  // This should never happen unless we have a bug.
     Trace.log(Trace.ERROR, "No targets in registry " + REGISTRY_NAME + " for EID " + appEimID.getName());
     throw new EimException("No RegistryUser is associated with the specified EID", Constants.INTERNAL_ERROR);
   }
   Iterator iterator = targets.iterator();
   boolean found = false;
   RegistryUser regUser = null;
   // If the addAssociation worked, there should now be exactly 1 RegistryUser with a targetUserName matching the public key.
   while (iterator.hasNext() && !found) {
     regUser = (RegistryUser)iterator.next();
     if (regUser.getTargetUserName().equals(publicKeyUnicodeString)) {
       found = true;
     }
   }
   if (!found) { // This should never happen unless we have a bug.
     throw new EimException("No RegistryUser entry has target name matching public key", Constants.INTERNAL_ERROR);
   }

   // Add the additional info to help identify this RegistryUser entry.
   String appInstanceID = keyPair.getAppInstanceID();
   regUser.addAdditionalInfo(appInstanceID + "=cur");
   keyPair.setPublishTime(System.currentTimeMillis());
   regUser.addAdditionalInfo(appInstanceID + "="+keyPair.getFormattedTimestamp());
 }

  /**
   Generates a new (encapsulated) keypair and publishes the new public key, with a new timestamp.
   Marks the new public-key entry as "cur" in EIM registry,
   and marks the previous (expired) public key as "prev".
   Deletes any prior "prev" entry.
   Updates the information in the passed-in IdentityKeyPair object.
   **/
  private void renewKeyPair(IdentityKeyPair keyPair)
    throws EimException, IOException, NoSuchAlgorithmException, NoSuchProviderException
  {
    if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Renewing keypair.");
    //------------------------------------------------------------------
    //  Update existing association information.
    //------------------------------------------------------------------
    //  Remove any "previous" target association if it exists.
    //  - get target from identifer with previous appInstanceID
    //  - if found, remove
    //------------------------------------------------------------------

    String appInstanceID = keyPair.getAppInstanceID();
    Eid appEimID = keyPair.getEid();
    removeTargetAssociations(appEimID, appInstanceID+"=prev");

    //------------------------------------------------------------------
    //  Convert "current" target association (if one exists) to "previous"
    //  - Check to see if a "current" target association exists.
    //    - get target from identifier with current appInstanceID
    //    - if found,
    //      then add previous alias string
    //           and remove current string.
    //      The association stays intact.
    //------------------------------------------------------------------
    Set targets = appEimID.findTarget(REGISTRY_NAME, appInstanceID+"=cur");
    switch (targets.size())
    {
      case 0:
        break;  // This is OK, since this app might not have published before.

      case 1:
        break;  // This will usually be the case.

      default:  // More than 1 'cur' entry was found.  This should never happen unless we have a bug.  Report the error and continue.
        Trace.log(Trace.WARNING, "Multiple targets returned with additional info == \"" + appInstanceID+"=cur\"");
    }

    if (targets.size() != 0) {
      RegistryUser[] regUsers = new RegistryUser[targets.size()];
      targets.toArray(regUsers);
      for (int i=0; i<regUsers.length; i++) {
        regUsers[i].removeAdditionalInfo(appInstanceID+"=cur");
        regUsers[i].addAdditionalInfo(appInstanceID+"=prev");
      }
    }

    // Generate a new "inner" key pair, and publish the new public key.
    keyPair.setInnerKeyPair(generateKeyPair(keyPair.getKeySize()));
    publish(keyPair);
  }


 // Throws an EimException if verification fails.
  private void verify(IdentityToken token, Eid currentAppEimID, String currentAppInstanceID)
    throws EimException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, InvalidKeySpecException, SignatureException
  {
    // Check that we are the intended receiver.
    // Compare receiver info in the token to the caller's EIM ID and
    // application instance id.

    if (!currentAppEimID.getName().equals(token.getManifest().getReceiverEidName()))
    {
      throw new EimException("Receiver EIM ID in token does not match current application", Constants.ITKNERR_TKN_EIMID_MISMATCH);
    }

    if (!currentAppInstanceID.equals(token.getManifest().getReceiverAppInstanceID())) {
      throw new EimException("Receiver appInstanceID in token does not match current application", Constants.ITKNERR_TKN_APP_INST_MISMATCH);
    }

    // Get sender's public key from EIM.

    // First derive sender's EID from the manifest information in the token.
    String senderEidName = token.getManifest().getSenderEidName();
    String senderAppInstanceID = token.getManifest().getSenderAppInstanceID();
    String senderTimestamp = token.getManifest().getSenderTimestamp();
    Set eids = domain_.getEidsByName(senderEidName);
    switch (eids.size()) {
      case 0:
        throw new EimException("Sender's EID not found in registry", Constants.ITKNERR_EIMID_NOT_FOUND);
      case 1:  // This is what we expected.
        break;
      default:
        throw new EimException("Found multiple EIDs for the specified sender name (" + senderEidName + ")", Constants.ITKNERR_AMBIGUOUS);
    }

    Eid senderEid = (Eid)eids.toArray()[0]; // We know the array has length==1.
    Set targets = senderEid.findTarget(REGISTRY_NAME, senderAppInstanceID + "=" + senderTimestamp);

    switch (targets.size()) {
      case 0:
        throw new EimException("Sender's appInstanceID/timestamp not found in registry", Constants.ITKNERR_PUBLIC_KEY_NOT_FOUND);
      case 1:
        break;
      default:
        throw new EimException("Found multiple registry entries for the specified appInstanceID/timestamp", Constants.ITKNERR_AMBIGUOUS);
    }
    RegistryUser regUser = (RegistryUser)targets.toArray()[0];
    String publicKeyAsHexString = regUser.getTargetUserName(); // sender's published public key
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(hexStringToBytes(publicKeyAsHexString));
    KeyFactory factory = KeyFactory.getInstance("RSA");
    PublicKey sendersPublicKey = factory.generatePublic(keySpec);

    // Verify the signature.

    Signature signer = Signature.getInstance("SHA1withRSA");
    signer.initVerify(sendersPublicKey);
    // Update the signer with the data that the signature is for.
    // This is everything in the token _after_ the Signature Header.
    // TBD There's probably a more efficient way to extract out the signed bytes.
    byte[] signature = token.getSignatureHeader().getSignature();
    byte[] tokenAsBytes = token.toBytes();  // TBD: This does extra work, it needlessly converts the signature header to bytes.
    int sigHeaderLength = token.getSignatureHeader().getLength();
    byte[] bytesAfterSignature = new byte[tokenAsBytes.length - sigHeaderLength];
    if (DEBUG &&
        bytesAfterSignature.length != token.getSignatureHeader().getSignedLength()) {
      System.out.println("DEBUG bytesAfterSignature.length==" + bytesAfterSignature.length + "; signedLength == " + token.getSignatureHeader().getSignedLength());
      System.out.println("DEBUG token.getLength() == " + token.getLength());
      System.out.println("DEBUG token.toBytes().length == " + token.toBytes().length);
    }
    System.arraycopy(tokenAsBytes, sigHeaderLength, bytesAfterSignature, 0, bytesAfterSignature.length);
    signer.update(bytesAfterSignature);

    if (!signer.verify(signature)) {
      throw new EimException("Signature is not valid", Constants.ITKNERR_SIGNATURE_INVALID);
    }
  }

  // Utility method for debugging.
  // Logs data from a byte array starting at offset for the length specified.
  // Output sixteen bytes per line, two hexadecimal digits per byte, one space between bytes.
  static void printByteArray(byte[] data)
  {
    if (data == null) System.out.println("null");
    else
    {
      int offset = 0;
      for (int i = 0; i < data.length; i++, offset++)
      {
        int leftDigitValue = (data[offset] >>> 4) & 0x0F;
        int rightDigitValue = data[offset] & 0x0F;
        // 0x30 = '0', 0x41 = 'A'
        char leftDigit = leftDigitValue < 0x0A ? (char)(0x30 + leftDigitValue) : (char)(leftDigitValue - 0x0A + 0x41);
        char rightDigit = rightDigitValue < 0x0A ? (char)(0x30 + rightDigitValue) : (char)(rightDigitValue - 0x0A + 0x41);
        System.out.print(leftDigit);
        System.out.print(rightDigit);
        System.out.print(" ");

        if ((i & 0x0F ) == 0x0F)
        {
          System.out.println();
        }
      }
      if (((data.length - 1) & 0x0F) != 0x0F)
      {
        // Finish the line of data.
        System.out.println();
      }
    }
    System.out.flush();
  }

  private static void removeTargetAssociations(Eid appEimID, String additionalInfo)
    throws EimException
  {
    // The associations of interest are those with the specified "additional info" string.
    Set targets = appEimID.findTarget(REGISTRY_NAME, additionalInfo);
    if (targets.isEmpty()) {
      if (Trace.isTraceOn()) {
        Trace.log(Trace.DIAGNOSTIC, "IdentityDomain.removeTargetAssociations("+appEimID.getName()+","+additionalInfo+ ") found no targets.");
      }
    }
    else {
      Iterator iterator = targets.iterator();
      while (iterator.hasNext()) {
        RegistryUser regUser = (RegistryUser)iterator.next();
        appEimID.removeAssociation(Association.EIM_TARGET,
                                   REGISTRY_NAME,
                                   regUser.getTargetUserName());
      }
    }
  }


  // Converts a byte array into its hex string representation.
  private static String bytesToHexString(final byte[] b)
  {
    char[] c = new char[b.length*2];
    for (int i=0; i<b.length; ++i)
    {
      final int j = i*2;
      final byte hi = (byte)((b[i]>>>4) & 0x0F);
      final byte lo = (byte)((b[i] & 0x0F));
      c[j] = CHAR_FOR_NIBBLE[hi];
      c[j+1] = CHAR_FOR_NIBBLE[lo];
    }
    return new String(c);
  }

  // Converts a hex string to an array of bytes.
  private static byte[] hexStringToBytes(String s)
  {
    if (s.length() == 0) return new byte[0];
    char[] hexChars = s.toCharArray();
    byte[] bytes = new byte[hexChars.length/2];  // 1 char per nibble, 2 nibbles per byte
    for (int i=0; i<bytes.length; ++i)
    {
      final int j = i*2;
      final int c1 = 0x00FFFF & hexChars[j];
      final int c2 = 0x00FFFF & hexChars[j+1];
      if(c1 > 255 || c2 > 255) { // out of range
        // This should never happen, since we composed the hex string ourselves.
        throw new NumberFormatException();
      }
      else
      {
        final byte b1 = NIBBLE_FOR_CHAR[c1];
        final byte b2 = NIBBLE_FOR_CHAR[c2];
        if (b1 == 0x11 || b2 == 0x11) { // out of range
          // This should never happen, since we composed the hex string ourselves.
          throw new NumberFormatException();
        }
        else
        {
          final byte hi = (byte)(b1<<4);
          bytes[i] = (byte)(hi + b2);
        }
      }
    }
    return bytes;
  }

}
