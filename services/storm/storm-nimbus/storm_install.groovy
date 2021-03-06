/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import static Shell.*;
import java.util.concurrent.TimeUnit
import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import java.net.InetAddress;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory


context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("storm-service.properties").toURL())

def service = null

while (service == null)
{
   println "Locating zookeeper service...";
   service = context.waitForService("zookeeper", 120, TimeUnit.SECONDS)
}
def zooks = null;
def rowCount=0;
while(zooks==null)
{
   println "Locating zookeeper service instances. Expecting " + service.getNumberOfPlannedInstances();
   zooks = service.waitForInstances(service.getNumberOfPlannedInstances(), 120, TimeUnit.SECONDS )
}

println "Found ${zooks.length} zookeeper nodes"

def nimbus = InetAddress.localHost.hostAddress

def binding=["zooks":zooks,"nimbus":nimbus]
def yaml = new File('templates/storm.yaml')
engine = new SimpleTemplateEngine()
template = engine.createTemplate(yaml).make(binding)

sh "chmod +x initnode.sh"
sh "./initnode.sh"

new AntBuilder().sequential {
	mkdir(dir:"${config.installDir}")
	get(src:"http://maven-repository.cloudifysource.org/storm/starter/storm-starter/0.0.1-SNAPSHOT/storm-starter-0.0.1-SNAPSHOT.jar", dest:"commands", skipexisting:true)
	get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
	unzip(src:"${config.installDir}/${config.zipName}", dest:config.installDir, overwrite:true)
	//dos2unix on the linux script files
	fixcrlf(srcDir:"${config.installDir}/${config.name}/bin", eol:"lf", eof:"remove", excludes:"*.bat *.jar")
	delete(file:"${config.installDir}/${config.zipName}")

   //templates start scripts
	chmod(file:"${config.installDir}/${config.name}/bin/storm", perm:'ugo+rx')
	chmod(dir:"${config.installDir}/${config.name}/bin", perm:'ugo+rx', includes:"*.sh")
	chmod(dir:"commands", perm:'ugo+rx', includes:"*.sh")
	delete(file:"${config.installDir}/${config.name}/conf/storm.yaml")
}

new File("${config.installDir}/${config.name}/conf/storm.yaml").withWriter{ out->
  out.write(template.toString())
}

=======
/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import static Shell.*;
import java.util.concurrent.TimeUnit
import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import java.net.InetAddress;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory


context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("storm-service.properties").toURL())

def service = null

while (service == null)
{
   println "Locating zookeeper service...";
   service = context.waitForService("zookeeper", 120, TimeUnit.SECONDS)
}
def zooks = null;
def rowCount=0;
while(zooks==null)
{
   println "Locating zookeeper service instances. Expecting " + service.getNumberOfPlannedInstances();
   zooks = service.waitForInstances(service.getNumberOfPlannedInstances(), 120, TimeUnit.SECONDS )
}

println "Found ${zooks.length} zookeeper nodes"

def nimbus = InetAddress.localHost.hostAddress
def hostName= InetAddress.localHost.hostName

def binding=["zooks":zooks,"nimbus":nimbus,"hostName":hostName]
def yaml = new File('templates/storm.yaml')
engine = new SimpleTemplateEngine()
template = engine.createTemplate(yaml).make(binding)

sh "chmod +x initnode.sh"
sh "./initnode.sh"

new AntBuilder().sequential {
	mkdir(dir:"${config.installDir}")
	get(src:"http://maven-repository.cloudifysource.org/storm/starter/storm-starter/0.0.1-SNAPSHOT/storm-starter-0.0.1-SNAPSHOT.jar", dest:"commands", skipexisting:true)
	get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
	unzip(src:"${config.installDir}/${config.zipName}", dest:config.installDir, overwrite:true)
	//dos2unix on the linux script files
	fixcrlf(srcDir:"${config.installDir}/${config.name}/bin", eol:"lf", eof:"remove", excludes:"*.bat *.jar")
	delete(file:"${config.installDir}/${config.zipName}")

   //templates start scripts
	chmod(file:"${config.installDir}/${config.name}/bin/storm", perm:'ugo+rx')
	chmod(dir:"${config.installDir}/${config.name}/bin", perm:'ugo+rx', includes:"*.sh")
	chmod(dir:"commands", perm:'ugo+rx', includes:"*.sh")
	delete(file:"${config.installDir}/${config.name}/conf/storm.yaml")


	//add host entry
	exec(executable:"commands/addhost.sh", osfamily:"unix") {
		arg(line:"${context.privateAddress} ${InetAddress.localHost.hostName}")
	}
}

new File("${config.installDir}/${config.name}/conf/storm.yaml").withWriter{ out->
  out.write(template.toString())
}


