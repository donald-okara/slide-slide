#!/bin/bash

# Required Environment Variables:
# SUPABASE_URL
# SUPABASE_KEY
# GH_SLUG
# PR_NUMBER

set -e

BENCHMARK_FILE="solver_benchmarks.json"

if [ ! -f "$BENCHMARK_FILE" ]; then
  echo "Error: $BENCHMARK_FILE not found."
  exit 1
fi

# Clean up SUPABASE_URL
CLEAN_URL=$(echo "$SUPABASE_URL" | sed 's#/$##' | sed 's#/rest/v1$##')
API_URL="${CLEAN_URL}/rest/v1/user_benchmarks"

# Calculate Advanced Total Score (80% Success, 20% Efficiency)
# Thresholds:
# EASY: 10 moves, 20ms (W=10)
# MEDIUM: 35 moves, 150ms (W=30)
# HARD: 100 moves, 800ms (W=60)
TOTAL_SCORE=$(jq '
  def calc_diff_score:
    if .difficulty == "EASY" then {w: 10, tm: 10, tt: 20}
    elif .difficulty == "MEDIUM" then {w: 30, tm: 35, tt: 150}
    else {w: 60, tm: 100, tt: 800}
    end;

  map(
    calc_diff_score as $cfg |
    (if .total_iterations > 0 then (.success_count | tonumber) / (.total_iterations | tonumber) else 0 end) as $sr |
    if $sr > 0 then
      ($sr * $cfg.w * 0.8) +
      ($sr * (
        ([1.0, (if .avg_moves > 0 then $cfg.tm / .avg_moves else 0 end)] | min | [0, .] | max) * ($cfg.w * 0.1) +
        ([1.0, (if .avg_time_ms > 0 then $cfg.tt / .avg_time_ms else 0 end)] | min | [0, .] | max) * ($cfg.w * 0.1)
      ))
    else 0 end
  ) | add
' "$BENCHMARK_FILE")

# Prepare JSON payload
BENCHMARKS_JSON=$(cat "$BENCHMARK_FILE")

PAYLOAD=$(jq -n \
  --arg gh_slug "$GH_SLUG" \
  --argjson pr_number "$PR_NUMBER" \
  --argjson total_score "$TOTAL_SCORE" \
  --argjson benchmarks "$BENCHMARKS_JSON" \
  '{
    gh_slug: $gh_slug,
    pr_number: $pr_number,
    total_score: $total_score,
    benchmarks: $benchmarks,
    updated_at: (now | strftime("%Y-%m-%dT%H:%M:%SZ"))
  }')

echo "Syncing benchmark results for $GH_SLUG to Supabase..."
echo "Calculated Advanced Score (Success + Efficiency): $TOTAL_SCORE"

curl -X POST "${API_URL}?on_conflict=gh_slug" \
  -H "apikey: ${SUPABASE_KEY}" \
  -H "Authorization: Bearer ${SUPABASE_KEY}" \
  -H "Content-Type: application/json" \
  -H "Prefer: resolution=merge-duplicates" \
  -d "$PAYLOAD"

echo ""
echo "Sync completed successfully."
