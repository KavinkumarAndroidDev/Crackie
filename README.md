# Fortune 🥠

**Your daily dose of wisdom, wrapped in a delightful digital cookie.**

Fortune is a simple, beautiful Android application that delivers a new fortune every day. Built with modern Android technologies, it focuses on a polished and emotionally satisfying user experience, from the tactile cracking of the cookie to the magical reveal of a rare "Mystic" fortune.

---

## ✨ Features

*   **Daily Fortunes**: A new, unique fortune cookie is delivered every 24 hours.
*   **Tactile Cracking Experience**: Crack open your cookie with a series of taps, complete with satisfying sounds, haptic feedback, and a delightful explosion animation.
*   **Fortune Rarity System**: Fortunes come in three rarities: **Common**, **Golden**, and the elusive **Mystic**. Each rarity has a unique, animated background to make the reveal feel special.
*   **Save Your Favorites**: Found a piece of wisdom that resonates with you? Save it to your personal collection with a single tap.
*   **Advanced Sorting for Favorites**: Your saved fortunes can be sorted by the date they were added or by their rarity, making it easy to find the messages that matter most.
*   **Engaging Cooldown Screen**: The 24-hour waiting period is now a fun and educational experience. A beautifully styled card displays random fun facts and origin stories about fortune cookies.
*   **Share with Friends**: Easily share your daily fortune with friends through any messaging or social media app.
*   **Daily Notifications**: Receive a gentle reminder when your next fortune cookie is ready to be opened.
*   **Endless Content**: The app ensures you never get a repeat fortune until you've seen them all. Once the list is exhausted, it resets for a fresh cycle.
*   **Polished Onboarding**: A beautiful and informative welcome modal for first-time users that explains the core mechanics of the app.

---

## 🛠️ Tech Stack & Architecture

This project showcases a modern Android development setup, built entirely with Kotlin and Jetpack Compose.

*   **UI**: 100% [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/) for a clean, modern design.
*   **Architecture**: Follows a reactive **MVVM (Model-View-ViewModel)** architecture to ensure a clean separation of concerns.
*   **Asynchronous Programming**: Uses **Kotlin Coroutines** and **Flow** to handle background tasks and manage UI state.
*   **Database**: Persists fortunes and user data locally using the **Room** persistence library.
*   **Background Tasks**: Leverages **WorkManager** to reliably schedule the 24-hour cooldown notification.
*   **Navigation**: Uses the **Jetpack Navigation** library for Compose to move between the home screen and the favorites screen.

---

## 🚀 How to Build

1.  Clone the repository:
    
    ```sh
    git clone https://github.com/KavinkumarAndroidDev/Crackie.git
    ```

2.  Open the project in the latest stable version of Android Studio.
3.  Let Gradle sync and download the required dependencies.
4.  Build and run on an emulator or a physical device.

---

## 🔮 Future Enhancements

Fortune is a complete experience, but there's always room for more magic. Here are some ideas for future versions:

*   **Daily Streak Counter**: Reward users for cracking cookies multiple days in a row.
*   **More Animations**: Add subtle animations to the cooldown screen (like a sleeping "Zzz" effect) or the favorites list.
*   **Cloud Backup**: Use a service like Firebase to back up a user's favorite fortunes so they are never lost.
*   **Customization**: Allow users to change the app's theme, sounds, or even the design of the cookie itself.

---

## 📄 License

This project is licensed under the MIT License. See the `LICENSE` file for details.
