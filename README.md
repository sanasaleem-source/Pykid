# PyKid: Kid-Friendly Python Learning Sandbox 🎓🐍

Welcome to **PyKid**, a beautiful, fully interactive, and completely offline-safe Android application designed to introduce children and beginners to the magic of Python programming!

PyKid functions as a high-fidelity learning companion and interpreter. It allows young learners to run core Python code, interact with standard logic (variables, conditional branching, functions, and loops), utilize simulated modules, and visually control an interactive **Turtle Graphics Canvas** directly on their phones.

---

## 🌟 Key Features

### 1. Interactive Learning Academy 🎓
*   **5 Core Coding Modules**: Progressively teaches Python essentials:
    *   📦 **Magic Box Secrets**: Variables and terminal/speech inputs.
    *   🔮 **Decisions & Branches**: Interactive `if`/`else` control flow.
    *   🐢 **Loops & Painting**: Repeating instructions dynamically using Turtle graphics.
    *   🪄 **Custom Commands**: Defining and reusing code snippets using `def` functions.
    *   🎮 **Intro to Pygame**: Understanding real game event loops.
*   **3 Kid-Friendly Creative Starter Projects**:
    *   🧮 **Magic Voice Calculator**: Combines math variables with text-to-speech output.
    *   🎮 **Pygame Guess the Number**: An interactive number guessing game.
    *   🎨 **Spiral Turtle Art**: Dynamic, colorful neon geometry drawing.

### 2. High-Fidelity Custom Python Simulator ⚡
*   **Turtle Graphics Engine**: Standard `turtle` commands (`forward`, `backward`, `right`, `left`, `width`, `color`) draw directly to an animated visual canvas.
*   **Phone Integration Module**: Custom `phone` functions allow Python code to physically interact with mobile hardware offline:
    *   `phone.speak("message")`: Real-time offline text-to-speech feedback.
    *   `phone.vibrate(duration)`: Tactile and haptic vibration feedback.
    *   `phone.play_sound("sound_type")`: Interactive sound effects (`beep`, `success`, `error`, `chime`, `laser`, `alarm`).
*   **Pre-Installed Library Support**: Built-in support for simulating offline loads of Kivy and Pygame workflows.

### 3. File Explorer & Persistence 📂
*   Keep files clean and safe! Save, load, modify, and delete custom Python programs with full offline persistence powered by a local SQLite Room Database.

---

## 📱 Mobile Setup Guide: Pydroid 3

For learners ready to transition to full-fledged mobile Python development on an actual device, PyKid recommends setting up **Pydroid 3** on their phone:

### 1. Download & Prepare the Sandbox
*   Install **Pydroid 3 - IDE for Python 3** directly from the Google Play Store.
*   In internal storage, create a safe local directory called `/PyKidProjects` to isolate, store, and keep scripts organized.

### 2. Pre-Install Essential Libraries (No PC Required!)
*   **Turtle (Graphics)**: Built-in default. Ready to go using `import turtle`.
*   **Pygame (Arcade Games)**:
    *   Navigate to *Pydroid Menu* ➔ *PIP* ➔ *QuickInstall* ➔ Select **Pygame** to install.
*   **Kivy (GUI Interfaces)**:
    *   Navigate to *Pydroid Menu* ➔ *PIP* ➔ *PIP tab* ➔ Check *"Use prebuilt libraries"* ➔ Type `kivy` ➔ Tap **Install**.

### 3. Safety First: 100% Offline Mode 🔌
*   Pydroid 3 works completely offline! Put the phone into **Airplane Mode** for an absolute child-safe, distraction-free, sandboxed coding workspace with secure local data storage.

---

## 🏗️ Architecture & Technical Stack

This application is written following modern Android and Material Design 3 guidelines:
*   **Language**: Kotlin (100% Type-Safe)
*   **UI Framework**: Jetpack Compose (Declarative UI)
*   **Database**: Room Database (Local Persistence)
*   **Styling**: Material 3 Design System with high-contrast color schemes, custom animations, and rounded layouts.
*   **State Management**: Kotlin Coroutines & `StateFlow` conforming to MVVM architectural design.

---

## 🚀 How to Run locally on Android Studio

1.  Clone this repository:
    ```bash
    git clone https://github.com/your-username/pykid-android.git
    ```
2.  Open the project directory in **Android Studio (Ladybug or newer)**.
3.  Let Gradle sync dependencies.
4.  Run the application on a connected Android Device or Emulator.

---

*Made with love for future programmers! Happy coding! 🎓🐍*
