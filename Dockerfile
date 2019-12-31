FROM centos:8

RUN yum install -y \
       java-1.8.0-openjdk \
       java-1.8.0-openjdk-devel \
       git \
       which \
       rpm-build && \
    yum clean all && \
	groupadd -g 666 docker && \
	useradd -u 666 -g 666 jenkins
	
