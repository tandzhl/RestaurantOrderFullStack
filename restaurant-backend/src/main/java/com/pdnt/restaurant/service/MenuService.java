package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.request.MenuRequest;
import com.pdnt.restaurant.dto.response.MenuResponse;
import com.pdnt.restaurant.entity.Menu;
import com.pdnt.restaurant.entity.Restaurant;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.exceptions.ErrorCode;
import com.pdnt.restaurant.exceptions.WebException;
import com.pdnt.restaurant.mapper.MenuMapper;
import com.pdnt.restaurant.repository.MenuRepository;
import com.pdnt.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final RestaurantRepository restaurantRepository;

    /**
     * Lấy current user từ SecurityContextHolder
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new WebException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new WebException(ErrorCode.UNAUTHORIZED);
    }

    /**
     * Kiểm tra quyền sở hữu nhà hàng của current user
     */
    private void checkOwnerPermission(Restaurant restaurant) {
        User currentUser = getCurrentUser();
        if (!restaurant.getOwner().getId().equals(currentUser.getId())) {
            throw new WebException(ErrorCode.FORBIDDEN);
        }
    }

    public List<MenuResponse> getMenusByRestaurant(Long restaurantId) {
        return menuMapper.toMenuResponseList(menuRepository.findByRestaurantId(restaurantId));
    }

    public MenuResponse createMenu(MenuRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        checkOwnerPermission(restaurant);

        Menu menu = Menu.builder()
                .name(request.getName())
                .restaurant(restaurant)
                .build();

        return menuMapper.toMenuResponse(menuRepository.save(menu));
    }

    public MenuResponse updateMenu(Long id, MenuRequest request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        checkOwnerPermission(menu.getRestaurant());

        menu.setName(request.getName());
        return menuMapper.toMenuResponse(menuRepository.save(menu));
    }

    public void deleteMenu(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        checkOwnerPermission(menu.getRestaurant());

        menuRepository.delete(menu);
    }
}
