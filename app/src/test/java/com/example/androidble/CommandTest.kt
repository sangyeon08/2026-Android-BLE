package com.example.androidble

import junit.framework.TestCase.assertEquals
import com.example.androidble.data.ble.protocol.BleCommand
import org.junit.Test
class CommandTest {

    @Test
    fun test1(){
        val array1 = BleCommand("SV").toByteArray()
        assertEquals(
            array1.joinToString(" "){ String.format("%02x", it)},
            "02 68 0c 0a 53 56 31 03"
        )
    }

    @Test
    fun test2(){
        val array1 = BleCommand("DS", byteArrayOf(0x30, 0x30)).toByteArray()
        assertEquals(
            array1.joinToString(" "){ String.format("%02x", it)},
            "02 68 0e 0a 44 53 30 30 41 03"
        )
    }
}