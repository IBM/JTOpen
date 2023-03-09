<?xml version="1.0"?> 

<!--///////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: xpcml_basic.xsl
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
////////////////////////////////////////////////////////////////////////////-->

 <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">

 <xsl:param name="xsdFileName" select="uBasicDef.xsd"/>

 <xsl:template match="xpcml">
   <xsl:text>
</xsl:text>
   <xpcml version="4.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation='{$xsdFileName}'>
   <xsl:apply-templates/>
   </xpcml>
 </xsl:template>

<!-- Copy comments over to result tree -->
<xsl:template match="comment()">
  <xsl:comment><xsl:value-of select="."/></xsl:comment>
</xsl:template>

 <xsl:template match='*'>   <!-- copy most over directly-->
 <xsl:copy>
    <xsl:for-each select="@*">  <!-- copy attributes of current node -->
       <xsl:copy/>
    </xsl:for-each>
 <xsl:apply-templates/>      <!-- process the child nodes -->
 </xsl:copy>
 </xsl:template>

<xsl:template match="stringParm[@name] | intParm[@name] | unsignedIntParm[@name] | hexBinaryParm[@name] | shortParm[@name] | unsignedShortParm[@name] | longParm[@name] | floatParm[@name] | doubleParm[@name] | zonedDecimalParm[@name] | packedDecimalParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
     <xsl:call-template name="setName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
       <xsl:with-param name="parmValue" select="$parmVal"/>
     </xsl:call-template>
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
    <xsl:choose>
      <xsl:when test="preceding::stringParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@ccsid)=string($ccsid) and string(@passMode)=string($passMode) and
           string(@trim)=string($trim) and string(@bytesPerChar)=string($bytesPerChar) and
           string(@offsetFrom)=string($offsetFrom) and string(@bidiStringType)=string($bidiStringType)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::stringParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@ccsid)=string($ccsid) and string(@passMode)=string($passMode) and
              string(@trim)=string($trim) and string(@bytesPerChar)=string($bytesPerChar) and
              string(@offsetFrom)=string($offsetFrom) and string(@bidiStringType)=string($bidiStringType)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='intParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::intParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.   -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::intParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='unsignedIntParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::unsignedIntParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.   -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::unsignedIntParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='hexBinaryParm'">
    <xsl:variable name="totalBytes" select="@totalBytes"/>
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="ccsid" select="@ccsid"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::hexBinaryParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and string(@ccsid)=string($ccsid) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::hexBinaryParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and string(@totalBytes)=string($totalBytes) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='shortParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::shortParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::shortParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='unsignedShortParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::unsignedShortParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::unsignedShortParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='longParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::longParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::longParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='doubleParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::doubleParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::doubleParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='floatParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::floatParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::floatParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='zonedDecimalParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="totalDigits" select="@totalDigits"/>
    <xsl:variable name="fractionDigits" select="@fractionDigits"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::zonedDecimalParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@totalDigits)=string($totalDigits) and string(@fractionDigits)=string($fractionDigits) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::zonedDecimalParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and string(@totalDigits)=string($totalDigits) and
              string(@offsetFrom)=string($offsetFrom) and string(@fractionDigits)=string($fractionDigits)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
  <xsl:when test="name()='packedDecimalParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="totalDigits" select="@totalDigits"/>
    <xsl:variable name="fractionDigits" select="@fractionDigits"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::packedDecimalParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and string(@totalDigits)=string($totalDigits)
           and string(@fractionDigits)=string($fractionDigits) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::packedDecimalParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and string(@totalDigits)=string($totalDigits) and
              string(@offsetFrom)=string($offsetFrom) and string(@fractionDigits)=string($fractionDigits)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
 </xsl:choose>
 <xsl:apply-templates/>
</xsl:template>


<xsl:template match="arrayOfStringParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
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
    <xsl:choose>
      <xsl:when test="preceding::arrayOfStringParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
           string(@ccsid)=string($ccsid) and string(@passMode)=string($passMode) and
           string(@trim)=string($trim) and string(@bytesPerChar)=string($bytesPerChar) and
           string(@offsetFrom)=string($offsetFrom) and string(@bidiStringType)=string($bidiStringType)
           and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:for-each select="(preceding::arrayOfStringParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@ccsid)=string($ccsid) and string(@passMode)=string($passMode) and
              string(@trim)=string($trim) and string(@bytesPerChar)=string($bytesPerChar) and
              string(@offsetFrom)=string($offsetFrom) and string(@bidiStringType)=string($bidiStringType)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setArrayName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:call-template name="setArrayName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>


<xsl:template match="arrayOfIntParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfIntParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfIntParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.   -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfIntParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>


<xsl:template match="arrayOfUnsignedIntParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfUnsignedIntParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfUnsignedIntParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.   -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfUnsignedIntParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>


<xsl:template match="arrayOfShortParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfShortParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfShortParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfShortParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>


<xsl:template match="arrayOfUnsignedShortParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfUnsignedShortParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfUnsignedShortParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfUnsignedShortParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>


<xsl:template match="arrayOfLongParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfLongParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfLongParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfLongParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>


<xsl:template match="arrayOfDoubleParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfDoubleParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfDoubleParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfDoubleParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>

<xsl:template match="arrayOfFloatParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfFloatParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfFloatParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfFloatParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>


<xsl:template match="arrayOfZonedDecimalParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfZonedDecimalParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="totalDigits" select="@totalDigits"/>
    <xsl:variable name="fractionDigits" select="@fractionDigits"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfZonedDecimalParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and
           string(@totalDigits)=string($totalDigits) and string(@fractionDigits)=string($fractionDigits) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfZonedDecimalParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and string(@totalDigits)=string($totalDigits) and
              string(@offsetFrom)=string($offsetFrom) and string(@fractionDigits)=string($fractionDigits)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>

<xsl:template match="arrayOfPackedDecimalParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfPackedDecimalParm'">
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="totalDigits" select="@totalDigits"/>
    <xsl:variable name="fractionDigits" select="@fractionDigits"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfPackedDecimalParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and string(@totalDigits)=string($totalDigits)
           and string(@fractionDigits)=string($fractionDigits) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined. We need to figure out correct -->
         <!-- parm name for this node... -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfPackedDecimalParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and string(@totalDigits)=string($totalDigits) and
              string(@offsetFrom)=string($offsetFrom) and string(@fractionDigits)=string($fractionDigits)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>


<xsl:template match="arrayOfHexBinaryParm[@name]">
 <xsl:variable name="nameWithUScore" select="concat(@name,'_')"/>
 <xsl:choose>
  <xsl:when test="not(preceding::*/@name=@name)">
     <xsl:call-template name="setArrayName">
       <xsl:with-param name="fullName" select="$nameWithUScore" />
     </xsl:call-template>
  </xsl:when>
  <xsl:when test="name()='arrayOfHexBinaryParm'">
    <xsl:variable name="totalBytes" select="@totalBytes"/>
    <xsl:variable name="parmName" select="@name"/>
    <xsl:variable name="passDirection" select="@passDirection"/>
    <xsl:variable name="length" select="@length"/>
    <xsl:variable name="count" select="@count"/>
    <xsl:variable name="ccsid" select="@ccsid"/>
    <xsl:variable name="passMode" select="@passMode"/>
    <xsl:variable name="offset" select="@offset"/>
    <xsl:variable name="offsetFrom" select="@offsetFrom"/>
    <xsl:variable name="outputSize" select="@outputSize"/>
    <xsl:variable name="minvrm" select="@minvrm"/>
    <xsl:variable name="maxvrm" select="@maxvrm"/>
    <xsl:choose>
      <xsl:when test="preceding::arrayOfHexBinaryParm[@name=$parmName and string(@length)=string($length) and
           string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
           and string(@offset)=string($offset) and string(@outputSize)=string($outputSize)
           and string(@passMode)=string($passMode) and string(@ccsid)=string($ccsid) and
           string(@offsetFrom)=string($offsetFrom) and string(@passDirection)=string($passDirection)]">
         <!-- Attributes match so this node has already been defined.  -->
         <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
         <xsl:for-each select="(preceding::arrayOfHexBinaryParm[@name=$parmName and string(@length)=string($length) and
              string(@count)=string($count) and string(@minvrm)=string($minvrm) and string(@maxvrm)=string($maxvrm)
              and string(@offset)=string($offset) and string(@outputSize)=string($outputSize) and
              string(@passMode)=string($passMode) and string(@totalBytes)=string($totalBytes) and
              string(@offsetFrom)=string($offsetFrom)
              and string(@passDirection)=string($passDirection)])[1]">
            <xsl:variable name="nodePos" select='count(preceding::*[@name=$parmName])'/>
            <xsl:call-template name="setName">
              <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
              <xsl:with-param name="nodeCount" select="$nodePos"/>
              <xsl:with-param name="parmValue" select="$parmVal"/>
            </xsl:call-template>
         </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
          <!-- Attributes don't match so add new parm with new number on end -->
          <xsl:variable name="parmVal"><xsl:value-of select="." /></xsl:variable>
          <xsl:call-template name="setName">
               <xsl:with-param name="fullName" select="concat($nameWithUScore,'_')"   />
               <xsl:with-param name="nodeCount" select='count(preceding::*[@name=$parmName])'/>
               <xsl:with-param name="parmValue" select="$parmVal"/>
          </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
 </xsl:choose>
</xsl:template>


<xsl:template name="setName">
  <xsl:param name="fullName">concat(@name,'_')</xsl:param>
  <xsl:param name="nodeCount"></xsl:param>
  <xsl:param name="parmValue"></xsl:param>
  <xsl:if test='$nodeCount=0'>
     <xsl:variable name="completeName" select="substring($fullName,1, string-length($fullName)-1)"/>
     <xsl:element name="{$completeName}">
        <xsl:value-of select="$parmValue"/>
     </xsl:element>
  </xsl:if>
  <xsl:if test='$nodeCount != 0'>
     <xsl:variable name="completeName" select="concat($fullName,$nodeCount)"/>
     <xsl:element name="{$completeName}">
         <xsl:value-of select="$parmValue"/>
     </xsl:element>
  </xsl:if>
</xsl:template>

<xsl:template name="setArrayName">
  <xsl:param name="fullName">concat(@name,'_')</xsl:param>
  <xsl:param name="nodeCount"></xsl:param>
  <xsl:if test='$nodeCount=0'>
     <xsl:variable name="completeName" select="substring($fullName,1, string-length($fullName)-1)"/>
     <xsl:element name="{$completeName}">
        <xsl:apply-templates/>
     </xsl:element>
  </xsl:if>
  <xsl:if test='$nodeCount != 0'>
     <xsl:variable name="completeName" select="concat($fullName,$nodeCount)"/>
     <xsl:element name="{$completeName}">
       <xsl:apply-templates/>
     </xsl:element>
  </xsl:if>
</xsl:template>

 <xsl:template match="stringParm[@name]/text()"/>
 <xsl:template match="intParm[@name]/text()"/>
 <xsl:template match="unsignedIntParm[@name]/text()"/>
 <xsl:template match="packedDecimalParm[@name]/text()"/>
 <xsl:template match="zonedDecimalParm[@name]/text()"/>
 <xsl:template match="floatParm[@name]/text()"/>
 <xsl:template match="shortParm[@name]/text()"/>
 <xsl:template match="unsignedShortParm[@name]/text()"/>
 <xsl:template match="hexBinaryParm[@name]/text()"/>
 <xsl:template match="longParm[@name]/text()"/>
 <xsl:template match="doubleParm[@name]/text()"/>
 <xsl:template match="hexBinaryParm[@name]/text()"/>

 <xsl:template match="//structParm/*[stringParm]/text()"/>
 <xsl:template match="//structParm/*[intParm]/text()"/>
 <xsl:template match="//structParm/*[unsignedIntParm]/text()"/>
 <xsl:template match="//structParm/*[hexBinaryParm]/text()"/>
 <xsl:template match="//structParm/*[shortParm]/text()"/>
 <xsl:template match="//structParm/*[unsignedShortParm]/text()"/>
 <xsl:template match="//structParm/*[longParm]/text()"/>
 <xsl:template match="//structParm/*[floatParm]/text()"/>
 <xsl:template match="//structParm/*[doubleParm]/text()"/>
 <xsl:template match="//structParm/*[zonedDecimalParm]/text()"/>
 <xsl:template match="//structParm/*[packedDecimalParm]/text()"/>

<!--
 <xsl:template match="//parameterList/*/text()"/>
 <xsl:template match="//parameterList/*[@name]/text()"/>
 <xsl:template match="//parameterList/*/text()"/>
-->

</xsl:stylesheet>
