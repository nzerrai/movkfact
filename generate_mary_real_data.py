#!/usr/bin/env python3
"""
Generate realistic business CSV data for Mary's S2.2 Accuracy Measurement
Simulates real production data with proper formatting and business patterns
"""

import os
import csv
from datetime import datetime, timedelta
import random

OUTPUT_DIR = "src/test/resources/accuracy-test-data/mary-real-data"
os.makedirs(OUTPUT_DIR, exist_ok=True)

random.seed(2026)  # Reproducible for February 2026

# Real business data patterns
REAL_FIRST_NAMES = [
    "Jean", "Marie", "Pierre", "Sophie", "Marc", "Anne", "Paul", "Isabelle",
    "François", "Laura", "Michel", "Véronique", "André", "Christine", "Philippe",
    "Monique", "René", "Nathalie", "Claude", "Martine", "Bernard", "Sylvie",
    "Jacques", "Catherine", "Alain", "Pascale"
]

REAL_LAST_NAMES = [
    "Dupont", "Martin", "Bernard", "Durand", "Thomas", "Robert", "Lefevre",
    "Moreau", "Laurent", "Simon", "Michel", "Garcia", "David", "Bertrand",
    "Roux", "Vincent", "Fournier", "Morel", "Girardin", "Andre", "Lefevre",
    "Henry", "Gillet", "Leroy", "Mercier"
]

REAL_COMPANIES = [
    "Acme Corp", "TechSoft SA", "Global Industries", "France Distribution",
    "Services Plus", "Innovation Labs", "Commerce Direct", "Enterprise Solutions",
    "Digital Services", "Business Partners"
]

REAL_EMAILS = [
    "@company.fr", "@techsoft.com", "@global-industries.eu", "@france-dist.fr",
    "@services-plus.com", "@innovation.fr", "@commerce-direct.fr", "@enterprise.eu",
    "@digital-services.com", "@business-partners.fr"
]

REAL_IBAN_PREFIX = ["FR", "DE", "ES", "IT", "BE"]
REAL_PHONE_PREFIX = ["+33", "+49", "+34", "+39", "+32"]

def generate_customer_database():
    """Generate realistic customer database simulation"""
    rows = []
    headers = ["id", "first_name", "last_name", "email", "phone", "company", "created_date"]
    rows.append(headers)
    
    for i in range(1, 51):  # 50 customer records
        first = random.choice(REAL_FIRST_NAMES)
        last = random.choice(REAL_LAST_NAMES)
        email = f"{first.lower()}.{last.lower()}{random.randint(1,999)}@business.fr"
        phone = f"{random.choice(REAL_PHONE_PREFIX)}{''.join([str(random.randint(0,9)) for _ in range(9)])}"
        company = random.choice(REAL_COMPANIES)
        date = (datetime(2024, 1, 1) + timedelta(days=random.randint(0, 400))).strftime("%Y-%m-%d")
        
        rows.append([str(i), first, last, email, phone, company, date])
    
    return rows

def generate_transaction_data():
    """Generate realistic financial transaction data"""
    rows = []
    headers = ["transaction_id", "amount", "currency", "account_number", "date", "description"]
    rows.append(headers)
    
    for i in range(1, 61):  # 60 transactions
        amount = f"{random.randint(100, 50000)}.{random.randint(0, 99):02d}"
        currency = random.choice(["EUR", "USD", "GBP"])
        # Create realistic IBAN
        prefix = random.choice(REAL_IBAN_PREFIX)
        iban = f"{prefix}{random.randint(10, 99)} {random.randint(1000, 9999)} {random.randint(1000, 9999)} {''.join([str(random.randint(0,9)) for _ in range(11)])}"
        date = (datetime(2026, 1, 1) + timedelta(days=random.randint(0, 58))).strftime("%d/%m/%Y")
        category = random.choice(["Salary", "Expense", "Refund", "Payment", "Fee Adjustment"])
        
        rows.append([f"TXN{i:05d}", amount, currency, iban, date, category])
    
    return rows

def generate_temporal_data():
    """Generate realistic time-series data"""
    rows = []
    headers = ["record_id", "birth_date", "hire_date", "last_activity", "time_zone"]
    rows.append(headers)
    
    for i in range(1, 46):  # 45 records
        birth = (datetime(1960, 1, 1) + timedelta(days=random.randint(0, 25000))).strftime("%Y-%m-%d")
        hire = (datetime(2015, 1, 1) + timedelta(days=random.randint(0, 4000))).strftime("%d-%m-%Y")
        last_activity = datetime.now().strftime("%Y-%m-%d %H:%M")
        tz = random.choice(["Europe/Paris", "Europe/London", "Europe/Berlin", "UTC", "America/New_York"])
        
        rows.append([f"REC{i:04d}", birth, hire, last_activity, tz])
    
    return rows

def generate_mixed_business_data():
    """Generate realistic mixed business data (resembles actual company ERP export)"""
    rows = []
    headers = ["emp_id", "given_name", "family_name", "work_email", "mobile", "salary_amount", 
               "dept", "start_date", "last_salary_review"]
    rows.append(headers)
    
    for i in range(1, 41):  # 40 employees
        emp_id = f"EMP{2000+i:04d}"
        first = random.choice(REAL_FIRST_NAMES)
        last = random.choice(REAL_LAST_NAMES)
        email = f"{first.lower()}.{last.lower()}@company.fr"
        mobile = f"06 {''.join([str(random.randint(0,9)) for _ in range(8)])}"
        salary = f"{random.randint(25000, 120000)}.00"
        depts = ["Sales", "Engineering", "HR", "Finance", "Operations", "Marketing"]
        dept = random.choice(depts)
        start = (datetime(2015, 1, 1) + timedelta(days=random.randint(0, 3650))).strftime("%Y-%m-%d")
        review = (datetime(2025, 1, 1) + timedelta(days=random.randint(0, 60))).strftime("%Y-%m-%d")
        
        rows.append([emp_id, first, last, email, mobile, salary, dept, start, review])
    
    return rows

def write_csv(filename, rows):
    filepath = os.path.join(OUTPUT_DIR, filename)
    with open(filepath, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerows(rows)
    print(f"✅ Created: {filename}")

# Generate all datasets
print("\n🚀 Generating Realistic Business Data for Mary's S2.2 Accuracy Test\n")

print("📊 CUSTOMER DATABASE (50 records)")
write_csv("mary-customers-001.csv", generate_customer_database())

print("\n💰 FINANCIAL TRANSACTIONS (60 records)")
write_csv("mary-transactions-001.csv", generate_transaction_data())

print("\n📅 TEMPORAL/TIME-SERIES DATA (45 records)")
write_csv("mary-temporal-001.csv", generate_temporal_data())

print("\n🏢 EMPLOYEE ERP EXPORT (40 records - Mixed Business Data)")
write_csv("mary-employees-001.csv", generate_mixed_business_data())

print("\n✅ Realistic business data generated successfully!")
print(f"📁 Location: {OUTPUT_DIR}")
print(f"📈 Total files: 4 datasets, 195 total records")
