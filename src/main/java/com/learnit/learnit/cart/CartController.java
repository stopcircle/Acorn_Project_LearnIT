package com.learnit.learnit.cart;

import com.learnit.learnit.payment.common.LoginRequiredException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        if(userId == null) throw new LoginRequiredException("로그인이 필요한 서비스입니다.");

        List<CartItem> items = cartService.getCartItems(userId);
        int totalPrice = cartService.calcTotal(items);

        model.addAttribute("items", items);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("discountPrice", 0);
        model.addAttribute("finalPrice", totalPrice);

        return "cart/cart";
    }

    // ✅ X 버튼 삭제
    @PostMapping("/cart/delete")
    public String deleteItem(@RequestParam("cartId") Long cartId, HttpSession session) {
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");
        cartService.removeItem(userId, cartId);
        return "redirect:/cart";
    }

    // ✅ 전체삭제 추가
    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session) {
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");
        cartService.clearCart(userId);
        return "redirect:/cart";
    }
}
