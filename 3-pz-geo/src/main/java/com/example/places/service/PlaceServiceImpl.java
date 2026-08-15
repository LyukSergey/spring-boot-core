package com.example.places.service;

import com.example.places.dto.CreatePlaceRequest;
import com.example.places.dto.NearbyDto;
import com.example.places.dto.PlaceDto;
import com.example.places.geo.GeocodingService;
import com.example.places.repo.PlaceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * СКЕЛЕТ реалізації бізнес-логіки (архітектура на інтерфейсах).
 *
 * <p>Залежності вже впроваджені, транзакційні межі й сигнатури проставлені —
 * лишається наповнити тіла методів. Підказки в TODO та у файлі {@code pz-3-geo.md}.
 *
 * <p>Гео-шар ({@link GeocodingService}, {@code GeoDistance}) реалізований ПОВНІСТЮ,
 * тому {@code create()} і {@code nearby()} можна одразу спиратись на нього.
 */
@Service
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository repo;
    private final GeocodingService geocoding;

    public PlaceServiceImpl(PlaceRepository repo, GeocodingService geocoding) {
        this.repo = repo;
        this.geocoding = geocoding;
    }

    @Override
    @Transactional
    public PlaceDto create(CreatePlaceRequest req) {
        // TODO: new Place(name, address, category);
        //       geocoding.geocode(req.address()).ifPresent(c -> p.setCoordinates(c.lat(), c.lon()));
        //       return PlaceDto.of(repo.save(p));
        throw new UnsupportedOperationException("PlaceServiceImpl.create: реалізувати (Крок 5)");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlaceDto> byCategory(String category, Pageable pageable) {
        // TODO: return repo.findByCategory(category, pageable).map(PlaceDto::of);
        throw new UnsupportedOperationException("PlaceServiceImpl.byCategory: реалізувати (Крок 5)");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceDto> searchByName(String part) {
        // TODO: return repo.findByNameContainingIgnoreCase(part).stream().map(PlaceDto::of).toList();
        throw new UnsupportedOperationException("PlaceServiceImpl.searchByName: реалізувати (Крок 5)");
    }

    @Override
    @Transactional(readOnly = true)
    public List<NearbyDto> nearby(double lat, double lon, double radiusKm) {
        // TODO: repo.findByLatIsNotNullAndLonIsNotNull().stream()
        //          .map(p -> new NearbyDto(PlaceDto.of(p), GeoDistance.km(lat, lon, p.getLat(), p.getLon())))
        //          .filter(n -> n.distanceKm() <= radiusKm)
        //          .sorted(Comparator.comparingDouble(NearbyDto::distanceKm))
        //          .toList();
        throw new UnsupportedOperationException("PlaceServiceImpl.nearby: реалізувати (Крок 5, Haversine)");
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // TODO: if (!repo.existsById(id)) throw new PlaceNotFoundException(id);
        //       repo.deleteById(id);
        throw new UnsupportedOperationException("PlaceServiceImpl.delete: реалізувати (Крок 5)");
    }
}
