# LMCore `.lm` How‑To (M2 Meta‑Models + M1 Instance Models)

This is a pragmatic guide for writing LMCore `.lm` meta‑models (M2) that work well with `logoce.lmf.core.api`, `logoce.lmf.core.loader`, and `logoce.lmf.core.generator`, with a short section on M1 instance models (data) written in the same syntax.

If you are new to this repo, read this file together with:

- `logoce.lmf.core.api/src/main/model/asset/LMCore.lm` – the LMCore meta‑model (M3), which defines the language itself.
- `logoce.lmf.core.generator/src/test/model/CarCompanyGenerator.lm` – a minimal but complete M2 example.
- The `lsp-design/` folder (optional, for tooling work) – contains copied `.lm` examples and LSP design notes.

The LMCore definition itself lives in `logoce.lmf.core.api/src/main/model/asset/LMCore.lm`. The CarCompany example model is in `logoce.lmf.core.generator/src/test/model/CarCompanyGenerator.lm`.

## 0. Orientation: what you are writing

- **M3 (LMCore)**  
  LMCore (`LMCore.lm`) is the meta‑model of the language: it defines what a `MetaModel`, `Group`, `Definition`, `Enum`, `Relation`, `Generic`, `Operation`, etc. are. The Java API in `org.logoce.lmf.core.lang.*` is generated from LMCore.

- **M2 (your `.lm` files)**  
  When you write a new `.lm` file (e.g. `CarCompany.lm`), you are defining an M2 meta‑model *in terms of* LMCore. From that M2 meta‑model:
  - `logoce.lmf.core.generator` generates Java types in your `domain` package.
  - The runtime (`LmLoader` / `ResourceUtil`) can load and link your `.lm` into `MetaModel` and related LMCore objects.

- **M1 (instance models / “data”)**  
  LM syntax is also used to describe **instances of your M2 meta‑models** (object graphs). The loader reads from an input stream (so the extension is technically irrelevant), but for IDE/editor support we keep files ending in `.lm` (for example `Scene.lm` or `Compositor.subpass.lm`).
  - If the root is `(MetaModel ...)` → you are loading an **M2** meta‑model.
  - If the root is a domain type (e.g. `(CarCompany ...)`) → you are loading an **M1** instance model.

- **Loading `.lm` programmatically**
  - For simple experiments:
    ```java
    var loader = new org.logoce.lmf.core.api.loader.LmLoader(ModelRegistry.empty());
    var doc    = loader.loadModel(Files.newInputStream(path));
    MetaModel mm = (MetaModel) doc.model();
    ```
  - The older `ResourceUtil.loadModel/loadModels` APIs now delegate to this loader; you can keep using them if you prefer.

## 0.1 M1 models (instance models) in practice

An M1 model is “just objects”, but the loader still needs to know **which meta‑model (M2) to use** to interpret types, features, enums, containment, etc.

### 0.1.1 The “header” is just the `#LMCore@Model` concept

LMCore defines a `Model` group (M3) that provides:

- `name` (via `#LMCore@Named`)
- `domain` (required)
- `imports` (optional, list)
- `metamodels` (optional, list)

In practice:

- In **M2** meta‑models, `imports=` is what enables cross‑model type references like `#OtherModel@Type`.
- In **M1** instance models, `metamodels=` is what tells the loader which meta‑model packages to use to interpret the instance.

In most projects, the root M1 type includes `#LMCore@Model` (directly or through inheritance). That’s why M1 roots often start with:

```lm
(SomeRootType domain=my.domain name=MyInstance metamodels=my.domain.MyMetaModel
    ;; object graph...
)
```

`metamodels=` is a list of **meta‑model identifiers** in the form `domain.name` (for example, `test.model.CarCompany` matches `(MetaModel domain=test.model name=CarCompany ...)`). If your instance uses types coming from multiple meta‑models, list them all:

```lm
metamodels=my.domain.Process,my.domain.Resource
```

**Common confusion:** `domain=` means different things depending on whether you write M2 or M1:

- On an M2 `(MetaModel ...)`, `domain` is the Java package for generated code.
- On an M1 root that implements `#LMCore@Model`, `domain` is just an identifier for the instance model itself (it does not have to match the meta‑model domain).

### 0.1.2 Minimal M1 example (based on the repo tests)

Meta‑model (M2): `logoce.lmf.core.api/src/test/model/CarCompany.lm`  
Instance model (M1): `logoce.lmf.core.api/src/test/model/Peugeot.lm`

```lm
(CarCompany domain=test.model name=PeugeotCompany metamodels=test.model.CarCompany
    (ceo name=Macron)
    (CarParc
        (Car name=peugeot1 brand=Peugeot)))
```

Points that are easy to miss at first:

- `(ceo ...)` uses the **feature name** `ceo` (a `+contains` relation on `CarCompany`) to create the contained `Person` instance.
- `(CarParc ...)` and `(Car ...)` use **type names**. The loader attaches them through the unique compatible containment feature:
  - `CarCompany` contains `CarParc` through `parcs`
  - `CarParc` contains `Car` through `cars`
- Enum values are written as **literal names** (`brand=Peugeot`).

### 0.1.3 References inside an M1 model (`@name`)

If a relation is a non‑containment reference (`+refers`), you usually point to an existing object using `@...`:

```lm
(CarCompany domain=test.model name=PeugeotCompanyWithReference metamodels=test.model.CarCompany
    (ceo name=Macron car=@peugeot1)
    (CarParc
        (Car name=peugeot1 brand=Peugeot)))
```

This works because the target (`Car`) is `#LMCore@Named`, so it has a stable `name` used for resolution.

### 0.1.4 Containment: pick one style (feature *or* type), don’t double‑wrap

For a containment relation, you can usually describe children in two equivalent styles:

- **By feature name** (explicit):
  ```lm
  (CarParc
      (cars name=peugeot1 brand=Peugeot))
  ```
- **By concrete type name** (implicit containment resolution):
  ```lm
  (CarParc
      (Car name=peugeot1 brand=Peugeot))
  ```

Avoid mixing both for the same step, for example don’t write a “feature wrapper” that contains a second “typed child” for the same object. This often leads to linker errors like:

> “Cannot find containment relation from parent … to child …”

When you hit this, flatten the structure: use either the feature node *or* the typed node, but not both.

If you rely on implicit containment resolution (typed nodes), and a parent has multiple containment features that could accept the same child type, resolution becomes ambiguous. In that case, prefer the explicit feature form.

### 0.1.5 Mandatory features still need values at load time

During linking, objects are built using the generated builders. If a feature is mandatory in the meta‑model (`[1..1]`), the builder may require a value immediately. If you plan to overwrite something programmatically later, you may still need a placeholder value in the M1 file so the model can load.

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
- Start from a small working model (e.g. `logoce.lmf.core.generator/src/test/model/CarCompanyGenerator.lm`) and tweak `domain` and `name` first.
- Use `Group` for abstract concepts, `Definition` for concrete ones. `includes group=@Base` sets inheritance.
- Keep one logical domain per file: one `MetaModel` per `.lm`.
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
- List separators are whitespace-tolerant: `A,B,C` is equivalent to `A , B , C` and can be split across lines.

### Enum literal attributes

Enums can also declare **literal attributes** (similar to Java enums with extra fields) via `EnumAttribute` blocks.

```lm
    (Enum NameAndId
        A : 1,
        B : 2,
        C : 3
        (EnumAttribute id #LMCore@int))
```

- Each `EnumAttribute` declares a literal field:
  - attribute name = Java accessor name (LMF style, e.g. `int id()`).
  - type = the referenced `Unit` (currently intended for primitive units like `#LMCore@int`, `#LMCore@float`, `#LMCore@string`, ...).
- Literal values are **positional**: they map to attributes by the **order** of the `EnumAttribute` declarations.
- Within a literal, parts are separated by `:`; literals are separated by `,` (whitespace around separators is ignored).
- If an enum declares no `EnumAttribute`, literals are declared by name only (as before).
- If an enum has attributes, every literal must provide a value for each attribute.

Multiple attributes:

```lm
    (Enum CodeAndLabel
        A : 1 : "Hello world",
        B : 2 : "foo:bar",
        C : 3 : "hi,there"
        (EnumAttribute id #LMCore@int)
        (EnumAttribute label #LMCore@string))
```

- Use quotes for string values when you need spaces, `:` or `,`.
- References to enum literals (for example attribute `defaultValue`) always use the **base literal name** only: `defaultValue="A"` (never `A:1`).

Units (custom scalar types) are more advanced and mostly used in LMCore itself:

```lm
    (Enum name=Primitive boolean,int,long,float,double,string)
    (Unit name=string)
```

For most domain models you can stick to LMCore’s built‑in primitives (`#LMCore@string`, `#LMCore@int`, etc.) and your own enums.

## 5. Example: CarCompany

`logoce.lmf.core.generator/src/test/model/CarCompanyGenerator.lm` shows a minimal but complete model:

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

This model is used by tests in `logoce.lmf.core.generator` and is a good template for new M2 models.

## 6. Generics

Generics in LMCore let you describe type‑parameterised concepts and have the generator produce Java generics for you.

- In LMCore’s own model (`LMCore.lm`), look at:
  - `Group Feature`:
    ```lm
    (Group Feature
        (includes group=@Named)
        (Generic UnaryType)
        (Generic EffectiveType)
        ...)
    ```
  - `Definition Attribute` and `Definition Relation`, which `includes group=@Feature parameters=../generics.0,../generics.1`.
- In the generated Java (`logoce.lmf.core.api/src/main/generated/org/logoce/lmf/core/lang`):
  - `Feature`, `Attribute`, and `Relation` are generic types (`Feature<T, E>`, etc.).
  - The generic parameters in the `.lm` model map directly to these Java type parameters.

For your own M2 models:

- Add `(generics name=T)` or `(Generic T ...)` blocks to groups when you need a type parameter.
- Pass those generics down through `includes ... parameters=...` so that features know the unary/effective types.
- The generator will emit Java types that carry those generics; this is powerful for strongly‑typed APIs, but it’s easy to get wrong if parameter lists don’t line up, so it’s best to copy patterns from LMCore’s `Feature` / `Attribute` / `Relation` definitions.
- Relative paths: `../` climbs one level in the current block, `/groups.N/generics.M` jumps by index. When in doubt, mirror LMCore’s patterns (e.g. `Attribute`/`Relation` in `LMCore.lm`).

### 6.1 Contextual name paths (`^`)

To make some references less brittle than raw indices, LMCore adds a small “contextual name” shortcut:

- `^name` means “look for an element called `name` starting from my current context, walking up through parents and each parent’s immediate children”.
- This is mostly used for generics and other locally‑scoped names where `../../generics.0` would be noisy or fragile.

Typical LMCore pattern for generics:

```lm
(Group Concept
    (includes @Type
        (parameters ^T))
    (Generic name=T))
```

Here `^T` resolves to the nearest `Generic` named `T` in the surrounding context.  
The same `^name` form can also be used for instance‑level references (for example in M1 models) when you want to prefer the “closest” matching object instead of searching the whole model with `@name`.

**Common pitfall:** `Operation` now carries a `returnType` plus contained `returnTypeParameters` and `parameters` (each `OperationParameter` can itself have contained `parameters` for generics). When you need to refer to a group‑level generic from inside an operation parameter, you still have to walk back up with the right number of `../generics.N` hops. Example (pattern aligned with LMCore’s `LMCore.lm`):

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

### 7.1 Folder layout and Gradle

- Keep `.lm` files in a dedicated `src/.../model` folder and generate Java into a sibling `generated` folder for that source set.
- Typical layout for a new project:
  - `src/main/model/MyModel.lm`
  - `src/main/generated/` – generator output (add this as a Java source directory).
- Apply the Gradle plugin `org.logoce.lmf.gradle-plugin` and configure:
  - The `.lm` source directory.
  - The generated sources directory.

The generator can process multiple `.lm` files at once; make sure upstream models are part of the `availableModels` input or imports will fail.

### 7.2 Modelling guidelines

- Start from CarCompany:
  - Copy `logoce.lmf.core.generator/src/test/model/CarCompanyGenerator.lm`.
  - Adjust `domain` and `name`.
  - Rename groups/definitions and tweak attributes/relations.
- Use `+contains` for ownership/containment relationships (where moving/removing elements should propagate container changes); use `+refers` for pure references.
- For primitive attributes, prefer `#LMCore@string`, `#LMCore@int`, etc., instead of re‑declaring units.
- For cross‑model references, add `imports=Other.Domain.Model` on your `(MetaModel ...)` and reference with `#OtherModel@Type`. All imported models must be provided to the generator together (either in a single run, or wired through Gradle so that all relevant `.lm` files are loaded into the `ModelRegistry`).

If you need to express something that doesn’t fit these patterns, open `LMCore.lm` (LMCore itself) and look for a similar construct; almost everything in the language is modeled there.

### 7.3 Loading models at runtime

For small tools, tests, or REPL‑style experiments, the easiest entry point is the new loader:

```java
	import org.logoce.lmf.core.lang.MetaModel;
	import org.logoce.lmf.core.api.loader.LmLoader;
	import org.logoce.lmf.core.api.loader.model.LmDocument;
	import org.logoce.lmf.core.api.model.ModelRegistry;

var loader = new LmLoader(ModelRegistry.empty());
try (var in = Files.newInputStream(Path.of("src/main/model/MyModel.lm"))) {
    LmDocument doc = loader.loadModel(in);
    MetaModel mm = (MetaModel) doc.model();
    // use mm.groups(), mm.enums(), ...
}
```

`LmDocument` carries:

- `model()` – the linked `Model` (or `null` on failure).
- `diagnostics()` – a list of `LmDiagnostic` entries (syntax and linking).
- `roots()` / `source()` – the underlying parse trees and text.
- `linkTrees()` – the linker trees (`LinkNode<?, PNode>`) that connect `PNode`s to LMCore objects (useful for advanced tools like an LSP).

Use this for detailed diagnostics while editing or validating models; the legacy `ResourceUtil.loadModelWithDiagnostics(...)` façade has been removed.

## 8. JavaWrappers and Units

- `JavaWrapper` wraps an existing Java type to reuse it as a datatype. The wrapper name is a free alias; point to the type with `qualifiedClassName`:
  ```
  (JavaWrapper name=Vector3f qualifiedClassName=org.joml.Vector3f)
  (Group PositionParameter
      (+att name=position datatype=@Vector3f [0..1]))
  ```
  Optional `(contains serializer @Serializer)` lets you attach string conversion hooks (see LMCore’s `LMCore.lm` for the structure).
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

## 11. Where to look in the code

To relate the `.lm` syntax and this how‑to to the Java implementation:

- **LMCore meta‑model (M3)**  
  `logoce.lmf.core.api/src/main/model/asset/LMCore.lm` and the generated API under `logoce.lmf.core.api/src/main/generated/org/logoce/lmf/core/lang`.

- **Loading and linking `.lm`**  
 - High‑level loader: `org.logoce.lmf.core.api.loader.LmLoader` (recommended entry point for tools, tests, and the LSP).
  - Interpretation and linking internals:
    - `org.logoce.lmf.core.api.loader.parsing.*` – parsing entry points (`LmTreeReader`, headers, etc.).
    - `org.logoce.lmf.core.api.text.syntax.*` – parse trees (`PNode`, `PToken`).
    - `org.logoce.lmf.core.loader.internal.interpretation.*` – `PGroup`, `PFeature`, alias expansion.
    - `org.logoce.lmf.core.api.loader.linking.*` – feature resolution, reference handling, and link tree construction.

If you intend to build advanced tooling (e.g. an LSP server), also check the `lsp-design/` folder at the repo root for additional notes and example models.
