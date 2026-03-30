#!/bin/bash

# To make it work on both Linux date and macOS date)
if date --version >/dev/null 2>&1; then
  # Linux date
  WHEN=$(date -d "$(date +%Y-%m-01) -6 months" +%Y-%m-01)
else
  # macOS date
  WHEN=$(date -v -6m +%Y-%m-01)
fi

echo "Reviews since $WHEN"
{ for line in $(
  gh pr list --repo geonetwork/geonetwork --state closed \
      --search "merged:>=$WHEN base:main sort:closedAt-desc" \
      --json number --limit 1000 --template \
      '{{range .}}{{tablerow (printf "%v" .number) }}{{end}}'
); do
  gh api "repos/geonetwork/geonetwork/pulls/${line}/reviews" --template \
    '{{range .}}{{tablerow .user.login}}{{end}}' | sort | uniq
  done;
  for line in $(
  gh pr list --repo geonetwork/core-geonetwork --state closed \
      --search "merged:>=$WHEN base:main sort:closedAt-desc" \
      --json number --limit 1000 --template \
      '{{range .}}{{tablerow (printf "%v" .number) }}{{end}}'
); do
  gh api "repos/geonetwork/core-geonetwork/pulls/${line}/reviews" --template \
    '{{range .}}{{tablerow .user.login}}{{end}}' | sort | uniq
  done;
} | sort | uniq -c | sort -r
