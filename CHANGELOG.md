# Change Log

## [v3.1.0](https://bintray.com/joffrey-bion/applications/fx-log/3.1.0) (2016-11-01)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v3.0.0...v3.1.0)

**Implemented enhancements:**

- Display current search match and total matches count [\#94](https://github.com/joffrey-bion/fx-log/issues/94)
- Add "visible" field to columns edition table [\#90](https://github.com/joffrey-bion/fx-log/issues/90)
- Add "duplicate" option in list of columnizers/colorizers and rules [\#88](https://github.com/joffrey-bion/fx-log/issues/88)
- Built-in columnizers naming [\#72](https://github.com/joffrey-bion/fx-log/issues/72)

**Fixed bugs:**

- Freeze when loading large file [\#95](https://github.com/joffrey-bion/fx-log/issues/95)
- Built-in weblogic columnizer does not recognize sessionId anymore [\#89](https://github.com/joffrey-bion/fx-log/issues/89)

## [v3.0.0](https://bintray.com/joffrey-bion/applications/fx-log/3.0.0) (2016-10-17)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v2.1.0...v3.0.0)

**Implemented enhancements:**

- Add auto-complete data for column ID fields [\#85](https://github.com/joffrey-bion/fx-log/issues/85)
- Add memory usage info [\#82](https://github.com/joffrey-bion/fx-log/issues/82)
- Make secondary stages stay on top while tuning [\#81](https://github.com/joffrey-bion/fx-log/issues/81)
- Add a "reload file" button [\#75](https://github.com/joffrey-bion/fx-log/issues/75)
- Search/highlight without filtering [\#35](https://github.com/joffrey-bion/fx-log/issues/35)
- Add re-ordering for customization lists [\#23](https://github.com/joffrey-bion/fx-log/issues/23)

**Fixed bugs:**

- Indentation in Stacktraces is not preserved [\#87](https://github.com/joffrey-bion/fx-log/issues/87)
- "Stick to bottom" icon looks like it will expand some menu [\#78](https://github.com/joffrey-bion/fx-log/issues/78)
- Following tail stops after clearing logs [\#60](https://github.com/joffrey-bion/fx-log/issues/60)
- Scrolling down on mouse causes Scroll button to uncheck [\#38](https://github.com/joffrey-bion/fx-log/issues/38)

## [v2.1.0](https://bintray.com/joffrey-bion/applications/fx-log/2.1.0) (2016-09-29)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v2.0.0...v2.1.0)

**Implemented enhancements:**

- Limit number of logs displayed [\#61](https://github.com/joffrey-bion/fx-log/issues/61)
- Add context menu on logs with basic options [\#51](https://github.com/joffrey-bion/fx-log/issues/51)
- Add clear button in the filter [\#50](https://github.com/joffrey-bion/fx-log/issues/50)
- Provide parameters to override tailing settings [\#31](https://github.com/joffrey-bion/fx-log/issues/31)

## [v2.0.0](https://bintray.com/joffrey-bion/applications/fx-log/2.0.0) (2016-09-25)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.7...v2.0.0)

**Implemented enhancements:**

- Separate styles from style rules [\#63](https://github.com/joffrey-bion/fx-log/issues/63)
- Use icons for buttons like "add"/"delete" [\#22](https://github.com/joffrey-bion/fx-log/issues/22)

**Fixed bugs:**

- View not updated when changing selected colorizer [\#66](https://github.com/joffrey-bion/fx-log/issues/66)
- Exiting the program does not close secondary windows [\#65](https://github.com/joffrey-bion/fx-log/issues/65)
- Regex editing is not instantly taken into account anymore [\#64](https://github.com/joffrey-bion/fx-log/issues/64)
- View not updated when changing color rule regex [\#62](https://github.com/joffrey-bion/fx-log/issues/62)

## [v1.7](https://bintray.com/joffrey-bion/applications/fx-log/1.7) (2016-04-26)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.6...v1.7)

**Implemented enhancements:**

- Log clear feature [\#59](https://github.com/joffrey-bion/fx-log/issues/59)
- Secondary windows: bring to front instead of disabling button [\#55](https://github.com/joffrey-bion/fx-log/issues/55)
- Add the version number in the Help menu [\#42](https://github.com/joffrey-bion/fx-log/issues/42)
- FX Log updates notification mechanism [\#33](https://github.com/joffrey-bion/fx-log/issues/33)

**Merged pull requests:**

- Added a new Columnizer for Amadeus input.log files [\#58](https://github.com/joffrey-bion/fx-log/pull/58) ([lwouis](https://github.com/lwouis))

## [v1.6](https://bintray.com/joffrey-bion/applications/fx-log/1.6) (2016-04-04)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.5...v1.6)

**Implemented enhancements:**

- Add config format version concept [\#56](https://github.com/joffrey-bion/fx-log/issues/56)

**Fixed bugs:**

- NPE when opening a file or changing theme [\#57](https://github.com/joffrey-bion/fx-log/issues/57)

## [v1.5](https://bintray.com/joffrey-bion/applications/fx-log/1.5) (2016-03-14)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.4.2...v1.5)

**Implemented enhancements:**

- Missing File name and path in the UI [\#48](https://github.com/joffrey-bion/fx-log/issues/48)

**Fixed bugs:**

- Colorizer background color has no effect [\#54](https://github.com/joffrey-bion/fx-log/issues/54)
- Colorizer's "stacktrace" rule does not match first line [\#52](https://github.com/joffrey-bion/fx-log/issues/52)
- Weblogic's columnizer fails on messages containing \<\> [\#49](https://github.com/joffrey-bion/fx-log/issues/49)
- Colors not updating immediately [\#21](https://github.com/joffrey-bion/fx-log/issues/21)

## [v1.4.2](https://bintray.com/joffrey-bion/applications/fx-log/1.4.2) (2016-02-23)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.4.1...v1.4.2)

**Implemented enhancements:**

- Refine CSS for dark theme [\#7](https://github.com/joffrey-bion/fx-log/issues/7)

**Fixed bugs:**

- Resizing down hides the scrollbars [\#47](https://github.com/joffrey-bion/fx-log/issues/47)

## [v1.4.1](https://bintray.com/joffrey-bion/applications/fx-log/1.4.1) (2016-02-20)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.4...v1.4.1)

**Fixed bugs:**

- Log colors shift when changing filter state [\#45](https://github.com/joffrey-bion/fx-log/issues/45)

**Closed issues:**

- Project first import issue on Intellij [\#6](https://github.com/joffrey-bion/fx-log/issues/6)

## [v1.4](https://bintray.com/joffrey-bion/applications/fx-log/1.4) (2016-02-19)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.3...v1.4)

**Implemented enhancements:**

- Drag & Drop the file to Open [\#39](https://github.com/joffrey-bion/fx-log/issues/39)
- Filter field should be case-insentive by default [\#37](https://github.com/joffrey-bion/fx-log/issues/37)

## [v1.3](https://bintray.com/joffrey-bion/applications/fx-log/1.3) (2016-02-18)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.2...v1.3)

**Implemented enhancements:**

- Customize logs text font and size [\#36](https://github.com/joffrey-bion/fx-log/issues/36)
- Toggle "open last file" via a preference [\#12](https://github.com/joffrey-bion/fx-log/issues/12)
- Toggle "skip empty lines" via a preference [\#11](https://github.com/joffrey-bion/fx-log/issues/11)
- Remove blue border on the data table when its focused [\#2](https://github.com/joffrey-bion/fx-log/issues/2)

## [v1.2](https://bintray.com/joffrey-bion/applications/fx-log/1.2) (2016-02-11)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.1...v1.2)

**Implemented enhancements:**

- Remember last selected theme [\#13](https://github.com/joffrey-bion/fx-log/issues/13)
- Add headers when "copying with columns" [\#9](https://github.com/joffrey-bion/fx-log/issues/9)
- Make the scrollbar larger and more visible [\#4](https://github.com/joffrey-bion/fx-log/issues/4)

**Fixed bugs:**

- Tailing doesn't stop when deactivated [\#29](https://github.com/joffrey-bion/fx-log/issues/29)
- Removing your filter should bring you to last line [\#28](https://github.com/joffrey-bion/fx-log/issues/28)

## [v1.1](https://bintray.com/joffrey-bion/applications/fx-log/1.1) (2016-02-10)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v1.0...v1.1)

**Implemented enhancements:**

- Colorizer Customization [\#24](https://github.com/joffrey-bion/fx-log/issues/24)
- Rename feature for columnizer [\#20](https://github.com/joffrey-bion/fx-log/issues/20)
- Rename feature for colorizers [\#19](https://github.com/joffrey-bion/fx-log/issues/19)
- Delete option in columnizers [\#17](https://github.com/joffrey-bion/fx-log/issues/17)
- Columnizer customization [\#16](https://github.com/joffrey-bion/fx-log/issues/16)
- Delete option in colorizers [\#15](https://github.com/joffrey-bion/fx-log/issues/15)
- Add a "snap to tail" UI [\#3](https://github.com/joffrey-bion/fx-log/issues/3)

**Fixed bugs:**

- Can't edit column definitions [\#25](https://github.com/joffrey-bion/fx-log/issues/25)
- Resources broken in deployed JAR [\#14](https://github.com/joffrey-bion/fx-log/issues/14)
- Regex validation for raw filter [\#10](https://github.com/joffrey-bion/fx-log/issues/10)
- Copy-paste with columns to Excel adds blank lines [\#8](https://github.com/joffrey-bion/fx-log/issues/8)

## [v1.0](https://bintray.com/joffrey-bion/applications/fx-log/1.0) (2016-02-07)
[Full Changelog](https://github.com/joffrey-bion/fx-log/compare/v0.9...v1.0)



\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*