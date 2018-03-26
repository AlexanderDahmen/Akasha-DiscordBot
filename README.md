# Akasha-Bot #

Task-Management Discord Bot

Programmed by: Alexander Dahmen  
For: FacemannMcHomme#6479

## Description ##

This is a Discord-Bot for organizing tasks and events in your schedule.
It uses Discord to receive tasks and DMs to remind you of upcoming events.

## Features ##

* Creating tasks via a Discord Text-Message interface
  * Repeating tasks (every day/week/month/...)
  * Deadline tasks (in X hours/days/...)
* Query and alter tasks via a Discord Text-Message interface
  * Setting a task status (in progress, done, cancelled, ...)
* Send DMs to users to remind them of pending tasks and events

## Technology ##

This bot is a Java Application using [JDA](https://github.com/DV8FromTheWorld/JDA/)
(Java Discord API) to communicate with Discord.  
The user data and tasks are stored in a [MySQL](https://www.mysql.com/) Database,
which uses a schema migration with [FlywayDB](https://flywaydb.org/).
