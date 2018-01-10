<?xml version="1.0"?> 

<!--///////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: xpcml_xpcml.xsl
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
////////////////////////////////////////////////////////////////////////////-->

 <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">

 <xsl:output method="text" />
 <xsl:strip-space elements='*'/>
 <xsl:template match='*'>
  <xsl:choose>
   <xsl:when test="@substitutionGroup='stringParmGroup'">parm type=string name="<xsl:value-of select="@name" />"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='stringParmArrayGroup'">parm type=arrayOfString name="<xsl:value-of select="@name" />"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='intParmGroup'">parm type=int name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='intParmArrayGroup'">parm type=arrayOfInt name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='unsignedIntParmGroup'">parm type=uint name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='unsignedIntParmArrayGroup'">parm type=arrayOfUInt name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='shortParmGroup'">parm type=short name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='shortParmArrayGroup'">parm type=arrayOfShort name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='unsignedShortParmGroup'">parm type=ushort name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='unsignedShortParmArrayGroup'">parm type=arrayOfUShort name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='longParmGroup'">parm type=long name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='longParmArrayGroup'">parm type=arrayOfLong name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='floatParmGroup'">parm type=float name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='floatParmArrayGroup'">parm type=arrayOfFloat name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='packedDecimalParmGroup'">parm type=packed name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='packedDecimalParmArrayGroup'">parm type=arrayOfPacked name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='zonedDecimalParmGroup'">parm type=zoned name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='zonedDecimalParmArrayGroup'">parm type=arrayOfZoned name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='doubleParmGroup'">parm type=double name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='doubleParmArrayGroup'">parm type=arrayOfDouble name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='hexBinaryParmGroup'">parm type=hexBinary name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='hexBinaryParmArrayGroup'">parm type=arrayOfHexBinary name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='structParmGroup'">parm type=structParm name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='structParmArrayGroup'">parm type=arrayOfStructParm name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@substitutionGroup='structArrayGroup'">parm type=structArray name="<xsl:value-of select="@name"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='name'">attributeName=name    attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='length'">attributeName=length  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='passDirection'">attributeName=passDirection  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='passMode'">attributeName=passMode  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='count'">attributeName=count  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='offset'">attributeName=offset  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='offsetFrom'">attributeName=offsetFrom  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='outputSize'">attributeName=outputSize attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='minvrm'">attributeName=minvrm  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='maxvrm'">attributeName=maxvrm  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='totalDigits'">attributeName=totalDigits  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='fractionDigits'">attributeName=fractionDigits  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='trim'">attributeName=trim  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='bytesPerChar'">attributeName=bytesPerChar  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='bidiStringType'">attributeName=bidiStringType  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='ccsid'">attributeName=ccsid  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='totalBytes'">attributeName=totalBytes  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@name='isEmptyString'">attributeName=isEmptyString  attributeValue=<xsl:value-of select="@fixed"/>
  <xsl:text>  
</xsl:text>
   </xsl:when>
   <xsl:when test="@ref">parameterName="<xsl:value-of select="@ref"/>"
  <xsl:text>  
</xsl:text>
   </xsl:when>
  </xsl:choose>
 <xsl:apply-templates/>  
</xsl:template>

</xsl:stylesheet> 
