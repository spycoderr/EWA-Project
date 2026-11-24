#!/usr/bin/env bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOCAL_TOOLS="$PROJECT_DIR/.tools"

if [ -x "$LOCAL_TOOLS/jdk21/Contents/Home/bin/java" ]; then
    export JAVA_HOME="$LOCAL_TOOLS/jdk21/Contents/Home"
fi

# Use the optional local demo MySQL only when no external database URL is set.
if [ -z "${DB_URL:-}" ] && [ -x "$LOCAL_TOOLS/mysql/bin/mysqld" ]; then
    MYSQL_SOCKET="$LOCAL_TOOLS/mysql.sock"
    if ! "$LOCAL_TOOLS/mysql/bin/mysqladmin" --no-defaults --socket="$MYSQL_SOCKET" -uroot ping >/dev/null 2>&1; then
        "$LOCAL_TOOLS/mysql/bin/mysqld" --no-defaults \
            --basedir="$LOCAL_TOOLS/mysql" --datadir="$LOCAL_TOOLS/mysql-data" \
            --bind-address=127.0.0.1 --port=3307 --socket="$MYSQL_SOCKET" \
            --pid-file="$LOCAL_TOOLS/mysql.pid" --log-error="$LOCAL_TOOLS/mysql.log" \
            --mysqlx=OFF --daemonize
    fi
    "$LOCAL_TOOLS/mysql/bin/mysql" --no-defaults --socket="$MYSQL_SOCKET" -uroot \
        -e 'CREATE DATABASE IF NOT EXISTS expense_approval_lab;'
    export DB_URL="jdbc:mysql://localhost:3307/expense_approval_lab"
    export DB_USERNAME=root
    export DB_PASSWORD=
fi

export MAVEN_USER_HOME="${MAVEN_USER_HOME:-$LOCAL_TOOLS/maven}"
cd "$PROJECT_DIR/backend"
exec ./mvnw "-Dmaven.repo.local=$LOCAL_TOOLS/m2" spring-boot:run
