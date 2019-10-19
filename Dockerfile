FROM centos:8

RUN yum install -y \
       java-1.8.0-openjdk \
       java-1.8.0-openjdk-devel \
       git \
	   which && \
    yum clean all \
    groupadd -g 989 docker && \
	useradd -u 1001 -g 989 jenkins
// TODO: hardcoded group and user id for jenkins and docker should be smarter
	