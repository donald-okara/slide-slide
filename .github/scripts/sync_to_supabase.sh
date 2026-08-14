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

# Clean up SUPABASE_URL: remove trailing slashes and the /rest/v1 suffix if it exists
# to prevent "Invalid path" errors when we append it ourselves.
CLEAN_URL=$(echo "$SUPABASE_URL" | sed 's#/$##' | sed 's#/rest/v1$##')
API_URL="${CLEAN_URL}/rest/v1/user_benchmarks"

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

# Use jq to build the payload with a valid ISO timestamp
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
echo "Total Score: $TOTAL_SCORE"
echo "Endpoint: $API_URL"

# Note: Prefer: resolution=merge-duplicates requires identifying the conflict column
# via the on_conflict query parameter for clarity.
curl -X POST "${API_URL}?on_conflict=gh_slug" \
  -H "apikey: ${SUPABASE_KEY}" \
  -H "Authorization: Bearer ${SUPABASE_KEY}" \
  -H "Content-Type: application/json" \
  -H "Prefer: resolution=merge-duplicates" \
  -d "$PAYLOAD"

echo ""
echo "Sync completed successfully."
