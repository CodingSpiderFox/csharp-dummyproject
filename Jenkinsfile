def build (String solutionFileName) {
        def compilerTool;
        println("Set compiler to default MSBuild 14")
        compilerTool = "\"${tool 'DefaultMSBuild14'}\""
        
        println("Building MSBuild 14 Config:Release|AnyCPU")
        bat "${compilerTool} ${solutionFileName} /p:Configuration=Release /p:Platform=\"Any CPU\""
}

node("vc14") {
    checkout scm
    build("CsDummyProject.sln")
}