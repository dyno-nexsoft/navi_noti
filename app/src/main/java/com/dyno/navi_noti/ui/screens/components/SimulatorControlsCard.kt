package com.dyno.navi_noti.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Stop
import com.dyno.navi_noti.R

/// Thẻ điều khiển kịch bản mô phỏng hành trình mẫu để test trước trên đồng hồ
@Composable
fun SimulatorControlsCard(
    isSimulatorRunning: Boolean,
    onStartSimulation: () -> Unit,
    onStopSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null
                )
                Text(stringResource(R.string.demo_journey_title))
            }

            Text(stringResource(R.string.demo_journey_desc))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isSimulatorRunning) {
                    OutlinedButton(onClick = onStopSimulation) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Stop, contentDescription = null)
                            Text(stringResource(R.string.demo_stop_btn))
                        }
                    }
                } else {
                    Button(onClick = onStartSimulation) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                            Text(stringResource(R.string.demo_start_btn))
                        }
                    }
                }
            }
        }
    }
}
