///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DirectoryEntry.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * The DirectoryEntry class represents system distribution directory information
 * for a particular directory entry. Use the {@link com.ibm.as400.access.DirectoryEntryList#getEntries DirectoryEntryList.getEntries()}
 * method to obtain a DirectoryEntry object.
 * <P>
 * For more information on the system distribution directory and its entries, see
 * the WRKDIRE, ADDDIRE, and RMVDIRE OS/400 CL commands.
 *
 * @see com.ibm.as400.access.DirectoryEntryList
 * @see com.ibm.as400.access.User
**/
public class DirectoryEntry
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  private AS400 system_;
  private String[] values_;

  
  DirectoryEntry(AS400 system, String[] values)
  {
    system_ = system;
    values_ = values;
  }


  /**
   * Returns the administration domain.
   * @return The administration domain.
  **/
  public String getAdministrationDomain()
  {
    return values_[39].trim();
  }


  /**
   * Returns the building.
   * @return The building.
  **/
  public String getBuilding()
  {
    return values_[23].trim();
  }


  /**
   * Returns the cc:Mail address.
   * @return The address.
  **/
  public String getCCMailAddress()
  {
    return values_[29].trim();
  }


  /**
   * Returns the cc:Mail comment.
   * @return The comment.
  **/
  public String getCCMailComment()
  {
    return values_[30].trim();
  }


  /**
   * Returns the company.
   * @return The company.
  **/
  public String getCompany()
  {
    return values_[16].trim();
  }


  /**
   * Returns the country.
   * @return The country.
  **/
  public String getCountry()
  {
    return values_[38].trim();
  }


  /**
   * Returns the department.
   * @return The department.
  **/
  public String getDepartment()
  {
    return values_[17].trim();
  }


  /**
   * Returns which profile will be assigned ownership of the document
   * library objects (DLOs) for this directory entry.
   * Valid values are:
   * <ul>
   * <li>*USRPRF - The user profile.
   * <li>*GRPPRF - The group profile.
   * </ul>
   * @return The DLO owner.
  **/
  public String getDLOOwner()
  {
    return values_[35].trim();
  }


  /**
   * Returns the type of domain-defined attribute 1.
   * @return The type.
  **/
  public String getDomainDefinedAttributeType1()
  {
    return values_[50].trim();
  }


  /**
   * Returns the type of domain-defined attribute 2.
   * @return The type.
  **/
  public String getDomainDefinedAttributeType2()
  {
    return values_[52].trim();
  }


  /**
   * Returns the type of domain-defined attribute 3.
   * @return The type.
  **/
  public String getDomainDefinedAttributeType3()
  {
    return values_[54].trim();
  }


  /**
   * Returns the type of domain-defined attribute 4.
   * @return The type.
  **/
  public String getDomainDefinedAttributeType4()
  {
    return values_[56].trim();
  }


  /**
   * Returns the value of domain-defined attribute 1.
   * @return The value.
  **/
  public String getDomainDefinedAttributeValue1()
  {
    return values_[51].trim();
  }


  /**
   * Returns the value of domain-defined attribute 2.
   * @return The value.
  **/
  public String getDomainDefinedAttributeValue2()
  {
    return values_[53].trim();
  }


  /**
   * Returns the value of domain-defined attribute 3.
   * @return The value.
  **/
  public String getDomainDefinedAttributeValue3()
  {
    return values_[55].trim();
  }


  /**
   * Returns the value of domain-defined attribute 4.
   * @return The value.
  **/
  public String getDomainDefinedAttributeValue4()
  {
    return values_[57].trim();
  }


  /**
   * Returns the fax telephone number.
   * @return The fax telephone number.
  **/
  public String getFaxNumber()
  {
    return values_[21].trim();
  }

  
  /**
   * Returns the first name.
   * @return The first name.
  **/
  public String getFirstName()
  {
    return values_[10].trim();
  }


  /**
   * Returns the full name. 
   * @return The full name.
  **/
  public String getFullName()
  {
    return values_[14].trim();
  }


  /**
   * Returns the generation qualifier.
   * @return The generation qualifier.
  **/
  public String getGenerationQualifier()
  {
    return values_[45].trim();
  }


  /**
   * Returns the given name.
   * @return The given name.
  **/
  public String getGivenName()
  {
    return values_[43].trim();
  }


  /**
   * Returns the initials.
   * @return The initials.
  **/
  public String getInitials()
  {
    return values_[44].trim();
  }


  /**
   * Returns the job title.
   * @return The job title.
  **/
  public String getJobTitle()
  {
    return values_[15].trim();
  }


  /**
   * Returns the last name.
   * @return The last name.
  **/
  public String getLastName()
  {
    return values_[13].trim();
  }

  
  /**
   * Returns the location.
   * @return The location.
  **/
  public String getLocation()
  {
    return values_[22].trim();
  }


  /**
   * Returns line 1 of the mailing address.
   * @return The mailing address.
  **/
  public String getMailingAddress1()
  {
    return values_[25].trim();
  }


  /**
   * Returns line 2 of the mailing address.
   * @return The mailing address.
  **/
  public String getMailingAddress2()
  {
    return values_[26].trim();
  }


  /**
   * Returns line 3 of the mailing address.
   * @return The mailing address.
  **/
  public String getMailingAddress3()
  {
    return values_[27].trim();
  }


  /**
   * Returns line 4 of the mailing address.
   * @return The mailing address.
  **/
  public String getMailingAddress4()
  {
    return values_[28].trim();
  }


  /**
   * Returns the type of mail notification.
   * Possible values are:
   * <ul>
   * <li>*SPECIFIC - Notification is enabled for specific types of mail. See
   * {@link #isMessageMailNotification isMessageMailNotification()} and
   * {@link #isPriorityMailNotification isPriorityMailNotification()} for
   * more information.
   * <li>*ALLMAIL - Notification is enabled for all types of mail.
   * <li>*NOMAIL - Mail notification is not enabled.
   * </ul>
   * @return The type of mail notification.
   * @see #isMessageMailNotification
   * @see #isPriorityMailNotification
  **/
  public String getMailNotification()
  {
    char byte1 = values_[3].charAt(0);
    if (byte1 == '1') return "*SPECIFIC";
    if (byte1 == '2') return "*ALLMAIL";
    return "*NOMAIL";
  }


  /**
   * Returns the value of the mail service level field and product ID. This
   * value is blank when the mail service name is one of the special values:
   * *USRIDX, *SYSMS, or *DOMINO.
   * @return The mail service level.
   * @see #getMailServiceName
  **/
  public String getMailServiceLevel()
  {
    String mail = values_[32];
    return mail.substring(14,mail.length()).trim();
  }


  /**
   * Returns the field name portion of the mail server framework service level.
   * Special values include:
   * <ul>
   * <li>*USRIDX - User index.
   * <li>*SYSMS - System message store.
   * <li>*DOMINO - Lotus Domino mail database.
   * </ul>
   * @return The mail service name.
  **/
  public String getMailServiceName()
  {
    String mail = values_[32];
    return mail.substring(0,10).trim();
  }


  /**
   * Returns the product ID portion of the mail server framework service level.
   * Special values include *NONE, which indicates that there is no product ID.
   * @return The mail service product ID.
  **/
  public String getMailServiceProductID()
  {
    String mail = values_[32];
    return mail.substring(10,17).trim();
  }


  /**
   * Returns the middle name.
   * @return The middle name.
  **/
  public String getMiddleName()
  {
    return values_[12].trim();
  }

  
  /**
   * Returns the network user ID.
   * @return The network user ID.
  **/
  public String getNetworkUserID()
  {
    return values_[18].trim();
  }
  
  
  /**
   * Returns the office.
   * @return The office.
  **/
  public String getOffice()
  {
    return values_[24].trim();
  }


  /**
   * Returns the organization.
   * @return The organization.
  **/
  public String getOrganization()
  {
    return values_[41].trim();
  }


  /**
   * Returns organization unit 1.
   * @return The organization unit.
  **/
  public String getOrganizationUnit1()
  {
    return values_[46].trim();
  }


  /**
   * Returns organization unit 1.
   * @return The organization unit.
  **/
  public String getOrganizationUnit2()
  {
    return values_[47].trim();
  }


  /**
   * Returns organization unit 1.
   * @return The organization unit.
  **/
  public String getOrganizationUnit3()
  {
    return values_[48].trim();
  }


  /**
   * Returns organization unit 1.
   * @return The organization unit.
  **/
  public String getOrganizationUnit4()
  {
    return values_[49].trim();
  }


  /**
   * Returns the paper representation of the X.400 O/R name,
   * if one exists.
   * @return The X.400 O/R name.
  **/
  public String getORName()
  {
    return values_[37].trim();
  }


  /**
   * Returns the field name portion of the preferred address.
   * The field name can be a special value, user-defined field name, or an
   * IBM-defined field name. Special values include:
   * <ul>
   * <li>*USRID - User ID/address.
   * <li>*ORNAME - X.400 O/R name.
   * <li>*SMTP - SMTP name.
   * </ul>
   * @return The preferred address name.
  **/
  public String getPreferredAddressName()
  {
    String addr = values_[33];
    return addr.substring(0,10).trim();
  }


  /**
   * Returns the product ID portion of the preferred address.
   * Special values include:
   * <ul>
   * <li>*IBM - Indicates an IBM-defined field name.
   * <li>*NONE - Indicates that there is no product ID.
   * </ul>
   * @return The product ID.
  **/
  public String getPreferredAddressProductID()
  {
    String addr = values_[33];
    return addr.substring(10,17).trim();
  }


  /**
   * Returns the address type name of the preferred address.
   * @return The name of the preferred address type.
  **/
  public String getPreferredAddressTypeName()
  {
    String addr = values_[33];
    return addr.substring(21,29).trim();
  }


  /**
   * Returns the address type value of the preferred address.
   * @return The value of the preferred address type.
  **/
  public String getPreferredAddressTypeValue()
  {
    String addr = values_[33];
    return addr.substring(17,21).trim();
  }

  
  /**
   * Returns the value of the preferred address field and product ID.
   * This format of this String is dependent upon the preferred address name:
   * <ul>
   * <li>When the name is *USRID, the value consists of an 8-character user ID,
   * 8-character user address, 8-character system name, and 8-character system group.
   * <li>When the name is *ORNAME, the value consists of an X.400 O/R name formatted
   * based on those fields that exist for this user, up to 909 characters.
   * <li>When the name is *SMTP, the value consists of a 24-character STMP user ID and
   * a 256-character SMTP domain or route, followed by an optional 64-character SMTP user ID
   * (if the SMTP names are converted using the CVTNAMSMTP command).
   * <li>When the name is an IBM-defined or user-defined field, the value consists of
   * the field in the system distribution directory, with a maximum size of 512 characters.
   * </ul>
   * @return The value of the preferred address.
   * @see #getPreferredAddressName
  **/
  public String getPreferredAddressValue()
  {
    String addr = values_[33];
    return addr.substring(33,addr.length()).trim();
  }


  /**
   * Returns the preferred name.
   * @return The preferred name.
  **/
  public String getPreferredName()
  {
    return values_[11].trim();
  }


  /**
   * Returns the private management domain.
   * @return The private management domain.
  **/
  public String getPrivateManagementDomain()
  {
    return values_[40].trim();
  }


  /**
   * Returns the SMTP domain, if one exists. Either the SMTP domain
   * or route can exist, not both.
   * @return The SMTP domain.
   * @see #getSMTPRoute
  **/
  public String getSMTPDomain()
  {
    return values_[59].trim();
  }


  /**
   * Returns the SMTP route, if one exists. Either the SMTP domain
   * or route can exist, not both.
   * @return The SMTP route.
   * @see #getSMTPDomain
  **/
  public String getSMTPRoute()
  {
    return values_[60].trim();
  }


  /**
   * Returns the SMTP user ID, if one exists.
   * @return The SMTP user ID.
  **/
  public String getSMTPUserID()
  {
    return values_[58].trim();
  }


  /**
   * Returns the surname.
   * @return The surname.
  **/
  public String getSurname()
  {
    return values_[42].trim();
  }


  /**
   * Returns the system.
   * @return The system.
  **/  
  public AS400 getSystem()
  {
    return system_;
  }
  
  
  /**
   * Returns the system group of this directory entry.
   * @return The system group.
  **/
  public String getSystemGroup()
  {
    return values_[8].trim();
  }


  /**
   * Returns the system name of this directory entry.
   * @return The system name.
  **/
  public String getSystemName()
  {
    return values_[7].trim();
  }


  /**
   * Returns the first telephone number.
   * @return The telephone number.
  **/
  public String getTelephoneNumber1()
  {
    return values_[19].trim();
  }


  /**
   * Returns the second telephone number.
   * @return The telephone number.
  **/
  public String getTelephoneNumber2()
  {
    return values_[20].trim();
  }


  /**
   * Returns the text for this directory entry. This is not the same
   * as the user description.
   * @return The text.
  **/
  public String getText()
  {
    return values_[31].trim();
  }


  /**
   * Returns the address of this directory entry.
   * @return The address.
  **/
  public String getUserAddress()
  {
    return values_[6].trim();
  }


  /**
   * Returns the user description field for this directory entry.
   * @return The description.
  **/
  public String getUserDescription()
  {
    return values_[9].trim();
  }


  /**
   * Returns the user ID of this directory entry. This is not the
   * same as the associated user profile.
   * @return The user ID (which has a maximum length of 8 characters).
   * @see #getUserProfile
  **/
  public String getUserID()
  {
    return values_[4].trim();
  }

  
  /**
   * Returns the user profile name associated with this entry. This is not
   * the same as the entry's user ID. 
   * @return The user profile (which has a maximum length of 10 characters).
   * @see #getUserID
  **/
  public String getUserProfile()
  {
    return values_[0];
  }


  /**
   * Indicates if this entry represents an indirect user. This only
   * applies to local directory entries.
   * @return true if the user is an indirect user; false otherwise.
   * @see #isLocal
  **/
  public boolean isIndirectUser()
  {
    return values_[1].trim().equals("1");
  }

  /**
   * Indicates if this entry is a local entry.
   * @return true if this is a local entry; false if it is a shadowed entry.
  **/
  public boolean isLocal()
  {
    return values_[5].trim().equals("0"); // 1 means shadowed
  }

  
  /**
   * Indicates if this entry represents a manager.
   * @return true if the user is a manager; false otherwise.
  **/
  public boolean isManager()
  {
    return values_[36].charAt(0) == '1';
  }


  /**
   * Indicates a type of mail notification used for this entry.
   * @return true if notification of messages is enabled for this entry;
   * false otherwise.
   * @see #getMailNotification
   * @see #isPriorityMailNotification
  **/
  public boolean isMessageMailNotification()
  {
    return values_[3].charAt(2) == '1';
  }

  
  /**
   * Indicates if the cover page is to be printed. This only applies to
   * local directory entries.
   * @return true if the cover page is to be printed; false otherwise.
   * @see #isLocal
  **/
  public boolean isPrintCoverPage()
  {
    return values_[2].trim().equals("1");
  }


  /**
   * Indicates a type of mail notification used for this entry.
   * @return true if notification of priority, private, and important mail
   * is enabled for this entry; false otherwise.
   * @see #getMailNotification
   * @see #isMessageMailNotification
  **/
  public boolean isPriorityMailNotification()
  {
    return values_[3].charAt(1) == '1';
  }
    

  /**
   * Indicates whether this entry should be synchronized with directories
   * other than the System Distribution Directory.
   * @return true if synchronization is allowed; false otherwise.
  **/
  public boolean isSynchronized()
  {
    return values_[34].charAt(0) == '1';
  }

}
