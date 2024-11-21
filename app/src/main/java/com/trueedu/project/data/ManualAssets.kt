package com.trueedu.project.data

import android.util.Log
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
class ManualAssets @Inject constructor(
    private val firebaseAssets: FirebaseAssetsManager,
    private val googleAccount: GoogleAccount,
) {
    companion object {
        private val TAG = ManualAssets::class.java.simpleName
    }

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
        Log.d(TAG, "load()")
        CoroutineScope(Dispatchers.IO).launch {
            val assetList = firebaseAssets.loadAssets()
            Log.d(TAG, "assetList: $assetList")
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

    fun deleteAsset(code: String, onSuccess: () -> Unit) {
        val assetList = assets.value
            .filterNot { it.code == code }
        assets.value = assetList

        CoroutineScope(Dispatchers.IO).launch {
            firebaseAssets.writeAssets(assets.value)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun get(code: String): UserAsset? {
        return assets.value.firstOrNull { it.code == code }
    }
}
