resource "local_file" "jenkins_logs" {
  filename = "${path.module}/logs/jenkins.log"
  content  = "Jenkins logs - ${timestamp()}\n"
  
  provisioner "local-exec" {
    command = "mkdir -p ${path.module}/logs && docker logs -f ${docker_container.jenkins_task.name} >> ${path.module}/logs/jenkins.log 2>&1 &"
    when    = create
  }
}

resource "local_file" "app_logs" {
  filename = "${path.module}/logs/app.log"
  content  = "App logs - ${timestamp()}\n"
  
  provisioner "local-exec" {
    command = "mkdir -p ${path.module}/logs && docker logs -f ${docker_container.app_task.name} >> ${path.module}/logs/app.log 2>&1 &"
    when    = create
  }
}
