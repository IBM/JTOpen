###############################################################################
##                                                                             
## JTOpen (AS/400 Toolbox for Java - OSS version)                              
##                                                                             
## Filename: UTMRI.pro
##                                                                             
## The source code contained herein is licensed under the IBM Public License   
## Version 1.0, which has been approved by the Open Source Initiative.         
## Copyright (C) 1997-2000 International Business Machines Corporation and     
## others. All rights reserved.                                                
##                                                                             
###############################################################################


REMOTE_CALL_JAVA_ERROR = Error calling Java application.
REMOTE_SIGN_ON_ERROR = Incorrect signon.
REMOTE_MESSAGE_FROM_COMMAND_SEP = :
REMOTE_LACK_PARAMETER = Lack parameters
REMOTE_FORMAT_ERROR   = Format error
REMOTE_SET_PROPERTY_1 = The option
REMOTE_SET_PROPERTY_2 = does not exist.
REMOTE_BAD_COMMAND    = Incorrect command.
REMOTE_START_PROGRAM = Starting program
REMOTE_END_PROGRAM1 = Program
REMOTE_END_PROGRAM2 = ended

#Only used by RunJavaApplication
#REMOTE_MORE_CLASSPATH = Classpath specified more than once.
#REMOTE_MORE_VERBOSE   = Verbose specified more than once.
#REMOTE_ERR_CLASSPATH  = Set classpath error.
#REMOTE_ERR_VERBOSE    = Set verbose error.
#REMOTE_ERR_PROPERTIES = Set properties error.
REMOTE_ERR_SET        = Set command incorrect.\nUsage: set <property=value>
REMOTE_ERR_JAVA       = Java command incorrect.\nUsage:  Java [-classpath=<value>] [-verbose] [<-Dproperty=value>] \n      class [<parm1> <parm2> [...]]
REMOTE_PORT_VALUE_ERROR = The value must be true or false.
REMOTE_HELP_LINE0  =
REMOTE_HELP_LINE1  = \nRun a Java program:
REMOTE_HELP_LINE2  = java [-classpath=<value>] [-verbose]\n       [-D<property>=<value> [-D<property2>=<value>] [...]]
REMOTE_HELP_LINE3  = <class> [<parm1> [<parm2>] [...]]
REMOTE_HELP_LINE4  = \nSet an option:
REMOTE_HELP_LINE5  = set <option>=<value>
REMOTE_HELP_LINE6  = where option is one of:
REMOTE_HELP_LINE7  = Classpath, DefaultPort, FindPort, Interpret, Optimize, Option,
REMOTE_HELP_LINE8  = SecurityCheckLevel, GarbageCollectionFrequency,
REMOTE_HELP_LINE9  = GarbageCollectionInitialSize, GarbageCollectionMaximumSize,
REMOTE_HELP_LINE10 = or GarbageCollectionPriority
REMOTE_HELP_LINE11 = \nDisplay current option values: \n  d
REMOTE_HELP_LINE12 = \nDisplay this message: \n  help, h or ?
REMOTE_HELP_LINE13 = \nEnd the program: \n  quit or q
REMOTE_D_LINE1  =  Current option values are:
REMOTE_D_LINE2  =     SecurityCheckLevel=
REMOTE_D_LINE3  =     Classpath=
REMOTE_D_LINE4  =     GarbageCollectionFrequency=
REMOTE_D_LINE5  =     GarbageCollectionInitialSize=
REMOTE_D_LINE6  =     GarbageCollectionMaximumSize=
REMOTE_D_LINE7  =     GarbageCollectionPriority=
REMOTE_D_LINE8  =     Interpret=
REMOTE_D_LINE9  =     Optimize=
REMOTE_D_LINE10 =     Option=
REMOTE_D_LINE11 =     DefaultPort=
REMOTE_D_LINE12 =     FindPort=

REMOTE_PROMPT = ==>

REMOTE_MORE_PARAMETER = Too many parameters.  Command format is: \n   utilities.VRunJavaApplication <system> <userid> <password>
REMOTE_WINDOW_TITLE = VJavaApplicationCall

#Only used by JPING
JPING_VERIFYING = Verifying connections to system 
JPING_VERIFIED = Connection verified
JPING_NOTVERIFIED = Connection verify failed
JPING_USAGE   = USAGE: java utilities.JPing (-h) <systemName> [-service <service>] [-ssl] \n
JPING_HELP    = [-h | -? | -help]   help
JPING_SERVICE = [-service | -s]     AS400 service: as-file, as-netprt, as-rmtcmd,
JPING_SERVICE2= \n                                   as-dtaq, as-database, as-ddm,
JPING_SERVICE3= \n                                   as-central, as-signon
JPING_SSL     = [-ssl]              use ssl ports
JPING_TIMEOUT = [-timeout | -t]     timeout period in millisec.  default is 20000 (20 sec) 
