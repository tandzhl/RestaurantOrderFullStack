import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { useState } from "react";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import HomePage from "./pages/HomePage";
import RestaurantPage from "./pages/RestaurantPage";
import CategoryPage from "./pages/CategoryPage";
import CartPage from "./pages/CartPage";
import RestaurantDetailPage from "./pages/RestaurantDetailPage";
import Header from "./pages/Header";
import FoodDetailPage from "./pages/FoodDetailPage";
import RegisterRestaurantPage from "./pages/RegisterRestaurantPage";
import PendingShops from "./pages/PendingShop";
import RestaurantDashboard from "./pages/RestaurantDashboard";
import MenuPage from "./pages/MenuPage";
import FoodManagerPage from "./pages/FoodManagerPage";
import OrderPage from "./pages/OrderPage";
import RestaurantOrdersPage from "./pages/RestaurantOrdersPage";
import PaymentResultPage from "./pages/PaymentResultPage";
import RestaurantEditPage from "./pages/RegisterEditPage";
import RestaurantRevenueReport from "./pages/RestaurantRevenueReport";
import RestaurantMessages from "./pages/RestaurantMessages";
import ProfilePage from "./pages/ProfilePage";
function App() {
  const [slugMap, setSlugMap] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");

  return (
    <BrowserRouter>
      {/* Header luôn hiện */}
      <Header onSlugMapReady={setSlugMap} onSearch={setSearchTerm} />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<HomePage searchTerm={searchTerm} />} />
        <Route path="/restaurants" element={<RestaurantPage />} />
        <Route path="/category/:slug" element={<CategoryPage slugMap={slugMap} />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/restaurants/:slug" element={<RestaurantDetailPage />} />
        <Route path="/food/:slug" element={<FoodDetailPage />} />
        <Route path="/register-shop" element={<RegisterRestaurantPage />} />
        <Route path="/admin/shops" element={<PendingShops />} />
        <Route path="/owner/restaurant" element={<RestaurantDashboard />} />
        <Route path="/owner/restaurant/:id/menu" element={<MenuPage />} />
        <Route path="/owner/restaurant/:id/food" element={<FoodManagerPage />} />
        <Route path="/owner/restaurant/:id/orders" element={<RestaurantOrdersPage />} />
        <Route path="/owner/restaurant/:id/infor" element={<RestaurantEditPage />} />
        <Route path="/owner/restaurant/:id/revenue" element={<RestaurantRevenueReport />} />
        <Route path="/owner/restaurant/:id/messages" element={<RestaurantMessages />} />
        <Route path="/orders" element={<OrderPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/payment-result" element={<PaymentResultPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
