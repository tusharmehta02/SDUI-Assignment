# Cars24 SDUI Assignment — Android

A Server-Driven UI renderer for the Cars24 home/landing screen: the client ships zero screen-specific layout code, and the entire page — every section, its content, and one interactive tab-driven content switch — is described by a JSON contract fetched at runtime.

## Which screen, and why

**Home/landing page.**

The home screen is the page most likely to change in a real product — new promotional rails, reordered sections, seasonal banners, A/B-tested category groupings — and it's the one where teams want to experiment without waiting on a release train. It also naturally clears the assignment's complexity bar: 6 distinct widget types, a horizontal rail, a horizontal carousel, a 2-column and 3-column grid, and a tab bar whose selection swaps the entire content section below it — not a static filter, an actual SDUI-driven re-render.

## Setup

```bash
git clone https://github.com/tusharmehta02/SDUI-Assignment.git
cd SDUI-Assignment
./gradlew assembleDebug
```

Open in Android Studio (Jellyfish+) and run on an emulator/device with internet access — the app fetches its contract from a hosted JSON endpoint (`api.npoint.io`) on launch. No local server or build-time config needed.

## Architecture overview

**MVVM**, chosen for a clean separation between "what the contract says" and "how it's drawn":

```
ApiService (Retrofit)
   ↓ DiscoverPageResponse
WidgetDeserializer (Gson, polymorphic on widget_type)
   ↓ sealed WidgetPayload tree
HomeRepository → HomeRepositoryImpl
   ↓ HomeWidgets (headerWidgets, contentByTab)
HomeViewModel
   ↓ StateFlow<HomeUiState>
MainActivity → HomeAdapter (component registry)
   ↓
Per-widget ViewHolders + sub-adapters
```

- **Repository sits behind an interface** (`HomeRepository`), bound via Hilt (`@Binds`). The ViewModel never sees Retrofit — swapping in a fake/local-file repository for tests is a one-line DI change.
- **`HomeAdapter` is the component registry**: `getItemViewType` switches on `widget_type`, and each widget gets its own ViewHolder + small `RecyclerView.Adapter` that knows only its own `WidgetPayload` subtype. Adding a widget type means adding one `WidgetPayload` case, one deserializer branch, one layout, one adapter — nothing else changes.
- **Header vs. content are rendered as two separate RecyclerViews.** Sublayouts of type `HEADER` (search bar, tab bar) render pinned above a scrollable `MAIN_SCREEN` content list — this is what makes the search bar and tabs sticky while the rest of the page scrolls.

## Schema design

The contract is broken into **sublayouts → widget groups → widgets**, each widget carrying a `widget_type` discriminator and a `json_payload` shaped for that type:

```json
{
  "data": {
    "sublayouts": [
      { "type": "HEADER", "widgetgroups": [ { "widgets": [ ...search bar, tab bar... ] } ] },
      { "type": "MAIN_SCREEN", "tabId": "all", "widgetgroups": [ { "widgets": [ ...rail, grid, banner, products... ] } ] },
      { "type": "MAIN_SCREEN", "tabId": "buy_used_car", "widgetgroups": [ { "widgets": [ ...different grid, different products... ] } ] }
    ]
  }
}
```

- **`sublayouts`** separate structurally distinct regions of the page (pinned header vs. scrolling body). A `MAIN_SCREEN` sublayout can carry a `tabId`, so the same contract can describe multiple content sets and the client swaps between them on a tab tap — no second network call.
- **`widgetgroups`** exist as a grouping seam for future use (e.g., server-driven A/B variants of a group, or lazy-loading a group independently) without changing the widget-level schema.
- **`widgets`** are self-contained: `widget_id`, `widget_type`, `status`, `position`, `json_payload`. `status` lets the server mark a widget `PENDING`/`SUCCESS`/failed independently, and `position` lets ordering change without reordering the JSON array.
- **Actions are a shared, typed shape** (`ActionRef`) reused across every widget — navigation, widget-refresh, or operation intents — so tapping a tab, a category card, or a product all resolve through the same dispatcher.
- **Style is data, not code**: colors (`backgroundColor` on tabs, badges, cards) are hex strings read at runtime and applied to shape drawables; a `cardStyle` field (`ICON` vs `IMAGE_CARD`) lets one `CategoryGridWidget` type express two different visual templates from JSON alone.
- **Unknown-component fallback**: any `widget_type` the client doesn't recognize (or a widget with `status != SUCCESS`) parses to a sealed `WidgetPayload.Unknown` and is filtered out before rendering — the rest of the page renders normally, nothing crashes.

## Versioning story

Two kinds of contract change, two different mechanisms:

1. **Same widget shape, different visual treatment** (e.g., swap the search bar for a "square search bar" variant). This is a **prop-level or `variant`-field change within an existing `widget_type`** — the backend gates it on `app_version` / a feature flag when building the response, so old clients keep receiving the payload shape they already know how to render, and new clients opt into the new variant. No client release needed on either side.

2. **A structural/breaking schema change** (e.g., introducing a new nesting level above `sublayouts` — a "super layout" wrapping multiple layouts). This changes what the *shape of the response itself* means, which old clients cannot safely interpret no matter what data is in it. For this, the **backend cuts a new API version** (e.g. `/v2/discover`) rather than mutating `/v1` in place. Old app builds keep calling `/v1` and keep working; new builds move to `/v2` once they ship a renderer that understands the new structure. This mirrors how the client already treats `status` and unknown `widget_type` — the safety net for "old client, new field" is unknown-fallback; the safety net for "old client, new shape" is a versioned endpoint.

## Trade-offs / scoping decisions

- **Action dispatch is real; navigation isn't.** `ActionHandler` fully parses and routes every `ActionRef`, but resolves to a log line instead of a real screen — proving dispatch mattered more than building destinations nothing else needs.
- **One platform, built deep.** Android only, per the brief's own preference for depth over shallow multi-platform.
- **`PENDING` widgets are dropped, not polled.** The schema supports progressive loading; the renderer doesn't yet re-poll for a widget that arrives late.
- **No offline cache, no personalization.** Every launch re-fetches the same contract fresh — proving the render pipeline was the priority, not caching.
- **Versioning is a written decision, not shipped code.** Per the brief, a README section is enough; no `app_version` gating or `/v2` endpoint exists in this repo.
