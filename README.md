# ImageCool / ImageReader

ImageCool 是一个用于阅读本地 APK 资源包中图片内容的 Android 应用。应用会扫描手机存储中的资源包 APK，按包名分类展示，并提供列表/网格视图、排序、阅读进度记录、收藏和自动发布构建等功能。

## 主要功能

- 扫描本地资源包 APK，并按分类显示在顶部 Tabs 中。
- 支持按名称、图片数量、APK 体积排序。
- 最近阅读过的资源会排在列表前面。
- 支持 List/Grid 显示切换。
- Grid 模式会读取 APK `assets/imgs` 下第一张图片作为封面。
- 阅读页自动记录阅读进度和偏移位置。
- 支持收藏资源包。
- 启动页包含开屏动画和广告占位信息。
- GitHub Actions 支持每次 push 后自动编译 release APK 并发布 GitHub Release。

## APK 资源包放置方法

应用默认扫描外部存储根目录下的文件夹：

```text
/sdcard/0ImageCool
```

使用步骤：

1. 在手机存储根目录创建 `0ImageCool` 文件夹。
2. 把资源包 APK 放入该文件夹，子目录中也可以放 APK。
3. 打开应用并授予文件管理权限。
4. 应用会扫描 APK，扫描完成后资源会出现在首页 Tabs 下。

如果新增或删除 APK 后数量没有立即变化，可以退出应用后重新进入，或确认应用已经获得“管理所有文件”权限。

## 资源包 APK 格式

资源包 APK 需要包含图片资源目录：

```text
assets/imgs/
```

支持的图片格式：

```text
.jpg
.webp
.avif
```

应用还会读取资源包中的资源信息：

- `app_name`：资源包名称。
- `image_count`：图片数量。

包名需要符合当前分类规则，例如：

```text
com.golds.assets.<分类名>.<资源名>
```

应用会取包名中的 `<分类名>` 作为首页 Tab 分类。

## 使用方法

### 首页

- 顶部 Tabs：按资源分类显示，每个 Tab 下方显示当前分类 APK 数量。
- 排序 Spinner：选择名称、图片数量、体积排序；重复选择同一项会切换升序/降序。
- List/Grid 图标：点击右侧图标切换列表或网格显示。
- Grid 模式：显示资源封面，封面来自 APK `assets/imgs` 下按文件名排序后的第一张图片。
- 点击资源：进入图片阅读页。

### 阅读页

- 上下滑动阅读图片。
- 底部显示当前页码、总页数和阅读百分比。
- 返回时自动保存阅读进度。
- 点击图片可显示/隐藏工具栏。
- 双击图片可切换收藏状态。
- 长按工具栏中的卸载按钮可卸载对应资源包。

## 权限说明

应用需要读取 `/sdcard/0ImageCool` 中的 APK 文件，因此需要以下权限：

- `MANAGE_EXTERNAL_STORAGE`
- `READ_EXTERNAL_STORAGE`
- `WRITE_EXTERNAL_STORAGE`

Android 11 及以上系统需要在系统设置中授予“管理所有文件”权限，否则应用无法扫描本地 APK。

## 本地构建

推荐使用 JDK 21：

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :app:assembleDebug --no-daemon
```

Release 构建需要正确的签名配置：

```bash
./gradlew :app:assembleRelease --no-daemon
```

签名配置来自根目录 `keystore.properties`：

```properties
storePassword=...
keyPassword=...
keyAlias=...
storeFile=app/sign.jks
```

## GitHub Actions 发布

仓库包含 workflow：

```text
.github/workflows/android.yml
```

每次 push 后会自动：

1. 安装 JDK 21 和 Android CMake。
2. 编译 release APK。
3. 创建公开 GitHub Release。
4. 上传 APK 文件。

发布前需要在 GitHub 仓库 Secrets 中配置：

```text
PASSWORD
```

该值必须和 release 签名 keystore 的 `storePassword/keyPassword` 一致。

## 注意事项

- 当前项目仍使用 Android Support Library，不是 AndroidX。
- Release APK 需要有效签名密码，否则 CI 会在签名阶段失败。
- 如果仓库是公开仓库，不建议把 keystore 和签名配置明文提交到 Git。
- 资源包数量按数据库中有效 APK 记录统计，文件不存在的记录不会计入 Tabs 数量。
