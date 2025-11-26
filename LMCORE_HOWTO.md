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

### Quick start for new `.lm` authors
- Start from a small working model (e.g. `logoce.lmf.generator/src/test/model/CarCompany.lm`) and tweak names/domain first.
- Use `Group` for abstract concepts, `Definition` for concrete ones. `includes group=@Base` sets inheritance.
- Generics live under the group/definition that owns them; pass them down via `includes ... (parameters ../../generics.N)`.
- Operations need explicit type arguments: use `returnTypeParameters` for the return type and `OperationParameter ... (parameters ...)` for arguments.
- Cross‑model refs: add `imports=Other.Domain.Model` on `(MetaModel ...)`, then reference with `#OtherModel@Type`.

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

    (+contains name=children @ChildType [0..*])
    (+refers name=owner @OwnerType [0..1])
```

- `datatype`:
  - `#LMCore@string` → primitive from LMCore (see `Primitive` enum in LMCore).
  - `@MyEnum` → enum defined in the same meta‑model *or* in an imported/upper meta‑model (e.g. in an M2 you can reference the LMCore `string` unit with `@string` instead of `#LMCore@string` when imports are set up accordingly).
  - `/units.X` or `/enums.X` → reference LMCore units/enums by index (used inside LMCore itself).
- `[min..max]`:
  - `[0..1]`, `[1..1]`, `[0..*]`, `[1..*]` are aliases that expand to `mandatory` / `many` flags.

**Relation targets (concept/parameters)**:

- Relations now point directly to a concept; the `reference ...` form is removed.
- For a plain target:

```lm
    (+contains name=children @SomeGroup [0..*])
    (+refers name=owner @SomeGroup [0..1])
```

- When the target is generic, pass parameters explicitly:

```lm
    (+refers name=typedChild @GenericContainer parameters=/generics.0)
```

- You can still add `group=@SomeGroup` if you need disambiguation, but `@SomeGroup` alone is enough in most cases.

Under the hood, the runtime populates `Relation.concept()` and `Relation.parameters()`; there is no `relation.reference()` anymore.

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
        (+contains name=passengers @Person [0..*]))

    (Definition CarParc
        (+contains cars [0..*] @Car))

    (Definition Person
        (includes group=@Entity)
        (+refers car [0..1] @Car))

    (Definition CarCompany
        (includes group=@Entity)
        (+contains ceo @Person [1..1])
        (+contains name=parcs @Car [0..*]))
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
- Relative paths: `../` climbs one level in the current block, `/groups.N/generics.M` jumps by index. When in doubt, mirror LMCore’s patterns (e.g. `Attribute`/`Relation` in `model.lm`).

**Common pitfall:** `Operation` now carries a `returnType` plus contained `returnTypeParameters` and `parameters` (each `OperationParameter` can itself have contained `parameters` for generics). When you need to refer to a group‑level generic from inside an operation parameter, you still have to walk back up with the right number of `../generics.N` hops. Example (pattern aligned with LMCore’s `model.lm`):

```lm
(Group NativeParameter
    (includes group=@Parameter)
    (Generic T)
    (Operation name=getNativeValue returnType=@JavaObject
        (returnTypeParameters ../../generics.0)
        (OperationParameter name=value type=@JavaObject
            (parameters wildcard=true wildcardBoundType=Extends type=../../generics.0))))
```

If you see linker errors about missing generics, double‑check the relative `parameters` paths inside both `returnTypeParameters` and `OperationParameter.parameters`.

## 7. Practical Tips

- Keep `.lm` files in a dedicated `src/.../model` folder and generate Java into a sibling `generated` folder for that source set.
- Start from CarCompany:
  - Copy the file, adjust `domain` and `name`.
  - Rename groups/definitions and tweak attributes/relations.
- Use `+contains` for ownership/containment relationships (where moving/removing elements should propagate container changes); use `+refers` for pure references.
- For primitive attributes, prefer `#LMCore@string`, `#LMCore@int`, etc., instead of re‑declaring units.
- For cross‑model references, add `imports=OtherModel` on your `(MetaModel ...)` and reference with `#OtherModel@Type`. All imported models must be provided to the generator together (either in one invocation or via a build that passes all relevant .lm files to the generator).
- Build wiring: put .lm files under `src/main/model`, apply the Gradle plugin `org.logoce.lmf.gradle-plugin`, and add `src/main/generated` to your `sourceSets`. The generator can process multiple .lm files at once; make sure upstream models are part of the `availableModels` input or imports will fail.

If you need to express something that doesn’t fit these patterns, open `model.lm` (LMCore itself) and look for a similar construct; almost everything in the language is modeled there.

## 8. JavaWrappers and Units

- `JavaWrapper` wraps an existing Java type to reuse it as a datatype. The wrapper name is a free alias; point to the type with `qualifiedClassName`:
  ```
  (JavaWrapper name=Vector3f qualifiedClassName=org.joml.Vector3f)
  (Group PositionParameter
      (+att name=position datatype=@Vector3f [0..1]))
  ```
  Optional `(contains serializer @Serializer)` lets you attach string conversion hooks (see LMCore’s `model.lm` for the structure).
- `Unit` is for custom scalars with matcher/primitive info (see LMCore’s own units for patterns). Parsing/formatting logic currently lives in runtime code; the `.lm` only declares the unit.

## 9. Migration cheatsheet (ecore → lm)

- EClass → Group/Definition; EAttribute → `+att`/`-att`; EReference → `+contains`/`+refers`; EEnum → `(Enum ...)`; custom EDataType → `(Unit ...)` or `(JavaWrapper ...)`.
- Root objects: `EObject` → `LMObject`; `EClass` → `Group`; containment stays with `+contains`; multiplicities map to `[0..1]`, `[1..1]`, `[0..*]`, `[1..*]`.
- Defaults: keep `defaultValue=`; names are case‑sensitive.
- Operations: `(Operation name=... (returnType @ReturnType) (parameters p (type @ParamType)))`; add `returnTypeParameters` and `parameters` blocks for generic type arguments as needed.

## 10. Tiny example

```lm
(MetaModel domain=test.model name=Hello
    (Group Entity
        (+att name=name datatype=#LMCore@string [1..1]))

    (Enum Kind Foo,Bar)

    (Definition Thing
        (includes group=@Entity)
        (+att name=kind datatype=@Kind [0..1])
        (+contains name=children @Thing [0..*]))
)
```
Generates `Thing`, `Kind`, builders, and feature constants; containment of `children` triggers structure notifications.
