#!/bin/sh

function die() {
  echo $*
  exit 1
}

test -x $JAVA_HOME/bin/java || die "missing or wrong definition for JAVA_HOME"

HOME_DIR=`dirname $0`
OPT="-Xmx128m -Dxml.dir=$HOME_DIR/xml -Dlog.dir=$HOME_DIR/log -Djava.ext.dirs=$HOME_DIR/lib"

nohup $JAVA_HOME/bin/java $OPT com.iv.logView.Main $* &>/dev/null &
