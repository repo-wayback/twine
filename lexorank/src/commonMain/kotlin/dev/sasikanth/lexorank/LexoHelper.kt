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

object LexoHelper {

  fun arrayCopy(
    sourceArray: IntArray,
    sourceIndex: Int,
    destinationArray: IntArray,
    destinationIndex: Int,
    length: Int
  ) {
    var destination = destinationIndex
    val finalLength = sourceIndex + length
    for (i in sourceIndex until finalLength) {
      destinationArray[destination] = sourceArray[i]
      ++destination
    }
    // while (length-- > 0) destinationArray[destinationIndex++] = sourceArray[sourceIndex++]
  }
}
