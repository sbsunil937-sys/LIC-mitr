# LIC Mitr - Policy Tracker & Premium Manager

A modern, offline-first Android application designed for LIC policyholders and insurance agents to register, track, and manage Life Insurance Corporation of India (LIC) policies, premium cycles (half-yearly/yearly), digital receipt vaults, and cloud database synchronization.

---

## 🌟 Key Features

- **Exact LIC Policy Card Display:**
  - Front card formatted to match real LIC policy bond documents with golden trim, official insignia, and clean typography.
  - Policy Number, Plan Name (e.g. Jeevan Labh #936), Sum Assured, and Premium Amount.
  - Frequency tracking with clear labels (Half-Yearly 6-Month cycle, Yearly, Quarterly, Monthly).

- **Automated Next Premium Due Date Calculation:**
  - Automatically calculates upcoming premium dates based on payment frequency.
  - Live status indicators:
    - 🟢 **Paid / Up to date** (Due in > 30 days)
    - 🟡 **Due Soon** (Due within 30 days)
    - 🔴 **Pending / Overdue** (Due date passed)

- **Instant Payment & Advance Cycle:**
  - Record payments with UPI, Net Banking, Cards, or Cheque.
  - Automatically advances next due date by policy frequency (e.g. +6 months).

- **Digital Receipt & Policy Vault:**
  - Upload policy bond photos and premium receipts using zero-permission Android Photo Picker.
  - Full-screen document preview with zoom and download/share.

- **Supabase Cloud Database Integration:**
  - Dual persistence architecture: instant local **Room Database** for offline resilience + **Supabase REST API** cloud sync.
  - Automatically stores policyholder details in Supabase cloud (`policies` and `payment_receipts` tables).

- **Agent & Admin Portal:**
  - Portfolio statistics (Total Active Policies, Overdue Alerts, Total Premium Under Management).
  - Search by policy number, customer name, plan, or phone number.
  - 1-Click CSV Report export for WhatsApp / email sharing.
  - 1-Click "Sync Cloud" button to sync all local policies to Supabase.

---

## 🛠️ Architecture & Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material Design 3 (M3)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Persistence:** Room Database (SQLite) + Kotlin Coroutines & StateFlow
- **Networking & Cloud Sync:** Retrofit 2 + OkHttp 3 + Moshi
- **Cloud Backend:** Supabase (PostgREST API)
- **Image Loading:** Coil Compose
- **Compatibility:** Android 7.0 (API 24) to Android 16 (API 36)

---

## ⚙️ Configuration & Supabase Setup

Add the following credentials to your `.env` file (or configure in AI Studio Secrets):

```properties
SUPABASE_PROJECT_ID=fldgbvzimoxfnjavzdpg
SUPABASE_URL=https://fldgbvzimoxfnjavzdpg.supabase.co
SUPABASE_KEY=your_supabase_anon_key_here
```

### Supabase SQL Schema

Execute this SQL query in your Supabase SQL Editor:

```sql
CREATE TABLE IF NOT EXISTS policies (
    id BIGSERIAL PRIMARY KEY,
    policy_number TEXT UNIQUE NOT NULL,
    customer_name TEXT NOT NULL,
    customer_dob TEXT,
    mobile_number TEXT,
    email TEXT,
    address TEXT,
    policy_name TEXT,
    policy_type TEXT,
    sum_assured NUMERIC,
    premium_amount NUMERIC NOT NULL,
    frequency_months INTEGER DEFAULT 6,
    start_date TEXT,
    next_premium_date TEXT,
    last_payment_date TEXT,
    nominee_name TEXT,
    nominee_relation TEXT,
    agent_name TEXT,
    agent_code TEXT,
    status TEXT DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS payment_receipts (
    id BIGSERIAL PRIMARY KEY,
    policy_number TEXT REFERENCES policies(policy_number) ON DELETE CASCADE,
    payment_date TEXT NOT NULL,
    amount_paid NUMERIC NOT NULL,
    payment_mode TEXT,
    transaction_ref TEXT,
    period_covered TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

---

## 🚀 How to Build

```bash
# Compile and run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```
