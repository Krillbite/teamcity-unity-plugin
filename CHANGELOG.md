# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

### Changed

### Fixed

## 1.0.1 - 2023-02-23

### Changed
- Correct minimal compatible TeamCity version was specified (2022.10)

## 1.0.0 - 2023-02-20
As of now, the plugin is switching to SemVer versioning. This is the initial plugin release with the new scheme.
So if you used an old version of the plugin, please uninstall it before installing the new one.

### Added
- Managing Unity as a TC tool support - Credits to [AaronZurawski](https://github.com/AaronZurawski)

### Changed
- Editor console output now fully captured by TC
- The plugin is now only compatible with TeamCity 2020.1+

### Fixed
- Automatic licence return on the latest versions of Unity (via build feature)
- [TW-59710](https://youtrack.jetbrains.com/issue/TW-59710/Unity-build-feature-setting-Unity-version-doesnt-generate-an-agent-requirement) 
\: Build feature now generates agent requirement when Unity version specified
- [TW-68480](https://youtrack.jetbrains.com/issue/TW-68480/Unity-plugin-Unity-not-detected-with-no-apparent-reason-why)
\: Unity CN (Chinese version) installation discovery on Windows
