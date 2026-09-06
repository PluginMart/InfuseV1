#!/bin/bash

case "$1" in
  "26.1.1"|"26.1.2"|"26.2")
    echo "Switching to version $1"
    sed -i "s/minecraft = \".*\"\$/minecraft = \"$1\"/" gradle/libs.versions.toml ;;
  *)
    echo "Incompatible version '$1'.  Please use 26.1.1+" ;;
esac

