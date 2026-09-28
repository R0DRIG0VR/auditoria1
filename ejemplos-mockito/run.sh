#!/usr/bin/env bash
# Compila y ejecuta todas las pruebas SIN necesitar Maven (sólo JDK 21+ y curl).
# Uso:  ./run.sh            -> ejecuta todas las pruebas
#       ./run.sh cobertura  -> además genera el reporte de cobertura JaCoCo en target/cobertura/index.html
#       ./run.sh rojos      -> ejecuta SOLO las pruebas "en rojo a propósito" del paquete pasoapaso
set -euo pipefail
cd "$(dirname "$0")"

MAVEN=https://repo1.maven.org/maven2
LIB=lib
DEPS=(
  "org/junit/platform/junit-platform-console-standalone/6.1.3/junit-platform-console-standalone-6.1.3.jar"
  "org/mockito/mockito-core/5.24.0/mockito-core-5.24.0.jar"
  "org/mockito/mockito-junit-jupiter/5.24.0/mockito-junit-jupiter-5.24.0.jar"
  "net/bytebuddy/byte-buddy/1.18.14/byte-buddy-1.18.14.jar"
  "net/bytebuddy/byte-buddy-agent/1.18.14/byte-buddy-agent-1.18.14.jar"
  "org/objenesis/objenesis/3.3/objenesis-3.3.jar"
  "org/assertj/assertj-core/3.27.7/assertj-core-3.27.7.jar"
  "org/jacoco/org.jacoco.agent/0.8.15/org.jacoco.agent-0.8.15-runtime.jar"
  "org/jacoco/org.jacoco.cli/0.8.15/org.jacoco.cli-0.8.15-nodeps.jar"
)

mkdir -p "$LIB"
for d in "${DEPS[@]}"; do
  f="$LIB/$(basename "$d")"
  [ -f "$f" ] || { echo "Descargando $(basename "$d")"; curl -sfL "$MAVEN/$d" -o "$f"; }
done

CP=$(ls "$LIB"/*.jar | grep -v jacoco | tr '\n' ':')
rm -rf target && mkdir -p target/classes target/test-classes

echo "== Compilando =="
javac -encoding UTF-8 -d target/classes $(find src/main/java -name '*.java')
javac -encoding UTF-8 -d target/test-classes -cp "target/classes:$CP" $(find src/test/java -name '*.java')

AGENTES="-javaagent:$LIB/mockito-core-5.24.0.jar"
if [ "${1:-}" = "cobertura" ]; then
  AGENTES="$AGENTES -javaagent:$LIB/org.jacoco.agent-0.8.15-runtime.jar=destfile=target/jacoco.exec,includes=bo.edu.umss.*"
fi

if [ "${1:-}" = "rojos" ]; then
  echo "== Pruebas en ROJO a propósito (paso a paso) =="
  java $AGENTES -cp "target/classes:target/test-classes:$CP" \
    org.junit.platform.console.ConsoleLauncher execute \
    --select-package bo.edu.umss.pedidos.pasoapaso \
    --config junit.jupiter.conditions.deactivate='org.junit.*DisabledCondition' \
    --details=tree || true
  exit 0
fi

echo "== Ejecutando pruebas =="
java $AGENTES -cp "target/classes:target/test-classes:$CP" \
  org.junit.platform.console.ConsoleLauncher execute \
  --scan-class-path target/test-classes \
  --details=tree

if [ "${1:-}" = "cobertura" ]; then
  echo "== Reporte de cobertura =="
  java -jar "$LIB/org.jacoco.cli-0.8.15-nodeps.jar" report target/jacoco.exec \
    --classfiles target/classes --sourcefiles src/main/java \
    --html target/cobertura --csv target/cobertura.csv
  echo "Abrir: target/cobertura/index.html"
fi
