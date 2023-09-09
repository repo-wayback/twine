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

import dev.sasikanth.lexorank.numeralSystems.LexoNumeralSystem36

class LexoRank
private constructor(private val bucket: LexoRankBucket, private val decimal: LexoDecimal) {

  companion object {
    val NUMERAL_SYSTEM: LexoNumeralSystem36 = LexoNumeralSystem36()

    private val ZERO_DECIMAL: LexoDecimal by lazy { LexoDecimal.parse("0", NUMERAL_SYSTEM) }

    private val ONE_DECIMAL: LexoDecimal by lazy { LexoDecimal.parse("1", NUMERAL_SYSTEM) }

    private val EIGHT_DECIMAL: LexoDecimal by lazy { LexoDecimal.parse("8", NUMERAL_SYSTEM) }

    private val MIN_DECIMAL: LexoDecimal by lazy { ZERO_DECIMAL }

    private val MAX_DECIMAL: LexoDecimal by lazy {
      LexoDecimal.parse("1000000", NUMERAL_SYSTEM).subtract(ONE_DECIMAL)
    }

    private val MID_DECIMAL: LexoDecimal by lazy { between(MIN_DECIMAL, MAX_DECIMAL) }

    private val INITIAL_MIN_DECIMAL: LexoDecimal by lazy {
      LexoDecimal.parse("100000", NUMERAL_SYSTEM)
    }

    private val INITIAL_MAX_DECIMAL: LexoDecimal by lazy {
      LexoDecimal.parse(NUMERAL_SYSTEM.toChar(NUMERAL_SYSTEM.base() - 2) + "00000", NUMERAL_SYSTEM)
    }

    fun min(): LexoRank {
      return from(LexoRankBucket.BUCKET_0, MIN_DECIMAL)
    }

    fun middle(): LexoRank {
      val minLexoRank = min()
      return minLexoRank.between(max(minLexoRank.bucket))
    }

    fun max(bucket: LexoRankBucket = LexoRankBucket.BUCKET_0): LexoRank {
      return from(bucket, MAX_DECIMAL)
    }

    fun initial(bucket: LexoRankBucket): LexoRank {
      return if (bucket == LexoRankBucket.BUCKET_0) from(bucket, INITIAL_MIN_DECIMAL)
      else from(bucket, INITIAL_MAX_DECIMAL)
    }

    fun between(oLeft: LexoDecimal, oRight: LexoDecimal): LexoDecimal {
      if (oLeft.getSystem().base() != oRight.getSystem().base()) {
        throw IllegalArgumentException("Expected same system")
      }

      var left = oLeft
      var right = oRight
      var nLeft: LexoDecimal
      if (oLeft.getScale() < oRight.getScale()) {
        nLeft = oRight.setScale(oLeft.getScale(), false)
        if (oLeft.compareTo(nLeft) >= 0) {
          return mid(oLeft, oRight)
        }

        right = nLeft
      }

      if (oLeft.getScale() > right.getScale()) {
        nLeft = oLeft.setScale(right.getScale(), true)
        if (nLeft.compareTo(right) >= 0) {
          return mid(oLeft, oRight)
        }

        left = nLeft
      }

      var nRight: LexoDecimal
      var scale = left.getScale()
      while (scale > 0) {
        val nScale1 = scale - 1
        val nLeft1 = left.setScale(nScale1, true)
        nRight = right.setScale(nScale1, false)
        val cmp = nLeft1.compareTo(nRight)
        if (cmp == 0) {
          return checkMid(oLeft, oRight, nLeft1)
        }
        if (nLeft1.compareTo(nRight) > 0) {
          break
        }

        scale = nScale1
        left = nLeft1
      }

      var mid = middleInternal(oLeft, oRight, left, right)

      var nScale: Int
      var mScale = mid.getScale()
      while (mScale > 0) {
        nScale = mScale - 1
        val nMid = mid.setScale(nScale)
        if (oLeft.compareTo(nMid) >= 0 || nMid.compareTo(oRight) >= 0) {
          break
        }

        mid = nMid
        mScale = nScale
      }

      return mid
    }

    fun parse(str: String): LexoRank {
      val parts = str.split("|")
      val bucket = LexoRankBucket.from(parts[0])
      val decimal = LexoDecimal.parse(parts[1], NUMERAL_SYSTEM)
      return LexoRank(bucket, decimal)
    }

    fun from(bucket: LexoRankBucket, decimal: LexoDecimal): LexoRank {
      if (decimal.getSystem().base() != NUMERAL_SYSTEM.base()) {
        throw IllegalArgumentException("Expected different system")
      }

      return LexoRank(bucket, decimal)
    }

    private fun middleInternal(
      lbound: LexoDecimal,
      rbound: LexoDecimal,
      left: LexoDecimal,
      right: LexoDecimal
    ): LexoDecimal {
      val mid = mid(left, right)
      return checkMid(lbound, rbound, mid)
    }

    private fun checkMid(lbound: LexoDecimal, rbound: LexoDecimal, mid: LexoDecimal): LexoDecimal {
      if (lbound.compareTo(mid) >= 0) {
        return mid(lbound, rbound)
      }

      return if (mid.compareTo(rbound) >= 0) mid(lbound, rbound) else mid
    }

    private fun mid(left: LexoDecimal, right: LexoDecimal): LexoDecimal {
      val sum = left.add(right)
      val mid = sum.multiply(LexoDecimal.half(left.getSystem()))
      val scale = if (left.getScale() > right.getScale()) left.getScale() else right.getScale()
      if (mid.getScale() > scale) {
        val roundDown = mid.setScale(scale, false)
        if (roundDown.compareTo(left) > 0) {
          return roundDown
        }
        val roundUp = mid.setScale(scale, true)
        if (roundUp.compareTo(right) < 0) {
          return roundUp
        }
      }
      return mid
    }

    private fun formatDecimal(decimal: LexoDecimal): String {
      val formattedDecimal = decimal.format()
      val stringBuilder = StringBuilder(formattedDecimal)
      val zero = NUMERAL_SYSTEM.toChar(0)
      var partialIndex = formattedDecimal.indexOf(NUMERAL_SYSTEM.radixPointChar())

      if (partialIndex < 0) {
        partialIndex = formattedDecimal.length
        stringBuilder.append(NUMERAL_SYSTEM.radixPointChar())
      }

      while (partialIndex < 6) {
        stringBuilder.insert(0, zero)
        partialIndex++
      }

      while (stringBuilder[stringBuilder.length - 1] == zero) {
        stringBuilder.setLength(stringBuilder.length - 1)
      }

      return stringBuilder.toString()
    }
  }

  fun genPrev(): LexoRank {
    if (isMax()) {
      return LexoRank(this.bucket, INITIAL_MAX_DECIMAL)
    }

    val floorInteger = decimal.floor()
    val floorDecimal = LexoDecimal.from(floorInteger)
    var nextDecimal = floorDecimal.subtract(EIGHT_DECIMAL)
    if (nextDecimal.compareTo(MIN_DECIMAL) <= 0) {
      nextDecimal = between(MIN_DECIMAL, decimal)
    }

    return LexoRank(this.bucket, nextDecimal)
  }

  fun genNext(): LexoRank {
    if (isMin()) {
      return LexoRank(this.bucket, INITIAL_MIN_DECIMAL)
    }
    val ceilInteger = decimal.ceil()
    val ceilDecimal = LexoDecimal.from(ceilInteger)
    var nextDecimal = ceilDecimal.add(EIGHT_DECIMAL)
    if (nextDecimal.compareTo(MAX_DECIMAL) >= 0) {
      nextDecimal = between(decimal, MAX_DECIMAL)
    }

    return LexoRank(this.bucket, nextDecimal)
  }

  fun between(other: LexoRank): LexoRank {
    if (this.bucket != other.bucket) {
      throw IllegalArgumentException("Between works only within the same bucket")
    }

    val cmp = decimal.compareTo(other.decimal)
    return if (cmp > 0) {
      LexoRank(this.bucket, between(other.decimal, decimal))
    } else if (cmp == 0) {
      throw IllegalArgumentException(
        "Try to rank between issues with same rank this=$this other=$other " +
          "this.decimal=$decimal other.decimal=${other.decimal}"
      )
    } else {
      LexoRank(this.bucket, between(decimal, other.decimal))
    }
  }

  fun getBucket(): LexoRankBucket {
    return bucket
  }

  fun getDecimal(): LexoDecimal {
    return decimal
  }

  fun inNextBucket(): LexoRank {
    return from(bucket.next(), decimal)
  }

  fun inPrevBucket(): LexoRank {
    return from(bucket.prev(), decimal)
  }

  fun isMin(): Boolean {
    return decimal.equals(MIN_DECIMAL)
  }

  fun isMax(): Boolean {
    return decimal.equals(MAX_DECIMAL)
  }

  fun format(): String {
    return bucket.format() + "|" + formatDecimal(decimal)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    if (other !is LexoRank) {
      return false
    }

    return this.value == other.value
  }

  override fun toString(): String {
    return format()
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }

  fun compareTo(other: LexoRank): Int {
    if (this === other) {
      return 0
    }

    return this.value.compareTo(other.value)
  }

  private val value: String = format()
}
