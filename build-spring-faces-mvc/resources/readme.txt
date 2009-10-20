SPRING FACES 3.0.0.M1 (December 2008)
----------------------------------
http://www.springframework.org/faces

1. INTRODUCTION
---------------
Spring Faces is Spring's JSF integration project.  It provides support for Web Flow in a JSF environment, 
as well as a lightweight component library for progressive Ajax built on Spring Javascript.

2. RELEASE NOTES
----------------
Spring Faces 3 requires Java SE 5.0 and Spring Framework 3.0.0 or above to run.

Java SE 5.0 with Ant 1.7 is required to build.

Release distribution contents:

"." contains the Spring Faces distribution readme, license, changelog, and copyright
"dist" contains the Spring Faces distribution jar files
"src" contains the Spring Faces distribution source jar files
"docs" contains the Spring Faces reference manual and API Javadocs
"projects" contains all buildable projects, including sample applications
"projects/build-spring-faces" is the directory to access to build the Spring Faces distribution
"projects/spring-build" is the master build system used by all Spring projects, including Spring Faces
"projects/org.springframework.faces" contains buildable Spring Faces project sources

See the readme.txt within the above directories for additional information.

Spring Faces is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES
-------------------------
The following jar files are included in the distribution.
The contents of each jar and its dependencies are noted.
Dependencies in [brackets] are optional, and are just necessary for certain functionality.

* org.springframework.faces-3.0.0.M1.jar
- Contents: The Spring Faces library, containing Spring's integration with Java Server Faces (JSF) and additional JSF functionality.
- Dependencies: Spring MVC, Spring Web Flow, Spring JavaScript, JSF API

For an exact list of project dependencies, see each project's ivy file at "projects/${project_name}/ivy.xml".