///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;                        // @B1A

/**
  * The PrintObjectImplRemote class implements the public methods defined in
  * PrintObjectImpl.  It also provides for the complete implementation of the
  * PrintObject class, an abstract base class for the various types of
  * network print objects.
 **/

abstract class PrintObjectImplRemote
implements PrintObjectImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    NPCPAttribute       attrs;
    private AS400ImplRemote system_; // @A6C - changed from AS400 to AS400ImplRemote
    private NPCPID      cpID_;
    private int         objectType_;


    /**
     * Check to see if the iSeries system has been set...
     **/
    void checkRunTimeState()
    {
        if( getSystem() == null )
        {
            Trace.log(Trace.ERROR, "Parameter 'system' has not been set.");
            throw new ExtendedIllegalStateException(
              "system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
    }



    abstract NPCPAttributeIDList getAttrIDsToRetrieve();
    abstract NPCPAttributeIDList getAttrIDsToRetrieve(int AttrID); //@C1A



    // @A5A - Added getAttrValue() method
    /**
     * Gets the print object attributes.
     * This method is required so changes in the public class
     * are propagated to this remote implementation of the class.
     *
     * @return NPCPAttribute
     **/
    public NPCPAttribute getAttrValue()
    {
        return attrs;
    }



    // This method is available for use by other classes within the package.
    final NPCPID getIDCodePoint()
    {
        return cpID_;
    }



    /**
     * Returns an attribute of the object that is a Integer type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     *
     * @exception AS400Exception If the iSeries system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the iSeries system is not at the correct level.
     **/
    public Integer getIntegerAttribute(int attributeID)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        Integer aValue = null;
        if (attrs != null)
        {
           aValue = attrs.getIntValue(attributeID);
        }
        if (aValue == null)
        {
            aValue = getIDCodePoint().getIntValue(attributeID);
            if (aValue == null)
            {
               NPCPAttributeIDList attrIDsToRetreive = getAttrIDsToRetrieve();

               if (attrIDsToRetreive.containsID(attributeID))
               {
                   updateAttrs(attrIDsToRetreive);
                   if (attrs != null)
                   {
                      aValue = attrs.getIntValue(attributeID);
                   }
               }
            }
        }

        if (aValue == null) // @A3A
        {
            NPSystem npSystem = NPSystem.getSystem(getSystem());
            NPConversation conversation = npSystem.getConversation();
            String curLevel = conversation.getAttribute(PrintObject.ATTR_NPSLEVEL);
            npSystem.returnConversation(conversation);
            throw new RequestNotSupportedException(curLevel,
                                                   RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
        }

        return aValue;
    }

//@C1A
    /**
     * Returns an attribute of the object that is a Integer type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     *
     * @exception AS400Exception If the iSeries system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the iSeries system is not at the correct level.
     **/
    public Integer getSingleIntegerAttribute(int attributeID)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        Integer aValue = null;
        if (attrs != null)
        {
           aValue = attrs.getIntValue(attributeID);
        }
        if (aValue == null)
        {
            aValue = getIDCodePoint().getIntValue(attributeID);
            if (aValue == null)
            {
               NPCPAttributeIDList attrIDsToRetreive = getAttrIDsToRetrieve(attributeID);

               if (attrIDsToRetreive.containsID(attributeID))
               {
                   updateAttrs(attrIDsToRetreive);
                   if (attrs != null)
                   {
                      aValue = attrs.getIntValue(attributeID);
                   }
               }
            }
        }

        if (aValue == null) 
        {
            NPSystem npSystem = NPSystem.getSystem(getSystem());
            NPConversation conversation = npSystem.getConversation();
            String curLevel = conversation.getAttribute(PrintObject.ATTR_NPSLEVEL);
            npSystem.returnConversation(conversation);
            throw new RequestNotSupportedException(curLevel,
                                                   RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
        }

        return aValue;
    }


    /**
     * Returns an attribute of the object that is a Float type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     * @exception AS400Exception If the iSeries system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the iSeries system is not at the correct level.
     **/
    public Float getSingleFloatAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        Float aValue = null;
        if (attrs != null)
        {
           aValue = attrs.getFloatValue(attributeID);
        }
        if (aValue == null)
        {
            aValue = getIDCodePoint().getFloatValue(attributeID);
            if (aValue == null)
            {
               NPCPAttributeIDList attrIDsToRetreive = getAttrIDsToRetrieve(attributeID);

               if (attrIDsToRetreive.containsID(attributeID))
               {
                   updateAttrs(attrIDsToRetreive);
                   if (attrs != null)
                   {
                      aValue = attrs.getFloatValue(attributeID);
                   }
               }
            }
        }

        if (aValue == null) // @A3A
        {
            NPSystem npSystem = NPSystem.getSystem(getSystem());
            NPConversation conversation = npSystem.getConversation();
            String curLevel = conversation.getAttribute(PrintObject.ATTR_NPSLEVEL);
            npSystem.returnConversation(conversation);
            throw new RequestNotSupportedException(curLevel,
                                                   RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
        }

        return aValue;
    }


    /**
     * Returns an attribute of the object that is a String type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     *
     * @exception AS400Exception If the iSeries system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the iSeries system is not at the correct level.
     **/
    public String getSingleStringAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        String str = null;
        if (attrs != null)
        {
           str = attrs.getStringValue(attributeID);
        }
        if (str == null)
        {
            str = getIDCodePoint().getStringValue(attributeID);
            if (str == null)
            {
               NPCPAttributeIDList attrIDsToRetreive = getAttrIDsToRetrieve(attributeID);

               if (attrIDsToRetreive.containsID(attributeID))
               {
                   updateAttrs(attrIDsToRetreive);
                   if (attrs != null) {
                      str = attrs.getStringValue(attributeID);
                   }
               }
            }
        }

        if (str == null) 
        {
            NPSystem npSystem = NPSystem.getSystem(getSystem());
            NPConversation conversation = npSystem.getConversation();
            String curLevel = conversation.getAttribute(PrintObject.ATTR_NPSLEVEL);
            npSystem.returnConversation(conversation);
            throw new RequestNotSupportedException(curLevel,
                                                   RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
        }

        return str;
    }




//@C1A end

    /**
     * Returns an attribute of the object that is a Float type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     * @exception AS400Exception If the iSeries system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the iSeries system is not at the correct level.
     **/
    public Float getFloatAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        Float aValue = null;
        if (attrs != null)
        {
           aValue = attrs.getFloatValue(attributeID);
        }
        if (aValue == null)
        {
            aValue = getIDCodePoint().getFloatValue(attributeID);
            if (aValue == null)
            {
               NPCPAttributeIDList attrIDsToRetreive = getAttrIDsToRetrieve();

               if (attrIDsToRetreive.containsID(attributeID))
               {
                   updateAttrs(attrIDsToRetreive);
                   if (attrs != null)
                   {
                      aValue = attrs.getFloatValue(attributeID);
                   }
               }
            }
        }

        if (aValue == null) // @A3A
        {
            NPSystem npSystem = NPSystem.getSystem(getSystem());
            NPConversation conversation = npSystem.getConversation();
            String curLevel = conversation.getAttribute(PrintObject.ATTR_NPSLEVEL);
            npSystem.returnConversation(conversation);
            throw new RequestNotSupportedException(curLevel,
                                                   RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
        }

        return aValue;
    }



    /**
     * Returns an attribute of the object that is a String type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     *
     * @exception AS400Exception If the iSeries system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the iSeries.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the iSeries system is not at the correct level.
     **/
    public String getStringAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        String str = null;
        if (attrs != null)
        {
           str = attrs.getStringValue(attributeID);
        }
        if (str == null)
        {
            str = getIDCodePoint().getStringValue(attributeID);
            if (str == null)
            {
               NPCPAttributeIDList attrIDsToRetreive = getAttrIDsToRetrieve();

               if (attrIDsToRetreive.containsID(attributeID))
               {
                   updateAttrs(attrIDsToRetreive);
                   if (attrs != null) {
                      str = attrs.getStringValue(attributeID);
                   }
               }
            }
        }

        if (str == null) // @A3A
        {
            NPSystem npSystem = NPSystem.getSystem(getSystem());
            NPConversation conversation = npSystem.getConversation();
            String curLevel = conversation.getAttribute(PrintObject.ATTR_NPSLEVEL);
            npSystem.returnConversation(conversation);
            throw new RequestNotSupportedException(curLevel,
                                                   RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
        }

        return str;
    }



    /**
      * Returns the iSeries system on which this object exists.
      * @return The iSeries system on which this object exists.
      **/
    final public AS400ImplRemote getSystem() // @A6C - changed AS400 to AS400ImplRemote
    {
        return system_;
    }



    // This method is available for use by other classes within the package.
    final void setIDCodePoint(NPCPID cpID)
    {
        cpID_ = cpID;
    }



    // @A5A - Added setPrintObjectAttrs method
    /**
     * Sets the print object attributes.
     * This method is required so changes in the public class
     * are propogated to this remote implementation of the class.
     *
     * @param idCodePoint  The ID code point
     * @param cpAttrs      The code point attributes
     * @param type         The type.
     **/
    public void setPrintObjectAttrs(NPCPID idCodePoint,
                                    NPCPAttribute cpAttrs,
                                    int type)
    {
        cpID_       = idCodePoint;
        attrs       = cpAttrs;
        objectType_ = type;

        try {                                                                                       // @B1A
            cpID_.setConverter(ConverterImplRemote.getConverter(system_.getCcsid(), system_));      // @B1A
            if (attrs != null)                                                                      // @B1A
                attrs.setConverter(ConverterImplRemote.getConverter(system_.getCcsid(), system_));  // @B1A
        }                                                                                           // @B1A
        catch(UnsupportedEncodingException e) {                                                     // @B1A
            if (Trace.isTraceErrorOn())                                                             // @B1A
                Trace.log(Trace.ERROR, "Error initializing converter for print object", e);         // @B1A
        }                                                                                           // @B1A
    }



    /**
     * Sets the iSeries system on which this object exists. This
     * method is primarily provided for visual application builders
     * that support JavaBeans. Application programmers should
     * specify the iSeries system in the constructor for the
     * specific print object.
     *
     * @param system The iSeries system on which this object exists.
     **/
    final public void setSystem(AS400Impl system)
    {
        system_ = (AS400ImplRemote) system;  // @A6C - cast to ImplRemote
        attrs   = null;
    }



    /**
     * Updates the attributes of this object by going to the server and
     * retrieving the latest attributes for the object.
     *
     * @exception AS400Exception If the iSeries system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the iSeries system is not at the correct level.
     **/
    public void update()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
       updateAttrs(getAttrIDsToRetrieve());
    }



    /**
     * Go to the host and get the lastest attributes for this object
     **/
    void updateAttrs(NPCPAttributeIDList attrIDs)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        checkRunTimeState();

        NPDataStream req = new NPDataStream(objectType_);
        NPDataStream reply = new NPDataStream(objectType_);
        NPCPAttribute cpAttrs = new NPCPAttribute();

        req.setAction(NPDataStream.RETRIEVE_ATTRIBUTES);
        req.addCodePoint(getIDCodePoint());
        req.addCodePoint(attrIDs);

        reply.addCodePoint(cpAttrs);
        NPSystem npSystem = NPSystem.getSystem(getSystem());
        if (npSystem != null) {
            int rc = npSystem.makeRequest(req, reply);
            if (rc == 0) {
                if (attrs != null) {
                    attrs.addUpdateAttributes(cpAttrs);
                }
                else {
                    attrs = cpAttrs;
                }
            }
            else {
                NPConversation conversation = npSystem.getConversation();
                String curLevel = conversation.getAttribute(PrintObject.ATTR_NPSLEVEL);
                npSystem.returnConversation(conversation);

                switch(rc) {
                        // we get back RET_INV_REQ_ACT on pre-V3R7 systems if we try
                        // to open an AFP resource.  The host must be at V3R7 with PTFs
                        // to work with AFP resources so throw a requestNotSupportedException
                        // here.
                    case NPDataStream.RET_INV_REQ_ACT:
                        throw new RequestNotSupportedException(curLevel,
                                                               RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);


                        // any other error is either an unexpected error or an error
                        // completing request
                    default:

                        break;
                    }
            } // end else
        }
    }

}
