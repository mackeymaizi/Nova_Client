# Nova Client

把 **Wurst / Meteor / Meteor Rejects / LiquidBounce** 的功能整合到一起的
Minecraft Fabric 辅助客户端，自研架构。

## 分支结构

`main` 分支**刻意留空**，只放这份说明和 `.gitignore`。真正的代码在两个版本分支上：

| 分支 | 游戏版本 | Java | 本地文件夹 | 产物 |
|---|---|---|---|---|
| [`26.2`](../../tree/26.2) | Minecraft **26.2** | 25 | `./26.2/` | `Nova-Client-1.3.1-MC26.2.jar` |
| [`1.21.11`](../../tree/1.21.11) | Minecraft **1.21.11** | 21 | `./1.21.11/` | `Nova-Client-1.3.1-MC1.21.11.jar` |

两个版本功能集完全一致（**77 个模块、28 条命令**），只是 API 适配不同。
1.21.11 的移植细节见该分支的 `PORTING.md`，版本历史见 `CHANGELOG.md`。

## 下载

去 [Releases](../../releases) 页面按游戏版本下载对应的 jar。

## 本地布局（git worktree）

这个仓库用 **git worktree** 让两个版本的代码在本地同时存在：

```
Nova Client/                 <- 仓库根，停在 main（空）
├── .git/                    <- 只有这一份仓库数据
├── 26.2/                    <- 26.2 分支的工作区
│   └── src/ ...
└── 1.21.11/                 <- 1.21.11 分支的工作区
    └── src/ ...
```

在哪个文件夹里改，就是改哪个分支。提交/推送在对应文件夹里正常 `git` 操作即可。

> 重新克隆仓库后不会自带这两个文件夹，用下面的命令补回来：
> ```bash
> git worktree add ./26.2 26.2
> git worktree add ./1.21.11 1.21.11
> ```

## 构建

需要 JDK（26.2 用 25，1.21.11 用 21 作为编译目标）。

```bash
cd 26.2      # 或 cd 1.21.11
./gradlew build
```

产物在 `dist/`（仓库里已经放了一份），或 `build/libs/`。

## 功能概览

- **77 个模块**：战斗 / 移动 / 渲染 / 玩家 / 世界 / 杂项 / 客户端
  （GUI 与 HUD 显示中文名，命令和配置文件用英文标识符）
- **28 条命令**：彗星（Meteor）风格 `.` 前缀 + Brigadier Tab 补全
- Nova 主题 ClickGUI + HUD 模块列表
- 破坏方式可选：`Normal / Legit / Speed`（Nuker 三档、VeinMiner 两档、Scaffold 有放置模式）
- **Kaboom**（Wurst 移植，轰爆）
- **Sodium 兼容 XRay**（仅 26.2 分支）

## 许可

GPL-3.0-or-later
