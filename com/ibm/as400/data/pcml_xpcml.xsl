<?xml version="1.0"?> 

<!--///////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: pcml_xpcml.xsl
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
////////////////////////////////////////////////////////////////////////////-->

 <!-- XSLT transformation sheet for transforming PCML to XPCML. --> 

 <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"> 

 <xsl:output method="xml" indent="no" omit-xml-declaration="no"/> 

 <!-- Template for the most general case: Copy all attributes of current node, and then process the child nodes.
     Note that we can't simply do a full copy, since that will bypass any further template matching, and we
     end up simply copying the entire input document. -->

<xsl:template match="*">
    <xsl:copy>
     <xsl:for-each select="@*">  <!-- copy attributes of current node -->
       <xsl:copy/>
     </xsl:for-each>
     <xsl:apply-templates/>      <!-- process the child nodes --> 
   </xsl:copy>
 </xsl:template>

<!-- Copy comments over to result tree -->
<xsl:template match="comment()">
  <xsl:comment><xsl:value-of select="."/></xsl:comment>
</xsl:template>

<xsl:template match="pcml">
<xsl:text>
</xsl:text>
 <xpcml xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation='xpcml.xsd' >

          <xsl:attribute name="version">6.0</xsl:attribute>
    <xsl:apply-templates/>
  </xpcml>
</xsl:template>
  
<xsl:template match="program">
<xsl:text>
</xsl:text>
  <program>
      <xsl:copy-of select="@name"/>
      <xsl:copy-of select="@path"/>
      <xsl:copy-of select="@epccsid"/>
      <xsl:if test="@parseorder">  <!-- program has a 'parseorder' attribute -->
           <xsl:attribute name="parseOrder">
        	     <xsl:value-of select="@parseorder"/>
          </xsl:attribute>
      </xsl:if>
      <xsl:if test="@entrypoint">  <!-- program has a 'entrypoint' attribute -->
           <xsl:attribute name="entryPoint">
        	     <xsl:value-of select="@entrypoint"/>
          </xsl:attribute>
      </xsl:if>
      <xsl:if test="@threadsafe">  <!-- program has a 'threadsafe' attribute -->
           <xsl:attribute name="threadSafe">
        	     <xsl:value-of select="@threadsafe"/>
          </xsl:attribute>
      </xsl:if> 
      <xsl:if test="@returnvalue">  <!-- program has a 'returnvalue' attribute -->
           <xsl:attribute name="returnValue">
        	     <xsl:value-of select="@returnvalue"/>
          </xsl:attribute>
      </xsl:if> 
<xsl:text> 
</xsl:text>
 <parameterList>
     <xsl:apply-templates/>      <!-- process the child nodes -->
     </parameterList>
<xsl:text> 
</xsl:text>
  </program>
</xsl:template>


<xsl:template match="struct">
  <xsl:choose>
   <xsl:when test="@count and @count != '0'" >
     <arrayOfStruct>
      <xsl:copy-of select="@name"/>
      <xsl:copy-of select="@count"/>
      <xsl:copy-of select="@minvrm"/>
      <xsl:copy-of select="@maxvrm"/>
      <xsl:copy-of select="@offset"/>
      <xsl:if test="@offsetfrom">  <!-- struct has an 'offsetfrom' attribute -->
           <xsl:attribute name="offsetFrom">
        	     <xsl:value-of select="@offsetfrom"/>
          </xsl:attribute>
      </xsl:if>
      <xsl:if test="@outputsize">  <!-- struct has an 'outputsize' attribute -->
           <xsl:attribute name="outputSize">
        	     <xsl:value-of select="@outputsize"/>
          </xsl:attribute>
      </xsl:if>
      <xsl:if test="@usage">  <!-- the parm has a 'usage' attribute -->
         <xsl:attribute name="passDirection">
           <xsl:choose>
             <xsl:when test="@usage='input'">in</xsl:when>
             <xsl:when test="@usage='output'">out</xsl:when>
             <xsl:when test="@usage='inputoutput'">inout</xsl:when>
            <xsl:when test="@usage='inherit'">inherit</xsl:when>
           </xsl:choose>
         </xsl:attribute>
       </xsl:if>
      <struct_i>
      <xsl:apply-templates/>      <!-- process the child nodes -->
      </struct_i>
      </arrayOfStruct>
   </xsl:when>
  <xsl:otherwise>
     <struct>
      <xsl:copy-of select="@name"/>
      <xsl:copy-of select="@count"/>
      <xsl:copy-of select="@minvrm"/>
      <xsl:copy-of select="@maxvrm"/>
      <xsl:copy-of select="@offset"/>
      <xsl:if test="@offsetfrom">  <!-- struct has an 'offsetfrom' attribute -->
           <xsl:attribute name="offsetFrom">
        	     <xsl:value-of select="@offsetfrom"/>
          </xsl:attribute>
      </xsl:if>
      <xsl:if test="@outputsize">  <!-- struct has an 'outputsize' attribute -->
           <xsl:attribute name="outputSize">
        	     <xsl:value-of select="@outputsize"/>
          </xsl:attribute>
      </xsl:if>
      <xsl:if test="@usage">  <!-- the parm has a 'usage' attribute --> 
        <xsl:attribute name="passDirection">
           <xsl:choose>
             <xsl:when test="@usage='input'">in</xsl:when>
             <xsl:when test="@usage='output'">out</xsl:when>
             <xsl:when test="@usage='inputoutput'">inout</xsl:when>
             <xsl:when test="@usage='inherit'">inherit</xsl:when>
           </xsl:choose>
         </xsl:attribute>
       </xsl:if>
     <xsl:apply-templates/>      <!-- process the child nodes -->
  </struct>
 </xsl:otherwise>
</xsl:choose>
</xsl:template>

  <xsl:template match="data">
    <xsl:if test="@type='char'">
     <xsl:choose>
      <xsl:when test="@count and @count != '0'" >
       <arrayOfStringParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:copy-of select="@length"/>
         <xsl:copy-of select="@ccsid"/>
         <xsl:copy-of select="@trim"/>
          <xsl:if test="@bidistringtype">  <!-- the stringParm has a 'trim' attribute -->
             <xsl:attribute name="bidiStringType">
            	<xsl:value-of select="@bidistringtype"/>
             </xsl:attribute>
          </xsl:if>
         <xsl:if test="@chartype">  <!-- the stringParm has a 'charType' attribute -->
             <xsl:attribute name="bytesPerChar">
            	<xsl:value-of select="@chartype"/>
            </xsl:attribute>
         </xsl:if>
         <xsl:if test="@init">
            <xsl:if test="@init=''">
                <xsl:attribute name="isEmptyString">true</xsl:attribute>
            </xsl:if>
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
         </arrayOfStringParm>
      </xsl:when>
      <xsl:otherwise>
       <stringParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:copy-of select="@length"/>
         <xsl:copy-of select="@ccsid"/>
         <xsl:copy-of select="@trim"/>
         <xsl:if test="@bidistringtype">  <!-- the stringParm has a 'trim' attribute --> 
            <xsl:attribute name="bidiStringType">
             	<xsl:value-of select="@bidistringtype"/>
            </xsl:attribute>
         </xsl:if>
         <xsl:if test="@chartype">  <!-- the stringParm has a 'charType' attribute -->
             <xsl:attribute name="bytesPerChar">
            	<xsl:value-of select="@chartype"/>
            </xsl:attribute>
         </xsl:if>
         <xsl:if test="@init">
            <xsl:if test="@init=''">
                <xsl:attribute name="isEmptyString">true</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="@init"/>
         </xsl:if>
       </stringParm>
       </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

  <!-- QUESTION - Is hexBinary the right way to represent binary data? -->
  <xsl:if test="@type='byte'">
     <xsl:choose>
      <xsl:when test="@count and @count != '0'">
       <arrayOfHexBinaryParm>
         <xsl:call-template name="commonAttributes"/> 
          <xsl:if test="@length">  <!-- the hexBinaryParm has a 'length' attribute -->
             <xsl:attribute name="totalBytes">
             	<xsl:value-of select="@length"/>
             </xsl:attribute>
          </xsl:if>
          <xsl:if test="@ccsid">  <!-- the hexBinaryParm has a 'ccsid' attribute -->
             <xsl:attribute name="ccsid">
             	<xsl:value-of select="@ccsid"/>
             </xsl:attribute>
          </xsl:if>
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
        </arrayOfHexBinaryParm>
    </xsl:when>
    <xsl:otherwise>
      <hexBinaryParm>
         <xsl:call-template name="commonAttributes"/> 
          <xsl:if test="@length">  <!-- the hexBinaryParm has a 'length' attribute -->
             <xsl:attribute name="totalBytes">
            	<xsl:value-of select="@length"/>
             </xsl:attribute>
          </xsl:if>
          <xsl:if test="@ccsid">  <!-- the hexBinaryParm has a 'ccsid' attribute -->
             <xsl:attribute name="ccsid">
             	<xsl:value-of select="@ccsid"/>
             </xsl:attribute>
          </xsl:if>
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
     </hexBinaryParm>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:if>

  <!-- QUESTION - What do we want to do about unsigned types, i.e., precision specified -->
  <xsl:if test="@type='int'">
    <xsl:if test="@length='4'">  <!-- the intParm has a 'length' of 4 -->
      <xsl:if test="not(@precision='32')">   <!-- precision isn't 32 thus this is signed -->
       <xsl:choose>
        <xsl:when test="@count and @count != '0'">
      <arrayOfIntParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
        </arrayOfIntParm>
        </xsl:when>
        <xsl:otherwise>
      <intParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
      </intParm>
        </xsl:otherwise>
       </xsl:choose>       
      </xsl:if>
    </xsl:if>
  </xsl:if>
 <xsl:if test="@type='int'">
    <xsl:if test="@length='4'">  <!-- the intParm has a 'length' of 4 -->
      <xsl:if test="@precision='32'">   <!-- precision = 32 thus this is unsigned -->
       <xsl:choose>
        <xsl:when test="@count and @count != '0' ">
      <arrayOfUnsignedIntParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
             <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
       </arrayOfUnsignedIntParm>
        </xsl:when>
        <xsl:otherwise>
      <unsignedIntParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
      </unsignedIntParm>
       </xsl:otherwise>
      </xsl:choose>
     </xsl:if>
    </xsl:if>
  </xsl:if>
  <xsl:if test="@type='int'">
    <xsl:if test="@length='2'">  <!-- the intParmrm has a 'length' of 2 -->
     <xsl:if test="not(@precision='16')">   <!-- precision isn't 16 thus this is signed -->
       <xsl:choose>
        <xsl:when test="@count and @count != '0' ">
      <arrayOfShortParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
      </arrayOfShortParm>
        </xsl:when>
        <xsl:otherwise>
      <shortParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
      </shortParm>
        </xsl:otherwise>
       </xsl:choose>
     </xsl:if>
    </xsl:if>
  </xsl:if>
  <xsl:if test="@type='int'">
    <xsl:if test="@length='2'">  <!-- the intParmrm has a 'length' of 2 -->
     <xsl:if test="@precision='16'">   <!-- precision = 16 thus this is unsigned -->
       <xsl:choose>
        <xsl:when test="@count and @count != '0' ">
      <arrayOfUnsignedShortParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
      </arrayOfUnsignedShortParm>
        </xsl:when>
        <xsl:otherwise>
      <unsignedShortParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
      </unsignedShortParm>
         </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:if>
  </xsl:if>
 <xsl:if test="@type='int'">
    <xsl:if test="@length='8'">  <!-- the intParmrm has a 'length' of 2 -->
     <xsl:choose>
      <xsl:when test="@count and @count != '0' ">
      <arrayOfLongParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
      </arrayOfLongParm>
     </xsl:when>
     <xsl:otherwise>
     <longParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
     </longParm>
     </xsl:otherwise>
     </xsl:choose>
    </xsl:if>
  </xsl:if>
 <xsl:if test="@type='zoned'">
     <xsl:choose>
      <xsl:when test="@count and @count != '0' ">
       <arrayOfZonedDecimalParm>
         <xsl:call-template name="commonAttributes"/> 
          <xsl:if test="@length">  <!-- the zoned parm has a 'length' attribute -->
             <xsl:attribute name="totalDigits">
                <xsl:value-of select="@length"/>
             </xsl:attribute>
          </xsl:if>
          <xsl:if test="@precision">  <!-- the zoned parm has a 'precision' attribute -->
             <xsl:attribute name="fractionDigits">
            	<xsl:value-of select="@precision"/>
             </xsl:attribute>
          </xsl:if>
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
     </arrayOfZonedDecimalParm>
     </xsl:when>
     <xsl:otherwise>
      <zonedDecimalParm>
         <xsl:call-template name="commonAttributes"/> 
          <xsl:if test="@length">  <!-- the zoned parm has a 'length' attribute -->
             <xsl:attribute name="totalDigits">
            	<xsl:value-of select="@length"/>
             </xsl:attribute> 
          </xsl:if>
          <xsl:if test="@precision">  <!-- the zoned parm has a 'precision' attribute -->
             <xsl:attribute name="fractionDigits">
          	     <xsl:value-of select="@precision"/>
             </xsl:attribute>
          </xsl:if>
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
     </zonedDecimalParm>
     </xsl:otherwise>
    </xsl:choose>
   </xsl:if>
 <xsl:if test="@type='packed'">
     <xsl:choose>
      <xsl:when test="@count and @count != '0' ">
      <arrayOfPackedDecimalParm>
         <xsl:call-template name="commonAttributes"/> 
          <xsl:if test="@length">  <!-- the packed parm has a 'length' attribute -->
             <xsl:attribute name="totalDigits">
             	<xsl:value-of select="@length"/>
             </xsl:attribute>
          </xsl:if>
          <xsl:if test="@precision">  <!-- the packed parm has a 'precision' attribute -->
             <xsl:attribute name="fractionDigits">
            	<xsl:value-of select="@precision"/>
             </xsl:attribute>
          </xsl:if>
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
     </arrayOfPackedDecimalParm>
     </xsl:when>
     <xsl:otherwise>
      <packedDecimalParm>
         <xsl:call-template name="commonAttributes"/> 
          <xsl:if test="@length">  <!-- the packed parm has a 'length' attribute -->
             <xsl:attribute name="totalDigits">
            	<xsl:value-of select="@length"/>
             </xsl:attribute>
          </xsl:if>
          <xsl:if test="@precision">  <!-- the packed parm has a 'precision' attribute -->
             <xsl:attribute name="fractionDigits">
            	<xsl:value-of select="@precision"/>
             </xsl:attribute>
          </xsl:if>
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
     </packedDecimalParm>
     </xsl:otherwise>
    </xsl:choose>
  </xsl:if>

 <!-- Question - Float and doubles don't have precision, right? -->

 <xsl:if test="@type='float'">
    <xsl:if test="@length='4'">  <!-- the floatParm has a 'length' of 4 -->
    <xsl:choose>
      <xsl:when test="@count and @count != '0' ">
      <arrayOfFloatParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
      </arrayOfFloatParm>
      </xsl:when>
      <xsl:otherwise>
      <floatParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
      </floatParm>
      </xsl:otherwise>
     </xsl:choose>
    </xsl:if>
  </xsl:if>
  <xsl:if test="@type='float'">
    <xsl:if test="@length='8'">  <!-- the floatParm has a 'length' of 8 which maps to a double -->
    <xsl:choose>
      <xsl:when test="@count and @count != '0' ">
      <arrayOfDoubleParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
      </arrayOfDoubleParm>
      </xsl:when>
      <xsl:otherwise>
      <doubleParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
      </doubleParm>
      </xsl:otherwise>
     </xsl:choose>
    </xsl:if>
  </xsl:if>
  
  <xsl:if test="@type='date'">
    <xsl:choose>
      <xsl:when test="@count and @count != '0' ">
      <arrayOfDateParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:call-template name="dateAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
      </arrayOfDateParm>
      </xsl:when>
      <xsl:otherwise>
      <dateParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:call-template name="dateAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
         
      </dateParm>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:if>
  
  <xsl:if test="@type='time'">
    <xsl:choose>
      <xsl:when test="@count and @count != '0' ">
      <arrayOfTimeParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:call-template name="timeAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
      </arrayOfTimeParm>
      </xsl:when>
      <xsl:otherwise>
      <timeParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:call-template name="timeAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
         
      </timeParm>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:if>
  
  <xsl:if test="@type='timestamp'">
    <xsl:choose>
      <xsl:when test="@count and @count != '0' ">
      <arrayOfTimestampParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:variable name="count" select="@count"/>
            <xsl:call-template name="writeArrayElements">
              <xsl:with-param name="countVal" select="$count" />
              <xsl:with-param name="val" select="@init"  />
            </xsl:call-template>
         </xsl:if>
      </arrayOfTimestampParm>
      </xsl:when>
      <xsl:otherwise>
      <timestampParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:if test="@init">
            <xsl:value-of select="@init"/>
         </xsl:if>
         
      </timestampParm>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:if>
  
  
  
  
  <xsl:if test="@type='struct'">
    <xsl:choose>
      <xsl:when test="@count and @count != '0' ">
       <arrayOfStructParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:copy-of select="@struct"/>
       </arrayOfStructParm>
      </xsl:when>
      <xsl:otherwise>
       <structParm>
         <xsl:call-template name="commonAttributes"/> 
         <xsl:copy-of select="@struct"/>
       </structParm>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:if>
</xsl:template> 
 <!-- Named templates for copying groups of attributes -->
  <xsl:template name="commonAttributes">
    <xsl:copy-of select="@name"/>
    <xsl:copy-of select="@count"/>
    <xsl:copy-of select="@offset"/>
    <xsl:copy-of select="@minvrm"/>
    <xsl:copy-of select="@maxvrm"/>
     <xsl:if test="@usage">  <!-- the parm has a 'usage' attribute -->
       <xsl:attribute name="passDirection">
         <xsl:choose>
           <xsl:when test="@usage='input'">in</xsl:when>
           <xsl:when test="@usage='output'">out</xsl:when>
           <xsl:when test="@usage='inputoutput'">inout</xsl:when>
          <xsl:when test="@usage='inherit'">inherit</xsl:when>
         </xsl:choose>
       </xsl:attribute>
     </xsl:if>
    <xsl:if test="@passby">  <!-- the parm has a 'passby' attribute -->
        <xsl:attribute name="passMode">
            <xsl:value-of select="@passby"/>
        </xsl:attribute>
    </xsl:if>
    <xsl:if test="@offsetfrom">  <!-- the parm has an 'offsetfrom' attribute -->
        <xsl:attribute name="offsetFrom">
            <xsl:value-of select="@offsetfrom"/>
        </xsl:attribute>
    </xsl:if>
    <xsl:if test="@outputsize">  <!-- the parm has an 'outputsize' attribute -->
        <xsl:attribute name="outputSize">
            <xsl:value-of select="@outputsize"/>
        </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template name="dateAttributes">
     <xsl:if test="@dateformat">  <!-- the parm has a 'dateFormat' attribute -->
        <xsl:attribute name="dateFormat">
          <xsl:value-of select="@dateformat"/>
        </xsl:attribute>
     </xsl:if>
     <xsl:if test="@dateseparator">  <!-- the parm has a 'dateFormat' attribute -->
        <xsl:attribute name="dateSeparator">
          <xsl:value-of select="@dateseparator"/>
        </xsl:attribute>
     </xsl:if>
  </xsl:template>

  <xsl:template name="timeAttributes">
     <xsl:if test="@timeformat">  <!-- the parm has a 'timeformat' attribute -->
        <xsl:attribute name="timeFormat">
          <xsl:value-of select="@timeformat"/>
        </xsl:attribute>
     </xsl:if>
     <xsl:if test="@timeseparator">  <!-- the parm has a 'timeseparatorformat' attribute -->
         <xsl:attribute name="timeSeparator">
            <xsl:value-of select="@timeseparator"/>
         </xsl:attribute>
     </xsl:if>
  </xsl:template>



<xsl:template name="writeArrayElements">
  <xsl:param name="countVal"></xsl:param>
  <xsl:param name="val"></xsl:param>
      <xsl:element name="i"><xsl:value-of select="@init"/></xsl:element>
  <xsl:if test='number($countVal) > number(1)'>
     <xsl:call-template name="writeArrayElements">
     <xsl:with-param name="countVal" select="number($countVal) - number(1)" />
     <xsl:with-param name="val" select="@init"  />
     </xsl:call-template>
  </xsl:if>
</xsl:template>
     
 </xsl:stylesheet> 
