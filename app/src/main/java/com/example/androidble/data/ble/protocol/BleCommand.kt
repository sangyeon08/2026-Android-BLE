package com.example.androidble.data.ble.protocol

class BleCommand(val cmd: String, val data: ByteArray? = null) {
    fun toByteArray(): ByteArray {
        val body = cmd.toByteArray() + (data ?: byteArrayOf()) // 명령어 두글자랑 데이터 더한 값
        val x = body.size
        val bod1 = (x and 0x3f) + 0x0A // 64로 나눈 나머지
        val bod2 = ((x shr 6) and 0x3f) + 0x0A // 몫
        var sum = 0x68 + bod1 + bod2
        for (b in body) { sum += b.toInt() and 0xFF } // 마지막에 음수 방지하기
        val crc = (sum and 0x3f) + 0x0A // 체크섬
        return byteArrayOf(0x02, 0x68, bod1.toByte(), bod2.toByte()) + body + byteArrayOf(crc.toByte(), 0x03)
    }

    companion object {
        fun fromByteArray(bytes: ByteArray): BleCommand? {
            if (bytes.size < 8) return null // 최소한 필요한 애들 모아둔게 8
            val cmd = String(bytes, 4, 2, Charsets.US_ASCII)
            val data = bytes.copyOfRange(6, bytes.size - 2) // CRC, ETX 빼고 DATA만 계산해주기
            val command = BleCommand(cmd, if (data.isEmpty()) null else data)
            return if (command.toByteArray().contentEquals(bytes)) command else null
        }
    }
}
