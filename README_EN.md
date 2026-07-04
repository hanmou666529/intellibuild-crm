<p align="center">
  <img alt="logo" src="image/logo1.png" width="80">
</p>
<h1 align="center">IntelliBuild CRM</h1>
<p align="center">
  <b>AI-Powered Smart CRM System for Building Materials Sales</b>
</p>
<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/Spring%20Boot-2.5.15-brightgreen" alt="Spring Boot"></a>
  <a href="#"><img src="https://img.shields.io/badge/Vue-2.6.12-4FC08D" alt="Vue"></a>
  <a href="#"><img src="https://img.shields.io/badge/Element%20UI-2.15-bluetop" alt="Element UI"></a>
  <a href="#"><img src="https://img.shields.io/badge/MySQL-8.0-4479A1" alt="MySQL"></a>
  <a href="#"><img src="https://img.shields.io/badge/license-MIT-blue" alt="License"></a>
</p>

---

## 📋 Overview

**IntelliBuild CRM** is a smart customer relationship management system tailored for the building materials sales industry, built on top of RuoYi-Vue 3.9.0. It features a decoupled front-end/back-end architecture, an integrated AI Copilot streaming assistant, and full-lifecycle customer management, sales pipeline tracking, and data analytics dashboards.

### Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 2.5.15, MyBatis, Spring Security, Redis, Jwt |
| Frontend | Vue 2.6.12, Element UI, ECharts |
| Database | MySQL 8.0 |
| Build | Maven, npm |

---

## ✨ Features

### CRM Full-Lifecycle Management

| Module | Description |
|--------|-------------|
| 📋 **Customer** | CRUD, assignment, public pool (put in / claim / assign), duplicate detection & merge, batch import/export |
| 📞 **Follow-ups** | Customer follow-up history with multiple record types |
| 📦 **Orders** | Order creation, line items, status transitions |
| 📄 **Contracts** | Contract signing, archiving, order linkage |
| 📈 **Sales Pipeline** | Lead → Interest → Quote → Won → Payment, fully visualized |
| 🏷️ **Products** | Product categories & product information management |
| 💰 **Payment Plans** | Payment tracking and schedule management |
| 🔔 **Notifications** | In-system notification push |

### AI Capabilities

- 🤖 **AI Copilot** — Streaming conversational assistant, query customers/orders/contracts in natural language
- 🧭 **Role-Based Guidance** — Tailored onboarding for sales reps, managers, and admins

### Data Dashboard

- 📊 Customer stats overview (total, new, pool, follow-ups)
- 📉 Customer source / tier distribution
- 🥤 Sales funnel analysis
- 📈 Follow-up trend chart

---

## 🎨 Design Style

**Black & White Minimal**

- Colors: `#000` / `#fff` / `#f4f4f6`
- Font: Inter (Google Fonts)
- Frosted glass navigation + pill-style UI
- Video-background login page + frosted glass card + mouse parallax effects
- Per-module accent color system

| Module | Accent |
|--------|--------|
| Dashboard | `#0052D9` Blue |
| Customer | `#00A86B` Green |
| Order | `#7C3AED` Purple |
| Contract | `#0891B2` Cyan |
| Follow-up | `#D97706` Amber |
| Pipeline | `#FF6B35` Orange |
| Product | `#DC2626` Rose |
| Notification | `#4F46E5` Indigo |
| Payment | `#0D9488` Teal |
| Pool | `#4338CA` Deep Indigo |

---

## 🖥️ Screenshots

### 1. Admin Dashboard (Super Admin)

The super admin has full system access, including system management, CRM management, and all menus.

<p align="center">
  <img alt="Super Admin Home" src="image/1.png" width="90%">
</p>

---

### 2. Admin Workspace

The workspace aggregates key business data and quick action entries.

<p align="center">
  <img alt="Super Admin Workspace" src="image/2.png" width="90%">
</p>

---

### 3. Product Management

Category management and product listing for building materials, with enable/disable controls.

<p align="center">
  <img alt="Product Management" src="image/3.png" width="90%">
</p>

---

### 4. AI Assistant

AI Copilot integration for querying customers, orders, contracts, and more via natural language.

<p align="center">
  <img alt="AI Assistant" src="image/4.png" width="90%">
</p>

---

### 5. Manager Workspace

Managers have CRM business menus, with system administration menus removed compared to super admin.

<p align="center">
  <img alt="Manager Workspace" src="image/5.png" width="90%">
</p>

---

### 6. Employee Analytics Dashboard

Employee accounts can view personal analytics dashboards for customers, orders, and follow-ups.

<p align="center">
  <img alt="Employee Analytics Dashboard" src="image/6.png" width="90%">
</p>

---

### 7. AI Assistant (Extended)

Extended AI assistant page with richer conversational interaction and intelligent analysis.

<p align="center">
  <img alt="AI Assistant Extended" src="image/7.png" width="90%">
</p>

---

## 🗄️ Database Schema (11 CRM Tables)

| Table | Description |
|-------|-------------|
| `crm_customer` | Customer info (with pool flag, assigned user) |
| `crm_followup` | Follow-up records |
| `crm_order` | Orders |
| `crm_order_item` | Order line items |
| `crm_contract` | Contracts |
| `crm_pipeline` | Sales pipeline stages |
| `crm_product` | Products |
| `crm_product_category` | Product categories |
| `crm_notification` | Notifications |
| `crm_payment_plan` | Payment plans |
| `crm_customer_pool_log` | Public pool operation log |

---

## 🚀 Quick Start Guide

### Prerequisites

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0
- Redis
- Node.js 14+

---

### Step 1: Initialize the Database

The project uses MySQL 8.0. Import the SQL script before first run.

```bash
# 1. Log into MySQL
mysql -u root -p

# 2. Create the database
mysql> CREATE DATABASE IF NOT EXISTS `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
mysql> exit

# 3. Import the SQL script (table structure + seed data)
mysql -u root -p ry-vue < sql/ry-vue.sql
```

> `sql/ry-vue.sql` contains the full database schema and demo data. **This step is mandatory** for the project to function.

---

### Step 2: Configure Database Connection

Open `ruoyi-admin/src/main/resources/application-druid.yml` and update the database connection info:

```yaml
# Lines 9-11
url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
username: root          # Change to your MySQL username
password: password  # Change to your MySQL password
```

---

### Step 3: Configure Redis

Open `ruoyi-admin/src/main/resources/application.yml` and verify the Redis connection:

```yaml
# Around lines 40-45
redis:
  host: localhost
  port: 6379
  password:       # Leave empty if no password is set
```

Make sure Redis is running locally.

---

### Step 4: Start the Backend

```bash
# Build the project
mvn clean install -DskipTests

# Start the server
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

The backend starts at `http://localhost:8080` by default.

---

### Step 5: Start the Frontend

```bash
cd ruoyi-ui

# Install dependencies
npm install

# Run in dev mode (hot reload)
npm run dev
```

The frontend starts at `http://localhost:1024` by default. Open it in your browser.

> For production build, run `npm run build:prod`. The output is in `ruoyi-ui/dist/`.

---

### Default Accounts

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| User | ry | admin123 |

---

## 📁 Project Structure

```
ruoyi-admin/          # Admin module (Controllers)
├── controller/crm/   # CRM controllers (11)
ruoyi-system/         # Business logic module
├── domain/crm/       # Domain models (11)
├── mapper/crm/       # Mapper interfaces + XML (11 pairs)
├── service/crm/      # Business services (11 interfaces + implementations)
ruoyi-ui/src/
├── views/crm/        # Frontend pages (11 modules)
├── api/crm/          # API definitions (11 files)
└── styles/           # Global styles
```

---

## 📜 License

[MIT License](LICENSE)

> Built on top of [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 3.9.0
