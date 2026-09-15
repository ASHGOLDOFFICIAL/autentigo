default:
    @just --list

build:
    sbt app/assembly

run:
    sbt app/run

test:
    sbt test

fmt:
    sbt scalafmtAll

checkstyle:
    sbt scalafmtCheckAll

up:
    podman-compose up -d

down:
    podman-compose down
