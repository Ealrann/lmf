This file is the **single source of truth** for how the LMF IntelliJ plugin is set up and how it talks to the LM LSP server. It replaces older notes that used to live at the repo root.

It is split into:

1. What the LM **LSP server** must expose for IntelliJ.
2. How the **IntelliJ plugin** is structured in this module (Gradle, plugin.xml, LSP wiring).

You are on IntelliJ IDEA **2025.3**, using the unified JetBrains distribution. In this distribution:

- The **LSP API is available to all users** (no Ultimate subscription required), but only in the unified IntelliJ IDEA product, not in the old open‑source “Community build” from GitHub.  
- Plugins using `com.intellij.modules.lsp` are supported in this unified build and other commercial IDEs; they will not load in pure OSS/Android Studio builds.

---

## 1. LM LSP server – required capabilities for IntelliJ

IntelliJ’s LSP support is a **pure LSP client**: it speaks the standard Microsoft protocol over stdio or socket; there are no IntelliJ‑specific messages. For IntelliJ 2025.3, the “Supported Features” table in the docs lists diagnostics, completion, definition, references, rename, code actions, formatting, semantic tokens, document symbols, workspace/symbol, documentHighlight, signatureHelp, etc.

To get a good LM editing experience in IntelliJ, the **LM LSP server** should:

### 1.1. Core lifecycle + sync

- Implement:
  - `initialize`, `initialized`, `shutdown`, `exit`
  - `textDocument/didOpen`, `textDocument/didChange`, `textDocument/didClose` with incremental sync.
- Advertise `textDocumentSync = Incremental` in `InitializeResult.capabilities`.

### 1.2. Core language features

Implement and advertise at least the following methods:

- **Diagnostics**  
  - `textDocument/publishDiagnostics` (push diagnostics).  
  - Optionally `textDocument/diagnostic` (pull diagnostics); not required initially.

- **Navigation & structure**  
  - `textDocument/documentSymbol` → file structure / outline / breadcrumbs.  
  - `textDocument/definition` → go to definition.  
  - `textDocument/references` → find usages.

- **Editing aid**  
  - `textDocument/completion` → basic completion.  
  - `textDocument/hover` → meta‑model / type info.  
  - `textDocument/rename` + `workspace/applyEdit` → rename (cross‑file aware).  
  - `textDocument/codeAction` → quick‑fixes / refactorings.

- **Optional but recommended**  
  - `textDocument/formatting` → LM formatter.  
  - `textDocument/semanticTokens/full` → semantic highlighting.  
  - `workspace/symbol` → “Go to symbol in workspace”.  
  - `textDocument/documentHighlight` → highlight usages in file.  
  - `textDocument/signatureHelp` → parameter info.  
  - `textDocument/inlayHint` → inlay hints (e.g. inferred types).

### 1.3. Transport & custom methods

- Use **stdio** for now. IntelliJ supports stdio since 2023.2 and treats it as the default channel.
- If the server has LM‑specific custom requests/notifications (e.g. `lm/getModelGraph`), IntelliJ will tunnel them normally over JSON‑RPC. Extra IntelliJ‑side code is only needed if we want special UI for these customs.

### 1.4. Important note for this repo

In this repo, the LSP server currently sets **only** `textDocumentSync` in `ServerCapabilities`. To make IntelliJ use def/ref/hover/rename/documentSymbol, `LmLanguageServer.initialize(...)` must be updated to advertise the implemented features, e.g.:

```java
final var capabilities = new ServerCapabilities();
capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);

capabilities.setCompletionProvider(new CompletionOptions());
capabilities.setDefinitionProvider(true);
capabilities.setReferencesProvider(true);
capabilities.setHoverProvider(true);
capabilities.setDocumentSymbolProvider(true);
capabilities.setRenameProvider(true);
```

Those methods are already implemented in `LmTextDocumentService` / `LmLanguageServer`; this is just the capabilities handshake.

---

## 2. IntelliJ plugin – structure in `logoce.lmf.intellij`

This module contains the IntelliJ plugin that wires `.lm` files to the LM LSP server. Its responsibilities:

1. Define the `.lm` file type and language.
2. Depend on the IntelliJ LSP API.
3. Register an LSP server support provider that starts the LM LSP server when `.lm` files are opened.

### 2.1. Gradle setup (IntelliJ Platform Gradle Plugin 2.x)

`logoce.lmf.intellij/build.gradle`:

```groovy
plugins {
    id 'java'
    id 'org.jetbrains.intellij.platform' version '2.10.5'
}

group = 'org.logoce.lmf'
version = '0.0.1'

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    intellijPlatform {
        // Use a recent IntelliJ IDEA platform with LSP enabled.
        // Currently we depend on 2025.2.4 (latest published); this is
        // compatible with a locally installed 2025.3 IDE.
        intellijIdea('2025.2.4')
    }
}
```

If JetBrains publishes IntelliJ IDEA `2025.3` artifacts to the plugin repos, you can bump to `intellijIdea("2025.3")` without changing the rest of the setup.

### 2.2. plugin.xml – platform & LSP dependencies, file type

`logoce.lmf.intellij/src/main/resources/META-INF/plugin.xml`:

```xml
<idea-plugin>
    <id>org.logoce.lmf.intellij</id>
    <name>LMF .lm Support (LSP)</name>
    <version>0.0.1</version>
    <vendor email="dev@logoce.org" url="https://github.com/">LMF</vendor>

    <description>
        LSP-based support for LMF .lm files. The plugin registers the .lm file type
        and uses the Language Server Protocol API to start the external LMF LSP server.
    </description>

    <!-- Platform + LSP API (unified IntelliJ IDEA build, 2025.2+). -->
    <depends>com.intellij.modules.platform</depends>
    <depends>com.intellij.modules.lsp</depends>

    <extensions defaultExtensionNs="com.intellij">
        <fileType name="LMF"
                  language="LMF"
                  implementationClass="org.logoce.lmf.intellij.LmFileType"
                  fieldName="INSTANCE"
                  extensions="lm"/>
    </extensions>
</idea-plugin>
```

### 2.3. lsp.xml – LSP support provider registration

`logoce.lmf.intellij/src/main/resources/META-INF/lsp.xml`:

```xml
<idea-plugin>
    <extensions defaultExtensionNs="com.intellij">
        <platform.lsp.serverSupportProvider
                implementation="org.logoce.lmf.intellij.LmLspServerSupportProvider"/>
    </extensions>
</idea-plugin>
```

LSP‑specific registration is kept here so the main `plugin.xml` stays focused on declarations and file types.

### 2.4. `.lm` language and file type

`LmLanguage` (`org.logoce.lmf.intellij.LmLanguage`):

```java
public final class LmLanguage extends Language {
    public static final LmLanguage INSTANCE = new LmLanguage();
    private LmLanguage() { super("LMF"); }
}
```

`LmFileType` (`org.logoce.lmf.intellij.LmFileType`):

```java
public final class LmFileType extends LanguageFileType {
    public static final LmFileType INSTANCE = new LmFileType();

    private LmFileType() {
        super(LmLanguage.INSTANCE);
    }

    @Override public String getName()        { return "LMF"; }
    @Override public String getDescription() { return "LMF meta-model file"; }
    @Override public String getDefaultExtension() { return "lm"; }
    @Override public Icon getIcon()          { return null; }
}
```

This gives `.lm` its own language and file type while delegating semantics to the LSP server.

### 2.5. LSP support provider + descriptor

`LmLspServerSupportProvider` (`org.logoce.lmf.intellij.LmLspServerSupportProvider`):

```java
public final class LmLspServerSupportProvider implements LspServerSupportProvider {

    @Override
    public void fileOpened(@NotNull Project project,
                           @NotNull VirtualFile file,
                           @NotNull LspServerStarter serverStarter) {
        if (!"lm".equalsIgnoreCase(file.getExtension())) {
            return;
        }
        // Lazily start the LM LSP server when the first .lm file is opened
        serverStarter.ensureServerStarted(new LmLspServerDescriptor(project));
    }

    private static final class LmLspServerDescriptor extends ProjectWideLspServerDescriptor {

        private LmLspServerDescriptor(@NotNull Project project) {
            super(project, "LMF LSP");
        }

        @Override
        public boolean isSupportedFile(@NotNull VirtualFile file) {
            return "lm".equalsIgnoreCase(file.getExtension());
        }

        @Override
        public @NotNull GeneralCommandLine createCommandLine() {
            // Assume the LM LSP launcher is on PATH as 'logoce.lmf.lsp'
            // (from :logoce.lmf.lsp:installDist).
            return new GeneralCommandLine(List.of("logoce.lmf.lsp"));
        }
    }
}
```

This matches JetBrains’ recommended pattern:

- The provider is invoked when a file is opened.
- It checks the extension, then starts a **project‑wide** LSP server descriptor on first use.
- The descriptor restricts the server to `.lm` files and launches the LSP server via stdio.

#### Launcher expectation

The plugin expects the LM LSP server to be runnable as `logoce.lmf.lsp` from `PATH`. This is the script created by:

```bash
./gradlew :logoce.lmf.lsp:installDist
```

Under the effective `buildDir` (your Gradle init may redirect this to `/dev/shm/...`), the launcher lives at:

```text
<buildDir>/install/logoce.lmf.lsp/bin/logoce.lmf.lsp
```

For development, you can either:

- Add that `bin` directory to `PATH`, or  
- Symlink the script into a directory on `PATH`, e.g.:

```bash
ln -s <buildDir>/install/logoce.lmf.lsp/bin/logoce.lmf.lsp ~/.local/bin/logoce.lmf.lsp
```

In the future we can add a plugin setting or system property to make this path configurable.

---

## 3. Quick checklist for future maintainers

When updating IntelliJ / Gradle / the plugin:

1. **Gradle + IntelliJ Platform Plugin**
   - Keep using `id("org.jetbrains.intellij.platform")` 2.x and Gradle ≥ 8.5.
   - Update `intellijIdea("...")` to a recent release once artifacts are published; re‑run `:logoce.lmf.intellij:buildPlugin`.

2. **LSP API dependency**
   - Ensure `plugin.xml` keeps:
     - `<depends>com.intellij.modules.platform</depends>`
     - `<depends>com.intellij.modules.lsp</depends>`
   - This guarantees the plugin only loads where the LSP API is available (unified IntelliJ IDEA, commercial IDEs).

3. **LSP server capabilities**
   - When you add/remove LSP features on the server, update `LmLanguageServer.initialize(...)` (`logoce.lmf.lsp`) to set the corresponding flags in `ServerCapabilities`.

4. **Launcher wiring**
   - If the LM LSP launcher name or location changes, update:
     - `LmLspServerDescriptor.createCommandLine()` (plugin side).
     - The dev helper scripts (`run-lsp-server.sh`, `build-intellij-plugin.sh`) that discover `buildDir` and start the server.

