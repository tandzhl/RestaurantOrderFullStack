package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.MenuRequest;
import com.pdnt.restaurant.dto.response.MenuResponse;
import com.pdnt.restaurant.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    @PostMapping
    public MenuResponse createMenu(@RequestBody MenuRequest request) {
        return menuService.createMenu(request);
    }
    @PutMapping("/{id}")
    public MenuResponse updateMenu(@PathVariable Long id,
                                   @RequestBody MenuRequest request) {
        return menuService.updateMenu(id, request);
    }
    @DeleteMapping("/{id}")
    public void deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
    }
}
