Because default Eclispe projects import for IntelliJ Idea doesn't have enough options. And I need to import bunch of Eclipse projects to Idea modules. On a constant basis.

Usage
-----

.classpath import: ```java -jar clj-ideagen.jar path/to/.classpath -o ideaproj.iml -x target,bin```

Generating project file: ```java -cp clj-ideagen.jar ideagen.project path/to/scan/dir -o where/to/put/.idea```
