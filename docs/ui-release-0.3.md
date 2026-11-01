# Android 0.3.0 — sage UI and resizable windows

Approved browser direction applied to native home, shared-community feed and care discovery. Charcoal surfaces, sage actions, compact typography, vehicle-led home and horizontally scrollable service controls. Uses actual server content; does not publish mockup posts/providers or imaginary counts.

Window content is centered and capped at 640dp, remeasured against actual available width. Resize/multi-window enabled. Size, orientation and screen-layout configuration changes preserve the live view hierarchy. Process-death draft recovery and hinge/Flex Mode integration are not included.

Validation (2026-11-01):
- assembleDebug, assembleDebugAndroidTest, lintDebug: success; 0 lint errors, 6 existing warnings.
- Instrumentation: 7 tests passed (2 responsive, 1 server connection, 3 session safety, 1 cloud navigation).
- Responsive measurement: 320/420/840/1100dp; content contained and centered.
- Actual emulator size transition: 2208x1840 and 1080x2400, same Activity and selected tab, visible generic input dialog and retained text. Original emulator size restored by test.
- Home/care screenshots reviewed, expanded display screenshot reviewed.
- Independent review completed, fixed button clipping and fold-recreation issue.

Limits: no Galaxy Fold8 hardware or physical hinge test. Generic dialog continuity test is not an authenticated cloud-composer end-to-end test. Kakao provider remains unconfigured as before. Native UI adapts approved design to existing features; photo attachments/category tabs in the illustrative mockup are not added to the backend.
