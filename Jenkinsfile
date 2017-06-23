def build (String solutionFilePath) {
    stage("Building") {
        def compilerTool;
        println("Set compiler to default MSBuild 14")
        compilerTool = "\"${tool 'DefaultMSBuild14'}\""
        
        println("Building MSBuild 14 Config:Release|AnyCPU")
        bat "${compilerTool} ${solutionFilePath} /p:Configuration=Release /p:Platform=\"Any CPU\""
    }
}

def NUnit() {
    return NUnit("2.6.4")
}

def NUnit(String version ){
    def toolString = tool("NUnit-${version}")
    return toolString + "/NUnit-${version}/bin/nunit-console.exe"
}

def buildTestItemsString(List<String> testItemFileNames) {
    String result;
    for(testItemFileName in testItemFileNames) {
        result += testItemFileName + " ";
    }
    return result;
}

def test(List<String> testItemFileNames) {
    node("net-4.5")
    {
        stage("Testing") {
            dir("bin/Tests/Release") {
                def testTool = NUnit() //TODO make an option for NUnit version
                def testItems = buildTestItemsString(testItemFileNames)
                def testArgs = "${testItems} /labels /framework:net-4.5 /xml=testResults-CI-${env.BUILD_ID}.xml"
                
                bat "${testTool} ${testArgs}"
            }
        }
    }
}


def installDeps(String solutionFilePath) {
    nuget.restore(solutionFilePath)
}

node("vc14") {
    def solutionFilePath = "CsDummyProject.sln"
    
    stage("Checkout") {
        checkout scm
    }
    
    def nuget = load("nuget.groovy")
    
    installDeps(solutionFilePath)
    
    build(solutionFilePath)
    
    def testItemFileNames = new List<String>() {"CsDummyProjectTest.dll"}
    test(testItemFileNames)
}
