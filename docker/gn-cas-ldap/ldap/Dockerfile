FROM debian
ENV DEBIAN_FRONTEND noninteractive

RUN echo "slapd slapd/internal/adminpw password secret" | debconf-set-selections &&           \
    echo "slapd slapd/internal/generated_adminpw password secret" | debconf-set-selections && \
    echo "slapd slapd/password1 password secret" | debconf-set-selections &&                  \
    echo "slapd slapd/password2 password secret" | debconf-set-selections &&                  \
    echo "slapd slapd/domain string geonetwork-opensource.org"| debconf-set-selections


RUN apt update && apt install -y slapd ldap-utils &&     \
    apt clean &&                                         \
    rm -rf /var/lib/apt/lists/*


COPY ldif/*.ldif /tmp/

RUN service slapd start

RUN service slapd start && ldapadd -H ldap:/// -Dcn=admin,dc=geonetwork-opensource,dc=org -f /tmp/users.ldif -x -w secret -c



EXPOSE 389

CMD [ "sh", "-c", "exec slapd -d32768" ]
