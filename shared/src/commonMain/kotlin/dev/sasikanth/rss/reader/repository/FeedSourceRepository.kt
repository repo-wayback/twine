/*
 * Copyright 2023 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.repository

import androidx.paging.PagingSource
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.database.FeedSourceQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class FeedSourceRepository(
  private val feedSourceQueries: FeedSourceQueries,
  dispatchersProvider: DispatchersProvider
) {

  private val ioDispatcher = dispatchersProvider.io

  suspend fun save(sources: List<String>) {
    withContext(ioDispatcher) {
      feedSourceQueries.transaction {
        sources.forEach { source -> feedSourceQueries.save(source) }
      }
    }
  }

  fun search(query: String): PagingSource<Int, String> {
    return QueryPagingSource(
      countQuery = feedSourceQueries.countSearchResults(query),
      transacter = feedSourceQueries,
      context = ioDispatcher,
      queryProvider = { limit, offset -> feedSourceQueries.search(query, limit, offset) }
    )
  }
}
