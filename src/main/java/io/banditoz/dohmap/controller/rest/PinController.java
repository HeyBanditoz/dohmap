package io.banditoz.dohmap.controller.rest;

import io.banditoz.dohmap.database.mapper.EstablishmentMapper;
import io.banditoz.dohmap.model.BaseResponse;
import io.banditoz.dohmap.model.Pin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pins")
public class PinController {
    private final EstablishmentMapper establishmentMapper;

    @Autowired
    public PinController(EstablishmentMapper establishmentMapper) {
        this.establishmentMapper = establishmentMapper;
    }

    @GetMapping("all")
    ResponseEntity<BaseResponse<List<Pin>>> getAllPins() {
//        Map<String, List<Pin>> map = establishmentMapper.getPins().stream()
//                .collect(Collectors.groupingBy(pin -> pin.establishment().city()));
        return ResponseEntity.ok(BaseResponse.of(establishmentMapper.getPins()));
    }
}
