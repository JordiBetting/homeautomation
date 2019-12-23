# Java home automation framework using Domoticz
#### Under active development
This framework aims to improve the usability of domoticz for Java developers. The following goals are defined for the framework:
- Enable testing of rules
- Reproducible build and deployment
- Improved troubleshooting (tracing why a certain action happened)

Non-content related goals:
- Personal learning and improvement (e.g. on Java, web technology, ci/cd)
- Environment for learning new techniques (e.g. new Java versions, software architecture spikes, docker)
- Fun! Hey, I'm an architect and engineer who likes to over engineer things to learn!

Potential future uses:
- Replacement of Domoticz by directly supporting the api of for example openzwave and the Hue bridge
- Addition of user-friendly user interface

TODO:
- Example folder

## Usage
**Build**

The Jenkinsfile 'documents' how the build is arranged. Basic principle: reproducible build using baselined tooling.

The build allows for publication into a maven repository. It is used for testing and integrating the library. This requires a gradle.properties files with at least the following settings
```
repoUser=myUser
repoPassword=myPassword
repoURL=https://mavenrepo.mydomain/repository/myRepo/
```
**Branches**

The branch strategy is:
- master is used for publication and release. 
- development is not in use. Once the framework is 'fit for release', this will be introduced.
- buildagent, containing the definition of the docker container as build node in the Jenkinsfile
- others, are either development, tests, learning or whatsoever.
