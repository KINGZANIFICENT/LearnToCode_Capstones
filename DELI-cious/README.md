# DELI-couse Project

## 🎯 Overview
DELI-couse is a Java Swing application that simulates a deli sandwich shop’s ordering system.  
It lets you build, customize, and submit sandwich orders, then review all past orders in the same session. This README will guide you through the core features, architecture, setup steps, and talking points for your presentation.

---

## 📦 Features
- **Build Your Own Sandwich**
    - Select bread, protein, cheese, toppings, and condiments
    - Live order summary (item count, individual prices, total)

- **Signature Sandwich Templates**
    - BLT, Philly Cheese Steak, and more
    - Templates can be customized on the fly

- **Order Management**
    - “Add to Order” button appends to current order
    - “Past Orders” tab shows a list of all orders placed this session

- **In-Memory Persistence**
    - Orders are stored in memory and displayed until you close the application

---

## 🏗️ Architecture
1. **Model**
    - `Sandwich`, `Topping`, `Order` classes
    - Encapsulate business logic (price calculation, ingredient lists)
2. **View**
    - Swing components: `MainFrame`, `OrderPanel`, `PastOrdersPanel`
    - Uses `JTabbedPane` for “Order” vs. “Past Orders”
3. **Controller**
    - Action listeners on buttons and combo-boxes
    - Updates model and refreshes view in real time
4. **FileManager**
    - `OrderManager` handles loading/saving orders (JSON or XML)

---

## 🚀 Getting Started

### Prerequisites
- **JDK 8+** installed
- **IDE** (IntelliJ IDEA, Eclipse) or command-line `javac` & `java`

### Run Locally
1. **Clone the repo**
   ```bash
   git clone https://github.com/your-username/deli-course.git
   cd DELI-cious
