package com.maahi.iractvremote.core.ir.tv

import com.maahi.iractvremote.model.TvBrand
import com.maahi.iractvremote.model.TvCommand

/**
 * Registry mapping TV brands to candidate code sets and command infrared pulse generation.
 */
object TvBrandCodeRegistry {

    fun getCodeCount(brand: TvBrand): Int {
        return when (brand) {
            TvBrand.SAMSUNG -> 2
            TvBrand.LG -> 2
            TvBrand.SONY -> 2
            TvBrand.XIAOMI -> 4
            TvBrand.ONEPLUS -> 2
            TvBrand.TCL -> 2
            TvBrand.PANASONIC -> 2
            TvBrand.PHILIPS -> 2
            TvBrand.TOSHIBA -> 2
            TvBrand.VU -> 2
            TvBrand.MICROMAX -> 2
            TvBrand.LLOYD_TV -> 2
            TvBrand.HAIER -> 2
            TvBrand.SANSUI -> 2
        }
    }

    fun getCodeLabel(brand: TvBrand, index: Int): String {
        return when (brand) {
            TvBrand.SAMSUNG -> when (index) {
                0 -> "Code 1: Samsung Smart TV (Tizen / LED)"
                else -> "Code 2: Samsung Classic LCD / CRT"
            }
            TvBrand.LG -> when (index) {
                0 -> "Code 1: LG WebOS Smart TV"
                else -> "Code 2: LG / Goldstar Classic"
            }
            TvBrand.SONY -> when (index) {
                0 -> "Code 1: Sony Bravia (SIRC 12-bit)"
                else -> "Code 2: Sony Bravia / OLED (SIRC 15-bit)"
            }
            TvBrand.XIAOMI -> when (index) {
                0 -> "Code 1: Mi Smart TV (PatchWall)"
                1 -> "Code 2: Xiaomi Android TV"
                2 -> "Code 3: Mi Box 4K / TV Stick (Xiaomi Protocol)"
                else -> "Code 4: Mi Box / TV Box (NEC 0x80 Alternative)"
            }
            TvBrand.ONEPLUS -> when (index) {
                0 -> "Code 1: OnePlus TV Y / U Series"
                else -> "Code 2: OnePlus TV Q Series"
            }
            TvBrand.TCL -> when (index) {
                0 -> "Code 1: TCL Smart TV / Roku"
                else -> "Code 2: TCL Classic"
            }
            TvBrand.PANASONIC -> when (index) {
                0 -> "Code 1: Panasonic Viera Series"
                else -> "Code 2: Panasonic Classic LCD"
            }
            TvBrand.PHILIPS -> when (index) {
                0 -> "Code 1: Philips Smart TV (NEC)"
                else -> "Code 2: Philips Classic (RC5 36kHz)"
            }
            TvBrand.TOSHIBA -> when (index) {
                0 -> "Code 1: Toshiba Regza / Android"
                else -> "Code 2: Toshiba Classic"
            }
            TvBrand.VU -> when (index) {
                0 -> "Code 1: Vu Smart TV (Android)"
                else -> "Code 2: Vu Classic LED"
            }
            TvBrand.MICROMAX -> when (index) {
                0 -> "Code 1: Micromax Canvas Smart TV"
                else -> "Code 2: Micromax Classic"
            }
            TvBrand.LLOYD_TV -> when (index) {
                0 -> "Code 1: Lloyd Smart TV"
                else -> "Code 2: Lloyd Classic LED"
            }
            TvBrand.HAIER -> when (index) {
                0 -> "Code 1: Haier Smart TV"
                else -> "Code 2: Haier Classic"
            }
            TvBrand.SANSUI -> when (index) {
                0 -> "Code 1: Sansui Smart TV"
                else -> "Code 2: Sansui Classic"
            }
        }
    }

    fun encode(brand: TvBrand, command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        return when (brand) {
            TvBrand.SAMSUNG -> encodeSamsung(command, codeIndex)
            TvBrand.LG -> encodeLg(command, codeIndex)
            TvBrand.SONY -> encodeSony(command, codeIndex)
            TvBrand.XIAOMI -> encodeXiaomi(command, codeIndex)
            TvBrand.ONEPLUS -> encodeOnePlus(command, codeIndex)
            TvBrand.TCL -> encodeTcl(command, codeIndex)
            TvBrand.PANASONIC -> encodePanasonic(command, codeIndex)
            TvBrand.PHILIPS -> encodePhilips(command, codeIndex)
            TvBrand.TOSHIBA -> encodeToshiba(command, codeIndex)
            TvBrand.VU -> encodeVu(command, codeIndex)
            TvBrand.MICROMAX -> encodeMicromax(command, codeIndex)
            TvBrand.LLOYD_TV -> encodeLloydTv(command, codeIndex)
            TvBrand.HAIER -> encodeHaier(command, codeIndex)
            TvBrand.SANSUI -> encodeSansui(command, codeIndex)
        }
    }

    private fun encodeSamsung(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x0707 else 0x0101
        val cmd = when (command) {
            TvCommand.POWER -> 0x02
            TvCommand.MUTE -> 0x0F
            TvCommand.VOL_UP -> 0x07
            TvCommand.VOL_DOWN -> 0x0B
            TvCommand.CH_UP -> 0x12
            TvCommand.CH_DOWN -> 0x10
            TvCommand.UP -> 0x60
            TvCommand.DOWN -> 0x61
            TvCommand.LEFT -> 0x65
            TvCommand.RIGHT -> 0x62
            TvCommand.OK -> 0x68
            TvCommand.BACK -> 0x58
            TvCommand.HOME -> 0x79
            TvCommand.MENU -> 0x1A
            TvCommand.INPUT -> 0x01
            TvCommand.DIGIT_0 -> 0x11
            TvCommand.DIGIT_1 -> 0x04
            TvCommand.DIGIT_2 -> 0x05
            TvCommand.DIGIT_3 -> 0x06
            TvCommand.DIGIT_4 -> 0x08
            TvCommand.DIGIT_5 -> 0x09
            TvCommand.DIGIT_6 -> 0x0A
            TvCommand.DIGIT_7 -> 0x0C
            TvCommand.DIGIT_8 -> 0x0D
            TvCommand.DIGIT_9 -> 0x0E
        }
        return TvProtocols.encodeSamsung(address, cmd)
    }

    private fun encodeLg(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x04 else 0x00
        val cmd = when (command) {
            TvCommand.POWER -> 0x08
            TvCommand.MUTE -> 0x09
            TvCommand.VOL_UP -> 0x02
            TvCommand.VOL_DOWN -> 0x03
            TvCommand.CH_UP -> 0x00
            TvCommand.CH_DOWN -> 0x01
            TvCommand.UP -> 0x40
            TvCommand.DOWN -> 0x41
            TvCommand.LEFT -> 0x07
            TvCommand.RIGHT -> 0x06
            TvCommand.OK -> 0x44
            TvCommand.BACK -> 0x28
            TvCommand.HOME -> 0x7C
            TvCommand.MENU -> 0x43
            TvCommand.INPUT -> 0x0B
            TvCommand.DIGIT_0 -> 0x10
            TvCommand.DIGIT_1 -> 0x11
            TvCommand.DIGIT_2 -> 0x12
            TvCommand.DIGIT_3 -> 0x13
            TvCommand.DIGIT_4 -> 0x14
            TvCommand.DIGIT_5 -> 0x15
            TvCommand.DIGIT_6 -> 0x16
            TvCommand.DIGIT_7 -> 0x17
            TvCommand.DIGIT_8 -> 0x18
            TvCommand.DIGIT_9 -> 0x19
        }
        return TvProtocols.encodeNec(address, cmd)
    }

    private fun encodeSony(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val bits = if (codeIndex == 0) 12 else 15
        val address = 1 // TV address in Sony SIRC
        val cmd = when (command) {
            TvCommand.POWER -> 0x15
            TvCommand.MUTE -> 0x14
            TvCommand.VOL_UP -> 0x12
            TvCommand.VOL_DOWN -> 0x13
            TvCommand.CH_UP -> 0x10
            TvCommand.CH_DOWN -> 0x11
            TvCommand.UP -> 0x74
            TvCommand.DOWN -> 0x75
            TvCommand.LEFT -> 0x34
            TvCommand.RIGHT -> 0x33
            TvCommand.OK -> 0x65
            TvCommand.BACK -> 0x23
            TvCommand.HOME -> 0x60
            TvCommand.MENU -> 0x60
            TvCommand.INPUT -> 0x25
            TvCommand.DIGIT_0 -> 0x09
            TvCommand.DIGIT_1 -> 0x00
            TvCommand.DIGIT_2 -> 0x01
            TvCommand.DIGIT_3 -> 0x02
            TvCommand.DIGIT_4 -> 0x03
            TvCommand.DIGIT_5 -> 0x04
            TvCommand.DIGIT_6 -> 0x05
            TvCommand.DIGIT_7 -> 0x06
            TvCommand.DIGIT_8 -> 0x07
            TvCommand.DIGIT_9 -> 0x08
        }
        return TvProtocols.encodeSony(address, cmd, bits)
    }

    private fun encodeXiaomi(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        return when (codeIndex) {
            2 -> MiBoxProtocol.encode(command)
            3 -> encodeUniversalNec(0x80, command)
            1 -> encodeUniversalNec(0x08, command)
            else -> encodeUniversalNec(0x00, command)
        }
    }

    private fun encodeOnePlus(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x40 else 0x02
        return encodeUniversalNec(address, command)
    }

    private fun encodeTcl(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x00 else 0x40
        return encodeUniversalNec(address, command)
    }

    private fun encodePanasonic(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x01 else 0x08
        return encodeUniversalNec(address, command)
    }

    private fun encodePhilips(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        if (codeIndex == 1) {
            val cmd = when (command) {
                TvCommand.POWER -> 0x0C
                TvCommand.MUTE -> 0x0D
                TvCommand.VOL_UP -> 0x10
                TvCommand.VOL_DOWN -> 0x11
                TvCommand.CH_UP -> 0x20
                TvCommand.CH_DOWN -> 0x21
                else -> 0x0C
            }
            return TvProtocols.encodeRc5(0x00, cmd)
        }
        return encodeUniversalNec(0x00, command)
    }

    private fun encodeToshiba(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x02 else 0x40
        return encodeUniversalNec(address, command)
    }

    private fun encodeVu(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x00 else 0x04
        return encodeUniversalNec(address, command)
    }

    private fun encodeMicromax(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x00 else 0x20
        return encodeUniversalNec(address, command)
    }

    private fun encodeLloydTv(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x00 else 0x04
        return encodeUniversalNec(address, command)
    }

    private fun encodeHaier(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x00 else 0x10
        return encodeUniversalNec(address, command)
    }

    private fun encodeSansui(command: TvCommand, codeIndex: Int): Pair<Int, IntArray> {
        val address = if (codeIndex == 0) 0x00 else 0x04
        return encodeUniversalNec(address, command)
    }

    private fun encodeUniversalNec(address: Int, command: TvCommand): Pair<Int, IntArray> {
        val cmd = when (command) {
            TvCommand.POWER -> 0x12
            TvCommand.MUTE -> 0x10
            TvCommand.VOL_UP -> 0x1A
            TvCommand.VOL_DOWN -> 0x1E
            TvCommand.CH_UP -> 0x1B
            TvCommand.CH_DOWN -> 0x1F
            TvCommand.UP -> 0x06
            TvCommand.DOWN -> 0x07
            TvCommand.LEFT -> 0x08
            TvCommand.RIGHT -> 0x09
            TvCommand.OK -> 0x0A
            TvCommand.BACK -> 0x14
            TvCommand.HOME -> 0x15
            TvCommand.MENU -> 0x16
            TvCommand.INPUT -> 0x0F
            TvCommand.DIGIT_0 -> 0x00
            TvCommand.DIGIT_1 -> 0x01
            TvCommand.DIGIT_2 -> 0x02
            TvCommand.DIGIT_3 -> 0x03
            TvCommand.DIGIT_4 -> 0x04
            TvCommand.DIGIT_5 -> 0x05
            TvCommand.DIGIT_6 -> 0x06
            TvCommand.DIGIT_7 -> 0x07
            TvCommand.DIGIT_8 -> 0x08
            TvCommand.DIGIT_9 -> 0x09
        }
        return TvProtocols.encodeNec(address, cmd)
    }
}
