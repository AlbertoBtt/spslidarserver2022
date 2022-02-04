FROM ubuntu:20.04

RUN  apt-get update \
  && apt-get install -y wget gpg software-properties-common zip unzip \
  && rm -rf /var/lib/apt/lists/*

RUN dpkg --add-architecture i386 \
    && wget -nc https://dl.winehq.org/wine-builds/winehq.key \
    && gpg -o /etc/apt/trusted.gpg.d/winehq.key.gpg --dearmor winehq.key \
    && add-apt-repository 'deb https://dl.winehq.org/wine-builds/ubuntu/ focal main' \
    && apt update \
    && apt install -y --install-recommends winehq-stable  \
    && wget https://lastools.github.io/download/LAStools.zip \
    && unzip LAStools.zip;
#&& wine LAStools/bin/las2las -h

####Install Java 11
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk && \
    apt-get install -y ant && \
    apt-get clean;

RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/
RUN export JAVA_HOME


###Install Python
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3.7 \
    python3-pip \
    && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

#Install pylas library as it is the one used by the python script
#Probably should change this and read the requirements.txt file associated to the python project
RUN pip3 install pylas

COPY python-utils python-utils
COPY LASutils LASutils

VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]