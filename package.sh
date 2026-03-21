#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
DIST_DIR="$ROOT_DIR/dist"
FRONTEND_OUTPUT_DIR="$DIST_DIR/appFrontEnd"
BACKEND_OUTPUT_JAR="$DIST_DIR/app.jar"

echo "[1/4] 清理打包目录"
rm -rf "$DIST_DIR"
mkdir -p "$FRONTEND_OUTPUT_DIR"

echo "[2/4] 打包后端"
(
  cd "$BACKEND_DIR"
  ./gradlew clean bootJar
)

# Spring Boot 打包结果可能带版本号，这里统一重命名为 app.jar。
BACKEND_JAR_PATH="$(find "$BACKEND_DIR/build/libs" -maxdepth 1 -type f -name '*.jar' ! -name '*plain*.jar' | head -n 1)"

if [[ -z "${BACKEND_JAR_PATH:-}" ]]; then
  echo "未找到后端 jar 包，请检查 backend/build/libs 目录。" >&2
  exit 1
fi

cp "$BACKEND_JAR_PATH" "$BACKEND_OUTPUT_JAR"

echo "[3/4] 打包前端"
(
  cd "$FRONTEND_DIR"
  yarn build
)

if [[ ! -d "$FRONTEND_DIR/dist" ]]; then
  echo "未找到前端打包目录，请检查 frontend/dist 是否生成。" >&2
  exit 1
fi

# 前端静态资源按目录 appFrontEnd 输出，便于后续独立部署或统一归档。
cp -R "$FRONTEND_DIR/dist/." "$FRONTEND_OUTPUT_DIR/"

echo "[4/4] 打包完成"
echo "后端产物: $BACKEND_OUTPUT_JAR"
echo "前端产物: $FRONTEND_OUTPUT_DIR"
