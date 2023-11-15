package dev.sasikanth.rss.reader.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.cash.paging.PagingSourceLoadParamsAppend
import app.cash.paging.PagingSourceLoadParamsPrepend
import app.cash.paging.PagingSourceLoadParamsRefresh
import app.cash.paging.PagingSourceLoadResultInvalid
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.sqldelight.Query
import app.cash.sqldelight.TransactionCallbacks
import dev.sasikanth.rss.reader.database.PostQueries
import dev.sasikanth.rss.reader.models.local.PostWithMetadata
import dev.sasikanth.rss.reader.refresh.LastUpdatedAt.UpdatedFrom
import dev.sasikanth.rss.reader.utils.Constants.NUMBER_OF_FEATURED_POSTS
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates
import kotlinx.coroutines.withContext

/**
 * This custom paging source monitors and fetches posts. It's quite similar to
 * [OffsetQueryPagingSource] found in SQLDelight's paging extension. The main difference here is
 * that it controls when to call the 'invalidate' function upon changes in the query results.
 *
 * Whenever articles get refreshed, we don't want the UI to update unless it's triggered by
 * [UpdatedFrom.SwipeToRefresh] or a null value. This approach ensures that the posts list doesn't
 * get refreshed in the UI when the data changes in the background. This helps maintain scroll
 * positions and improves the user experience.
 */
internal class PostsPagingSource(
  private val selectedFeed: String?,
  private val postQueries: PostQueries,
  private val context: CoroutineContext,
  private val updatedFrom: () -> UpdatedFrom?,
) : PagingSource<Int, PostWithMetadata>(), Query.Listener {

  private var currentQuery: Query<PostWithMetadata>? by
    Delegates.observable(null) { _, old, new ->
      old?.removeListener(this)
      new?.addListener(this)
    }

  init {
    registerInvalidatedCallback {
      currentQuery?.removeListener(this)
      currentQuery = null
    }
  }

  override fun queryResultsChanged() {
    val updatedFrom = updatedFrom()
    when (updatedFrom) {
      UpdatedFrom.BackgroundRefresh,
      UpdatedFrom.AppStart -> {
        // no-op: trigger user refresh by calling `LazyPagingItems#refresh`
      }
      else -> {
        invalidate()
      }
    }
  }

  override val jumpingSupported
    get() = true

  override fun getRefreshKey(state: PagingState<Int, PostWithMetadata>): Int? =
    state.anchorPosition?.let { maxOf(0, it - (state.config.initialLoadSize / 2)) }

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostWithMetadata> =
    withContext(context) {
      val key = params.key ?: 0
      val limit =
        when (params) {
          is PagingSourceLoadParamsPrepend<*> -> minOf(key, params.loadSize)
          else -> params.loadSize
        }

      val getPagingSourceLoadResult:
        TransactionCallbacks.() -> PagingSourceLoadResultPage<Int, PostWithMetadata> =
        {
          val count =
            postQueries
              .count(feedLink = selectedFeed, featuredPostsLimit = NUMBER_OF_FEATURED_POSTS)
              .executeAsOne()
              .toInt()

          val offset =
            when (params) {
              is PagingSourceLoadParamsPrepend<*> -> maxOf(0, key - params.loadSize)
              is PagingSourceLoadParamsAppend<*> -> key
              is PagingSourceLoadParamsRefresh<*> ->
                if (key >= count) maxOf(0, count - params.loadSize) else key
              else -> error("Unknown PagingSourceLoadParams ${params::class}")
            }
          val data =
            postQueries
              .posts(
                feedLink = selectedFeed,
                featuredPostsLimit = NUMBER_OF_FEATURED_POSTS,
                limit = limit.toLong(),
                offset = offset.toLong(),
                mapper = ::PostWithMetadata
              )
              .also { currentQuery = it }
              .executeAsList()
          val nextPosToLoad = offset + data.size
          PagingSourceLoadResultPage(
            data = data,
            prevKey = offset.takeIf { it > 0 && data.isNotEmpty() },
            nextKey =
              nextPosToLoad.takeIf { data.isNotEmpty() && data.size >= limit && it < count },
            itemsBefore = offset,
            itemsAfter = maxOf(0, count - nextPosToLoad),
          )
        }
      val loadResult = postQueries.transactionWithResult(bodyWithReturn = getPagingSourceLoadResult)
      (if (invalid) PagingSourceLoadResultInvalid() else loadResult)
    }
}
