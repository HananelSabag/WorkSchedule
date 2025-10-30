# 📅 WorkSchedule - Smart Shift Management System

A modern Android application for intelligent work shift scheduling and management, built with Kotlin and Jetpack Compose.

> **Note:** This application was originally developed for a specific workplace with predefined shift configurations. However, the architecture is designed to be easily adaptable for general use. Future enhancements may include dynamic shift creation and customization.

## ✨ Key Features

### 👥 Employee Management
- Add, edit, and delete employees
- Shabbat observer support (automatic blocking of Shabbat shifts)
- "Mitgaber" status for temporarily unavailable employees
- Intuitive and user-friendly interface

### 🚫 Smart Blocking System
- **Regular blocking (Cannot)** - Mark shifts when employees are unavailable
- **Can-Only blocking** - Mark only the shifts employees are available for
- Automatic blocking for Shabbat observers
- Prevention of mixed blocking types per employee
- Clear warning messages and override options

### 🤖 Automatic Scheduling
- Intelligent algorithm for shift assignment
- Considers all blocks and constraints
- Fair distribution of shifts
- Overload and duplicate detection

### ✍️ Manual Scheduling
- Manual employee assignment to shifts
- Free text support (notes, custom hours)
- Smart blocking with override confirmation
- Auto-save draft system
- Continue from where you left off

### 📊 Preview & Analytics
- Detailed shift table with zoom capabilities
- Direct cell editing
- **Advanced Statistics:**
  - Shift count per employee
  - Smart weekly hours calculation
  - Free text support (custom hours detection)
  - Automatic employee name recognition from text

### 📤 Export & Share
- Export to image (PNG)
- Direct sharing via WhatsApp and other apps
- Full RTL support for Hebrew

### 💾 History & Storage
- Automatic schedule saving
- Complete history of all schedules
- Edit existing schedules
- Delete and override schedules
- **Smart Draft System:**
  - Auto-save when exiting the app
  - Opens in the correct screen (blocking/manual)
  - Auto-delete when schedule is completed

### 🎨 Design & UI/UX
- Modern UI with Material Design 3
- Full RTL support (Hebrew)
- Complete Dark Mode
- Smooth animations
- Intuitive user experience

## 🛠️ Technologies

### Core
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI framework
- **Material Design 3** - UI design system

### Architecture
- **MVVM** - Clean architecture pattern
- **StateFlow** - Reactive state management
- **ViewModel** - Separation of logic from UI
- **Coroutines** - Asynchronous programming

### Database & Storage
- **Room Database** - Local database
- **KSP** - Annotation processing
- **Gson** - JSON serialization

### Additional Libraries
- **Coil** - Image loading
- **Material Icons Extended** - Extended icon set

## 📋 System Requirements

- **Android:** 8.0 (API 26) and above
- **Storage:** ~10MB
- **Permissions:** Storage (for image export)

## 🚀 Installation & Development

### Development Requirements
```
- Android Studio Hedgehog | 2023.1.1 or higher
- JDK 11
- Gradle 8.2
- Kotlin 1.9.0
```

### Local Setup
```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/WorkSchedule.git

# Open in Android Studio
# Build and run on emulator or physical device
```

### Build APK
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## 📱 Screenshots

(Add screenshots of your app here)

## 🎯 Use Cases

### Scenario 1: Quick Schedule
1. Open the app
2. Click "New Schedule"
3. Select basic blocks
4. Click "Create Automatic Schedule"
5. Save and distribute

### Scenario 2: Detailed Schedule
1. Create detailed blocks
2. Move to manual scheduling
3. Manual assignment with free text
4. Check statistics
5. Final edit and share

### Scenario 3: Continue Work
1. Open the app
2. Click "Continue Draft"
3. Continue from where you stopped
4. Complete and save

## 📝 Project Structure

```
app/src/main/java/com/hananel/workschedule/
├── data/                    # Models and data
│   ├── AppDatabase.kt      # Room database
│   ├── Employee.kt         # Employee model
│   ├── Schedule.kt         # Schedule model
│   ├── ScheduleDao.kt      # Data access object
│   └── ShiftDefinitions.kt # Shift definitions
├── ui/                      # User interface
│   ├── components/         # UI components
│   ├── BlockingScreen.kt   # Blocking screen
│   ├── ManualCreationScreen.kt  # Manual scheduling
│   ├── PreviewScreen.kt    # Preview screen
│   ├── HistoryScreen.kt    # History screen
│   └── theme/              # Theme and colors
├── viewmodel/               # Business logic
│   └── ScheduleViewModel.kt
├── utils/                   # Utilities
│   └── ScheduleGenerator.kt # Scheduling algorithm
└── MainActivity.kt          # Main activity
```

## 🔄 Versions

### Version 1.1 (Current)
- ✅ Smart hours calculation with free text support
- ✅ Enhanced draft system
- ✅ Improved "Can-Only" logic
- ✅ Block persistence with schedules
- ✅ UI/UX fixes

### Version 1.0
- 🎉 Initial release
- Automatic and manual scheduling
- Employee and block management
- History and export features

## 🔮 Future Enhancements

### Planned Features
- **Dynamic Shift Creation** - User-defined shift types and hours
- **Shift Template System** - Save and reuse shift configurations
- **Multi-workplace Support** - Manage different locations
- **Cloud Sync** - Backup and sync across devices
- **Advanced Analytics** - More detailed reports and insights
- **Custom Themes** - User-defined color schemes

Currently, the shift definitions are hardcoded for a specific workplace, but the architecture supports easy customization.

## 🤝 Contributing

Found a bug? Have an idea for improvement?
Feel free to open an Issue or submit a Pull Request!

### How to Contribute
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is open source and available under the MIT License.

## 👨‍💻 Author

**Hananel Sabag**

Originally developed for internal workplace use, but designed with scalability and adaptability in mind.

## 🙏 Acknowledgments

- Built with modern Android development best practices
- Inspired by real-world scheduling challenges
- Designed for efficiency and ease of use

---

**Built with ❤️ in Israel 🇮🇱**

*Made with Kotlin & Jetpack Compose*
