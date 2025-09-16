import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  Spin,
  Card,
  Button,
  message,
  Row,
  Col,
  Rate,
  Input,
  Upload,
  List,
  Popconfirm,
} from "antd";
import { UploadOutlined, DeleteOutlined, EditOutlined } from "@ant-design/icons";
import api from "../api/axios";

const { TextArea } = Input;

const FoodDetailPage = () => {
  const { slug } = useParams();
  const [food, setFood] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newComment, setNewComment] = useState("");
  const [newRating, setNewRating] = useState(5);
  const [imageFile, setImageFile] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [editingReview, setEditingReview] = useState(null);
  const foodId = slug.split("-").pop();

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        // Lấy chi tiết món ăn
        const resFood = await api.get(`/food-items/${foodId}`);
        setFood(resFood.data);

        // Lấy reviews
        const resReviews = await api.get(`/food-reviews/${foodId}`);
        setReviews(resReviews.data || []);

        // Lấy user hiện tại
        const resUser = await api.get("/users/me");
        setCurrentUser(resUser.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [foodId]);

  const addToCart = () => {
    if (!food) return;
    const cart = JSON.parse(localStorage.getItem("cart")) || [];
    const existing = cart.find((c) => c.id === food.id);
    if (existing) {
      existing.quantity += 1;
    } else {
      cart.push({ ...food, quantity: 1 });
    }
    localStorage.setItem("cart", JSON.stringify(cart));
    message.success(`${food.name} đã được thêm vào giỏ hàng!`);
  };

  const handleAddReview = async () => {
    if (!currentUser) {
        message.warning("Bạn cần đăng nhập để bình luận!");
        return;
    }

    try {
        const formData = new FormData();
        formData.append("rating", newRating);
        formData.append("comment", newComment);
        formData.append("foodItemId", foodId); // bắt buộc gửi foodItemId
        if (imageFile) formData.append("image", imageFile); // tên field phải đúng với backend

        // Debug log
        for (let pair of formData.entries()) {
        console.log(pair[0], pair[1]);
        }

        await api.post(`/food-reviews`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
        });

        message.success("Đã thêm đánh giá!");
        setNewComment("");
        setImageFile(null);

        const resReviews = await api.get(`/food-reviews/${foodId}`);
        setReviews(resReviews.data || []);
    } catch (err) {
        console.error(err.response?.data || err);
        message.error("Không thể thêm đánh giá!");
    }
  };

  const startEditReview = (review) => {
    setEditingReview({
        ...review,
        newRating: review.rating,
        newComment: review.comment,
        newImageFile: null,
    });
  };  

  const handleEditReview = async () => {
    if (!editingReview) return;

    try {
        const formData = new FormData();
        formData.append("rating", editingReview.newRating);
        formData.append("comment", editingReview.newComment);
        if (editingReview.newImageFile) {
        formData.append("image", editingReview.newImageFile);
        }

        await api.put(`/food-reviews/${editingReview.id}`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
        });

        message.success("Đã cập nhật bình luận!");

        // Cập nhật local state
        setReviews((prev) =>
        prev.map((r) =>
            r.id === editingReview.id
            ? { ...r, rating: editingReview.newRating, comment: editingReview.newComment, imgUrl: editingReview.newImageFile ? URL.createObjectURL(editingReview.newImageFile) : r.imgUrl }
            : r
        )
        );

        setEditingReview(null); // đóng form sửa
    } catch (err) {
        console.error(err.response?.data || err);
        message.error("Không thể cập nhật bình luận!");
    }
  };

  const handleDeleteReview = async (reviewId) => {
    try {
      await api.delete(`/food-reviews/${reviewId}`);
      message.success("Xóa bình luận thành công!");
      setReviews(reviews.filter((r) => r.id !== reviewId));
    } catch (err) {
      console.error(err);
      message.error("Không thể xóa bình luận!");
    }
  };

  if (loading)
    return (
      <Spin
        size="large"
        style={{ marginTop: 50, display: "block", textAlign: "center" }}
      />
    );

  if (!food)
    return (
      <p style={{ textAlign: "center", marginTop: 50 }}>
        Không tìm thấy món ăn
      </p>
    );

  return (
    <div style={{ padding: 24, maxWidth: 900, margin: "auto" }}>
      <Card style={{ background: "#fff", borderRadius: 8, padding: 16 }}>
        <Row gutter={24} align="middle">
          {/* Cột trái: ảnh */}
          <Col xs={24} md={12}>
            <img
              alt={food.name}
              src={food.imageUrl}
              style={{
                width: "100%",
                maxHeight: 400,
                objectFit: "cover",
                borderRadius: 8,
              }}
            />
          </Col>

          {/* Cột phải: thông tin */}
          <Col xs={24} md={12}>
            <h2>{food.name}</h2>
            <p>{food.description}</p>
            <p style={{ fontWeight: "bold", fontSize: 18 }}>
              {food.price.toLocaleString()} đ
            </p>
            <Rate disabled value={food.averageRating} />
            <div style={{ marginTop: 20 }}>
              <Button type="primary" onClick={addToCart}>
                Thêm vào giỏ hàng
              </Button>
            </div>
          </Col>
        </Row>
      </Card>

      {/* Review Section */}
      <Card
        title="Đánh giá món ăn"
        style={{ background: "#fff", borderRadius: 8, marginTop: 24 }}
        >
        <List
            dataSource={reviews}
            renderItem={(review) => (
            <List.Item
                actions={
                currentUser && currentUser.id === review.userId
                    ? [
                        <Popconfirm
                        title="Xóa bình luận?"
                        onConfirm={() => handleDeleteReview(review.id)}
                        >
                        <Button icon={<DeleteOutlined />} type="text" danger />
                        </Popconfirm>,
                        <Button
                        icon={<EditOutlined />}
                        type="text"
                        onClick={() => startEditReview(review)}
                        />,
                    ]
                    : []
                }
            >
                <List.Item.Meta
                title={
                    <div>
                    <b>{review.userFullname}</b>{" "}
                    <Rate disabled value={review.rating} style={{ fontSize: 14 }} />
                    </div>
                }
                description={
                    editingReview && editingReview.id === review.id ? (
                    <>
                        <Rate
                        value={editingReview.newRating}
                        onChange={(val) =>
                            setEditingReview((prev) => ({ ...prev, newRating: val }))
                        }
                        />
                        <TextArea
                        rows={3}
                        value={editingReview.newComment}
                        onChange={(e) =>
                            setEditingReview((prev) => ({
                            ...prev,
                            newComment: e.target.value,
                            }))
                        }
                        style={{ marginTop: 10 }}
                        />
                        <Upload
                        beforeUpload={(file) => {
                            setEditingReview((prev) => ({ ...prev, newImageFile: file }));
                            return false;
                        }}
                        maxCount={1}
                        style={{ marginTop: 10 }}
                        >
                        <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
                        </Upload>
                        <div style={{ marginTop: 10 }}>
                        <Button type="primary" onClick={handleEditReview}>
                            Lưu thay đổi
                        </Button>
                        <Button
                            style={{ marginLeft: 8 }}
                            onClick={() => setEditingReview(null)}
                        >
                            Hủy
                        </Button>
                        </div>
                    </>
                    ) : (
                    <>
                        <p>{review.comment}</p>
                        {review.imgUrl && (
                        <img
                            src={review.imgUrl}
                            alt="review"
                            style={{ maxWidth: 200, marginTop: 8, borderRadius: 6 }}
                        />
                        )}
                    </>
                    )
                }
                />
            </List.Item>
            )}
        />
        </Card>

        {/* Form thêm review */}
        <Card
        title="Viết đánh giá của bạn"
        style={{ background: "#fff", borderRadius: 8, marginTop: 24, padding: 16 }}
        >
        <Rate value={newRating} onChange={setNewRating} />
        <TextArea
            rows={3}
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="Viết bình luận..."
            style={{ marginTop: 10 }}
        />
        <Upload
            beforeUpload={(file) => {
            setImageFile(file);
            return false;
            }}
            maxCount={1}
            style={{ marginTop: 10 }}
        >
            <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
        </Upload>
        <Button
            type="primary"
            onClick={handleAddReview}
            style={{ marginTop: 10 }}
        >
            Gửi bình luận
        </Button>
      </Card>
    </div>
  );
};

export default FoodDetailPage;
