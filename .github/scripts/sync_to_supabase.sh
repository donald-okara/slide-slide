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

# Calculate Total Score
# Formula: Sum across all difficulties: (Success Rate * Difficulty Weight)
# Difficulty Weights: EASY=10, MEDIUM=30, HARD=60
TOTAL_SCORE=$(jq '
  map(
    (if .total_iterations > 0 then (.success_count | tonumber) / (.total_iterations | tonumber) else 0 end) *
    (if .difficulty == "EASY" then 10 elif .difficulty == "MEDIUM" then 30 else 60 end)
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
    updated_at: now
  }')

echo "Syncing benchmark results for $GH_SLUG to Supabase..."
echo "Total Score: $TOTAL_SCORE"

curl -X POST "${SUPABASE_URL}/rest/v1/user_benchmarks" \
  -H "apikey: ${SUPABASE_KEY}" \
  -H "Authorization: Bearer ${SUPABASE_KEY}" \
  -H "Content-Type: application/json" \
  -H "Prefer: resolution=merge-duplicates" \
  -d "$PAYLOAD"

echo "Sync completed successfully."
