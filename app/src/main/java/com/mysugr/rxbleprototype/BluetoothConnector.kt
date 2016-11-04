package com.mysugr.rxbleprototype

import android.content.Context
import android.util.Log
import com.mysugr.logbook.hardware.glucometers.GlucoseObject
import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.RxBleDevice
import com.polidea.rxandroidble.RxBleScanResult
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject

/**
 * Created by pmessenger on 21.10.16.
 */

class BluetoothConnector(private val context: Context) {

	private val client: RxBleClient

	private val importCompleteSubject: PublishSubject<Unit> = PublishSubject.create()

	private val connectionSubject: PublishSubject<RxBleConnection> = PublishSubject.create()

	val connectionObservable: Observable<RxBleConnection>
		get() = connectionSubject.asObservable()

	init {
		client = RxBleClient.create(context)
	}

	companion object {
		private const val TAG = "BluetoothConnector"
		private const val BEURER_ID = "Beurer GL50EVO"
		private const val ACCU_CHEK_ID = "Accu-Chek"
		private const val MAC_ADDRESS = "00:60:19:61:69:33"

	}

	private var scanSubscription: Subscription? = null
	private var readDataSubscription: Subscription? = null

	fun scan() {

		scanSubscription = client.scanBleDevices()
				.doOnError { e -> Log.d(TAG, e.message) }
				.subscribe { r -> onScanResultReceived(r) }
	}

	private fun onScanResultReceived(r: RxBleScanResult) {
		Log.d(TAG, "ScanResult: " + r.bleDevice.name)

		if (r.bleDevice.name == ACCU_CHEK_ID || r.bleDevice.name == BEURER_ID) {
			scanSubscription?.unsubscribe()
			connect(r.bleDevice)
		}
	}

	fun connect() {
		val macAddress = MAC_ADDRESS
		val device = client.getBleDevice(macAddress)
		connect(device)
	}

	private var connectionSubscription: Subscription? = null

	private fun connect(device: RxBleDevice) {

		Log.d(TAG, "MAC ADDRESS: ${device.macAddress}")

//		device.establishConnection(context, false)
//				.doOnError { e -> Log.d(TAG, e.message) }
//				.flatMap { c -> c.readCharacteristic(GlucometerConstants.CHARACTERISTIC_DEVICE_SERIALNUMBER) }
//				.subscribe { c -> Log.d(TAG, "serialNr: ${c.toString(Charsets.UTF_8)}") }

		val next_seq = 0


		var previousConnectionState: RxBleConnection.RxBleConnectionState? = null

		connectionSubscription?.unsubscribe()
		connectionSubscription = device.observeConnectionStateChanges().subscribe { s ->
			if (previousConnectionState != null) {
				Log.d(TAG, "connectionStateChange: $s")
				if (s == RxBleConnection.RxBleConnectionState.DISCONNECTED && previousConnectionState == RxBleConnection.RxBleConnectionState.CONNECTED) {
					onDisconnected()
				}
			}
			previousConnectionState = s
		}

		readDataSubscription = device
				.establishConnection(context, true)
				.observeOn(AndroidSchedulers.mainThread())
				.doOnNext { c -> connectionSubject.onNext(c) }
				.doOnUnsubscribe { onImportComplete() }
				.takeUntil(importCompleteSubject)
				.flatMap { c ->

					Observable.combineLatest(
							c.setupNotification(GlucometerConstants.CHARACTERISTIC_BG_MEASUREMENT_UUID),
							c.setupNotification(GlucometerConstants.CHARACTERISTIC_BG_CONTEXT_UUID),
							c.setupIndication(GlucometerConstants.CHARACTERISTIC_Record_Access_Control_Point_UUID),
							{ a, b, d ->
								a.subscribe { r ->
									val glucoseObject = GlucoseObject.parseFromCharacteristic(r, "t", device.macAddress, "serial")
									Log.d(TAG, "result: ${glucoseObject.toString()}")

								}
								d.subscribe { r -> importCompleteSubject.onNext(Unit) }
								c
							})
				}
				.flatMap { c -> c.writeCharacteristic(GlucometerConstants.CHARACTERISTIC_Record_Access_Control_Point_UUID, byteArrayOf(0x01, 0x03, 0x01, next_seq.toByte(), (next_seq shr 8).toByte())) }
				.subscribe({ }, { e -> Log.d(TAG, e.message) })


	}


	private fun onImportComplete() {
		Log.d(TAG, "Import Complete")
	}

	private fun onDisconnected() {
		connectionSubscription?.unsubscribe()
		Log.d(TAG, "disconnected")
	}

}