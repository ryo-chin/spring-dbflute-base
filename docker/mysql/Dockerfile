FROM mysql:5.7.12

ENV TZ "Asia/Tokyo"
ENV MYSQL_ALLOW_EMPTY_PASSWORD "yes"
ENV MYSQL_ROOT_PASSWORD ""

COPY init/* /docker-entrypoint-initdb.d/
COPY ./my.cnf /etc/mysql/my.cnf