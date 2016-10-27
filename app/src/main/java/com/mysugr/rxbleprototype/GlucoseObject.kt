package com.mysugr.logbook.hardware.glucometers

import android.annotation.TargetApi
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
import android.util.Log
import com.mysugr.rxbleprototype.GlucometerConstants

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.SimpleTimeZone
import java.util.TimeZone

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class GlucoseObject private constructor() {


    var sourceType: String? = null
        private set
    var sourceMacAddress: String? = null
        private set
    var serialNumber: String? = null
        private set

    private var timeOffset = false // has local date offset
    private var glucoseConcentration = false // define is value exist
    var isMmolPerLiter = false
        private set //unit
    var sequenceNumber = 0
        private set
    var date: Date? = null
        private set

    var timeOffsetValue: Int = 0
        private set // local offset
    var glucoseValue: Float = 0.toFloat()
        private set //BG value

    override fun toString(): String {

        val dateFormatter = SimpleDateFormat()
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        return "date: " + dateFormatter.format(date) + " time offset: " + timeOffsetValue + "\n" +
                "value: " + glucoseValue + "\n" +
                "unit: " + (if (isMmolPerLiter) "mmol/dl" else "mg/dl") + "\n" +
                "sequenceNumber: " + sequenceNumber + "macAddress: " + sourceMacAddress +
                "serialNumber: " + serialNumber
    }

    companion object {

        val TAG = GlucoseObject::class.java.simpleName

        fun parseFromCharacteristic(rawByte: ByteArray, sourceType: String, sourceMacAddress: String, serialNumber: String): GlucoseObject? {

            val FACTOR_MOLPERLITER_TO_MMOLPERLITER = 1000
            val FACTOR_KGPERLITER_TO_MGPERDECILITER = 100000

            val glucoseObject = GlucoseObject()

            var i = 0
            val flag = rawByte[i]

            val characteristic = BluetoothGattCharacteristic(null, 0, 0)
            characteristic.value = rawByte

            glucoseObject.timeOffset = isBitSet(flag, 0)
            glucoseObject.glucoseConcentration = isBitSet(flag, 1)
            glucoseObject.isMmolPerLiter = isBitSet(flag, 2)
            glucoseObject.sourceType = sourceType
            glucoseObject.sourceMacAddress = sourceMacAddress
            glucoseObject.serialNumber = serialNumber

            i++
            glucoseObject.sequenceNumber = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, i)!!
            i += 2

            val year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, i)!!
            i += 2
            val month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, i)!!
            i++
            val day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, i)!!
            i++
            val hour = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, i)!!
            i++
            val min = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, i)!!
            i++
            val sec = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, i)!!

            val localSimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            localSimpleDateFormat.timeZone = SimpleTimeZone(0, "GMT")
            try {

                glucoseObject.date = localSimpleDateFormat.parse("$year-$month-$day $hour:$min:$sec")
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            i++

            if (glucoseObject.timeOffset) {
                glucoseObject.timeOffsetValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, i)!!
                i += 2
            }

            if (glucoseObject.glucoseConcentration) {
                val rawValue = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, i)!!
                val factor = if (glucoseObject.isMmolPerLiter) FACTOR_MOLPERLITER_TO_MMOLPERLITER else FACTOR_KGPERLITER_TO_MGPERDECILITER
                glucoseObject.glucoseValue = rawValue * factor
            }


            return glucoseObject
        }

        private fun isBitSet(b: Byte, position: Int): Boolean {
            return (b.toInt() shr position) and 1 == 1
        }
    }

}