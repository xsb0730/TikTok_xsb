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
import com.example.tiltok_xsb.ui.view.AvatarChooseDialog
import com.example.tiltok_xsb.ui.viewmodel.PersonalHomeViewModel
import com.example.tiltok_xsb.utils.ImageUtils
import com.example.tiltok_xsb.utils.Resource
import com.yalantis.ucrop.UCrop
import java.io.File


class PersonalHomeFragment : BaseBindingFragment<FragmentPersonalHomeBinding>({ FragmentPersonalHomeBinding.inflate(it) }) {
    private val viewModel: PersonalHomeViewModel by viewModels()

    private var tempPhotoUri: Uri? = null

    // 相机权限请求
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else showToast("需要相机权限才能拍照")
    }

    // 存储权限请求
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openGallery()
        else showToast("需要存储权限才能选择图片")
    }

    // 拍照结果
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            startCrop(tempPhotoUri!!)
        }
    }

    // 图库选择结果
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { startCrop(it) }
    }

    // 裁剪结果
    private val cropImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedUri = UCrop.getOutput(result.data!!)
            croppedUri?.let {
                // ✅ 通过 ViewModel 上传头像
                viewModel.uploadAvatar(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupAvatarClick()
        observeViewModel()

        // ✅ 加载用户数据
        viewModel.loadUserInfo()
    }

    // ✅ 观察 ViewModel 数据变化
    private fun observeViewModel() {
        // 观察用户信息
        viewModel.userInfo.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 显示加载状态
                }
                is Resource.Success -> {
                    resource.data?.let { userInfo ->
                        updateUI(userInfo)
                    }
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
                    // 显示上传进度
                    showToast("正在上传头像...")
                }
                is Resource.Success -> {
                    // 上传成功，头像已在 userInfo 中更新
                }
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

    // ✅ 更新 UI
    private fun updateUI(userInfo: com.example.tiltok_xsb.data.model.UserInfo) {
        with(binding.homeHeader) {
            // 昵称
            tvNickname.text = userInfo.nickname

            // 抖音号
            val douyinIdText = "抖音号: ${userInfo.douyinId}"
            // TODO: 设置抖音号到对应的 TextView

            // 个性签名
            tvSign.text = userInfo.signature

            // 年龄和地区（这里需要使用 tools:text，实际数据通过代码设置）
            // tvAge.text = "${userInfo.age}岁"
            // tvLocation.text = userInfo.location

            // 统计数据
            tvGetLikeCount.text = viewModel.formatCount(userInfo.likesCount)
            tvFocusCount.text = viewModel.formatCount(userInfo.followingCount)
            tvFansCount.text = viewModel.formatCount(userInfo.fansCount)

            // 加载头像
            Glide.with(requireContext())
                .load(userInfo.avatarUrl.ifEmpty { R.mipmap.default_avatar })
                .circleCrop()
                .placeholder(R.mipmap.default_avatar)
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

    private fun setupAvatarClick() {
        binding.homeHeader.ivHead.setOnClickListener {
            showAvatarChooseDialog()
        }
    }

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

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openGallery()
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }
                else -> {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openCamera() {
        val photoFile = ImageUtils.createTempImageFile(requireContext())
        tempPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(tempPhotoUri)
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(requireContext().cacheDir, "cropped_avatar_${System.currentTimeMillis()}.jpg")
        )

        val options = UCrop.Options().apply {
            setCompressionQuality(80)
            setHideBottomControls(false)
            setFreeStyleCropEnabled(false)
            setCircleDimmedLayer(true)
            setShowCropFrame(false)
            setShowCropGrid(false)
            setToolbarTitle("裁剪头像")
        }

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .withOptions(options)

        cropImageLauncher.launch(uCrop.getIntent(requireContext()))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

