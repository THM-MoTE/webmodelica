FROM openjdk:11-jre-slim

ADD ./target/universal/webmodelica-0.1-snapshot.txz /opt
RUN cd /opt &&\
    mv webmodelica* webmodelica

EXPOSE 3000

CMD /opt/webmodelica/bin/webmodelica -http.port=3000 -env=docker
