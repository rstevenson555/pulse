#!/bin/bash
export JAVA_HOME=/opt/art/java/jdk1.7.0_51
export PATH=$JAVA_HOME/bin:$CATALINA_HOME/bin:$PATH

nohup ant start-collector-sol -Dparam=-server -logfile ./ArtCollectorStdout.log &
