package com.example.utils

import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex
import java.io.File

/**
 * Reliable High-Definition Map Tile Provider Manager for OSMDroid.
 * Supplies working tile sources for Satellite Hybrid, Esri Imagery, Carto Dark, and Terrain.
 */
object MapTileManager {

    /**
     * Google Hybrid Satellite Tile Source (High-definition Satellite Imagery + Street Labels)
     * URL pattern: https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}
     */
    val GoogleHybridSatelliteTileSource: ITileSource = object : OnlineTileSourceBase(
        "GoogleHybridSatellite",
        1,
        20,
        256,
        "",
        arrayOf(
            "https://mt0.google.com/vt/lyrs=y&",
            "https://mt1.google.com/vt/lyrs=y&",
            "https://mt2.google.com/vt/lyrs=y&",
            "https://mt3.google.com/vt/lyrs=y&"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val zoom = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "${baseUrl}x=$x&y=$y&z=$zoom"
        }
    }

    /**
     * Esri World Imagery (HD Aerial Satellite)
     * URL pattern: https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}
     */
    val EsriSatelliteTileSource: ITileSource = object : OnlineTileSourceBase(
        "EsriWorldImagery",
        1,
        19,
        256,
        ".jpg",
        arrayOf(
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/",
            "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val zoom = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "$baseUrl$zoom/$y/$x"
        }
    }

    /**
     * Carto Dark Night Mode
     */
    val CartoDarkTileSource: ITileSource = XYTileSource(
        "CartoDBDark",
        1,
        20,
        256,
        ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/dark_all/",
            "https://b.basemaps.cartocdn.com/dark_all/",
            "https://c.basemaps.cartocdn.com/dark_all/"
        )
    )

    /**
     * OpenTopoMap Topographic Terrain
     */
    val OpenTopoTileSource: ITileSource = XYTileSource(
        "OpenTopoMap",
        1,
        17,
        256,
        ".png",
        arrayOf(
            "https://a.tile.opentopomap.org/",
            "https://b.tile.opentopomap.org/",
            "https://c.tile.opentopomap.org/"
        )
    )

    val StandardOsmTileSource: ITileSource = TileSourceFactory.MAPNIK

    /**
     * Configures osmdroid cache directory, tile size limit, and user agent header.
     */
    fun configureOsmdroid(context: Context) {
        try {
            Configuration.getInstance().apply {
                userAgentValue = context.packageName
                val basePath = File(context.cacheDir, "osmdroid")
                osmdroidBasePath = basePath
                osmdroidTileCache = File(basePath, "tiles")
                tileFileSystemCacheMaxBytes = 100L * 1024 * 1024 // 100 MB
                tileFileSystemCacheTrimBytes = 80L * 1024 * 1024
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
