# Logging with WildFly or JBoss EAP

There are numerous ways for setting Java properties with WildFly or JBoss EAP. 
Reference [the JBoss documentation](https://docs.jboss.org/author/display/WFLY/Command%20line%20parameters.html)
for information about these techniques, some of which are summarized here. 


### Command-line parameters to `standalone.sh` (standalone mode)

System properties can be added to the invocation of `standalone.sh` if running standalone mode, 
by way of using the `-D` syntax.

```bash
$JBOSS_HOME/bin/standalone.sh -Dcom.ibm.as400.access.Trace.category=all \
    -Dcom.ibm.as400.access.Trace.file=/tmp/toolbox_trace.txt
```

### Command-line parameters to `domain.sh` (domain mode) 
```bash
$JBOSS_HOME/bin/domain.sh --properties=/some/location/jboss.properties
```

Whereas the `jboss.properties` file would contain the necessary properties:
```properties
com.ibm.as400.access.Trace.category=all
com.ibm.as400.access.Trace.file=/tmp/toolbox_trace.txt
```

### Persistent configuration via XML

System properties can also be added to `standalone.xml` or `domain.xml`, depending on your
deployment technique. This is generally not recommended, though, since trace options are
often a temporary change.

```xml
<system-properties>
        <property name="com.ibm.as400.access.Trace.category" value="all"/>
        <property name="com.ibm.as400.access.Trace.file" value="/tmp/toolbox_trace.txt"/>
</system-properties>
```