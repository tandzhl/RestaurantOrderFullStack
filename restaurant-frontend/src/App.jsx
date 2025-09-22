  import { BrowserRouter, Routes, Route } from "react-router-dom";
  import { useState } from "react";
  import { AuthProvider } from "../src/context/AuthContext";
  import ProtectedRoute from "../src/context/ProtectedRoute";

  // Import các page
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
      <AuthProvider>
        <BrowserRouter>
          <Header onSlugMapReady={setSlugMap} onSearch={setSearchTerm} />
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/" element={<HomePage searchTerm={searchTerm} />} />
            <Route path="/restaurants" element={<RestaurantPage />} />
            <Route path="/category/:slug" element={<CategoryPage slugMap={slugMap} />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/restaurants/:slug" element={<RestaurantDetailPage />} />
            <Route path="/food/:slug" element={<FoodDetailPage />} />
            <Route path="/payment-result" element={<PaymentResultPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/register-shop" element={<RegisterRestaurantPage />} />
            <Route path="/orders" element={<OrderPage />} />
            {/* Restaurant_owner routes */}

            <Route
              path="/owner/restaurant"
              element={
                  <RestaurantDashboard />
              }
            />
            <Route
              path="/owner/restaurant/:id/menu"
              element={
                  <MenuPage />
              }
            />
            <Route
              path="/owner/restaurant/:id/food"
              element={
                <FoodManagerPage />
              }
            />
            <Route
              path="/owner/restaurant/:id/orders"
              element={
                <RestaurantOrdersPage />
              }
            />
            <Route
              path="/owner/restaurant/:id/infor"
              element={
                <RestaurantEditPage />
              }
            />
            <Route
              path="/owner/restaurant/:id/revenue"
              element={<RestaurantRevenueReport />
              }
            />
            <Route
              path="/owner/restaurant/:id/messages"
              element={
                  <RestaurantMessages />
              }
            />

            {/* Admin routes */}
            <Route
              path="/admin/shops"
              element={
                  <PendingShops />
              }
            />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    );
  }

  export default App;
