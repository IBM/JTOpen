<?xml version="1.0"?> 

<!--////////////////////////////////////////////////////////////////////
//
// LICENSED MATERIALS - PROPERTY OF IBM
// 5722-JC1
// 5722-SS1
// (C) Copyright IBM Corp. 2002
// All Rights Reserved
// US Government Users Restricted Rights - Use,   
// duplication or disclosure restricted by 
// GSA ADP Schedule Contract with IBM Corp. 
//
////////////////////////////////////////////////////////////////////////-->

<!DOCTYPE xsl:stylesheet [
<!-- entities for use in the generated output  -->
<!ENTITY amp    "&amp;">
<!ENTITY nbsp   "&#160;">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Declare named parameters and the default values used if not passed in  -->
<xsl:param name="ThreadSafe" select="0"/>
<xsl:param name="WhereAllowed" select="000000000000000"/>
<xsl:param name="CommandHelp" select="__NO_HELP"/>
<xsl:param name="ShowChoicePgmValues" select="0"/> <!-- Default=Don't show CHOICEPGM values -->

<!-- Declare the type of output file produced 
     (currently HTML, might change to XML)                           -->
<xsl:output method="html" encoding="iso-8859-1" indent="no" />
            
<!-- Set the document variable once, so we don't re-parse the CommandHelp
     document every time we would've encountered a document(...) call. -->
<xsl:variable name="CommandHelpDocument" select="document($CommandHelp)"/>

<!-- Set variable that is the name of the command (*CMD) object.        -->
<xsl:variable name="CommandName" select="QcdCLCmd/Cmd/@CmdName"/>  

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
*        of the generated HTML source.  The two strings are:                    *
*                                                                               *
* (1)  <xsl:text>&#xa;</xsl:text>   <== used to insert a 'new line' character.  *
* (2)  <xsl:text/>  <== used to prevent XSL from inserting 'new line' character.*
*                                                                               *
*********************************************************************************
                                                                            -->
<xsl:template match="QcdCLCmd">

<!-- Generate the DOCTYPE element at the beginning of the output file 
      (required by chkpii tool)                                             -->   
  <xsl:text disable-output-escaping="yes">
  &#x3c;</xsl:text><xsl:text>&#x21;</xsl:text>doctype html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"<xsl:text disable-output-escaping="yes">&#x3e;
  </xsl:text>

<!-- Generate the <html>, <head>, <title>, </head>, and <body> elements.
     Process the <cmd> element found in the input command XML file, which
     will cause the command documentation to be generated as XHTML.
     Finally, generate the closing </body> and </html> elements.            --> 
  <html><xsl:text/>
  <xsl:text>&#xa;</xsl:text>
  <head><xsl:text>&#xa;</xsl:text>
  <title><xsl:value-of select="Cmd/@Prompt"/>&nbsp;&nbsp;(<xsl:value-of select="Cmd/@CmdName"/>)</title>
  <xsl:text>&#xa;</xsl:text>
  </head>
  <xsl:text>&#xa;</xsl:text>
  <body bgcolor="white">
  <xsl:text>&#xa;</xsl:text>
  <a><xsl:attribute name="name"><xsl:value-of select="$CommandName"/>.Top_Of_Page</xsl:attribute></a>
  <xsl:text>&#xa;</xsl:text>
  <xsl:apply-templates select="Cmd"/>
  </body>
  <xsl:text>&#xa;</xsl:text>
  </html>
</xsl:template>

<!-- 
     High-level description of information generated/copied by this stylesheet:
     
     - Generate command heading using H2-level element
     - Generate links to Parameters, Examples, and Error messages sections
     - Generate Where allowed and Threadsafe information for command 
     - Copy command-level help (if present) from the input CommandHelp file
     - Generate parameter syntax table using command description XML file
     - Copy parameter-level help (if present) from the input CommandHelp file 
     - Copy the Examples help (if present) from the CommandHelp file
     - Copy the Error messages help (if present) from the CommandHelp file         
                                                                               -->
<xsl:template match="Cmd">
  <h2><xsl:value-of select="@Prompt"/>&nbsp;&nbsp;(<xsl:value-of select="@CmdName"/>)</h2>

<!-- Make a table which contains the information on where the command is allowed to run, 
     whether the command is "safe" to run in a multi-threaded job, and hypertext links 
     to the Parameters section, Examples section, and Error Messages section of the file.
     The "where allowed" and "threadsafe" information will not be generated for command
     objects that are really command definition statements or utility statements.  The 
     command information will be in one table cell aligned on the left, and the section
     links will be generated in one table cell aligned to the right. Table has one row.   -->
     
 <xsl:text>&#xa;</xsl:text>
  <table width="100%">
    <xsl:text>&#xa;</xsl:text><tr><xsl:text>&#xa;</xsl:text><td align="left" valign="top">
<!-- Generate information about what environments the command can be run and 
     whether the command will run in a job that has multiple threads of execution.   -->

<!-- If the command object is a command definition statement (like PMTCTL) or 
     a utility statement (like STRPGMEXP), don't generate this information.          -->
  <xsl:if test="substring($WhereAllowed,1,9)!='000000000'">
    <b><xsl:value-of select="$_WHERE_ALLOWED_TO_RUN"/>:&nbsp;&nbsp;</b><xsl:text/>
    <xsl:choose>

      <xsl:when test="substring($WhereAllowed,1,9)='111111111'">
        <xsl:value-of select="$_ALLOW_ALL"/> <br /><xsl:text>&#xa;</xsl:text>
      </xsl:when>
      
      <xsl:when test="substring($WhereAllowed,1,9)='011100101'">
        <xsl:value-of select="$_ALLOW_INTERACTIVE1"/><br /><xsl:text>&#xa;</xsl:text>
      </xsl:when>

      <xsl:when test="substring($WhereAllowed,1,9)='011100100'">
        <xsl:value-of select="$_ALLOW_INTERACTIVE2"/><br /><xsl:text>&#xa;</xsl:text>
      </xsl:when>

      <xsl:when test="substring($WhereAllowed,1,9)='110001111'">
        <xsl:value-of select="$_ALLOW_COMPILED_CL_OR_REXX1"/><br /><xsl:text>&#xa;</xsl:text>
      </xsl:when>

      <xsl:when test="substring($WhereAllowed,1,9)='110001100'">
        <xsl:value-of select="$_ALLOW_COMPILED_CL_OR_REXX2"/><br /><xsl:text>&#xa;</xsl:text>
      </xsl:when>

      <xsl:otherwise>
        <xsl:text>&#xa;</xsl:text>
        <ul>
  
          <xsl:if test="substring($WhereAllowed,5,1)='1'">
            <li><xsl:value-of select="$_ALLOW_JOB_BATCH"/></li><xsl:text>&#xa;</xsl:text>
          </xsl:if>
          <xsl:if test="substring($WhereAllowed,4,1)='1'">
            <li><xsl:value-of select="$_ALLOW_JOB_INTERACTIVE"/></li><xsl:text>&#xa;</xsl:text>
          </xsl:if>                                     
          <xsl:if test="substring($WhereAllowed,1,1)='1'">
            <li><xsl:value-of select="$_ALLOW_PROGRAM_BATCH"/></li><xsl:text>&#xa;</xsl:text>
          </xsl:if>
          <xsl:if test="substring($WhereAllowed,2,1)='1'">
            <li><xsl:value-of select="$_ALLOW_PROGRAM_INTERACTIVE"/></li><xsl:text>&#xa;</xsl:text>
          </xsl:if>
          <xsl:if test="substring($WhereAllowed,8,1)='1'">
            <li><xsl:value-of select="$_ALLOW_MODULE_BATCH"/></li><xsl:text>&#xa;</xsl:text>
          </xsl:if>
          <xsl:if test="substring($WhereAllowed,9,1)='1'">
            <li><xsl:value-of select="$_ALLOW_MODULE_INTERACTIVE"/></li><xsl:text>&#xa;</xsl:text>
          </xsl:if>
          <xsl:if test="substring($WhereAllowed,6,1)='1'">
            <li><xsl:value-of select="$_ALLOW_REXX_BATCH"/></li><xsl:text>&#xa;</xsl:text>
          </xsl:if>
          <xsl:if test="substring($WhereAllowed,7,1)='1'">
            <li><xsl:value-of select="$_ALLOW_REXX_INTERACTIVE"/></li><xsl:text>&#xa;</xsl:text>
          </xsl:if>
          <xsl:if test="substring($WhereAllowed,3,1)='1'">
            <li><xsl:value-of select="$_ALLOW_USING_COMMAND_API"/></li><xsl:text>&#xa;</xsl:text>
          </xsl:if>

        </ul>
      </xsl:otherwise>
    </xsl:choose>

    <b><xsl:value-of select="$_THREADSAFE"/>:&nbsp;&nbsp;</b><xsl:text/> 
    <xsl:choose>
      <xsl:when test="substring($ThreadSafe,1,1)='0'">
        <xsl:value-of select="$_THREADSAFE_NO"/><xsl:text>&#xa;</xsl:text></xsl:when>
      <xsl:when test="substring($ThreadSafe,1,1)='1'">
        <xsl:value-of select="$_THREADSAFE_YES"/><xsl:text>&#xa;</xsl:text></xsl:when>
      <xsl:when test="substring($ThreadSafe,1,1)='2'">
        <xsl:value-of select="$_THREADSAFE_CONDITIONAL"/><xsl:text>&#xa;</xsl:text></xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
    
   </xsl:if>
   </td>   

   <xsl:text>&#xa;</xsl:text>
   
   <td align="right" valign="top"><xsl:text>&#xa;</xsl:text>
   <a><xsl:attribute name="href">#<xsl:value-of select="@CmdName"/>.PARAMETERS.TABLE</xsl:attribute><xsl:value-of select="$_PARAMETERS"/></a>
   <br /><xsl:text>&#xa;</xsl:text>
   <a><xsl:attribute name="href">#<xsl:value-of select="@CmdName"/>.COMMAND.EXAMPLES</xsl:attribute><xsl:value-of select="$_EXAMPLES"/></a>
   <br /><xsl:text>&#xa;</xsl:text>
   <a><xsl:attribute name="href">#<xsl:value-of select="@CmdName"/>.ERROR.MESSAGES</xsl:attribute><xsl:value-of select="$_ERRORS"/></a>
   </td><xsl:text>&#xa;</xsl:text>
   </tr><xsl:text>&#xa;</xsl:text>    
  </table>
  <xsl:text>&#xa;</xsl:text>

<!-- Process the input command help file to copy the command-level help 
     to the output file.  If the error messages section is imbedded
     in the command-level help, that section gets copied too.                  -->
  <xsl:if test="$CommandHelp!='__NO_HELP'">
    <xsl:text>&#xa;</xsl:text>
    <xsl:copy-of select="$CommandHelpDocument//div[a/@name=$CommandHelpID]"/>
  
  <!-- Add link to beginning of help file after the command-level help.        --> 
    <xsl:call-template name="AddLinkToTop"/>  
  </xsl:if>

<!-- Generate the parameter syntax summary table using the CDML file as input  -->                        
  
  <xsl:text>&#xa;</xsl:text>
  <hr width="100%" size="2" /><xsl:text>&#xa;</xsl:text>
  <xsl:text>&#xa;</xsl:text>
  <div>
  <xsl:text>&#xa;</xsl:text>                                                         
  <h3><a>
  <xsl:attribute name="name">                                  
  <xsl:value-of select="@CmdName"/>.PARAMETERS.TABLE</xsl:attribute>
  <xsl:value-of select="$_PARAMETERS"/>
  </a></h3>
 
 <!-- If command has no parameters, say 'None' and do not generate syntax table.  --> 
  <xsl:if test="count(child::Parm)=0">
    <xsl:value-of select="$_NONE"/> <br /><xsl:text>&#xa;</xsl:text>
  </xsl:if>
 
 <!-- Width percentages were removed because they goof up importing the HTML into
      a WordPro document.  Now that white space is under control, the browsers
      seem to do a good job of proportional spacing of the four columns.  -->
  <xsl:if test="$HasParameters">     
    <xsl:text>&#xa;</xsl:text>
    <table border="1">
    <xsl:text>&#xa;</xsl:text>
    <tr><xsl:text>&#xa;</xsl:text>
      <th align="left" valign="bottom" bgcolor="aqua">
        <xsl:value-of select="$_KEYWORD"/>
      </th>
      <xsl:text>&#xa;</xsl:text>
      <th align="left" valign="bottom" bgcolor="aqua">
        <xsl:value-of select="$_DESCRIPTION"/>
      </th>
      <xsl:text>&#xa;</xsl:text>
      <th align="left" valign="bottom" bgcolor="aqua">
        <xsl:value-of select="$_CHOICES"/>
      </th>
      <xsl:text>&#xa;</xsl:text>
      <th align="left" valign="bottom" bgcolor="aqua">
        <xsl:value-of select="$_NOTES"/>
      </th>
    <xsl:text>&#xa;</xsl:text>
    </tr>
    <xsl:text>&#xa;</xsl:text> 

    <!-- Process each <Parm> element to generate the table rows. Parameters are processed in 
         the order that they will be seen when the command is prompted, which may be different
         than the order that the <Parm> elements appear in the CDML file.              --> 
    <xsl:apply-templates select="Parm">
      <!-- Sort Parms by prompt position  --> 
      <xsl:sort data-type="number" select="@PosNbr"/>
    </xsl:apply-templates>

    <xsl:text/>
    </table>
    <xsl:text>&#xa;</xsl:text> 
    
  </xsl:if> 
      
 <!-- Add link to beginning of help file after the Parameters section.             --> 
  <xsl:call-template name="AddLinkToTop"/>
 
 <!-- Generate close of the Parameters division.                                   --> 
  <xsl:text>&#xa;</xsl:text> 
  </div>
  
 <!-- Generate parameter help only if there is help panel group HTML text available.  -->   
  <xsl:if test="$CommandHelp!='__NO_HELP'">
    
    <!-- Copy the parameter-level help sections from the input command help HTML file.
         The help sections will be generated in the same order that the parameters appear when
         the command is prompted and the parameters appear in the syntax table.            -->
    <xsl:for-each select="Parm"><xsl:sort data-type="number" select="@PosNbr"/>
      <xsl:text>&#xa;</xsl:text>
      <xsl:copy-of select=
       "$CommandHelpDocument//div[a/@name=concat($CommandHelpID,'.',current()/@Kwd)]"/>
    
    <!-- Add link to beginning of help file after the parameter help section.       --> 
      <xsl:call-template name="AddLinkToTop"/>
                  
    </xsl:for-each>
  </xsl:if>   

<!-- Copy the Examples help section from the command help HTML file (if present)  -->
  <xsl:text>&#xa;</xsl:text>
  <hr width="100%" size="2" /><xsl:text>&#xa;</xsl:text>
  <div><xsl:text>&#xa;</xsl:text>                                                         
    <xsl:choose>
      <xsl:when test="$CommandHelp!='__NO_HELP' and $CommandHelpDocument//div[a/@name=concat($CommandName,'.COMMAND.EXAMPLES')]">
        <xsl:text/><a><xsl:attribute name="name">
        <xsl:value-of select="$CommandName"/>.COMMAND.EXAMPLES</xsl:attribute></a>
        <xsl:text>&#xa;</xsl:text>   
        <xsl:copy-of select=
             "$CommandHelpDocument//div[a/@name=concat($CommandHelpID,'.COMMAND.EXAMPLES')]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text/><h3><a><xsl:attribute name="name">
           <xsl:value-of select="$CommandName"/>.COMMAND.EXAMPLES</xsl:attribute>
           <xsl:value-of select="$_EXAMPLES"/></a>
        </h3>
        <xsl:value-of select="$_NONE"/>   
      </xsl:otherwise>
    </xsl:choose>    
  <xsl:text>&#xa;</xsl:text>
  </div>

<!-- Add link to beginning of help file after the Examples section.              --> 
  <xsl:call-template name="AddLinkToTop"/>

<!-- Copy the Error Messages help section from the command help HTML file (if present) -->
  <xsl:text>&#xa;</xsl:text>
  <hr width="100%" size="2" />
  <xsl:text>&#xa;</xsl:text>
  <div><xsl:text>&#xa;</xsl:text>                                                         
    <xsl:choose>
      <xsl:when test="$CommandHelp!='__NO_HELP' and
                     $CommandHelpDocument//div[a/@name=concat($CommandName,'.ERROR.MESSAGES')]">
        <xsl:text/><a><xsl:attribute name="name">
        <xsl:value-of select="$CommandName"/>.ERROR.MESSAGES</xsl:attribute></a>
        <xsl:text>&#xa;</xsl:text>   
        <xsl:copy-of select=
             "$CommandHelpDocument//div[a/@name=concat($CommandHelpID,'.ERROR.MESSAGES')]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text/><h3><a><xsl:attribute name="name">
           <xsl:value-of select="$CommandName"/>.ERROR.MESSAGES</xsl:attribute>
           <xsl:value-of select="$_ERRORS"/></a>
        </h3>
        <xsl:value-of select="$_UNKNOWN"/>   
      </xsl:otherwise>
    </xsl:choose>    
  <xsl:text>&#xa;</xsl:text>
  </div>

<!-- Add link to beginning of help file after the Error messages section.             -->   
  <xsl:call-template name="AddLinkToTop"/>
  <xsl:text>&#xa;</xsl:text>

</xsl:template>                 


<!-- Template to process one <Parm> element to create one row in the 
       command syntax summary table.                                           -->
<xsl:template match="Parm">
  <xsl:text/>
  <tr>
  <xsl:text>&#xa;</xsl:text>
    <!-- For <Parm> elements that have associated child <Elem> or <Qual> elements, the 
         Keyword and Notes cells should span multiple rows.  Generate a rowspan attribute
         for these cells if there are child <Elem> or <Qual> elements.          --> 
    <xsl:variable name="NumRowSpan" 
         select="count(descendant::Elem)+count(descendant::Qual)+1"/>
    <td valign="top">
    <xsl:if test="$NumRowSpan>1">
      <xsl:attribute name="rowspan"><xsl:value-of select="$NumRowSpan"/></xsl:attribute>
    </xsl:if>      
         
    <!-- Generate value for "Keyword" cell of table row and 
         make a link to the corresponding parameter description               -->
      <xsl:choose>
        <xsl:when test="$CommandHelp!='__NO_HELP'">
          <a><xsl:attribute name="href">#<xsl:value-of select="$CommandHelpID"/>.<xsl:value-of select="@Kwd"/></xsl:attribute>
          <b><xsl:value-of select="@Kwd"/></b>                                              
          </a>
        </xsl:when>
        <xsl:otherwise>
          <b><xsl:value-of select="@Kwd"/></b>                                              
        </xsl:otherwise>
      </xsl:choose>
      </td>
    
    <!-- Generate value for "Description" cell of table row using parameter prompt text.  -->
      <xsl:text>&#xa;</xsl:text>
      <td valign="top">
        <xsl:value-of select="@Prompt"/>
      </td>

    <!-- Generate values for "Choices" cell of table row                     -->
      <xsl:text>&#xa;</xsl:text>
      <td valign="top">
      
      <!-- Process any <SngVal> elements for the parameter  -->
      <xsl:apply-templates select="SngVal"/>
      
      <!-- If the MAX value is greater then 1, indicate repetitions allowed    -->
      <xsl:if test="@Max>1">
        <xsl:choose>
          <xsl:when test="count(child::SngVal)>0">
            <xsl:value-of select="substring-before($_VALUES_OTHER_REPEAT,'&amp;amp;1')"/>
            <xsl:value-of select="@Max"/>
            <xsl:value-of select="substring-after($_VALUES_OTHER_REPEAT,'&amp;amp;1')"/>:&nbsp;<xsl:text/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="substring-before($_VALUES_REPEAT,'&amp;amp;1')"/>
            <xsl:value-of select="@Max"/>
            <xsl:value-of select="substring-after($_VALUES_REPEAT,'&amp;amp;1')"/>:&nbsp;<xsl:text/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:if test="@Max=1 and count(child::SngVal)>0">
        <xsl:text/>
        <xsl:value-of select="$_VALUES_OTHER"/>:&nbsp;<xsl:text/>
      </xsl:if>    
      
      <!-- If value is restricted (SNGVAL, SPCVAL,and VALUES), don't show the parameter type.
           If value must be within a range, show range instead of parameter type.
           If not restricted values or restricted range, call GenType template to show the 
           parameter type in italics.                                         -->        
      <xsl:choose>
      <xsl:when test="@Rstd='YES'"/>
      <xsl:when test="@RangeMinVal!=''">
        <xsl:value-of select="@RangeMinVal"/>-<xsl:value-of select="@RangeMaxVal"/><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type!=''">
        <xsl:call-template name="GenType"/>
      </xsl:when>
      </xsl:choose>

      <!-- Check if the default value needs to be generated as a choice or not. --> 
      <xsl:call-template name="CheckDft"/>

      <!-- Process all SpcVal, Values, and ChoicePgmValues child elements for this Parm.  -->
      <xsl:apply-templates select="SpcVal"/>
      <xsl:apply-templates select="Values"/>
      <xsl:apply-templates select="ChoicePgmValues"/>

    </td>
    <xsl:text>&#xa;</xsl:text>

 <!-- Generate values for "Notes" cell of table row                       -->        
    <td valign="top">
      <xsl:if test="$NumRowSpan>1">
        <xsl:attribute name="rowspan"><xsl:value-of select="$NumRowSpan"/></xsl:attribute>
      </xsl:if>
    <!-- Identify parameter as being either Required (if Min>0) or Optional (if Min=0).  -->
        <xsl:choose>
         <xsl:when test="@Min>0">
          <xsl:value-of select="$_REQUIRED"/>
         </xsl:when>
         <xsl:otherwise>
          <xsl:value-of select="$_OPTIONAL"/>
         </xsl:otherwise>
        </xsl:choose>  
          
      <!-- Check if this is a "Key" parameter (for prompt override program).         -->
      <xsl:if test="@KeyParm='YES'">                          
        <xsl:text/>, <xsl:value-of select="$_KEY"/>
      </xsl:if>  
      
      <!-- Determine the position of the Parm element as defined in the original XML -->
      <xsl:variable name="ParmPos" select="count(preceding-sibling::*)+1"/>
      <!-- if MAXPOS >= ParmPos, output parameter positional number  -->
      <xsl:if test="number(../@MaxPos) >= number($ParmPos)">
        <xsl:text/>, <xsl:value-of select="$_POSITIONAL"/>&nbsp;<xsl:value-of select="number($ParmPos)"/>
      </xsl:if>
      
    </td>
    <xsl:text>&#xa;</xsl:text>
    
  </tr>
  <xsl:text>&#xa;</xsl:text>
  
  <!-- Process all child Qual and Elem elements for the Parm element (may be none).   -->
  <xsl:apply-templates select="Qual"/>
  <xsl:apply-templates select="Elem"/>
                                                                                        
</xsl:template>
                                                                                        
<!-- Template to process one <Elem> element to create one row in the 
       command syntax summary table.                                           -->  
<xsl:template match="Elem">
  <tr>
  <xsl:text>&#xa;</xsl:text>
    
    <!-- Identify the element number and put the prompt text in "Description" cell -->
    <td valign="top">
    
    <!-- Test for nested Elem and set padding-left style attribute accordingly.    -->
    <xsl:variable name="NumIndent" select="count(ancestor::Elem)*10"/>
    <xsl:if test="$NumIndent!=0">
      <xsl:attribute name="style">padding-left:<xsl:value-of select="$NumIndent"/>pt</xsl:attribute>
    </xsl:if>
      
    <xsl:value-of select="$_ELEMENT"/>&nbsp;<xsl:number count="Elem"/>:&nbsp;<xsl:value-of select="@Prompt"/>
    </td>
    <xsl:text>&#xa;</xsl:text>
    
    <!-- Fill in the "Choices" cell for the element      -->
    <td valign="top">
    <xsl:text>&#xa;</xsl:text>
      
      <xsl:apply-templates select="SngVal"/>
      
      <!-- If the MAX value is greater then 1, indicate repetitions allowed    -->
      <xsl:if test="@Max>1">
        <xsl:choose>
          <xsl:when test="count(child::SngVal)>0">
            <xsl:value-of select="substring-before($_VALUES_OTHER_REPEAT,'&amp;amp;1')"/>
            <xsl:value-of select="@Max"/>
            <xsl:value-of select="substring-after($_VALUES_OTHER_REPEAT,'&amp;amp;1')"/>:&nbsp;<xsl:text/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="substring-before($_VALUES_REPEAT,'&amp;amp;1')"/>
            <xsl:value-of select="@Max"/>
            <xsl:value-of select="substring-after($_VALUES_REPEAT,'&amp;amp;1')"/>:&nbsp;<xsl:text/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:if test="@Max=1 and count(child::SngVal)>0">
        <xsl:value-of select="$_VALUES_OTHER"/>:&nbsp;<xsl:text/>
      </xsl:if> 

      <xsl:choose>
      <xsl:when test="@Rstd='YES'"/>
      <xsl:when test="@RangeMinVal!=''">
        <xsl:value-of select="@RangeMinVal"/>-<xsl:value-of select="@RangeMaxVal"/><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type!=''">
        <xsl:call-template name="GenType"/>
      </xsl:when>
      </xsl:choose>
      
      <!-- Check if the default value needs to be generated as a choice or not. --> 
      <xsl:call-template name="CheckDft"/>

      <xsl:apply-templates select="SpcVal"/>
      <xsl:apply-templates select="Values"/>
      <xsl:apply-templates select="ChoicePgmValues"/>
    </td>
    <xsl:text>&#xa;</xsl:text>
    </tr>
    <xsl:text>&#xa;</xsl:text>
  
  <!--  Process all child <Qual> and <Elem> elements of the <Elem> element (if any) -->
  <xsl:apply-templates select="Qual"/>
  <xsl:apply-templates select="Elem"/>

</xsl:template>


<!-- Template to process one <Qual> element to create one row in the 
       command syntax summary table.                                           -->
<xsl:template match="Qual">
  <tr>
  <xsl:text>&#xa;</xsl:text>
    <!-- Identify the qualifier number and put prompt text in "Description" cell
         (note that first qualifier takes prompt text from parent element)    --> 
    <td valign="top">
      
      <!-- Test for nested Qual and set padding-left style attribute accordingly.    -->
      <xsl:variable name="NumIndent" select="count(ancestor::Elem)*10"/>
      <xsl:if test="$NumIndent!=0">
        <xsl:attribute name="style">padding-left:<xsl:value-of select="$NumIndent"/>pt</xsl:attribute>
      </xsl:if>
      
      <xsl:value-of select="$_QUALIFIER"/>&nbsp;<xsl:number count="Qual"/>:&nbsp;<xsl:text/>
      <xsl:choose>
      <xsl:when test="position()=1">
      <xsl:value-of select="../@Prompt"/>
      </xsl:when>
      <xsl:otherwise>
      <xsl:value-of select="@Prompt"/>
      </xsl:otherwise>
      </xsl:choose>
    </td>
    <xsl:text>&#xa;</xsl:text>
    <!-- Construct the "Choices" cell contents for the qualifier  -->
    <td valign="top">
      <xsl:choose>
      <xsl:when test="@Rstd='YES'"/>
      <xsl:when test="@RangeMinVal!=''">
        <xsl:value-of select="@RangeMinVal"/>-<xsl:value-of select="@RangeMaxVal"/><xsl:text/>
      </xsl:when>  
      <xsl:when test="@Type!=''">
        <xsl:call-template name="GenType"/>
      </xsl:when>
      <xsl:otherwise/>
      </xsl:choose>
      
      <!-- Check if the default value needs to be generated as a choice or not. --> 
      <xsl:call-template name="CheckDft"/>

      <xsl:apply-templates select="SpcVal"/>
      <xsl:apply-templates select="Values"/>
      <xsl:apply-templates select="ChoicePgmValues"/>
    </td>
    <xsl:text>&#xa;</xsl:text>
    </tr>
</xsl:template>


<!-- Subroutine template to add link to top of generated file            -->
<xsl:template  name="AddLinkToTop">
  <!-- Add a hypertext link to the anchor tag at the very top of the generated file   -->
 
  <xsl:text>&#xa;</xsl:text>
  <table width="100%">
  <xsl:text>&#xa;</xsl:text>
  <tr><td align="right">
  <a><xsl:attribute name="href">#<xsl:value-of select="$CommandName"/>.Top_Of_Page</xsl:attribute><xsl:value-of select="$_TOP_OF_PAGE"/></a>
  </td></tr>
  <xsl:text>&#xa;</xsl:text>
  </table>
 
</xsl:template>


<!-- Subroutine template to generate the input field type for Choices     -->
<xsl:template  name="GenType">
   <xsl:choose>
      <xsl:when test="@Type='SNAME'">
      <i><xsl:value-of select="$_TYPE_SIMPLE_NAME"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='PNAME'">
      <i><xsl:value-of select="$_TYPE_PATH_NAME"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='NAME'">
      <i><xsl:value-of select="$_TYPE_NAME"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='CNAME'">
      <i><xsl:value-of select="$_TYPE_COMMUNICATIONS_NAME"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='GENERIC'">
      <i><xsl:value-of select="$_TYPE_GENERIC_NAME"/>,&nbsp;<xsl:value-of select="$_NAME_LOWERCASE"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='INT2'">
      <i><xsl:value-of select="$_TYPE_INTEGER"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='INT4'">
      <i><xsl:value-of select="$_TYPE_INTEGER"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='UINT2'">
      <i><xsl:value-of select="$_TYPE_UNSIGNED_INTEGER"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='UINT4'">
      <i><xsl:value-of select="$_TYPE_UNSIGNED_INTEGER"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='DEC'">
      <i><xsl:value-of select="$_TYPE_DECIMAL_NUMBER"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='CMD'">
      <i><xsl:value-of select="$_TYPE_COMMAND_STRING"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='CMDSTR'">
      <i><xsl:value-of select="$_TYPE_COMMAND_STRING"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='LGL'">
      <i><xsl:value-of select="$_TYPE_VALUE_LOGICAL"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='CHAR'">
      <i><xsl:value-of select="$_TYPE_VALUE_CHARACTER"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='VARNAME'">
      <i><xsl:value-of select="$_TYPE_CL_VARIABLE_NAME"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='DATE'">
      <i><xsl:value-of select="$_TYPE_DATE"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='TIME'">
      <i><xsl:value-of select="$_TYPE_TIME"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='HEX'">
      <i><xsl:value-of select="$_TYPE_VALUE_HEX"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='X'">
      <i><xsl:value-of select="$_TYPE_NOT_RESTRICTED"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='ELEM'">
      <i><xsl:value-of select="$_TYPE_ELEMENT_LIST"/></i><xsl:text/>
      </xsl:when>
      <xsl:when test="@Type='QUAL'">
       <xsl:choose>
       <xsl:when test="count(child::Qual)=2">
         <i><xsl:value-of select="$_TYPE_QUALIFIED_OBJECT_NAME"/></i><xsl:text/>
       </xsl:when>
       <xsl:when test="count(child::Qual)=3">
         <i><xsl:value-of select="$_TYPE_QUALIFIED_JOB_NAME"/></i><xsl:text/>
       </xsl:when>
       <xsl:otherwise>
         <i><xsl:value-of select="$_TYPE_QUALIFIER_LIST"/></i><xsl:text/>
       </xsl:otherwise>
       </xsl:choose>
      </xsl:when>
      <xsl:otherwise/>
      </xsl:choose>
</xsl:template>         
 

<!-- Subroutine template to check if default value needs to be generated as the 
     first choice value.                                                        -->
<xsl:template  name="CheckDft">
  <!-- If there is a default and no child element has a Val attribute equal 
       to the default, add default here as a bold and underlined value.     -->        
  <xsl:if test="@Dft!=''">
    <xsl:choose>
      <xsl:when test="descendant::Value/@Val=@Dft"/> 
      <xsl:otherwise>
        <!-- If the value is restricted, it is possible that the default is 
             the same as one of the values, but not quite.  For example, the 
             default for a decimal restricted parameter might be '1' and the 
             "matching" restricted value is '1.0'.  In that case, the default 
             will be the first generated choice (no preceding "type" string).  -->   
        <xsl:choose>
          <xsl:when test="@Rstd='YES'">
            <xsl:text/><b><u><xsl:value-of select="@Dft"/></u></b>,&nbsp;<xsl:text/> 
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>,&nbsp;<b><u><xsl:value-of select="@Dft"/></u></b><xsl:text/> 
          </xsl:otherwise>
        </xsl:choose>    
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>         


<!-- Template to handle <SngVal> element for <Parm> or <Elem> element     -->
<xsl:template match="SngVal">
  <!-- Put out text of 'Special values: ' and process the list of values. -->
  <xsl:value-of select="$_VALUES_SINGLE"/>:&nbsp;<xsl:text/>
  <xsl:apply-templates select="Value"/>
  <br /><xsl:text/>
</xsl:template>   


<!-- Template to handle <SpcVal> element for <Parm>,<Elem>, or <Qual> element     -->
<xsl:template match="SpcVal">
  <xsl:choose>
    <!-- If option taken to show values returned by choices program and there is a 
         <ChoicePgmValue> element for this <Parm>, do not list the values from
         this <SpcVal> element (because they would appear twice).                 --> 
    <xsl:when test="count(following-sibling::ChoicePgmValues)>0 and 
                  $ShowChoicePgmValues!='0'"/>
    
    <!-- Otherwise, list the special values allowed for this <Parm>, <Elem>, or 
         <Qual> element.                                                          --> 
    <xsl:otherwise>
      
      <!-- If RSTD(*NO) specified (*NO is also the default), the GenType procedure 
           has generated a string describing the type.  Generate a comma before 
           generating list of values.                                             --> 
      <xsl:if test="../@Rstd='NO'">
        <xsl:text/>,&nbsp;<xsl:text/>
      </xsl:if>
      
      <!-- Generate list of special values allowed (normally values preceded by "*") -->
      <xsl:apply-templates select="Value"/>
      
      </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- Template to handle <Values> element for <Parm>,<Elem>, or <Qual> element     -->
<xsl:template match="Values">
  <xsl:choose>
    <!-- If option taken to show values returned by choices program and there is a 
         <ChoicePgmValue> element for this <Parm>, do not list the values from
         this <Values> element (because they would appear twice).                 -->  
    <xsl:when test="count(following-sibling::ChoicePgmValues)>0 and 
                  $ShowChoicePgmValues!='0'"/>
    
    <!-- Otherwise, list the special values allowed for this <Parm>, <Elem>, or 
         <Qual> element.                                                          --> 
    <xsl:otherwise>
    
      <!-- If RSTD(*NO) specified (*NO is also the default), the GenType procedure 
           has generated a string describing the type.  Generate a comma before 
           generating list of values.  If there is a preceding sibling <SpcVal> element,  
           a comma needs to be generated before generating the list of values.    --> 
      <xsl:if test="../@Rstd='NO' or 
                    count(preceding-sibling::SpcVal)>0">
        <xsl:text/>,&nbsp;<xsl:text/>
      </xsl:if>
      
      <!-- Generate list of regular values allowed (values not preceded by "*").  -->
      <xsl:apply-templates select="Value"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Template to handle <ChoicePgmValues> element for <Parm>,<Elem>, or <Qual> element     -->
<xsl:template match="ChoicePgmValues">
  <xsl:if test="$ShowChoicePgmValues!='0'">
    <xsl:if test="../@Rstd='NO'">
      <xsl:text/>,&nbsp;<xsl:text/>
    </xsl:if>
    <xsl:apply-templates select="Value"/>
  </xsl:if>  
</xsl:template>

<!-- Template to handle <Value> element for <SngVal>,<SpcVal>,
     <Values> or <ChoicePgmValues> element     -->
<xsl:template match="Value">
    <!-- x, y, z - output ", " when in the middle - not last -->
  <xsl:variable name="OneValue" select="substring-before(@Val,' ')"/>
  <xsl:choose>
    <xsl:when test="$OneValue!=''">
      <!-- If this value is the default show it as bold and underlined    -->
      <xsl:choose>
       <xsl:when test="../../@Dft=$OneValue">
        <b><u><xsl:value-of select="$OneValue"/></u></b><xsl:text/>
       </xsl:when>
       <xsl:otherwise>
        <xsl:value-of select="$OneValue"/><xsl:text/>
       </xsl:otherwise>
      </xsl:choose>  
    </xsl:when>
    <xsl:otherwise>
      <!-- If this value is the default show it as bold and underlined    -->
      <xsl:choose>
       <xsl:when test="../../@Dft=@Val">
        <b><u><xsl:value-of select="@Val"/></u></b><xsl:text/>
       </xsl:when>
       <xsl:otherwise>
        <xsl:value-of select="@Val"/><xsl:text/>
       </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>    
  <xsl:if test="position()!=last() and @Val!=''">
    <xsl:text/>, <xsl:text/>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
