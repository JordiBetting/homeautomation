FROM centos:8

RUN yum install -y java-11-openjdk \
                   python3 \
                   python3-setuptools && \
\
    # onkyo support \
    mkdir -p /usr/local/lib/python3.6/site-packages/ && \
    /usr/bin/easy_install-3.6 onkyo-eiscp && \
\
    # user management \
    groupadd -r gingerbeard && \
    useradd -r -s /bin/false -g gingerbeard gingerbeard && \
\
    #cleanup \
    yum -y autoremove python3 python3-setuptools && \
    yum clean all

USER gingerbeard

ADD *.jar /opt/framework/
