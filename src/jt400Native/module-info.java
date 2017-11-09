/* ifdef JAVA9
module jt400Native {
    requires java.datatransfer;
    requires java.security.jgss;

    requires transitive java.desktop;
    requires transitive java.logging;
    requires transitive java.naming;
    requires transitive java.sql;
    requires transitive java.xml;
   

    exports com.ibm.as400.access;
    exports com.ibm.as400.access.jdbcClient;
    exports com.ibm.as400.access.list;
    exports com.ibm.as400.data;
    exports com.ibm.as400.resource;
    exports com.ibm.as400.security;
    exports com.ibm.as400.security.auth;
    exports com.ibm.as400.util;
    exports com.ibm.as400.util.commtrace;

    provides java.sql.Driver with
        com.ibm.as400.access.AS400JDBCDriver;

}

endif JAVA9 */ 