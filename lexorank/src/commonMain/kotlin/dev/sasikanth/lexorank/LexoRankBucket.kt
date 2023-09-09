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
package dev.sasikanth.lexorank

class LexoRankBucket private constructor(private val value: LexoInteger) {

  companion object {
    val BUCKET_0: LexoRankBucket by lazy {
      LexoRankBucket(LexoInteger.parse("0", LexoRank.NUMERAL_SYSTEM))
    }

    val BUCKET_1: LexoRankBucket by lazy {
      LexoRankBucket(LexoInteger.parse("1", LexoRank.NUMERAL_SYSTEM))
    }

    val BUCKET_2: LexoRankBucket by lazy {
      LexoRankBucket(LexoInteger.parse("2", LexoRank.NUMERAL_SYSTEM))
    }

    private val VALUES: List<LexoRankBucket> by lazy { listOf(BUCKET_0, BUCKET_1, BUCKET_2) }

    fun max(): LexoRankBucket {
      return VALUES[VALUES.size - 1]
    }

    fun from(str: String): LexoRankBucket {
      val valBucket = LexoInteger.parse(str, LexoRank.NUMERAL_SYSTEM)
      for (bucket in VALUES) {
        if (bucket.value.equals(valBucket)) {
          return bucket
        }
      }
      throw IllegalArgumentException("Unknown bucket: $str")
    }

    fun resolve(bucketId: Int): LexoRankBucket {
      for (bucket in VALUES) {
        if (bucket.equals(from(bucketId.toString()))) {
          return bucket
        }
      }
      throw IllegalArgumentException("No bucket found with id $bucketId")
    }
  }

  fun format(): String {
    return value.format()
  }

  fun next(): LexoRankBucket {
    return when (this) {
      BUCKET_0 -> BUCKET_1
      BUCKET_1 -> BUCKET_2
      BUCKET_2 -> BUCKET_0
      else -> BUCKET_2
    }
  }

  fun prev(): LexoRankBucket {
    return when (this) {
      BUCKET_0 -> BUCKET_2
      BUCKET_1 -> BUCKET_0
      BUCKET_2 -> BUCKET_1
      else -> BUCKET_0
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    if (other !is LexoRankBucket) {
      return false
    }

    return this.value.equals(other.value)
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}
