# WebLogicStuckThreadAlert
A tool that helps to assess and alert WebLogic status for following conditions, along with ThreadDump specific to a those impacted WebLogic cluster(s).

1. Stuck thread detection.
2. Chance of getting into Stuck Thread situations - using Thread execution ratio property.
3. Available memory becomes less than the configured percentage in property. 

Following dependencies need to be resolved for successful compilation and execution:
1. This jar requires WebLogic Full client for establishing JMX connection with Admin server.
Please refer this link for steps to prepare WebLogic Full Client and place in the 'lib' folder.
http://middlewaremagic.com/weblogic/?p=558

2.Supplying the property file location during runtime.

E.g. 

    java -jar -Dcom.serverstate.monitorconfig={path/to}/system.properties {path/to}/ServerState.jar &


Using Crontab setup, it can easily be scheduled for periodical alerts.

E.g. For executing for every 20 minutes.
    
    00,20,40  * * * *  java -jar -Dcom.serverstate.monitorconfig={path/to}/system.properties {path/to}/ServerState.jar &
