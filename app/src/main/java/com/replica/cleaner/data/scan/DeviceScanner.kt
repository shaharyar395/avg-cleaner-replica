package com.replica.cleaner.data.scan

import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.os.storage.StorageManager
import com.replica.cleaner.data.model.StorageSnapshot
import com.replica.cleaner.data.model.SystemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Storage totals and the System Info screen. */
class DeviceScanner(private val context: Context) {

    /**
     * Real phone free/total space as shown in system Settings.
     * Prefers StorageStatsManager (shared user data volume), then StatFs on
     * external shared storage, then internal /data as last resort.
     */
    fun storage(): StorageSnapshot {
        storageViaStatsManager()?.let { return it }
        storageViaStatFs(primaryStoragePath())?.let { return it }
        storageViaStatFs(Environment.getDataDirectory())?.let { return it }
        return StorageSnapshot()
    }

    private fun storageViaStatsManager(): StorageSnapshot? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return try {
            val sm = context.getSystemService(StorageManager::class.java) ?: return null
            val ssm = context.getSystemService(StorageStatsManager::class.java) ?: return null
            val path = primaryStoragePath()
            val uuid = try {
                sm.getUuidForPath(path)
            } catch (_: Exception) {
                StorageManager.UUID_DEFAULT
            }
            val total = ssm.getTotalBytes(uuid)
            val free = ssm.getFreeBytes(uuid)
            if (total <= 0L) null else StorageSnapshot(totalBytes = total, freeBytes = free.coerceAtLeast(0L))
        } catch (_: Exception) {
            null
        }
    }

    private fun storageViaStatFs(path: File?): StorageSnapshot? {
        if (path == null) return null
        return try {
            val target = if (path.exists()) path else return null
            val stat = StatFs(target.absolutePath)
            val total = stat.blockCountLong * stat.blockSizeLong
            val free = stat.availableBlocksLong * stat.blockSizeLong
            if (total <= 0L) null else StorageSnapshot(totalBytes = total, freeBytes = free.coerceAtLeast(0L))
        } catch (_: Exception) {
            null
        }
    }

    /** Emulated shared storage root when available (junk cleans live here). */
    private fun primaryStoragePath(): File {
        val external = Environment.getExternalStorageDirectory()
        if (external != null && external.exists()) return external
        context.getExternalFilesDir(null)?.let { appDir ->
            // /storage/emulated/0/Android/data/<pkg>/files → climb to volume root
            var cur: File? = appDir
            repeat(5) { cur = cur?.parentFile }
            cur?.let { if (it.exists()) return it }
        }
        return Environment.getDataDirectory()
    }

    suspend fun systemInfo(): SystemInfo = withContext(Dispatchers.IO) {
        val storage = storage()

        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }

        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val sd = sdCardSnapshot()
        val battery = batterySnapshot()

        SystemInfo(
            androidVersion = Build.VERSION.RELEASE ?: "",
            androidCodename = codename(Build.VERSION.SDK_INT),
            uptimeMillis = SystemClock.elapsedRealtime(),
            model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            wifiEnabled = wifi?.isWifiEnabled == true,
            ssid = currentSsid(wifi),
            ipAddress = ipAddress(),
            bluetoothOn = bluetoothOn(),
            mobileDataOn = mobileDataOn(cm),
            ramUsedBytes = (mem.totalMem - mem.availMem).coerceAtLeast(0),
            ramAvailableBytes = mem.availMem,
            storageUsedBytes = storage.usedBytes,
            storageAvailableBytes = storage.freeBytes,
            sdCardUsedBytes = sd.first,
            sdCardAvailableBytes = sd.second,
            sdCardPresent = sd.third,
            batteryPercent = battery.first,
            batteryCelsius = battery.second,
            cpuUsedPercent = cpuUsedPercent()
        )
    }

    private fun codename(sdk: Int): String = when {
        sdk >= 35 -> "VANILLA_ICE_CREAM"
        sdk == 34 -> "UPSIDE_DOWN_CAKE"
        sdk == 33 -> "TIRAMISU"
        sdk == 32 -> "SNOW_CONE_V2"
        sdk == 31 -> "SNOW_CONE"
        sdk == 30 -> "RED_VELVET_CAKE"
        sdk == 29 -> "QUINCE_TART"
        sdk == 28 -> "PIE"
        sdk == 27 -> "OREO_MR1"
        sdk == 26 -> "OREO"
        else -> "API $sdk"
    }

    /**
     * From Android 10 the SSID is behind location permission and comes back as
     * "<unknown ssid>" without it — the UI shows a dash in that case.
     */
    @Suppress("DEPRECATION")
    private fun currentSsid(wifi: WifiManager?): String? = try {
        val raw = wifi?.connectionInfo?.ssid?.trim('"')
        if (raw.isNullOrBlank() || raw.contains("unknown", true)) null else raw
    } catch (_: Exception) {
        null
    }

    @Suppress("DEPRECATION")
    private fun ipAddress(): String? = try {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val ip = wifi?.connectionInfo?.ipAddress ?: 0
        if (ip == 0) null else
            "${ip and 0xFF}.${(ip shr 8) and 0xFF}.${(ip shr 16) and 0xFF}.${(ip shr 24) and 0xFF}"
    } catch (_: Exception) {
        null
    }

    private fun bluetoothOn(): Boolean = try {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter?.isEnabled == true
    } catch (_: SecurityException) {
        false
    } catch (_: Exception) {
        false
    }

    private fun mobileDataOn(cm: ConnectivityManager?): Boolean = try {
        val network = cm?.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    } catch (_: Exception) {
        false
    }

    /** usedBytes, availableBytes, present */
    private fun sdCardSnapshot(): Triple<Long, Long, Boolean> = try {
        val dirs = context.getExternalFilesDirs(null)
        val secondary = dirs?.firstOrNull { dir ->
            dir != null && Environment.isExternalStorageRemovable(dir)
        }
        if (secondary == null) Triple(0L, 0L, false)
        else {
            val root = secondary.parentFile?.parentFile?.parentFile ?: secondary
            val snap = storageViaStatFs(root) ?: return Triple(0L, 0L, false)
            Triple(snap.usedBytes, snap.freeBytes, snap.totalBytes > 0)
        }
    } catch (_: Exception) {
        Triple(0L, 0L, false)
    }

    private fun batterySnapshot(): Pair<Int, Float> = try {
        val intent = context.registerReceiver(
            null,
            android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        )
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100) ?: 100
        val tempTenths = intent?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val percent = if (level >= 0 && scale > 0) (level * 100) / scale else 0
        percent to (tempTenths / 10f)
    } catch (_: Exception) {
        0 to 0f
    }

    /** Rough CPU load from /proc/stat — good enough for System Info display. */
    private fun cpuUsedPercent(): Int = try {
        val first = readCpuTimes() ?: return 0
        Thread.sleep(120)
        val second = readCpuTimes() ?: return 0
        val idleDelta = (second.second - first.second).coerceAtLeast(0L)
        val totalDelta = (second.first - first.first).coerceAtLeast(1L)
        (((totalDelta - idleDelta) * 100) / totalDelta).toInt().coerceIn(0, 100)
    } catch (_: Exception) {
        0
    }

    private fun readCpuTimes(): Pair<Long, Long>? {
        val line = java.io.File("/proc/stat").bufferedReader().use { it.readLine() } ?: return null
        val parts = line.trim().split(Regex("\\s+"))
        if (parts.size < 5 || parts[0] != "cpu") return null
        val values = parts.drop(1).mapNotNull { it.toLongOrNull() }
        if (values.size < 4) return null
        val idle = values[3]
        val total = values.sum()
        return total to idle
    }
}
