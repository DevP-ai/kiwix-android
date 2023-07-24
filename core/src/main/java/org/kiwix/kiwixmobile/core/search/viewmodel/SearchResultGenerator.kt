/*
 * Kiwix Android
 * Copyright (c) 2020 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.kiwix.kiwixmobile.core.search.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.kiwix.kiwixmobile.core.reader.ZimFileReader
import org.kiwix.kiwixmobile.core.search.adapter.SearchListItem
import org.kiwix.kiwixmobile.core.search.adapter.SearchListItem.ZimSearchResultListItem
import javax.inject.Inject

interface SearchResultGenerator {
  suspend fun generateSearchResults(
    searchTerm: String,
    zimFileReader: ZimFileReader?
  ): List<SearchListItem>
}

class ZimSearchResultGenerator @Inject constructor() : SearchResultGenerator {

  override suspend fun generateSearchResults(searchTerm: String, zimFileReader: ZimFileReader?) =
    withContext(Dispatchers.IO) {
      if (searchTerm.isNotEmpty()) readResultsFromZim(searchTerm, zimFileReader)
      else emptyList()
    }

  private suspend fun readResultsFromZim(
    searchTerm: String,
    reader: ZimFileReader?
  ) =
    reader.also { yield() }
      ?.searchSuggestions(searchTerm)
      .also { yield() }
      .run {
        val suggestionList = mutableListOf<ZimSearchResultListItem>()
        val suggestionIterator =
          this?.getResults(0, this.estimatedMatches.toInt())
        suggestionIterator?.let {
          while (it.hasNext()) {
            val entry = it.next()
            suggestionList.add(ZimSearchResultListItem(entry.title))
          }
        }
        return@run suggestionList
      }
}
