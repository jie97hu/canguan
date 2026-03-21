#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
DIST_DIR="$ROOT_DIR/dist"
PACKAGE_SCRIPT="$ROOT_DIR/package.sh"

REMOTE_HOST="${REMOTE_HOST:-47.108.60.14}"
REMOTE_PORT="${REMOTE_PORT:-22}"
REMOTE_USER="${REMOTE_USER:-root}"
REMOTE_DIR="${REMOTE_DIR:-/home/app}"
BUILD_BEFORE_UPLOAD="${BUILD_BEFORE_UPLOAD:-false}"

SSH_OPTS=(
  -p "$REMOTE_PORT"
  -o StrictHostKeyChecking=accept-new
)

SCP_OPTS=(
  -P "$REMOTE_PORT"
  -o StrictHostKeyChecking=accept-new
)

if ! command -v ssh >/dev/null 2>&1; then
  echo "未找到 ssh 命令，请先安装 OpenSSH 客户端。" >&2
  exit 1
fi

if ! command -v scp >/dev/null 2>&1; then
  echo "未找到 scp 命令，请先安装 OpenSSH 客户端。" >&2
  exit 1
fi

if [[ "$BUILD_BEFORE_UPLOAD" == "true" || ! -f "$DIST_DIR/app.jar" || ! -d "$DIST_DIR/appFrontEnd" ]]; then
  echo "[1/4] 本地打包产物缺失或要求重新打包，开始执行 package.sh"
  "$PACKAGE_SCRIPT"
else
  echo "[1/4] 使用已有打包产物"
fi

echo "[2/4] 准备远程目录"
# 先清理前端目录再重建，避免旧静态文件残留。
ssh "${SSH_OPTS[@]}" "${REMOTE_USER}@${REMOTE_HOST}" \
  "mkdir -p '${REMOTE_DIR}' && rm -rf '${REMOTE_DIR}/appFrontEnd' && mkdir -p '${REMOTE_DIR}/appFrontEnd'"

echo "[3/4] 上传后端 jar"
scp "${SCP_OPTS[@]}" "$DIST_DIR/app.jar" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/app.jar"

echo "[4/4] 上传前端静态文件"
scp "${SCP_OPTS[@]}" -r "$DIST_DIR/appFrontEnd/." "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/appFrontEnd/"

echo "上传完成"
echo "远程地址: ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}"
