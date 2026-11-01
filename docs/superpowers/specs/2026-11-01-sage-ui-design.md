# Approved sage UI direction
User approved the three-screen browser mockup and requested main push and Drive APK delivery.
Use charcoal #141617, sage #D7E2C6, restrained text, a vehicle-led home, editorial community feed and service-led care discovery. Keep native Java views and existing authentication/server behavior. Mockup fixture posts, reaction counts and providers must not become production data. Use honest empty states. Existing vehicle asset remains credited. Preserve five tabs and accessible 48dp controls.
Approaches: apply the approved native layout (chosen); WebView wrapper rejected because native flows already work; new UI framework unnecessary for this visual release.

User added foldable support before publishing: constrain the native content column to 640dp using actual measured window width, center on expanded screens, allow resizing/multi-window, retain current tab through configuration recreation. No Fold8-specific dimensions are assumed; hardware hinge/Flex Mode and actual device testing remain outside current verification.

Size/orientation configuration changes are handled by the existing live view hierarchy (manifest configChanges) so open compose dialogs and cloud detail survive window changes while the process is alive. Process-death draft restoration is not included. ResponsiveFrame measures every layout pass, independent of stale screenWidthDp values.
