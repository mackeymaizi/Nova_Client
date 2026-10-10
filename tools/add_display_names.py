"""Insert displayName("...") after the super(...) call in each module."""
import re
from pathlib import Path

ROOT = Path(r"D:/Nova Client/src/main/java/net/novaclient/module/modules")

# English class name -> Chinese display name
NAMES = {
    # client
    "ClickGuiModule": "点击界面",
    "HudModule": "界面",
    # combat
    "KillAuraModule": "杀戮光环",
    "CrystalAuraModule": "水晶光环",
    "AnchorAuraModule": "重生锚光环",
    "CriticalsModule": "暴击",
    "MaceKillModule": "重锤秒杀",
    "AutoTotemModule": "自动图腾",
    # movement
    "AutoSprintModule": "自动疾跑",
    "SpeedHackModule": "加速",
    "JesusModule": "水上行走",
    "JetpackModule": "喷气背包",
    "FishModule": "自动钓鱼",
    "NoWebModule": "无视蛛网",
    "NoSlowdownModule": "无视减速",
    "SnowShoeModule": "雪地靴",
    "AntiCactusModule": "防仙人掌",
    "InvWalkModule": "背包行走",
    "BoatFlyModule": "船飞行",
    "BoostModule": "冲刺",
    # render
    "FullbrightModule": "夜视",
    "AntiBlindModule": "防失明",
    "AntiWobbleModule": "防摇晃",
    "NoFogModule": "无雾",
    "NoOverlayModule": "无覆盖层",
    "NoVignetteModule": "无暗角",
    "NoPumpkinModule": "无南瓜头",
    "NoShieldOverlayModule": "无盾牌遮挡",
    "CameraNoClipModule": "相机穿墙",
    "TrueSightModule": "透视视野",
    "PlayerEspModule": "玩家透视",
    "ItemEspModule": "物品透视",
    "BarrierEspModule": "屏障透视",
    "NameTagsModule": "名牌",
    "HealthTagsModule": "血量标签",
    "TrajectoriesModule": "弹道预测",
    # player
    "AntiKnockbackModule": "防击退",
    "ReachModule": "加长手",
    # misc
    "AutoToolModule": "自动换工具",
    "RestockModule": "自动补货",
    "AntiWaterPushModule": "防水流推动",
    "PortalGuiModule": "传送门界面",
    "NoTelemetryModule": "无遥测",
    "NoChatReportsModule": "无聊天举报",
    "InfiniChatModule": "无限聊天",
}

changed, skipped, missing = [], [], []

for path in sorted(ROOT.rglob("*Module.java")):
    cls = path.stem
    if cls not in NAMES:
        missing.append(cls)
        continue

    src = path.read_text(encoding="utf-8")

    if "displayName(" in src:
        skipped.append(cls)
        continue

    # Find the super(...) call and insert right after it.
    m = re.search(r"(super\([^;]*?\);)", src, re.S)
    if not m:
        skipped.append(cls + " (no super)")
        continue

    indent = "\t\t"
    insert = m.group(1) + f'\n{indent}displayName("{NAMES[cls]}");'
    src = src[:m.start(1)] + insert + src[m.end(1):]
    path.write_text(src, encoding="utf-8")
    changed.append(cls)

print(f"changed: {len(changed)}")
print(f"skipped: {skipped}")
print(f"no mapping: {missing}")
