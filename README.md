# ALQ
Adaptive Ladder Queue implementation.<br /> It is an updated version of the Ladder Queue introduced by W.T.Tang and R.S.M.Goh (https://dl.acm.org/citation.cfm?id=1103324). Its main goal is to achieve O(1) theoretical complexity in real-world performance under various workloads. The Classic Hold model has been adopted to create the event queue. It implies that each dequeue operation is followed by an enqueue one.


## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 


### Prerequisites
Unix system reccomended.

Please install:<br />
-Java runtime environment version: 1.8.0_xxx.<br />
-Apache Commons Math 3.6.1 (Apache License 2.0) http://commons.apache.org/proper/commons-math/


### Settings
After the installation of all the prerequisites, please check /compile.sh and /bin/run.sh files.

In run.sh, it's possible to configure different values (START, END, INCR, LIMIT) in order to have a dynamic test configuration. 

For example:<br />
  START=100<br />
  END=900<br />
  INCR=100<br />
  LIMIT=10000000<br />
The above configuration evaluates the different data structure adopted (LadderQueue, Priority Queue, Adaptive Ladder Queue), ranging from queue size of 'START'=100 up to 'LIMIT'=10 millioon, following a logarithmic scale. 'END' and 'INCR' help to define and to handle the experiments in logaritmic scale. Varying these two variables it is possible to rearrange the scale.

It is possible to choose the distribution to test.<br />
8 distributions are considered and identified as follows:<br />
1) Exponential (λ = 1.0)<br />
2) Exponential (λ = 1.0 / 3000.0)<br />
3) Pareto(xm = 1, α = 1)<br />
4) Pareto(xm = 1, α = 1.5)<br />
5) Pareto(xm = 1, α = 700)<br />
6) Change(A = Exponential (1), B = Triangular (90000, 100000),n = 2000)<br />
7) Camel(x = 0.001 , y = 0.999)<br />
8) Bimodal(X) = 9.95238 · X + 9.95238, if X < 0.1<br />
	or<br />
   Bimodal(X) = 9.95238 · X + 0, otherwise<br />
 
In run.sh it is possible to set distribution number in 'DISTR', considering the desired function.


Moreover, in it.unical.dimes.elq.test.Test.java it is possible to set:<br />
- number of executions -->  'TOT'                           |<br />
                                                            |---> for every distribution and size<br />  
- number of thrown away executions --> 'SCRAP'              |<br />
                                                             <br />
- number of accesses in it.unical.dimes.elq.test.Test.java modifying variable 'accesses'.<br />


## Running the tests
To run the tests, first compile the code using the compile.sh file located in the main folder; second check variables values and eventually modify them as you wish and execute run.sh in bin folder. 


### Break down into end to end tests
Tests could take a considerable amount of time. 
Each generated .csv file contains information about all data structures analysed with details regarding average time execution for every distribution and size.


## Thanks

Ludovica Sacco [@ludvi](https://github.com/ludvi) for code fixes and improvements


## Contribution



Copyright (C) Angelo Furfaro, [<img src="https://www.gnu.org/graphics/lgplv3-88x31.png" alt="LGPL v3"/>](LICENSE).
