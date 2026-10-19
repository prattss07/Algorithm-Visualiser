# Algorithm Visualiser (Java 17+, Swing)

Animated sorting visualiser: 7 algorithms, play/pause/step/stop, speed control, sound,
custom arrays, and a Race mode that runs two algorithms side by side on the same data.

## Build & run
    javac -encoding UTF-8 -d out $(find src -name "*.java")
    java -cp out visualiser.ui.Main

(Windows PowerShell: `javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName`)

## Packages
- visualiser.model      ArrayModel (instrumented, thread-safe array), PlaybackControl (pause/step/cancel/speed),
                        Snapshot, ArrayGenerator, ArrayParser, exceptions, OperationListener
- visualiser.algorithms SortingAlgorithm (abstract) + Bubble, Selection, Insertion, Shell, Merge, Quick, Heap
- visualiser.engine     SortRunner (worker-thread task), RunState
- visualiser.audio      SoundPlayer (dedicated audio thread)
- visualiser.ui         MainFrame, AlgorithmPanel (one lane), BarPanel (painting)

## Threads
- EDT: all Swing code and a 30 ms refresh timer that paints Snapshots.
- One "sorter" thread per lane: runs the algorithm, paced by PlaybackControl.checkpoint().
- One "sound-writer" thread: plays blips from a bounded queue.

## Adding an algorithm
Extend SortingAlgorithm, use only the ArrayModel operations (less / lessThan / swap / set /
markSorted / setPivot), then add it to AlgorithmRegistry.all().
