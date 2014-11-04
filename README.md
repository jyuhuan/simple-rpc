# Simple RPC

## Overview

This is a course project for CS2510 - Advanced Operating System. The work is licensed under [a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License][2].

The library works as a framework that supports <strong>r</strong>emote <strong>p</strong>rocedure <strong>c</strong>alls (<strong>RPC</strong>s). The code demonstrates how to implement a simple mathematical library (`MathLib`) using this RPC framework. 


## Highlights

RPC is a simple idea. What components does an RPC framework have and how it works can be found on [Wikipedia][1]. This `readme` file only lists the highlights of this framework.

1. <strong>Port mapper</strong>: maintains information in a table that supports concurrent reads and writes about which server supports which procedure. This table is updated in two possible ways:
	1. When servers launch, they register the procedures they support at the port mapper. The port mapper adds these procedures and the information of the server to the table. 
	2. The port mapper periodically checks the availability of the servers. Once a server becomes too irreponsive (either too slow or died), they are removed from the table. 
	
	The port mapper allows replicated servers to register the same procedures. This will enable load balancing among the servers. 
	
	All tasks on the port mapper are done in threads. The port mapper's listener only listens and determines the task requested, and dispatches the task by creating new threads. Two public tasks, `PortMapperLookupWorker` and `PortMapperRegistrationWorker` , are supported for the client and server to lookup and register repectively. One private task, `TableEntryValidationWorker` is used by the port mapper to periodically check the availability of the procedures.
	
2. <strong>Server</strong>: Replicated servers are supported. Failure of one of the server replica will not affect the client. 

	All procedure executions on the server are done in threads. The server's listener only listens and determines the procedure to be executed, and dispatches the execution by creating new threads.
	
	A special task, `ProcedureAvailabilityCheckWorker`, is supported by the server to allow the port mapper and client to check whether a procedure is supported by the server. 


3. <strong>Client Stub</strong>: The client stub tries to contact the server to execute the procedure for a configurable maximum number of times before giving up and informing the client program the server failure. 

4. <strong>Reliable UDP Transmission Layer</strong>: The communication between all components are done via TCP. Only the procedure parameters are transmitted using a reliable UDP mechanism between a client and a server. This RPC framework deals with large data (i.e., parameters of procedures) by handing the data over to a reliable UDP transmission layer (named `ReliableUdpTransporter` in code) to handle. This layer segments the data, and transmits these segments in the style of <strong>burst acknowledgment</strong>. To ensure that the data segments are not lost, this Reliable UDP layer adds the concept of <strong>sequence number</strong>, and <strong>socket timeout</strong> in TCP on top of UDP. This gives the UDP layer a slight flavor of some TCP reliability. 


[1]: https://en.wikipedia.org/wiki/Remote_procedure_call
[2]: http://creativecommons.org/licenses/by-nc-nd/4.0/