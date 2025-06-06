/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.BottomBarWithGradientShadow
import dev.sasikanth.rss.reader.resources.icons.NewGroup
import dev.sasikanth.rss.reader.resources.icons.RSS
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.feedsBottomBarNewFeed
import twine.shared.generated.resources.feedsBottomBarNewGroup

@Composable
internal fun BottomSheetExpandedBottomBar(
  modifier: Modifier = Modifier,
  onNewGroupClick: () -> Unit,
  onNewFeedClick: () -> Unit,
) {
  BottomBarWithGradientShadow(modifier) {
    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
      BottomBarItem(
        icon = TwineIcons.NewGroup,
        label = stringResource(Res.string.feedsBottomBarNewGroup),
        modifier = Modifier.weight(1f),
        onClick = onNewGroupClick
      )

      Spacer(Modifier.requiredWidth(8.dp))

      VerticalDivider(
        modifier = Modifier.requiredHeight(32.dp).align(Alignment.CenterVertically),
        color = AppTheme.colorScheme.tintedHighlight,
        thickness = 2.dp
      )

      Spacer(Modifier.requiredWidth(8.dp))

      BottomBarItem(
        icon = TwineIcons.RSS,
        label = stringResource(Res.string.feedsBottomBarNewFeed),
        modifier = Modifier.weight(1f),
        onClick = onNewFeedClick
      )
    }
  }
}

@Composable
private fun BottomBarItem(
  icon: ImageVector,
  label: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Box(
    Modifier.clip(MaterialTheme.shapes.large).clickable { onClick() }.padding(12.dp).then(modifier)
  ) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = AppTheme.colorScheme.textEmphasisHigh
      )

      Spacer(Modifier.requiredHeight(4.dp))

      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.textEmphasisHigh
      )
    }
  }
}
