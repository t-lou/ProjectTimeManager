# ProjectTimeManager

I needed a simple tool to log my working time on different projects/tasks, but didn't find a handy one. Most apps
provide either too many unnecessary functionalities or are too easy to mess up (like delete by accident).

This toy project contains easy-to-use time logger with clear text log files (for manual modification) and GUI.

If Python is more available than JDK on your computer, a lite version for clock-in and report generation is: https://github.com/t-lou/clock_in.

# Start

- **Linux** run start.sh in terminal
- **Windows** double click start.bat

This program is written in Java, JDK is necessary for compilation and JRE for execution (please be aware of the paths on Windows).
It is tested on JDK 8 and OpenJDK 14.

# Build

run corresponding script in scripts

# Operations

- **left-mouse-button on buttons** for selection
- **right-mouse-button on background** for return

# Functions

- **clock in** clock in immediately and start a project with name YEAR-MONTH
- **start** start a new project (editable text field) or continue an existing project, hit ctrl-c when the work is
paused or ended
- **projects** show the sum of time for projects or select and show the log for one project
- **date** show the logs on different dates

# Report

Report for project (now only tested for clock-in) can be generated in RTF format.

# Logs

The logs are stored in .ptm_projects separately, one file for one project. Each line defines the start and end time for
one working session. The sessions must be timely consistent: ascending and no overlap (example below is outdated).

```bash
for i in $(ls .ptm_projects); do echo .ptm_projects/$i; cat .ptm_projects/$i; done
----------------------------------------------------------------------------------
.ptm_projects/common-vis.prt
20/07/2020 18:12:10 - 20/07/2020 20:21:19
21/07/2020 21:55:11 - 21/07/2020 23:49:08
.ptm_projects/study-aws.prt
21/07/2020 19:43:04 - 21/07/2020 21:49:18
22/07/2020 07:01:02 - 22/07/2020 07:29:22
```

# Screenshots

- main

![main](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/main-1.png)

- start

![start](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/start-1.png)

- projects

![projects](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/projects-1.png)
![projects](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/projects-2.png)

- date

![date](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/date-1.png)
![date](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/date-2.png)

- report examples

![report](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/report_google_doc.png)
![report](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/report_word.png)
![report](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/report_wordpad.png)
![report](https://github.com/t-lou/ProjectTimeManager/blob/master/doc/report_libreoffice.png)
