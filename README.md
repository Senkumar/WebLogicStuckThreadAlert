# WebLogicStuckThreadAlert
A tool that helps to assess and alert WebLogic status for following conditions, along with ThreadDump specific to a those impacted WebLogic cluster(s). Using Crontab setup it can easily be scheduled for periodical alerts.

1. Stuck thread detection.
2. Chance of getting into Stuck Thread situations - using Thread execution ratio property.
3. Available memory becomes less than the configured percentage in property. 

This jar requires WebLogic Full client for establishing JMX connection with Admin server.
Please refer this link for steps to prepare WebLogic Full Client and place in the 'lib' folder
http://middlewaremagic.com/weblogic/?p=558
