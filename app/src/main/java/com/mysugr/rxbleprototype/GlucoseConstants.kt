package com.mysugr.rxbleprototype

import java.util.UUID

object GlucometerConstants {
	val SERVICE_DEVICE_GLUCOSE_UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb")
	val SERVICE_DEVICE_INFORMATION_UUID = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb")

	val CHARACTERISTIC_BG_MEASUREMENT_UUID = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb")
	val CHARACTERISTIC_Record_Access_Control_Point_UUID = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb")
	val CHARACTERISTIC_CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
	val CHARACTERISTIC_BG_CONTEXT_UUID = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb")
	val CHARACTERISTIC_DEVICE_SERIALNUMBER = UUID.fromString("00002A25-0000-1000-8000-00805f9b34fb")

	val AVIVA_CONNECT_PLIST_ID = "roche.accuchek.aviva.connect";
	val PERFORMA_CONNECT_PLIST_ID = "roche.accuchek.performa.connect";
	val BG5_PLIST_ID = "ihealth.wireless.smart"
	val GL50_EVO_PLIST_ID = "beurer.gl50.evo"
	const val Abbott_Freestyle_Libre = "abbott.freestyle.libre"


	fun uuidToShortCode(uuid: UUID): String {
		return uuid.toString().substring(4, 8)
	}
}
