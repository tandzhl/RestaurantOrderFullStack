import { useEffect, useState } from "react";
import { Pagination, Card, Rate, Row, Col, Button, message } from "antd";
import { useNavigate } from "react-router-dom";  // ✅ thêm dòng này
import api from "../api/axios";
import "../css/HomePage.css";
import { toUniqueSlug } from "../utils/slug";

const { Meta } = Card;

function HomePage({ searchTerm }) {
  const [foods, setFoods] = useState([]);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(12);
  const [total, setTotal] = useState(0);
  const navigate = useNavigate(); // ✅ khởi tạo navigate

  useEffect(() => {
    const fetchFoods = async () => {
      try {
        const url = searchTerm
          ? `/food-items?name=${encodeURIComponent(searchTerm)}&page=${page - 1}&size=${pageSize}`
          : `/food-items?page=${page - 1}&size=${pageSize}`;
        const res = await api.get(url);
        setFoods(res.data.content || []);
        setTotal(res.data.totalElements || 0);
      } catch (err) {
        console.error(err);
        setFoods([]);
        setTotal(0);
      }
    };
    fetchFoods();
  }, [page, pageSize, searchTerm]);

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
      restaurantId: item.restaurantId, // 👈 cái này luôn undefined
      quantity: 1,
    });
    }
    localStorage.setItem("cart", JSON.stringify(cart));
    message.success(`${item.name} đã được thêm vào giỏ hàng!`);
  };

  // Xem chi tiết
  const viewDetail = (food) => {
    const slug = `${toUniqueSlug(food.name)}-${food.id}`;
    navigate(`/food/${slug}`); // ✅ điều hướng đến FoodDetailPage
  };

  return (
    <div className="home-container">
      {foods.length === 0 ? (
        <p className="no-result">Không tìm thấy món ăn nào</p>
      ) : (
        <>
          <Row gutter={[24, 24]}>
            {foods.map((food) => (
              <Col xs={24} sm={12} md={8} lg={6} key={food.id}>
                <Card
                  hoverable
                  cover={
                    <img
                      alt={food.name}
                      src={food.imageUrl}
                      style={{ height: 180, objectFit: "cover" }}
                    />
                  }
                  actions={[
                    <div style={{ display: "flex", gap: 8, justifyContent: "center" }}>
                      <Button type="primary" onClick={() => addToCart(food)}>
                        Thêm vào giỏ hàng
                      </Button>
                      <Button onClick={() => viewDetail(food)}>Xem chi tiết</Button>
                    </div>,
                  ]}
                >
                  <Meta
                    title={food.name}
                    description={
                      <>
                        <p>{food.description}</p>
                        <p style={{ fontWeight: "bold" }}>
                          {food.price.toLocaleString("vi-VN")} đ
                        </p>
                        <Rate disabled value={food.averageRating} style={{ fontSize: 14 }} />
                      </>
                    }
                  />
                </Card>
              </Col>
            ))}
          </Row>

          <div className="pagination-container" style={{ marginTop: 24, textAlign: "center" }}>
            <Pagination
              current={page}
              pageSize={pageSize}
              total={total}
              onChange={(p, s) => {
                setPage(p);
                setPageSize(s);
              }}
            />
          </div>
        </>
      )}
    </div>
  );
}

export default HomePage;
