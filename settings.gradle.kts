pluginManagement {
    repositories {
        // 配置插件仓库（用于下载Gradle插件，如Android、Kotlin插件）
        maven { url = uri("https://mirrors.cloud.tencent.com/maven/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/gradle/") }
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // 强制优先使用这里的仓库，禁止项目级配置
    repositories {
        // 优先国内镜像，替代官方源
        maven { url = uri("https://mirrors.cloud.tencent.com/maven/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/gradle/") }
        google() // 保留官方仓库兜底
        mavenCentral()
    }
}

rootProject.name = "tiltok_xsb"
include(":app")
