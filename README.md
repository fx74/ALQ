# ALQ
[Adaptive Ladder Queue implementation](https://doi.org/10.1145/3200921.3200925).<br /> It is an updated version of the [Ladder Queue](https://dx.doi.org/10.1145/1103323.1103324) introduced by W.T.Tang and R.S.M.Goh . Its main goal is to achieve O(1) theoretical complexity in real-world performance under various workloads. The Classic Hold model has been adopted to create the event queue. It implies that each dequeue operation is followed by an enqueue one.
The Adaptive Ladder Queue is an effective implementation of the Pending Event Set (PES) in Descrete Event Simulation (DES). Its use is not confined to the simulation field, but the introduced data structure can be employed in other areas such as multimedia systems or image analysis, etc.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 


### Data Structure
The Adaptive Ladder Queue can be downloaded in its whole implementation directing the attention on the path ALQ/src/it/unical/dimes/elq/. In 'elq' folder the following classes are needed in order to reuse and import the data structure:

- ALadderQueue.java<br />
- AtomicEvent.java<br />
- CompositeEvent.java<br />	
- Event.java<br />	
- EventList.java<br />	
- LinkedEventList.java<br />

For datasets creation, please see ALQ/src/dataset/ and import all the package:
- Action.java
- Get.java
- Put.java

### Settings
#### ALadderQueue.java
Possibility to set in the constructor following variables:
```
private int THRES = 64; //Threshold to start spawning in Bottom tier
private int THRES_TOP = 16 * THRES; //Threshold to indicate max number of events in Top tier
private int MAX_RUNGS = 10; //Maximum number of Rungs
private final boolean grouping; //Handling grouping operation 
private final boolean upgrowing; //Handling upgrowing operation
private final boolean smartspawn; //Handling smartspawning operation
```
MAIN EXAMPLE
```
package ***;

import dataset.Action;
import it.unical.dimes.elq.ALadderQueue;
import it.unical.dimes.elq.AtomicEvent;
import it.unical.dimes.elq.CompositeEvent;
import it.unical.dimes.elq.Event;

public class Test {
	public static void main(String [] args){
		
		ALadderQueue alq=new ALadderQueue(); 
		
		ALadderQueue alq1=new ALadderQueue(true,true,true); 
		
		ALadderQueue alq2=new ALadderQueue(true,true,false,64,1024,10);
		
		Event evt=new AtomicEvent(2);
		
		alq.enqueue(evt);
		alq.dequeue();
		alq.getRungInsert();
		
		alq1.enqueue(evt);
		alq1.dequeue();
		alq1.getRungused();
		
		alq2.enqueue(evt);
		alq2.dequeue();
		alq2.getTopInsert();
		
	}
}
```

## Experiments
### Prerequisites
Unix system reccomended.

Please install:<br />
-Java runtime environment version: 1.8.0_xxx.<br />
-Apache Commons Math 3.6.1 (Apache License 2.0) http://commons.apache.org/proper/commons-math/
After the installation of all the prerequisites, please check /compile.sh and /bin/run.sh files.
### Test implementation
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
- number of executions -->  'TOT'      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|  <br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|---> for every distribution and size<br />  
- number of thrown away executions --> 'SCRAP'     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; |              <br />
<br />
- number of accesses in it.unical.dimes.elq.test.Test.java modifying variable 'accesses'.<br />


## Running the tests
To run the tests, first compile the code using the compile.sh file located in the main folder; second check variables values and eventually modify them as you wish and execute run.sh in bin folder. 


### Break down into end to end tests
Tests could take a considerable amount of time. 
Each generated .csv file contains information about all data structures analysed with details regarding average time execution for every distribution and size.


## Thanks

Ludovica Sacco [@ludvi](https://github.com/ludvi) for code fixes and improvements



## License

Copyright (C) Angelo Furfaro, [<img src="https://www.gnu.org/graphics/lgplv3-88x31.png" alt="LGPL v3"/>](LICENSE).

Please cite the following paper if you use ALQ in your scientific work:

Angelo Furfaro, Ludovica Sacco [Adaptive Ladder Queue: Achieving O(1) Amortized Access Time in Practice](https://doi.org/10.1145/3200921.3200925), SIGSIM-PADS '18 Proceedings of the 2018 ACM SIGSIM Conference on Principles of Advanced Discrete Simulation, Pages 101-104, Rome, Italy — May 23 - 25, 2018.
