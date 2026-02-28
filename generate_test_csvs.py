#!/usr/bin/env python3
"""
Generate 80+ test CSV files for S2.2 accuracy measurement.
Mary's Dataset — 04 mars 2026
"""

import os
import csv
from datetime import datetime, timedelta
import random

OUTPUT_DIR = "src/test/resources/accuracy-test-data"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Random seed for reproducibility
random.seed(42)

# Test data generators
FIRST_NAMES_EASY = ["Jean", "Marie", "Pierre", "Sophie", "Marc", "Anne", "Paul", "Isabelle", "François", "Laura"]
LAST_NAMES_EASY = ["Dupont", "Martin", "Bernard", "Durand", "Thomas", "Robert", "Robert", "Lefevre", "Moreau", "Laurent"]

FIRST_NAMES_HARD = ["Αλέξανδρος", "José", "Müller", "Björn", "李明", "محمد", "François", "Séan", "José-María", "Jean-Luc"]
EMAILS = ["john.doe@example.com", "marie@test.fr", "pierre.martin@company.org", "sophie_bernard@mail.com"]
GENDERS = ["M", "F", "Male", "Female", "Homme", "Femme"]
PHONES = ["+33612345678", "06 12 34 56 78", "+1-555-123-4567", "555.123.4567", "06-12-34-56-78"]
ADDRESSES = ["123 Rue de Paris", "42 Avenue des Champs", "5 Boulevard Saint-Germain", "10 Quai du Louvre"]

AMOUNTS = ["1000.50", "50,00€", "€1.234,56", "$9,999.99", "15000"]
ACCOUNTS = ["FR76 3000 6000 0123 4567 8901 23", "DE89370400440532013000", "GB82WEST12345698765432"]
CURRENCIES = ["EUR", "USD", "GBP", "€", "$", "£"]

DATES_EASY = ["01/01/2020", "31/12/2021", "15/06/2019", "25/03/2022", "10/11/2020"]
DATES_HARD = ["2020-01-01", "01-01-2020", "01.01.2020", "01 janvier 2020", "01/1/2020"]
TIMES = ["14:30:00", "09:15", "23:59:59", "12:00", "08:30"]
TIMEZONES = ["UTC", "CET", "EST", "PST", "Asia/Tokyo"]
BIRTH_DATES = ["10/05/1990", "25/12/1985", "01/03/1995", "20/07/1988"]

def generate_easy_personal_csv(filename):
    """Generate well-formed personal data CSV."""
    rows = []
    headers = ["first_name", "last_name", "email", "gender"]
    rows.append(headers)
    for _ in range(random.randint(10, 20)):
        rows.append([
            random.choice(FIRST_NAMES_EASY),
            random.choice(LAST_NAMES_EASY),
            random.choice(EMAILS),
            random.choice(["M", "F"])
        ])
    write_csv(filename, rows)

def generate_medium_personal_csv(filename):
    """Generate personal data with some ambiguities."""
    rows = []
    headers = ["first_name", "last_name", "phone", "address"]
    rows.append(headers)
    for _ in range(random.randint(10, 20)):
        rows.append([
            random.choice(FIRST_NAMES_EASY),
            random.choice(LAST_NAMES_EASY),
            random.choice(PHONES),
            random.choice(ADDRESSES)
        ])
    write_csv(filename, rows)

def generate_hard_personal_csv(filename):
    """Generate personal data with multilingual/noisy data."""
    rows = []
    headers = ["prenom", "nom", "contact"]
    rows.append(headers)
    for _ in range(random.randint(10, 20)):
        rows.append([
            random.choice(FIRST_NAMES_HARD),
            random.choice(LAST_NAMES_EASY),
            random.choice(EMAILS + PHONES + ADDRESSES)
        ])
    write_csv(filename, rows)

def generate_financial_csv(filename, difficulty="easy"):
    """Generate financial data."""
    rows = []
    if difficulty == "easy":
        headers = ["amount", "account", "currency"]
    elif difficulty == "medium":
        headers = ["montant", "compte_bancaire", "devise"]
    else:
        headers = ["بيان حسابي", "trans_amount", "curr_code"]
    
    rows.append(headers)
    for _ in range(random.randint(10, 20)):
        rows.append([
            random.choice(AMOUNTS),
            random.choice(ACCOUNTS),
            random.choice(CURRENCIES)
        ])
    write_csv(filename, rows)

def generate_temporal_csv(filename, difficulty="easy"):
    """Generate temporal data."""
    rows = []
    if difficulty == "easy":
        headers = ["birth_date", "transaction_date", "time_recorded"]
    elif difficulty == "medium":
        headers = ["date_naissance", "date_op", "heure"]
    else:
        headers = ["дата рождения", "event_date", "recorded_at"]
    
    rows.append(headers)
    for _ in range(random.randint(10, 20)):
        rows.append([
            random.choice(BIRTH_DATES),
            random.choice(DATES_EASY if difficulty == "easy" else DATES_HARD),
            random.choice(TIMES)
        ])
    write_csv(filename, rows)

def generate_robustness_csv(filename):
    """Generate mixed data for robustness testing."""
    rows = []
    headers = ["id", "first_name", "email", "amount", "date", "phone", "country"]
    rows.append(headers)
    for i in range(random.randint(15, 25)):
        rows.append([
            str(i),
            random.choice(FIRST_NAMES_EASY if random.random() > 0.3 else FIRST_NAMES_HARD),
            random.choice(EMAILS) if random.random() > 0.2 else "invalid",
            random.choice(AMOUNTS),
            random.choice(DATES_EASY) if random.random() > 0.3 else random.choice(DATES_HARD),
            random.choice(PHONES) if random.random() > 0.2 else "N/A",
            random.choice(["France", "Allemagne", "España", "Royaume-Uni", "Italia"])
        ])
    write_csv(filename, rows)

def write_csv(filename, rows):
    """Write CSV file."""
    filepath = os.path.join(OUTPUT_DIR, filename)
    with open(filepath, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerows(rows)
    print(f"✅ Created: {filename}")

# Generate datasets
print("\n🚀 Generating 80+ Test CSV Files for S2.2 Accuracy Measurement\n")

# EASY DATASETS (25 files)
print("📊 EASY DATASETS (25 files - well-formed data)")
for i in range(1, 9):
    generate_easy_personal_csv(f"easy-personal-{i:02d}.csv")
for i in range(1, 9):
    generate_financial_csv(f"easy-financial-{i:02d}.csv", "easy")
for i in range(1, 9):
    generate_temporal_csv(f"easy-temporal-{i:02d}.csv", "easy")

# MEDIUM DATASETS (25 files)
print("\n📊 MEDIUM DATASETS (25 files - some ambiguity)")
for i in range(1, 9):
    generate_medium_personal_csv(f"medium-personal-{i:02d}.csv")
for i in range(1, 9):
    generate_financial_csv(f"medium-financial-{i:02d}.csv", "medium")
for i in range(1, 9):
    generate_temporal_csv(f"medium-temporal-{i:02d}.csv", "medium")

# HARD DATASETS (20 files)
print("\n📊 HARD DATASETS (20 files - multilingual/noisy)")
for i in range(1, 8):
    generate_hard_personal_csv(f"hard-personal-{i:02d}.csv")
for i in range(1, 7):
    generate_financial_csv(f"hard-financial-{i:02d}.csv", "hard")
for i in range(1, 7):
    generate_temporal_csv(f"hard-temporal-{i:02d}.csv", "hard")

# ROBUSTNESS DATASETS (15 files)
print("\n📊 ROBUSTNESS DATASETS (15 files - mixed clean/ambiguous/problematic)")
for i in range(1, 16):
    generate_robustness_csv(f"robustness-mixed-{i:02d}.csv")

print("\n✅ All 80+ test CSV files generated successfully!")
print(f"📁 Location: {OUTPUT_DIR}")
print(f"📈 Total files: {len(os.listdir(OUTPUT_DIR))}")
