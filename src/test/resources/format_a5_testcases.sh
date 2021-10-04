#!/usr/bin/env bash

dir="assignment_testcases/a5"
for f in ${dir}/*.java; do
  echo $f
  cls=`echo $f | cut -d / -f 3 | head -c -6`
  java A5Driver $dir $cls 1> ${dir}/${cls}.java.stdout
  echo $? > ${dir}/${cls}.java.status
done
for f in `find $dir -mindepth 1 -maxdepth 1 -type d`; do
  echo $f
  java A5Driver $f Main 1> ${f}.stdout
  echo $? > ${f}.status
done
