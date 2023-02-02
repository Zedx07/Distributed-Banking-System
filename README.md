# Distributed-Banking-System
Client and Server connection


System Architecture:
![image](https://user-images.githubusercontent.com/76422167/216410118-30f1ddac-6979-4764-bbc2-fa2bc33eea5e.png)


Objectives
● To develop a client-server based Online Banking Distributed System with multipleservers
and clients.
● To establish a communication channel between client and remote server. ● To implement
Clock Synchronization of servers and clients to ensure all transactions are recorded with
the synchronized timestamp.
● To prevent race conditions and maintain process synchronization among servers
usingthe Mutual Exclusion algorithm.
● To maintain consistency of data with respect to transactions of clients.
● To prevent any server from being overwhelmed by Load Balancing

Outcomes
● Clients are successfully able to connect to the server using Remote Method Invocation
(RMI).
● The clocks of multiple servers and clients are synchronized using Cristian's Algorithm
before making requests to the server.
● The process synchronization is implemented among servers using the Suzuki MassMutual
Exclusion Algorithm.
● The load on multiple servers is managed by using Round Robin Load Balancing Algorithm.
● The strict consistency database is maintained using data replication.


[Project  Report.pdf](https://github.com/Shubham7-cell/Distributed-Banking-System/files/10571969/Project.Report.pdf)
