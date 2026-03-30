# Design System Specification

## 1. Overview & Creative North Star: "The Monolithic Ledger"
This design system rejects the soft, bubbly aesthetics of modern consumer apps in favor of a high-end, editorial approach we call **The Monolithic Ledger**. It is inspired by archival financial documents and high-fashion lookbooks. 

The Creative North Star is **Absolute Precision.** By utilizing a strictly 0px radius across all components and a monochromatic palette, we create a "terminal-esque" environment that feels authoritative and distraction-free. The "premium" feel is achieved not through shadows or gradients, but through intentional void (white space), extreme typographic scale, and the mathematical rhythm of the layout.

## 2. Colors & Surface Logic
The palette is rooted in deep charcoal (`#131313`) to ensure the OLED display feels infinite, with off-white text providing high legibility without the harshness of pure white.

### The "No-Line" Rule
Traditional 1px borders are largely prohibited. To separate the header from the feed or a category from a list, use a shift in surface tokens.
*   **Primary Surface:** `surface` (#131313)
*   **Secondary Sectioning:** Use `surface-container-low` (#1c1b1b) or `surface-container-high` (#2a2a2a) to define content blocks.
*   **The Intentional Gap:** Use the Spacing Scale (specifically `8` or `10`) to create "gutters" of the base `background` color between containers. This "negative space as a border" is a hallmark of high-end editorial design.

### Surface Hierarchy & Nesting
Depth is achieved through tonal stacking rather than elevation.
1.  **Level 0 (Base):** `surface-dim` (#131313)
2.  **Level 1 (Cards/Sections):** `surface-container` (#201f1f)
3.  **Level 2 (Interaction/Active):** `surface-container-highest` (#353534)

## 3. Typography: The Silent Hierarchy
We use **Inter** exclusively. Since we are avoiding bold decorative weights to maintain a "utilitarian" vibe, hierarchy must be established through **Scale** and **Letter Spacing**.

*   **Display Scale:** Use `display-lg` (3.5rem) for the primary account balance. This creates a "hero" moment that feels like a magazine headline.
*   **Labeling:** Use `label-sm` (0.6875rem) with an increased tracking (letter-spacing) for metadata. 
*   **The Weight Rule:** Stick to Regular (400) and Medium (500). Let size do the heavy lifting. A `display-sm` at 400 weight feels more premium than a small bold header.

## 4. Elevation & Depth: Tonal Layering
Because this system uses a `0px` roundedness scale (strictly square) and forbids traditional shadows, we must use **Tonal Layering** to guide the eye.

*   **The Ghost Border:** For input fields or areas requiring a container definition, use the `outline-variant` token (#474747) at 30% opacity. It should be felt, not seen.
*   **Active States:** Instead of a shadow, an "elevated" or "pressed" state is represented by switching the background to `primary` (#ffffff) and the text to `on_primary` (#1a1c1c). This high-contrast "Invert" pattern is a classic brutalist technique.
*   **Backdrop Blurs:** For navigation bars or floating action elements, use `surface` (#131313) at 80% opacity with a heavy background blur. This creates a "glass" effect that prevents the UI from feeling claustrophobic while maintaining the dark aesthetic.

## 5. Components

### Buttons
*   **Primary:** Rectangular (0px radius). Background: `primary`. Text: `on_primary`. 
*   **Secondary:** Background: `transparent`. Border: 1px `outline` (#919191).
*   **Padding:** Use Spacing `3` (0.6rem) for vertical and `6` (1.3rem) for horizontal.

### The Dot List (Signature Element)
Replace all icons with the "Signature Dot."
*   **List Items:** Use a 4px x 4px square or circle (using `outline` color) to the left of the text.
*   **Visual Weight:** The dot should align with the cap-height of the `title-md` text. This removes visual noise and forces the user to focus on the data (the numbers).

### Input Fields
*   **Structure:** No enclosing box. Use a bottom-only border (1px `outline-variant`).
*   **Focus State:** The bottom border transitions to `primary` (#ffffff).
*   **Text:** Input text uses `body-lg`, while the label uses `label-md` floating above the line.

### Cards & Lists
*   **Card Styling:** Forbid the use of divider lines. Separate "Daily Expense" groups by placing them on a `surface-container-low` block with a `12` (2.75rem) margin between groups.
*   **Data Density:** Use `title-lg` for currency amounts and `body-sm` for timestamps. Ensure the currency is right-aligned to create a clean vertical axis.

## 6. Do's and Don'ts

### Do:
*   **Use the Grid:** Align everything to a strict margin (Spacing `6` or `8`). In a minimal system, misalignment is a catastrophic error.
*   **Embrace the Void:** If a screen feels "empty," do not add lines. Increase the font size of the primary data point.
*   **Use Subtle Transitions:** When a user taps a list item, transition the background color from `surface` to `surface-container-highest` over 200ms.

### Don't:
*   **Never Use Rounded Corners:** Even a 2px radius will break the "Monolithic" architectural feel of this design system.
*   **No Drop Shadows:** Shadows imply a light source that doesn't exist in this utilitarian world. Use color contrast for depth.
*   **Avoid Icons:** Unless it is a standard Android system gesture, default to text labels or the "Signature Dot." If an icon is mandatory, use thin-stroke (1px) linear icons only.