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

gen-jwt-keypair:
    openssl ecparam -name prime256v1 -genkey -noout -out jwt-private.pem
    openssl pkcs8 -topk8 -nocrypt -in jwt-private.pem -out jwt-private-pkcs8.pem
    openssl ec -in jwt-private.pem -pubout -out jwt-public.pem
    rm jwt-private.pem
    mv jwt-private-pkcs8.pem jwt-private.pem
    @echo "Wrote jwt-private.pem and jwt-public.pem."

up:
    podman-compose up -d

down:
    podman-compose down

db-reset:
    podman-compose down -v
