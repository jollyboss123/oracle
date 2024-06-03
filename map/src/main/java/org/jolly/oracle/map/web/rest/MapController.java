package org.jolly.oracle.map.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jolly.oracle.map.service.MapService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/oracle")
@Validated
@RequiredArgsConstructor
@Slf4j
public class MapController {
    private final MapService mapService;
    private final CacheManager cacheManager;
    public static final String PROCESSED_VAR_REQUEST_CACHE = "processedVarRequest";

    @PostMapping("/var")
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    ResponseEntity<Void> map(@Valid @RequestBody VarRequest request) {
        // 1. hash the request + timestamp into a job id
        String h = hash(request);
        // 2. check if job id exists or processed before
        Cache cache = cacheManager.getCache(PROCESSED_VAR_REQUEST_CACHE);
        if (cache != null && (cache.get(h) != null)) {
            log.info("request has been processed before, skipping to reduce step");
            return ResponseEntity.accepted().build();
        }
        // 3. if job id does not exist then launch service
        mapService.execute(request)
                .thenRunAsync(() -> {
                    if (cache != null) {
                        cache.putIfAbsent(h, true);
                    }
                });
        return ResponseEntity.accepted().build();
    }

    private static String hash(VarRequest request) {
        String rawString = request.toString() + LocalDate.now();
        return DigestUtils.sha256Hex(rawString);
    }
}
