package com.reactnativeflybuy

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.radiusnetworks.flybuy.sdk.FlyBuyCore
import com.radiusnetworks.flybuy.sdk.data.common.Pagination
import com.radiusnetworks.flybuy.sdk.data.customer.CustomerInfo
import com.radiusnetworks.flybuy.sdk.data.location.CircularRegion
import com.radiusnetworks.flybuy.sdk.data.pickup_config.PickupConfig
import com.radiusnetworks.flybuy.sdk.data.pickup_config.PickupTypeConfig
import com.radiusnetworks.flybuy.sdk.data.room.domain.Customer
import com.radiusnetworks.flybuy.sdk.data.room.domain.Order
import com.radiusnetworks.flybuy.sdk.data.room.domain.PickupWindow
import com.radiusnetworks.flybuy.sdk.data.room.domain.Site
import com.radiusnetworks.flybuy.sdk.notify.NotificationInfo
import com.radiusnetworks.flybuy.sdk.notify.NotifyManager
import com.radiusnetworks.flybuy.sdk.pickup.PickupManager
import com.radiusnetworks.flybuy.sdk.presence.PresenceLocator
import com.radiusnetworks.flybuy.sdk.presence.PresenceManager
import org.threeten.bp.Instant
import java.util.*
import java.util.concurrent.ExecutionException

class FlybuyModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return "Flybuy"
  }

  private fun startObserving() {
    val orderObserver = Observer<List<Order>> {
      orderProgress(it)
    }

    Handler(Looper.getMainLooper()).post {
      FlyBuyCore.orders.openLiveData.observeForever(orderObserver)
    }
  }

  private fun orderProgress(orders: List<Order>) {
    orders.forEach { order ->
      reactApplicationContext
        .getJSModule(RCTDeviceEventEmitter::class.java)
        .emit("orderUpdated", parseOrder(order))
    }
  }

  private fun stopObserving() {
    (currentActivity as AppCompatActivity?)?.let {
      if (FlyBuyCore.orders.openLiveData.hasObservers()) {
        FlyBuyCore.orders.openLiveData.removeObservers(it)
      }
    }
  }

  @Deprecated("Deprecated in Java")
  override fun onCatalystInstanceDestroy() {
    stopObserving()
  }

  @ReactMethod
  fun addListener(eventName: String) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  @ReactMethod
  fun removeListeners(eventName: String) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  @ReactMethod
  fun configure(token: String, promise: Promise) {
    FlyBuyCore.configure(reactApplicationContext.baseContext, token)
    val currentActivity = currentActivity
    if (currentActivity != null) {
      startObserving()
    }
  }

  // Customer

  @ReactMethod
  fun loginWithToken(token: String, promise: Promise) {
    FlyBuyCore.customer.loginWithToken(token = token) { customer, error ->
      if (null != error) {
        // Handle error
        handleFlyBuyError(error)
        promise.reject(error.userError())
      } else {
        if (null != customer) {
          promise.resolve(parseCustomer(customer))
        }
      }
    }
  }

  @ReactMethod
  fun login(email: String, password: String, promise: Promise) {
    FlyBuyCore.customer.login(email, password) { customer, error ->
      if (null != error) {
        // Handle error
        handleFlyBuyError(error)
        promise.reject(error.userError())
      } else {
        if (null != customer) {
          promise.resolve(parseCustomer(customer))
        }
      }
    }
  }

  @ReactMethod
  fun signUp(email: String, password: String, promise: Promise) {
    FlyBuyCore.customer.signUp(email, password) { customer, error ->
      if (null != error) {
        // Handle error
        handleFlyBuyError(error)
        promise.reject(error.userError())
      } else {
        if (null != customer) {
          promise.resolve(parseCustomer(customer))
        }
      }
    }
  }

  @ReactMethod
  fun logout(promise: Promise) {
    FlyBuyCore.customer.logout { error ->
      if (null != error) {
        // Handle error
        handleFlyBuyError(error)
        promise.reject(error.userError())
      } else {
        promise.resolve("ok")
      }
    }
  }

  @ReactMethod
  fun createCustomer(customer: ReadableMap, promise: Promise) {
    val customerInfo: CustomerInfo = decodeCustomerInfo(customer)
    FlyBuyCore.customer.create(customerInfo, true, true) { customer, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        customer?.let {
          promise.resolve(parseCustomer(customer))
        } ?: run {
          promise.reject("Create Customer Error", "Error retrieving customer")
        }
      }
    }
  }

  @ReactMethod
  fun updateCustomer(customer: ReadableMap, promise: Promise) {
    val customerInfo: CustomerInfo = decodeCustomerInfo(customer)
    FlyBuyCore.customer.update(customerInfo) { customer, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        customer?.let {
          promise.resolve(parseCustomer(customer))
        } ?: run {
          promise.reject("Update Customer Error", "Error updating customer")
        }
      }
    }
  }

  @ReactMethod
  fun getCurrentCustomer(promise: Promise) {
    val customer = FlyBuyCore.customer.current
    customer?.let {
      promise.resolve(parseCustomer(customer))
    } ?: run {
      promise.reject("Not logged in", "Current Customer null")
    }
  }

  // Orders

  @ReactMethod
  fun fetchOrders(promise: Promise) {
    FlyBuyCore.orders.fetch { orders, sdkError ->
      sdkError?.let {
        handleFlyBuyError(it)
        promise.reject(it.userError(), it.userError())
      } ?: run {
        promise.resolve(orders?.let { parseOrders(it) })
      }

    }
  }

  @ReactMethod
  fun claimOrder(
    redeemCode: String,
    customer: ReadableMap,
    pickupType: String? = null,
    promise: Promise
  ) {
    FlyBuyCore.orders.claim(
      redeemCode,
      decodeCustomerInfo(customer),
      pickupType
    ) { order, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        order?.let { promise.resolve(parseOrder(it)) } ?: run {
          promise.reject("null", "Null order")
        }
      }
    }
  }

  @ReactMethod
  fun fetchOrderByRedemptionCode(redeemCode: String, promise: Promise) {
    FlyBuyCore.orders.fetch(redeemCode) { order, sdkError ->
      if (null != sdkError) {
        promise.reject(sdkError.userError(), sdkError.userError())
      } else {
        promise.resolve(order?.let { parseOrder(it) })
      }
    }
  }

  @ReactMethod
  fun createOrder(
    siteID: Int,
    pid: String,
    customer: ReadableMap,
    pickupWindow: ReadableMap? = null,
    orderState: String? = null,
    pickupType: String? = null,
    promise: Promise
  ) {
    val customerInfo: CustomerInfo = decodeCustomerInfo(customer)
    val pickupWindowInfo = pickupWindow?.let { decodePickupWindow(it) }

    FlyBuyCore.orders.create(
      siteID = siteID,
      partnerIdentifier = pid,
      customerInfo = customerInfo,
      pickupWindow = pickupWindowInfo,
      state = orderState,
      pickupType = pickupType
    ) { order, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        order?.let {
          promise.resolve(parseOrder(order))
        } ?: run {
          promise.reject("Create Order Error", "Error retrieving order")
        }
      }
    }
  }

  @ReactMethod
  fun createOrderWithPartnerIdentifier(
    sitePid: String,
    orderPid: String,
    customer: ReadableMap,
    pickupWindow: ReadableMap? = null,
    orderState: String? = null,
    pickupType: String? = null,
    promise: Promise
  ) {
    val customerInfo: CustomerInfo = decodeCustomerInfo(customer)
    val pickupWindowInfo = pickupWindow?.let { decodePickupWindow(it) }

    FlyBuyCore.orders.create(
      sitePartnerIdentifier = sitePid,
      orderPartnerIdentifier = orderPid,
      customerInfo = customerInfo,
      pickupWindow = pickupWindowInfo,
      state = orderState,
      pickupType = pickupType
    ) { order, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        order?.let {
          promise.resolve(parseOrder(order))
        } ?: run {
          promise.reject("Create Order Error", "Error retrieving order")
        }
      }
    }
  }

  @ReactMethod
  fun updateOrderState(orderId: Int, state: String, promise: Promise) {
    FlyBuyCore.orders.updateState(orderId, state) { order, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        order?.let { promise.resolve(parseOrder(it)) } ?: run {
          promise.reject("null", "Null order")
        }
      }
    }
  }

  @ReactMethod
  fun rateOrder(orderId: Int, rating: Int, comments: String, promise: Promise) {
    FlyBuyCore.orders.rateOrder(
      orderId = orderId,
      rating = rating,
      comments = comments
    ) { order, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        order?.let { promise.resolve(parseOrder(it)) } ?: run {
          promise.reject("null", "Null order")
        }
      }
    }
  }

  @ReactMethod
  fun updateOrderCustomerState(orderId: Int, state: String, promise: Promise) {
    FlyBuyCore.orders.updateCustomerState(orderId, state) { order, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        order?.let { promise.resolve(parseOrder(it)) } ?: run {
          promise.reject("null", "Null order")
        }
      }
    }
  }

  @ReactMethod
  fun updateOrderCustomerStateWithSpot(orderId: Int, state: String, spot: String, promise: Promise) {
    FlyBuyCore.orders.updateCustomerState(orderId, state, spot) { order, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        order?.let { promise.resolve(parseOrder(it)) } ?: run {
          promise.reject("null", "Null order")
        }
      }
    }
  }

  // Pickup

  @ReactMethod
  fun pickupConfigure(promise: Promise) {
    PickupManager.getInstance()?.configure(reactApplicationContext.baseContext)
  }

  @ReactMethod
  fun onPermissionChangedPickup() {
    PickupManager.getInstance().onLocationPermissionChanged()
  }

  // Notify

  @ReactMethod
  fun notifyConfigure(bgTaskIdentifier: String? = null, promise: Promise) {
    NotifyManager.getInstance()?.configure(reactApplicationContext.baseContext)
  }

  @ReactMethod
  fun createForSitesInRegion(region: ReadableMap, notification: ReadableMap, promise: Promise) {
    val regionInfo: CircularRegion = decodeRegion(region)
    val notificationInfo: NotificationInfo = decodeNotification(notification)

    NotifyManager.getInstance().createForSitesInRegion(
      region = regionInfo,
      notificationInfo = notificationInfo
    ) { sites, sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        sites?.let {
          promise.resolve(parseSites(sites))
        } ?: run {
          promise.reject(
            "Create Notification for sites in region Error",
            "Error creating notification"
          )
        }
      }
    }

  }

  @ReactMethod
  fun createForSites(sitesList: ReadableArray, notification: ReadableMap, promise: Promise) {
    val sites = decodeSites(sitesList)
    val notificationInfo: NotificationInfo = decodeNotification(notification)

    NotifyManager.getInstance().createForSites(
      sites = sites,
      notificationInfo = notificationInfo
    ) { sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        promise.resolve("ok")
      }
    }

  }

  @ReactMethod
  fun clearNotifications(promise: Promise) {
    NotifyManager.getInstance().clear() { sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        promise.resolve("ok")
      }
    }
  }

  @ReactMethod
  fun sync(force: Boolean, promise: Promise) {
    NotifyManager.getInstance().sync(
      force
    ) { sdkError ->
      sdkError?.let {
        promise.reject(it.userError(), it.userError())
      } ?: run {
        promise.resolve("ok")
      }
    }
  }

  fun parseNotifyMetadata(values: Map<String, Any>): WritableMap {
    val map = Arguments.createMap()
    for ((key, value) in values) {
      when (value) {
        null -> map.putNull(key)
        is Boolean -> map.putBoolean(key, value)
        is Double -> map.putDouble(key, value)
        is Int -> map.putInt(key, value)
        is String -> map.putString(key, value)
        is WritableMap -> map.putMap(key, value)
        is WritableArray -> map.putArray(key, value)
        else -> throw IllegalArgumentException("Unsupported value type ${value::class.java.name} for key [$key]")
      }
    }
    map.putInt("time", (System.currentTimeMillis() / 1000).toInt())
    return map
  }

  fun handleNotification(intent: Intent?) {
    intent?.let {
      val notifyMetadata = NotifyManager.getInstance().handleNotification(it)
      if (null != notifyMetadata) {
        reactApplicationContext
          .getJSModule(RCTDeviceEventEmitter::class.java)
          .emit("notifyEvents", parseNotifyMetadata(notifyMetadata.toMap()))
      }
    }
  }

  @ReactMethod
  fun onPermissionChangedNotify() {
    NotifyManager.getInstance().onPermissionChanged()
  }

// Sites

  @ReactMethod
  fun fetchAllSites(promise: Promise) {
    FlyBuyCore.sites.fetchAll { sites, sdkError ->
      sdkError?.let {
        handleFlyBuyError(it)
        promise.reject(it.userError(), it.userError())
      } ?: run {
        promise.resolve(sites?.let { parseSites(it) })
      }

    }
  }

  @ReactMethod
  fun fetchSitesByQuery(params: ReadableMap, promise: Promise) {
    val query = params.getString("query")
    val page = params.getInt("page")
    FlyBuyCore.sites.fetch(query, page) { sites, pagination, sdkError ->
      sdkError?.let {
        handleFlyBuyError(it)
        promise.reject(it.userError(), it.userError())
      } ?: run {
        sites?.let {
          val map = Arguments.createMap()
          var sites = parseSites(it)
          if (pagination != null) {
            var pagination = parsePagination(pagination)
            map.putArray("data", sites)
            map.putMap("pagination", pagination)
            promise.resolve(map)
          } else {
            promise.reject("Fetch sites Error", "Error retrieving pagination")
          }
        } ?: run {
          promise.reject("Fetch sites Error", "Error retrieving sites")
        }

      }

    }
  }

  @ReactMethod
  fun fetchSitesByRegion(params: ReadableMap, promise: Promise) {
    val per = params.getInt("per")
    val page = params.getInt("page")
    val regionInfo = params.getMap("region")!!
    val region: CircularRegion = decodeRegion(regionInfo)

    FlyBuyCore.sites.fetch(region, page, per) { sites, sdkError ->
      sdkError?.let {
        handleFlyBuyError(it)
        promise.reject(it.userError(), it.userError())
      } ?: run {
        sites?.let {
          promise.resolve(parseSites(sites))
        } ?: run {
          promise.reject("Fetch sites Error", "Error retrieving sites")
        }

      }

    }
  }

  @ReactMethod
  fun fetchSiteByPartnerIdentifier(params: ReadableMap, promise: Promise) {
    val pid = params.getString("partnerIdentifier")!!

    FlyBuyCore.sites.fetchByPartnerIdentifier(pid) { site, sdkError ->
      sdkError?.let {
        handleFlyBuyError(it)
        promise.reject(it.userError(), it.userError())
      } ?: run {
        site?.let {
          promise.resolve(parseSite(site))
        } ?: run {
          promise.reject("Fetch site by partnerIdentifier Error", "Error retrieving site")
        }

      }
    }
  }

  // Presence

  @ReactMethod
  fun presenceConfigure(presenceUUID: String) {
    val uid = UUID.fromString(presenceUUID)
    PresenceManager.getInstance()?.configure(reactApplicationContext.baseContext, uid)
  }

  @ReactMethod
  fun createLocatorWithIdentifier(byte_presenceId: String, payload: String, promise: Promise) {
    var presenceId = byte_presenceId.toByteArray()
    PresenceManager.getInstance()
      ?.createLocatorWithIdentifier(presenceId, payload) { presenceLocator, sdkError ->
        sdkError?.let {
          // Handle error
          promise.reject(it.userError(), it.userError())
        }
        presenceLocator?.let {

          //  promise.resolve(presenceLocator.refere)
          // Set locator listener
          // it.listener = locatorListener
          // Store locator or start it here
          startLocator(presenceLocator)
        }
      }
  }

  @ReactMethod
  fun startLocator(presenceLocator: PresenceLocator) {
    PresenceManager.getInstance()?.start(presenceLocator)
  }

  @ReactMethod
  fun startLocatorWithIdentifier(byte_presenceId: String, payload: String, promise: Promise) {
    var presenceId = byte_presenceId.toByteArray()
    PresenceManager.getInstance()
      ?.createLocatorWithIdentifier(presenceId, payload) { presenceLocator, sdkError ->
        sdkError?.let {
          // Handle error
          promise.reject(it.userError(), it.userError())
        }
        presenceLocator?.let {
          // Set locator listener
          // it.listener = locatorListener
          // Store locator or start it here
          startLocator(presenceLocator)
          promise.resolve("Locator started successfully")
        }
      }
  }

  @ReactMethod
  fun stopLocator(promise: Promise) {
    try {
      PresenceManager.getInstance()?.stop()
      promise.resolve("Locator is stopped successfully.")
    } catch (e: ExecutionException) {
      promise.reject(e.message)
    }
  }

  // Notifications

  @ReactMethod
  fun updatePushToken(token: String) {
    FlyBuyCore.onNewPushToken(token)
  }

  @ReactMethod
  fun handleRemoteNotification(data: ReadableMap) {
    val dataMap: Map<String, String> = decodeData(data)
    FlyBuyCore.onMessageReceived(dataMap, null)
  }

}

fun parsePagination(pagination: Pagination): WritableMap {
  val map = Arguments.createMap()
  map.putInt("currentPage", pagination.currentPage)
  map.putInt("totalPages", pagination.totalPages)
  return map
}

fun parseCustomer(customer: Customer): WritableMap {
  val map = Arguments.createMap()
  map.putInt("id", customer.id)
  map.putString("token", customer.apiToken)
  map.putString("emailAddress", customer.email)

  val info = Arguments.createMap()
  info.putString("name", customer.name)
  info.putString("carType", customer.carType)
  info.putString("carColor", customer.carColor)
  info.putString("licensePlate", customer.licensePlate)
  info.putString("phone", customer.phone)

  map.putMap("info", info)

  return map
}

fun parseOrders(items: List<Order>): WritableArray {
  val array = WritableNativeArray()
  for (item in items) {
    array.pushMap(parseOrder(item))
  }
  return array
}

fun parseOrder(order: Order): WritableMap {
  val map = Arguments.createMap()
  map.putInt("id", order.id)
  map.putString("state", order.state)
  map.putString("customerState", order.customerState)
  map.putString("partnerIdentifier", order.partnerIdentifier)
  map.putString("partnerIdentifierForCustomer", order.partnerIdentifierForCustomer)
  map.putString("partnerIdentifierForCrew", order.partnerIdentifierForCrew)
  val pickupWindow = Arguments.createArray()
  pickupWindow.pushString(order.pickupWindow?.start.toString())
  pickupWindow.pushString(order.pickupWindow?.end.toString())
  map.putArray("pickupWindow", pickupWindow)
  map.putString("pickupType", order.pickupType)
  map.putString("etaAt", order.etaAt?.toString())
  map.putString("createdAt", order.createdAt?.toString())
  map.putString("redemptionCode", order.redemptionCode)
  map.putString("redeemedAt", order.redeemedAt.toString())
  order.customerRatingValue?.let { map.putInt("customerRating", it) }
  map.putString("customerComment", order.customerRatingComments)
  map.putInt("siteID", order.site?.id)
  map.putString("siteName", order.site?.name)
  map.putString("sitePhone", order.site?.phone)
  map.putString("siteFullAddress", order.site?.fullAddress)
  map.putString("siteLongitude", order.site?.longitude)
  map.putString("siteLatitude", order.site?.latitude)
  map.putString("siteInstructions", order.site?.instructions)
  map.putString("siteDescription", order.site?.description)
  map.putString("siteCoverPhotoURL", order.site?.coverPhotoUrl)
  map.putString("customerName", order.customer?.name)
  map.putString("customerCarType", order.customer?.carType)
  map.putString("customerCarColor", order.customer?.carColor)
  map.putString("customerLicensePlate", order.customer?.licensePlate)
  map.putString("spotIdentifier", order.spotIdentifier)
  map.putBoolean("spotIdentifierEntryEnabled", order.spotIdentifierEntryEnabled)
  map.putString("spotIdentifierInputType", order.spotIdentifierInputType.toString())

  return map
}

fun parseSites(items: List<Site>): WritableArray {
  val array = WritableNativeArray()
  for (item in items) {
    array.pushMap(parseSite(item))
  }
  return array
}

fun parsePickupTypeConfig(pickupTypeConfig: PickupTypeConfig): WritableMap {
  val map = Arguments.createMap()
  map.putString("pickupType", pickupTypeConfig.pickupType)
  map.putString("pickupTypeLocalizedString", pickupTypeConfig.pickupTypeLocalizedString)
  map.putBoolean("requireVehicleInfo", pickupTypeConfig.requireVehicleInfo)
  map.putBoolean("showVehicleInfoFields", pickupTypeConfig.showVehicleInfoFields)

  return map
}

fun parsePickupTypeConfigs(items: List<PickupTypeConfig>): WritableArray {
  val array = WritableNativeArray()
  for (item in items) {
    array.pushMap(parsePickupTypeConfig(item))
  }
  return array
}

fun parsePickupConfig(pickupConfig: PickupConfig): WritableMap {
  val map = Arguments.createMap()
  map.putString("accentColor", pickupConfig.projectAccentColor)
  map.putString("accentTextColor", pickupConfig.projectAccentTextColor)
  map.putString("askToAskImageURL", pickupConfig.askToAskImageUrl)
  map.putBoolean("customerNameEditingEnabled", pickupConfig.customerNameEditingEnabled)
  map.putInt("id", pickupConfig.id)
  map.putBoolean("pickupTypeSelectionEnabled", pickupConfig.pickupTypeSelectionEnabled)
  map.putString("privacyPolicyURL", pickupConfig.privacyPolicyUrl)
  map.putString("termsOfServiceURL", pickupConfig.termsOfServiceUrl)
  map.putString("type", pickupConfig.type)
  map.putArray("availablePickupTypes", parsePickupTypeConfigs(pickupConfig.availablePickupTypes))
  return map
}

fun parseSite(site: Site): WritableMap {
  val map = Arguments.createMap()
  map.putInt("id", site.id)
  map.putString("name", site.name)
  map.putString("phone", site.phone)
  map.putString("streetAddress", site.streetAddress)
  map.putString("fullAddress", site.fullAddress)
  map.putString("locality", site.locality)
  map.putString("region", site.region)
  map.putString("country", site.country)
  map.putString("postalCode", site.postalCode)
  map.putString("latitude", site.latitude)
  map.putString("longitude", site.longitude)
  map.putString("coverPhotoUrl", site.coverPhotoUrl)
  map.putString("instructions", site.instructions)
  map.putString("description", site.description)
  map.putString("partnerIdentifier", site.partnerIdentifier)
  map.putMap("pickupConfig", parsePickupConfig(site.pickupConfig))

  return map
}


fun decodeCustomerInfo(customer: ReadableMap): CustomerInfo {
  var name = ""
  var carType = ""
  var carColor = ""
  var licensePlate = ""
  var phone = ""


  if (customer.hasKey("name")) {
    name = customer.getString("name")!!
  }
  if (customer.hasKey("carType")) {
    carType = customer.getString("carType")!!
  }
  if (customer.hasKey("carColor")) {
    carColor = customer.getString("carColor")!!
  }
  if (customer.hasKey("phone")) {
    phone = customer.getString("phone")!!
  }
  if (customer.hasKey("licensePlate")) {
    licensePlate = customer.getString("licensePlate")!!
  }

  return CustomerInfo(
    name = name,
    carType = carType,
    carColor = carColor,
    licensePlate = licensePlate,
    phone = phone
  )

}

fun decodeRegion(region: ReadableMap): CircularRegion {
  var latitude = region.getDouble("latitude")!!
  var longitude = region.getDouble("longitude")!!
  var radius = region.getInt("radius").toFloat()

  return CircularRegion(
    latitude = latitude,
    longitude = longitude,
    radius = radius
  )
}

fun decodeNotification(notification: ReadableMap): NotificationInfo {
  var title = ""
  var message = ""
  var data = mapOf<String, String>()

  if (notification.hasKey("title")) {
    title = notification.getString("title")!!
  }

  if (notification.hasKey("message")) {
    message = notification.getString("message")!!
  }

  if (notification.hasKey("data")) {
    var dataMap = notification.getMap("data")!!
    val iterator: ReadableMapKeySetIterator = dataMap.keySetIterator()
    while (iterator.hasNextKey()) {
      val key = iterator.nextKey()
      val type: ReadableType = dataMap.getType(key)
      when (type) {
        ReadableType.String -> data += Pair(key, dataMap.getString(key)!!)
        else -> throw IllegalArgumentException("Could not convert object with key: $key.")
      }
    }

  }

  return NotificationInfo(
    title = title,
    message = message,
    data = data
  )
}

fun decodeSites(sitesList: ReadableArray): List<Site> {
  var list = listOf<Site>()
  for (i in 0 until sitesList.size()) {
    var site = sitesList.getMap(i)!!
    list += decodeSite(site)
  }

  return list
}

fun decodeSite(site: ReadableMap): Site {
  var type: String? = null
  var displayName: String? = null
  var name: String? = null
  var phone: String? = null
  var streetAddress: String? = null
  var fullAddress: String? = null
  var locality: String? = null
  var region: String? = null
  var country: String? = null
  var postalCode: String? = null
  var latitude: String? = null
  var longitude: String? = null
  var coverPhotoUrl: String? = null
  var iconUrl: String? = null
  var instructions: String? = null
  var description: String? = null
  var partnerIdentifier: String? = null
  var operationalStatus: String? = null
  var pickupConfigId: Int? = null


  var id = site.getInt("id")!!

  if (site.hasKey("name")) {
    name = site.getString("name")
  }

  if (site.hasKey("phone")) {
    phone = site.getString("phone")
  }

  if (site.hasKey("streetAddress")) {
    streetAddress = site.getString("streetAddress")
  }

  if (site.hasKey("fullAddress")) {
    fullAddress = site.getString("fullAddress")
  }

  if (site.hasKey("locality")) {
    locality = site.getString("locality")
  }

  if (site.hasKey("region")) {
    region = site.getString("region")
  }

  if (site.hasKey("country")) {
    country = site.getString("country")
  }

  if (site.hasKey("postalCode")) {
    postalCode = site.getString("postalCode")
  }

  if (site.hasKey("latitude")) {
    latitude = site.getString("latitude")
  }

  if (site.hasKey("longitude")) {
    longitude = site.getString("longitude")
  }

  if (site.hasKey("iconUrl")) {
    iconUrl = site.getString("iconUrl")
  }

  if (site.hasKey("instructions")) {
    instructions = site.getString("instructions")
  }

  if (site.hasKey("description")) {
    description = site.getString("description")
  }

  if (site.hasKey("type")) {
    type = site.getString("type")
  }

  if (site.hasKey("partnerIdentifier")) {
    partnerIdentifier = site.getString("partnerIdentifier")
  }

  if (site.hasKey("operationalStatus")) {
    operationalStatus = site.getString("operationalStatus")
  }

  if (site.hasKey("pickupConfigId")) {
    pickupConfigId = site.getInt("pickupConfigId")
  }

  var site = com.radiusnetworks.flybuy.api.model.Site(
    id = id,
    name = name,
    phone = phone,
    streetAddress = streetAddress,
    fullAddress = fullAddress,
    locality = locality,
    region = region,
    country = country,
    postalCode = postalCode,
    latitude = latitude,
    longitude = longitude,
    coverPhotoURL = coverPhotoUrl,
    projectLogoURL = iconUrl,
    instructions = instructions,
    description = description,
    partnerIdentifier = partnerIdentifier,
    type = type,
    displayName = displayName,
    operationalStatus = operationalStatus,
    pickupConfigId = pickupConfigId,
    // TODO: Map this value from API response
    projectAccentColor = null,
    geofence = null,
    prearrivalSeconds = null,
    projectAccentTextColor = null,
    wrongSiteArrivalRadius = null,

  )

  var pickupConfig = null

  return Site(
    site,
    pickupConfig
  )
}

fun decodePickupWindow(pickupWindow: ReadableMap): PickupWindow {
  val instantStart = Instant.parse(pickupWindow.getString("start")!!)
  val instantEnd = Instant.parse(pickupWindow.getString("end")!!)
  return PickupWindow(
    start = instantStart,
    end = instantEnd
  )
}


fun decodeData(data: ReadableMap): Map<String, String> {
  var dataMap = mapOf<String, String>()
  val iterator: ReadableMapKeySetIterator = data.keySetIterator()
  while (iterator.hasNextKey()) {
    val key = iterator.nextKey()
    val type: ReadableType = data.getType(key)
    when (type) {
      ReadableType.String -> dataMap += Pair(key, data.getString(key)!!)
      else -> throw IllegalArgumentException("Could not convert object with key: $key.")
    }
  }
  return dataMap
}
