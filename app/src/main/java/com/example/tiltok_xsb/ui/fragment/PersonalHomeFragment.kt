package com.example.tiltok_xsb.ui.fragment


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.base.BaseBindingFragment
import com.example.tiltok_xsb.databinding.FragmentPersonalHomeBinding
import com.example.tiltok_xsb.ui.adapter.PersonalHomePagerAdapter
import com.example.tiltok_xsb.ui.view.AvatarChooseDialog
import com.example.tiltok_xsb.ui.viewmodel.PersonalHomeViewModel
import com.example.tiltok_xsb.utils.ImageUtils
import com.example.tiltok_xsb.utils.Resource
import com.google.android.material.tabs.TabLayoutMediator
import com.yalantis.ucrop.UCrop
import java.io.File


class PersonalHomeFragment : BaseBindingFragment<FragmentPersonalHomeBinding>({ FragmentPersonalHomeBinding.inflate(it) }) {
    private val viewModel: PersonalHomeViewModel by viewModels()

    //临时存储拍照后的照片 URI
    private var tempPhotoUri: Uri? = null

    // 相机权限请求
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ granted ->
        if (granted) {
            openCamera()
        } else {
            showToast("需要相机权限才能拍照")
        }
    }

    // 存储权限请求
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openGallery()
        } else {
            showToast("需要存储权限才能选择图片")
        }
    }

    // 拍照结果
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            startCrop(tempPhotoUri!!)
        } else {
            showToast("拍照失败")
        }
    }


    // 图库选择结果
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    )  { uri ->
        if (uri != null) {
            startCrop(uri)
        } else {
            showToast("未选择图片")
        }
    }

    // 裁剪结果
    private val cropImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val croppedUri = result.data?.let { UCrop.getOutput(it) }
                if (croppedUri != null) {
                    viewModel.uploadAvatar(croppedUri)
                } else {
                    showToast("裁剪失败")
                }
            }
            UCrop.RESULT_ERROR -> {
                val error = result.data?.let { UCrop.getError(it) }
                showToast("裁剪失败: ${error?.message}")
            }
            else -> {
                android.util.Log.d("PersonalHome", "用户取消裁剪")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupToolbar()              //设置返回按钮和更多按钮
        setupAvatarClick()          //设置头像点击事件
        setupViewPager()            //设置 ViewPager 和 TabLayout
        observeViewModel()          //观察 ViewModel 数据变化
        viewModel.loadUserInfo()    // 加载个人主页用户数据
    }

    // 设置 ViewPager 和 TabLayout
    private fun setupViewPager() {
        val adapter = PersonalHomePagerAdapter(this)  // 传入 Fragment
        binding.viewPager.adapter = adapter

        // 使用 TabLayoutMediator 连接 TabLayout 和 ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    // 观察 ViewModel 数据变化
    private fun observeViewModel() {
        // 观察用户信息
        viewModel.userInfo.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    resource.data?.let { updateUI(it) }
                }
                is Resource.Error -> {
                    showToast(resource.message ?: "加载失败")
                }
            }
        }

        // 观察头像上传状态
        viewModel.avatarUploadStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showToast("正在上传头像...")
                }
                is Resource.Success -> {}
                is Resource.Error -> {
                    showToast(resource.message ?: "上传失败")
                }
            }
        }

        // 观察 Toast 消息
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }

    // 更新 UI
    private fun updateUI(userInfo: com.example.tiltok_xsb.data.model.UserInfo) {
        with(binding.homeHeader) {
            // 昵称
            tvNickname.text = userInfo.nickname
            // 个性签名
            tvSign.text = userInfo.signature
            // 统计数据
            tvGetLikeCount.text = viewModel.formatCount(userInfo.likesCount)
            tvFocusCount.text = viewModel.formatCount(userInfo.followingCount)
            tvFansCount.text = viewModel.formatCount(userInfo.fansCount)

            // 加载头像
            Glide.with(requireContext())
                .load(userInfo.avatarUrl.ifEmpty { R.mipmap.default_avatar })
                .circleCrop()
                .placeholder(R.mipmap.default_avatar)
                .error(R.mipmap.default_avatar)
                .into(ivHead)
        }

        // 加载背景图
        Glide.with(requireContext())
            .load(userInfo.backgroundUrl.ifEmpty { R.drawable.personal_home_background })
            .placeholder(R.drawable.personal_home_background)
            .into(binding.ivBg)
    }

    private fun setupToolbar() {
        binding.ivReturn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.ivMore.setOnClickListener {
            showToast("更多功能待实现")
        }
    }

    // 设置头像点击事件
    private fun setupAvatarClick() {
        binding.homeHeader.ivHead.setOnClickListener {
            android.util.Log.d("PersonalHome", "点击头像，显示选择对话框")
            showAvatarChooseDialog()
        }
    }

    // 显示头像选择对话框
    private fun showAvatarChooseDialog() {
        val dialog = AvatarChooseDialog()
        dialog.setOnChooseListener(object : AvatarChooseDialog.OnChooseListener {
            override fun onCamera() {
                checkCameraPermission()
            }

            override fun onGallery() {
                checkStoragePermission()
            }
        })
        dialog.show(childFragmentManager, "AvatarChooseDialog")
    }

    // 检查相机权限
    private fun checkCameraPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 检查存储权限
    private fun checkStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            openGallery()
        } else {
            storagePermissionLauncher.launch(permission)
        }
    }

    // 打开相册
    private fun openGallery() {
        try {
            pickImageLauncher.launch("image/*")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("打开相册失败: ${e.message}")
        }
    }

    // 打开相机
    private fun openCamera() {
        try {
            val photoFile = ImageUtils.createTempImageFile(requireContext())

            tempPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )

            takePictureLauncher.launch(tempPhotoUri)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("打开相机失败: ${e.message}")
        }
    }

    // 启动裁剪
    private fun startCrop(sourceUri: Uri) {
        try {
            val destinationUri = Uri.fromFile(
                File(requireContext().cacheDir, "cropped_avatar_${System.currentTimeMillis()}.jpg")
            )

            val options = UCrop.Options().apply {
                setCompressionQuality(80)           //压缩质量
                setHideBottomControls(false)
                setFreeStyleCropEnabled(false)
                setCircleDimmedLayer(true)          //圆形遮罩层
                setShowCropFrame(false)
                setShowCropGrid(false)
                setToolbarTitle("裁剪头像")
                setToolbarColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                setStatusBarColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                setActiveControlsWidgetColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                setToolbarWidgetColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }

            val uCrop = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)                //1:1 裁剪比例
                .withMaxResultSize(800, 800)     //最大输出尺寸
                .withOptions(options)

            cropImageLauncher.launch(uCrop.getIntent(requireContext()))

        } catch (e: Exception) {
            e.printStackTrace()
            showToast("启动裁剪失败: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

