# Nova Client 版本记录

所有版本号格式：`<主版本>.<次版本>.<修订>-MC<游戏版本>`

> `26.2` 和 `1.21.11` 两个分支各自维护自己的版本号，下面按分支分开记录。
> 1.21.11 的 API 适配细节见 `PORTING.md`。

## 1.21.11 分支

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.3.0-MC1.21.11 | 2026-10-10 | 从 26.2 分支移植到 Minecraft 1.21.11（Loom `fabric-loom-remap` + 官方映射 + Java 21）。功能集与 26.2 一致：77 个模块、28 条命令。详见 `PORTING.md` |
| 1.3.1-MC1.21.11 | 2026-10-10 | **修复启动崩溃**：`MultiPlayerGameModeMixin.nova_onContinueDestroyBlock` 注入的是返回 `boolean` 的 `continueDestroyBlock`，回调却写成了 `CallbackInfo`，Mixin 要求 `CallbackInfoReturnable<Boolean>`。任何触及 `MultiPlayerGameMode` 的模组（如 Item Interactions / Easy Shulker Boxes）都会触发 `InvalidInjectionException`，导致游戏在初始化阶段直接崩溃 |
| 1.3.2-MC1.21.11 | 2026-10-10 | **补上 Sodium 集成（X-Ray 终于能在装 Sodium 时用了）**：针对 **Sodium 0.8.15-beta.1+mc1.21.11** 移植了 `BlockRendererMixin`（隐藏白名单外方块 / 透明度模式）+ `SodiumWorldRendererMixin`（X-Ray 开启时关闭遮挡剔除，否则墙后的矿会被整块剔除），恢复 `NovaMixinPlugin` + `nova.sodium.mixins.json`（按需加载）。**修复 2 个坏的注入点**：`Gui.screen()` / `Gui.overlay()` 在 1.21.11 不存在，分别改为 `Minecraft.screen` 字段和 `Minecraft.handleKeybinds()` 调用（影响 PortalGui 和 HandleInputEvent 钩子）。**全量审计**：把 21 个 `@At` 注入锚点逐个对着 1.21.11 映射 jar 的字节码核了一遍，全部命中 |
| 1.3.3-MC1.21.11 | 2026-10-10 | 新增 **PacketFly 模块**（Meteor Rejects 移植，发包飞行；水平/垂直速度上限 100000 格/秒，另可配 packets-per-tick / teleport / invalid-packet / anti-kick）。**修复 X-Ray 在 Sodium 下的两个静默失效**（都是 `require = 0` 所以一直没报错）：<br>① `BlockRendererMixin` 的两处 `@At` 锚点 owner 应为 `MutableQuadViewImpl`，写成 `QuadViewImpl` 永远匹配不上（`getRenderType` / `baseColor` 声明在父类，javac 按接收者静态类型发射 `invokevirtual`）→ 透明度模式失效；<br>② `SodiumWorldRendererMixin` 关遮挡剔除的方式**对 Sodium 0.8.15 无效**：0.8.15 把 `setupTerrain` 的第一个 boolean 透传给 `RenderSectionManager.shouldUseOcclusionCulling(Camera, boolean)`，而该方法在 `else` 分支返回 `Minecraft.smartCull`（默认 `true`）—— 所以置 `false` 只是跳过了「相机在实心方块内就不剔除」那个特例，**遮挡剔除照旧开着**，墙后的矿还是被整块剔除。改为注入 `RenderSectionManager.shouldUseOcclusionCulling` 的返回值强制 `false`（`SodiumWorldRendererMixin` → `RenderSectionManagerMixin`）。注意 0.9.2（26.2 分支）语义不同，那边透传即开关，原写法是对的。<br>另修正 `NovaClient.MC_VERSION` 误写成 `26.2`。模块总数 77 → **78** |
| 1.3.4-MC1.21.11 | 2026-10-11 | **ClickGUI 自适应布局**：分类面板改为按屏宽自动计算的响应式网格 —— 列数随宽度自动增减（面板宽 120 → 最小 84 自适应），保证**任何窗口尺寸下所有分类标题都留在屏内**、不再像以前那样横向排开把 World 挤出屏幕；窗口小到放不下时**全部折叠**只留标题栏（底部有提示文字），设置里新增 `Force collapse` 可关掉这个行为。**模块切换聊天反馈**：仿 Meteor，点击切换模块时往聊天栏打一条 `Toggled <模块名> on/off.`（绿色 on / 红色 off），设置里新增 `Chat feedback` 开关（`Module.sendToggledMsg()` 也顺带重写成这个格式，全局 + 单模块两级可控）。面板滚动范围改用实际可见高度计算，不再固定 `height - 60` |
| 1.3.5-MC1.21.11 | 2026-10-11 | **ClickGUI 默认绑定右 Shift**（`ClickGuiModule.DEFAULT_KEYBIND = GLFW_KEY_RIGHT_SHIFT`，和 Meteor 一致），仍可用 `.bind` 改键。**按 `.` 直接开命令输入**：仿原版 `/` 的行为，在没有打开任何界面时按 `.` 会打开聊天栏并预填 `.`，光标停在后面等输入；新增 `KeyboardHandlerMixin` 的 `nova$commandShortcut` 负责这段逻辑，`KeyPressEvent` 照旧投递。**顺手修**：`ModuleManager.onKeyPress` 之前不检查当前界面，导致在聊天/背包里按模块绑定键也会触发模块开关 —— 现在除 Nova 自己的 ClickGUI 外，任何界面打开时都忽略模块按键 |
| 1.3.6-MC1.21.11 | 2026-10-11 | **模块可拓展性：逐模块对齐上游原始设置**（全部 78 个模块对照其移植出处 Wurst / Meteor / Meteor Rejects / LiquidBounce 补齐）。新增/扩展共 **11 个模块**、**+31 个设置**：<br>• **Flight**（Wurst，3→8）：`Allow unsafe vertical speed`、`Scroll to change speed`、`Show speed in HackList`、`Anti-Kick Interval`、`Anti-Kick Distance`（垂直速度改走 `clamp(horizontal×vertical, 0.05, unsafe?10:3.95)`，与原版一致）<br>• **AutoEat**（Wurst，4→8）：`Eat while walking`、`Allow hunger effect`、`Allow poison effect`、`Allow chorus fruit`（读 `DataComponents.CONSUMABLE` 的 `onConsumeEffects()` 过滤）<br>• **AimAssist**（Wurst，6→8）：`Ignore mouse input`、`Aim while blocking`<br>• **AutoArmor**（Wurst，2→4）：`Use enchantments`、`Swap while moving`<br>• **AntiAfk**（Wurst，4→6）：`Wait time random`、`Show wait time`<br>• **VeinMiner**（Wurst，5→6）：`Swing hand`<br>• **HoleESP**（Meteor，6→9）：`Vertical radius`、`Min height`、`Ignore own hole`<br>• **Chams**（Meteor，7→8）：`Ignore self`<br>• **Velocity**（Meteor，5→8）：`Blocks`、`Sinking`、`Fishing`<br>• **XRay**（Meteor，2→3）：`Fluid opacity`<br>• **PacketLogger**（Meteor，2→6）：`Show timestamp`、`Show packet data`、`Show total count`（+ 运行计数）<br>• **BetterChat**（Meteor，4→9）：`Timestamps`、`Show seconds`、`Coords protection`（正则屏蔽坐标）、`Regex filter` + `Filter regex`<br>• **KillAura**（Meteor，16→22）：`Priority`、`Attack when holding`、`Auto switch`、`Swap back`、`Pause on lag`、`Swing`<br>• **CrystalAura**（Meteor，11→17）：`Walls range`、`Predict movement`、`Ignore nakeds`、`Anti weakness`、`Pause on use`、`Pause on mine`<br>• **AnchorAura**（Meteor，12→19）：`Walls range`、`Air place`、`Pause on use`、`Pause on mine`、`Pause on CA`、`Swing`、`Target priority`<br>• **LongJump**（Meteor，4→7）：`Only on ground`、`Auto disable`、`Disable on rubberband`<br>• **PacketFly**（Rejects，9→12）：`Set yaw`、`Set move`、`Set ID`<br>（普查脚本 `D:/nova-tmp/settings_diff.py`；18 个纯开关模块在上游本就无设置，无需改动） |
| 1.3.7-MC1.21.11 | 2026-10-11 | **ClickGUI 里可直接改按键绑定（+重置）**：展开模块后，设置列表顶部多出一行 **`Keybind`**，点它即进入监听状态（行内显示 `<press key>`、底部有提示条），按下任意键完成绑定；按 **Esc 或 Delete** 清空绑定（显示 `NONE`）。键名用稳定的英文名（新增 `KeyNames` 表，不依赖键盘布局，含字母/数字/功能键/小键盘/修饰键等）。改键后立即写入配置。**顺带修两个老问题**：① ClickGUI 里改设置/改按键现在会**立即存盘**（以前只有在 GUI 里改完再手动 `.reload save` 才保存，改了等于白改）；② 右键展开模块时不再显示上一次残留的设置行（现在右键和中键展开都会重建设置列表）。为避免「选定绑定时误触发模块」，绑定监听期间会忽略模块绑定键 |
| 1.3.8-MC1.21.11 | 2026-10-11 | **ClickGUI 重做：改为彗星（Meteor）式自由拖拽窗口** —— 修掉面板互相叠加、点击穿透到后层面板的严重问题。
• 每个分类现在是**独立可拖拽的窗口**，位置与折叠状态存到 `config/clickgui.json`，下次打开原样恢复（拖动后松手即保存）。
• 默认布局按行排布、超出右边界自动换行，同排窗口统一高度，**行与行之间不再重叠**（内容超出时窗口内部滚动，右侧有滚动条）。
• **点击顺序修正**：按 Z 序从最上层往下判定，并且点中面板会**置顶** —— 之前面板重叠时点击总是打到后面那个，现在点谁谁在上面。
• **字号/行高缩小**（行高 14→11、标题栏 16→13、面板宽 120→104），修掉字体过大导致文字挤在一起的问题。
• 窗口整体夹取在屏幕内，改分辨率后不会跑到屏幕外够不着。
• 折叠/展开开关从右键标题栏改为**右键（或中键）切换**，左键拖标题栏移动。
（`Config` 新增 `loadClickGuiWindows()` / `saveClickGuiWindows()`） |
| 1.3.9-MC1.21.11 | 2026-10-11 | **修复 X-Ray 装了 Sodium 后完全没效果**（`26.2` 分支同步修复，编号 1.3.8）。真正原因不是锚点写错，而是 **`BlockRendererMixin` 的 `@Shadow` 注入失败导致整个 Mixin 被丢弃**：`state` / `pos` / `level` 三个字段其实声明在**父类** `AbstractBlockRenderContext` 上，而 Mixin 对「继承来的字段」只能靠 refMap 解析 —— 本项目用 Loom 直接把注解串改写成 intermediary、**根本不生成 refMap**，于是 apply 阶段直接抛<br>`InvalidMixinException: @Shadow field state was not located in the target class ... . No refMap loaded`（实测日志原文，游戏启动时打一条 WARN 就悄悄跳过），整个 `BlockRendererMixin` 作废，X-Ray 在 Sodium 下自然一点反应都没有（连透明度模式也一起没了）。<br>**改法：彻底不要 `@Shadow`**，改为从方法参数里拿值 —— `prepare(ChunkBuildBuffers, LevelSlice, ...)` 的第 2 个参数就是 level，`renderModel(model, BlockState, BlockPos, origin)` 直接给了方块与坐标；两者跑在同一个 chunk 构建工作线程上，用一个 `ThreadLocal` 把 `(state, pos, level)` 传给后面的 `processQuad` / `bufferQuad` 钩子。<br>顺带一提：`RenderSectionManagerMixin`（关遮挡剔除）没有 `@Shadow`，所以一直是正常的；这次真正拖后腿的只有 `BlockRendererMixin`。目标 Sodium 版本经核对：1.21.11 用 0.8.14/0.8.15（两者这批方法签名完全一致）。 |
| 1.3.10-MC1.21.11 | 2026-10-11 | **修复「水上行走」（Jesus）在水上走时游戏崩溃**（`26.2` 分支同步修复，编号 1.3.9）。崩溃是 `StackOverflowError`：`onSendPacket` 收到 `PacketSendEvent` 后先 `event.cancel()`、又自己 `send()` 了一个把 y 修正过的包，而这个包**同样会触发 `PacketSendEvent`**（`ConnectionMixin` 的 `@ModifyVariable` 就挂在 `Connection.send` 上）→ 再次进入 `onSendPacket` → 再 `send()` → **无限递归**直到爆栈，整个客户端直接崩掉。<br>**改法**：不再 cancel + 重发，改用 `event.setPacket(newPacket)` **原地替换**要发出的包 —— `ConnectionMixin` 本来就是 `@ModifyVariable` 返回 `event.packet()`，替换后包内容正确、又不会重新进入发送流程，递归从根上消失。 |

## 26.2 分支

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0-MC26.2 | 2026-10-07 | 初始骨架：事件总线、Mixin 注入点、模块系统、设置系统、配置存档、渲染基础设施（自研 RenderPipeline / RenderType）、34 个功能模块 |
| 1.0.1-MC26.2 | 2026-10-08 | 补齐 7 个缺失模块（ClickGUI / HUD / AnchorAura / CrystalAura / Criticals / MaceKill / AutoTotem）；新增 Nova 主题 ClickGUI；修复 BoatFly / InvWalk 包名归类 |
| 1.1.0-MC26.2 | 2026-10-08 | **首次编译通过**（0 错误）。新增命令系统（彗星风格 `.` 命令 + Brigadier Tab 补全，共 28 条命令）；新增 Nova 徽标 icon；修复设置类构造器签名、`Minecraft.gui.screen()` 迁移、26.2 实体包路径、`fabric.mod.json` 入口点（main → client） |
| 1.1.1-MC26.2 | 2026-10-09 | 修复 **TickEvent 从未发布**的严重 bug（导致 AnchorAura / AutoTotem / Criticals / CrystalAura 四个战斗模块完全失效）；修复 InfiniChat 漏注册；新增 **Boost** 模块（Meteor Rejects，瞬间前冲加速度）；模块总数 44 → 45 |
| 1.2.0-MC26.2 | 2026-10-09 | 新增 **32 个模块**（NoFall / XRay / Search / AimAssist / TriggerBot / AutoArmor / AutoTotem 强化 / Chams / HoleEsp / Velocity / AutoEat / AutoPotion / AutoReconnect / AntiAfk / VeinMiner / Nuker / FastBreak / Scaffold / AutoWalk / NoHurtcam / NoWeather / BetterChat / NameProtect / Waypoints / Breadcrumbs / PacketLogger / Flight / HighJump / Spider / Step / Blink / LongJump）；**全部模块新增中文显示名**（GUI/HUD 显示中文，命令仍用英文标识符）；HUD 新增模块列表渲染；新增 `.waypoint` 命令；新增 5 个 Mixin（XRay 方块渲染、NoWeather 天气、Chams 描边颜色、AutoReconnect 断线检测、聊天拦截）。模块总数 45 → **76** |
| 1.3.0-MC26.2 | 2026-10-10 | **Sodium 兼容**：新增 `NovaMixinPlugin` + 独立 `nova.sodium.mixins.json`（`required:false`，按需加载），XRay 在 Sodium 下也能隐藏方块、并关闭 Sodium 遮挡剔除以免墙后矿物被整块剔除；新增 `SodiumCompat` 统一重建区块。新增 **Kaboom 模块**（Wurst 移植，爆炸特效 + 单 tick 内对周围 6 格所有方块疯狂发破坏包，用完自动关闭）。**破坏方式可选**：`BlockUtils` 新增 `BreakMode`（Normal / Legit / Speed）+ `breakBlocksWithPacketSpam`，`Nuker` 可切三档、`VeinMiner` 可切 Legit/Speed；`Scaffold` 新增放置模式（Normal 客户端预测 / Speed 直发交互包）。模块总数 76 → **77** |
| 1.3.1-MC26.2 | 2026-10-10 | **修复启动崩溃**：`MultiPlayerGameModeMixin.nova_onContinueDestroyBlock` 注入的是返回 `boolean` 的 `continueDestroyBlock`，回调却写成了 `CallbackInfo`，Mixin 要求 `CallbackInfoReturnable<Boolean>`。任何触及 `MultiPlayerGameMode` 的模组（如 Item Interactions / Easy Shulker Boxes）都会触发 `InvalidInjectionException`，导致游戏在初始化阶段直接崩溃 |

---

## 版本规则（给自己看）

- **修订号 +1**：修 bug、补文件、小调整
- **次版本 +1**：加一整块新功能（如命令系统、新模块类别）
- **主版本 +1**：架构级重构或首个正式可用版本

每次有实质改动，同步更新：
1. 本文件（加一行）
2. `gradle.properties` 的 `mod_version`
3. `src/main/resources/fabric.mod.json` 的 `version`
