<?xml version="1.0"?> 

<!--///////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: gencmdhlp.xsl
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
////////////////////////////////////////////////////////////////////////////-->


<!DOCTYPE xsl:stylesheet [
<!-- entities for use in the generated output  -->
<!ENTITY amp    "&amp;">
<!ENTITY nbsp   "&#160;">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Declare named parameters (used for MRI strings used in generated UIM)  -->

<xsl:param name="_HELP"/>        
<xsl:param name="_HELP_FOR_COMMAND"/>                 
<xsl:param name="_INTRO_COMMAND_HELP"/>
<xsl:param name="_DESCRIBE_COMMAND"/>

<xsl:param name="_RESTRICTIONS_HEADING"/>
<xsl:param name="_RESTRICTION_AUT"/>        
<xsl:param name="_RESTRICTION_THREADSAFE"/>        
<xsl:param name="_LIST_SPECIAL_AUT"/>        
<xsl:param name="_LIST_OTHER_AUT"/>        
<xsl:param name="_LIST_THREADSAFE_RESTRICTIONS"/>        
<xsl:param name="_DESCRIBE_OTHER_RESTRICTION"/>        
<xsl:param name="_RESTRICTION_COMMENT"/>
        
<xsl:param name="_NO_PARAMETERS"/>
        
<xsl:param name="_EXAMPLES_HEADING"/>        
<xsl:param name="_EXAMPLE_1_TITLE"/>        
<xsl:param name="_DESCRIBE_EXAMPLE_1"/>        
<xsl:param name="_EXAMPLE_2_TITLE"/>
<xsl:param name="_DESCRIBE_EXAMPLE_2"/>        
<xsl:param name="_INTRO_EXAMPLE_HELP"/>
        
<xsl:param name="_ERROR_MESSAGES_HEADING"/>        
<xsl:param name="_ERROR_MESSAGES_COMMENT_1"/>        
<xsl:param name="_ERROR_MESSAGES_COMMENT_2"/>        
<xsl:param name="_ERROR_MESSAGES_COMMENT_3"/>
        
<xsl:param name="_HELP_FOR_PARAMETER"/>        
<xsl:param name="_INTRO_PARAMETER_HELP"/>
<xsl:param name="_DESCRIBE_PARAMETER"/>
<xsl:param name="_REQUIRED_PARAMETER"/>

<xsl:param name="_ELEMENT"/>
<xsl:param name="_QUALIFIER"/>
<xsl:param name="_VALUES_OTHER"/>              
<xsl:param name="_VALUES_OTHER_REPEAT"/>       
<xsl:param name="_VALUES_REPEAT"/>       
<xsl:param name="_VALUES_SINGLE"/>

<xsl:param name="_VALUE_CHARACTER"/>      
<xsl:param name="_VALUE_CL_VARIABLE_NAME"/>     
<xsl:param name="_VALUE_COMMAND_STRING"/>       
<xsl:param name="_VALUE_COMMUNICATIONS_NAME"/>  
<xsl:param name="_VALUE_DATE"/>                 
<xsl:param name="_VALUE_DECIMAL_NUMBER"/>       
<xsl:param name="_VALUE_GENERIC_NAME"/>         
<xsl:param name="_VALUE_HEX"/>            
<xsl:param name="_VALUE_INTEGER"/>              
<xsl:param name="_VALUE_LOGICAL"/>        
<xsl:param name="_VALUE_NAME"/>         
<xsl:param name="_VALUE_NOT_RESTRICTED"/>       
<xsl:param name="_VALUE_PATH_NAME"/>            
<xsl:param name="_VALUE_SIMPLE_NAME"/>          
<xsl:param name="_VALUE_TIME"/>                 
<xsl:param name="_VALUE_UNSIGNED_INTEGER"/>
     
<xsl:param name="_SPECIFY_VALUE"/>      
<xsl:param name="_SPECIFY_NAME"/>      
<xsl:param name="_SPECIFY_GENERIC_NAME"/>      
<xsl:param name="_SPECIFY_PATH_NAME"/>      
<xsl:param name="_SPECIFY_NUMBER"/>      
<xsl:param name="_SPECIFY_CL_VARIABLE_NAME"/>      
<xsl:param name="_SPECIFY_COMMAND_STRING"/>      
<xsl:param name="_SPECIFY_DATE"/>      
<xsl:param name="_SPECIFY_TIME"/>      

<xsl:param name="_MULTIPLE_PARAMETER_VALUES_ALLOWED"/> 
<xsl:param name="_MULTIPLE_ELEMENT_VALUES_ALLOWED"/> 
             
<xsl:param name="_DESCRIBE_PREDEFINED_PARAMETER_VALUE"/> 
<xsl:param name="_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/> 
<xsl:param name="_DESCRIBE_PARAMETER_DEFAULT"/> 
<xsl:param name="_DESCRIBE_PARAMETER_VALUE_WITH_RANGE"/> 


<!-- Declare the type of output file produced                           --> 
<xsl:output method="text" encoding="UTF-8" indent="no" />
            
<!-- Set variable that is the name of the command (*CMD) object.        -->
<xsl:variable name="CommandName" select="QcdCLCmd/Cmd/@CmdName"/>  

<!-- Set variable that is the name of the prompt message file (if specified) -->
<xsl:variable name="PromptFile" select="QcdCLCmd/Cmd/@PmtFile"/>  

<!-- Set variable for the prompt message file library (if PMTFILE specified) -->
<xsl:variable name="PromptFileLib" select="QcdCLCmd/Cmd/@PmtFileLib"/>  

<!-- Set variable that is the help ID for the command.  Normally this is 
     the same as the command name, but for 'alias' commands, it may be the
     name of the 'standard' command that it is an alias for. For example, 
     FTP is an alias for the STRTCPFTP command.                         -->
<xsl:variable name="CommandHelpID" select="QcdCLCmd/Cmd/@HlpID"/>

<!-- Set variable used to test if the command has one or more parameters.  -->  
<xsl:variable name="HasParameters" select="//Parm"/>

<!-- 
*********************************************************************************
* Main template to process <QcdCLCMD> element for the command description XML   *
* for one Control Language (CL) command.                                        *
*                                                                               *
* Note:  You will find two short XSL strings used very often in this stylesheet.*
*        These strings are used to improve the formatting, size, and readability* 
*        of the generated UIM source.  The two strings are:                     *
*                                                                               *
* (1)  <xsl:text>&#xa;</xsl:text>   <== used to insert a 'new line' character.  *
* (2)  <xsl:text/>  <== used to prevent XSL from inserting 'new line' character *
*                       or other whitespace characters.                         *
*                                                                               *
*********************************************************************************  -->
<xsl:template match="QcdCLCmd">
<xsl:text/>:pnlgrp<xsl:text/>
<xsl:if test="$PromptFile!=''">
  <xsl:text/> submsgf='<xsl:value-of select="$PromptFileLib"/>/<xsl:text/>
  <xsl:text/><xsl:value-of select="$PromptFile"/>'<xsl:text/>
</xsl:if>
<xsl:text/>.<xsl:text>&#xa;</xsl:text>
<xsl:text/>.************************************************************************
.*  <xsl:value-of select="$_HELP_FOR_COMMAND"/> <xsl:value-of select="Cmd/@CmdName"/>
.************************************************************************<xsl:text/>
<!-- Process the <Cmd> element found in the input command XML file.  
     The <Cmd> element processing will, in turn, process any <Parm> 
     elements in the command XML file.  The command may have no parms.      -->
     
<!-- Generate :HELP tags, using prompt message ID (if present) or literal text. -->
 
<xsl:text>&#xa;</xsl:text>:help name='<xsl:value-of select="Cmd/@CmdName"/>'.
<xsl:choose>

<xsl:when test="Cmd/@PromptMsgID!=''">
<xsl:text/>&amp;msg(<xsl:value-of select="Cmd/@PromptMsgID"/>). - <xsl:value-of select="$_HELP"/><xsl:text/>
:p.<xsl:value-of select="substring-before($_INTRO_COMMAND_HELP,'&amp;amp;1')"/>
&amp;msg(<xsl:value-of select="Cmd/@PromptMsgID"/>). (<xsl:value-of select="Cmd/@CmdName"/>)
<xsl:value-of select="substring-after($_INTRO_COMMAND_HELP,'&amp;amp;1')"/> <xsl:text/>
.* <xsl:value-of select="$_DESCRIBE_COMMAND"/>
</xsl:when>

<xsl:when test="Cmd/@Prompt!=''">
<xsl:text/><xsl:value-of select="Cmd/@Prompt"/> - <xsl:value-of select="$_HELP"/><xsl:text/>
:p.<xsl:value-of select="substring-before($_INTRO_COMMAND_HELP,'&amp;amp;1')"/>
<xsl:value-of select="Cmd/@Prompt"/> (<xsl:value-of select="Cmd/@CmdName"/>)
<xsl:value-of select="substring-after($_INTRO_COMMAND_HELP,'&amp;amp;1')"/> <xsl:text/>
.* <xsl:value-of select="$_DESCRIBE_COMMAND"/>
</xsl:when>

<xsl:otherwise>
<xsl:text/><xsl:value-of select="Cmd/@CmdName"/> - <xsl:value-of select="$_HELP"/><xsl:text/>
:p.<xsl:value-of select="substring-before($_INTRO_COMMAND_HELP,'&amp;amp;1')"/>
<xsl:value-of select="Cmd/@CmdName"/>
<xsl:value-of select="substring-after($_INTRO_COMMAND_HELP,'&amp;amp;1')"/> <xsl:text/>
.* <xsl:value-of select="$_DESCRIBE_COMMAND"/> 
</xsl:otherwise>
</xsl:choose>  
 
<xsl:apply-templates select="Cmd"/>

</xsl:template>

<xsl:template match="Cmd">
<!-- Generate a Restrictions heading and describe the types of items that should 
     be listed under this heading.                                          -->
<xsl:text>&#xa;</xsl:text>:p.:hp2.<xsl:value-of select="$_RESTRICTIONS_HEADING"/>::ehp2.<xsl:text/>
:ul.<xsl:text/>
:li.
<xsl:value-of select="$_RESTRICTION_AUT"/>
.* <xsl:value-of select="$_LIST_SPECIAL_AUT"/><xsl:text/>
:li.
<xsl:value-of select="$_RESTRICTION_AUT"/>
.* <xsl:value-of select="$_LIST_OTHER_AUT"/><xsl:text/>
:li.
<xsl:value-of select="$_RESTRICTION_THREADSAFE"/>
.* <xsl:value-of select="$_LIST_THREADSAFE_RESTRICTIONS"/><xsl:text/>
:li.
... 
.* <xsl:value-of select="$_DESCRIBE_OTHER_RESTRICTION"/><xsl:text/>
.* <xsl:value-of select="$_RESTRICTION_COMMENT"/><xsl:text/>
:eul.<xsl:text/>

<!-- If command has no parameters, add sentence to command-level help.               -->
<xsl:if test="count(child::Parm)=0">
  <xsl:text>&#xa;</xsl:text>:p.<xsl:value-of select="$_NO_PARAMETERS"/><xsl:text/>
</xsl:if>

<!-- Generate :ehelp tag to close the command-level help section.                    -->
<xsl:text>&#xa;</xsl:text>:ehelp.<xsl:text/>
 
<!-- Process all <Parm> elements in the input command XML file.  Each <Parm> will 
     generate a new help section.  The layout of the help section will be determined 
     by the parameter definition, including default value, single values, special values,
     regular values, whether the parameter choices are restricted, and whether the 
     parameter is a scalar, simple array, element list, or qualifier list.           -->
     
<xsl:apply-templates select="Parm"><xsl:sort data-type="number" select="@PosNbr"/>
</xsl:apply-templates>

<!-- Generate an Examples section skeleton showing two examples.               --> 
<xsl:text>&#xa;</xsl:text>
<xsl:text/>.**************************************************
.*
.* <xsl:value-of select="substring-before($_EXAMPLES_HEADING,'&amp;amp;1')"/> <xsl:value-of select="$CommandName"/>
<xsl:value-of select="substring-after($_EXAMPLES_HEADING,'&amp;amp;1')"/> <xsl:text/>
.*
.**************************************************<xsl:text>&#xa;</xsl:text>
<xsl:text/>:help name='<xsl:value-of select="$CommandName"/>/COMMAND/EXAMPLES'.
<xsl:value-of select="substring-before($_EXAMPLES_HEADING,'&amp;amp;1')"/> <xsl:value-of select="$CommandName"/>
<xsl:value-of select="substring-after($_EXAMPLES_HEADING,'&amp;amp;1')"/> - <xsl:value-of select="$_HELP"/><xsl:text/> 
:xh3.<xsl:value-of select="substring-before($_EXAMPLES_HEADING,'&amp;amp;1')"/> <xsl:value-of select="$CommandName"/>
<xsl:value-of select="substring-after($_EXAMPLES_HEADING,'&amp;amp;1')"/> <xsl:text/> 

:p.:hp2.<xsl:value-of select="$_EXAMPLE_1_TITLE"/>:ehp2.  
:xmp.
<xsl:value-of select="$CommandName"/>  KWD1(PARMVAL1)  
:exmp.
:p.<xsl:value-of select="$_INTRO_EXAMPLE_HELP"/>
.* <xsl:value-of select="$_DESCRIBE_EXAMPLE_1"/><xsl:text/>     
.*
:p.:hp2.<xsl:value-of select="$_EXAMPLE_2_TITLE"/>:ehp2.  
:xmp.
<xsl:value-of select="$CommandName"/>  KWD1(PARMVAL1)  KWD2(PARMVAL2)    
             KWD3(PARMVAL3)
:exmp.
:p.<xsl:value-of select="$_INTRO_EXAMPLE_HELP"/>
.* <xsl:value-of select="$_DESCRIBE_EXAMPLE_2"/><xsl:text/>     
:ehelp.<xsl:text>&#xa;</xsl:text>

<!-- Generate an Error Messages section skeleton.                         --> 
<xsl:text/>.**************************************************
.*
.* <xsl:value-of select="substring-before($_ERROR_MESSAGES_HEADING,'&amp;amp;1')"/> <xsl:value-of select="$CommandName"/>
<xsl:value-of select="substring-after($_ERROR_MESSAGES_HEADING,'&amp;amp;1')"/> <xsl:text/>
.*
.**************************************************<xsl:text>&#xa;</xsl:text>
<xsl:text/>:help name='<xsl:value-of select="$CommandName"/>/ERROR/MESSAGES'.
&amp;msg(CPX0005,QCPFMSG). <xsl:value-of select="$CommandName"/> - <xsl:value-of select="$_HELP"/>
:xh3.&amp;msg(CPX0005,QCPFMSG). <xsl:value-of select="$CommandName"/>
:p.:hp3.*ESCAPE &amp;msg(CPX0006,QCPFMSG).:ehp3.
<xsl:text/>.*******************************************************************************
.* <xsl:value-of select="$_ERROR_MESSAGES_COMMENT_1"/> 
.* <xsl:value-of select="$_ERROR_MESSAGES_COMMENT_2"/> 
.* <xsl:value-of select="$_ERROR_MESSAGES_COMMENT_3"/> 
.*******************************************************************************<xsl:text/>
:DL COMPACT.
:DT.CPF9801
:DD.&amp;MSG(CPF9801,QCPFMSG,nosub).
:DT.CPF9802
:DD.&amp;MSG(CPF9802,QCPFMSG,nosub).
:DT.CPF9803
:DD.&amp;MSG(CPF9803,QCPFMSG,nosub).
:DT.CPF9807
:DD.&amp;MSG(CPF9807,QCPFMSG,nosub).
:DT.CPF9808
:DD.&amp;MSG(CPF9808,QCPFMSG,nosub).
:DT.CPF9810
:DD.&amp;MSG(CPF9810,QCPFMSG,nosub).
:DT.CPF9811
:DD.&amp;MSG(CPF9811,QCPFMSG,nosub).
:DT.CPF9812
:DD.&amp;MSG(CPF9812,QCPFMSG,nosub).
:DT.CPF9820
:DD.&amp;MSG(CPF9820,QCPFMSG,nosub).
:DT.CPF9830
:DD.&amp;MSG(CPF9830,QCPFMSG,nosub).
:DT.CPF9899
:DD.&amp;MSG(CPF9899,QCPFMSG,nosub).
:EDL.
:ehelp.<xsl:text>&#xa;</xsl:text>

<!-- Generate :epnlgrp. tag to end the panel group source file.                -->
<xsl:text/>:epnlgrp.       
  
</xsl:template>                 


<!-- Template to process one <Parm> element to create one parameter help section.
     The help section is for entire parameter, including any child elements 
     (e.g. <Elem> or <Qual> or (SngVal> or <SpcVal> or <Values> elements).        -->
<xsl:template match="Parm">

  <!-- Check that this <Parm> element isn't a constant (constants don't have PosNbr attribute) -->                         
  <xsl:if test="@PosNbr!=''" >                             
  
    <xsl:text>&#xa;</xsl:text>
    <xsl:text/>.*******************************************<xsl:text>&#xa;</xsl:text>
    <xsl:text/>.*   <xsl:value-of select="$_HELP_FOR_PARAMETER"/> <xsl:value-of select="@Kwd"/><xsl:text>&#xa;</xsl:text>
    <xsl:text/>.*******************************************<xsl:text>&#xa;</xsl:text>
    <xsl:text/>:help name='<xsl:value-of select="$CommandName"/>/<xsl:value-of select="@Kwd"/>'.<xsl:text/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:choose>
      <xsl:when test="@PromptMsgID!=''">
        <xsl:text/>&amp;msg(<xsl:value-of select="@PromptMsgID"/>). (<xsl:value-of select="@Kwd"/>) - <xsl:value-of select="$_HELP"/><xsl:text/>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text/>:xh3.&amp;msg(<xsl:value-of select="@PromptMsgID"/>). (<xsl:value-of select="@Kwd"/>)<xsl:text/>
      </xsl:when>
      <xsl:when test="@Prompt!=''">
        <xsl:text/><xsl:value-of select="@Prompt"/> (<xsl:value-of select="@Kwd"/>) - <xsl:value-of select="$_HELP"/><xsl:text/>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text/>:xh3.<xsl:value-of select="@Prompt"/> (<xsl:value-of select="@Kwd"/>)<xsl:text/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text/><xsl:value-of select="@Kwd"/> - <xsl:value-of select="$_HELP"/><xsl:text/>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text/>:xh3.<xsl:value-of select="@Kwd"/><xsl:text/>
      </xsl:otherwise>
    </xsl:choose>  
    <xsl:text>&#xa;</xsl:text>
    <xsl:text/>:p.<xsl:value-of select="$_INTRO_PARAMETER_HELP"/><xsl:text>&#xa;</xsl:text>
    <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_PARAMETER"/><xsl:text/>
    
    <!-- Identify parameter as being required if Min>0                         -->
      <xsl:choose>
       <xsl:when test="@Min>0">
         <xsl:text>&#xa;</xsl:text>
         <xsl:text/>:p.<xsl:value-of select="$_REQUIRED_PARAMETER"/><xsl:text/>
        </xsl:when>
       <xsl:otherwise/>
      </xsl:choose>  
          
      <!-- Process any <SngVal> elements for the parameter  -->
      <xsl:apply-templates select="SngVal"/>
      
      <!-- If the MAX value is greater then 1, indicate repetitions allowed    -->
      <xsl:if test="@Max>1">
        <xsl:choose>
          <xsl:when test="count(child::SngVal)>0">
            <xsl:text>&#xa;</xsl:text>
            <xsl:text/>:p.:hp2.<xsl:value-of select="substring-before($_VALUES_OTHER_REPEAT,'&amp;amp;1')"/>
            <xsl:value-of select="@Max"/>
            <xsl:value-of select="substring-after($_VALUES_OTHER_REPEAT,'&amp;amp;1')"/>:ehp2.<xsl:text/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>&#xa;</xsl:text>
            <xsl:text/>:p.<xsl:value-of select="substring-before($_MULTIPLE_PARAMETER_VALUES_ALLOWED,'&amp;amp;1')"/>
            <xsl:value-of select="@Max"/>
            <xsl:value-of select="substring-after($_MULTIPLE_PARAMETER_VALUES_ALLOWED,'&amp;amp;1')"/><xsl:text/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      
      <xsl:if test="@Type!='' and @Type!='ELEM' and @Type!='QUAL'">
        <xsl:text>&#xa;</xsl:text>
        <xsl:text/>:parml.<xsl:text/>
      </xsl:if>
        
      <!-- If there is a default and no child element has a Val attribute equal 
           to the default, add default here as a bold and underlined value.     -->        
      <xsl:if test="@Dft!=''">
        <xsl:choose>
          <xsl:when test="descendant::Value/@Val=@Dft"/> 
          <xsl:otherwise>
            <xsl:text>&#xa;</xsl:text>
            <xsl:text/>:pt.:pk def.<xsl:value-of select="@Dft"/>:epk.<xsl:text/>
            <xsl:text>&#xa;</xsl:text>
            <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
            <xsl:text/>... <xsl:text>&#xa;</xsl:text>
            <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_PARAMETER_DEFAULT"/><xsl:text/>  
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>

      <!-- Process all <SpcVal> and <Values> child elements for this <Parm> element.    -->
      <xsl:apply-templates select="SpcVal"/>
      <xsl:apply-templates select="Values"/>
      
      <!-- If value is restricted (SNGVAL, SPCVAL,and VALUES), don't show the parameter type.
           If value must be within a range, show range instead of parameter type.
           If not restricted values or restricted range, call GenType template to show the 
           parameter type in italics.                                         -->        
      
      <xsl:choose>
      <xsl:when test="@Rstd='YES'"/>
      <xsl:when test="@RangeMinVal!=''">
        <xsl:text>&#xa;</xsl:text>:pt.:pv.<xsl:value-of select="@RangeMinVal"/>-<xsl:value-of select="@RangeMaxVal"/><xsl:text/>:epv.<xsl:text/>
        <xsl:text>&#xa;</xsl:text>:pd.<xsl:text>&#xa;</xsl:text>
        <xsl:text/>... <xsl:text>&#xa;</xsl:text>
        <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_PARAMETER_VALUE_WITH_RANGE"/><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type!='' and @Type!='ELEM' and @Type!='QUAL'">
        <xsl:call-template name="GenType"/>
      </xsl:when>
      </xsl:choose>

    <xsl:if test="@Type!='' and @Type!='ELEM' and @Type!='QUAL'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:eparml.<xsl:text/>
    </xsl:if>  
  
  <!-- Process all child Qual and Elem elements for the Parm element (may be none).   -->
  <xsl:apply-templates select="Qual"/>
  <xsl:apply-templates select="Elem"/>
                                                                                        
  <xsl:text>&#xa;</xsl:text>
  <xsl:text/>:ehelp.<xsl:text/>                                                                                          
  
  </xsl:if> 
</xsl:template>
                                                                                        
<!-- Template to process one <Elem> element to create one element description.        -->  
<xsl:template match="Elem">

  <!-- Check that this <Elem> element isn't a constant or a zero-element structure    -->                                
  <xsl:if test="not(@Constant) and @Type!='ZEROELEM'">                         
  
    <xsl:variable name="DoIndent" select="count(ancestor::Elem)"/>
     
    <!-- Test for nested Elem and generate extra :parml tag if first element.  -->
    <xsl:if test="$DoIndent!=0 and position()=1">
      <xsl:text>&#xa;</xsl:text>:parml.<xsl:text/>
    </xsl:if>  
    
    <xsl:text>&#xa;</xsl:text>
  
    <!-- If nested Elem, generate :pt. and :pd. ahead of :p. for heading.    -->
    <xsl:if test="$DoIndent!=0">
      <xsl:text/>:pt.:pd.<xsl:text/>
    </xsl:if>
    
    <!-- Identify the element number and put the prompt text in a heading      -->
    <xsl:text/>:p.:hp2.<xsl:value-of select="$_ELEMENT"/> <xsl:number count="Elem"/>: <xsl:text/>
    <xsl:choose>
      <xsl:when test="@PromptMsgID!=''">
        <xsl:text/>&amp;msg(<xsl:value-of select="@PromptMsgID"/>).
      </xsl:when>
      <xsl:when test="@Prompt!=''">
        <xsl:text/><xsl:value-of select="@Prompt"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
    <xsl:text/>:ehp2.<xsl:text/>  
      
      <!-- Process any <SngVal> elements for the Elem element                  -->
      <xsl:apply-templates select="SngVal"/>
      
      <!-- If the MAX value is greater then 1, indicate repetitions allowed    -->
      <xsl:if test="@Max>1">
        <xsl:choose>
          <xsl:when test="count(child::SngVal)>0">
            <xsl:text>&#xa;</xsl:text>
            <xsl:text/>:p.:hp2.<xsl:value-of select="substring-before($_VALUES_OTHER_REPEAT,'&amp;amp;1')"/>
            <xsl:value-of select="@Max"/>
            <xsl:value-of select="substring-after($_VALUES_OTHER_REPEAT,'&amp;amp;1')"/>:ehp2.<xsl:text/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>&#xa;</xsl:text>
            <xsl:text/>:p.<xsl:value-of select="substring-before($_MULTIPLE_ELEMENT_VALUES_ALLOWED,'&amp;amp;1')"/>
            <xsl:value-of select="@Max"/>
            <xsl:value-of select="substring-after($_MULTIPLE_ELEMENT_VALUES_ALLOWED,'&amp;amp;1')"/><xsl:text/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      
      <xsl:if test="@Type!='' and @Type!='ELEM' and @Type!='QUAL'">
        <xsl:text>&#xa;</xsl:text>
        <xsl:text/>:parml.<xsl:text/>
      </xsl:if>  
    
      <!-- If there is a default and no child element has a Val attribute equal 
           to the default, add default here as a bold and underlined value.     -->        
      <xsl:if test="@Dft!=''">
        <xsl:choose>
          <xsl:when test="descendant::Value/@Val=@Dft"/> 
          <xsl:otherwise>
            <xsl:text>&#xa;</xsl:text>
            <xsl:text/>:pt.:pk def.<xsl:value-of select="@Dft"/>:epk.<xsl:text/>
            <xsl:text>&#xa;</xsl:text>
            <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
            <xsl:text/>... <xsl:text>&#xa;</xsl:text>
            <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_PARAMETER_DEFAULT"/><xsl:text/>  
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>

      <!-- Process all SpcVal, Values, and ChoicePgmValues child elements for this Parm.  -->
      <xsl:apply-templates select="SpcVal"/>
      <xsl:apply-templates select="Values"/>
      <xsl:apply-templates select="ChoicePgmValues"/>
      
      <!-- If value is restricted (SNGVAL, SPCVAL,and VALUES), don't show the parameter type.
           If value must be within a range, show range instead of parameter type.
           If not restricted values or restricted range, call GenType template to show the 
           parameter type in italics.                                         -->        
      <xsl:choose>
      <xsl:when test="@Rstd='YES'"/>
      <xsl:when test="@RangeMinVal!=''">
        <xsl:text>&#xa;</xsl:text>:pt.:pv.<xsl:value-of select="@RangeMinVal"/>-<xsl:value-of select="@RangeMaxVal"/><xsl:text/>:epv.<xsl:text/>
        <xsl:text>&#xa;</xsl:text>:pd.<xsl:text>&#xa;</xsl:text>
        <xsl:text/>... <xsl:text>&#xa;</xsl:text>
        <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_PARAMETER_VALUE_WITH_RANGE"/><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type!='' and @Type!='ELEM' and @Type!='QUAL'">
        <xsl:call-template name="GenType"/>
      </xsl:when>
      </xsl:choose>

    <xsl:if test="@Type!='' and @Type!='ELEM' and @Type!='QUAL'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:eparml.<xsl:text/>
    </xsl:if>  
      
    <!-- Process all child Qual and Elem elements for the Elem element (may be none).   -->
    <xsl:apply-templates select="Qual"/>
    <xsl:apply-templates select="Elem"/>
    
    <!-- If this was the last element of a nested set, generate :eparml tag.          -->
    <xsl:if test="$DoIndent!=0 and position()=last()">
      <xsl:text>&#xa;</xsl:text>:eparml.<xsl:text/>
    </xsl:if>

  </xsl:if>    
</xsl:template>


<!-- Template to process one <Qual> element to create one row in the 
       command syntax summary table.                                           -->
<xsl:template match="Qual">

  <!-- Check that this <Qual> element isn't a constant                         -->
  <xsl:if test="not(@Constant)">                       
      
    <!-- Test for nested Qual and generate extra :parml tag if first qualifier.  -->
    <xsl:variable name="DoIndent" select="count(ancestor::Elem)"/>
    
    <xsl:if test="$DoIndent!=0 and position()=1">
      <xsl:text>&#xa;</xsl:text>:parml.<xsl:text/>
    </xsl:if>  
    
    <xsl:text>&#xa;</xsl:text>
                                                                          
    <!-- If nested Qual, generate :pt. and :pd. ahead of :p. for heading.    -->
    <xsl:if test="$DoIndent!=0">
      <xsl:text/>:pt.:pd.<xsl:text/>
    </xsl:if>
    
    <!-- Identify the qualifier number and put the prompt text in a heading      -->
    <xsl:text/>:p.:hp2.<xsl:value-of select="$_QUALIFIER"/> <xsl:number count="Qual"/>: <xsl:text/>
    <xsl:choose>
      <xsl:when test="position()=1">
        <xsl:choose>
          <xsl:when test="../@PromptMsgID!=''">
            <xsl:text/>&amp;msg(<xsl:value-of select="../@PromptMsgID"/>).<xsl:text/>
          </xsl:when>
          <xsl:when test="../@Prompt!=''">
            <xsl:text/><xsl:value-of select="../@Prompt"/><xsl:text/>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>  
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="@PromptMsgID!=''">
            <xsl:text/>&amp;msg(<xsl:value-of select="@PromptMsgID"/>).<xsl:text/>
          </xsl:when>
          <xsl:when test="@Prompt!=''">
            <xsl:text/><xsl:value-of select="@Prompt"/><xsl:text/>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>  
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text/>:ehp2.<xsl:text/>
    
    <xsl:text>&#xa;</xsl:text>
    <xsl:text/>:parml.<xsl:text/>
    
    <!-- If there is a default and no child element has a Val attribute equal 
         to the default, add default here as a bold and underlined value.     -->        
    <xsl:if test="@Dft!=''">
      <xsl:choose>
         <xsl:when test="descendant::Value/@Val=@Dft"/> 
         <xsl:otherwise>
           <xsl:text/>:pt.:pk def.<xsl:value-of select="@Dft"/>:epk.<xsl:text/>
           <xsl:text>&#xa;</xsl:text>
           <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
           <xsl:text/>... <xsl:text>&#xa;</xsl:text>
           <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_PARAMETER_DEFAULT"/><xsl:text/>  
         </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

      <!-- Process all SpcVal, Values, and ChoicePgmValues child elements for this Qual.  -->
      <xsl:apply-templates select="SpcVal"/>
      <xsl:apply-templates select="Values"/>
      
      <!-- If value is restricted (to SPCVAL and VALUES values), don't show the parameter type.
           If value must be within a range, show range instead of parameter type.
           If not restricted values or restricted range, call GenType template to show the 
           parameter type in italics.                                         -->        
      
      <xsl:choose>
      <xsl:when test="@Rstd='YES'"/>
      <xsl:when test="@RangeMinVal!=''">
        <xsl:text>&#xa;</xsl:text>:pt.:pv.<xsl:value-of select="@RangeMinVal"/>-<xsl:value-of select="@RangeMaxVal"/><xsl:text/>:epv.<xsl:text/>
        <xsl:text>&#xa;</xsl:text>:pd.<xsl:text>&#xa;</xsl:text>
        <xsl:text/>... <xsl:text>&#xa;</xsl:text>
        <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_PARAMETER_VALUE_WITH_RANGE"/><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type!=''">
        <xsl:call-template name="GenType"/>
      </xsl:when>
      </xsl:choose>

    <xsl:text>&#xa;</xsl:text>
    <xsl:text/>:eparml.<xsl:text/>
    
    <!-- If this was the last qualifier of a nested set, generate :eparml tag.          -->
    <xsl:if test="$DoIndent!=0 and position()=last()">
      <xsl:text>&#xa;</xsl:text>:eparml.<xsl:text/>
    </xsl:if>

  </xsl:if> 
</xsl:template>


<!-- Subroutine template to generate the input field type for Choices     -->
<xsl:template  name="GenType">
   <xsl:choose>
      
      <xsl:when test="@Type='SNAME'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_SIMPLE_NAME"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_NAME"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='PNAME'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_PATH_NAME"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_PATH_NAME"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='NAME'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_NAME"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_NAME"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='CNAME'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_COMMUNICATIONS_NAME"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_NAME"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='GENERIC'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_GENERIC_NAME"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_GENERIC_NAME"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_NAME"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_NAME"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='INT2' or @Type='INT4'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_INTEGER"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_NUMBER"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='UINT2' or @Type='UINT4'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_UNSIGNED_INTEGER"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_NUMBER"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='DEC'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_DECIMAL_NUMBER"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_NUMBER"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='CMD' or @Type='CMDSTR'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_COMMAND_STRING"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_COMMAND_STRING"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='LGL'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_LOGICAL"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_VALUE"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='CHAR'">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_CHARACTER"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_VALUE"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='VARNAME'">      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_CL_VARIABLE_NAME"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_CL_VARIABLE_NAME"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='DATE'">      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_DATE"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_DATE"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='TIME'">      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_TIME"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_TIME"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='HEX'">
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_HEX"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_VALUE"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:when test="@Type='X'">
      <xsl:text/>:pt.:pv.<xsl:value-of select="$_VALUE_NOT_RESTRICTED"/>:epv.<xsl:text/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pd.<xsl:text>&#xa;</xsl:text>
      <xsl:text/><xsl:value-of select="$_SPECIFY_VALUE"/><xsl:text>&#xa;</xsl:text>
      <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_USERDEFINED_PARAMETER_VALUE"/><xsl:text/>
      </xsl:when>
      
      <xsl:otherwise/>
      </xsl:choose>
</xsl:template>         


<!-- Template to handle <SngVal> element for <Parm> or <Elem> element     -->
<xsl:template match="SngVal">
  <xsl:text>&#xa;</xsl:text>:p.:hp2.<xsl:value-of select="$_VALUES_SINGLE"/>:ehp2.<xsl:text/>
  <xsl:text>&#xa;</xsl:text>:parml.<xsl:text/>
  <xsl:apply-templates select="Value"/>
  <xsl:text>&#xa;</xsl:text>:eparml.<xsl:text/>
</xsl:template>   


<!-- Template to handle <SpcVal> element for <Parm>,<Elem>, or <Qual> element     -->
<xsl:template match="SpcVal">
  <xsl:apply-templates select="Value"/>
</xsl:template>


<!-- Template to handle <Values> element for <Parm>,<Elem>, or <Qual> element     -->
<xsl:template match="Values">
  <xsl:apply-templates select="Value"/>
</xsl:template>


<!-- Template to handle <Value> element for <SngVal>,<SpcVal>, or <Values> element -->
<xsl:template match="Value">
  <!-- If this value is the default show it as bold and underlined    -->
  <xsl:choose>
    <xsl:when test="../../@Dft=@Val">
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pk def.<xsl:value-of select="@Val"/>:epk.<xsl:text/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>&#xa;</xsl:text>
      <xsl:text/>:pt.:pk.<xsl:value-of select="@Val"/>:epk.<xsl:text/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:text>&#xa;</xsl:text>
  <xsl:text/>:pd.<xsl:text/>
  <xsl:text>&#xa;</xsl:text>... <xsl:text>&#xa;</xsl:text>
  <xsl:text/>.* <xsl:value-of select="$_DESCRIBE_PREDEFINED_PARAMETER_VALUE"/><xsl:text/> 
</xsl:template>


</xsl:stylesheet>
