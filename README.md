# 📅 WorkSchedule - Smart Shift Management System

A modern, fully-featured Android application for intelligent work shift scheduling and management, built with Kotlin and Jetpack Compose.

**✨ Now with Dynamic Shift Templates - Create custom shifts tailored to your workplace!**

> **Free to Use!** This app is open-source and ready for anyone to use. Whether you manage a restaurant, retail store, hospital, or any business with shift workers - this tool is here to help you! ❤️

## 🎯 About This Project

WorkSchedule was developed as a complete solution for shift management challenges. The app provides intuitive manual scheduling with smart blocking and customizable templates, offering the flexibility needed for real-world scenarios.

**Looking for opportunities:** I'm Hananel Sabag, a Software Engineering graduate actively seeking development positions. This project demonstrates my skills in Android development, clean architecture, and modern UI/UX design. Feel free to reach out!

## ✨ Key Features

### 🔧 Dynamic Shift Template System (NEW!)
- **Fully Customizable Shifts** - Define your own shift names, hours, and days
- **Flexible Table Configuration** - Choose 2-8 shifts and 4-7 working days
- **Easy Template Management** - Edit and update your shift template anytime
- **Automatic Migration** - Seamlessly upgrades from previous versions

### 👥 Employee Management
- Add, edit, and delete employees
- Shabbat observer support (automatic blocking of Shabbat shifts)
- "Mitgaber" status for temporarily unavailable employees
- Clean and intuitive interface

### 🚫 Smart Blocking System
- **Regular blocking (Cannot)** - Mark shifts when employees are unavailable
- **Can-Only blocking** - Mark only the shifts employees are available for
- Automatic blocking for Shabbat observers
- Prevention of mixed blocking types per employee
- Clear warning messages and override options

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
- Export to image (PNG) with your custom shift template
- Export to CSV/Excel format
- Direct sharing via WhatsApp and other apps
- Beautiful formatted text output
- Full RTL support for Hebrew

### 💾 History & Storage
- Automatic schedule saving
- Complete history of all schedules
- Rename, edit, and manage existing schedules
- Delete and override schedules
- **Smart Draft System:**
  - Auto-save when exiting the app
  - Opens in the correct screen (blocking/manual)
  - Auto-delete when schedule is completed

### 🎨 Design & UI/UX
- Modern UI with Material Design 3
- Beautiful redesigned History screen with modern card design
- Centered empty states with elegant visuals
- Full RTL support (Hebrew)
- Complete Dark Mode support
- Smooth animations and transitions
- Intuitive user experience

## 🛠️ Technologies

### Core
- **Kotlin** - Modern, concise programming language
- **Jetpack Compose** - Declarative UI framework
- **Material Design 3** - Latest design system

### Architecture
- **MVVM** - Clean architecture pattern
- **StateFlow** - Reactive state management
- **ViewModel** - Separation of logic from UI
- **Coroutines** - Asynchronous programming

### Database & Storage
- **Room Database** - Local database with migrations
- **KSP** - Annotation processing
- **Gson** - JSON serialization
- **Multi-table relations** - Complex data management

### Additional Libraries
- **Coil** - Efficient image loading
- **Material Icons Extended** - Comprehensive icon set

## 📋 System Requirements

- **Android:** 8.0 (API 26) and above
- **Storage:** ~10MB
- **Permissions:** Storage (for image/file export)

## 🚀 Installation

### For End Users

**Download & Install APK:**
1. Download: `סידור עבודהV2.apk` from the repository root
2. Transfer the file to your Android device
3. On your device, enable "Install from unknown sources" in Settings (if prompted)
4. Tap the APK file to install
5. Grant necessary permissions (Storage for exports)
6. Start scheduling!

> **Note:** The APK is ready to use and includes all features of Version 2.0

### For Developers

**Development Requirements:**
```
- Android Studio Hedgehog | 2023.1.1 or higher
- JDK 11
- Gradle 8.2
- Kotlin 1.9.0
```

**Local Setup:**
```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/WorkSchedule.git

# Open in Android Studio
# Build and run on emulator or physical device
```

**Build APK:**
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## 📝 Project Structure

```
app/src/main/java/com/hananel/workschedule/
├── data/                      # Data layer
│   ├── AppDatabase.kt        # Room database with migrations
│   ├── Employee.kt           # Employee entity
│   ├── Schedule.kt           # Schedule entity
│   ├── ShiftTemplate.kt      # Dynamic shift template
│   ├── ShiftRow.kt           # Shift row definition
│   ├── DayColumn.kt          # Day column definition
│   ├── DynamicShiftManager.kt # Template manager
│   └── *Dao.kt               # Data access objects
├── ui/                        # Presentation layer
│   ├── components/           # Reusable UI components
│   │   ├── SimpleTable.kt    # Dynamic table component
│   │   └── ShiftRowEditDialog.kt # Shift editor
│   ├── BlockingScreen.kt     # Shift blocking interface
│   ├── ManualCreationScreen.kt # Manual scheduling
│   ├── PreviewScreen.kt      # Schedule preview & stats
│   ├── HistoryScreen.kt      # Beautiful schedule history
│   ├── EmployeeManagementScreen.kt # Employee CRUD
│   ├── ShiftTemplateSetupScreen.kt # Template configuration
│   ├── SplashScreen.kt       # App intro screen
│   ├── HomeScreen.kt         # Main navigation hub
│   └── theme/                # Theme & styling
├── viewmodel/                 # Business logic layer
│   └── ScheduleViewModel.kt  # Main ViewModel
├── utils/                     # Utilities
│   ├── WhatsAppSharer.kt     # Text export
│   ├── ExcelExporter.kt      # CSV export
│   └── ImageSharer.kt        # Image export
└── MainActivity.kt            # App entry point
```

## 🎯 How to Use

### First Time Setup
1. **Open the app** - Welcome splash screen
2. **Add employees** - Navigate to Employee Management
3. **Configure shift template** - Set up your custom shifts and days
4. **Ready to schedule!**

### Creating a Schedule
1. **Click "New Schedule"** 
2. **Block unavailable shifts** - Mark when employees can't work
3. **Assign shifts manually** - Drag employees to shifts or type freely
4. **Review statistics** - Check fairness and balance
5. **Export and share** - Send via WhatsApp or save as image

### Managing Templates
1. **Click "Edit Table Structure"** from home screen
2. **Add shifts** - Create up to 8 custom shifts with structured time input
3. **Reorder shifts** - Drag & drop with hamburger icon (☰) or long press
4. **Edit shift details** - Names and working hours
5. **Enable/disable days** - Choose 4-7 working days (compact 2-column layout)
6. **Auto-save** - Changes save automatically, exit when done

### Continuing Draft Work
1. **App remembers your progress** - Auto-saved when closing
2. **Click "Continue Draft"** - Picks up exactly where you left off
3. **Complete and save** - Draft auto-deletes when done

## 🔄 Version History

### Version 2.0 (Current) 🚀
- ✨ **NEW: Dynamic Shift Template System** - Fully customizable shifts (2-8 shifts, 4-7 days)
- ✨ **NEW: Drag & Drop Reordering** - Intuitive shift organization with hamburger menu or long press
- ✨ **NEW: Structured Time Input** - Easy hour/minute fields for shift times
- ✨ **NEW: Auto-Save System** - Smart saving without leaving the editing screen
- ✨ **NEW: Compact Day Selection** - Space-efficient 2-column grid layout
- ✨ **NEW: Beautiful History UI** - Modern card design with elegant empty states
- ✨ **NEW: Template Management Screen** - Configure shifts anytime with auto-save
- ✨ **NEW: Automatic Migration** - Seamless upgrade for existing users
- ✨ **NEW: Generic Employee System** - No hardcoded employee data
- ✨ **NEW: First-Time Setup Flow** - Guided template creation
- ✅ Complete application generalization - works for any workplace
- ✅ Perfect Dark Mode support across all screens
- ✅ Real-time statistics with dynamic template support
- ✅ CSV/Excel export with dynamic templates
- ✅ Schedule renaming capability
- ✅ Image export with custom shift configurations
- ✅ Smart draft management - auto-clear when schedule saved
- ✅ Shadow-free UI design throughout
- ✅ UI/UX improvements throughout

### Version 1.1
- ✅ Smart hours calculation with free text support
- ✅ Enhanced draft system with better persistence
- ✅ Improved "Can-Only" blocking logic
- ✅ Block persistence with schedules
- ✅ UI/UX fixes

### Version 1.0
- 🎉 Initial release
- Manual scheduling with smart blocking
- Employee and block management
- History and export features
- Basic shift definitions

## 🔮 Future Enhancements

### Planned Features
- **Cloud Sync** - Backup and sync across devices
- **Multi-workplace Support** - Manage different locations/teams
- **Advanced Analytics** - Detailed reports and insights
- **Notifications** - Shift reminders for employees
- **Custom Themes** - User-defined color schemes
- **Team Collaboration** - Shared scheduling with multiple managers
- **Export to Google Calendar** - Automatic calendar integration

## 🤝 Contributing

Found a bug? Have an idea for improvement? Contributions are welcome!

### How to Contribute
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Reporting Issues
- Use the GitHub Issues tab
- Provide detailed description and steps to reproduce
- Include Android version and device model if relevant

## 📄 License

This project is open source and available under the MIT License.
Feel free to use, modify, and distribute - just keep the attribution! ❤️

## 👨‍💻 Developer

**Hananel Sabag**  
Software Engineering Graduate

Passionate about creating elegant, user-friendly solutions to real-world problems. This project showcases my skills in:
- Modern Android development (Kotlin, Jetpack Compose)
- Clean Architecture & MVVM pattern
- Database design and migrations
- Complex algorithm implementation
- UI/UX design with Material Design 3
- State management with Coroutines & Flow

**Currently seeking software development opportunities!**  
If you're looking for a dedicated developer with strong technical skills and attention to detail, let's connect!

## 🙏 Acknowledgments

- Built with modern Android development best practices
- Inspired by real-world workplace scheduling challenges
- Designed for simplicity, efficiency, and ease of use
- Community feedback and suggestions welcome

## 📞 Contact & Support

For questions, suggestions, or collaboration opportunities:
- Open an issue on GitHub
- Check out my other projects
- Feel free to reach out!

---

**Built with ❤️ in Israel 🇮🇱**

*Made with Kotlin & Jetpack Compose*

**Free for everyone to use - Happy Scheduling! 📅✨**
