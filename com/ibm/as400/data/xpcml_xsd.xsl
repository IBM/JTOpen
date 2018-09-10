<?xml version="1.0"?> 

<!--///////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: xpcml_xsd.xsl
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
////////////////////////////////////////////////////////////////////////////-->

<!-- xpcml_xsd.xsl -->

 <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">
 <xsl:param name="xsdFileName" select="xpcml.xsd"/>

 <xsl:output method="text" />

 <!-- Note to reader: Recall that, at any given step of XSL transformation, only the most-specific
 matching template is applied. -->

<xsl:param name="nodeCount" select="0"/>

 <xsl:template match='xpcml'>
  &lt;xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
   &lt;xs:include schemaLocation='<xsl:value-of select='$xsdFileName'/>'/>
  <xsl:apply-templates/>
  &lt;/xs:schema>
 </xsl:template>

<!-- <xsl:template match="parameterList//*[@name] | struct//*[@name]"> -->
<xsl:template match="stringParm[@name] | arrayOfStringParm[@name] | arrayOfStructParm[@name] | intParm[@name] |
   arrayOfIntParm[@name] | unsignedIntParm[@name] | arrayOfUnsignedIntParm[@name] |  hexBinaryParm[@name] | 
   arrayOfHexBinaryParm[@name] | shortParm[@name] | arrayOfShortParm[@name]  | unsignedShortParm[@name] |
   arrayOfUnsignedShortParm[@name] | longParm[@name] | arrayOfLongParm[@name] | floatParm[@name] | 
   arrayOfFloatParm[@name] | doubleParm[@name] | arrayOfDoubleParm[@name] | zonedDecimalParm[@name] | 
   arrayOfZonedDecimalParm[@name] | packedDecimalParm[@name] | arrayOfPackedDecimalParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="name()='struct'">
  </xsl:when>
  <xsl:when test="name()='structParm'"/>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
     <xsl:call-template name="finishParm"/>
  </xsl:when>
  <xsl:when test="name()='stringParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="ccsid" select="@ccsid"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="trim" select="@trim"/>
    <xsl:variable name="bytesPerChar" select="@bytesPerChar"/>
    <xsl:variable name="bidiStringType" select="@bidiStringType"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:variable name="nodeName" select="name()"/>
    <xsl:choose>
      <xsl:when test="preceding::stringParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@ccsid)=string($ccsid) and string(@passMode)=string($passMode) and
           string(@trim)=string($trim) and string(@bytesPerChar)=string($bytesPerChar) and
           string(@offsetFrom)=string($offsetFrom) and string(@bidiStringType)=string($bidiStringType)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise >
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfStringParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="ccsid" select="@ccsid"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="trim" select="@trim"/>
    <xsl:variable name="bytesPerChar" select="@bytesPerChar"/>
    <xsl:variable name="bidiStringType" select="@bidiStringType"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:variable name="nodeName" select="name()"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfStringParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@ccsid)=string($ccsid) and string(@passMode)=string($passMode) and
           string(@trim)=string($trim) and string(@bytesPerChar)=string($bytesPerChar) and
           string(@offsetFrom)=string($offsetFrom) and string(@bidiStringType)=string($bidiStringType)
           and string(@passDirection)=string($passDirection)]" >
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise >
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfStructParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="struct" select="@struct"/>
    <xsl:variable name="nodeName" select="name()"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfStructParm[@name=$parmName  and
           string(@count)=string($count) and string(@struct)=string($struct)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise >
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='intParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::intParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfIntParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfIntParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='unsignedIntParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::unsignedIntParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfUnsignedIntParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfUnsignedIntParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='hexBinaryParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="totalBytes" select="@totalBytes"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="ccsid" select="@ccsid"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::hexBinaryParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm) and
           string(@totalBytes)=string($totalBytes) and string(@ccsid)=string($ccsid) and
           string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfHexBinaryParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="totalBytes" select="@totalBytes"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="ccsid" select="@ccsid"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfHexBinaryParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm) and
           string(@totalBytes)=string($totalBytes) and string(@ccsid)=string($ccsid) and
           string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='shortParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::shortParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfShortParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfShortParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='unsignedShortParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::unsignedShortParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfUnsignedShortParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfUnsignedShortParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='longParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::longParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='unsignedLongParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::unsignedLongParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='floatParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::floatParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfFloatParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfFloatParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='doubleParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::doubleParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfDoubleParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfDoubleParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='zonedDecimalParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="totalDigits" select="@totalDigits"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="fractionDigits" select="@fractionDigits"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::zonedDecimalParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm) and
           string(@totalDigits)=string($totalDigits) and string(@fractionDigits)=string($fractionDigits)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfZonedDecimalParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="totalDigits" select="@totalDigits"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="fractionDigits" select="@fractionDigits"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfZonedDecimalParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm) and
           string(@totalDigits)=string($totalDigits) and string(@fractionDigits)=string($fractionDigits)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='packedDecimalParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="totalDigits" select="@totalDigits"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="fractionDigits" select="@fractionDigits"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::packedDecimalParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm) and
           string(@totalDigits)=string($totalDigits) and string(@fractionDigits)=string($fractionDigits)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='arrayOfPackedDecimalParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="totalDigits" select="@totalDigits"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="fractionDigits" select="@fractionDigits"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfPackedDecimalParm[@name=$parmName and string(@count)=string($count) and
           string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm) and
           string(@totalDigits)=string($totalDigits) and string(@fractionDigits)=string($fractionDigits)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@passMode)=string($passMode) and string(@offsetFrom)=string($offsetFrom)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
          <xsl:call-template name="finishParm"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
 </xsl:choose>
 <xsl:apply-templates/>
</xsl:template>

<xsl:template name="finishParm">
     &lt;xs:complexType>
         <xsl:choose>
         <xsl:when test="name()='stringParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="stringParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfStringParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="stringParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="stringElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='arrayOfStructParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="structParmArrayType">
           &lt;xs:sequence>
              &lt;xs:element name="struct_i" type="structElementType" minOccurs="0" maxOccurs="unbounded"/>
           &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='intParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="intParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfIntParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="intParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="intElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='unsignedIntParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="unsignedIntParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfUnsignedIntParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="unsignedIntParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="unsignedIntElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='hexBinaryParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="hexBinaryParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfHexBinaryParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="hexBinaryParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="hexBinaryElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='shortParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="shortParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfShortParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="shortParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="shortElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='unsignedShortParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="unsignedShortParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfUnsignedShortParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="unsignedShortParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="unsignedShortElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='longParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="longParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfLongParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="longParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="longElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='floatParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="floatParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfFloatParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="floatParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="floatElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='doubleParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="doubleParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfDoubleParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="doubleParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="doubleElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='packedDecimalParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="packedDecimalParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfPackedDecimalParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="packedDecimalParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="packedDecimalElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         <xsl:when test="name()='zonedDecimalParm'">
         &lt;xs:simpleContent>
         &lt;xs:restriction base="zonedDecimalParmType">
         </xsl:when>
         <xsl:when test="name()='arrayOfZonedDecimalParm'">
         &lt;xs:complexContent>
         &lt;xs:restriction base="zonedDecimalParmArrayType">
            &lt;xs:sequence>
              &lt;xs:element name="i" type="zonedDecimalElementType" minOccurs="0" maxOccurs="unbounded"/>
            &lt;/xs:sequence>
         </xsl:when>
         </xsl:choose>
              <xsl:if test="@name">  <!-- name attribute specified -->
                  &lt;xs:attribute name="name" type="string50" fixed="<xsl:value-of select="@name"/>" />
              </xsl:if>
              <xsl:if test="@length">  <!-- length attribute specified -->
                  &lt;xs:attribute name="length" type="xs:string" fixed="<xsl:value-of select="@length"/>" />
              </xsl:if>
              <xsl:if test="@passDirection">  <!-- passDirection attribute specified -->
                  &lt;xs:attribute name="passDirection" type="passDirectionType" fixed="<xsl:value-of select="@passDirection"/>" />
              </xsl:if>
              <xsl:if test="@passMode">  <!-- passMode attribute specified -->
                  &lt;xs:attribute name="passMode" type="passModeType" fixed="<xsl:value-of select="@passMode"/>" />
              </xsl:if>
              <xsl:if test="@count">  <!-- count attribute specified -->
                  &lt;xs:attribute name="count" type="xs:string" fixed="<xsl:value-of select="@count"/>" />
              </xsl:if>
              <xsl:if test="@totalBytes">  <!-- count attribute specified -->
                  &lt;xs:attribute name="totalBytes" type="xs:string" fixed="<xsl:value-of select="@totalBytes"/>" />
              </xsl:if>
              <xsl:if test="@offsetFrom">  <!-- count attribute specified -->
                  &lt;xs:attribute name="offsetFrom" type="xs:string" fixed="<xsl:value-of select="@offsetFrom"/>" />
              </xsl:if>
              <xsl:if test="@outputSize">  <!-- count attribute specified -->
                  &lt;xs:attribute name="outputSize" type="xs:string" fixed="<xsl:value-of select="@outputSize"/>" />
              </xsl:if>
              <xsl:if test="@offset">  <!-- count attribute specified -->
                  &lt;xs:attribute name="offset" type="xs:string" fixed="<xsl:value-of select="@offset"/>" />
              </xsl:if>
              <xsl:if test="@minvrm">  <!-- count attribute specified -->
                  &lt;xs:attribute name="minvrm" type="string10" fixed="<xsl:value-of select="@minvrm"/>" />
              </xsl:if>
              <xsl:if test="@maxvrm">  <!-- count attribute specified -->
                  &lt;xs:attribute name="maxvrm" type="string10" fixed="<xsl:value-of select="@maxvrm"/>" />
              </xsl:if>
              <xsl:if test="@totalDigits">  <!-- count attribute specified -->
                  &lt;xs:attribute name="totalDigits" type="xs:positiveInteger" fixed="<xsl:value-of select="@totalDigits"/>" />
              </xsl:if>
              <xsl:if test="@fractionDigits">  <!-- count attribute specified -->
                  &lt;xs:attribute name="fractionDigits" type="xs:nonNegativeInteger" fixed="<xsl:value-of select="@fractionDigits"/>" />
              </xsl:if>
              <xsl:if test="@ccsid">  <!-- count attribute specified -->
                  &lt;xs:attribute name="ccsid" type="xs:string" fixed="<xsl:value-of select="@ccsid"/>" />
              </xsl:if>
              <xsl:if test="@storageEncoding">  <!-- count attribute specified -->
                  &lt;xs:attribute name="storageEncoding" type="xs:string" fixed="<xsl:value-of select="@storageEncoding"/>" />
              </xsl:if>
              <xsl:if test="@trim">  <!-- count attribute specified -->
                  &lt;xs:attribute name="trim" type="trimType" fixed="<xsl:value-of select="@trim"/>" />
              </xsl:if>
              <xsl:if test="@bytesPerChar">  <!-- bytesPerChar attribute specified -->
                  &lt;xs:attribute name="bytesPerChar" type="charType" fixed="<xsl:value-of select="@bytesPerChar"/>" />
              </xsl:if>
              <xsl:if test="@bidiStringType">  <!-- count attribute specified -->
                  &lt;xs:attribute name="bidiStringType" type="bidiStringTypeType" fixed="<xsl:value-of select="@bidiStringType"/>" />
              </xsl:if>
              <xsl:if test="@isEmptyString">  <!-- init value is the empty string -->
                  &lt;xs:attribute name="isEmptyString" type="xs:boolean" fixed="true" />
              </xsl:if>

              <xsl:if test="@struct">  <!-- count attribute specified -->
                  &lt;xs:attribute name="struct" type="string50" fixed="<xsl:value-of select="@struct"/>" />
              </xsl:if>
         &lt;/xs:restriction>
       <xsl:if test="name()='arrayOfStructParm' or name() = 'arrayOfStringParm' or name() = 'arrayOfIntParm' or
                    name()= 'arrayOfUnsignedIntParm' or name() = 'arrayOfShortParm' or
                    name()= 'arrayOfUnsignedShortParm' or name() = 'arrayOfLongParm' or 
                    name() = 'arrayOfFloatParm' or name() = 'arrayOfDoubleParm' or 
                    name() = 'arrayOfHexBinaryParm' or name() = 'arrayOfZonedDecimalParm' or
                    name() =  'arrayOfPackedDecimalParm' ">
        &lt;/xs:complexContent>
       </xsl:if>
       <xsl:if test="name() != 'arrayOfStructParm' and name() != 'arrayOfStringParm' and name() != 'arrayOfIntParm' 
           and name() != 'arrayOfUnsignedIntParm' and name() != 'arrayOfShortParm' and 
           name() != 'arrayOfUnsignedShortParm' and name() != 'arrayOfLongParm' and 
           name() != 'arrayOfFloatParm' and name() != 'arrayOfDoubleParm' and 
           name() != 'arrayOfZonedDecimalParm' and name() != 'arrayOfPackedDecimalParm' and
           name() != 'arrayOfHexBinaryParm' ">
          &lt;/xs:simpleContent>
       </xsl:if>
     &lt;/xs:complexType>
   &lt;/xs:element>
</xsl:template>

<!-- Don't replace structParms for now - Not sure how to handle
<xsl:template match="structParm[@name]">
  <xsl:choose>
   <xsl:when test="following::structParm/@name = @name" >
   </xsl:when>
   <xsl:otherwise>
   &lt;xs:element name="<xsl:value-of select="@name"/>" substitutionGroup="structParmGroup" >
     &lt;xs:complexType>
       &lt;xs:simpleContent>
         &lt;xs:restriction base="structParmType">
              <xsl:if test="@name">
                  &lt;xs:attribute name="name" type="string50" fixed="<xsl:value-of select="@name"/>" />
              </xsl:if>
              <xsl:if test="@struct">
                  &lt;xs:attribute name="struct" type="string50" fixed="<xsl:value-of select="@struct"/>" />
              </xsl:if>
              <xsl:if test="@count">
                  &lt;xs:attribute name="count" type="xs:string" fixed="<xsl:value-of select="@count"/>" />
              </xsl:if>
         &lt;/xs:restriction>
       &lt;/xs:simpleContent>
     &lt;/xs:complexType>
   &lt;/xs:element>
   </xsl:otherwise>
  </xsl:choose>
 <xsl:apply-templates/>
</xsl:template>
-->

<!-- Template for the specific case of processing a text element.  -->
<xsl:template match="text()">
  <xsl:apply-templates />
</xsl:template>

<xsl:template match="*">
   <xsl:apply-templates />
</xsl:template>

<xsl:template name="setName">
  <xsl:param name="fullName">concat(@name,'_')</xsl:param>
  <xsl:param name="nodeCount"></xsl:param>
  <xsl:variable name="completeName" select="concat($fullName,$nodeCount)"/>
  <xsl:choose>
    <xsl:when test="name()='stringParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="stringParmGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfStringParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="stringParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfStructParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="structParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='intParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="intParmGroup"  >
    </xsl:when>
    <xsl:when test="name()='arrayOfIntParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="intParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='unsignedIntParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="unsignedIntParmGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfUnsignedIntParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="unsignedIntParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='hexBinaryParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="hexBinaryParmGroup" default="" >
    </xsl:when>
    <xsl:when test="name()='arrayOfHexBinaryParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="hexBinaryParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='shortParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="shortParmGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfShortParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="shortParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='unsignedShortParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="unsignedShortParmGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfUnsignedShortParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="unsignedShortParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='longParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="longParmGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfLongParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="longParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='floatParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="floatParmGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfFloatParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="floatParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='doubleParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="doubleParmGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfDoubleParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="doubleParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='zonedDecimalParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="zonedDecimalParmGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfZonedDecimalParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="zonedDecimalParmArrayGroup" >
    </xsl:when>
    <xsl:when test="name()='packedDecimalParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="packedDecimalParmGroup" >
    </xsl:when>
    <xsl:when test="name()='arrayOfPackedDecimalParm'">
    &lt;xs:element name="<xsl:value-of select="$completeName"/>" substitutionGroup="packedDecimalParmArrayGroup" >
    </xsl:when>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
