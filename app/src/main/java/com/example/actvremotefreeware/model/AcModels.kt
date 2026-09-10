package com.example.actvremotefreeware.model

/**
 * Supported Air Conditioner brands in the application.
 */
enum class AcBrand(val displayName: String, val protocolName: String, val defaultTemp: Int = 24) {
    HITACHI("Hitachi", "Hitachi 38kHz Multi-byte Frame"),
    LLOYD("Lloyd", "Lloyd / Coolix 48-bit Inverted"),
    LG("LG", "LG AC 28-bit / Sequence 2"),
    VOLTAS("Voltas", "Voltas Sequence 3 / Midea Frame"),
    O_GENERAL("O General", "Fujitsu General 128-bit"),
    CARRIER("Carrier", "Carrier Dynamic Frame & Checksum"),
    SAMSUNG("Samsung", "Samsung Sequence 1 (38kHz)"),
    DAIKIN("Daikin", "Daikin Dual-Frame (38kHz)"),
    PANASONIC("Panasonic", "Panasonic Sequence 3 (38kHz)"),
    WHIRLPOOL("Whirlpool", "Whirlpool Sequence 1 (38kHz)"),
    GREE_MIDEA("Gree / Midea", "Gree 64-bit Modulo-8")
}

/**
 * Operating modes for the Air Conditioner.
 */
enum class AcMode(val displayName: String) {
    COOL("Cool"),
    HEAT("Heat"),
    DRY("Dry"),
    FAN("Fan"),
    AUTO("Auto")
}

/**
 * Fan speeds.
 */
enum class FanSpeed(val displayName: String, val level: Int) {
    AUTO("Auto", 0),
    LOW("Low", 1),
    MED("Med", 2),
    HIGH("High", 3)
}

/**
 * Complete machine state of the Air Conditioner.
 */
data class AcState(
    val brand: AcBrand = AcBrand.CARRIER,
    val power: Boolean = true,
    val temp: Int = 24, // 16 to 30 °C
    val mode: AcMode = AcMode.COOL,
    val fanSpeed: FanSpeed = FanSpeed.AUTO,
    val swing: Boolean = false,
    val turbo: Boolean = false,
    val eco: Boolean = false
) {
    init {
        require(temp in 16..30) { "Temperature must be between 16 and 30 °C" }
    }
}

/**
 * Result of an IR transmission attempt.
 */
data class IrTransmissionResult(
    val success: Boolean,
    val brand: AcBrand,
    val frequency: Int,
    val patternLength: Int,
    val isSimulated: Boolean,
    val message: String,
    val patternPreview: String,
    val timestamp: Long = System.currentTimeMillis()
)
