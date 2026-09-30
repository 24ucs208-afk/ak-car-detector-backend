package com.akcardetector.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.akcardetector.entity.MechanicShop;
import com.akcardetector.repository.MechanicShopRepository;

@RestController
@RequestMapping("/api/mechanics")
@CrossOrigin(origins = "*")
public class MechanicShopController {

    private final MechanicShopRepository mechanicShopRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public MechanicShopController(
            MechanicShopRepository mechanicShopRepository) {

        this.mechanicShopRepository = mechanicShopRepository;
    }

    // =========================================================
    // GET ALL MECHANIC SHOPS FROM DATABASE
    // =========================================================

    @GetMapping
    public ResponseEntity<List<MechanicShop>> getAllMechanicShops() {

        return ResponseEntity.ok(
                mechanicShopRepository.findAll()
        );
    }

    // =========================================================
    // GET NEARBY MECHANICS FROM DATABASE
    // =========================================================

    @GetMapping("/nearby")
    public ResponseEntity<List<MechanicShop>> getNearbyMechanics(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {

        List<MechanicShop> shops =
                mechanicShopRepository.findAll();

        List<MechanicShop> nearby =
                new ArrayList<>();

        for (MechanicShop shop : shops) {

            double distance = calculateDistance(
                    latitude,
                    longitude,
                    shop.getLatitude(),
                    shop.getLongitude()
            );

            if (distance <= radiusKm) {
                nearby.add(shop);
            }
        }

        nearby.sort(
                Comparator.comparingDouble(
                        shop -> calculateDistance(
                                latitude,
                                longitude,
                                shop.getLatitude(),
                                shop.getLongitude()
                        )
                )
        );

        return ResponseEntity.ok(nearby);
    }

    // =========================================================
    // GET NEARBY CAR MECHANICS FROM OPENSTREETMAP
    // =========================================================

    @GetMapping("/nearby-osm")
    public ResponseEntity<List<Map<String, Object>>>
    getNearbyMechanicsFromOSM(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        System.out.println("========================================");
        System.out.println("CAR MECHANIC SEARCH STARTED");
        System.out.println("Latitude  : " + latitude);
        System.out.println("Longitude : " + longitude);

        // 20 KM
        int radius = 20000;

        // =====================================================
        // BROADER OPENSTREETMAP QUERY
        // =====================================================

        String overpassQuery =
                "[out:json][timeout:25];"
                + "("

                // Standard car repair
                + "nwr[\"shop\"=\"car_repair\"]"
                + "(around:" + radius + "," + latitude + "," + longitude + ");"

                // Craft car repair
                + "nwr[\"craft\"=\"car_repair\"]"
                + "(around:" + radius + "," + latitude + "," + longitude + ");"

                // Amenity car repair
                + "nwr[\"amenity\"=\"car_repair\"]"
                + "(around:" + radius + "," + latitude + "," + longitude + ");"

                // Car shop / service
                + "nwr[\"shop\"=\"car\"]"
                + "(around:" + radius + "," + latitude + "," + longitude + ");"

                // Vehicle service
                + "nwr[\"service\"=\"vehicle\"]"
                + "(around:" + radius + "," + latitude + "," + longitude + ");"

                // Car service
                + "nwr[\"service\"=\"car_repair\"]"
                + "(around:" + radius + "," + latitude + "," + longitude + ");"

                + ");"
                + "out center tags;";

        System.out.println("CAR REPAIR OVERPASS QUERY CREATED");

        // =====================================================
        // OVERPASS SERVERS
        // =====================================================

        String[] overpassServers = {

                "https://overpass-api.de/api/interpreter",

                "https://overpass.kumi.systems/api/interpreter"
        };

        Map<String, Object> jsonResponse = null;

        // =====================================================
        // TRY OVERPASS SERVERS
        // =====================================================

        for (String server : overpassServers) {

            try {

                System.out.println(
                        "CALLING OVERPASS SERVER: " + server
                );

                URI uri =
                        UriComponentsBuilder
                                .fromUriString(server)
                                .queryParam(
                                        "data",
                                        overpassQuery
                                )
                                .build()
                                .encode()
                                .toUri();

                ResponseEntity<Map<String, Object>> response =
                        restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<
                                        Map<String, Object>>() {
                                }
                        );

                System.out.println(
                        "OSM STATUS: " +
                                response.getStatusCode()
                );

                jsonResponse = response.getBody();

                if (jsonResponse != null) {

                    System.out.println(
                            "OVERPASS SERVER SUCCESS"
                    );

                    break;
                }

            } catch (Exception e) {

                System.out.println(
                        "OVERPASS SERVER FAILED: " + server
                );

                System.out.println(
                        "Trying next server..."
                );
            }
        }

        // =====================================================
        // NO OSM RESPONSE
        // =====================================================

        if (jsonResponse == null) {

            System.out.println(
                    "ALL OVERPASS SERVERS FAILED"
            );

            System.out.println(
                    "========================================"
            );

            return ResponseEntity.ok(result);
        }

        // =====================================================
        // GET ELEMENTS
        // =====================================================

        Object elementsObject =
                jsonResponse.get("elements");

        if (!(elementsObject instanceof List<?>)) {

            System.out.println(
                    "OSM ELEMENTS NOT FOUND"
            );

            return ResponseEntity.ok(result);
        }

        List<?> elements =
                (List<?>) elementsObject;

        System.out.println(
                "CAR REPAIR ELEMENTS FOUND: "
                        + elements.size()
        );

        // =====================================================
        // PROCESS EACH OSM ELEMENT
        // =====================================================

        for (Object elementObject : elements) {

            if (!(elementObject instanceof Map<?, ?>)) {
                continue;
            }

            Map<?, ?> element =
                    (Map<?, ?>) elementObject;

            double shopLat = 0.0;
            double shopLon = 0.0;

            // =================================================
            // NODE LOCATION
            // =================================================

            Object latObject =
                    element.get("lat");

            Object lonObject =
                    element.get("lon");

            if (latObject instanceof Number &&
                    lonObject instanceof Number) {

                shopLat =
                        ((Number) latObject)
                                .doubleValue();

                shopLon =
                        ((Number) lonObject)
                                .doubleValue();

            } else {

                // =============================================
                // WAY / RELATION CENTER
                // =============================================

                Object centerObject =
                        element.get("center");

                if (centerObject instanceof Map<?, ?>) {

                    Map<?, ?> center =
                            (Map<?, ?>) centerObject;

                    Object centerLat =
                            center.get("lat");

                    Object centerLon =
                            center.get("lon");

                    if (centerLat instanceof Number &&
                            centerLon instanceof Number) {

                        shopLat =
                                ((Number) centerLat)
                                        .doubleValue();

                        shopLon =
                                ((Number) centerLon)
                                        .doubleValue();
                    }
                }
            }

            // =================================================
            // INVALID LOCATION
            // =================================================

            if (shopLat == 0.0 ||
                    shopLon == 0.0) {

                continue;
            }

            // =================================================
            // DISTANCE
            // =================================================

            double distance =
                    calculateDistance(
                            latitude,
                            longitude,
                            shopLat,
                            shopLon
                    );

            if (distance > 20.0) {
                continue;
            }

            // =================================================
            // OSM TAGS
            // =================================================

            Map<String, Object> tags =
                    new HashMap<>();

            Object tagsObject =
                    element.get("tags");

            if (tagsObject instanceof Map<?, ?>) {

                Map<?, ?> osmTags =
                        (Map<?, ?>) tagsObject;

                for (Map.Entry<?, ?> entry :
                        osmTags.entrySet()) {

                    if (entry.getKey() != null) {

                        tags.put(
                                entry.getKey().toString(),
                                entry.getValue()
                        );
                    }
                }
            }

            // =================================================
            // NAME
            // =================================================

            String name =
                    getTag(
                            tags,
                            "name"
                    );

            if (name.isEmpty()) {

                name =
                        "Nearby Car Mechanic";
            }

            // =================================================
            // ADDRESS
            // =================================================

            String address =
                    getAddressFromTags(tags);

            if (address.isEmpty()) {

                address =
                        "Nearby car service";
            }

            // =================================================
            // PHONE
            // =================================================

            String phone =
                    getTag(
                            tags,
                            "phone"
                    );

            if (phone.isEmpty()) {

                phone =
                        getTag(
                                tags,
                                "contact:phone"
                        );
            }

            // =================================================
            // CREATE SHOP
            // =================================================

            Map<String, Object> shop =
                    new HashMap<>();

            shop.put(
                    "id",
                    element.get("id")
            );

            shop.put(
                    "name",
                    name
            );

            shop.put(
                    "shopType",
                    "Car Mechanic"
            );

            shop.put(
                    "address",
                    address
            );

            shop.put(
                    "phone",
                    phone
            );

            shop.put(
                    "latitude",
                    shopLat
            );

            shop.put(
                    "longitude",
                    shopLon
            );

            shop.put(
                    "distanceKm",
                    roundDistance(distance)
            );

            result.add(shop);
        }

        // =====================================================
        // REMOVE DUPLICATES
        // =====================================================

        result =
                removeDuplicateShops(result);

        // =====================================================
        // SORT NEAREST FIRST
        // =====================================================

        result.sort(
                Comparator.comparingDouble(
                        shop ->
                                ((Number)
                                        shop.get(
                                                "distanceKm"
                                        ))
                                        .doubleValue()
                )
        );

        // =====================================================
        // PRINT RESULTS
        // =====================================================

        System.out.println(
                "CAR MECHANICS FOUND: "
                        + result.size()
        );

        for (Map<String, Object> shop :
                result) {

            System.out.println(
                    shop.get("name")
                            + " - "
                            + shop.get("distanceKm")
                            + " km"
            );
        }

        System.out.println(
                "========================================"
        );

        return ResponseEntity.ok(result);
    }

    // =========================================================
    // GET TAG
    // =========================================================

    private String getTag(
            Map<String, Object> tags,
            String key) {

        Object value =
                tags.get(key);

        if (value == null) {
            return "";
        }

        return value.toString().trim();
    }

    // =========================================================
    // CREATE ADDRESS
    // =========================================================

    private String getAddressFromTags(
            Map<String, Object> tags) {

        List<String> parts =
                new ArrayList<>();

        String houseNumber =
                getTag(
                        tags,
                        "addr:housenumber"
                );

        String street =
                getTag(
                        tags,
                        "addr:street"
                );

        String area =
                getTag(
                        tags,
                        "addr:suburb"
                );

        String city =
                getTag(
                        tags,
                        "addr:city"
                );

        String postcode =
                getTag(
                        tags,
                        "addr:postcode"
                );

        if (!houseNumber.isEmpty()) {
            parts.add(houseNumber);
        }

        if (!street.isEmpty()) {
            parts.add(street);
        }

        if (!area.isEmpty()) {
            parts.add(area);
        }

        if (!city.isEmpty()) {
            parts.add(city);
        }

        if (!postcode.isEmpty()) {
            parts.add(postcode);
        }

        return String.join(
                ", ",
                parts
        );
    }

    // =========================================================
    // REMOVE DUPLICATES
    // =========================================================

    private List<Map<String, Object>>
    removeDuplicateShops(
            List<Map<String, Object>> shops) {

        Set<String> unique =
                new HashSet<>();

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map<String, Object> shop :
                shops) {

            String name =
                    safeString(
                            shop.get("name")
                    );

            String lat =
                    safeString(
                            shop.get("latitude")
                    );

            String lon =
                    safeString(
                            shop.get("longitude")
                    );

            String key =
                    name.toLowerCase()
                            + "_"
                            + lat
                            + "_"
                            + lon;

            if (unique.add(key)) {

                result.add(shop);
            }
        }

        return result;
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safeString(
            Object value) {

        if (value == null) {
            return "";
        }

        return value.toString();
    }

    // =========================================================
    // ROUND DISTANCE
    // =========================================================

    private double roundDistance(
            double distance) {

        return Math.round(
                distance * 100.0
        ) / 100.0;
    }

    // =========================================================
    // HAVERSINE DISTANCE
    // =========================================================

    private double calculateDistance(
        double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final double EARTH_RADIUS_KM =
                6371.0;

        double dLat =
                Math.toRadians(
                        lat2 - lat1
                );

        double dLon =
                Math.toRadians(
                        lon2 - lon1
                );

        double a =
                Math.sin(dLat / 2)
                        * Math.sin(dLat / 2)
                +
                Math.cos(
                        Math.toRadians(lat1)
                )
                        * Math.cos(
                                Math.toRadians(lat2)
                        )
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS_KM * c;
    }
}