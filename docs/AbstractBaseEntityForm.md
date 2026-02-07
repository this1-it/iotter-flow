# AbstractBaseEntityForm – Usage Guide

## Purpose

`AbstractBaseEntityForm<T>` is a reusable **base form framework** for editing domain entities in Vaadin Flow. It standardizes how forms are built, ordered, bound, validated, and committed, while leaving domain-specific details to subclasses.

The class is designed to work together with `AbstractBaseEntityListing`, forming a coherent CRUD architecture:

* *Listing* handles selection, navigation, and actions
* *Form* handles editing, validation, and persistence hooks

---

## Core Responsibilities of the Base Form

### 1. Binder-Centered Form Management

`AbstractBaseEntityForm` is built around Vaadin `Binder`:

* A `Binder<T>` is created and owned by the base class
* Validation state automatically controls the Save button
* Read-only mode is enforced consistently

Subclasses define *what* to bind, the base class controls *when* validation and commit occur.

---

### 2. Declarative Field Discovery and Ordering

The base form automatically discovers form fields using reflection:

* All declared fields of the subclass are scanned
* Only Vaadin `Component` fields are considered
* Fields are ordered using a dedicated ordering annotation
* Property names are resolved using `@PropertyId` or the field name

This allows form layout to be **declarative** and **self-describing**, with no manual field registration required.

---

### 3. Explicit Field Ordering

Field order is not implicit or positional.

A dedicated annotation is used to define form layout order:

* Each field can declare its position explicitly
* Fields without an order are placed at the end
* Ordering is deterministic and refactoring-safe

This avoids reliance on source-code declaration order.

---

### 4. Automatic Form Layout Composition

The base class builds the visual structure of the form:

* Fields are added to a `FormLayout`
* The form is wrapped in a panel container
* A footer with action buttons is always appended

Subclasses never deal with layout boilerplate unless they want to override it.

---

### 5. Read-Only and Action Handling

The form supports both editable and read-only modes:

* Save button is hidden in read-only mode
* Fields can query `isReadOnly()` during initialization
* Cancel always closes the parent dialog

This makes the same form reusable for *create*, *edit*, and *view* scenarios.

---

### 6. Commit Lifecycle Hooks

The save process follows a strict lifecycle:

1. Pre-commit validation hook
2. Binder writes values into the entity
3. Post-commit hook
4. Event notification (optional)
5. Invocation of save handlers

Subclasses inject domain rules without duplicating infrastructure code.

---

## Responsibilities of Subclasses

A concrete form (e.g. `ChannelAlarmForm`) must:

* Instantiate and configure all field components
* Bind fields to entity properties using Binder
* Define domain-specific validation rules
* Implement pre-commit and post-commit hooks

The subclass focuses purely on **domain logic**, not form mechanics.

---

## Typical Usage Pattern

1. The form subclass declares its fields as component members
2. Fields define their logical order via an ordering annotation
3. Fields optionally define a binding property identifier
4. The base form automatically assembles the UI
5. Binder validation controls save availability
6. Commit hooks enforce domain constraints

---

# AbstractBaseEntityForm – Field-Based and Tabbed Variants

## Overview

`AbstractBaseEntityForm<T>` supports **multiple form composition strategies** while keeping the same binding, validation, and commit lifecycle.

Within the codebase there are **two complementary usage styles**:

1. **Field-based, ordered forms** (single FormLayout, automatic ordering)
2. **Tabbed, manually composed forms** (multiple FormLayouts organized in tabs)

Both approaches share the same base class and lifecycle, but differ intentionally in how the UI is composed.

---

## 1. Field-Based (Ordered) Form Variant

This is the **default and most compact usage** of `AbstractBaseEntityForm`.

### Characteristics

* Fields are declared as component members
* Field discovery is automatic via reflection
* Field order is declared explicitly using an ordering annotation
* The base class assembles a single `FormLayout`
* Suitable for linear or moderately complex forms

### When to Use

* Simple or medium-sized forms
* Forms with a natural vertical flow
* Cases where layout structure is not conditional
* High reuse and minimal UI boilerplate

## Example: /iotter-flow-ui/src/main/java/it/thisone/iotter/ui/channels/ChannelAlarmForm.java (Ordered)

The ChannelAlarmForm is a representative example of the tabbed variant.

### Benefits

* Very low code duplication
* Deterministic ordering independent of source code order
* Easy to refactor and extend
* Strong consistency across forms

---

## 2. Tabbed (Manually Composed) Form Variant

Some forms require **logical grouping** of fields rather than a flat structure.

In these cases, `AbstractBaseEntityForm` is still used, but the **layout composition is overridden**.

### Key Difference

Instead of relying on automatic field ordering, the subclass:

* Overrides `getFieldsLayout()`
* Builds multiple `FormLayout` instances explicitly
* Groups fields by semantic responsibility
* Places them inside a tab container (e.g. `TabSheet`)

The base class still provides:

* Binder creation and validation
* Save / cancel / reset / delete lifecycle
* Read-only handling
* Commit hooks

Only the **visual composition strategy changes**.

---

## Example: /iotter-flow-ui/src/main/java/it/thisone/iotter/ui/users/UserForm.java (Tabbed)

The UserForm is a representative example of the tabbed variant.

### Rationale

The user entity combines multiple independent concerns:

* Login and credentials
* Personal and contact information
* Authorization and group membership
* Optional visualization configuration

A single linear form would be hard to read and maintain.

---

### Tabbed Structure

The form is split into logical sections:

* **Login**: username, status, passwords
* **User Info**: personal and contact fields
* **Authorization**: role, network, groups
* **Visualizations** (optional): advanced configuration

Each section:

* Uses its own `FormLayout`
* Is added to a dedicated tab
* Can be conditionally visible based on role, permissions, or entity state

---

## Why This Is Still the Same Abstract Form

Despite the different UI composition:

* All fields are still bound via the same `Binder`
* Validation rules are unchanged
* Read-only mode is enforced uniformly
* Commit lifecycle (`beforeCommit` / `afterCommit`) is identical
* Event posting and handlers are reused

The difference is **structural**, not architectural.

---

## Choosing the Right Variant

| Scenario                    | Recommended Variant      |
| --------------------------- | ------------------------ |
| Small or linear forms       | Field-based ordered form |
| Large or multi-domain forms | Tabbed form              |
| Conditional field groups    | Tabbed form              |
| High reuse and simplicity   | Field-based ordered form |

---

## Architectural Insight

`AbstractBaseEntityForm` is intentionally **layout-agnostic**.

It defines:

* *Lifecycle*
* *Validation semantics*
* *Commit boundaries*

but does **not** enforce:

* a single layout type
* a single composition strategy

This allows the same base class to support both simple forms and highly structured, role-dependent editors without duplication or fragmentation.

---

## Summary

The presence of both ordered and tabbed forms is not an inconsistency, but a **deliberate design choice**.

`AbstractBaseEntityForm` acts as a stable form engine, while subclasses are free to choose the most appropriate UI composition strategy:

* Automatic, ordered layouts for clarity and speed
* Manual, tabbed layouts for complex domain editors

Both variants coexist cleanly and share the same underlying infrastructure.

---

## Architectural Benefits

Using `AbstractBaseEntityForm` provides:

* Consistent form behaviour across the application
* Strong separation between infrastructure and domain logic
* Deterministic field ordering independent of source layout
* Reduced boilerplate in concrete forms
* Easy reuse for create, edit, and view flows

---

## Summary

`AbstractBaseEntityForm` acts as a **form construction and lifecycle engine**. It removes repetitive UI and Binder logic from concrete forms, enforces consistent behaviour, and allows developers to focus entirely on domain-specific validation and binding.

Together with `AbstractBaseEntityListing`, it forms a robust, scalable CRUD UI framework suitable for complex enterprise applications.
