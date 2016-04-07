Text Java Console - CLI JConsole
================================

[![Join the chat at https://gitter.im/m-szalik/tjconsole](https://badges.gitter.im/m-szalik/tjconsole.svg)](https://gitter.im/m-szalik/tjconsole?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/m-szalik/tjconsole.svg?branch=master)](https://travis-ci.org/m-szalik/tjconsole)
[![codecov.io](https://codecov.io/github/m-szalik/tjconsole/coverage.svg?branch=master)](https://codecov.io/github/m-szalik/tjconsole?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/56e2b350df573d003a5f5be2/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56e2b350df573d003a5f5be2)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/7c50206d52624536aa54f1cdd9bbceb7)](https://www.codacy.com/app/szalik/tjconsole)

### About
TJConsole is a Text version of JConsole.
Perfect if you have only text console or terminal. You can use it also for communication between java programs and external software like shell scripts for example.

### Features

 * Display, update of JMX attributes
 * Invoke JMX operations (remote operations)
 * Connect to local and remote java applications (with SSL or credentials)
 * Remember recent remote connections
 * Auto completion with _tab_
 * Support for script files – load mxBeans instructions from external file    (currently only by sending commands to standard input - _stdin_)

### Requirements

 * Java Development Kit 6 or newer (tools.jar from JDK is required)

### License
Apache License 2.0

### Download
Download latest stable version [tjconsole-1.7](https://github.com/m-szalik/tjconsole/blob/master/tjconsole-1.7-all.jar?raw=true).

### Problems and questions
In case of problems or questions? [Create an issue](https://github.com/m-szalik/tjconsole/issues) on GitHub.

### Example
`java -jar tjconsole-all.jar -xterm -connect <java_process_id> -use <bean_name> -cmd <command_1> -cmd <command_2>`

`java -jar tjconsole-all.jar -xterm -connect 18478 -use java.lang:type=Threading -cmd get -cmd describe`
