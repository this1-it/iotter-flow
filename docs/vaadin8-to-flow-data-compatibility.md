# Compatibility Matrix  
## `com.vaadin.flow.data.*` — Vaadin 8 Standalone → Vaadin Flow

> **Assumptions**
> - Vaadin 8 **standalone** (no Vaadin 7 compatibility packages)
> - Native use of `DataProvider`, `Binder`, `Grid<T>`, `ValueProvider`
> - No `Container` / `Item` / `Property` APIs involved

This document focuses **only** on the data/binding layer and is intended for **technical migration**, not introduction.

---

## 1. DataProvider & Backend Data

| Vaadin 8 Standalone | Vaadin Flow | Compatibility |
|--------------------|-------------|---------------|
| `com.vaadin.flow.data.provider.DataProvider<T, F>` | `com.vaadin.flow.data.provider.DataProvider<T, F>` | ✅ **1:1** |
| `AbstractBackEndDataProvider<T, F>` | `AbstractBackEndDataProvider<T, F>` | ✅ **1:1** |
| `CallbackDataProvider<T, F>` | `CallbackDataProvider<T, F>` | ✅ **1:1** |
| `ListDataProvider<T>` | `ListDataProvider<T>` | ✅ **1:1** |
| `Query<T, F>` | `Query<T, F>` | ✅ **1:1** |
| `QuerySortOrder` | `QuerySortOrder` | ✅ **1:1** |
| `SortDirection` | `SortDirection` | ✅ **1:1** |

**Notes**
- `fetchFromBackEnd` / `sizeInBackEnd` signatures are identical
- Same semantics and lifecycle
- Same constraints: stateless, UI-thread safe

---

## 2. Binder (Binding & Form Model)

| Vaadin 8 Standalone | Vaadin Flow | Compatibility |
|--------------------|-------------|---------------|
| `com.vaadin.flow.data.Binder<T>` | `com.vaadin.flow.data.binder.Binder<T>` | ✅ **1:1** |
| `Binder.Binding<T, V>` | `Binder.Binding<T, V>` | ✅ |
| `Binder.BindingBuilder<T, V>` | `Binder.BindingBuilder<T, V>` | ✅ |
| `BinderValidationStatus<T>` | `BinderValidationStatus<T>` | ✅ |
| `ValidationResult` | `ValidationResult` | ✅ |

**Notes**
- `readBean`, `writeBean`, `writeBeanIfValid` unchanged
- `setBean()` unchanged
- Validation handling is identical

---

## 3. ValueProvider / Getter / Setter

| Vaadin 8 Standalone | Vaadin Flow | Compatibility |
|--------------------|-------------|---------------|
| `ValueProvider<T, V>` | `ValueProvider<T, V>` | ✅ **1:1** |
| `Setter<T, V>` | `Setter<T, V>` | ✅ |
| `SerializableFunction` | `SerializableFunction` | ✅ |

Used by:
- `Grid.addColumn(...)`
- `Binder.bind(getter, setter)`

---

## 4. Converter

| Vaadin 8 Standalone | Vaadin Flow | Compatibility |
|--------------------|-------------|---------------|
| `Converter<PRESENTATION, MODEL>` | `Converter<PRESENTATION, MODEL>` | ✅ **1:1** |
| `StringToIntegerConverter` | `StringToIntegerConverter` | ✅ |
| `StringToDoubleConverter` | `StringToDoubleConverter` | ✅ |
| `LocalDateToDateConverter` | `LocalDateToDateConverter` | ✅ |
| `Result<T>` | `Result<T>` | ✅ |

**Notes**
- `withConverter(...)` pipeline unchanged
- Error handling via `Result.error(...)` unchanged

---

## 5. Validator

| Vaadin 8 Standalone | Vaadin Flow | Compatibility |
|--------------------|-------------|---------------|
| `Validator<V>` | `Validator<V>` | ✅ **1:1** |
| `StringLengthValidator` | `StringLengthValidator` | ✅ |
| `EmailValidator` | `EmailValidator` | ✅ |
| `DateRangeValidator` | `DateRangeValidator` | ✅ |
| `RegexpValidator` | `RegexpValidator` | ✅ |
| `BeanValidator` | `BeanValidator` | ⚠️ see note |

**Important notes**
- `Validator.apply(value, context)` unchanged
- `ValidationResult.ok()` / `.error()` unchanged
- `BeanValidator`:
  - Still available
  - In Flow, **JSR-380 (`javax.validation`) is preferred**
  - Compatibility still exists

---

## 6. HasValue / Value Change API

| Vaadin 8 Standalone | Vaadin Flow | Compatibility |
|--------------------|-------------|---------------|
| `HasValue<V>` | `HasValue<C, V>` | ⚠️ generic signature differs |
| `HasValue.ValueChangeEvent<V>` | `HasValue.ValueChangeEvent<V>` | ✅ |
| `ValueChangeMode` | `ValueChangeMode` | ✅ |

**Note**
- Flow introduces `HasValue<C, V>` (component + value)
- Rarely impacts Binder or Data logic

---

## 7. DataView (if used)

| Vaadin 8 (late versions) | Vaadin Flow | Compatibility |
|--------------------------|-------------|---------------|
| `Grid.getDataProvider()` | `Grid.getDataProvider()` | ✅ |
| `Grid.getDataCommunicator()` | `DataView` | ⚠️ improved |
| `GridListDataView` | `GridListDataView` | ✅ |
| `GridLazyDataView` | `GridLazyDataView` | ✅ |

**Note**
- Flow makes `DataView` explicit
- No breaking changes in data semantics

---

## 8. APIs Not Present in Flow  
*(already absent in Vaadin 8 standalone)*

| API | Status |
|----|-------|
| `Container` | ❌ |
| `BeanItemContainer` | ❌ |
| `Item` / `Property<?>` | ❌ |
| `FieldGroup` | ❌ |

No migration required — they should not exist in your codebase.

---

## ✅ Technical Summary

- **`com.vaadin.flow.data.*` Vaadin 8 standalone → Flow is almost entirely 1:1**
- `DataProvider`, `Binder`, `Validator`, `Converter` are **conceptually and technically continuous**
- Differences are **package-level**, not architectural
- Migration effort is **not in the data layer**, but in:
  - UI components
  - Layouts
  - Grid rendering
  - Navigation / lifecycle

