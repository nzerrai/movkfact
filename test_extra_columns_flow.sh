#!/bin/bash

# Test script to verify extra columns flow end-to-end
# This simulates: CSV upload → Extra columns addition → Data generation

set -e

BASE_URL="http://localhost:8080/api"

echo "=========================================="
echo "Test: Extra Columns E2E Flow"
echo "=========================================="

# Step 1: Create a domain
echo -e "\n[1/5] Creating domain..."
DOMAIN_RESPONSE=$(curl -s -X POST "$BASE_URL/domains" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TestDomain_'$(date +%s)'",
    "description": "Test domain for extra columns"
  }')

DOMAIN_ID=$(echo "$DOMAIN_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo "Domain created: ID=$DOMAIN_ID"

if [ -z "$DOMAIN_ID" ]; then
  echo "ERROR: Failed to create domain"
  echo "Response: $DOMAIN_RESPONSE"
  exit 1
fi

# Step 2: Upload CSV
echo -e "\n[2/5] Uploading CSV..."
CSV_DATA=$(cat << 'EOF'
name,age
John,30
Jane,25
EOF
)

UPLOAD_RESPONSE=$(curl -s -X POST "$BASE_URL/domains/$DOMAIN_ID/detect-columns" \
  -H "Content-Type: text/csv" \
  -d "$CSV_DATA")

echo "CSV columns detected: $(echo "$UPLOAD_RESPONSE" | grep -o '"name":"[^"]*"' | head -3)"

# Step 3: Build generation request with EXTRA COLUMNS
echo -e "\n[3/5] Building generation request with extra columns..."

GENERATION_REQUEST=$(cat << 'EOF'
{
  "datasetName": "TestDataset",
  "numberOfRows": 10,
  "columns": [
    {
      "name": "name",
      "columnType": "FIRST_NAME",
      "nullable": false
    },
    {
      "name": "age",
      "columnType": "INTEGER",
      "minValue": 18,
      "maxValue": 80
    }
  ],
  "extraColumns": [
    {
      "name": "email",
      "columnType": "EMAIL",
      "nullable": false
    },
    {
      "name": "phone",
      "columnType": "PHONE",
      "nullable": true
    }
  ]
}
EOF
)

echo "Request payload:"
echo "$GENERATION_REQUEST" | python3 -m json.tool 2>/dev/null || echo "$GENERATION_REQUEST"

# Step 4: Generate dataset
echo -e "\n[4/5] Generating dataset with extra columns..."
GENERATE_RESPONSE=$(curl -s -X POST "$BASE_URL/domains/$DOMAIN_ID/data-sets" \
  -H "Content-Type: application/json" \
  -d "$GENERATION_REQUEST")

echo "Generation response:"
echo "$GENERATE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$GENERATE_RESPONSE"

# Extract dataset ID
DATASET_ID=$(echo "$GENERATE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ -z "$DATASET_ID" ]; then
  echo "ERROR: Dataset generation failed"
  exit 1
fi

echo "Dataset created: ID=$DATASET_ID"

# Step 5: Retrieve dataset and verify columns
echo -e "\n[5/5] Verifying dataset contains extra columns..."
DATASET_RESPONSE=$(curl -s -X GET "$BASE_URL/data-sets/$DATASET_ID")

echo "Dataset metadata:"
echo "$DATASET_RESPONSE" | python3 -m json.tool 2>/dev/null | head -20

# Check if extra columns are present in the data
DATASET_DATA=$(curl -s -X GET "$BASE_URL/data-sets/$DATASET_ID/data?page=0&size=1")
echo -e "\nDataset sample data:"
echo "$DATASET_DATA" | python3 -m json.tool 2>/dev/null | head -30

# Verify columns exist
COLUMNS_CHECK=$(echo "$DATASET_DATA" | grep -E '"email"|"phone"' | wc -l)
if [ "$COLUMNS_CHECK" -gt 0 ]; then
  echo -e "\n✅ SUCCESS: Extra columns (email, phone) found in dataset!"
else
  echo -e "\n❌ FAILURE: Extra columns not found in dataset"
  exit 1
fi

echo -e "\n=========================================="
echo "✅ All tests passed!"
echo "=========================================="
