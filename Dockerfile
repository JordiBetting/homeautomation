FROM centos:8

ARG username
ARG userid
ARG groupname
ARG groupid

RUN yum install -y \
       java-1.8.0-openjdk \
       java-1.8.0-openjdk-devel \
       git \
	   which && \
    yum clean all \
    groupadd -g ${groupid} ${groupname} && \
	useradd -u ${userid} -g ${groupid} ${username}
	