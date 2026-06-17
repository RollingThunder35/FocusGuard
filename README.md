# FocusGuard

FocusGuard is an Android focus and standby-clock app made for Samsung phones.

## Main features

- Start a 30 minute focus timer or custom timer.
- Set a daily focus schedule.
- Pick distracting apps to block during focus mode.
- Ongoing focus notification for the status bar and lock screen.
- Samsung Now Bar-compatible notification where One UI allows it.
- Standby clock with visual clock style cards and exact colour wheel.
- Separate portrait and landscape standby layouts.
- Auto standby when the charger connects.
- Optional auto standby after the phone is idle.
- Tap the standby screen to exit auto standby.
- Battery percentage on the standby clock.
- Weather on the standby clock using a saved city.
- Currently playing music on the standby clock using notification access.

## Setup on your phone

1. Open the project in Android Studio.
2. Run the app on your Samsung phone.
3. Allow notifications when asked.
4. Open **Accessibility Settings** from the app and enable **FocusGuard App Blocker**.
5. Choose the apps you want to block.
6. For music on the standby clock, open **Enable music notification access** and allow **FocusGuard Music Display**.
7. For weather, enter your city in **Standby extras** and press **Save and refresh weather**.

## Important limits

Now Bar is controlled by Samsung/One UI. FocusGuard sends an ongoing/live-style notification, but Samsung decides whether it appears in Now Bar. If FocusGuard is not listed under Now Bar / Live notifications settings, the app cannot force itself there.

Automatic standby can be blocked by some Android/Samsung background-launch rules. Manual standby always works from inside the app.

Music display uses Android notification access. It reads media notification title/artist only. It does not read messages, passwords or screen content.

Weather uses Open-Meteo, so no API key is needed. The app needs internet permission for weather updates.

## Galaxy Store note

Because this app uses Accessibility and Notification Listener features, the Galaxy Store listing/privacy policy should clearly explain:

- Accessibility is used only to detect opened apps so FocusGuard can block selected distracting apps during active focus sessions.
- Notification access is optional and used only to display currently playing music metadata on the standby clock.
- Weather uses the city typed by the user.
- The app does not collect messages, passwords, browsing content or screen text.

## v7 Standby Clock Redesign

This version redesigns the standby clock to match the newer concept:
- Large gradient clock
- Better portrait layout
- Better landscape layout
- Weather, battery and music shown in aesthetic rounded cards
- Minimized floating menu by default
- Expandable menu for Style, Colour, Dim, Refresh and Close
- Visual style picker
- Colour wheel picker
- Auto standby still exits by tapping the screen

## v8 Music Controls + Layout Fixes

This version adds:
- Previous, play/pause and next controls on the standby music card
- Uses Android media sessions, so Notification Access must be enabled
- Long song names and artist names now scroll/marquee instead of being cut off
- Landscape standby layout is more compact so the clock, weather, battery and music fit better
- Portrait clock menu is smaller so the full style menu can be seen

## v9 Standby Improvements

This version adds:
- Immersive standby mode to hide/minimise Samsung navigation buttons
- Landscape clock menu now uses a horizontal scroll row so all options can be reached
- Music buttons now use cleaner matching circular controls
- Pause now uses the same button style as play
- Music previous/play-pause/next controls are still powered by Android media sessions

## v10 Standby Clock Update

This version adds:
- Landscape minimised menu now fully hides instead of leaving the bar on screen
- Tap the clock in landscape to bring the menu back
- Clock and layout resize in landscape when the menu is shown/hidden
- 10 visual clock styles instead of 6
- Separate Seconds on/off toggle that works with every style
- Style picker uses horizontal scrolling so more styles fit neatly

## v11 Layout + Simple Home Update

This version adds:
- Smaller clock sizing so date and seconds are visible across more styles
- Reduced spacing around date, seconds and status text
- Landscape standby hides extra hint/status text when the menu is hidden
- Main app home screen is simpler and less crowded
- Home screen now focuses on Start focus, Main actions, Schedule, Standby and Setup
