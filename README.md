# jena-native

Small PoC to determine feasibility of running Jena as a cloud native friendly service!

## Compile

To compile it natively make sure to have GraalVM and native-image installed.

More information: https://www.graalvm.org/docs/reference-manual/native-image/#install-native-image

Step to produce native binary:

```
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
native-image -jar target/jena-native-1.0-SNAPSHOT-jar-with-dependencies.jar
```

This should produce `jena-native-1.0-SNAPSHOT-jar-with-dependencies`, however we get the following warning:

```
Warning: Image 'jena-native-1.0-SNAPSHOT-jar-with-dependencies' is a fallback image that requires a JDK for execution (use --no-fallback to suppress fallback image generation and to print more detailed information why a fallback image was necessary).
```

Now this is something we potentially don't want. Adding the `--no-fallback` options reveals us all the errors during the compilation process. Mostly we see
that there are some error due to the dynamic class loading (https://github.com/oracle/graal/blob/master/substratevm/LIMITATIONS.md) which could be resolved for now
by adding `--report-unsupported-elements-at-runtime` flag.

Analyzing all the error messages we find out that we could add 
`--report-unsupported-elements-at-runtime`, `--allow-incomplete-classpath` and 
`-H:ReflectionConfigurationFiles=reflection-config.json` to push the problems to runtime.

```
/usr/lib/jvm/java-11-graalvm/bin/native-image --report-unsupported-elements-at-runtime --no-fallback -H:ReflectionConfigurationFiles=reflect-config.json -H:ResourceConfigurationFiles=resource-config.json --initialize-at-run-time=org.slf4j,org.apache.log4j -jar target/jena-native-1.0-SNAPSHOT-jar-with-dependencies.jar
```

```
WARNING: sun.reflect.Reflection.getCallerClass is not supported. This will impact performance.
Exception in thread "main" java.lang.ExceptionInInitializerError
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:290)
	at java.lang.Class.ensureInitialized(DynamicHub.java:499)
	at org.apache.jena.ext.xerces.impl.dv.SchemaDVFactory.<clinit>(SchemaDVFactory.java:45)
	at com.oracle.svm.core.hub.ClassInitializationInfo.invokeClassInitializer(ClassInitializationInfo.java:350)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:270)
	at java.lang.Class.ensureInitialized(DynamicHub.java:499)
	at org.apache.jena.datatypes.xsd.XSDDatatype.<init>(XSDDatatype.java:230)
	at org.apache.jena.datatypes.xsd.XSDDatatype.<init>(XSDDatatype.java:242)
	at org.apache.jena.datatypes.xsd.impl.XSDFloat.<init>(XSDFloat.java:49)
	at org.apache.jena.datatypes.xsd.XSDDatatype.<clinit>(XSDDatatype.java:54)
	at com.oracle.svm.core.hub.ClassInitializationInfo.invokeClassInitializer(ClassInitializationInfo.java:350)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:270)
	at java.lang.Class.ensureInitialized(DynamicHub.java:499)
	at org.apache.jena.vocabulary.XSD.<clinit>(XSD.java:183)
	at com.oracle.svm.core.hub.ClassInitializationInfo.invokeClassInitializer(ClassInitializationInfo.java:350)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:270)
	at java.lang.Class.ensureInitialized(DynamicHub.java:499)
	at org.apache.jena.shared.PrefixMapping.<clinit>(PrefixMapping.java:223)
	at com.oracle.svm.core.hub.ClassInitializationInfo.invokeClassInitializer(ClassInitializationInfo.java:350)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:270)
	at java.lang.Class.ensureInitialized(DynamicHub.java:499)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initializeSuperInterfaces(ClassInitializationInfo.java:323)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:245)
	at java.lang.Class.ensureInitialized(DynamicHub.java:499)
	at org.apache.jena.sparql.ARQConstants.<clinit>(ARQConstants.java:114)
	at com.oracle.svm.core.hub.ClassInitializationInfo.invokeClassInitializer(ClassInitializationInfo.java:350)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:270)
	at java.lang.Class.ensureInitialized(DynamicHub.java:499)
	at org.apache.jena.query.ARQ.init(ARQ.java:598)
	at org.apache.jena.sparql.system.InitARQ.start(InitARQ.java:29)
	at org.apache.jena.sys.JenaSystem.lambda$init$2(JenaSystem.java:117)
	at java.util.ArrayList.forEach(ArrayList.java:1540)
	at org.apache.jena.sys.JenaSystem.forEach(JenaSystem.java:192)
	at org.apache.jena.sys.JenaSystem.forEach(JenaSystem.java:169)
	at org.apache.jena.sys.JenaSystem.init(JenaSystem.java:115)
	at org.apache.jena.fuseki.main.FusekiServer.<clinit>(FusekiServer.java:98)
	at com.oracle.svm.core.hub.ClassInitializationInfo.invokeClassInitializer(ClassInitializationInfo.java:350)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:270)
	at java.lang.Class.ensureInitialized(DynamicHub.java:499)
	at jenanative.Main.main(Main.java:9)
Caused by: java.lang.RuntimeException: internal error
	at org.apache.jena.ext.xerces.impl.dv.xs.XSSimpleTypeDecl.applyFacets1(XSSimpleTypeDecl.java:768)
	at org.apache.jena.ext.xerces.impl.dv.xs.BaseSchemaDVFactory.createBuiltInTypes(BaseSchemaDVFactory.java:205)
	at org.apache.jena.ext.xerces.impl.dv.xs.SchemaDVFactoryImpl.createBuiltInTypes(SchemaDVFactoryImpl.java:44)
	at org.apache.jena.ext.xerces.impl.dv.xs.SchemaDVFactoryImpl.<clinit>(SchemaDVFactoryImpl.java:39)
	at com.oracle.svm.core.hub.ClassInitializationInfo.invokeClassInitializer(ClassInitializationInfo.java:350)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:270)
	... 39 more
ERROR StatusLogger Could not unregister MBeans for org.apache.logging.log4j2:type=Default
 java.lang.NullPointerException
	at org.apache.logging.log4j.core.jmx.Server.unregisterAllMatching(Server.java:337)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:261)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:249)
	at org.apache.logging.log4j.core.LoggerContext.stop(LoggerContext.java:372)
	at org.apache.logging.log4j.core.LoggerContext$1.run(LoggerContext.java:313)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry$RegisteredCancellable.run(DefaultShutdownCallbackRegistry.java:109)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry.run(DefaultShutdownCallbackRegistry.java:74)
	at java.lang.Thread.run(Thread.java:834)
	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:517)
	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:193)
ERROR StatusLogger Could not unregister MBeans for org.apache.logging.log4j2:type=Default,component=StatusLogger
 java.lang.NullPointerException
	at org.apache.logging.log4j.core.jmx.Server.unregisterAllMatching(Server.java:337)
	at org.apache.logging.log4j.core.jmx.Server.unregisterStatusLogger(Server.java:290)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:264)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:249)
	at org.apache.logging.log4j.core.LoggerContext.stop(LoggerContext.java:372)
	at org.apache.logging.log4j.core.LoggerContext$1.run(LoggerContext.java:313)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry$RegisteredCancellable.run(DefaultShutdownCallbackRegistry.java:109)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry.run(DefaultShutdownCallbackRegistry.java:74)
	at java.lang.Thread.run(Thread.java:834)
	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:517)
	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:193)
ERROR StatusLogger Could not unregister MBeans for org.apache.logging.log4j2:type=Default,component=ContextSelector
 java.lang.NullPointerException
	at org.apache.logging.log4j.core.jmx.Server.unregisterAllMatching(Server.java:337)
	at org.apache.logging.log4j.core.jmx.Server.unregisterContextSelector(Server.java:295)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:265)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:249)
	at org.apache.logging.log4j.core.LoggerContext.stop(LoggerContext.java:372)
	at org.apache.logging.log4j.core.LoggerContext$1.run(LoggerContext.java:313)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry$RegisteredCancellable.run(DefaultShutdownCallbackRegistry.java:109)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry.run(DefaultShutdownCallbackRegistry.java:74)
	at java.lang.Thread.run(Thread.java:834)
	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:517)
	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:193)
ERROR StatusLogger Could not unregister MBeans for org.apache.logging.log4j2:type=Default,component=Loggers,name=*
 java.lang.NullPointerException
	at org.apache.logging.log4j.core.jmx.Server.unregisterAllMatching(Server.java:337)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerConfigs(Server.java:301)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:266)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:249)
	at org.apache.logging.log4j.core.LoggerContext.stop(LoggerContext.java:372)
	at org.apache.logging.log4j.core.LoggerContext$1.run(LoggerContext.java:313)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry$RegisteredCancellable.run(DefaultShutdownCallbackRegistry.java:109)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry.run(DefaultShutdownCallbackRegistry.java:74)
	at java.lang.Thread.run(Thread.java:834)
	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:517)
	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:193)
ERROR StatusLogger Could not unregister MBeans for org.apache.logging.log4j2:type=Default,component=Appenders,name=*
 java.lang.NullPointerException
	at org.apache.logging.log4j.core.jmx.Server.unregisterAllMatching(Server.java:337)
	at org.apache.logging.log4j.core.jmx.Server.unregisterAppenders(Server.java:313)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:267)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:249)
	at org.apache.logging.log4j.core.LoggerContext.stop(LoggerContext.java:372)
	at org.apache.logging.log4j.core.LoggerContext$1.run(LoggerContext.java:313)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry$RegisteredCancellable.run(DefaultShutdownCallbackRegistry.java:109)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry.run(DefaultShutdownCallbackRegistry.java:74)
	at java.lang.Thread.run(Thread.java:834)
	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:517)
	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:193)
ERROR StatusLogger Could not unregister MBeans for org.apache.logging.log4j2:type=Default,component=AsyncAppenders,name=*
 java.lang.NullPointerException
	at org.apache.logging.log4j.core.jmx.Server.unregisterAllMatching(Server.java:337)
	at org.apache.logging.log4j.core.jmx.Server.unregisterAsyncAppenders(Server.java:319)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:268)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:249)
	at org.apache.logging.log4j.core.LoggerContext.stop(LoggerContext.java:372)
	at org.apache.logging.log4j.core.LoggerContext$1.run(LoggerContext.java:313)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry$RegisteredCancellable.run(DefaultShutdownCallbackRegistry.java:109)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry.run(DefaultShutdownCallbackRegistry.java:74)
	at java.lang.Thread.run(Thread.java:834)
	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:517)
	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:193)
ERROR StatusLogger Could not unregister MBeans for org.apache.logging.log4j2:type=Default,component=AsyncLoggerRingBuffer
 java.lang.NullPointerException
	at org.apache.logging.log4j.core.jmx.Server.unregisterAllMatching(Server.java:337)
	at org.apache.logging.log4j.core.jmx.Server.unregisterAsyncLoggerRingBufferAdmins(Server.java:325)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:269)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:249)
	at org.apache.logging.log4j.core.LoggerContext.stop(LoggerContext.java:372)
	at org.apache.logging.log4j.core.LoggerContext$1.run(LoggerContext.java:313)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry$RegisteredCancellable.run(DefaultShutdownCallbackRegistry.java:109)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry.run(DefaultShutdownCallbackRegistry.java:74)
	at java.lang.Thread.run(Thread.java:834)
	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:517)
	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:193)
ERROR StatusLogger Could not unregister MBeans for org.apache.logging.log4j2:type=Default,component=Loggers,name=*,subtype=RingBuffer
 java.lang.NullPointerException
	at org.apache.logging.log4j.core.jmx.Server.unregisterAllMatching(Server.java:337)
	at org.apache.logging.log4j.core.jmx.Server.unregisterAsyncLoggerConfigRingBufferAdmins(Server.java:331)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:270)
	at org.apache.logging.log4j.core.jmx.Server.unregisterLoggerContext(Server.java:249)
	at org.apache.logging.log4j.core.LoggerContext.stop(LoggerContext.java:372)
	at org.apache.logging.log4j.core.LoggerContext$1.run(LoggerContext.java:313)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry$RegisteredCancellable.run(DefaultShutdownCallbackRegistry.java:109)
	at org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry.run(DefaultShutdownCallbackRegistry.java:74)
	at java.lang.Thread.run(Thread.java:834)
	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:517)
	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:193)

```
