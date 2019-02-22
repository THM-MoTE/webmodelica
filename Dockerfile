FROM openjdk:8-alpine

RUN apk add bash &&\
  addgroup -g 1000 -S openmodelica &&\
  adduser -u 1000 -D -S -G openmodelica openmodelica

ADD ./target/universal/webmodelica-*.txz /opt
RUN cd /opt &&\
    mv webmodelica* webmodelica

USER openmodelica
EXPOSE 3000

CMD cd /opt/webmodelica && ./bin/webmodelica -http.port=:3000 -env=docker
