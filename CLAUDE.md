# CLAUDE.md

Ground rules for this project. Stick to them — if a task seems to call for breaking one, ask me first instead of just picking your own approach.

## Architecture

MVVM. View (Activity/Adapter) talks to ViewModel, ViewModel talks to Repository, Repository talks to the data source — don't skip a layer.

Repository is an interface (`HomeRepository`) with the real network implementation (`HomeRepositoryImpl`) bound to it through Hilt. The ViewModel only ever sees the interface — don't have it reference the impl directly, that's the whole point of doing this.

DI is Hilt, constructor injection everywhere. No manual singletons, no service locator. `@Module` + `@InstallIn(SingletonComponent::class)` for network/repo bindings, `@HiltViewModel` on ViewModels, `@AndroidEntryPoint` on Activities.

Views, not Compose. XML layouts, `RecyclerView`, `ViewBinding`. Don't reach for Compose just because it'd be quicker for one screen — the rest of the app isn't on it.

## SDUI component registry

Every widget type is the same four pieces. Adding a new one means all four, not a shortcut version:
1. A `WidgetPayload` subtype matching the JSON shape.
2. A branch in `WidgetDeserializer` mapping `widget_type` to it.
3. An XML layout.
4. A `ViewHolder` (plus a small adapter if it has its own inner list) wired into `HomeAdapter`.

Anything the parser doesn't recognize — an unknown `widget_type`, or `status != SUCCESS` — becomes `WidgetPayload.Unknown` and gets filtered out before rendering. The page should never crash because the server sent something the client doesn't know yet; that's the entire point of the fallback.

## Networking / data

Retrofit + Gson, OkHttp underneath. Don't bring in Moshi or kotlinx.serialization on top of that — one stack.

Glide for images.

The JSON contract is the source of truth. If something's wrong on screen, fix the contract or the renderer — don't hardcode a patch over it, even a small one.

## Working style

Build before calling anything done — `./gradlew assembleDebug` at minimum, not just reading the diff back.

UI changes get run on the emulator and screenshotted. Code that looks right isn't the same as a screen that renders right.

If something breaks, get a fast repro — a JVM unit test if you can — rather than staring at the suspect function trying to spot it.
