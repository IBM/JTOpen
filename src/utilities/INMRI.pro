###############################################################################
##                                                                             
## JTOpen (IBM Toolbox for Java - OSS version)                              
##                                                                             
## Filename: INMRI.pro
##                                                                             
## The source code contained herein is licensed under the IBM Public License   
## Version 1.0, which has been approved by the Open Source Initiative.         
## Copyright (C) 1997-2000 International Business Machines Corporation and     
## others. All rights reserved.                                                
##                                                                             
###############################################################################

#TRANNOTE
#TRANNOTE Exception messages used by installer.
#TRANNOTE
#TRANNOTE THIS FILE NOT CURRENTLY TRANSLATED!!!!!
#TRANNOTE NOTE TO TRANSLATOR: Translate only the part
#TRANNOTE after the first equal sign (=).
#TRANNOTE

ERR_INVALID_ARGUMENT         =Error: Option is not valid:
ERR_OPTION_NOT_COMPATIBLE    =Error: Option is not compatible with other options:
ERR_MISSING_OPTION_VALUE     =Error: Option value missing for option
ERR_MISSING_OPTION           =Error: Missing option:
ERR_NO_I_U_C                 =Error: One and only one of the options -i, -u, and -c can be specified.
ERR_TOO_MANY_OPTIONS         =Error: Too many options:
ERR_UNEXPECTED_OPTION_VALUE  =Error: Unexpected option value:
ERR_UNEXPECTED_OPTION        =Error: Unexpected option:
ERR_NOT_VALID_URL            =URL is not valid:

EXC_CANNOT_CREATE_DIRECTORY  =Cannot create directory.
EXC_INSTALLATION_ABORTED     =Installation aborted.
EXC_NO_PACKAGES_INSTALLED    =No packages were installed.
EXC_PACKAGE_NOT_INSTALLED    =Package is not installed.

HELP01=Usage:
HELP02=  java utilities.AS400ToolboxInstaller
HELP03=                                       [-package(p) package1[,package2[...]]
HELP04=                                       [-source(s) sourceURL]
HELP05=                                       [-target(t) directory]
HELP06=                                       [-install(i)]
HELP07=                                       [-uninstall(u)]
HELP08=                                       [-compare(c)]
HELP09=                                       [-prompt(pr)]
HELP10=                                       [-? or -help] \n
HELP11=  When installing packages, the following options must be specified:
HELP12=    -install -source, -target \n
HELP13=  When uninstalling packages, the following options must be specified:
HELP14=    -uninstall -target \n
HELP15=  When comparing packages, the following options must be specified:
HELP16=    -compare, -source, -target
HELP17=  If you want to install, uninstall, or compare specific packages,\n  the option -package must be specified. \n
HELP18=  If -package is omitted, all packages are used. \n
HELP19=  -? or -help also displays this text. \n
HELP20=For example, to install all packages, run
HELP21=  java utilities.AS400ToolboxInstaller -i -s myAS400 -t c:\\target

PROMPT_IF_REPLACE_ONE        =Package &0 is out of date, replace it? [1 for yes, 0 for no]:
PROMPT_IF_REPLACE_TWO        =Package &0 does not exist, add it? [1 for yes, 0 for no]:

RESULT_ADD_CLASSPATHS        =To use the Toolbox, the CLASSPATH environment variable must be updated by adding the following:
RESULT_PACKAGE_NEEDS_UPDATED =Package &0 is out of date.
RESULT_PACKAGE_NOT_NEED_UPDATED=Package &0 is up to date.
RESULT_REMOVE_CLASSPATHS     =The following can be removed from the CLASSPATH environement variable:
RESULT_PACKAGE_INSTALLED     =Package &0 is installed.
RESULT_PACKAGE_UNINSTALLED   =Package &0 is uninstalled.
RESULT_UNEXPANDED_FILES      =The following are unexpanded files:

WARNING_SOURCE_URL_NOT_USED  =Source URL is not used when uninstalling packages.

