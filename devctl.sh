#!/bin/bash
# devctl.sh — Contrôle backend + frontend
# Usage: ./devctl.sh [D|A|V]

BACKEND_LOG=/tmp/backend.log
FRONTEND_LOG=/tmp/frontend.log
FRONTEND_DIR=/home/seplos/mockfact/movkfact-frontend
BACKEND_DIR=/home/seplos/mockfact

case "$1" in
  D)
    echo "==> Démarrage backend..."
    pkill -f "spring-boot:run" 2>/dev/null
    pkill -f "MoveFactApplication" 2>/dev/null
    cd "$BACKEND_DIR" && mvn spring-boot:run -q > "$BACKEND_LOG" 2>&1 &
    echo "    Backend PID: $! — logs: tail -f $BACKEND_LOG"

    echo "==> Démarrage frontend..."
    pkill -f "react-scripts start" 2>/dev/null
    fuser -k 3000/tcp 2>/dev/null
    cd "$FRONTEND_DIR" && npm start > "$FRONTEND_LOG" 2>&1 &
    echo "    Frontend PID: $! — logs: tail -f $FRONTEND_LOG"
    ;;

  A)
    echo "==> Arrêt backend + frontend..."
    pkill -f "spring-boot:run"
    pkill -f "MoveFactApplication"
    pkill -f "react-scripts start"
    echo "    Arrêtés."
    ;;

  V)
    echo "==> Vérification..."
    curl -s http://localhost:8080/actuator/health && echo "" && echo "    Backend  : OK" \
      || echo "    Backend  : KO"
    curl -s http://localhost:3000 > /dev/null \
      && echo "    Frontend : OK" \
      || echo "    Frontend : KO"
    ;;

  *)
    echo "Usage: $0 [D|A|V]"
    echo "  D : Démarrer backend + frontend"
    echo "  A : Arrêter  backend + frontend"
    echo "  V : Vérifier l'état"
    exit 1
    ;;
esac
