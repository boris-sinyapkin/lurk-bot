FROM mariadb:10.4

ENV MARIADB_DATABASE="lurkdb" \
    MARIADB_RANDOM_ROOT_PASSWORD="true" \
    MARIADB_USER="maria" \
    MARIADB_PASSWORD="mdbpw"

# Copy scheme to built-in directory. The docker-entrypoint.sh file 
# will run any files in this directory ending with ".sql" against 
# the database.
ADD schema.sql /docker-entrypoint-initdb.d

EXPOSE 3306