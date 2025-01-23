package ch.heigvd.dai.caching;

import io.javalin.http.Context;
import io.javalin.http.NotModifiedResponse;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class Cacher {
  private final ConcurrentHashMap<String, LocalDateTime> cache = new ConcurrentHashMap<>();

  public void setCacheHeader(String key, Context ctx) {
    LocalDateTime now;
    if (cache.containsKey(key)) {
      // If it is already in the cache, get the last modification date
      now = cache.get(key);
    } else {
      // Otherwise, set to the current date
      now = LocalDateTime.now();
      cache.put(key, now);
    }

    // Add the last modification date to the response
    ctx.header("Last-Modified", String.valueOf(now));
  }

  public void setLastModified(String key) {
    cache.put(key, LocalDateTime.now());
  }

  public void checkCache(String key, Context ctx) {
    // Get the last known modification date of the user
    LocalDateTime lastKnownModification = ctx.headerAsClass("If-Modified-Since", LocalDateTime.class).getOrDefault(null);

    // Check if the user has been modified since the last known modification date
    if (lastKnownModification != null && cache.get(key).equals(lastKnownModification)) {
      throw new NotModifiedResponse();
    }
  }

  public void removeCache(String key) {
    cache.remove(key);
  }
}
