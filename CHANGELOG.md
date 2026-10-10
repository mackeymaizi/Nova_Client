# Nova Client 版本记录

所有版本号格式：`<主版本>.<次版本>.<修订>-MC<游戏版本>`

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
