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
 Encapsulates an instance of class <tt>com.ibm.eim.Domain</tt>, adding methods for creating and processing Authentication Tokens.
 **/
public final class AuthenticationDomain
{
  private static final boolean DEBUG = true; /// set to false before shipping
  private static final String REGISTRY_NAME = "PKA_REG";
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
   Constructs an AuthenticationDomain object.
   @param domain The Domain object to be encapsulated.
   **/
  public AuthenticationDomain(Domain domain)
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
    if (tcpipHostName == null) {
      tcpipHostName = domain_.getHost(); // Default to local host.
    }

    // Get the EIDs whose alias matches the specified host.  Ideally there will be exactly one.
    Set eidList = domain_.getEidsByAlias(HOST_PREFIX + tcpipHostName);
    Eid eid = null;

    switch (eidList.size())
    {
      case 0:   // no match
        throw new EimException("EIM ID not found for host " + tcpipHostName, Constants.ATKNERR_EIMID_NOT_FOUND);
      case 1:   // exactly 1 match, this is what we want
        eid = (Eid)eidList.toArray()[0];
        break;
      default:  // multiple matches; ambiguity
        throw new EimException("Multiple EIM IDs found for host " + tcpipHostName, Constants.ATKNERR_AMBIGUOUS);
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
        case 1:  // This is what we want.
          break;
        default:
          throw new EimException("EIM ID for host " + tcpipHostName + " has multiple entries for port " + portNumber, Constants.ATKNERR_AMBIGUOUS);
      }
    }

    return new ApplicationInfo(eid, appInstanceID);
  }


/**
 Publishes an application's public authentication key to EIM.  The information is published
 using a target association in the Enterprise Identity Mapping (EIM) domain.
 Any existing published keys for this instance of the application will be
 removed from EIM.

 @param appEimID The name of the EIM identifier that represents this application.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.
 @param appInstanceID The application instance identifier is used to uniquely identify this
 instance of the application in the network.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.
 @param period This is the period of time that each public key pair will be
 considered valid for signing in seconds.  Valid range is 1 - 31,536,000
 seconds (365 days).
 @param keySize The modulus length in bits for the key size.  Valid range is 512 - 2048.

 @return An AuthenticationKeyPair object that contains information about the published key.
 The AuthenticationKeyPair should be used on subsequent calls to authentication token methods.
**/
 public AuthenticationKeyPair publishPublicKey(Eid appEimID, String appInstanceID, long period, int keySize)
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
   AuthenticationKeyPair keyPair = new AuthenticationKeyPair(generateKeyPair(keySize), appEimID, appInstanceID, (long)period*1000, keySize);
   publish(keyPair);
   return keyPair;
 }


/**
 Unpublishes the application's public authentication key from EIM.
**/
  public void unpublishPublicKey(AuthenticationKeyPair keyPair)
    throws EimException
  {
    if (keyPair == null) throw new NullPointerException("keyPair");

    removeTargetAssociations(keyPair.getEid(),
                       keyPair.getAppInstanceID()+"=cur");
    removeTargetAssociations(keyPair.getEid(),
                       keyPair.getAppInstanceID()+"=prev");
  }


  /**
   Builds a new authentication token.

   @param keyPair Contains published key information.  It was returned on a previous call to {@link #publishPublicKey(Eid,String,long,int) publishPublicKey}.
   @param authenticatedUser The name of the user that has been authenticated by the caller.  This user name will be the source registry user name for the EIM getTargetFromSource()
   operation for the endpoint application.
   @param authenticationRegistry The name of the EIM user registry for the authenticated user.  This registry name will be the source registry name for the EIM getTargetFromSource() operation for the endpoint application.
   @param rcvEimID The name of the EIM identifier that represents the application
   that is to receive this token.  This information will be added to
   the token to be checked by the receiver to make sure this token was
   intended for the receiver.
   @param rcvInstanceID The application instance identifier that uniquely identifies the
   instance of the application in the network that is to receive this
   authentication token.  This information will be added to the token to
   be checked by the receiver to make sure this token was intended for
   the receiver.

   @return A new AuthenticationToken object.
   **/
  public AuthenticationToken generate(AuthenticationKeyPair keyPair, String authenticatedUser, String authenticationRegistry, String rcvEimID, String rcvInstanceID)
    throws EimException, IOException, GeneralSecurityException
  {
    if (authenticatedUser == null) throw new NullPointerException("authenticatedUser");
    if (authenticationRegistry == null) throw new NullPointerException("authenticationRegistry");
    if (rcvEimID == null) throw new NullPointerException("rcvEimID");
    if (rcvInstanceID == null) throw new NullPointerException("rcvInstanceID");
    if (keyPair == null) throw new NullPointerException("keyPair");

    // Build the User Token.
    UserToken userToken = new UserToken(authenticatedUser, authenticationRegistry);

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

    // Assemble the various parts into a new Authentication Token.
    AuthenticationToken authToken = new AuthenticationToken(sigHeader, tokenManifest, null, userToken);

    return authToken;
  }


  /**
   Adds (prepends) another token manifest to the authentication token.  The new token
   manifest specifies this application's EIM identifier and application instance
   identifier as the sender, and the receiver's EIM identifier and
   application instance identifier as the receiver.

   @param keyPair Contains published key information.  It was returned on a previous call to {@link #publishPublicKey(Eid,String,long,int) publishPublicKey}.
   @param authenticationToken The authentication token to be delegated.
   @param rcvEimID The name of the EIM identifier that represents the application
   that is to receive this token.  This information will be added to
   the token to be checked by the receiver to make sure this token was
   intended for the receiver.
   @param rcvInstanceID The application instance identifier that uniquely identifies the
   instance of the application in the network that is to receive this
   authentication token.  This information will be added to the token to
   be checked by the receiver to make sure this token was intended for
   the receiver.
   **/
  public void delegate(AuthenticationKeyPair keyPair, AuthenticationToken authenticationToken, String rcvEimID, String rcvInstanceID)
    throws EimException, IOException, GeneralSecurityException
  {
    if (keyPair == null) throw new NullPointerException("keyPair");
    if (authenticationToken == null) throw new NullPointerException("authenticationToken");
    if (rcvEimID == null) throw new NullPointerException("rcvEimID");
    if (rcvInstanceID == null) throw new NullPointerException("rcvInstanceID");

    verify(authenticationToken, keyPair.getEid(), keyPair.getAppInstanceID());

    if (keyPair.isExpired()) renewKeyPair(keyPair);

    // Build a new Token Manifest.
    int priorCounter = authenticationToken.getManifest().getCounter();
    TokenManifest newTokenManifest =
      new TokenManifest(priorCounter+1,                  // TM counter
                        keyPair.getEid().getName(),      // sender EIM ID name
                        keyPair.getAppInstanceID(),      // sender App Inst ID
                        keyPair.getFormattedTimestamp(), // sender timestamp
                        rcvEimID,                        // receiver EIM ID name
                        rcvInstanceID);                  // receiver App Inst ID

    // Build a new Token Signature Header.
///    SignatureAndManifest[] priorManifests = authenticationToken.getAllManifests();
    byte[] priorManifests = authenticationToken.getPriorManifests();
///    SignatureHeader newSigHeader =
///      SignatureHeader.getInstance(newTokenManifest,
///                                  priorManifests,
///                                  authenticationToken.getUserToken(),
///                                  keyPair.getPrivate());
    SignatureHeader newSigHeader =
      SignatureHeader.getInstance(newTokenManifest,
                                  authenticationToken,
                                  keyPair.getPrivate());

    // Add the new Sig Header and Token Manifest to the passed-in Authentication Token.
    authenticationToken.addNewSignatureAndManifest(newSigHeader, newTokenManifest);
  }


/**
 Verifies the authentication token, and returns the target user name associated with this token, using the target registry.

 @param authenticationToken The authentication token for which to return the user name.  The
 EIM source registry and source registry user name used to find the
 target user are retrieved from the authentication token.
 @param targetRegistry The name of the EIM registry to check for an association.
 @param appEimID The name of the EIM identifier that represents this application.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.
 @param appInstanceID The application instance identifier that uniquely identifies this
 instance of the application in the network.
 This value can be obtained from {@link #getApplicationInfo(String,int) getApplicationInfo}.

 @return The target user associated with this authentication token.  If the source user name could not be mapped to a target user, <tt>null</tt> is returned.
**/
  public RegistryUser getUser(AuthenticationToken authenticationToken, String targetRegistry, Eid currentAppEimID, String currentAppInstanceID)
    throws EimException, IOException, GeneralSecurityException
  {
    if (authenticationToken == null) throw new NullPointerException("authenticationToken");
    if (targetRegistry == null) throw new NullPointerException("targetRegistry");
    if (currentAppEimID == null) throw new NullPointerException("currentAppEimID");
    if (currentAppInstanceID == null) throw new NullPointerException("currentAppInstanceID");

    verify(authenticationToken, currentAppEimID, currentAppInstanceID);

    UserToken userToken = authenticationToken.getUserToken();

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
        ///RegistryUser[] users = (RegistryUser[])targets.toArray();
        RegistryUser[] users = new RegistryUser[targets.size()];
        targets.toArray(users);
        String userName = users[0].getTargetUserName(); // grab first one in list
        for (int i=0; i<users.length; i++) { // compare it to the rest in the list
          if (!userName.equals(users[i].getTargetUserName())) {
            throw new EimException("The source username maps to multiple targets", Constants.ATKNERR_AMBIGUOUS);
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
    throws /*EimException, IOException,*/ /*GeneralSecurityException*/NoSuchAlgorithmException, NoSuchProviderException
 {
   if (keyPairGenerator_ == null) {
     keyPairGenerator_ = KeyPairGenerator.getInstance("RSA");
   }
   synchronized(keyPairGenerator_) {
     keyPairGenerator_.initialize(keySize);
     return keyPairGenerator_.generateKeyPair();
   }
 }


 private static void publish(AuthenticationKeyPair keyPair)
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

///   /////////// experiment starts here //////////////
///
///   appEimID.addAssociation(Association.EIM_TARGET, // TBD experiment
///                           REGISTRY_NAME,
///                           "thisIsJustATest"); // TBD experiment
///
///   appEimID.addAssociation(Association.EIM_TARGET, // TBD experiment
///                           REGISTRY_NAME,
///                           "thisIsAnotherTest"); // TBD experiment
///
///   Set associations = appEimID.getAssociations(Association.EIM_TARGET);
///   Iterator iter = associations.iterator();
///   Association assoc = null;
///   while (iter.hasNext()) {
///     assoc = (Association)iter.next();
///     System.out.println("DEBUG assoc.getUid() == |" + assoc.getUid() + "|");
///   }
///
///   /////////// experiment ends   here //////////////

   appEimID.addAssociation(Association.EIM_TARGET,
                           REGISTRY_NAME,
                           publicKeyUnicodeString);

   // Get the Registry User object for the Association that we just added.

   Set targets = appEimID.findTarget(REGISTRY_NAME);
   ///System.out.println("DEBUG publish(): Number of targets == " + targets.size());
   if (targets.isEmpty()) {  // This should never happen unless we have a bug.
     Trace.log(Trace.ERROR, "No targets in registry " + REGISTRY_NAME + " for EID " + appEimID.getName());
     throw new EimException("No RegistryUser is associated with the specified EID", Constants.INTERNAL_ERROR);
   }
   Iterator iterator = targets.iterator();
   boolean found = false;
   RegistryUser regUser = null;
   // If the addAssociation worked, there should now be exactly 1 RegistryUser with a targetUserName matching the public key.
   ///System.out.println("DEBUG publicKeyUnicodeString == |" + publicKeyUnicodeString + "|");
   while (iterator.hasNext() && !found) {
     regUser = (RegistryUser)iterator.next();
     ///System.out.println("DEBUG regUser.getTargetUserName() == |" + regUser.getTargetUserName() + "|");
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
   Updates the information in the passed-in AuthenticationKeyPair object.
   **/
  private void renewKeyPair(AuthenticationKeyPair keyPair)
    throws EimException, IOException, /*GeneralSecurityException*/NoSuchAlgorithmException, NoSuchProviderException
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
      ///RegistryUser[] regUsers = (RegistryUser[])targets.toArray();
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
  private void verify(AuthenticationToken authToken, Eid currentAppEimID, String currentAppInstanceID)
    throws EimException, IOException, /*GeneralSecurityException*/NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, InvalidKeySpecException, SignatureException
  {
    // Check that we are the intended receiver.
    // Compare receiver info in the token to the caller's EIM ID and
    // application instance id.

    if (!currentAppEimID.getName().equals(authToken.getManifest().getReceiverEidName()))
    {
///      System.out.println("DEBUG AuthDomain.verify(): currentAppEimID.getName() == |" + currentAppEimID.getName() + "|");
///      System.out.println("DEBUG AuthDomain.verify(): authToken.getManifest().getReceiverEidName() == |" + authToken.getManifest().getReceiverEidName() + "|");
      throw new EimException("Receiver EIM ID in token does not match current application", Constants.ATKNERR_TKN_EIMID_MISMATCH);
    }

    if (!currentAppInstanceID.equals(authToken.getManifest().getReceiverAppInstanceID())) {
      throw new EimException("Receiver appInstanceID in token does not match current application", Constants.ATKNERR_TKN_APP_INST_MISMATCH);
    }

    // Get sender's public key from EIM.

    // First derive sender's EID from the manifest information in the token.
    String senderEidName = authToken.getManifest().getSenderEidName();
    String senderAppInstanceID = authToken.getManifest().getSenderAppInstanceID();
    String senderTimestamp = authToken.getManifest().getSenderTimestamp();
    ///System.out.println("senderEidName: |" + senderEidName + "|");
    ///System.out.println("senderAppInstanceID: |" + senderAppInstanceID + "|");
    ///System.out.println("senderTimestamp: |" + senderTimestamp + "|");
    Set eids = domain_.getEidsByName(senderEidName);
    switch (eids.size()) {
      case 0:
        throw new EimException("Sender's EID not found in registry", Constants.ATKNERR_EIMID_NOT_FOUND);
      case 1:  // This is what we expected.
        break;
      default:
        throw new EimException("Found multiple EIDs for the specified sender name (" + senderEidName + ")", Constants.ATKNERR_AMBIGUOUS);
    }

    Eid senderEid = (Eid)eids.toArray()[0]; // We know the array has length==1.
    Set targets = senderEid.findTarget(REGISTRY_NAME, senderAppInstanceID + "=" + senderTimestamp);
/*
    ///TBD experiment:
    if (targets.isEmpty()) {
      System.out.println("DEBUG: No RegistryUser's returned from findTarget.");
    }
    else
    {
      Iterator iterator = targets.iterator();
      RegistryUser user = null;
      while (iterator.hasNext()) {
        user = (RegistryUser)iterator.next();
        System.out.println("RegistryUser.getTargetUserName: |"+user.getTargetUserName()+"|");
        System.out.println("RegistryUser.getRegistryName: |"+user.getRegistryName()+"|");
        System.out.println("RegistryUser.getAdditionalInfo:");
        Set addlInfos = user.getAdditionalInfo();
        Iterator iter = addlInfos.iterator();
        while (iter.hasNext()) {
          String addlInfo = (String)iter.next();
          System.out.println("|"+addlInfo+"|");
        }
      }
    }
    /// Experiment to here.
*/

    switch (targets.size()) {
      case 0:
        throw new EimException("Sender's appInstanceID/timestamp not found in registry", Constants.ATKNERR_PUBLIC_KEY_NOT_FOUND);
      case 1:
        break;
      default:
        throw new EimException("Found multiple registry entries for the specified appInstanceID/timestamp", Constants.ATKNERR_AMBIGUOUS);
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
    byte[] signature = authToken.getSignatureHeader().getSignature();
    byte[] tokenAsBytes = authToken.toBytes();  // TBD: This does extra work, it needlessly converts the signature header to bytes.
    int sigHeaderLength = authToken.getSignatureHeader().getLength();
    byte[] bytesAfterSignature = new byte[tokenAsBytes.length - sigHeaderLength];
    if (DEBUG &&
        bytesAfterSignature.length != authToken.getSignatureHeader().getSignedLength()) {
      System.out.println("DEBUG bytesAfterSignature.length==" + bytesAfterSignature.length + "; signedLength == " + authToken.getSignatureHeader().getSignedLength());
      System.out.println("DEBUG authToken.getLength() == " + authToken.getLength());
      System.out.println("DEBUG authToken.toBytes().length == " + authToken.toBytes().length);
    }
    System.arraycopy(tokenAsBytes, sigHeaderLength, bytesAfterSignature, 0, bytesAfterSignature.length);
    signer.update(bytesAfterSignature);

    if (!signer.verify(signature)) {
      throw new EimException("Signature is not valid", Constants.ATKNERR_SIGNATURE_INVALID);
    }
  }

  // Utility method for debugging.
  // Logs data from a byte array starting at offset for the length specified.
  // Output sixteen bytes per line, two hexadecimal digits per byte, one space between bytes.
  private static void printByteArray(byte[] data)
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
        Trace.log(Trace.DIAGNOSTIC, "AuthenticationDomain.removeTargetAssociations("+appEimID.getName()+","+additionalInfo+ ") found no targets.");
      }
///      // List all source and target associations for the specified EID.
///      System.out.println("DEBUG: All source associations for the EID:");
///      Set associations = appEimID.getAssociations(Association.EIM_SOURCE);
///      Iterator iter = associations.iterator();
///      while (iter.hasNext()) {
///        Association assoc = (Association)iter.next();
///        System.out.println("DEBUG assoc.getUid() == |" + assoc.getUid() + "|");
///      }
///      System.out.println("DEBUG: All target associations for the EID:");
///      associations = appEimID.getAssociations(Association.EIM_TARGET);
///      iter = associations.iterator();
///      while (iter.hasNext()) {
///        Association assoc = (Association)iter.next();
///        System.out.println("DEBUG assoc.getUid() == |" + assoc.getUid() + "|");
///      }
///      System.out.println("DEBUG: All associations for the EID:");
///      associations = appEimID.getAssociations(Association.EIM_SOURCE_AND_TARGET);
///      iter = associations.iterator();
///      while (iter.hasNext()) {
///        Association assoc = (Association)iter.next();
///        System.out.println("DEBUG assoc.getUid() == |" + assoc.getUid() + "|");
///      }
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

///  private static void removeAssociations(Eid appEimID, String additionalInfo, int associationType)
///    throws EimException
///  {
///    // The associations of interest are those with the specified "additional info" string.
///    Set targets = appEimID.findTarget(REGISTRY_NAME, additionalInfo);
///    if (targets.isEmpty()) {
///      System.out.println("DEBUG: AuthDomain.removeAssociations("+appEimID.getName()+","+additionalInfo+","+associationType + ") found no targets.");
///      // List all source and target associations for the specified EID.     DEBUG xxxxxx
///      System.out.println("DEBUG: All source associations for the EID:");
///      Set associations = appEimID.getAssociations(Association.EIM_SOURCE);
///      Iterator iter = associations.iterator();
///      while (iter.hasNext()) {
///        Association assoc = (Association)iter.next();
///        System.out.println("DEBUG assoc.getUid() == |" + assoc.getUid() + "|");
///      }
///      System.out.println("DEBUG: All target associations for the EID:");
///      associations = appEimID.getAssociations(Association.EIM_TARGET);
///      iter = associations.iterator();
///      while (iter.hasNext()) {
///        Association assoc = (Association)iter.next();
///        System.out.println("DEBUG assoc.getUid() == |" + assoc.getUid() + "|");
///      }
///      System.out.println("DEBUG: All associations for the EID:");
///      associations = appEimID.getAssociations(Association.EIM_SOURCE_AND_TARGET);
///      iter = associations.iterator();
///      while (iter.hasNext()) {
///        Association assoc = (Association)iter.next();
///        System.out.println("DEBUG assoc.getUid() == |" + assoc.getUid() + "|");
///      }
///    }
///    else {
///      Iterator iterator = targets.iterator();
///      while (iterator.hasNext()) {
///        RegistryUser regUser = (RegistryUser)iterator.next();
///        appEimID.removeAssociation(associationType,
///                                   REGISTRY_NAME,
///                                   regUser.getTargetUserName());
///      }
///    }
///  }


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
