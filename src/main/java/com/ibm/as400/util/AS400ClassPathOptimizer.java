
package com.ibm.as400.util;

import com.ibm.as400.access.*;
import com.ibm.as400.resource.*;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.*;



/**
 * This class is used to optimize an AS400 classpath using the CRTJVAPGM command.
 * The original need was a software update/install procedure that needed to make sure
 * all the classes in the classpath were optimized at the appropriate levels.  We
 * needed something that didn't recompile classes if they were already optimized, as
 * our app was large (over 10MB) and it could take several hours and LOTS of cpu to
 * optimize at level 40.  We needed something that did "touch up" optimizations.
 * Here is an example of how you can use it...
 *
 * <pre>
 *	public static void main(String args[]) throws Exception {
 *
 *		String classpath = "/home/toronto/DEV/:/home/toronto/DEV/server.jar:" +
 *      	"/home/toronto/DEV/preprocessor.jar:/qibm/ProdData/HTTP/Public/jt400/lib/jt400.jar:" +
 *			"/qibm/proddata/java400/jt400ntv.jar:/home/toronto/DEV/collections.jar:" +
 *			"/home/toronto/DEV/antlr.jar:/home/toronto/DEV/crimson.jar:/home/toronto/DEV/xalan.jar:" +
 *			"/home/toronto/DEV/jaxp.jar:";
 *
 *		AS400 as400 = new AS400( "mysystem", "user", "password" );
 *		AS400ClassPathOptimizer cpo = new AS400ClassPathOptimizer( as400, classpath );
 *
 *		cpo.setOptimizationLevel( cpo.LEVEL_40 );
 *		cpo.setLicensedInternalCodeOptions( cpo.LICOPT_NOPRERESOLVEEXTREF );
 *
 *		// submit the optimizations and exit since this could take some time
 *		cpo.setOptimizeInParallel( true );
 *
 *		// so we can see the details of what it is doing...
 *		cpo.setOutputLog( System.out );
 *
 *		// submit the optimizations
 *		cpo.optimize();
 *
 *		System.exit( 0 );
 *	}
 * </pre>
 * @author Glen Marchesani
**/
public class AS400ClassPathOptimizer {

//------------------------------------------------------------------------------
// constants

	public static final int NO_OPTIMIZATION = 0;
	public static final int LEVEL_10 = 10;
	public static final int LEVEL_20 = 20;
	public static final int LEVEL_30 = 30;
	public static final int LEVEL_40 = 40;

	public static final String LICOPT_NOPRERESOLVEEXTREF = "NOPRERESOLVEEXTREF";

//------------------------------------------------------------------------------
// instance variables

	private AS400 as400_;
	private CommandCall commandCall_;
	private String classpath_;

	private boolean wait_ = false;

	private boolean optimizeInParallel_ = false;
	private int optimizationLevel_= NO_OPTIMIZATION;
	private boolean optimizeDirectories_ = false;

	private String licensedInternalCodeOptions_ = LICOPT_NOPRERESOLVEEXTREF;

	private PrintStream out_;

//------------------------------------------------------------------------------
// constructors

	public AS400ClassPathOptimizer() {
		this( null, null );
	}

	public AS400ClassPathOptimizer( AS400 as400 ) {
		this( as400, null );
	}

	public AS400ClassPathOptimizer( AS400 as400, String classpath ) {
		setAS400( as400 );
		setClasspath( classpath );
	}

//------------------------------------------------------------------------------
// methods


	/**
	 *  set the AS400 classpath to opimize.  valid path separators are ; and :
	 *
	 */
	public void setClasspath( String classpath ) {
		classpath_ = classpath;
	}

	public void setAS400( AS400 as400 ) {
		as400_ = as400;
	}

	/**
	 *  if true the optimize() method will wait for all CRTJVAPGM commands to end
	 *  before it returns.  Otherwise
	 *
	 */
	public void setWaitForOptimizations( boolean s ) {
		wait_ = s;
	}

	public boolean isWaitForOptimizations() {
		return wait_;
	}


	/**
	 *  if true run each CRTJVAPGM in a separate job.  Using a value of true setting will ignore the "wait for optimizations" attribute.
	 *  if false will run each CRTJVAPGM in serial waiting for the first to end before the next CRTJBAPGM is run.
	 *
	 */
	public void setOptimizeInParallel( boolean s ) {
		optimizeInParallel_ = s;
	}

	public void setOptimizeDirectories( boolean b ) {
		optimizeDirectories_ = b;
	}

	public boolean isOptimizeInParallel() {
		return optimizeInParallel_;
	}

	public String getLicensedInternalCodeOptions() {
		return licensedInternalCodeOptions_;
	}

	/**
	 * Sets the value to use on the CRTJVAPGM LICOPT parameter
	 */
	public void setLicensedInternalCodeOptions( String s ) {
		licensedInternalCodeOptions_ = s;
	}


	/**
	 *   Set the level to optimize classes to.  Note that if a class/jar/zip is currently
	 *   optimized at say level 30 and you set this at level 20 then no optimization will be done.
	 *
	 *   This is not the case for optimizing a directory.  If you optimize a directory the whole directory
	 *   will be reoptimized to the level set.  This is due to performance of checking the
	 *   optimization level of say 400 classes in a directory versus making a single call
	 *   to the CRTJVAPGM which.  The single call to CRTJVAPGM (to optimize the entire directory) is almost always quicker than
	 *   checking the 400 classes individually plus the time to optimize the classes individually.
	 *
	 */
	public void setOptimizationLevel( int level ) {
		optimizationLevel_ = level;
	}


	public void setOutputLog( PrintStream out ) {
		out_ = out;
	}

	public void optimize() throws Exception {

		if ( optimizeInParallel_ ) {
			wait_ = false;
		}

		run();

	}


	public void run() throws Exception {

		logit( "classpath=" + classpath_ );
		logit( "wait=" + wait_ );
		logit( "optimizationLevel=" + optimizationLevel_ );
		logit( "optimizeDirectories=" + optimizeDirectories_ );
		logit( "optimizeInParallel=" + optimizeInParallel_ );
		logit( "licensedInternalCodeOptions=" + licensedInternalCodeOptions_ );

		StringTokenizer st = new StringTokenizer( classpath_, ";:" );

		while( st.hasMoreTokens() ) {

			String path = st.nextToken();

			IFSFile file = new IFSFile( as400_, path );

			if ( file.isDirectory() && optimizeDirectories_ == false ) {
				logit( path + " is a directory and optimize directories is false." );
			} else if ( file.isAbsolute() && file.exists() == false ) {
				logit( path + " does not exist." );
			} else if ( file.isAbsolute() == false ) {
				logit( path + " must be an absolute path to be optimized." );
			} else {

				int level = retrieveOptimizationLevel( file );

				if ( level < optimizationLevel_ ) {
					optimize( file );
					logit( path + " currently optimized at level " + level + " optimizing to level " + optimizationLevel_ );
				} else {
					logit( path + " currently optimized at level " + level + " not optimizing." );
				}
			}
		}
	}


	private void optimize( IFSFile file ) throws Exception {

		String command = getOptimizationCommand( file );

		if ( wait_ ) {
			runCommand( command );
		} else if ( optimizeInParallel_ ) {
			runCommand( "SBMJOB JOB(CRTJVAPGM) CMD(" + command + ")" );
		}
	}


	void logit( String msg ) {
		if ( out_ != null ) {
			out_.println( msg );
		}
	}


	private String getOptimizationCommand( IFSFile file ) throws Exception {

		StringBuffer sb = new StringBuffer();

		sb.append( "CRTJVAPGM " );
		sb.append( "CLSF(" + file.getAbsolutePath() + ") "  );
		sb.append( "OPTIMIZE(" + optimizationLevel_ + ") " );

		if ( file.isDirectory() && optimizeDirectories_ ) {
			sb.append( "SUBTREE(*ALL) " );
		}

		if ( licensedInternalCodeOptions_ != null ) {
			sb.append( "LICOPT(" + licensedInternalCodeOptions_ + ") " );
		}

		return sb.toString();
	}


	void runCommand( String command ) throws Exception {

		if ( commandCall_ == null ) {
			commandCall_ = new CommandCall( as400_ );
		}

		logit( command );

		boolean status = commandCall_.run( command );

		if ( status == false ) {
			AS400Message[] log = commandCall_.getMessageList();
			logit( "error running command " + command );
			for ( int i=0; i<log.length; i++ ) {
				logit( "" + i + ":" + log[ i ].getText() );
			}
		}

	}


	int retrieveOptimizationLevel( IFSFile file ) throws Exception {

		RJavaProgram javaProgram = new RJavaProgram( as400_, file.getAbsolutePath() );

		Integer integer = (Integer) javaProgram.getAttributeUnchangedValue( RJavaProgram.OPTIMIZATION );

		return integer.intValue();

	}


}
