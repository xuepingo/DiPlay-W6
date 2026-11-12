# CI 示例（默认不启用）

这里是从上游 `DiPlay-Legacy-Android` 原样带过来的 4 个 GitHub Actions 工作流。

**它们不放在 `.github/workflows/`，所以不会自动运行。**

## 为什么不在 workflows 目录

1. 这些工作流是上游的 source-only 构建流水线（JDK 25 + lint + 单测 + debug 编译），**不产出带 offline-mfi 身份的可用 APK**。本项目的 APK 是本地手工构建的：构建需要从别处取得身份资产（见根目录 README 的 5.3），CI 环境里没有这些东西，跑起来只会长期报红。
2. 推送 `.github/workflows/` 下的文件要求 token 具备 `workflow` scope，而本仓库的发布流程（tag + Release + APK 附件）用不到这个权限。

## 想启用怎么办

把需要的工作流复制回 `.github/workflows/`，并给你的 Git 凭据补上 `workflow` 权限：

```bash
gh auth refresh -h github.com -s workflow
mkdir -p .github/workflows
cp docs/ci-examples/android.yml .github/workflows/
git add .github/workflows && git commit -m "ci: 启用 Android CI" && git push
```

## 文件说明

| 文件 | 用途 |
|---|---|
| `android.yml` | 单测 + lint + debug 编译，在 push/PR 上做门禁 |
| `pages.yml` | 部署 GitHub Pages（上游文档站） |
| `verify-release-apk-version.yml` | 校验 Release 附件 APK 的 versionCode/versionName 与 tag 一致 |
| `verify-release-signature.yml` | 校验 Release 附件 APK 的签名 |
