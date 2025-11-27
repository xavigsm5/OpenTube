package com.opentube.data.extractor

/**
 * Resultado paginado
 */
data class PagedResult<T>(
    val items: List<T>,
    val nextPageUrl: String?
)
