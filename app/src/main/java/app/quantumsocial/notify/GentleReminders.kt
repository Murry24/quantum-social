package app.quantumsocial.notify

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.quantumsocial.MainActivity
import app.quantumsocial.R
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.GentleNotes
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.ui.screens.DailyPrefs
import java.util.Calendar

/**
 * Jemné pripomienky (bod 15): raz denne večer jedna poetická veta – bez čísiel,
 * bez červených odznakov, bez zvuku. Dá sa vypnúť v Profile.
 */
object GentleReminders {
    private const val CHANNEL = "gentle"
    private const val PREFS = "quantum_prefs"
    private const val KEY = "gentle_reminders"
    private const val NOTIFICATION_ID = 1801

    fun isOn(context: Context): Boolean {
        val p = prefs(context)
        // predvolene podľa voľby z onboardingu
        return p.getBoolean(KEY, p.getBoolean("notifications", false))
    }

    fun setOn(
        context: Context,
        on: Boolean,
    ) {
        prefs(context).edit().putBoolean(KEY, on).apply()
        apply(context)
    }

    /** Naplánuje alebo zruší večernú pripomienku podľa nastavenia. */
    fun apply(context: Context) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context)
        alarms.cancel(pending)
        if (!isOn(context)) return
        val next =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, GentleNotes.HOUR)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
        // nepresný alarm – systém si ho zaradí, keď sa mu to hodí (šetrí batériu)
        alarms.setInexactRepeating(AlarmManager.RTC, next.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
    }

    fun show(context: Context) {
        val allowed =
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!allowed || !isOn(context)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, tr("Jemné pripomienky"), NotificationManager.IMPORTANCE_LOW).apply {
                setShowBadge(false)
            },
        )
        // smädné semienko má prednosť pred bežnou pripomienkou
        val thirsty = DailyPrefs.thirstySeed(context)
        val (title, text) =
            if (thirsty != null) {
                tr("🥀 Tvoje semienko je smädné") to tr("„%s“ – zalej ho malým krokom.", thirsty.text)
            } else {
                GentleNotes.forDay(StarDustState.epochDay(System.currentTimeMillis()))
            }
        val open =
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(open)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, GentleReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/** Prijme večerný alarm a ukáže jemnú pripomienku. */
class GentleReminderReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        GentleReminders.show(context)
    }
}
