FROM node:alpine
ADD ./build /opt/webmodelica-ui
RUN npm install -g serve

EXPOSE 3000
CMD serve -l tcp://0.0.0.0:3000 -s /opt/webmodelica-ui
