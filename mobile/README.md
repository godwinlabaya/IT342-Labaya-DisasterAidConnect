# DisasterAidConnect

DisasterAidConnect is a disaster response coordination platform that connects communities, volunteers, and aid organizations in real time. The system enables users to report disaster incidents on an interactive map, request and track aid, and donate to those in need — all from a unified web, mobile, and admin interface.

---

# Key Features

## 🌍 Real-Time Disaster Map
Users can place geo-tagged disaster points on an interactive map with severity levels, proof images, and GCash donation numbers for direct community support.

## 🔐 Role-Based Access Control
Distinct dashboards and workflows for regular users and administrators, with automatic role detection and route protection on both web and mobile.

## 🛠️ Admin Management Panel
Admins can manage all disaster reports, view the full map, mute or remove users, and monitor all platform donations through a dedicated admin interface.

## 💳 GCash Donation Integration
- **Web:** Users can manually donate using the displayed GCash number of the disaster reporter.
- **Mobile:** Includes a deep-link button that redirects users directly to the GCash app for a seamless donation experience.

## 🔕 User Mute System
Admins can restrict specific users from adding map points with:
- Custom mute duration
- Mute reason
- Automatic expiry handling

Muted users are informed through the notification system.

## 🔑 Security Question Password Recovery
Custom forgot-password flow using user-defined security questions, allowing secure account recovery without email verification.

---

# System Architecture

The application follows a **Three-Tier Architecture**:

## 1. Presentation Tier
Handles user interaction through:
- Web Application (React.js)
- Android Mobile Application

## 2. Logic Tier
Processes business logic using:
- Java Spring Boot REST API
- Authentication & Authorization
- Donation & Payment Processing
- Disaster Management Services

## 3. Data Tier
Handles persistent data storage using:
- Supabase PostgreSQL Database
- Supabase Authentication

---

# Tech Stack

## Frontend (Web)

| Technology | Description |
|---|---|
| React.js | Frontend framework |
| React Router DOM | Client-side routing |
| Plain CSS | Custom styling system |
| Leaflet.js | Interactive maps |
| OpenStreetMap | Map tile provider |
| Supabase | Authentication & Database |

---

## Frontend (Mobile)

| Technology | Description |
|---|---|
| Android Java | Native Android development |
| MVVM Architecture | Activity, ViewModel, Repository pattern |
| Retrofit2 | API networking |
| OkHttp | Supabase PostgREST requests |
| Supabase | Shared authentication & database |

---

## Backend

| Technology | Description |
|---|---|
| Spring Boot 3.x | Backend framework |
| Java 21 | Programming language |
| Spring Data JPA | ORM framework |
| Hibernate | Database ORM |
| PostgreSQL | Relational database |
| PayMongo | GCash payment integration |
| Docker | Containerization |
| Render | Cloud deployment |
| Spring Dotenv | Environment variable management |

---

## Database & Authentication

| Technology | Description |
|---|---|
| Supabase | PostgreSQL + Authentication |
| Row Level Security (RLS) | Database security policies |
| Security Definer Functions | Admin-level secured database access |

---

# Database Tables

## users
Stores user accounts, roles, profile information, and authentication details.

### Main Fields
- id
- username
- email
- role
- security_question
- security_answer
- profile_picture
- created_at

---

## disasters
Stores disaster reports created by administrators.

### Main Fields
- id
- title
- description
- severity_level
- status
- latitude
- longitude
- image_url_1
- image_url_2
- image_url_3
- gcash_number
- created_by
- created_at

---

## donations
Stores user monetary donations linked to disasters.

### Main Fields
- id
- user_id
- disaster_id
- amount
- status
- donated_at

---

## payments
Stores GCASH and payment transaction records.

### Main Fields
- id
- donation_id
- payment_date
- payment_method
- payment_status
- processing_fee
- total_amount
- transaction_reference
- payment_intent_id

---

## notifications
Stores notifications sent to users.

### Main Fields
- id
- user_id
- type
- title
- message
- is_read
- created_at

---

# Entity Relationships

- One User can create multiple Donations
- One Admin can create multiple Disaster Reports
- One Disaster can receive multiple Donations
- One Donation is linked to one Payment Record
- One User can receive multiple Notifications

---

# Deployment

| Service | Platform |
|---|---|
| Backend API | Render Web Service |
| Frontend Web App | Render Static Site |
| Database | Supabase Cloud |
| Region | ap-southeast-1 |

---

# Security Features

- JWT-based authentication via Supabase Auth
- Role-based authorization
- Route protection (Web & Mobile)
- Row Level Security (RLS)
- Admin-only disaster management
- User mute and moderation system
- Secure password recovery flow

---

# Developers

  Godwin M. Labaya

---

# License

This project is intended for academic purposes only.