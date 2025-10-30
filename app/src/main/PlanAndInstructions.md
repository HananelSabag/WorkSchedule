# 📅 Work Schedule App - Complete Development Specification
# מפרט מלא לפיתוח אפליקציית סידור עבודה

---

## 📱 Project Overview / סקירה כללית

**English:** Build a professional Android app for managing weekly work shifts for a small business with 6 employees and 27 weekly shifts.

**עברית:** בנה אפליקציית Android מקצועית לניהול משמרות עבודה שבועיות לעסק קטן עם 6 עובדים ו-27 משמרות בשבוע.

### Technical Stack / סטק טכנולוגי
- **Language / שפה:** Kotlin
- **UI Framework:** Jetpack Compose
- **Database:** Room
- **Package Name:** `com.hananel.workschedule`
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34

### Developer Info / מידע מפתח
**Developed by / פותח על ידי:** חננאל סבג (Hananel Sabag)

---

## 👥 Default Employees / רשימת עובדים

**English:** The app should start with these 6 employees:
1. מאור (Maor)
2. דוד (David)
3. אלכס (Alex)
4. דן (Dan)
5. סלים (Salim)
6. חננאל (Hananel)

**עברית:** האפליקציה צריכה להתחיל עם 6 העובדים האלה בדיוק.

---

## 📅 Weekly Shifts / משמרות שבועיות

### Regular Days (Sunday-Thursday) / ימים רגילים (ראשון-חמישי)

**Normal Mode / מצב רגיל:**

| Shift Name / שם המשמרת | Time / שעות | Hours / משך |
|---|---|---|
| בוקר (Morning) | 06:45-15:00 | 8.25 hours |
| בוקר ארוך (Long Morning) | 06:45-19:00 | 12.25 hours |
| צהריים (Afternoon) | 14:45-23:00 | 8.25 hours |
| לילה (Night) | 22:30-07:00 | 8.5 hours |

**Saving Mode / מצב חיסכון:**
When user activates "Shift Saving" for a day, use ONLY these 2 shifts:

כאשר המשתמש מפעיל "חיסכון במשמרות" ליום, השתמש רק ב-2 המשמרות האלה:

| Shift Name / שם המשמרת | Time / שעות | Hours / משך |
|---|---|---|
| בוקר ארוך (Long Morning) | 06:45-19:00 | 12.25 hours |
| לילה ארוך (Long Night) | 18:45-07:00 | 12.25 hours |

**Important:** The afternoon shift disappears in saving mode!
**חשוב:** משמרת הצהריים נעלמת במצב חיסכון!

---

### Friday / יום שישי

**Normal Mode / מצב רגיל:**

| Shift Name / שם המשמרת | Time / שעות | Hours / משך |
|---|---|---|
| בוקר קצר (Short Morning) | 06:45-13:00 | 6.25 hours |
| צהריים (Afternoon) | 14:45-23:00 | 8.25 hours |
| לילה (Night) | 22:30-07:00 | 8.5 hours |

**Saving Mode / מצב חיסכון:**

| Shift Name / שם המשמרת | Time / שעות | Hours / משך |
|---|---|---|
| בוקר קצר (Short Morning) | 06:45-13:00 | 6.25 hours |
| לילה ארוך (Long Night) | 18:45-07:00 | 12.25 hours |

---

### Saturday / יום שבת

**English:** Saturday has only 3 shifts, no long morning:
**עברית:** שבת יש רק 3 משמרות, אין בוקר ארוך:

| Shift Name / שם המשמרת | Time / שעות | Hours / משך |
|---|---|---|
| בוקר (Morning) | 06:45-15:00 | 8.25 hours |
| צהריים (Afternoon) | 14:45-23:00 | 8.25 hours |
| לילה (Night) | 22:30-07:00 | 8.5 hours |

---

## 🕎 Shabbat Observer Feature / עובד שומר שבת

**English:** When an employee is marked as "Shabbat Observer", automatically block these 4 shifts:

**עברית:** כאשר עובד מסומן כ"שומר שבת", חסום אוטומטית את 4 המשמרות האלה:

1. **שישי - צהריים** (Friday Afternoon) - 14:45-23:00
2. **שישי - לילה** (Friday Night) - 22:30-07:00 
3. **שישי - לילה ארוך** (Friday Long Night) - 18:45-07:00
4. **שבת - בוקר** (Saturday Morning) - 06:45-15:00
5. **שבת - צהריים** (Saturday Afternoon) - 14:45-23:00

**Important:** These blocks are automatic and permanent for Shabbat observers!
**חשוב:** החסימות האלה אוטומטיות וקבועות לשומרי שבת!

---

## 🎨 UI/UX Design / עיצוב ממשק

### Colors / צבעים

```kotlin
val PrimaryGreen = Color(0xFF4CAF50)
val PrimaryBlue = Color(0xFF2196F3)
val PrimaryTeal = Color(0xFF2C7873)  // For logo
val BlockedRed = Color(0xFFEF5350)
val CanOnlyBlue = Color(0xFF1976D2)
```

### Direction / כיוון
**All UI must be RTL (Right-to-Left) for Hebrew!**
**כל הממשק חייב להיות RTL מימין לשמאל עבור עברית!**

---

## 📱 App Screens / מסכי האפליקציה

### 1. Splash Screen / מסך פתיחה

**Duration:** 2 seconds / **משך:** 2 שניות

**Content / תוכן:**
- Logo: Calendar icon in teal color (תכלת)
- Text "סידור עבודה" (Work Schedule)
- Bottom text: "פותח על ידי חננאל סבג" (Developed by Hananel Sabag)
- English name: "Hananel Sabag"

---

### 2. Home Screen / מסך הבית

**English:** Show 3 large buttons:
**עברית:** הצג 3 כפתורים גדולים:

1. **"סידורים אחרונים"** (Recent Schedules)
   - Color: Blue / צבע: כחול
   - Show count: (5) - number of saved schedules
   - הצג ספירה: (5) - מספר הסידורים השמורים

2. **"סידור חדש"** (New Schedule)
   - Color: Green / צבע: ירוק
   - Goes directly to Blocking Screen
   - עובר ישירות למסך החסימות

3. **"ניהול עובדים"** (Employee Management)
   - Color: Gray / צבע: אפור
   - Settings for employees
   - הגדרות עובדים

---

### 3. Employee Management Screen / מסך ניהול עובדים

**Components / רכיבים:**

1. **Add Employee Section / קטע הוספת עובד:**
   - Text input: "שם עובד חדש" (New employee name)
   - Button: "+" (Add button)

2. **Employee List / רשימת עובדים:**
   
   **Each employee card shows / כל כרטיס עובד מציג:**
   - Employee name / שם העובד
   - Checkbox: "שומר שבת" (Shabbat Observer)
   - Statistics: "5 / 2" 
     - Red number = Cannot blocks / מספר אדום = חסימות "לא יכול"
     - Blue number = Can Only blocks / מספר כחול = חסימות "יכול רק"
   - Delete button (trash icon) / כפתור מחיקה (אייקון פח)

3. **Info Box / תיבת מידע:**
   ```
   ℹ️ הסבר:
   • סמן "שומר שבת" לעובדים שלא יכולים לעבוד בשישי אחה"צ/לילה ושבת בוקר/אחה"צ
   • החסימות מתעדכנות אוטומטית בכל סידור חדש
   • המספרים: חסימות / יכול רק
   ```

4. **Back Button / כפתור חזרה:**
   - "חזור למסך הבית" (Back to Home)

---

### 4. Blocking Screen / מסך החסימות

**English:** This is where the user marks when employees CANNOT work or CAN ONLY work.

**עברית:** כאן המשתמש מסמן מתי עובדים לא יכולים לעבוד או יכולים רק לעבוד.

#### Top Section / חלק עליון:

**1. Employee Selector / בוחר עובד:**
```
Dropdown menu: "-- בחר עובד --"
Shows all 6 employees
```

**2. Blocking Mode Buttons / כפתורי מצב חסימה:**

Two buttons side by side:

| Button / כפתור | Color / צבע | Meaning / משמעות |
|---|---|---|
| "לא יכול" (Cannot) | Red / אדום | Employee CANNOT work this shift |
| "יכול רק" (Can Only) | Blue / כחול | Employee CAN ONLY work these shifts |

**Important:** When selected, the button shows a thick border (ring-2)!
**חשוב:** כשנבחר, הכפתור מציג מסגרת עבה!

**3. Shift Saving Buttons / כפתורי חיסכון במשמרות:**

Show buttons for each day: ראשון, שני, שלישי, רביעי, חמישי, שישי
- Purple when active / סגול כשפעיל
- Gray when inactive / אפור כשלא פעיל
- NOT available for Saturday / לא זמין לשבת

**Explanation text / טקסט הסבר:**
```
✓ ימים מסומנים: בוקר ארוך (6:45-19:00) + לילה ארוך (18:45-7:00)
```

#### Schedule Table / טבלת הסידור:

**Columns / עמודות:** ראשון, שני, שלישי, רביעי, חמישי, שישי, שבת

**Rows / שורות:** All shifts for that day / כל המשמרות של היום

**Cells / תאים:**

**English:** 
- Click on cell to add/remove block for selected employee
- Employee names in RED text = "Cannot" blocks
- Employee names in BLUE text = "Can Only" blocks
- Multiple employees can be in same cell (stacked vertically)
- Currently selected employee's cell has YELLOW border

**עברית:**
- לחץ על תא כדי להוסיף/להסיר חסימה לעובד הנבחר
- שמות עובדים בטקסט אדום = חסימות "לא יכול"
- שמות עובדים בטקסט כחול = חסימות "יכול רק"
- מספר עובדים יכולים להיות באותו תא (מוצגים אנכית)
- לתא של העובד הנבחר יש מסגרת צהובה

#### Legend / מקרא:

```
📌 מקרא:
● אדום = לא יכול (חסום)
● כחול = יכול רק (כל השאר חסום)
```

#### Bottom Buttons / כפתורים תחתונים:

1. **"סיימתי עובד זה"** (Finished this employee)
   - Gray button / כפתור אפור
   - Clears employee selection / מנקה בחירת עובד

2. **"צור סידור עבודה"** (Generate Schedule)
   - Blue button / כפתור כחול
   - Runs the algorithm / מריץ את האלגוריתם

---

### 5. Preview Screen / מסך תצוגה מקדימה

#### Top Statistics / סטטיסטיקה עליונה:

```
📊 סטטיסטיקה:
מאור: 4 משמרות
דוד: 5 משמרות
אלכס: 4 משמרות
דן: 5 משמרות
סלים: 4 משמרות
חננאל: 5 משמרות
```

#### View/Edit Mode Buttons / כפתורי צפייה/עריכה:

Two buttons:

| Button / כפתור | Active State / מצב פעיל |
|---|---|
| 👁️ מצב צפייה (View Mode) | Blue background + thick border |
| ✏️ מצב עריכה (Edit Mode) | Orange background + thick border |

**When Edit Mode active / כשמצב עריכה פעיל:**
- Show blinking text: "✏️ לחץ על תא לערוך (מלל חופשי)"
- Table cells become text inputs
- User can type freely in any cell
- תאים הופכים לשדות טקסט
- המשתמש יכול להקליד בחופשיות בכל תא

#### Schedule Table / טבלת הסידור:

**Same structure as Blocking Screen but:**
**אותה מבנה כמו מסך החסימות אבל:**

- Shows assigned employee names / מציג שמות עובדים משובצים
- Empty cells show: "⚠️ ריק" in red / תאים ריקים מציגים באדום
- In edit mode: cells are editable / במצב עריכה: ניתן לערוך תאים

#### Bottom Buttons / כפתורים תחתונים:

1. **"שמור סידור זה"** (Save This Schedule)
   - Purple button / כפתור סגול
   - 💾 Icon

2. **"שתף / ייצא"** (Share / Export)
   - Green button / כפתור ירוק
   - 📤 Icon
   - Opens menu with 3 options:
   
   **Menu Options / אפשרויות תפריט:**
   
   a) **"שתף בווצאפ (טקסט)"** (Share WhatsApp Text)
   - Creates formatted text message
   - יוצר הודעת טקסט מעוצבת
   
   b) **"שתף בווצאפ (תמונה)"** (Share WhatsApp Image)
   - Takes screenshot of table
   - צולם screenshot של הטבלה
   
   c) **"ייצא לאקסל"** (Export to Excel)
   - Creates .xlsx file
   - יוצר קובץ .xlsx

---

### 6. History Screen / מסך היסטוריה

**Shows list of saved schedules:**
**מציג רשימת סידורים שמורים:**

**Each item shows / כל פריט מציג:**
- Week date: "שבוע 12/10/2024"
- Save date: "נשמר ב: 15/10/2024"
- Two buttons:
  - 📂 "פתח" (Open) - Blue
  - 🗑️ Delete icon - Red

**Delete confirmation:**
```
האם למחוק סידור זה?
```

---

## 🧠 Schedule Generation Algorithm / אלגוריתם יצירת סידור

### HARD RULES (Must Never Break) / חוקים קשיחים (אסור לעבור)

**English:** These rules MUST be followed. If cannot be satisfied, leave shift empty.

**עברית:** חוקים אלה חייבים להתקיים. אם לא ניתן לעמוד בהם, השאר משמרת ריקה.

#### 1. Block Rules / חוקי חסימות

```
❌ NEVER assign employee to shift they marked as "Cannot" (red)
❌ אל תשבץ אף פעם עובד למשמרת שסימן כ"לא יכול" (אדום)

✅ If employee has "Can Only" marks (blue), assign them ONLY to those shifts
✅ אם לעובד יש סימוני "יכול רק" (כחול), שבץ אותו רק למשמרות האלה
```

#### 2. Maximum Hours / מקסימום שעות

```
❌ Employee cannot work more than 12 hours in one day
❌ עובד לא יכול לעבוד יותר מ-12 שעות ביום אחד
```

#### 3. Same-Day Overlaps / התנגשויות באותו יום

**English:** Employee CANNOT work these combinations on same day:

**עברית:** עובד לא יכול לעבוד את השילובים האלה באותו יום:

```
❌ בוקר + בוקר ארוך (Morning + Long Morning)
❌ בוקר + צהריים (Morning + Afternoon)
❌ בוקר ארוך + צהריים (Long Morning + Afternoon)
❌ צהריים + לילה (Afternoon + Night)
```

#### 4. Night to Morning = FORBIDDEN / לילה לבוקר = אסור

**English:** NEVER assign employee to morning shift after night shift!

**עברית:** לעולם לא לשבץ עובד לבוקר אחרי לילה!

```
❌ יום X: לילה (22:30-07:00)
   יום X+1: בוקר (06:45-15:00)
   
❌ יום X: לילה ארוך (18:45-07:00)
   יום X+1: בוקר (06:45-15:00)
```

**This gives ZERO rest time! Absolutely forbidden!**
**זה נותן אפס זמן מנוחה! אסור לחלוטין!**

#### 5. Shabbat Observer Auto-Blocks / חסימות אוטומטיות שומר שבת

**English:** If employee is marked "Shabbat Observer", automatically block:

**עברית:** אם עובד מסומן "שומר שבת", חסום אוטומטית:

```
1. שישי - צהריים (Friday Afternoon)
2. שישי - לילה (Friday Night)
3. שישי - לילה ארוך (Friday Long Night)
4. שבת - בוקר (Saturday Morning)
5. שבת - צהריים (Saturday Afternoon)
```

---

### SOFT RULES (Try to Avoid) / חוקים רכים (נסה להימנע)

**English:** Try to avoid these, but allowed if no other option.

**עברית:** נסה להימנע מאלה, אבל מותר אם אין ברירה אחרת.

#### 1. Short Rest Periods / תקופות מנוחה קצרות

```
⚠️ Try to avoid: Night → Afternoon next day (only 8 hours rest)
⚠️ נסה להימנע: לילה → צהריים למחרת (רק 8 שעות מנוחה)

⚠️ Try to avoid: Afternoon → Morning next day (not enough rest)
⚠️ נסה להימנע: צהריים → בוקר למחרת (לא מספיק מנוחה)
```

#### 2. Fair Distribution / חלוקה הוגנת

```
✅ Try to give each employee similar number of shifts
✅ נסה לתת לכל עובד מספר דומה של משמרות
```

---

### Algorithm Steps / שלבי האלגוריתם

**English:**

1. Collect all shifts for the week (27 total, or less with saving mode)
2. Sort shifts by difficulty (shifts with fewer available employees first)
3. For each shift:
   - Find available employees (not blocked, meets all HARD rules)
   - Calculate score for each available employee:
     - +10 points for each shift they already have
     - +5 points if this creates SOFT rule violation
   - Choose employee with LOWEST score
   - Assign them to shift
4. If NO employees available:
   - Leave shift empty
   - Mark for error message

**עברית:**

1. אסוף את כל המשמרות לשבוע (27 סה"כ, או פחות עם מצב חיסכון)
2. מיין משמרות לפי קושי (משמרות עם פחות עובדים זמינים קודם)
3. לכל משמרת:
   - מצא עובדים זמינים (לא חסומים, עומדים בכל החוקים הקשיחים)
   - חשב ציון לכל עובד זמין:
     - +10 נקודות לכל משמרת שכבר יש להם
     - +5 נקודות אם זה יוצר הפרה של חוק רך
   - בחר עובד עם הציון הנמוך ביותר
   - שבץ אותו למשמרת
4. אם אין עובדים זמינים:
   - השאר משמרת ריקה
   - סמן להודעת שגיאה

---

### Error Message / הודעת שגיאה

**Show this ONLY if there are empty cells after generation:**

**הצג את זה רק אם יש תאים ריקים אחרי היצירה:**

```
⚠️ לא ניתן ליצור סידור שלם!

יש משמרות שלא ניתן למלא בגלל:
• יותר מדי חסימות
• חוסר איזון בין "יכול רק" לחסימות
• עובדים לא זמינים

הסידור נוצר עם חורים - תצטרך למלא ידנית בעריכה! ✏️
```

---

## 💾 Database Structure / מבנה Database

### Table: employees

```kotlin
@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,              // "מאור", "דוד", etc.
    val shabbatObserver: Boolean   // true/false
)
```

### Table: schedules

```kotlin
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weekStart: String,         // "2024-10-12"
    val scheduleData: String,      // JSON: Map<"יום-משמרת", List<String>>
    val blocksData: String,        // JSON: Map<"עובד-יום-משמרת", Boolean>
    val canOnlyData: String,       // JSON: Map<"עובד-יום-משמרת", Boolean>
    val savingModeData: String,    // JSON: Map<"יום", Boolean>
    val createdDate: Long          // System.currentTimeMillis()
)
```

---

## 📤 Sharing Features / תכונות שיתוף

### 1. WhatsApp Text / טקסט ווצאפ

**Format / פורמט:**

```
📅 *סידור עבודה - שבוע 12/10/2024*

*ראשון:*
  בוקר: מאור
  בוקר ארוך: -----
  צהריים: דן, אלכס
  לילה: סלים

*שני:*
  בוקר: דוד
  בוקר ארוך: חננאל
  צהריים: -----
  לילה: מאור

[...continue for all days...]

_נוצר באמצעות מערכת שיבוץ עובדים_
_פותח על ידי חננאל סבג_
```

**Code:**
```kotlin
val intent = Intent(Intent.ACTION_SEND)
intent.type = "text/plain"
intent.putExtra(Intent.EXTRA_TEXT, scheduleText)
intent.setPackage("com.whatsapp")
startActivity(Intent.createChooser(intent, "שתף בווצאפ"))
```

### 2. WhatsApp Image / תמונה ווצאפ

**Steps:**
1. Capture the schedule table as Bitmap
2. Save to cache directory
3. Share via WhatsApp

**Code:**
```kotlin
// Capture composable as bitmap
val bitmap = captureComposable(scheduleTable)

// Save to cache
val file = File(context.cacheDir, "schedule_${System.currentTimeMillis()}.png")
val outputStream = FileOutputStream(file)
bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
outputStream.close()

// Get URI
val uri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.provider",
    file
)

// Share
val intent = Intent(Intent.ACTION_SEND)
intent.type = "image/png"
intent.putExtra(Intent.EXTRA_STREAM, uri)
intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
intent.setPackage("com.whatsapp")
startActivity(Intent.createChooser(intent, "שתף בווצאפ"))
```

### 3. Excel Export / ייצוא אקסל

**File name:** `סידור_עבודה_2024-10-12.xlsx`

**Structure:**
- One sheet named "סידור עבודה"
- Columns: ראשון, שני, שלישי, רביעי, חמישי, שישי, שבת
- Rows: Shift names with times
- Cells: Employee names (comma-separated if multiple)
- Right-to-left text direction
- Bold headers
- Borders on all cells

---

## 📁 Project File Structure / מבנה קבצים

```
com.hananel.workschedule/
├── MainActivity.kt
├── ui/
│   ├── SplashScreen.kt
│   ├── HomeScreen.kt
│   ├── EmployeeManagementScreen.kt
│   ├── BlockingScreen.kt
│   ├── PreviewScreen.kt
│   └── HistoryScreen.kt
├── data/
│   ├── Employee.kt
│   ├── Schedule.kt
│   ├── AppDatabase.kt
│   ├── EmployeeDao.kt
│   └── ScheduleDao.kt
├── viewmodel/
│   └── ScheduleViewModel.kt
├── utils/
│   ├── ScheduleGenerator.kt
│   ├── WhatsAppSharer.kt
│   └── ExcelExporter.kt
└── theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

---

## 📦 Required Dependencies / תלויות נדרשות

```gradle
dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Apache POI (Excel)
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    
    // Coil (Images)
    implementation("io.coil-kt:coil-compose:2.5.0")
}
```

---

## ✅ Important Notes / הערות חשובות

### RTL Support / תמיכה ב-RTL

**English:** The entire app must support RTL (Right-to-Left) for Hebrew.

**עברית:** כל האפליקציה חייבת לתמוך ב-RTL מימין לשמאל עבור עברית.

Add to `AndroidManifest.xml`:
```xml
<application
    android:supportsRtl="true"
    ...>
```

### Permissions / הרשאות

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

### FileProvider Setup / הגדרת FileProvider

**For sharing images, add to `AndroidManifest.xml`:**

**לשיתוף תמונות, הוסף ל-`AndroidManifest.xml`:**

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

**Create file: `res/xml/file_paths.xml`:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="shared_images" path="/" />
</paths>
```

---

## 🎯 Implementation Priorities / סדר יישום

**English:** Build the app in this order:

**עברית:** בנה את האפליקציה בסדר הזה:

### Phase 1: Foundation / שלב 1: בסיס
1. ✅ Create project with Jetpack Compose
2. ✅ Setup Room Database
3. ✅ Create data models (Employee, Schedule)
4. ✅ Setup color theme and RTL support

### Phase 2: Basic Screens / שלב 2: מסכים בסיסיים
5. ✅ Splash Screen with logo
6. ✅ Home Screen with 3 buttons
7. ✅ Employee Management Screen
8. ✅ Navigation between screens

### Phase 3: Core Functionality / שלב 3: פונקציונליות ליבה
9. ✅ Blocking Screen with table
10. ✅ Implement "Cannot" and "Can Only" modes
11. ✅ Implement "Shift Saving" feature
12. ✅ Shabbat Observer auto-blocking

### Phase 4: Algorithm / שלב 4: אלגוריתם
13. ✅ Build schedule generation algorithm
14. ✅ Handle all HARD rules
15. ✅ Handle all SOFT rules
16. ✅ Error message for impossible schedules

### Phase 5: Preview & Edit / שלב 5: תצוגה ועריכה
17. ✅ Preview Screen with statistics
18. ✅ View/Edit mode toggle
19. ✅ Manual editing of cells
20. ✅ Save to database

### Phase 6: Sharing / שלב 6: שיתוף
21. ✅ WhatsApp text sharing
22. ✅ WhatsApp image sharing
23. ✅ Excel export
24. ✅ History screen

### Phase 7: Polish / שלב 7: ליטושים
25. ✅ Logo placement
26. ✅ Developer credit
27. ✅ Final UI polish
28. ✅ Testing with 6 employees

---

## 🧪 Testing Checklist / רשימת בדיקות

**English:** Test these scenarios before release:

**עברית:** בדוק תרחישים אלה לפני שחרור:

### Basic Functionality / פונקציונליות בסיסית
- [ ] Add new employee
- [ ] Delete employee
- [ ] Mark employee as Shabbat Observer
- [ ] הוספת עובד חדש
- [ ] מחיקת עובד
- [ ] סימון עובד כשומר שבת

### Blocking / חסימות
- [ ] Block shifts in "Cannot" mode (red)
- [ ] Block shifts in "Can Only" mode (blue)
- [ ] Multiple employees in same cell
- [ ] Shabbat auto-blocks work correctly
- [ ] חסימת משמרות במצב "לא יכול" (אדום)
- [ ] חסימת משמרות במצב "יכול רק" (כחול)
- [ ] מספר עובדים באותו תא
- [ ] חסימות שבת אוטומטיות עובדות

### Shift Saving / חיסכון משמרות
- [ ] Activate shift saving for regular day
- [ ] Activate shift saving for Friday
- [ ] Verify afternoon shift disappears
- [ ] Verify long shifts appear
- [ ] הפעלת חיסכון ליום רגיל
- [ ] הפעלת חיסכון לשישי
- [ ] צהריים נעלם
- [ ] משמרות ארוכות מופיעות

### Algorithm / אלגוריתם
- [ ] Generate schedule with no blocks
- [ ] Generate schedule with many blocks
- [ ] Generate schedule with "Can Only"
- [ ] Verify no night→morning assignments
- [ ] Verify no same-day overlaps
- [ ] Verify max 12 hours per day
- [ ] Fair distribution of shifts
- [ ] יצירת סידור ללא חסימות
- [ ] יצירת סידור עם הרבה חסימות
- [ ] יצירת סידור עם "יכול רק"
- [ ] אין לילה→בוקר
- [ ] אין התנגשויות באותו יום
- [ ] מקסימום 12 שעות ביום
- [ ] חלוקה הוגנת

### Preview & Edit / תצוגה ועריכה
- [ ] View mode shows schedule
- [ ] Edit mode allows typing
- [ ] Empty cells show "⚠️ ריק"
- [ ] Statistics show correct counts
- [ ] מצב צפייה מציג סידור
- [ ] מצב עריכה מאפשר הקלדה
- [ ] תאים ריקים מציגים אזהרה
- [ ] סטטיסטיקה נכונה

### Sharing / שיתוף
- [ ] WhatsApp text sharing works
- [ ] WhatsApp image sharing works
- [ ] Excel export creates valid file
- [ ] שיתוף טקסט לווצאפ עובד
- [ ] שיתוף תמונה לווצאפ עובד
- [ ] ייצוא אקסל יוצר קובץ תקין

### Persistence / שמירה
- [ ] Schedule saves to database
- [ ] Schedule loads from history
- [ ] Delete schedule works
- [ ] Employee settings persist
- [ ] סידור נשמר למסד נתונים
- [ ] סידור נטען מהיסטוריה
- [ ] מחיקת סידור עובדת
- [ ] הגדרות עובדים נשמרות

---

## 🎨 UI Components Details / פרטי רכיבי ממשק

### Table Styling / עיצוב טבלה

**English:** The schedule table is the core component. Style it carefully:

**עברית:** טבלת הסידור היא הרכיב המרכזי. עצב אותה בקפידה:

```kotlin
// Column headers - Days
Row(modifier = Modifier.background(Color(0xFF4CAF50))) {
    Text("ראשון", color = Color.White, fontWeight = FontWeight.Bold)
    Text("שני", color = Color.White, fontWeight = FontWeight.Bold)
    // ... etc
}

// Row headers - Shifts
Column(modifier = Modifier.background(Color(0xFFE8F5E9))) {
    Text("בוקר", fontWeight = FontWeight.Bold)
    Text("06:45-15:00", fontSize = 10.sp, color = Color.Gray)
}

// Cells
Box(
    modifier = Modifier
        .border(1.dp, Color.Gray)
        .padding(4.dp)
        .clickable { /* handle click */ }
) {
    // Cell content
}
```

### Button Styling / עיצוב כפתורים

```kotlin
Button(
    onClick = { /* action */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF4CAF50)
    ),
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
    shape = RoundedCornerShape(12.dp)
) {
    Icon(Icons.Default.Add, contentDescription = null)
    Spacer(modifier = Modifier.width(8.dp))
    Text("כפתור", fontSize = 18.sp, fontWeight = FontWeight.Bold)
}
```

### Input Fields / שדות קלט

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("שם עובד חדש") },
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    textStyle = LocalTextStyle.current.copy(
        textDirection = TextDirection.Rtl
    ),
    singleLine = true
)
```

---

## 🔤 Hebrew Typography / טיפוגרפיה עברית

**Font sizes / גדלי גופן:**

```kotlin
val Typography = Typography(
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
)
```

**Apply RTL to all text:**

```kotlin
CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl
) {
    // Your content here
}
```

---

## 📊 Example Data Flow / דוגמת זרימת מידע

**English:** Here's how data flows through the app:

**עברית:** כך המידע זורם באפליקציה:

### 1. User Creates Blocks / משתמש יוצר חסימות

```
User → Blocking Screen → ViewModel → Database
משתמש → מסך חסימות → ViewModel → מסד נתונים

blocksMap["מאור-ראשון-בוקר"] = true (Cannot)
canOnlyMap["דוד-שני-לילה"] = true (Can Only)
```

### 2. Generate Schedule / יצירת סידור

```
ViewModel → ScheduleGenerator.generate()
         → Apply all rules
         → Return Map<"יום-משמרת", List<Employee>>

Example output:
scheduleMap["ראשון-בוקר"] = ["מאור"]
scheduleMap["ראשון-צהריים"] = ["דן", "אלכס"]
scheduleMap["ראשון-לילה"] = []  // Empty!
```

### 3. Display & Edit / הצגה ועריכה

```
ViewModel → Preview Screen → User edits
         ↓
    Database (Save)
```

### 4. Share / שיתוף

```
Preview Screen → Export/Share → WhatsApp/Excel
```

---

## 🐛 Common Issues & Solutions / בעיות נפוצות ופתרונות

### Issue 1: Text not RTL / הבעיה: טקסט לא RTL

**Solution / פתרון:**
```kotlin
CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    // All content
}
```

### Issue 2: WhatsApp not opening / הבעיה: ווצאפ לא נפתח

**Solution / פתרון:**
```kotlin
// Check if WhatsApp installed
val intent = Intent(Intent.ACTION_SEND)
intent.setPackage("com.whatsapp")
if (intent.resolveActivity(packageManager) != null) {
    startActivity(intent)
} else {
    // Show error: "WhatsApp לא מותקן"
}
```

### Issue 3: Excel file corrupted / הבעיה: קובץ אקסל מקולקל

**Solution / פתרון:**
```kotlin
// Make sure to close streams
workbook.write(outputStream)
outputStream.close()
workbook.close()
```

### Issue 4: Schedule has empty cells / הבעיה: סידור עם תאים ריקים

**Solution / פתרון:**
- This is expected when there are too many blocks!
- Show error message to user
- Let them edit manually
- זה צפוי כשיש יותר מדי חסימות!
- הצג הודעת שגיאה למשתמש
- תן להם לערוך ידנית

---

## 💡 Code Snippets / קטעי קוד

### Schedule Generator Core Logic / לוגיקת יצירת סידור

```kotlin
fun generateSchedule(
    employees: List<Employee>,
    blocks: Map<String, Boolean>,
    canOnly: Map<String, Boolean>,
    savingMode: Map<String, Boolean>
): Map<String, List<String>> {
    
    val schedule = mutableMapOf<String, MutableList<String>>()
    val employeeShifts = mutableMapOf<String, MutableList<ShiftAssignment>>()
    
    // Initialize
    employees.forEach { employeeShifts[it.name] = mutableListOf() }
    
    // Collect all shifts
    val allShifts = collectAllShifts(savingMode)
    
    // Sort by difficulty (fewer available employees first)
    allShifts.sortBy { shift ->
        getAvailableEmployees(shift, employees, blocks, canOnly, employeeShifts).size
    }
    
    // Assign each shift
    allShifts.forEach { shift ->
        val key = "${shift.day}-${shift.id}"
        
        val available = getAvailableEmployees(shift, employees, blocks, canOnly, employeeShifts)
            .map { emp ->
                val score = calculateScore(emp, shift, employeeShifts[emp.name]!!)
                emp to score
            }
            .sortedBy { it.second }
        
        if (available.isNotEmpty()) {
            val chosen = available.first().first
            schedule[key] = mutableListOf(chosen.name)
            employeeShifts[chosen.name]!!.add(ShiftAssignment(shift.day, shift.id))
        } else {
            schedule[key] = mutableListOf()
        }
    }
    
    return schedule
}

fun getAvailableEmployees(
    shift: Shift,
    employees: List<Employee>,
    blocks: Map<String, Boolean>,
    canOnly: Map<String, Boolean>,
    employeeShifts: Map<String, List<ShiftAssignment>>
): List<Employee> {
    
    return employees.filter { emp ->
        // Check blocks
        val blockKey = "${emp.name}-${shift.day}-${shift.id}"
        if (blocks[blockKey] == true) return@filter false
        
        // Check can only
        val hasCanOnly = canOnly.any { it.key.startsWith("${emp.name}-") && it.value }
        if (hasCanOnly) {
            val canOnlyKey = "${emp.name}-${shift.day}-${shift.id}"
            if (canOnly[canOnlyKey] != true) return@filter false
        }
        
        // Check overlaps and max hours
        val currentShifts = employeeShifts[emp.name] ?: emptyList()
        if (hasConflict(shift, currentShifts)) return@filter false
        
        true
    }
}

fun hasConflict(shift: Shift, currentShifts: List<ShiftAssignment>): Boolean {
    currentShifts.forEach { existing ->
        // Check same day overlaps
        if (existing.day == shift.day) {
            if (overlapsOnSameDay(existing.shiftId, shift.id)) return true
        }
        
        // Check night to morning
        if (existing.shiftId.contains("night") || existing.shiftId.contains("לילה")) {
            val existingDayIndex = getDayIndex(existing.day)
            val shiftDayIndex = getDayIndex(shift.day)
            if (shiftDayIndex == existingDayIndex + 1) {
                if (shift.id.contains("morning") || shift.id.contains("בוקר")) {
                    return true  // FORBIDDEN!
                }
            }
        }
    }
    return false
}

fun calculateScore(
    employee: Employee,
    shift: Shift,
    currentShifts: List<ShiftAssignment>
): Int {
    var score = currentShifts.size * 10
    
    // Add penalty for soft conflicts
    currentShifts.forEach { existing ->
        if (hasSoftConflict(existing, shift)) {
            score += 5
        }
    }
    
    return score
}
```

---

## 📱 App Icon / אייקון אפליקציה

**English:** The app icon should show the calendar logo.

**עברית:** אייקון האפליקציה צריך להציג את הלוגו של הלוח שנה.

**Create these files:**
- `res/mipmap-mdpi/ic_launcher.png` (48x48)
- `res/mipmap-hdpi/ic_launcher.png` (72x72)
- `res/mipmap-xhdpi/ic_launcher.png` (96x96)
- `res/mipmap-xxhdpi/ic_launcher.png` (144x144)
- `res/mipmap-xxxhdpi/ic_launcher.png` (192x192)

**Use the calendar logo in teal color (#2C7873)**

---

## 🎓 Final Notes / הערות סיום

### Code Quality / איכות קוד

**English:**
- Use meaningful variable names
- Add comments in English for complex logic
- Follow Kotlin coding conventions
- Keep functions small and focused

**עברית:**
- השתמש בשמות משתנים ברורים
- הוסף הערות באנגלית ללוגיקה מורכבת
- עקוב אחרי מוסכמות קוד של Kotlin
- שמור פונקציות קטנות וממוקדות

### Performance / ביצועים

**English:**
- Use LazyColumn for long lists
- Memoize expensive calculations
- Don't block the main thread

**עברית:**
- השתמש ב-LazyColumn לרשימות ארוכות
- שמור בזיכרון חישובים יקרים
- אל תחסום את ה-thread הראשי

### User Experience / חוויית משתמש

**English:**
- Show loading indicators
- Provide feedback for actions
- Handle errors gracefully
- Make buttons large and easy to tap

**עברית:**
- הצג אינדיקטורי טעינה
- תן משוב על פעולות
- טפל בשגיאות בצורה חלקה
- עשה כפתורים גדולים וקלים ללחיצה

---

## 🚀 Ready to Build! / מוכן לבנות!

**English:** You now have everything needed to build this app. Follow the phases in order, test thoroughly, and create an amazing work schedule app!

**עברית:** יש לך עכשיו את כל מה שצריך כדי לבנות את האפליקציה הזו. עקוב אחרי השלבים לפי הסדר, בדוק היטב, ותיצור אפליקציית סידור עבודה מדהימה!

**Good luck! בהצלחה! 🎉**

---

**Developed by / פותח על ידי: חננאל סבג (Hananel Sabag)**