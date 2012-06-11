

//Get used kb's per experiment 
SELECT E.*, (SUM(P.Size)/1000) AS TotalKbSize FROM Experiments AS E, Packets AS P WHERE
E.ExperimentId = P.ExperimentId
GROUP BY E.ExperimentId ORDER BY E.Mode, E.nChanges, E.Iteration



//For all experiments, get the timings (EXTREMELY SLOW, use script)
SELECT DISTINCT E.*, (SELECT (UNIX_TIMESTAMP(D.Timestamp) - UNIX_TIMESTAMP(E.TimeStamp)) FROM Daemon AS D WHERE D.TimeStamp >= E.Timestamp AND D.Mode = E.Mode AND D.ExperimentId = E.ExperimentId ORDER BY D.TimeStamp DESC LIMIT 1) AS TimeDiff FROM Experiments AS E, Daemon AS D 
WHERE 1
ORDER BY E.Mode, E.nChanges, E.Iteration



//get results for a certain experimentId
SELECT E.*, D.*, P.* FROM Experiments AS E, Daemon AS E, Packets AS P 
WHERE 
E.ExperimentId = 292
AND D.ExperimentId = E.ExperimentId
AND P.ExperimentId = E.ExperimentId


//Get current stats of how many runs for how many modes
SELECT Mode, nChanges, COUNT(Iteration) as count FROM Experiments
WHERE 1
GROUP BY Mode, nChanges
