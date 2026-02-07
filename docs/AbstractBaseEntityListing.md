# AbstractBaseEntityListing – Usage Guide

## Purpose

`AbstractBaseEntityListing<T>` is a reusable **base UI component** designed to implement entity listings with common CRUD behaviour in Vaadin Flow applications. It provides a shared structural and behavioural foundation that can be specialized for different domain entities (e.g. Users, Devices, Networks).

The class follows the **Template Method** pattern: the base class defines *when* actions happen, while subclasses define *how* they are executed.

---

## What the Base Class Provides

### 1. Common UI Structure

`AbstractBaseEntityListing` owns and manages the core layout elements:

* A main container layout for the view
* A toolbar / buttons area
* A selectable component (typically a Grid)
* An optional editor area

This guarantees a consistent layout and interaction model across all listings.

---

### 2. Selection Handling

The base class manages selection logic in a generic way:

* A selectable component (usually a Grid) is registered
* Selection changes are automatically detected
* Action buttons are enabled or disabled based on selection state

The subclass does **not** need to implement selection listeners explicitly.

---

### 3. Permissions Awareness

`AbstractBaseEntityListing` is permission-aware:

* Permissions are injected once
* View / create / modify / remove modes are respected consistently
* Subclasses only decide *which* actions exist, not *when* they are allowed

---

### 4. Data Provider Abstraction

The base class is agnostic about how data is loaded. It only relies on a Vaadin `AbstractDataProvider` and exposes hooks to:

* Assign a backend data provider
* Apply filters (if supported)
* Query the total size of the dataset

This allows the same UI logic to work with different data-loading strategies.

---

## Responsibilities of Subclasses

A concrete listing (e.g. `UsersListing`) must provide domain-specific behaviour by implementing the abstract hooks:

* How to open the editor form (read-only or editable)
* How to show details
* How to handle removal

In addition, the subclass is responsible for:

* Building the Grid (columns, headers, sorting)
* Defining filters and filter UI
* Creating action buttons and wiring them to base-class actions

---

## Data Provider Strategies

`AbstractBaseEntityListing` supports **two main data provider approaches**, without any change to its API.

### In-Memory Data Provider

A listing can be backed by an in-memory data provider when:

* The dataset is small
* All data can be loaded eagerly
* Filtering and sorting can be handled in memory

This approach is simple and fast for administrative or low-volume views.

---

### Lazy / Backend Data Provider

For large datasets or database-backed listings, a lazy-loading strategy can be used.

A typical setup uses:

* A lazy query data provider
* A query definition object holding filters, paging, and sorting
* A query factory translating UI queries into repository calls

This allows:

* Server-side paging
* Server-side sorting
* Complex filtering
* Permission-aware queries

The base class treats this provider exactly the same as an in-memory one.

---

## Typical Usage Flow

1. A concrete listing extends `AbstractBaseEntityListing<T>`
2. The listing initializes permissions and builds its layout
3. A Grid is created and registered as the selectable component
4. A data provider (in-memory or lazy) is assigned
5. Action buttons delegate to base-class behaviour
6. Editors and dialogs are provided by the subclass

---

## Architectural Benefits

Using `AbstractBaseEntityListing` provides:

* Strong consistency across all CRUD listings
* Clear separation between UI structure and domain logic
* Reusable permission and selection handling
* Easy switching between in-memory and lazy data providers
* Reduced boilerplate in concrete listings

---

## Summary

`AbstractBaseEntityListing` acts as a **generic CRUD listing framework** inside the application. Subclasses focus only on domain-specific details, while the base class enforces consistent UI behaviour, selection logic, permissions, and data-provider integration.

This design scales well from simple in-memory lists to complex, lazy-loaded, permission-aware enterprise vi
