# Lumina Spend — AI Smart Expense & Budget Hub

Lumina Spend is a mobile-first personal expense and adaptive budgeting Android application that pairs **Gemini multimodal intelligence** (receipt vision OCR, voice-to-structured-expense parsing, and proactive spend insights) with **zero-latency local persistence** and a high-contrast obsidian dark interface.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product and architectural choices reflect your confirmed preferences from our design interview and govern how Lumina Spend will be built.

- **Confirmed Decision 1 — Default Currency & Formatting**: **Indian Rupee (`INR` / `₹`)** with Indian numeral grouping (e.g., `₹42,500 / ₹60,000`) is the default currency, with an instant switcher in the top settings sheet supporting `INR (₹)`, `USD ($)`, `EUR (€)`, `GBP (£)`, and `JPY (¥)`.
- **Confirmed Decision 2 — Multimodal AI Ingestion Flow**: **Full Multimodal Capture** combining Android's zero-permission **Photo Picker**, live **Camera Capture**, **Speech-to-Text (`SpeechRecognizer`)**, and **Smart Natural-Language Text Input**, all routed through **Firebase AI Logic (`gemini-3.8-flash`)** with strict JSON schema enforcement.
- **Confirmed Decision 3 — Multi-Tab Navigation Architecture**: **Four-Tab Hub Layout** featuring **Hub** (Overview, Hero Gauge, Quick-Add Strip, AI Alert Carousel), **Analytics** (Weekly/Monthly velocity charts, subscription audit, CSV export), **Categories** (Custom categories, emoji/color pickers, budget caps, merchant mapping rules), and **History** (Searchable, date-grouped editable ledger).
- **Confirmed Decision 4 — Local Persistence & Seed Data**: **Room SQLite Database + DataStore Preferences** for 100% offline-capable local durability, pre-seeded on first launch with 6 realistic categories and 8 transactions so gauges, burn-rate metrics, and anomaly banners work out of the box.

---

## 1. Overview & Core Concept

- **What It Does**: Lumina Spend eliminates manual bookkeeping friction by letting users snap a photo of a paper receipt, speak a natural sentence (*"Spent 350 rupees on dinner with friends at Paradise Biryani"*), or type a shorthand note (*"Swiggy order 420"*). Gemini extracts the exact amount, vendor, date, line items, tax split, tags, and category, while an adaptive budgeting engine tracks daily burn velocity and flags anomalies before budgets are breached.
- **Target Audience / Persona**: Everyday consumers, professionals, and budget-conscious households who want effortless daily expense logging, transparent category ceilings, and intelligent spend-velocity warnings without spreadsheet complexity.
- **Key Value**: Turns unstructured voice, text, and receipt photos into structured financial records in seconds while proactively warning users about spend spikes, duplicate charges, and category overruns.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **First-Launch Dashboard Experience**: Opens directly to the **Hub** tab for the active month with 8 realistic seed transactions (`₹42,500 / ₹60,000` spent — `71%`). The circular glassmorphic gauge animates into place alongside a live velocity badge (*"Paced to finish ₹4,200 under budget"*) and active anomaly cards.
2. **Voice & Smart Text Quick-Log**: Tapping **Voice Entry** launches a live microphone listening sheet (or smart conversational input) where speaking or typing *"Fuel at Indian Oil 1200"* invokes Gemini structured extraction, previews the parsed fields (Merchant, Amount, Date, Category, Tags, Sentiment Note), and saves to the ledger in one tap.
3. **Receipt Lens (Camera & Photo Picker)**: Tapping **Scan Receipt** lets the user snap a receipt photo or pick an image from their gallery. Gemini Vision extracts the merchant name, date, tax split, line items, and total amount into an editable confirmation sheet before committing to the database.
4. **Custom Category & Self-Learning Merchant Rules**: On the **Categories** tab, users create or edit categories with custom emoji badges, neon accent colors, and monthly budget ceilings. Reassigning a merchant's category offers to save a **Self-Learning Merchant Rule** so future expenses from that vendor map automatically.
5. **Analytics & CSV Export**: On the **Analytics** tab, users inspect weekly vs. monthly spend bars, category distribution donut charts, recurring subscription totals, and export any month's ledger to a standard `.csv` file via the Android system file saver or share sheet.

### Visual Identity & Theme
- **Aesthetic Direction**: **Obsidian Financial Cockpit** — high-contrast dark UI with layered glassmorphic cards, crisp 1dp structural borders, and luminous semantic accents.
- **Color Palette & Mood**:
  - `--background`: Deep Obsidian (`#0B0F19`)
  - `--surface-card`: Elevated Slate (`#161F30`) with subtle `#22304A` hairline borders
  - `--primary`: Vibrant Indigo (`#6366F1`) for interactive actions, AI triggers, and active navigation pills
  - `--accent-surplus`: Emerald (`#10B981`) for positive runway, under-budget velocity, and savings badges
  - `--accent-Warning`: Amber (`#F59E0B`) for 50% and 80% category budget warnings
  - `--accent-deficit`: Crimson Rose (`#F43F5E`) for 100%+ budget breaches, 3x median spikes, and duplicate alerts
- **Typography & Hierarchy**:
  - **Display & Monetary Readouts**: **Space Grotesk** (bundled locally) for bold, tabular numerical precision in currency totals, gauges, and percentages.
  - **Body & UI Labels**: **Plus Jakarta Sans** (bundled locally) for crisp legibility across merchant names, AI insight banners, and category pills.
- **Component Styling & Layout**:
  - Edge-to-edge layout respecting status bar and navigation bar insets.
  - Interactive custom-drawn Jetpack Compose `Canvas` circular velocity gauge and interactive multi-segment Donut Chart with tap-to-highlight category inspection.
- **Interactive Feedback & Motion**:
  - Smooth spring-animated gauge sweeps (`animateFloatAsState`), staggered card reveals, shimmer/pulse states during Gemini AI extraction, and instant snackbar confirmation on transaction edits or CSV export.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Gemini Structured JSON Output (`Schema.obj`) with Local Smart Fallback**
  - *Chosen Approach*: Configure `Firebase.ai.generativeModel("gemini-3.8-flash")` with a strict `responseSchema` matching the expense extraction contract (`merchant`, `amount`, `currency`, `date`, `category`, `tags`, `taxAmount`, `lineItems`, `isSubscription`, `sentimentNote`), paired with a deterministic local NLP/regex parser fallback when offline.
  - *Why*: Guarantees valid, markdown-free JSON from Gemini for both voice/text notes and receipt images while ensuring the app remains 100% functional even without network connectivity.
  - *Alternatives Considered*: Free-form text prompting with regex markdown stripping (fragile and prone to parsing errors).
- **Decision 2 — Proactive Anomaly & Burn-Velocity Engine**
  - *Chosen Approach*: Compute real-time budget alerts locally whenever transactions or budgets change:
    1. **Threshold Warnings** at `50%` (Watch), `80%` (Near Ceiling), and `100%+` (Overrun) per category.
    2. **Burn Velocity & Runway** comparing daily spend rate (`spentSoFar / dayOfMonth`) against allowed daily pace (`monthlyBudget / daysInMonth`).
    3. **3x Category Median Spike & Duplicate Detection** flagging any single transaction exceeding `3×` the category's median transaction value or matching the same merchant + amount within 48 hours.
  - *Why*: Delivers instantaneous, deterministic alert updates on every transaction edit without waiting for a network round-trip, while allowing users to trigger on-demand Gemini AI coaching insights.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (Edge-to-Edge)                         │
│             configureAppCheck(this, intent) -> LuminaSpendApp              │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │
          ┌───────────────────────────┼───────────────────────────┐
          ▼                           ▼                           ▼
┌───────────────────┐       ┌───────────────────┐       ┌───────────────────┐
│   Hub & Alerts    │       │  AI Ingestion UI  │       │ Analytics, Cats & │
│  • Month/Curr Bar │       │  • Receipt Lens   │       │     History       │
│  • Hero Gauge     │       │    (Camera/Picker)│       │  • Donut & Bars   │
│  • Alert Carousel │       │  • Voice QuickLog │       │  • Category Caps  │
│  • Donut & Feed   │       │  • Smart Note Box │       │  • CSV Exporter   │
└─────────┬─────────┘       └─────────┬─────────┘       └─────────┬─────────┘
          └───────────────────────────┼───────────────────────────┘
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     LuminaViewModel (MVVM StateFlow)                       │
│   • Reactive Month Ledger & Category Aggregation                           │
│   • Velocity, Runway, 50/80/100% Ceiling & 3x Median Anomaly Engine        │
└──────────────────┬──────────────────────────────────────┬──────────────────┘
                   ▼                                      ▼
┌─────────────────────────────────────┐  ┌───────────────────────────────────┐
│      LuminaRepository (Local)       │  │     GeminiExpenseAiService        │
│  • Room DAO (Transactions,          │  │  • Firebase.ai (gemini-3.8-flash) │
│    Categories, MerchantRules,       │  │  • Multimodal Receipt OCR         │
│    DismissedAlerts)                 │  │  • Voice/Text Structured JSON     │
│  • Preferences (Currency, Budget)   │  │  • AI Spend Coaching Insights     │
└─────────────────────────────────────┘  └───────────────────────────────────┘
```

### Data Model & State
- **`CategoryEntity`**: `id`, `name`, `emoji`, `colorHex`, `monthlyBudget`, `isCustom`, `sortOrder`.
- **`TransactionEntity`**: `id`, `merchant`, `amount`, `currency`, `dateEpochDay`, `dateString` (`YYYY-MM-DD`), `categoryId`, `categoryName`, `note`, `tagsCsv`, `taxAmount`, `lineItemsSummary`, `isSubscription`, `sentimentNote`, `createdAt`.
- **`MerchantRuleEntity`**: `merchantKeyword` (normalized lowercase), `targetCategoryId`, `targetCategoryName` — powers the self-learning categorization engine.
- **`DismissedAlertEntity`**: `alertKey`, `monthYear` — persists dismissed warning cards across app restarts.

### Interactive Component & State Mapping
- **Month Switcher & Currency Selector**: Updates `selectedYearMonth` and `activeCurrency` in `LuminaViewModel`, immediately recalculating the Hero Gauge, Category Breakdown, Anomaly Alerts, and Transaction Feed via reactive `combine()` flows.
- **Quick-Add Strip (`Scan Receipt` / `Voice Entry` / `Manual Quick Add`)**: Opens dedicated modal sheets with live camera/photo preview or speech recognizer state, executes Gemini extraction with a 60-second timeout and cancellation handling, and populates an editable review card before saving to Room.
- **Alert Carousel & Notification Bell**: Displays active threshold (`50%`, `80%`, `100%`), velocity, duplicate, and `3×` median spike alerts; tapping the bell opens the full Alerts Drawer, and dismissing an alert records its key in Room.
- **Category Breakdown & Editor**: Tapping any slice of the Donut Chart or category pill filters the recent feed; tapping `+ Custom` or an existing category opens the Category Editor with emoji grid, neon color swatches, and budget slider/input.
