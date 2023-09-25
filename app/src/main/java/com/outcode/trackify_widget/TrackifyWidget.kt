package com.outcode.trackify_widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class TrackifyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                TrackifyMedium()
            }
        }
    }

    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        private val BIG_SQUARE = DpSize(250.dp, 250.dp)

        private val PREF_PROJECT_NAME_KEY = stringPreferencesKey("PREF_PROJECT_NAME_KEY")
        private val PREF_PROJECT_STATUS_KEY = booleanPreferencesKey("PREF_PROJECT_STATUS_KEY")

        private val PARAM_PROJECT_NAME_KEY = ActionParameters.Key<String>("PREF_PROJECT_NAME_KEY")
        private val PARAM_PROJECT_STATUS_KEY =
            ActionParameters.Key<Boolean>("PREF_PROJECT_STATUS_KEY")
    }

    private lateinit var timer: Timer
    var totalElapsedTime = 0L

    override val sizeMode = SizeMode.Exact

    @Composable
    fun TrackifyMedium() {
        val isRunning = currentState(key = PREF_PROJECT_STATUS_KEY)
        val projectName = currentState(key = PREF_PROJECT_NAME_KEY) ?: "Working on Trackify"
        Column(
            modifier = GlanceModifier
                .width(BIG_SQUARE.width)
                .height(BIG_SQUARE.height)
                .padding(8.dp)
                .background(color = Color.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Trackify")
            Column(modifier = GlanceModifier.fillMaxWidth().padding(0.dp).padding(top = 12.dp)) {
                Row {
                    Text(text = if (isRunning!!) "Running" else "Paused")
                    Spacer()
                    Text(text = getFormattedMillis(totalElapsedTime))
                }
                Text(
                    text = projectName,
                    modifier = GlanceModifier.padding(0.dp).padding(top = 4.dp)
                )
            }
            Spacer()
            Button(
                text = if (isRunning!!) "Stop" else "Start", onClick = {
                    if (!isRunning) startResumeTimer(isRunning) else pauseTimer(isRunning)
                }, modifier = GlanceModifier.fillMaxWidth().padding(0.dp).padding(vertical = 12.dp)
            )
        }
    }

    private fun startResumeTimer(isRunning: Boolean) {
        timer = Timer()
        if (::timer.isInitialized) timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                totalElapsedTime += 1000
            }
        }, 0, 1000)
        actionRunCallback<ToggleProjectStatusCallback>(
            actionParametersOf(
                PARAM_PROJECT_STATUS_KEY to isRunning
            )
        )
    }

    private fun pauseTimer(isRunning: Boolean) {
        if (::timer.isInitialized) timer.cancel()
        actionRunCallback<ToggleProjectStatusCallback>(
            actionParametersOf(
                PARAM_PROJECT_STATUS_KEY to isRunning
            )
        )
    }

    private fun getFormattedMillis(millis: Long): String = String.format(
        "%02dh:%02dm:%02ds",
        TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(
            TimeUnit.MILLISECONDS.toDays(
                millis
            )
        ),
        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(
                millis
            )
        ),
        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(
                millis
            )
        )
    )

    class ToggleProjectStatusCallback : ActionCallback {
        override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
        ) {
            val isRunning = parameters[PARAM_PROJECT_STATUS_KEY] ?: false
            updateAppWidgetState(context, glanceId) {
                it[PREF_PROJECT_STATUS_KEY] = !isRunning
            }
            TrackifyWidget().update(context, glanceId)
        }
    }


}


