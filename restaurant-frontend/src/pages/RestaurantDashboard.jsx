import { useEffect, useState } from "react";
import { Card, Dropdown, Menu, Button } from "antd";
import { MoreOutlined } from "@ant-design/icons";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";

function RestaurantDashboard() {
  const [restaurants, setRestaurants] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    api
      .get("/restaurants/my-restaurant")
      .then((res) => setRestaurants(res.data))
      .catch((err) => console.error("Error fetching restaurants:", err));
  }, []);

  const menu = (id) => (
    <Menu>
      <Menu.Item key="infor" onClick={() => navigate(`/owner/restaurant/${id}/infor`)}>
        Thay đổi thông tin nhà hàng
      </Menu.Item>
      <Menu.Item key="menu" onClick={() => navigate(`/owner/restaurant/${id}/menu`)}>
        Quản lý thực đơn
      </Menu.Item>
      <Menu.Item key="dishes" onClick={() => navigate(`/owner/restaurant/${id}/food`)}>
        Quản lý món ăn
      </Menu.Item>
      <Menu.Item key="revenue" onClick={() => navigate(`/owner/restaurant/${id}/revenue`)}>
        Quản lý doanh thu
      </Menu.Item>
      <Menu.Item key="orders" onClick={() => navigate(`/owner/restaurant/${id}/orders`)}>
        Đơn hàng
      </Menu.Item>
    </Menu>
  );

  return (
    <div style={{ padding: 24, background: "#f5f5f5", minHeight: "100vh" }}>
      <h1 className="text-3xl font-bold mb-8">Nhà hàng của tôi</h1>
      <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
        {restaurants.map((restaurant) => (
          <Card
            key={restaurant.id}
            hoverable
            style={{ borderRadius: 12, width: "70%", alignSelf: "center" }}
            bodyStyle={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}
          >
            {/* Bên trái: Hình ảnh */}
            <img
              src={restaurant.imageUrl}
              alt={restaurant.name}
              style={{ width: 120, height: 120, objectFit: "cover", borderRadius: 8, marginRight: 16 }}
            />

            {/* Giữa: Thông tin */}
            <div style={{ flex: 1 }}>
              <h2 style={{ margin: 0, fontSize: "18px", fontWeight: "600" }}>{restaurant.name}</h2>
              <p style={{ margin: 0 }}>{restaurant.address}</p>
              <p style={{ margin: 0 }}>
                Giờ mở cửa: {restaurant.openingTime} - {restaurant.closingTime}
              </p>
              <p style={{ margin: 0 }}>⭐ {restaurant.averageRating.toFixed(1)} ({restaurant.totalReviews} đánh giá)</p>
            </div>

            {/* Bên phải: nút ba chấm */}
            <Dropdown overlay={menu(restaurant.id)} trigger={["click"]}>
              <Button type="text" icon={<MoreOutlined />} />
            </Dropdown>
          </Card>
        ))}
      </div>
    </div>
  );
}

export default RestaurantDashboard;