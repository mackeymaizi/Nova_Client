# Nova Client — Minecraft 1.21.11 分支

这个分支是 `26.2` 分支的 **1.21.11 移植版**，代码结构、模块、命令完全一致，
只是把 Minecraft / Fabric 相关的 API 换成了 1.21.11 的写法。

| | |
|---|---|
| Minecraft | `1.21.11` |
| Fabric Loader | `0.19.5` |
| Fabric API | `0.141.6+1.21.11` |
| Fabric Loom | `1.18-SNAPSHOT`（`net.fabricmc.fabric-loom-remap`） |
| Java | 编译目标 21（Gradle 用 JDK 25 跑没问题） |
| Sodium（可选，编译期依赖） | `0.8.15-beta.1+mc1.21.11` |
| 产物 | `dist/Nova-Client-1.3.2-MC1.21.11.jar` |

---

## 构建

```bash
JAVA_HOME="C:/Program Files/Java/jdk-25.0.3" \
GRADLE_USER_HOME=D:/gradle-nova NOVA_BUILD_DIR=D:/nova-build-121 \
TMP=D:/nova-tmp TEMP=D:/nova-tmp \
GRADLE_OPTS="-Dorg.gradle.native.dir=D:/gradle-nova/native -Djava.io.tmpdir=D:/nova-tmp" \
./gradlew build --console=plain
```

> 第一次构建会下载并 remap 整个 1.21.11 客户端，大概要 15 分钟；之后每次 1 分钟左右。

---

## 构建系统上的坑（和 26.2 完全不一样）

| 项目 | 26.2 | 1.21.11 |
|---|---|---|
| Loom 插件 | `net.fabricmc.fabric-loom` | **`net.fabricmc.fabric-loom-remap`** |
| 映射 | 不需要（原版已不混淆） | 需要 `loom.officialMojangMappings()` |
| 依赖写法 | `implementation` | `modImplementation` |
| Access Widener 命名空间 | `official` | **`named`** |
| remapJar | 没有这个任务 | 有，且会校验每个 Mixin 目标 |

---

## 已完成的移植

### 1. 大部分 API 名字其实一样

1.21.11 已经提前拿到了 26.2 的不少新东西，所以移植量比想象中小很多：

- `net.minecraft.resources.Identifier`（**不是** `ResourceLocation`）✅ 直接可用
- `net.minecraft.client.renderer.rendertype.RenderType` / `RenderSetup` /
  `LayeringTransform` / `OutputTarget` ✅ 直接可用
- `com.mojang.blaze3d.pipeline.RenderPipeline` / `Builder` / `BlendFunction` ✅
- `Screen.keyPressed(KeyEvent)` / `mouseClicked(MouseButtonEvent, boolean)` ✅
- `MultiPlayerGameMode.startDestroyBlock` / `continueDestroyBlock` ✅
- `SoundEvents.GENERIC_EXPLODE` 仍然是 `Holder<SoundEvent>`（`.value()` 不用改）✅
- 实体包路径 `animal.golem.*` / `monster.zombie.*` / `npc.villager.*` 也都一样 ✅
  （只有 `Slime` 从 `monster.cubemob` 换回了 `monster`）

### 2. 需要改的（已改完）

| 26.2 写法 | 1.21.11 写法 | 涉及文件 |
|---|---|---|
| `MC.gui.screen()` | `MC.screen` | ClickGuiModule / InvWalk / Restock / AutoTotem / LocalPlayerMixin |
| `MC.gui.setScreen(s)` | `MC.setScreen(s)` | ClickGuiScreen / ContainerUtils / ClickGuiModule / LocalPlayerMixin |
| `MC.gui.hud.getChat()` | `MC.gui.getChat()` | ChatUtils / ChatScreenMixin |
| `MC.gui` = 屏幕管理器 | `MC.gui` = HUD，`Gui` 类就是 HUD | HudMixin 改 `@Mixin(Gui.class)` |
| `Hud` 类 | 不存在（HUD 就是 `Gui`） | HudMixin |
| `GuiGraphicsExtractor` | `GuiGraphics` | Render2DEvent / HudModule / ClickGuiScreen |
| `context.text(font,…)` | `context.drawString(font,…)` | HudModule / ClickGuiScreen |
| `context.outline(…)` | `context.renderOutline(…)` | ClickGuiScreen |
| `StagedVertexBuffer` | `MultiBufferSource.BufferSource` | render/BufferSource |
| `RenderPipeline.withDepthStencilState` | `withDepthTestFunction` | render/NovaRenderPipelines |
| `withPrimitiveTopology` + `withVertexBinding` | `withVertexFormat(fmt, Mode)` | render/NovaRenderPipelines |
| `MC.levelExtractor` | `MC.levelRenderer` | XRayModule |
| `gameRenderer.mainCamera()` | `gameRenderer.getMainCamera()` | render/RenderUtils |
| `ServerboundAttackPacket` | `ServerboundInteractPacket` + `PacketUtils.isAttack()` | Criticals / CrystalAura / MaceKill |
| `ContainerInput` + `handleContainerInput` | `ClickType` + `handleInventoryMouseClick` | MultiPlayerGameModeMixin |
| `ItemContainerContents.nonEmptyItemCopyStream()` | `nonEmptyItems()` | PeekCommand |
| `world.entity.monster.cubemob.Slime` | `world.entity.monster.Slime` | TrueSightModule |
| `net.minecraft.client.multiplayer.chat.GuiMessageTag` | `net.minecraft.client.GuiMessageTag` | ChatComponentMixin |
| `ChatComponent.addServerSystemMessage/addPlayerMessage` | `ChatComponent.addMessage` | ChatComponentMixin |
| `Entity.updateFluidInteraction()` | `Entity.updateFluidHeightAndDoFluidPushing()` | EntityMixin |
| `EntityRenderer.extractNameTags(…)` | `EntityRenderer.extractRenderState(…)` | nametags/EntityRendererMixin |
| `ItemInHandRenderer.submitArmWithItem` | `renderArmWithItem` | noshieldoverlay |
| `FogRenderer.setupFog` 返回 `FogData` | 返回 `Vector4f` | nofog/FogRendererMixin |
| `CommandSuggestions.updateUsageInfo(ParseResults, Suggestions)` | `updateUsageInfo()` | CommandSuggestionsMixin |
| `Gui.setScreen`（自动重连） | `Minecraft.setScreen` | autoreconnect/GuiMixin |
| `Renderer.extractRenderState` 系列 | `render` / `submitNameTag` 体系 | — |

### 3. 新增的 Access Widener 条目

1.21.11 里这些东西还是包私有的，所以在 `nova.accesswidener` 里放开了：

```
accessible method net/minecraft/client/renderer/rendertype/RenderType create (Ljava/lang/String;Lnet/minecraft/client/renderer/rendertype/RenderSetup;)Lnet/minecraft/client/renderer/rendertype/RenderType;
accessible field  net/minecraft/client/renderer/RenderPipelines LINES_SNIPPET Lcom/mojang/blaze3d/pipeline/RenderPipeline$Snippet;
accessible field  net/minecraft/client/renderer/RenderPipelines DEBUG_FILLED_SNIPPET Lcom/mojang/blaze3d/pipeline/RenderPipeline$Snippet;
accessible method net/minecraft/client/renderer/RenderPipelines register (Lcom/mojang/blaze3d/pipeline/RenderPipeline;)Lcom/mojang/blaze3d/pipeline/RenderPipeline;
```

---

## 已知差距 / 未验证项（重要）

1. **Sodium 集成已移植**（针对 Sodium `0.8.15-beta.1+mc1.21.11`）。
   `NovaMixinPlugin` + `nova.sodium.mixins.json`（`required:false`，按需加载）已恢复，
   `mixin/sodium` 下两个 Mixin 只在对面的 Sodium 存在时才应用。
   - `BlockRendererMixin`：在 Sodium 的 `BlockRenderer.renderModel` HEAD 取消掉白名单外的方块；
     `processQuad` / `bufferQuad` 上两个 `require = 0` 的注入负责「透明度模式」
   - `SodiumWorldRendererMixin`：`setupTerrain` 的第一个 boolean（local slot 4）置 false，
     关闭遮挡剔除 —— 不关的话墙后的矿会被整块剔除，X-Ray 就白开了
   - 构建时会打印 3 条 `Cannot remap state/pos/level ...` 警告：
     那是 remapper 看不到 Sodium 的父类 `AbstractBlockRenderContext` 导致的，
     **无害**（字段名不在映射表里，本来就不需要 remap；运行期 Mixin 会沿继承链解析到）
   - ⚠️ 这套是**照着 0.8.15 的字节码写的**，没有实机验证过

2. **Mixin 只做到「目标能解析」，没做实机验证。**
   `./gradlew build` 的 remapJar 阶段已经确认所有 Mixin 的目标方法/字段在
   1.21.11 里全部存在；21 个 `@At` 注入锚点也逐个对着映射 jar 的字节码核过了。
   但「锚点命中」不等于「语义正确」（比如 ordinal 选对了没有），仍需实机确认。

3. **`nova.mixins.json` 里 `injectors.defaultRequire` 是 `0`。**
   26.2 分支是 `1`（注入失败直接报错）。1.21.11 还没实测，
   设成 `0` 是为了**万一某个注入点对不上也不会导致游戏启动崩溃**，
   代价是那个功能会静默失效。
   如果发现某个功能没反应，把 `nova.mixins.json` 里的 `defaultRequire` 改成 `1`
   再启动，崩溃日志会直接点名是哪个 Mixin 出问题。

4. **渲染管线用的是自定义 shader**（`assets/nova/shaders/core/fogless_lines.*`）。
   1.21.11 的 pipeline/shader 加载机制和 26.2 未必完全一致，没实测。

5. **Fabric API 的 `CoreShaderRegistrationCallback` 没有接。**
   如果自定义 pipeline 起不来，ESP 类模块（PlayerEsp / ItemEsp / HoleEsp /
   Trajectories / Waypoints / Breadcrumbs / Search）会画不出来。

6. **还没在真机上跑过。** 上面的「已改完」全部只保证**编译通过 + remap 校验通过**。

---

## 和 26.2 分支的关系

- `26.2` 分支：完整、已打包、同样未经实机验证，但 Mixin 注入点对着 26.2 映射 jar 逐个核过。
- `1.21.11` 分支：本分支。功能集完全一致（77 个模块、28 条命令），
  差别只在 API 适配和上面列出的差距。

两个分支互不影响，各自 `gradlew build` 就能出包。
