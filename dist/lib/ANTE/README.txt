ANTE Framework, March 2013, Copyright LIACC/FEUP
------------------------------------------------

This file explains how to use the ANTE framework. It also briefly explains how to develop and lauch additional agents that are to be run within this environment.

To use the ANTE framework in your own project, you should:

1- Create a Java project in your IDE (e.g. Eclipse).

2- Copy the contents of the ANTE subfolder into your project.

3- Add all .jar files included in the lib subfolder to the Java build path of your project.

4- In order to launch ANTE, execute the ei.ElectronicInstitution class of ANTE.jar, providing as an argument an appropriate XML configuration file. Examples are included in the config\scenarios subfolder. For example, you may use the argument config\scenarios\textiles\ei-config.xml

To create your own agents, you may extend the ei.EIAgent class.

To create user agents (representing enterprises), you may extend the ei.agent.enterpriseagent.EnterpriseAgent or one of its subclasses (namely ei.agent.enterpriseagent.enactment.AutomaticEnterpriseAgent).

To launch automatically your own agents at ANTE startup, you can simply add them to the XML configuration file. As exemplified in the configuration file mentioned above, you may provide arguments to your agents. See the documentation of ei.ElectronicInstitution for details, or inspect ei-config.dtd which is in the config subfolder.

If you want to launch agents in a different (possibly remote) container, you can run the ei.EAgLauncher class, providing a configuration file as above. An example is config\scenarios\textiles\ea-config.xml which includes a usage of the _host_ and _port_ JADE parameters.

Any inquiries should be sent to hlc@fe.up.pt
