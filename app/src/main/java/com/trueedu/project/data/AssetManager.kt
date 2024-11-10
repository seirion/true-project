package com.trueedu.project.data

import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.data.firebase.FirebaseAssetsManager
import com.trueedu.project.model.dto.firebase.UserAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetManager @Inject constructor(
    private val firebaseAssets: FirebaseAssetsManager,
    private val googleAccount: GoogleAccount,
) {
    val assets = mutableStateOf<List<UserAsset>>(emptyList())
    init {
        MainScope().launch {
            googleAccount.loginSignal
                .collect {
                    if (it) {
                        // login
                        load()
                    } else {
                        // logout
                        assets.value = emptyList()
                    }
                }
        }
    }

    private fun load() {
        CoroutineScope(Dispatchers.IO).launch {
            val assetList = firebaseAssets.loadAssets()
            withContext(Dispatchers.Main) {
                assets.value = assetList
            }
        }
    }

    fun addAsset(asset: UserAsset, onSuccess: () -> Unit) {
        val assetList = assets.value
            .filterNot { it.code == asset.code }
        assets.value = assetList + asset

        CoroutineScope(Dispatchers.IO).launch {
            firebaseAssets.writeAssets(assets.value)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
