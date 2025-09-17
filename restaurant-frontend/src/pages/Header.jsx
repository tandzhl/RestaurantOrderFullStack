import React, { useEffect, useState } from "react";
import { Menu, Input, Button, Badge, Avatar, Dropdown, message, } from "antd";
import { SearchOutlined, ShoppingCartOutlined, UserOutlined, AppstoreOutlined, ToolOutlined, ShopOutlined, PlusOutlined, BellOutlined, } from "@ant-design/icons";
import api from "../api/axios";
import "../css/Header.css";
import { toUniqueSlug } from "../utils/slug";
import { useNavigate } from "react-router-dom";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/vi";

dayjs.extend(relativeTime);
dayjs.locale("vi");

const { Search } = Input;

function Header({ onSlugMapReady, onSearch }) {
  const [categories, setCategories] = useState([]);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [cartCount, setCartCount] = useState(0);
  const [userRole, setUserRole] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const navigate = useNavigate();

  // --- Fetch user info + notifications ---
  useEffect(() => {
    const updateLoginState = () => {
      const token = localStorage.getItem("token");
      setIsLoggedIn(!!token);
      if (token) {
        fetchUserInfo();
        fetchNotifications();
      } else {
        setUserRole(null);
        setNotifications([]);
      }
    };

    updateLoginState();

    window.addEventListener("storage", updateLoginState);
    return () => window.removeEventListener("storage", updateLoginState);
  }, []);

  const fetchUserInfo = async () => {
    try {
      const res = await api.get("http://localhost:8080/users/me");
      setUserRole(res.data.role);
    } catch (err) {
      console.error("Lỗi lấy thông tin user:", err);
    }
  };

  const fetchNotifications = async () => {
    try {
      const res = await api.get("http://localhost:8080/notifications/me");
      setNotifications(res.data || []);
    } catch (err) {
      console.error("Lỗi lấy thông báo:", err);
    }
  };

  // --- Fetch categories ---
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await api.get("/categories");
        const dataWithSlug = res.data.map((cat) => ({
          ...cat,
          slug: toUniqueSlug(cat.name),
        }));
        setCategories(dataWithSlug);

        const map = {};
        dataWithSlug.forEach((cat) => (map[cat.slug] = cat.id));
        if (onSlugMapReady) onSlugMapReady(map);
      } catch (error) {
        console.error("Lỗi khi fetch categories:", error);
      }
    };
    fetchCategories();
  }, [onSlugMapReady]);

  // --- Cart count ---
  useEffect(() => {
    const updateCartCount = () => {
      const cart = JSON.parse(localStorage.getItem("cart")) || [];
      const total = cart.reduce((sum, item) => sum + item.quantity, 0);
      setCartCount(total);
    };
    updateCartCount();
    window.addEventListener("storage", updateCartCount);
    return () => window.removeEventListener("storage", updateCartCount);
  }, []);

  const handleNotificationClick = async (n) => {
    try {
      // 1. Đánh dấu đã đọc
      await api.post(`http://localhost:8080/notifications/${n.id}/read`);
      fetchNotifications();

      if (n.type === "NEW_FOOD" && n.foodId) {
        const res = await api.get(`http://localhost:8080/food-items/${n.foodId}`);
        const food = res.data;
        const slug = `${toUniqueSlug(food.name)}-${food.id}`;
        navigate(`/food/${slug}`);
      } else if (n.type === "SOMETHING_ELSE" && n.restaurantId) {
        // ví dụ sau này có noti kiểu mở nhà hàng mới
        navigate(`/restaurants/${n.restaurantId}`);
      }

    } catch (err) {
      console.error("Lỗi khi xử lý notification:", err);
      message.error("Không thể mở thông báo");
    }
  };

  // --- Logout ---
  const handleLogout = async () => {
    try {
      await api.post("/auth/logout");
    } catch (e) {
      console.error("Logout error:", e);
    }
    localStorage.removeItem("token");
    setIsLoggedIn(false);
    setUserRole(null);
    message.success("Đăng xuất thành công!");
    window.location.href = "/";
  };

  // --- Menu items ---
  const menuItems = [
    { key: "home", label: <a href="/">Home</a> },
    { key: "restaurants", label: <a href="/restaurants">Nhà hàng</a> },
    {
      key: "categories",
      icon: <AppstoreOutlined />,
      label: "Danh mục món ăn",
      children: categories.map((cat) => ({
        key: cat.id,
        label: <a href={`/category/${cat.slug}`}>{cat.name}</a>,
      })),
    },
  ];

  // --- User menu ---
  const userMenuItems = [
    { key: "profile", label: <a href="/profile">Quản lý hồ sơ</a> },
    { key: "orders", label: <a href="/orders">Đơn hàng của tôi</a> },
  ];

  // --- Role specific ---
  let roleSpecificItems = [];
  if (userRole === "ADMIN") {
    roleSpecificItems.push({
      key: "review-shops",
      label: <a href="/admin/shops">Duyệt các cửa hàng</a>,
    });
  } else if (userRole === "RESTAURANT_OWNER") {
    roleSpecificItems.push({
      key: "manage-restaurant",
      label: <a href="/owner/restaurant">Quản lý các nhà hàng của bạn</a>,
    });
  }

  const dropdownItems = [
    ...userMenuItems,
    ...roleSpecificItems,
    { type: "divider" },
    { key: "logout", danger: true, label: "Đăng xuất" },
  ];

  // --- Avatar icon ---
  const getAvatarIcon = () => {
    if (userRole === "ADMIN") return <ToolOutlined />;
    if (userRole === "RESTAURANT_OWNER") return <ShopOutlined />;
    return <UserOutlined />;
  };

  // --- Hàm format thời gian ---
  const formatNotificationTime = (createdAt) => {
    if (!createdAt) return "30+ ngày trước";

    const now = dayjs();
    const created = dayjs(createdAt);
    const diffDays = now.diff(created, "day");

    if (diffDays > 30) {
      return "hơn 1 tháng trước";
    }

    return created.fromNow(); // "3 phút trước", "2 giờ trước", "5 ngày trước"
  };

  const sortedNotifications = [...notifications].sort((a, b) => {
    if (!a.createdAt) return 1; // null => xuống dưới
    if (!b.createdAt) return -1;
    return dayjs(b.createdAt).valueOf() - dayjs(a.createdAt).valueOf();
  });

  // --- Notifications ---
  const unreadCount = notifications.filter((n) => !n.read).length;
  const badgeCount = unreadCount > 9 ? "9+" : unreadCount;

  const notificationList = (
    <div
      className="notification-list"
      style={{
        maxHeight: "50vh", // nửa màn hình
        overflowY: "auto",
        width: 320,
        background: "#fff",
        border: "1px solid #f0f0f0",
        borderRadius: 6,
        boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
      }}
    >
      {notifications.length === 0 && (
        <div style={{ padding: 12, textAlign: "center", color: "#888" }}>
          Không có thông báo nào
        </div>
      )}

      {sortedNotifications.map((n) => (
        <div
          key={n.id}
          onClick={() => handleNotificationClick(n)}
          style={{
            padding: "8px 12px",
            backgroundColor: !n.read ? "#e6f7ff" : "white",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            borderBottom: "1px solid #f0f0f0",
          }}
        >
          <div>
            <strong>{n.title}</strong>
            <div style={{ fontSize: 12, color: "#666" }}>{n.message}</div>
            <div style={{ fontSize: 11, color: "#999" }}>
              {formatNotificationTime(n.createdAt)}
            </div>
          </div>
          {!n.read && (
            <span
              style={{
                width: 8,
                height: 8,
                borderRadius: "50%",
                backgroundColor: "green",
                marginLeft: 8,
              }}
            />
          )}
        </div>
      ))}
    </div>
  );


  // --- Render ---
  return (
    <header className="header-container">
      <nav className="nav-links">
        <Menu mode="horizontal" items={menuItems} />
      </nav>
      <div className="right-actions">
        {/* Chuông thông báo */}
        {isLoggedIn && (
          <Dropdown
            dropdownRender={() => notificationList}
            placement="bottomRight"
            trigger={["click"]}
            onOpenChange={(open) => open && fetchNotifications()}
          >
            <Badge
              count={badgeCount}
              overflowCount={9}
              offset={[-2, 4]}
              style={{ backgroundColor: "#f5222d" }}
            >
              <BellOutlined
                style={{
                  fontSize: 22,
                  marginRight: 16,
                  cursor: "pointer",
                }}
              />
            </Badge>
          </Dropdown>
        )}

        <Search
          placeholder="Tìm món ăn..."
          allowClear
          enterButton={<SearchOutlined />}
          style={{ width: 200 }}
          onSearch={(value) => onSearch && onSearch(value.trim())}
        />

        {/* Giỏ hàng */}
        <a href="/cart" className="cart-icon">
          <Badge count={cartCount} offset={[0, 5]}>
            <ShoppingCartOutlined style={{ fontSize: 22 }} />
          </Badge>
        </a>

        {/* Đăng ký cửa hàng chỉ cho USER */}
        {isLoggedIn && userRole === "USER" && (
          <Button
            type="dashed"
            icon={<PlusOutlined />}
            href="/register-shop"
            style={{ marginRight: 12 }}
          >
            Đăng ký cửa hàng
          </Button>
        )}

        {/* Avatar */}
        {isLoggedIn ? (
          <Dropdown
            menu={{
              items: dropdownItems,
              onClick: ({ key }) => key === "logout" && handleLogout(),
            }}
            placement="bottomRight"
          >
            <Avatar icon={getAvatarIcon()} style={{ cursor: "pointer" }} />
          </Dropdown>
        ) : (
          <Button type="primary" href="/login">
            Đăng nhập
          </Button>
        )}
      </div>
    </header>
  );
}

export default Header;
