package com.example.actvremotefreeware.util

import android.graphics.Bitmap
import android.graphics.Color
import com.example.actvremotefreeware.data.SavedRemote
import com.example.actvremotefreeware.model.AcBrand
import com.example.actvremotefreeware.model.AcMode
import com.example.actvremotefreeware.model.AcState
import com.example.actvremotefreeware.model.ApplianceType
import com.example.actvremotefreeware.model.FanSpeed
import com.example.actvremotefreeware.model.TvBrand
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject
import java.util.UUID

/**
 * Utility for generating and scanning OnePlus-style QR codes for sharing remote configurations.
 */
object QrCodeUtils {

    private const val APP_IDENTIFIER = "ACTVRemote"

    /**
     * Generates an Android Bitmap QR Code for the given text payload.
     */
    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /**
     * Serializes a SavedRemote into a JSON string suitable for QR encoding.
     */
    fun serializeRemote(remote: SavedRemote): String {
        val obj = JSONObject().apply {
            put("app", APP_IDENTIFIER)
            put("v", 1)
            put("type", remote.applianceType.name)
            put("name", remote.name)
            put("room", remote.room)
            put("codeIndex", remote.codeIndex)
            put("codeDesc", remote.codeDescription)

            if (remote.applianceType == ApplianceType.TV) {
                put("tvBrand", (remote.tvBrand ?: TvBrand.SAMSUNG).name)
            } else {
                put("acBrand", remote.brand.name)
                put("temp", remote.state.temp)
                put("mode", remote.state.mode.name)
                put("fan", remote.state.fanSpeed.name)
                put("power", remote.state.power)
            }
        }
        return obj.toString()
    }

    /**
     * Deserializes a QR code payload or shared text string into a SavedRemote instance.
     */
    fun deserializeRemote(rawString: String): SavedRemote? {
        return try {
            val cleanJson = rawString.trim()
            val obj = JSONObject(cleanJson)
            if (obj.optString("app") != APP_IDENTIFIER) {
                return null
            }

            val typeName = obj.optString("type", ApplianceType.AC.name)
            val applianceType = try { ApplianceType.valueOf(typeName) } catch (_: Exception) { ApplianceType.AC }
            val name = obj.optString("name", "Imported Remote")
            val room = obj.optString("room", "Home")
            val codeIndex = obj.optInt("codeIndex", 0)
            val codeDesc = obj.optString("codeDesc", "")

            if (applianceType == ApplianceType.TV) {
                val tvBrandName = obj.optString("tvBrand", TvBrand.SAMSUNG.name)
                val tvBrand = try { TvBrand.valueOf(tvBrandName) } catch (_: Exception) { TvBrand.SAMSUNG }
                SavedRemote(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    room = room,
                    codeIndex = codeIndex,
                    applianceType = ApplianceType.TV,
                    tvBrand = tvBrand,
                    codeDescription = codeDesc
                )
            } else {
                val acBrandName = obj.optString("acBrand", AcBrand.HITACHI.name)
                val acBrand = try { AcBrand.valueOf(acBrandName) } catch (_: Exception) { AcBrand.HITACHI }
                val temp = obj.optInt("temp", 24).coerceIn(16, 30)
                val mode = try { AcMode.valueOf(obj.optString("mode", AcMode.COOL.name)) } catch (_: Exception) { AcMode.COOL }
                val fan = try { FanSpeed.valueOf(obj.optString("fan", FanSpeed.AUTO.name)) } catch (_: Exception) { FanSpeed.AUTO }
                val power = obj.optBoolean("power", true)

                SavedRemote(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    room = room,
                    brand = acBrand,
                    codeIndex = codeIndex,
                    state = AcState(
                        brand = acBrand,
                        temp = temp,
                        mode = mode,
                        fanSpeed = fan,
                        power = power
                    ),
                    applianceType = ApplianceType.AC,
                    codeDescription = codeDesc
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Decodes a QR code string from a Bitmap.
     */
    fun decodeQrBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            MultiFormatReader().decode(binaryBitmap).text
        } catch (_: Exception) {
            null
        }
    }
}
