import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { 
  Card, Row, Col, Spin, message, Tag, Rate, List, Button, Input, Modal, Upload 
} from "antd";
import { 
  EnvironmentOutlined, ClockCircleOutlined, EditOutlined, DeleteOutlined, UploadOutlined, MessageOutlined
} from "@ant-design/icons";
import api from "../api/axios";
import AddReviewForm from "../pages/AddReviewForm"
import dayjs from "dayjs";
import customParseFormat from "dayjs/plugin/customParseFormat";
import ChatBox from "../pages/ChatBox";
dayjs.extend(customParseFormat);

const { TextArea } = Input;

const RestaurantDetailPage = () => {
  const { slug } = useParams();
  const id = Number(slug?.split("-").pop());

  const [restaurant, setRestaurant] = useState(null);
  const [loading, setLoading] = useState(true);
  const [menus, setMenus] = useState([]);
  const [foods, setFoods] = useState({});
  const [reviews, setReviews] = useState([]);
  const [editingReview, setEditingReview] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [showChatBox, setShowChatBox] = useState(false);

  // Lấy thông tin user từ token
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const res = await api.get("/users/me");
        setCurrentUser(res.data);
      } catch (err) {
        console.error("Không lấy được user:", err);
        setCurrentUser(null);
      }
    };
    fetchUser();
  }, []);

  useEffect(() => {
    const fetchRestaurantData = async () => {
      try {
        const res = await api.get(`/restaurants/${id}`);
        console.log(res.data)
        setRestaurant(res.data);

        const menuRes = await api.get(`/restaurants/${id}/menus`);
        const menusData = Array.isArray(menuRes.data)
          ? menuRes.data
          : menuRes.data?.menus ?? menuRes.data?.data ?? [];
        setMenus(menusData);

        const foodPromises = menusData.map((menu) =>
          api
            .get(`/food-items/menu/${menu.id}`)
            .then((foodRes) => {
              const d = foodRes.data;
              const items = Array.isArray(d) ? d : d?.items ?? d?.content ?? [];
              return { id: menu.id, items };
            })
            .catch(() => ({ id: menu.id, items: [] }))
        );
        const foodsArray = await Promise.all(foodPromises);
        const foodsMap = {};
        foodsArray.forEach((f) => (foodsMap[f.id] = f.items));
        setFoods(foodsMap);

        // fetch reviews
        const reviewRes = await api.get(`/restaurant-reviews/${id}`);
        setReviews(reviewRes.data);
      } catch (err) {
        console.error(err);
        message.error("Không thể tải dữ liệu!");
      } finally {
        setLoading(false);
      }
    };

    if (!Number.isNaN(id)) {
      fetchRestaurantData();
    } else {
      setLoading(false);
      message.error("ID nhà hàng không hợp lệ!");
    }
  }, [id]);

  useEffect(() => {
    if (!currentUser) return;

    const fetchFollowStatus = async () => {
      try {
        const res = await api.get(`/follow/${id}/is-following`);
        setIsFollowing(res.data);
      } catch (err) {
        console.error("Lỗi khi lấy trạng thái follow:", err);
        setIsFollowing(false); // fallback nếu lỗi
      }
    };

    fetchFollowStatus();
  }, [id, currentUser]);

  const handleEditReview = async (reviewId) => {
    if (!editingReview) return;
    try {
      const res = await api.put(`/restaurant-reviews/${reviewId}`, {
        comment: editingReview.comment,
        rating: editingReview.rating,
      });
      setReviews(
        reviews.map((r) => (r.id === reviewId ? res.data : r))
      );
      setEditingReview(null);
      message.success("Cập nhật bình luận thành công!");
    } catch (err) {
      console.error(err);
      message.error("Cập nhật thất bại!");
    }
  };

  const handleDeleteReview = async (reviewId) => {
    try {
      await api.delete(`/restaurant-reviews/${reviewId}`);
      setReviews(reviews.filter((r) => r.id !== reviewId));
      message.success("Xóa bình luận thành công!");
    } catch (err) {
      console.error(err);
      message.error("Xóa bình luận thất bại!");
    }
  };

  const addToCart = (item) => {
    const cart = JSON.parse(localStorage.getItem("cart")) || [];
    const existing = cart.find((c) => c.id === item.id);
    if (existing) {
      existing.quantity += 1;
    } else {
      cart.push({
        id: item.id,
        name: item.name,
        price: item.price,
        imageUrl: item.imageUrl,
        restaurantId: item.restaurantId,
        quantity: 1,
      });
    }
    localStorage.setItem("cart", JSON.stringify(cart));
    message.success(`${item.name} đã được thêm vào giỏ hàng!`);
  };

  const toggleFollow = async () => {
    try {
      if (!currentUser) {
        message.warning("Bạn cần đăng nhập để theo dõi cửa hàng");
        return;
      }

      if (isFollowing) {
        await api.delete(`/follow/${id}`);
        setIsFollowing(false);
        message.success("Đã hủy theo dõi nhà hàng");
      } else {
        await api.post(`/follow/${id}`);
        setIsFollowing(true);
        message.success("Đã theo dõi nhà hàng");
      }
    } catch (err) {
      console.error("Lỗi khi follow/unfollow:", err);
      message.error("Thao tác thất bại");
    }
  };


  if (loading)
    return (
      <div style={{ textAlign: "center", marginTop: 50 }}>
        <Spin size="large" />
      </div>
    );

  if (!restaurant) return <p>Không tìm thấy nhà hàng.</p>;

  return (
    <div style={{ display: "flex", justifyContent: "center" }}>
      <div style={{ width: "80%", padding: 24 }}>
        <Row gutter={24}>
          <Col xs={24} md={10}>
            <Card
              cover={
                <img
                  alt={restaurant.name}
                  src={restaurant.imageUrl}
                  style={{ width: "100%", height: 300, objectFit: "cover" }}
                />
              }
            />
          </Col>

          <Col xs={24} md={14}>
            <h2 style={{ fontSize: 22, marginBottom: 8 }}>{restaurant.name}</h2>
            <p>
              <EnvironmentOutlined /> {restaurant.address}
            </p>
            <div
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "left", // 👈 căn giữa ngang
                gap: 8,
                margin: "12px 0", // thêm khoảng cách cho đẹp
              }}
            >
              <Rate
                disabled
                value={restaurant.averageRating || 0}
                allowHalf
                style={{ color: "#fadb14", fontSize: 18 }}
              />
              <span>
                {restaurant.averageRating
                  ? `${restaurant.averageRating.toFixed(1)} (${restaurant.totalReviews || 0} đánh giá)`
                  : "Chưa có đánh giá"}
              </span>
            </div>
            <p>
              <ClockCircleOutlined /> Giờ hoạt động:{" "}
              <b>
                {restaurant.openingTime
                  ? dayjs(restaurant.openingTime, "HH:mm:ss").format("HH:mm")
                  : "?"}
                {" - "}
                {restaurant.closingTime
                  ? dayjs(restaurant.closingTime, "HH:mm:ss").format("HH:mm")
                  : "?"}
              </b>
            </p>

            <p>
              Trạng thái:{" "}
              {(() => {
                if (restaurant.openingTime && restaurant.closingTime) {
                  const now = dayjs();
                  const today = now.format("YYYY-MM-DD");

                  const open = dayjs(
                    `${today} ${restaurant.openingTime}`,
                    "YYYY-MM-DD HH:mm:ss"
                  );
                  let close = dayjs(
                    `${today} ${restaurant.closingTime}`,
                    "YYYY-MM-DD HH:mm:ss"
                  );

                  // Nếu giờ đóng < giờ mở => đóng qua ngày hôm sau
                  if (close.isBefore(open)) {
                    close = close.add(1, "day");
                  }

                  if (now.isAfter(open) && now.isBefore(close)) {
                    return <Tag color="green">Đang mở cửa</Tag>;
                  }
                  return <Tag color="red">Đang đóng</Tag>;
                }

                return <Tag color="default">Không rõ</Tag>;
              })()}
              {/* Nút follow/unfollow */}
              <Button
                type={isFollowing ? "default" : "primary"}
                onClick={toggleFollow}
                style={{ marginBottom: 12 }}
              >
                {isFollowing ? "Hủy theo dõi" : "Theo dõi"}
              </Button>

              {/* 👉 Nút chat */}
              <Button
                type="dashed"
                icon={<MessageOutlined />}
                onClick={() => setShowChatBox(!showChatBox)}
                style={{ marginLeft: 12 }}
              >
                Liên hệ với cửa hàng
              </Button>
            </p>
            {/* 👉 Chat box */}
            {showChatBox && (
              <div style={{ marginTop: 20 }}>
                <ChatBox restaurant={restaurant} />
              </div>
            )}
          </Col>

          
          </Row>

        <Row gutter={24} style={{ marginTop: 40 }}>
          <Col xs={24} md={14}>
            {/* MENU & FOODS */}
            <h2 style={{ fontSize: 20, marginBottom: 16 }}>Thực đơn</h2>
            {menus.length === 0 && <p>Không có thực đơn.</p>}
            {menus.map((menu) => {
              const items = Array.isArray(foods[menu.id]) ? foods[menu.id] : [];
              return (
                <div key={menu.id} style={{ marginBottom: 32 }}>
                  <h3 style={{ fontSize: 18, marginBottom: 12 }}>{menu.name}</h3>
                  <List
                    itemLayout="horizontal"
                    dataSource={items}
                    renderItem={(item) => (
                      <List.Item
                        key={item.id ?? item.name}
                        style={{
                          borderBottom: "1px solid #f0f0f0",
                          padding: "12px 0",
                          display: "flex",
                          alignItems: "center",
                        }}
                        actions={[
                          <div
                            key="price"
                            style={{ display: "flex", alignItems: "center", gap: 12 }}
                          >
                            <span
                              style={{
                                fontWeight: "bold",
                                color: "#1890ff",
                                fontSize: 16,
                              }}
                            >
                              {item.price ? Number(item.price).toLocaleString() : ""} đ
                            </span>
                            <Button
                              type="primary"
                              shape="circle"
                              style={{
                                background: "#ff4d4f",
                                border: "none",
                                fontSize: 18,
                                width: 32,
                                height: 32,
                              }}
                              onClick={() => addToCart(item)}
                            >
                              +
                            </Button>
                          </div>,
                        ]}
                      >
                        <img
                          src={item.imageUrl ?? item.image ?? ""}
                          alt={item.name}
                          style={{
                            width: 80,
                            height: 80,
                            objectFit: "cover",
                            borderRadius: 6,
                            marginRight: 16,
                          }}
                        />
                        <List.Item.Meta
                          title={<b>{item.name}</b>}
                          description={item.description}
                        />
                      </List.Item>
                    )}
                  />
                </div>
              );
            })}
          </Col>

          <Col xs={24} md={10}>
            {/* REVIEWS */}
            <h2 style={{ fontSize: 20, marginBottom: 16 }}>Đánh giá</h2>
            <List
              itemLayout="vertical"
              dataSource={reviews}
              renderItem={(review) => (
                <List.Item
                  key={review.id}
                  extra={
                    review.userId === currentUser?.id && (
                      <div style={{ display: "flex", gap: 8 }}>
                        <Button
                          icon={<EditOutlined />}
                          onClick={() => setEditingReview(review)}
                        />
                        <Button
                          icon={<DeleteOutlined />}
                          danger
                          onClick={() => handleDeleteReview(review.id)}
                        />
                      </div>
                    )
                  }
                >
                  <List.Item.Meta
                    avatar={
                      <div
                        style={{
                          width: 40,
                          height: 40,
                          borderRadius: "50%",
                          background: "#ccc",
                          color: "#fff",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          fontWeight: "bold",
                          fontSize: 16,
                        }}
                      >
                        {review.userFullname?.[0]?.toUpperCase()}
                      </div>
                    }
                    title={
                      <div style={{ display: "flex", flexDirection: "column" }}>
                        <span style={{ fontWeight: "bold" }}>{review.userFullname}</span>
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                          <Rate disabled value={review.rating} />
                          <span style={{ color: "#888", fontSize: 12 }}>
                            {new Date(review.createAt).toLocaleString("vi-VN")}
                          </span>
                        </div>
                      </div>
                    }
                    description={
                      <div>
                        <p style={{ marginTop: 8 }}>{review.comment}</p>

                        {/* Hiển thị ảnh bình luận */}
                        {review.imgUrl && (
                          <div style={{ marginTop: 8 }}>
                            <img
                              src={review.imgUrl}
                              alt="review-img"
                              style={{
                                maxWidth: 200,
                                borderRadius: 8,
                                display: "block",
                              }}
                            />
                          </div>
                        )}
                      </div>
                    }
                  />
                </List.Item>
              )}
            />

            <Modal
              open={!!editingReview}
              title="Chỉnh sửa bình luận"
              onCancel={() => setEditingReview(null)}
              onOk={() => handleEditReview(editingReview.id)}
            >
              {/* Rating */}
              <div style={{ marginBottom: 16 }}>
                <span style={{ marginRight: 8 }}>Đánh giá:</span>
                <Rate
                  value={editingReview?.rating}
                  onChange={(value) =>
                    setEditingReview({ ...editingReview, rating: value })
                  }
                />
              </div>

              {/* Comment */}
              <TextArea
                rows={3}
                value={editingReview?.comment}
                onChange={(e) =>
                  setEditingReview({ ...editingReview, comment: e.target.value })
                }
              />

              {/* Upload ảnh */}
              <Upload
                beforeUpload={(file) => {
                  setEditingReview({ ...editingReview, newImageFile: file });
                  return false;
                }}
                onRemove={() => setEditingReview({ ...editingReview, newImageFile: null })}
                maxCount={1}
                style={{ marginTop: 10 }}
              >
                <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
              </Upload>

              {/* ✅ Preview ảnh mới chọn */}
              {editingReview?.newImageFile && (
                <img
                  src={URL.createObjectURL(editingReview.newImageFile)}
                  alt="preview"
                  style={{ maxWidth: 200, marginTop: 8, borderRadius: 6 }}
                />
              )}

              {/* ✅ Preview ảnh cũ nếu chưa chọn ảnh mới */}
              {!editingReview?.newImageFile && editingReview?.imgUrl && (
                <img
                  src={editingReview.imgUrl}
                  alt="old-review"
                  style={{ maxWidth: 200, marginTop: 8, borderRadius: 6 }}
                />
              )}
            </Modal>

            <AddReviewForm
              restaurantId={id}
              currentUser={currentUser}
              onReviewAdded={(newReview) => setReviews([newReview, ...reviews])}
            />
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default RestaurantDetailPage;
