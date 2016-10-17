# <img src="https://raw.githubusercontent.com/joffrey-bion/fx-log/master/src/deploy/package/icon.png" alt="FX Log" height=27/> FX Log

[![Bintray](https://img.shields.io/bintray/v/joffrey-bion/applications/fx-log.svg)](https://bintray.com/joffrey-bion/applications/fx-log/_latestVersion)
[![Travis Build](https://img.shields.io/travis/joffrey-bion/fx-log/master.svg)](https://travis-ci.org/joffrey-bion/fx-log)
[![Dependency Status](https://www.versioneye.com/user/projects/56c99d2d18b27104252dcc77/badge.svg)](https://www.versioneye.com/user/projects/56c99d2d18b27104252dcc77)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/joffrey-bion/fx-log/blob/master/LICENSE)

A clean and free log viewer.

## Download

You can find the executable Jar in the
[FX Log bintray repository](https://bintray.com/joffrey-bion/applications/fx-log/).

## What does it look like?

Light Theme & Light Colorizer                                     |  Dark Theme & Dark Colorizer
:----------------------------------------------------------------:|:----------------------------------------------:
![Main view (light theme)](https://raw.githubusercontent.com/joffrey-bion/fx-log/master/doc/screenshots/main_light_theme.png)  |  ![Main view (dark theme)](https://raw.githubusercontent.com/joffrey-bion/fx-log/master/doc/screenshots/main_dark_theme.png)

## Features

### Tailing

FX Log follows the end of your file and streams in real time.

### Filtering

Choose the logs that matter to you at any time.

### Columnization

Use regular expressions to parse your raw log lines into nice columns. Built-in support is already included for
standard server logs like Weblogic and Apache.

Custom columnizer                                                   |  Select interesting columns
:------------------------------------------------------------------:|:----------------------------------------------:
![Customize Columnizers](https://raw.githubusercontent.com/joffrey-bion/fx-log/master/doc/screenshots/customize_columnizers.png) | ![Show/hide columns](https://raw.githubusercontent.com/joffrey-bion/fx-log/master/doc/screenshots/hide_columns.png)

### Coloration

Use regular expressions on raw log lines or column values to change the style of some logs:

<img alt="Customize Colorizers" src="https://raw.githubusercontent.com/joffrey-bion/fx-log/master/doc/screenshots/customize_colorizers.png" height="500"/>

## Report issues - Suggest improvements

You can suggest improvements or report problems in the
[GitHub issues page](https://github.com/joffrey-bion/fx-log/issues).

## Contribute

### Required Software

- [Git LFS extension](https://git-lfs.github.com/) (for big sample log files)

- [Gradle](http://gradle.org/gradle-download/) (at least version 2.7)

- [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (at least jdk8_u40)

- [optional] [Java FX Scene Builder](http://gluonhq.com/open-source/scene-builder/) (pretty useful to edit
the views)

### Coding style

For your pull-request to build, your code has to respect the checkstyle config in `config/checkstyle`.

## Copyright and License [![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/joffrey-bion/fx-log/blob/master/LICENSE)

Copyright (c) 2016 Joffrey Bion. Code released under
[the MIT license](https://github.com/joffrey-bion/fx-log/blob/master/LICENSE)
