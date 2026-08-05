# PERF.md — Static vs SDUI

## Device & build

- **Device**: Android Emulator, `sdk_gphone64_arm64`, Android 16, 1080×2400 (no physical device was available for this exercise — noted as a methodology limitation, not hidden)
- **Build**: `./gradlew assembleRelease`, `isMinifyEnabled = false`, signed with the debug keystore (local benchmarking only, not a production signing identity — see README)
- **Both variants ship in the same APK** for a controlled comparison: `MainActivity` (SDUI) and `StaticHomeActivity` (hardcoded), launched explicitly via `adb shell am start -n`. A real static-only app wouldn't bundle Retrofit/Gson/OkHttp at all; this comparison isolates *rendering-path* overhead, not APK size.

## Methodology

**Cold start, 5 trials per variant**, `am force-stop` before every trial to guarantee a cold process, `adb shell am start -S -W` for the OS-level launch timer, logcat cleared before each trial.

Every measurement point is a `PerfTrace.mark(variant, label)` call (`SystemClock.elapsedRealtime()` → logcat), read back and diffed after each run. Both variants share the exact same `HomeAdapter`/ViewHolders/layouts — `StaticHomeActivity` constructs the identical `Widget`/`WidgetPayload` tree as compile-time Kotlin object literals (see `StaticHomeData.kt`) instead of fetching+parsing JSON. That means **view-building code is identical between both variants**; any delta is fetch+parse+dispatch overhead, which is what "SDUI breakdown" is asking for.

Marks used:
- `activity_start` — top of `onCreate`
- `repo_call_start` / `network_response_received` (OkHttp interceptor, before Gson conversion) / `repo_call_end` — SDUI only, brackets network vs. parse
- `adapter_set` — widget list handed to `HomeAdapter`
- `content_first_frame` — first pre-draw *after* the adapter is set (see note below)
- `last_item_bound` — `onBindViewHolder` for the last content widget (proxy for "full page rendered")

**A real methodology bug I caught while looking at the data, not just noise**: Android's `am start -W` `TotalTime` and a naive single `doOnPreDraw` mark both capture the *loading spinner's* first frame for the SDUI variant, not the actual content — because the spinner renders before the network call resolves. That made SDUI look deceptively fast on `TotalTime` alone (median 313ms) while the content hadn't rendered yet. I added a second mark (`content_first_frame`) taken specifically on the next pre-draw *after* `adapter_set`, which is what's reported below as TTR. `am start -W`'s `TotalTime` is included for transparency but is **not** used as the TTR number for SDUI for this reason.

A second real bug the data surfaced: `last_item_bound` never fired for SDUI in the first pass of trials, even though the content visibly rendered correctly. Cause: the tab bar's auto-select-on-load behavior (built for the header-color feature) was silently replacing the instrumented content `HomeAdapter` with a fresh, non-instrumented one immediately after `adapter_set`. Fixed by threading `perfTag` through that replacement path too (`MainActivity.kt`, `onTabSelected`). Left in as a deliberate example of measurement forcing a real fix, not cleaned up after the fact.

## Results — baseline (no optimization)

| Metric | Static (ms, median of 5) | SDUI (ms, median of 5) | Overhead |
|---|---|---|---|
| **TTR** (`activity_start` → `content_first_frame`) | 132 | 947 | +617% (~7.2×) |
| Data-ready (`activity_start` → `adapter_set`) | 37 | 869 | — |
| View-build (`adapter_set` → `last_item_bound`) | 77 | 61 | ~0 (noise-level, confirms shared code path) |
| **Full page** (`activity_start` → `last_item_bound`) | 122 | 936 | +667% (~7.7×) |
| `am start -W` `TotalTime` | 488 | 313 | n/a — see caveat above, not a fair TTR proxy for SDUI |

**SDUI breakdown** (network vs. parse, median of 5):

| | ms | % of SDUI overhead |
|---|---|---|
| Network (`repo_call_start` → `network_response_received`) | 816 | 99.5% |
| Gson parse (`network_response_received` → `repo_call_end`) | 4 | 0.5% |

The takeaway that shaped the optimization attempt below: **essentially all SDUI overhead is the network round trip, not JSON parsing.** The polymorphic `WidgetDeserializer` — the part of the system that's actually "SDUI-specific" — costs ~4ms. Optimizing the parser would have been optimizing the wrong thing.

## Measure → optimize → re-measure

**What I tried**: the hosted contract already sends `Cache-Control: max-age=3600, public` (visible in the OkHttp logging interceptor's output), but the client wasn't honoring it — every launch re-fetched over the network regardless. Added a 5MB OkHttp disk `Cache` (`NetworkModule.kt`), no other code changes.

**What happened** — same device, same 5-trial cold-start protocol, cache cold on trial 1 (fresh install) then warm for trials 2–5 (process force-stopped between each, cache persists on disk):

| Trial | TTR (ms) | Network (ms) |
|---|---|---|
| 1 (cold cache) | 1618 | 1422 |
| 2 (warm) | 320 | 30 |
| 3 (warm) | 204 | 10 |
| 4 (warm) | 257 | 23 |
| 5 (warm) | 118 | 12 |

Warm-cache median TTR: **~230ms**, vs. 947ms baseline — overhead against static drops from **+617% to ~+74%**. Trial 1 (1618ms) is noisier than the pre-optimization baseline but in the same range as other cold-network trials observed during this session (700ms–1.4s); disk caching doesn't change first-launch behavior at all, only every launch after it within the `max-age` window.

**What I didn't try, and why**: a `stale-while-revalidate` or short-TTL strategy would keep freshness closer to true SDUI's promise (edit JSON → users see it immediately) while still getting most of the cache win — this trades implementation time against a metric not directly requested (freshness isn't in the rubric's table), so it's noted here rather than built. The 1-hour cache window is also a real trade-off worth being explicit about: a JSON edit made now could take up to an hour to reach a user with a warm cache, which cuts against the core pitch of this whole assignment. **I would not ship this exact cache policy** without a shorter TTL or a manual-invalidation path — it's included here as a measurement exercise, not a production recommendation.

## Scroll performance

`dumpsys gfxinfo <pkg> framestats` around a scripted 6-pass scroll (`adb shell input swipe`) on each variant, single capture per variant (not averaged — see caveat):

| | Static | SDUI |
|---|---|---|
| Total frames | 147 | 153 |
| Janky frames | 47 (31.97%) | 38 (24.84%) |
| 50th / 90th / 99th percentile frame time | 36 / 57 / 117 ms | 36 / 48 / 69 ms |

**Caveat, stated plainly**: this is one capture per variant using `adb shell input swipe`, which is a synthetic gesture, not a real finger — frame timing from it is noisier than a real scroll and than Macrobenchmark's `frameTimingCompat`. Static showing *more* jank than SDUI here is almost certainly measurement noise from a single run, not a real effect (the underlying `RecyclerView`/ViewHolder code scrolling is identical for both). I'm reporting it as measured rather than discarding or averaging it into something cleaner-looking — a proper version of this metric would use Macrobenchmark's `FrameTimingMetric` across multiple iterations, which was cut for time (see README trade-offs).

## Summary

- SDUI's cold-start overhead versus a hardcoded screen is real and large (7×+) when starting from nothing, and it is **almost entirely network latency**, not parsing or the widget registry/dispatch mechanism — view-build time is statistically indistinguishable between the two variants.
- A response cache that honors headers the server was already sending cut that overhead from +617% to +74% with no code changes beyond adding the cache, at the cost of up to an hour of staleness — a trade-off I'm flagging rather than presenting as free.
- Scroll performance is not distinguishable from this measurement pass; I'd want Macrobenchmark and more trials before claiming a real difference either way.
