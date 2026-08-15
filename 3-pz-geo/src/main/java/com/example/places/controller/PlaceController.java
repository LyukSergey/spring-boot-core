package com.example.places.controller;

import com.example.places.dto.CreatePlaceRequest;
import com.example.places.dto.NearbyDto;
import com.example.places.dto.PlaceDto;
import com.example.places.service.PlaceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST-контролер місць. СКЕЛЕТ: маршрути/статуси/валідація вже описані,
 * тіла делегують у сервіс — лишається розкоментувати виклики.
 *
 * <p>Haversine у контролері НЕ рахуємо — це робота сервісу (штраф −2 інакше).
 */
@RestController
@RequestMapping("/places")
public class PlaceController {

    private final PlaceService service;

    public PlaceController(PlaceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceDto create(@Valid @RequestBody CreatePlaceRequest req) {
        // TODO: return service.create(req); // координати підтягнуться автоматично
        throw new UnsupportedOperationException("PlaceController.create: делегувати у сервіс (Крок 6)");
    }

    /** GET /places?category=кафе&page=0&size=20 */
    @GetMapping
    public Page<PlaceDto> byCategory(@RequestParam String category,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        // TODO: return service.byCategory(category, PageRequest.of(page, size, Sort.by("name")));
        throw new UnsupportedOperationException("PlaceController.byCategory: делегувати у сервіс (Крок 6)");
    }

    /** GET /places/search?name=part */
    @GetMapping("/search")
    public List<PlaceDto> search(@RequestParam String name) {
        // TODO: return service.searchByName(name);
        throw new UnsupportedOperationException("PlaceController.search: делегувати у сервіс (Крок 6)");
    }

    /** GET /places/nearby?lat=49.84&lon=24.03&radiusKm=5 */
    @GetMapping("/nearby")
    public List<NearbyDto> nearby(@RequestParam double lat,
                                  @RequestParam double lon,
                                  @RequestParam(defaultValue = "5") double radiusKm) {
        // TODO: return service.nearby(lat, lon, radiusKm);
        throw new UnsupportedOperationException("PlaceController.nearby: делегувати у сервіс (Крок 6)");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // TODO: service.delete(id);
        throw new UnsupportedOperationException("PlaceController.delete: делегувати у сервіс (Крок 6)");
    }
}
