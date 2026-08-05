# COVERAGE.md

## Component registry

Every widget is `{ widget_id, widget_type, status, position, json_payload }`. `WidgetDeserializer` reads `widget_type` and picks the matching `WidgetPayload` subtype; `HomeAdapter.getItemViewType` does the same lookup for rendering. Six types are registered:

| `widget_type` | Payload shape | Visual pattern |
|---|---|---|
| `SearchBarWidget` | `placeholderText`, `hintChips[]`, `variant`, `action` | Header search entry point; hint chips animate/rotate |
| `TabBarWidget` | `items[]`: `id`, `label`, `selected`, `backgroundColor`, `action` | Horizontal single-select segmented control |
| `HorizontalRailWidget` | `header{title,badge}`, `items[]`: `id`,`title`,`backgroundImage`,`backgroundColor`,`action` | Horizontal scrolling rail of image+label cards |
| `CategoryGridWidget` | `header{title}`, `gridConfig{rowCount,columnCount,cardStyle}`, `items[]`: `title`,`subtitle`,`categoryImage`,`backgroundColor`,`action` | Vertical N-column grid; `cardStyle` (`ICON` vs `IMAGE_CARD`) switches visual template from the same widget type |
| `BannerCarouselWidget` | `items[]`: `id`,`image`,`action`,`mediaType`; `carousel{autoScrollEnabled,autoScrollIntervalMs,indicatorEnabled,variant}` | Auto-advancing full-width carousel with dot indicators |
| `ProductCollectionWidget` | `header{title,subtitle,cta}`, `filterChips[]`, `items[]`: `productId`,`title`,`subtitle`,`productImage`,`tag`,`specs[]`,`price{...}`,`badges[]`,`wishlistIcon`,`action`,`status` | Filterable rail of rich product cards |

## Patterns the schema expresses without new widget types

- **Actions, uniformly.** Every tappable element in every widget resolves through one `ActionRef` shape (`ACTION_TYPE_DISCOVER_PAGE` / `_DISCOVER_WIDGETS` / `_OPERATION`, each with its own typed sub-payload). A tab, a rail card, a grid tile, and a product card all dispatch the same way.
- **Styling as data.** Any `*Color` field is a hex string, parsed and applied to the view's `GradientDrawable` at bind time — tab pills, header background, rail-card and grid-item backgrounds, badge colors. Rebranding a section or accenting one category is a JSON-only edit.
- **A style-variant field reusing one widget type.** `CategoryGridWidget`'s `cardStyle` (`ICON` vs `IMAGE_CARD`) proves a single registry entry can carry two genuinely different visual templates — no new `widget_type` needed for a "richer" version of an existing pattern.
- **Graceful degradation.** `status != SUCCESS` or an unrecognized `widget_type` both resolve to `WidgetPayload.Unknown` and are filtered before rendering — the rest of the page is unaffected.
- **Content sets keyed by tab, no new endpoint.** `MAIN_SCREEN` sublayouts carry an optional `tabId`; a tab tap swaps the entire rendered section list to a different, fully JSON-authored set. This is the assignment's own example of "a tab/chip selection that changes content," built and demonstrated live (`Buy used car` tab).
- **Reordering and partial readiness.** `position` and `status` per widget mean the server can reorder sections or mark one not-yet-ready without touching array structure or client code.

## Coverage claim

**Given a new Cars24 screen in the same family as this one — home/landing, category browse, listing/search results — I'd estimate roughly 65–75% renders with JSON-only changes**, reusing the existing rail/grid/carousel/product-collection/tab patterns with new data, images, and colors. That's the range of screens sharing this one's actual structure: a scrolling stack of section types.

**What would need new client code, named specifically:**

- **Any visual template not already registered** — a video player, a map, a comparison table, a star-rating control, a stepper/wizard, a chat thread, a text-input form, a bottom-sheet/modal container. Each needs one `WidgetPayload` subtype + one deserializer branch + one layout + one ViewHolder — the exact pattern this repo's own commit history shows for every widget added so far (each was a same-session addition, typically under an hour). Fast, but it's code, not JSON.
- **More than one sticky region.** The renderer supports exactly one pinned `HEADER` sublayout above one scrolling `MAIN_SCREEN` list. A screen needing a second sticky sub-header partway down (common on filter/listing screens) isn't covered.
- **Non-tap interactions** — drag-to-reorder, swipe-to-dismiss, long-press menus. `ActionRef` models tap-driven intents only.
