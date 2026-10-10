# Nova Client 测试指南（MC 26.2 分支）

## 一、你需要准备的环境

| 组件 | 版本 | 说明 |
|---|---|---|
| Minecraft | **26.2** | 必须是 26.2，其他版本不兼容 |
| Fabric Loader | **≥ 0.19.5** | 用官方安装器装 |
| Fabric API | **0.161.0+26.2** | 必须装，否则崩 |
| Java | **25** | 26.2 强制要求 Java 25 |
| Sodium | 可选（`0.9.2`） | 装了才有 X-Ray 透墙，不装也能用 |

Java 25 你机器上已经有了：`C:\Program Files\Java\jdk-25.0.3`

## 二、安装步骤

1. 用 [Fabric 官方安装器](https://fabricmc.net/use/installer/) 给 26.2 装上 Fabric Loader
2. 打开游戏目录的 `mods` 文件夹（启动器里一般有「打开游戏目录」按钮）
3. 把这两个 jar 丢进去：
   - `Nova-Client-1.3.2-MC26.2.jar`（本项目 `dist/` 目录里）
   - `fabric-api-0.161.0+26.2.jar`（从 [Modrinth](https://modrinth.com/mod/fabric-api) 下）
4. 启动器里选择 **Fabric 26.2** 那个版本启动

> 你自己的测试实例是 `versions/26.2-Fabric_0.19.5/`。

## 三、启动后按这个顺序测

### 1. 能不能进主界面
如果崩了，**99% 是 Mixin 注入失败**，见第四节抓日志。

### 2. 进游戏后先测 HUD
按 `右 Shift` 或者用命令 `.toggle hud` 打开。
**预期**：屏幕右上角出现模块列表（中文名）。
如果没反应 → HUD 渲染有问题。

### 3. 测 ClickGUI
命令 `.toggle clickgui`，或者按你绑的键。
**预期**：弹出 Nova 风格的面板，左侧是分类（战斗/移动/渲染…），模块名显示中文。
点模块名能开关，点箭头能展开设置项。

### 4. 测命令 + Tab 补全（重点）
按 `T` 打开聊天，输入 `.`，然后按 `Tab`。
**预期**：弹出所有命令的补全列表。
继续输入 `.toggle ` 再按 `Tab` → 应该列出所有模块名。

试试这几条：
- `.help` — 列出命令
- `.modules` — 列出模块（按类别分组，绿色=已开启）
- `.toggle flight` — 开飞行
- `.setting flight` — 看飞行的设置项
- `.waypoint add home` — 加路径点

### 5. 逐个测关键模块
| 模块 | 命令 | 预期效果 |
|---|---|---|
| 飞行 | `.toggle flight` | 空格上升、Shift 下降、WASD 平移 |
| 发包飞行 | `.toggle packetfly` | 空格上升、Shift 下降、WASD 平移（走数据包，不走原版物理） |
| 高跳 | `.toggle highjump` | 跳跃高度明显变高 |
| 爬墙 | `.toggle spider` | 顶着墙走能爬上去 |
| 连锁挖矿 | `.toggle veinminer` | 挖一个矿，整条矿脉一起掉 |
| 范围挖掘 | `.toggle nuker` | 周围方块自动碎 |
| 防摔伤 | `.toggle nofall` | 高空落地不掉血 |
| 夜视 | `.toggle fullbright` | 暗处看得见 |
| X光 | `.toggle xray` | 除了矿物，其他方块全部消失 |
| 方块搜寻 | `.toggle search` 然后 `.setting search` 改 block | 目标方块被画框高亮 |
| 自动图腾 | `.toggle autototem` | 副手自动放不死图腾 |
| 自动进食 | `.toggle autoeat` | 饿了自动吃 |
| 自动穿甲 | `.toggle autoarmor` | 背包里的好甲自动穿上 |
| 人物描边 | `.toggle chams` | 其他玩家透墙显示发光轮廓 |
| 安全洞透视 | `.toggle holeesp` | 周围的 1x1 安全洞被画框 |
| 瞄准辅助 | `.toggle aimassist` | 视角自动往附近敌人转 |
| 自动攻击 | `.toggle triggerbot` | 准星对准实体就自动打 |
| 数据包记录 | `.toggle packetlogger` | 聊天框刷数据包名 |
| 延迟发包 | `.toggle blink` | 别人看你卡在原地，关掉后瞬移 |
| 聊天增强 | `.toggle betterchat` | 刷屏的重复消息被吞掉 |

### 6. 测 X-Ray + Sodium（v1.3.2 重点）
前提：`mods/` 里有 Sodium `0.9.2+mc26.2`。

1. `.toggle xray`，站在地下往上看
2. **预期**：只有矿物/白名单方块可见，石头全部消失
3. 把 `.setting xray` 里的 `Opacity` 调到 100 左右
4. **预期**：非白名单方块变成半透明，而不是完全消失
5. **关键**：墙**后面**的矿也要看得见
   （这是遮挡剔除那条路径，v1.3.2 修的就是它）

### 7. 测 PacketFly（v1.3.2 新增）
1. `.toggle packetfly`
2. `.setting packetfly`，把 `Horizontal speed` 调到 5.2 左右先试
3. 按空格上升、Shift 下降、WASD 平移
4. **预期**：能飞；关掉模块后落地
5. 想试高速：把速度调到几千甚至 100000，看服务器会不会把你拉回来
   （`Set position` 建议保持开启，否则客户端跟不上）
6. **注意**：能不能飞、能多快，**完全取决于服务器有没有移动校验**。
   有正经反作弊的服务器会直接把你 rubber-band 回来，这不是客户端 bug。

## 四、崩了怎么办（重要）

**抓日志**：游戏目录下的 `logs/latest.log` 和 `crash-reports/` 里最新的那个文件。

在日志里搜这几个关键词，能直接定位问题：
- `Mixin apply failed` — Mixin 注入失败，后面会写清楚是哪个类哪个方法
- `InvalidInjectionException` — 注入签名不匹配（比如回调类型写错了）
- `InvalidMixinException` — Mixin 配置问题
- `NoSuchMethodError` / `NoClassDefFoundError` — API 用错了
- `Caused by:` — 真正的根因通常在最后这个后面

把 `crash-reports` 里那个文件发我，或者把 `latest.log` 里从
`---- Minecraft Crash Report ----` 开始的部分发我。

**临时排查法**：编辑 jar 里的 `nova.mixins.json`，把 `"required": true` 改成
`"required": false`，这样单个 Mixin 失败不会崩游戏，但对应功能会失效 ——
可以用这个方法快速定位是哪个 Mixin 的问题。

## 五、已知可能不稳的点

这些地方大多是照着 javap 反编译的签名写的，**部分没有实机验证过**，如果出问题优先查：

1. **`CommandSuggestionsMixin`** — Tab 补全。如果补全不出来但不崩，是这里的注入点没命中。
2. **X-Ray + Sodium** — v1.3.2 起支持（`mixin/sodium/BlockRendererMixin`）。
   如果装了 Sodium 但 X-Ray 失效，先确认
   `BlockRendererMixin` 里两处 `@At` 的 owner 是 `MutableQuadViewImpl`
   而不是 `QuadViewImpl`（写错了会因为 `require = 0` 静默失效）。
3. **`ChamsModule`** — 靠 `Entity.getTeamColor()` + 发光标记。如果服务器有队伍系统可能被覆盖。
4. **`AutoReconnectModule`** — 重连用的是 `ConnectScreen.startConnecting`，
   某些启动器的会话校验可能导致重连失败。
5. **`ScaffoldModule` / `AutoArmorModule`** — 涉及背包点击和方块放置，
   不同服务器反作弊拦截程度不同。
6. **`VeinMinerModule` / `NukerModule`** — 直接发破坏包，部分服务器会回弹。
7. **`PacketFlyModule`** — 见上面第 7 节，效果取决于服务器。
8. **ESP 类模块**（PlayerEsp / ItemEsp / HoleEsp / Trajectories / Waypoints /
   Breadcrumbs / Search）走的是自研 RenderPipeline + 自定义 shader
   （`assets/nova/shaders/core/fogless_lines.*`）。这套链路静态上核对过，但没实测。

## 六、当前状态

- 版本：`1.3.2-MC26.2`
- 模块：**78 个**
- 命令：**29 条**
- Mixin：**34 个**
- 编译：0 错误
- 全部 Mixin 注入点已对着 26.2 映射 jar 逐个核对通过（方法名、参数签名、父类、字段可见性）
