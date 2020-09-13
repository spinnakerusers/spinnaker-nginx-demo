//Docker 镜像仓库信息
registryServer = "192.168.1.200:8088"
projectName = "${JOB_NAME}".split('-')[0]
repoName = "${JOB_NAME}"
imageName = "${registryServer}/${projectName}/${repoName}"

//pipeline
pipeline{
    agent { node { label "master"}}


  //设置构建触发器
	triggers {
  		GenericTrigger( causeString: 'Generic Cause', 
						genericVariables: [[defaultValue: '', key: 'branchName', regexpFilter: '', value: '$.ref']], 		
						printContributedVariables: true, 
						printPostContent: true, 
						regexpFilterExpression: '', 
						regexpFilterText: '', 
						silentResponse: true, 
						token: 'spinnaker-nginx-demo')
	}


    stages{
        stage("CheckOut"){
            steps{
                script{
          					srcUrl = "http://gitlab.idevops.site/spinnaker/spinnaker-nginx-demo.git"
          					branchName = branchName - "refs/heads/"
          					currentBuild.description = "Trigger by ${branchName}"
                    println("${branchName}")
                    checkout([$class: 'GitSCM', 
                              branches: [[name: "${branchName}"]], 
                              doGenerateSubmoduleConfigurations: false, 
                              extensions: [], 
                              submoduleCfg: [], 
                              userRemoteConfigs: [[credentialsId: 'gitlab-admin-user',
                                                   url: "${srcUrl}"]]])
                }
            }
        }

        stage("Push Image "){
            steps{
                script{
                    withCredentials([usernamePassword(credentialsId: 'harbor-admin-user', passwordVariable: 'password', usernameVariable: 'username')]) {

                        sh """
                           sed -i -- "s/VER/${branchName}/g" app/index.html
                           docker login --username="${username}" -p ${password} ${registryServer}
                           docker build -t ${imageName}:${branchName}  .
                           docker push ${imageName}:${branchName}
                           docker rmi ${imageName}:${branchName}

                        """
                    }
                }
            }
        }

        stage("Trigger File"){
            steps {
                script{
                    sh """
                        echo IMAGE=${imageName}:${branchName} >trigger.properties
                        echo ACTION=DEPLOY >> trigger.properties
                        cat trigger.properties
                    """
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'trigger.properties', followSymlinks: false
                }
            }
        }

    }
}