package com.dyno.navi_noti.ui.screens.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import com.dyno.navi_noti.R

/// Thẻ hiển thị và hướng dẫn cấp quyền Notification Listener Access
@Composable
fun PermissionCard(
    isGranted: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isGranted) {
                        Icons.Outlined.NotificationsActive
                    } else {
                        Icons.Outlined.NotificationsOff
                    },
                    contentDescription = null
                )

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        stringResource(
                            if (isGranted) R.string.perm_ready_title else R.string.perm_needed_title
                        )
                    )

                    Text(
                        stringResource(
                            if (isGranted) R.string.perm_ready_desc else R.string.perm_needed_desc
                        )
                    )
                }
            }

            if (!isGranted) {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.perm_grant_btn))
                }
            }
        }
    }
}
