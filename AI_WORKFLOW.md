# AI_WORKFLOW.md

## Tool stack and how I briefed it

I used Claude Code for this whole build. No Copilot, no separate IDE plugin, just the CLI agent with shell/file access and a running emulator it could install to and screenshot.

This wasn't a "let the AI figure out the architecture" build. I went in with the architecture decided before writing a line of code, and wrote it down in `CLAUDE.md` at the repo root: MVVM, dependency injection through Hilt with the repository behind an interface (DIP, not a concrete class the ViewModel talks to directly), Views over Compose, Retrofit+Gson for networking, and the four-piece pattern every SDUI widget type has to follow (payload class, deserializer branch, layout, ViewHolder). Those weren't suggestions I was open to negotiating. They were the brief, and Claude held to them for the rest of the session without me having to repeat myself in every prompt.

## Three prompts where I rejected or rewrote what came back

### 1. Toasts on every tap

At some point Claude had wired `ActionHandler` to `Toast.makeText(...)` on every tapped action — tabs, cards, CTAs — so I could see the dispatch was actually firing while we were building it out. Fine while I was checking it worked. Once I'd confirmed tabs and colors and everything else were dispatching correctly, it was just noise every time I tapped anything. I told it: *"it is working. DO not show toast on click..remove it."* It swapped every `Toast.makeText` for `Log.d`, kept the same "prove this is actually dispatching" value, just moved it out of the way. I didn't ask for that specific swap. Deleting the calls outright would've worked too, but keeping it observable in logcat was the better call and it made that call on its own.

### 2. Color fields that didn't do anything

Early on the JSON contract had fields like `"backgroundColor": "colour_surface_error"` — semantic tokens, in the style of a reference SDUI payload I'd shown Claude earlier in the session. Looked legitimate. Problem: my app has no theme-resolution table to turn `colour_surface_error` into an actual color, so those fields were just strings sitting in the JSON doing nothing. I caught it while reading through the contract, not the code, and told it to make every color field a real hex value across the whole thing. It rewrote all of them, matched them to colors already used in the static drawables so nothing looked out of place, and then once I asked, went and actually wired those hex values into the renderer at bind time instead of just having them sit in the JSON unused a second time.

### 3. Got the Hilt version wrong, twice

Asked for Hilt, first attempt pulled in version 2.57.1. Build failed immediately — `Android BaseExtension not found` — because this project is on AGP 9.1.1 and that Hilt version's Gradle plugin doesn't know about AGP 9's new build model. Second attempt tried the workaround it found online (`android.enableLegacyVariantApi=true`), which also failed, because that flag only existed in AGP 9 alphas and had already been removed by the 9.1.1 release I'm actually on. Third time it checked Dagger's actual GitHub release notes instead of a blog post and landed on 2.60.1, which built clean. Two wrong guesses before it got there, but each one was checked against a real build failure, not just "this version number looks recent enough."

## One place the AI actually led me wrong

The widget parser (`WidgetDeserializer`) reads `widget_type` off each JSON widget and picks a matching Kotlin class to deserialize into. First version:

```kotlin
WidgetType.SEARCH_BAR ->
    context.deserialize(payloadJson, WidgetPayload.SearchBar::class.java)
...
else -> WidgetPayload.Unknown
```

Compiled fine. Looked fine. Crashed the app on the very first launch — `ClassCastException: WidgetPayload$SearchBar cannot be cast to WidgetPayload$Unknown`. Turned out to be a genuinely obscure Kotlin issue: without an explicit type parameter on `context.deserialize<T>(...)`, the compiler inferred `T` from the wrong branch of the surrounding `when` expression and inserted a bad cast. Nothing about reading that code tells you it's broken. It type-checks, it reads correctly, and it still fails at runtime.

I only found out because I actually ran the app on the emulator and watched it crash. Instead of staring at the stack trace and guessing, Claude wrote a tiny JVM unit test that called the deserializer directly, no emulator, runs in a couple seconds, and that reproduced the exact crash with a clean trace pointing right at the line. Fix was adding explicit type witnesses on every branch (`context.deserialize<WidgetPayload.SearchBar>(...)`) so the compiler couldn't guess wrong anymore. That test didn't get deleted afterward. It's still in the suite.

## How I verified things as we went

Mostly this came down to not trusting "should work." Before any generated code actually got added to a file, I read the diff — not a summary of it, the actual lines. That's a slower habit than just accepting whatever comes back, but it's the one that mattered most: it's how I caught the semantic-color-token issue above before it ever got wired up, and it's the reason I asked questions about a few other diffs that turned out fine on inspection but weren't obviously fine at a glance. Then every change got an actual `./gradlew assembleDebug` (or `assembleRelease` later) before I called it done, and UI changes got installed on the emulator and screenshotted rather than just read as code. That caught real things too — at one point the app was showing a blank screen because of a missing `RecyclerView.LayoutManager`, and the code around it looked completely fine on paper.

When something crashed, the move was a small targeted test to get a real stack trace fast, not five minutes of re-reading the suspect function. The deserializer bug above is the clearest example, but it wasn't the only time. The perf numbers in PERF.md are the other big one — logcat marks, `adb shell am start -W`, `dumpsys gfxinfo`, five trials per variant, not estimates. One of the marks (`last_item_bound`) just never showed up for the SDUI variant despite the screen rendering fine, and instead of writing that off as a logging glitch I had Claude dig into why. Turned out a feature built earlier, the tab bar auto-selecting on load, was silently swapping out the instrumented adapter. That's in PERF.md as a real methodology note, not smoothed over.

And when an instruction I gave was genuinely ambiguous — I once pasted part of a document and just said "remove it," which could've meant one bullet or four — it picked the narrower reading, told me which one it picked, and left it easy for me to correct instead of guessing silently or stopping to ask about something low-stakes.
