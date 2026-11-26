# Tablet Floating Skin — Poweramp 平板悬浮布局皮肤

## 项目简介

一个 Poweramp v3 皮肤（Skin APK），针对**平板/大屏横屏**场景优化播放器界面：

| 调整项         | 默认行为       | 本皮肤效果                                         |
| ----------- | ---------- | --------------------------------------------- |
| 底部导航栏       | 宽度撑满屏幕     | 左右各缩进 `320dp`，宽度大幅收窄，底部抬高 `10dp` 呈悬浮胶囊        |
| 专辑封面        | 左侧贴边、高度撑满  | 原始 `62%` 尺寸（≈423dp，上限 520dp），严格居中于屏幕左半，上下视觉居中 |
| 标题/艺术家      | 叠加在封面图左下角  | 移到封面图正下方，左缘与封面图左缘对齐                           |
| 歌曲信息行（meta） | 位于右侧控件区下方  | 保持默认位置（右侧控件区下方）                               |
| 右侧控件区       | 波形条/按钮横跨右侧 | 硬锚定屏幕右半（50%），距右边缘 `32dp`                      |
| 播放控件        | 默认按钮组      | 黑色圆形背景 + 白色图标，并启用官方 MoreButtons 扩展布局          |

手机及竖屏**不受影响**，保持 Poweramp 默认布局（通过 `values-sw600dp-land` 限定符隔离）。

- 包名：`com.cjq7821.poweramp.v3.skins.tabletfloating`
- 皮肤样式：`TabletFloatingSkinDark`（深色，唯一版本）
- 目标 Poweramp：build 1003+（基于 build 1026 资源编译）
- 调优基准：3200×2136 物理像素（开发者选项「最小宽度」683dp）→ 逻辑分辨率 **1024×683dp @ ~3.13x**
- **无设置项**：布局参数全部为编译期固定值（2026-10-20 起移除全部皮肤选项）
- 针对 Xiaomi pad 7 pro 横屏调优，个人自用，不考虑其他设备兼容性。

## 目录结构

```
TabletFloatingSkin/
├── build.gradle                  # 根构建脚本（SDK 版本等 ext 常量）
├── settings.gradle               # 包含 :skin 模块；:poweramp_api_lib 映射到参考仓库
├── gradle.properties
├── local.properties              # poweramp.apk 资源包路径（本地配置，不入库）
├── gradlew / gradlew.bat         # Gradle Wrapper（8.x）
├── gradle/                       # wrapper + libs.versions.toml 版本目录
├── Poweramp-build-1026-resources.zip   # Poweramp 资源包（编译皮肤必需）
├── PROJECT.md                    # 本文档
└── skin/                         # 皮肤模块（可安装 APK）
    ├── build.gradle              # aapt2 参数：-I 资源包、--package-id 0x80、stable-ids
    ├── stable-ids.txt            # 稳定资源 ID（皮肤样式 ID 固定不变）
    └── src/main/
        ├── AndroidManifest.xml   # com.maxmpz.PowerampSkins meta-data + 入口 Activity
        ├── java/.../SkinInfoActivity.kt   # 皮肤信息页（应用皮肤/打开皮肤设置）
        └── res/
            ├── xml/skins.xml                 # 皮肤声明（仅深色一个 skin，无选项页）
            ├── values/skin_styles.xml        # 基础皮肤样式（播放控件强制覆盖 + 状态栏透明）
            ├── values/strings.xml, styles.xml
            ├── layout/activity_skin_info.xml
            └── values-sw600dp-land/          # ★ 平板横屏覆盖（核心）
                └── skin_styles.xml           #   深色皮肤样式（全部固定值，含布局参数）
```

> 已删除文件（2026-10-20 布局固化时移除）：`xml/skin_options.xml`、`values/skin_options_styles.xml`、`values-sw600dp-land/skin_options_styles.xml`（选项页与选项样式）、`values/attrs.xml`（选项用的 tfl* 自定义 attr）、`values/dimens.xml`、`values-sw600dp-land/dimens.xml`（选项默认值，已并入样式固定值）。

## 实现原理

Poweramp v3 皮肤**不能修改布局 XML**（仅允许 `merge_*.xml` 注入），所有定制通过「**主题属性 + 样式覆盖**」完成：

1. **导航栏宽度** — 主题属性 `navbar_marginLR` / `navbar_marginB` 直接作为 `Base_Navbar` 样式的左右/底部边距，主题层覆盖即生效（本皮肤固定 `320dp` / `10dp`，圆角 `corners_navbar=28dp`）。
2. **专辑封面（62%，左半居中，上下居中）** — 两级覆盖（官方示例 `SampleSkinAAA` 的约定模式）：
   - `TopListWidget`（主题属性）→ `TflTopListWidget` → `listSubstyle` → `TflListSubstyle`（继承 `com.maxmpz.audioplayer:ListSubstyle2`）；
   - **水平定位**：列表保持全宽 + `aaGravity=center_horizontal` + `aaPaddingLeft=0` + `aaPaddingRight=512dp`（屏宽 1024dp 的一半）。封面内容区被限制在 `[0, 512dp]`，`center_horizontal` 使封面在左半区内居中——**任意封面大小均严格居中左半**（62% 封面宽 ≈423dp，左缘 ≈44dp），无需按封面尺寸计算补偿值（旧方案 `aaMarginLeft` 已被真机证伪：margin 不驱动封面图像定位）；
   - **垂直定位**：`aaPaddingTop=aaPaddingBottom=76dp` 上下对称，引擎在内容区内垂直居中 → 封面图视觉居中；底部内边距同时容纳封面下方两行文本；
   - **尺寸**：`aaItemRatio=62%`（横屏屏高 683dp 的 62% ≈ 423dp）+ `aaMaxSize=520dp`（取较小者，不截断）；`aaSideItemNum=0` 隐藏相邻封面偷窥；`aaItemBoundsMatchList=width` 使 item 布局边界匹配列表全宽（不影响封面图自身定位，便于标题等子视图在全列表范围锚定）；
   - `TopAABounds`（主题属性）→ `TflTopAABounds`（继承 `TopAABounds2`，`matchAASubstyle=true` 封面贴身框），其 `aa*` 值与 `TflListSubstyle` **逐项保持一致**（手写同步，无 attr 间接层）。
3. **右侧控件分栏（不动）** — 不依赖 aa_bounds，直接对 `TopWaveseekLayout`/`TopMetaInfoLayout`/`TopSubAAButtons` 设 `layout_attachLeft=50%`（FastLayout fraction）硬锚定屏幕右半 + `layout_marginRight=32dp` 留白；waveseek 的 `layout_gravityTarget=@null`（垂直居中基准改回父容器）。
4. **标题/艺术家（封面下方、左对齐封面）** — 横屏标题/艺术家**不是** `TopMetaInfoLayout` 子元素，而是 `ItemTrackTitle_scene_aa`/`ItemTrackLine2_scene_aa` 主题 attr 指向的 scene_aa 覆盖 item。本皮肤：
   - 垂直：`attachBottom=@null` + `attachTop=@id/aa_image`（标题）/ `@id/title`（艺术家）→ 落在封面图下方；
   - 水平：`attachLeft=@null`（清除默认 `attachLeft=parent`）+ `attachLeftToLeft=@id/aa_image` + `marginLeft=0` → **左缘动态对齐封面图左缘**（≈44dp，随封面尺寸自动跟随，不依赖固定补偿值）。
5. **播放控件扩展（MoreButtons）** — `TopWaveseekLayout_extraLayout` 注入官方 `merge_topwaveseeklayout_extralayout` 布局，并把 `TopPlay/TopPause/TopTrackElapsed/TopTrackDuration` 及其 `scene_playing` 变体切到官方 `*MoreButtons` 系列样式。
6. **库视图兼容** — 库视图与播放器封面**共用唯一 `TopListWidget` attr**，无法分场景样式；列表保持全宽（match_parent），封面定位完全由 ListSubstyle 的 padding/gravity 实现，库视图不受任何影响。
7. **配置隔离** — 所有覆盖放在 `values-sw600dp-land`，仅 smallest width ≥ 600dp 且横屏时命中；`values/` 中的同名样式只含播放控件强制覆盖与状态栏透明，手机竖屏布局走 Poweramp 默认。

关键属性参考（参考仓库 `reference/powerampapi-master/poweramp_skin_sdk/reference_resources/`）：

- `values/attrs-player.xml` — `ListSubstyle` 的 `aaItemRatio`、`aaMaxSize`、`aaPadding*`、`aaMargin*`、`aaSideItemNum`、`aaGravity`、`aaItemBoundsMatchList`、`corners_aa_scene_aa`（dimension|reference）等
- `values/attrs-fastlayout.xml` — `layout_attachLeft/Top/Right/Bottom`（reference|integer|fraction）、`layout_attachLeftToLeft`/`layout_attachRightToRight`（对齐目标左/右缘）、`layout_widthPercent`、`@null` 清除继承锚定
- `values/styles-item-track.xml` — `ItemTrackTitle_scene_aa`/`ItemTrackLine2_scene_aa` 默认锚定（attachLeft=parent + attachBottom 链）
- `values-land/styles-top.xml` — 横屏默认 `aaGravity=left`、`TopAABounds2`（matchAASubstyle）、`TopWaveseekLayout`（gravityTarget=aa_bounds）的来源

### 深色主题与状态栏

- 仅提供**深色**一个皮肤（`TabletFloatingSkinDark`，继承 `ActivityTheme_Black`），无浅色变体，不做跟随系统切换。
- 状态栏透明需两层覆盖（均已真机验证）：`android:statusBarColor=@android:color/transparent`（系统层）+ `com.maxmpz.audioplayer:statusBarBg=#0000`（Poweramp 自绘 status_bar_bg 遮罩，黑主题默认 `#5000`，仅设前者无效）。

## 构建

前置条件：JDK 17+、Android SDK（cmdline-tools 即可）、工程根目录已有 `Poweramp-build-1026-resources.zip`。

> 从 GitHub 克隆时：`settings.gradle` 通过 `../reference/powerampapi-master/poweramp_api_lib` 复用官方 API 库（未入库）。
> 先 `git clone https://github.com/maxmpz/powerampapi ../reference/powerampapi-master`，或修改 `settings.gradle` 指向本机已有的该模块路径。
> `local.properties` 不入库（含本机 SDK 路径），首次构建由 Android Studio 自动生成，命令行构建时手动创建并写入 `sdk.dir`；`poweramp.apk` 未配置时默认使用根目录的资源包。

```powershell
# 在工程根目录（TabletFloatingSkin/）执行：
.\gradlew.bat :skin:clean :skin:assembleDebug
```

产物：`skin/build/outputs/apk/debug/skin-debug.apk`，安装到平板后在 Poweramp「设置 → 外观 → 主题」中选择 **Tablet Floating**，或打开本 App 点击「应用 Tablet Floating 皮肤」。

> Android Studio：直接 `Open` 本工程目录，首次同步会自动写入 `sdk.dir`。

### 构建注意事项

- **必须 JDK 17+**（AGP 8.13 + Kotlin 2.1.20 要求）。本机 `JAVA_HOME` 已指向 JDK 17，Gradle 会自动使用；若报 JVM 版本错误，先设置 `JAVA_HOME` 再构建。
- **`stable-ids.txt` 不能包含注释行**。aapt2 的 `--stable-ids` 解析器只接受 `包名:类型/名称 = 0x...` 形式的行，出现 `#` 注释会直接报 `error: missing '='`。需要说明时请写在 `PROJECT.md` 里。
- `local.properties` 中的 `poweramp.apk` 指向资源包，路径相对工程根目录；`sdk.dir` 指向本机 Android SDK。

## 调参指南（编译期固定值）

所有布局参数集中在 `skin/src/main/res/values-sw600dp-land/skin_styles.xml`，修改后需重新编译：

| 参数（样式内固定值）                         | 当前值               | 作用                                                   |
| ---------------------------------- | ----------------- | ---------------------------------------------------- |
| `navbar_marginLR`                  | 320dp             | 导航栏左右边距，调大 → 导航栏更窄                                   |
| `navbar_marginB`                   | 10dp              | 导航栏底部抬高量                                             |
| `corners_navbar`                   | 28dp              | 导航栏圆角                                                |
| `corners_aa_scene_aa`              | 8dp               | 封面圆角                                                 |
| `aaItemRatio`                      | 62%               | 封面大小（横屏屏高 683dp 的百分比；62% ≈ 423dp）                    |
| `aaMaxSize`                        | 520dp             | 封面尺寸上限（与 aaItemRatio 取较小者）                           |
| `aaGravity`                        | center_horizontal | 封面在内容区内水平居中                                          |
| `aaPaddingLeft` / `aaPaddingRight` | 0dp / 512dp       | 封面内容区 = `[0, 屏宽/2]`；**右侧 padding 决定左半居中**，调大 → 封面越靠左 |
| `aaPaddingTop` / `aaPaddingBottom` | 76dp / 76dp       | 上下对称 → 垂直视觉居中；底部同时容纳封面下方两行文本                         |
| `aaSideItemNum`                    | 0                 | 隐藏相邻封面偷窥                                             |
| `aaItemBoundsMatchList`            | width             | item 布局边界匹配列表全宽（不影响封面图定位）                            |
| 右侧控件 `layout_marginRight`          | 32dp              | waveseek/meta/sub_aa_buttons 距屏幕右边缘留白                |

**注意**：修改 `aa*` 参数时，`TflListSubstyle` 与 `TflTopAABounds` 两处需**逐项手动同步**（无 attr 间接层后不再自动一致）。

**不同分辨率的换算**：`aaPaddingRight` 应设为屏宽的一半（如 1024dp → 512dp）；`aaPaddingTop/Bottom` 对称即可（垂直居中由引擎在内容区内完成）。

## 已知限制与说明

- **皮肤信任层（Poweramp build 1026+，实测确认）**：签名不在 Poweramp 内置白名单中的皮肤（含所有 debug 签名包）会被软降级——皮肤选择器显示 `[unverified]` 前缀、只能从本 App 的「打开皮肤设置」按钮以 `startActivityForResult` 认证打开、首次选用 **48 小时后自动取消选择**。本皮肤已无设置项，该限制仅影响 `[unverified]` 前缀与 48h 自动取消。
- **加入白名单流程**：发布皮肤（Play 内部测试轨道即可，或直接分发 APK）→ 取签名证书 SHA-256 指纹（Play Console「App signing key」，或 `apksigner verify --print-certs skin.apk` 的 certificate SHA-256 digest）→ 把包名 + 指纹发给 Poweramp 作者 → 随下个 Poweramp 版本/beta 生效。详见官方 readme「Skin Verification」一节。
- **待真机验证（本次布局固化后）**：
  1. `aaGravity=center_horizontal` 的左半居中是否生效（回退方案：`aaGravity=left` + `aaMarginLeft≈44dp` 固定补偿，62% 封面专用）；
  2. 标题 `attachLeftToLeft=@id/aa_image` 水平锚定是否生效（回退方案：`attachLeft=parent` + `marginLeft≈44dp`）；
  3. `aaPaddingTop/Bottom=76dp` 对称后的垂直视觉居中效果（若偏上/偏下，微调 `aaPaddingTop`）。
- Poweramp「评分」选项（`_w_rating`）会覆盖 scene_aa item 的锚定样式，可能影响标题位置。
- 发布时请使用正式签名，并保证各版本签名一致（白名单按签名证书识别皮肤）。
- `stable-ids.txt` 请勿删除或乱改：它保证皮肤样式资源 ID 跨构建稳定，避免 Poweramp 缓存失效后皮肤错位。

## 后续可扩展

- 竖屏平板如需类似效果，新增 `values-sw600dp-port`（竖屏 AA 在顶部居中，需另一组参数）。
- 发布后把签名指纹提交给 Poweramp 作者加入信任白名单（去除 `[unverified]`、48h 自动取消）。

## 修改记录
### 2026-10-20（10）margin 定稿 + 闪烁原因分析 + 手机排版改造
- 平板横屏封面定位定稿：`marginLeft=140dp`、`marginTop=110dp`（真机多轮微调后用户指定值）
- 滑动切歌标题消失/闪烁两下原因查明（未改动）：官方样式 `applyViewParams=true`+`recalcChildren=dynamic`+封面 scale 动画（scene_aa=0.975→playing=1，标题锚定封面底边跟随跳动）；切歌两次场景重应用=两次闪烁；艺术家行 `layout_goneWhenEmpty=true` 瞬空消失；标题右缘默认锚定 `@id/lyrics` 未清除
- 手机竖屏/平板竖屏：基础样式（values/skin_styles.xml）加入 MoreButtons 播放控件扩展
- 手机横屏（超宽）：新增 values-land/skin_styles.xml —— MoreButtons + 标题移到封面下方（alt 禁用+widthToHeight+ratio 71%+side 0，机制同平板但封面用默认 8dp margin 左上定位，分辨率无关）+ 导航栏左右缩进 320dp（同平板）
- 右侧控件可调参数（备忘）：TopWaveseekLayout/TopMetaInfoLayout/TopSubAAButtons 均可通过 layout_attachLeft/Right/Top/Bottom（parent/inset/@id/fraction）、layout_gravityTarget、android:layout_margin*、高度/scale/alpha 调整；默认三者均锚定 aa_bounds 右侧


### 2026-10-20（9）— 布局固化（无设置项版本）

- **封面恢复原始 62%**：`aaItemRatio` 固定 `62%`（≈423dp），`aaMaxSize=520dp`。
- **左半侧严格居中**：改用 `aaGravity=center_horizontal` + `aaPaddingLeft=0` + `aaPaddingRight=512dp`（屏宽/2），任意封面大小严格居中左半；废除按封面尺寸计算的 `aaMarginLeft` 补偿（真机已证伪：margin 不驱动封面定位）。
- **上下视觉居中**：`aaPaddingTop` `40dp→76dp`，与 `aaPaddingBottom=76dp` 对称，封面图垂直居中。
- **标题左对齐封面**：新增 `attachLeftToLeft=@id/aa_image`（清除默认 `attachLeft=parent`），标题/艺术家左缘动态对齐封面图左缘。
- **右侧控件不动**：`attachLeft=50%` + `marginRight=32dp` 维持不变。
- **删除全部设置项**：删除 `xml/skin_options.xml`、两个 `skin_options_styles.xml`、`values/attrs.xml`（tfl* attr）、两个 `dimens.xml`；`skins.xml` 移除 `<include>`；`strings.xml` 清理全部 `opt_*`；`corners_aa_scene_aa` 由 seekbar 注入改为固定 `8dp`；样式内 `?attr/tfl*` 全部替换为直接值。
- **恢复 MoreButtons 播放控件扩展**：`TopWaveseekLayout_extraLayout` + `TopPlay/TopPause/TopTrackElapsed/TopTrackDuration`（含 `scene_playing` 变体）切官方 MoreButtons 系列样式（此前仅存在于 16:43 构建的 APK 中，源文件曾被环境回退丢失）。
- 保留：`aaItemBoundsMatchList=width`（item 边界匹配列表全宽）、状态栏透明双覆盖、导航栏 320dp/10dp/28dp、播放控件黑圆白图标。

### 2026-10-20（8）

- 按 3200×2136 @ 2x（1600×1068dp）重新调优默认定位：`tfl_aa_margin_left` `18dp→140dp`，`tfl_aa_margin_top` `48dp→160dp`。
- 仅保留深色版本：`skins.xml` 单 skin 指向 `TabletFloatingSkinDark`；删除浅色样式与相关字符串。
- 状态栏透明：深色主题新增 `android:statusBarColor=@android:color/transparent`。

### 2026-10-20（7）

- 封面定位机制修正（后被（9）推翻）：`aaGravity=left` 下改用 `aaMarginLeft`/`aaMarginTop` 定位；`AAScale` 选项只保留 50%/62% 两档。

### 2026-10-20（6）

- 放弃 50/50 分栏：`layout_widthPercent=50%` 限宽 `TopListWidget` 会同时收窄横屏库视图 → 列表恢复全宽；右侧控件保留 `attachLeft=50%` 硬锚定。

### 2026-10-20（5）

- 信息行（44.1 KHZ 406 KBPS ALAC）恢复默认位置：`TflTopMetaInfoLayout` 改为 `attachLeft=50%` + 右边距。

### 2026-10-20（4）

- 修复真机反馈：标题/艺术家仍叠加封面左下角 → 重锚定 `attachTop=@id/aa_image` 移到封面图下方；waveseek 从封面中部穿过 → 右侧控件改为 `layout_attachLeft=50%` 硬锚定，不再依赖 aa_bounds。

### 2026-10-20（3）

- 导航栏默认值调整：左右缩进 `160dp→320dp`，底部抬高 `20dp→10dp`。
- 布局重构为 50/50 分栏（后被（6）放弃）。

### 2026-10-20（2）

- 定位「选项调节不生效」根因：Poweramp build 1026+ 信任层对未验证皮肤的软降级，并非选项实现问题。

### 2026-10-20

- 初版：导航栏悬浮收窄、封面缩小四周留白、右侧控件留白；新增皮肤选项页（后于（9）整体移除）；自定义 attr 间接引用正交设计（后于（9）随选项一并移除）；`stable-ids.txt` 固定样式 ID。
