import { useState } from "react";
import { Button, Input, Rate, Upload, message } from "antd";
import { UploadOutlined } from "@ant-design/icons";
import api from "../api/axios";

const { TextArea } = Input;

const AddReviewForm = ({ restaurantId, currentUser, onReviewAdded }) => {
  const [comment, setComment] = useState("");
  const [rating, setRating] = useState(5);
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleAddReview = async () => {
    console.log("currentUser:", currentUser);

    if (!currentUser) return message.warning("Bạn cần đăng nhập để bình luận!");
    if (!comment.trim()) return message.warning("Nội dung bình luận không được để trống");

    try {
      setLoading(true);
      const formData = new FormData();
      formData.append("comment", comment);
      formData.append("rating", rating);
      formData.append("restaurantId", restaurantId);
      formData.append("userId", currentUser.id);
      if (image) formData.append("image", image);

      const res = await api.post("/restaurant-reviews", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      onReviewAdded(res.data); // gọi callback để update list reviews
      setComment("");
      setImage(null);
      message.success("Thêm bình luận thành công!");
    } catch (err) {
      console.error(err);
      message.error("Thêm bình luận thất bại!");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ marginBottom: 16 }}>
      <Rate value={rating} onChange={setRating} />
      <TextArea
        rows={3}
        placeholder="Viết bình luận..."
        value={comment}
        onChange={(e) => setComment(e.target.value)}
        style={{ marginTop: 8 }}
      />
      <Upload
        beforeUpload={(file) => {
          setImage(file);
          return false; // không upload ngay
        }}
        onRemove={() => setImage(null)}
        maxCount={1}
        style={{ marginTop: 8 }}
      >
        <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
      </Upload>
      <Button
        type="primary"
        loading={loading}
        style={{ marginTop: 8 }}
        onClick={handleAddReview}
      >
        Bình luận
      </Button>
    </div>
  );
};

export default AddReviewForm;
