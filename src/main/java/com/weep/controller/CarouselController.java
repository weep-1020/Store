package com.weep.controller;

import com.weep.entity.Carousel;
import com.weep.response.Result;
import com.weep.service.CarouselService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carousels")
public class CarouselController {

    private static final Logger logger = LoggerFactory.getLogger(CarouselController.class);

    @Autowired
    private CarouselService carouselService;

    @GetMapping
    public Result<List<Carousel>> listActive() {
        List<Carousel> carousels = carouselService.findActive();
        return Result.success(carousels);
    }
}
