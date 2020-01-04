package com.example.tm18app.network;

import android.content.Context;
import android.util.Log;

import com.example.tm18app.R;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

/**
 * A custom  implementation of a {@link DataSource}. This class is useful when downloaded media
 * has to be cached in order to save network requests and bandwidth.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 25.12.2019
 */
public class CacheDataSourceFactory implements DataSource.Factory {
    private final Context context;
    private final DefaultDataSourceFactory defaultDatasourceFactory;
    private final long maxFileSize, maxCacheSize;

    /**
     * The {@link SimpleCache} cannot be used multiple times by different {@link DataSource} entities.
     * For that reason, a Singleton is implemented.
     */
    static class SimpleCacheSingleton {
        static SimpleCache simpleCache;

        static SimpleCache getSimpleCache(File file, LeastRecentlyUsedCacheEvictor evictor){
            if(simpleCache == null)
                simpleCache = new SimpleCache(file, evictor);
            return simpleCache;
        }

    }

    public CacheDataSourceFactory(Context context, long maxCacheSize, long maxFileSize) {
        super();
        this.context = context;
        this.maxCacheSize = maxCacheSize;
        this.maxFileSize = maxFileSize;
        String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        defaultDatasourceFactory = new DefaultDataSourceFactory(this.context,
                bandwidthMeter,
                new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));
    }

    @Override
    public DataSource createDataSource() {
        LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(maxCacheSize);
        SimpleCache simpleCache =
                SimpleCacheSingleton.getSimpleCache(new File(context.getCacheDir(), "media"), evictor);
        return new CacheDataSource(simpleCache, defaultDatasourceFactory.createDataSource(),
                new FileDataSource(), new CacheDataSink(simpleCache, maxFileSize),
                CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
    }
}