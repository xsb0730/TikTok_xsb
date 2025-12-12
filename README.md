# TikTok 抖音风格短视频 App

> 2025届字节跳动客户端训练营项目

一款基于 **MVVM 架构**的 Android 短视频应用，高度还原抖音核心交互体验，实现了双列信息流、视频播放、评论系统、个人主页等 11 个核心功能模块。

## 核心特性

### 视频浏览
- **双列瀑布流**: 采用 `StaggeredGridLayoutManager` 实现高度自适应的卡片布局
- **共享元素转场**: 封面点击平滑过渡到全屏播放页，提供流畅的视觉体验
- **垂直滑动切换**: 基于 `ViewPager2` 的上下滑动切换视频，预加载机制保证流畅播放

### 视频播放
- **ExoPlayer 播放器**: 支持单曲循环、播放状态监听、生命周期管理
- **智能播放控制**: 页面切换自动暂停/播放，ViewHolder 缓存机制优化性能
- **音乐转盘动画**: 旋转动画与播放状态同步

### 社交交互
- **双击点赞动画**: 自定义 `GestureDetector` 识别手势，6 种动画组合实现爱心特效
- **实时评论系统**: 基于 Room 数据库的本地评论存储，支持发布、点赞、删除
- **共享 ViewModel**: 评论弹窗与播放页共享数据，实时同步评论数

### 数据加载
- **下拉刷新**: 自定义触摸事件拦截，流畅的刷新动画和进度提示
- **上拉加载更多**: 智能检测滑动到底部，自动追加新数据
- **协程异步处理**: 使用 Kotlin Coroutines 避免阻塞主线程

### 个人主页
- **头像上传**: 支持相机拍照和相册选择，集成 UCrop 实现圆形裁剪
- **权限管理**: 使用 `ActivityResultContracts` 现代化权限请求
- **作品展示**: 网格布局展示个人作品和点赞视频

## 技术架构

### MVVM 架构
```
View (Activity/Fragment) 
    ↕ LiveData 双向绑定
ViewModel (业务逻辑)
    ↓
Repository (数据仓库)
    ↓
DataSource (本地数据库)
```

### 技术栈
- **语言**: Kotlin
- **架构**: MVVM + LiveData + Repository
- **UI**: ViewBinding + DataBinding
- **播放器**: ExoPlayer (Media3)
- **数据库**: Room
- **图片加载**: Glide
- **异步处理**: Kotlin Coroutines
- **图片裁剪**: UCrop
- **动画**: ObjectAnimator + AnimationSet

## 项目结构

```
app/src/main/java/com/example/tiltok_xsb/
├── ui/                          # UI 层
│   ├── activity/                # Activity
│   │   ├── MainActivity         # 主页面
│   │   └── VideoPlayActivity    # 视频播放页
│   ├── fragment/                # Fragment
│   │   ├── RecommendFragment    # 推荐页（双列流）
│   │   ├── PersonalHomeFragment # 个人主页
│   │   └── CommentDialog        # 评论弹窗
│   ├── adapter/                 # 适配器
│   │   ├── VideoPlayAdapter     # 视频播放适配器
│   │   ├── SameCityVideoAdapter # 双列流适配器
│   │   └── CommentAdapter       # 评论列表适配器
│   ├── view/                    # 自定义 View
│   │   ├── LikeAnimationView    # 点赞动画视图
│   │   ├── CircleImageView      # 圆形头像
│   │   └── IconFontTextView     # 图标字体
│   └── viewmodel/               # ViewModel
│       ├── VideoPlayViewModel   # 视频播放业务逻辑
│       ├── RecommendViewModel   # 推荐页业务逻辑
│       ├── CommentViewModel     # 评论系统业务逻辑
│       └── PersonalHomeViewModel# 个人主页业务逻辑
├── data/                        # 数据层
│   ├── model/                   # 数据模型
│   │   ├── VideoBean            # 视频实体
│   │   ├── UserBean             # 用户实体
│   │   └── CommentBean          # 评论实体
│   ├── repository/              # 数据仓库
│   │   ├── VideoRepository      # 视频数据仓库
│   │   ├── CommentRepository    # 评论数据仓库
│   │   └── UserRepository       # 用户数据仓库
│   └── database/                # 数据库
│       ├── AppDatabase          # Room 数据库
│       ├── CommentDao           # 评论 DAO
│       └── CommentEntity        # 评论实体
└── utils/                       # 工具类
    ├── AnimUtils                # 动画工具
    ├── VideoPlayTouchHelper     # 触摸手势处理
    └── ImageUtils               # 图片处理工具
```

## 核心功能

| 功能模块 | 实现技术 | 说明 |
|---------|---------|------|
| 双列信息流 | StaggeredGridLayoutManager | 瀑布流布局，高度自适应 |
| 转场动画 | Shared Element Transition | 共享元素平滑过渡 |
| 视频播放 | ExoPlayer + ViewPager2 | 垂直滑动切换，预加载优化 |
| 双击点赞 | GestureDetector + AnimationSet | 手势识别 + 组合动画 |
| 评论系统 | Room + BottomSheetDialog | 本地存储，共享 ViewModel |
| 下拉刷新 | 自定义 TouchHelper | 触摸事件拦截，自定义动画 |
| 头像上传 | UCrop + FileProvider | 圆形裁剪，权限管理 |
| 音乐转盘 | ObjectAnimator | 旋转动画与播放同步 |
| 数据加载 | Coroutines + LiveData | 异步处理，响应式更新 |
| 状态管理 | Resource 封装 | Loading/Success/Error 统一处理 |
| 导航系统 | ViewPager2 + TabLayout | Tab 联动，Fragment 管理 |

## 性能优化

- **ViewHolder 缓存**: 避免重复创建 ExoPlayer，提升滑动性能
-  **预加载机制**: `offscreenPageLimit = 1` 预加载前后视频
-  **图片优化**: Glide 缓存 + 提取视频首帧作为封面
-  **数据库优化**: 批量查询评论数，减少数据库访问次数
-  **协程异步**: 所有耗时操作在 IO 线程执行
-  **资源释放**: Activity/Fragment 销毁时及时释放播放器资源

## 开发环境

- **IDE**: Android Studio Ladybug | 2024.1.1
- **Kotlin**: 1.9.0
- **Gradle**: 8.5
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 33 (Android 13)
- **Compile SDK**: 35 (Android 15)

## 待修复BUG
- 点击双列外流卡片进入单列视频伴随画面放大转场后，但封面有时候无法及时关闭的问题

---

**开发者**: 重庆邮电大学-肖思博  
**项目**: 2025届字节跳动客户端训练营  
**更新时间**: 2025-12-11
