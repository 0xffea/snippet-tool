@echo off
set mvnbin=apache-maven-3.0\bin
@echo on
call %mvnbin%\mvn.bat install:install-file -Dfile=lib\saxon9he.jar -DgroupId=net.sf.saxon -DartifactId=saxon9he -Dversion=9.2.1.2 -Dpackaging=jar -DgeneratePom=true
call %mvnbin%\mvn.bat install:install-file -Dfile=lib\colorchooser.jar -DgroupId=net.java.dev -DartifactId=colorchooser -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
call %mvnbin%\mvn.bat install:install-file -Dfile=lib\exist.jar -DgroupId=org.exist -DartifactId=exist -Dversion=1.5-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
call %mvnbin%\mvn.bat install:install-file -Dfile=lib\exist-optional.jar -DgroupId=org.exist -DartifactId=exist-optional -Dversion=1.5-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
call %mvnbin%\mvn.bat install:install-file -Dfile=lib\xmldb.jar -DgroupId=org.exist -DartifactId=xmldb -Dversion=1.5-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
%mvnbin%\mvn.bat clean package
