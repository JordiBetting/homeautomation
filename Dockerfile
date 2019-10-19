FROM centos:8

RUN yum install -y \
       java-1.8.0-openjdk \
       java-1.8.0-openjdk-devel \
       git \
	   which && \
    yum clean all   
	