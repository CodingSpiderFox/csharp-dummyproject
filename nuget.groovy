def repositoryAddress () {
    return "http://${env.ARTIFACTORY_IP}/artifactory/api/nuget/"
}

def init() {
    println("Entered nuget init")
    try {
        exec("sources Remove -Name NuGet.org")
    } catch(any) {}
    try {
        addPackageSource("cab-nuget-extern",repositoryAddress()+ "cab-nuget-extern")
        addPackageSource("cab-nuget-ci",repositoryAddress()+ "cab-nuget-ci")
        addPackageSource("cab-nuget-stable",repositoryAddress()+ "cab-nuget-stable")
        addPackageSource("cab-nuget-rc",repositoryAddress()+ "cab-nuget-rc")
    } catch(any) {}
}
  
def runningOnKON() {
    try {
        return env.BOP_JENKINS_SYSTEM == "KON";
    } catch(any) {
        return false;
    }
}
  
def addPackageSource(name, url) {
    if(runningOnKON()) {
        name += "-kon"
        url += "-kon"
    }
    try {
        //always remove source so it isn't duplicated when set by a previous execution
        //should also help to update the username / password when crendentials of jenkins user in repositories change
        exec("sources Remove -Name ${name}", true)
    } catch (any) {}
     try {
        //add source and set username / password
        withCredentials([usernamePassword(credentialsId: 'cab-svn', passwordVariable: 'cred_pass', usernameVariable: 'cred_user')]) { 
            exec("sources Add -Name ${name} -Source ${url} -UserName ${cred_user} -Password ${cred_pass}", true)
        }
    }
    catch(any) {}    
}

def publish(config) {
    def packArgs = "";
    def publishingRepo = "";
   
    if (config.publishingChannel == "rc") {
        packArgs += "-suffix rc${config.rcVersion}"
        publishingRepo = repositoryAddress() + "cab-nuget-rc" //todo make global variables for repo paths. Maybe maintained at a central point in Jenkins
    }
    else if (config.publishingChannel == "stable") {
        publishingRepo = repositoryAddress() + "cab-nuget-stable"
    }
    else {
        def suffix = ""
        if(config.publishingChannel == "testing" || config.publishingChannel == "unstable") {
            suffix = "${config.publishingChannel}"
        }
        //TODO implement force publish like in CPP
        else if(config.publishingChannel.startsWith('feature_kon_')) {
            //nuget only allows 20 chars in "Special Version String", underscores not allowed
            config.publishingChannel = config.publishingChannel.replace("feature_kon_","");
            if(config.publishingChannel.length() >= 20) {
                config.publishingChannel = config.publishingChannel.substring(0,15)
            }
            suffix = "${config.publishingChannel}"
        }
        //do not publish feature branches other than kon_ jenkins development branches to artifactory
        else {
            return;
        }
        suffix = suffix.replace("_","")
        packArgs += "-suffix ${suffix}"
        publishingRepo = repositoryAddress() + "cab-nuget-ci"
    }
    
    //only publish to kon repos on KON jenkins
    if(runningOnKON()) {
        publishingRepo += "-kon";
    }
    
    def files = glob("*.nuspec")
    
    packNuSpecs(files, packArgs)
    publishArtifactWithMetadata(publishingRepo)
}

def glob(String pattern) {
    def files = []
    def foundFiles = findFiles(glob: pattern)
    for(def file in foundFiles) {
        files.add("${file}");
    }
    return files;
}

def packNuSpecs(files, packArgs) {
    println("${files}")
    for (file in files) {
        println(file)
        exec("pack ${file} ${packArgs}", true)
    }
    
}

def publishArtifactWithMetadata(repo) {
    repo = repo.replace(repositoryAddress(),"")
    def server = Artifactory.server 'Artifactory'
    println("repo: ${repo}")
    def uploadSpec = """{
        "files": [
            {
              "pattern": "*.nupkg",
              "target": "${repo}"
            }
                 ]
    }"""
    def buildInfo = Artifactory.newBuildInfo()
    buildInfo.env.capture = true
    buildInfo.append(server.upload(uploadSpec))
    server.publishBuildInfo(buildInfo)
}

def restore(solutionFilePath) {
    init()
    exec("locals all -clear") //always clear cache to make sure newest packages are downloaded
    exec("restore ${solutionFilePath}");
}

def exec(command, returnOutput = false) {
    def nuget = "${tool 'nuget'}" + "\\nuget"
    println "Executing command:${nuget} ${command}"
    return bat(returnStdout: returnOutput, script: "${nuget} ${command}");
}

return this
