## Spinnaker Nginx Demo1

1.开发人员向Gitlab提交代码，触发JenkinsCI构建。
2.Jenkins构建该分支，生成docker镜像，上传到Harbor镜像仓库。
3.Spinnaker检测到JenkinsCI构建完成后运行CD部署流水线。

