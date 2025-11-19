# LMCore `.lm` How‑To (M2 Meta‑Models)

This is a short, pragmatic guide for writing LMCore `.lm` meta‑models (M2) that work well with `logoce.lmf.model` and `logoce.lmf.generator`.

The LMCore definition itself lives in `logoce.lmf.model/src/main/model/asset/model.lm`. The CarCompany example model is in `logoce.lmf.generator/src/test/model/CarCompany.lm`.

## 1. File Skeleton

- One file = one `MetaModel`:

```lm
(MetaModel domain=my.domain.name name=MyModel
    ;; Groups, enums, units, aliases, wrappers...
)
```

- `domain` is the Java package where generated types will live.
- `name` is the model name; the generator will produce e.g. `MyModelDefinition`, `MyModelPackage`, etc.

## 2. Groups vs Definitions

- **Group** = abstract concept (like a “class kind”).
- **Definition** = concrete subtype of a `Group`.

Example:

```lm
    (Group Entity
        (+att name=name datatype=#LMCore@string [1..1]))

    (Definition Person
        (includes group=@Entity)
        (+att name=age datatype=#LMCore@int [0..1]))
```

- `Group Entity` declares a reusable concept.
- `Definition Person` extends `Entity` via `includes group=@Entity`.

Use `Group` when you want a reusable base; use `Definition` for each concrete domain type.

## 3. Features: Attributes and Relations

Features live inside `Group` or `Definition` blocks and are declared using **aliases** defined in LMCore:

- `+att` / `-att` → mutable / immutable **Attribute**
- `+contains` / `-contains` → mutable / immutable **Relation** with `contains = true`
- `+refers` / `-refers` → mutable / immutable **Relation** with `contains = false`

General patterns:

```lm
    (+att name=foo datatype=#LMCore@string [1..1])
    (-att name=bar datatype=@MyEnum [0..1] defaultValue=SomeLiteral)

    (+contains name=children [0..*] (reference @ChildType))
    (+refers name=owner [0..1] (reference @OwnerType))
```

- `datatype`:
  - `#LMCore@string` → primitive from LMCore (see `Primitive` enum in LMCore).
  - `@MyEnum` → enum defined in the same meta‑model *or* in an imported/upper meta‑model (e.g. in an M2 you can reference the LMCore `string` unit with `@string` instead of `#LMCore@string` when imports are set up accordingly).
  - `/units.X` or `/enums.X` → reference LMCore units/enums by index (used inside LMCore itself).
- `[min..max]`:
  - `[0..1]`, `[1..1]`, `[0..*]`, `[1..*]` are aliases that expand to `mandatory` / `many` flags.

**Relation targets**:

```lm
    (reference @SomeGroup)
    (reference group=@SomeGroup)
```

Both forms refer to the `Group` called `SomeGroup`; the `group=` form is used when disambiguation is needed.

## 4. Enums and Units

Enums:

```lm
    (Enum Brand Renault,Peugeot)
```

- Generates a Java enum `Brand` in `domain` with the given literals.

Units (custom scalar types) are more advanced and mostly used in LMCore itself:

```lm
    (Enum name=Primitive boolean,int,long,float,double,string)
    (Unit name=string)
```

For most domain models you can stick to LMCore’s built‑in primitives (`#LMCore@string`, `#LMCore@int`, etc.) and your own enums.

## 5. Example: CarCompany

`logoce.lmf.generator/src/test/model/CarCompany.lm` shows a minimal but complete model:

```lm
(MetaModel domain=test.model name=CarCompany
    (Group Entity
        (+att name=name datatype=#LMCore@string [1..1]))

    (Enum Brand Renault,Peugeot)

    (Definition Car
        (includes group=@Entity)
        (-att name=brand datatype=@Brand [1..1] defaultValue="Peugeot")
        (+contains name=passengers (reference @Person) [0..*]))

    (Definition CarParc
        (+contains cars [0..*] (reference @Car)))

    (Definition Person
        (includes group=@Entity)
        (+refers car [0..1] (reference @Car)))

    (Definition CarCompany
        (includes group=@Entity)
        (+contains ceo (reference @Person) [1..1])
        (+contains name=parcs (reference @Car) [0..*]))
)
```

Key points:

- `Entity` is a shared base with a `name` attribute.
- `Car` contains `passengers` (containment list).
- `CarParc` contains `cars` (containment list).
- `Person` refers to a `Car` (non‑containment reference).
- `CarCompany`:
  - contains a mandatory `ceo` (`[1..1]`).
  - contains a list of `parcs` (cars) via `[0..*]`.

This model is used by tests in `logoce.lmf.generator` and is a good template for new M2 models.

## 6. Generics

Generics in LMCore let you describe type‑parameterised concepts and have the generator produce Java generics for you.

- In LMCore’s own model (`model.lm`), look at:
  - `Group Feature`:
    ```lm
    (Group Feature
        (includes group=@Named)
        (Generic UnaryType)
        (Generic EffectiveType)
        ...)
    ```
  - `Definition Attribute` and `Definition Relation`, which `includes group=@Feature parameters=../generics.0,../generics.1`.
- In the generated Java (`logoce.lmf.model/src/main/generated/org/logoce/lmf/model/lang`):
  - `Feature`, `Attribute`, and `Relation` are generic types (`Feature<T, E>`, etc.).
  - The generic parameters in the `.lm` model map directly to these Java type parameters.

For your own M2 models:

- Add `(generics name=T)` or `(Generic T ...)` blocks to groups when you need a type parameter.
- Pass those generics down through `includes ... parameters=...` so that features know the unary/effective types.
- The generator will emit Java types that carry those generics; this is powerful for strongly‑typed APIs, but it’s easy to get wrong if parameter lists don’t line up, so it’s best to copy patterns from LMCore’s `Feature` / `Attribute` / `Relation` definitions.

## 7. Practical Tips

- Keep `.lm` files in a dedicated `src/.../model` folder and generate Java into a sibling `generated` folder for that source set.
- Start from CarCompany:
  - Copy the file, adjust `domain` and `name`.
  - Rename groups/definitions and tweak attributes/relations.
- Use `+contains` for ownership/containment relationships (where moving/removing elements should propagate container changes); use `+refers` for pure references.
- For primitive attributes, prefer `#LMCore@string`, `#LMCore@int`, etc., instead of re‑declaring units.

If you need to express something that doesn’t fit these patterns, open `model.lm` (LMCore itself) and look for a similar construct; almost everything in the language is modeled there.
