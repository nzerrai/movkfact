#!/bin/bash

# Test duplicate detection with extra columns
set -e

BASE_URL="http://localhost:8080/api"

echo "=========================================="
echo "Test: Duplicate Detection with Extra Columns"
echo "=========================================="

# Step 1: Create a domain
echo -e "\n[1/3] Creating test domain..."
DOMAIN_RESPONSE=$(curl -s -X POST "$BASE_URL/domains" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "DupTestDomain_'$(date +%s)'",
    "description": "Test domain for duplicate detection"
  }')

DOMAIN_ID=$(echo "$DOMAIN_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo "Domain created: ID=$DOMAIN_ID"

# Step 2: Try to add duplicate extra column (should fail)
echo -e "\n[2/3] Testing duplicate detection: Adding column that already exists..."
DUPLICATE_REQUEST=$(cat << 'EOF'
{
  "datasetName": "DuplicateTest",
  "numberOfRows": 5,
  "columns": [
    {
      "name": "id",
      "columnType": "UUID"
    }
  ],
  "extraColumns": [
    {
      "name": "id",
      "columnType": "INTEGER"
    }
  ]
}
EOF
)

echo "Attempting to add duplicate column 'id'..."
DUPLICATE_RESPONSE=$(curl -s -X POST "$BASE_URL/domains/$DOMAIN_ID/data-sets" \
  -H "Content-Type: application/json" \
  -d "$DUPLICATE_REQUEST")

ERROR_MSG=$(echo "$DUPLICATE_RESPONSE" | grep -o '"error":"[^"]*"' || echo "")
if echo "$DUPLICATE_RESPONSE" | grep -q "Duplicate"; then
  echo "✅ Duplicate detection works! Error received:"
  echo "   $DUPLICATE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$DUPLICATE_RESPONSE"
else
  echo "❌ Duplicate detection FAILED - no error received"
  echo "$DUPLICATE_RESPONSE"
fi

# Step 3: Test with valid extra columns (no duplicates)
echo -e "\n[3/3] Testing valid extra columns (no duplicates)..."
VALID_REQUEST=$(cat << 'EOF'
{
  "datasetName": "ValidTest",
  "numberOfRows": 5,
  "columns": [
    {
      "name": "id",
      "columnType": "UUID"
    },
    {
      "name": "name",
      "columnType": "FIRST_NAME"
    }
  ],
  "extraColumns": [
    {
      "name": "email",
      "columnType": "EMAIL"
    },
    {
      "name": "phone",
      "columnType": "PHONE"
    }
  ]
}
EOF
)

echo "Generating dataset with unique extra columns..."
VALID_RESPONSE=$(curl -s -X POST "$BASE_URL/domains/$DOMAIN_ID/data-sets" \
  -H "Content-Type: application/json" \
  -d "$VALID_REQUEST")

if echo "$VALID_RESPONSE" | grep -q '"id"'; then
  echo "✅ Dataset created successfully!"
  echo "$VALID_RESPONSE" | python3 -m json.tool | head -10
else
  echo "❌ Dataset creation failed"
  echo "$VALID_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$VALID_RESPONSE"
fi

echo -e "\n=========================================="
echo "Validation complete!"
echo "=========================================="
