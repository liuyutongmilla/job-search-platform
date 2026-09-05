#!/usr/bin/env bash
# 全量校验：编译 7 个模块 + 跑单元测试。
#
# 输出被刻意压到最少 —— 成功只打一个 ✅，失败才打错误行。
# 用法：
#   ./verify.sh              编译 + 测试
#   ./verify.sh compile      只编译（更快）
#   ./verify.sh job-service  只验一个模块
#
# 失败时把红字整段贴给 Claude 就行，不用贴成功的部分。

set -uo pipefail
cd "$(dirname "$0")"

MODULES=(eurekaserver configserver gatewayserver user-service job-service matching-service notification-service)
MODE="${1:-all}"

# 只验单个模块
if [[ " ${MODULES[*]} " == *" $MODE "* ]]; then
  MODULES=("$MODE")
  MODE="all"
fi

FAILED=()
LOGDIR=".verify-logs"
mkdir -p "$LOGDIR"

echo "=== 编译 ==="
for m in "${MODULES[@]}"; do
  printf '%-22s' "$m"
  if (cd "$m" && ./mvnw -B -DskipTests compile) > "$LOGDIR/$m-compile.log" 2>&1; then
    echo "✅"
  else
    echo "❌"
    FAILED+=("$m:compile")
    # 只打真正有用的行：编译错误 + Maven 的 ERROR 汇总
    grep -E '^\[ERROR\]' "$LOGDIR/$m-compile.log" | grep -v 'Re-run Maven\|stack trace\|Help 1\|cwiki.apache' | head -20 | sed 's/^/    /'
  fi
done

if [[ "$MODE" != "compile" ]]; then
  echo
  echo "=== 单元测试 ==="
  for m in "${MODULES[@]}"; do
    # 没有测试目录就跳过，不算失败
    [[ -d "$m/src/test/java" ]] || continue
    printf '%-22s' "$m"
    if (cd "$m" && ./mvnw -B test) > "$LOGDIR/$m-test.log" 2>&1; then
      # 提取 "Tests run: N, Failures: 0..." 那一行
      grep -oE 'Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+' \
        "$LOGDIR/$m-test.log" | tail -1 | xargs -I{} echo "✅  {}"
    else
      echo "❌"
      FAILED+=("$m:test")
      grep -E '^\[ERROR\].*Test|^\[ERROR\].*\.java|expected:|actual:|AssertionFailedError' \
        "$LOGDIR/$m-test.log" | head -20 | sed 's/^/    /'
    fi
  done
fi

echo
if [[ ${#FAILED[@]} -eq 0 ]]; then
  echo "全部通过。完整日志在 $LOGDIR/"
  exit 0
else
  echo "失败：${FAILED[*]}"
  echo "完整日志：$LOGDIR/  —— 把上面的错误行贴给 Claude 即可，不用贴日志全文"
  exit 1
fi
