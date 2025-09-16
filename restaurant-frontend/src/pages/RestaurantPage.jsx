import { useEffect, useState } from "react";
import { Card, Row, Col, Spin, message, Button } from "antd";
import { EnvironmentOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import { toUniqueSlug } from "../utils/slug";

const RestaurantPage = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [follows, setFollows] = useState([]); 
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  // load restaurants + follow list
  useEffect(() => {
    const fetchData = async () => {
      try {
        // lấy danh sách nhà hàng
        const res = await api.get("/restaurants");
        const dataWithSlug = res.data.map((r) => ({
          ...r,
          slug: `${toUniqueSlug(r.name)}-${r.id}`,
        }));
        setRestaurants(dataWithSlug);

        // lấy danh sách follow
        const followRes = await api.get("/follow/my-follows");
        const followedIds = followRes.data.map((f) => f.restaurantId);
        setFollows(followedIds);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleViewDetails = (slug) => {
    navigate(`/restaurants/${slug}`);
  };

  const handleFollowToggle = async (restaurantId) => {
    try {
      if (follows.includes(restaurantId)) {
        // đang follow → hủy
        await api.delete(`/follow/${restaurantId}`);
        setFollows(follows.filter((id) => id !== restaurantId));
        message.success("Đã hủy theo dõi!");
      } else {
        // chưa follow → follow
        await api.post(`/follow/${restaurantId}`);
        setFollows([...follows, restaurantId]);
        message.success("Đã theo dõi!");
      }
    } catch (err) {
      console.error(err);
      message.warning("Cần đăng nhập để theo dõi nhà hàng");
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: "center", marginTop: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ padding: 24 }}>
      <h2 style={{ marginBottom: 24 }}>Danh sách nhà hàng</h2>
      <Row gutter={[24, 24]}>
        {restaurants.map((r) => (
          <Col xs={24} sm={12} md={8} lg={6} key={r.id}>
            <Card
              hoverable
              cover={
                <img
                  alt={r.name}
                  src={r.imageUrl}
                  style={{ height: 180, objectFit: "cover" }}
                />
              }
            >
              <h3>{r.name}</h3>
              <p>
                <EnvironmentOutlined /> {r.address}
              </p>
              <Button
                type="primary"
                onClick={() => handleViewDetails(r.slug)}
                style={{ marginRight: 8 }}
              >
                Xem chi tiết
              </Button>
              <Button
                danger={follows.includes(r.id)} // màu đỏ khi đang follow
                onClick={() => handleFollowToggle(r.id)}
              >
                {follows.includes(r.id) ? "Hủy theo dõi" : "Theo dõi"}
              </Button>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
};

export default RestaurantPage;
