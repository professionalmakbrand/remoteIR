package com.maahi.iractvremote.data

import android.content.Context
import android.content.SharedPreferences
import com.maahi.iractvremote.model.AcBrand
import com.maahi.iractvremote.model.AcMode
import com.maahi.iractvremote.model.AcState
import com.maahi.iractvremote.model.ApplianceType
import com.maahi.iractvremote.model.FanSpeed
import com.maahi.iractvremote.model.TvBrand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Entity representing a user-configured saved remote (AC or TV).
 */
data class SavedRemote(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val room: String,
    val brand: AcBrand = AcBrand.HITACHI,
    val codeIndex: Int = 0,
    val state: AcState = AcState(brand = brand),
    val applianceType: ApplianceType = ApplianceType.AC,
    val tvBrand: TvBrand? = null,
    val codeDescription: String = ""
)

/**
 * Persistent repository managing saved remotes.
 */
class SavedRemotesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _remotes = MutableStateFlow<List<SavedRemote>>(emptyList())
    val remotes: StateFlow<List<SavedRemote>> = _remotes.asStateFlow()

    init {
        loadRemotes()
    }

    fun saveRemote(
        name: String,
        room: String,
        brand: AcBrand,
        codeIndex: Int,
        codeDescription: String = ""
    ): SavedRemote {
        val remote = SavedRemote(
            id = UUID.randomUUID().toString(),
            name = name,
            room = room,
            brand = brand,
            codeIndex = codeIndex,
            state = AcState(brand = brand, temp = brand.defaultTemp),
            applianceType = ApplianceType.AC,
            codeDescription = codeDescription
        )
        val updated = _remotes.value.toMutableList().apply { add(remote) }
        persistList(updated)
        return remote
    }

    fun saveTvRemote(
        name: String,
        room: String,
        tvBrand: TvBrand,
        codeIndex: Int,
        codeDescription: String = ""
    ): SavedRemote {
        val remote = SavedRemote(
            id = UUID.randomUUID().toString(),
            name = name,
            room = room,
            codeIndex = codeIndex,
            applianceType = ApplianceType.TV,
            tvBrand = tvBrand,
            codeDescription = codeDescription
        )
        val updated = _remotes.value.toMutableList().apply { add(remote) }
        persistList(updated)
        return remote
    }

    fun importRemote(remote: SavedRemote): SavedRemote {
        val newRemote = remote.copy(id = UUID.randomUUID().toString())
        val updated = _remotes.value.toMutableList().apply { add(newRemote) }
        persistList(updated)
        return newRemote
    }

    fun updateRemoteState(id: String, newState: AcState) {
        val updated = _remotes.value.map {
            if (it.id == id) it.copy(state = newState) else it
        }
        persistList(updated)
    }

    fun updateRemoteCodeIndex(id: String, newCodeIndex: Int, newDescription: String) {
        val updated = _remotes.value.map {
            if (it.id == id) it.copy(codeIndex = newCodeIndex, codeDescription = newDescription) else it
        }
        persistList(updated)
    }

    fun deleteRemote(id: String) {
        val updated = _remotes.value.filter { it.id != id }
        persistList(updated)
    }

    fun getRemote(id: String): SavedRemote? {
        return _remotes.value.find { it.id == id }
    }

    private fun loadRemotes() {
        val rawJson = prefs.getString(KEY_REMOTES, null)
        if (rawJson.isNullOrBlank()) {
            _remotes.value = emptyList()
            return
        }

        try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<SavedRemote>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeName = obj.optString("applianceType", ApplianceType.AC.name)
                val applianceType = try { ApplianceType.valueOf(typeName) } catch (_: Exception) { ApplianceType.AC }

                if (applianceType == ApplianceType.TV) {
                    val tvBrandName = obj.optString("tvBrand", TvBrand.SAMSUNG.name)
                    val tvBrand = try { TvBrand.valueOf(tvBrandName) } catch (_: Exception) { TvBrand.SAMSUNG }
                    list.add(
                        SavedRemote(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            name = obj.optString("name", "${tvBrand.displayName} TV"),
                            room = obj.optString("room", "Living Room"),
                            codeIndex = obj.optInt("codeIndex", 0),
                            applianceType = ApplianceType.TV,
                            tvBrand = tvBrand,
                            codeDescription = obj.optString("codeDescription", "")
                        )
                    )
                } else {
                    val brandName = obj.optString("brand", AcBrand.HITACHI.name)
                    val brand = try { AcBrand.valueOf(brandName) } catch (_: Exception) { AcBrand.HITACHI }
                    val modeName = obj.optString("mode", AcMode.COOL.name)
                    val mode = try { AcMode.valueOf(modeName) } catch (_: Exception) { AcMode.COOL }
                    val fanName = obj.optString("fan", FanSpeed.AUTO.name)
                    val fan = try { FanSpeed.valueOf(fanName) } catch (_: Exception) { FanSpeed.AUTO }

                    val state = AcState(
                        brand = brand,
                        power = obj.optBoolean("power", true),
                        temp = obj.optInt("temp", 24).coerceIn(16, 30),
                        mode = mode,
                        fanSpeed = fan,
                        swing = obj.optBoolean("swing", false),
                        turbo = obj.optBoolean("turbo", false)
                    )

                    list.add(
                        SavedRemote(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            name = obj.optString("name", "${brand.displayName} AC"),
                            room = obj.optString("room", "Bedroom"),
                            brand = brand,
                            codeIndex = obj.optInt("codeIndex", 0),
                            state = state,
                            applianceType = ApplianceType.AC,
                            codeDescription = obj.optString("codeDescription", "")
                        )
                    )
                }
            }
            _remotes.value = list
        } catch (_: Exception) {
            _remotes.value = emptyList()
        }
    }

    private fun persistList(list: List<SavedRemote>) {
        _remotes.value = list
        val array = JSONArray()
        for (remote in list) {
            val obj = JSONObject().apply {
                put("id", remote.id)
                put("name", remote.name)
                put("room", remote.room)
                put("applianceType", remote.applianceType.name)
                put("codeIndex", remote.codeIndex)
                put("codeDescription", remote.codeDescription)

                if (remote.applianceType == ApplianceType.TV) {
                    put("tvBrand", remote.tvBrand?.name ?: TvBrand.SAMSUNG.name)
                } else {
                    put("brand", remote.brand.name)
                    put("power", remote.state.power)
                    put("temp", remote.state.temp)
                    put("mode", remote.state.mode.name)
                    put("fan", remote.state.fanSpeed.name)
                    put("swing", remote.state.swing)
                    put("turbo", remote.state.turbo)
                }
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_REMOTES, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "saved_remotes_prefs"
        private const val KEY_REMOTES = "key_saved_remotes"
    }
}
