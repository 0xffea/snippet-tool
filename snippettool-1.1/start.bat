@echo off
set mvnbin=apache-maven-3.0\bin
@echo on
%mvnbin%\mvn.bat mvn exec:java
